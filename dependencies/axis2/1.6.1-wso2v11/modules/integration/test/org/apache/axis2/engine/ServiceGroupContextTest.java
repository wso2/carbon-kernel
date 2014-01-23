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
import org.apache.axis2.Constants;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.RequestCounter;
import org.apache.axis2.integration.LocalWithAddressingTestCase;

/**
 * This test will first sends a request to a dummy service deployed. That service will get
 * message contexts as inputs and will put a property in the service group context to count the
 * number of requests. Then the client, upon receiving the response, extracts the sgc id from
 * the received message (this will come as a reference parameter in the ReplyTo EPR) and sets
 * that as a top level soap header in the next request to the same service group. Server will
 * correctly identify the service group from the information sent by the client and retrieve the
 * sgc earlier used and will use that for the current request as well. The service will retrieve
 * the request count from the sgc and increase that by one.
 * <p/>
 * Test will asserts whether the client gets the number of requests as 2, when he invokes two
 * times.
 */
public class ServiceGroupContextTest extends LocalWithAddressingTestCase {
    protected void setUp() throws Exception {
    	super.setUp();
    	AxisService service = deployClassAsService("RequestCounter", RequestCounter.class);
        service.setScope(Constants.SCOPE_SOAP_SESSION);
    }

    public void testEchoXMLSync() throws Exception {
        ServiceClient sender = getClient("RequestCounter", "getRequestCount");
        sender.getOptions().setManageSession(true);
        
        sender.sendReceive(null);

        OMElement result2 = sender.sendReceive(null);
        String text = result2.getText();
        assertEquals("Number of requests should be 2", 2, Integer.parseInt(text));
    }
}
