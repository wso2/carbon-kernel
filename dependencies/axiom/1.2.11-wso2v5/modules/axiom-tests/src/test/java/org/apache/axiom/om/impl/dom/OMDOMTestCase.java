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

package org.apache.axiom.om.impl.dom;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.TestConstants;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.dom.factory.DOMSOAPFactory;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class OMDOMTestCase extends AbstractTestCase {

    protected static final String IN_FILE_NAME = TestConstants.SOAP_SOAPMESSAGE;
    protected StAXSOAPModelBuilder builder;
    protected OMFactory ombuilderFactory;
    protected SOAPFactory soapFactory;

    protected SOAPEnvelope soapEnvelope;

    protected void setUp() throws Exception {
        super.setUp();
        soapEnvelope = (SOAPEnvelope) getOMBuilder("").getDocumentElement();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        soapEnvelope.close(false);
    }

    protected StAXSOAPModelBuilder getOMBuilder(String fileName) throws Exception {
        if ("".equals(fileName) || fileName == null) {
            fileName = IN_FILE_NAME;
        }
        XMLStreamReader parser = StAXUtils.createXMLStreamReader(getTestResource(fileName));
        builder = new StAXSOAPModelBuilder(parser, new SOAP11Factory(), null);
        return builder;
    }


    protected StAXSOAPModelBuilder getOMBuilder(InputStream in) throws Exception {
        XMLStreamReader parser = StAXUtils.createXMLStreamReader(in);
        builder = new StAXSOAPModelBuilder(parser, new DOMSOAPFactory(), null);
        return builder;
    }

    protected XMLStreamWriter getStAXStreamWriter(OutputStream out) throws XMLStreamException {
        return StAXUtils.createXMLStreamWriter(out);
    }
}
