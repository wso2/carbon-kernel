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
import java.io.StringWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.ts.AxiomTestCase;

/**
 * Tests that {@link OMElement#serializeAndConsume(java.io.Writer)} consumes incomplete descendants,
 * even if the element itself is complete (more precisely, created programmatically). This situation
 * may occur when an element obtained from
 * {@link org.apache.axiom.om.OMXMLParserWrapper#getDocumentElement(boolean)} (with
 * <code>discardDocument</code> set to true) is added to an existing tree.
 */
public class TestSerializeAndConsumeWithIncompleteDescendant extends AxiomTestCase {
    public TestSerializeAndConsumeWithIncompleteDescendant(OMMetaFactory metaFactory) {
        super(metaFactory);
    }

    protected void runTest() throws Throwable {
        OMFactory factory = metaFactory.getOMFactory();
        OMElement incompleteElement = OMXMLBuilderFactory.createOMBuilder(factory,
                new StringReader("<elem>text</elem>")).getDocumentElement(true);
        OMElement root = factory.createOMElement("root", null);
        OMElement child = factory.createOMElement("child", null, root);
        child.addChild(incompleteElement);
        StringWriter out = new StringWriter();
        root.serializeAndConsume(out);
        assertEquals("<root><child><elem>text</elem></child></root>", out.toString());
        assertConsumed(incompleteElement);
    }
}
