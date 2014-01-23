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
import org.apache.axiom.om.OMNamespace;

import java.util.Iterator;

public class SOAP12HeaderBlockTestBase extends SOAPHeaderBlockTestBase {
    public SOAP12HeaderBlockTestBase(OMMetaFactory omMetaFactory) {
        super(omMetaFactory, SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
    }

    public void testSetMustUnderstandStringTrueFalse() {
        SOAPHeaderBlock soapHeaderBlock = createSOAPHeaderBlock();
        soapHeaderBlock.setMustUnderstand("true");
        assertTrue(
                "SOAP HeaderBlock Test : - After setting MustUnderstand \"true\" calling setMustUnderstand method , getMustUnderstand method returns false",
                soapHeaderBlock.getMustUnderstand());
        soapHeaderBlock.setMustUnderstand("false");
        assertFalse(
                "SOAP HeaderBlock Test : - After setting MustUnderstand \"0\" calling setMustUnderstand method , getMustUnderstand method returns true",
                soapHeaderBlock.getMustUnderstand());
    }

    // SOAPHeaderBlock Test (With Parser)
    public void testGetRoleWithParser() {
        Iterator iterator = getTestMessage(MESSAGE).getHeader().examineAllHeaderBlocks();
        assertTrue(
                "SOAP HeaderBlock Test With Parser : - getRole method returns incorrect role value",
                ((SOAPHeaderBlock) iterator.next()).getRole().equals(
                        "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"));
    }

    public void testGetMustUnderstandWithParser() {
        Iterator iterator = getTestMessage(MESSAGE).getHeader().examineAllHeaderBlocks();
        assertTrue(
                "SOAP HeaderBlock Test With Parser : - getMustUnderstand method returns incorrect value",
                ((SOAPHeaderBlock) iterator.next()).getMustUnderstand());
        assertFalse(
                "SOAP HeaderBlock Test With Parser : - getMustUnderstand method returns incorrect value",
                ((SOAPHeaderBlock) iterator.next()).getMustUnderstand());
        ((SOAPHeaderBlock) iterator.next()).getMustUnderstand();
    }

    public void testRelayAttributeWithParser() throws Exception {
        Iterator iterator = getTestMessage(MESSAGE).getHeader().examineAllHeaderBlocks();
        assertFalse(((SOAPHeaderBlock) iterator.next()).getRelay());
        assertTrue(((SOAPHeaderBlock) iterator.next()).getRelay());
        assertFalse(((SOAPHeaderBlock) iterator.next()).getRelay());
    }

    public void testRelayAttribute() throws Exception {
        SOAPEnvelope env = soapFactory.createSOAPEnvelope();
        SOAPHeader header = soapFactory.createSOAPHeader(env);
        soapFactory.createSOAPBody(env);
        OMNamespace ns = soapFactory.createOMNamespace("http://ns1", "ns1");
        SOAPHeaderBlock relayHeader = header.addHeaderBlock("foo", ns);
        relayHeader.setText("hey there");
        relayHeader.setRelay(true);

        String envString = env.toString();
        assertTrue("No relay header after setRelay(true)",
                   envString.indexOf("relay=\"true\"") >= 0);
    }
}
