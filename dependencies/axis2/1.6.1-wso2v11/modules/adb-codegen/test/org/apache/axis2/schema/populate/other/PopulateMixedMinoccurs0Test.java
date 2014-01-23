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
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.schema.populate.Util;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PopulateMixedMinoccurs0Test extends TestCase {

    /*

    <xsd:element name="stringListResponseElement" type="tns:StringListResponseType"/>
    <xsd:complexType name="StringListResponseType">
        <xsd:choice>
            <xsd:element name="stringList" type="tns:StringList"/>
            <xsd:element name="exception" type="tns:ExceptionType"/>
        </xsd:choice>
    </xsd:complexType>
    <xsd:complexType name="StringList">
        <xsd:sequence>
            <xsd:element name="s" type="xsd:string" minOccurs="0" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

   */


    private String xmlString1 = "<stringListResponseElement " +
            "xmlns=\"http://recursion1.org\">" +
            "<stringList>" +
            "<s>item1</s>" +
            "<s>item2</s>" +
            "<s>item3</s>" +
            "</stringList>" +
            "</stringListResponseElement>";

    private String xmlString2 = "<stringListResponseElement " +
            "xmlns=\"http://recursion1.org\">" +
            "<stringList>" +
            "</stringList>" +
            "</stringListResponseElement>";


    public void testPopulate1() throws Exception {
        populateAndAssert(xmlString1,3);
    }

    public void testPopulate2() throws Exception {
        populateAndAssert(xmlString2,0);
    }


    private void populateAndAssert(String s,int count) throws XMLStreamException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, IntrospectionException {
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(s.getBytes()));
        Class clazz = Class.forName("org.recursion1.StringListResponseElement");
        Class innerClazz = Util.getFactory(clazz);
        Method parseMethod = innerClazz.getMethod("parse", new Class[]{XMLStreamReader.class});
        Object obj = parseMethod.invoke(null, new Object[]{reader});

        assertNotNull(obj);

        Object stringListResponseElement = null;
        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Method readMethod;

        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            String displayName = propertyDescriptor.getDisplayName();
            if ("stringListResponseElement".equals(displayName)) {
                readMethod = propertyDescriptor.getReadMethod();
                stringListResponseElement = readMethod.invoke(obj, null);
                break;
            }
        }

        assertNotNull(stringListResponseElement);

        beanInfo = Introspector.getBeanInfo(stringListResponseElement.getClass());
        propertyDescriptors = beanInfo.getPropertyDescriptors();
        Object stringArray = null;
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            String displayName = propertyDescriptor.getDisplayName();
            if ("stringList".equals(displayName)) {
                readMethod = propertyDescriptor.getReadMethod();
                stringArray = readMethod.invoke(stringListResponseElement, null);
                break;
            }
        }

        assertNotNull(stringArray);

        beanInfo = Introspector.getBeanInfo(stringArray.getClass());
        propertyDescriptors = beanInfo.getPropertyDescriptors();
        Object sArray = null;
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            String displayName = propertyDescriptor.getDisplayName();
            if ("stringList".equals(displayName)) {
                readMethod = propertyDescriptor.getReadMethod();
                sArray = readMethod.invoke(stringArray, null);
                break;
            }
        }
        if (sArray!=null){
            Object[] array = (Object[])sArray;
            assertEquals(count,array.length);
        }

    }
}
