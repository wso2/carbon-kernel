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
package org.apache.axiom.test;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.felix.ipojo.junit4osgi.OSGiTestCase;

public class OMAbstractFactoryTest extends OSGiTestCase {

	public void testgetOMFactory() throws Exception {
		assertNotNull(OMAbstractFactory.getOMFactory());
	}

	public void testgetSOAP11Factory() throws Exception {
		assertNotNull(OMAbstractFactory.getSOAP11Factory());
	}

	public void testgetSOAP12Factory() throws Exception {
		assertNotNull(OMAbstractFactory.getSOAP12Factory());
	}
}
