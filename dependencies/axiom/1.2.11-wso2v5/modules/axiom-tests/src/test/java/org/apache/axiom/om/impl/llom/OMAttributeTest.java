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

package org.apache.axiom.om.impl.llom;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

public class OMAttributeTest extends TestCase {

    public void testNullLocalName() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        try {
            factory.createOMAttribute(null, null, null);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("Null localname was accepted!");
    }

    public void testEmptyLocalName() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        try {
            factory.createOMAttribute("", null, null);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("Empty localname was accepted!");
    }

    public void testWhitespaceLocalName() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        try {
            factory.createOMAttribute("    ", null, null);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("Whitespace localname was accepted!");
    }

    public void testAddAttribute() throws Exception {
        String xmlString =
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header name = \"jhon\"/><soapenv:Body><my:uploadFileUsingMTOM xmlns:my=\"http://localhost/my\"><my:folderName>/home/saliya/Desktop</my:folderName></my:uploadFileUsingMTOM></soapenv:Body><Body>TTTT</Body> </soapenv:Envelope>";

        assertEquals(addAttributeMethod1(xmlString), addAttributeMethod2(xmlString));
    }

    public void testDefaultAttributeType() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://www.me.com", "axiom");
        OMAttribute at = factory.createOMAttribute("id", ns, "value");

        assertEquals(at.getAttributeType(), "CDATA");
    }

    private String addAttributeMethod1(String xmlString) throws Exception {
        XMLStreamReader parser2;

        parser2 = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()));
        StAXOMBuilder builder2 = new StAXOMBuilder(parser2);
        OMElement doc = builder2.getDocumentElement();

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://www.me.com", "axiom");

        //code line to be tested
        OMAttribute at = factory.createOMAttribute("id", ns, "value");
        doc.addAttribute(at);

        return doc.toString();
    }

    private String addAttributeMethod2(String xmlString) throws Exception {
        XMLStreamReader parser2;

        parser2 = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()));
        StAXOMBuilder builder2 = new StAXOMBuilder(parser2);
        OMElement doc = builder2.getDocumentElement();

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://www.me.com", "axiom");

        //code line to be tested
        doc.addAttribute("id", "value", ns);

        return doc.toString();
    }

}
