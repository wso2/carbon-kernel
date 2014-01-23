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

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLStreamReader;

public abstract class SOAPTestCase extends AbstractTestCase {
    protected OMMetaFactory omMetaFactory;
    protected SOAPFactory soap11Factory;
    protected SOAPFactory soap12Factory;
    protected OMFactory omFactory;

    protected SOAPEnvelope soap11Envelope;
    protected SOAPEnvelope soap12Envelope;

    protected SOAPEnvelope soap11EnvelopeWithParser;
    protected SOAPEnvelope soap12EnvelopeWithParser;

    protected static final String SOAP11_FILE_NAME = "soap/soap11/message.xml";
    protected static final String SOAP12_FILE_NAME = "soap/soap12/message.xml";

    public SOAPTestCase(OMMetaFactory omMetaFactory) {
        this.omMetaFactory = omMetaFactory;
    }

    protected void setUp() throws Exception {
        super.setUp();

        soap11Factory = omMetaFactory.getSOAP11Factory();
        soap12Factory = omMetaFactory.getSOAP12Factory();
        omFactory = omMetaFactory.getOMFactory();
        
        soap11Envelope = soap11Factory.createSOAPEnvelope();
        soap12Envelope = soap12Factory.createSOAPEnvelope();

        soap11EnvelopeWithParser =
                (SOAPEnvelope) this.getSOAPBuilder(SOAP11_FILE_NAME)
                        .getDocumentElement();
        soap12EnvelopeWithParser =
                (SOAPEnvelope) this.getSOAPBuilder(SOAP12_FILE_NAME)
                        .getDocumentElement();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        
        soap11Envelope.close(false);
        soap12Envelope.close(false);
        
        soap11EnvelopeWithParser.close(false);
        soap12EnvelopeWithParser.close(false);
    }

    protected StAXSOAPModelBuilder getSOAPBuilder(String fileName) throws Exception {
        XMLStreamReader parser = StAXUtils.createXMLStreamReader(
                getTestResource(fileName));
        return new StAXSOAPModelBuilder(omMetaFactory, parser, null);
    }

}
