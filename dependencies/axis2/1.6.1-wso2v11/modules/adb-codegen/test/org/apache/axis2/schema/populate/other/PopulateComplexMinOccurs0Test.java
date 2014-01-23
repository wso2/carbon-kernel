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

public class PopulateComplexMinOccurs0Test  extends TestCase {



    // all are present
    private String xmlString1 = "<root xmlns=\"http://test1.org\">" +
            "<city>Colombo</city>" +
            "<person>" +
            "<name>Amy</name>" +
            "<age>10</age>" +
            "</person>" +
            "<address>" +
            "<streetName>My Street</streetName>" +
            "<streetNo>1</streetNo>" +
            "</address>" +
            "</root>";



    //person element is missing
    private String xmlString2 = "<root xmlns=\"http://test1.org\">" +
            "<city>Colombo</city>" +
            "<address>" +
            "<streetName>My Street</streetName>" +
            "<streetNo>1</streetNo>" +
            "</address>" +
            "</root>";

    //Only city is present
    private String xmlString3 = "<root xmlns=\"http://test1.org\">" +
            "<city>Colombo</city>" +
            "</root>";


    // all are present with two addresses
    private String xmlString4 = "<root xmlns=\"http://test1.org\">" +
            "<city>Colombo</city>" +
            "<person>" +
            "<name>Amy</name>" +
            "<age>10</age>" +
            "</person>" +
            "<address>" +
            "<streetName>My Street</streetName>" +
            "<streetNo>1</streetNo>" +
            "</address>" +
            "<address>" +
            "<streetName>My Street2</streetName>" +
            "<streetNo>2</streetNo>" +
            "</address>" +
            "</root>";
    public void testPopulate1() throws Exception {
        populateAndAssert(xmlString1, 1, true);
    }

    public void testPopulate2() throws Exception {
        populateAndAssert(xmlString2, 1, false);
    }

    public void testPopulate3() throws Exception {
        populateAndAssert(xmlString3, 0, false);
    }

    public void testPopulate4() throws Exception {
        populateAndAssert(xmlString4, 2, true);
    }

    private void populateAndAssert(String s,
                                   int expectedAddressCount,
                                   boolean personPresent) throws XMLStreamException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, IntrospectionException {
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(s.getBytes()));
        Class clazz = Class.forName("org.test1.Root");
        Class innerClazz = Util.getFactory(clazz);
        Method parseMethod = innerClazz.getMethod("parse", new Class[]{XMLStreamReader.class});
        Object obj = parseMethod.invoke(null, new Object[]{reader});

        assertNotNull(obj);

        Object personObject = null;
        Object addressObject = null;
        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Method readMethod;

        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            if ("person".equals(propertyDescriptor.getDisplayName())) {
                readMethod = propertyDescriptor.getReadMethod();
                personObject = readMethod.invoke(obj, null);

                if (personPresent)
                    assertNotNull(personObject);
                else
                    assertNull(personObject);
            }else if ("address".equals(propertyDescriptor.getDisplayName())) {
                readMethod = propertyDescriptor.getReadMethod();
                addressObject = readMethod.invoke(obj, null);
                if (expectedAddressCount!=0) {
                    Object[] objArray = (Object[])addressObject;
                    assertEquals(expectedAddressCount,objArray.length);
                }
            }
        }


    }

}
