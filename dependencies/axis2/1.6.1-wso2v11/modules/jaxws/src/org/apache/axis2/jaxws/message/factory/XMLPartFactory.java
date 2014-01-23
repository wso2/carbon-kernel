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

package org.apache.axis2.jaxws.message.factory;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLPart;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

/**
 * XMLPartFactory
 * <p/>
 * Creates an XMLPart object.  The two common patterns are: - Create an empty message for a specific
 * protocol - Create a xmlPart sourced from OM (XMLStreamReader)
 * <p/>
 * The FactoryRegistry should be used to get access to the Factory
 *
 * @see org.apache.axis2.jaxws.registry.FactoryRegistry
 */
public interface XMLPartFactory {
    /**
     * create XMLPart from XMLStreamReader
     *
     * @param reader   XMLStreamReader
     * @param protocol (if null, the soap protocol is inferred from the namespace)
     * @throws MessageStreamException
     */
    public XMLPart createFrom(XMLStreamReader reader, Protocol protocol)
            throws XMLStreamException, WebServiceException;

    /**
     * create XMLPart from OMElement
     *
     * @param omElement OMElement
     * @param protocol  (if null, the soap protocol is inferred from the namespace)
     * @throws WebServiceException
     */
    public XMLPart createFrom(OMElement omElement, Protocol protocol)
            throws XMLStreamException, WebServiceException;

    /**
     * create XMLPart from SOAPEnvelope
     *
     * @param soapEnvelope SOAPEnvelope
     * @throws WebServiceException
     */
    public XMLPart createFrom(SOAPEnvelope soapEnvelope)
            throws XMLStreamException, WebServiceException;

    /**
     * create empty XMLPart of the specified protocol
     * @param protocol
     * @throws WebServiceException
     */
    public XMLPart create(Protocol protocol) throws XMLStreamException, WebServiceException;
	
}
