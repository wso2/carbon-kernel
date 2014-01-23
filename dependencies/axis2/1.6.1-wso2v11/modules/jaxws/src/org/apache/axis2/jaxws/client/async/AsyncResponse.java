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

package org.apache.axis2.jaxws.client.async;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.handler.AttachmentsAdapter;
import org.apache.axis2.jaxws.handler.HandlerChainProcessor;
import org.apache.axis2.jaxws.handler.HandlerInvokerUtils;
import org.apache.axis2.jaxws.handler.SOAPHeadersAdapter;
import org.apache.axis2.jaxws.handler.TransportHeadersAdapter;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.attachments.AttachmentUtils;
import org.apache.axis2.jaxws.spi.Constants;
import org.apache.axis2.jaxws.spi.migrator.ApplicationContextMigratorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.Response;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
/**
 * The AsyncResponse class is used to collect the response information from Axis2 and deliver it to
 * a JAX-WS client.  AsyncResponse implements the <link>javax.xml.ws.Response</link> API that is
 * defined in the JAX-WS 2.0 specification.  The <code>Response</code> object will contain both the
 * object that is returned as the response along with a <link>java.util.Map</link> with the context
 * information of the response.
 */
public abstract class AsyncResponse implements Response {

    private static final Log log = LogFactory.getLog(AsyncResponse.class);

    private boolean cancelled;

    private Throwable fault;
    private MessageContext faultMessageContext;
    private MessageContext response;

    private EndpointDescription endpointDescription;
    private Map<String, Object> responseContext;

    // CountDownLatch is used to track whether we've received and
    // processed the async response.  For example, the client app
    // could be polling on 30 second intervals, and we don't receive
    // the async response until the 1:15 mark.  In that case, the
    // first few polls calling the .get() would hit the latch.await()
    // which blocks the thread if the latch count > 0
    private CountDownLatch latch;
    private boolean cacheValid = false;
    private Object cachedObject = null;
    
    // The response business object to be returned
    private Object responseObject = null;
    
    // The exception to be returned in the event of a fault or failure in 
    // processing the response content.
    private ExecutionException savedException = null;
    
    protected AsyncResponse(EndpointDescription ed) {
        endpointDescription = ed;
        latch = new CountDownLatch(1);
    }

    protected void onError(Throwable flt, MessageContext mc, ClassLoader cl) {
        ClassLoader contextCL = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
        onError(flt, mc);
        checkClassLoader(cl, contextCL);
    }
    
    /**
     * @param flt Throwable fault that occurred
     * @param faultCtx MessageContext if fault is a SOAP Fault
     */
    protected void onError(Throwable flt, MessageContext faultCtx) {
        // Note:
        // This code is hardened to prevent a secondary exception from being
        // thrown back to the caller of onError.  It is likely that a
        // thrown exception will cause other errors leading to 
        // system fragility.
        if (log.isDebugEnabled()) {
            log.debug("AsyncResponse received a fault.");
        }
        Throwable t = null;
        try {
            fault = flt;
            faultMessageContext = faultCtx;
            if (faultMessageContext != null) {
                faultMessageContext.setEndpointDescription(endpointDescription);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("The faultMessageContext is not available because the error likely occurred on" +
                    		" the client and is not the result of a SOAP Fault");
                }
            }

            // Probably a good idea to invalidate the cache
            cacheValid = false;
            cachedObject = null;

            t = processFaultResponse();
        } catch (Throwable unexpected) {
            // An unexpected error occurred while processing the fault.
            // The Response's throwable is set to this unexpected exception.
            if (log.isDebugEnabled()) {
                log.debug("A secondary exception occurred during onError processing: " + 
                        unexpected);
            }
            t = unexpected;
        }
        
        // JAXWS 4.3.3 conformance bullet says to throw an ExecutionException from here
        savedException = new ExecutionException(t);
         
