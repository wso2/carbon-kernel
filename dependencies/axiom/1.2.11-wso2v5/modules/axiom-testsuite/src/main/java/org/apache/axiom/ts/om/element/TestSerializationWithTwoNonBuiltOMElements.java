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
package org.apache.axiom.ts.om.element;

import java.io.StringReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.ts.AxiomTestCase;

/**
 * Regression test for <a href="https://issues.apache.org/jira/browse/AXIOM-65">AXIOM-65</a>.
 */
public class TestSerializationWithTwoNonBuiltOMElements extends AxiomTestCase {
    public TestSerializationWithTwoNonBuiltOMElements(OMMetaFactory metaFactory) {
        super(metaFactory);
    }

    protected void runTest() throws Throwable {
        String sampleXMLOne = "<ChildOne><Name>ChildName</Name></ChildOne>";
        String sampleXMLTwo = "<ChildTwo><Name>ChildName</Name></ChildTwo>";

        String expectedXML =
                "<Root><ChildOne><Name>ChildName</Name></ChildOne><ChildTwo><Name>ChildName</Name></ChildTwo></Root>";
        OMFactory omFactory = metaFactory.getOMFactory();

        OMElement rootElement = omFactory.createOMElement("Root", null);
        OMElement childOne = OMXMLBuilderFactory.createOMBuilder(omFactory,
                new StringReader(sampleXMLOne)).getDocumentElement(true);
        rootElement.addChild(childOne);
        OMElement childTwo = OMXMLBuilderFactory.createOMBuilder(omFactory,
                new StringReader(sampleXMLTwo)).getDocumentElement(true);
        rootElement.addChild(childTwo);

        assertTrue(expectedXML.equals(rootElement.toString()));
        
        childOne.close(false);
        childTwo.close(false);
    }
}
