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

package org.apache.axis2.jaxws.handler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HandlerInvokerUtils {
    private static Log log = LogFactory.getLog(HandlerInvokerUtils.class);

    /**
     * Invoke Inbound Handlers
     * @param requestMsgCtx
     */
    public static boolean invokeInboundHandlers(MEPContext mepMessageCtx, List<Handler> handlers,
                                                HandlerChainProcessor.MEP mep, boolean isOneWay) {

        if (handlers == null || handlers.isEmpty())
            return true;

        String bindingProto = null;
        if (mep.equals(HandlerChainProcessor.MEP.REQUEST)) // inbound request; must be on the server
            bindingProto = mepMessageCtx.getEndpointDesc().getBindingType();
        else {
            // inbound response; must be on the client
            bindingProto = mepMessageCtx.getEndpointDesc().getClientBindingID();
        }
        Protocol proto = Protocol.getProtocolForBinding(bindingProto);
        

        HandlerChainProcessor processor = new HandlerChainProcessor(handlers, proto);
        // if not one-way, expect a response
        boolean success = true;
        try {
            if (mepMessageCtx.getMessageObject().isFault()) {
                processor.processFault(mepMessageCtx, HandlerChainProcessor.Direction.IN);
            } else {
                success =
                        processor.processChain(mepMessageCtx,
                                               HandlerChainProcessor.Direction.IN,
                                               mep,
                                               !isOneWay);
            }
        } catch (RuntimeException re) {
            /*
             * handler framework should only throw an exception here if
             * we are in the client inbound case.  Make sure the message
             * context and message are transformed.
             */
            HandlerChainProcessor.convertToFaultMessage(mepMessageCtx, re, proto);
            
            // Set the Caused By Exception on the MessageContext so that it can be obtained in
            // the JAXWSMessageReceiver
            if (log.isDebugEnabled()) {
                log.debug("Runtime Exception detected.  Setting causedByException field to " + re);
            }
            
            if (mepMessageCtx.getRequestMessageContext() != null) {
                mepMessageCtx.getRequestMessageContext().setCausedByException(new AxisFault(re.getMessage(), re));
            }
            if (mepMessageCtx.getResponseMessageContext() != null) {
                mepMessageCtx.getResponseMessageContext().setCausedByException(new AxisFault(re.getMessage(), re));
            }
            
            
            // done invoking inbound handlers, be sure to set the access lock flag on the context to true
            mepMessageCtx.setApplicationAccessLocked(true);
            return false;
        }

        if (!success && mep.equals(HandlerChainProcessor.MEP.REQUEST)) {
            // uh-oh.  We've changed directions on the server inbound handler processing,
            // This means we're now on an outbound flow, and the endpoint will not
            // be called.  Be sure to mark the context and message as such.

            // done invoking inbound handlers, be sure to set the access lock flag on the context to true
            mepMessageCtx.setApplicationAccessLocked(true);
            return false;
        }
        // done invoking inbound handlers, be sure to set the access lock flag on the context to true
        mepMessageCtx.setApplicationAccessLocked(true);
        return true;

    }

    /**
     * Invoke OutboundHandlers
     * 
     * @param msgCtx
     */
    public static boolean invokeOutboundHandlers(MEPContext mepMessageCtx, List<Handler> handlers,
                                                 HandlerChainProcessor.MEP mep, boolean isOneWay) {

        if (handlers == null || handlers.isEmpty())
            return true;

        String bindingProto = null;
        if (mep.equals(HandlerChainProcessor.MEP.REQUEST)) // outbound request; must be on the client
            bindingProto = mepMessageCtx.getEndpointDesc().getClientBindingID();
        else
            // outbound response; must be on the server
            bindingProto = mepMessageCtx.getEndpointDesc().getBindingType();
        Protocol proto = Protocol.getProtocolForBinding(bindingProto);

        HandlerChainProcessor processor = new HandlerChainProcessor(handlers, proto);
        // if not one-way, expect a response
        boolean success = true;
        try {
            if (mepMessageCtx.getMessageObject().isFault()) {
                processor.processFault(mepMessageCtx, HandlerChainProcessor.Direction.OUT);
            } else {
                success =
                        processor.processChain(mepMessageCtx,
                                               HandlerChainProcessor.Direction.OUT,
                                               mep,
                                               !isOneWay);
            }
        } catch (RuntimeException re) {
            /*
             * handler framework will throw an exception here on client outbound flow and
             * server outbound flow.  Make sure the message context and message are transformed
             * and the exception is saved on the message context.
             */
            HandlerChainProcessor.convertToFaultMessage(mepMessageCtx, re, proto);
            if (mepMessageCtx.getRequestMessageContext() != null) {
                mepMessageCtx.getRequestMessageContext().setCausedByException(new AxisFault(re.getMessage(), re));
            }
            if (mepMessageCtx.getResponseMessageContext() != null) {
                mepMessageCtx.getResponseMessageContext().setCausedByException(new AxisFault(re.getMessage(), re));
            }
            return false;
        }

        if (!success && mep.equals(HandlerChainProcessor.MEP.REQUEST)) {
            // uh-oh.  We've changed directions on the client outbound handler processing,
            // This means we're now on an inbound flow, and the service will not
            // be called.  Be sure to mark the context and message as such.
            return false;
        }
        return true;

    }
    /**
     * Invoke Inbound Handlers
     * @param requestMsgCtx
     */
    public static boolean invokeInboundHandlersForClosure(MEPContext mepMessageCtx, List<Handler> handlers,
                                                HandlerChainProcessor.MEP mep) {

        if (handlers == null || handlers.isEmpty()) 
            return true;
        if(log.isDebugEnabled()){
            log.debug("invokeInboundHandlersForClosure Entry");
        }

        String bindingProto = null;
        if (mep.equals(HandlerChainProcessor.MEP.REQUEST)) // inbound request; must be on the server
            bindingProto = mepMessageCtx.getEndpointDesc().getBindingType();
        else {
            // inbound response; must be on the client
            bindingProto = mepMessageCtx.getEndpointDesc().getClientBindingID();
        }
        Protocol proto = Protocol.getProtocolForBinding(bindingProto);

        HandlerChainProcessor processor = new HandlerChainProcessor(handlers, proto);
        // if not one-way, expect a response
        boolean success = true;
        try {
                success =
                        processor.processChainForClose(mepMessageCtx,
                                               HandlerChainProcessor.Direction.OUT);
        } catch (RuntimeException re) {
            /*
             * handler framework should never throw an exception on close
             */
            if(log.isDebugEnabled()){
                log.debug("invokeInboundHandlersForClosure caught exception from close: " + re);
                log.debug("invokeInboundHandlersForClosure returning false");
            }
            return false;
        }
        if(log.isDebugEnabled()){
            log.debug("invokeInboundHandlersForClosure Exit");
        }

        return true;
    }
}
