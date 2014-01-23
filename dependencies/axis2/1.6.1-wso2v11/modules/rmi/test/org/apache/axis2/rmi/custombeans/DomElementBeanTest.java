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

package org.apache.axis2.rmi.custombeans;

import junit.framework.TestCase;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.rmi.exception.XmlParsingException;
import org.apache.axis2.rmi.exception.XmlSerializingException;
import org.apache.axis2.rmi.util.NamespacePrefix;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;


public class DomElementBeanTest extends TestCase {

    public void testParse(){
         String xmlString = "<starttag><xsd:schema targetNamespace=\"http://ws.apache.org/rmi/types\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
                 "            <xsd:complexType name=\"MapType\">" +
                 "                <xsd:sequence>" +
                 "                    <xsd:element form=\"unqualified\" minOccurs=\"0\" name=\"key\" nillable=\"true\" type=\"xsd:anyType\"/>" +
                 "                    <xsd:element form=\"unqualified\" minOccurs=\"0\" name=\"value\" nillable=\"true\" type=\"xsd:anyType\"/>" +
                 "                </xsd:sequence>" +
                 "            </xsd:complexType>" +
                 "        </xsd:schema></starttag>";
        try {
            XMLStreamReader reader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()));

            Element domElement = (Element) DomElementBean.parse(reader,null);

            // serializing the document
            StringWriter stringWriter = new StringWriter();
            XMLStreamWriter xmlStreamWriter = StAXUtils.createXMLStreamWriter(stringWriter);
            DomElementBean domElementBean = new DomElementBean(domElement);
            domElementBean.serialize(xmlStreamWriter,null,new QName("http://test","starttag"),new NamespacePrefix());
            xmlStreamWriter.flush();
            System.out.println("xml string ==>" + stringWriter.toString());
            System.out.println("OK");
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (XmlParsingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (XmlSerializingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
