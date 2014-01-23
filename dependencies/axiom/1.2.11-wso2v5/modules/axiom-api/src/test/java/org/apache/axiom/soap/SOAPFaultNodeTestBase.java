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

package org.apache.axiom.soap;

import org.apache.axiom.om.OMMetaFactory;

public class SOAPFaultNodeTestBase extends SOAPFaultTestCase {
    protected SOAPFaultNode soap12FaultNode;
    protected SOAPFaultNode soap12FaultNodeWithParser;

    public SOAPFaultNodeTestBase(OMMetaFactory omMetaFactory) {
        super(omMetaFactory);
    }

    protected void setUp() throws Exception {
        super.setUp();
        soap12FaultNode = soap12Factory.createSOAPFaultNode(soap12Fault);
        soap12FaultNodeWithParser = soap12FaultWithParser.getNode();
    }

    //SOAP 1.1 Fault Node Test (Programaticaly Created)
    public void testSOAP11SetNodeValue() {
        try {
            soap11Factory.createSOAPFaultNode(soap11Fault);
        } catch (UnsupportedOperationException e) {
            // Good!
            return;
        }
        fail("Unsupported SOAP 1.1 node was created");
    }

    //SOAP 1.2 Fault Node Test (Programaticaly Created)
    public void testSOAP12SetNodeValue() {
        soap12FaultNode.setNodeValue("This is only a test");
        assertTrue(
                "SOAP 1.2 Fault Node Test : - After calling setNodeValue method, getNodeValue method returns incorrect value",
                soap12FaultNode.getNodeValue().equals("This is only a test"));
    }

    public void testSOAP12GetNodeValue() {
        assertTrue(
                "SOAP 1.2 Fault Node Test : - After creating SOAPFaultNode, it has a value",
                soap12FaultNode.getNodeValue().equals(""));
        soap12FaultNode.setNodeValue("This is only a test");
        assertTrue(
                "SOAP 1.2 Fault Node Test : - After calling setNodeValue method, getNodeValue method returns incorrect value",
                soap12FaultNode.getNodeValue().equals("This is only a test"));
    }

    //SOAP 1.2 Fault Node Test (With Parser)
    public void testSOAP12GetNodeValueWithParser() {
        assertTrue(
                "SOAP 1.2 Fault Node Test With Parser : - getNodeValue method returns incorrect value",
                soap12FaultNodeWithParser.getNodeValue().trim().equals(
                        "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"));
    }
}
