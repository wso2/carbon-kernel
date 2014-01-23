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

import org.apache.axiom.attachments.impl.BufferUtils;
import org.apache.axiom.attachments.utils.BAAInputStream;
import org.apache.axiom.attachments.utils.BAAOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * FilterInputStream that delegates to the original 
 * InputStream.  When detach() is called, the original
 * InputStream is consumed, and closed. The unread bytes are
 * stored in a local stream.
 * Subsequent requests are serviced by the local stream.
 * 
 * Rationale: The detached stream can be closed and its
 * resources freed, but the consumer of the stream can 
 * continue to receive data.
 * 
 * Cons: If there are a lot of bytes remaining, these are
 * buffered.  Currently they are buffered incore (this
 * could be improved to buffer in a file).
 * 
 */
public class DetachableInputStream extends FilterInputStream {

    private static Log log = LogFactory.getLog(DetachableInputStream.class);
    

    private long count = 0;
    BAAInputStream localStream = null;
    boolean isClosed = false;
    public DetachableInputStream(InputStream in) {
        super(in);
        count = 0;
    }

    /**
     * @return count of bytes read
     */
    public long length() throws IOException {
        if (localStream == null) {
            detach();
        }
        return count;
        
    }
    
    /**
     * Consume the input stream and close it.
     * The bytes in the input stream are buffered.
     * (Thus the original input stream can release its 
     * resources, but the consumer of the stream can
     * still receive data).
     * @throws IOException
     */
    public void detach() throws IOException {
        if (localStream == null && !isClosed) {

            BAAOutputStream baaos = new BAAOutputStream();
            try {
                // It is possible that the underlying stream was closed
                BufferUtils.inputStream2OutputStream(in, baaos);
                super.close();
            } catch (Throwable t) {
                if (log.isDebugEnabled()) {
                    log.debug("detach caught exception.  Processing continues:" 
                              + t);
                    log.debug("  " + stackToString(t));
                }
            } finally {
                in = null; // GC the incoming stream
            }
            
            localStream = new BAAInputStream(baaos.buffers(), baaos.length());
            if (log.isDebugEnabled()) {
                log.debug("The local stream built from the detached " +
                                "stream has a length of:" + baaos.length());
            }
            count = count + baaos.length();
        }
    }

    public int available() throws IOException {
        if (localStream != null) {
            return localStream.available();
        } else {
            return super.available();
        }
    }

    public void close() throws IOException {
        isClosed = true;
        if (localStream != null) {
            localStream.close();
        } else {
            super.close();
        }
    }

    public boolean markSupported() {
        // Mark is not supported because stream can
        // switch
        return false;
    }
    
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

    public int read() throws IOException {
        if (localStream == null) {
            int rc = super.read();
            if (rc != -1) {
                count++;
            }
            return rc;
        } else {
            return localStream.read();
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (localStream == null) {
            int rc = super.read(b, off, len);
            if (rc > 0) {
                count += rc;
            }
            return rc;
        } else {
            return localStream.read(b, off, len);
        }
    }

    public int read(byte[] b) throws IOException {
        if (localStream == null) {
            int rc =  super.read(b);
            if (rc > 0) {
                count += rc;
            }
            return rc;
        } else {
            return localStream.read(b);
        }
    }

    public synchronized void reset() throws IOException {
        throw new IOException();
    }

    public long skip(long n) throws IOException {
        if (localStream == null) {
            long rc = super.skip(n);
            if (rc > 0) {
                count += rc;
            }
            return rc;
        } else {
            return localStream.skip(n);
        }
    }

    private static String stackToString(Throwable e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.BufferedWriter bw = new java.io.BufferedWriter(sw);
        java.io.PrintWriter pw = new java.io.PrintWriter(bw);
        e.printStackTrace(pw);
        pw.close();
        String text = sw.getBuffer().toString();   
        return text;
    }


}
