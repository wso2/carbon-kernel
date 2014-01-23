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
import org.apache.axis2.jaxws.utility.DataSourceFormatter;
import org.apache.axis2.jaxws.client.async.AsyncResponse;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.DataSourceBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.OMBlockFactory;
import org.apache.axis2.jaxws.message.factory.SOAPEnvelopeBlockFactory;
import org.apache.axis2.jaxws.message.factory.SourceBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.message.databinding.OMBlock;
import org.apache.axis2.jaxws.message.databinding.impl.OMBlockFactoryImpl;

import javax.activation.DataSource;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

public class XMLDispatch<T> extends BaseDispatch<T> {
    private static final Log log = LogFactory.getLog(XMLDispatch.class);
    private Class type;
    private Class blockFactoryType;

    public XMLDispatch(ServiceDelegate svcDelegate,
                       EndpointDescription endpointDesc,
                       WebServiceFeature... features) {
        this(svcDelegate, endpointDesc, null, null, features);
    }

    public XMLDispatch(ServiceDelegate svcDelegate,
            EndpointDescription endpointDesc,
            EndpointReference epr,
            String addressingNamespace,
            WebServiceFeature... features) {
        super(svcDelegate, endpointDesc, epr, addressingNamespace, features);
    }

    public Class getType() {
        return type;
    }

    public void setType(Class c) {
        type = c;
    }

    public AsyncResponse createAsyncResponseListener() {
        if (log.isDebugEnabled()) {
            log.debug("Creating new AsyncListener for XMLDispatch");
        }

        XMLDispatchAsyncListener al = new XMLDispatchAsyncListener(getEndpointDescription());
        al.setMode(mode);
        al.setType(type);
        al.setBlockFactoryType(blockFactoryType);
        return al;
    }

