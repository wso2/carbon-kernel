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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.common.config.WSDLValidatorElement;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;

import java.util.List;
import java.util.Set;

/**
 * An EndpointDescription corresponds to a particular Service Implementation. It can correspond to
 * either either a client to that impl or the actual service impl.
 * <p/>
 * The EndpointDescription contains information that is relevant to both a Provider-based and
 * SEI-based (aka Endpoint-based or Java-based) enpdoints. SEI-based endpoints (whether they have an
 * explicit or implcit SEI) will have addtional metadata information in an
 * EndpointInterfaceDescription class and sub-hierachy; Provider-based endpoitns do not have such a
 * hierachy.
 * <p/>
 * <pre>
 * <b>EndpointDescription details</b>
 * <p/>
 *     CORRESPONDS TO:      The endpoint (both Client and Server)
 * <p/>
 *     AXIS2 DELEGATE:      AxisService
 * <p/>
 *     CHILDREN:            0..1 EndpointInterfaceDescription
 * <p/>
 *     ANNOTATIONS:
 *         WebService [181]
 *         WebServiceProvider [224]
 *             ServicMode [224]
 *         BindingType [224]
 * <p/>
 *     WSDL ELEMENTS:
 *         port
 * <p/>
 *  </pre>
 */

public interface EndpointDescription {

    public static final String AXIS_SERVICE_PARAMETER =
            "org.apache.axis2.jaxws.description.EndpointDescription";
    public static final String DEFAULT_CLIENT_BINDING_ID = SOAPBinding.SOAP11HTTP_BINDING;

    /**
     * Paramater set on AxisService which contains an ArrayList of SOAP header QNames
     * of SOAPHandlers. 
     */
    public static final String HANDLER_PARAMETER_QNAMES = "org.apache.axis2.jaxws.description.EndpointDescription.handlerParameterQNames";
        
    public abstract AxisService getAxisService();

    public abstract ServiceClient getServiceClient();

    public abstract ServiceDescription getServiceDescription();

    public abstract EndpointInterfaceDescription getEndpointInterfaceDescription();

    /**
     * Returns the JAX-WS handler PortInfo object for this endpoint.
     *
     * @return PortInfo
     */
    public abstract PortInfo getPortInfo();

    public abstract boolean isProviderBased();

    public abstract boolean isEndpointBased();

    public abstract String getName();

    public abstract String getTargetNamespace();

    /**
     * Returns the binding type FOR A SERVER.  This is based on the BindingType annotation and/or
     * the WSDL. This will return the default binding (SOAP11) if no annotation was specified on the
     * server. This should NOT be called on the client since it will always return the default
     * binding. Use getClientBindingID() on clients.
     *
     * @return
     */
    public abstract String getBindingType();

    public abstract void setHandlerChain(HandlerChainsType handlerChain);
    
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
     * Set the binding type FOR A CLIENT.  The BindingType annotation is not valid on the client per
     * the JAX-WS spec.  The value can be set via addPort(...) for a Dispatch client or via TBD for
     * a Proxy client.
     */
    public abstract void setClientBindingID(String clientBindingID);

    /**
     * Return the binding type FOR A CLIENT.  This will return the default client binding type if
     * called on the server.  Use getBindingType() on servers.
     *
     * @return String representing the client binding type
     * @see setClientBindingID();
     */
    public abstract String getClientBindingID();

    public void setEndpointAddress(String endpointAddress);

    public abstract String getEndpointAddress();

    //public abstract List<String> getHandlerList();
    public abstract QName getPortQName();

    public abstract QName getServiceQName();

    public abstract Service.Mode getServiceMode();
    
    /**
     * Signals whether or not MTOM has been turned on for the endpoint 
     * based on the annotation configuration.
     * 
     * Both the @MTOM and @BindingType are inspected.  The @MTOM
     * annotation is inspected first.  If the @MTOM
     * annotation is not present, then the @BindingType is inspected.
     * 
     * @return a boolean value 
     */
    public boolean isMTOMEnabled();
    
    /**
     * If MTOM is enabled, returns the threshold value.
     * 
     * @return -1 if MTOM is not enabled, a positive integer value if 
     * one was configured.
     */
    public int getMTOMThreshold();
    
    /**
     * Returns true if the contents of the <code>&lt;wsdl:binding&gt;</code> must be 
     * strictly respected by the runtime.
     * 
     * @return a boolean value
     */
    public boolean respectBinding();
    
    /**
     * Indicate whether or not strict binding support should be used.
     */
    public void setRespectBinding(boolean respect);
    
    /**
     * Adds the QName to a list of binding types that are required to be
     * supported by the endpoint as defined in the WSDL.
     * 
     * @param name
     * @return
     */
    public boolean addRequiredBinding(WSDLValidatorElement element);
    
    /**
     * Returns a list of all known bindings that should be supported based
     * on the information in the WSDL.
     * 
     * @return
     */
    public Set<WSDLValidatorElement> getRequiredBindings();
    
    /**
     * Return the DescriptionBuilderComposite, if any, used to build this service description.
     * @return
     */
    public DescriptionBuilderComposite getDescriptionBuilderComposite();
    
    /**
     * Return the Object that corresponds to the property key supplied.
     */
    public Object getProperty(String key);
    
    /**
     * Store the property by the key specified.
     */
    public void setProperty(String key, Object value);
    
}