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

package org.apache.axiom.util.stax.dialect;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.util.stax.wrapper.XMLStreamWriterWrapper;

class SJSXPStreamWriterWrapper extends XMLStreamWriterWrapper {
    public SJSXPStreamWriterWrapper(XMLStreamWriter parent) {
        super(parent);
    }

    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        if (encoding == null) {
            throw new IllegalArgumentException();
        } else {
            super.writeStartDocument(encoding, version);
        }
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        // When no properties have been set on the writer, getProperty throws a
        // NullPointerException; see https://sjsxp.dev.java.net/issues/show_bug.cgi?id=28
        try {
            return super.getProperty(name);
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException();
        }
    }
}
