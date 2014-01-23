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

package org.apache.axis2.jaxws.client.proxy;

import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.client.async.AsyncResponse;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.InvocationContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.controller.InvocationController;
import org.apache.axis2.jaxws.core.controller.InvocationControllerFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.validator.EndpointDescriptionValidator;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.factory.MethodMarshallerFactory;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescriptionFactory;
import org.apache.axis2.jaxws.spi.Binding;
import org.apache.axis2.jaxws.spi.Constants;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.spi.migrator.ApplicationContextMigratorUtil;
import org.apache.axis2.jaxws.util.WSDLExtensionUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.SOAPBinding;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * ProxyHandler is the java.lang.reflect.InvocationHandler implementation. When a JAX-WS client
 * calls the method on a proxy object, created by calling the ServiceDelegate.getPort(...) method,
 * the inovke method on the ProxyHandler is called.
 * <p/>
 * ProxyHandler uses EndpointInterfaceDescriptor and finds out if 1) The client call is Document
 * Literal or Rpc Literal 2) The WSDL is wrapped or unWrapped.
 * <p/>
 * ProxyHandler then reads OperationDescription using Method name called by Client From
 * OperationDescription it does the following 1) if the wsdl isWrapped() reads RequestWrapper Class
 * and responseWrapperClass 2) then reads the webParams for the Operation.
 * <p/>
 * isWrapped() = true  and DocLiteral then ProxyHandler then uses WrapperTool to create Request that
 * is a Wrapped JAXBObject. Creates JAXBBlock using JAXBBlockFactory Creates MessageContext->Message
 * and sets JAXBBlock to xmlPart as RequestMsgCtx in InvocationContext. Makes call to
 * InvocationController. Reads ResponseMsgCtx ->MessageCtx->Message->XMLPart. Converts that to
 * JAXBlock using JAXBBlockFactory and returns the BO from this JAXBBlock.
 * <p/>
 * isWrapped() != true and DocLiteral then ProxyHandler creates the JAXBBlock for the input request
 * creates a MessageContext that is then used by IbvocationController to invoke. Response is read
 * and return object is derived using @Webresult annotation. A JAXBBlock is created from the
 * Response and the BO from JAXBBlock is returned.
 */

