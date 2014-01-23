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
package org.apache.axiom.ts.soap.envelope;

import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.ts.soap.SOAPSpec;
import org.apache.axiom.ts.soap.SOAPTestCase;

/**
 * Test the behavior when adding a header to an envelope that has not yet been built completely.
 * This is a regression test for WSCOMMONS-552.
 */
public class TestAddHeaderToIncompleteEnvelope extends SOAPTestCase {
    public TestAddHeaderToIncompleteEnvelope(OMMetaFactory metaFactory, SOAPSpec spec) {
        super(metaFactory, spec);
    }

    protected void runTest() throws Throwable {
        SOAPEnvelope envelope = getTestMessage(MESSAGE_WITHOUT_HEADER);
        assertNull(envelope.getHeader());
        SOAPHeader header = soapFactory.createSOAPHeader(envelope);
        assertSame(header, envelope.getHeader());
    }
}
