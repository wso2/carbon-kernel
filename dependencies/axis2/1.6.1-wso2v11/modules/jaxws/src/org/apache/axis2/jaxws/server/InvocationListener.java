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

/**
 * This interface will be implemented by those components wishing to
 * be notified when requests are received on the server-side and when
 * responses have been constructed on the server-side. Implementations
 * can be registered with the FactoryRegistry.
 *
 */
public interface InvocationListener {

    
    /**
     * This method will be called by JAX-WS when a message has been
     * received or just before a response is sent to the client.
     */
    public void notify(InvocationListenerBean bean) throws Exception;
    
    /**
     * This method will be called anytime that an exception occurs
     * within the JAX-WS server-side code flow. InvocationListener
     * instances may change the exception being operated on by setting 
     * the org.apache.axis2.jaxws.spi.Constants.MAPPED_EXCEPTION
     * property on either the request or response MessageContext. The
     * value of the property should be an instance of Throwable.
     */
    public void notifyOnException(InvocationListenerBean bean);
    
}
