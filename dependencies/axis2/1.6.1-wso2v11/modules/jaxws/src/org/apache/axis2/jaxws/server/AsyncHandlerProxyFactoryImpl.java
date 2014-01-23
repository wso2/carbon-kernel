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
 * This class is the default implementation of the AsyncHandlerProxyFactory
 * plug-point which is supported by the JAX-WS runtime.
 * This default implementation bypasses the creation of the actual proxy
 * and simply returns the passed-in AsyncHandler instance as-is.
 */
public class AsyncHandlerProxyFactoryImpl implements AsyncHandlerProxyFactory {

    public AsyncHandler createAsyncHandlerProxy(AsyncHandler ah) throws Exception {
        return ah;
    }
}