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

package org.apache.axiom.util.stax.wrapper;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Base class for {@link XMLStreamWriter} wrappers. The class provides default implementations for
 * all methods. Each of them calls the corresponding method in the parent writer. Note that in
 * contrast to {@link javax.xml.stream.XMLStreamReader}, for which there is a
 * {@link javax.xml.stream.util.StreamReaderDelegate}, no equivalent exists in the StAX API for
 * {@link XMLStreamWriter}.
 */
public class XMLStreamWriterWrapper implements XMLStreamWriter {
    private final XMLStreamWriter parent;

    /**
     * Constructor.
     * 
     * @param parent the parent writer
     */
    public XMLStreamWriterWrapper(XMLStreamWriter parent) {
        this.parent = parent;
    }

    public void close() throws XMLStreamException {
        parent.close();
    }

    public void flush() throws XMLStreamException {
        parent.flush();
    }

    public NamespaceContext getNamespaceContext() {
        return parent.getNamespaceContext();
    }

    public String getPrefix(String uri) throws XMLStreamException {
        return parent.getPrefix(uri);
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        return parent.getProperty(name);
    }

    public void setDefaultNamespace(String uri) throws XMLStreamException {
        parent.setDefaultNamespace(uri);
    }

    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        parent.setNamespaceContext(context);
    }

    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        parent.setPrefix(prefix, uri);
    }

    public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
            throws XMLStreamException {
        parent.writeAttribute(prefix, namespaceURI, localName, value);
    }

    public void writeAttribute(String namespaceURI, String localName, String value)
            throws XMLStreamException {
        parent.writeAttribute(namespaceURI, localName, value);
    }

    public void writeAttribute(String localName, String value) throws XMLStreamException {
        parent.writeAttribute(localName, value);
    }

    public void writeCData(String data) throws XMLStreamException {
        parent.writeCData(data);
    }

    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        parent.writeCharacters(text, start, len);
    }

    public void writeCharacters(String text) throws XMLStreamException {
        parent.writeCharacters(text);
    }

    public void writeComment(String data) throws XMLStreamException {
        parent.writeComment(data);
    }

    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        parent.writeDefaultNamespace(namespaceURI);
    }

    public void writeDTD(String dtd) throws XMLStreamException {
        parent.writeDTD(dtd);
    }

    public void writeEmptyElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        parent.writeEmptyElement(prefix, localName, namespaceURI);
    }

    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        parent.writeEmptyElement(namespaceURI, localName);
    }

    public void writeEmptyElement(String localName) throws XMLStreamException {
        parent.writeEmptyElement(localName);
    }

    public void writeEndDocument() throws XMLStreamException {
        parent.writeEndDocument();
    }

    public void writeEndElement() throws XMLStreamException {
        parent.writeEndElement();
    }

    public void writeEntityRef(String name) throws XMLStreamException {
        parent.writeEntityRef(name);
    }

    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        parent.writeNamespace(prefix, namespaceURI);
    }

    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        parent.writeProcessingInstruction(target, data);
    }

    public void writeProcessingInstruction(String target) throws XMLStreamException {
        parent.writeProcessingInstruction(target);
    }

    public void writeStartDocument() throws XMLStreamException {
        parent.writeStartDocument();
    }

    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        parent.writeStartDocument(encoding, version);
    }

    public void writeStartDocument(String version) throws XMLStreamException {
        parent.writeStartDocument(version);
    }

    public void writeStartElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        parent.writeStartElement(prefix, localName, namespaceURI);
    }

    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        parent.writeStartElement(namespaceURI, localName);
    }

    public void writeStartElement(String localName) throws XMLStreamException {
        parent.writeStartElement(localName);
    }
}
