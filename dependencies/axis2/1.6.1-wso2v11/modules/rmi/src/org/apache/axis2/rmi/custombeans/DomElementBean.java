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

package org.apache.axis2.rmi.custombeans;

import org.apache.axis2.rmi.databind.AbstractRMIBean;
import org.apache.axis2.rmi.databind.JavaObjectSerializer;
import org.apache.axis2.rmi.databind.XmlStreamParser;
import org.apache.axis2.rmi.exception.XmlParsingException;
import org.apache.axis2.rmi.exception.XmlSerializingException;
import org.apache.axis2.rmi.util.Constants;
import org.apache.axis2.rmi.util.NamespacePrefix;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * this class is used to handle dom element related things.
 */

public class DomElementBean extends AbstractRMIBean {

    private Element domElement;

    public DomElementBean(Element domElement) {
        this.domElement = domElement;
    }

    public void serialize(XMLStreamWriter writer,
                          JavaObjectSerializer serializer,
                          QName parentQName,
                          NamespacePrefix namespacePrefix)
            throws XMLStreamException, XmlSerializingException {
        // first write the start element for parent
        writeStartElement(writer,
                parentQName.getNamespaceURI(),
                parentQName.getLocalPart(),
                namespacePrefix);
        serializeDomElement(this.domElement,writer,namespacePrefix);
        writer.writeEndElement();

    }

    public static Object parse(XMLStreamReader reader,
                               XmlStreamParser parser)
            throws XMLStreamException, XmlParsingException {
        // this must be in the start state
        while(!reader.isStartElement()){
            reader.next();
        }

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Element domElement = null;
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            // check whether the element is null or not
            String nillable = reader.getAttributeValue(Constants.URI_DEFAULT_SCHEMA_XSI,"nil");
            if ("true".equals(nillable) || "1".equals(nillable)){
                while(!reader.isEndElement()){
                    reader.next();
                }
            } else {
               // point to the next element
               reader.next();
               // since this is a document element reader must be at the start element of the
               // dom document
               if (reader.isStartElement()){
                  domElement = getDOMElement(reader,document,new HashMap(),new NamespacePrefix());
               } else {
                  throw new XmlParsingException("Dom element is not point to a start element");
               }
            }
        } catch (ParserConfigurationException e) {
            throw new XmlParsingException("Error while creating the document factory ",e);
        }
        return domElement;
    }

    private static  Element getDOMElement(XMLStreamReader reader,
                                          Document document,
                                          Map namespaceToPrefixMap,
                                          NamespacePrefix namespacePrefix)
            throws XMLStreamException, XmlParsingException {
        // reader must be at a start element
        while(!reader.isStartElement()){
            reader.next();
        }
        if (!namespaceToPrefixMap.containsKey(reader.getNamespaceURI())){
            namespaceToPrefixMap.put(reader.getNamespaceURI(),
                    "ns" + namespacePrefix.getNamesapcePrefix());
        }
        QName elementQName = reader.getName();
        String prefix = (String) namespaceToPrefixMap.get(reader.getNamespaceURI());
        Element element = document.createElementNS(reader.getNamespaceURI(),
                prefix + ":" + reader.getLocalName());

        // set the attributes
        Attr attribute = null;
        String attributeNamespace = null;
        String attributePrefix = null;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            attributeNamespace = reader.getAttributeNamespace(i);
            if ((attributeNamespace == null) || attributeNamespace.equals("")) {
                // this attribute has no namespace
                element.setAttribute(reader.getAttributeLocalName(i),reader.getAttributeValue(i));
            } else {
                attributeNamespace = reader.getAttributeNamespace(i);
                if (!namespaceToPrefixMap.containsKey(attributeNamespace)) {
                    namespaceToPrefixMap.put(attributeNamespace,
                            "ns" + namespacePrefix.getNamesapcePrefix());
                }
                attributePrefix = (String) namespaceToPrefixMap.get(attributeNamespace);
                element.setAttributeNS(attributeNamespace,
                        attributePrefix + ":" + reader.getAttributeLocalName(i),
                        reader.getAttributeValue(i));

            }
        }
        reader.next();
        int state;
        // at the end we have to point the reader to end of this element.
        while (!reader.isEndElement() || !reader.getName().equals(elementQName)) {
            state = reader.getEventType();
            if (state == XMLStreamConstants.START_ELEMENT) {
                element.appendChild(getDOMElement(reader, document, namespaceToPrefixMap, namespacePrefix));
            } else if (state == XMLStreamConstants.CHARACTERS) {
                element.appendChild(document.createTextNode(reader.getText()));
            }
            reader.next();
        }
        return element;
    }

    private void serializeDomElement(Element domElement,
                                     XMLStreamWriter writer,
                                     NamespacePrefix namespacePrefix) throws XMLStreamException, XmlSerializingException {

        // first write the start element
        writeStartElement(writer,domElement.getNamespaceURI(),domElement.getLocalName(),namespacePrefix);
        // write the attributes writer now at the start element
        NamedNodeMap attributes = domElement.getAttributes();
        Attr attribute;
        QName attributeQName;
        for (int i = 0; i < attributes.getLength(); i++) {
            attribute = (Attr) attributes.item(i);
            attributeQName = new QName(attribute.getNamespaceURI(), attribute.getName());
            writeAttribute(writer, attribute.getValue(), attributeQName, namespacePrefix);
        }

        // write the other children
        NodeList nodeList = domElement.getChildNodes();
        Node node;
        Text textNode;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node instanceof Element) {
                // write the element
                serializeDomElement((Element) node, writer, namespacePrefix);
            } else if (node instanceof Text) {
                textNode = (Text) node;
                writer.writeCharacters(textNode.getNodeValue());
            } else {
                throw new XmlSerializingException("Unknown dom element node found node type ==>"
                        + node.getNodeType());
            }
        }
        writer.writeEndElement();
    }
}
