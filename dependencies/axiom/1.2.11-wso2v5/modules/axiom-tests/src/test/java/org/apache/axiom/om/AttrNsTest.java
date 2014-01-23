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

package org.apache.axiom.om;

import org.apache.axiom.om.util.StAXUtils;
import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.Iterator;

public class AttrNsTest extends AbstractOMSerializationTest {

    private String attrNamespaceTestXML = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<foo xmlns:e=\"http://opensource.lk\">" +
            "    <bar1 b:attr=\"test attr value1\" xmlns:b=\"http://opensource.lk/ns1\">test1</bar1>" +
            "    <bar2 b:attr=\"test attr value2\" xmlns:b=\"http://opensource.lk/ns1\">test2</bar2>" +
            "</foo>";

    public void testAttributeNamespaces() throws Exception {
        ignoreXMLDeclaration = true;
        ignoreDocument = true;

        Document document1 = newDocument(attrNamespaceTestXML);
        String serializedOM = getSerializedOM(attrNamespaceTestXML);
        Document document2 = newDocument(serializedOM);

        Diff diff = compareXML(document1, document2);
        assertXMLEqual(diff, true);
    }

    public void testAttributesWithProgrammaticalCreation() throws Exception {
        String expectedXML =
                "<AttributeTester xmlns=\"\" xmlns:myAttr2NS=\"http://test-attributes-2.org\" " +
                        "xmlns:myAttr1NS=\"http://test-attributes-1.org\" myAttr2NS:attrNumber=\"2\" myAttr1NS:attrNumber=\"1\" />";

        OMFactory omFactory = OMAbstractFactory.getOMFactory();

        OMNamespace attrNS1 =
                omFactory.createOMNamespace("http://test-attributes-1.org", "myAttr1NS");
        OMNamespace attrNS2 =
                omFactory.createOMNamespace("http://test-attributes-2.org", "myAttr2NS");
        OMElement omElement = omFactory.createOMElement("AttributeTester", null);
        omElement.addAttribute(omFactory.createOMAttribute("attrNumber", attrNS1, "1"));
        omElement.addAttribute(omFactory.createOMAttribute("attrNumber", attrNS2, "2"));

        int nsCount = 0;
        for (Iterator iterator = omElement.getAllDeclaredNamespaces(); iterator.hasNext();) {
            iterator.next();
            nsCount++;
        }
        assertTrue(nsCount == 2);

        Document document1 = newDocument(expectedXML);
        Document document2 = newDocument(omElement.toString());

        Diff diff = compareXML(document1, document2);
        assertXMLEqual(diff, true);
    }


    public void testAttributesWithNamespaceSerialization() throws Exception {
        String xmlString =
                "<root xmlns='http://custom.com'><node cust:id='123' xmlns:cust='http://custom.com' /></root>";
        XMLStreamReader xmlStreamReader = StAXUtils.createXMLStreamReader(
                new StringReader(xmlString));

        // copied code from the generated stub class toOM method
        org.apache.axiom.om.impl.builder.StAXOMBuilder builder =
                new org.apache.axiom.om.impl.builder.StAXOMBuilder(xmlStreamReader);
        org.apache.axiom.om.OMElement documentElement = builder
                .getDocumentElement();

        ((org.apache.axiom.om.impl.OMNodeEx) documentElement).setParent(null);
        // end copied code

        // now print the object after it has been processed
        System.out.println("after - '" + documentElement.toString() + "'");
        Document document1 = newDocument(xmlString);
        Document document2 = newDocument(documentElement.toString());
        Diff diff = compareXML(document1, document2);
        assertXMLEqual(diff, true);
    }
}
