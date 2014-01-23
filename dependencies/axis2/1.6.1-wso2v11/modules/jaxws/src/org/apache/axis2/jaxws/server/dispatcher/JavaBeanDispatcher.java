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

package org.apache.axis2.jaxws.server.dispatcher;


import org.apache.axis2.AxisFault;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.binding.BindingUtils;
import org.apache.axis2.jaxws.context.utils.ContextUtils;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.marshaller.factory.MethodMarshallerFactory;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.EndpointCallback;
import org.apache.axis2.jaxws.server.EndpointInvocationContext;
import org.apache.axis2.jaxws.server.InvocationHelper;
import org.apache.axis2.jaxws.server.ServerConstants;
import org.apache.axis2.jaxws.server.endpoint.Utils;
import org.apache.axis2.jaxws.spi.Constants;
import org.apache.axis2.jaxws.utility.ExecutorFactory;
import org.apache.axis2.jaxws.utility.SingleThreadedExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

/**
 * The JavaBeanDispatcher is used to manage creating an instance of a JAX-WS service implementation
 * bean and dispatching the inbound request to that instance.
 */
public class JavaBeanDispatcher extends JavaDispatcher {

    private static final Log log = LogFactory.getLog(JavaBeanDispatcher.class);

    private EndpointDescription endpointDesc = null;

    public JavaBeanDispatcher(Class implClass, Object serviceInstance) {
        super(implClass, serviceInstance);
    }

