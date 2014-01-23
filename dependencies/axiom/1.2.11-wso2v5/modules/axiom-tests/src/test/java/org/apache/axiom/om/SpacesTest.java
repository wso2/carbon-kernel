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

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;


public class SpacesTest extends AbstractTestCase {
    private static final String filePath = "spaces.xml";
    private static final String filePath2 = "spaces2.xml";


    private OMElement rootElement;

    public void testCData() throws Exception {
        checkOMConformance(getTestResource(filePath));
    }

    private void checkOMConformance(InputStream iStream) throws Exception {
        StAXOMBuilder staxOMBuilder = OMXMLBuilderFactory.
                createStAXOMBuilder(OMAbstractFactory.getOMFactory(),
                                    StAXUtils.createXMLStreamReader(
                                            iStream));
        rootElement = staxOMBuilder.getDocumentElement();
        boolean hasCDataNode = hasCDataNode(rootElement);
        String file = hasCDataNode ? filePath : filePath2;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ((OMDocument) rootElement.getParent()).serialize(baos);

        InputSource resultXML = new InputSource(new InputStreamReader(
                new ByteArrayInputStream(baos.toByteArray())));

        Document dom1 = newDocument(new InputSource(getTestResource(file)));
        Document dom2 = newDocument(resultXML);
        Diff diff = compareXML(dom1, dom2);
        assertXMLEqual(diff, true);
        rootElement.close(false);
    }

    private boolean hasCDataNode(OMNode pNode) {
        switch (pNode.getType()) {
            case OMNode.CDATA_SECTION_NODE:
                return true;
            case OMNode.ELEMENT_NODE:
                OMElement e = (OMElement) pNode;
                for (Iterator iter = e.getChildren(); iter.hasNext();) {
                    if (hasCDataNode((OMNode) iter.next())) {
                        return true;
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }

    public Document newDocument(InputSource in)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(in);
    }


}
