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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

public class UnifiedSOAPTestCase extends AbstractTestCase {
    protected static final String MESSAGE = "message.xml";
    protected static final String MESSAGE_WITHOUT_HEADER = "message_without_header.xml";
    
    protected final OMMetaFactory omMetaFactory;
    protected final String envelopeNamespaceURI;
    protected SOAPFactory soapFactory;
    
    public UnifiedSOAPTestCase(OMMetaFactory omMetaFactory, String envelopeNamespaceURI) {
        this.omMetaFactory = omMetaFactory;
        this.envelopeNamespaceURI = envelopeNamespaceURI;
    }

    protected void setUp() throws Exception {
        super.setUp();
        soapFactory = isSOAP11() ? omMetaFactory.getSOAP11Factory() : omMetaFactory.getSOAP12Factory();
    }

    protected boolean isSOAP11() {
        return envelopeNamespaceURI.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
    }
    
    protected SOAPEnvelope getTestMessage(String name) {
        String folder = isSOAP11() ? "soap11" : "soap12";
        XMLStreamReader parser;
        try {
            parser = StAXUtils.createXMLStreamReader(getTestResource("soap/" + folder + "/" + name));
        } catch (XMLStreamException ex) {
            fail("Failed to get test message " + name + ": " + ex.getMessage());
            return null;
        }
        return new StAXSOAPModelBuilder(parser, soapFactory, envelopeNamespaceURI).getSOAPEnvelope();
    }
}
