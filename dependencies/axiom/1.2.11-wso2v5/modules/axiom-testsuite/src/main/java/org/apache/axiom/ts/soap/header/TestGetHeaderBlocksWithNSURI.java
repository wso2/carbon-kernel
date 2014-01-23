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
package org.apache.axiom.ts.soap.header;

import java.util.ArrayList;

import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.ts.soap.SOAPSpec;
import org.apache.axiom.ts.soap.SOAPTestCase;

public class TestGetHeaderBlocksWithNSURI extends SOAPTestCase {
    public TestGetHeaderBlocksWithNSURI(OMMetaFactory metaFactory, SOAPSpec spec) {
        super(metaFactory, spec);
    }

    protected void runTest() throws Throwable {
        SOAPEnvelope soapEnvelope = soapFactory.createSOAPEnvelope();
        SOAPHeader soapHeader = soapFactory.createSOAPHeader(soapEnvelope);
        OMNamespace namespace = soapFactory.createOMNamespace("http://www.example.org", "test");
        soapHeader.addHeaderBlock("echoOk1", namespace);
        soapHeader.addHeaderBlock("echoOk2",
                                    soapFactory.createOMNamespace("http://www.test1.org", "test1"));
        ArrayList arrayList = soapHeader.getHeaderBlocksWithNSURI(
                "http://www.test1.org");
        assertEquals(
                "SOAP Header Test : - getHeaderBlocksWithNSURI returns an arrayList of incorrect size",
                1, arrayList.size());
        assertEquals(
                "SOAP Header Test : - headerBlock of given namespace uri mismatch",
                "http://www.test1.org",
                ((SOAPHeaderBlock) arrayList.get(0)).getNamespace().getNamespaceURI());
    }
}
