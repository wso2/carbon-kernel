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

/**
 * Code wishing to create web service implementation instances for JAX-WS
 * requests must implement this interface and register it with the the
 * JAX-WS FactoryRegistry. The instance of this factory will be looked up
 * and utilized by the JAX-WS runtime in order to create or otherwise
 * obtain a web service implementation instance for any request.
 *
 */
public interface ServiceInstanceFactory {

        /**
         * This method will create, or otherwise obtain a reference to an 
         * instance of a web service implementation class. The expectation
         * is that upon the completion of this method there will be a web
         * service implementation instance cretaed and all necessary resource
         * injection will have taken place.
         */
        public Object createServiceInstance(MessageContext request, Class serviceClass) 
                throws Exception;
        
}