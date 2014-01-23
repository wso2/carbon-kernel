/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.apache.axis2.transport.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;

/**
 * Partial implementation of {@link AbstractTransportListener} with a higher level
 * of abstraction. It maintains the mapping between services and protocol specific
 * endpoints.
 * <p>
 * Note: the intention is to eventually merge the code in this class into
 * {@link AbstractTransportListener}
 * 
 * @param <E> the type of protocol endpoint for this transport
 */
public abstract class AbstractTransportListenerEx<E extends ProtocolEndpoint>
        extends AbstractTransportListener {
    
    /**
     * The collection of protocol specific endpoints configured at the service level.
     */
    private List<E> serviceEndpoints = new ArrayList<E>();

    /**
     * The endpoint configured at the transport level. <code>null</code> if no
     * such endpoint is configured.
     */
    private E globalEndpoint;

    @Override
    public final void init(ConfigurationContext cfgCtx,
            TransportInDescription transportIn) throws AxisFault {

        super.init(cfgCtx, transportIn);
        
        doInit();
        
        // Create endpoint configured at transport level (if available)
        E endpoint = createEndpoint();
        endpoint.init(this, null);
        if (endpoint.loadConfiguration(transportIn)) {
            globalEndpoint = endpoint;
        }
    }
    
    /**
     * Initialize the transport. This method will be called after the initialization work in
     * {@link AbstractTransportListener} and before the first endpoint is created, i.e. before the
     * first call to {@link #createEndpoint()}.
     * 
     * @throws AxisFault
     */
    protected abstract void doInit() throws AxisFault;
    
    @Override
    public void start() throws AxisFault {
        super.start();
        // Explicitly start the endpoint configured at the transport level. All other endpoints will
        // be started by startListeningForService.
        if (globalEndpoint != null) {
            startEndpoint(globalEndpoint);
        }
    }

    @Override
    public void stop() throws AxisFault {
        super.stop();
        // Explicitly stop the endpoint configured at the transport level. All other endpoints will
        // be stopped by stopListeningForService.
        if (globalEndpoint != null) {
            stopEndpoint(globalEndpoint);
        }
    }

    @Override
    public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault {
        // strip out the endpoint name if present
        if (serviceName.indexOf('.') != -1) {
            serviceName = serviceName.substring(0, serviceName.indexOf('.'));
        }
        for (E endpoint : serviceEndpoints) {
            AxisService service = endpoint.getService();
            if (service.getName().equals(serviceName)) {
                return endpoint.getEndpointReferences(service, ip);
            }
        }
        // If we get here, this means that the service is not explicitly configured
        // with a specific protocol endpoint. However, it is still exposed over the
        // transport. In this case, we build the EPR using the endpoint configured
        // at the transport level, if there is one.
        if (globalEndpoint != null) {
            AxisService service = cfgCtx.getAxisConfiguration().getService(serviceName);
            if (service == null) {
                // Oops, something strange is happening here
                return null;
            } else {
                return globalEndpoint.getEndpointReferences(service, ip);
            }
        } else {
            return null;
        }
    }

    /**
     * Get the collection of all protocol endpoints managed by this transport, including the
     * endpoint configured at the transport level.
     * 
     * @return the collection of all protocol endpoints
     */
    public final Collection<E> getEndpoints() {
        if (globalEndpoint == null) {
            return Collections.unmodifiableCollection(serviceEndpoints);
        } else {
            List<E> endpoints = new ArrayList<E>(serviceEndpoints.size() + 1);
            endpoints.add(globalEndpoint);
            endpoints.addAll(serviceEndpoints);
            return endpoints;
        }
    }

    protected abstract E createEndpoint();
    
    @Override
    protected final void startListeningForService(AxisService service) throws AxisFault {
        E endpoint = createEndpoint();
        endpoint.init(this, service);
        if (endpoint.loadConfiguration(service)) {
            startEndpoint(endpoint);
            serviceEndpoints.add(endpoint);
        } else if (globalEndpoint != null) {
            return;
        } else {
            throw new AxisFault("Service doesn't have configuration information for transport " +
                    getTransportName());
        }
    }

    protected abstract void startEndpoint(E endpoint) throws AxisFault;

    @Override
    protected final void stopListeningForService(AxisService service) {
        for (E endpoint : serviceEndpoints) {
            if (service == endpoint.getService()) {
                stopEndpoint(endpoint);
                serviceEndpoints.remove(endpoint);
                return;
            }
        }
        if (globalEndpoint == null) {
            log.error("Unable to stop service : " + service.getName() +
                    " - unable to find the corresponding protocol endpoint");
        }
    }
    
    protected abstract void stopEndpoint(E endpoint);
}
