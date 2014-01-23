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

package org.apache.axis2.databinding.utils.writer;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.databinding.utils.ConverterUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;


public class MTOMAwareOMBuilderTest extends TestCase {

    private int prefixNum;

    public void testGetOMElement() {

        MTOMAwareOMBuilder writer = new MTOMAwareOMBuilder();

        try {

            writeStartElement(null, "test1", writer);
            writeStartElement("http://apach.temp.uri", "test2", writer);
            writeStartElement("http://apach.temp.uri1", "test3", writer);
            writer.writeCharacters("test string3");
            writeAttribute("http://apach.temp.uri", "attribute1", "attrubteValue1", writer);
            writeQNames(new QName[]{new QName("http://axis2.apach.org1", "testQName"),
                    new QName("http://axis2.apach.org2", "testQName2")}, writer);
            writer.writeEndElement();
            writeStartElement("http://apach.temp.uri1", "test4", writer);
            writeAttribute(null, "attribute1", "attrubteValue1", writer);
            writeAttribute("ns1", "http://apach.temp.uri", "attribute1", "attrubteValue1", writer);
            writer.writeCharacters("test string4");
            writer.writeEndElement();
            writeStartElement("http://temp.new.org", "testattributeElement", writer);
            writeQNameAttribute(null, "testQname", new QName("http://temp.new.org", "testQName"), writer);
            writeQName(new QName("http://axis2.apach.org", "testQName"), writer);
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();

            OMElement omElement = writer.getOMElement();
            System.out.println("OM String ==> " + omElement.toString());

            XMLStreamReader xmlReader = omElement.getXMLStreamReader();
            StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(xmlReader);
            OMElement result = stAXOMBuilder.getDocumentElement();

            System.out.println("OM result ==> " + result.toString());
        } catch (XMLStreamException e) {
            fail();
        }

    }

    private String generatePrefix(String string) {
        return "ns" + ++prefixNum;
    }

    private void writeStartElement(String namespace, String localName, XMLStreamWriter xmlWriter) throws XMLStreamException {

        if ((namespace != null) && ! namespace.equals("")) {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null) {
                prefix = "ns" + ++prefixNum;
                xmlWriter.writeStartElement(prefix, localName, namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            } else {
                xmlWriter.writeStartElement(namespace, localName);
            }
        } else {
            xmlWriter.writeStartElement(localName);
        }
    }

    private void writeAttribute(String prefix, String namespace, String attName,
                                String attValue, XMLStreamWriter xmlWriter)
            throws XMLStreamException {
        if (xmlWriter.getPrefix(namespace) == null) {
            xmlWriter.writeNamespace(prefix, namespace);
            xmlWriter.setPrefix(prefix, namespace);

        }
        xmlWriter.writeAttribute(namespace, attName, attValue);
    }

    private void writeAttribute(String namespace, String attName,
                                String attValue, XMLStreamWriter xmlWriter) throws XMLStreamException {
        if ((namespace == null) || namespace.equals("")) {
            xmlWriter.writeAttribute(attName, attValue);
        } else {
            registerPrefix(xmlWriter, namespace);
            xmlWriter.writeAttribute(namespace, attName, attValue);
        }
    }

    private void writeQNameAttribute(String namespace, String attName,
                                     QName qname, XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

        String attributeNamespace = qname.getNamespaceURI();
        String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
        if (attributePrefix == null) {
            attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
        }
        java.lang.String attributeValue;
        if (attributePrefix.trim().length() > 0) {
            attributeValue = attributePrefix + ":" + qname.getLocalPart();
        } else {
            attributeValue = qname.getLocalPart();
        }

        if ((namespace != null) && namespace.equals("")) {
            xmlWriter.writeAttribute(attName, attributeValue);
        } else {
            if (namespace != null) {
                registerPrefix(xmlWriter, namespace);
            }
            xmlWriter.writeAttribute(namespace, attName, attributeValue);
        }
    }

    private void writeQName(QName qname,
                            XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
        String namespaceURI = qname.getNamespaceURI();
        if (namespaceURI != null) {
            String prefix = xmlWriter.getPrefix(namespaceURI);
            if (prefix == null) {
                prefix = generatePrefix(namespaceURI);
                xmlWriter.writeNamespace(prefix, namespaceURI);
                xmlWriter.setPrefix(prefix, namespaceURI);
            }

            if (prefix.trim().length() > 0) {
                xmlWriter.writeCharacters(prefix + ":" + ConverterUtil.convertToString(qname));
            } else {
                // i.e this is the default namespace
                xmlWriter.writeCharacters(ConverterUtil.convertToString(qname));
            }

        } else {
            xmlWriter.writeCharacters(ConverterUtil.convertToString(qname));
        }
    }

    private void writeQNames(QName[] qnames,
                             XMLStreamWriter xmlWriter) throws XMLStreamException {

        if (qnames != null) {
            // we have to store this data until last moment since it is not possible to write any
            // namespace data after writing the charactor data
            StringBuffer stringToWrite = new StringBuffer();
            String namespaceURI = null;
            String prefix = null;

            for (int i = 0; i < qnames.length; i++) {
                if (i > 0) {
                    stringToWrite.append(" ");
                }
                namespaceURI = qnames[i].getNamespaceURI();
                if (namespaceURI != null) {
                    prefix = xmlWriter.getPrefix(namespaceURI);
                    if ((prefix == null) || (prefix.length() == 0)) {
                        prefix = generatePrefix(namespaceURI);
                        xmlWriter.writeNamespace(prefix, namespaceURI);
                        xmlWriter.setPrefix(prefix, namespaceURI);
                    }

                    if (prefix.trim().length() > 0) {
                        stringToWrite.append(prefix).append(":").append(ConverterUtil.convertToString(qnames[i]));
                    } else {
                        stringToWrite.append(ConverterUtil.convertToString(qnames[i]));
                    }
                } else {
                    stringToWrite.append(ConverterUtil.convertToString(qnames[i]));
                }
            }
            xmlWriter.writeCharacters(stringToWrite.toString());
        }

    }


    private String registerPrefix(XMLStreamWriter xmlWriter, String namespace) throws XMLStreamException {
        String prefix = xmlWriter.getPrefix(namespace);

        if (prefix == null) {
            prefix = generatePrefix(namespace);
            while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                prefix = BeanUtil.getUniquePrefix();
            }
            xmlWriter.writeNamespace(prefix, namespace);
            xmlWriter.setPrefix(prefix, namespace);
        }
        return prefix;
    }

}
