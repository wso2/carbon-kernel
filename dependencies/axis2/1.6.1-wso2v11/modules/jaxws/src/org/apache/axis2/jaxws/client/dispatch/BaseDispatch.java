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

package org.apache.axis2.jaxws.client.dispatch;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.client.async.AsyncResponse;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.InvocationContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.controller.InvocationController;
import org.apache.axis2.jaxws.core.controller.InvocationControllerFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.impl.alt.MethodMarshallerUtils;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.spi.Binding;
import org.apache.axis2.jaxws.spi.Constants;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.spi.migrator.ApplicationContextMigratorUtil;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.Response;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public abstract class BaseDispatch<T> extends BindingProvider
        implements javax.xml.ws.Dispatch {

    private static Log log = LogFactory.getLog(BaseDispatch.class);

    protected InvocationController ic;

    protected ServiceClient serviceClient;

    protected Mode mode;

    protected BaseDispatch(ServiceDelegate svcDelgate,
                           EndpointDescription epDesc,
                           EndpointReference epr,
                           String addressingNamespace,
                           WebServiceFeature... features) {
        super(svcDelgate, epDesc, epr, addressingNamespace, features);

        InvocationControllerFactory icf = (InvocationControllerFactory) FactoryRegistry.getFactory(InvocationControllerFactory.class);
        ic = icf.getInvocationController();
        
        if (ic == null) {
            throw new WebServiceException(Messages.getMessage("missingInvocationController"));
        }
    }

    /**
     * Take the input object and turn it into an OMElement so that it can be sent.
     *
     * @param value
     * @return
     */
    protected abstract Message createMessageFromValue(Object value);

    /**
     * Given a message, return the business object based on the requestor's required format (PAYLOAD
     * vs. MESSAGE) and datatype.
     *
     * @param message
     * @return
     */
    protected abstract Object getValueFromMessage(Message message);

    /**
     * Creates an instance of the AsyncListener that is to be used for waiting for async responses.
     *
     * @return a configured AsyncListener instance
     */
    protected abstract AsyncResponse createAsyncResponseListener();

    
    /**
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
     */
    public Object invoke(Object obj) throws WebServiceException {

        // Catch all exceptions and rethrow an appropriate WebService Exception
        try {
            if (log.isDebugEnabled()) {
                log.debug("Entered synchronous invocation: BaseDispatch.invoke()");
            }

            // Create the InvocationContext instance for this request/response flow.
            InvocationContext invocationContext =
                    InvocationContextFactory.createInvocationContext(null);
            invocationContext.setServiceClient(serviceClient);

            // Create the MessageContext to hold the actual request message and its
            // associated properties
            MessageContext requestMsgCtx = new MessageContext();
            requestMsgCtx.getAxisMessageContext().setProperty(BINDING_PROVIDER, this);
            requestMsgCtx.setEndpointDescription(getEndpointDescription());
            invocationContext.setRequestMessageContext(requestMsgCtx);
            
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
            Binding binding = (Binding) getBinding();
            invocationContext.setHandlers(binding.getHandlerChain());

            initMessageContext(obj, requestMsgCtx);

            // call common init method for all invoke* paths
            preInvokeInit(invocationContext);
                        
            // Migrate the properties from the client request context bag to
            // the request MessageContext.
            ApplicationContextMigratorUtil.performMigrationToMessageContext(
                    Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID,
                    getRequestContext(), requestMsgCtx);

            // Perform the WebServiceFeature configuration requested by the user.
            binding.configure(requestMsgCtx, this);

            // Initializing the message context above will put the outbound message onto the messageContext
            // Determine the operation if possible from the outbound message.  If it can not be determined
            // it will be set to null.  In this case, an anonymous operation will be used.  Note that determining
            // the operation will mean deserializing the message.  That means that any WebServiceFeatures must have
            // been configured first so that any relevant configurations (such as MTOM) have been initialized prior to 
            // the message being deserialized.  This is particularly true for Dispatch<JAXB Element>.
            requestMsgCtx.setOperationDescription(getOperationDescriptionForDispatch(requestMsgCtx));

            // Send the request using the InvocationController
            ic.invoke(invocationContext);

            MessageContext responseMsgCtx = invocationContext.getResponseMessageContext();
            responseMsgCtx.setEndpointDescription(requestMsgCtx.getEndpointDescription());

            // Migrate the properties from the response MessageContext back
            // to the client response context bag.
            ApplicationContextMigratorUtil.performMigrationFromMessageContext(
                    Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID,
                    getResponseContext(), responseMsgCtx);
            
            if (hasFaultResponse(responseMsgCtx)) {
                WebServiceException wse = BaseDispatch.getFaultResponse(responseMsgCtx);
                throw wse;
            }

            // Get the return object
            Object returnObj = null;
            try {
                Message responseMsg = responseMsgCtx.getMessage();
                returnObj = getValueFromMessage(responseMsg);
            }
            finally {
                // Free the incoming input stream
                try {
                    responseMsgCtx.freeInputStream();
                }
                catch (Throwable t) {
                    throw ExceptionFactory.makeWebServiceException(t);
                }
            }
           
            //Check to see if we need to maintain session state
            checkMaintainSessionState(requestMsgCtx, invocationContext);

            if (log.isDebugEnabled()) {
                log.debug("Synchronous invocation completed: BaseDispatch.invoke()");
            }

            return returnObj;
        } catch (WebServiceException e) {
            if (log.isDebugEnabled()) {
                log.debug("BaseDispatch.invoke(): Synchronous invocation failed, " 
                        + "caught a WebServiceException: ", e);
            }
            throw e;
        } catch (Exception e) {
            // All exceptions are caught and rethrown as a WebServiceException
            if (log.isDebugEnabled()) {
                log.debug("BaseDispatch.invoke(): Synchronous invocation failed, caught an Exception, " + 
                        "wrapping into a WebServiceException. Exception caught: ", e);
            }  
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    /**
     * Given a JAXWS Message Context which contains an outbound service-requester Message for a Dispatch client, 
     * determine the OperationDescription for the operation contained in that Dispatch message.
     * 
     * Note that operation resolution can be disabled by a property setting.
     * @see org.apache.axis2.jaxws.Constants.DISPATCH_CLIENT_OUTBOUND_RESOLUTION
     * 
     * @param requestMessageCtx JAXWS Message Context containing the outbound Dispatch message
     * @return the OperationDescription corresponding to the operation contained in the Dispatch message, or null 
     * if it can not be determined or if dispatch operation resolution is disabled via a property.
     */
    private OperationDescription getOperationDescriptionForDispatch(MessageContext requestMessageCtx) {
        OperationDescription operationDesc = null;
        if (dispatchOperationResolutionEnabled()) {
            EndpointInterfaceDescription endpointInterfaceDesc = getEndpointDescription().getEndpointInterfaceDescription();
            // The SEI interface could be null (for example if there was no SEI and all the ports were dynamically added).
            // If there is an SEI, then try to determine the operation for the outbound dispatch message.
            if (endpointInterfaceDesc != null) {
                QName bodyElementQName = getBodyElementQNameFromDispatchMessage(requestMessageCtx);
                operationDesc = determineOperationDescFromBodyElementQName(endpointInterfaceDesc, bodyElementQName);
            }
        }
        return operationDesc;
    }
    
    /**
     * Returns the OperationDescription corresponding to the bodyElementQName passed in.  What that body element corresponds to
     * depends on the type of the message:
     * - For Doc/Lit/Wrapped, the body element is the operation name
     * - For Doc/Lit/Bare, the body element is the element name contained in the wsdl:message wsdl:part
     * - For RPC, the body element is effectively the operation name.
     * 
     * @param endpointInterfaceDesc The interface (i.e. SEI) on which to search for the operation
     * @param bodyElementQName the QName of the first body element for which to find the operation
     * 
     * @return The OperationDescription corresponding to the body element QName or null if one can not be found.
     */
    private OperationDescription determineOperationDescFromBodyElementQName(EndpointInterfaceDescription endpointInterfaceDesc,
                                                                            QName bodyElementQName) {
        OperationDescription operationDesc = null;
        
        // If there's no bodyElementQName for us to work with, there's nothing more we can do.
        if (bodyElementQName != null) {
            // This logic mimics the code in SOAPMessageBodyBasedOperationDispatcher.findOperation.  We will look for
            // the AxisOperation corresponding to the body element name.  Note that we are searching for the AxisOperation instead
            // of searching through the OperationDescriptions so that we can use the getOperationByMessageElementQName
            // for the Doc/Lit/Bare case.  Once we have the AxisOperation, we'll use that to find the Operation Description.
            AxisService axisService = endpointInterfaceDesc.getEndpointDescription().getAxisService();
            AxisOperation axisOperation = null;
    
            // Doc/Lit/Wrapped and RPC, the operation name is the first body element qname
            axisOperation = axisService.getOperation(new QName(bodyElementQName.getLocalPart()));
            
            if (axisOperation == null) {
                // Doc/Lit/Bare, the first body element qname is the element name contained in the wsdl:message part
                axisOperation = axisService.getOperationByMessageElementQName(bodyElementQName);
            }
            
            if (axisOperation == null) {
                // Not sure why we wouldn't have found the operation above using just the localPart rather than the full QName used here,
                // but this is what SOAPMessageBodyBasedOperationDispatcher.findOperation does.
                axisOperation = axisService.getOperation(bodyElementQName);
            }
    
            // If we found an axis operation, then find the operation description that corresponds to it
            if (axisOperation != null) {
                OperationDescription allOpDescs[] = endpointInterfaceDesc.getDispatchableOperations();
                for (OperationDescription checkOpDesc : allOpDescs ) {
                    AxisOperation checkAxisOperation = checkOpDesc.getAxisOperation();
                    if (checkAxisOperation == axisOperation) {
                        operationDesc = checkOpDesc;
                        break;
                    }
                }
            }
        }
        return operationDesc;
    }

    /**
     * Answer if operation resolution on outbound messages for dispatch clients should be done.  The default value 
     * is TRUE, enabling operation resolution.  Resolution can be disabled via a property on the AxisConfiguration
     * or on the RequestContext.  
     * 
     * Operation resolution is also disabled if a non-null value is specified on the request context for the Action
     * 
     * @see org.apache.axis2.jaxws.Constants.DISPATCH_CLIENT_OUTBOUND_RESOLUTION
     * @see javax.xml.ws.BindingProvider.SOAPACTION_USE_PROPERTY
     * @see javax.xml.ws.BindingProvider.SOAPACTION_URI_PROPERTY
     * 
     * @return true if operation resolution should be performed on outbound 
     */
    private boolean dispatchOperationResolutionEnabled() {
        boolean resolutionEnabled = true;
        
        // See if any properties disabled operation resolution 
        // Check for System property setting
        String flagValue = getProperty(org.apache.axis2.jaxws.Constants.DISPATCH_CLIENT_OUTBOUND_RESOLUTION);

        // If no System property was set, see if one was set on this request context.
        if (flagValue == null) {
            flagValue =  (String) getRequestContext().get(org.apache.axis2.jaxws.Constants.DISPATCH_CLIENT_OUTBOUND_RESOLUTION);
        }
        
        // If any property was set, check the value.
        if (flagValue != null) {
            if ("false".equalsIgnoreCase(flagValue)) {
                resolutionEnabled = false;
            } else if ("true".equalsIgnoreCase(flagValue)) {
                resolutionEnabled = true;
            }
        }

        // If a property didn't disable resolution, then see if a URI value was specified.
        // If so, we'll use that later and there's no need to do operation resolution.         
        if (resolutionEnabled) {
            Boolean useSoapAction = (Boolean) getRequestContext().get(SOAPACTION_USE_PROPERTY);
            if (useSoapAction != null && useSoapAction.booleanValue()) {
                String soapAction = (String) getRequestContext().get(SOAPACTION_URI_PROPERTY);
                if (soapAction != null) {
                    resolutionEnabled = false;
                }
            }
        }
        return resolutionEnabled;
    }

    /**
     * Retrieve the specified property from the AxisConfiguration.
     * 
     * @param key The property to retrieve from the AxisConfiguration
     * @return the value associated with the property or null if the property did not exist on the configuration.
     */
    private String getProperty(String key) {
        String propertyValue = null;
        AxisConfiguration axisConfig = serviceDelegate.getServiceDescription().getAxisConfigContext().getAxisConfiguration();
        Parameter parameter = axisConfig.getParameter(key);
        if (parameter != null) {
            propertyValue = (String) parameter.getValue();
        }
        return propertyValue;
    }


    /**
     * Given a JAXWS Message Context which contains an outbound service-requester Message for a Dispatch client,
     * determine the QName of the first body element contained in that message.
     * 
     * @param requestMessageCtx requestMessageCtx JAXWS Message Context containing the outbound Dispatch message
     * @return the QName of the first body element contained in the outbound Dispatch message, or null if it 
     * can not be determined.
     */
    QName getBodyElementQNameFromDispatchMessage(MessageContext requestMessageCtx) {
        QName bodyElementQName = null;
        Message dispatchMessage = requestMessageCtx.getMessage();
        SOAPMessage soapMessage = dispatchMessage.getAsSOAPMessage();
        try {
            SOAPBody soapBody = soapMessage.getSOAPBody();
            Node firstElement = soapBody.getFirstChild();
            // A Doc/Lit/Bare message may not have a firsElement.  The soap:Body element may be empty if there 
            // are no arguments to the operation.
            if (firstElement != null) {
                String ns = firstElement.getNamespaceURI();
                String lp= firstElement.getLocalName();
                // A Doc/Lit/Bare message may not have a localPart on the element.  That can happen if the first element
                // is the argument value and there is no wrapper element surrounding it.
                if (lp != null) {
                    bodyElementQName = new QName(ns, lp);
                }
            }
        } catch (SOAPException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unabled to get the first body element from the outbound dispatch message", e);
            }
        }
        return bodyElementQName;
    }

    protected void initMessageContext(Object obj, MessageContext requestMsgCtx) {
        Message requestMsg = createRequestMessage(obj);
        setupMessageProperties(requestMsg);
        requestMsgCtx.setMessage(requestMsg);
        // handle HTTP_REQUEST_METHOD property
        String method = (String)requestContext.get(javax.xml.ws.handler.MessageContext.HTTP_REQUEST_METHOD);
        if (method != null) {
            requestMsgCtx.setProperty(org.apache.axis2.Constants.Configuration.HTTP_METHOD, method);
        }
    }

    /**
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
     */
    public void invokeOneWay(Object obj) throws WebServiceException {

        // All exceptions are caught and rethrown as a WebServiceException
        try {
            if (log.isDebugEnabled()) {
                log.debug("Entered one-way invocation: BaseDispatch.invokeOneWay()");
            }

            // Create the InvocationContext instance for this request/response flow.
            InvocationContext invocationContext =
                    InvocationContextFactory.createInvocationContext(null);
            invocationContext.setServiceClient(serviceClient);

            // Create the MessageContext to hold the actual request message and its
            // associated properties
            MessageContext requestMsgCtx = new MessageContext();
            requestMsgCtx.getAxisMessageContext().setProperty(BINDING_PROVIDER, this);
            requestMsgCtx.setEndpointDescription(getEndpointDescription());
            invocationContext.setRequestMessageContext(requestMsgCtx);
            
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
            Binding binding = (Binding) getBinding();
            invocationContext.setHandlers(binding.getHandlerChain());

            initMessageContext(obj, requestMsgCtx);

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
                    if (invocationContext.getServiceClient().getServiceContext().getProperty(HTTPConstants.HEADER_COOKIE) == null) {
                        invocationContext.getServiceClient().getServiceContext().setProperty(HTTPConstants.HEADER_COOKIE, requestContext.get(HTTPConstants.HEADER_COOKIE));
                        if (log.isDebugEnabled()) {
                            log.debug("Client-app defined Cookie property (assume to be session cookie) on request context copied to service context." +
                                "  Caution:  server may or may not support sessions, but client app will not be informed when not supported.");
                        }
                    }
                }
            }

            // call common init method for all invoke* paths
            preInvokeInit(invocationContext);
            
            // Migrate the properties from the client request context bag to
            // the request MessageContext.
            ApplicationContextMigratorUtil.performMigrationToMessageContext(
                    Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID,
                    getRequestContext(), requestMsgCtx);

            // Perform the WebServiceFeature configuration requested by the user.
            binding.configure(requestMsgCtx, this);

            // Initializing the message context above will put the outbound message onto the messageContext
            // Determine the operation if possible from the outbound message.  If it can not be determined
            // it will be set to null.  In this case, an anonymous operation will be used.  Note that determining
            // the operation will mean deserializing the message.  That means that any WebServiceFeatures must have
            // been configured first so that any relevant configurations (such as MTOM) have been initialized prior to 
            // the message being deserialized.  This is particularly true for Dispatch<JAXB Element>.
            requestMsgCtx.setOperationDescription(getOperationDescriptionForDispatch(requestMsgCtx));

            // Send the request using the InvocationController
            ic.invokeOneWay(invocationContext);

            //Check to see if we need to maintain session state
            checkMaintainSessionState(requestMsgCtx, invocationContext);

            if (log.isDebugEnabled()) {
                log.debug("One-way invocation completed: BaseDispatch.invokeOneWay()");
            }

            return;
        } catch (WebServiceException e) {
            if (log.isDebugEnabled()) {
                log.debug("BaseDispatch.invokeOneWay(): One-way invocation failed, " + 
                        "caught a WebServiceException: ", e);
            }
            throw e;
        } catch (Exception e) {
            // All exceptions are caught and rethrown as a WebServiceException
            if (log.isDebugEnabled()) {
                log.debug("BaseDispatch.invokeOneWay(): One-way invocation failed, " + 
                        "caught an Exception, wrapping into a WebServicesException. " + 
                        " Exception caught: ", e);
            }
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    /**
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
     */
    public Future<?> invokeAsync(Object obj, AsyncHandler asynchandler) throws WebServiceException {

        // All exceptions are caught and rethrown as a WebServiceException
        try {
            if (log.isDebugEnabled()) {
                log.debug("Entered asynchronous (callback) invocation: BaseDispatch.invokeAsync()");
            }

            // Create the InvocationContext instance for this request/response flow.
            InvocationContext invocationContext =
                    InvocationContextFactory.createInvocationContext(null);
            invocationContext.setServiceClient(serviceClient);

            // Create the MessageContext to hold the actual request message and its
            // associated properties
            MessageContext requestMsgCtx = new MessageContext();
            requestMsgCtx.getAxisMessageContext().setProperty(BINDING_PROVIDER, this);
            requestMsgCtx.setEndpointDescription(getEndpointDescription());
            invocationContext.setRequestMessageContext(requestMsgCtx);
            
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
            Binding binding = (Binding) getBinding();
            invocationContext.setHandlers(binding.getHandlerChain());

            initMessageContext(obj, requestMsgCtx);
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
                    if (invocationContext.getServiceClient().getServiceContext().getProperty(HTTPConstants.HEADER_COOKIE) == null) {
                        invocationContext.getServiceClient().getServiceContext().setProperty(HTTPConstants.HEADER_COOKIE, requestContext.get(HTTPConstants.HEADER_COOKIE));
                        if (log.isDebugEnabled()) {
                            log.debug("Client-app defined Cookie property (assume to be session cookie) on request context copied to service context." +
                                "  Caution:  server may or may not support sessions, but client app will not be informed when not supported.");
                        }
                    }
                }
            }

            // call common init method for all invoke* paths
            preInvokeInit(invocationContext);
            
            // Migrate the properties from the client request context bag to
            // the request MessageContext.
            ApplicationContextMigratorUtil.performMigrationToMessageContext(
                    Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID,
                    getRequestContext(), requestMsgCtx);

            // Perform the WebServiceFeature configuration requested by the user.
            binding.configure(requestMsgCtx, this);

            // Initializing the message context above will put the outbound message onto the messageContext
            // Determine the operation if possible from the outbound message.  If it can not be determined
            // it will be set to null.  In this case, an anonymous operation will be used.  Note that determining
            // the operation will mean deserializing the message.  That means that any WebServiceFeatures must have
            // been configured first so that any relevant configurations (such as MTOM) have been initialized prior to 
            // the message being deserialized.  This is particularly true for Dispatch<JAXB Element>.
            requestMsgCtx.setOperationDescription(getOperationDescriptionForDispatch(requestMsgCtx));

            // Setup the Executor that will be used to drive async responses back to 
            // the client.
            // FIXME: We shouldn't be getting this from the ServiceDelegate, rather each 
            // Dispatch object should have it's own.
            Executor e = serviceDelegate.getExecutor();
            invocationContext.setExecutor(e);

            // Create the AsyncListener that is to be used by the InvocationController.
            AsyncResponse listener = createAsyncResponseListener();
            invocationContext.setAsyncResponseListener(listener);

            // Send the request using the InvocationController
            Future<?> asyncResponse = ic.invokeAsync(invocationContext, asynchandler);

            //Check to see if we need to maintain session state
            checkMaintainSessionState(requestMsgCtx, invocationContext);

            if (log.isDebugEnabled()) {
                log.debug("Asynchronous (callback) invocation sent: BaseDispatch.invokeAsync()");
            }

            return asyncResponse;
        } catch (WebServiceException e) {
            if (log.isDebugEnabled()) {
                log.debug("BaseDispatch.invokeAsync() [Callback]: Asynchronous invocation failed, " +
                    "caught a WebServiceException: ", e);
            }
            throw e;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("BaseDispatch.invokeAsync() [Callback]: Asynchronous invocation failed, " +
                    "caught an Exception, wrapping into a WebServiceException. Exception caught: ", e);
            }
            // All exceptions are caught and rethrown as a WebServiceException
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    /**
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
     */
    public Response invokeAsync(Object obj) throws WebServiceException {

        // All exceptions are caught and rethrown as a WebServiceException
        try {
            if (log.isDebugEnabled()) {
                log.debug("Entered asynchronous (polling) invocation: BaseDispatch.invokeAsync()");
            }

            // Create the InvocationContext instance for this request/response flow.
            InvocationContext invocationContext =
                    InvocationContextFactory.createInvocationContext(null);
            invocationContext.setServiceClient(serviceClient);

            // Create the MessageContext to hold the actual request message and its
            // associated properties
            MessageContext requestMsgCtx = new MessageContext();
            requestMsgCtx.getAxisMessageContext().setProperty(BINDING_PROVIDER, this);
            requestMsgCtx.setEndpointDescription(getEndpointDescription());
            invocationContext.setRequestMessageContext(requestMsgCtx);
            
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
            Binding binding = (Binding) getBinding();
            invocationContext.setHandlers(binding.getHandlerChain());

            initMessageContext(obj, requestMsgCtx);

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
                    if (invocationContext.getServiceClient().getServiceContext().getProperty(HTTPConstants.HEADER_COOKIE) == null) {
                        invocationContext.getServiceClient().getServiceContext().setProperty(HTTPConstants.HEADER_COOKIE, requestContext.get(HTTPConstants.HEADER_COOKIE));
                        if (log.isDebugEnabled()) {
                            log.debug("Client-app defined Cookie property (assume to be session cookie) on request context copied to service context." +
                                "  Caution:  server may or may not support sessions, but client app will not be informed when not supported.");
                        }
                    }
                }
            }

            // call common init method for all invoke* paths
            preInvokeInit(invocationContext);
            
            // Migrate the properties from the client request context bag to
            // the request MessageContext.
            ApplicationContextMigratorUtil.performMigrationToMessageContext(
                    Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID,
                    getRequestContext(), requestMsgCtx);

            // Perform the WebServiceFeature configuration requested by the user.
            binding.configure(requestMsgCtx, this);

            // Initializing the message context above will put the outbound message onto the messageContext
            // Determine the operation if possible from the outbound message.  If it can not be determined
            // it will be set to null.  In this case, an anonymous operation will be used.  Note that determining
            // the operation will mean deserializing the message.  That means that any WebServiceFeatures must have
            // been configured first so that any relevant configurations (such as MTOM) have been initialized prior to 
            // the message being deserialized.  This is particularly true for Dispatch<JAXB Element>.
            requestMsgCtx.setOperationDescription(getOperationDescriptionForDispatch(requestMsgCtx));
            

            // Setup the Executor that will be used to drive async responses back to 
            // the client.
            // FIXME: We shouldn't be getting this from the ServiceDelegate, rather each 
            // Dispatch object should have it's own.
            Executor e = serviceDelegate.getExecutor();
            invocationContext.setExecutor(e);

            // Create the AsyncListener that is to be used by the InvocationController.
            AsyncResponse listener = createAsyncResponseListener();
            invocationContext.setAsyncResponseListener(listener);

            // Send the request using the InvocationController
            Response asyncResponse = ic.invokeAsync(invocationContext);

            //Check to see if we need to maintain session state
            checkMaintainSessionState(requestMsgCtx, invocationContext);

            if (log.isDebugEnabled()) {
                log.debug("Asynchronous (polling) invocation sent: BaseDispatch.invokeAsync()");
            }

            return asyncResponse;
        } catch (WebServiceException e) {
            if (log.isDebugEnabled()) {
                log.debug("BaseDispatch.invokeAsync() [Polling]: Asynchronous invocation failed, " +
                    "caught a WebServiceException: ", e);
            }
            throw e;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("BaseDispatch.invokeAsync() [Polling]: Asynchronous invocation failed, " +
                    "caught an Exception, wrapping into a WebServiceException. Exception caught: ",e);
            }
            // All exceptions are caught and rethrown as a WebServiceException
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public void setServiceClient(ServiceClient sc) {
        serviceClient = sc;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode m) {
        mode = m;
    }

    /**
     * Returns the fault that is contained within the MessageContext for an invocation. If no fault
     * exists, null will be returned.
     *
     * @param msgCtx
     * @return
     */
    public static WebServiceException getFaultResponse(MessageContext msgCtx) {
        try {
            Message msg = msgCtx.getMessage();
            if (msg != null && msg.isFault()) {
                //XMLFault fault = msg.getXMLFault();
                // 4.3.2 conformance bullet 1 requires a ProtocolException here
                ProtocolException pe =
                    MethodMarshallerUtils.createSystemException(msg.getXMLFault(), msg);
                return pe;
            } else if (msgCtx.getLocalException() != null) {
                // use the factory, it'll throw the right thing:
                return ExceptionFactory.makeWebServiceException(msgCtx.getLocalException());
            }
        } finally {
            // Free the incoming input stream
            try {
                msgCtx.freeInputStream();
            } catch (IOException ioe) {
                return ExceptionFactory.makeWebServiceException(ioe);
            }
        }

        return null;
    }

    /**
     * Returns a boolean indicating whether or not the MessageContext contained a fault.
     *
     * @param msgCtx
     * @return
     */
    public boolean hasFaultResponse(MessageContext msgCtx) {
    	if(!msgCtx.getAxisMessageContext().getOptions().isExceptionToBeThrownOnSOAPFault()){
        	if(log.isDebugEnabled()){
        		log.debug("msgCtx.Options.isExceptionToBeThrownOnSOAPFault set to false; Exception will not be thrown on fault");
        	}
    		return false;
    	}
        if (msgCtx.getMessage() != null && msgCtx.getMessage().isFault())
            return true;
        else if (msgCtx.getLocalException() != null)
            return true;
        else
            return false;
    }

    /*
     * Configure any properties that will be needed on the Message
     */
    private void setupMessageProperties(Message msg) {
        // If the user has enabled MTOM on the SOAPBinding, we need
        // to make sure that gets pushed to the Message object.
        Binding binding = (Binding) getBinding();
        if (binding != null && binding instanceof SOAPBinding) {
            SOAPBinding soapBinding = (SOAPBinding)binding;
            if (soapBinding.isMTOMEnabled())
                msg.setMTOMEnabled(true);
        }
    }

    /*
    * Checks to see if the parameter for the invocation is valid
    * given the scenario that the client is operating in.  There are
    * some cases when nulls are allowed and others where it is
    * an error.
    */
    private boolean isValidInvocationParam(Object object) {
        String bindingId = endpointDesc.getClientBindingID();

        // If no bindingId was found, use the default.
        if (bindingId == null) {
            bindingId = SOAPBinding.SOAP11HTTP_BINDING;
        }

        // If it's not an HTTP_BINDING, then we can allow for null params,  
        // but only in PAYLOAD mode per JAX-WS Section 4.3.2.
        if (!bindingId.equals(HTTPBinding.HTTP_BINDING)) {
            if (mode.equals(Mode.MESSAGE) && object == null) {
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("dispatchNullParamMessageMode"));
            }
        } else {
            // In all cases (PAYLOAD and MESSAGE) we must throw a WebServiceException
            // if the parameter is null and request method is POST or PUT.
            if (object == null && isPOSTorPUTRequest()) {
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("dispatchNullParamHttpBinding"));
            }
        }

        if (object instanceof DOMSource) {
            DOMSource ds = (DOMSource)object;
            if (ds.getNode() == null && ds.getSystemId() == null) {
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("dispatchBadDOMSource"));
            }
        }

        // If we've gotten this far, then all is good.
        return true;
    }
    
    private boolean isPOSTorPUTRequest() {
        String method = (String)this.requestContext.get(javax.xml.ws.handler.MessageContext.HTTP_REQUEST_METHOD);
        // if HTTP_REQUEST_METHOD is not specified, assume it is a POST method
        return (method == null || 
                HTTPConstants.HEADER_POST.equalsIgnoreCase(method) || 
                HTTPConstants.HEADER_PUT.equalsIgnoreCase(method));
    }
    
    private Message createRequestMessage(Object obj) throws WebServiceException {
        
        // Check to see if the object is a valid invocation parameter.  
        // Then create the message from the object.
        // If an exception occurs, it is local to the client and therefore is a
        // WebServiceException (and not ProtocolExceptions).
        // This code complies with JAX-WS 2.0 sections 4.3.2, 4.3.3 and 4.3.4.
        if (!isValidInvocationParam(obj)) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("dispatchInvalidParam"));
        } 
        Message requestMsg = null;
        try {
             requestMsg = createMessageFromValue(obj);
        } catch (Throwable t) {
            // The webservice exception wraps the thrown exception.
            throw ExceptionFactory.makeWebServiceException(t);
        }
        return requestMsg;
    }
    
    private void preInvokeInit(InvocationContext requestIC) {
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
    }
}
