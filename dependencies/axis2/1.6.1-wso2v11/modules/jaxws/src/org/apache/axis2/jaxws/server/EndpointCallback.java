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
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.context.utils.ContextUtils;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.handler.HandlerChainProcessor;
import org.apache.axis2.jaxws.handler.HandlerInvocationContext;
import org.apache.axis2.jaxws.handler.HandlerInvoker;
import org.apache.axis2.jaxws.handler.factory.HandlerInvokerFactory;
import org.apache.axis2.jaxws.message.util.MessageUtils;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.axis2.util.ThreadContextMigratorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class EndpointCallback {
    
    private static final Log log = LogFactory.getLog(EndpointCallback.class);
    
    public void handleResponse(EndpointInvocationContext eic) {
        MessageContext responseMsgCtx = eic.getResponseMessageContext();
        org.apache.axis2.context.MessageContext axisResponseMsgCtx =
                responseMsgCtx.getAxisMessageContext();

        try {
            if (log.isDebugEnabled()) {
                log.debug("start handleResponse");
            }
            invokeOutboundHandlerFlow(eic);
            responseReady(eic);
            MessageUtils.putMessageOnMessageContext(responseMsgCtx.getMessage(),
                                                    axisResponseMsgCtx);

            OperationContext opCtx = axisResponseMsgCtx.getOperationContext();
            opCtx.addMessageContext(axisResponseMsgCtx);         
            
            
            if (log.isDebugEnabled()) {
                log.debug("perform thread migration");
            }
            // This assumes that we are on the ultimate execution thread
            ThreadContextMigratorUtil.performMigrationToContext(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID,
                                                                axisResponseMsgCtx);
    
            //Create the AxisEngine for the response and send it.
            if (log.isDebugEnabled()) {
                log.debug("Sending async response.");
            }
            AxisEngine.send(axisResponseMsgCtx);
            
            if (log.isDebugEnabled()) {
                log.debug("perform thread cleanup");
            }
            //This assumes that we are on the ultimate execution thread
            ThreadContextMigratorUtil.performContextCleanup(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID,
                                                            axisResponseMsgCtx);
        } catch (Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug("An error occurred while attempting to send the async response.");
                t.printStackTrace();
            }
            
            Throwable faultMessage = InvocationHelper.determineMappedException(t, eic);
            if(faultMessage != null) {
                t = faultMessage;
            }
            eic.getResponseMessageContext().setCausedByException(AxisFault.makeFault(t));
           
            ThreadContextMigratorUtil.performThreadCleanup(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID,
                eic.getRequestMessageContext().getAxisMessageContext());
            
            // FIXME (NLG): This is probably not right
            handleFaultResponse(eic);
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("release resources");
            }
            ContextUtils.releaseWebServiceContextResources(eic.getRequestMessageContext());
            
            if (log.isDebugEnabled()) {
                log.debug("end handleResponse");
            }
        }
    }
    
    public void handleFaultResponse(EndpointInvocationContext eic) {
        MessageContext responseMsgCtx = eic.getResponseMessageContext();
        org.apache.axis2.context.MessageContext axisResponseMsgCtx =
                responseMsgCtx.getAxisMessageContext();
        
        try {
            if (log.isDebugEnabled()) {
                log.debug("start handleFaultResponse");
            }
            
            responseReady(eic);
            MessageUtils.putMessageOnMessageContext(responseMsgCtx.getMessage(),
                axisResponseMsgCtx);

            OperationContext opCtx = axisResponseMsgCtx.getOperationContext();
            opCtx.addMessageContext(axisResponseMsgCtx);
            
            if (log.isDebugEnabled()) {
                log.debug("perform thread cleanup");
            }
            ThreadContextMigratorUtil.performThreadCleanup(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID,
                eic.getRequestMessageContext().getAxisMessageContext());
            
            //Create the AxisEngine for the reponse and send it.
            AxisEngine.sendFault(axisResponseMsgCtx);
            
        } catch (Throwable t) {
            Throwable faultMessage = InvocationHelper.determineMappedException(t, eic);
            if(faultMessage != null) {
                t = faultMessage;
            }
            
            throw ExceptionFactory.makeWebServiceException(AxisFault.makeFault(t));
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("release resources");
            }
            ContextUtils.releaseWebServiceContextResources(eic.getRequestMessageContext());
            if (log.isDebugEnabled()) {
                log.debug("end handleFaultResponse");
            }
        }
    }
    
    /** 
     * This will call the InvocationListener instances that were called during
     * the request processing for this message.
     */
    protected void responseReady(EndpointInvocationContext eic)  {
        List<InvocationListener> listenerList = eic.getInvocationListeners();
        if(listenerList != null) {
            InvocationListenerBean bean = new InvocationListenerBean(eic, InvocationListenerBean.State.RESPONSE);
            for(InvocationListener listener : listenerList) {
                try {
                    listener.notify(bean); 
                }
                catch(Exception e) {
                    throw ExceptionFactory.makeWebServiceException(e);
                }
            }
        }
    }
    
    /**
     * This method will drive the invocation of the outbound JAX-WS
     * application handler flow.
     */
    protected void invokeOutboundHandlerFlow(EndpointInvocationContext eic) {
        MessageContext request = eic.getRequestMessageContext();
        MessageContext response = eic.getResponseMessageContext();
        if (response != null) {
            // Invoke the outbound response handlers.
            // We can be sure we need to invoke any handlers because this
            // cannot be a one-way flow
            response.setMEPContext(request.getMEPContext());
            HandlerInvocationContext hiContext = EndpointController.buildHandlerInvocationContext(
                                                                               request, 
                                                                               eic.getHandlers(), 
                                                                               HandlerChainProcessor.MEP.RESPONSE,
                                                                               false);
            HandlerInvokerFactory hiFactory = (HandlerInvokerFactory) 
                FactoryRegistry.getFactory(HandlerInvokerFactory.class);
            HandlerInvoker handlerInvoker = hiFactory.createHandlerInvoker(response);
            handlerInvoker.invokeOutboundHandlers(hiContext);
        } 
    }
    
}
