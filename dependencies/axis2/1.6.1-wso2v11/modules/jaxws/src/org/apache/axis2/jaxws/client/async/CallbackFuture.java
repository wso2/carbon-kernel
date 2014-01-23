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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.AsyncHandlerProxyFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.WebServiceException;
import java.security.PrivilegedAction;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * The CallbackFuture implements the Axis2 <link>org.apache.axis2.client.async.Callback</link> API
 * and will get registered with the Axis2 engine to receive the asynchronous callback responses.
 * This object is also responsible for taking the <link>java.util.concurrent.Executor</link> given
 * to it by the JAX-WS client and using that as the thread on which to deliver the async response
 * the JAX-WS <link>javax.xml.ws.AsynchHandler</link>.
 */
public class CallbackFuture implements AxisCallback {

    private static final Log log = LogFactory.getLog(CallbackFuture.class);
    private static final boolean debug = log.isDebugEnabled();

    private CallbackFutureTask cft;
    private Executor executor;
    private FutureTask task;

    private InvocationContext invocationCtx;

    public static String displayHandle(Object obj) {
        return obj.getClass().getName() + '@' + Integer.toHexString(obj.hashCode());
    }

    /*
    * There are two Async Callback Future.cancel scenario that we address
    * 1) Client app creates request and call Async Operation. Now before the request is submitted
    *    by JAXWS to Executor for processing and any response is received client decides to cancel
    *    the future task.
    * 2) Client app creates request and call Async Operation. Request is submitted by JAXWS
    *    to Executor for processing and a response is received and client decides to cancel the future
    *    task.
    *
    * We will address both these scenarios in the code. In scenario 1 we will do the following:
    * 1) Check the for the future.isCancelled before submitting the task to Executor
    * 2) If cancelled then do not submit the task and do not call the Async Handler of client.
    * 3)The client program in this case (Since it cancelled the future) will be responsible for cleaning any resources that it engages.
    *
    * In Second Scenario we will call the AsyncHandler as Future.isCancelled will be false. As per java doc
    * the Future cannot be cancelled once the task has been submitted. Also the response has already arrived so
    * we will make the AsyncHandler and let the client code decided how it wants to treat the response.
    */

    @SuppressWarnings("unchecked")
    public CallbackFuture(InvocationContext ic, AsyncHandler handler) {

        // We need to save off the classloader associated with the AsyncHandler instance
        // since we'll need to set this same classloader on the thread where
        // handleResponse() is invoked.
        // This is required so that we don't encounter ClassCastExceptions.
        final Object handlerObj = handler;
        final ClassLoader handlerCL = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return handlerObj.getClass().getClassLoader();
            }
        });

        // Allow the AsyncHandlerProxyFactory to create the proxy for the AsyncHandler
        // passed in (which was provided by the client on the async invocation).
        // This allows any server-specific work to be done, such as thread context management, etc.
        AsyncHandler originalHandler = handler;
        try {
            if (debug) {
                log.debug("Calling factory to create proxy for AsyncHandler instance: " + displayHandle(handler));
            }
            AsyncHandlerProxyFactory proxyFactory = (AsyncHandlerProxyFactory) FactoryRegistry
                    .getFactory(AsyncHandlerProxyFactory.class);
            handler = proxyFactory.createAsyncHandlerProxy(handler);
            if (debug) {
                log.debug("Factory returned AsyncHandler proxy instance: " + displayHandle(handler));
            }
        }
        catch (Exception e) {
            if (debug) {
                log.debug("AsyncHandlerProxyFactory threw an exception: " + e.toString());
                e.printStackTrace();
            }

            // Just use the original handler provided by the client if we 
            // failed to create a proxy for it.
            handler = originalHandler;
        }

        cft = new CallbackFutureTask(ic.getAsyncResponseListener(), handler, handlerCL);
        task = new FutureTask(cft);
        executor = ic.getExecutor();

        /*
        * TODO review.  We need to save the invocation context so we can set it on the
        * response (or fault) context so the FutureCallback has access to the handler list.
        */
        invocationCtx = ic;
    }

    public Future<?> getFutureTask() {
        return (Future<?>)task;
    }


    public void onComplete(org.apache.axis2.context.MessageContext mc) {
        if (debug) {
            log.debug("JAX-WS received the async response");
        }

        MessageContext response = null;
        try {
            response = AsyncUtils.createJAXWSMessageContext(mc);
            response.setInvocationContext(invocationCtx);
            // make sure request and response contexts share a single parent
            response.setMEPContext(invocationCtx.getRequestMessageContext().getMEPContext());
        } catch (WebServiceException e) {
            cft.setError(e);
            if (debug) {
                log.debug(
                        "An error occured while processing the async response.  " + e.getMessage());
            }
        }

        if (response == null) {
            // TODO: throw an exception
        }

        cft.setMessageContext(response);
        execute();
    }


    public void onError(Exception e) {
        // If a SOAPFault was returned by the AxisEngine, the AxisFault
        // that is returned should have a MessageContext with it.  Use
        // this to unmarshall the fault included there.
        if (e.getClass().isAssignableFrom(AxisFault.class)) {
            AxisFault fault = (AxisFault)e;
            MessageContext faultMessageContext = null;
            try {
                faultMessageContext  = AsyncUtils.createJAXWSMessageContext(fault.getFaultMessageContext());
                faultMessageContext.setInvocationContext(invocationCtx);
                // make sure request and response contexts share a single parent
                faultMessageContext.setMEPContext(invocationCtx.getRequestMessageContext().getMEPContext());
            } catch (WebServiceException wse) {
                cft.setError(wse);
            }

            cft.setError(e);
            cft.setMessageContext(faultMessageContext);
        } else {
            cft.setError(e);
        }

        execute();
    }

    private void execute() {
        if (log.isDebugEnabled()) {
            log.debug("Executor task starting to process async response");
        }

        if (executor != null) {
            if (task != null && !task.isCancelled()) {
                try {
                    executor.execute(task);
                }
                catch (Exception executorExc) {
                    if (log.isDebugEnabled()) {
                        log.debug("CallbackFuture.execute():  executor exception [" +
                                executorExc.getClass().getName() + "]");
                    }

                    // attempt to cancel the FutureTask
                    task.cancel(true);

                    //   note: if it is becomes required to return the actual exception
                    //         to the client, then we would need to doing something
                    //         similar to setting the CallbackFutureTask with the error
                    //         and invoking the CallbackFutureTask.call() interface
                    //         to process the information
                    //
                }

                if (log.isDebugEnabled()) {
                    log.debug("Task submitted to Executor");
                }

                /*
                * TODO:  review
                * A thread switch will occur immediately after going out of scope
                * on this method.  This is ok, except on some platforms this will
                * prompt the JVM to clean up the old thread, thus cleaning up any
                * InputStreams there.  If that's the case, and we have not fully
                * read the InputStreams, we will likely get a NullPointerException
                * coming from the parser, which has a reference to the InputStream
                * that got nulled out from under it.  Make sure to do the
                * cft.notifyAll() in the right place.  CallbackFutureTask.call()
                * is the right place since at that point, the parser has fully read
                * the InputStream.
                */
                try {
                    synchronized (cft) {
                        if(!cft.done) {
                            cft.wait(180000);  // 3 minutes
                        }
                    }
                } catch (InterruptedException e) {
                    if (debug) {
                        log.debug("cft.wait() was interrupted");
                        log.debug("Exception: " + e.getMessage());
                    }
                }

            } else {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Executor task was not sumbitted as Async Future task was cancelled by clients");
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Executor task completed");
        }
    }

    public void onMessage(org.apache.axis2.context.MessageContext msgContext) {
        onComplete(msgContext);

    }

    public void onComplete() {

    }

    public void onFault(org.apache.axis2.context.MessageContext msgContext) {
        onComplete(msgContext);

    }
}