        try {
            // Countdown so that the Future object will know that procesing is complete.
            latch.countDown();

            if (log.isDebugEnabled()) {
                log.debug("New latch count = [" + latch.getCount() + "]");
            }
        } catch (Throwable unexpected) {
            // An unexpected error occurred after processing the fault response
            // The Response's throwable has already been set to the savedException
            if (log.isDebugEnabled()) {
                log.debug("A secondary exception occurred during onError processing " +
                		"after the fault is processed: " + unexpected);
            }
        }
    }
    
    /**
     * Check for a valid relationship between unmarshalling classloader and
     * Application's current context classloader
     * @param cl
     * @param contextCL
     */
    private void checkClassLoader(final ClassLoader cl, final ClassLoader contextCL) {
        // Ensure that the classloader (cl) used for unmarshalling is the same
        // or a parent of the current context classloader.  Otherwise 
        // ClassCastExceptions can occur
        if(log.isDebugEnabled()) {
            log.debug("AsyncResponse ClassLoader is:");
            log.debug(cl.toString());
        }
        if (cl.equals(contextCL)) {
            if(log.isDebugEnabled()) {
                log.debug("AsyncResponse ClassLoader matches Context ClassLoader");
            }
            return;
        } else {
            if(log.isDebugEnabled()) {
                log.debug("Context ClassLoader is:");
                log.debug(contextCL.toString());
            }
            ClassLoader parent = getParentClassLoader(contextCL);
            while(parent != null) {
                if (parent.equals(cl)) {
                    return;
                }
                if(log.isDebugEnabled()) {
                    log.debug("AsyncResponse ClassLoader is an ancestor of the Context ClassLoader");
                }
                parent = getParentClassLoader(parent);
            }
        }
        throw ExceptionFactory.
          makeWebServiceException(Messages.getMessage("threadClsLoaderErr",
                   contextCL.getClass().toString(), cl.getClass().toString()));
    }
    
    
    ClassLoader getParentClassLoader(final ClassLoader cl) {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return cl.getParent();
            }
        });
    }
    
    protected void onComplete(MessageContext mc, ClassLoader cl) {
        ClassLoader contextCL = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
        onComplete(mc);
        checkClassLoader(cl, contextCL);
    }

    protected void onComplete(MessageContext mc) {
        if (log.isDebugEnabled()) {
            log.debug("AsyncResponse received a MessageContext.");
        }

        // A new message context invalidates the cached object retrieved
        // during the last get()
        if (response != mc) {
            cachedObject = null;
            cacheValid = false;
        }

        response = mc;
        response.setEndpointDescription(endpointDescription);
        
        // Check for cached attachment file(s) if attachments exist. 
        if (response.getAxisMessageContext().getAttachmentMap() != null){
            AttachmentUtils.findCachedAttachment(response.getAxisMessageContext().getAttachmentMap());
        }
        
        // Process the response as soon as it is available.  This means making sure that
        // no content is left unread in the response stream.  Leaving content there could
        // result in an error if the runtime is greedy about cleaning up.
        try {
            responseObject = processResponse();
        } catch (ExecutionException e) {
            savedException = e;
            if (log.isDebugEnabled()) {
                log.debug("An error occurred while processing the response: " + e.getCause());
            }
            latch.countDown();
        }

        // Countdown so that the Future object will know that procesing is complete.
        latch.countDown();
        if (log.isDebugEnabled()) {
            log.debug("New latch count = [" + latch.getCount() + "]");
        }
    }

    //-------------------------------------
    // javax.xml.ws.Response APIs
    //-------------------------------------

    public boolean cancel(boolean mayInterruptIfRunning) {
        // The task cannot be cancelled if it has already been cancelled
        // before or if it has already completed.
        if (cancelled || latch.getCount() == 0) {
            if (log.isDebugEnabled()) {
                log.debug("Cancellation attempt failed.");
            }
            return false;
        }

        cancelled = true;
        return cancelled;
    }

    public Object get() throws InterruptedException, ExecutionException {
        if (cancelled) {
            throw new CancellationException(Messages.getMessage("getErr"));
        }

        // Wait for the response to come back
        if (log.isDebugEnabled()) {
            log.debug("Waiting for async response delivery.");
        }
        
        // If latch count > 0, it means we have not yet received
        // and processed the async response, and must block the
        // thread.
        latch.await();

        if (savedException != null) {
            throw savedException;
        }
        
        return responseObject;
    }

    public Object get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (cancelled) {
            throw new CancellationException(Messages.getMessage("getErr"));
        }

        // Wait for the response to come back
        if (log.isDebugEnabled()) {
            log.debug("Waiting for async response delivery with time out.");
            log.debug("timeout = " + timeout);
            log.debug("units   = " + unit);
        }
        
        // latch.await will only block if its count is > 0
        latch.await(timeout, unit);

        if (savedException != null) {
            throw savedException;
        }
        
        // If the response still hasn't been returned, then we've timed out
        // and must throw a TimeoutException
        if (latch.getCount() > 0) {
            throw new TimeoutException(Messages.getMessage("getErr1"));
        }

        return responseObject;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isDone() {
        return (latch.getCount() == 0);
    }

    public Map getContext() {
        return responseContext;
    }

    private Object processResponse() throws ExecutionException {
    	
        // If we don't have a fault, then we have to have a MessageContext for the response.
        if (response == null) {
            latch.countDown();
            throw new ExecutionException(ExceptionFactory.makeWebServiceException(Messages.getMessage("processRespErr")));
        }

        // Avoid a reparse of the message. If we already retrived the object, return
        // it now.
        if (cacheValid) {
            if (log.isDebugEnabled()) {
                log.debug("Return object cached from last get()");
            }
            return cachedObject;
        }

        Object obj = null;
        try {
            // Install the adapters and invoke inbound handlers.
            TransportHeadersAdapter.install(response);
            AttachmentsAdapter.install(response);
            SOAPHeadersAdapter.install(response);
            HandlerInvokerUtils.invokeInboundHandlers(response.getMEPContext(),
                                                      response.getInvocationContext().getHandlers(),
                                                      HandlerChainProcessor.MEP.RESPONSE,
                                                      false);

            // TODO: IMPORTANT: this is the right call here, but beware that the 
            // messagecontext may be turned into a fault context with a fault message.  
            // We need to check for this and, if necessary, make an exception and throw it.
            if (log.isDebugEnabled()) {
                log.debug("Unmarshalling the async response message.");
            }
            
            // Do the real work to unmarshall the response.
            obj = getResponseValueObject(response);
            
            if (log.isDebugEnabled() && obj != null) {
                log.debug("Unmarshalled response object of type: " + obj.getClass());
            }
            
            // Cache the object in case it is required again
            cacheValid = true;
            cachedObject = obj;

            responseContext = new HashMap<String, Object>();

            // Migrate the properties from the response MessageContext back
            // to the client response context bag.
            ApplicationContextMigratorUtil.performMigrationFromMessageContext(Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID,
                                                                              responseContext,
                                                                              response);
        } catch (Throwable t) {
            throw new ExecutionException(ExceptionFactory.makeWebServiceException(t));
        }

        return obj;
    }

    private Throwable processFaultResponse() {
        // A faultMessageContext means that there could possibly be a SOAPFault
        // on the MessageContext that we need to unmarshall.
        if (faultMessageContext != null) {
        	Throwable throwable = null;
            // it is possible the message could be null.  For example, if we gave the proxy a bad endpoint address.
            // If it is the case that the message is null, there's no sense running through the handlers.
            if (faultMessageContext.getMessage() != null) {
                // The adapters are intentionally NOT installed here.  They cause unit test failures
                // TransportHeadersAdapter.install(faultMessageContext);
                // AttachmentsAdapter.install(faultMessageContext);
            	try {
                    // Invoke inbound handlers.
            	    if (log.isDebugEnabled()) {
            	        log.debug("Invoking the JAX-WS handler chain for the fault response.");
            	    }
                    HandlerInvokerUtils.invokeInboundHandlers(faultMessageContext.getMEPContext(),
                                                          faultMessageContext.getInvocationContext()
                                                                             .getHandlers(),
                                                          HandlerChainProcessor.MEP.RESPONSE,
                                                          false);
            	} catch (Throwable t) {
            	    if (log.isDebugEnabled()) {
            	        log.debug("An error occurred (" + t.getClass() + " while processing " +
            	        "the fault response handler chain.");
            	    }
            	    throwable = t;
            	}
            }
            if (throwable == null) {
                // Do the real work to unmarshal the fault response.
                throwable = getFaultResponse(faultMessageContext);
            }
            
            if (throwable != null) {
                return throwable;
            } else {
                return ExceptionFactory.makeWebServiceException(fault);
            }
        } else {
            return ExceptionFactory.makeWebServiceException(fault);
        }
    }

    public abstract Object getResponseValueObject(MessageContext mc);

    public abstract Throwable getFaultResponse(MessageContext mc);

}
