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
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;

import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

/**
 * MessageFactory
 * <p/>
 * Creates a Message object.  The common patterns are: - Create an empty message for a specific
 * protocol - Create a message with the xml sourced from OM (XMLStreamReader) - Create a message
 * (xml + attachments) from a SOAPMessage
 * <p/>
 * The FactoryRegistry should be used to get access to the Factory
 *
 * @see org.apache.axis2.jaxws.registry.FactoryRegistry
 */
public interface MessageFactory {
    /**
     * create Message with the xml from the XMLStreamReader
     *
     * @param reader   XMLStreamReader
     * @param protocol (if null, the soap protocol is inferred from the namespace)
     * @throws XMLStreamException
     */
    public Message createFrom(XMLStreamReader reader, Protocol protocol)
            throws XMLStreamException, WebServiceException;

    /**
     * create Message with the xml from the OMElement
     *
     * @param omElement OMElement
     * @param protocol  (if null, the soap protocol is inferred from the namespace)
     * @throws XMLStreamException
     */
    public Message createFrom(OMElement omElement, Protocol protocol)
            throws XMLStreamException, WebServiceException;

    /**
     * create Message from a Block
     *
     * @param block
     * @param context  Associated Context or null
     * @param protocol (if null, the soap protocol is inferred from the namespace)
     * @throws XMLStreamException
     */
    public Message createFrom(Block other, Object context, Protocol protocol)
            throws XMLStreamException, WebServiceException;

    /**
     * create Message from SOAPMessage The xml and attachments from the SOAPMessage are used to
     * populate the new Message The protocol is inferred from the SOAPMessage
     *
     * @param SOAPMessage
     * @throws XMLStreamException, WebServiceException
     */
    public Message createFrom(SOAPMessage message) throws XMLStreamException, WebServiceException;


    /**
     * create empty Message of the specified protocol
     * @param protocol
     * @throws XMLStreamException
     */
    public Message create(Protocol protocol) throws XMLStreamException, WebServiceException;
}