class CallbackFutureTask implements Callable {

    private static final Log log = LogFactory.getLog(CallbackFutureTask.class);
    private static final boolean debug = log.isDebugEnabled();

    AsyncResponse response;
    MessageContext msgCtx;
    AsyncHandler handler;
    ClassLoader handlerCL;
    Exception error;
    boolean done = false;

    CallbackFutureTask(AsyncResponse r, AsyncHandler h, ClassLoader cl) {
        response = r;
        handler = h;
        handlerCL = cl;
    }

    protected AsyncHandler getHandler() {
        return handler;
    }

    void setMessageContext(MessageContext mc) {
        msgCtx = mc;
    }

    void setError(Exception e) {
        error = e;
    }


    /*
    * An invocation of the call() method is what drives the response processing
    * for Callback clients.  The end result of this should be that the AysncHandler
    * (the callback instance) provided by the client is called and the response or
    * an error is delivered.
    */
    @SuppressWarnings("unchecked")
    public Object call() throws Exception {
        ClassLoader oldCL = null;
        try {

            if (log.isDebugEnabled()) {
                log.debug("Setting up the thread's context classLoader");
                log.debug(handlerCL.toString());
            }

            // Retrieve the existing classloader from the thread.
            oldCL = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            });

            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    Thread.currentThread().setContextClassLoader(handlerCL);
                    return null;
                }
            });

            // Set the response or fault content on the AsyncResponse object
            // so that it can be collected inside the Executor thread and processed.
            if (error != null) {
                response.onError(error, msgCtx, handlerCL);
            } else {
                response.onComplete(msgCtx, handlerCL);
            }

            // Now that the content is available, call the JAX-WS AsyncHandler class
            // to deliver the response to the user.
            if (debug) {
                log.debug("Calling JAX-WS AsyncHandler.handleResponse() with response object: " + CallbackFuture.displayHandle(response));
            }
            handler.handleResponse(response);
            if (debug) {
                log.debug("Returned from handleResponse() invocation...");
            }
        } catch (Throwable t) {
            if (debug) {
                log.debug("An error occurred while invoking the callback object.");
                log.debug("Error: " + t.toString());
                t.printStackTrace();
            }
        } finally {
            synchronized(this) {
                // Restore the old classloader on this thread.
                if (oldCL != null) {
                    final ClassLoader t = oldCL;
                    AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            Thread.currentThread().setContextClassLoader(t);
                            return null;
                        }
                    });
                    if (debug) {
                        log.debug("Restored thread context classloader: " + oldCL.toString());
                    }
                }

                done = true;
                this.notifyAll();
            }
        }

        return null;
    }
}