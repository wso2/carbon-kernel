/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.commons.io.input.ReaderInputStream;

/**
 * Data source that represents the text of a given {@link OMElement}.
 * <p>
 * The expression
 * <pre>new TextFromElementDataSource(element, charset, contentType)</pre>
 * produces a DataSource implementation that is equivalent to
 * <pre>new ByteArrayDataSource(element.getText().getBytes(charset), contentType)</pre>
 * but that is more efficient.
 */
public class TextFromElementDataSource implements DataSource {
    private final OMElement element;
    private final String charset;
    private final String contentType;
    
    public TextFromElementDataSource(OMElement element, String charset, String contentType) {
        this.element = element;
        this.charset = charset;
        this.contentType = contentType;
    }
    
    public String getContentType() {
        return contentType;
    }

    public String getName() {
        return null;
    }

    public InputStream getInputStream() throws IOException {
        return new ReaderInputStream(ElementHelper.getTextAsStream(element, true), charset);
    }

    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }
}
