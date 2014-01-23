/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axiom.attachments.impl;

import org.apache.axiom.attachments.MIMEBodyPartInputStream;
import org.apache.axiom.attachments.Part;
import org.apache.axiom.attachments.lifecycle.LifecycleManager;
import org.apache.axiom.attachments.utils.BAAInputStream;
import org.apache.axiom.attachments.utils.BAAOutputStream;
import org.apache.axiom.om.OMException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.Header;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

/**
 * The PartFactory creates an object that represents a Part
 * (implements the Part interface).  There are different ways
 * to represent a part (backing file or backing array etc.).
 * These different implementations should not be exposed to the 
 * other layers of the code.  The PartFactory helps maintain this
 * abstraction, and makes it easier to add new implementations.
 */
public class PartFactory {
    
    private static int inflight = 0;  // How many attachments are currently being built.
    private static String semifore = "PartFactory.semifore";
    
    private static Log log = LogFactory.getLog(PartFactory.class);
    
    // Maximum number of threads allowed through createPart
    private static int INFLIGHT_MAX = 4;
    
    // Constants for dynamic threshold 
    // Dynamic Threshold = availMemory / THRESHOLD_FACTOR
    private static final int THRESHOLD_FACTOR = 5;
    
    /**
     * Creates a part from the input stream.
     * The remaining parameters are used to determine if the
     * part should be represented in memory (byte buffers) or
     * backed by a file.
     * 
     * @param in MIMEBodyPartInputStream
     * @param isSOAPPart
     * @param thresholdSize
     * @param attachmentDir
     * @param messageContentLength
     * @return Part
     * @throws OMException if any exception is encountered while processing.
     */
    public static Part createPart(LifecycleManager manager, MIMEBodyPartInputStream in,
                    boolean isSOAPPart,
                    int thresholdSize,
                    String attachmentDir,
                    int messageContentLength
                    ) throws OMException {
        if(log.isDebugEnabled()){
            log.debug("Start createPart()");
            log.debug("  isSOAPPart=" + isSOAPPart);
            log.debug("  thresholdSize= " + thresholdSize);
            log.debug("  attachmentDir=" + attachmentDir);
            log.debug("  messageContentLength " + messageContentLength);
        }
        
        try {
            // Read enough of the InputStream to build the headers 
            // The readHeaders returns some extra bits that were read, but are part
            // of the data section.
            Hashtable headers = new Hashtable();
            InputStream dross = readHeaders(in, headers);
            
            Part part;
            try {
                
                // Message throughput is increased if the number of threads in this
                // section is limited to INFLIGHT_MAX.  Allowing more threads tends to cause
                // thrashing while reading from the HTTP InputStream.  
                // Allowing fewer threads reduces the thrashing.  And when the remaining threads
                // are notified their input (chunked) data is available.
                // 
                // Note: SOAPParts are at the beginning of the message and much smaller than attachments,
                // so don't wait on soap parts.
                if (!isSOAPPart) {
                    synchronized(semifore) {
                        if (inflight >= INFLIGHT_MAX) {
                            semifore.wait();
                        }
                        inflight++;
                    }
                }
                // Get new threshold based on the current available memory in the runtime.
                // We only use the thresholds for non-soap parts.
                if (!isSOAPPart && thresholdSize > 0) {     
                    thresholdSize = getRuntimeThreshold(thresholdSize, inflight);
                }

                
                if (isSOAPPart ||
                        thresholdSize <= 0 ||  
                        (messageContentLength > 0 && 
                                messageContentLength < thresholdSize)) {
                    // If the entire message is less than the threshold size, 
                    // keep it in memory.
                    // If this is a SOAPPart, keep it in memory.

                    // Get the bytes of the data without a lot 
                    // of resizing and GC.  The BAAOutputStream 
                    // keeps the data in non-contiguous byte buffers.
                    BAAOutputStream baaos = new BAAOutputStream();
                    BufferUtils.inputStream2OutputStream(dross, baaos);
                    BufferUtils.inputStream2OutputStream(in, baaos);
                    part = new PartOnMemoryEnhanced(headers, baaos.buffers(), baaos.length());
                } else {
                    // We need to read the input stream to determine whether
                    // the size is bigger or smaller than the threshold.
                    BAAOutputStream baaos = new BAAOutputStream();
                    int t1 = BufferUtils.inputStream2OutputStream(dross, baaos, thresholdSize);
                    int t2 =  BufferUtils.inputStream2OutputStream(in, baaos, thresholdSize - t1);
                    int total = t1 + t2;

                    if (total < thresholdSize) {
                        return new PartOnMemoryEnhanced(headers, baaos.buffers(), baaos.length());
                    } else {
                        // A BAAInputStream is an input stream over a list of non-contiguous 4K buffers.
                        BAAInputStream baais = 
                            new BAAInputStream(baaos.buffers(), baaos.length());

                        part = new PartOnFile(manager, headers, 
                                              baais,
                                              in, 
                                              attachmentDir);
                    }

                } 
            } finally {
                if (!isSOAPPart) {
                    synchronized(semifore) {
                        semifore.notify();
                        inflight--;
                    }
                }
            }

            return part;
            
        } catch (Exception e) {
            throw new OMException(e);
        } 
    }
    
