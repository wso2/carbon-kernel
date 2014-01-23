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

package org.apache.axiom.util.stax;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.util.namespace.ScopedNamespaceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Partial implementation of the {@link XMLStreamWriter} interface. It handles namespace bindings,
 * i.e. the methods related to the namespace context. Subclasses only need to implement write
 * methods that take a prefix together with the namespace URI argument. This class implements all
 * {@link XMLStreamWriter} methods that have a namespace URI argument, but no prefix argument.
 */
public abstract class AbstractXMLStreamWriter implements XMLStreamWriter {
    private static final Log log = LogFactory.getLog(AbstractXMLStreamWriter.class);
    
    private final ScopedNamespaceContext namespaceContext = new ScopedNamespaceContext();
    private boolean inEmptyElement;

    public final NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    public final void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        // We currently don't support this method
        throw new UnsupportedOperationException();
    }

    public final String getPrefix(String uri) throws XMLStreamException {
        return namespaceContext.getPrefix(uri);
    }

    private void internalSetPrefix(String prefix, String uri) {
        if (inEmptyElement) {
            log.warn("The behavior of XMLStreamWriter#setPrefix and " +
            		"XMLStreamWriter#setDefaultNamespace is undefined when invoked in the " +
            		"context of an empty element");
        }
        namespaceContext.setPrefix(prefix, uri);
    }
    
    public final void setDefaultNamespace(String uri) throws XMLStreamException {
        internalSetPrefix("", uri);
    }

    public final void setPrefix(String prefix, String uri) throws XMLStreamException {
        internalSetPrefix(prefix, uri);
    }

    public final void writeStartDocument() throws XMLStreamException {
        doWriteStartDocument();
    }
    
    protected abstract void doWriteStartDocument() throws XMLStreamException;

    public final void writeStartDocument(String encoding, String version) throws XMLStreamException {
        doWriteStartDocument(encoding, version);
    }

    protected abstract void doWriteStartDocument(String encoding, String version) throws XMLStreamException;

    public final void writeStartDocument(String version) throws XMLStreamException {
        doWriteStartDocument(version);
    }

    protected abstract void doWriteStartDocument(String version) throws XMLStreamException;
    
    public final void writeDTD(String dtd) throws XMLStreamException {
        doWriteDTD(dtd);
    }

    protected abstract void doWriteDTD(String dtd) throws XMLStreamException;
    
    public final void writeEndDocument() throws XMLStreamException {
        doWriteEndDocument();
    }

    protected abstract void doWriteEndDocument() throws XMLStreamException;
    
    private String internalGetPrefix(String namespaceURI) throws XMLStreamException {
        String prefix = namespaceContext.getPrefix(namespaceURI);
        if (prefix == null) {
            throw new XMLStreamException("Unbound namespace URI '" + namespaceURI + "'");
        } else {
            return prefix;
        }
    }
    
    public final void writeStartElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        doWriteStartElement(prefix, localName, namespaceURI);
        namespaceContext.startScope();
        inEmptyElement = false;
    }

    public final void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        doWriteStartElement(internalGetPrefix(namespaceURI), namespaceURI, localName);
        namespaceContext.startScope();
        inEmptyElement = false;
    }

    protected abstract void doWriteStartElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException;

    public final void writeStartElement(String localName) throws XMLStreamException {
        doWriteStartElement(localName);
        namespaceContext.startScope();
        inEmptyElement = false;
    }

    protected abstract void doWriteStartElement(String localName) throws XMLStreamException;

    public final void writeEndElement() throws XMLStreamException {
        doWriteEndElement();
        namespaceContext.endScope();
        inEmptyElement = false;
    }

    protected abstract void doWriteEndElement() throws XMLStreamException;

    public final void writeEmptyElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        doWriteEmptyElement(prefix, localName, namespaceURI);
        inEmptyElement = true;
    }

    public final void writeEmptyElement(String namespaceURI, String localName)
            throws XMLStreamException {
        doWriteEmptyElement(internalGetPrefix(namespaceURI), namespaceURI, localName);
        inEmptyElement = true;
    }

    protected abstract void doWriteEmptyElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException;

    public final void writeEmptyElement(String localName) throws XMLStreamException {
        doWriteEmptyElement(localName);
        inEmptyElement = true;
    }

    protected abstract void doWriteEmptyElement(String localName) throws XMLStreamException;
    
    public final void writeAttribute(String prefix, String namespaceURI, String localName, String value)
            throws XMLStreamException {
        doWriteAttribute(prefix, namespaceURI, localName, value);
    }

    public final void writeAttribute(String namespaceURI, String localName, String value)
            throws XMLStreamException {
        doWriteAttribute(internalGetPrefix(namespaceURI), namespaceURI, localName, value);
    }

    protected abstract void doWriteAttribute(String prefix, String namespaceURI, String localName,
            String value) throws XMLStreamException;

    public final void writeAttribute(String localName, String value) throws XMLStreamException {
        doWriteAttribute(localName, value);
    }
    
    protected abstract void doWriteAttribute(String localName, String value)
            throws XMLStreamException;

    public final void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        doWriteNamespace(prefix, namespaceURI);
    }
    
    protected abstract void doWriteNamespace(String prefix, String namespaceURI)
            throws XMLStreamException;

    public final void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        doWriteDefaultNamespace(namespaceURI);
    }

    protected abstract void doWriteDefaultNamespace(String namespaceURI) throws XMLStreamException;

    public final void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        doWriteCharacters(text, start, len);
        inEmptyElement = false;
    }
    
    protected abstract void doWriteCharacters(char[] text, int start, int len)
            throws XMLStreamException;

    public final void writeCharacters(String text) throws XMLStreamException {
        doWriteCharacters(text);
        inEmptyElement = false;
    }
    
    protected abstract void doWriteCharacters(String text) throws XMLStreamException;

    public final void writeCData(String data) throws XMLStreamException {
        doWriteCData(data);
        inEmptyElement = false;
    }
    
    protected abstract void doWriteCData(String data) throws XMLStreamException;

    public final void writeComment(String data) throws XMLStreamException {
        doWriteComment(data);
        inEmptyElement = false;
    }

    protected abstract void doWriteComment(String data) throws XMLStreamException;

    public final void writeEntityRef(String name) throws XMLStreamException {
        doWriteEntityRef(name);
        inEmptyElement = false;
    }

    protected abstract void doWriteEntityRef(String name) throws XMLStreamException;

    public final void writeProcessingInstruction(String target, String data)
            throws XMLStreamException {
        doWriteProcessingInstruction(target, data);
        inEmptyElement = false;
    }

    protected abstract void doWriteProcessingInstruction(String target, String data)
            throws XMLStreamException;

    public final void writeProcessingInstruction(String target) throws XMLStreamException {
        doWriteProcessingInstruction(target);
        inEmptyElement = false;
    }

    protected abstract void doWriteProcessingInstruction(String target) throws XMLStreamException;
}
