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
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;

public class PopulateAnyTypeTest extends TestCase {

     private String xmlString = "<myObject xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://soapinterop.org/xsd2\" xsi:type=\"xsd:int\">" +
            "5" +
            "</myObject>";

    public void testPopulate() throws Exception{

               XMLStreamReader reader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()));
               Class clazz = Class.forName("org.soapinterop.xsd2.MyObject");
               Class innerClazz = Util.getFactory(clazz);
               Method parseMethod = innerClazz.getMethod("parse",new Class[]{XMLStreamReader.class});
               Object obj = parseMethod.invoke(null,new Object[]{reader});




    }

}
