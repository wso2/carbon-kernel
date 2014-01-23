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
package org.apache.axiom.ts.om.node;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.ts.AxiomTestCase;

/**
 * Tests that {@link OMNode#insertSiblingAfter(OMNode)} fails if the node doesn't have a parent.
 */
public class TestInsertSiblingBeforeOnOrphan extends AxiomTestCase {
    public TestInsertSiblingBeforeOnOrphan(OMMetaFactory metaFactory) {
        super(metaFactory);
    }

    protected void runTest() throws Throwable {
        OMFactory fac = metaFactory.getOMFactory();
        OMText text1 = fac.createOMText("text1");
        OMText text2 = fac.createOMText("text2");
        try {
            text1.insertSiblingAfter(text2);
            fail("Expected OMException because node has no parent");
        } catch (OMException ex) {
            // Expected
        }
    }
}
