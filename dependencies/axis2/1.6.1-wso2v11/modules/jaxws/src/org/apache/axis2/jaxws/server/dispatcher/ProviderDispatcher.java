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

import org.apache.axis2.Constants;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.context.listener.ContextListenerUtils;
import org.apache.axis2.jaxws.context.utils.ContextUtils;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.impl.alt.MethodMarshallerUtils;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.databinding.OMBlock;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.DataSourceBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.OMBlockFactory;
import org.apache.axis2.jaxws.message.factory.SOAPEnvelopeBlockFactory;
import org.apache.axis2.jaxws.message.factory.SourceBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.message.util.XMLFaultUtils;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.EndpointCallback;
import org.apache.axis2.jaxws.server.EndpointInvocationContext;
import org.apache.axis2.jaxws.server.InvocationHelper;
import org.apache.axis2.jaxws.server.ServerConstants;
import org.apache.axis2.jaxws.utility.ClassUtils;
import org.apache.axis2.jaxws.utility.DataSourceFormatter;
import org.apache.axis2.jaxws.utility.ExecutorFactory;
import org.apache.axis2.jaxws.utility.SingleThreadedExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;

import javax.activation.DataSource;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

/**
 * The ProviderDispatcher is used to invoke instances of a target endpoint that implement the {@link
 * javax.xml.ws.Provider} interface.
 * <p/>
 * The Provider<T> is a generic class, with certain restrictions on the parameterized type T.  This
 * implementation supports the following types:
 * <p/>
 * java.lang.String javax.activation.DataSource javax.xml.soap.SOAPMessage
 * javax.xml.transform.Source
 */
public class ProviderDispatcher extends JavaDispatcher {

    private static Log log = LogFactory.getLog(ProviderDispatcher.class);

    private BlockFactory _blockFactory = null;  // Cache the block factory
    private Class _providerType = null;        // Cache the provider type
    private Provider providerInstance = null;
    private Message message = null;
    private EndpointDescription endpointDesc = null;

    /**
     * Constructor
     *
     * @param _class
     * @param serviceInstance
     */
    public ProviderDispatcher(Class _class, Object serviceInstance) {
        super(_class, serviceInstance);
    }

