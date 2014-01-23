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
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.util.stax.AbstractXMLStreamWriter;

/**
 * {@link XMLStreamWriter} wrapper that handles namespace bindings on behalf of the underlying
 * writer. This wrapper can be used to correct two issues found in some stream writer
 * implementations:
 * <ol>
 *   <li>The writer doesn't correctly scope the namespace bindings. According to the StAX
 *       specifications, the scope of a namespace binding defined using
 *       {@link XMLStreamWriter#setPrefix(String, String)} or
 *       {@link XMLStreamWriter#setDefaultNamespace(String)} is limited to
 *       "the current <tt>START_ELEMENT</tt> / <tt>END_ELEMENT</tt> pair". Some implementations
 *       such as early versions of XL XP-J don't satisfy this requirement.
 *   <li>The writer doesn't handle masked prefixes correctly. To ensure consistent behavior
 *       in the presence of masked prefixes, the {@link XMLStreamWriter#getPrefix(String)} method
 *       (and the corresponding methods in the namespace context returned by
 *       {@link XMLStreamWriter#getNamespaceContext()}) must not return a prefix that
 *       is bound to a different namespace URI in a nested scope. Some implementations such as
 *       the StAX reference implementation fail to meet this requirement.
 * </ol>
 * <p>
 * Invocations of the following methods will be completely processed by the wrapper, and will never
 * reach the underlying writer:
 * <ul>
 *   <li>{@link XMLStreamWriter#getNamespaceContext()}
 *   <li>{@link XMLStreamWriter#setNamespaceContext(NamespaceContext)}
 *   <li>{@link XMLStreamWriter#getPrefix(String)}
 *   <li>{@link XMLStreamWriter#setDefaultNamespace(String)}
 *   <li>{@link XMLStreamWriter#setPrefix(String, String)}
 * </ul>
 * <p>
 * The following methods rely on information from the namespace context to choose a the namespace
 * prefix; the wrapper redirects invocations of these methods to the corresponding variants taking
 * an explicit prefix parameter:
 * <ul>
 *   <li>{@link XMLStreamWriter#writeStartElement(String, String)}
 *   <li>{@link XMLStreamWriter#writeAttribute(String, String, String)}
 *   <li>{@link XMLStreamWriter#writeEmptyElement(String, String)}
 * </ul>
 * <p>
 * This implies that if the wrapper is used, these methods will never be called on the underlying
 * writer.
 */
class NamespaceContextCorrectingXMLStreamWriterWrapper extends AbstractXMLStreamWriter {
    private final XMLStreamWriter parent;
    
    public NamespaceContextCorrectingXMLStreamWriterWrapper(XMLStreamWriter parent) {
        this.parent = parent;
    }

    protected void doWriteAttribute(String prefix, String namespaceURI, String localName,
            String value) throws XMLStreamException {
        parent.writeAttribute(prefix, namespaceURI, localName, value);
    }

    protected void doWriteAttribute(String localName, String value) throws XMLStreamException {
        parent.writeAttribute(localName, value);
    }

    protected void doWriteCData(String data) throws XMLStreamException {
        parent.writeCData(data);
    }

    protected void doWriteCharacters(char[] text, int start, int len) throws XMLStreamException {
        parent.writeCharacters(text, start, len);
    }

    protected void doWriteCharacters(String text) throws XMLStreamException {
        parent.writeCharacters(text);
    }

    protected void doWriteComment(String data) throws XMLStreamException {
        parent.writeComment(data);
    }

    protected void doWriteDefaultNamespace(String namespaceURI) throws XMLStreamException {
        parent.writeDefaultNamespace(namespaceURI);
    }

    protected void doWriteDTD(String dtd) throws XMLStreamException {
        parent.writeDTD(dtd);
    }

    protected void doWriteEmptyElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        parent.writeEmptyElement(prefix, localName, namespaceURI);
    }

    protected void doWriteEmptyElement(String localName) throws XMLStreamException {
        parent.writeEmptyElement(localName);
    }

    protected void doWriteEndDocument() throws XMLStreamException {
        parent.writeEndDocument();
    }

    protected void doWriteEndElement() throws XMLStreamException {
        parent.writeEndElement();
    }

    protected void doWriteEntityRef(String name) throws XMLStreamException {
        parent.writeEntityRef(name);
    }

    protected void doWriteNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        parent.writeNamespace(prefix, namespaceURI);
    }

    protected void doWriteProcessingInstruction(String target, String data)
            throws XMLStreamException {
        parent.writeProcessingInstruction(target, data);
    }

    protected void doWriteProcessingInstruction(String target) throws XMLStreamException {
        parent.writeProcessingInstruction(target);
    }

    protected void doWriteStartDocument() throws XMLStreamException {
        parent.writeStartDocument();
    }

    protected void doWriteStartDocument(String encoding, String version) throws XMLStreamException {
        parent.writeStartDocument(encoding, version);
    }

    protected void doWriteStartDocument(String version) throws XMLStreamException {
        parent.writeStartDocument(version);
    }

    protected void doWriteStartElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        parent.writeStartElement(prefix, localName, namespaceURI);
    }

    protected void doWriteStartElement(String localName) throws XMLStreamException {
        parent.writeStartElement(localName);
    }

    public void close() throws XMLStreamException {
        parent.close();
    }

    public void flush() throws XMLStreamException {
        parent.flush();
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        return parent.getProperty(name);
    }
}
