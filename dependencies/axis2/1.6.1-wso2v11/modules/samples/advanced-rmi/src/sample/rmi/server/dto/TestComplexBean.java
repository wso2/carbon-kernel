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
package sample.rmi.server.dto;

import org.apache.axis2.rmi.databind.AbstractRMIBean;
import org.apache.axis2.rmi.databind.JavaObjectSerializer;
import org.apache.axis2.rmi.databind.XmlStreamParser;
import org.apache.axis2.rmi.util.NamespacePrefix;
import org.apache.axis2.rmi.exception.XmlSerializingException;
import org.apache.axis2.rmi.exception.XmlParsingException;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import sample.rmi.server.type.TestComplexType;


public class TestComplexBean extends AbstractRMIBean {

    private List testBeans;

    public TestComplexBean() {
        this.testBeans = new ArrayList();
    }

    public static Class getBeanClass(){
        return TestComplexType.class;
    }

    public void serialize(XMLStreamWriter writer,
                          JavaObjectSerializer serializer,
                          QName parentQName,
                          NamespacePrefix namespacePrefix) throws XMLStreamException, XmlSerializingException {
        // first write the start element
        writeStartElement(writer,
                parentQName.getNamespaceURI(),
                parentQName.getLocalPart(),
                namespacePrefix);
        String namespace = "http://ws.apache.org/axis2/rmi/samples/types";
        TestBean testBean;
        for (Iterator iter = testBeans.iterator();iter.hasNext();){
            testBean = (TestBean) iter.next();
            writeStartElement(writer,namespace,"param1",namespacePrefix);
            writer.writeCharacters(String.valueOf(testBean.getParam1()));
            writer.writeEndElement();
            writeStartElement(writer,namespace,"param2",namespacePrefix);
            writer.writeCharacters(testBean.getParam2());
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

     public static Object parse(XMLStreamReader reader,
                               XmlStreamParser parser)
            throws XMLStreamException, XmlParsingException {
         // we do not have attributes
         String namespace = "http://ws.apache.org/axis2/rmi/samples/types";
         QName parentQName = reader.getName();
         reader.next();
         while(!reader.isStartElement()){
             reader.next();
         }
         TestComplexBean testComplexBean = new TestComplexBean();
         TestBean testBean;
         // now reader is at the begining.
         while (!reader.isEndElement()) {
             testBean = new TestBean();
             if (reader.getName().equals(new QName(namespace, "param1"))) {
                 reader.next();
                 testBean.setParam1(Integer.parseInt(reader.getText()));
                 while (!reader.isEndElement()) {
                     reader.next();
                 }
             }
             reader.next();
             if (reader.getName().equals(new QName(namespace, "param2"))) {
                 reader.next();
                 testBean.setParam2(reader.getText());
                 while (!reader.isEndElement()) {
                     reader.next();
                 }
             }
             reader.next();
             testComplexBean.addTestBean(testBean);
         }
         return testComplexBean;
     }

    public void addTestBean(TestBean testBean){
        this.testBeans.add(testBean);
    }

    public List getTestBeans() {
        return testBeans;
    }

    public void setTestBeans(List testBeans) {
        this.testBeans = testBeans;
    }

}
