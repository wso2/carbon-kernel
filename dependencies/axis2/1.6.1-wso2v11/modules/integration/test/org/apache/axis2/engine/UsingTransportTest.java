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
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.integration.LocalTestCase;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.wsdl.WSDLConstants;

public class UsingTransportTest extends LocalTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        deployClassAsService(Echo.SERVICE_NAME, Echo.class);
    }

    public void testSOAPVersioning() throws Exception{
    	ServiceClient sender = getClient(Echo.SERVICE_NAME, Echo.ECHO_OM_ELEMENT_OP_NAME);
    	sender.getOptions().setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
    	sender.setCachingOperationContext(true);
    	OMElement response = sender.sendReceive(TestingUtils.createDummyOMElement());
    	TestingUtils.compareWithCreatedOMElement(response);
    	SOAPEnvelope result =
            sender.getLastOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE).getEnvelope();
    	 assertEquals("SOAP Version received is not compatible",
                 SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                 result.getNamespace().getNamespaceURI());
    }
    
    // The valid case is covered by loads of other tests...
    public void testInvalidTransport() throws AxisFault {
    	ServiceClient sender = getClient(Echo.SERVICE_NAME, Echo.ECHO_OM_ELEMENT_OP_NAME);
    	String oldAddress = sender.getOptions().getTo().getAddress();
    	String newAddress = "lcl"+oldAddress.substring(5);
    	sender.getOptions().getTo().setAddress(newAddress);
    	sender.getOptions().setTransportOut(null);
    	try {
            sender.sendReceive(TestingUtils.createDummyOMElement());
            fail("Expecting a failure - shouldn't be able to determine the out transport from 'lcl'");
        }
        catch (Exception ex) {
            assertTrue("Expecting an exception of type [org.apache.axis2.AxisFault]",
                       (ex instanceof org.apache.axis2.AxisFault));
        }
    }
}
