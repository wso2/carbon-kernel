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

package org.apache.axiom.om.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

import javax.activation.DataHandler;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.ext.stax.CharacterDataReader;
import org.apache.axiom.ext.stax.datahandler.DataHandlerProvider;
import org.apache.axiom.ext.stax.datahandler.DataHandlerReader;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMDocType;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMProcessingInstruction;
import org.apache.axiom.om.OMSerializable;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.OMNavigator;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.exception.OMStreamingException;
import org.apache.axiom.util.namespace.MapBasedNamespaceContext;
import org.apache.axiom.util.stax.AbstractXMLStreamReader;
import org.apache.axiom.util.stax.DummyLocation;
import org.apache.axiom.util.stax.XMLStreamReaderUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class used internally by {@link OMStAXWrapper}.
 */
class SwitchingWrapper extends AbstractXMLStreamReader
    implements DataHandlerReader, CharacterDataReader, XMLStreamConstants {
    
    private static final Log log = LogFactory.getLog(SwitchingWrapper.class);
    private static boolean DEBUG_ENABLED = log.isDebugEnabled();
    
    /** Field navigator */
    private OMNavigator navigator;

    /** Field builder */
    private OMXMLParserWrapper builder;

    /** Field parser */
    private XMLStreamReader parser;
    
    /**
     * The {@link DataHandlerReader} extension of the underlying parser, or <code>null</code>
     * if the parser doesn't support this extension.
     */
    private DataHandlerReader dataHandlerReader;
    
    private boolean _isClosed = false;              // Indicate if parser is closed
    private boolean _releaseParserOnClose = false;  // Defaults to legacy behavior, which is keep the reference

    /** Field rootNode */
    private OMContainer rootNode;

    /** Field isFirst */
    private boolean isFirst = true;

    // Navigable means the output should be taken from the navigator.
    // As soon as the navigator returns a null navigable will be reset
    // to false and the subsequent events will be taken from the builder
    // or the parser directly.

    /** Field NAVIGABLE */
    private static final short NAVIGABLE = 0;
    private static final short SWITCH_AT_NEXT = 1;
    private static final short COMPLETED = 2;
    private static final short SWITCHED = 3;
    private static final short DOCUMENT_COMPLETE = 4;

    /** Field state */
    private short state;

    /** Field currentEvent Default set to START_DOCUMENT */
    private int currentEvent = START_DOCUMENT;

    // SwitchingAllowed is set to false by default.
    // This means that unless the user explicitly states
    // that he wants things not to be cached, everything will
    // be cached.

    /** Field switchingAllowed */
    boolean switchingAllowed = false;
    
    // namespaceURI interning
    // default is false because most XMLStreamReader implementations don't do interning
    // due to performance impacts
    boolean namespaceURIInterning = false;

    /** Field elementStack */
    private Stack nodeStack = null;

    // keeps the next event. The parser actually keeps one step ahead to
    // detect the end of navigation. (at the end of the stream the navigator
    // returns a null

    /** Field nextNode */
    private OMSerializable nextNode = null;

    // holder for the current node. Needs this to generate events from the
    // current node

    /** Field currentNode */
    private OMSerializable currentNode = null;

    // needs this to refer to the last known node

    /** Field lastNode */
    private OMSerializable lastNode = null;

    /** Track depth to ensure we stop generating events when we are done with the root node. */
    int depth = 0;

    private boolean needToThrowEndDocument = false;

    // Cache attributes and namespaces. This avoids creating a new Iterator for every call
    // to getAttributeXXX and getNamespaceXXX. A value of -1 indicates that the
    // attributes or namespaces for the current element have not been loaded yet. The
    // two arrays are resized on demand.
    private int attributeCount = -1;
    private OMAttribute[] attributes = new OMAttribute[16];
    private int namespaceCount = -1;
    private OMNamespace[] namespaces = new OMNamespace[16];
    
    /**
     * Method setAllowSwitching.
     *
     * @param b
     */
    public void setAllowSwitching(boolean b) {
        this.switchingAllowed = b;
    }

    /**
     * Method isAllowSwitching.
     *
     * @return Returns boolean.
     */
    public boolean isAllowSwitching() {
        return switchingAllowed;
    }

    /**
     * Set namespace uri interning
     * @param b
     */
    public void setNamespaceURIInterning(boolean b) {
        this.namespaceURIInterning = b;
    }
    
    /**
     * @return if namespace uri interning 
     */
    public boolean isNamespaceURIInterning() {
        return this.namespaceURIInterning;
    }
    
    /**
     * Constructor.
     *
     * @param builder
     * @param startNode
     * @param cache
     */
    public SwitchingWrapper(OMXMLParserWrapper builder, OMContainer startNode,
                            boolean cache) {

        // create a navigator
        this.navigator = new OMNavigator(startNode);
        this.builder = builder;
        this.rootNode = startNode;
        if (rootNode instanceof OMElement && ((OMElement)rootNode).getParent() instanceof OMDocument) {
            needToThrowEndDocument = true;
        }

        // initiate the next and current nodes
        // Note - navigator is written in such a way that it first
        // returns the starting node at the first call to it
        // Note - for OMSourcedElements, temporarily set caching
        // to get the initial navigator nodes
        boolean resetCache = false;
        try {
            if (startNode instanceof OMSourcedElement && 
                    !cache && builder != null) {
                if (!builder.isCache()) {
                    resetCache = true;
                }
                builder.setCache(true); // bootstrap the navigator
                
            }
        } catch(Throwable t) {}
        
        currentNode = navigator.getNext();
        updateNextNode();
        if (resetCache) {
            builder.setCache(cache); 
        }
        switchingAllowed = !cache;
        
        if (startNode instanceof OMDocument) {
            try {
                next();
            } catch (XMLStreamException ex) {
                throw new OMException(ex);
            }
        }
    }

    /**
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getPrefix()
     */
    public String getPrefix() {
        if (parser != null) {
            return parser.getPrefix();
        } else {
            if ((currentEvent == START_ELEMENT)
                    || (currentEvent == END_ELEMENT)) {
                OMNamespace ns = ((OMElement) lastNode).getNamespace();
                if (ns == null) {
                    return null;
                } else {
                    String prefix = ns.getPrefix();
                    return prefix == null || prefix.isEmpty() ? null : prefix;
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getNamespaceURI()
     */
    public String getNamespaceURI() {
        String returnStr;
        if (parser != null) {
            returnStr = parser.getNamespaceURI();
        } else {
            if ((currentEvent == START_ELEMENT)
                    || (currentEvent == END_ELEMENT)
                    || (currentEvent == NAMESPACE)) {
                OMNamespace ns = ((OMElement) lastNode).getNamespace();
                if (ns == null) {
                    returnStr = null;
                } else {
                    String namespaceURI = ns.getNamespaceURI();
                    returnStr = namespaceURI.isEmpty() ? null : namespaceURI;
                }
            } else {
                throw new IllegalStateException();
            }
        }
        
        // By default most parsers don't intern the namespace.
        // Unfortunately the property to detect interning on the delegate parsers is hard to detect.
        // Woodstox has a proprietary property on the XMLInputFactory.
        // IBM has a proprietary property on the XMLStreamReader.
        // For now only force the interning if requested.
        if (this.isNamespaceURIInterning()) {
            returnStr = (returnStr != null) ? returnStr.intern() : null;
        }
        return returnStr;
    }

    /**
     * @return Returns boolean.
     * @see javax.xml.stream.XMLStreamReader#hasName()
     */
    public boolean hasName() {
        if (parser != null) {
            return parser.hasName();
        } else {
            return ((currentEvent == START_ELEMENT)
                    || (currentEvent == END_ELEMENT));
        }
    }

    /**
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getLocalName()
     */
    public String getLocalName() {
        if (parser != null) {
            return parser.getLocalName();
        } else {
            if ((currentEvent == START_ELEMENT)
                    || (currentEvent == END_ELEMENT)
                    || (currentEvent == ENTITY_REFERENCE)) {
                return ((OMElement) lastNode).getLocalName();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * @return Returns QName.
     * @see javax.xml.stream.XMLStreamReader#getName()
     */
    public QName getName() {
        if (parser != null) {
            return parser.getName();
        } else {
            if ((currentEvent == START_ELEMENT)
                    || (currentEvent == END_ELEMENT)) {
                return getQName((OMElement) lastNode);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * @return Returns int.
     * @see javax.xml.stream.XMLStreamReader#getTextLength()
     */
    public int getTextLength() {
        if (parser != null) {
            return parser.getTextLength();
        } else if (currentEvent == DTD) {
            // Not sure if that conforms to the StAX spec, but it is what Woodstox does
            throw new IllegalStateException();
        } else {
            return getTextFromNode().length();
        }
    }

    /**
     * @return Returns int.
     * @see javax.xml.stream.XMLStreamReader#getTextStart()
     */
    public int getTextStart() {
        if (parser != null) {
            return parser.getTextStart();
        } else {
            if (currentEvent == DTD) {
                // Not sure if that conforms to the StAX spec, but it is what Woodstox does
                throw new IllegalStateException();
            } else if (hasText()) {
                // getTextCharacters always returns a new char array and the start
                // index is therefore always 0
                return 0;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * @param sourceStart
     * @param target
     * @param targetStart
     * @param length
     * @return Returns int.
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamReader#getTextCharacters(int, char[], int, int)
     */
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length)
            throws XMLStreamException {
        if (parser != null) {
            try {
                return parser.getTextCharacters(sourceStart, target, targetStart, length);
            } catch (XMLStreamException e) {
                throw new OMStreamingException(e);
            }
        } else {
            if (currentEvent == DTD) {
                // Not sure if that conforms to the StAX spec, but it is what Woodstox does
                throw new IllegalStateException();
            } else {
                String text = getTextFromNode();
                int copied = Math.min(length, text.length()-sourceStart);
                text.getChars(sourceStart, sourceStart + copied, target, targetStart);
                return copied;
            }
        }
    }

    /**
     * @return Returns char[].
     * @see javax.xml.stream.XMLStreamReader#getTextCharacters()
     */
    public char[] getTextCharacters() {
        if (parser != null) {
            return parser.getTextCharacters();
        } else if (currentEvent == DTD) {
            // Not sure if that conforms to the StAX spec, but it is what Woodstox does
            throw new IllegalStateException();
        } else {
            return getTextFromNode().toCharArray();
        }
    }

    /**
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getText()
     */
    public String getText() {
        if (parser != null) {
            return parser.getText();
        } else {
            return getTextFromNode();
        }
    }
    
    private String getTextFromNode() {
        switch (currentEvent) {
            case CHARACTERS:
            case CDATA:
            case SPACE:
                return ((OMText)lastNode).getText();
            case COMMENT:
                return ((OMComment)lastNode).getValue();
            case DTD:
                return ((OMDocType)lastNode).getValue();
            default:
                throw new IllegalStateException();
        }
    }

    public void writeTextTo(Writer writer) throws XMLStreamException, IOException {
        if (parser != null) {
            XMLStreamReaderUtils.writeTextTo(parser, writer);
        } else {
            switch (currentEvent) {
                case CHARACTERS:
                case CDATA:
                case SPACE:
                    OMText text = (OMText)lastNode;
                    if (text.isCharacters()) {
                        writer.write(text.getTextCharacters());
                    } else {
                        // TODO: we should cover the binary case in an optimized way
                        writer.write(text.getText());
                    }
                    break;
                case COMMENT:
                    writer.write(((OMComment)lastNode).getValue());
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    /**
     * @return Returns int.
     * @see javax.xml.stream.XMLStreamReader#getEventType()
     */

    // todo this should be improved
    public int getEventType() {
        return currentEvent;
    }

    private void loadAttributes() {
        if (attributeCount == -1) {
            attributeCount = 0;
            for (Iterator it = ((OMElement)lastNode).getAllAttributes(); it.hasNext(); ) {
                OMAttribute attr = (OMAttribute)it.next();
                if (attributeCount == attributes.length) {
                    OMAttribute[] newAttributes = new OMAttribute[attributes.length*2];
                    System.arraycopy(attributes, 0, newAttributes, 0, attributes.length);
                    attributes = newAttributes;
                }
                attributes[attributeCount] = attr;
                attributeCount++;
            }
        }
    }
    
    private void loadNamespaces() {
        if (namespaceCount == -1) {
            namespaceCount = 0;
            for (Iterator it = ((OMElement)lastNode).getAllDeclaredNamespaces(); it.hasNext(); ) {
                OMNamespace ns = (OMNamespace)it.next();
                // Axiom internally creates an OMNamespace instance for the "xml" prefix, even
                // if it is not declared explicitly. Filter this instance out.
                if (!"xml".equals(ns.getPrefix())) {
                    if (namespaceCount == namespaces.length) {
                        OMNamespace[] newNamespaces = new OMNamespace[namespaces.length*2];
                        System.arraycopy(namespaces, 0, newNamespaces, 0, namespaces.length);
                        namespaces = newNamespaces;
                    }
                    namespaces[namespaceCount] = ns;
                    namespaceCount++;
                }
            }
        }
    }
    
    /**
     * @param i
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getNamespaceURI
     */
    public String getNamespaceURI(int i) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getNamespaceURI(i);
        } else {
            if (isStartElement() || isEndElement()
                    || (currentEvent == NAMESPACE)) {
                loadNamespaces();
                returnString = namespaces[i].getNamespaceURI();
            }
        }

        /*
          The following line is necessary to overcome an issue where the empty
          namespace URI returning null rather than the empty string. Our resolution
          is to return "" if the return is actually null

          Note that this is not the case for  getNamespaceURI(prefix) method
          where the contract clearly specifies that the return may be null

        */
        if (returnString == null) returnString = "";

        return returnString;
    }

    /**
     * @param i
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getNamespacePrefix
     */
    public String getNamespacePrefix(int i) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getNamespacePrefix(i);
        } else {
            if (isStartElement() || isEndElement()
                    || (currentEvent == NAMESPACE)) {
                loadNamespaces();
                String prefix = namespaces[i].getPrefix();
                returnString = prefix == null || prefix.isEmpty() ? null : prefix;
            }
        }
        return returnString;
    }

    /**
     * @return Returns int.
     * @see javax.xml.stream.XMLStreamReader#getNamespaceCount()
     */
    public int getNamespaceCount() {
        if (parser != null) {
            return parser.getNamespaceCount();
        } else {
            if (isStartElement() || isEndElement()
                    || (currentEvent == NAMESPACE)) {
                loadNamespaces();
                return namespaceCount;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * @param i
     * @return Returns boolean.
     * @see javax.xml.stream.XMLStreamReader#isAttributeSpecified
     */
    public boolean isAttributeSpecified(int i) {
        if (parser != null) {
            return parser.isAttributeSpecified(i);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                // The Axiom object model doesn't store this information,
                // but returning true is a reasonable default.
                return true;
            } else {
                throw new IllegalStateException(
                        "attribute type accessed in illegal event!");
            }
        }
    }

    /**
     * @param i
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getAttributeValue
     */
    public String getAttributeValue(int i) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getAttributeValue(i);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                loadAttributes();
                returnString = attributes[i].getAttributeValue();
            } else {
                throw new IllegalStateException(
                        "attribute type accessed in illegal event!");
            }
        }
        return returnString;
    }

    /**
     * @param i
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getAttributeType
     */
    public String getAttributeType(int i) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getAttributeType(i);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                loadAttributes();
                returnString = attributes[i].getAttributeType();
            } else {
                throw new IllegalStateException(
                        "attribute type accessed in illegal event!");
            }
        }
        return returnString;
    }

    /**
     * @param i
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getAttributePrefix
     */
    public String getAttributePrefix(int i) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getAttributePrefix(i);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                loadAttributes();
                OMAttribute attrib = attributes[i];
                if (attrib != null) {
                    OMNamespace nameSpace = attrib.getNamespace();
                    if (nameSpace != null) {
                        returnString = nameSpace.getPrefix();
                    }
                }
            } else {
                throw new IllegalStateException(
                        "attribute prefix accessed in illegal event!");
            }
        }
        return returnString;
    }

    /**
     * @param i
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getAttributeLocalName
     */
    public String getAttributeLocalName(int i) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getAttributeLocalName(i);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                loadAttributes();
                returnString = attributes[i].getLocalName();
            } else {
                throw new IllegalStateException(
                        "attribute localName accessed in illegal event!");
            }
        }
        return returnString;
    }

    /**
     * @param i
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getAttributeNamespace
     */
    public String getAttributeNamespace(int i) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getAttributeNamespace(i);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                loadAttributes();
                OMAttribute attrib = attributes[i];
                if (attrib != null) {
                    OMNamespace nameSpace = attrib.getNamespace();
                    if (nameSpace != null) {
                        returnString = nameSpace.getNamespaceURI();
                    }
                }
            } else {
                throw new IllegalStateException(
                        "attribute nameSpace accessed in illegal event!");
            }
        }
        return returnString;
    }

    /**
     * @param i
     * @return Returns QName.
     * @see javax.xml.stream.XMLStreamReader#getAttributeName
     */
    public QName getAttributeName(int i) {
        QName returnQName = null;
        if (parser != null) {
            returnQName = parser.getAttributeName(i);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                loadAttributes();
                returnQName = attributes[i].getQName();
            } else {
                throw new IllegalStateException(
                        "attribute count accessed in illegal event!");
            }
        }
        return returnQName;
    }

    /**
     * @return Returns int.
     * @see javax.xml.stream.XMLStreamReader#getAttributeCount
     */
    public int getAttributeCount() {
        int returnCount = 0;
        if (parser != null) {
            returnCount = parser.getAttributeCount();
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                loadAttributes();
                returnCount = attributeCount;
            } else {
                throw new IllegalStateException(
                        "attribute count accessed in illegal event (" +
                                currentEvent + ")!");
            }
        }
        return returnCount;
    }

    // todo

    /**
     * Method getAttributeValue.
     *
     * @param s
     * @param s1
     * @return Returns String.
     */
    public String getAttributeValue(String s, String s1) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getAttributeValue(s, s1);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                QName qname = new QName(s, s1);
                OMAttribute attrib = ((OMElement) lastNode).getAttribute(qname);
                if (attrib != null) {
                    returnString = attrib.getAttributeValue();
                }
            } else {
                throw new IllegalStateException(
                        "attribute type accessed in illegal event!");
            }
        }
        return returnString;
    }

    /**
     * Method isWhiteSpace.
     *
     * @return Returns boolean.
     */
    public boolean isWhiteSpace() {
        if (parser != null) {
            return parser.isWhiteSpace();
        } else {
            switch (currentEvent) {
                case SPACE:
                    return true;
                case CHARACTERS:
                    // XMLStreamReader Javadoc says that isWhiteSpace "returns true if the cursor
                    // points to a character data event that consists of all whitespace". This
                    // means that this method may return true for a CHARACTER event and we need
                    // to scan the text of the node.
                    String text = getTextFromNode();
                    for (int i=0; i<text.length(); i++) {
                        char c = text.charAt(i);
                        if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                            return false;
                        }
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    /**
     * Method isCharacters.
     *
     * @return Returns boolean.
     */
    public boolean isCharacters() {
        boolean b;
        if (parser != null) {
            b = parser.isCharacters();
        } else {
            b = (currentEvent == CHARACTERS);
        }
        return b;
    }

    /**
     * Method isEndElement.
     *
     * @return Returns boolean.
     */
    public boolean isEndElement() {
        boolean b;
        if (parser != null) {
            b = parser.isEndElement();
        } else {
            b = (currentEvent == END_ELEMENT);
        }
        return b;
    }

    /**
     * Method isStartElement.
     *
     * @return Returns boolean.
     */
    public boolean isStartElement() {
        boolean b;
        if (parser != null) {
            b = parser.isStartElement();
        } else {
            b = (currentEvent == START_ELEMENT);
        }
        return b;
    }

    /**
     * Method getNamespaceURI.
     *
     * @param prefix
     * @return Returns String.
     */
    public String getNamespaceURI(String prefix) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getNamespaceURI(prefix);
        } else {
            if (isStartElement() || isEndElement()
                    || (currentEvent == NAMESPACE)) {

                if (lastNode instanceof OMElement) {
                    OMNamespace namespaceURI =
                            ((OMElement) lastNode).findNamespaceURI(prefix);
                    return namespaceURI != null ? namespaceURI.getNamespaceURI() : null;
                }
            }
        }
        return returnString;
    }

    /**
     * Method close.
     *
     * @throws XMLStreamException
     */
    public void close() throws XMLStreamException {

        // If there is a builder, it controls its parser
        if (builder != null && builder instanceof StAXBuilder) {
            StAXBuilder staxBuilder = (StAXBuilder) builder;
            staxBuilder.close();
            setParser(null);
        } else {
            if (parser != null) {
                try {
                    if (!isClosed()) {
                        parser.close();
                    }
                } finally {
                    _isClosed = true;
                    // Release the parser so that it can be GC'd or reused.
                    if (_releaseParserOnClose) {
                        setParser(null);
                    }
                }
            }
        }
    }

    /**
     * Method hasNext.
     *
     * @return Returns boolean.
     * @throws XMLStreamException
     */
    public boolean hasNext() throws XMLStreamException {
        if (needToThrowEndDocument) {
            return !(state == DOCUMENT_COMPLETE);
        } else {
            return (state != COMPLETED && currentEvent != END_DOCUMENT);
        }
    }

    /**
     * @return Returns String.
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamReader#getElementText()
     */
    public String getElementText() throws XMLStreamException {
        if (parser != null) {
            try {
                String elementText = parser.getElementText();
                currentEvent = END_ELEMENT;
                return elementText;
            } catch (XMLStreamException e) {
                throw new OMStreamingException(e);
            }
        } else {
            ///////////////////////////////////////////////////////
            //// Code block directly from the API documentation ///
            if (getEventType() != XMLStreamConstants.START_ELEMENT) {
                throw new XMLStreamException(
                        "parser must be on START_ELEMENT to read next text", getLocation());
            }
            int eventType = next();
            StringBuffer content = new StringBuffer();
            while (eventType != XMLStreamConstants.END_ELEMENT) {
                if (eventType == XMLStreamConstants.CHARACTERS
                        || eventType == XMLStreamConstants.CDATA
                        || eventType == XMLStreamConstants.SPACE
                        || eventType == XMLStreamConstants.ENTITY_REFERENCE) {
                    content.append(getText());
                } else if (eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                        || eventType == XMLStreamConstants.COMMENT) {
                    // skipping
                } else if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    throw new XMLStreamException(
                            "unexpected end of document when reading element text content");
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    throw new XMLStreamException(
                            "element text content may not contain START_ELEMENT");
                } else {
                    throw new XMLStreamException(
                            "Unexpected event type " + eventType, getLocation());
                }
                eventType = next();
            }
            return content.toString();
            ///////////////////////////////////////////////////////////////
        }

    }

    /**
     * Method next.
     *
     * @return Returns int.
     * @throws XMLStreamException
     */
    public int next() throws XMLStreamException {
        switch (state) {
            case DOCUMENT_COMPLETE:
                throw new NoSuchElementException("End of the document reached");
            case COMPLETED:
                state = DOCUMENT_COMPLETE;
                currentEvent = END_DOCUMENT;
                break;
            case SWITCH_AT_NEXT:
                state = SWITCHED;

                // load the parser
                try {
                    setParser((XMLStreamReader) builder.getParser());
                } catch (Exception e) {
                    throw new XMLStreamException("problem accessing the parser. " + e.getMessage(),
                                                 e);
                }

                // We should throw an END_DOCUMENT
                if ((currentEvent == START_DOCUMENT)
                        && (currentEvent == parser.getEventType())) {
                    currentEvent = parser.next();
                } else {
                    currentEvent = parser.getEventType();
                }
                updateCompleteStatus();
                break;
            case NAVIGABLE:
                currentEvent = generateEvents(currentNode);
                updateCompleteStatus();
                updateLastNode();
                break;
            case SWITCHED:
                if (parser.hasNext()) {
                    currentEvent = parser.next();
                }
                updateCompleteStatus();
                break;
            default:
                throw new OMStreamingException("unsuppported state!");
        }
        return currentEvent;
    }

    /**
     * Method getProperty.
     *
     * @param s
     * @return Returns Object.
     * @throws IllegalArgumentException
     */
    public Object getProperty(String s) throws IllegalArgumentException {
        Object value = XMLStreamReaderUtils.processGetProperty(this, s);
        if (value != null) {
            return value;
        }
        if (CharacterDataReader.PROPERTY.equals(s)) {
            return this;
        }
        if (parser != null) {
            return parser.getProperty(s);
        }
        // Delegate to the builder's parser.
        if (builder != null && builder instanceof StAXBuilder) {
            StAXBuilder staxBuilder = (StAXBuilder) builder;
            if (!staxBuilder.isClosed()) {
                // If the parser was closed by something other
                // than the builder, an IllegalStateException is
                // thrown.  For now, return null as this is unexpected
                // by the caller.
                try {
                    return ((StAXBuilder) builder).getReaderProperty(s);
                } catch (IllegalStateException ise) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * This is a very important method. It keeps the navigator one step ahead and pushes it one
     * event ahead. If the nextNode is null then navigable is set to false. At the same time the
     * parser and builder are set up for the upcoming event generation.
     *
     * @throws XMLStreamException
     */
    private void updateLastNode() throws XMLStreamException {
        lastNode = currentNode;
        attributeCount = -1;
        namespaceCount = -1;
        currentNode = nextNode;
        try {
            updateNextNode();
        } catch (Exception e) {
            throw new XMLStreamException(e);
        }
    }

    /** Method updateNextNode. */
    private void updateNextNode() {
        if (navigator.isNavigable()) {
            nextNode = navigator.getNext();
        } else {
            if (!switchingAllowed) {
                if (navigator.isCompleted() || builder == null || builder.isCompleted()) {
                    nextNode = null;
                    if (DEBUG_ENABLED) {
                        if (builder == null || builder.isCompleted()) {
                            log.debug("Builder is complete.  Next node is set to null.");
                        }
                    }
                } else {
                    builder.next();
                    navigator.step();
                    nextNode = navigator.getNext();
                }
            } else {
                //at this point check whether the navigator is done
                //if the navigator is done then we are fine and can directly
                // jump to the complete state ?
                if (navigator.isCompleted()) {
                    nextNode = null;
                } else {
                    // reset caching (the default is ON so it was not needed in the
                    // earlier case!
                    if (builder != null) {
                        builder.setCache(false);
                    }
                    state = SWITCH_AT_NEXT;
                }
            }
        }
    }

    /** Method updateCompleteStatus. */
    private void updateCompleteStatus() {
        if (state == NAVIGABLE) {
            if (rootNode == currentNode) {
                if (isFirst) {
                    isFirst = false;
                } else if (currentEvent == END_DOCUMENT) {
                    state = DOCUMENT_COMPLETE;
                } else {
                    state = COMPLETED;
                }
            }
        } else {
            if (state == SWITCHED && rootNode instanceof OMElement) {
                //this is a potential place for bugs
                //we have to test if the root node of this parser
                //has the same name for this test
                if (currentEvent == START_ELEMENT &&
                        (parser.getLocalName().equals(((OMElement)rootNode).getLocalName()))) {
                    ++depth;
                } else if (currentEvent == END_ELEMENT   &&
                       (parser.getLocalName().equals(((OMElement)rootNode).getLocalName())) ) {                                      
                    --depth;
                    if (depth < 0) {
                        state = COMPLETED;
                    }
                }
            }
            state = (currentEvent == END_DOCUMENT)
                    ? DOCUMENT_COMPLETE
                    : state;
        }
    }

    /**
     * Method getNamespaceContext.
     *
     * @return Returns NamespaceContext.
     */
    public NamespaceContext getNamespaceContext() {
        if (state==SWITCHED){
            return parser.getNamespaceContext();
        }
        return new MapBasedNamespaceContext(
                currentEvent == END_DOCUMENT ? Collections.EMPTY_MAP : getAllNamespaces(lastNode));
    }

    /**
     * Method getEncoding.
     *
     * @return Returns String.
     */
    public String getEncoding() {
        return null;
    }

    /**
     * Method getLocation.
     *
     * @return Returns Location.
     */
    public Location getLocation() {
        return DummyLocation.INSTANCE;
    }

    /**
     * Method getVersion.
     *
     * @return Returns String.
     */
    public String getVersion() {
        return "1.0"; // todo put the constant
    }

    /**
     * Method isStandalone.
     *
     * @return Returns boolean.
     */
    public boolean isStandalone() {
        return true;
    }

    /**
     * Method standaloneSet.
     *
     * @return Returns boolean.
     */
    public boolean standaloneSet() {
        return false;
    }

    /**
     * Method getCharacterEncodingScheme.
     *
     * @return Returns String.
     */
    public String getCharacterEncodingScheme() {
        if(builder != null) {
            return builder.getCharacterEncoding();
        }
        return "utf-8";
    }

    /**
     * Method getPITarget.
     *
     * @return Returns String.
     */
    public String getPITarget() {
        if (parser != null) {
            return parser.getPITarget();
        } else {
            if (currentEvent == PROCESSING_INSTRUCTION) {
                return ((OMProcessingInstruction)lastNode).getTarget();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * Method getPIData.
     *
     * @return Returns String.
     */
    public String getPIData() {
        if (parser != null) {
            return parser.getPIData();
        } else {
            if (currentEvent == PROCESSING_INSTRUCTION) {
                return ((OMProcessingInstruction)lastNode).getValue();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /*
     *
     * ################################################################
     * DataHandlerReader extension methods
     * ################################################################
     *
     */

    public boolean isBinary() {
        if (parser != null) {
            if (dataHandlerReader != null) {
                return dataHandlerReader.isBinary();
            } else {
                return false;
            }
        } else {
            if (lastNode instanceof OMText) {
                return ((OMText)lastNode).isBinary();
            } else {
                return false;
            }
        }
    }

    public boolean isOptimized() {
        if (parser != null) {
            if (dataHandlerReader != null) {
                return dataHandlerReader.isOptimized();
            } else {
                throw new IllegalStateException();
            }
        } else {
            if (lastNode instanceof OMText) {
                return ((OMText)lastNode).isOptimized();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public boolean isDeferred() {
        if (parser != null) {
            if (dataHandlerReader != null) {
                return dataHandlerReader.isDeferred();
            } else {
                throw new IllegalStateException();
            }
        } else {
            if (lastNode instanceof OMText) {
                // TODO: we should support deferred building of the DataHandler
                return false;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public String getContentID() {
        if (parser != null) {
            if (dataHandlerReader != null) {
                return dataHandlerReader.getContentID();
            } else {
                throw new IllegalStateException();
            }
        } else {
            if (lastNode instanceof OMText) {
                return ((OMText)lastNode).getContentID();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public DataHandler getDataHandler() throws XMLStreamException {
        if (parser != null) {
            if (dataHandlerReader != null) {
                return dataHandlerReader.getDataHandler();
            } else {
                throw new IllegalStateException();
            }
        } else {
            if (lastNode instanceof OMText) {
                return (DataHandler)((OMText)lastNode).getDataHandler();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public DataHandlerProvider getDataHandlerProvider() {
        if (parser != null) {
            if (dataHandlerReader != null) {
                return dataHandlerReader.getDataHandlerProvider();
            } else {
                throw new IllegalStateException();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    /*
     *
     * ################################################################
     * Generator methods for the OMNodes returned by the navigator
     * ################################################################
     *
     */

    /**
     * Method generateEvents.
     *
     * @param node
     * @return Returns int.
     */
    private int generateEvents(OMSerializable node) {
        if (node == null) {
            if (log.isDebugEnabled()) {
                log.debug("Node is null...returning END_DOCUMENT");
            }
            return END_DOCUMENT;
        }
        if (node instanceof OMDocument) {
            return generateContainerEvents((OMDocument)node, true);
        } else {
            int nodeType = ((OMNode)node).getType();
            if (nodeType == OMNode.ELEMENT_NODE) {
                return generateContainerEvents((OMElement)node, false);
            } else {
                return nodeType;
            }
        }
    }

    private int generateContainerEvents(OMContainer container, boolean isDocument) {
        if (nodeStack == null) {
            nodeStack = new Stack();
        }
        if (!nodeStack.isEmpty() && nodeStack.peek().equals(container)) {
            nodeStack.pop();
            return isDocument ? END_DOCUMENT : END_ELEMENT;
        } else {
            nodeStack.push(container);
            return isDocument ? START_DOCUMENT : START_ELEMENT;
        }
    }

    /*
     * ####################################################################
     * Other helper methods
     * ####################################################################
     */

    /**
     * Helper method getQName.
     *
     * @param element
     * @return Returns QName.
     */
    private QName getQName(OMElement element) {
        QName returnName;
        OMNamespace ns = element.getNamespace();
        String localPart = element.getLocalName();
        if (ns != null) {
            String prefix = ns.getPrefix();
            String uri = ns.getNamespaceURI();
            if ((prefix == null) || prefix.equals("")) {
                returnName = new QName(uri, localPart);
            } else {
                returnName = new QName(uri, localPart, prefix);
            }
        } else {
            returnName = new QName(localPart);
        }
        return returnName;
    }

    public void setParser(XMLStreamReader parser) {
        this.parser = parser;
        dataHandlerReader =
                parser == null ? null : XMLStreamReaderUtils.getDataHandlerReader(parser);
    }

    private Map getAllNamespaces(OMSerializable contextNode) {
        if (contextNode == null) {
            return Collections.EMPTY_MAP;
        }
        OMContainer context;
        if (contextNode instanceof OMContainer) {
            context = (OMContainer)contextNode;
        } else {
            context = ((OMNode)contextNode).getParent();
        }
        Map nsMap = new LinkedHashMap();
        while (context != null && !(context instanceof OMDocument)) {
            OMElement element = (OMElement) context;
            Iterator i = element.getAllDeclaredNamespaces();
            while (i != null && i.hasNext()) {
                addNamespaceToMap((OMNamespace) i.next(), nsMap);
            }
            if (element.getNamespace() != null) {
                addNamespaceToMap(element.getNamespace(), nsMap);
            }
            for (Iterator iter = element.getAllAttributes();
                 iter != null && iter.hasNext();) {
                OMAttribute attr = (OMAttribute) iter.next();
                if (attr.getNamespace() != null) {
                    addNamespaceToMap(attr.getNamespace(), nsMap);
                }
            }
            context = element.getParent();
        }
        return nsMap;
    }

    private void addNamespaceToMap(OMNamespace ns, Map map) {
        if (map.get(ns.getPrefix()) == null) {
            map.put(ns.getPrefix(), ns.getNamespaceURI());
        }
    }

    public OMXMLParserWrapper getBuilder() {
        return builder;
    }
    
    /**
     * @return if parser is closed
     */
    public boolean isClosed() {
        
        // If there is a builder, the builder owns the parser
        // and knows the isClosed status
        if (builder != null && builder instanceof StAXBuilder) {
           return ((StAXBuilder) builder).isClosed();
        } else {
            return _isClosed;
        }
    }
    
    /**
     * Indicate if the parser resource should be release when closed.
     * @param value boolean
     */
    public void releaseParserOnClose(boolean value) {
        // if there is a StAXBuilder, it owns the parser 
        // and controls the releaseOnClose status
        if (builder != null && builder instanceof StAXBuilder) {
            ((StAXBuilder) builder).releaseParserOnClose(value);
            if (isClosed() && value) {
                setParser(null);
            }
            return;
        } else {
            // Release parser if already closed
            if (isClosed() && value) {
                setParser(null);
            }
            _releaseParserOnClose = value;
        }
        
    }
    
    /**
     * @return OMDataSource associated with the current node or Null
     */
    public OMDataSource getDataSource() {
        if (getEventType() != XMLStreamReader.START_ELEMENT ||
                !(state == this.NAVIGABLE || 
                  state == this.SWITCH_AT_NEXT)) {
            return null;
        }
        OMDataSource ds = null;
        if (lastNode != null &&
            lastNode instanceof OMSourcedElement) {
            try {
                ds = ((OMSourcedElement) lastNode).getDataSource();
            } catch (UnsupportedOperationException e) {
                // Some implementations throw an UnsupportedOperationException.
                ds =null;
            }
            if (log.isDebugEnabled()) {
                if (ds != null) {
                    log.debug("OMSourcedElement exposed an OMDataSource." + ds);
                } else {
                    log.debug("OMSourcedElement does not have a OMDataSource.");
                }
            }
        }
        return ds;
    }
    
    /**
     * Enable if an OMSourcedElement with an OMDataSource should be treated as a
     * leaf node.  Disable (the default) if the OMDataSource should be parsed and
     * converted into events.
     * @param value boolean
     */
    public void enableDataSourceEvents(boolean value) {
        navigator.setDataSourceIsLeaf(value);
    }
}
