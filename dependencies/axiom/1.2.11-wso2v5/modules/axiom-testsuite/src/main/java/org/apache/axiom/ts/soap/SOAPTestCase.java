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
package org.apache.axiom.ts.soap;

import java.io.InputStream;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.StAXParserConfiguration;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.ts.AxiomTestCase;
import org.xml.sax.InputSource;

public class SOAPTestCase extends AxiomTestCase {
    protected static final String MESSAGE = "message.xml";
    protected static final String MESSAGE_WITHOUT_HEADER = "message_without_header.xml";
    
    protected final SOAPSpec spec;
    protected SOAPFactory soapFactory;
    protected SOAPFactory altSoapFactory;
    
    public SOAPTestCase(OMMetaFactory metaFactory, SOAPSpec spec) {
        super(metaFactory);
        this.spec = spec;
        setName(getName() + " [" + spec.getName() + "]");
    }

    protected void setUp() throws Exception {
        super.setUp();
        soapFactory = spec.getFactory(metaFactory);
        altSoapFactory = spec.getAltFactory(metaFactory);
    }

    protected SOAPEnvelope getTestMessage(String name) {
        InputStream in = AbstractTestCase.getTestResource("soap/" + spec.getName() + "/" + name);
        SOAPEnvelope envelope = (SOAPEnvelope)metaFactory.createSOAPModelBuilder(StAXParserConfiguration.SOAP,
                new InputSource(in)).getDocumentElement();
        assertSame(spec.getEnvelopeNamespaceURI(), ((SOAPFactory)envelope.getOMFactory()).getSoapVersionURI());
        return envelope;
    }

    protected SOAPHeaderBlock createSOAPHeaderBlock() {
        OMNamespace namespace = soapFactory.createOMNamespace("http://www.example.org", "test");;
        SOAPEnvelope soapEnvelope = soapFactory.createSOAPEnvelope();
        SOAPHeader soapHeader = soapFactory.createSOAPHeader(soapEnvelope);
        return soapFactory.createSOAPHeaderBlock("testHeaderBlock", namespace, soapHeader);
    }
}
