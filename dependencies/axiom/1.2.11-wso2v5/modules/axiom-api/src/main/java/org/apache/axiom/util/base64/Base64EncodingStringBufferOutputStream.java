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
 * {@link OutputStream} implementation that writes base64 encoded data to a {@link StringBuffer}.
 */
public class Base64EncodingStringBufferOutputStream extends AbstractBase64EncodingOutputStream {
    private final StringBuffer buffer;

    /**
     * Constructor.
     * 
     * @param buffer the buffer to append the encoded data to
     */
    public Base64EncodingStringBufferOutputStream(StringBuffer buffer) {
        this.buffer = buffer;
    }

    protected void doWrite(byte[] b) throws IOException {
        for (int i=0; i<4; i++) {
            buffer.append((char)(b[i] & 0xFF));
        }
    }

    protected void flushBuffer() throws IOException {
        // Nothing to do
    }
    
    protected void doClose() throws IOException {
        // Nothing to do
    }

    protected void doFlush() throws IOException {
        // Nothing to do
    }
}
