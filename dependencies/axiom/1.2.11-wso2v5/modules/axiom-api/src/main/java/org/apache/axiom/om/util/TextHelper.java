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

package org.apache.axiom.om.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.mail.MessagingException;

import org.apache.axiom.attachments.impl.BufferUtils;
import org.apache.axiom.attachments.lifecycle.LifecycleManager;
import org.apache.axiom.attachments.lifecycle.impl.FileAccessor;
import org.apache.axiom.attachments.lifecycle.impl.LifecycleManagerImpl;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.apache.axiom.util.base64.Base64Utils;

public class TextHelper {
    
    private static int DEFAULT_FILE_THRESHOLD = 100 * 1024;
    private static String DEFAULT_ATTACHMENT_DIR = "axiomTmp";
    private static int DELETE_TIME = 60 * 60; // 1 Hour
    
    /**
     * @param inStream InputStream
     * @return Base64 encoded string representint the data in inStream
     * @throws IOException
     */
    public static String toString(InputStream inStream) throws IOException {
        StringBuffer buffer = new StringBuffer();
        toStringBuffer(inStream, buffer);
        return buffer.toString();
    }
    
    /**
     * Append Base64 encoding of the data in the inStream to the specified buffer
     * @param inStream InputStream
     * @param buffer Buffer
     * @throws IOException
     */
    public static void toStringBuffer(InputStream inStream, StringBuffer buffer) throws IOException {
        int avail = inStream.available();
        
        // The Base64 will increase the size by 1.33 + some additional 
        // space at the data byte[] boundaries.  So a factor of 1.35 is used
        // to ensure capacity.
        if (avail > 0) {
            buffer.ensureCapacity((int) (avail* 1.35) + buffer.length());
        }
        
        // The size of the buffer must be a multiple of 3. Otherwise usage of the
        // stateless Base64 class would produce filler characters inside the Base64
        // encoded text.
        byte[] data = new byte[1023];
        boolean eos = false;
        do {
            int len = 0;
            do {
                // Always fill the buffer entirely (unless the end of the stream has
                // been reached); see above.
                int read = inStream.read(data, len, data.length-len);
                if (read == -1) {
                    eos = true;
                    break;
                }
                len += read;
            } while (len < data.length);
            Base64Utils.encode(data, 0, len, buffer);
        } while (!eos);
    }
    
    /**
     * Append data in the omText to the specified buffer
     * @param omText the text node to get the character data from
     * @param buffer Buffer
     * @throws IOException
     */
    public static void toStringBuffer(OMText omText, StringBuffer buffer) throws IOException {
        // If an InputStream is present, stream the BASE64 text to the StreamBuffer
        if (omText.isOptimized()) {
           Object dh = omText.getDataHandler();
           if (dh instanceof DataHandler) {
               InputStream is = ((DataHandler) dh).getInputStream();
               if (is != null) {
                   toStringBuffer(is, buffer);
                   return;
               }
           }
        }
        
        // Otherwise append the text
        buffer.append(omText.getText());
        return;
    }
    
    
    /**
     * Create an OMText node from a byte array containing binary data
     * If the byte array is large and the optimize flag is set, then 
     * the data is stored in a temp file to reduce in-core memory
     * @param b
     * @param off
     * @param length
     * @param factory
     * @param isOptimize
     */
    public static OMText toOMText(byte[] b, int off, int length, 
                                  OMFactory factory, 
                                  boolean isOptimize) throws IOException, MessagingException {
        String attachmentDir = getAttachmentDir(factory);
        return toOMText(b, off, length, factory, isOptimize, attachmentDir);
    }
    
    /**
     * Create an OMText node from a byte array containing binary data
     * If the byte array is large and the optimize flag is set, then 
     * the data is stored in a temp file to reduce in-core memory
     * @param b
     * @param off
     * @param length
     * @param factory
     * @param isOptimize
     * @param attachmentDir
     */
    public static OMText toOMText(byte[] b, int off, int length, 
                                      OMFactory factory, 
                                      boolean isOptimize,
                                      String attachmentDir) throws IOException, MessagingException {
        OMText omText = null;
        if (isOptimize) {
            LifecycleManager lm = getLifecycleManager(factory);
            int threshold = getThreshold(factory);
            
            // TODO Consider lowering the threshold in low memory situations ?
            //threshold = lm.getRuntimeThreshold(threshold);
            
            if (length >= threshold && attachmentDir != null) {
                
                // Get the file accessor
                FileAccessor fileAccessor = lm.create(attachmentDir);
                OutputStream fos = fileAccessor.getOutputStream();
                
                //Copy the bytes into the file
                ByteArrayInputStream is = new ByteArrayInputStream(b, off, length);
                BufferUtils.inputStream2OutputStream(is, fos);
                fos.close();
                
                // Delete this temp file on exit
                lm.deleteOnExit(fileAccessor.getFile());
                lm.deleteOnTimeInterval(DELETE_TIME, fileAccessor.getFile());
                
                // Create the OMText node from the datahandler
                DataHandler dh = fileAccessor.getDataHandler(null);
                omText = factory.createOMText(dh, isOptimize);
            }
        }
        if (omText == null) {
            omText = factory.createOMText(Base64Utils.encode(b, off, length));
            omText.setOptimize(isOptimize);
        }
        return omText;
    }
   
    private static LifecycleManager getLifecycleManager(OMFactory factory) {
        LifecycleManager lm = null;
        
        /* TODO Support access to lifecycle manager from the factory
        if (factory.getProperty(LIFECYCLE_MANAGER)) {
            ...
        }
        */
        if (lm == null) {
            return new LifecycleManagerImpl();
        }
        return lm;
        
    }
    
    private static int getThreshold(OMFactory factory) {
       
        int threshold = DEFAULT_FILE_THRESHOLD;
        /* TODO Support access to threshold from the factory
        if (factory.getProperty(FILE_THRESHOLD)) {
            ...
        }
        */
        return threshold;
        
    }
    
    private static String getAttachmentDir(OMFactory factory) {
        
        String attachmentDir = DEFAULT_ATTACHMENT_DIR;
        /* TODO Support access to threshold from the factory
        if (factory.getProperty(FILE_THRESHOLD)) {
            ...
        }
        */
        return attachmentDir;
        
    }
    
}
