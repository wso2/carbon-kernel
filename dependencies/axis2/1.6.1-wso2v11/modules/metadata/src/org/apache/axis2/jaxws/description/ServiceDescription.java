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

package org.apache.axis2.jaxws.description;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.jaxws.catalog.JAXWSCatalogManager;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.AddressingFeature.Responses;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A ServiceDescription corresponds to a Service under which there can be a collection of enpdoints.
 * In WSDL 1.1 terms, then, a ServiceDescription corresponds to a wsdl:Service under which there are
 * one or more wsdl:Port entries. The ServiceDescription is the root of the metdata abstraction
 * Description hierachy.
 * <p/>
 * The Description hierachy is:
 * <pre>
 * ServiceDescription
 *     EndpointDescription[]
 *         EndpointInterfaceDescription
 *             OperationDescription[]
 *                 ParameterDescription[]
 *                 FaultDescription[]
 * <p/>
 * <b>ServiceDescription details</b>
 * <p/>
 *     CORRESPONDS TO:
 *         On the Client: The JAX-WS Service class or generated subclass.
 * <p/>
 *         On the Server: The Service implementation.  Note that there is a 1..1
 *         correspondence between a ServiceDescription and EndpointDescription
 *         on the server side.
 * <p/>
 *     AXIS2 DELEGATE:      None
 * <p/>
 *     CHILDREN:            1..n EndpointDescription
 * <p/>
 *     ANNOTATIONS:
 *         None
 * <p/>
 *     WSDL ELEMENTS:
 *         service
 * <p/>
 *  </pre>
 */

public interface ServiceDescription {
    
	public abstract EndpointDescription[] getEndpointDescriptions();

    public abstract Collection<EndpointDescription> getEndpointDescriptions_AsCollection();

    public abstract EndpointDescription getEndpointDescription(QName portQName);

    // Called the client-side to retrieve defined and dynamic ports
    public abstract EndpointDescription getEndpointDescription(QName portQName, Object serviceDelegateKey);

    /**
     * Return the EndpointDescriptions corresponding to the SEI class.  Note that Dispatch endpoints
     * will never be returned because they do not have an associated SEI.
     *
     * @param seiClass
     * @return
     */
    public abstract EndpointDescription[] getEndpointDescription(Class seiClass);

    public abstract ConfigurationContext getAxisConfigContext();

    public abstract ServiceClient getServiceClient(QName portQName, Object serviceDelegateKey);

    public abstract QName getServiceQName();

    /**
     * Return the handler chain configuration information as a HandlerChainsType object.  If the
     * key is non-null then it is used to look for handler chain configuration information in the
     * sparse metadata.  The order in which the configuration information is resolved is:
     * 1) Look in sparse composite if the key is not null
     * 2) Look in the composite
     * 3) Look for a HandlerChain annotation and read in the file it specifies  
     * 
     * @param serviceDelegateKey May be null.  If non-null, used to look for service-delegate
     *     specific sparse composite information.
     * @return A HandlerChainsType object or null
     */
    public abstract HandlerChainsType getHandlerChain(Object serviceDelegateKey);
    
    /**
     * Return the handler chain configuration information as a HandlerChainsType object.
     * This is the same as calling getHandlerChain(null).
     * @see #getHandlerChain(Object)
     */
    public abstract HandlerChainsType getHandlerChain();
    
    /**
     * Returns a list of the ports for this serivce.  The ports returned are the - Ports declared
     * ports for this Service.  They can be delcared in the WSDL or via annotations. - Dynamic ports
     * added to the service
     *
     * @param serviceDelegateKey This should always be non-null when called via ServiceDelegate and is
     *                            used to help retrieve dynamic ports per client

     * @return
     */
    public List<QName> getPorts(Object serviceDelegateKey);

    public ServiceRuntimeDescription getServiceRuntimeDesc(String name);

    public void setServiceRuntimeDesc(ServiceRuntimeDescription ord);
    
    public boolean isServerSide();
    
    /**
     * Answer if MTOM is enabled for the service represented by this Service Description.  This
     * is currently only supported on the service-requester side; it is not supported on the 
     * service-provider side.  If the key is non-null, it is used to look up an sparse metadata
     * that may have been specified when the Service Description was created.
     *  
     * @param key If non-null, used to look up any sparse metadata that may have been specified
     *     when the service was created.
     * @return TRUE if mtom was enabled either in the sparse metadata or in the composite; FALSE
     *     othewise.
     */
    public boolean isMTOMEnabled(Object key);
    
    /**
     * Answer if MTOM is enabled for the service represented by this Service Description  
     * and the service-endpoint-interface indicated. This is currently only supported on the 
     * service-requester side; it is not supported on the service-provider side.  If the key is 
     * non-null, it is used to look up an sparse metadata that may have been specified when the 
     * Service Description was created. If the seiClass is non-null it is used to further scope
     * the enablement of MTOM to a specific SEI.
     *  
     * @param key If non-null, used to look up any sparse metadata that may have been specified
     *     when the service was created.
     * @param seiClass Represents client service-endpoint-interface class.
     * 
     * @return TRUE if mtom was enabled either in the sparse metadata or in the composite; FALSE
     *     othewise.
     */
    public boolean isMTOMEnabled(Object key, Class seiClass);
    
    public QName getPreferredPort(Object key);
    
    public JAXWSCatalogManager getCatalogManager();

