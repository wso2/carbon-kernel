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
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.json.gson.factory.JsonConstant;
import org.apache.axis2.json.gson.rpc.JsonRpcMessageReceiver;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;


public class JSONMessageHandlerTest {

    private MessageContext messageContext;
    private AxisConfiguration axisConfiguration;
    private ConfigurationContext configurationContext;
    private AxisService axisService;
    private AxisMessage message;
    private MessageReceiver messageReceiver;
    AxisOperation axisOperation;

    JSONMessageHandler jsonMessageHandler;
    GsonXMLStreamReader gsonXMLStreamReader;

    @Before
    public void setUp() throws Exception {
        messageContext = new MessageContext();
        axisConfiguration = new AxisConfiguration();
        configurationContext = new ConfigurationContext(axisConfiguration);
        axisService = new AxisService("Dummy Service");
        message = new AxisMessage();
        axisOperation = AxisOperationFactory.getAxisOperation(WSDLConstants.MEP_CONSTANT_IN_OUT);
        jsonMessageHandler = new JSONMessageHandler();


        String fileName = "test-resources/custom_schema/testSchema_2.xsd";
        InputStream is = new FileInputStream(fileName);
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is), null);


        QName elementQName = new QName("http://test.json.axis2.apache.org" ,"echoPerson");
        message.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
        message.setElementQName(elementQName);
        message.setParent(axisOperation);
        axisOperation.addMessage(message, WSDLConstants.MESSAGE_LABEL_IN_VALUE);

        axisService.addSchema(schema);
        axisService.addOperation(axisOperation);

        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();

        messageContext.setConfigurationContext(configurationContext);
        messageContext.setAxisService(axisService);
        messageContext.setAxisOperation(axisOperation);
        messageContext.setEnvelope(soapEnvelope);
    }

    @After
    public void tearDown() throws Exception {
        messageContext = null;
        axisConfiguration = null;
        configurationContext = null;
        axisService = null;
        message = null;
        messageReceiver = null;
        axisOperation = null;

        jsonMessageHandler = null;
        gsonXMLStreamReader = null;



    }

    @Test
    public void testInvoke() throws Exception {

        String jsonRequest = "{\"echoPerson\":{\"arg0\":{\"name\":\"Simon\",\"age\":\"35\",\"gender\":\"male\"}}}";
        JsonReader jsonReader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(jsonRequest.getBytes()),"UTF-8"));
        gsonXMLStreamReader = new GsonXMLStreamReader(jsonReader);

        messageReceiver = new RPCMessageReceiver();
        axisOperation.setMessageReceiver(messageReceiver);


        messageContext.setProperty(JsonConstant.IS_JSON_STREAM, true);
        messageContext.setProperty(JsonConstant.GSON_XML_STREAM_READER, gsonXMLStreamReader);

        jsonMessageHandler.invoke(messageContext);

        String expected = "<echoPerson xmlns=\"http://test.json.axis2.apache.org\"><arg0><name>Simon</name><age>35</age><gender>male</gender></arg0></echoPerson>";

        OMElement omElement = messageContext.getEnvelope().getBody().getFirstElement();
        String elementString = omElement.toStringWithConsume();

        Assert.assertEquals(expected , elementString);

    }

    @Test
    public void testInvoke_2() throws Exception {
        String jsonRequest = "{\"echoPerson\":{\"arg0\":{\"name\":\"Simon\",\"age\":\"35\",\"gender\":\"male\"}}}";
        JsonReader jsonReader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(jsonRequest.getBytes()),"UTF-8"));
        gsonXMLStreamReader = new GsonXMLStreamReader(jsonReader);

        messageReceiver = new JsonRpcMessageReceiver();
        axisOperation.setMessageReceiver(messageReceiver);

        messageContext.setProperty(JsonConstant.IS_JSON_STREAM, true);
        messageContext.setProperty(JsonConstant.GSON_XML_STREAM_READER, gsonXMLStreamReader);

        jsonMessageHandler.invoke(messageContext);

        GsonXMLStreamReader gsonStreamReader = (GsonXMLStreamReader) messageContext.getProperty(JsonConstant.GSON_XML_STREAM_READER);

        Assert.assertEquals(false, gsonStreamReader.isProcessed());
    }

    @Test
    public void testInvokeWithNullGsonXMLStreamReader() throws Exception {
        messageContext.setProperty(JsonConstant.IS_JSON_STREAM, true);

        try {
            jsonMessageHandler.invoke(messageContext);
            Assert.assertFalse(true);
        } catch (AxisFault axisFault) {
            Assert.assertEquals("GsonXMLStreamReader should not be null" ,axisFault.getMessage());
        }
    }
}
