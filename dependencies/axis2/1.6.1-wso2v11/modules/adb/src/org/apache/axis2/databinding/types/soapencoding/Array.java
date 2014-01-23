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

/**
 * Array.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: SNAPSHOT  Built on : Dec 21, 2007 (04:03:30 LKT)
 */

package org.apache.axis2.databinding.types.soapencoding;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.databinding.ADBDataSource;
import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.databinding.utils.ConverterUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Array bean class
 */

public class Array implements ADBBean {

    public static final java.lang.String CHILD_LOCAL_NAME = "item";
    public static final java.lang.String SOAP_NAMESPACE_PREFIX = "SOAP-ENC";

    public Array() {
        this.objectList = new ArrayList();
    }

    private static java.lang.String generatePrefix(java.lang.String namespace) {
        if (namespace.equals("http://schemas.xmlsoap.org/soap/encoding/")) {
            return "SOAP-ENC";
        }
        return BeanUtil.getUniquePrefix();
    }


    protected List objectList;

    public List getObjectList() {
        return objectList;
    }

    public void setObjectList(List objectList) {
        this.objectList = objectList;
    }


    public void addObject(Object object) {
        this.objectList.add(object);
    }

    protected javax.xml.namespace.QName arrayTypeQName;

    public void setArrayTypeQName(javax.xml.namespace.QName qname){
        arrayTypeQName = qname;
    }

    public javax.xml.namespace.QName getArrayTypeQName(){
        return arrayTypeQName;
    }

    /**
     * field for ArrayType
     * This was an Attribute!
     */


    protected java.lang.String localArrayType;


    /**
     * Auto generated getter method
     *
     * @return java.lang.String
     */
    public java.lang.String getArrayType() {
        return localArrayType;
    }


    /**
     * Auto generated setter method
     *
     * @param param ArrayType
     */
    public void setArrayType(java.lang.String param) {

        this.localArrayType = param;


    }


    /**
     * field for Offset
     * This was an Attribute!
     */


    protected java.lang.String localOffset;


    /**
     * Auto generated getter method
     *
     * @return java.lang.String
     */
    public java.lang.String getOffset() {
        return localOffset;
    }


    /**
     * Auto generated setter method
     *
     * @param param Offset
     */
    public void setOffset(java.lang.String param) {

        this.localOffset = param;


    }


    /**
     * field for Id
     * This was an Attribute!
     */


    protected org.apache.axis2.databinding.types.Id localId;


    /**
     * Auto generated getter method
     *
     * @return org.apache.axis2.databinding.types.Id
     */
    public org.apache.axis2.databinding.types.Id getId() {
        return localId;
    }


    /**
     * Auto generated setter method
     *
     * @param param Id
     */
    public void setId(org.apache.axis2.databinding.types.Id param) {

        this.localId = param;


    }


    /**
     * field for Href
     * This was an Attribute!
     */


    protected org.apache.axis2.databinding.types.URI localHref;


    /**
     * Auto generated getter method
     *
     * @return org.apache.axis2.databinding.types.URI
     */
    public org.apache.axis2.databinding.types.URI getHref() {
        return localHref;
    }


    /**
     * Auto generated setter method
     *
     * @param param Href
     */
    public void setHref(org.apache.axis2.databinding.types.URI param) {

        this.localHref = param;


    }


    /**
     * isReaderMTOMAware
     *
     * @return true if the reader supports MTOM
     */
    public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;

        try {
            isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        } catch (java.lang.IllegalArgumentException e) {
            isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
    }


