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

import javax.xml.ws.handler.MessageContext;

/*
 * HandlerPostInvoker - this is the interface returned by the
 * HandlerPostInvokerFactory to be called just after each
 * JAXWS handler.handleMessage invocation.
 */
public interface HandlerPostInvoker {

	/**
	 * postInvoke is called just prior to each JAXWS handler.handleMessage.
	 * Implementations may need to check if we are inbound or outbound, and
	 * client or server.  Implementations may validate the pre-conditions for handlers,
	 * such as saving a SOAP message body, that were registered by HandlerPreInvoker.
	 * 
	 * @param mc
	 */
	public void postInvoke(MessageContext mc);
	
}
