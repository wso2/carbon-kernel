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
import java.lang.reflect.Method;

public class PopulateParticleAllTest extends TestCase {
    private String xmlString = "<myParticleAllElement xmlns=\"http://soapinterop.org/types\">" +
            "<varFloat>3.3</varFloat>" +
            "<varString>foo</varString>" +
            "</myParticleAllElement>";

    public void testPopulate() throws Exception{

        XMLStreamReader reader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()));
        Class clazz = Class.forName("org.soapinterop.types.MyParticleAllElement");
        Class innerClazz = Util.getFactory(clazz);
        Method parseMethod = innerClazz.getMethod("parse",new Class[]{XMLStreamReader.class});
        Object obj = parseMethod.invoke(null,new Object[]{reader});

        Object myParticleAllElement = null;
        BeanInfo beanInfo =  Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Method readMethod;
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            if ("myParticleAllElement".equals(propertyDescriptor.getDisplayName())){
                readMethod = propertyDescriptor.getReadMethod();
                myParticleAllElement = readMethod.invoke(obj, (Object[]) null);
                break;
            }
        }
        assertNotNull(myParticleAllElement);

        BeanInfo structBeanInfo =  Introspector.getBeanInfo(myParticleAllElement.getClass());
        PropertyDescriptor[] structPropertyDescriptors = structBeanInfo.getPropertyDescriptors();
        for (int i = 0; i < structPropertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = structPropertyDescriptors[i];
            if ("varFloat".equals(propertyDescriptor.getDisplayName())){
                readMethod = propertyDescriptor.getReadMethod();
                assertEquals("varFloat is not properly set",new Float(3.3),
                        readMethod.invoke(myParticleAllElement, (Object[]) null));

            }else if ("varString".equals(propertyDescriptor.getDisplayName())){
                readMethod = propertyDescriptor.getReadMethod();
                //this should not be set ! - it should return zero
                assertEquals("varString is not properly set","foo",
                        readMethod.invoke(myParticleAllElement, (Object[]) null));

            }else if ("varInt".equals(propertyDescriptor.getDisplayName())){
                readMethod = propertyDescriptor.getReadMethod();
                //this should not be set ! - it should return zero
                assertEquals("varInt is not properly set",new Integer(0),
                        readMethod.invoke(myParticleAllElement, (Object[]) null));
            }

        }
    }
}
