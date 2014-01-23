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

package org.apache.axis2.jaxws.core.controller.impl;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.controller.InvocationController;
import org.apache.axis2.jaxws.core.controller.InvocationPattern;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.handler.AttachmentsAdapter;
import org.apache.axis2.jaxws.handler.HandlerChainProcessor;
import org.apache.axis2.jaxws.handler.HandlerInvokerUtils;
import org.apache.axis2.jaxws.handler.SOAPHeadersAdapter;
import org.apache.axis2.jaxws.handler.TransportHeadersAdapter;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set; 
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * An abstract implementation of the InvocationController interface. 
 */
public abstract class InvocationControllerImpl implements InvocationController {

    private static final Log log = LogFactory.getLog(InvocationControllerImpl.class);

    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.core.controller.InvocationController#invoke(org.apache.axis2.jaxws.core.InvocationContext)
     */
    public InvocationContext invoke(InvocationContext ic) {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: synchronous");
        }

        // Check to make sure we at least have a valid InvocationContext
        // and request MessageContext
        if (ic == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr1"));
        }
        if (ic.getRequestMessageContext() == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr2"));
        }

        MessageContext request = ic.getRequestMessageContext();
        MessageContext response = null;

        request.setProperty(Constants.INVOCATION_PATTERN, InvocationPattern.SYNC);

        // Invoke outbound handlers.
        boolean success =
                HandlerInvokerUtils.invokeOutboundHandlers(request.getMEPContext(),
                                                           ic.getHandlers(),
                                                           HandlerChainProcessor.MEP.REQUEST,
                                                           false);

