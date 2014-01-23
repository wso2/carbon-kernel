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
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.ts.soap.SOAPSpec;
import org.apache.axiom.ts.soap.SOAPTestCase;

//SOAP Fault Detail Test (Programaticaly Created)
public class TestAddDetailEntry extends SOAPTestCase {
    public TestAddDetailEntry(OMMetaFactory omMetaFactory, SOAPSpec spec) {
        super(omMetaFactory, spec);
    }

    protected void runTest() throws Throwable {
        SOAPEnvelope envelope = soapFactory.createSOAPEnvelope();
        SOAPBody body = soapFactory.createSOAPBody(envelope);
        SOAPFault fault = soapFactory.createSOAPFault(body);
        SOAPFaultDetail soapFaultDetail = soapFactory.createSOAPFaultDetail(fault);
        OMNamespace omNamespace = soapFactory.createOMNamespace("http://www.test.org", "test");
        soapFaultDetail.addDetailEntry(
                soapFactory.createOMElement("DetailEntry1", omNamespace));
        soapFaultDetail.addDetailEntry(
                soapFactory.createOMElement("DetailEntry2", omNamespace));
        Iterator iterator = soapFaultDetail.getAllDetailEntries();
        OMElement detailEntry1 = (OMElement) iterator.next();
        assertNotNull(
                "SOAP Fault Detail Test : - After calling addDetailEntry method twice, getAllDetailEntries method returns empty iterator",
                detailEntry1);
        assertEquals(
                "SOAP Fault Detail Test : - detailEntry1 local name mismatch",
                "DetailEntry1", detailEntry1.getLocalName());
        assertEquals(
                "SOAP Fault Detail Test : - detailEntry1 namespace uri mismatch",
                "http://www.test.org", detailEntry1.getNamespace().getNamespaceURI());
        OMElement detailEntry2 = (OMElement) iterator.next();
        assertNotNull(
                "SOAP Fault Detail Test : - After calling addDetailEntry method twice, getAllDetailEntries method returns an iterator with only one object",
                detailEntry2);
        assertEquals(
                "SOAP Fault Detail Test : - detailEntry2 local name mismatch",
                "DetailEntry2", detailEntry2.getLocalName());
        assertEquals(
                "SOAP Fault Detail Test : - detailEntry2 namespace uri mismatch",
                "http://www.test.org", detailEntry2.getNamespace().getNamespaceURI());
        assertFalse(
                "SOAP Fault Detail Test : - After calling addDetailEntry method twice, getAllDetailEntries method returns an iterator with three objects",
                iterator.hasNext());
    }
}
