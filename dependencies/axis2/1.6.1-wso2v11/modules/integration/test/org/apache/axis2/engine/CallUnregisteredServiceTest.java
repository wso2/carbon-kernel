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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.integration.LocalTestCase;
import org.apache.axis2.integration.TestingUtils;

public class CallUnregisteredServiceTest extends LocalTestCase {

	protected void setUp() throws Exception {
		super.setUp();
		deployClassAsService(Echo.SERVICE_NAME, Echo.class);
	}

	public void testEchoXMLSync() throws Exception {
		try {
			ServiceClient sender = getClient(Echo.SERVICE_NAME + "-fail", Echo.ECHO_OM_ELEMENT_OP_NAME);
			sender.sendReceive(TestingUtils.createDummyOMElement());
			fail("The test must fail due to wrong service Name");
		} catch (AxisFault e) {
			assertTrue(e.getMessage().indexOf("The service cannot be found for the") >= 0);
		}
	}
}
