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

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.util.namespace.MapBasedNamespaceContext;

/**
 * {@link XMLStreamReader} implementation that
 * represents a text node wrapped inside an element. The text data is provided by a
 * {@link java.io.Reader Reader}.
 * <p>
 * It will produce the following sequence of XML events:
 * <ul>
 *   <li>START_DOCUMENT</li>
 *   <li>START_ELEMENT</li>
 *   <li>(CHARACTER)*</li>
 *   <li>END_ELEMENT</li>
 *   <li>END_DOCMENT</li>
 * </ul>
 * The class is implemented as a simple state machine, where the state is identified
 * by the current event type. The initial state is <tt>START_DOCUMENT</tt> and the
 * following transitions are triggered by {@link #next()}:
 * <ul>
 *   <li>START_DOCUMENT &rarr; START_ELEMENT</li>
 *   <li>START_ELEMENT &rarr; END_ELEMENT (if character stream is empty)</li>
 *   <li>START_ELEMENT &rarr; CHARACTERS (if character stream is not empty)</li>
 *   <li>CHARACTERS &rarr; CHARACTERS (if data available in stream)</li>
 *   <li>CHARACTERS &rarr; END_ELEMENT (if end of stream reached)</li>
 *   <li>END_ELEMENT &rarr; END_DOCUMENT</li>
 * </ul>
 * Additionally, {@link #getElementText()} triggers the following transition:
 * <ul>
 *   <li>START_ELEMENT &rarr; END_ELEMENT</li>
 * </ul>
 * Note that since multiple consecutive CHARACTERS events may be returned, this
 * "parser" is not coalescing.
 * 
 */
// TODO: this is a good candidate to implement the CharacterDataReader interface
public class WrappedTextNodeStreamReader implements XMLStreamReader {
    /**
     * The qualified name of the wrapper element.
     */
    private final QName wrapperElementName;
    
    /**
     * The Reader object that represents the text data.
     */
    private final Reader reader;
    
    /**
     * The maximum number of characters to return for each CHARACTER event.
     */
    private final int chunkSize;
    
    /**
     * The type of the current XML event.
     */
    private int eventType = START_DOCUMENT;
    
    /**
     * The character data for the current event. This is only set if the current
     * event is a CHARACTER event. The size of the array is determined by
     * {@link #chunkSize}
     */
    private char[] charData;
    
    /**
     * The length of the character data in {@link #charData}.
     */
    private int charDataLength;
    
    /**
     * The namespace context applicable in the scope of the wrapper element.
     * Beside the default mappings for xml and xmlns, it only contains the
     * mapping for the namespace of the wrapper element.
     * This attribute is initialized lazily by {@link #getNamespaceContext()}.
     */
    private NamespaceContext namespaceContext;
    
    /**
     * Create a new instance.
     * 
     * @param wrapperElementName the qualified name of the wrapper element
     * @param reader the Reader object holding the character data to be wrapped
     * @param chunkSize the maximum number of characters that are returned for each CHARACTER event
     */
    public WrappedTextNodeStreamReader(QName wrapperElementName, Reader reader, int chunkSize) {
        this.wrapperElementName = wrapperElementName;
        this.reader = reader;
        this.chunkSize = chunkSize;
    }
    
    /**
     * Create a new instance with chunk size 4096.
     * 
     * @param wrapperElementName the qualified name of the wrapper element
     * @param reader the Reader object holding the character data to be wrapped
     */
    public WrappedTextNodeStreamReader(QName wrapperElementName, Reader reader) {
        this(wrapperElementName, reader, 4096);
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        // We don't define any properties
        return null;
    }
    
    //
    // Methods to manipulate the parser state
    //
    
    public boolean hasNext() throws XMLStreamException {
        return eventType != END_DOCUMENT;
    }
    
    public int next() throws XMLStreamException {
        // Determine next event type based on current event type. If current event type
        // is START_ELEMENT or CHARACTERS, pull new data from the reader.
        switch (eventType) {
            case START_DOCUMENT:
                eventType = START_ELEMENT;
                break;
            case START_ELEMENT:
                charData = new char[chunkSize];
                // No break here!
            case CHARACTERS:
                try {
                    charDataLength = reader.read(charData);
                }
                catch (IOException ex) {
                    throw new XMLStreamException(ex);
                }
                if (charDataLength == -1) {
                    charData = null;
                    eventType = END_ELEMENT;
                } else {
                    eventType = CHARACTERS;
                }
                break;
            case END_ELEMENT:
                eventType = END_DOCUMENT;
                break;
            default:
                throw new IllegalStateException();
        }
        return eventType;
    }
    
    public int nextTag() throws XMLStreamException {
        // We don't have white space, comments or processing instructions
        throw new XMLStreamException("Current event is not white space");
    }

    public int getEventType() {
        return eventType;
    }

