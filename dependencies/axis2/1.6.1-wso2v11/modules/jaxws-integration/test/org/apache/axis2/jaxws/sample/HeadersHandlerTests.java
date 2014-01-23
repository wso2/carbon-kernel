/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;

import org.apache.axis2.Constants;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.sample.headershandler.HeadersClientTrackerHandler;
import org.apache.axis2.jaxws.sample.headershandler.HeadersHandlerPortType;
import org.apache.axis2.jaxws.sample.headershandler.HeadersHandlerService;
import org.apache.axis2.jaxws.sample.headershandler.TestHeaders;
import org.test.headershandler.HeadersHandlerResponse;

/**
 * @author rott
 *
 */
public class HeadersHandlerTests extends AbstractTestCase {

    String axisEndpoint = "http://localhost:6060/axis2/services/HeadersHandlerService.HeadersHandlerPortTypeImplPort";
    
    private static final String filelogname = "target/HeadersHandlerTests.log";

    public static Test suite() {
        return getTestSetup(new TestSuite(HeadersHandlerTests.class));
    }
    
    protected void setUp() throws Exception {
        deleteFile();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        //deleteFile();
        super.tearDown();
    }

    private void deleteFile() throws Exception {
        File file = new File(filelogname);
        file.delete();  // yes, delete for each retrieval, which should only happen once per test
    }



    public void testHeadersHandler() {
        try {
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());

            HeadersHandlerService service = new HeadersHandlerService();
            HeadersHandlerPortType proxy = service.getHeadersHandlerPort();
            BindingProvider p = (BindingProvider) proxy;
            Map<String, Object> requestCtx = p.getRequestContext();
            
            requestCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
            
            /*
             * add several headers by way of HeadersAdapter property
             */
            String acoh1, acoh2, acoh3, acoh4, acoh5, acoh6;
            SOAPFactory sf = SOAPFactory.newInstance();
        	try {
            	Map<QName, List<String>> requestHeaders = new HashMap<QName, List<String>>();
            	
            	// QName used here should match the key for the list set on the requestCtx
            	acoh1 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL1);
            	
            	// QName used here should match the key for the list set on the requestCtx
            	acoh2 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL2);
            	