    /* (non-Javadoc)
    * @see org.apache.axis2.jaxws.server.EndpointDispatcher#execute()
    */
    public MessageContext invoke(MessageContext request) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Preparing to invoke javax.xml.ws.Provider based endpoint");
            log.debug("Invocation pattern: two way, sync");
        }

        initialize(request);

        providerInstance = getProviderInstance();

        Object param = createRequestParameters(request);

        if (log.isDebugEnabled()) {
            Class providerType = getProviderType();
            final Object input = providerType.cast(param);
            log.debug("Invoking Provider<" + providerType.getName() + ">");
            if (input != null) {
                log.debug("Parameter type: " + input.getClass().getName());
            }
            else {
                log.debug("Parameter is NULL");
            }
        }

        // Invoke the actual Provider.invoke() method
        boolean faultThrown = false;
        Throwable fault = null;
        Object[] input = new Object[] {param};
        Object responseParamValue = null;
        Method target = null;
        try {
            target = getJavaMethod();
            responseParamValue = invokeTargetOperation(target, input);
        } catch (Throwable e) {
            fault = ClassUtils.getRootCause(e);
            faultThrown = true;
        }
        
        // Create the response MessageContext
        MessageContext responseMsgCtx = null;
        if (faultThrown) {
            // If a fault was thrown, we need to create a slightly different
            // MessageContext, than in the response path.
            responseMsgCtx = createFaultResponse(request, fault);
            setExceptionProperties(responseMsgCtx, target, fault);
        } else if(responseParamValue == null && 
                MessageContextUtils.getJaxwsProviderInterpretNullOneway(request) &&
                !request.getAxisMessageContext().getAxisService().isWsdlFound()) {
            // If the Provider returned null, we will interpret this as a one-way op if
            // - the custom property jaxws.provider.interpretNullAsOneway is true AND
            // - the operation was NOT wsdl defined.   
            if (log.isDebugEnabled()) {
                log.debug("detected null return from Provider, " + target + 
                        "and operation is not wsdl defined and " +
                "interpretNullAsOneway property is true.");
            }
            // JAXWS 2.2 If Provider returns null, an empty response is given (no SOAPEnvelope)
            responseMsgCtx = null; // return null, JAXWSMessageReceiver will interpret as one-way

        } else {
            responseMsgCtx = createResponse(request, input, responseParamValue);
        }

        return responseMsgCtx;
    }
    
    public void invokeOneWay(MessageContext request) {
        if (log.isDebugEnabled()) {
            log.debug("Preparing to invoke javax.xml.ws.Provider based endpoint");
            log.debug("Invocation pattern: one way");
        }
        
        initialize(request);

        providerInstance = getProviderInstance();

        Object param = createRequestParameters(request);

        if (log.isDebugEnabled()) {
            Class providerType = getProviderType();
            final Object input = providerType.cast(param);
            log.debug("Invoking Provider<" + providerType.getName() + ">");
            if (input != null) {
                log.debug("Parameter type: " + input.getClass().getName());
            }
            else {
                log.debug("Parameter is NULL");
            }
        }

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
        
        Method m = getJavaMethod();
        Object[] params = new Object[] {param};
        
        EndpointInvocationContext eic = (EndpointInvocationContext) request.getInvocationContext();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        AsyncInvocationWorker worker = new AsyncInvocationWorker(m, params, cl, eic);
        FutureTask task = new FutureTask<AsyncInvocationWorker>(worker);
        executor.execute(task);
        
        return;
    }
    
    public void invokeAsync(MessageContext request, EndpointCallback callback) {
        if (log.isDebugEnabled()) {
            log.debug("Preparing to invoke javax.xml.ws.Provider based endpoint");
            log.debug("Invocation pattern: two way, async");
        }
        
        initialize(request);

        providerInstance = getProviderInstance();

        Object param = createRequestParameters(request);

        if (log.isDebugEnabled()) {
            Class providerType = getProviderType();
            final Object input = providerType.cast(param);
            log.debug("Invoking Provider<" + providerType.getName() + ">");
            if (input != null) {
                log.debug("Parameter type: " + input.getClass().getName());
            }
            else {
                log.debug("Parameter is NULL");
            }
        }

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
        
        Method m = getJavaMethod();
        Object[] params = new Object[] {param};
        
        EndpointInvocationContext eic = (EndpointInvocationContext) request.getInvocationContext();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        AsyncInvocationWorker worker = new AsyncInvocationWorker(m, params, cl, eic);
        FutureTask task = new FutureTask<AsyncInvocationWorker>(worker);
        executor.execute(task);
        
        return;
    }
    
    public Object createRequestParameters(MessageContext request) {
        // First we need to know what kind of Provider instance we're going
        // to be invoking against
        Class providerType = getProviderType();

        // REVIEW: This assumes there is only one endpoint description on the service.  Is that always the case?
        EndpointDescription endpointDesc = request.getEndpointDescription();

        // Now that we know what kind of Provider we have, we can create the 
        // right type of Block for the request parameter data
        Object requestParamValue = null;
        Message message = request.getMessage();
        if (message != null) {
            
            // Enable MTOM if indicated
            if (endpointDesc.isMTOMEnabled()) {
                message.setMTOMEnabled(true);
            }

            // Save off the protocol info so we can use it when creating the response message.
            Protocol messageProtocol = message.getProtocol();
            // Determine what type blocks we want to create (String, Source, etc) based on Provider Type
            BlockFactory factory = createBlockFactory(providerType);


            Service.Mode providerServiceMode = endpointDesc.getServiceMode();

            if (providerServiceMode != null && providerServiceMode == Service.Mode.MESSAGE) {
                if (log.isDebugEnabled()) {
                    log.debug("Provider type is " + providerType.getClass().getName());
                }
                if (providerType.equals(SOAPMessage.class)) {
                    // We can get the SOAPMessage directly from the message itself
                    if (log.isDebugEnabled()) {
                        log.debug("Number Message attachments=" + message.getAttachmentIDs().size());
                    }
                }
                if (providerType.equals(OMElement.class)) {
                    //Register the ContextListener for performance.
                    ContextListenerUtils.registerProviderOMListener(request);
                    // TODO avoid call to message.getValue due to
                    // current unnecessary message transformation in
                    // message.getValue.  Once message.getValue is fixed,
                    // this code block is unnecessary, though no harm
                    // will come if it remains.  rott
                    requestParamValue = message.getAsOMElement();
                } else {
                    requestParamValue = message.getValue(null, factory);
                }
                if (requestParamValue == null) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "There are no elements to unmarshal.  ProviderDispatch will pass a null as input");
                    }
                }
            } else {
                // If it is not MESSAGE, then it is PAYLOAD (which is the default); only work with the body 
                Block block = message.getBodyBlock(null, factory);
                if (block != null) {
                    try {
                        requestParamValue = block.getBusinessObject(true);
                        if (requestParamValue instanceof OMBlock) {
                            // Provider<OMBlock> is not supported, so we need to get the OMElement out
                            if (log.isDebugEnabled()) {
                                log.debug("request parameter business object is OMBlock.  Now retrieving OMElement from OMBlock.");
                            }
                            requestParamValue = ((OMBlock)requestParamValue).getOMElement();
                        }
                    } catch (WebServiceException e) {
                        throw ExceptionFactory.makeWebServiceException(e);
                    } catch (XMLStreamException e) {
                        throw ExceptionFactory.makeWebServiceException(e);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "No body blocks in SOAPMessage, Calling provider method with null input parameters");
                    }
                    requestParamValue = null;
                }
            }
        }
        
        
        return requestParamValue;
    }
    
    /**
     * Create a MessageContext for the response. This could be a normal response
     * or a fault response depending on the characteristics of output
     * @param request MessageContext
     * @param input[] input Objects
     * @param output Object representing output of Provider
     * @return MessageContext for normal or fault path
     */
    public MessageContext createResponse(MessageContext request, Object[] input, Object output) {
        if (log.isDebugEnabled()) {
            log.debug("Start createResponse");
        }
        Message m;
        EndpointDescription endpointDesc = null;
        try {
            endpointDesc = request.getEndpointDescription();
            Service.Mode mode = endpointDesc.getServiceMode();
            m = createMessageFromValue(output, request.getMessage().getProtocol(), mode);
        } catch (Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug("Throwable caught");
                log.debug("Throwable=" + t);
            }
            throw ExceptionFactory.makeWebServiceException(t);
        }

        
        MessageContext response = null;
        try {
            // Enable MTOM if indicated 
            if (endpointDesc.isMTOMEnabled()) {
                m.setMTOMEnabled(true);
            }
            
            if (!m.isFault()) {
                if (log.isDebugEnabled()) {
                    log.debug("Non-Fault Response MessageContext is created.");
                }
                response = MessageContextUtils.createResponseMessageContext(request);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Fault Response MessageContext is created.");
                }
                response = MessageContextUtils.createFaultMessageContext(request);
            }
            initMessageContext(response, m, output);
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Throwable caught creating Response MessageContext");
                log.debug("Throwable=" + e);
            }
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("End createResponse");
            }
        }
        
        return response;
    }
    
    protected void initMessageContext(MessageContext responseMsgCtx, Message m, Object output) {
        responseMsgCtx.setMessage(m);
        if(output instanceof DataSource){
            responseMsgCtx.setProperty(Constants.Configuration.MESSAGE_FORMATTER, 
                    new DataSourceFormatter(((DataSource)output).getContentType()));
        }
    }
    
    public MessageContext createFaultResponse(MessageContext request, Throwable fault) {
        if (log.isDebugEnabled()) {
            log.debug("Create XMLFault for createFaultResponse");
        }
        
        // call the InvocationListener instances before marshalling
        // the fault into a message
        Throwable faultMessage = InvocationHelper.determineMappedException(fault, request);
        if(faultMessage != null) {
            fault = faultMessage;
        }
        
        Message m;
        try {
            EndpointDescription endpointDesc = request.getEndpointDescription();
            Service.Mode mode = endpointDesc.getServiceMode();
            XMLFault xmlFault = MethodMarshallerUtils.createXMLFaultFromSystemException(fault);
            m = createMessageFromValue(xmlFault, request.getMessage().getProtocol(), mode);
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
        MessageContext response = MessageContextUtils.createFaultMessageContext(request);
        response.setMessage(m);
        
        return response;
    }
    
    /**
     * Get the endpoint provider instance
     *
     * @return Provider
     * @throws Exception
     */
    public Provider getProvider() throws Exception {
        Provider p = getProviderInstance();
        setProvider(p);
        return p;
    }

    /**
     * Set the endpoint provider instance
     *
     * @param _provider
     */
    public void setProvider(Provider _provider) {
        this.providerInstance = _provider;
    }

    /**
     * Get the parameter for a given endpoint invocation
     *
     * @return
     * @throws Exception
     */
    public Message getMessage() throws Exception {
        return message;
    }

    /**
     * Set the parameter for a given endpoint invocation
     *
     * @param msg
     */
    public void setMessage(Message msg) {
        this.message = msg;
    }

    /*
    * Create a Message object out of the value object that was returned.
    */
    private Message createMessageFromValue(Object value, Protocol protocol, 
                                           Service.Mode mode) throws Exception {
        MessageFactory msgFactory =
                (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
        Message message = null;

        if (value != null) {
            Class providerType = getProviderType();
            BlockFactory factory = createBlockFactory(providerType);

            if (value instanceof XMLFault) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating message from XMLFault");
                }
                message = msgFactory.create(protocol);
                message.setXMLFault((XMLFault)value);
            } else if (mode != null && mode == Service.Mode.MESSAGE) {
                // For MESSAGE mode, work with the entire message, Headers and Body
                // This is based on logic in org.apache.axis2.jaxws.client.XMLDispatch.createMessageFromBundle()
                if (value instanceof SOAPMessage) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating message from SOAPMessage");
                    }
                    message = msgFactory.createFrom((SOAPMessage)value);
                } else if (value instanceof SOAPEnvelope) {
                    // The value from the provider is already an SOAPEnvelope OMElement, so
                    // it doesn't need to be parsed into one.
                    if (log.isDebugEnabled()) {
                        log.debug("Creating message from OMElement");
                    }
                    message = msgFactory.createFrom((SOAPEnvelope) value, protocol);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating message using " + factory);
                    }
                    Block block = factory.createFrom(value, null, null);
                    message = msgFactory.createFrom(block, null, protocol);
                }
            } else {
                // PAYLOAD mode deals only with the body of the message.
                if (log.isDebugEnabled()) {
                    log.debug("Creating message (payload) using " + factory);
                }
                Block block = factory.createFrom(value, null, null);
                message = msgFactory.create(protocol);
                
                if (XMLFaultUtils.containsFault(block)) {
                    if (log.isDebugEnabled()) {
                        log.debug("The response block created contained a fault.  Converting to an XMLFault object.");
                    }
                    // If the Provider returned a fault, then let's correct the output and 
                    // put an XMLFault on the Message.  This makes it easier for downstream 
                    // consumers to get the SOAPFault from the OM SOAPEnvelope.
                    XMLFault fault = XMLFaultUtils.createXMLFault(block, message.getProtocol());
                    message.setXMLFault(fault);
                }
                else {
                    message.setBodyBlock(block);
                }
            }
        }

        if (message == null) {
            // If we didn't create a message above (because there was no value), create one here
            message = msgFactory.create(protocol);
        }


        return message;
    }

    /*
      * Determine the Provider type for this instance
      */
    private Provider getProviderInstance() {
        Class<?> clazz = getProviderType();

        if (!isValidProviderType(clazz)) {
            // TODO This will change once deployment code it in place
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("InvalidProvider", clazz.getName()));
        }

        Provider provider = null;
        if (clazz == String.class) {
            provider = (Provider<String>)serviceInstance;
        } else if (clazz == Source.class) {
            provider = (Provider<Source>)serviceInstance;
        } else if (clazz == SOAPMessage.class) {
            provider = (Provider<SOAPMessage>)serviceInstance;
        } else if (clazz == DataSource.class) {
            provider = (Provider<DataSource>)serviceInstance;
        } else if (clazz == OMElement.class) {
            provider = (Provider<OMElement>)serviceInstance;
        }

        if (provider == null) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("InvalidProviderCreate", clazz.getName()));
        }

        return provider;

    }

    /*
     * Get the provider type from a given implemention class instance
     */
    private Class<?> getProviderType() {

        if (_providerType != null) {
            return _providerType;
        }
        Class providerType = null;

        Type[] giTypes = serviceImplClass.getGenericInterfaces();
        for (Type giType : giTypes) {
            ParameterizedType paramType = null;
            try {
                paramType = (ParameterizedType)giType;
            } catch (ClassCastException e) {
                // this may not be a parameterized interface
                continue;
            }
            Class interfaceName = (Class)paramType.getRawType();

            if (interfaceName == javax.xml.ws.Provider.class) {
                if (paramType.getActualTypeArguments().length > 1) {
                    throw ExceptionFactory.makeWebServiceException(Messages.getMessage("pTypeErr"));
                }
                providerType = (Class)paramType.getActualTypeArguments()[0];
            }
        }
        _providerType = providerType;
        return providerType;
    }

    /*
    * Validate whether or not the Class passed in is a valid type of
    * javax.xml.ws.Provider<T>.  Per the JAX-WS 2.0 specification, the
    * parameterized type of a Provider can only be:
    *
    *   javax.xml.transform.Source
    *   javax.xml.soap.SOAPMessage
    *   javax.activation.DataSource
    *   org.apache.axiom.om.OMElement
    *
    * We've also added support for String types which is NOT dictated
    * by the spec.
    */
    private boolean isValidProviderType(Class clazz) {
        boolean valid = clazz == String.class ||
                clazz == SOAPMessage.class ||
                clazz == Source.class ||
                clazz == DataSource.class ||
                clazz == OMElement.class;

        if (!valid) {
            if (log.isDebugEnabled()) {
                log.debug("Class " + clazz.getName() + " is not a valid Provider<T> type");
            }
        }

        return valid;
    }

    /*
    * Given a target class type for a payload, load the appropriate BlockFactory.
    */
    private BlockFactory createBlockFactory(Class type) {
        if (_blockFactory != null) {
            return _blockFactory;
        }

        if (type.equals(String.class)) {
            _blockFactory = (XMLStringBlockFactory)FactoryRegistry.getFactory(
                    XMLStringBlockFactory.class);
        } else if (type.equals(DataSource.class)) {
            _blockFactory = (DataSourceBlockFactory)FactoryRegistry.getFactory(
                    DataSourceBlockFactory.class);
        } else if (type.equals(Source.class)) {
            _blockFactory = (SourceBlockFactory)FactoryRegistry.getFactory(
                    SourceBlockFactory.class);
        } else if (type.equals(SOAPMessage.class)) {
            _blockFactory = (SOAPEnvelopeBlockFactory)FactoryRegistry.getFactory(
                    SOAPEnvelopeBlockFactory.class);
        } else if (type.equals(OMElement.class)) {
            _blockFactory = (OMBlockFactory)FactoryRegistry.getFactory(
                    OMBlockFactory.class);
        } else {
            throw ExceptionFactory.makeWebServiceException(
            		Messages.getMessage("bFactoryErr",type.getName()));
        }

        return _blockFactory;
    }
    
    protected Method getJavaMethod() {
        Method m = null;
        try {
            m = providerInstance.getClass().getMethod("invoke", new Class[] {getProviderType()});
        } catch (SecurityException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        } catch (NoSuchMethodException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
        return m;
    }
    
    protected void initialize(MessageContext mc) {

        mc.setOperationName(mc.getAxisMessageContext().getAxisOperation().getName());

        endpointDesc = mc.getEndpointDescription();
        if (endpointDesc.isMTOMEnabled()) {
            mc.getMessage().setMTOMEnabled(true);
        }

        //Set SOAP Operation Related properties in SOAPMessageContext.

        ContextUtils.addWSDLProperties_provider(mc);
    }


}
