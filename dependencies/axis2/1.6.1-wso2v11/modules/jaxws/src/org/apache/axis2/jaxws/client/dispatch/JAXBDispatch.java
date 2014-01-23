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

package org.apache.axis2.jaxws.client.dispatch;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.client.async.AsyncResponse;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.utility.XMLRootElementUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

public class JAXBDispatch<T> extends BaseDispatch<T> {
    private static final Log log = LogFactory.getLog(JAXBDispatch.class);
    private JAXBContext jaxbContext;
    private QName elementQName;

    public JAXBDispatch(ServiceDelegate svcDelegate,
                        EndpointDescription epDesc,
                        WebServiceFeature... features) {
        this(svcDelegate, epDesc, null, null, features);
    }

    public JAXBDispatch(ServiceDelegate svcDelegate,
            EndpointDescription epDesc,
            EndpointReference epr,
            String addressingNamespace,
            WebServiceFeature... features) {
        super(svcDelegate, epDesc, epr, addressingNamespace, features);
    }

    public JAXBContext getJAXBContext() {
        return jaxbContext;
    }

    public void setJAXBContext(JAXBContext jbc) {
        jaxbContext = jbc;
    }

    public AsyncResponse createAsyncResponseListener() {
        JAXBDispatchAsyncListener listener =
                new JAXBDispatchAsyncListener(getEndpointDescription());
        listener.setJAXBContext(jaxbContext);
        listener.setMode(mode);
        return listener;
    }

    public Message createMessageFromValue(Object value) {
        Message message = null;
        
        if (value == null) {
            if (log.isDebugEnabled()) {
                log.debug("Dispatch invoked with null parameter Value");
                log.debug("creating empty soap message");
            }
            try {
                return createEmptyMessage(
                        Protocol.getProtocolForBinding(endpointDesc.getClientBindingID()));

            } catch (XMLStreamException e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
        }
        
        try {
            JAXBBlockFactory factory =
                (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);

            Class clazz = value.getClass();
            JAXBBlockContext context = null;
            if (jaxbContext != null) {
                context = new JAXBBlockContext(jaxbContext);
            } else {
                context = new JAXBBlockContext(clazz.getPackage().getName());
            }
            // The protocol of the Message that is created should be based
            // on the binding information available.
            Protocol proto = Protocol.getProtocolForBinding(endpointDesc.getClientBindingID());

            // Create a block from the value
            elementQName = XMLRootElementUtil.getXmlRootElementQNameFromObject(value);
            Block block = factory.createFrom(value, context, elementQName);
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);

            if (mode.equals(Mode.PAYLOAD)) {
                // Normal case

                message = mf.create(proto);
                message.setBodyBlock(block);
            } else {
                // Message mode..rare case

                // Create Message from block
                message = mf.createFrom(block, null, proto);
            }

        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }

        return message;
    }
    
    // For a JAXB element we don't need to parse the message to get the element QName (which is
    // slow); we saved the element QName from the JAXBElement earlier.  If for some reason it was not
    // saved, we'll try parsing the message to get it.
    QName getBodyElementQNameFromDispatchMessage(MessageContext requestMessageCtx) {
        QName returnElementQName = null;
        if (elementQName != null) {
            returnElementQName = elementQName;
        } else {
            returnElementQName = super.getBodyElementQNameFromDispatchMessage(requestMessageCtx);
        }
        return returnElementQName;
    }
    public Object getValueFromMessage(Message message) {
        return getValue(message, mode, jaxbContext);
    }

    /**
     * Common code to get the value for JAXBDispatch and JAXBDispatchAsyncListener
     *
     * @param message
     * @param mode
     * @param jaxbContext
     * @return
     */
    static Object getValue(Message message, Mode mode, JAXBContext jaxbContext) {
        Object value = null;
        try {
            if (mode.equals(Mode.PAYLOAD)) {
                // Normal Case
                JAXBBlockFactory factory =
                        (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
                JAXBBlockContext context = new JAXBBlockContext(jaxbContext);
                Block block = message.getBodyBlock(context, factory);

                if (block != null) {
                    value = block.getBusinessObject(true);
                } else {
                    // REVIEW This seems like the correct behavior.  If the body is empty, return a null
                    // Any changes here should also be made to XMLDispatch.getValue
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "There are no elements in the body to unmarshal.  JAXBDispatch returns a null value");
                    }
                    value = null;
                }
            } else {
                BlockFactory factory =
                        (BlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
                JAXBBlockContext context = new JAXBBlockContext(jaxbContext);
                value = message.getValue(context, factory);
                if (value == null) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "There are no elements to unmarshal.  JAXBDispatch returns a null value");
                    }
                }
            }
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        } finally {
            message.close();
        }

        return value;
    }
    
    private Message createEmptyMessage(Protocol protocol)
            throws WebServiceException, XMLStreamException {
        MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(protocol);
        return m;
    }    
}
