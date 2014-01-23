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

import javax.xml.ws.AsyncHandler;

/**
 * This interface defines the plug-point that is responsible for creating a
 * proxy for an <code>AsyncHandler</code> instance provided by a client
 * application as part of an async request invocation. This interface allows the
 * plug-in implementor to create a proxy which wraps the client-provided
 * <code>AsyncHandler</code> instance and can be used to perform
 * application-server-specific actions in conjunction with the execution of the
 * <code>AsyncHandler</code> instance, such as thread context migration, etc.
 * 
 * To use this plug-point, you must define a class which implements this
 * interface, then register your implementation with the JAX-WS
 * <code>FactoryRegistry.setFactory()</code> method.
 */
public interface AsyncHandlerProxyFactory {

    /**
     * This method is invoked by the JAX-WS runtime to allow the implementation
     * to provide a proxy for the specified AsyncHandler instance.
     * 
     * @param ah
     *            the AsyncHandler instance to be wrapped with the new proxy
     * @return
     *            the proxy which wraps the original AsyncHandler instance
     */
    public AsyncHandler createAsyncHandlerProxy(AsyncHandler ah) throws Exception;
}