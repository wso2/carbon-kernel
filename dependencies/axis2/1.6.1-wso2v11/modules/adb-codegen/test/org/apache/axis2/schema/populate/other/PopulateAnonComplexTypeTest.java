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

public class PopulateAnonComplexTypeTest extends TestCase {
    private String xmlString = "<tempElt xmlns=\"http://soapinterop.org1/types\">" +
            "<varString>Hello</varString>" +
            "<varInt>5</varInt>" +
            "<varFloat>3.3</varFloat>" +
            "</tempElt>";

    public void testPopulate() throws Exception{

        XMLStreamReader reader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()));
        Class clazz = Class.forName("org1.soapinterop.types.TempElt");
        Class innerClazz = Util.getFactory(clazz);
        Method parseMethod = innerClazz.getMethod("parse",new Class[]{XMLStreamReader.class});
        Object obj = parseMethod.invoke(null,new Object[]{reader});


        BeanInfo beanInfo =  Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Method readMethod;
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            if ("varInt".equals(propertyDescriptor.getDisplayName())){
                readMethod = propertyDescriptor.getReadMethod();
                assertEquals(new Integer(5),readMethod.invoke(obj,(Object[]) null));
            } else if ("varFloat".equals(propertyDescriptor.getDisplayName())){
                readMethod = propertyDescriptor.getReadMethod();
                assertEquals(new Float(3.3),readMethod.invoke(obj,
                        (Object[]) null));
            }  else if ("varString".equals(propertyDescriptor.getDisplayName())){
                readMethod = propertyDescriptor.getReadMethod();
                assertEquals("Hello",readMethod.invoke(obj, (Object[]) null));
            }

        }

    }



}

