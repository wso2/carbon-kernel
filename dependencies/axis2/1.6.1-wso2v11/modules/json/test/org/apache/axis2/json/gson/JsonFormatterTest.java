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

import com.google.gson.Gson;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.json.gson.factory.JsonConstant;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class JsonFormatterTest {
    MessageContext outMsgContext;
    String contentType;
    String jsonString;
    SOAPEnvelope soapEnvelope;
    OMOutputFormat outputFormat;
    OutputStream outputStream;
    @Before
    public void setUp() throws Exception {
        contentType = "application/json-impl";
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
        soapEnvelope = soapFactory.getDefaultEnvelope();
        outputFormat = new OMOutputFormat();
        outputStream = new ByteArrayOutputStream();

        outMsgContext = new MessageContext();
        outMsgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-8");
    }

    @After
    public void tearDown() throws Exception {
        outputStream.close();
    }

    @Test
    public void testWriteToFaultMessage() throws Exception {
        jsonString = "{\"Fault\":{\"faultcode\":\"soapenv:Server\",\"faultstring\":\"javax.xml.stream.XMLStreamException\",\"detail\":\"testFaultMsg\"}}";
        outMsgContext.setProcessingFault(true);
        soapEnvelope.getBody().addChild(createFaultOMElement());
        outMsgContext.setEnvelope(soapEnvelope);
        JsonFormatter jsonFormatter = new JsonFormatter();
        jsonFormatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        String faultMsg = outputStream.toString();
        Assert.assertEquals(jsonString , faultMsg);
    }


    @Test
    public void testWriteToXMLtoJSON() throws Exception {
        jsonString = "{\"response\":{\"return\":{\"name\":\"kate\",\"age\":\"35\",\"gender\":\"female\"}}}";
        String fileName = "test-resources/custom_schema/testSchema_1.xsd";
        InputStream is = new FileInputStream(fileName);
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is), null);
        QName elementQName = new QName("http://www.w3schools.com", "response");
        ConfigurationContext configCtxt = new ConfigurationContext(new AxisConfiguration());
        outMsgContext.setConfigurationContext(configCtxt);
        AxisOperation axisOperation = AxisOperationFactory.getAxisOperation(AxisOperation.MEP_CONSTANT_IN_OUT);
        AxisMessage message = new AxisMessage();
        message.setElementQName(elementQName);
        axisOperation.addMessage(message , WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
        outMsgContext.setAxisOperation(axisOperation);
        AxisService axisService = new AxisService("testService");
        axisService.addSchema(schema);
        outMsgContext.setAxisService(axisService);
        soapEnvelope.getBody().addChild(getResponseOMElement());
        outMsgContext.setEnvelope(soapEnvelope);
        JsonFormatter jsonFormatter = new JsonFormatter();
        jsonFormatter.writeTo(outMsgContext, outputFormat , outputStream , false);
        String response = outputStream.toString();
        Assert.assertEquals(jsonString, response);
    }


    @Test
    public void testWriteToJSON() throws Exception {
        Person person = new Person();
        person.setName("Leo");
        person.setAge(27);
        person.setGender("Male");
        person.setSingle(true);
        outMsgContext.setProperty(JsonConstant.RETURN_OBJECT, person);
        outMsgContext.setProperty(JsonConstant.RETURN_TYPE, Person.class);
        jsonString = "{\""+ JsonConstant.RESPONSE +"\":" + new Gson().toJson(person, Person.class) + "}";

        JsonFormatter jsonFormatter = new JsonFormatter();
        jsonFormatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        String personString = outputStream.toString();
        Assert.assertEquals(jsonString, personString);

    }


    private OMElement createFaultOMElement() {
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = omFactory.createOMNamespace("", "");
        OMElement faultCode = omFactory.createOMElement("faultcode", ns);
        faultCode.setText("soapenv:Server");
        OMElement faultString = omFactory.createOMElement("faultstring", ns);
        faultString.setText("javax.xml.stream.XMLStreamException");
        OMElement detail = omFactory.createOMElement("detail", ns);
        detail.setText("testFaultMsg");
        OMElement fault = omFactory.createOMElement("Fault", ns);
        fault.addChild(faultCode);
        fault.addChild(faultString);
        fault.addChild(detail);
        return  fault;
    }

    private OMElement getResponseOMElement() {
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = omFactory.createOMNamespace("", "");

        OMElement response = omFactory.createOMElement("response", ns);
        OMElement ret = omFactory.createOMElement("return", ns);
        OMElement name = omFactory.createOMElement("name", ns);
        name.setText("kate");
        OMElement age = omFactory.createOMElement("age", ns);
        age.setText("35");
        OMElement gender = omFactory.createOMElement("gender", ns);
        gender.setText("female");
        ret.addChild(name);
        ret.addChild(age);
        ret.addChild(gender);
        response.addChild(ret);
        return response;
    }

    public static class Person {

        private String name;
        private int age;
        private String gender;
        private boolean single;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public boolean isSingle() {
            return single;
        }

        public void setSingle(boolean single) {
            this.single = single;
        }
    }
}
