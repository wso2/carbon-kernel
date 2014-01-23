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
 * CreateAccountRequest.java
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

/** CreateAccountRequest bean class */

public class CreateAccountRequest implements
        org.apache.axis2.databinding.ADBBean {

    public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
            "http://www.wso2.com/types", "createAccountRequest", "ns1");

    /** field for ClientInfo */
    protected ClientInfo localClientInfo;

    /**
     * Auto generated getter method
     *
     * @return com.wso2.www.types.ClientInfo
     */
    public ClientInfo getClientInfo() {
        return localClientInfo;
    }

    /**
     * Auto generated setter method
     *
     * @param param ClientInfo
     */
    public void setClientInfo(ClientInfo param) {

        this.localClientInfo = param;
    }

    /** field for Password */
    protected java.lang.String localPassword;

    /**
     * Auto generated getter method
     *
     * @return java.lang.String
     */
    public java.lang.String getPassword() {
        return localPassword;
    }

    /**
     * Auto generated setter method
     *
     * @param param Password
     */
    public void setPassword(java.lang.String param) {

        this.localPassword = param;
    }

    /** databinding method to get an XML representation of this object */
    public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName) {

        java.util.ArrayList elementList = new java.util.ArrayList();
        java.util.ArrayList attribList = new java.util.ArrayList();

        elementList.add(new javax.xml.namespace.QName(
                "http://www.wso2.com/types", "clientinfo"));
        elementList.add(localClientInfo);

        elementList.add(new javax.xml.namespace.QName("", "password"));
        elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
                .convertToString(localPassword));

        return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl
                (qName, elementList.toArray(), attribList
                        .toArray());

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
        public static CreateAccountRequest parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
            CreateAccountRequest object = new CreateAccountRequest();
            try {
                int event = reader.getEventType();
                int count = 0;
                int argumentCount = 2;
                boolean done = false;
                // event better be a START_ELEMENT. if not we should go up to
                // the start element here
                while (!reader.isStartElement()) {
                    event = reader.next();
                }

                while (!done) {
                    if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event) {

                        if ("clientinfo".equals(reader.getLocalName())) {

                            object
                                    .setClientInfo(ClientInfo.Factory
                                            .parse(reader));
                            count++;

                        }

                        if ("password".equals(reader.getLocalName())) {

                            String content = reader.getElementText();
                            object
                                    .setPassword(org.apache.axis2.databinding.utils.ConverterUtil
                                            .convertToString(content));
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
    }// end of factory class

}
