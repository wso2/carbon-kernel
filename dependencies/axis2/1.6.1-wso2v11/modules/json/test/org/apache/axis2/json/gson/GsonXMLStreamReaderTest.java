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

package org.apache.axis2.json.gson;

import com.google.gson.stream.JsonReader;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GsonXMLStreamReaderTest {
    
    
    @Test
    public void testGsonXMLStreamReader() throws Exception {
        String jsonString = "{\"response\":{\"return\":{\"name\":\"kate\",\"age\":\"35\",\"gender\":\"female\"}}}";
        String xmlString = "<response xmlns=\"http://www.w3schools.com\"><return><name>kate</name><age>35</age><gender>female</gender></return></response>";
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream , "UTF-8"));
        String fileName = "test-resources/custom_schema/testSchema_1.xsd";
        InputStream is = new FileInputStream(fileName);
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is), null);
        List<XmlSchema> schemaList = new ArrayList<XmlSchema>();
        schemaList.add(schema);
        QName elementQName = new QName("http://www.w3schools.com", "response");
        ConfigurationContext configCtxt = new ConfigurationContext(new AxisConfiguration());
        GsonXMLStreamReader gsonXMLStreamReader = new GsonXMLStreamReader(jsonReader);
        gsonXMLStreamReader.initXmlStreamReader(elementQName , schemaList , configCtxt);
        StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(gsonXMLStreamReader);
        OMElement omElement = stAXOMBuilder.getDocumentElement();
        String actual = omElement.toString();
        inputStream.close();
        is.close();
        Assert.assertEquals(xmlString , actual);

    }
}
