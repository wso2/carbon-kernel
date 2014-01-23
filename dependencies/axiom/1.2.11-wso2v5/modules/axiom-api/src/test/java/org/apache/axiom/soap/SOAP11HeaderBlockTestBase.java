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

import java.util.Iterator;

import org.apache.axiom.om.OMMetaFactory;

public class SOAP11HeaderBlockTestBase extends SOAPHeaderBlockTestBase {
    public SOAP11HeaderBlockTestBase(OMMetaFactory omMetaFactory) {
        super(omMetaFactory, SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
    }

    public void testSetMustUnderstandStringTrueFalse() {
        SOAPHeaderBlock soapHeaderBlock = createSOAPHeaderBlock();
        try {
            soapHeaderBlock.setMustUnderstand("true");
        } catch (Exception e) {
            fail(
                    "SOAP HeaderBlock Test : - MustUnderstand value can not be set to any value rather than 1 or 0");
        }
    }

    // SOAPHeaderBlock Test (With Parser)
    public void testGetRoleWithParser() {
        Iterator iterator = getTestMessage(MESSAGE).getHeader().examineAllHeaderBlocks();
        assertTrue(
                "SOAP HeaderBlock Test With Parser : - getRole method returns incorrect role value",
                ((SOAPHeaderBlock) iterator.next()).getRole().equals(
                        "http://schemas.xmlsoap.org/soap/actor/next"));
    }

    public void testGetMustUnderstandWithParser() {
        Iterator iterator = getTestMessage(MESSAGE).getHeader().examineAllHeaderBlocks();
        iterator.next();
        assertTrue(
                "SOAP HeaderBlock Test With Parser : - getMustUnderstand method returns incorrect value",
                ((SOAPHeaderBlock) iterator.next()).getMustUnderstand());
        assertFalse(
                "SOAP HeaderBlock Test With Parser : - getMustUnderstand method returns incorrect value",
                ((SOAPHeaderBlock) iterator.next()).getMustUnderstand());
    }
}
