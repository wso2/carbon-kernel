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

package org.apache.axiom.om.impl.serialize;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/** Class StreamWriterToContentHandlerConverter */
public class StreamWriterToContentHandlerConverter implements ContentHandler {
    /** Field log */
    private static Log log = LogFactory.getLog(StreamWriterToContentHandlerConverter.class);

    /** Field writer */
    private XMLStreamWriter writer;

    /**
     * Constructor StreamWriterToContentHandlerConverter.
     *
     * @param writer
     */
    public StreamWriterToContentHandlerConverter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    /**
     * Method endDocument.
     *
     * @throws SAXException
     */
    public void endDocument() throws SAXException {

        // do nothing
    }

    /**
     * Method startDocument.
     *
     * @throws SAXException
     */
    public void startDocument() throws SAXException {

        // 
    }

    /**
     * Method characters.
     *
     * @param ch
     * @param start
     * @param length
     * @throws SAXException
     */
    public void characters(char ch[], int start, int length)
            throws SAXException {
        try {
            writer.writeCharacters(ch, start, length);
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Method ignorableWhitespace.
     *
     * @param ch
     * @param start
     * @param length
     * @throws SAXException
     */
    public void ignorableWhitespace(char ch[], int start, int length)
            throws SAXException {

        // throw new UnsupportedOperationException();
    }

    /**
     * Method endPrefixMapping.
     *
     * @param prefix
     * @throws SAXException
     */
    public void endPrefixMapping(String prefix) throws SAXException {

        // throw new UnsupportedOperationException();
    }

    /**
     * Method skippedEntity.
     *
     * @param name
     * @throws SAXException
     */
    public void skippedEntity(String name) throws SAXException {

        // throw new UnsupportedOperationException();
    }

    /**
     * Method setDocumentLocator.
     *
     * @param locator
     */
    public void setDocumentLocator(Locator locator) {

        // throw new UnsupportedOperationException();
    }

    /**
     * Method processingInstruction.
     *
     * @param target
     * @param data
     * @throws SAXException
     */
    public void processingInstruction(String target, String data)
            throws SAXException {

        // throw new UnsupportedOperationException();
    }

    /**
     * Method startPrefixMapping.
     *
     * @param prefix
     * @param uri
     * @throws SAXException
     */
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        try {
            writer.writeNamespace(prefix, uri);
            writer.setPrefix(prefix, uri);
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Method endElement.
     *
     * @param namespaceURI
     * @param localName
     * @param qName
     * @throws SAXException
     */
    public void endElement(String namespaceURI,
                           String localName,
                           String qName)
            throws SAXException {
        try {
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Method getPrefix.
     *
     * @param qName
     * @return Returns String.
     */
    private String getPrefix(String qName) {
        if (qName != null) {
            return qName.substring(0, qName.indexOf(":"));
        }
        return null;
    }

    /**
     * Method startElement.
     *
     * @param namespaceURI
     * @param localName
     * @param qName
     * @param atts
     * @throws SAXException
     */
    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException {
        try {
            log.info("writing element {" + namespaceURI + '}' + localName
                    + " directly to stream ");
            String prefix = getPrefix(qName);

            // it is only the prefix we want to learn from the QName! so we can get rid of the
            // spliting QName
            if (prefix == null) {
                writer.writeStartElement(namespaceURI, localName);
            } else {
                writer.writeStartElement(prefix, localName, namespaceURI);
            }
            if (atts != null) {
                int attCount = atts.getLength();
                for (int i = 0; i < attCount; i++) {
                    writer.writeAttribute(atts.getURI(i), localName,
                                          atts.getValue(i));
                }
            }
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }
}
