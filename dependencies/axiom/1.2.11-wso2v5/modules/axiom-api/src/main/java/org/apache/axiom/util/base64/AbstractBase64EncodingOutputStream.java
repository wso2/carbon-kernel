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

package org.apache.axiom.util.base64;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Base class for {@link OutputStream} implementations that encode data in base64.
 */
public abstract class AbstractBase64EncodingOutputStream extends OutputStream {
    private final byte[] in = new byte[3];
    private final byte[] out = new byte[4];
    private int rest; // Number of bytes remaining in the inBuffer
    private boolean completed;

    public final void write(byte[] b, int off, int len) throws IOException {
        if (completed) {
            throw new IOException("Attempt to write data after base64 encoding has been completed");
        }
        if (rest > 0) {
            while (len > 0 && rest < 3) {
                in[rest++] = b[off++];
                len--;
            }
            if (rest == 3) {
                encode(in, 0, 3);
                rest = 0;
            }
        }
        while (len >= 3) {
            encode(b, off, 3);
            off += 3;
            len -= 3;
        }
        while (len > 0) {
            in[rest++] = b[off++];
            len--;
        }
    }

    public final void write(int b) throws IOException {
        in[rest++] = (byte)b;
        if (rest == 3) {
            encode(in, 0, 3);
            rest = 0;
        }
    }

    /**
     * Write out any pending data, including padding if necessary.
     * 
     * @throws IOException if an I/O error occurs
     */
    public final void complete() throws IOException {
        if (!completed) {
            if (rest > 0) {
                encode(in, 0, rest);
            }
            flushBuffer();
            completed = true;
        }
    }
    
    private void encode(byte[] data, int off, int len) throws IOException {
        if (len == 1) {
            int i = data[off] & 0xff;
            out[0] = Base64Constants.S_BASE64CHAR[i >> 2];
            out[1] = Base64Constants.S_BASE64CHAR[(i << 4) & 0x3f];
            out[2] = Base64Constants.S_BASE64PAD;
            out[3] = Base64Constants.S_BASE64PAD;
        } else if (len == 2) {
            int i = ((data[off] & 0xff) << 8) + (data[off + 1] & 0xff);
            out[0] = Base64Constants.S_BASE64CHAR[i >> 10];
            out[1] = Base64Constants.S_BASE64CHAR[(i >> 4) & 0x3f];
            out[2] = Base64Constants.S_BASE64CHAR[(i << 2) & 0x3f];
            out[3] = Base64Constants.S_BASE64PAD;
        } else {
            int i = ((data[off] & 0xff) << 16)
                    + ((data[off + 1] & 0xff) << 8)
                    + (data[off + 2] & 0xff);
            out[0] = Base64Constants.S_BASE64CHAR[i >> 18];
            out[1] = Base64Constants.S_BASE64CHAR[(i >> 12) & 0x3f];
            out[2] = Base64Constants.S_BASE64CHAR[(i >> 6) & 0x3f];
            out[3] = Base64Constants.S_BASE64CHAR[i & 0x3f];
        }
        doWrite(out);
    }

    public final void flush() throws IOException {
        flushBuffer();
        doFlush();
    }

    public final void close() throws IOException {
        complete();
        doClose();
    }
    
    /**
     * Write base64 encoded data. If necessary, the implementation should accumulate
     * the data in a buffer before writing it to the underlying stream.
     * 
     * @param b a byte array of length 4
     * @throws IOException if an I/O error occurs
     */
    protected abstract void doWrite(byte[] b) throws IOException;
    
    /**
     * Write any pending data to the underlying stream, if applicable.
     * Note that implementations should not flush the underlying stream.
     * 
     * @throws IOException if an I/O error occurs
     */
    protected abstract void flushBuffer() throws IOException;
    
    /**
     * Flush the underlying stream, if applicable.
     * 
     * @throws IOException if an I/O error occurs
     */
    protected abstract void doFlush() throws IOException;
    
    /**
     * Close the underlying stream, if applicable.
     * 
     * @throws IOException if an I/O error occurs
     */
    protected abstract void doClose() throws IOException;
}
