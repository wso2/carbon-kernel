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

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class TransformTest extends TestCase {
    
    public void testTransform() throws Exception {
        OMFactory fac = new OMDOMFactory();
        OMDocument doc = fac.createOMDocument();
        OMElement element = fac.createOMElement(new QName("http://foo", "bar"));
        OMText text = fac.createOMText("test");
        element.addChild(text);
        doc.addChild(element);

        Document domDoc1 = ((ElementImpl)element).getOwnerDocument();
        assertNotNull(domDoc1);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DOMSource source = new DOMSource(domDoc1);
        Result result = new StreamResult(baos);
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
        
        byte [] data = baos.toByteArray();
        String xml = new String(data, 0, data.length);
        
        System.out.println(xml);
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document domDoc2 = db.parse(new InputSource(new StringReader(xml)));
        
        assertEquals("http://foo", domDoc2.getDocumentElement().getNamespaceURI());
        assertEquals("bar", domDoc2.getDocumentElement().getLocalName());
        assertEquals("test", domDoc2.getDocumentElement().getFirstChild().getNodeValue());
    }
}
