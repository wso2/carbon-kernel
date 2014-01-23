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

package org.apache.axis2.jaxws.server.endpoint.lifecycle.impl;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.context.WebServiceContextImpl;
import org.apache.axis2.jaxws.context.factory.MessageContextFactory;
import org.apache.axis2.jaxws.context.utils.ContextUtils;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.handler.SoapMessageContext;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.injection.ResourceInjectionException;
import org.apache.axis2.jaxws.lifecycle.BaseLifecycleManager;
import org.apache.axis2.jaxws.lifecycle.LifecycleException;
import org.apache.axis2.jaxws.runtime.description.injection.ResourceInjectionServiceRuntimeDescription;
import org.apache.axis2.jaxws.runtime.description.injection.ResourceInjectionServiceRuntimeDescriptionFactory;
import org.apache.axis2.jaxws.server.endpoint.injection.ResourceInjector;
import org.apache.axis2.jaxws.server.endpoint.injection.WebServiceContextInjector;
import org.apache.axis2.jaxws.server.endpoint.injection.factory.ResourceInjectionFactory;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.EndpointLifecycleManager;
import org.apache.axis2.util.Loader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.WebServiceContext;
import java.lang.reflect.Method;

public class EndpointLifecycleManagerImpl extends BaseLifecycleManager implements
        EndpointLifecycleManager {
    public static final String WEBSERVICE_MESSAGE_CONTEXT = "javax.xml.ws.WebServiceContext";
    private static final Log log = LogFactory.getLog(EndpointLifecycleManagerImpl.class);

    public EndpointLifecycleManagerImpl(Object endpointInstance) {
        this.instance = endpointInstance;
    }

    public EndpointLifecycleManagerImpl() {
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.server.endpoint.lifecycle.EndpointLifecycleManager#createServiceInstance(org.apache.axis2.jaxws.core.MessageContext, java.lang.Class)
      */
    public Object createServiceInstance(MessageContext mc, Class serviceImplClass)
                                                                                  throws LifecycleException,
                                                                                  ResourceInjectionException {
        org.apache.axis2.context.MessageContext msgContext = mc.getAxisMessageContext();

        // Get the ServiceDescription and injectionDesc which contain
        // cached information
        ServiceDescription serviceDesc = mc.getEndpointDescription().getServiceDescription();
        ResourceInjectionServiceRuntimeDescription injectionDesc =
                getInjectionDesc(serviceDesc, serviceImplClass);


        Object serviceimpl = retrieveServiceInstance(mc);
        if (serviceimpl != null) {
            this.instance = serviceimpl;

            if (log.isDebugEnabled()) {
                log.debug("Service Instance found in the service context, reusing the instance");
            }

            // If resource injection is needed, create the SOAPMessageContext and update the WebServiceContext
            // Create MessageContext for current invocation.
            if (hasResourceAnnotation(injectionDesc)) {
                performWebServiceContextUpdate(mc);
            }

            //since service impl is there in service context , take that from there
            return serviceimpl;
        } else {
            // create a new service impl class for that service
            serviceimpl = createServiceInstance(msgContext.getAxisService(), serviceImplClass);
            this.instance = serviceimpl;

            if (log.isDebugEnabled()) {
                log.debug("New Service Instance created");
            }

            // If resource injection is needed, create the SOAPMessageContext and build the WebServiceContext
            // Create MessageContext for current invocation.
            if (hasResourceAnnotation(injectionDesc)) {
                performWebServiceContextInjection(mc, serviceimpl);
            }


            //Invoke PostConstruct
            if (injectionDesc != null && injectionDesc.getPostConstructMethod() != null) {
                invokePostConstruct(injectionDesc.getPostConstructMethod());
            }
            ServiceContext serviceContext = msgContext.getServiceContext();
            serviceContext.setProperty(ServiceContext.SERVICE_OBJECT, serviceimpl);
            return serviceimpl;
        }
    }

    /**
     * This method locates an existing service implementation instance if
     * one has been previously stored away.
     */
    protected Object retrieveServiceInstance(MessageContext mc) {
        Object instance = null;
        ServiceContext serviceContext = mc.getAxisMessageContext().getServiceContext();
        instance = serviceContext.getProperty(ServiceContext.SERVICE_OBJECT);
        return instance;
    }

    /**
     * This method will provide the necessary function in order to inject
     * a WebServiceContext instance on a member of the service implementation class.
     */
    protected void performWebServiceContextInjection(MessageContext mc, Object serviceImpl)
                                                                                           throws ResourceInjectionException {
        WebServiceContext wsContext = createWebServiceContext(mc);
        // Inject WebServiceContext
        injectWebServiceContext(mc, wsContext, serviceImpl);
        saveWebServiceContext(mc, wsContext);
    }

    /**
     * This method will provide the necessary function in order to update
     * an existing WebServiceContext instance with a MessageContext for
     * the current request.
     */
    protected void performWebServiceContextUpdate(MessageContext mc)
                                                                    throws ResourceInjectionException {
        javax.xml.ws.handler.MessageContext soapMessageContext = createSOAPMessageContext(mc);
        ServiceContext serviceContext = mc.getAxisMessageContext().getServiceContext();

        //Get WebServiceContext from ServiceContext
        WebServiceContext ws =
                (WebServiceContext) serviceContext.getProperty(WEBSERVICE_MESSAGE_CONTEXT);

        //Add the MessageContext for current invocation
        if (ws != null) {
            updateWebServiceContext(ws, soapMessageContext);
            // Store the WebServiceContext on the MessageContext so that its resource
            // can be freed after the web service method completes.
            mc.setProperty(WEBSERVICE_MESSAGE_CONTEXT, ws);
        }
    }

    /**
     * This method will provide the function necessary to save the WebServiceContext
     * associated with the endpoint.
     */
    protected void saveWebServiceContext(MessageContext mc, WebServiceContext wsContext) {
        ServiceContext sc = mc.getAxisMessageContext().getServiceContext();
        sc.setProperty(WEBSERVICE_MESSAGE_CONTEXT, wsContext);
    }

    /**
     * This method will be responsible for creating an instance of a WebServiceContext
     * and initializing the instance with a MessageContext.
     */
    protected WebServiceContext createWebServiceContext(MessageContext mc) {
        javax.xml.ws.handler.MessageContext soapMessageContext = createSOAPMessageContext(mc);
        // Create WebServiceContext
        WebServiceContextImpl wsContext = new WebServiceContextImpl();
        //Add MessageContext for this request.
        wsContext.setSoapMessageContext(soapMessageContext);
        
        // Store the WebServiceContext on the MessageContext so that its resource
        // can be freed after the web service method completes.
        mc.setProperty(WEBSERVICE_MESSAGE_CONTEXT, wsContext);
        return wsContext;
    }

    /**
     * This method will retrieve a ResourceInjectionServiceRuntimeDescription if one
     * is associated with the current ServiceDescription.
     */
    protected ResourceInjectionServiceRuntimeDescription getInjectionDesc(
                                                                          ServiceDescription serviceDesc,
                                                                          Class serviceImplClass) {
        ResourceInjectionServiceRuntimeDescription injectionDesc = null;
        if (serviceDesc != null) {
            injectionDesc =
                    ResourceInjectionServiceRuntimeDescriptionFactory.get(serviceDesc,
                                                                          serviceImplClass);
        }

        return injectionDesc;
    }

    /**
     * This method indicates whether or not we need to perform WebServiceContext injection
     * on a field within our endpoint instance.
     */
    protected boolean hasResourceAnnotation(ResourceInjectionServiceRuntimeDescription injectionDesc) {
        return (injectionDesc != null && injectionDesc.hasResourceAnnotation());
    }

    private Object createServiceInstance(AxisService service, Class serviceImplClass) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new instance of service endpoint");
        }

        if (serviceImplClass == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("EndpointControllerErr5"));
        }

        Object instance = null;
        try {
            // allow alternative definition of makeNewServiceObject
            if (service != null && service.getParameter(Constants.SERVICE_OBJECT_SUPPLIER) != null) {
                ClassLoader classLoader = service.getClassLoader();

                Parameter serviceObjectParam =
                        service.getParameter(Constants.SERVICE_OBJECT_SUPPLIER);
                Class serviceObjectMaker =
                        Loader.loadClass(classLoader,
                                         ((String) serviceObjectParam.getValue()).trim());

                // Find static getServiceObject() method, call it if there
                Method method =
                        serviceObjectMaker.getMethod("getServiceObject",
                                                     new Class[] { AxisService.class });
                if (method != null) {
                    return method.invoke(serviceObjectMaker.newInstance(), new Object[] { service });
                }
            }
            instance = serviceImplClass.newInstance();
        } catch (IllegalAccessException e) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("EndpointControllerErr6",
                                                                               serviceImplClass.getName()));
        } catch (InstantiationException e) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("EndpointControllerErr6",
                                                                               serviceImplClass.getName()));
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("EndpointControllerErr6",
                                                                               serviceImplClass.getName()));
        }

        return instance;
    }

    protected javax.xml.ws.handler.MessageContext createSOAPMessageContext(MessageContext mc) {
        SoapMessageContext soapMessageContext =
                (SoapMessageContext) MessageContextFactory.createSoapMessageContext(mc);
        return soapMessageContext;
    }

    protected void injectWebServiceContext(MessageContext mc, WebServiceContext wsContext,
                                           Object serviceInstance)
                                                                  throws ResourceInjectionException {
        ResourceInjector ri =
                ResourceInjectionFactory.createResourceInjector(WebServiceContext.class);
        ri.inject(wsContext, serviceInstance);
    }

    protected void updateWebServiceContext(WebServiceContext wsContext,
                                           javax.xml.ws.handler.MessageContext soapMessageContext)
                                                                                                  throws ResourceInjectionException {
        WebServiceContextInjector wci =
                (WebServiceContextInjector) ResourceInjectionFactory.createResourceInjector(WebServiceContextInjector.class);
        wci.addMessageContext(wsContext, soapMessageContext);

    }

}