    public boolean isStartElement() { return eventType == START_ELEMENT; }
    public boolean isEndElement() { return eventType == END_ELEMENT; }
    public boolean isCharacters() { return eventType == CHARACTERS; }
    public boolean isWhiteSpace() { return false; }
    public boolean hasText() { return eventType == CHARACTERS; }
    public boolean hasName() { return eventType == START_ELEMENT || eventType == END_ELEMENT; }
    
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        if (type != eventType
             || (namespaceURI != null && !namespaceURI.equals(getNamespaceURI()))
             || (localName != null && !namespaceURI.equals(getLocalName()))) {
            throw new XMLStreamException("Unexpected event type");
        }
    }
    
    public Location getLocation() {
        // We do not support location information
        return DummyLocation.INSTANCE;
    }
    
    public void close() throws XMLStreamException {
        // Javadoc says that this method should not close the underlying input source,
        // but we need to close the reader somewhere.
        try {
            reader.close();
        }
        catch (IOException ex) {
            throw new XMLStreamException(ex);
        }
    }

    //
    // Methods related to the xml declaration.
    //
    
    public String getEncoding() {
        // Encoding is not known (not relevant?)
        return null;
    }

    public String getCharacterEncodingScheme() {
        // Encoding is not known (not relevant?)
        return null;
    }

    public String getVersion() {
        // Version is not relevant
        return null;
    }

    public boolean standaloneSet() {
        return false;
    }

    public boolean isStandalone() {
        return true;
    }
    
    //
    // Methods related to the namespace context
    //
    
    public NamespaceContext getNamespaceContext() {
        if (namespaceContext == null) {
            namespaceContext = new MapBasedNamespaceContext(Collections.singletonMap(wrapperElementName.getPrefix(), wrapperElementName.getNamespaceURI()));
        }
        return namespaceContext;
    }
    
    public String getNamespaceURI(String prefix) {
        String namespaceURI = getNamespaceContext().getNamespaceURI(prefix);
        // NamespaceContext#getNamespaceURI and XMLStreamReader#getNamespaceURI have slightly
        // different semantics for unbound prefixes.
        return namespaceURI.equals(XMLConstants.NULL_NS_URI) ? null : prefix;
    }
    
    //
    // Methods related to elements
    //
    
    private void checkStartElement() {
        if (eventType != START_ELEMENT) {
            throw new IllegalStateException();
        }
    }
    
    public String getAttributeValue(String namespaceURI, String localName) {
        checkStartElement();
        return null;
    }

    public int getAttributeCount() {
        checkStartElement();
        return 0;
    }
    
    public QName getAttributeName(int index) {
        checkStartElement();
        throw new ArrayIndexOutOfBoundsException();
    }

    public String getAttributeLocalName(int index) {
        checkStartElement();
        throw new ArrayIndexOutOfBoundsException();
    }

    public String getAttributePrefix(int index) {
        checkStartElement();
        throw new ArrayIndexOutOfBoundsException();
    }

    public String getAttributeNamespace(int index) {
        checkStartElement();
        throw new ArrayIndexOutOfBoundsException();
    }

    public String getAttributeType(int index) {
        checkStartElement();
        throw new ArrayIndexOutOfBoundsException();
    }

    public String getAttributeValue(int index) {
        checkStartElement();
        throw new ArrayIndexOutOfBoundsException();
    }

    public boolean isAttributeSpecified(int index) {
        checkStartElement();
        throw new ArrayIndexOutOfBoundsException();
    }
    
    private void checkElement() {
        if (eventType != START_ELEMENT && eventType != END_ELEMENT) {
            throw new IllegalStateException();
        }
    }
    
    public QName getName() {
        return null;
    }

    public String getLocalName() {
        checkElement();
        return wrapperElementName.getLocalPart();
    }

    public String getPrefix() {
        return wrapperElementName.getPrefix();
    }

    public String getNamespaceURI() {
        checkElement();
        return wrapperElementName.getNamespaceURI();
    }
    
    public int getNamespaceCount() {
        checkElement();
        // There is one namespace declared on the wrapper element
        return 1;
    }

    public String getNamespacePrefix(int index) {
        checkElement();
        if (index == 0) {
            return wrapperElementName.getPrefix();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public String getNamespaceURI(int index) {
        checkElement();
        if (index == 0) {
            return wrapperElementName.getNamespaceURI();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }
    
    public String getElementText() throws XMLStreamException {
        if (eventType == START_ELEMENT) {
            // Actually the purpose of this class is to avoid storing
            // the character data entirely in memory, but if the caller
            // wants a String, we don't have the choice...
            try {
                StringBuffer buffer = new StringBuffer();
                char[] cbuf = new char[4096];
                int c;
                while ((c = reader.read(cbuf)) != -1) {
                    buffer.append(cbuf, 0, c);
                }
                eventType = END_ELEMENT;
                return buffer.toString();
            }
            catch (IOException ex) {
                throw new XMLStreamException(ex);
            }
        } else {
            throw new XMLStreamException("Current event is not a START_ELEMENT");
        }
    }

    private void checkCharacters() {
        if (eventType != CHARACTERS) {
            throw new IllegalStateException();
        }
    }
    
    public String getText() {
        checkCharacters();
        return new String(charData, 0, charDataLength);
    }

    public char[] getTextCharacters() {
        checkCharacters();
        return charData;
    }

    public int getTextStart() {
        checkCharacters();
        return 0;
    }

    public int getTextLength() {
        checkCharacters();
        return charDataLength;
    }

    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        checkCharacters();
        int c = Math.min(charDataLength-sourceStart, length);
        System.arraycopy(charData, sourceStart, target, targetStart, c);
        return c;
    }
    
    //
    // Methods related to processing instructions
    //
    
    public String getPIData() {
        throw new IllegalStateException();
    }

    public String getPITarget() {
        throw new IllegalStateException();
    }
}
