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

import java.util.Iterator;

import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.ts.soap.SOAPSpec;
import org.apache.axiom.ts.soap.SOAPTestCase;

public class TestAddHeaderBlock extends SOAPTestCase {
    public TestAddHeaderBlock(OMMetaFactory metaFactory, SOAPSpec spec) {
        super(metaFactory, spec);
    }

    protected void runTest() throws Throwable {
        SOAPEnvelope soapEnvelope = soapFactory.createSOAPEnvelope();
        SOAPHeader soapHeader = soapFactory.createSOAPHeader(soapEnvelope);
        OMNamespace namespace = soapFactory.createOMNamespace("http://www.example.org", "test");
        soapHeader.addHeaderBlock("echoOk1", namespace);
        soapHeader.addHeaderBlock("echoOk2", namespace);
        Iterator iterator = soapHeader.getChildren();
        SOAPHeaderBlock headerBlock1 = (SOAPHeaderBlock) iterator.next();
        assertNotNull(
                "SOAP Header Test : - After calling addHeaderBlock method twice, getChildren method returns empty iterator",
                headerBlock1);
        assertEquals("SOAP Header Test : - HeaderBlock1 local name mismatch",
                "echoOk1", headerBlock1.getLocalName());
        assertEquals(
                "SOAP Header Test : - HeaderBlock1 namespace uri mismatch",
                "http://www.example.org", headerBlock1.getNamespace().getNamespaceURI());

        SOAPHeaderBlock headerBlock2 = (SOAPHeaderBlock) iterator.next();
        assertNotNull(
                "SOAP Header Test : - After calling addHeaderBlock method twice, getChildren method returns an iterator with only one object",
                headerBlock2);
        assertEquals("SOAP Header Test : - HeaderBlock2 local name mismatch",
                "echoOk2", headerBlock2.getLocalName());
        assertEquals(
                "SOAP Header Test : - HeaderBlock2 namespace uri mismatch",
                "http://www.example.org", headerBlock2.getNamespace().getNamespaceURI());

        assertFalse(
                "SOAP Header Test : - After calling addHeaderBlock method twice, getChildren method returns an iterator with more than two elements",
                iterator.hasNext());
    }
}
