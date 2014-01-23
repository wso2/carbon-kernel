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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Utility class containing IO helper methods */
public class IOUtils {
    private IOUtils() {
    }

    /**
     * Reads into a byte array. Ensures that the full buffer is read. Helper method, just calls
     * <tt>readFully(in, b, 0, b.length)</tt>
     *
     * @see #readFully(java.io.InputStream, byte[], int, int)
     */
    public static int readFully(InputStream in, byte[] b)
            throws IOException {
        return readFully(in, b, 0, b.length);
    }

    /**
     * Same as the normal <tt>in.read(b, off, len)</tt>, but tries to ensure that the entire len
     * number of bytes is read.
     *
     * @return Returns the number of bytes read, or -1 if the end of file is reached before any
     *         bytes are read
     */
    public static int readFully(InputStream in, byte[] b, int off, int len)
            throws IOException {
        int total = 0;
        for (; ;) {
            int got = in.read(b, off + total, len - total);
            if (got < 0) {
                return (total == 0) ? -1 : total;
            } else {
                total += got;
                if (total == len)
                    return total;
            }
        }
    }

    /**
     * Returns the contents of the input stream as byte array.
     *
     * @param stream the <code>InputStream</code>
     * @return the stream content as byte array
     */
    public static byte[] getStreamAsByteArray(InputStream stream) throws IOException {
        return getStreamAsByteArray(stream, -1);
    }

    /**
     * Returns the contents of the input stream as byte array.
     *
     * @param stream the <code>InputStream</code>
     * @param length the number of bytes to copy, if length < 0, the number is unlimited
     * @return the stream content as byte array
     */
    public static byte[] getStreamAsByteArray(InputStream stream, int length) throws IOException {
        if (length == 0) return new byte[0];
        boolean checkLength = true;
        if (length < 0) {
            length = Integer.MAX_VALUE;
            checkLength = false;
        }
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int nextValue = stream.read();
        if (checkLength) length--;
        while (-1 != nextValue && length >= 0) {
            byteStream.write(nextValue);
            nextValue = stream.read();
            if (checkLength) length--;
        }
        return byteStream.toByteArray();
    }
}
