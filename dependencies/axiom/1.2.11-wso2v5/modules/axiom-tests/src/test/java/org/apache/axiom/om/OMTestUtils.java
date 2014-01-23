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

import junit.framework.TestCase;

import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Iterator;

public class OMTestUtils {
    public static OMXMLParserWrapper getOMBuilder(File file) throws Exception {
        XMLStreamReader parser = StAXUtils.createXMLStreamReader(new FileReader(file));
        return new StAXSOAPModelBuilder(parser, null);
    }

    public static OMXMLParserWrapper getOMBuilder(InputStream in) throws Exception {
        XMLStreamReader parser = StAXUtils.createXMLStreamReader(in);
        return new StAXSOAPModelBuilder(parser, null);
    }

    public static void walkThrough(OMElement omEle) {
        Iterator attibIt = omEle.getAllAttributes();
        if (attibIt != null) {
            while (attibIt.hasNext()) {
                TestCase.assertNotNull("once the has next is not null, the " +
                        "element should not be null",
                                       attibIt.next());
            }
        }
        Iterator it = omEle.getChildren();
        if (it != null) {
            while (it.hasNext()) {
                OMNode ele = (OMNode) it.next();
                TestCase.assertNotNull("once the has next is not null, the " +
                        "element should not be null", ele);
                if (ele instanceof OMElement) {
                    walkThrough((OMElement) ele);
                }
            }
        }
    }

    public static void compare(Element ele, OMElement omele) throws Exception {
        if (ele == null && omele == null) {
            return;
        } else if (ele != null && omele != null) {
            TestCase.assertEquals("Element name not correct",
                                  ele.getLocalName(),
                                  omele.getLocalName());
            if (omele.getNamespace() != null) {
                TestCase.assertEquals("Namespace URI not correct",
                                      ele.getNamespaceURI(),
                                      omele.getNamespace().getNamespaceURI());

            }

            //go through the attributes
            NamedNodeMap map = ele.getAttributes();
            Iterator attIterator = omele.getAllAttributes();
            OMAttribute omattribute;
            while (attIterator != null && attIterator.hasNext() && map == null) {
                omattribute = (OMAttribute) attIterator.next();
                Node node = map.getNamedItemNS(
                        omattribute.getNamespace().getNamespaceURI(),
                        omattribute.getLocalName());
                if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                    Attr attr = (Attr) node;
                    TestCase.assertEquals(attr.getValue(),
                                          omattribute.getAttributeValue());
                } else {
                    throw new OMException("return type is not a Attribute");
                }

            }
            Iterator it = omele.getChildren();
            NodeList list = ele.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    TestCase.assertTrue(it.hasNext());
                    OMNode tempOmNode = (OMNode) it.next();
                    while (tempOmNode.getType() != OMNode.ELEMENT_NODE) {
                        TestCase.assertTrue(it.hasNext());
                        tempOmNode = (OMNode) it.next();
                    }
                    compare((Element) node, (OMElement) tempOmNode);
                }
            }


        } else {
            throw new Exception("One is null");
        }
    }
}
