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

package org.apache.axis2.deployment;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.integration.LocalTestCase;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;

public class TargetResolverServiceTest extends LocalTestCase {

    protected void setUp() throws Exception {
    	super.setUp();
    	serverConfig.addMessageReceiver(WSDL2Constants.MEP_URI_IN_OUT, new MessageReceiver(){
			public void receive(MessageContext msgContext) throws AxisFault {
				// Set the reply to on the server side to test server side
				// target resolvers
				msgContext.setReplyTo(new EndpointReference(
	            "http://ws.apache.org/new/anonymous/address"));
			    new RawXMLINOutMessageReceiver().receive(msgContext);
			}
    	});
    	deployClassAsService(Echo.SERVICE_NAME, Echo.class);
    	clientCtx.getAxisConfiguration().addTargetResolver(new TestTargetResolver());
    	serverConfig.getAxisConfiguration().addTargetResolver(new TestTargetResolver());
    }
    
    public void testTargetRsolver() throws Exception {
    	ServiceClient sender = getClient(Echo.SERVICE_NAME, Echo.ECHO_OM_ELEMENT_OP_NAME);
    	String oldAddress = sender.getOptions().getTo().getAddress();
    	String newAddress = "trtest"+oldAddress.substring(5);
    	sender.getOptions().getTo().setAddress(newAddress);
    	OMElement response = sender.sendReceive(TestingUtils.createDummyOMElement());
    	TestingUtils.compareWithCreatedOMElement(response);
    }

}
