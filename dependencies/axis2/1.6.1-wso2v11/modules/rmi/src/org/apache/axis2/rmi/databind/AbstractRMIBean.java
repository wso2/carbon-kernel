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

package org.apache.axis2.rmi.databind;

import org.apache.axis2.rmi.exception.XmlSerializingException;
import org.apache.axis2.rmi.util.Constants;
import org.apache.axis2.rmi.util.NamespacePrefix;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


public abstract class AbstractRMIBean implements RMIBean {

    public abstract void serialize(XMLStreamWriter writer,
                                   JavaObjectSerializer serializer,
                                   QName parentQName,
                                   NamespacePrefix namespacePrefix)
            throws XMLStreamException, XmlSerializingException;

    protected void writeStartElement(XMLStreamWriter writer,
                                     String namespace,
                                     String localPart,
                                     NamespacePrefix namespacePrefix)
            throws XMLStreamException {
        if (!namespace.equals("")) {
            String prefix = writer.getPrefix(namespace);
            if (prefix == null) {
                prefix = "ns" + namespacePrefix.getNamesapcePrefix();
                writer.writeStartElement(prefix, localPart, namespace);
                writer.writeNamespace(prefix, namespace);
                writer.setPrefix(prefix, namespace);
            } else {
                writer.writeStartElement(namespace, localPart);
            }
        } else {
            writer.writeStartElement(localPart);
        }
    }

    protected void writeTypeAttribute(XMLStreamWriter writer,
                                      QName typeQname,
                                      NamespacePrefix namespacePrefix)
            throws XMLStreamException {
        String xsiPrefix = writer.getPrefix(Constants.URI_DEFAULT_SCHEMA_XSI);
        if (xsiPrefix == null) {
            xsiPrefix = "ns" + namespacePrefix.getNamesapcePrefix();
            writer.writeNamespace(xsiPrefix, Constants.URI_DEFAULT_SCHEMA_XSI);
            writer.setPrefix(xsiPrefix, Constants.URI_DEFAULT_SCHEMA_XSI);
        }

        String typePrefix = writer.getPrefix(typeQname.getNamespaceURI());
        if (typePrefix == null) {
            typePrefix = "ns" + namespacePrefix.getNamesapcePrefix();
            writer.writeNamespace(typePrefix, typeQname.getNamespaceURI());
            writer.setPrefix(typePrefix, typeQname.getNamespaceURI());
        }

        String attributeValue = typeQname.getLocalPart();
        if (!typePrefix.equals("")) {
            attributeValue = typePrefix + ":" + attributeValue;
        }
        writer.writeAttribute(Constants.URI_DEFAULT_SCHEMA_XSI, "type", attributeValue);
    }

    protected void writeNullAttribute(XMLStreamWriter writer,
                                      NamespacePrefix namespacePrefix)
            throws XMLStreamException {
        QName attributeQName = new QName(Constants.URI_DEFAULT_SCHEMA_XSI, "nil");
        writeAttribute(writer, "1", attributeQName, namespacePrefix);
    }

    /**
     * this method wrtes the simple attribute to the writer.
     *
     * @param writer
     * @param attributeValue
     * @param attributeQName
     * @param namespacePrefix
     * @throws XMLStreamException
     */
    protected void writeAttribute(XMLStreamWriter writer,
                                  String attributeValue,
                                  QName attributeQName,
                                  NamespacePrefix namespacePrefix) throws XMLStreamException {
        if ((attributeQName.getNamespaceURI() != null) && !attributeQName.getNamespaceURI().trim().equals("")) {
            String prefix = writer.getPrefix(attributeQName.getNamespaceURI());
            if (prefix == null) {
                prefix = "ns" + namespacePrefix.getNamesapcePrefix();
                writer.writeNamespace(prefix, attributeQName.getNamespaceURI());
                writer.setPrefix(prefix, attributeQName.getNamespaceURI());
            }
            writer.writeAttribute(attributeQName.getNamespaceURI(),
                    attributeQName.getLocalPart(),
                    attributeValue);

        } else {
            writer.writeAttribute(attributeQName.getLocalPart(), attributeValue);
        }
    }

    protected void writeAttribute(XMLStreamWriter writer,
                                  QName attributeValue,
                                  QName attributeQName,
                                  NamespacePrefix namespacePrefix)
            throws XMLStreamException {

        String attributeStringVaule = attributeValue.getLocalPart();
        if ((attributeValue.getNamespaceURI() != null) && (attributeValue.getNamespaceURI().trim().equals(""))){
            // i.e. this is a qname
            String prefix = writer.getPrefix(attributeValue.getNamespaceURI());
            if (prefix == null){
                prefix = "ns" + namespacePrefix.getNamesapcePrefix();
                writer.writeNamespace(prefix,attributeValue.getNamespaceURI());
                writer.setPrefix(prefix,attributeValue.getNamespaceURI());
            }
           if (!"".equals(prefix)){
              attributeStringVaule = prefix + ":" + attributeStringVaule;
           }
        }

        if ((attributeQName.getNamespaceURI() != null) && !attributeQName.getNamespaceURI().trim().equals("")) {
            String prefix = writer.getPrefix(attributeQName.getNamespaceURI());
            if (prefix == null) {
                prefix = "ns" + namespacePrefix.getNamesapcePrefix();
                writer.writeNamespace(prefix, attributeQName.getNamespaceURI());
                writer.setPrefix(prefix, attributeQName.getNamespaceURI());
            }
            writer.writeAttribute(attributeQName.getNamespaceURI(),
                    attributeQName.getLocalPart(),
                    attributeStringVaule);

        } else {
            writer.writeAttribute(attributeQName.getLocalPart(), attributeStringVaule);
        }
    }
}
