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


package org.apache.axis2.transport.local;

import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Iterator;
import java.util.Map;

/**
 * LocalResponder
 */
public class LocalResponder extends AbstractHandler implements TransportSender {
    protected static final Log log = LogFactory.getLog(LocalResponder.class);

    //  fixed for Executing LocalTransport in MulthThread. 
    private OutputStream out;

    public LocalResponder(OutputStream response) {
        this.out = response;
    }

    public void init(ConfigurationContext confContext, TransportOutDescription transportOut)
            throws AxisFault {
    }

    public void stop() {
    }

    public void cleanup(MessageContext msgContext) throws AxisFault {
    }

    /**
     * Method invoke
     *
     * @param msgContext the active MessageContext
     * @throws AxisFault
     */
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

        // Check for the REST behaviour, if you desire rest beahaviour
        // put a <parameter name="doREST" value="true"/> at the axis2.xml
        msgContext.setDoingMTOM(TransportUtils.doWriteMTOM(msgContext));
        msgContext.setDoingSwA(TransportUtils.doWriteSwA(msgContext));

        EndpointReference epr = null;

        if (msgContext.getTo() != null && !msgContext.getTo().hasAnonymousAddress()) {
            epr = msgContext.getTo();
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Response - " + msgContext.getEnvelope().toString());
            }

            if (epr != null) {
                if (!epr.hasNoneAddress()) {
                    TransportUtils.writeMessage(msgContext, out);
                }
            } else {
                if (/*(msgContext != null) &&*/ (msgContext.getOperationContext() != null) &&
                        (msgContext.getOperationContext().getMessageContexts() != null)) {
                    MessageContext proxyInMessageContext = msgContext.
                            getOperationContext().getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);

                    if (proxyInMessageContext != null) {
                        MessageContext initialMessageContext = (MessageContext) proxyInMessageContext.
                                getProperty(LocalTransportReceiver.IN_MESSAGE_CONTEXT);

                        if (initialMessageContext != null) {
                            handleResponse(msgContext, initialMessageContext);
                        } else {
                            out = (OutputStream) msgContext.getProperty(MessageContext.TRANSPORT_OUT);

                            if (out != null) {
                                TransportUtils.writeMessage(msgContext, out);
                            } else {
                                throw new AxisFault(
                                        "Both the TO and Property MessageContext.TRANSPORT_OUT is Null, No where to send");
                            }

                        }
                    }
                } else {
                    out = (OutputStream) msgContext.getProperty(MessageContext.TRANSPORT_OUT);

                    if (out != null) {
                        TransportUtils.writeMessage(msgContext, out);
                    } else {
                        throw new AxisFault(
                                "Both the TO and Property MessageContext.TRANSPORT_OUT is Null, No where to send");
                    }
                }
            }
        } catch (AxisFault axisFault) {
            // At this point all we can do is log this error, since it happened while
            // we were sending the response!
            log.error("Error sending response", axisFault);
        }

        TransportUtils.setResponseWritten(msgContext, true);

        return InvocationResponse.CONTINUE;
    }

    /**
     * Retrieves the properties from the proxyOutMessageContext and sets the values to the
     * inMessageContext.
     *
     * @param proxyOutMessageContext the active message context
     * @param initialMessageContext  the initial message context, which was stored as a property
     *                               in the proxyOutMessageContext
     * @throws AxisFault AxisFault
     */
    private void handleResponse(MessageContext proxyOutMessageContext, MessageContext initialMessageContext) throws AxisFault {
        MessageContext inMessageContext = initialMessageContext.getOperationContext().
                getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);

        // setting the properties
		Iterator<String> initialPropertyIterator = proxyOutMessageContext.getPropertyNames();
		if (initialPropertyIterator != null) {
			while (initialPropertyIterator.hasNext()) {
				String strKey = initialPropertyIterator.next();
				Object paramObj = proxyOutMessageContext.getProperty(strKey);
				if (paramObj != null) {
					inMessageContext.setProperty(strKey, paramObj);
				}
			}
		} 

        inMessageContext.setEnvelope(getEnvelope(proxyOutMessageContext));
        inMessageContext.setAxisServiceGroup(initialMessageContext.getAxisServiceGroup());
        inMessageContext.setAxisService(initialMessageContext.getAxisService());
        inMessageContext.setAxisOperation(initialMessageContext.getAxisOperation());
        inMessageContext.setAxisMessage(initialMessageContext.getAxisOperation().getMessage(
                WSDLConstants.MESSAGE_LABEL_OUT_VALUE));
        inMessageContext.setIncomingTransportName(Constants.TRANSPORT_LOCAL);
        inMessageContext.setServiceContext(initialMessageContext.getServiceContext());

        // set properties on response
        inMessageContext.setServerSide(true);
        inMessageContext.setProperty(MessageContext.TRANSPORT_OUT,
                initialMessageContext.getProperty(MessageContext.TRANSPORT_OUT));
        inMessageContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                initialMessageContext.getProperty(Constants.OUT_TRANSPORT_INFO));
        inMessageContext.setTransportIn(initialMessageContext.getTransportIn());
        inMessageContext.setTransportOut(initialMessageContext.getTransportOut());

        if (log.isDebugEnabled()) {
            log.debug("Setting AxisServiceGroup - " + initialMessageContext.getAxisServiceGroup());
            log.debug("Setting AxisService - " + initialMessageContext.getAxisService());
            log.debug("Setting AxisOperation - " + initialMessageContext.getAxisOperation());
            log.debug("Setting AxisMessage - " + initialMessageContext.getAxisOperation().
                    getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE));
            log.debug("Setting Incoming Transport name - " + Constants.TRANSPORT_LOCAL);
            log.debug("Setting Service Context " + initialMessageContext.getServiceGroupContext().toString());

            log.debug("Setting ServerSide to true");
            log.debug("Setting " + MessageContext.TRANSPORT_OUT + " property to " +
                    initialMessageContext.getProperty(MessageContext.TRANSPORT_OUT));
            log.debug("Setting " + Constants.OUT_TRANSPORT_INFO + " property to " +
                    initialMessageContext.getProperty(Constants.OUT_TRANSPORT_INFO));
            log.debug("Setting TransportIn - " + initialMessageContext.getTransportIn());
            log.debug("Setting TransportOut - " + initialMessageContext.getTransportOut());

            log.debug("Setting ReplyTo - " + initialMessageContext.getReplyTo());
            log.debug("Setting FaultTo - " + initialMessageContext.getFaultTo());
        }

        // copy the message type property that is used by the out message to the response message
        inMessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE,
                initialMessageContext.getProperty(Constants.Configuration.MESSAGE_TYPE));

        if (initialMessageContext.getMessageID() != null) {
            inMessageContext.setRelationships(
                    new RelatesTo[]{new RelatesTo(initialMessageContext.getMessageID())});
        }

        inMessageContext.setReplyTo(initialMessageContext.getReplyTo());
        inMessageContext.setFaultTo(initialMessageContext.getFaultTo());

        AxisEngine.receive(inMessageContext);
    }

    private SOAPEnvelope getEnvelope(MessageContext messageContext) throws AxisFault {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TransportUtils.writeMessage(messageContext, out);

        ByteArrayInputStream bs = new ByteArrayInputStream(out.toByteArray());
        InputStreamReader streamReader = new InputStreamReader(bs);
        OMXMLParserWrapper builder;

        try {
            builder = BuilderUtil.getBuilder(streamReader);
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        }

        return (SOAPEnvelope) builder.getDocumentElement();
    }
}
