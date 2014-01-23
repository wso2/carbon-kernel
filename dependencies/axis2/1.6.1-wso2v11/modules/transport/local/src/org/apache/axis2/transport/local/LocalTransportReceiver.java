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
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class LocalTransportReceiver {
    protected static final Log log = LogFactory.getLog(LocalTransportReceiver.class);

    public static ConfigurationContext CONFIG_CONTEXT;
    private ConfigurationContext confContext;
    private MessageContext inMessageContext;
    /** Whether the call is blocking or non-blocking */
    private boolean nonBlocking = false;
    /** If the call is non-blocking the in message context will be stored in this property */
    public static final String IN_MESSAGE_CONTEXT = "IN_MESSAGE_CONTEXT";

    public LocalTransportReceiver(ConfigurationContext configContext) {
        confContext = configContext;
    }

    public LocalTransportReceiver(boolean nonBlocking, ConfigurationContext configContext) {
        confContext = configContext;
        this.nonBlocking = nonBlocking;
    }

    public LocalTransportReceiver(LocalTransportSender sender) {
        this(CONFIG_CONTEXT);
    }

    public LocalTransportReceiver(LocalTransportSender sender, boolean nonBlocking) {
        this(CONFIG_CONTEXT);
        this.nonBlocking = nonBlocking;
    }

    public void processMessage(MessageContext inMessageContext,
                               InputStream in,
                               OutputStream response) throws AxisFault {
        if (this.confContext == null) {
            this.confContext = inMessageContext.getConfigurationContext();
        }
        this.inMessageContext = inMessageContext;
        EndpointReference to = inMessageContext.getTo();
        String action = inMessageContext.getOptions().getAction();
        processMessage(in, to, action, response);
    }

    public void processMessage(ConfigurationContext configurationContext,
                               InputStream in,
                               EndpointReference to,
                               String action,
                               OutputStream response) throws AxisFault {
        if (this.confContext == null) {
            this.confContext = configurationContext;
        }
        processMessage(in, to, action, response);
    }

    public void processMessage(InputStream in, EndpointReference to, String action,
                               OutputStream response)
            throws AxisFault {
        MessageContext msgCtx = confContext.createMessageContext();

        if (this.nonBlocking) {
            if (log.isDebugEnabled()) {
                log.debug("Setting the in-message context as a property(" + IN_MESSAGE_CONTEXT +
                        ") to the current message context");
            }
            // Set the in-message context as a property to the  current message context.
            msgCtx.setProperty(IN_MESSAGE_CONTEXT, inMessageContext);
        }

        if (inMessageContext != null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting the property " + HTTPConstants.MC_HTTP_SERVLETREQUEST + " to " +
                        inMessageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST));
                log.debug("Setting the property " + MessageContext.REMOTE_ADDR + " to " +
                        inMessageContext.getProperty(MessageContext.REMOTE_ADDR));
            }
            msgCtx.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST,
                               inMessageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST));
            msgCtx.setProperty(MessageContext.REMOTE_ADDR,
                               inMessageContext.getProperty(MessageContext.REMOTE_ADDR));
        }

        TransportInDescription tIn = confContext.getAxisConfiguration().getTransportIn(
                Constants.TRANSPORT_LOCAL);
        TransportOutDescription tOut = confContext.getAxisConfiguration().getTransportOut(
                Constants.TRANSPORT_LOCAL);

        // CAUTION : When using Local Transport of Axis2,  class LocalTransportReceiver changed the name of LocalTransportSender's class in configContext.
        // We escaped this problem by the following code.
        LocalResponseTransportOutDescription localTransportResOut = new LocalResponseTransportOutDescription(
                tOut);
        localTransportResOut.setSender(new LocalResponder(response));

        try {
            msgCtx.setIncomingTransportName(Constants.TRANSPORT_LOCAL);
            msgCtx.setTransportIn(tIn);
            msgCtx.setTransportOut(localTransportResOut);
            msgCtx.setProperty(MessageContext.TRANSPORT_OUT, response);

            Object headers = inMessageContext.getProperty(MessageContext.TRANSPORT_HEADERS);
            if (headers != null) {
                msgCtx.setProperty(MessageContext.TRANSPORT_HEADERS, headers);
            }

            msgCtx.setTo(to);
            msgCtx.setWSAAction(action);
            msgCtx.setServerSide(true);

            InputStreamReader streamReader = new InputStreamReader(in);
            OMXMLParserWrapper builder;
            try {
                builder = BuilderUtil.getBuilder(streamReader);
            } catch (XMLStreamException e) {
                throw AxisFault.makeFault(e);
            }
            SOAPEnvelope envelope = (SOAPEnvelope) builder.getDocumentElement();

            msgCtx.setEnvelope(envelope);

             if (log.isDebugEnabled()) {
                log.debug("Setting incoming Transport name - " + Constants.TRANSPORT_LOCAL);
                log.debug("Setting TransportIn - " + tIn);
                log.debug("Setting TransportOut - " + localTransportResOut);
                log.debug("Setting To address - " + to);
                log.debug("Setting WSAction - " + action);
                log.debug("Setting Envelope - " + envelope.toString());
            }

            AxisEngine.receive(msgCtx);
        } catch (AxisFault e) {
            // write the fault back.
            try {
                MessageContext faultContext =
                        MessageContextBuilder.createFaultMessageContext(msgCtx, e);
                faultContext.setTransportOut(localTransportResOut);
                faultContext.setProperty(MessageContext.TRANSPORT_OUT, response);

                if(log.isDebugEnabled()) {
                    log.debug("Setting FaultContext's TransportOut - " + localTransportResOut);
                }

                AxisEngine.sendFault(faultContext);
            } catch (AxisFault axisFault) {
                // can't handle this, so just throw it
                throw axisFault;
            }
        }
    }

}
