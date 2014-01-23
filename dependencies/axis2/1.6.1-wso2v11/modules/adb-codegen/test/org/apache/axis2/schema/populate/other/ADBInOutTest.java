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

package org.apache.axis2.schema.populate.other;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

public class ADBInOutTest extends TestCase {

    private String xmlString = "<tempElt xmlns=\"http://soapinterop.org1/types\">" +
            "hello" +
            "</tempElt>";
    private String xmlString2 = "<header1 xmlns=\"http://soapinterop.org1/types\">" +
            "<varString>Hello</varString>" +
            "<varInt>5</varInt>" +
            "<varFloat>3.3</varFloat>" +
            "</header1>";

    public void testPopulate() throws Exception{

        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()));
        SOAPEnvelope defaultEnvelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        OMElement documentElement = new StAXOMBuilder(reader).getDocumentElement();
        defaultEnvelope.getBody().addChild(documentElement);
        XMLStreamReader xmlStreamReader = defaultEnvelope.getBody().getFirstElement().getXMLStreamReader();
        while(xmlStreamReader.hasNext()){
            System.out.println("event " + xmlStreamReader.next());
            System.out.println("text " +  (xmlStreamReader.hasText()?xmlStreamReader.getText():""));
            System.out.println("localName " + (xmlStreamReader.hasName()?xmlStreamReader.getLocalName():""));
        }
    }

    public void testPopulate2() throws Exception{

        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()));
        XMLStreamReader reader2 = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(xmlString2.getBytes()));

        SOAPFactory soap11Factory = OMAbstractFactory.getSOAP11Factory();

        SOAPEnvelope defaultEnvelope = soap11Factory.getDefaultEnvelope();
        OMElement documentElement = new StAXOMBuilder(reader).getDocumentElement();
        defaultEnvelope.getBody().addChild(documentElement);

        OMElement documentElement2 = new StAXOMBuilder(reader2).getDocumentElement();
        defaultEnvelope.getHeader().addChild(documentElement2);

        XMLStreamReader xmlStreamReader = defaultEnvelope.getBody().getFirstElement().getXMLStreamReader();
        while(xmlStreamReader.hasNext()){
            System.out.println("event " + xmlStreamReader.next());
            System.out.println("text " +  (xmlStreamReader.hasText()?xmlStreamReader.getText():""));
            System.out.println("localName " + (xmlStreamReader.hasName()?xmlStreamReader.getLocalName():""));
        }
    }
}
