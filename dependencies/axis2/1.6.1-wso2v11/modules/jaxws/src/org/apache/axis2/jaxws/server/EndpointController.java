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

import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.context.utils.ContextUtils;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.handler.HandlerChainProcessor;
import org.apache.axis2.jaxws.handler.HandlerInvocationContext;
import org.apache.axis2.jaxws.handler.HandlerInvoker;
import org.apache.axis2.jaxws.handler.HandlerInvokerUtils;
import org.apache.axis2.jaxws.handler.HandlerResolverImpl;
import org.apache.axis2.jaxws.handler.HandlerUtils;
import org.apache.axis2.jaxws.handler.factory.HandlerInvokerFactory;
import org.apache.axis2.jaxws.handler.lifecycle.factory.HandlerLifecycleManager;
import org.apache.axis2.jaxws.handler.lifecycle.factory.HandlerLifecycleManagerFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.dispatcher.EndpointDispatcher;
import org.apache.axis2.jaxws.server.dispatcher.ProviderDispatcher;
import org.apache.axis2.jaxws.server.dispatcher.factory.EndpointDispatcherFactory;
import org.apache.axis2.jaxws.server.endpoint.Utils;
import org.apache.axis2.jaxws.spi.Constants;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004_Constants;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2006Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.PortInfo;

import java.io.StringReader;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * The EndpointController is the server side equivalent to the InvocationController on the client
 * side.  It is an abstraction of the server side endpoint invocation that encapsulates all of the
 * Axis2 semantics.
 * <p/>
 * Like the InvocationController, this class is responsible for invoking the JAX-WS application
 * handler chain along with taking all of the provided information and setting up what's needed to
 * perform the actual invocation of the endpoint.
 */
public class EndpointController {

    private static final Log log = LogFactory.getLog(EndpointController.class);


