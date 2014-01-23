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
import java.io.Writer;

import javax.activation.DataHandler;

import org.apache.axiom.util.activation.DataSourceUtils;

/**
 * Contains utility methods to work with base64 encoded data.
 */
public class Base64Utils {
    private static final char[] S_BASE64CHAR = { 'A', 'B', 'C', 'D', 'E', 'F',
        'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
        'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
        't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', '+', '/' };

    private static final char S_BASE64PAD = '=';
    
    private static final byte[] S_DECODETABLE = new byte[128];
    
    static {
        for (int i = 0; i < S_DECODETABLE.length; i++)
            S_DECODETABLE[i] = Byte.MAX_VALUE; // 127
        for (int i = 0; i < S_BASE64CHAR.length; i++)
            // 0 to 63
            S_DECODETABLE[S_BASE64CHAR[i]] = (byte) i;
    }
    
    private static int getEncodedSize(int unencodedSize) {
        return (unencodedSize+2) / 3 * 4;
    }
    
    /**
     * Get a base64 representation of the content of a given {@link DataHandler}.
     * This method will try to carry out the encoding operation in the most efficient way.
     * 
     * @param dh the data handler with the content to encode
     * @return the base64 encoded content
     * @throws IOException if an I/O error occurs when reading the content of the data handler
     */
    public static String encode(DataHandler dh) throws IOException {
        long size = DataSourceUtils.getSize(dh.getDataSource());
        StringBuffer buffer;
        if (size == -1) {
            // Use a reasonable default capacity (better than the default of 16).
            buffer = new StringBuffer(4096);
        } else if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("DataHandler is too large to encode to string");
        } else {
            buffer = new StringBuffer(getEncodedSize((int)size));
        }
        Base64EncodingStringBufferOutputStream out = new Base64EncodingStringBufferOutputStream(buffer);
        // Always prefer writeTo, because getInputStream will create a thread and a pipe if
        // the DataHandler was constructed using an object instead of a DataSource
        dh.writeTo(out);
        out.complete();
        return buffer.toString();
    }

    private static int decode0(char[] ibuf, byte[] obuf, int wp) {
        int outlen = 3;
        if (ibuf[3] == S_BASE64PAD)
            outlen = 2;
        if (ibuf[2] == S_BASE64PAD)
            outlen = 1;
        int b0 = S_DECODETABLE[ibuf[0]];
        int b1 = S_DECODETABLE[ibuf[1]];
        int b2 = S_DECODETABLE[ibuf[2]];
        int b3 = S_DECODETABLE[ibuf[3]];
        switch (outlen) {
            case 1:
                obuf[wp] = (byte) (b0 << 2 & 0xfc | b1 >> 4 & 0x3);
                return 1;
            case 2:
                obuf[wp++] = (byte) (b0 << 2 & 0xfc | b1 >> 4 & 0x3);
                obuf[wp] = (byte) (b1 << 4 & 0xf0 | b2 >> 2 & 0xf);
                return 2;
            case 3:
                obuf[wp++] = (byte) (b0 << 2 & 0xfc | b1 >> 4 & 0x3);
                obuf[wp++] = (byte) (b1 << 4 & 0xf0 | b2 >> 2 & 0xf);
                obuf[wp] = (byte) (b2 << 6 & 0xc0 | b3 & 0x3f);
                return 3;
            default:
                throw new RuntimeException("internalError00");
        }
    }

    /**
     *
     */
    public static byte[] decode(char[] data, int off, int len) {
        char[] ibuf = new char[4];
        int ibufcount = 0;
        byte[] obuf = new byte[len / 4 * 3 + 3];
        int obufcount = 0;
        for (int i = off; i < off + len; i++) {
            char ch = data[i];
            if (ch == S_BASE64PAD || ch < S_DECODETABLE.length
                    && S_DECODETABLE[ch] != Byte.MAX_VALUE) {
                ibuf[ibufcount++] = ch;
                if (ibufcount == ibuf.length) {
                    ibufcount = 0;
                    obufcount += decode0(ibuf, obuf, obufcount);
                }
            }
        }
        if (obufcount == obuf.length)
            return obuf;
        byte[] ret = new byte[obufcount];
        System.arraycopy(obuf, 0, ret, 0, obufcount);
        return ret;
    }

    /**
     *
     */
    public static byte[] decode(String data) {
        char[] ibuf = new char[4];
        int ibufcount = 0;
        byte[] obuf = new byte[data.length() / 4 * 3 + 3];
        int obufcount = 0;
        for (int i = 0; i < data.length(); i++) {
            char ch = data.charAt(i);
            if (ch == S_BASE64PAD || ch < S_DECODETABLE.length
                    && S_DECODETABLE[ch] != Byte.MAX_VALUE) {
                ibuf[ibufcount++] = ch;
                if (ibufcount == ibuf.length) {
                    ibufcount = 0;
                    obufcount += decode0(ibuf, obuf, obufcount);
                }
            }
        }
        if (obufcount == obuf.length)
            return obuf;
        byte[] ret = new byte[obufcount];
        System.arraycopy(obuf, 0, ret, 0, obufcount);
        return ret;
    }

    /**
     * checks input string for invalid Base64 characters
     *
     * @param data
     * @return true, if String contains only valid Base64 characters. false, otherwise
     */
    public static boolean isValidBase64Encoding(String data) {
        for (int i = 0; i < data.length(); i++) {
            char ch = data.charAt(i);

            if (ch == S_BASE64PAD || ch < S_DECODETABLE.length
                    && S_DECODETABLE[ch] != Byte.MAX_VALUE) {
                //valid character.Do nothing
            } else if (ch == '\r' || ch == '\n') {
                //do nothing
            } else {
                return false;
            }
        }//iterate over all characters in the string
        return true;
    }


    /**
     *
     */
    public static void decode(char[] data, int off, int len,
                              OutputStream ostream) throws IOException {
        char[] ibuf = new char[4];
        int ibufcount = 0;
        byte[] obuf = new byte[3];
        for (int i = off; i < off + len; i++) {
            char ch = data[i];
            if (ch == S_BASE64PAD || ch < S_DECODETABLE.length
                    && S_DECODETABLE[ch] != Byte.MAX_VALUE) {
                ibuf[ibufcount++] = ch;
                if (ibufcount == ibuf.length) {
                    ibufcount = 0;
                    int obufcount = decode0(ibuf, obuf, 0);
                    ostream.write(obuf, 0, obufcount);
                }
            }
        }
    }

    /**
     *
     */
    public static void decode(String data, OutputStream ostream)
            throws IOException {
        char[] ibuf = new char[4];
        int ibufcount = 0;
        byte[] obuf = new byte[3];
        for (int i = 0; i < data.length(); i++) {
            char ch = data.charAt(i);
            if (ch == S_BASE64PAD || ch < S_DECODETABLE.length
                    && S_DECODETABLE[ch] != Byte.MAX_VALUE) {
                ibuf[ibufcount++] = ch;
                if (ibufcount == ibuf.length) {
                    ibufcount = 0;
                    int obufcount = decode0(ibuf, obuf, 0);
                    ostream.write(obuf, 0, obufcount);
                }
            }
        }
    }

    /** Returns base64 representation of specified byte array. */
    public static String encode(byte[] data) {
        return encode(data, 0, data.length);
    }

    /** Returns base64 representation of specified byte array. */
    public static String encode(byte[] data, int off, int len) {
        if (len <= 0)
            return "";
        char[] out = new char[len / 3 * 4 + 4];
        int rindex = off;
        int windex = 0;
        int rest = len - off;
        while (rest >= 3) {
            int i = ((data[rindex] & 0xff) << 16)
                    + ((data[rindex + 1] & 0xff) << 8)
                    + (data[rindex + 2] & 0xff);
            out[windex++] = S_BASE64CHAR[i >> 18];
            out[windex++] = S_BASE64CHAR[(i >> 12) & 0x3f];
            out[windex++] = S_BASE64CHAR[(i >> 6) & 0x3f];
            out[windex++] = S_BASE64CHAR[i & 0x3f];
            rindex += 3;
            rest -= 3;
        }
        if (rest == 1) {
            int i = data[rindex] & 0xff;
            out[windex++] = S_BASE64CHAR[i >> 2];
            out[windex++] = S_BASE64CHAR[(i << 4) & 0x3f];
            out[windex++] = S_BASE64PAD;
            out[windex++] = S_BASE64PAD;
        } else if (rest == 2) {
            int i = ((data[rindex] & 0xff) << 8) + (data[rindex + 1] & 0xff);
            out[windex++] = S_BASE64CHAR[i >> 10];
            out[windex++] = S_BASE64CHAR[(i >> 4) & 0x3f];
            out[windex++] = S_BASE64CHAR[(i << 2) & 0x3f];
            out[windex++] = S_BASE64PAD;
        }
        return new String(out, 0, windex);
    }

    /** Outputs base64 representation of the specified byte array to the specified String Buffer */
    public static void encode(byte[] data, int off, int len, StringBuffer buffer) {
        if (len <= 0) {
            return;
        }

        char[] out = new char[4];
        int rindex = off;
        int rest = len - off;
        while (rest >= 3) {
            int i = ((data[rindex] & 0xff) << 16)
                    + ((data[rindex + 1] & 0xff) << 8)
                    + (data[rindex + 2] & 0xff);
            out[0] = S_BASE64CHAR[i >> 18];
            out[1] = S_BASE64CHAR[(i >> 12) & 0x3f];
            out[2] = S_BASE64CHAR[(i >> 6) & 0x3f];
            out[3] = S_BASE64CHAR[i & 0x3f];
            buffer.append(out);
            rindex += 3;
            rest -= 3;
        }
        if (rest == 1) {
            int i = data[rindex] & 0xff;
            out[0] = S_BASE64CHAR[i >> 2];
            out[1] = S_BASE64CHAR[(i << 4) & 0x3f];
            out[2] = S_BASE64PAD;
            out[3] = S_BASE64PAD;
            buffer.append(out);
        } else if (rest == 2) {
            int i = ((data[rindex] & 0xff) << 8) + (data[rindex + 1] & 0xff);
            out[0] = S_BASE64CHAR[i >> 10];
            out[1] = S_BASE64CHAR[(i >> 4) & 0x3f];
            out[2] = S_BASE64CHAR[(i << 2) & 0x3f];
            out[3] = S_BASE64PAD;
            buffer.append(out);
        }
    }

    /** Outputs base64 representation of the specified byte array to a byte stream. */
    public static void encode(byte[] data, int off, int len,
                              OutputStream ostream) throws IOException {
        if (len <= 0)
            return;
        byte[] out = new byte[4];
        int rindex = off;
        int rest = len - off;
        while (rest >= 3) {
            int i = ((data[rindex] & 0xff) << 16)
                    + ((data[rindex + 1] & 0xff) << 8)
                    + (data[rindex + 2] & 0xff);
            out[0] = (byte) S_BASE64CHAR[i >> 18];
            out[1] = (byte) S_BASE64CHAR[(i >> 12) & 0x3f];
            out[2] = (byte) S_BASE64CHAR[(i >> 6) & 0x3f];
            out[3] = (byte) S_BASE64CHAR[i & 0x3f];
            ostream.write(out, 0, 4);
            rindex += 3;
            rest -= 3;
        }
        if (rest == 1) {
            int i = data[rindex] & 0xff;
            out[0] = (byte) S_BASE64CHAR[i >> 2];
            out[1] = (byte) S_BASE64CHAR[(i << 4) & 0x3f];
            out[2] = (byte) S_BASE64PAD;
            out[3] = (byte) S_BASE64PAD;
            ostream.write(out, 0, 4);
        } else if (rest == 2) {
            int i = ((data[rindex] & 0xff) << 8) + (data[rindex + 1] & 0xff);
            out[0] = (byte) S_BASE64CHAR[i >> 10];
            out[1] = (byte) S_BASE64CHAR[(i >> 4) & 0x3f];
            out[2] = (byte) S_BASE64CHAR[(i << 2) & 0x3f];
            out[3] = (byte) S_BASE64PAD;
            ostream.write(out, 0, 4);
        }
    }

    /** Outputs base64 representation of the specified byte array to a character stream. */
    public static void encode(byte[] data, int off, int len, Writer writer)
            throws IOException {
        if (len <= 0)
            return;
        char[] out = new char[4];
        int rindex = off;
        int rest = len - off;
        int output = 0;
        while (rest >= 3) {
            int i = ((data[rindex] & 0xff) << 16)
                    + ((data[rindex + 1] & 0xff) << 8)
                    + (data[rindex + 2] & 0xff);
            out[0] = S_BASE64CHAR[i >> 18];
            out[1] = S_BASE64CHAR[(i >> 12) & 0x3f];
            out[2] = S_BASE64CHAR[(i >> 6) & 0x3f];
            out[3] = S_BASE64CHAR[i & 0x3f];
            writer.write(out, 0, 4);
            rindex += 3;
            rest -= 3;
            output += 4;
            if (output % 76 == 0)
                writer.write("\n");
        }
        if (rest == 1) {
            int i = data[rindex] & 0xff;
            out[0] = S_BASE64CHAR[i >> 2];
            out[1] = S_BASE64CHAR[(i << 4) & 0x3f];
            out[2] = S_BASE64PAD;
            out[3] = S_BASE64PAD;
            writer.write(out, 0, 4);
        } else if (rest == 2) {
            int i = ((data[rindex] & 0xff) << 8) + (data[rindex + 1] & 0xff);
            out[0] = S_BASE64CHAR[i >> 10];
            out[1] = S_BASE64CHAR[(i >> 4) & 0x3f];
            out[2] = S_BASE64CHAR[(i << 2) & 0x3f];
            out[3] = S_BASE64PAD;
            writer.write(out, 0, 4);
        }
    }
}
