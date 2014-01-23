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

/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.marshaller.impl.alt.MethodMarshallerUtils;
import org.apache.axis2.jaxws.sample.doclitbare.sei.BareDocLitService;
import org.apache.axis2.jaxws.sample.doclitbare.sei.DocLitBarePortType;

import javax.xml.ws.BindingProvider;

public class BareTests extends AbstractTestCase {

    static final String ENDPOINT_URL = 
        "http://localhost:6060/axis2/services/BareDocLitService.DocLitBarePortTypeImplPort";

    // String containing some characters that require XML encoding
    private static String XMLCHARS = ">><<<3>>>3>>>3";
    
    public static Test suite() {
        return getTestSetup(new TestSuite(BareTests.class));
    }

    public void testEchoString() throws Exception {
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());


        BareDocLitService service = new BareDocLitService();
        DocLitBarePortType proxy = service.getBareDocLitPort();
        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(
                                  BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        p.getRequestContext().put(
                                  BindingProvider.SOAPACTION_URI_PROPERTY, "echoString");
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_URL);

        String request = "Hello World";
        String response = proxy.echoString(request);
        assertTrue(request.equals(response));
        
        // Try the test again
        response = proxy.echoString(request);
        assertTrue(request.equals(response));

        TestLogger.logger.debug("------------------------------");

    }
    
    public void testEchoString_xmlencode() throws Exception {
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());

        BareDocLitService service = new BareDocLitService();
        DocLitBarePortType proxy = service.getBareDocLitPort();
        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(
                                  BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        p.getRequestContext().put(
                                  BindingProvider.SOAPACTION_URI_PROPERTY, "echoString");
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_URL);

        String request = XMLCHARS;
        String response = proxy.echoString(request);
        assertTrue(request.equals(response));
        
        // Try the test again
        response = proxy.echoString(request);
        assertTrue(request.equals(response));

        TestLogger.logger.debug("------------------------------");

    }
    
    public void testTwoWaySync(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());

        try{

            BareDocLitService service = new BareDocLitService();
            DocLitBarePortType proxy = service.getBareDocLitPort();
            BindingProvider p = (BindingProvider) proxy;
            p.getRequestContext().put(
                                      BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
            p.getRequestContext().put(
                                      BindingProvider.SOAPACTION_URI_PROPERTY, "twoWaySimple");
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_URL);

            String response = proxy.twoWaySimple(10);
            TestLogger.logger.debug("Sync Response =" + response);
            
            // Try the call again
            response = proxy.twoWaySimple(10);
            
            TestLogger.logger.debug("Sync Response =" + response);
            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }

    public void testTwoWaySyncWithBodyRouting(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());

        try{

            BareDocLitService service = new BareDocLitService();
            DocLitBarePortType proxy = service.getBareDocLitPort();
            BindingProvider p = (BindingProvider) proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_URL);

            String response = proxy.twoWaySimple(10);
            TestLogger.logger.debug("Sync Response =" + response);
            
            
            // Try the call again
            response = proxy.twoWaySimple(10);
            TestLogger.logger.debug("Sync Response =" + response);
            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }

    public void testOneWayEmpty(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());

        try{

            BareDocLitService service = new BareDocLitService();
            DocLitBarePortType proxy = service.getBareDocLitPort();
            BindingProvider p = (BindingProvider) proxy;

            p.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY,
                                      Boolean.TRUE);
            p.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,
            "oneWayEmpty");
            p.getRequestContext().put(
                                      BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_URL);
            proxy.oneWayEmpty();
            
            // Try the call again
            proxy.oneWayEmpty();

            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }
    
    public void testHeader() throws Exception {
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());


        BareDocLitService service = new BareDocLitService();
        DocLitBarePortType proxy = service.getBareDocLitPort();
        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(
                                  BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        p.getRequestContext().put(
                                  BindingProvider.SOAPACTION_URI_PROPERTY, "headerTest");
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_URL);

        String request = "Hello World";
        String response = proxy.headerTest(1, request);
        assertTrue(response != null);
        assertTrue(response.indexOf(request) > 0);
        
        // Try the test again
        request = "Hello World";
        response = proxy.headerTest(1,request);
        assertTrue(response != null);
        assertTrue(response.indexOf(request) > 0);

        TestLogger.logger.debug("------------------------------");

    }
    
    public void testHeaderWithNull() throws Exception {
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());


        BareDocLitService service = new BareDocLitService();
        DocLitBarePortType proxy = service.getBareDocLitPort();
        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(
                                  BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        p.getRequestContext().put(
                                  BindingProvider.SOAPACTION_URI_PROPERTY, "headerTest");
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_URL);
        
        // Don't write a header element when the @WebParam header parameter is null.
        p.getRequestContext().put(org.apache.axis2.jaxws.Constants.WRITE_HEADER_ELEMENT_IF_NULL, Boolean.FALSE);

        String request = null;  // No header
        String response = proxy.headerTest(1, request);
        assertTrue(response != null);
        assertTrue(response.indexOf("No Header") > 0);

        // Try the test again
        request = null;
        response = proxy.headerTest(1,request);
        assertTrue(response != null);
        assertTrue(response.indexOf("No Header") > 0);

        TestLogger.logger.debug("------------------------------");
    }
}
