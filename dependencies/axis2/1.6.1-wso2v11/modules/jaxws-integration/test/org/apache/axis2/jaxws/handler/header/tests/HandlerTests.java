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

package org.apache.axis2.jaxws.handler.header.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;

import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

public class HandlerTests extends AbstractTestCase {
    public static Test suite() {
        return getTestSetup(new TestSuite(HandlerTests.class));
    }

    public void testHandler_getHeader_invocation() {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        Object res;
        //Add myHeader to SOAPMessage that will be injected by handler.getHeader().
        String soapMessage =
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:demo=\"http://demo/\"><soap:Header> <demo:myheader soap:mustUnderstand=\"1\"/></soap:Header><soap:Body><demo:echo><arg0>test</arg0></demo:echo></soap:Body></soap:Envelope>";
        String url = "http://localhost:6060/axis2/services/DemoService.DemoServicePort";
        QName name = new QName("http://demo/", "DemoService");
        QName portName = new QName("http://demo/", "DemoServicePort");
        //Create Service
        Service s = Service.create(name);
        assertNotNull(s);
        //add port
        s.addPort(portName, null, url);

        //Create Dispatch
        Dispatch<String> dispatch = s.createDispatch(portName, String.class, Service.Mode.MESSAGE);
        assertNotNull(dispatch);
        res = dispatch.invoke(soapMessage);
        assertNotNull(res);
        TestLogger.logger.debug("----------------------------------");
    }

    public void test_MU_Failure() {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        //Add bad header to SOAPMessage, we expect MU to fail
        String soapMessage =
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:demo=\"http://demo/\"><soap:Header> <demo:badHeader soap:mustUnderstand=\"1\"/></soap:Header><soap:Body><demo:echo><arg0>test</arg0></demo:echo></soap:Body></soap:Envelope>";
        String url = "http://localhost:6060/axis2/services/DemoService.DemoServicePort";
        QName name = new QName("http://demo/", "DemoService");
        QName portName = new QName("http://demo/", "DemoServicePort");
        try {
            //Create Service
            Service s = Service.create(name);
            assertNotNull(s);
            //add port
            s.addPort(portName, null, url);
            //Create Dispatch
            Dispatch<String> dispatch =
                    s.createDispatch(portName, String.class, Service.Mode.MESSAGE);
            assertNotNull(dispatch);
            dispatch.invoke(soapMessage);
            fail("Did not get expected SOAPFaultException for not understood MustUnderstand header");
            TestLogger.logger.debug("----------------------------------");
        } catch (SOAPFaultException e) {
            TestLogger.logger.debug("Got expected SOAPFault", e);
            assertTrue(e.toString().indexOf(
                    "Must Understand check failed for header soap : {http://demo/}badHeader") !=
                                                                                              -1);
            TestLogger.logger.debug("MustUnderstand failed as exptected");
            TestLogger.logger.debug("----------------------------------");
        }
    }

    /**
     * Test that a mustUnderstand header with a specific SOAP role that the endpoint is acting in
     * doesn't cause a NotUnderstood fault if the header QName is one that the handler understands.
     */
    public void testSoapRoleActedIn() {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        Object res;
        //Add myHeader to SOAPMessage that will be injected by handler.getHeader().
        String soapMessage =
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:demo=\"http://demo/\"><soap:Header> <demo:myheader soap:actor=\"http://DemoHandler/Role\" soap:mustUnderstand=\"1\"/></soap:Header><soap:Body><demo:echo><arg0>test</arg0></demo:echo></soap:Body></soap:Envelope>";
        String url = "http://localhost:6060/axis2/services/DemoService.DemoServicePort";
        QName name = new QName("http://demo/", "DemoService");
        QName portName = new QName("http://demo/", "DemoServicePort");
        //Create Service
        Service s = Service.create(name);
        assertNotNull(s);
        //add port
        s.addPort(portName, null, url);

        //Create Dispatch
        Dispatch<String> dispatch = s.createDispatch(portName, String.class, Service.Mode.MESSAGE);
        assertNotNull(dispatch);
        res = dispatch.invoke(soapMessage);
        assertNotNull(res);
        TestLogger.logger.debug("----------------------------------");
    }

    /**
     * Test that a mustUnderstand header with a specific SOAP role that the endpoint is acting in
     * doesn't cause a NotUnderstood fault if the header QName is one that the handler understands.
     */
    public void testSoapRoleNotActedIn() {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        Object res;
        //Add myHeader to SOAPMessage that will be injected by handler.getHeader().
        String soapMessage =
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:demo=\"http://demo/\"><soap:Header> <demo:myheader soap:actor=\"http://DemoHandler/NotActedIn\" soap:mustUnderstand=\"1\"/></soap:Header><soap:Body><demo:echo><arg0>test</arg0></demo:echo></soap:Body></soap:Envelope>";
        String url = "http://localhost:6060/axis2/services/DemoService.DemoServicePort";
        QName name = new QName("http://demo/", "DemoService");
        QName portName = new QName("http://demo/", "DemoServicePort");
        //Create Service
        Service s = Service.create(name);
        assertNotNull(s);
        //add port
        s.addPort(portName, null, url);

        //Create Dispatch
        Dispatch<String> dispatch = s.createDispatch(portName, String.class, Service.Mode.MESSAGE);
        assertNotNull(dispatch);
        res = dispatch.invoke(soapMessage);
        assertNotNull(res);
        TestLogger.logger.debug("----------------------------------");
    }

    /**
     * Test that a mustUnderstand header with a specific SOAP role that the endpoint is acting in
     * which has a mustUnderstand header that will not be processed by the handler causes a fault.
     */
    public void testSoapRoleActedInNotUnderstoodFault() {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        //Add myHeader to SOAPMessage that will be injected by handler.getHeader().
        String soapMessage =
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:demo=\"http://demo/\"><soap:Header> <demo:myNOTUNDERSTOODheader soap:actor=\"http://DemoHandler/Role\" soap:mustUnderstand=\"1\"/></soap:Header><soap:Body><demo:echo><arg0>test</arg0></demo:echo></soap:Body></soap:Envelope>";
        String url = "http://localhost:6060/axis2/services/DemoService.DemoServicePort";
        QName name = new QName("http://demo/", "DemoService");
        QName portName = new QName("http://demo/", "DemoServicePort");
        try {
            //Create Service
            Service s = Service.create(name);
            assertNotNull(s);
            //add port
            s.addPort(portName, null, url);

            //Create Dispatch
            Dispatch<String> dispatch =
                    s.createDispatch(portName, String.class, Service.Mode.MESSAGE);
            assertNotNull(dispatch);
            dispatch.invoke(soapMessage);
            fail("Did not received expected notUnderstood fault");
        } catch (Exception e) {
            TestLogger.logger.debug("Caught expected exception", e);
            assertTrue(e.toString().indexOf(
                    "Must Understand check failed for header soap : {http://demo/}myNOTUNDERSTOODheader") !=
                                                                                                          -1);
            TestLogger.logger.debug("----------------------------------");
        }
    }

}
