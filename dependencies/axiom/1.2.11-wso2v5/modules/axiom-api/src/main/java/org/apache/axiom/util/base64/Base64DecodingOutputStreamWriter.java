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
 * {@link java.io.Writer} implementation that decodes base64 data and writes it
 * to a an {@link OutputStream}.
 */
public class Base64DecodingOutputStreamWriter extends AbstractBase64DecodingWriter {
    private final OutputStream stream;

    public Base64DecodingOutputStreamWriter(OutputStream stream) {
        this.stream = stream;
    }

    protected void doWrite(byte[] b, int len) throws IOException {
        stream.write(b, 0, len);
    }

    public void flush() throws IOException {
        stream.flush();
    }

    public void close() throws IOException {
        stream.close();
    }
}