    /**
     * This method is used to start the JAX-WS invocation of a target endpoint. It takes an
     * InvocationContext, which must have a MessageContext specied for the request.  Once the
     * invocation is complete, the information will be stored
     * 
     * @param eic
     * @return
     */
    public EndpointInvocationContext invoke(EndpointInvocationContext eic) throws AxisFault, WebServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: synchronous");
        }
        
        MessageContext request = eic.getRequestMessageContext();
        boolean good = true;
        try {
            good = handleRequest(eic);

            if (!good) {
                return eic;
            }
            MessageContext response = null;
            EndpointDispatcher dispatcher = eic.getDispatcher();
            if (request != null && dispatcher != null) {
                response = dispatcher.invoke(request);    
                // Note that response may be null in the case of a Provider returning null
                eic.setResponseMessageContext(response);
            }
            else {
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("invokeErr"));
            }
        } catch (AxisFault af) {
            throw af;
        } catch (Exception e) {
            Throwable toBeThrown = InvocationHelper.determineMappedException(e, eic);
            if(toBeThrown == null) {
                toBeThrown = e;
            }
            throw ExceptionFactory.makeWebServiceException(toBeThrown);
        } finally {
            if (good) {
                // Passed pivot point
                request.getMessage().setPostPivot();
                handleResponse(eic);
            } else {
                destroyHandlers(eic, request);
            }
            // Release WebServiceContextResources if available
            ContextUtils.releaseWebServiceContextResources(request);
        }
        
        return eic;
    }
    
    public void invokeAsync(EndpointInvocationContext eic) {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: asynchronous");
        }
        
        MessageContext request = eic.getRequestMessageContext();
        try {
            boolean good = handleRequest(eic);

            if (!good) {
                return;
            }
            EndpointDispatcher dispatcher = eic.getDispatcher();
            if (request != null && dispatcher != null) {
                dispatcher.invokeAsync(request, eic.getCallback());    
            }
            else {
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("invokeErr"));
            }
        } catch (Exception e) {
            Throwable toBeThrown = InvocationHelper.determineMappedException(e, eic);
            if(toBeThrown == null) {
                toBeThrown = e;
            }
            throw ExceptionFactory.makeWebServiceException(toBeThrown);
        } finally {
            // FIXME (NLG): Probably need to revisit this location.  Should it be moved down?
            // Passed pivot point
            request.getMessage().setPostPivot();
        }
        
        return;
    }
    
    public void invokeOneWay(EndpointInvocationContext eic) {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: one-way");
        }
    

        MessageContext request = eic.getRequestMessageContext();
        try {
            boolean good = handleRequest(eic);

            if (!good) {
                return;
            }
            EndpointDispatcher dispatcher = eic.getDispatcher();
            if (request != null && dispatcher != null) {
                dispatcher.invokeOneWay(request);    
            }
            else {
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("invokeErr"));
            }
        } catch (Exception e) {
            Throwable toBeThrown = InvocationHelper.determineMappedException(e, eic);
            if(toBeThrown == null) {
                toBeThrown = e;
            }
            throw ExceptionFactory.makeWebServiceException(toBeThrown);
        } finally {
            // Passed pivot point
            request.getMessage().setPostPivot();
        }
        
        return;
    }
    
    protected boolean handleRequest(EndpointInvocationContext eic) throws AxisFault, WebServiceException {
        
        MessageContext responseMsgContext = null;

        try {
            requestReceived(eic);
            
            MessageContext request = eic.getRequestMessageContext();
            
            Class serviceEndpoint = getServiceImplementation(request);
            EndpointDescription endpointDesc = getEndpointDescription(request);
            request.setEndpointDescription(endpointDesc);
           
            //  TODO: review: make sure the handlers are set on the InvocationContext
            //  This implementation of the JAXWS runtime does not use Endpoint, which
            //  would normally be the place to initialize and store the handler list.
            //  In lieu of that, we will have to intialize and store them on the 
            //  InvocationContext.  also see the InvocationContextFactory.  On the client
            //  side, the binding is not yet set when we call into that factory, so the
            //  handler list doesn't get set on the InvocationContext object there.  Thus
            //  we gotta do it here.
            //  
            //  Since we're on the server, and there apparently is no Binding object
            //  anywhere to be found...
            List<String> handlerRoles = null;
            if (eic.getHandlers() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No handlers found on the InvocationContext, initializing handler list.");
                }
                HandlerResolverImpl hri = new HandlerResolverImpl(endpointDesc.getServiceDescription());
                PortInfo portInfo = endpointDesc.getPortInfo();
                eic.setHandlers(hri.getHandlerChain(portInfo));
                handlerRoles = hri.getRoles(portInfo);
            }
            
            //  Get the service instance.  This will run the @PostConstruct code.
            ServiceInstanceFactory instanceFactory = (ServiceInstanceFactory) 
                FactoryRegistry.getFactory(ServiceInstanceFactory.class);
            Object serviceInstance = instanceFactory.createServiceInstance(request, serviceEndpoint);
            
            // The application handlers and dispatcher invoke will 
            // modify/destroy parts of the message.  Make sure to save
            // the request message if appropriate.
            saveRequestMessage(request);
            
            boolean success = true;
            
            // Perform inbound header/handler processing only if there is are headers OR handlers
            if ( (request.getAxisMessageContext() != null &&
                 request.getAxisMessageContext().getEnvelope().getHeader() != null) ||
                 (eic.getHandlers() != null && !eic.getHandlers().isEmpty())) {
                success = inboundHeaderAndHandlerProcessing(request, eic, handlerRoles);
            }
            
            if (success) {
                if (log.isDebugEnabled()) {
                    log.debug("JAX-WS inbound handler chain invocation complete.");
                }
                // Set the dispatcher.
                EndpointDispatcher dispatcher = getEndpointDispatcher(request, serviceEndpoint, serviceInstance);
                Boolean ignoreSOAPVersion = false;
                if(log.isDebugEnabled()){
                	log.debug("Checking for ProviderDispatcher instance");
                }
                if(dispatcher instanceof ProviderDispatcher){
                	if(log.isDebugEnabled()){
                		log.debug("ProviderDispatcher instance Found");
                	}
                	String bindingType = endpointDesc.getBindingType();
                	if(bindingType.equals(org.apache.axis2.jaxws.Constants.SOAP_HTTP_BINDING)){
                		ignoreSOAPVersion = true;
                	}   
                	if(log.isDebugEnabled()){
                		log.debug("ignoreSOAPVersion Value ="+ignoreSOAPVersion.booleanValue());
                	}
                }
                //Need to make sure the protocol (envelope ns)  of the request matches the binding
                // expected by the service description
                if (!ignoreSOAPVersion && !Utils.bindingTypesMatch(request, endpointDesc)) {
                	Protocol protocol = request.getMessage().getProtocol();
                	MessageContext faultContext = Utils.createVersionMismatchMessage(request, protocol);
                	eic.setResponseMessageContext(faultContext);
                	return false;
                }
                
                eic.setEndpointDispatcher(dispatcher);
                return true;
            } else { // the inbound handler chain must have had a problem, and we've reversed directions
                if (log.isDebugEnabled()) {
                    log.debug("JAX-WS inbound handler chain invocation completed with errors.");
                }
                responseMsgContext =
                        MessageContextUtils.createResponseMessageContext(request);
                // since we've reversed directions, the message has "become a response message" (section 9.3.2.1, footnote superscript 2)
                responseMsgContext.setMessage(request.getMessage());
                eic.setResponseMessageContext(responseMsgContext);
                responseReady(eic);
                return false;
            }
        } catch (AxisFault af) {
            throw af;
        } catch (Exception e) {
            // TODO for now, throw it.  We probably should try to make an XMLFault object and set it on the message
            throw ExceptionFactory.makeWebServiceException(e);
        } 
    }
    
    /**
     * Perform inbound Handler and Header processing
     * This includes the must understand checking and
     * invoking the inbound handler chain
     * @param request
     * @param eic
     * @param handlerRoles
     * @return
     * @throws AxisFault
     */
    private boolean inboundHeaderAndHandlerProcessing(MessageContext request, 
                                            EndpointInvocationContext eic, 
                                            List<String> handlerRoles) throws AxisFault {
        //Lets Initialize the understood QName here, add only the headers that the handler 
        //injects when we invoke the getHeader().
        //Since we are adding the handlers to description layer here we will register all the
        //headers set by SOAPHandler->getHeader().
         List<QName> understood =
             HandlerUtils.registerSOAPHandlerHeaders(request.getAxisMessageContext(), eic.getHandlers());

        //As per section 10.2.1 of JAXWS Specification, perform a mustUnderstand processing before
        //invoking inbound handlers.
        HandlerUtils.checkMustUnderstand(request.getAxisMessageContext(), understood, handlerRoles);

        // Invoke inbound application handlers.  It's safe to use the first object on the iterator because there is
        // always exactly one EndpointDescription on a server invoke
        HandlerInvocationContext hiContext = buildHandlerInvocationContext(request, eic.getHandlers(), 
                                                                           HandlerChainProcessor.MEP.REQUEST,
                                                                           isOneWay(request.getAxisMessageContext()));
        HandlerInvokerFactory hiFactory = (HandlerInvokerFactory) 
        FactoryRegistry.getFactory(HandlerInvokerFactory.class);
        HandlerInvoker handlerInvoker = hiFactory.createHandlerInvoker(request);
        boolean success = handlerInvoker.invokeInboundHandlers(hiContext);

        return success;
        
    }
    
    protected boolean handleResponse(EndpointInvocationContext eic) {
        MessageContext request = eic.getRequestMessageContext();
        MessageContext response = eic.getResponseMessageContext();
        
        try {
            if (response != null) {
            	//Before running inbound handlers lets make sure that the request and response have no protocol mismatch.
            	EndpointDescription endpointDesc =request.getEndpointDescription();
            	String bindingType = endpointDesc.getBindingType();
            	if(bindingType.equals(org.apache.axis2.jaxws.Constants.SOAP_HTTP_BINDING)){
            		if(log.isDebugEnabled()){
            			log.debug("Check for protocol mismatch");
            		}
            		MessageContext faultContext = isProtocolMismatch(request, response);
            		if(faultContext!=null){
            			if(log.isDebugEnabled()){
            				log.debug("There is a protocol mismatch, generating fault message");
            			}
            			eic.setResponseMessageContext(faultContext);
            			return false;
            		}
            		if(log.isDebugEnabled()){
            			log.debug("There is no protocol mismatch");
            		}
            	}
               // Invoke the outbound response handlers.
               // If the message is one way, we should not invoke the response handlers.  There is no response
               // MessageContext since a one way invocation is considered to have a "void" return.
               
               if (!isOneWay(request.getAxisMessageContext())) {
                    response.setMEPContext(request.getMEPContext());
                    
                    HandlerInvocationContext hiContext = buildHandlerInvocationContext(request, eic.getHandlers(), 
                                                                                       HandlerChainProcessor.MEP.RESPONSE,
                                                                                       false);
                    HandlerInvokerFactory hiFactory = (HandlerInvokerFactory) 
                        FactoryRegistry.getFactory(HandlerInvokerFactory.class);
                    HandlerInvoker handlerInvoker = hiFactory.createHandlerInvoker(response);
                    handlerInvoker.invokeOutboundHandlers(hiContext);
                    
               }
           } else {  // reponse is null.  
               if (MessageContextUtils.getJaxwsProviderInterpretNullOneway(request)){
                   // Provider must have returned null, and property is set.
                   // so now we only need to call closure
                   HandlerInvokerUtils.invokeInboundHandlersForClosure(request.getMEPContext(),
                           eic.getHandlers(),
                           HandlerChainProcessor.MEP.RESPONSE);
               }
           } 
        } catch (Exception e) {
            // TODO for now, throw it.  We probably should try to make an XMLFault object and set it on the message
            Throwable toBeThrown = InvocationHelper.determineMappedException(e, eic);
            if(toBeThrown == null) {
                toBeThrown = e;
            }
            throw ExceptionFactory.makeWebServiceException(toBeThrown);
        } finally {
        	// at this point, we are done with handler instances on the server; call @PreDestroy on all of them
            destroyHandlers(eic, request);
            responseReady(eic);
            restoreRequestMessage(request);
        }
        
        eic.setResponseMessageContext(response);
        return true;
    }

    private void destroyHandlers(EndpointInvocationContext eic, MessageContext request) {
        HandlerLifecycleManager hlm = createHandlerlifecycleManager();
        List<Handler> list = eic.getHandlers();
        if(list != null) {
            for (Iterator it = list.iterator(); it.hasNext();) {
                try {
                    Handler handler = (Handler) it.next();
                    hlm.destroyHandlerInstance(request, handler);
                } catch (Exception e) {
                    // TODO: can we ignore this?
                    throw ExceptionFactory.makeWebServiceException(e);
                }
            }
        }
    }

    /*
     * Returns the Class object for the implementation of the web service.
     */
    private Class getServiceImplementation(MessageContext mc) {
        String implClassName = getServiceImplClassName(mc);
        Class implClass = loadServiceImplClass(implClassName, mc.getClassLoader());
        return implClass;
    }

    /*
      * Get the appropriate EndpointDispatcher for a given service endpoint.
      */
    protected EndpointDispatcher getEndpointDispatcher(Class serviceImplClass, Object serviceInstance)
            throws Exception {
            return getEndpointDispatcher(null, serviceImplClass, serviceInstance);
    }
    
    protected EndpointDispatcher getEndpointDispatcher(MessageContext mc, Class serviceImplClass, 
                                                       Object serviceInstance) 
        throws Exception {
        EndpointDispatcherFactory factory = 
            (EndpointDispatcherFactory)FactoryRegistry.getFactory(EndpointDispatcherFactory.class);        
        return factory.createEndpointDispatcher(mc, serviceImplClass, serviceInstance);   
    }

    private String getServiceImplClassName(MessageContext mc) {
        // The PARAM_SERVICE_CLASS property that is set on the AxisService
        // will tell us what the service implementation class is.
        org.apache.axis2.context.MessageContext axisMsgContext = mc.getAxisMessageContext();
        AxisService as = axisMsgContext.getAxisService();
        Parameter param = as.getParameter(org.apache.axis2.Constants.SERVICE_CLASS);

        // If there was no implementation class, we should not go any further
        if (param == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage(
                    "EndpointControllerErr2"));
        }

        String className = ((String)param.getValue()).trim();
        return className;
    }
    
    /*
      * Tries to load the implementation class that was specified for the
      * target endpoint
      */
    private Class loadServiceImplClass(String className, ClassLoader cl) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to load service impl class: " + className);
        }

        try {
            //TODO: What should be done if the supplied ClassLoader is null?
            Class _class = forName(className, true, cl);
            return _class;
            //Catch Throwable as ClassLoader can throw an NoClassDefFoundError that
            //does not extend Exception, so lets catch everything that extends Throwable
            //rather than just Exception.
        } catch (Throwable cnf) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage(
                    "EndpointControllerErr4", className), cnf);
        }
    }

    /**
     * Return the class for this name
     *
     * @return Class
     */
    private static Class forName(final String className, final boolean initialize,
                                 final ClassLoader classloader) throws ClassNotFoundException {
        // NOTE: This method must remain private because it uses AccessController
        Class cl = null;
        try {
            cl = (Class)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Class.forName(className, initialize, classloader);
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("PrivilegedActionException thrown from AccessController: " + e);
                log.debug("Real Cause is " + e.getException().getCause());
            }
            throw (ClassNotFoundException)e.getException();
        }

        return cl;
    }

    /*
    * Gets the EndpointDescription associated with the request that is currently
    * being processed.
    */
    private EndpointDescription getEndpointDescription(MessageContext mc) {
        AxisService axisSvc = mc.getAxisMessageContext().getAxisService();
        Parameter param = axisSvc.getParameter(EndpointDescription.AXIS_SERVICE_PARAMETER);
        
        if (param == null) {
        	// If we've made it here, its very likely that although the AxisService was deployed, the 
        	// associated ServiceDescription was not created successfully
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("endpointDescErr1"));
        }
        
        EndpointDescription ed = (EndpointDescription) param.getValue();
        return ed;
    }

    /**
     * Save the request message if indicated by the SAVE_REQUEST_MSG property
     *
     * @param requestMsgContext
     */
    private void saveRequestMessage(MessageContext requestMsgContext) {

        // TODO: TESTING...FORCE SAVING THE REQUEST MESSAGE
        // requestMsgContext.getAxisMessageContext().setProperty(Constants.SAVE_REQUEST_MSG, Boolean.TRUE);
        // END TESTING

        Boolean value = (Boolean)
                requestMsgContext.getAxisMessageContext().getProperty(Constants.SAVE_REQUEST_MSG);
        if (value != null && value.booleanValue()) {
            // REVIEW: This does not properly account for attachments.
            Message m = requestMsgContext.getMessage();
            String savedMsg = m.getAsOMElement().toString();
            requestMsgContext.getAxisMessageContext()
                    .setProperty(Constants.SAVED_REQUEST_MSG_TEXT, savedMsg);
        }
    }

    /**
     * Restore the request message from the saved message text
     *
     * @param requestMsgContext
     */
    private void restoreRequestMessage(MessageContext requestMsgContext) {

        Boolean value = (Boolean)
                requestMsgContext.getAxisMessageContext().getProperty(Constants.SAVE_REQUEST_MSG);
        if (value != null && value.booleanValue()) {
            // REVIEW: This does not properly account for attachments.
            String savedMsg = (String)requestMsgContext.getAxisMessageContext()
                    .getProperty(Constants.SAVED_REQUEST_MSG_TEXT);
            if (savedMsg != null && savedMsg.length() > 0) {
                try {
                    StringReader sr = new StringReader(savedMsg);
                    XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader(sr);
                    MessageFactory mf = (MessageFactory)
                            FactoryRegistry.getFactory(MessageFactory.class);
                    Protocol protocol = requestMsgContext.getAxisMessageContext().isDoingREST() ?
                            Protocol.rest : null;
                    Message msg = mf.createFrom(xmlreader, protocol);
                    requestMsgContext.setMessage(msg);
                } catch (Throwable e) {
                    throw ExceptionFactory.makeWebServiceException(e);
                }
            }
        }

        // TESTING....SIMULATE A PERSIST OF THE REQUEST MESSAGE
        // String text = requestMsgContext.getMessage().getAsOMElement().toString();
        // System.out.println("Persist Message" + text);
        // END TESTING
    }

    /*
    * Determine if this is a one-way invocation or not.
    */
    public static boolean isOneWay(org.apache.axis2.context.MessageContext mc) {
        if (mc != null) {
            AxisOperation op = mc.getAxisOperation();
            String mep = op.getMessageExchangePattern();

            if (mep.equals(WSDL20_2004_Constants.MEP_URI_ROBUST_IN_ONLY) ||
                    mep.equals(WSDL20_2004_Constants.MEP_URI_IN_ONLY) ||
                    mep.equals(WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY) ||
                    mep.equals(WSDL20_2006Constants.MEP_URI_IN_ONLY)||
                    mep.equals(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY)||
                    mep.equals(WSDL2Constants.MEP_URI_IN_ONLY)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Builds the HandlerInvocationContext that will be used when invoking 
     * inbound/outbound handler chains.
     */
    static HandlerInvocationContext buildHandlerInvocationContext(MessageContext request, List<Handler> handlers, 
                                                          HandlerChainProcessor.MEP mep, boolean isOneWay) {
        HandlerInvocationContext hiContext = new HandlerInvocationContext();
        hiContext.setMessageContext(request);
        hiContext.setMEP(mep);
        hiContext.setHandlers(handlers);
        hiContext.setOneWay(isOneWay);
        return hiContext;
    }
    
    /**
     * This method will retrieve all the InvocationListenerFactory instances and
     * call the 'createInvocationListener' instance on each. If a non-null listener
     * is returned, the 'requestReceived' method will be called on the instance,
     * and it will be added to the EndpointInvocationContext.
     */
    protected void requestReceived(EndpointInvocationContext eic)  {
        Collection<InvocationListenerFactory> factoryList = eic.getInvocationListenerFactories();
        if(factoryList != null) {
            InvocationListenerBean bean = new InvocationListenerBean(eic, InvocationListenerBean.State.REQUEST);
            Iterator<InvocationListenerFactory> factoryIter = factoryList.iterator();
            while(factoryIter.hasNext()) {
                InvocationListenerFactory factory  = factoryIter.next();
                InvocationListener listener = factory.createInvocationListener(eic.getRequestMessageContext());
                if(listener != null) {
                    try {
                        listener.notify(bean); 
                    }
                    catch(Exception e) {
                        throw ExceptionFactory.makeWebServiceException(e);
                    }
                    finally {
                        // add this instance so it can be called on the response also
                        eic.addInvocationListener(listener);
                    }
                }
            }
            MessageContext request = eic.getRequestMessageContext();
            request.setProperty(org.apache.axis2.jaxws.spi.Constants.INVOCATION_LISTENER_LIST, 
                                eic.getInvocationListeners());
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
     * we need a HandlerLifecycleManager so we can call the @PreDestroy when we are done with the server-side handler instances
     */
    private HandlerLifecycleManager createHandlerlifecycleManager() {
        HandlerLifecycleManagerFactory elmf = (HandlerLifecycleManagerFactory)FactoryRegistry
                .getFactory(HandlerLifecycleManagerFactory.class);
        return elmf.createHandlerLifecycleManager();
    }
    
    private MessageContext isProtocolMismatch(MessageContext request, MessageContext response){
    	Protocol requestProtocol =request.getMessage().getProtocol();
    	Protocol responseProtocol = response.getMessage().getProtocol();
    	boolean protocolMismatch = false;
    	String msg = null;
    	if(requestProtocol.equals(Protocol.soap11)){
    		if(!responseProtocol.equals(Protocol.soap11)){
    			protocolMismatch = true;
    			msg = "Request SOAP message protocol is version 1.1, but Response SOAP message is configured for SOAP 1.2.  This is not supported.";
    		}
    	}
    	else if(requestProtocol.equals(Protocol.soap12)){
    		if(!responseProtocol.equals(Protocol.soap12)){
    			protocolMismatch = true;
    			msg = "Request SOAP message protocol is version 1.2, but Response SOAP message is configured for SOAP 1.1.  This is not supported.";
    		}
    	}
    	MessageContext msgContext = null;
    	if(protocolMismatch){
    		msgContext = Utils.createFaultMessage(response, msg);
    	}
    	return msgContext;
    }
    
}