    /**
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final OMFactory factory) throws ADBException {


        OMDataSource dataSource = new ADBDataSource(this, parentQName);
        return factory.createOMElement(dataSource,parentQName);

    }

    public void serialize(final javax.xml.namespace.QName parentQName,
                          XMLStreamWriter xmlWriter) throws XMLStreamException, ADBException {
        serialize(parentQName, xmlWriter, false);
    }

    public void serialize(final javax.xml.namespace.QName parentQName,
                          XMLStreamWriter xmlWriter,
                          boolean serializeType) throws XMLStreamException, ADBException {


        java.lang.String prefix = parentQName.getPrefix();
        java.lang.String namespace = parentQName.getNamespaceURI();


        writeStartElement(namespace, parentQName.getLocalPart(), prefix, xmlWriter);

        if (serializeType) {

            java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://schemas.xmlsoap.org/soap/encoding/");
            if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
                writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                        namespacePrefix + ":Array",
                        xmlWriter);
            } else {
                writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                        "Array",
                        xmlWriter);
            }
        }

        java.lang.String arrayTypePrefix = null;
        java.lang.String arrayType = null;
        if (arrayTypeQName != null){
            arrayTypePrefix = registerPrefix(xmlWriter, arrayTypeQName.getNamespaceURI());
            arrayType = arrayTypeQName.getLocalPart() + "[" + objectList.size() + "]";
        } else {
            // write it as ur-type
            arrayTypePrefix = registerPrefix(xmlWriter, "http://www.w3.org/2001/XMLSchema");
            arrayType =  "ur-type[" + objectList.size() + "]";
        }

        if ((arrayTypePrefix != null) && (arrayTypePrefix.trim().length() > 0)) {
            arrayType = arrayTypePrefix + ":" + arrayType;
        }

        writeAttribute(registerPrefix(xmlWriter, "http://schemas.xmlsoap.org/soap/encoding/"),
                "http://schemas.xmlsoap.org/soap/encoding/",
                "arrayType", arrayType, xmlWriter);

        if (localOffset != null) {
            writeAttribute("http://schemas.xmlsoap.org/soap/encoding/",
                    "offset", ConverterUtil.convertToString(localOffset), xmlWriter);
        }

        if (localId != null) {
            writeAttribute("", "id", ConverterUtil.convertToString(localId), xmlWriter);
        }

        if (localHref != null) {
            writeAttribute("", "href", ConverterUtil.convertToString(localHref), xmlWriter);
        }

        // serialize the object array
        // since soap encoding does not enforce the name of the children items
        // here we use item as the child name.

        // all the list objects must be ADBBeans for basic schema types such as
        // int,float corresponding soapencoding class must be used
        ADBBean adbBean;
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            adbBean = (ADBBean) iter.next();
            if (adbBean != null) {
                if (arrayTypeQName != null) {
                    // if the array Type is given then each element does not have to
                    // write the type
                    adbBean.serialize(new javax.xml.namespace.QName("", CHILD_LOCAL_NAME), xmlWriter);
                } else {
                    adbBean.serialize(new javax.xml.namespace.QName("", CHILD_LOCAL_NAME), xmlWriter, true);
                }

            } else {
                //write the null attribute
                writeStartElement("", CHILD_LOCAL_NAME, null, xmlWriter);
                writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter);
                xmlWriter.writeEndElement();
            }
        }


        xmlWriter.writeEndElement();


    }

    private void writeStartElement(java.lang.String namespace,
                                   java.lang.String localPart,
                                   java.lang.String prefix,
                                   XMLStreamWriter xmlWriter) throws XMLStreamException {
        if ((namespace != null) && (namespace.trim().length() > 0)) {
            java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
            if (writerPrefix != null) {
                xmlWriter.writeStartElement(namespace, localPart);
            } else {
                if (prefix == null) {
                    prefix = generatePrefix(namespace);
                }

                xmlWriter.writeStartElement(prefix, localPart, namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
        } else {
            xmlWriter.writeStartElement(localPart);
        }
    }

    /**
     * Util method to write an attribute with the ns prefix
     */
    private void writeAttribute(java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                                java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
        if (xmlWriter.getPrefix(namespace) == null) {
            xmlWriter.writeNamespace(prefix, namespace);
            xmlWriter.setPrefix(prefix, namespace);

        }

        xmlWriter.writeAttribute(namespace, attName, attValue);

    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeAttribute(java.lang.String namespace, java.lang.String attName,
                                java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
        if (namespace.equals("")) {
            xmlWriter.writeAttribute(attName, attValue);
        } else {
            registerPrefix(xmlWriter, namespace);
            xmlWriter.writeAttribute(namespace, attName, attValue);
        }
    }


    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                     javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

        java.lang.String attributeNamespace = qname.getNamespaceURI();
        java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
        if (attributePrefix == null) {
            attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
        }
        java.lang.String attributeValue;
        if (attributePrefix.trim().length() > 0) {
            attributeValue = attributePrefix + ":" + qname.getLocalPart();
        } else {
            attributeValue = qname.getLocalPart();
        }

        if (namespace.equals("")) {
            xmlWriter.writeAttribute(attName, attributeValue);
        } else {
            registerPrefix(xmlWriter, namespace);
            xmlWriter.writeAttribute(namespace, attName, attributeValue);
        }
    }

