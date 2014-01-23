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

import org.apache.commons.io.input.ProxyInputStream;

/**
 * {@link InputStream} wrapper that throw an exception when the end of the
 * parent stream is reached.
 */
public class ExceptionInputStream extends ProxyInputStream {
    public ExceptionInputStream(InputStream in) {
        super(in);
    }

    public int read() throws IOException {
        int b = super.read();
        if (b == -1) {
            throw new IOException("End of stream reached");
        }
        return b;
    }

    public int read(byte[] b) throws IOException {
        int c = super.read(b);
        if (c == -1) {
            throw new IOException("End of stream reached");
        }
        return c;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int c = super.read(b, off, len);
        if (c == -1) {
            throw new IOException("End of stream reached");
        }
        return c;
    }
}
