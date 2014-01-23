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

import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.server.dispatcher.EndpointDispatcher;

import java.util.Collection;
import java.util.List;

/**
 * The EndpointInvocationContext is an extension of the base InvocationContext
 * that provides extensions specific to the environment of the service
 * endpoint.  
 */
public interface EndpointInvocationContext extends InvocationContext {

    /**
     * Returns the callback object to be used for asynchronous 
     * endpoint invocations.
     * 
     * @return EndpointCallback - the EndpointCallback instance.
     */
    public EndpointCallback getCallback();
    
    /**
     * Sets the callback object to be used for asynchronous 
     * endpoint invocations.
     * 
     * @param cb - the EndpointCallback instance to be used.
     */
    public void setCallback(EndpointCallback cb);
    
    /**
     * 
     * @return
     */
    public EndpointDispatcher getDispatcher();
    
    /**
     * 
     * @param ed
     */
    public void setEndpointDispatcher(EndpointDispatcher ed);
    
    /**
     * Returns a boolean value indicating whether or not the invocation
     * pattern for the request is one way or not.
     * 
     * @return
     */
    public boolean isOneWay(); 
    
    /**
     * Sets the value for marking the request as a one way invocation.
     * 
     * @param value
     */
    public void setIsOneWay(boolean value);
    
    /**
     * Sets the InvocationListenerFactory instances registered with JAX-WS.
     * @return
     */
    public void setInvocationListenerFactories(Collection<InvocationListenerFactory> factories);
    
    /**
     * Returns the InvocationListenerFactory instances registered with JAX-WS.
     * @return
     */
    public Collection<InvocationListenerFactory> getInvocationListenerFactories();
    
    /**
     * Adds an InvocationListener to the contex.
     */
    public void addInvocationListener(InvocationListener listener);
    
    /**
     * Sets list of InvocationListener instances
     */
    public void setInvocationListeners(List<InvocationListener> listeners);
    
    /**
     * Gets the InvocationListener instances from the context.
     */
    public List<InvocationListener> getInvocationListeners();

}
