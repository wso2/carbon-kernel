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

package org.apache.axiom.xpath;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;

public class XPathAppliedToSOAPEnvelopeTest extends TestCase {

    public void testDocumentNotAdded() throws Exception {
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();

        OMElement elem1 = factory.createOMElement("elem1", null);
        OMElement elem2 = factory.createOMElement("elem2", null);
        OMElement elem3 = factory.createOMElement("elem3", null);
        elem2.addChild(elem3);
        elem1.addChild(elem2);
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(elem1);

        //The only difference of the two test methods is the following line.
//		factory.createOMDocument().addChild(envelope);

        String XPathString = "//elem1";

        AXIOMXPath XPath = new AXIOMXPath(XPathString);
        OMNode node = (OMNode) XPath.selectSingleNode(envelope);


        assertNotNull(node);
    }

    public void testDocumentAdded() throws Exception {
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();

        OMElement elem1 = factory.createOMElement("elem1", null);
        OMElement elem2 = factory.createOMElement("elem2", null);
        OMElement elem3 = factory.createOMElement("elem3", null);
        elem2.addChild(elem3);
        elem1.addChild(elem2);
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(elem1);

        //The only difference of the two test methods is the following line.
        factory.createOMDocument().addChild(envelope);

        String XPathString = "//elem1";

        AXIOMXPath XPath = new AXIOMXPath(XPathString);
        OMNode node = (OMNode) XPath.selectSingleNode(envelope);


        assertNotNull(node);
    }
}
