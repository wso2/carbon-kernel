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

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.ext.stax.DelegatingXMLStreamReader;
import org.apache.axiom.util.namespace.ScopedNamespaceContext;
import org.apache.axiom.util.stax.wrapper.XMLStreamReaderWrapper;

/**
 * {@link XMLStreamReader} wrapper that tracks the namespace bindings on behalf of the underlying
 * reader. This class may be used to wrap {@link XMLStreamReader} instances known to have issues in
 * their namespace context implementation. It tracks the namespace bindings using the
 * {@link XMLStreamReader#getNamespacePrefix(int)} and {@link XMLStreamReader#getNamespaceURI(int)}
 * methods and exposes this information by overriding the
 * {@link XMLStreamReader#getNamespaceContext()} and {@link XMLStreamReader#getNamespaceURI(String)}
 * methods. Invocations of these two methods will therefore never reach the underlying reader.
 */
class NamespaceContextCorrectingXMLStreamReaderWrapper extends XMLStreamReaderWrapper implements DelegatingXMLStreamReader {
    private final ScopedNamespaceContext namespaceContext = new ScopedNamespaceContext();

    /**
     * Constructor.
     * 
     * @param parent the parent reader
     */
    public NamespaceContextCorrectingXMLStreamReaderWrapper(XMLStreamReader parent) {
        super(parent);
    }

    private void startElement() {
        namespaceContext.startScope();
        for (int i=0, c=getNamespaceCount(); i<c; i++) {
            String prefix = getNamespacePrefix(i);
            namespaceContext.setPrefix(prefix == null ? "" : prefix, getNamespaceURI(i));
        }
    }
    
    public int next() throws XMLStreamException {
        if (isEndElement()) {
            namespaceContext.endScope();
        }
        int event = super.next();
        if (event == START_ELEMENT) {
            startElement();
        }
        return event;
    }

    public int nextTag() throws XMLStreamException {
        if (isEndElement()) {
            namespaceContext.endScope();
        }
        int event = super.nextTag();
        if (event == START_ELEMENT) {
            startElement();
        }
        return event;
    }

    public NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    public String getNamespaceURI(String prefix) {
        return namespaceContext.getNamespaceURI(prefix);
    }

    public XMLStreamReader getParent() {
        return super.getParent();
    }
}
