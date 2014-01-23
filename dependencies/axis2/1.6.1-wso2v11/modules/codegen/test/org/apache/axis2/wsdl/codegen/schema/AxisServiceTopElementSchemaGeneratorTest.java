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

package org.apache.axis2.wsdl.codegen.schema;

import junit.framework.TestCase;
import org.apache.axis2.namespace.Constants;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class AxisServiceTopElementSchemaGeneratorTest extends TestCase {

    public void testSchemaGeneration() throws Exception {

        AxisServiceTopElementSchemaGenerator schemaGenerator = new AxisServiceTopElementSchemaGenerator(null);

        Set topElements = new HashSet();

        TopElement topElement;

        topElement = new TopElement(new QName("http://test.com","testElement1"));
        topElements.add(topElement);
        topElement = new TopElement(new QName("http://test1.com","testElement2"));
        topElements.add(topElement);
        topElement = new TopElement(new QName("http://test1.com","testElement3"));
        topElement.setTypeQName(new QName(Constants.URI_2001_SCHEMA_XSD,"string"));
        topElements.add(topElement);

        topElement = new TopElement(new QName("http://test1.com","testElement4"));
        topElement.setTypeQName(new QName("http://test1.com","testComplexType1"));
        topElements.add(topElement);

        topElement = new TopElement(new QName("http://test1.com","testElement5"));
        topElement.setTypeQName(new QName("http://test.com","testComplexType2"));
        topElements.add(topElement);

        topElement = new TopElement(new QName("http://test.com","testElement6"));
        topElement.setTypeQName(new QName("http://test2.com","testComplexType2"));
        topElements.add(topElement);

        Map schemaMap = schemaGenerator.getSchemaMap(topElements);
        schemaGenerator.getXmlSchemaList(schemaMap);

//        List xmlSchemaList = schemaGenerator.getXmlSchemaList(schemaMap);
//        for (Object aXmlSchemaList : xmlSchemaList) {
//            ((org.apache.ws.commons.schema.XmlSchema)aXmlSchemaList).write(System.out);
//        }
    }
}
