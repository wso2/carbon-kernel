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

package org.apache.axiom.om.impl.streamwrapper;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.TestConstants;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;

import javax.xml.stream.XMLStreamReader;
import java.io.File;

public class OMStaxStreamingWrapperTest extends AbstractTestCase {
    private SOAPEnvelope envelope = null;
    private File tempFile;
    private XMLStreamReader parser;

    public OMStaxStreamingWrapperTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        XMLStreamReader xmlStreamReader = StAXUtils.createXMLStreamReader(
                getTestResource(TestConstants.SOAP_SOAPMESSAGE1));
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(
                OMAbstractFactory.getSOAP11Factory(), xmlStreamReader);
        envelope = (SOAPEnvelope) builder.getDocumentElement();
        tempFile = File.createTempFile("temp", "xml");

    }

    public void testWrapperHalfOM() throws Exception {
        assertNotNull(envelope);
        parser = envelope.getXMLStreamReaderWithoutCaching();
        while (parser.hasNext()) {
            int event = parser.next();
            assertTrue(event > 0);
        }
    }

    protected void tearDown() throws Exception {
        envelope.close(false);
        tempFile.delete();
    }
}
