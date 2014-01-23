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

import java.util.NoSuchElementException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Wrapping XML stream reader that reads a single element from the underlying stream.
 * It will generate START_DOCUMENT and END_DOCUMENT events as required to make
 * the sequence of events appear as a complete document.
 * <p>
 * Assume for example that the parent reader is parsing the following document:
 * <pre>&lt;a>&lt;b>text&lt;/b>&lt;/a></pre>
 * If the current event is <code>&lt;b></code> when the wrapper is created, it will produce
 * the following sequence of events:
 * <p>
 * <ul>
 *   <li>A synthetic START_DOCUMENT event.</li>
 *   <li>START_ELEMENT, CHARACTERS and END_ELEMENT events for <code>&lt;b>text&lt;/b></code>.
 *       For these events, the wrapper directly delegates to the parent reader.</li>
 *   <li>A synthetic END_DOCUMENT event.</li>
 * </ul>
 * After all events have been consumed from the wrapper, the current event on the parent reader
 * will be the event following the last END_ELEMENT of the fragment. In the example above this
 * will be <code>&lt;/a></code>.
 * <p>
 * The wrapper will release the reference to the parent reader when {@link #close()} is called.
 * For obvious reasons, the wrapper will never call {@link XMLStreamReader#close()} on the parent
 * reader.
 */
public class XMLFragmentStreamReader implements XMLStreamReader {
    // The current event is a synthetic START_DOCUMENT event
    private static final int STATE_START_DOCUMENT = 0;
    
    // The current event is from the fragment and there will be more events from the fragment
    private static final int STATE_IN_FRAGMENT = 1;
    
    // The current event is the final END_ELEMENT event from the fragment
    private static final int STATE_FRAGMENT_END = 2;
    
    // The current event is a synthetic END_DOCUMENT event
    private static final int STATE_END_DOCUMENT = 3;
    
    private XMLStreamReader parent;
    private int state;
    private int depth;
    
    /**
     * Constructor.
     * 
     * @param parent the parent reader to read the fragment from
     * @throws IllegalStateException if the current event on the parent is not a START_ELEMENT
     */
    public XMLFragmentStreamReader(XMLStreamReader parent) {
        this.parent = parent;
        if (parent.getEventType() != START_ELEMENT) {
            throw new IllegalStateException("Expected START_ELEMENT as current event");
        }
    }

    public int getEventType() {
        switch (state) {
            case STATE_START_DOCUMENT:
                return START_DOCUMENT;
            case STATE_IN_FRAGMENT:
                return parent.getEventType();
            case STATE_FRAGMENT_END:
                return END_ELEMENT;
            case STATE_END_DOCUMENT:
                return END_DOCUMENT;
            default:
                // We will never get here; just make the compiler happy.
                throw new IllegalStateException();
        }
    }

    public int next() throws XMLStreamException {
        switch (state) {
            case STATE_START_DOCUMENT:
                state = STATE_IN_FRAGMENT;
                return START_ELEMENT;
            case STATE_IN_FRAGMENT:
                int type = parent.next();
                switch (type) {
                    case START_ELEMENT:
                        depth++;
                        break;
                    case END_ELEMENT:
                        if (depth == 0) {
                            state = STATE_FRAGMENT_END;
                        } else {
                            depth--;
                        }
                }
                return type;
            case STATE_FRAGMENT_END:
                // Consume the event from the parent to put the parser in a well-defined state
                parent.next();
                state = STATE_END_DOCUMENT;
                return END_DOCUMENT;
            default:
                throw new NoSuchElementException("End of document reached");
        }
    }

    public int nextTag() throws XMLStreamException {
        switch (state) {
            case STATE_START_DOCUMENT:
                state = STATE_IN_FRAGMENT;
                return START_ELEMENT;
            case STATE_END_DOCUMENT:
            case STATE_FRAGMENT_END:
                throw new NoSuchElementException();
            default:
                int result = parent.nextTag();
                switch (result) {
                    case START_ELEMENT:
                        depth++;
                        break;
                    case END_ELEMENT:
                        if (depth == 0) {
                            state = STATE_FRAGMENT_END;
                        } else {
                            depth--;
                        }
                }
                return result;
        }
    }

    public void close() throws XMLStreamException {
        parent = null;
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        return parent.getProperty(name);
    }

    public String getCharacterEncodingScheme() {
        return "UTF-8";
    }

    public String getEncoding() {
        return "UTF-8";
    }

    public String getVersion() {
        return "1.0";
    }
    
    public boolean isStandalone() {
        return true;
    }

    public boolean standaloneSet() {
        return false;
    }

    public Location getLocation() {
        return parent.getLocation();
    }

    public int getAttributeCount() {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getAttributeCount();
        }
    }

    public String getAttributeLocalName(int index) {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getAttributeLocalName(index);
        }
    }

    public QName getAttributeName(int index) {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getAttributeName(index);
        }
    }

    public String getAttributeNamespace(int index) {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getAttributeNamespace(index);
        }
    }

    public String getAttributePrefix(int index) {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getAttributePrefix(index);
        }
    }

    public String getAttributeType(int index) {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getAttributeType(index);
        }
    }

    public String getAttributeValue(int index) {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getAttributeValue(index);
        }
    }

    public boolean isAttributeSpecified(int index) {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.isAttributeSpecified(index);
        }
    }

    public String getAttributeValue(String namespaceURI, String localName) {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getAttributeValue(namespaceURI, localName);
        }
    }

    public String getElementText() throws XMLStreamException {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getElementText();
        }
    }

    public String getLocalName() {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getLocalName();
        }
    }

    public QName getName() {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getName();
        }
    }

    public String getPrefix() {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getPrefix();
        }
    }

    public String getNamespaceURI() {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getNamespaceURI();
        }
    }

    public int getNamespaceCount() {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getNamespaceCount();
        }
    }

    public String getNamespacePrefix(int index) {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getNamespacePrefix(index);
        }
    }

    public String getNamespaceURI(int index) {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getNamespaceURI(index);
        }
    }

    public String getNamespaceURI(String prefix) {
        // It is not clear whether this method is allowed in all states.
        // The XMLStreamReader Javadoc suggest it is, but Woodstox doesn't
        // allow it on states other than START_ELEMENT and END_ELEMENT.
        // We emulate behavior of Woodstox.
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getNamespaceURI(prefix);
        }
    }

    public NamespaceContext getNamespaceContext() {
        return parent.getNamespaceContext();
    }

    public String getPIData() {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getPIData();
        }
    }

    public String getPITarget() {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getPITarget();
        }
    }

    public String getText() {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getText();
        }
    }

    public char[] getTextCharacters() {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getTextCharacters();
        }
    }

    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length)
            throws XMLStreamException {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return getTextCharacters(sourceStart, target, targetStart, length);
        }
    }

    public int getTextLength() {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getTextLength();
        }
    }

    public int getTextStart() {
        if (state == STATE_START_DOCUMENT || state == STATE_END_DOCUMENT) {
            throw new IllegalStateException();
        } else {
            return parent.getTextStart();
        }
    }

    public boolean hasName() {
        return state != STATE_START_DOCUMENT && state != STATE_END_DOCUMENT && parent.hasName();
    }

    public boolean hasNext() throws XMLStreamException {
        return state != STATE_END_DOCUMENT;
    }

    public boolean hasText() {
        return state != STATE_START_DOCUMENT && state != STATE_END_DOCUMENT && parent.hasText();
    }

    public boolean isCharacters() {
        return state != STATE_START_DOCUMENT && state != STATE_END_DOCUMENT && parent.isCharacters();
    }

    public boolean isStartElement() {
        return state != STATE_START_DOCUMENT && state != STATE_END_DOCUMENT && parent.isStartElement();
    }

    public boolean isEndElement() {
        return state != STATE_START_DOCUMENT && state != STATE_END_DOCUMENT && parent.isEndElement();
    }

    public boolean isWhiteSpace() {
        return state != STATE_START_DOCUMENT && state != STATE_END_DOCUMENT && parent.isWhiteSpace();
    }

    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        switch (state) {
            case STATE_START_DOCUMENT:
                if (type != START_DOCUMENT) {
                    throw new XMLStreamException("Expected START_DOCUMENT");
                }
                break;
            case STATE_END_DOCUMENT:
                if (type != END_DOCUMENT) {
                    throw new XMLStreamException("Expected END_DOCUMENT");
                }
                break;
            default:
                parent.require(type, namespaceURI, localName);
        }
    }
}
