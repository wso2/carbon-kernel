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

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.axiom.om.OMMetaFactory;

public class SOAP11HeaderTestBase extends SOAPHeaderTestBase {
    public SOAP11HeaderTestBase(OMMetaFactory omMetaFactory) {
        super(omMetaFactory, SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI, SOAP11Constants.SOAP_ACTOR_NEXT);
    }

    //SOAP Header Test (With Parser)---------------------------------------------------------------------------------------------
    public void testExamineHeaderBlocksWithParser() {
        Iterator iterator = getTestMessage(MESSAGE).getHeader().examineHeaderBlocks(
                "http://schemas.xmlsoap.org/soap/actor/next");
        iterator.hasNext();
        SOAPHeaderBlock headerBlock1 = (SOAPHeaderBlock) iterator.next();
        assertEquals(
                "SOAP Header Test With Parser : - headerBlock1 localname mismatch",
                headerBlock1.getLocalName(),
                "From");
        assertTrue(
                "SOAP Header Test With Parser : - headerBlock1 role value mismatch",
                headerBlock1.getRole().equals(
                        "http://schemas.xmlsoap.org/soap/actor/next"));
        iterator.hasNext();
        SOAPHeaderBlock headerBlock2 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP Header Test With Parser : - headerBlock2 localname mmismatch",
                headerBlock2.getLocalName().equals("MessageID"));
        assertTrue(
                "SOAP Header Test With Parser : - headerBlock2 role value mmismatch",
                headerBlock2.getRole().equals(
                        "http://schemas.xmlsoap.org/soap/actor/next"));

        assertFalse(
                "SOAP Header Test With Parser : - examineHeaderBlocks(String role) method returns an iterator with more than two objects",
                iterator.hasNext());
    }

    public void testExamineMustUnderstandHeaderBlocksWithParser() {
        Iterator iterator = getTestMessage(MESSAGE).getHeader().examineMustUnderstandHeaderBlocks(
                "http://schemas.xmlsoap.org/soap/actor/next");
        iterator.hasNext();
        SOAPHeaderBlock headerBlock1 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP Header Test With Parser : - headerBlock1 localname mmismatch",
                headerBlock1.getLocalName().equals("MessageID"));
        assertTrue(
                "SOAP Header Test With Parser : - headerBlock1 role value mmismatch",
                headerBlock1.getRole().equals(
                        "http://schemas.xmlsoap.org/soap/actor/next"));

        assertFalse(
                "SOAP Header Test With Parser : - examineMustUnderstandHeaderBlocks(String role) method returns an iterator with more than one objects",
                iterator.hasNext());
    }

    public void testExamineAllHeaderBlocksWithParser() {
        Iterator iterator = getTestMessage(MESSAGE).getHeader().examineAllHeaderBlocks();
        iterator.hasNext();
        SOAPHeaderBlock headerBlock1 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP Header Test With Parser : - headerBlock1 localname mmismatch",
                headerBlock1.getLocalName().equals("From"));
        assertTrue(iterator.hasNext());
        SOAPHeaderBlock headerBlock2 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP Header Test With Parser : - headerBlock1 localname mmismatch",
                headerBlock2.getLocalName().equals("MessageID"));
        assertTrue(iterator.hasNext());
        SOAPHeaderBlock headerBlock3 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP Header Test With Parser : - headerBlock1 localname mmismatch",
                headerBlock3.getLocalName().equals("To"));

        assertFalse(
                "SOAP Header Test With Parser : - examineAllHeaderBlocks method returns an iterator with more than three objects",
                iterator.hasNext());
    }

    public void testGetHeaderBlocksWithNSURIWithParser() {
        ArrayList arrayList = getTestMessage(MESSAGE).getHeader().getHeaderBlocksWithNSURI(
                "http://example.org/ts-tests");
        assertTrue(
                "SOAP Header Test With Parser : - getHeaderBlocksWithNSURI returns an arrayList of incorrect size",
                arrayList.size() == 1);
        assertTrue(
                "SOAP Header Test With Parser : - headerBlock of given namespace uri, local name mismatch",
                ((SOAPHeaderBlock) arrayList.get(0)).getLocalName().equals(
                        "MessageID"));
        assertTrue(
                "SOAP Header Test With Parser : - headerBlock of given namespace uri, mismatch",
                ((SOAPHeaderBlock) arrayList.get(0)).getNamespace().getNamespaceURI()
                        .equals("http://example.org/ts-tests"));
    }
}
