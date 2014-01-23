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

package org.apache.axis2.processingModel;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.LocalTestCase;
import org.apache.axis2.integration.TestingUtils;

public class SoapProcessingModelTest extends LocalTestCase {
	
    protected void setUp() throws Exception {
    	super.setUp();
    	deployClassAsService(Echo.SERVICE_NAME, Echo.class);
    }

    public void testSendingMustUnderstandWithNextRole() throws Exception {
        ServiceClient serviceClient = getClient(Echo.SERVICE_NAME, Echo.ECHO_OM_ELEMENT_OP_NAME);
        
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        OMNamespace headerNs = fac.createOMNamespace("http://dummyHeader", "dh");
        SOAPHeaderBlock h1 = fac.createSOAPHeaderBlock("DummyHeader", headerNs);
        h1.setMustUnderstand(true);
        h1.addChild(fac.createOMText("Dummy String"));
        h1.setRole(SOAP12Constants.SOAP_ROLE_NEXT);
        
        serviceClient.addHeader(h1);
        
        OMElement payload = TestingUtils.createDummyOMElement();

        serviceClient.getOptions().setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        
        try {
        	serviceClient.sendReceive(payload);
        } catch (AxisFault fault) {
            // This should be a MustUnderstand fault
            assertEquals(fault.getFaultCode(), SOAP12Constants.QNAME_MU_FAULTCODE);
            return;
        }
        fail("MU header was processed");
    }
}
