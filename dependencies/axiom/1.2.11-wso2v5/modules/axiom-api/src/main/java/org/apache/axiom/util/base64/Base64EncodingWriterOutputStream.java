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

/**
 * {@link OutputStream} implementation that writes base64 encoded data to a {@link Writer}.
 * This class internally buffers the data before writing it to the underlying stream.
 */
public class Base64EncodingWriterOutputStream extends AbstractBase64EncodingOutputStream {
    private final Writer writer;
    private final char[] buffer;
    private int len;
    
    /**
     * Constructor.
     * 
     * @param writer the stream to write the encoded data to
     * @param bufferSize the buffer size to use
     */
    public Base64EncodingWriterOutputStream(Writer writer, int bufferSize) {
        this.writer = writer;
        buffer = new char[bufferSize];
    }
    
    /**
     * Constructor that sets the buffer size to its default value of 4096 characters.
     * 
     * @param writer the stream to write the encoded data to
     */
    public Base64EncodingWriterOutputStream(Writer writer) {
        this(writer, 4096);
    }

    protected void doWrite(byte[] b) throws IOException {
        if (buffer.length - len < 4) {
            flushBuffer();
        }
        for (int i=0; i<4; i++) {
            buffer[len++] = (char)(b[i] & 0xFF);
        }
    }
    
    protected void flushBuffer() throws IOException {
        writer.write(buffer, 0, len);
        len = 0;
    }

    protected void doFlush() throws IOException {
        writer.flush();
    }

    protected void doClose() throws IOException {
        writer.close();
    }
}
