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

package org.apache.axis2.jaxws.server;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.InvocationContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.handler.AttachmentsAdapter;
import org.apache.axis2.jaxws.handler.MEPContext;
import org.apache.axis2.jaxws.handler.SOAPHeadersAdapter;
import org.apache.axis2.jaxws.handler.TransportHeadersAdapter;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.util.MessageUtils;
import org.apache.axis2.jaxws.registry.InvocationListenerRegistry;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.ThreadContextMigratorUtil;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004_Constants;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2006Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceException;
import java.security.PrivilegedAction;

/**
 * The JAXWSMessageReceiver is the entry point, from the server's perspective, to the JAX-WS code.
 * This will be called by the Axis Engine and is the end of the chain from an Axis2 perspective.
 */
public class JAXWSMessageReceiver implements MessageReceiver {

    private static final Log log = LogFactory.getLog(JAXWSMessageReceiver.class);

    private static String PARAM_SERVICE_CLASS = "ServiceClass";
    public static String PARAM_BINDING = "Binding";

    /**
     * We should have already determined which AxisService we're targetting at this point.  So now,
     * just get the service implementation and invoke the appropriate method.
     * @param axisRequestMsgCtx
     * @throws org.apache.axis2.AxisFault
     */
    public void receive(org.apache.axis2.context.MessageContext axisRequestMsgCtx)
            throws AxisFault {
        AxisFault faultToReturn = null;

        if (log.isDebugEnabled()) {
            log.debug("new request received");
        }

        //Get the name of the service impl that was stored as a parameter
        // inside of the services.xml.
        AxisService service = axisRequestMsgCtx.getAxisService();

        // we need to set the deployment class loader as the TCCL. This is because, in JAX-WS
        // services, there can be situations where we have to load classes from the deployment
        // artifact (JAX-WS jar file) in the message flow. Ex: Handler classes in the service
        // artifact. Adding this as a fix for AXIS2-4930.
        setContextClassLoader(service.getClassLoader());

        org.apache.axis2.description.Parameter svcClassParam =
                service.getParameter(PARAM_SERVICE_CLASS);

        if (svcClassParam == null) {
            throw new RuntimeException(
                    Messages.getMessage("JAXWSMessageReceiverNoServiceClass"));
        }

        Parameter endpointDescParam =
                service.getParameter(EndpointDescription.AXIS_SERVICE_PARAMETER);
        if (endpointDescParam == null) {
            throw new RuntimeException(Messages.getMessage("JAXWSMessageReceiverNoServiceClass"));
        }
        AxisOperation operation = axisRequestMsgCtx.getAxisOperation();
        String mep = operation.getMessageExchangePattern();
        if (log.isDebugEnabled()) {
            log.debug("MEP: " + mep);
        }

        try {

            //This assumes that we are on the ultimate execution thread
            ThreadContextMigratorUtil.performMigrationToThread(
                    Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisRequestMsgCtx);

            //We'll need an instance of the EndpointController to actually
            //drive the invocation.
            //TODO: More work needed to determine the lifecycle of this thing
            EndpointController endpointCtlr = new EndpointController();

            MessageContext requestMsgCtx = new MessageContext(axisRequestMsgCtx);
            requestMsgCtx.setServer(true);
            requestMsgCtx.setMEPContext(new MEPContext(requestMsgCtx));
            ClassLoader loader = getCachedClassLoader(axisRequestMsgCtx);
            if (loader != null) {
                requestMsgCtx.setProperty(org.apache.axis2.jaxws.spi.Constants.CACHE_CLASSLOADER,
                        loader);
            }
            // The adapters need to be installed on the new request Message Context
            AttachmentsAdapter.install(requestMsgCtx);
            TransportHeadersAdapter.install(requestMsgCtx);
            SOAPHeadersAdapter.install(requestMsgCtx);
            
            Binding binding = (Binding)axisRequestMsgCtx.getProperty(PARAM_BINDING);
            EndpointInvocationContext eic = InvocationContextFactory.createEndpointInvocationContext(binding);
            addInvocationListenerFactories(eic);
            eic.setRequestMessageContext(requestMsgCtx);

            // WARNING: This should be left disabled for now.  This locks the server side
            // into a single threaded invocation.
            eic.getRequestMessageContext().setProperty(ServerConstants.SERVER_DISABLE_THREAD_SWITCH, true);

            if (isMepInOnly(mep)) {
                if (log.isDebugEnabled()) {
                    log.debug("Detected a one way invocation.");
                }
                eic.setIsOneWay(true);
                endpointCtlr.invokeOneWay(eic);
            } else if (JavaUtils.isTrueExplicitly(axisRequestMsgCtx.getProperty(
                AddressingConstants.IS_ADDR_INFO_ALREADY_PROCESSED))
                && (axisRequestMsgCtx.getReplyTo() != null
                && !axisRequestMsgCtx.getReplyTo().hasAnonymousAddress())) {
                
                if (log.isDebugEnabled()) {
                    log.debug("Detected an async invocation.");
                }
                
                EndpointCallback ecb = new EndpointCallback();
                eic.setCallback(ecb);
                
                endpointCtlr.invokeAsync(eic);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Detected a sync invocation.");
                }
                eic = endpointCtlr.invoke(eic);

                // If this is a two-way exchange, there should already be a 
                // JAX-WS MessageContext for the response.  We need to pull 
                // the Message data out of there and set it on the Axis2 
                // MessageContext.
                MessageContext responseMsgCtx = eic.getResponseMessageContext();
                // Note that responseMsgCtx may be null if the Provider returned null
                // and no wsdl was specified.
                // In JAX-WS 2.2 for Providers that return null we should send back
                // an empty payload, not a SOAPEnvelope.
                if (responseMsgCtx == null &&
                        MessageContextUtils.getJaxwsProviderInterpretNullOneway(requestMsgCtx)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Detected a null return from a Provider, sending back an ack instead of a response.");
                    }
                    sendAckBack(axisRequestMsgCtx);                   
                } else {
                    org.apache.axis2.context.MessageContext axisResponseMsgCtx =
                        responseMsgCtx.getAxisMessageContext();
                    if (loader != null) {
                        responseMsgCtx.setProperty(org.apache.axis2.jaxws.spi.Constants.CACHE_CLASSLOADER,
                                loader);
                    }
                    MessageUtils.putMessageOnMessageContext(responseMsgCtx.getMessage(),
                            axisResponseMsgCtx);

                    OperationContext opCtx = axisResponseMsgCtx.getOperationContext();
                    opCtx.addMessageContext(axisResponseMsgCtx);

                    // If this is a fault message, we want to throw it as an
                    // exception so that the transport can do the appropriate things
                    if (responseMsgCtx.getMessage().isFault()) {

                        //Rather than create a new AxisFault, we should use the AxisFault that was
                        //created at the causedBy
                        if (responseMsgCtx.getCausedByException() != null) {
                            faultToReturn = responseMsgCtx.getCausedByException();
                            if (log.isDebugEnabled()) {
                                log.debug("Setting causedByException from response MessageContext");
                            }
                        } else if (requestMsgCtx.getCausedByException() != null) {
                            faultToReturn = requestMsgCtx.getCausedByException();
                            if (log.isDebugEnabled()) {
                                log.debug("Setting causedByException from request MessageContext..which indicates an exception occured in the inbound handler processing");
                            }
                        } else {
                            faultToReturn = new AxisFault("An error was detected during JAXWS processing",
                                    axisResponseMsgCtx);
                            if (log.isDebugEnabled()) {
                                log.debug("No causedByException detected");
                            }
                        }
                    } else {
                    //This assumes that we are on the ultimate execution thread
                    ThreadContextMigratorUtil.performMigrationToContext(
                            Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisResponseMsgCtx);

                    //Create the AxisEngine for the reponse and send it.
                    AxisEngine.send(axisResponseMsgCtx);
                    //This assumes that we are on the ultimate execution thread
                    ThreadContextMigratorUtil.performContextCleanup(
                            Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisResponseMsgCtx);
                }
            }
            }
        } catch (AxisFault af) {
            throw af;
        } catch (Exception e) {
            ThreadContextMigratorUtil.performThreadCleanup(
                    Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisRequestMsgCtx);

            //e.printStackTrace();

            // TODO.  This is throwing a client exception ?
            // TODO Why are we preserving the stack information  ?  
            
            // Make a webservice exception (which will strip out a unnecessary stuff)
            WebServiceException wse = ExceptionFactory.makeWebServiceException(e);

            // The AxisEngine expects an AxisFault
            throw AxisFault.makeFault(wse);

        }

        //This assumes that we are on the ultimate execution thread
        ThreadContextMigratorUtil
                .performThreadCleanup(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisRequestMsgCtx);

        if (faultToReturn != null) {
            throw faultToReturn;
        }
    }

    /**
     * Set context class loader of the current thread.
     *
     * @param cl the context ClassLoader for the Thread
     */
    private void setContextClassLoader(final ClassLoader cl) {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Thread.currentThread().setContextClassLoader(cl);
                return null;
            }
        });
    }

    private void sendAckBack(org.apache.axis2.context.MessageContext axisMsgCtx){
        if (log.isDebugEnabled()) {
            log.debug("sendAckBack entry");
        }

        try {
            Object requestResponseTransport =
                axisMsgCtx.getProperty(RequestResponseTransport.TRANSPORT_CONTROL);
            if (requestResponseTransport != null) {
                ((RequestResponseTransport) requestResponseTransport).acknowledgeMessage(axisMsgCtx);
            }
        }catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring exception from acknowledgeMessage.", e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("sendAckBack exit");
        }

    }
 
    private boolean isMepInOnly(String mep) {
        boolean inOnly = mep.equals(WSDL20_2004_Constants.MEP_URI_ROBUST_IN_ONLY) ||
                mep.equals(WSDL20_2004_Constants.MEP_URI_IN_ONLY) ||
                mep.equals(WSDL2Constants.MEP_URI_IN_ONLY) ||
                mep.equals(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY) ||
                mep.equals(WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY) ||
                mep.equals(WSDL20_2006Constants.MEP_URI_IN_ONLY);
        return inOnly;
    }
    
    /**
     * Retrieves the registered InvocationListenerFactory instances and sets them
     * on the current EndpointInvocationContext.
     * @param eic
     */
    void addInvocationListenerFactories(EndpointInvocationContext eic) {
        eic.setInvocationListenerFactories(InvocationListenerRegistry.getFactories());
    }

    public ClassLoader getCachedClassLoader(org.apache.axis2.context.MessageContext msgContext) {
        return (ClassLoader) msgContext.getAxisService().getParameterValue(org.apache.axis2.jaxws.spi.Constants.CACHE_CLASSLOADER);
    }
}
