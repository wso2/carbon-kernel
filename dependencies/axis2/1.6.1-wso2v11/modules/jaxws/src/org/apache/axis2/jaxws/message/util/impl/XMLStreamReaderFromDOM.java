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


package org.apache.axis2.jaxws.message.util.impl;

import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.util.SAAJConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * XMLStreamReader created from walking a DOM. This is an implementation class used by
 * SOAPElementReader.
 *
 * @see org.apache.axis2.jaxws.util.SOAPElementReader
 */
public class XMLStreamReaderFromDOM implements XMLStreamReader {
	private static final Log log = LogFactory.getLog(XMLStreamReaderFromDOM.class);

    private Node cursor;
    private Stack<Node> nextCursorStack = new Stack<Node>();
    private Node root;
    private int event = XMLStreamReader.START_DOCUMENT;
    private Node nextCursor = null;
    private int nextEvent = -1;

    private NamespaceContextFromDOM cacheNCI = null;
    private Element cacheNCIKey = null;

    private List cacheND = null;
    private Element cacheNDKey = null;


    /**
     * Create the XMLStreamReader with an Envelope
     *
     * @param envelope Element (probably an SAAJ SOAPEnvelope) representing the Envelope
     */
    public XMLStreamReaderFromDOM(Element envelope) {
        root = envelope;
        cursor = root;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getProperty(java.lang.String)
      */
    public Object getProperty(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException(Messages.getMessage("XMLSRErr1"));
        }
        return null;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#next()
      */
    public int next() throws XMLStreamException {
        if (!hasNext()) {
            throw new XMLStreamException(Messages.getMessage("XMLSRErr2"));
        }
        getNext();
        cursor = nextCursor;
        event = nextEvent;
        return event;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#require(int, java.lang.String, java.lang.String)
      */
    public void require(int event, String namespace, String localPart)
            throws XMLStreamException {
        try {
            if (event != this.event) {
                throw new XMLStreamException(Messages.getMessage("XMLSRErr3", String.valueOf(event),
                                                                 String.valueOf(this.event)));
            }
            if (namespace != null &&
                    !namespace.equals(cursor.getNamespaceURI())) {
                throw new XMLStreamException(
                        Messages.getMessage("XMLSRErr3", namespace, this.cursor.getNamespaceURI()));
            }
            if (localPart != null &&
                    !localPart.equals(cursor.getLocalName())) {
                throw new XMLStreamException(
                        Messages.getMessage("XMLSRErr3", localPart, this.cursor.getLocalName()));
            }
        } catch (XMLStreamException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLStreamException(e);
        }
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getElementText()
      */
    public String getElementText() throws XMLStreamException {
        if (event == XMLStreamReader.START_ELEMENT) {
            next();
            StringBuffer buffer = new StringBuffer();
            while (event != XMLStreamReader.END_ELEMENT) {
                if (event == XMLStreamReader.CHARACTERS ||
                        event == XMLStreamReader.CDATA ||
                        event == XMLStreamReader.SPACE ||
                        event == XMLStreamReader.ENTITY_REFERENCE) {
                    buffer.append(getText());
                } else if (event == XMLStreamReader.PROCESSING_INSTRUCTION ||
                        event == XMLStreamReader.COMMENT) {
                    // whitespace
                } else {
                    throw new XMLStreamException(
                            Messages.getMessage("XMLSRErr4", "getElementText()"));
                }
                next();
            }
            return buffer.toString();
        }
        throw new XMLStreamException(Messages.getMessage("XMLSRErr4", "getElementText()"));
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#nextTag()
      */
    public int nextTag() throws XMLStreamException {
        next();
        while (event == XMLStreamReader.CHARACTERS && isWhiteSpace() ||
                event == XMLStreamReader.CDATA && isWhiteSpace() ||
                event == XMLStreamReader.SPACE ||
                event == XMLStreamReader.PROCESSING_INSTRUCTION ||
                event == XMLStreamReader.COMMENT) {
            event = next();
        }
        if (event == XMLStreamReader.START_ELEMENT ||
                event == XMLStreamReader.END_ELEMENT) {
            return event;
        }
        throw new XMLStreamException(Messages.getMessage("XMLSRErr4", "nextTag()"));
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#hasNext()
      */
    public boolean hasNext() throws XMLStreamException {
        return (event != XMLStreamReader.END_DOCUMENT);
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#close()
      */
    public void close() throws XMLStreamException {
        return;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getNamespaceURI(java.lang.String)
      */
    public String getNamespaceURI(String prefix) {
        if (cursor instanceof Element) {
            return getNamespaceContext().getNamespaceURI(prefix);
        }
        throw new IllegalStateException(
                Messages.getMessage("XMLSRErr4", "getNamespaceURI(String)"));
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#isStartElement()
      */
    public boolean isStartElement() {
        return (event == XMLStreamReader.START_ELEMENT);
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#isEndElement()
      */
    public boolean isEndElement() {
        return (event == XMLStreamReader.END_ELEMENT);
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#isCharacters()
      */
    public boolean isCharacters() {
        return (event == XMLStreamReader.CHARACTERS);
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#isWhiteSpace()
      */
    public boolean isWhiteSpace() {
        if (event == XMLStreamReader.CHARACTERS ||
                event == XMLStreamReader.CDATA) {
            String value = ((CharacterData)cursor).getData();
            StringTokenizer st = new StringTokenizer(value);
            return !(st.hasMoreTokens());
        }
        return false;
    }

    /** @return list of attributes that are not namespace declarations */
    private List getAttributes() {
        if (event == XMLStreamReader.START_ELEMENT) {
            List attrs = new ArrayList();
            NamedNodeMap map = ((Element)cursor).getAttributes();
            if (map != null) {
                for (int i = 0; i < map.getLength(); i++) {
                    Attr attr = (Attr)map.item(i);
                    if (attr.getName().equals("xmlns") ||
                            attr.getName().startsWith("xmlns:")) {
                        // this is a namespace declaration
                    } else {
                    	if (log.isDebugEnabled()) {
                            log.debug("Attr string: " + attr.toString());
                        }
                        attrs.add(attr);
                    }
                }
            }
            return attrs;
        }
        throw new IllegalStateException(Messages.getMessage("XMLSRErr4", "getAttributes()"));
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getAttributeValue(java.lang.String, java.lang.String)
      */
    public String getAttributeValue(String namespace, String localPart) {
        if (event == XMLStreamReader.START_ELEMENT) {
            return ((Element)cursor).getAttributeNS(namespace, localPart);
        }
        throw new IllegalStateException(
                Messages.getMessage("XMLSRErr4", "getAttributeValue(String, String)"));
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getAttributeCount()
      */
    public int getAttributeCount() {
        return getAttributes().size();
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getAttributeName(int)
      */
    public QName getAttributeName(int index) {
        Attr attr = (Attr)getAttributes().get(index);
        return new QName(attr.getNamespaceURI(), attr.getLocalName());
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getAttributeNamespace(int)
      */
    public String getAttributeNamespace(int index) {
        Attr attr = (Attr)getAttributes().get(index);
        return attr.getNamespaceURI();
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getAttributeLocalName(int)
      */
    public String getAttributeLocalName(int index) {
        Attr attr = (Attr)getAttributes().get(index);
        return attr.getLocalName();
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getAttributePrefix(int)
      */
    public String getAttributePrefix(int index) {
        Attr attr = (Attr)getAttributes().get(index);
        return attr.getPrefix();
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getAttributeType(int)
      */
    public String getAttributeType(int index) {
        String attrType = null;    
        Attr attr = (Attr)getAttributes().get(index);
        TypeInfo typeInfo = attr.getSchemaTypeInfo();
	    if (typeInfo != null) {
	    	attrType = typeInfo.getTypeName();
        }
	    
        if (attrType == null) {
            try {
                attrType = (String) attr.getUserData(SAAJConverter.OM_ATTRIBUTE_KEY);
                if (log.isDebugEnabled()) {
                	log.debug("Retrieving attrType from UserData: " + attrType);
                }
            } catch (Exception e) {
           	    if (log.isDebugEnabled()) {
         		    log.debug("An error occured while getting attrType: " + e.getMessage());
         	    }
            }
        }
              
        return attrType;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getAttributeValue(int)
      */
    public String getAttributeValue(int index) {
        Attr attr = (Attr)getAttributes().get(index);
        return attr.getValue();
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#isAttributeSpecified(int)
      */
    public boolean isAttributeSpecified(int arg0) {
        return true;
    }

    /*
      * @return number of namespace declarations on this element
      */
    public int getNamespaceCount() {
        if (cursor instanceof Element) {
            List list = getNamespaceDeclarations();
            return list.size();
        }
        throw new IllegalStateException(Messages.getMessage("XMLSRErr4", "getNamespaceCount()"));
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getNamespacePrefix(int)
      */
    public String getNamespacePrefix(int index) {
        if (cursor instanceof Element) {
            List list = getNamespaceDeclarations();
            return ((NamespaceDeclare)list.get(index)).getPrefix();
        }
        throw new IllegalStateException(
                Messages.getMessage("XMLSRErr4", "getNamespacePrefix(int)"));
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getNamespaceURI(int)
      */
    public String getNamespaceURI(int index) {
        if (cursor instanceof Element) {
            List list = getNamespaceDeclarations();
            return ((NamespaceDeclare)list.get(index)).getURI();
        }
        throw new IllegalStateException(Messages.getMessage("XMLSRErr4", "getNamespaceURI(int)"));
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getNamespaceContext()
      */
    public NamespaceContext getNamespaceContext() {
        Element element = null;
        if (cursor instanceof Element) {
            element = (Element)cursor;
        } else {
            Element parent = (Element)cursor.getParentNode();
            if (parent == null) {
                parent = (Element)nextCursorStack.peek();
            }
            element = (Element)cursor.getParentNode();
        }
        if (element == cacheNCIKey) {
            return cacheNCI;
        }
        cacheNCIKey = element;
        cacheNCI = new NamespaceContextFromDOM(element);
        return cacheNCI;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getEventType()
      */
    public int getEventType() {
        return event;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getText()
      */
    public String getText() {
        if (event == XMLStreamReader.CHARACTERS ||
                event == XMLStreamReader.CDATA ||
                event == XMLStreamReader.COMMENT) {
            return ((CharacterData)cursor).getData();
        }
        throw new IllegalStateException(Messages.getMessage("XMLSRErr4", "getText()"));
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getTextCharacters()
      */
    public char[] getTextCharacters() {
        return getText().toCharArray();
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getTextCharacters(int, char[], int, int)
      */
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length)
            throws XMLStreamException {
        String value = getText();
        // Calculate the sourceEnd index
        int sourceEnd = sourceStart + length;
        if (value.length() < sourceEnd) {
            sourceEnd = value.length();
        }
        value.getChars(sourceStart, sourceEnd, target, targetStart);
        return sourceEnd - sourceStart;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getTextStart()
      */
    public int getTextStart() {
        return 0;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getTextLength()
      */
    public int getTextLength() {
        return getText().length();
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getEncoding()
      */
    public String getEncoding() {
        return null;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#hasText()
      */
    public boolean hasText() {
        return (event == XMLStreamReader.CHARACTERS ||
                event == XMLStreamReader.CDATA ||
                event == XMLStreamReader.COMMENT);
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getLocation()
      */
    public Location getLocation() {
        return dummyLocation;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getName()
      */
    public QName getName() {
        if (cursor instanceof Element) {
            return new QName(cursor.getNamespaceURI(), cursor.getLocalName());
        }
        throw new IllegalStateException(Messages.getMessage("XMLSRErr4", "getName()"));
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getLocalName()
      */
    public String getLocalName() {
        if (cursor instanceof Element) {
            return cursor.getLocalName();
        }
        throw new IllegalStateException(Messages.getMessage("XMLSRErr4", "getLocalName()"));
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#hasName()
      */
    public boolean hasName() {
        return (isStartElement() || isEndElement());
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getNamespaceURI()
      */
    public String getNamespaceURI() {
        if (cursor instanceof Element) {
            return cursor.getNamespaceURI();
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getPrefix()
      */
    public String getPrefix() {
        if (cursor instanceof Element) {
            return cursor.getPrefix();
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getVersion()
      */
    public String getVersion() {
        return null;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#isStandalone()
      */
    public boolean isStandalone() {
        return false;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#standaloneSet()
      */
    public boolean standaloneSet() {
        return false;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getCharacterEncodingScheme()
      */
    public String getCharacterEncodingScheme() {
        return null;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getPITarget()
      */
    public String getPITarget() {
        return null;
    }

    /* (non-Javadoc)
      * @see javax.xml.stream.XMLStreamReader#getPIData()
      */
    public String getPIData() {
        return null;
    }

    /** Sets nextCursor and nextEvent from using the current cursor and event. */
    private void getNext() throws IllegalStateException {
        switch (event) {
            case XMLStreamReader.START_DOCUMENT: {
                nextCursor = cursor;
                nextEvent = XMLStreamReader.START_ELEMENT;
                break;
            }

            case XMLStreamReader.START_ELEMENT: {
                if (cursor.getFirstChild() != null) {
                    nextCursorStack.push(nextCursor);
                    nextCursor = cursor.getFirstChild();
                    nextEvent = startEvent(nextCursor);
                } else {
                    nextEvent = XMLStreamReader.END_ELEMENT;
                }
                break;
            }
            case XMLStreamReader.ATTRIBUTE: {
                throw new IllegalStateException(Messages.getMessage("XMLSRErr5", "ATTRIBUTE"));
            }
            case XMLStreamReader.NAMESPACE: {
                throw new IllegalStateException(Messages.getMessage("XMLSRErr5", "NAMESPACE"));
            }
            case XMLStreamReader.END_ELEMENT:
            case XMLStreamReader.CHARACTERS:
            case XMLStreamReader.CDATA:
            case XMLStreamReader.COMMENT:
            case XMLStreamReader.SPACE:
            case XMLStreamReader.PROCESSING_INSTRUCTION:
            case XMLStreamReader.ENTITY_REFERENCE:
            case XMLStreamReader.DTD: {
                if (cursor.getNextSibling() != null) {
                    nextCursor = cursor.getNextSibling();
                    nextEvent = startEvent(nextCursor);
                } else if (cursor == root) {
                    nextEvent = XMLStreamReader.END_DOCUMENT;
                } else {
                    // The following does not work with
                    // Axiom Text nodes
                    // nextCursor = cursor.getParentNode();
                    // This is the reason why a stack is used.
                    nextCursor = nextCursorStack.pop();

                    nextEvent = XMLStreamReader.END_ELEMENT;
                }
                break;
            }

            case XMLStreamReader.END_DOCUMENT: {
                nextCursor = null;
                nextEvent = -1;
            }
            default:
                throw new IllegalStateException(
                        Messages.getMessage("XMLSRErr5", String.valueOf(event)));
        }

    }

    /**
     * Returns the start event for this particular node
     *
     * @param node
     * @return
     */
    private int startEvent(Node node) {
        if (node instanceof ProcessingInstruction) {
            return XMLStreamReader.PROCESSING_INSTRUCTION;
        }
        if (node instanceof CDATASection) {
            return XMLStreamReader.CDATA;
        }
        if (node instanceof Comment) {
            return XMLStreamReader.COMMENT;
        }
        if (node instanceof Text) {
            if (node instanceof javax.xml.soap.Text) {
                javax.xml.soap.Text soapText = (javax.xml.soap.Text)node;
                if (soapText.isComment()) {
                    return XMLStreamReader.COMMENT;
                } else {
                    return XMLStreamReader.CHARACTERS;
                }
            }
            return XMLStreamReader.CHARACTERS;
        }
        if (node instanceof Element) {
            return XMLStreamReader.START_ELEMENT;
        }
        if (node instanceof Attr) {
            return XMLStreamReader.ATTRIBUTE;
        }
        if (node instanceof Document) {
            return XMLStreamReader.START_DOCUMENT;
        }
        if (node instanceof EntityReference) {
            return XMLStreamReader.ENTITY_REFERENCE;
        }
        if (node instanceof DocumentType) {
            return XMLStreamReader.DTD;
        }
        return -1;
    }

    // This is the definition of a dummy Location
    private DummyLocation dummyLocation = new DummyLocation();

    private class DummyLocation implements Location {

        public int getLineNumber() {
            return -1;
        }

        public int getColumnNumber() {
            return 0;
        }

        public int getCharacterOffset() {
            return 0;
        }

        public String getPublicId() {
            return null;
        }

        public String getSystemId() {
            return null;
        }

    }

    public List getNamespaceDeclarations() {
        Element element = null;
        if (cursor instanceof Element) {
            element = (Element)cursor;
        } else {
            return new ArrayList();
        }
        if (element == cacheNDKey) {
            return cacheND;
        }
        cacheNDKey = element;
        cacheND = new ArrayList();
        NamedNodeMap attrs = element.getAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr)attrs.item(i);
                String name = attr.getNodeName();
                if (name.startsWith("xmlns")) {
                    String prefix = "";
                    if (name.startsWith("xmlns:")) {
                        prefix = name.substring(6);
                    }
                    NamespaceDeclare nd = new NamespaceDeclare(prefix, attr.getNodeValue());
                    cacheND.add(nd);
                }
            }
        }
        return cacheND;
    }

    class NamespaceDeclare {
        String prefix;
        String uri;

        NamespaceDeclare(String prefix, String uri) {
            this.prefix = prefix;
            this.uri = uri;
        }

        String getPrefix() {
            return prefix;
        }

        String getURI() {
            return uri;
        }
    }

    Node getNode() {
        return cursor;
	}
	
}
