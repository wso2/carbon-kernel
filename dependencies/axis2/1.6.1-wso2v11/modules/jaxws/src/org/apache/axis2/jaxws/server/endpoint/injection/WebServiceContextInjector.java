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

package org.apache.axis2.jaxws.server.endpoint.injection;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/*
 * WebserviceContext Injection is responsible to Injecting WebServiceContext Object to a JAXWS endpoint Instance at runtime.
 * Injection will happen when the Endpoint instance is first initialized.
 * WebServiceContext methods should be invoked when one of the webservice methods is invoked.
 * Invocation of WebServiceContext method outside of invocation of its webservice method is illigal as per jaxws spec section 5.3.
 */

public interface WebServiceContextInjector extends ResourceInjector {

    /**
     * MessageContext is made availble to the endpoint instance via the WebServiceContext. This method
     * will add MessageContext to WebServiceContext that is injected in WebService. MessageContext
     * represents the context of Inbound message following the Handler Execution. Only properties with
     * Application scope will be exposed.
     *
     * @param wc
     * @param mc
     */
    public void addMessageContext(WebServiceContext wc, MessageContext mc);
	
}
