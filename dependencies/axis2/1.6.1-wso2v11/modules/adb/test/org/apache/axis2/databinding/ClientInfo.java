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
 * ClientInfo.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */

package org.apache.axis2.databinding;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/** ClientInfo bean class */

public class ClientInfo
        implements org.apache.axis2.databinding.ADBBean {
    /* This type was generated from the piece of schema that had
    name = ClientInfo
    Namespace URI = http://www.wso2.com/types
    Namespace Prefix = ns1
    */

    public ClientInfo(String localName, String localSsn) {
        this.localName = localName;
        this.localSsn = localSsn;
    }

    public ClientInfo() {
    }

    /** field for Name */
    protected java.lang.String localName;


    /**
     * Auto generated getter method
     *
     * @return java.lang.String
     */
    public java.lang.String getName() {
        return localName;
    }

    /**
     * Auto generated setter method
     *
     * @param param Name
     */
    public void setName(java.lang.String param) {


        this.localName = param;
    }


    /** field for Ssn */
    protected java.lang.String localSsn;


    /**
     * Auto generated getter method
     *
     * @return java.lang.String
     */
    public java.lang.String getSsn() {
        return localSsn;
    }

    /**
     * Auto generated setter method
     *
     * @param param Ssn
     */
    public void setSsn(java.lang.String param) {


        this.localSsn = param;
    }


    /** databinding method to get an XML representation of this object */
    public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName) {


        java.util.ArrayList elementList = new java.util.ArrayList();
        java.util.ArrayList attribList = new java.util.ArrayList();


        elementList.add(new javax.xml.namespace.QName("http://www.wso2.com/types",
                                                      "name"));
        elementList
                .add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localName));

        elementList.add(new javax.xml.namespace.QName("http://www.wso2.com/types",
                                                      "ssn"));
        elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSsn));


        return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl
                (qName, elementList.toArray(), attribList.toArray());


    }

    public OMElement getOMElement(QName parentQName, OMFactory factory) throws ADBException {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    public void serialize(final QName parentQName,
                          XMLStreamWriter xmlWriter)
                throws XMLStreamException, ADBException {
            serialize(parentQName,xmlWriter,false);
        }

        public void serialize(final QName parentQName,
                              XMLStreamWriter xmlWriter,
                              boolean serializeType)
                throws XMLStreamException, ADBException {
            throw new UnsupportedOperationException("Unimplemented method");
        }

    /** Factory class that keeps the parse method */
    public static class Factory {
        /** static method to create the object */
        public static ClientInfo parse(javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
            ClientInfo object = new ClientInfo();
            try {
                int event = reader.getEventType();
                int count = 0;
                int argumentCount = 2;
                boolean done = false;
                //event better be a START_ELEMENT. if not we should go up to the start element here
                while (!reader.isStartElement()) {
                    event = reader.next();
                }


                while (!done) {
                    if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event) {


                        if ("name".equals(reader.getLocalName())) {

                            String content = reader.getElementText();
                            object.setName(
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                            content));
                            count++;


                        }


                        if ("ssn".equals(reader.getLocalName())) {

                            String content = reader.getElementText();
                            object.setSsn(
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                            content));
                            count++;


                        }


                    }

                    if (argumentCount == count) {
                        done = true;
                    }

                    if (!done) {
                        event = reader.next();
                    }

                }

            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }
    }//end of factory class

}
    