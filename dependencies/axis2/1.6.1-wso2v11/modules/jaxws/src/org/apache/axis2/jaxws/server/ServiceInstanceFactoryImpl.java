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

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.EndpointLifecycleManager;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.factory.EndpointLifecycleManagerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class will be responsible for creating an instance of
 * a web service implementation class for each incoming request
 * into the JAX-WS runtime.
 *
 */
public class ServiceInstanceFactoryImpl implements ServiceInstanceFactory {
        
        private static final Log log = LogFactory.getLog(ServiceInstanceFactoryImpl.class);

        public Object createServiceInstance(MessageContext request, Class serviceClass) throws 
                Exception 
        {
            if(log.isDebugEnabled()) {
                log.debug("Creating web service implementation instance for: " + serviceClass.
                                getName());
            }
            EndpointLifecycleManagerFactory elmf = (EndpointLifecycleManagerFactory) 
                FactoryRegistry.getFactory(EndpointLifecycleManagerFactory.class);
            EndpointLifecycleManager elm = elmf.createEndpointLifecycleManager();
            return elm.createServiceInstance(request, serviceClass);
        }

}
