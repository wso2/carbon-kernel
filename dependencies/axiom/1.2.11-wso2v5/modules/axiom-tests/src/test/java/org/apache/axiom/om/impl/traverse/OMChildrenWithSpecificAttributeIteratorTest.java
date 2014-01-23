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

package org.apache.axiom.om.impl.traverse;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class OMChildrenWithSpecificAttributeIteratorTest extends TestCase {

    public OMChildrenWithSpecificAttributeIteratorTest(String testName) {
        super(testName);
    }

    public void testChildrenRetrievalWithDetaching() {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace testNamespace = factory.createOMNamespace("http://test.ws.org", "test");
        OMElement documentElement = getSampleDocumentElement(testNamespace);

        Iterator childrenIter = new OMChildrenWithSpecificAttributeIterator(
                documentElement.getFirstOMChild(),
                new QName(testNamespace.getNamespaceURI(), "myAttr",
                          testNamespace.getPrefix()), "Axis2", true);

        int childCount = getChidrenCount(childrenIter);
        assertEquals("Iterator must return 5 children with the given attribute", childCount, 5);

        Iterator children = documentElement.getChildren();
        childCount = getChidrenCount(children);
        assertEquals("Iterator must return only one child, having detached the other children",
                     childCount, 1);

    }

    public void testChildrenRetrievalWithNoDetaching() {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace testNamespace = factory.createOMNamespace("http://test.ws.org", "test");
        OMElement documentElement = getSampleDocumentElement(testNamespace);

        Iterator childrenIter = new OMChildrenWithSpecificAttributeIterator(
                documentElement.getFirstOMChild(),
                new QName(testNamespace.getNamespaceURI(), "myAttr",
                          testNamespace.getPrefix()), "Axis2", false);

        int childCount = getChidrenCount(childrenIter);
        assertEquals("Iterator must return 5 children with the given attribute", childCount, 5);

        Iterator children = documentElement.getChildren();
        childCount = getChidrenCount(children);
        assertEquals("Iterator must return 6 children, having not detached the children",
                     childCount, 6);

    }

    private OMElement getSampleDocumentElement(OMNamespace testNamespace) {
        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMElement documentElement = factory.createOMElement("Employees", testNamespace);
        documentElement.declareNamespace(testNamespace);

        OMElement employee;
        OMElement name;

        for (int i = 0; i < 5; i++) {
            employee = factory.createOMElement("Employee", testNamespace, documentElement);
            name = factory.createOMElement("Name" + i, testNamespace);
            employee.addAttribute("myAttr", "Axis2", testNamespace);
            name.setText("Apache Developer");
            employee.addChild(name);
        }

        //adding one more child with the given attr
        employee = factory.createOMElement("Employee", testNamespace, documentElement);
        name = factory.createOMElement("Name", testNamespace);
        name.addAttribute("myAttr", "Un-Related Value", testNamespace);
        name.setText("Apache Developer");
        employee.addChild(name);

        return documentElement;
    }

    private int getChidrenCount(Iterator childrenIter) {
        int childCount = 0;
        while (childrenIter.hasNext()) {
            childrenIter.next();
            childCount++;
        }

        return childCount;
    }
}
