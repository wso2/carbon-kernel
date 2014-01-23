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

package org.apache.axis2.description.java2wsdl;

import org.apache.axis2.description.AxisService;
import org.apache.ws.commons.schema.XmlSchema;

import junit.framework.TestCase;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class DefaultSchemaGeneratorTest extends TestCase {

    public static class TestWebService {
        
    }
    
    public void testGeneratesExtraClass() throws Exception {
        
        AxisService axisService = new AxisService();
        
        DefaultSchemaGenerator generator = new DefaultSchemaGenerator(getClass().getClassLoader(),
                TestWebService.class.getName(), "http://example.org", "ex", axisService);
        ArrayList<String> extraClasses = new ArrayList<String>();
        extraClasses.add(ExtraClass.class.getName());
        generator.setExtraClasses(extraClasses);
        
        Collection<XmlSchema> schemaColl = generator.generateSchema();
        assertEquals(1, schemaColl.size());
        XmlSchema schema = schemaColl.iterator().next();

        boolean foundExtra = false;
        Iterator names = schema.getElements().getNames();
        while (names.hasNext()) {
            QName name = (QName) names.next();
            if (name.getLocalPart().equals("ExtraClass"))
                foundExtra = true;
        }
        assertTrue(foundExtra);
    }
}
