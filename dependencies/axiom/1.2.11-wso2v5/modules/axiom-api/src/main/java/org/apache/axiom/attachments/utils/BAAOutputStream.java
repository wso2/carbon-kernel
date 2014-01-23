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
package org.apache.axiom.attachments.utils;

import org.apache.axiom.attachments.impl.BufferUtils;
import org.apache.axiom.ext.io.ReadFromSupport;
import org.apache.axiom.ext.io.StreamCopyException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * BAAOutputStream is like a ByteArrayOutputStream.
 * A ByteArrayOutputStream stores the backing data in a byte[].
 * BAAOutputStream stores the backing data in a Array of 
 * byte[].  Using several non-contiguous chunks reduces 
 * memory copy and resizing.
 */
public class BAAOutputStream extends OutputStream implements ReadFromSupport {

    ArrayList data = new ArrayList();
    final static int BUFFER_SIZE = BufferUtils.BUFFER_LEN;
    int index = 0;
    byte[] currBuffer = null;
    public BAAOutputStream() {
        super();
        addBuffer();
    }

    private void addBuffer() {
        currBuffer = new byte[BUFFER_SIZE];
        data.add(currBuffer);
        index = 0;
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
       int total = 0;
       while (total < len) {
           int copy = Math.min(len-total, BUFFER_SIZE-index);
           System.arraycopy(b, off, currBuffer, index, copy);
           total += copy;
           index += copy;
           off += copy;
           if (index >= BUFFER_SIZE) {
               addBuffer();
           }
       }
    }

    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    
    byte[] writeByte = new byte[1];
    public void write(int b) throws IOException {
        writeByte[0] = (byte) b;
        this.write(writeByte, 0, 1);
    }

    public ArrayList buffers() {
        return data;
    }
    
    public int length() {
        return (BUFFER_SIZE * (data.size()-1)) + index;
    }
    
    /**
     * @param is InputStream containing data
     * @param maxRead the maximum number of bytes to receive
     * @return bytesReceived
     */
    public long receive(InputStream is, long maxRead) throws IOException {
        return readFrom(is, maxRead);
    }
    
    public long readFrom(InputStream is, long maxRead) throws StreamCopyException {
        if (maxRead == -1) {
            maxRead = Long.MAX_VALUE;
        }
        long bytesReceived = 0;
        
        // Now directly write to the buffers
        boolean done = false;
        while (!done) {
            
            // Don't get more than will fit in the current buffer
            int len = (int) Math.min(BUFFER_SIZE - index, maxRead-bytesReceived);
            
            // Now get the bytes
            int bytesRead;
            try {
                bytesRead = is.read(currBuffer, index, len);
            } catch (IOException ex) {
                throw new StreamCopyException(StreamCopyException.READ, ex);
            }
            if (bytesRead >= 0) {
                bytesReceived += bytesRead;
                index += bytesRead;
                if (index >= BUFFER_SIZE) {
                    addBuffer();
                }
                if (bytesReceived >= maxRead) {
                    done = true;
                }
            } else {
                done = true;
            }
        }
        
        return bytesReceived;
    }
}