        if (success) {
            // If there are any headers understood by handlers, then set a property
            // on the message context so the response mustUnderstand processing takes those
            // understood headers into consideration.
            registerUnderstoodHeaders(request, ic.getHandlers()); 
            prepareRequest(request);
            response = doInvoke(request);
            prepareResponse(response);
            
            // make sure request and response contexts share a single parent
            response.setMEPContext(request.getMEPContext());

            /*
             * TODO TODO TODO review
             * 
             * In most cases we are adding the endpointDesc to the
             * MessageContext. Notice here that the "response" object is set by
             * the call to doInvoke. It's a new context we are now working with.
             * The invokeInboundHandlers uses that context way down in
             * createMessageContext --> ContextUtils.addProperties()
             * 
             * This may also occur in the AsyncResponse class when calling
             * invokeInboundHandlers
             * 
             * For now, make sure the endpointDesc is set on the response
             * context.
             */
            response.setEndpointDescription(request.getEndpointDescription());

            // Invoke inbound handlers.
            TransportHeadersAdapter.install(response);
            AttachmentsAdapter.install(response);
            SOAPHeadersAdapter.install(response);
            HandlerInvokerUtils.invokeInboundHandlers(response.getMEPContext(),
                                                      ic.getHandlers(),
                                                      HandlerChainProcessor.MEP.RESPONSE,
                                                      false);
        } else { // the outbound handler chain must have had a problem, and
                    // we've reversed directions
            response = MessageContextUtils.createMinimalResponseMessageContext(request);
            // since we've reversed directions, the message has "become a
            // make sure request and response contexts share a single parent
            response.setMEPContext(request.getMEPContext());
            response.setMessage(request.getMessage());
        }
        ic.setResponseMessageContext(response);
        return ic;
    }

    protected abstract MessageContext doInvoke(MessageContext request);

    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.core.controller.InvocationController#invokeOneWay(org.apache.axis2.jaxws.core.InvocationContext)
     */
    public void invokeOneWay(InvocationContext ic) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: one-way");
        }

        // Check to make sure we at least have a valid InvocationContext
        // and request MessageContext
        if (ic == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr1"));
        }
        if (ic.getRequestMessageContext() == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr2"));
        }

        MessageContext request = ic.getRequestMessageContext();
        request.setProperty(Constants.INVOCATION_PATTERN, InvocationPattern.ONEWAY);

        // Invoke outbound handlers.
        boolean success =
                HandlerInvokerUtils.invokeOutboundHandlers(request.getMEPContext(),
                                                           ic.getHandlers(),
                                                           HandlerChainProcessor.MEP.REQUEST,
                                                           true);

        if (success) {
            prepareRequest(request);
            doInvokeOneWay(request);
        } else { // the outbound handler chain must have had a problem, and we've reversed directions
            // check to see if problem is due to a handler throwing an exception.  If so, throw it,
            // even in this oneWay invoke.
            Exception e = request.getCausedByException();
            if (e != null) {
                throw (Exception)e.getCause();
            }
        }
        return;
    }

    protected abstract void doInvokeOneWay(MessageContext mc);

    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.core.controller.InvocationController#invokeAsync(org.apache.axis2.jaxws.core.InvocationContext)
     */
    public Response invokeAsync(InvocationContext ic) {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: asynchronous(polling)");
        }

        // Check to make sure we at least have a valid InvocationContext
        // and request MessageContext
        if (ic == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr1"));
        }
        if (ic.getRequestMessageContext() == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr2"));
        }

        MessageContext request = ic.getRequestMessageContext();
        request.setProperty(Constants.INVOCATION_PATTERN, InvocationPattern.ASYNC_POLLING);

        Response resp = null;

        // Invoke outbound handlers.
        // TODO uncomment, and get the EndpointDescription from the request context, which should soon be available
        boolean success =
                HandlerInvokerUtils.invokeOutboundHandlers(request.getMEPContext(),
                                                           ic.getHandlers(),
                                                           HandlerChainProcessor.MEP.REQUEST,
                                                           false);
        if (success) {
            // If there are any headers understood by handlers, then set a property
            // on the message context so the response mustUnderstand processing takes those
            // understood headers into consideration.
            registerUnderstoodHeaders(request, ic.getHandlers()); 
            prepareRequest(request);
            resp = doInvokeAsync(request);
        } else
        { // the outbound handler chain must have had a problem, and we've reversed directions
            // since we've reversed directions, the message has "become a response message" (section 9.3.2.1, footnote superscript 2)

            // TODO we know the message is a fault message, we should
            // convert it to an exception and throw it.
            // something like:

            //throw new AxisFault(request.getMessage());
        }

        return resp;
    }

    public abstract Response doInvokeAsync(MessageContext mc);

    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.core.controller.InvocationController#invokeAsync(org.apache.axis2.jaxws.core.InvocationContext, javax.xml.ws.AsyncHandler)
     */
    public Future<?> invokeAsync(InvocationContext ic, AsyncHandler asyncHandler) {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: asynchronous(callback)");
        }

        // Check to make sure we at least have a valid InvocationContext
        // and request MessageContext
        if (ic == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr1"));
        }
        if (ic.getRequestMessageContext() == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr2"));
        }
        if ((ic.getExecutor() != null) && (ic.getExecutor() instanceof ExecutorService)) {
            ExecutorService es = (ExecutorService) ic.getExecutor();
            if (es.isShutdown()) {
                // the executor service is shutdown and won't accept new tasks
                // so return an error back to the client
                throw ExceptionFactory.makeWebServiceException(Messages
                                .getMessage("ExecutorShutdown"));
            }
        }

        MessageContext request = ic.getRequestMessageContext();
        request.setProperty(Constants.INVOCATION_PATTERN, InvocationPattern.ASYNC_CALLBACK);

        Future<?> future = null;

        // Invoke outbound handlers.
        boolean success =
                HandlerInvokerUtils.invokeOutboundHandlers(request.getMEPContext(),
                                                           ic.getHandlers(),
                                                           HandlerChainProcessor.MEP.REQUEST,
                                                           false);
        if (success) {
            // If there are any headers understood by handlers, then set a property
            // on the message context so the response mustUnderstand processing takes those
            // understood headers into consideration.
            registerUnderstoodHeaders(request, ic.getHandlers()); 
            prepareRequest(request);
            future = doInvokeAsync(request, asyncHandler);
        } else { // the outbound handler chain must have had a problem, and
                    // we've reversed directions
            // since we've reversed directions, the message has "become a
            // response message" (section 9.3.2.1, footnote superscript 2)

            // TODO: how do we deal with this? The response message may or may
            // not be a fault
            // message. We do know that the direction has reversed, so somehow
            // we need to
            // flow immediately out of the async and give the exception and/or
            // response object
            // back to the client app without calling
            // AsyncResponse.processResponse or processFault
                
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("invokeAsyncErr"));

            // throw new AxisFault(request.getMessage());
        }
        return future;
    }

    public abstract Future<?> doInvokeAsync(MessageContext mc, AsyncHandler asyncHandler);

    /**
     * Abstract method that must be implemented by whoever is providing the specific client binding.
     *  Once this is called, everything that is needed to invoke the operation must be available in
     * the MessageContext.
     *
     * @param mc
     */
    protected abstract void prepareRequest(MessageContext mc);

    /**
     * Abstract method that must be implemented by whoever is providing the specific client binding.
     *  This is called after the response has come back and allows the client binding to put
     * whatever info it has in the response MessageContext.
     *
     * @param mc
     */
    protected abstract void prepareResponse(MessageContext mc);

    private void registerUnderstoodHeaders(MessageContext request,
                                           List<Handler> handlerList) {
      if (handlerList != null && handlerList.size() > 0) {
        Set<QName> understoodHeaders = new HashSet<QName>();
        
        // Add the headers from each of the SOAP handlers to the collection
        for (Handler handler : handlerList) {
          if(handler instanceof SOAPHandler){
            SOAPHandler soapHandler = (SOAPHandler)handler;
            Set<QName> headers = soapHandler.getHeaders();
            if (headers != null && headers.size() > 0) {
              understoodHeaders.addAll(headers);
            }
          }
        }
        
        // Put the understood header qnames on the request context where it can
        // be found during response processing.
        if (understoodHeaders != null && understoodHeaders.size() > 0) {
          if (log.isDebugEnabled()) {
            log.debug("Adding understood header QName collection to message context " + understoodHeaders);
          }
          request.setProperty("client.UnderstoodHeaders", understoodHeaders);
        }
      }
    }
}
