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

package org.apache.axis2.description;

import java.io.InputStream;
import java.util.Enumeration;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.axis2.AbstractTestCase;

/**
 * This class tests that AxisBindingOperation and AxisBindingMessage objects
 * include information about the message serialization format when the HTTP
 * binding is used. The test cases verify the following serialization mappings:
 * <ul>
 * <li>&lt;http:urlEncoded/&gt; : application/x-www-form-urlencoded
 * <li>&lt;mime:mimeXml/&gt; : text/xml
 * <li>&lt;mime:content type="ABC"/&gt; : ABC
 * </ul>
 */
public class HttpMessagesSerializationTest extends AbstractTestCase {

    public static final String HTTP_MESSAGE_SERIALIZATION_WSDL_PATH = 
        "wsdl/httpMessagesSerialization.wsdl";

    private AxisBindingOperation bindingOperation;
    private TestConfig testConfig;

    public HttpMessagesSerializationTest(String testName_) {
        super(testName_);
    }

    public String getName() {
        TestConfig testConfig = getTestConfig();
        return super.getName() + ". Expected Input Serialization: " +
            testConfig.getInputSerialization() +
            " ; Expected Output Serialization: " +
            testConfig.getOutputSerialization();
    }

    public static Test suite() {

        final String targetNamespace = "http://www.example.org";
        final QName serviceName = new QName(targetNamespace, "FooService");
        final String httpGetPortName = "FooHttpGetPort";
        final String httpPostPortName = "FooHttpPostPort";
        final QName fooOperationName = new QName(targetNamespace, "getFoo");
        final QName barOperationName = new QName(targetNamespace, "getBar");

        final String contentTypeTextXml = "text/xml";
        final String contentTypeUrlEncoded = "application/x-www-form-urlencoded";
        final String contentTypeImageJpeg = "image/jpeg";

        final String wsdlPath = HTTP_MESSAGE_SERIALIZATION_WSDL_PATH;

        TestConfig[] testConfigs = new TestConfig[] {
            new TestConfig(wsdlPath, serviceName, httpGetPortName,
                fooOperationName, contentTypeUrlEncoded, contentTypeTextXml),
            new TestConfig(wsdlPath, serviceName, httpGetPortName,
                barOperationName, contentTypeUrlEncoded, contentTypeImageJpeg),
            new TestConfig(wsdlPath, serviceName, httpPostPortName,
                fooOperationName, contentTypeTextXml, contentTypeTextXml),
            new TestConfig(wsdlPath, serviceName, httpPostPortName,
                barOperationName, contentTypeUrlEncoded, contentTypeTextXml)};

        String className = HttpMessagesSerializationTest.class.getName();
        TestSuite suite = new TestSuite(className);
        for (int i = 0; i < testConfigs.length; i++) {
            // Have JUnit create a TestCase instance for every test method of
            // this class.
            TestSuite testCases = new TestSuite(
                HttpMessagesSerializationTest.class, testConfigs[i].toString());

            // Loop through the TestCase instances and inject them with the test
            // data.
            Enumeration tests = testCases.tests();
            while (tests.hasMoreElements()) {
                HttpMessagesSerializationTest test = 
                    (HttpMessagesSerializationTest) tests.nextElement();
                test.setTestConfig(testConfigs[i]);
            }

            suite.addTest(testCases);
        }
        return suite;
    }

    public void setUp() throws Exception {
        TestConfig testConfig = getTestConfig();
        InputStream contentTypeWsdlIn = getTestResource(testConfig.getWsdlPath());
        // Populate AxisService (WSDL port)
        WSDL11ToAxisServiceBuilder wsdl11Builder = new WSDL11ToAxisServiceBuilder(
            contentTypeWsdlIn, testConfig.getServiceName(),
            testConfig.getPortName());
        AxisService service = wsdl11Builder.populateService();

        // Get Binding Operation
        AxisEndpoint endpoint = service.getEndpoint(testConfig.getPortName());
        AxisBinding binding = endpoint.getBinding();
        bindingOperation = (AxisBindingOperation) binding.getChild(testConfig.getOperationName());
    }

    // Input Serialization Format @ AxisBindingOperation
    public void testOperationInputSerialization() throws Exception {
        TestConfig testConfig = getTestConfig();
        String actualInputSerialization = (String) bindingOperation.getProperty(
            WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION);
        assertEquals("Input Serialization Format for the '" +
            testConfig.getOperationName() + "' operation is Incorrect",
            testConfig.getInputSerialization(), actualInputSerialization);
    }

    // Output Serialization Format @ AxisBindingOperation
    public void testOperationOutputSerialization() throws Exception {
        TestConfig testConfig = getTestConfig();
        String actualOutputSerialization = (String) bindingOperation.getProperty(
            WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION);
        assertEquals("Output Serialization Format for the '" +
            testConfig.getOperationName() + "' operation is Incorrect",
            testConfig.getOutputSerialization(), actualOutputSerialization);
    }

    public TestConfig getTestConfig() {
        return testConfig;
    }

    public void setTestConfig(TestConfig testConfig_) {
        testConfig = testConfig_;
    }

    private final static class TestConfig {

        private final String wsdlPath;
        private final QName serviceName;
        private final String portName;
        private final QName operationName;
        private final String inputSerialization;
        private final String outputSerialization;

        TestConfig(String wsdlPath, QName serviceName, String portName,
            QName operationName, String inputSerialization,
            String outputSerialization) {
            this.wsdlPath = wsdlPath;
            this.serviceName = serviceName;
            this.portName = portName;
            this.operationName = operationName;
            this.inputSerialization = inputSerialization;
            this.outputSerialization = outputSerialization;
        }

        public String getWsdlPath() {
            return wsdlPath;
        }

        public QName getServiceName() {
            return serviceName;
        }

        public String getPortName() {
            return portName;
        }

        public QName getOperationName() {
            return operationName;
        }

        public String getInputSerialization() {
            return inputSerialization;
        }

        public String getOutputSerialization() {
            return outputSerialization;
        }

        public String toString() {
            return "serviceName:" + serviceName + "  portName:" + portName +
                "  operationName:" + operationName +
                "  expectedInputSerialization:" + inputSerialization +
                "  expectedOutputSerialization:" + outputSerialization;
        }
    }

}