    /**
     * The implementing class must call initHeaders prior to using
     * any of the Part methods.  
     * @param is
     * @param headers
     */
    private static InputStream readHeaders(InputStream in, Map headers) throws IOException {
        if(log.isDebugEnabled()){
            log.debug("initHeaders");
        }
        boolean done = false;
        
        
        final int BUF_SIZE = 1024;
        byte[] headerBytes = new byte[BUF_SIZE];
        
        int size = in.read(headerBytes);
        int index = 0;
        StringBuffer sb = new StringBuffer(50);
        
        while (!done && index < size) {
            
            // Get the next byte
            int ch = headerBytes[index];
            index++;
            if (index == size) {
                size = in.read(headerBytes);
                index =0;
            }
                
            if (ch == 13) {
                
                // Get the next byte
                ch = headerBytes[index];
                index++;
                if (index == size) {
                    size = in.read(headerBytes);
                    index =0;
                }
                
                if (ch == 10) {
                    // 13, 10 indicates we are starting a new line...thus a new header
                    // Get the next byte
                    ch = headerBytes[index];
                    index++;
                    if (index == size) {
                        size = in.read(headerBytes);
                        index =0;
                    }
                    
                    if (ch == 13) {
                        
                        // Get the next byte
                        ch = headerBytes[index];
                        index++;
                        if (index == size) {
                            size = in.read(headerBytes);
                            index =0;
                        }
                        
                        if (ch == 10) {
                            // Blank line indicates we are done.
                            readHeader(sb, headers);
                            sb.delete(0, sb.length()); // Clear the buffer for reuse
                            done = true;
                        } 
                    } else {
                        
                        // Semicolon is a continuation character
                        String check = sb.toString().trim();
                        if (!check.endsWith(";")) {
                            // now parse and add the header String
                            readHeader(sb, headers);
                            sb.delete(0, sb.length()); // Clear the buffer for reuse
                        }
                        sb.append((char) ch);
                    }
                } else {
                    sb.append(13);
                    sb.append((char) ch);
                }
            } else {
                sb.append((char) ch);
            }
        }
        if(log.isDebugEnabled()){
            log.debug("End initHeaders");
        }
        
        // Return an input stream containing the dross bits
        if (index >= size) {
            index = size;
        }
        ByteArrayInputStream dross = new ByteArrayInputStream(headerBytes, index, size-index);
        return dross;
    }
    
    
    /**
     * Parse the header into a name and value pair.
     * Add the name value pair to the map.
     * @param header StringBuffer
     * @param headers Map
     */
    private static void readHeader(StringBuffer header, Map headers) {
        int delimiter = header.indexOf(":");
        String name = header.substring(0, delimiter).trim();
        String value = header.substring(delimiter + 1, header.length()).trim();
        
        if (log.isDebugEnabled()){
            log.debug("addHeader: (" + name + ") value=(" + value +")");
        }
        Header headerObj = new Header(name, value);
        
        // Use the lower case name as the key
        String key = name.toLowerCase();
        headers.put(key, headerObj);
    }
    
    /**
     * This method checks the configured threshold and
     * the current runtime information.  If it appears that we could
     * run out of memory, the threshold is reduced.
     * 
     * This method allows the user to request a much larger threshold without 
     * fear of running out of memory.  Using a larger in memory threshold generally 
     * results in better throughput.
     * 
     * @param configThreshold
     * @param inflight
     * @return threshold
     */
    private static int getRuntimeThreshold(int configThreshold, int inflight) {
        
        // Determine how much free memory is available
        Runtime r = Runtime.getRuntime();
        long totalmem = r.totalMemory();
        long maxmem = r.maxMemory();
        long freemem = r.freeMemory();
        
        // @REVIEW
        // If maximum is not defined...limit to 1G
        if (maxmem == java.lang.Long.MAX_VALUE) {
            maxmem = 1024*1024*1024; 
        }
        
        long availmem = maxmem - (totalmem - freemem);
        
       
        // Now determine the dynamic threshold
        int dynamicThreshold = (int) availmem / (THRESHOLD_FACTOR * inflight);
        
        // If it appears that we might run out of memory with this
        // threshold, reduce the threshold size.
        if (dynamicThreshold < configThreshold) {
            if (log.isDebugEnabled()) {
                log.debug("Using Runtime Attachment File Threshold " + dynamicThreshold);
                log.debug("maxmem   = " + maxmem);
                log.debug("totalmem = " + totalmem);
                log.debug("freemem  = " + freemem);
                log.debug("availmem = " + availmem);
            }
            
        } else {
            dynamicThreshold = configThreshold;
            if (log.isDebugEnabled()) {
                log.debug("Using Configured Attachment File Threshold " + configThreshold);
                log.debug("maxmem   = " + maxmem);
                log.debug("totalmem = " + totalmem);
                log.debug("freemem  = " + freemem);
                log.debug("availmem = " + availmem);
            }
        }
        return dynamicThreshold;
    }
    
    /**
     * A normal ByteArrayOutputStream, except that it returns the buffer
     * directly instead of returning a copy of the buffer.
     */
    static class BAOS extends ByteArrayOutputStream {

        /**
         * Create a BAOS with a decent sized buffer
         */
        public BAOS() {
            super(16 * 1024);
        }

        public byte[] toByteArray() {
            return buf;
        }
        
    }
}
