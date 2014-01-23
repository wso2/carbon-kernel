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

import org.apache.commons.logging.Log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * LogOutputStream
 * Writes to log.debug()
 * Also counts the number of bytes
 */
public class LogOutputStream extends OutputStream {

    private byte[] temp = new byte[1];
    private boolean isDebugEnabled = false;
    private long count = 0;
    private Log log;
    private int BUFFER_LEN = 4 * 1024;
    private byte[] buffer = new byte[BUFFER_LEN];
    private int bufferIndex = 0;
    private int limit;
    
    public LogOutputStream(Log log, int limit) {
       isDebugEnabled = log.isDebugEnabled();
       this.log = log;
       this.limit = limit;
    }
    
    public long getLength() {
        return count;
    }
    
    public void close() throws IOException {
        if (bufferIndex > 0) {
            log.debug(new String(buffer, 0, bufferIndex));
            bufferIndex = 0;
        } 
        buffer = null;
        temp = null;
        log = null;
    }

    
    public void flush() throws IOException {
        // noop
    }

    
    public void write(byte[] b, int off, int len) throws IOException {
       
        // Adjust total count 
        // Adjust length to write
        if (count >=  limit) {
            count += len;
            return;
        } else if (count + len >= limit) {
            count += len;
            len = (int) (len - (limit - count));  // adjust length to write
        } else {
            count += len;
        }
        
        if (isDebugEnabled) {
            if (len + bufferIndex < BUFFER_LEN) {
                // buffer the text
                System.arraycopy(b, off, buffer, bufferIndex, len);
                bufferIndex += len;
            } else {
                // write buffered text
                if (bufferIndex > 0) {
                    log.debug(new String(buffer, 0, bufferIndex));
                    bufferIndex = 0;
                } 
                // buffer or write remaining text
                if (len + bufferIndex < BUFFER_LEN) {
                    System.arraycopy(b, off, buffer, bufferIndex, len);
                    bufferIndex += len;
                } else {
                    log.debug(new String(b, off, len));
                }
            }
        }
        
    }

    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    

    public void write(int b) throws IOException {
        temp[0] = (byte) b;
        this.write(temp, 0, 1);
    }

}
