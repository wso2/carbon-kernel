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

package org.apache.axis2.jaxws.handler.lifecycle.factory;

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.injection.ResourceInjectionException;
import org.apache.axis2.jaxws.lifecycle.LifecycleException;

import javax.xml.ws.handler.Handler;

/*
 * HandlerLifecycleManager is responsible to invoke lifycycle methods on Handler.
 */
public interface HandlerLifecycleManager {

    public Handler createHandlerInstance(MessageContext mc, Class handlerClass)
            throws LifecycleException, ResourceInjectionException;

    public void destroyHandlerInstance(MessageContext mc, Handler handler)
    		throws LifecycleException, ResourceInjectionException;
    
    /*
      * Invokes method on endpoint marked with @PostConstruct annotation.
      */
    public void invokePostConstruct() throws LifecycleException;

    /*
      * Invokes method on endpoint marked with @preDestroy annotation.
      */
    public void invokePreDestroy() throws LifecycleException;
}
