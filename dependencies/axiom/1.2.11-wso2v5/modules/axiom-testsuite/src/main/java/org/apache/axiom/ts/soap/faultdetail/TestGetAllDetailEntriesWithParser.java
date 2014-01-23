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
package org.apache.axiom.ts.soap.faultdetail;

import java.util.Iterator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.ts.soap.SOAPSpec;
import org.apache.axiom.ts.soap.SOAPTestCase;

//SOAP Fault Detail Test (With Parser)
public class TestGetAllDetailEntriesWithParser extends SOAPTestCase {
    public TestGetAllDetailEntriesWithParser(OMMetaFactory metaFactory, SOAPSpec spec) {
        super(metaFactory, spec);
    }

    protected void runTest() throws Throwable {
        SOAPFaultDetail soapFaultDetail = getTestMessage(MESSAGE).getBody().getFault().getDetail();
        Iterator iterator = soapFaultDetail.getAllDetailEntries();
        OMText textEntry = (OMText) iterator.next();
        assertNotNull(
                "SOAP Fault Detail Test With Parser : - getAllDetailEntries method returns empty iterator",
                textEntry);
        assertEquals(
                "SOAP Fault Detail Test With Parser : - text value mismatch",
                "Details of error", textEntry.getText().trim());
        OMElement detailEntry1 = (OMElement) iterator.next();
        assertNotNull(
                "SOAP Fault Detail Test With Parser : - getAllDetailEntries method returns an itrator without detail entries",
                detailEntry1);
        assertEquals(
                "SOAP Fault Detail Test With Parser : - detailEntry1 localname mismatch",
                "MaxTime", detailEntry1.getLocalName());
        iterator.next();
        OMElement detailEntry2 = (OMElement) iterator.next();
        assertNotNull(
                "SOAP Fault Detail Test With Parser : - getAllDetailEntries method returns an itrator with only one detail entries",
                detailEntry2);
        assertEquals(
                "SOAP Fault Detail Test With Parser : - detailEntry2 localname mismatch",
                "AveTime", detailEntry2.getLocalName());
        iterator.next();
        assertFalse(
                "SOAP Fault Detail Test With Parser : - getAllDetailEntries method returns an itrator with more than two detail entries",
                iterator.hasNext());
    }
}
