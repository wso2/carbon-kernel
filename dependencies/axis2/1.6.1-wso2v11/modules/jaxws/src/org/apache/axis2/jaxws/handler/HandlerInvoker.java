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

package org.apache.axis2.jaxws.handler;


/**
 * This interface represents a class that will be called by the
 * JAX-WS EndpointController to handle the invocation of both
 * inbound and outbound handlers. Instances of these will be
 * served up by the HandlerInvokerFactory.
 *
 */
public interface HandlerInvoker {

    /**
     * Invokes all inbound handlers for the incoming request to the endpoint.
     */
    public boolean invokeInboundHandlers(HandlerInvocationContext context);
    
    /**
     * Invokes all inbound handlers for the incoming request to the endpoint.
     */
    public boolean invokeOutboundHandlers(HandlerInvocationContext context);
    
}