    /*
    * (non-Javadoc)
    * @see org.apache.axis2.jaxws.server.EndpointDispatcher#invoke(org.apache.axis2.jaxws.core.MessageContext)
    */
    public MessageContext invoke(MessageContext mc) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Invoking service endpoint: " + serviceImplClass.getName());
            log.debug("Invocation pattern: two way, sync");
        }

        initialize(mc);
        
        OperationDescription operationDesc = Utils.getOperationDescription(mc);
        
        Object[] methodInputParams = createRequestParameters(mc);
        
        Method target = getJavaMethod(mc, serviceImplClass);
        if (log.isDebugEnabled()) {
            // At this point, the OpDesc includes everything we know, including the actual method
            // on the service impl we will delegate to; it was set by getJavaMethod(...) above.
            log.debug("JavaBeanDispatcher about to invoke using OperationDesc: " +
                    operationDesc.toString());
        }

        // We have the method that is going to be invoked and the parameter data to invoke it 
        // with, so just invoke the operation.
        boolean faultThrown = false;
        Throwable fault = null;
        Object output = null;
        try {
            output = invokeTargetOperation(target, methodInputParams);
        } 
        catch (Throwable e) {
            faultThrown = true;
            fault = e;
            if (log.isDebugEnabled()) {
              log.debug("Caught exception from 'invokeTargetOperation': " + fault.toString());
            }
        }

        MessageContext response = null;
        if (operationDesc.isOneWay()) {
            // If the operation is one-way, then we can just return null because
            // we cannot create a MessageContext for one-way responses.
            return null;
        } else if (faultThrown) {
            if (log.isDebugEnabled()) {
              log.debug("Processing fault response: " + fault.toString());
            }

            response = createFaultResponse(mc, mc.getMessage().getProtocol(), fault);
            setExceptionProperties(response, target, fault);
        } else {
            response = createResponse(mc, mc.getMessage().getProtocol(), methodInputParams, output);
        }

        
        if (log.isDebugEnabled()) {
          log.debug("Returning from JavaBeanDispatcher.invoke()...");
        }
        return response;
    }

    public void invokeOneWay(MessageContext request) {
        if (log.isDebugEnabled()) {
            log.debug("Invoking service endpoint: " + serviceImplClass.getName());
            log.debug("Invocation pattern: one way");
        }

        initialize(request);
        
        OperationDescription operationDesc = Utils.getOperationDescription(request);
        
        Object[] methodInputParams = createRequestParameters(request);
        
        Method target = getJavaMethod(request, serviceImplClass);
        if (log.isDebugEnabled()) {
            // At this point, the OpDesc includes everything we know, including the actual method
            // on the service impl we will delegate to; it was set by getJavaMethod(...) above.
            log.debug("JavaBeanDispatcher about to invoke using OperationDesc: "
                    + operationDesc.toString());
        }
        
        EndpointInvocationContext eic = (EndpointInvocationContext) request.getInvocationContext();
        ClassLoader cl = getContextClassLoader();
        
        AsyncInvocationWorker worker = new AsyncInvocationWorker(target, 
                                                                 methodInputParams, 
                                                                 cl, eic);
        FutureTask task = new FutureTask<AsyncInvocationWorker>(worker);
        
        ExecutorFactory ef = (ExecutorFactory) FactoryRegistry.getFactory(ExecutorFactory.class);
        Executor executor = ef.getExecutorInstance(ExecutorFactory.SERVER_EXECUTOR);
        
        // If the property has been set to disable thread switching, then we can 
        // do so by using a SingleThreadedExecutor instance to continue processing
        // work on the existing thread.
        Boolean disable = (Boolean) 
            request.getProperty(ServerConstants.SERVER_DISABLE_THREAD_SWITCH);
        if (disable != null && disable.booleanValue()) {
            if (log.isDebugEnabled()) {
                log.debug("Server side thread switch disabled.  " +
                                "Setting Executor to the SingleThreadedExecutor.");
            }
            executor = new SingleThreadedExecutor();
        }

        executor.execute(task);     
        return;
    }

    public void invokeAsync(MessageContext request, EndpointCallback callback) {
        if (log.isDebugEnabled()) {
            log.debug("Invoking service endpoint: " + serviceImplClass.getName());
            log.debug("Invocation pattern: two way, async");
        }

        initialize(request);
        
        OperationDescription operationDesc = Utils.getOperationDescription(request);
        
        Object[] methodInputParams = createRequestParameters(request);
        
        Method target = getJavaMethod(request, serviceImplClass);
        if (log.isDebugEnabled()) {
            // At this point, the OpDesc includes everything we know, including the actual method
            // on the service impl we will delegate to; it was set by getJavaMethod(...) above.
            log.debug("JavaBeanDispatcher about to invoke using OperationDesc: "
                    + operationDesc.toString());
        }
        
        EndpointInvocationContext eic = (EndpointInvocationContext) request.getInvocationContext();
        ClassLoader cl = getContextClassLoader();
        
        AsyncInvocationWorker worker = new AsyncInvocationWorker(target, methodInputParams, cl, eic);
        FutureTask task = new FutureTask<AsyncInvocationWorker>(worker);
        
        ExecutorFactory ef = (ExecutorFactory) FactoryRegistry.getFactory(ExecutorFactory.class);
        Executor executor = ef.getExecutorInstance(ExecutorFactory.SERVER_EXECUTOR);
        // If the property has been set to disable thread switching, then we can 
        // do so by using a SingleThreadedExecutor instance to continue processing
        // work on the existing thread.
        Boolean disable = (Boolean) request.getProperty(ServerConstants.SERVER_DISABLE_THREAD_SWITCH);
        if (disable != null && disable.booleanValue()) {
            if (log.isDebugEnabled()) {
                log.debug("Server side thread switch disabled.  Setting Executor to the SingleThreadedExecutor.");
            }
            executor = new SingleThreadedExecutor();
        }
        executor.execute(task);
        
        return;
    }
    
    protected void initialize(MessageContext mc) {
        mc.setOperationName(mc.getAxisMessageContext().getAxisOperation().getName());
        mc.setOperationDescription(Utils.getOperationDescription(mc));
        endpointDesc = mc.getEndpointDescription();
        if (endpointDesc.isMTOMEnabled()) {
            mc.getMessage().setMTOMEnabled(true);
        }
      
        
        //Set SOAP Operation Related properties in SOAPMessageContext.
        ContextUtils.addWSDLProperties(mc);
    }

    private MethodMarshaller getMethodMarshaller(Protocol protocol,
                                                 OperationDescription operationDesc,
                                                 MessageContext mc) {
        javax.jws.soap.SOAPBinding.Style styleOnSEI =
                endpointDesc.getEndpointInterfaceDescription().getSoapBindingStyle();
        javax.jws.soap.SOAPBinding.Style styleOnMethod = operationDesc.getSoapBindingStyle();
        if (styleOnMethod != null && styleOnSEI != styleOnMethod) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("proxyErr2"));
        }
        
        // check for a stored classloader to be used as the cache key
        ClassLoader cl = null;
        if(mc != null) {
            cl = (ClassLoader) mc.getProperty(Constants.CACHE_CLASSLOADER);
        }
        return MethodMarshallerFactory.getMarshaller(operationDesc, false, cl);
    }

    protected Method getJavaMethod(MessageContext mc, Class serviceImplClass) {

        OperationDescription opDesc = mc.getOperationDescription();
        if (opDesc == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("proxyErr3"));
        }

        Method returnMethod = opDesc.getMethodFromServiceImpl(serviceImplClass);
        if (returnMethod == null) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("JavaBeanDispatcherErr1"));
        }

        return returnMethod;
    }
    
    private Object[] createRequestParameters(MessageContext request) {
        // Get the appropriate MethodMarshaller for the WSDL type.  This will reflect
        // the "style" and "use" of the WSDL.
        Protocol requestProtocol = request.getMessage().getProtocol();
        MethodMarshaller methodMarshaller =
                getMethodMarshaller(requestProtocol, request.getOperationDescription(),
                                    request);
        
        // The MethodMarshaller will return the input parameters that are needed to 
        // invoke the target method.
        Object[] methodInputParams =
                methodMarshaller.demarshalRequest(request.getMessage(), request.getOperationDescription());
        
        if (log.isDebugEnabled()) {
            log.debug("Unmarshalled parameters for request");
            if (methodInputParams != null) {
                log.debug(methodInputParams.length + " parameters were found.");    
            }
        }
        
        return methodInputParams;
    }
    
    public MessageContext createResponse(MessageContext request, Object[] input, Object output) {
        return createResponse(request, request.getMessage().getProtocol(), input, output);
    }
    
    public MessageContext createResponse(MessageContext request, Protocol p, Object[] params, Object output) {
        OperationDescription operationDesc = request.getOperationDescription();
        Method method = operationDesc.getMethodFromServiceImpl(serviceImplClass);
        
        // Create the appropriate response message, using the protocol from the
        // request message.
        MethodMarshaller marshaller = getMethodMarshaller(p, request.getOperationDescription(),
                                                          request);
        Message m = null;
        if (method.getReturnType().getName().equals("void")) {
            m = marshaller.marshalResponse(null, params, operationDesc, p); 
        } else {
            m = marshaller.marshalResponse(output, params, operationDesc, p);
        }
        
        // We'll need a MessageContext configured based on the response.
        MessageContext response = MessageContextUtils.createResponseMessageContext(request);
        response.setMessage(m);
        
        // Enable MTOM for the response if necessary.
        // (MTOM is not enabled it this operation has SWA parameters
           
        EndpointDescription epDesc = request.getEndpointDescription();
        String bindingType = epDesc.getBindingType();
        boolean isMTOMBinding = epDesc.isMTOMEnabled();
        boolean isDoingSWA = m.isDoingSWA();
        if (log.isDebugEnabled()) {
            log.debug("EndpointDescription = " + epDesc.toString());
            log.debug("BindingType = " + bindingType);
            log.debug("isMTOMBinding = " + isMTOMBinding);
            log.debug("isDoingSWA = " + isDoingSWA);
        }
        if (!m.isDoingSWA() && isMTOMBinding) {
            if (log.isDebugEnabled()) {
                log.debug("MTOM enabled for the response message.");
            }
            m.setMTOMEnabled(true);
        }
        
        return response;
    }
    
    public MessageContext createFaultResponse(MessageContext request, Throwable t) {
        return createFaultResponse(request, request.getMessage().getProtocol(), t);
    }
    
    public MessageContext createFaultResponse(MessageContext request, Protocol p, Throwable t) {
        if (log.isDebugEnabled()) {
          log.debug("Entered JavaBeanDispatcher.createFaultResponse()...");
        }

        // call the InvocationListener instances before marshalling
        // the fault into a message
        // call the InvocationListener instances before marshalling
        // the fault into a message
        Throwable faultMessage = InvocationHelper.determineMappedException(t, request);
        if(faultMessage != null) {
            t = faultMessage;
        }
        
        MethodMarshaller marshaller = getMethodMarshaller(p, request.getOperationDescription(),
                                                          request);
        
        Message m = marshaller.marshalFaultResponse(t, request.getOperationDescription(), p);
        
        MessageContext response = MessageContextUtils.createFaultMessageContext(request);
        response.setMessage(m);

        AxisFault axisFault = new AxisFault("The endpoint returned a fault when invoking the target operation.",
                                            response.getAxisMessageContext(),
                                            t);
        
        response.setCausedByException(axisFault);
        
        setFaultResponseAction(t, request, response);

        if (log.isDebugEnabled()) {
          log.debug("Leaving JavaBeanDispatcher.createFaultResponse()...");
        }
        
        return response;
    }
    
    /**
     * @return ClassLoader
     */
    private static ClassLoader getContextClassLoader() {
        // NOTE: This method must remain private because it uses AccessController
        ClassLoader cl = null;
        try {
            cl = (ClassLoader) org.apache.axis2.java.security.AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws ClassNotFoundException {
                    return Thread.currentThread().getContextClassLoader();
                }
            });
        } catch (PrivilegedActionException e) {
            // The privileged method will throw a PriviledgedActionException which
            // contains the actual exception.
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            Exception wrappedE = e.getException();
            if (wrappedE instanceof RuntimeException) {
                throw (RuntimeException) wrappedE;
            } else {
                throw new RuntimeException(wrappedE);
            }
        }
        
        return cl;
    }
    
}
