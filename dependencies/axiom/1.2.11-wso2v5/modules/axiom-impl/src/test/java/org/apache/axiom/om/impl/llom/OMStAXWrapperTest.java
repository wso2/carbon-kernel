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
package org.apache.axiom.om.impl.llom;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.ds.CharArrayDataSource;
import org.apache.axiom.om.impl.OMStAXWrapperTestBase;
import org.apache.axiom.om.impl.llom.factory.OMLinkedListMetaFactory;

public class OMStAXWrapperTest extends OMStAXWrapperTestBase {
    public OMStAXWrapperTest() {
        super(new OMLinkedListMetaFactory());
    }
    
    // Test for WSCOMMONS-453
    public void _testOMSourcedElementDescendent() throws Exception {
        OMFactory omFactory = omMetaFactory.getOMFactory();
        OMDataSource ds = new CharArrayDataSource("<a>test</a>".toCharArray());
        OMElement root = omFactory.createOMElement(new QName("root"));
        OMSourcedElement child = omFactory.createOMElement(ds, "a", null);
        root.addChild(child);
        assertFalse(child.isExpanded());
        XMLStreamReader stream = root.getXMLStreamReader();
        assertEquals(XMLStreamReader.START_ELEMENT, stream.next());
        assertEquals("root", stream.getLocalName());
        assertEquals(XMLStreamReader.START_ELEMENT, stream.next());
        assertEquals(XMLStreamReader.CHARACTERS, stream.next());
        assertEquals("test", stream.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, stream.next());
        assertEquals(XMLStreamReader.END_ELEMENT, stream.next());
        assertEquals(XMLStreamReader.END_DOCUMENT, stream.next());
        assertFalse(child.isExpanded());
    }
}
