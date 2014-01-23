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

import javax.xml.stream.XMLStreamReader;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

public class PopulateArrayTest extends TestCase {
    private String xmlString = "<myobject xmlns=\"http://soapinterop.org2/xsd\">" +
            "<varFloat>3.3</varFloat>" +
            "<varInt>5</varInt>" +
            "<varString>Hello</varString>" +
            "<varString>Hello2</varString>" +
            "<varString>Hello3</varString>" +
            "</myobject>";

    public void testPopulate() throws Exception{

        XMLStreamReader reader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()));
        Class clazz = Class.forName("org2.soapinterop.xsd.Myobject");
        Class innerClazz = Util.getFactory(clazz);
        Method parseMethod = innerClazz.getMethod("parse",new Class[]{XMLStreamReader.class});
        Object obj = parseMethod.invoke(null,new Object[]{reader});

        Object soapStruct = null;
        BeanInfo beanInfo =  Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Method readMethod;
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            if ("myobject".equals(propertyDescriptor.getDisplayName())){
                readMethod = propertyDescriptor.getReadMethod();
                soapStruct = readMethod.invoke(obj, (Object[]) null);
                break;
            }
        }

        assertNotNull(soapStruct);

        BeanInfo structBeanInfo =  Introspector.getBeanInfo(soapStruct.getClass());
        PropertyDescriptor[] structPropertyDescriptors = structBeanInfo.getPropertyDescriptors();
        for (int i = 0; i < structPropertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = structPropertyDescriptors[i];
            if ("varInt".equals(propertyDescriptor.getDisplayName())){
                readMethod = propertyDescriptor.getReadMethod();
                assertEquals("varInt is not properly set",new Integer(5),
                        readMethod.invoke(soapStruct, (Object[]) null));
            } else if ("varFloat".equals(propertyDescriptor.getDisplayName())){
                readMethod = propertyDescriptor.getReadMethod();
                assertEquals("varFloat is not properly set",new Float(3.3),
                        readMethod.invoke(soapStruct, (Object[]) null));
            }  else if ("varString".equals(propertyDescriptor.getDisplayName())){
                readMethod = propertyDescriptor.getReadMethod();
                assertTrue("String array is not set",readMethod.getReturnType().isArray());
                Object array = readMethod.invoke(soapStruct, (Object[]) null);
                int length = Array.getLength(array);
                assertEquals("Array length is not correct",length,3);

                //check the items here
            }

        }


    }



}
