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
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Base class for {@link XMLStreamReader} wrappers. The class provides default implementations for
 * all methods. Each of them calls the corresponding method in the parent reader. This class is
 * similar to {@link javax.xml.stream.util.StreamReaderDelegate}, with the difference that it is
 * immutable.
 */
public class XMLStreamReaderWrapper implements XMLStreamReader {
    private final XMLStreamReader parent;

    /**
     * Constructor.
     * 
     * @param parent the parent reader
     */
    public XMLStreamReaderWrapper(XMLStreamReader parent) {
        this.parent = parent;
    }
    
    /**
     * Get the parent stream reader. This method is declared as protected because it should only be
     * used by subclasses. However, stream reader wrappers that can safely be unwrapped may
     * implement the {@link org.apache.axiom.ext.stax.DelegatingXMLStreamReader} interface to make
     * this a public method. Note that a corresponding <code>setParent</code> method is
     * intentionally omitted because {@link XMLStreamReaderWrapper} is immutable.
     * 
     * @return the parent stream reader that is wrapped by this object
     */
    protected XMLStreamReader getParent() {
        return parent;
    }

    public void close() throws XMLStreamException {
        parent.close();
    }

    public int getAttributeCount() {
        return parent.getAttributeCount();
    }

    public String getAttributeLocalName(int index) {
        return parent.getAttributeLocalName(index);
    }

    public QName getAttributeName(int index) {
        return parent.getAttributeName(index);
    }

    public String getAttributeNamespace(int index) {
        return parent.getAttributeNamespace(index);
    }

    public String getAttributePrefix(int index) {
        return parent.getAttributePrefix(index);
    }

    public String getAttributeType(int index) {
        return parent.getAttributeType(index);
    }

    public String getAttributeValue(int index) {
        return parent.getAttributeValue(index);
    }

    public String getAttributeValue(String namespaceURI, String localName) {
        return parent.getAttributeValue(namespaceURI, localName);
    }

    public String getCharacterEncodingScheme() {
        return parent.getCharacterEncodingScheme();
    }

    public String getElementText() throws XMLStreamException {
        return parent.getElementText();
    }

    public String getEncoding() {
        return parent.getEncoding();
    }

    public int getEventType() {
        return parent.getEventType();
    }

    public String getLocalName() {
        return parent.getLocalName();
    }

    public Location getLocation() {
        return parent.getLocation();
    }

    public QName getName() {
        return parent.getName();
    }

    public NamespaceContext getNamespaceContext() {
        return parent.getNamespaceContext();
    }

    public int getNamespaceCount() {
        return parent.getNamespaceCount();
    }

    public String getNamespacePrefix(int index) {
        return parent.getNamespacePrefix(index);
    }

    public String getNamespaceURI() {
        return parent.getNamespaceURI();
    }

    public String getNamespaceURI(int index) {
        return parent.getNamespaceURI(index);
    }

    public String getNamespaceURI(String prefix) {
        return parent.getNamespaceURI(prefix);
    }

    public String getPIData() {
        return parent.getPIData();
    }

    public String getPITarget() {
        return parent.getPITarget();
    }

    public String getPrefix() {
        return parent.getPrefix();
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        return parent.getProperty(name);
    }

    public String getText() {
        return parent.getText();
    }

    public char[] getTextCharacters() {
        return parent.getTextCharacters();
    }

    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length)
            throws XMLStreamException {
        return parent.getTextCharacters(sourceStart, target, targetStart, length);
    }

    public int getTextLength() {
        return parent.getTextLength();
    }

    public int getTextStart() {
        return parent.getTextStart();
    }

    public String getVersion() {
        return parent.getVersion();
    }

    public boolean hasName() {
        return parent.hasName();
    }

    public boolean hasNext() throws XMLStreamException {
        return parent.hasNext();
    }

    public boolean hasText() {
        return parent.hasText();
    }

    public boolean isAttributeSpecified(int index) {
        return parent.isAttributeSpecified(index);
    }

    public boolean isCharacters() {
        return parent.isCharacters();
    }

    public boolean isEndElement() {
        return parent.isEndElement();
    }

    public boolean isStandalone() {
        return parent.isStandalone();
    }

    public boolean isStartElement() {
        return parent.isStartElement();
    }

    public boolean isWhiteSpace() {
        return parent.isWhiteSpace();
    }

    public int next() throws XMLStreamException {
        return parent.next();
    }

    public int nextTag() throws XMLStreamException {
        return parent.nextTag();
    }

    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        parent.require(type, namespaceURI, localName);
    }

    public boolean standaloneSet() {
        return parent.standaloneSet();
    }
}
