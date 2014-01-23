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

package org.apache.axis2.jaxws.spi.migrator;

import org.apache.axis2.jaxws.core.MessageContext;

import java.util.Map;

/**
 * The ContextPropertyMigrator is a utility interface that can be implemented to handle any
 * transformation or migration that needs to happen between the internal JAX-WS MessageContext for a
 * request or a response and the associated context for the client or the server.
 * <p/>
 * client - On the client side, this will be called with the request or response context from the
 * BindingProvider instance.
 * <p/>
 * server - On the server side, this will be called with the javax.xml.ws.handler.MessageContext
 * instance that the service endpoint will see.  This is the same context that will be injected
 */
public interface ApplicationContextMigrator {

    /**
     * Is called to handle property migration FROM the user context (BindingProvider client context
     * or server MessageContext) TO a target internal org.apache.axis2.jaxws.core.MessageContext.
     *
     * @param userContext    - The source context that contains the user context properties.
     * @param messageContext - The target MessageContext to receive the properties.
     */
    public void migratePropertiesToMessageContext(Map<String, Object> userContext,
                                                  MessageContext messageContext);

    /**
     * Is called to handle property migratom FROM the internal org.apache.axis2.jaxws.core.MessageContext
     * TO a target user context (BindingProvider client context or server MessageContext) that the
     * user will access.
     *
     * @param userContext    - The target user context to receive the properties.
     * @param messageContext - The source MessageContext that contains the property values.
     */
    public void migratePropertiesFromMessageContext(Map<String, Object> userContext,
                                                    MessageContext messageContext);

}
