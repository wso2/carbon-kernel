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

package org.apache.axis2.json.gson.factory;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class XmlNodeGeneratorTest {

    @Test
    public void testMainXMLNode() throws Exception {

        QName elementQName = new QName("http://test.json.axis2.apache.org" ,"echoPerson");

        String fileName = "test-resources/custom_schema/testSchema_2.xsd";
        InputStream is = new FileInputStream(fileName);
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is), null);

        List<XmlSchema> schemaList = new ArrayList<XmlSchema>();
        schemaList.add(schema);

        XmlNodeGenerator xmlNodeGenerator = new XmlNodeGenerator(schemaList, elementQName);

        XmlNode mainXmlNode = xmlNodeGenerator.getMainXmlNode();

        Assert.assertNotNull(mainXmlNode);
        Assert.assertEquals("echoPerson", mainXmlNode.getName());
        Assert.assertEquals(1, mainXmlNode.getChildrenList().size());
        Assert.assertEquals("http://test.json.axis2.apache.org" , mainXmlNode.getNamespaceUri());

        Assert.assertEquals("arg0", mainXmlNode.getChildrenList().get(0).getName());
        Assert.assertEquals(3, mainXmlNode.getChildrenList().get(0).getChildrenList().size());

        Assert.assertEquals("name", mainXmlNode.getChildrenList().get(0).getChildrenList().get(0).getName());
        Assert.assertEquals(0, mainXmlNode.getChildrenList().get(0).getChildrenList().get(0).getChildrenList().size());

        Assert.assertEquals("age", mainXmlNode.getChildrenList().get(0).getChildrenList().get(1).getName());
        Assert.assertEquals(0, mainXmlNode.getChildrenList().get(0).getChildrenList().get(1).getChildrenList().size());

        Assert.assertEquals("gender", mainXmlNode.getChildrenList().get(0).getChildrenList().get(2).getName());
        Assert.assertEquals(0, mainXmlNode.getChildrenList().get(0).getChildrenList().get(2).getChildrenList().size());


    }
}
