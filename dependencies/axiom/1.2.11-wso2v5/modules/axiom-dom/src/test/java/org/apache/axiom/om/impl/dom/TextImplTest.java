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

package org.apache.axiom.om.impl.dom;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class TextImplTest extends AbstractTestCase {
    public void testAppendText() {
        OMDOMFactory factory = new OMDOMFactory();
        String localName = "TestLocalName";
        String namespace = "http://ws.apache.org/axis2/ns";
        String prefix = "axis2";
        String tempText = "The quick brown fox jumps over the lazy dog";
        String textToAppend = " followed by another fox";

        OMElement elem = factory.createOMElement(localName, namespace, prefix);
        OMText textNode = factory.createOMText(elem, tempText);

        ((Text) textNode).appendData(textToAppend);

        assertEquals("Text value mismatch", tempText + textToAppend, textNode
                .getText());
    }

    public void testSplitText() throws Exception {
        DOMTestUtil.execute(new DOMTestUtil.Test() {
            public void execute(DocumentBuilderFactory dbf) throws Exception {
                String textValue = "temp text value";
        
                Document doc = dbf.newDocumentBuilder().newDocument();
        
                Element element = doc.createElement("test");
                Text txt = doc.createTextNode(textValue);
                element.appendChild(txt);
                txt.splitText(3);
        
                assertNotNull("Text value missing in the original Text node", txt
                        .getNodeValue());
        
                assertNotNull("Sibling missing after split", txt.getNextSibling());
                assertNotNull("Text value missing in the new split Text node", txt
                        .getNextSibling().getNodeValue());
        
                assertEquals("Incorrect split point", textValue.substring(0, 3), txt
                        .getNodeValue());
                assertEquals("Incorrect split point", textValue.substring(3, textValue
                        .length()), txt.getNextSibling().getNodeValue());
            }
        });
    }

}
