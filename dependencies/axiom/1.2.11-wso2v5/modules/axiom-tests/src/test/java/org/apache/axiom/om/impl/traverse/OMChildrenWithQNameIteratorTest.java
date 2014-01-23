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
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class OMChildrenWithQNameIteratorTest extends TestCase {

    private final String NS_A = "urn://a";
    private final String NS_B = "urn://b";
    private final String NS_C = "urn://c";
    
    public OMChildrenWithQNameIteratorTest(String testName) {
        super(testName);
    }

    public void testChildrenRetrievalWithQName() {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace testNamespace = factory.createOMNamespace("http://test.ws.org", "test");
        OMElement documentElement = getSampleDocumentElement(testNamespace);

        Iterator childrenIter = documentElement.getChildrenWithName(new QName("http://test.ws.org", "Employee", "test"));

        int childCount = getChildrenCount(childrenIter);
        assertEquals("Iterator must return 1 child with the given qname", childCount, 1);
    }

    public void testChildrenRetrievalWithQName_286() {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace testNamespace = factory.createOMNamespace("http://test.ws.org", "test");
        OMElement documentElement = getSampleDocumentElement(testNamespace);

        Iterator childrenIter = documentElement.getChildrenWithName(new QName("http://test.ws.org", "Employee", "test"));
        OMElement employee = (OMElement) childrenIter.next(); // should walk past OMText
        assertEquals("Employee test was incorrect", employee.getText(), "Apache Developer");
    }

    private OMElement getSampleDocumentElement(OMNamespace testNamespace) {
        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMElement documentElement = factory.createOMElement("Employees", testNamespace);
        documentElement.declareNamespace(testNamespace);
        OMText txt = factory.createOMText(documentElement, " ");
        OMElement e = factory.createOMElement("Employee", testNamespace, documentElement);
        e.setText("Apache Developer");
        return documentElement;
    }
    
    public void testGetChildrenWithQName() {
        
        // Create a document with 2 children, each named "sample" but
        // have different namespaces.
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace a = factory.createOMNamespace(NS_A, "a");
        OMNamespace b = factory.createOMNamespace(NS_B, "b");
        
        OMElement documentElement = factory.createOMElement("Document", a);
        factory.createOMElement("sample", a, documentElement);
        factory.createOMElement("sample", b, documentElement);
        
        
        // Test for fully qualified names
        QName qName = new QName(NS_A, "sample");
        assertTrue(getChildrenCount(documentElement.getChildrenWithName(qName)) == 1);
        qName = new QName(NS_B, "sample");
        assertTrue(getChildrenCount(documentElement.getChildrenWithName(qName)) == 1);
        qName = new QName(NS_C, "sample");
        assertTrue(getChildrenCount(documentElement.getChildrenWithName(qName)) == 0);
        
        // Test for QName with no namespace.
        // The original Axiom implementation interpretted this as a wildcard.
        // In order to not break existing code, this should return 2
        qName = new QName("", "sample");
        assertTrue(getChildrenCount(documentElement.getChildrenWithName(qName)) == 2);
        
        // Now add an unqualified sample element to the documentElement
        factory.createOMElement("sample", null, documentElement);
        
        
        // Repeat the tests
        qName = new QName(NS_A, "sample");
        assertTrue(getChildrenCount(documentElement.getChildrenWithName(qName)) == 1);
        qName = new QName(NS_B, "sample");
        assertTrue(getChildrenCount(documentElement.getChildrenWithName(qName)) == 1);
        qName = new QName(NS_C, "sample");
        assertTrue(getChildrenCount(documentElement.getChildrenWithName(qName)) == 0);
       
        // Since there actually is an unqualified element child, the most accurate
        // interpretation of getChildrenWithName should be to return this one 
        // child
        qName = new QName("", "sample");
        assertTrue(getChildrenCount(documentElement.getChildrenWithName(qName)) == 1);
    }

    private int getChildrenCount(Iterator childrenIter) {
        int childCount = 0;
        while (childrenIter.hasNext()) {
            childrenIter.next();
            childCount++;
        }

        return childCount;
    }
}
