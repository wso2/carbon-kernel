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

package org.apache.axis2.jaxws.message.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLPart;
import org.apache.axis2.jaxws.message.factory.XMLPartFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

/** MessageFactoryImpl */
public class XMLPartFactoryImpl implements XMLPartFactory {

    /** Default Constructor required for Factory */
    public XMLPartFactoryImpl() {
        super();
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.factory.XMLPartFactory#createFrom(javax.xml.stream.XMLStreamReader)
      */
    public XMLPart createFrom(XMLStreamReader reader, Protocol protocol)
            throws XMLStreamException, WebServiceException {
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(reader,
                                                                null);  // Pass null has the version to trigger autodetection
        SOAPEnvelope omEnvelope = builder.getSOAPEnvelope();
        return createFrom(omEnvelope, protocol);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.MessageFactory#createFrom(org.apache.axiom.om.OMElement)
      */
    public XMLPart createFrom(OMElement omElement, Protocol protocol)
            throws XMLStreamException, WebServiceException {
        return new XMLPartImpl(omElement, protocol);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.MessageFactory#create(org.apache.axis2.jaxws.message.Protocol)
      */
    public XMLPart create(Protocol protocol) throws XMLStreamException, WebServiceException {
        return new XMLPartImpl(protocol);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.factory.XMLPartFactory#createFrom(javax.xml.soap.SOAPEnvelope)
      */
    public XMLPart createFrom(javax.xml.soap.SOAPEnvelope soapEnvelope)
            throws XMLStreamException, WebServiceException {
        return new XMLPartImpl(soapEnvelope);
    }

}