            	// QName used here should match the key for the list set on the requestCtx
            	acoh3 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH2_HEADER_QNAME, TestHeaders.CONTENT_SMALL3);
            	
            	// QName used here should match the key for the list set on the requestCtx
            	acoh4 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH2_HEADER_QNAME, TestHeaders.CONTENT_SMALL4);
            	
            	// create additional header strings that will need to be checked:
        		acoh5 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH3_HEADER_QNAME, TestHeaders.CONTENT_LARGE);
        		acoh6 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH4_HEADER_QNAME, TestHeaders.CONTENT_SMALL4);
            	
            	List<String> list1 = new ArrayList<String>();
            	list1.add(acoh1);
            	list1.add(acoh2);
            	
            	List<String> list2 = new ArrayList<String>();
            	list2.add(acoh3);
            	list2.add(acoh4);
            	
            	requestHeaders.put(TestHeaders.ACOH1_HEADER_QNAME, list1);
            	requestHeaders.put(TestHeaders.ACOH2_HEADER_QNAME, list2);
            	requestCtx.put(Constants.JAXWS_OUTBOUND_SOAP_HEADERS, requestHeaders);
        	} catch (Throwable e) {
        		fail(e.getMessage());
        		return;
        	}
        	
        	// some handlers decrement the value, so we can confirm SOAP body manipulation does not corrupt the headers
        	int numOfHandlerHitsInFlow = 3;
            
            int intParam1 = 10;
            int intParam2 = 10;
            int total = proxy.headersHandler(intParam1, intParam2);
            
            assertEquals("Return value should be " + (intParam1 + intParam2 - numOfHandlerHitsInFlow) + " but was " + total ,
                         (intParam1 + intParam2 - numOfHandlerHitsInFlow),
                         total);
            TestLogger.logger.debug("Total (after handler manipulation) = " + total);
            
            /*
             * I tried to give enough info below in the expected_calls list so you can tell what's
             * being tested without having to look at handler code.  All header manipulation is
             * done by SOAPHeadersAdapter.
             * 
             * TODO: I would very much like to have done some other means of
             * header manipulation, but the Axis2 SAAJ module is lacking necessary implementation
             * to do this with any reliability.
             */
            
            String log = readLogFile();
            String expected_calls =
            		// client outbound
                      "HeadersClientLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh1+"\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh2+"\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh3+"\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh4+"\n"
                    + "HeadersClientLogicalHandler REMOVED_HEADER "+acoh1+"\n"
                    + "HeadersClientLogicalHandler REMOVED_HEADER "+acoh3+"\n"
                    + "HeadersClientLogicalHandler REMOVED_HEADER "+acoh4+"\n"   // message manipulated after this action
                    + "HeadersClientProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersClientProtocolHandler CHECKED_HEADER "+acoh2+"\n"                   
                    + "HeadersClientProtocolHandler ADDED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersClientProtocolHandler2 CHECKED_HEADER "+acoh2+"\n"
                    + "HeadersClientProtocolHandler2 CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler GET_HEADERS\n"
                    + "HeadersClientProtocolHandler2 GET_HEADERS\n"
                    // server inbound
                    + "HeadersServerProtocolHandler GET_HEADERS\n"
                    + "HeadersServerProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersServerProtocolHandler CHECKED_HEADER "+acoh2+"\n"
                    + "HeadersServerProtocolHandler CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersServerProtocolHandler REMOVED_HEADER "+acoh2+"\n"
                    + "HeadersServerProtocolHandler ADDED_HEADER "+acoh6+"\n"
                    + "HeadersServerLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersServerLogicalHandler CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersServerLogicalHandler CHECKED_HEADER "+acoh6+"\n"
                    + "HeadersServerLogicalHandler REMOVED_HEADER "+acoh5+"\n"   // message manipulated after this action
                    + "HeadersServerLogicalHandler REMOVED_HEADER "+acoh6+"\n"
                    // server outbound
                    + "HeadersServerLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersServerLogicalHandler ADDED_HEADER "+acoh1+"\n"   // message manipulated after this action
                    + "HeadersServerProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersServerProtocolHandler CHECKED_HEADER "+acoh1+"\n"
                    + "HeadersServerProtocolHandler REMOVED_HEADER "+acoh1+"\n"
                    + "HeadersServerProtocolHandler ADDED_HEADER "+acoh5+"\n"
                    + "HeadersServerLogicalHandler CLOSE\n"
                    + "HeadersServerProtocolHandler CLOSE\n"
                    // client inbound
                    + "HeadersClientProtocolHandler2 HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersClientProtocolHandler2 CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler2 ADDED_HEADER "+acoh3+"\n"
                    + "HeadersClientProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersClientProtocolHandler CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler CHECKED_HEADER "+acoh3+"\n"
                    + "HeadersClientProtocolHandler REMOVED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler ADDED_HEADER "+acoh4+"\n"
                    + "HeadersClientLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersClientProtocolHandler2 CLOSE\n"
                    + "HeadersClientProtocolHandler CLOSE\n"
                    + "HeadersClientLogicalHandler CLOSE\n";
            
            assertEquals(expected_calls, log);
            
        } catch (Exception e) {
            e.printStackTrace();
            TestLogger.logger.debug("ERROR", e);
            fail(e.getMessage());
        }
        TestLogger.logger.debug("----------------------------------");
    }
    
    public void testHeadersHandlerAsyncCallback() {
        try {
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());

            HeadersHandlerService service = new HeadersHandlerService();
            HeadersHandlerPortType proxy = service.getHeadersHandlerPort();
            BindingProvider p = (BindingProvider) proxy;
            Map<String, Object> requestCtx = p.getRequestContext();
            
            requestCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
            
            /*
             * add several headers by way of HeadersAdapter property
             */
            String acoh1, acoh2, acoh3, acoh4, acoh5, acoh6;
            SOAPFactory sf = SOAPFactory.newInstance();
            try {
                Map<QName, List<String>> requestHeaders = new HashMap<QName, List<String>>();
                
                // QName used here should match the key for the list set on the requestCtx
                acoh1 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL1);
                
                // QName used here should match the key for the list set on the requestCtx
                acoh2 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL2);
                
                // QName used here should match the key for the list set on the requestCtx
                acoh3 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH2_HEADER_QNAME, TestHeaders.CONTENT_SMALL3);
                
                // QName used here should match the key for the list set on the requestCtx
                acoh4 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH2_HEADER_QNAME, TestHeaders.CONTENT_SMALL4);
                
                // create additional header strings that will need to be checked:
                acoh5 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH3_HEADER_QNAME, TestHeaders.CONTENT_LARGE);
                acoh6 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH4_HEADER_QNAME, TestHeaders.CONTENT_SMALL4);
                
                List<String> list1 = new ArrayList<String>();
                list1.add(acoh1);
                list1.add(acoh2);
                
                List<String> list2 = new ArrayList<String>();
                list2.add(acoh3);
                list2.add(acoh4);
                
                requestHeaders.put(TestHeaders.ACOH1_HEADER_QNAME, list1);
                requestHeaders.put(TestHeaders.ACOH2_HEADER_QNAME, list2);
                requestCtx.put(Constants.JAXWS_OUTBOUND_SOAP_HEADERS, requestHeaders);
            } catch (Throwable e) {
                fail(e.getMessage());
                return;
            }
            
            // some handlers decrement the value, so we can confirm SOAP body manipulation does not corrupt the headers
            int numOfHandlerHitsInFlow = 3;
            
            int intParam1 = 10;
            int intParam2 = 10;
            
            HeadersHandlerAsyncCallback callback = new HeadersHandlerAsyncCallback();
            Future<?> future = proxy.headersHandlerAsync(intParam1, intParam2, callback);

            while (!future.isDone()) {
                Thread.sleep(1000);
                TestLogger.logger.debug("Async invocation incomplete");
            }

            int total = callback.getResponseValue();
            
            
            assertEquals("Return value should be " + (intParam1 + intParam2 - numOfHandlerHitsInFlow) + " but was " + total ,
                         (intParam1 + intParam2 - numOfHandlerHitsInFlow),
                         total);
            TestLogger.logger.debug("Total (after handler manipulation) = " + total);
            
            /*
             * I tried to give enough info below in the expected_calls list so you can tell what's
             * being tested without having to look at handler code.  All header manipulation is
             * done by SOAPHeadersAdapter.
             * 
             * TODO: I would very much like to have done some other means of
             * header manipulation, but the Axis2 SAAJ module is lacking necessary implementation
             * to do this with any reliability.
             */
            
            String log = readLogFile();
            String expected_calls =
                    // client outbound
                      "HeadersClientLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh1+"\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh2+"\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh3+"\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh4+"\n"
                    + "HeadersClientLogicalHandler REMOVED_HEADER "+acoh1+"\n"
                    + "HeadersClientLogicalHandler REMOVED_HEADER "+acoh3+"\n"
                    + "HeadersClientLogicalHandler REMOVED_HEADER "+acoh4+"\n"   // message manipulated after this action
                    + "HeadersClientProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersClientProtocolHandler CHECKED_HEADER "+acoh2+"\n"                   
                    + "HeadersClientProtocolHandler ADDED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersClientProtocolHandler2 CHECKED_HEADER "+acoh2+"\n"
                    + "HeadersClientProtocolHandler2 CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler GET_HEADERS\n"
                    + "HeadersClientProtocolHandler2 GET_HEADERS\n"
                    // server inbound
                    + "HeadersServerProtocolHandler GET_HEADERS\n"
                    + "HeadersServerProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersServerProtocolHandler CHECKED_HEADER "+acoh2+"\n"
                    + "HeadersServerProtocolHandler CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersServerProtocolHandler REMOVED_HEADER "+acoh2+"\n"
                    + "HeadersServerProtocolHandler ADDED_HEADER "+acoh6+"\n"
                    + "HeadersServerLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersServerLogicalHandler CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersServerLogicalHandler CHECKED_HEADER "+acoh6+"\n"
                    + "HeadersServerLogicalHandler REMOVED_HEADER "+acoh5+"\n"   // message manipulated after this action
                    + "HeadersServerLogicalHandler REMOVED_HEADER "+acoh6+"\n"
                    // server outbound
                    + "HeadersServerLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersServerLogicalHandler ADDED_HEADER "+acoh1+"\n"   // message manipulated after this action
                    + "HeadersServerProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersServerProtocolHandler CHECKED_HEADER "+acoh1+"\n"
                    + "HeadersServerProtocolHandler REMOVED_HEADER "+acoh1+"\n"
                    + "HeadersServerProtocolHandler ADDED_HEADER "+acoh5+"\n"
                    + "HeadersServerLogicalHandler CLOSE\n"
                    + "HeadersServerProtocolHandler CLOSE\n"
                    // client inbound
                    + "HeadersClientProtocolHandler2 HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersClientProtocolHandler2 CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler2 ADDED_HEADER "+acoh3+"\n"
                    + "HeadersClientProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersClientProtocolHandler CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler CHECKED_HEADER "+acoh3+"\n"
                    + "HeadersClientProtocolHandler REMOVED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler ADDED_HEADER "+acoh4+"\n"
                    + "HeadersClientLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersClientProtocolHandler2 CLOSE\n"
                    + "HeadersClientProtocolHandler CLOSE\n"
                    + "HeadersClientLogicalHandler CLOSE\n";
            
            assertEquals(expected_calls, log);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        TestLogger.logger.debug("----------------------------------");
    }
    
    public void testHeadersHandlerServerInboundFault() {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());

        HeadersHandlerService service = new HeadersHandlerService();
        HeadersHandlerPortType proxy = service.getHeadersHandlerPort();
        BindingProvider p = (BindingProvider) proxy;
        Map<String, Object> requestCtx = p.getRequestContext();

        requestCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);

        /*
         * add several headers by way of HeadersAdapter property
         */
        String acoh1, acoh2, acoh3, acoh4, acoh5, acoh6;
        SOAPFactory sf;
        try {
            sf = SOAPFactory.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            return;
        }

        Map<QName, List<String>> requestHeaders = new HashMap<QName, List<String>>();

        // QName used here should match the key for the list set on the requestCtx
        acoh1 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL1);

        // QName used here should match the key for the list set on the requestCtx
        acoh2 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL2);

        // QName used here should match the key for the list set on the requestCtx
        acoh3 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH2_HEADER_QNAME, TestHeaders.CONTENT_SMALL3);

        // QName used here should match the key for the list set on the requestCtx
        acoh4 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH2_HEADER_QNAME, TestHeaders.CONTENT_SMALL4);

        // create additional header strings that will need to be checked:
        acoh5 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH3_HEADER_QNAME, TestHeaders.CONTENT_LARGE);
        acoh6 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH4_HEADER_QNAME, TestHeaders.CONTENT_SMALL4);

        List<String> list1 = new ArrayList<String>();
        list1.add(acoh1);
        list1.add(acoh2);

        List<String> list2 = new ArrayList<String>();
        list2.add(acoh3);
        list2.add(acoh4);

        requestHeaders.put(TestHeaders.ACOH1_HEADER_QNAME, list1);
        requestHeaders.put(TestHeaders.ACOH2_HEADER_QNAME, list2);
        requestCtx.put(Constants.JAXWS_OUTBOUND_SOAP_HEADERS, requestHeaders);

        // some handlers decrement the value, so we can confirm SOAP body manipulation does not corrupt the headers
        int numOfHandlerHitsInFlow = 3;

        int intParam1 = 10;
        int intParam2 = 66;
            
        try {
            int total = proxy.headersHandler(intParam1, intParam2);
            fail("headersHandler should have caused an exception, but did not.");
            
            /*
             * I tried to give enough info below in the expected_calls list so you can tell what's
             * being tested without having to look at handler code.  All header manipulation is
             * done by SOAPHeadersAdapter.
             * 
             * TODO: I would very much like to have done some other means of
             * header manipulation, but the Axis2 SAAJ module is lacking necessary implementation
             * to do this with any reliability.
             */
            
        } catch (Exception e) {
            
            String log = readLogFile();
            String expected_calls =
                    // client outbound
                      "HeadersClientLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh1+"\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh2+"\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh3+"\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh4+"\n"
                    + "HeadersClientLogicalHandler REMOVED_HEADER "+acoh1+"\n"
                    + "HeadersClientLogicalHandler REMOVED_HEADER "+acoh3+"\n"
                    + "HeadersClientLogicalHandler REMOVED_HEADER "+acoh4+"\n"   // message manipulated after this action
                    + "HeadersClientProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersClientProtocolHandler CHECKED_HEADER "+acoh2+"\n"                   
                    + "HeadersClientProtocolHandler ADDED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersClientProtocolHandler2 CHECKED_HEADER "+acoh2+"\n"
                    + "HeadersClientProtocolHandler2 CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler GET_HEADERS\n"
                    + "HeadersClientProtocolHandler2 GET_HEADERS\n"
                    // server inbound
                    + "HeadersServerProtocolHandler GET_HEADERS\n"
                    + "HeadersServerProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersServerProtocolHandler CHECKED_HEADER "+acoh2+"\n"
                    + "HeadersServerProtocolHandler CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersServerProtocolHandler REMOVED_HEADER "+acoh2+"\n"
                    + "HeadersServerProtocolHandler ADDED_HEADER "+acoh6+"\n"
                    + "HeadersServerLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersServerLogicalHandler CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersServerLogicalHandler CHECKED_HEADER "+acoh6+"\n"
                    + "HeadersServerLogicalHandler REMOVED_HEADER "+acoh5+"\n"   // message manipulated after this action
                    + "HeadersServerLogicalHandler REMOVED_HEADER "+acoh6+"\n"   // throws protocol exception
                    // server outbound
                    + "HeadersServerProtocolHandler HANDLE_FAULT_OUTBOUND\n"
                    + "HeadersServerProtocolHandler ADDED_HEADER "+acoh5+"\n"
                    + "HeadersServerLogicalHandler CLOSE\n"
                    + "HeadersServerProtocolHandler CLOSE\n"
                    // client inbound
                    + "HeadersClientProtocolHandler2 HANDLE_FAULT_INBOUND\n"
                    + "HeadersClientProtocolHandler2 CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler2 REMOVED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler HANDLE_FAULT_INBOUND\n"
                    + "HeadersClientLogicalHandler HANDLE_FAULT_INBOUND\n"   // getPayload called, just to exercise underlying code
                    + "HeadersClientProtocolHandler2 CLOSE\n"
                    + "HeadersClientProtocolHandler CLOSE\n"
                    + "HeadersClientLogicalHandler CLOSE\n";
            
            assertEquals(expected_calls, log);
            assertEquals("I don't like 66", e.getMessage());
            
        }
        TestLogger.logger.debug("----------------------------------");
    }
    
    
    /*
     * TODO: test is currently disabled due to exception:
     * 
     * Caused by: java.net.SocketException: Broken pipe
     * at java.net.SocketOutputStream.socketWrite(SocketOutputStream.java:103)
     * at java.net.SocketOutputStream.write(SocketOutputStream.java:147)
     * at org.apache.http.impl.io.AbstractSessionOutputBuffer.write(AbstractSessionOutputBuffer.java:109)
     * at org.apache.http.impl.io.ChunkedOutputStream.flushCacheWithAppend(ChunkedOutputStream.java:117)
     * at org.apache.http.impl.io.ChunkedOutputStream.write(ChunkedOutputStream.java:166)
     * at org.apache.axis2.transport.http.server.AxisHttpResponseImpl$AutoCommitOutputStream.write(AxisHttpResponseImpl.java:231)
     * at com.ctc.wstx.io.UTF8Writer.write(UTF8Writer.java:139)
     * at com.ctc.wstx.sw.BufferingXmlWriter.flushBuffer(BufferingXmlWriter.java:1103)
     * at com.ctc.wstx.sw.BufferingXmlWriter.flush(BufferingXmlWriter.java:213)
     * at com.ctc.wstx.sw.BaseStreamWriter.flush(BaseStreamWriter.java:311)
     * ... 12 more
     * 
     * Currently, the server side returns the inbound message when a handler.handleMessage
     * method returns 'false'.  This may be a misinterpretation of the jaxws spec.
     * When the response flow is fixed, remove this comment, and remove the '_' from the
     * test method name to enable it.
     */
    public void _testHeadersHandlerServerInboundFlowReversal() {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());

        HeadersHandlerService service = new HeadersHandlerService();
        HeadersHandlerPortType proxy = service.getHeadersHandlerPort();
        BindingProvider p = (BindingProvider) proxy;
        Map<String, Object> requestCtx = p.getRequestContext();

        requestCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);

        /*
         * add several headers by way of HeadersAdapter property
         */
        String acoh1, acoh2, acoh3, acoh4, acoh5, acoh6;
        SOAPFactory sf;
        try {
            sf = SOAPFactory.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            return;
        }

        Map<QName, List<String>> requestHeaders = new HashMap<QName, List<String>>();

        // QName used here should match the key for the list set on the requestCtx
        acoh1 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL1);

        // QName used here should match the key for the list set on the requestCtx
        acoh2 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH1_HEADER_QNAME, TestHeaders.CONTENT_SMALL2);

        // QName used here should match the key for the list set on the requestCtx
        acoh3 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH2_HEADER_QNAME, TestHeaders.CONTENT_SMALL3);

        // QName used here should match the key for the list set on the requestCtx
        acoh4 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH2_HEADER_QNAME, TestHeaders.CONTENT_SMALL4);

        // create additional header strings that will need to be checked:
        acoh5 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH3_HEADER_QNAME, TestHeaders.CONTENT_LARGE);
        acoh6 = TestHeaders.createHeaderXMLString(TestHeaders.ACOH4_HEADER_QNAME, TestHeaders.CONTENT_SMALL4);

        List<String> list1 = new ArrayList<String>();
        list1.add(acoh1);
        list1.add(acoh2);

        List<String> list2 = new ArrayList<String>();
        list2.add(acoh3);
        list2.add(acoh4);

        requestHeaders.put(TestHeaders.ACOH1_HEADER_QNAME, list1);
        requestHeaders.put(TestHeaders.ACOH2_HEADER_QNAME, list2);
        requestCtx.put(Constants.JAXWS_OUTBOUND_SOAP_HEADERS, requestHeaders);

        int intParam1 = 10;
        int intParam2 = 33;
            
        try {
            int total = proxy.headersHandler(intParam1, intParam2);
        } catch (Exception e) {
            /*
             * I tried to give enough info below in the expected_calls list so you can tell what's
             * being tested without having to look at handler code.  All header manipulation is
             * done by SOAPHeadersAdapter.
             * 
             * TODO: I would very much like to have done some other means of
             * header manipulation, but the Axis2 SAAJ module is lacking necessary implementation
             * to do this with any reliability.
             */
            
            
            String log = readLogFile();
            String expected_calls =
                    // client outbound
                      "HeadersClientLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh1+"\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh2+"\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh3+"\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh4+"\n"
                    + "HeadersClientLogicalHandler REMOVED_HEADER "+acoh1+"\n"
                    + "HeadersClientLogicalHandler REMOVED_HEADER "+acoh3+"\n"
                    + "HeadersClientLogicalHandler REMOVED_HEADER "+acoh4+"\n"   // message manipulated after this action
                    + "HeadersClientProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersClientProtocolHandler CHECKED_HEADER "+acoh2+"\n"                   
                    + "HeadersClientProtocolHandler ADDED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersClientProtocolHandler2 CHECKED_HEADER "+acoh2+"\n"
                    + "HeadersClientProtocolHandler2 CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler GET_HEADERS\n"
                    + "HeadersClientProtocolHandler2 GET_HEADERS\n"
                    // server inbound
                    + "HeadersServerProtocolHandler GET_HEADERS\n"
                    + "HeadersServerProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersServerProtocolHandler CHECKED_HEADER "+acoh2+"\n"
                    + "HeadersServerProtocolHandler CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersServerProtocolHandler REMOVED_HEADER "+acoh2+"\n"
                    + "HeadersServerProtocolHandler ADDED_HEADER "+acoh6+"\n"
                    + "HeadersServerLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersServerLogicalHandler CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersServerLogicalHandler CHECKED_HEADER "+acoh6+"\n"
                    + "HeadersServerLogicalHandler REMOVED_HEADER "+acoh5+"\n"   // message manipulated after this action
                    + "HeadersServerLogicalHandler REMOVED_HEADER "+acoh6+"\n"   // returns false
                    // server outbound
                    + "HeadersServerProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "HeadersServerProtocolHandler ADDED_HEADER "+acoh1+"\n"
                    + "HeadersServerProtocolHandler CHECKED_HEADER "+acoh1+"\n"
                    + "HeadersServerProtocolHandler REMOVED_HEADER "+acoh1+"\n"
                    + "HeadersServerProtocolHandler ADDED_HEADER "+acoh5+"\n"
                    + "HeadersServerLogicalHandler CLOSE\n"
                    + "HeadersServerProtocolHandler CLOSE\n"
                    // client inbound
                    + "HeadersClientProtocolHandler2 HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersClientProtocolHandler2 CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler2 ADDED_HEADER "+acoh3+"\n"
                    + "HeadersClientProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersClientProtocolHandler CHECKED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler CHECKED_HEADER "+acoh3+"\n"
                    + "HeadersClientProtocolHandler REMOVED_HEADER "+acoh5+"\n"
                    + "HeadersClientProtocolHandler ADDED_HEADER "+acoh4+"\n"
                    + "HeadersClientLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh3+"\n"
                    + "HeadersClientLogicalHandler CHECKED_HEADER "+acoh4+"\n"
                    + "HeadersClientLogicalHandler REMOVED_HEADER "+acoh3+"\n"
                    + "HeadersClientLogicalHandler REMOVED_HEADER "+acoh4+"\n"
                    + "HeadersClientProtocolHandler2 CLOSE\n"
                    + "HeadersClientProtocolHandler CLOSE\n"
                    + "HeadersClientLogicalHandler CLOSE\n";
            
            assertEquals("I don't like 33", e.getMessage());
            assertEquals(expected_calls, log);
            
        }
        TestLogger.logger.debug("----------------------------------");
    }
    
    /*
     * The intent of making the SOAPHeadersAdapter available to handlers is that they
     * use it as an alternative to SAAJ.  We have protection built in to prevent handler
     * implementations from doing both.  This method tests for that.
     */
    public void testHeadersHandlerTracker() {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        try {

            HeadersHandlerService service = new HeadersHandlerService();
            HeadersHandlerPortType proxy = service.getHeadersHandlerPort();
            BindingProvider p = (BindingProvider) proxy;
            List<Handler> handlers = p.getBinding().getHandlerChain();
            if (handlers == null)
                handlers = new ArrayList<Handler>();
            handlers.add(new HeadersClientTrackerHandler());
            p.getBinding().setHandlerChain(handlers);
            Map<String, Object> requestCtx = p.getRequestContext();

            requestCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
            
            int total = proxy.headersHandler(1, 1);
            fail("Should have received a WebServiceException, but did not.");

        } catch (Exception e) {
            assertTrue(e instanceof WebServiceException);
        }
        TestLogger.logger.debug("----------------------------------");
    }
    
    
    /*
     * A callback implementation that can be used to collect the exceptions
     */
    class HeadersHandlerAsyncCallback implements AsyncHandler<HeadersHandlerResponse> {

        private Exception exception;
        private int retVal;

        public void handleResponse(Response<HeadersHandlerResponse> response) {
            try {
                TestLogger.logger.debug("HeadersHandlerAsyncCallback.handleResponse() was called");
                HeadersHandlerResponse r = response.get();
                TestLogger.logger.debug("No exception was thrown from Response.get()");
                retVal = r.getReturn();
            } catch (Exception e) {
                TestLogger.logger.debug("An exception was thrown: " + e.getClass());
                exception = e;
            }
        }

        public int getResponseValue() {
            return retVal;
        }

        public Exception getException() {
            return exception;
        }
    }
    
    
    private String readLogFile() {
        try {
            FileReader fr = new FileReader(filelogname);
            BufferedReader inputStream = new BufferedReader(fr);
            String line = null;
            String ret = null;
            while ((line = inputStream.readLine()) != null) {
                if (ret == null) {
                    ret = "";
                }
                ret = ret.concat(line + "\n");
            }
            fr.close();
            return ret;
        } catch (FileNotFoundException fnfe) {
            // it's possible the test does not actually call any handlers and therefore
            // no file would have been written.  The test should account for this by
            // assertNull on the return value from here
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail(ioe.getMessage());
        }
        return null;
    }

}
