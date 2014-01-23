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

package org.apache.axiom.testutils.activation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * Test data source that produces a byte sequence with specified length and with all bytes
 * equal to a specified value.
 */
public class TestDataSource implements DataSource {
    final int value;
    final long length;

    public TestDataSource(int value, long length) {
        this.value = value;
        this.length = length;
    }

    public String getName() {
        return null;
    }
    
    public String getContentType() {
        return null;
    }
    
    public InputStream getInputStream() throws IOException {
        return new InputStream() {
            private long position;
            
            public int read() throws IOException {
                if (position == length) {
                    return -1;
                } else {
                    position++;
                    return value;
                }
            }
        };
    }
    
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }
}
