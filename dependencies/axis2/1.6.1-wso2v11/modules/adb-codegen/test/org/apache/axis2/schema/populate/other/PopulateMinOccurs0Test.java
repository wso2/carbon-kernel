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

public class PopulateMinOccurs0Test extends TestCase {

    /*
     <xs:element name="A" type="xs:string" minOccurs="1" maxOccurs="unbounded"/>
	 <xs:element name="B" type="xs:string" minOccurs="0" maxOccurs="unbounded"/>
	 <xs:element name="C" type="xs:string" minOccurs="0" maxOccurs="unbounded"/>
	 <xs:element name="D" type="xs:string" minOccurs="0" maxOccurs="1"/>
   */

    // element D is missing
    private String xmlString1 = "<root xmlns=\"http://test.org\">" +
            "<A>I am A</A>" +
            "<B>I am B1</B>" +
            "<B>I am B2</B>" +
            "<C>I am C1</C>" +
            "<C>I am C2</C>" +
            "</root>";

    //B elements are missing
    private String xmlString2 = "<root xmlns=\"http://test.org\">" +
            "<A>I am A</A>" +
            "<C>I am B2</C>" +
            "<C>I am B2</C>" +
            "<D>I am D1</D>" +
            "</root>";

    //Only A is present
    private String xmlString3 = "<root xmlns=\"http://test.org\">" +
            "<A>I am A</A>" +
            "</root>";


    public void testPopulate1() throws Exception {
        populateAndAssert(xmlString1, 2, "b");
        populateAndAssert(xmlString1, 2, "c");
    }

    public void testPopulate2() throws Exception {
        populateAndAssert(xmlString2, 0, "b");
        populateAndAssert(xmlString2, 2, "c");
    }

    public void testPopulate3() throws Exception {
        populateAndAssert(xmlString3, 0, "b");
        populateAndAssert(xmlString3, 1, "a");
    }

    private void populateAndAssert(String s, int expectedCount,
                                   String itemtoTest) throws XMLStreamException,
            ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException,
            IntrospectionException {
        XMLStreamReader reader =
                StAXUtils.createXMLStreamReader(new ByteArrayInputStream(s.getBytes()));
        Class clazz = Class.forName("org.test.Root");
        Class innerClazz = Util.getFactory(clazz);
        Method parseMethod = innerClazz.getMethod("parse", new Class[]{XMLStreamReader.class});
        Object obj = parseMethod.invoke(null, new Object[]{reader});

        assertNotNull(obj);

        Object stringArray = null;
        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Method readMethod;

        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            if (itemtoTest.equals(propertyDescriptor.getDisplayName())) {
                readMethod = propertyDescriptor.getReadMethod();
                stringArray = readMethod.invoke(obj, null);
                break;
            }
        }

        if (expectedCount!=0){
            assertNotNull(stringArray);
            String[] array = (String[]) stringArray;
            assertEquals(array.length, expectedCount);
        }
    }


}