    /**
     * Answer information for resolved handlers for the given port.  This information is set
     * when the handler resolver initially resolves the handlers based on the handler 
     * configuration information.  It is cached on the service description for performance 
     * so that subsequent queries by other handler resolvers for the same port do not have to
     * re-resolve the information from the handler configuration information.  
     * 
     * @param portInfo Port for which the handler information is desired
     * @return An object containing information for the resolved handlers, or null if no 
     *     information is found in the cache.
     */
    public ResolvedHandlersDescription getResolvedHandlersDescription(PortInfo portInfo);

    /**
     * Cache information for handlers which have been resolved for this port. This information is set
     * when the handler resolver initially resolves the handlers based on the handler 
     * configuration information.  It is cached on the service description for performance 
     * so that subsequent queries by other handler resolvers for the same port do not have to
     * re-resolve the information from the handler configuration information.
     *   
     * @param portInfo Port for which the handler information should be cached
     * @param resolvedHandlersInfo An object containing information for the resolved handlers
     */
    public void setResolvedHandlersDescription(PortInfo portInfo, ResolvedHandlersDescription resolvedHandlersInfo);
    
    /**
     * Check into releasing resources related to this ServiceDescription.  Those resources include
     * this ServiceDescription instance, the EndpointDescription instances it owns and their
     * associated AxisService and related objects.  
     * 
     * NOTE: This should only be called on ServiceDescrpition instances that are owned by
     * client ServiceDelegate instances; it SHOULD NOT be called on server-side 
     * ServiceDescriptions since those are built during server start and their life-cycle is
     * the life-cycle of the server.
     * 
     * @param delegate The ServiceDelegate instance that owns this ServiceDescription.
     */
    public void releaseResources(Object delegate);
        
    /**
     * This method is responsible for querying the metadata for properties associated with
     * a given BindingProvider instance. This is only applicable for the requestor-side, and
     * the properties are scoped at the port level.
     * @param serviceDelegateKey This should always be non-null when called via ServiceDelegate and is
     *                            used to help retrieve dynamic ports per client
     * @param key This should always be non-null and is used to retrieve properties for a given
     *            client-side port
     * @return 
     */
    public Map<String, Object> getBindingProperties(Object serviceDelegateKey, String key);

    /**
     * Return the MTOM Threshold as set by the Client via a sparse composite (such as a client deployment
     * descriptor).
     * 
     * @param serviceDelegateKey The instance of the service delegate related to this service
     * @param seiClass The SEI for the port to retrieve the MTOM threshold for
     * @return the MTOM threshold if set, or 0 if not set.
     */
    public int getMTOMThreshold(Object serviceDelegateKey, Class seiClass);

    /**
     * Return whether RespectBinding is enabled as set by the Client via a sparse composite (such as a client deployment
     * descriptor).
     * 
     * @param serviceDelegateKey The instance of the service delegate related to this service
     * @param seiClass The SEI for the port to retrieve the RespectBinding setting for.
     * @return true if RespectBinding is enabled; false otherwise.
     */
    public abstract boolean isRespectBindingEnabled(Object serviceDelegateKey, Class seiClass);

    /**
     * Answer whether Addressing was explicitly configured via metadata (such as a deployment descriptor) on the
     * service-requester.  Note that the related methods will return default values if Addressing was not explicitly
     * configured; otherwise they will return values set based on the metadata configuration.
     * @see #isAddressingEnabled(Object, Class)
     * @see #isAddressingRequired(Object, Class)
     * @see #getAddressingResponses(Object, Class)
     * @param serviceDelegateKey The instance of the service delegate related to this service
     * @param seiClass The SEI for the port to retrieve the setting for.
     * @return true if Addressing was explicitly set via metadata, false otherwise.
     */
    public abstract boolean isAddressingConfigured(Object serviceDelegateKey, Class seiClass);

    /**
     * Answer whether Addressing is enabled on the service-requester.
     * Note that if addressing was not configured via metadata, then this method will return a default value,
     * otherwise it will return the value configured via metadata.
     * @see #isAddressingConfigured(Object, Class)
     * @param serviceDelegateKey The instance of the service delegate related to this service
     * @param seiClass The SEI for the port to retrieve the setting for.
     * @return true if Addressing is enabled on the service-requester, false (default) otherwise 
     */
    public abstract boolean isAddressingEnabled(Object serviceDelegateKey, Class seiClass);

    /**
     * Answer whether Addressing is required on the service-requester.
     * Note that if addressing was not configured via metadata, then this method will return a default value,
     * otherwise it will return the value configured via metadata.
     * @see #isAddressingConfigured(Object, Class)
     * @param serviceDelegateKey The instance of the service delegate related to this service
     * @param seiClass The SEI for the port to retrieve the setting for.
     * @return true if Addressing is required on the service-requester, false (default) otherwise 
     */
    public abstract boolean isAddressingRequired(Object serviceDelegateKey, Class seiClass);

    /**
     * Answer the type of Addressing responses required by the service-requester.
     * Note that if addressing was not configured via metadata, then this method will return a default value,
     * otherwise it will return the value configured via metadata.
     * @see #isAddressingConfigured(Object, Class)
     * @param serviceDelegateKey The instance of the service delegate related to this service
     * @param seiClass The SEI for the port to retrieve the setting for.
     * @return AddressingFeature.Responses vale corresponding to the type of responses required by service-requester. 
     */
    public abstract Responses getAddressingResponses(Object serviceDelegateKey, Class seiClass);

}