    public Message createMessageFromValue(Object value) {
        if (value != null) {
            type = value.getClass();
            if (log.isDebugEnabled()) {
                log.debug("Parameter type: " + type.getName());
                log.debug("Message mode: " + mode.name());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Dispatch invoked with null parameter Value");
                log.debug("creating empty soap message");
            }
            try {
                blockFactoryType = getBlockFactory();
                return createEmptyMessage(
                        Protocol.getProtocolForBinding(endpointDesc.getClientBindingID()));

            } catch (XMLStreamException e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }

        }
        Block block = null;
        blockFactoryType = getBlockFactory(value);
        BlockFactory factory = (BlockFactory)FactoryRegistry.getFactory(blockFactoryType);
        if (log.isDebugEnabled()) {
            log.debug("Loaded block factory type [" + blockFactoryType.getName());
        }
        // The protocol of the Message that is created should be based
        // on the binding information available.
        Protocol proto = Protocol.getProtocolForBinding(endpointDesc.getClientBindingID());
        Message message = null;
        if (mode.equals(Mode.PAYLOAD)) {
            try {
                MessageFactory mf =
                        (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
                block = factory.createFrom(value, null, null);


                message = mf.create(proto);
                message.setBodyBlock(block);
            } catch (Exception e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
        } else if (mode.equals(Mode.MESSAGE)) {
            try {
                MessageFactory mf =
                        (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
                // If the value contains just the xml data, then you can create the Message directly from the
                // Block.  If the value contains attachments, you need to do more.
                // TODO For now the only value that contains Attachments is SOAPMessage
                if (value instanceof SOAPMessage) {
                    message = mf.createFrom((SOAPMessage)value);
                } else {
                    block = factory.createFrom(value, null, null);
                    message = mf.createFrom(block, null, proto);
                }
            } catch (Exception e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
        }

        return message;
    }

    public Object getValueFromMessage(Message message) {
        return getValue(message, mode, blockFactoryType);
    }

    /**
     * Common code used by XMLDispatch and XMLDispatchAsyncListener
     *
     * @param message
     * @return object
     */
    static Object getValue(Message message, Mode mode, Class blockFactoryType) {
        Object value = null;
        Block block = null;

        if (log.isDebugEnabled()) {
            log.debug("Attempting to get the value object from the returned message");
        }

        try {
            if (mode.equals(Mode.PAYLOAD)) {
                BlockFactory factory = (BlockFactory)FactoryRegistry
                        .getFactory(blockFactoryType);
                block = message.getBodyBlock(null, factory);
                if (block != null) {
                    value = block.getBusinessObject(true);
                } else {
                    // REVIEW This seems like the correct behavior.  If the body is empty, return a null
                    // Any changes here should also be made to XMLDispatch.getValue
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "There are no elements in the body to unmarshal.  XMLDispatch returns a null value");
                    }
                    value = null;
                }

            } else if (mode.equals(Mode.MESSAGE)) {
                BlockFactory factory = (BlockFactory)FactoryRegistry.getFactory(blockFactoryType);

                if (factory instanceof OMBlockFactory) {
                    /*
                     * see MessageImpl.getValue(Object, BlockFactory)
                     * The getValue method is not performant; it uses an intermediate StringBlock.  To support OMElement in a
                     * performant way, we simply retrieve the OMElement from the Message object, rather than unnecessarily
                     * using the non-performant code in MessageImpl.getValue.
                     * 
                     * TODO:  when MessageImpl.getValue is fixed, this code can be removed, and the check for (value instanceof OMBlock)
                     * placed below.  However, the solution here actually traverses less code, so perhaps it should remain as-is.
                     */

                    value = (OMElement)message.getAsOMElement();
                } else {
                    value = message.getValue(null, factory);
                }
                if (value == null) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "There are no elements to unmarshal.  XMLDispatch returns a null value");
                    }   
                }
            }

        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("An error occured while creating the block");
            }
            throw ExceptionFactory.makeWebServiceException(e);
        } finally {
            if (!(value instanceof OMElement)) {
                message.close();
            }
        }

        if (log.isDebugEnabled()) {
            if (value == null)
                log.debug("Returning a null value");
            else
                log.debug("Returning value of type: " + value.getClass().getName());
        }

        return value;
    }

    private Class getBlockFactory(Object o) {
        if (o instanceof String) {
            if (log.isDebugEnabled()) {
                log.debug(">> returning XMLStringBlockFactory");
            }
            return XMLStringBlockFactory.class;
        } else if (Source.class.isAssignableFrom(o.getClass())) {
            if (log.isDebugEnabled()) {
                log.debug(">> returning SourceBlockFactory");
            }
            return SourceBlockFactory.class;
        } else if (DataSource.class.isAssignableFrom(o.getClass())) {
            if (log.isDebugEnabled()) {
                log.debug(">> returning DataSourceBlockFactory");
            }
            return DataSourceBlockFactory.class;
        } else if (SOAPMessage.class.isAssignableFrom(o.getClass())) {
            if (log.isDebugEnabled()) {
                log.debug(">> returning SOAPMessageFactory");
            }
            return SOAPEnvelopeBlockFactory.class;
        } else if (SOAPEnvelope.class.isAssignableFrom(o.getClass())) {
            if (log.isDebugEnabled()) {
                log.debug(">> returning SOAPEnvelope");
            }
            return SOAPEnvelopeBlockFactory.class;
        } else if (OMElement.class.isAssignableFrom(o.getClass())) {
            if (log.isDebugEnabled()) {
                log.debug(">> returning OMBlockFactory");
            }
            return OMBlockFactory.class;
        }
        if (log.isDebugEnabled()) {
            log.debug(">> ERROR: Factory not found");
        }
        return null;
    }

    private Class getBlockFactory() {

        if (String.class.isAssignableFrom(type)) {
            if (log.isDebugEnabled()) {
                log.debug(">> returning XMLStringBlockFactory");
            }
            return XMLStringBlockFactory.class;
        } else if (Source.class.isAssignableFrom(type)) {
            if (log.isDebugEnabled()) {
                log.debug(">> returning SourceBlockFactory");
            }
            return SourceBlockFactory.class;
        } else if (SOAPMessage.class.isAssignableFrom(type)) {
            if (log.isDebugEnabled()) {
                log.debug(">> returning SOAPMessageFactory");
            }
            return SOAPEnvelopeBlockFactory.class;
        } else if (SOAPEnvelope.class.isAssignableFrom(type)) {
            if (log.isDebugEnabled()) {
                log.debug(">> returning SOAPEnvelope");
            }
            return SOAPEnvelopeBlockFactory.class;
        } else if (OMElement.class.isAssignableFrom(type)) {
            if (log.isDebugEnabled()) {
                log.debug(">> returning OMBlockFactory");
            }
            return OMBlockFactory.class;
        }
        if (log.isDebugEnabled()) {
            log.debug(">> ERROR: Factory not found");
        }
        return null;
    }

    private Message createEmptyMessage(Protocol protocol)
            throws WebServiceException, XMLStreamException {
        MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(protocol);
        return m;
    }


    protected void initMessageContext(Object obj, MessageContext requestMsgCtx) {
        super.initMessageContext(obj, requestMsgCtx);
        if(obj instanceof DataSource){
            requestMsgCtx.setProperty(Constants.Configuration.MESSAGE_FORMATTER, 
                    new DataSourceFormatter(((DataSource)obj).getContentType()));    
        }
    }

}
