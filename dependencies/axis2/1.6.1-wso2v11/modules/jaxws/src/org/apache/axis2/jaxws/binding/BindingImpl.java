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

package org.apache.axis2.jaxws.binding;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.feature.ClientConfigurator;
import org.apache.axis2.jaxws.feature.ClientFramework;
import org.apache.axis2.jaxws.handler.HandlerResolverImpl;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.registry.ClientConfiguratorRegistry;
import org.apache.axis2.jaxws.spi.Binding;
import org.apache.axis2.jaxws.spi.BindingProvider;
import org.apache.axis2.jaxws.utility.JavaUtils;

import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Classes that would normally "implement javax.xml.ws.Binding"
 * should extend this class instead.
 */
public abstract class BindingImpl implements Binding {
    
    private static final Log log = LogFactory.getLog(BindingImpl.class);
    
    // an unsorted list of handlers
    private List<Handler> handlers;

    private EndpointDescription endpointDesc;
    
    private ClientFramework framework = new ClientFramework();

    protected String bindingId;

    protected Set<String> roles;

    protected static final String SOAP11_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";

    protected static final String SOAP12_ENV_NS = "http://www.w3.org/2003/05/soap-envelope";

    public BindingImpl(EndpointDescription endpointDesc) {
        this.endpointDesc = endpointDesc;
        // client
        this.bindingId = endpointDesc.getClientBindingID();
        if (this.bindingId == null) {
            // server
            this.bindingId = endpointDesc.getBindingType();
        }
        
        Set<String> ids = ClientConfiguratorRegistry.getIds();
        
        for (String id : ids) {
            ClientConfigurator configurator = ClientConfiguratorRegistry.getConfigurator(id);
            
            if (configurator.supports(this))
                framework.addConfigurator(id, configurator);
        }
    }

    public List<Handler> getHandlerChain() {
        if (handlers == null) {
            // non-null so client apps can manipulate
            handlers =
                    new HandlerResolverImpl(endpointDesc.getServiceDescription()).
                        getHandlerChain(endpointDesc.getPortInfo());
            if (log.isDebugEnabled()) {
                log.debug("handers list constructed from HandlerResolverImpl.  The list is:" + 
                        JavaUtils.getObjectIdentity(handlers));
            }
        }
        return handlers;
    }

    public void setHandlerChain(List<Handler> list) {
        // handlers cannot be null so a client app can request and manipulate it
        if (list == null) {
            handlers = new ArrayList<Handler>(); // non-null, but rather
                                                    // empty
            if (log.isDebugEnabled()) {
                log.debug("setHandlerChain called with a null list.  " +
                        "A empty list is created:" + JavaUtils.getObjectIdentity(handlers));
            }
        } else {
            // Use the handler list provided by the caller.
            // The JAX-WS runtime nor user should ever modify this list or a ConcurrentModificationException 
            // may occur.
            // @see org.apache.axis2.jaxws.handler.HandlerChainProcessor which makes a copy of
            // the handler chain to avoid potential CMEs.
            if (log.isDebugEnabled()) {
                log.debug("setHandlerChain called with a list:" + JavaUtils.getObjectIdentity(list));
            }
            this.handlers = list;
        }
    }

    /**
     * @since JAX-WS 2.1
     */
    public String getBindingID() {
        return this.bindingId;
    }

    public void configure(MessageContext messageContext, BindingProvider provider) {
        framework.configure(messageContext, provider);
    }

    public WebServiceFeature getFeature(String id) {
        return framework.getFeature(id);
    }

    public void setFeatures(WebServiceFeature... features) {
        if (features != null) {
            for (WebServiceFeature feature : features) {
                framework.addFeature(feature);
            }
        }
    }
    
    public void setAddressingNamespace(String addressingNamespace) {
        if (addressingNamespace != null) {
            throw new UnsupportedOperationException(
                Messages.getMessage("bindingMethodNotSupported", 
                                    "setAddressingNamespace", 
                                    bindingId)); 
        }
    }

    public void setAxis2EndpointReference(EndpointReference epr) {
        if (epr != null) {
            throw new UnsupportedOperationException(
                Messages.getMessage("bindingMethodNotSupported", "setAxis2EndpointReference", 
                                    bindingId));
        }
    }

    public String getAddressingNamespace() {
        throw new UnsupportedOperationException(
           Messages.getMessage("bindingMethodNotSupported", "getAddressingNamespace", bindingId));
    }

    public EndpointReference getAxis2EndpointReference() {
        throw new UnsupportedOperationException(
           Messages.getMessage("bindingMethodNotSupported", "getAxis2EndpointReference", bindingId)); 
    }
}
