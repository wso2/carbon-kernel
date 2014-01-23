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

package org.apache.axis2.databinding;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.util.StreamWrapper;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/** Builds a SOAPEnvelope around an ADB pull parser */
public class ADBSOAPModelBuilder extends StAXSOAPModelBuilder {

    public ADBSOAPModelBuilder(XMLStreamReader parser, SOAPFactory factory) {
        super(new Envelope(parser).
                getPullParser(
                new QName(factory.getSoapVersionURI(),
                          SOAPConstants.SOAPENVELOPE_LOCAL_NAME,
                          SOAPConstants.SOAP_DEFAULT_NAMESPACE_PREFIX)),
              factory,
              factory.getSoapVersionURI());
    }

    public SOAPEnvelope getEnvelope() {
        return getSOAPEnvelope();
    }

    public static class Envelope
            implements org.apache.axis2.databinding.ADBBean {
        Body body;

        Envelope(XMLStreamReader parser) {
            body = new Body(parser);
        }

        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName) {
            java.util.ArrayList elementList = new java.util.ArrayList();
            elementList.add(new QName(qName.getNamespaceURI(), "Header",
                                      SOAPConstants.BODY_NAMESPACE_PREFIX));
            elementList.add(new Header());
            elementList.add(new QName(qName.getNamespaceURI(), "Body",
                                      SOAPConstants.BODY_NAMESPACE_PREFIX));
            elementList.add(body);
            return
                    new StreamWrapper(new org.apache.axis2.databinding.utils.reader.
                            ADBXMLStreamReaderImpl(qName, elementList.toArray(), null));
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
    }

    protected void identifySOAPVersion(String soapVersionURIFromTransport) {
        //Do nothing
    }

    public static class Body
            implements org.apache.axis2.databinding.ADBBean {
        Child child;

        Body(XMLStreamReader parser) {
            child = new Child(parser);
        }

        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName) {
            java.util.ArrayList elementList = new java.util.ArrayList();
            elementList.add(qName);
            elementList.add(child);
            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                                                                                        elementList.toArray(),
                                                                                        null);
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
    }

    public static class Header
            implements org.apache.axis2.databinding.ADBBean {
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName) {
            java.util.ArrayList elementList = new java.util.ArrayList();
            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                                                                                        elementList.toArray(),
                                                                                        null);
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
    }

    public static class Child
            implements org.apache.axis2.databinding.ADBBean {
        XMLStreamReader parser;

        Child(XMLStreamReader parser) {
            this.parser = parser;
        }

        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName) {
            return parser;
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
    }
}
