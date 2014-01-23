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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * BAAInputStream is like a ByteArrayInputStream.
 * A ByteArrayInputStream stores the backing data in a byte[].
 * BAAInputStream stores the backing data in a Array of 
 * byte[].  Using several non-contiguous chunks reduces 
 * memory copy and resizing.
 */
public class BAAInputStream extends InputStream {

    ArrayList data = new ArrayList();
    final static int BUFFER_SIZE = BufferUtils.BUFFER_LEN;
    int i;
    int size;
    int currIndex;
    int totalIndex;
    int mark = 0;
    byte[] currBuffer = null;
    byte[] read_byte = new byte[1];
    
    public BAAInputStream(ArrayList data, int size) {
        this.data = data;
        this.size = size;
        i = 0;
        currIndex = 0;
        totalIndex = 0;
        currBuffer = (byte[]) data.get(0);
    }

    public int read() throws IOException {
        int read = read(read_byte);

        if (read < 0) {
            return -1;
        } else {
            return read_byte[0] & 0xFF;
        }
    }

    public int available() throws IOException {
        return size - totalIndex;
    }


    public synchronized void mark(int readlimit) {
        mark = totalIndex;
    }

    public boolean markSupported() {
        return true;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int total = 0;
        if (totalIndex >= size) {
            return -1;
        }
        while (total < len && totalIndex < size) {
            int copy = Math.min(len - total, BUFFER_SIZE - currIndex);
            copy = Math.min(copy, size - totalIndex);
            System.arraycopy(currBuffer, currIndex, b, off, copy);
            total += copy;
            currIndex += copy;
            totalIndex += copy;
            off += copy;
            if (currIndex >= BUFFER_SIZE) {
                if (i+1 < data.size()) {
                    currBuffer = (byte[]) data.get(i+1);
                    i++;
                    currIndex = 0;
                } else {
                    currBuffer = null;
                    currIndex = BUFFER_SIZE;
                } 
            }
        }
        return total;
    }

    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    public synchronized void reset() throws IOException {
        i = mark / BUFFER_SIZE;
        currIndex = mark - (i * BUFFER_SIZE);
        currBuffer = (byte[]) data.get(i);
        totalIndex = mark;
    }

    /**
     * Write all of the buffers to the output stream
     * @param os
     * @throws IOException
     */
    public void writeTo(OutputStream os) throws IOException {
        
        if (data != null) {
            int numBuffers = data.size();
            for (int j = 0; j < numBuffers-1; j ++) {
                os.write( (byte[]) data.get(j), 0, BUFFER_SIZE);
            }
            if (numBuffers > 0) {
                int writeLimit = size - ((numBuffers-1) * BUFFER_SIZE);
                os.write( (byte[]) data.get(numBuffers-1), 0, writeLimit);
            }
        }
    }
}
