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

package org.apache.axiom.util.activation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.axiom.ext.activation.SizeAwareDataSource;

/**
 * A data source with empty (zero length) content.
 */
public class EmptyDataSource implements SizeAwareDataSource {
    /**
     * Empty data source instance with content type <tt>application/octet-stream</tt>.
     */
    public static final EmptyDataSource INSTANCE = new EmptyDataSource("application/octet-stream");
    
    private static final InputStream emptyInputStream = new InputStream() {
        public int read() throws IOException {
            return -1;
        }
    };
    
    private final String contentType;
    
    /**
     * Construct an empty data source with the given content type.
     * 
     * @param contentType the content type
     */
    public EmptyDataSource(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public String getName() {
        return null;
    }

    public long getSize() {
        return 0;
    }

    public InputStream getInputStream() throws IOException {
        return emptyInputStream;
    }

    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }
}
