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

package org.apache.axis2.engine;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.dispatchers.RequestURIBasedDispatcher;
import org.apache.axis2.dispatchers.RequestURIBasedOperationDispatcher;
import org.apache.axis2.dispatchers.SOAPMessageBodyBasedDispatcher;
import org.apache.axis2.integration.LocalTestCase;
import org.apache.axis2.integration.TestingUtils;

public class ServiceDispatchingTest extends LocalTestCase {

    protected void setUp() throws Exception {
    	super.setUp();
    	deployClassAsService(Echo.SERVICE_NAME, Echo.class);
    }

    public void testDispatchWithURLOnly() throws Exception {
    	DispatchPhase dp = new DispatchPhase();
    	dp.addHandler(new RequestURIBasedDispatcher());
    	dp.addHandler(new RequestURIBasedOperationDispatcher());
    	serverConfig.getInFlowPhases().set(1, dp);
    	
        ServiceClient sender = getClient(Echo.SERVICE_NAME, Echo.ECHO_OM_ELEMENT_OP_NAME);
        String oldAddress = sender.getOptions().getTo().getAddress();
        String newAddress = oldAddress+"/"+Echo.ECHO_OM_ELEMENT_OP_NAME;
        sender.getOptions().getTo().setAddress(newAddress);
        sender.getOptions().setAction(null);

        OMElement result = sender.sendReceive(TestingUtils.createDummyOMElement());

        TestingUtils.compareWithCreatedOMElement(result);
    }

    public void testDispatchWithURLAndSOAPAction() throws Exception {
        ServiceClient sender = getClient(Echo.SERVICE_NAME, Echo.ECHO_OM_ELEMENT_OP_NAME);
        OMElement result = sender.sendReceive(TestingUtils.createDummyOMElement());
        TestingUtils.compareWithCreatedOMElement(result);
    }

    public void testDispatchWithSOAPBody() throws Exception {
    	DispatchPhase dp = new DispatchPhase();
    	dp.addHandler(new SOAPMessageBodyBasedDispatcher());
    	serverConfig.getInFlowPhases().set(1, dp);
    	
    	ServiceClient sender = getClient(Echo.SERVICE_NAME, Echo.ECHO_OM_ELEMENT_OP_NAME);
    	OMElement payload = TestingUtils.createDummyOMElement(sender.getOptions().getTo().getAddress());
        OMElement result = sender.sendReceive(payload);
        TestingUtils.compareWithCreatedOMElement(result);
    }
}
