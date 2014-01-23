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

/**
 * 
 */
package org.apache.axis2.jaxws.nonanonymous.complextype;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.nonanonymous.complextype.sei.EchoMessagePortType;

import javax.jws.WebService;

@WebService(serviceName="EchoMessageService",
	 	targetNamespace="http://testApp.jaxws",
        endpointInterface="org.apache.axis2.jaxws.nonanonymous.complextype.sei.EchoMessagePortType")

public class EchoMessageImpl implements EchoMessagePortType {

	/**
	 * 
	 */
	public EchoMessageImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.nonanonymous.complextype.sei.EchoMessagePortType#echoMessage(java.lang.String)
	 */
	public String echoMessage(String request) {
		String response = null;
        TestLogger.logger.debug("echoMessage received: " + request);
        response = request.replaceAll("Server", "Client");
        return response;

	}

}
