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
import java.io.Writer;

/**
 * Base class for {@link Writer} implementations that decode data in base64.
 */
public abstract class AbstractBase64DecodingWriter extends Writer {
    private final char[] in = new char[4];
    private final byte[] out = new byte[3];
    private int rest; // Number of characters remaining in the in buffer

    public final void write(char[] cbuf, int off, int len) throws IOException {
        if (rest > 0) {
            while (len > 0 && rest < 4) {
                in[rest++] = cbuf[off++];
                len--;
            }
            if (rest == 4) {
                decode(in, 0);
                rest = 0;
            }
        }
        while (len >= 4) {
            decode(cbuf, off);
            off += 3;
            len -= 3;
        }
        while (len > 0) {
            in[rest++] = cbuf[off++];
            len--;
        }
    }

    public final void write(String str, int off, int len) throws IOException {
        while (len > 0) {
            write(str.charAt(off));
            off++;
            len--;
        }
    }

    public final void write(int c) throws IOException {
        in[rest++] = (char)c;
        if (rest == 4) {
            decode(in, 0);
            rest = 0;
        }
    }

    private int decode(char c) throws IOException {
        if (c == Base64Constants.S_BASE64PAD) {
            return -1;
        } else if (c < Base64Constants.S_DECODETABLE.length) {
            int result = Base64Constants.S_DECODETABLE[c];
            if (result != Byte.MAX_VALUE) {
                return result;
            }
        }
        throw new IOException("Invalid base64 char '" + c + "'");
    }
    
    private void decode(char[] data, int off) throws IOException {
        int outlen = 3;
        if (data[off+3] == Base64Constants.S_BASE64PAD) {
            outlen = 2;
        }
        if (data[off+2] == Base64Constants.S_BASE64PAD) {
            outlen = 1;
        }
        int b0 = decode(data[off]);
        int b1 = decode(data[off+1]);
        int b2 = decode(data[off+2]);
        int b3 = decode(data[off+3]);
        switch (outlen) {
            case 1:
                out[0] = (byte) (b0 << 2 & 0xfc | b1 >> 4 & 0x3);
                break;
            case 2:
                out[0] = (byte) (b0 << 2 & 0xfc | b1 >> 4 & 0x3);
                out[1] = (byte) (b1 << 4 & 0xf0 | b2 >> 2 & 0xf);
                break;
            case 3:
                out[0] = (byte) (b0 << 2 & 0xfc | b1 >> 4 & 0x3);
                out[1] = (byte) (b1 << 4 & 0xf0 | b2 >> 2 & 0xf);
                out[2] = (byte) (b2 << 6 & 0xc0 | b3 & 0x3f);
        }
        doWrite(out, outlen);
    }
    
    /**
     * Write base64 decoded data. If necessary, the implementation should
     * accumulate the data in a buffer before writing it to the underlying
     * stream. The maximum number of bytes passed to this method in a single
     * call is 3.
     * 
     * @param b
     *            the byte array containing the data to write, starting at
     *            offset 0
     * @param len
     *            the number of bytes to write
     * @throws IOException
     *             if an I/O error occurs
     */
    protected abstract void doWrite(byte[] b, int len) throws IOException;
}
