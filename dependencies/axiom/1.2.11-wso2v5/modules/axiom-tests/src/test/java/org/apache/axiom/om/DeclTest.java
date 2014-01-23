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

import org.custommonkey.xmlunit.XMLTestCase;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class DeclTest extends XMLTestCase {
    private OMFactory factory;

    private OMDocument model;
    
    protected void setUp() throws Exception {
        super.setUp();
        factory = OMAbstractFactory.getOMFactory();
        model = factory.createOMDocument();
        OMElement root = factory.createOMElement("root", null);
        model.addChild(root);
    }

    private OMElement getElement(String name) {
        OMNamespace ns1 = factory.createOMNamespace("axiom:declaration-test,2007:1", "test");
        OMElement elem = factory.createOMElement(name, ns1);
        return elem;
    }


    private void writeModel(OutputStream os) throws XMLStreamException {
        model.serialize(os);
    }

    public void testDecl() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        model.getOMDocumentElement().addChild(getElement("foo"));
        model.getOMDocumentElement().addChild(getElement("bar"));
        model.getOMDocumentElement().addChild(getElement("foo"));
        writeModel(baos);

        String xmlExpected = "<?xml version=\"1.0\" encoding=\"utf-8\"?><root><test:foo xmlns:test=\"axiom:declaration-test,2007:1\"></test:foo><test:bar xmlns:test=\"axiom:declaration-test,2007:1\"></test:bar><test:foo xmlns:test=\"axiom:declaration-test,2007:1\"></test:foo></root>";
        this.assertXMLEqual(new InputStreamReader(new ByteArrayInputStream(xmlExpected.getBytes())),
                new InputStreamReader(new ByteArrayInputStream(baos.toByteArray())));

    }
}
