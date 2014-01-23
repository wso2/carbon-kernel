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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.ts.AxiomTestCase;

/**
 * Regression test for <a href="https://issues.apache.org/jira/browse/AXIOM-153">AXIOM-153</a>.
 */
public class TestInsertSiblingAfterLastChild extends AxiomTestCase {
    public TestInsertSiblingAfterLastChild(OMMetaFactory metaFactory) {
        super(metaFactory);
    }

    protected void runTest() throws Throwable {
        OMFactory fac = metaFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace("http://www.testuri.com","ns");
        OMElement parent = fac.createOMElement("parent", ns);
        
        // Create three OMElements
        OMElement c1 = fac.createOMElement("c1", ns);
        OMElement c2 = fac.createOMElement("c2", ns);
        OMElement c3 = fac.createOMElement("c3", ns);

        // Add c1 to parent using parent.addChild()
        parent.addChild(c1);
        // Add c2 to c1 as a sibling after
        c1.insertSiblingAfter(c2);
        // Now add c3 to parent using parent.addChild()
        parent.addChild(c3);
        assertXMLEqual("<ns:parent xmlns:ns=\"http://www.testuri.com\">" +
                "<ns:c1 /><ns:c2 /><ns:c3 /></ns:parent>", parent.toString());
    }
}
