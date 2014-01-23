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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.databinding.OMBlock;
import org.apache.axis2.jaxws.message.databinding.SOAPEnvelopeBlock;
import org.apache.axis2.jaxws.message.databinding.DataSourceBlock;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.WrappedDataHandler;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;
import java.util.HashMap;
import java.util.Iterator;

/** MessageFactoryImpl */
public class MessageFactoryImpl implements MessageFactory {

    /** Default Constructor required for Factory */
    public MessageFactoryImpl() {
        super();
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.factory.MessageFactory#createFrom(javax.xml.stream.XMLStreamReader)
      */
    public Message createFrom(XMLStreamReader reader, Protocol protocol)
            throws XMLStreamException, WebServiceException {
        StAXOMBuilder builder;
        if (protocol == Protocol.rest) {
            // Build a normal OM tree
            builder = new StAXOMBuilder(reader);
        } else {
            // Build a SOAP OM tree
            builder = new StAXSOAPModelBuilder(reader,
                                               null);  // Pass null as the version to trigger autodetection
        }
        OMElement omElement = builder.getDocumentElement();
        return createFrom(omElement, protocol);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.MessageFactory#createFrom(org.apache.axiom.om.OMElement)
      */
    public Message createFrom(OMElement omElement, Protocol protocol)
            throws XMLStreamException, WebServiceException {
        return new MessageImpl(omElement, protocol);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.MessageFactory#create(org.apache.axis2.jaxws.message.Protocol)
      */
    public Message create(Protocol protocol) throws XMLStreamException, WebServiceException {
        return new MessageImpl(protocol);
    }


    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.factory.MessageFactory#createFrom(javax.xml.soap.SOAPMessage)
      */
    public Message createFrom(SOAPMessage message) throws XMLStreamException, WebServiceException {
        try {
            // Create a Message with an XMLPart from the SOAPEnvelope
            Message m = new MessageImpl(message.getSOAPPart().getEnvelope());

            MimeHeaders mimeHeaders = message.getMimeHeaders();
            HashMap map = new HashMap();
            Iterator iterator = mimeHeaders.getAllHeaders();
            while (iterator.hasNext()) {
                MimeHeader mimeHeader = (MimeHeader)iterator.next();
                String key = mimeHeader.getName();
                String value = mimeHeader.getValue();
                if(key != null && value != null) {
                    if(!HTTPConstants.HEADER_CONTENT_TYPE.equalsIgnoreCase(key)) {
                        map.put(key, value);
                    }
                }
            }
            m.setMimeHeaders(map);

            if (message.countAttachments() > 0) {
                Iterator it = message.getAttachments();
                m.setDoingSWA(true);
                while (it.hasNext()) {
                    AttachmentPart ap = (AttachmentPart)it.next();
                    m.addDataHandler(new WrappedDataHandler(ap.getDataHandler(), ap.getContentType()), ap.getContentId());
                }
            }
            return m;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.factory.MessageFactory#createFrom(org.apache.axis2.jaxws.message.Block, java.lang.Object)
      */
    public Message createFrom(Block block, Object context, Protocol protocol)
            throws XMLStreamException, WebServiceException {

        // Small optimization to quickly consider the SOAPEnvelope case
        if (block instanceof SOAPEnvelopeBlock) {
            return new MessageImpl((SOAPEnvelope)block.getBusinessObject(true), protocol);
        } else if (block instanceof DataSourceBlock) {
            return createFrom(block.getOMElement(), protocol);
        } else if (block instanceof OMBlock){
           	OMBlock omblock = (OMBlock)block;
        	return new MessageImpl((OMElement)omblock.getBusinessObject(true), protocol);
        }
        return createFrom(block.getXMLStreamReader(true), protocol);
    }
}
