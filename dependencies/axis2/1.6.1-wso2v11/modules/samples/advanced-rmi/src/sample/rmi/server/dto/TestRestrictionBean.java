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
import org.apache.axis2.rmi.exception.XmlParsingException;
import org.apache.axis2.rmi.exception.XmlSerializingException;
import org.apache.axis2.rmi.util.NamespacePrefix;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import sample.rmi.server.type.TestRestrictionType;


public class TestRestrictionBean extends AbstractRMIBean {
    
    private String param1;

    public TestRestrictionBean(String param1) {
        this.param1 = param1;
    }

    public void serialize(XMLStreamWriter writer,
                          JavaObjectSerializer serializer,
                          QName parentQName,
                          NamespacePrefix namespacePrefix)
            throws XMLStreamException, XmlSerializingException {
        writeStartElement(writer, parentQName.getNamespaceURI(), parentQName.getLocalPart(), namespacePrefix);
        writer.writeCharacters(param1);
        writer.writeEndElement();
    }

    public static Class getBeanClass(){
        return TestRestrictionType.class;
    }

    public static Object parse(XMLStreamReader reader,
                               XmlStreamParser parser)
            throws XMLStreamException, XmlParsingException {

       reader.next();
       TestRestrictionBean testRestrictionBean = new TestRestrictionBean(reader.getText());
       while (reader.isEndElement()){
           reader.next();
       }
       return testRestrictionBean;
    }


    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }
}