    /**
     * method to handle Qnames
     */

    private void writeQName(javax.xml.namespace.QName qname,
                            javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
        java.lang.String namespaceURI = qname.getNamespaceURI();
        if (namespaceURI != null) {
            java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
            if (prefix == null) {
                prefix = generatePrefix(namespaceURI);
                xmlWriter.writeNamespace(prefix, namespaceURI);
                xmlWriter.setPrefix(prefix, namespaceURI);
            }

            if (prefix.trim().length() > 0) {
                xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            } else {
                // i.e this is the default namespace
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }

        } else {
            xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
        }
    }

    private void writeQNames(javax.xml.namespace.QName[] qnames,
                             javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

        if (qnames != null) {
            // we have to store this data until last moment since it is not possible to write any
            // namespace data after writing the charactor data
            java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
            java.lang.String namespaceURI = null;
            java.lang.String prefix = null;

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
                        stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                } else {
                    stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                }
            }
            xmlWriter.writeCharacters(stringToWrite.toString());
        }

    }


    /**
     * Register a namespace prefix
     */
    private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
        java.lang.String prefix = xmlWriter.getPrefix(namespace);

        if (prefix == null) {
            prefix = generatePrefix(namespace);

            while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
            }

            xmlWriter.writeNamespace(prefix, namespace);
            xmlWriter.setPrefix(prefix, namespace);
        }

        return prefix;
    }


    /**
     * databinding method to get an XML representation of this object
     */
    public XMLStreamReader getPullParser(javax.xml.namespace.QName qName) throws ADBException {
        return null;
    }


    /**
     * Factory class that keeps the parse method
     */
    public static class Factory {


        /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         * If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         * If this object is a complex type, the reader is positioned at the end element of its outer element
         */
        public static Array parse(XMLStreamReader reader, Class mapperClass) throws Exception {
            Array object = new Array();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix = "";
            java.lang.String namespaceuri = "";
            try {

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

//                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
//                    java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
//                            "type");
//                    if (fullTypeName != null) {
//                        java.lang.String nsPrefix = null;
//                        if (fullTypeName.indexOf(":") > -1) {
//                            nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
//                        }
//                        nsPrefix = nsPrefix == null ? "" : nsPrefix;
//
//                        java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);
//
//                        if (!"Array".equals(type)) {
//                            //find namespace for the prefix
//                            java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
//                            return (Array) ExtensionMapper.getTypeObject(nsUri, type, reader);
//                        }
//                    }
//                }

                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();

                // handle attribute "arrayType"
                java.lang.String attributeType =
                        reader.getAttributeValue("http://schemas.xmlsoap.org/soap/encoding/", "arrayType");
                object.setArrayType(ConverterUtil.convertToString(attributeType));

                handledAttributes.add("arrayType");

                // handle attribute "offset"
                java.lang.String offset =
                        reader.getAttributeValue("http://schemas.xmlsoap.org/soap/encoding/", "offset");
                object.setOffset(ConverterUtil.convertToString(offset));

                handledAttributes.add("offset");

                // handle attribute "id"
                java.lang.String id = reader.getAttributeValue(null, "id");
                object.setId(ConverterUtil.convertToID(id));
                handledAttributes.add("id");

                // handle attribute "href"
                java.lang.String href = reader.getAttributeValue(null, "href");
                object.setHref(ConverterUtil.convertToAnyURI(href));
                handledAttributes.add("href");

                // at the starting point of the child elements
                reader.next();

                while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                javax.xml.namespace.QName arrayElementQName = getInnerElementsQName(attributeType, reader);
                object.setArrayTypeQName(arrayElementQName);
                if (reader.isStartElement()) {

                    boolean loopDone = false;

                    while (!loopDone) {

                        if (reader.isStartElement()) {
                            // check whether is object is null or not
                            nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                            if ("true".equals(nillableValue) || "1".equals(nillableValue)) {
                                // move the cursor to the end element
                                while (!reader.isEndElement()) reader.next();
                                object.addObject(null);
                            } else if (arrayElementQName != null){
                                // i.e this array has same attribute type
                                object.addObject(getObject(arrayElementQName,reader,mapperClass));
                            } else {
                                // arrayElementQName null means this does not have an arry level
                                // type declaration we have to check for each and every element
                                javax.xml.namespace.QName typeQName = getTypeQName(reader);
                                if (typeQName == null){
                                    typeQName = reader.getName();
                                }
                                object.addObject(getObject(typeQName,reader,mapperClass));
                            }
                            while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                            reader.next();
                        } else if (reader.isEndElement()) {
                            loopDone = true;
                        } else {
                            reader.next();
                        }

                    }

                }

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                if (reader.isStartElement()){
                      // A start element we are not expecting indicates a trailing invalid property
                    throw new ADBException("Unexpected subelement " + reader.getLocalName());
                }

            } catch (XMLStreamException e) {
                throw new Exception("Exception while parsing array",e);
            }

            return object;
        }

         private static Object getObject(javax.xml.namespace.QName qName,
                                         XMLStreamReader reader,
                                         Class mapperClass) throws Exception {
            Object returnObject = null;
            if (qName.getNamespaceURI().equals("http://schemas.xmlsoap.org/soap/encoding/")){
              returnObject = ExtensionMapper.getTypeObject(
                       qName.getNamespaceURI(),
                       qName.getLocalPart(),
                       reader);
            } else if (qName.getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema")){
                returnObject = org.apache.axis2.databinding.types.xsd.ExtensionMapper.getTypeObject(
                       qName.getNamespaceURI(),
                       qName.getLocalPart(),
                       reader);
            } else {
                // this could be a general one of have to call for the system Extension mapper
                // invoking the mapperclass using reflection
                Method getObjectMethod = mapperClass.getMethod("getTypeObject",
                        new Class[]{java.lang.String.class, java.lang.String.class, XMLStreamReader.class});
                returnObject = getObjectMethod.invoke(null,
                        new Object[]{qName.getNamespaceURI(), qName.getLocalPart(), reader});
            }
            return returnObject;
        }

        private static javax.xml.namespace.QName getInnerElementsQName(
                    java.lang.String attributeType, XMLStreamReader reader){
               // attribute type is similar to xsd:ur-type[4]
               javax.xml.namespace.QName typeQName = null;
               java.lang.String prefix = "";
               java.lang.String type = attributeType;
                if (attributeType.indexOf(":") > -1){
                    prefix = attributeType.substring(0,attributeType.indexOf(":"));
                    type = attributeType.substring(attributeType.indexOf(":") + 1);
                }
                java.lang.String namespace = reader.getNamespaceURI(prefix);
                type = type.substring(0,type.indexOf("["));
                if (!type.equals("ur-type")){
                    typeQName = new javax.xml.namespace.QName(namespace,type);
                }
                return typeQName;
            }

            private static javax.xml.namespace.QName getTypeQName(XMLStreamReader reader) {
                javax.xml.namespace.QName typeQName = null;
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
                    java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                            "type");
                    if (fullTypeName != null) {
                        java.lang.String nsPrefix = null;
                        if (fullTypeName.indexOf(":") > -1) {
                            nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
                        }
                        nsPrefix = nsPrefix == null ? "" : nsPrefix;
                        java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);
                        typeQName = new javax.xml.namespace.QName(reader.getNamespaceURI(nsPrefix), type);
                    }
                }
                return typeQName;
            }

    }

}
           
          