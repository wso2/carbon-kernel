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

package org.apache.axiom.testutils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Assert;

/**
 * {@link OutputStream} implementation that compares the data written to it with another character
 * sequence specified by an {@link InputStream}.
 */
public class ByteStreamComparator extends OutputStream {
    private final InputStream in;
    private final byte[] compareBuffer = new byte[1024];
    private int position;
    
    public ByteStreamComparator(InputStream in) {
        this.in = in;
    }

    public void write(byte[] buffer, int off, int len) throws IOException {
        while (len > 0) {
            int c = in.read(compareBuffer, 0, Math.min(compareBuffer.length, len));
            if (c == -1) {
                Assert.fail("The two streams have different lengths");
            }
            for (int i=0; i<c; i++) {
                if (buffer[off] != compareBuffer[i]) {
                    Assert.fail("Byte mismatch at position " + position);
                }
                off++;
                len--;
                position++;
            }
        }
    }

    public void flush() throws IOException {
    }

    public void close() throws IOException {
        if (in.read() != -1) {
            Assert.fail("The two streams have different lengths");
        }
    }

    public void write(int b) throws IOException {
        write(new byte[] { (byte)b });
    }
}