public class JAXWSProxyHandler extends BindingProvider implements
        InvocationHandler {
    private static Log log = LogFactory.getLog(JAXWSProxyHandler.class);

    private Class seiClazz = null;
    private Method method = null;
    
    //Reference to ServiceDelegate instance that was used to create the Proxy
    protected ServiceDescription serviceDesc = null;

    protected InvocationController controller;
    
    public JAXWSProxyHandler(ServiceDelegate delegate,
                             Class seiClazz,
                             EndpointDescription epDesc,
                             WebServiceFeature... features) {
        this(delegate, seiClazz, epDesc, null, null, features);
    }

    public JAXWSProxyHandler(ServiceDelegate delegate,
            Class seiClazz,
            EndpointDescription epDesc,
            EndpointReference epr,
            String addressingNamespace,
            WebServiceFeature... features) {
        super(delegate, epDesc, epr, addressingNamespace, features);
        
        this.seiClazz = seiClazz;
        this.serviceDesc = delegate.getServiceDescription();
    }

    /* (non-Javadoc)
    * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
    *
    * Invokes the method that was called on the java.lang.reflect.Proxy instance.
    */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean debug = log.isDebugEnabled();
        if (debug) {
            log.debug("Attemping to invoke Method: " + method.getName());
        }

        this.method = method;

        if (!isValidMethodCall(method)) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("proxyErr1", method.getName(), seiClazz.getName()));
        }

        if (!isPublic(method)) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("proxyPrivateMethod", method.getName()));
        }

        if (isBindingProviderInvoked(method)) {
            // Since the JAX-WS proxy instance must also implement the javax.xml.ws.BindingProvider
            // interface, this object must handle those invocations as well.  In that case, we'll
            // delegate those calls to the BindingProvider object.
            if (debug) {
                log.debug(
                        "Invoking a public method on the javax.xml.ws.BindingProvider interface.");
            }
            try {
                return method.invoke(this, args);
            }
            catch (Throwable e) {
                if (debug) {
                    log.debug("An error occured while invoking the method: " + e.getMessage());
                }
                throw ExceptionFactory.makeWebServiceException(e);
            }
        } else {
            OperationDescription operationDesc =
                    endpointDesc.getEndpointInterfaceDescription().getOperation(method);
            if (isMethodExcluded(operationDesc)) {
                throw ExceptionFactory.makeWebServiceException(
                        Messages.getMessage("proxyExcludedMethod", method.getName()));
            }
            return invokeSEIMethod(method, args);
        }
    }

    /*
     * Note to developer: When making a change or fix to this method, please consider
     * all 5 Proxy/Dispatch "invoke" methods now available in JAX-WS. For Dispatch, 
     * these are:
     * 1) Synchronous invoke()
     * 2) invokeOneWay()
     * 3) invokeAsynch (Future)
     * 4) invokeAsynch (Callback)
     * 
     * For Proxy:
     * 5) invokeSEIMethod() 
     *
     * Performs the invocation of the method defined on the Service Endpoint
     * Interface.
     */
    private Object invokeSEIMethod(Method method, Object[] args) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to invoke SEI Method " + method.getName());
        }

        OperationDescription operationDesc =
                endpointDesc.getEndpointInterfaceDescription().getOperation(method);

        // Create and configure the request MessageContext
        InvocationContext requestIC = InvocationContextFactory.createInvocationContext(null);
        MessageContext request = createRequest(method, args);
        request.getAxisMessageContext().setProperty(BINDING_PROVIDER, this);
        request.setEndpointDescription(getEndpointDescription());
        request.setOperationDescription(operationDesc);

        // Enable MTOM on the Message if the property was set on the SOAPBinding.
        Binding bnd = (Binding) getBinding();
        if (bnd != null && bnd instanceof SOAPBinding) {
            if (((SOAPBinding)bnd).isMTOMEnabled()) {
                Message requestMsg = request.getMessage();
                requestMsg.setMTOMEnabled(true);
                int threshold = ((org.apache.axis2.jaxws.binding.SOAPBinding)bnd).getMTOMThreshold();
                request.setProperty(org.apache.axis2.Constants.Configuration.MTOM_THRESHOLD, 
                        new Integer(threshold));
            }
            if (((org.apache.axis2.jaxws.binding.SOAPBinding)bnd).isRespectBindingEnabled()) {
                //lets invoke Utility to configure RespectBinding.
                EndpointDescription endpointDescription = getEndpointDescription();
                endpointDescription.setRespectBinding(true);
                WSDLExtensionUtils.processExtensions(endpointDescription);
                //We have build up set of extensions from wsdl
                //let go ahead and validate these extensions now.
                EndpointDescriptionValidator endpointValidator = new EndpointDescriptionValidator(endpointDescription);
                 
                boolean isEndpointValid = endpointValidator.validate(true);
                //throw Exception if extensions are not understood by Engine.
                if (!isEndpointValid) {
                    String msg = Messages.getMessage("endpointDescriptionValidationErrors",
                                                     endpointValidator.toString());
                    throw ExceptionFactory.makeWebServiceException(msg);
                }
            }
        }
        
        /*
         * TODO: review: make sure the handlers are set on the InvocationContext
         * This implementation of the JAXWS runtime does not use Endpoint, which
         * would normally be the place to initialize and store the handler list.
         * In lieu of that, we will have to intialize and store them on the 
         * InvocationContext.  also see the InvocationContextFactory.  On the client
         * side, the binding is not yet set when we call into that factory, so the
         * handler list doesn't get set on the InvocationContext object there.  Thus
         * we gotta do it here.
         */
        
        // be sure to use whatever handlerresolver is registered on the Service
        requestIC.setHandlers(bnd.getHandlerChain());

        requestIC.setRequestMessageContext(request);
        requestIC.setServiceClient(serviceDelegate.getServiceClient(endpointDesc.getPortQName()));
        
        /*
         * if SESSION_MAINTAIN_PROPERTY is true, and the client app has explicitly set a HEADER_COOKIE on the request context, assume the client
         * app is expecting the HEADER_COOKIE to be the session id.  If we were establishing a new session, no cookie would be sent, and the 
         * server would reply with a "Set-Cookie" header, which is copied as a "Cookie"-keyed property to the service context during response.
         * In this case, if we succeed in using an existing server session, no "Set-Cookie" header will be returned, and therefore no
         * "Cookie"-keyed property would be set on the service context.  So, let's copy our request context HEADER_COOKIE key to the service
         * context now to prevent the "no cookie" exception in BindingProvider.setupSessionContext.  It is possible the server does not support
         * sessions, in which case no error occurs, but the client app would assume it is participating in a session.
         */
        if ((requestContext.containsKey(BindingProvider.SESSION_MAINTAIN_PROPERTY)) && ((Boolean)requestContext.get(BindingProvider.SESSION_MAINTAIN_PROPERTY))) {
            if ((requestContext.containsKey(HTTPConstants.HEADER_COOKIE)) && (requestContext.get(HTTPConstants.HEADER_COOKIE) != null)) {
                if (requestIC.getServiceClient().getServiceContext().getProperty(HTTPConstants.HEADER_COOKIE) == null) {
                    requestIC.getServiceClient().getServiceContext().setProperty(HTTPConstants.HEADER_COOKIE, requestContext.get(HTTPConstants.HEADER_COOKIE));
                    if (log.isDebugEnabled()) {
                        log.debug("Client-app defined Cookie property (assume to be session cookie) on request context copied to service context." +
                                "  Caution:  server may or may not support sessions, but client app will not be informed when not supported.");
                    }
                }
            }
        }
        
        // Migrate the properties from the client request context bag to
        // the request MessageContext.
        ApplicationContextMigratorUtil.performMigrationToMessageContext(
                Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID, 
                getRequestContext(), request);

        // Note that configuring the MessageContext for addressing based on the metadata and for any 
        // WebService Features needs to be done after the application context migration since it will move properties
        // from the JAXWS RequestContext onto the Axis2 Message context, overwritting any that are already set.
        configureAddressing(request, this);
        // Perform the WebServiceFeature configuration requested by the user.
        bnd.configure(request, this);

        // We'll need an InvocationController instance to send the request.
        InvocationControllerFactory icf = (InvocationControllerFactory) FactoryRegistry.getFactory(InvocationControllerFactory.class);
        controller = icf.getInvocationController();
        
        if (controller == null) {
            throw new WebServiceException(Messages.getMessage("missingInvocationController"));
        }
        
        // Check if the call is OneWay, Async or Sync
        if (operationDesc.isOneWay()) {
            if (log.isDebugEnabled()) {
                log.debug("OneWay Call");
            }
            controller.invokeOneWay(requestIC);

            // Check to see if we need to maintain session state
            checkMaintainSessionState(request, requestIC);
        }

        if (method.getReturnType() == Future.class) {
            if (log.isDebugEnabled()) {
                log.debug("Async Callback");
            }

            //Get AsyncHandler from Objects and sent that to InvokeAsync
            AsyncHandler asyncHandler = null;
            for (Object obj : args) {
                if (obj != null && AsyncHandler.class.isAssignableFrom(obj.getClass())) {
                    asyncHandler = (AsyncHandler)obj;
                    break;
                }
            }

            // Don't allow the invocation to continue if the invocation requires a callback
            // object, but none was supplied.
            if (asyncHandler == null) {
                throw ExceptionFactory
                        .makeWebServiceException(Messages.getMessage("proxyNullCallback"));
            }
            AsyncResponse listener = createProxyListener(args, operationDesc);
            requestIC.setAsyncResponseListener(listener);

            if ((serviceDelegate.getExecutor() != null) &&
                    (serviceDelegate.getExecutor() instanceof ExecutorService)) {
                ExecutorService es = (ExecutorService)serviceDelegate.getExecutor();
                if (es.isShutdown()) {
                    // the executor service is shutdown and won't accept new tasks
                    // so return an error back to the client
                    throw ExceptionFactory
                            .makeWebServiceException(Messages.getMessage("ExecutorShutdown"));
                }
            }

            requestIC.setExecutor(serviceDelegate.getExecutor());

            Future<?> future = controller.invokeAsync(requestIC, asyncHandler);

            //Check to see if we need to maintain session state
            checkMaintainSessionState(request, requestIC);

            if (log.isDebugEnabled()) {
                log.debug("Exiting the method invokeSEIMethod() - Async Callback ");
            }

            return future;
        }

        if (method.getReturnType() == Response.class) {
            if (log.isDebugEnabled()) {
                log.debug("Async Polling");
            }
            AsyncResponse listener = createProxyListener(args, operationDesc);
            requestIC.setAsyncResponseListener(listener);
            requestIC.setExecutor(serviceDelegate.getExecutor());

            Response response = controller.invokeAsync(requestIC);

            //Check to see if we need to maintain session state
            checkMaintainSessionState(request, requestIC);
            
            if (log.isDebugEnabled()) {
                log.debug("Exiting the method invokeSEIMethod() - Async Polling ");
            }

            return response;
        }

        if (!operationDesc.isOneWay()) {
            InvocationContext responseIC = controller.invoke(requestIC);

            //Check to see if we need to maintain session state
            checkMaintainSessionState(request, requestIC);

            MessageContext responseContext = responseIC.getResponseMessageContext();
            
            // Migrate the properties from the response MessageContext back
            // to the client response context bag.
            ApplicationContextMigratorUtil.performMigrationFromMessageContext(
                    Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID, 
                    getResponseContext(), responseContext);
            
            Object responseObj = createResponse(method, args, responseContext, operationDesc);

            if (log.isDebugEnabled()) {
                log.debug("Exiting the method invokeSEIMethod() - Sync");
            }
                        
            return responseObj;
        }

        if (log.isDebugEnabled()) {
            log.debug("Exiting the method invokeSEIMethod() - One Way ");
        }

        return null;
    }
    
    /**
     * For a SOAP Binding, configure the Addressing-related properties on the message context based on the
     * addressing configuration specified via metadata (such as a deployment descriptor).  Note that if
     * addressing was not explicitly configured, then the Addressing-related propertes will not be set on the
     * message context.
     * <p>
     * This code is similar to the client-side Addressing configurator what the properties on the message context
     * are set to.
     * @see org.apache.axis2.jaxws.client.config.AddressingConfigurator
     * @param messageContext The message context on which Addressing properties will be set
     * @param bindingProvider Instance of the binding provider for which property values will be determined
     */
    private void configureAddressing(MessageContext messageContext, BindingProvider bindingProvider) {
        
        Binding binding = (Binding) bindingProvider.getBinding();
        if (binding != null && binding instanceof SOAPBinding) {
            SOAPBinding soapBinding = (SOAPBinding) binding;
            org.apache.axis2.jaxws.binding.SOAPBinding implBinding = (org.apache.axis2.jaxws.binding.SOAPBinding) soapBinding;
            if (implBinding.isAddressingConfigured()) {
                String addressingNamespace = implBinding.getAddressingNamespace();
                Boolean disableAddressing = new Boolean(true);
                String addressingRequired = AddressingConstants.ADDRESSING_UNSPECIFIED;
                
                if (implBinding.isAddressingEnabled()) {
                    addressingNamespace = Final.WSA_NAMESPACE;
                    disableAddressing = new Boolean(false);
                    if (implBinding.isAddressingRequired()) {
                        addressingRequired = AddressingConstants.ADDRESSING_REQUIRED;
                    }
                }
                
                messageContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION, addressingNamespace);                        
                messageContext.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, disableAddressing);
                messageContext.setProperty(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER, addressingRequired);
            
                // Get the responses value and map to the value the addressing handler expects
                messageContext.setProperty(AddressingConstants.WSAM_INVOCATION_PATTERN_PARAMETER_NAME, 
                        org.apache.axis2.jaxws.server.config.AddressingConfigurator.mapResponseAttributeToAddressing(implBinding.getAddressingResponses()));
            }
        }
    }

    private AsyncResponse createProxyListener(Object[] args, OperationDescription operationDesc) {
        ProxyAsyncListener listener = new ProxyAsyncListener(operationDesc);
        listener.setHandler(this);
        listener.setInputArgs(args);
        return listener;
    }

    protected boolean isAsync() {
        String methodName = method.getName();
        Class returnType = method.getReturnType();
        return methodName.endsWith("Async") && (returnType.isAssignableFrom(Response.class) ||
                returnType.isAssignableFrom(Future.class));
    }

    /**
     * Creates a request MessageContext for the method call. This request context will be used by
     * InvocationController to route the method call to axis engine.
     *
     * @param method - The method invoked on the proxy object.
     * @param args   - The parameter list
     * @return A MessageContext that can be used for the invocation
     */
    protected MessageContext createRequest(Method method, Object[] args) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("Creating a new Message using the request parameters.");
        }

        OperationDescription operationDesc =
                endpointDesc.getEndpointInterfaceDescription().getOperation(method);

        MessageContext request = new MessageContext();
        
        // Select a Classloader to use for marshaling
        ClassLoader cl = chooseClassLoader(seiClazz, serviceDesc);
        
        // Make sure the same classloader is used on the response
        request.setProperty(Constants.CACHE_CLASSLOADER, cl);
        
        Message message = MethodMarshallerFactory.getMarshaller(operationDesc, true, null)
                .marshalRequest(args, operationDesc, this.getRequestContext());

        if (log.isDebugEnabled()) {
            log.debug("Request Message created successfully.");
        }

        request.setMessage(message);

        if (log.isDebugEnabled()) {
            log.debug("Request MessageContext created successfully.");
        }

        return request;
    }

    /**
     * Creates a response MessageContext for the method call. This response context will be used to
     * create response result to the client call.
     *
     * @param method          - The method invoked on the proxy object.
     * @param args            - The parameter list.
     * @param responseContext - The MessageContext to be used for the response.
     * @param operationDesc   - The OperationDescription that for the invoked method.
     * @return
     */
    protected Object createResponse(Method method, Object[] args, MessageContext responseContext,
                                    OperationDescription operationDesc) throws Throwable {
        Message responseMsg = responseContext.getMessage();
        try {

            if (log.isDebugEnabled()) {
                log.debug("Processing the response Message to create the return value(s).");
            }

            // Find out if there was a fault on the response and create the appropriate 
            // exception type.
            if (hasFaultResponse(responseContext)) {
                Throwable t = getFaultResponse(responseContext, operationDesc);
                throw t;
            }
            
            // Get the classloader that was used for the request processing
            ClassLoader cl = (ClassLoader) responseContext.getProperty(Constants.CACHE_CLASSLOADER);
            if (cl == null) {
                InvocationContext ic = responseContext.getInvocationContext();
                if (ic != null) {
                    MessageContext requestMC = ic.getRequestMessageContext();
                    if (requestMC != null) {
                        cl = (ClassLoader) responseContext.getProperty(Constants.CACHE_CLASSLOADER);
                        if (cl != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Obtained ClassLoader for the request context: " + cl);
                            }
                        }
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Obtained ClassLoader for the response context: " + cl);
                }
            }
            Object object =
                    MethodMarshallerFactory.getMarshaller(operationDesc, true, cl)
                                       .demarshalResponse(responseMsg, args, operationDesc);
            if (log.isDebugEnabled()) {
                log.debug("The response was processed and the return value created successfully.");
            }
            return object;
        } finally {
            responseMsg.close();
            // Free incoming stream
            try {
                responseContext.freeInputStream();
            }
            catch (Throwable t) {
                throw ExceptionFactory.makeWebServiceException(t);
            }
        }
    }

    protected static Throwable getFaultResponse(MessageContext msgCtx,
                                                OperationDescription opDesc) {
        Message msg = msgCtx.getMessage();
        //Operation Description for Async method does not store the fault description as Asyc operation 
        //will never have throws clause in the method signature.
        //we will fetch the OperationDescription of the sync method and this should give us the
        //correct fault description so we can throw the right user defined exception.

        try {
            if (opDesc.isJAXWSAsyncClientMethod()) {
                opDesc = opDesc.getSyncOperation();
            }
            if (msg != null && msg.isFault()) {
                ClassLoader cl = (ClassLoader) msgCtx.getProperty(Constants.CACHE_CLASSLOADER);
                Object object = MethodMarshallerFactory.getMarshaller(opDesc, true, cl)
                .demarshalFaultResponse(msg, opDesc);
                if (log.isDebugEnabled() && object != null) {
                    log.debug("A fault was found and processed.");
                    log.debug("Throwing a fault of type: " + object.getClass().getName() +
                    " back to the clent.");
                }

                return (Throwable)object;
            } else if (msgCtx.getLocalException() != null) {
                // use the factory, it'll throw the right thing:
                return ExceptionFactory.makeWebServiceException(msgCtx.getLocalException());
            }
        } finally {
            try {
                msgCtx.freeInputStream();
            }
            catch (Throwable t) {
                throw ExceptionFactory.makeWebServiceException(t);
            }
        }

        return null;
    }

    protected static boolean hasFaultResponse(MessageContext mc) {
        if (mc.getMessage() != null && mc.getMessage().isFault())
            return true;
        else if (mc.getLocalException() != null)
            return true;
        else
            return false;
    }

    private boolean isBindingProviderInvoked(Method method) {
        Class methodsClass = method.getDeclaringClass();
        return (seiClazz == methodsClass) ? false : true;
    }

    private boolean isValidMethodCall(Method method) {
        Class clazz = method.getDeclaringClass();
        if (clazz.isAssignableFrom(seiClazz) ||
                clazz.isAssignableFrom(org.apache.axis2.jaxws.spi.BindingProvider.class) ||
                clazz.isAssignableFrom(javax.xml.ws.BindingProvider.class)) {
            return true;
        }
        return false;
    }

    private boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    private boolean isMethodExcluded(OperationDescription operationDesc) {
        return operationDesc.isExcluded();
    }

    public Class getSeiClazz() {
        return seiClazz;
    }

    public void setSeiClazz(Class seiClazz) {
        this.seiClazz = seiClazz;
    }
    
    /**
     * Choose a classloader most likely to marshal the message
     * successfully
     * @param cls
     * @return ClassLoader
     */
    private static ClassLoader chooseClassLoader(Class cls, ServiceDescription serviceDesc) {
        if (log.isDebugEnabled()) {
            log.debug("Choose Classloader for " + cls);
        }
        ClassLoader cl = null;
        ClassLoader contextCL = getContextClassLoader();
        ClassLoader classCL = getClassLoader(cls);
        if (log.isDebugEnabled()) {
            log.debug("Context ClassLoader is " + contextCL);
            log.debug("Class ClassLoader is " + classCL);
        }
        
        if (classCL == null ||
            contextCL == classCL) {
            // Normal case: Use the context ClassLoader
            cl = contextCL;
        } else {
            // Choose the better of the JAXBContexts
            MarshalServiceRuntimeDescription marshalDesc =
                 MarshalServiceRuntimeDescriptionFactory.get(serviceDesc);
            
            // Get the JAXBContext for the context classloader 
            Holder<JAXBUtils.CONSTRUCTION_TYPE> holder_contextCL = new Holder<JAXBUtils.CONSTRUCTION_TYPE>();   
            JAXBContext jbc_contextCL = null;
            try {
                jbc_contextCL = JAXBUtils.getJAXBContext(marshalDesc.getPackages(), 
                        holder_contextCL,
                        marshalDesc.getPackagesKey(), 
                        contextCL, 
                        null);
            } catch (Throwable t) {
                if (log.isDebugEnabled()) {
                    log.debug("Error occured..Processing continues " + t);
                }
            }
            
            // Get the JAXBContext using the class's ClassLoader
            Holder<JAXBUtils.CONSTRUCTION_TYPE> holder_classCL = new Holder<JAXBUtils.CONSTRUCTION_TYPE>(); 
            JAXBContext jbc_classCL = null;
            try {
                jbc_classCL = JAXBUtils.getJAXBContext(marshalDesc.getPackages(), 
                        holder_classCL,
                        marshalDesc.getPackagesKey(), 
                        contextCL, 
                        null);
            } catch (Throwable t) {
                if (log.isDebugEnabled()) {
                    log.debug("Error occured..Processing continues " + t);
                }
            }
            
            // Heuristic to choose the better classloader to marshal the
            // data.  Slight priority given to the classloader that loaded
            // the sei class.
            if (jbc_classCL == null) {
                // A JAXBContext could not be loaded for the class's classlaoder,
                // choose the context ClassLoader
                if (log.isDebugEnabled()) {
                    log.debug("Could not load JAXBContext for Class ClassLoader");
                }
                cl = contextCL;
            } else if (jbc_contextCL == null) {
                // A JAXBContext could not be loaded for the context's classloader,
                // choose the class ClassLoader
                if (log.isDebugEnabled()) {
                    log.debug("Could not load JAXBContext for Context ClassLoader");
                }
                cl = classCL;
            } else if (holder_contextCL.value == JAXBUtils.CONSTRUCTION_TYPE.BY_CONTEXT_PATH &&
                    holder_classCL.value == JAXBUtils.CONSTRUCTION_TYPE.BY_CONTEXT_PATH) {
                // Both were successfully built with the context path.
                // Choose the one associated with the proxy class
                if (log.isDebugEnabled()) {
                    log.debug("Loaded both JAXBContexts with BY_CONTEXT_PATH.  Choose Class ClassLoader");
                }
                cl = classCL;
            } else if (holder_contextCL.value == JAXBUtils.CONSTRUCTION_TYPE.BY_CONTEXT_PATH) {
                // Successfully found all classes with classloader, use this one
                if (log.isDebugEnabled()) {
                    log.debug("Successfully loaded JAXBContext with Contxst ClassLoader.  Choose Context ClassLoader");
                }
                cl = contextCL;
            } else if (holder_classCL.value == JAXBUtils.CONSTRUCTION_TYPE.BY_CONTEXT_PATH) {
                // Successfully found all classes with classloader, use this one
                if (log.isDebugEnabled()) {
                    log.debug("Successfully loaded JAXBContext with Class ClassLoader.  Choose Class ClassLoader");
                }
                cl = classCL;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Default to Class ClassLoader");
                }
                cl = classCL;
            }
        }
        
        
        
        if (log.isDebugEnabled()) {
            log.debug("Chosen ClassLoader is " + cls);
        }
        return cl;
    }
    
    private static ClassLoader getContextClassLoader() {
        // NOTE: This method must remain private because it uses AccessController
        ClassLoader cl = null;
        try {
            cl = (ClassLoader)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Thread.currentThread().getContextClassLoader();
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }

        return cl;
    }
    
    /**
     * @param cls
     * @return ClassLoader or null if cannot be obtained
     */
    private static ClassLoader getClassLoader(final Class cls) {
        // NOTE: This method must remain private because it uses AccessController
        if (cls == null) {
            return null;
        }
        ClassLoader cl = null;
        try {
            cl = (ClassLoader)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return cls.getClassLoader();
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
        }

        return cl;
    }
}
