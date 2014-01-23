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
package org.apache.axiom.ts.om.builder;

import java.io.StringReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.ts.AxiomTestCase;

/**
 * Tests the behavior of {@link OMXMLParserWrapper#getDocumentElement(boolean)} with
 * <code>discardDocument</code> set to <code>true</code>.
 */
public class TestGetDocumentElementWithDiscardDocument extends AxiomTestCase {
    public TestGetDocumentElementWithDiscardDocument(OMMetaFactory metaFactory) {
        super(metaFactory);
    }

    protected void runTest() throws Throwable {
        OMFactory factory = metaFactory.getOMFactory();
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(factory,
                new StringReader("<!--comment1--><root/><!--comment2-->"));
        OMElement element = builder.getDocumentElement(true);
        assertEquals("root", element.getLocalName());
        assertFalse(element.isComplete());
        assertNull(element.getParent());
        // Note: we can't test getNextOMSibling here because this would build the element
        assertNull(element.getPreviousOMSibling());
        OMElement newParent = factory.createOMElement("newParent", null);
        newParent.addChild(element);
        assertFalse(element.isComplete());
    }
}
