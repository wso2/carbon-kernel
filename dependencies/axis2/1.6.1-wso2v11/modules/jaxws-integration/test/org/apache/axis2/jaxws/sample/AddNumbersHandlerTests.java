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

package org.apache.axis2.jaxws.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersClientLogicalHandler;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersClientLogicalHandler2;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersClientLogicalHandler3;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersClientLogicalHandler4;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersClientProtocolHandler;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersHandlerFault_Exception;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersHandlerPortType;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersHandlerService;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersProtocolHandler2;
import org.test.addnumbershandler.AddNumbersHandlerResponse;

public class AddNumbersHandlerTests extends AbstractTestCase {
	
    String axisEndpoint = "http://localhost:6060/axis2/services/AddNumbersHandlerService.AddNumbersHandlerPortTypeImplPort";
    static File requestFile = null;
    static {
        String resourceDir = System.getProperty("basedir",".")+
            File.separator+"test-resources"+File.separator+"xml";
        requestFile = new File(resourceDir+File.separator+"addnumberstest.xml");
    }
    
    private static final String filelogname = "target/AddNumbersHandlerTests.log";

    public static Test suite() {
        return getTestSetup(new TestSuite(AddNumbersHandlerTests.class));
    }

    /**
     * Client app sends 10, 10 as params to sum.  No client-side handlers are configured
     * for this scenario.  The server-side AddNumbersLogicalHandler is instantiated with a
     * variable "deduction" with value 1.  Upon class initialization using PostConstruct
     * annotation, that internal variable is changed to value 2.  The inbound AddNumbersLogicalHandler
     * subtracts 1 from the first param, then outbound it subtracts 2 from the result sum.
     * 
     * This test accomplishes three things (which also carry over to other tests since they all use
     * the same endpoint and server-side handlers:
     * 1)  PostConstruct annotation honored in the handler framework for handler instantiation
     * 2)  AddNumbersLogicalHandler also sets two message context properties, one with APPLICATION
     *     scope, which the endpoint checks.
     * 3)  Handlers are sharing properties, both APPLICATION scoped and HANDLER scoped
     * 3)  General handler framework functionality; make sure handlers are instantiated and called
     */
    public void testAddNumbersHandler() {
		try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
			
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
			
            BindingProvider p =	(BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
            int total = proxy.addNumbersHandler(10, 10);

            assertEquals("With handler manipulation, total should be 3 less than a proper sumation.", 17, total);
            TestLogger.logger.debug("Total (after handler manipulation) = " + total);
            
            // also confirm that @PreDestroy method is called.  Since it only makes sense to call it on the managed
            // (server) side and just before the handler instance goes out of scope, we are creating a file in the
            // @PreDestroy method, and will check for its existance here.  If the file does not exist, it means
            // @PreDestroy method was never called.  The file is set to .deleteOnExit(), so no need to delete it.
            File file = new File("AddNumbersProtocolHandler.preDestroy.txt");
            assertTrue("File AddNumbersProtocolHandler.preDestroy.txt does not exist, meaning the @PreDestroy method was not called.", file.exists());

            String log = readLogFile();
            String expected_calls =
                      "AddNumbersLogicalHandler2 POST_CONSTRUCT\n"
                    + "AddNumbersProtocolHandler2 GET_HEADERS\n"
                    + "AddNumbersProtocolHandler GET_HEADERS\n"
                    + "AddNumbersProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                    + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_INBOUND\n"
                    + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_INBOUND\n"
                    + "AddNumbersLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                    + "AddNumbersLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                    + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                    + "AddNumbersProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "AddNumbersLogicalHandler CLOSE\n"
                    + "AddNumbersLogicalHandler2 CLOSE\n"
                    + "AddNumbersProtocolHandler2 CLOSE\n"
                    + "AddNumbersProtocolHandler CLOSE\n"
                    + "AddNumbersProtocolHandler PRE_DESTROY\n";
            assertEquals(expected_calls, log);
            
            TestLogger.logger.debug("----------------------------------");
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
    
    /**
     * Client app sends MAXVALUE, MAXVALUE as params to add.
     * No client-side handlers are configured for this scenario.  
     * The endpoint method (addNumbersHandler) will detect the possible overflow and
     * throw an application exception, AddNumbersHandleFault_Exception.
     * 
     * The server-side AddNumbersProtocolHandler will
     * access the thrown exception using the "jaxws.webmethod.exception"
     * property and add the stack trace string to fault string.
     * 
     * The client should receive a AddNumbersHandlerFault_Exception that has a stack
     * trace as part of the message.
     * This test verifies the following:
     * 
     * 1)  Proper exception/fault processing when handlers are installed.
     * 2)  Access to the special "jaxws.webmethod.exception"
     * 3)  Proper exception call flow when an application exception is thrown.
     */
    public void testAddNumbersHandler_WithCheckedException() throws Exception {

        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());

        AddNumbersHandlerService service = new AddNumbersHandlerService();
        AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();

        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
        AddNumbersHandlerFault_Exception expectedException = null;
        Throwable t = null;
        try {
            proxy.addNumbersHandler(Integer.MAX_VALUE, Integer.MAX_VALUE);
            
        } catch (Throwable e) {
            // An exception is expected
            t = e;
            
        } 
        
        // Make sure the proper exception is thrown
        if (t == null) {
            fail("Expected AddNumbersHandlerFault_Exception to be thrown");
        }
        if (t instanceof AddNumbersHandlerFault_Exception) {
            expectedException = (AddNumbersHandlerFault_Exception) t;
        } else {
            fail("Expected AddNumbersHandlerFault_Exception to be thrown, " +
                        "but the exception is: " + t);
        }
       
        // also confirm that @PreDestroy method is called.  Since it only makes sense to call it on the managed
        // (server) side and just before the handler instance goes out of scope, we are creating a file in the
        // @PreDestroy method, and will check for its existance here.  If the file does not exist, it means
        // @PreDestroy method was never called.  The file is set to .deleteOnExit(), so no need to delete it.
        File file = new File("AddNumbersProtocolHandler.preDestroy.txt");
        assertTrue("File AddNumbersProtocolHandler.preDestroy.txt does not exist, meaning the @PreDestroy method was not called.", file.exists());

        String log = readLogFile();
        String expected_calls =
            "AddNumbersLogicalHandler2 POST_CONSTRUCT\n"
            + "AddNumbersProtocolHandler2 GET_HEADERS\n"
            + "AddNumbersProtocolHandler GET_HEADERS\n"
            + "AddNumbersProtocolHandler HANDLE_MESSAGE_INBOUND\n"
            + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_INBOUND\n"
            + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_INBOUND\n"
            + "AddNumbersLogicalHandler HANDLE_MESSAGE_INBOUND\n"
            + "AddNumbersLogicalHandler HANDLE_FAULT_OUTBOUND\n"
            + "AddNumbersLogicalHandler2 HANDLE_FAULT_OUTBOUND\n"
            + "AddNumbersProtocolHandler2 HANDLE_FAULT_OUTBOUND\n"
            + "AddNumbersProtocolHandler HANDLE_FAULT_OUTBOUND\n"
            + "AddNumbersLogicalHandler CLOSE\n"
            + "AddNumbersLogicalHandler2 CLOSE\n"
            + "AddNumbersProtocolHandler2 CLOSE\n"
            + "AddNumbersProtocolHandler CLOSE\n"
            + "AddNumbersProtocolHandler PRE_DESTROY\n";
        
        assertEquals(expected_calls, log);
        
        TestLogger.logger.debug("Expected Exception is " + 
                                expectedException.getMessage());
        
        // The outbound service handler adds the stack trace to the 
        // message.  Make sure the stack trace contains the AddNumbersHandlerPortTypeImpl
        assertTrue("A stack trace was not present in the returned exception's message:" + 
                   expectedException.getMessage(),
                   expectedException.getMessage().indexOf("AddNumbersHandlerPortTypeImpl") > 0);

        TestLogger.logger.debug("----------------------------------");
        
    }
    
    
    
    /**
     * Client app sends MAXVALUE, MAXVALUE as params to add.
     * No client-side handlers are configured for this scenario.  
     * The endpoint method (addNumbersHandler) will detect the possible overflow and
     * throw an unchecked exception, NullPointerException.
     * 
     * The server-side AddNumbersProtocolHandler will
     * access the thrown exception using the "jaxws.webmethod.exception"
     * property and add the stack trace string to fault string.
     * 
     * The client should receive a SOAPFaultException that has a stack
     * trace as part of the message.
     * This test verifies the following:
     * 
     * 1)  Proper exception/fault processing when handlers are installed.
     * 2)  Access to the special "jaxws.webmethod.exception"
     * 3)  Proper exception call flow when an unchecked exception is thrown.
     */
    public void testAddNumbersHandler_WithUnCheckedException() throws Exception {

        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());

        AddNumbersHandlerService service = new AddNumbersHandlerService();
        AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();

        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
        SOAPFaultException expectedException = null;
        Throwable t = null;
        try {
            proxy.addNumbersHandler(-1000, Integer.MIN_VALUE);
            
        } catch (Throwable e) {
            // An exception is expected
            t = e;
            
        } 
        
        // Make sure the proper exception is thrown
        if (t == null) {
            fail("Expected AddNumbersHandlerFault_Exception to be thrown");
        }
        
        if (t instanceof SOAPFaultException) {
            expectedException = (SOAPFaultException) t;
        } else {
            fail("Expected SOAPFaultException to be thrown, " +
                        "but the exception is: " + t);
        }
       
        // also confirm that @PreDestroy method is called.  Since it only makes sense to call it on the managed
        // (server) side and just before the handler instance goes out of scope, we are creating a file in the
        // @PreDestroy method, and will check for its existance here.  If the file does not exist, it means
        // @PreDestroy method was never called.  The file is set to .deleteOnExit(), so no need to delete it.
        File file = new File("AddNumbersProtocolHandler.preDestroy.txt");
        assertTrue("File AddNumbersProtocolHandler.preDestroy.txt does not exist, meaning the @PreDestroy method was not called.", file.exists());

        String log = readLogFile();
        String expected_calls =
            "AddNumbersLogicalHandler2 POST_CONSTRUCT\n"
            + "AddNumbersProtocolHandler2 GET_HEADERS\n"
            + "AddNumbersProtocolHandler GET_HEADERS\n"
            + "AddNumbersProtocolHandler HANDLE_MESSAGE_INBOUND\n"
            + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_INBOUND\n"
            + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_INBOUND\n"
            + "AddNumbersLogicalHandler HANDLE_MESSAGE_INBOUND\n"
            + "AddNumbersLogicalHandler HANDLE_FAULT_OUTBOUND\n"
            + "AddNumbersLogicalHandler2 HANDLE_FAULT_OUTBOUND\n"
            + "AddNumbersProtocolHandler2 HANDLE_FAULT_OUTBOUND\n"
            + "AddNumbersProtocolHandler HANDLE_FAULT_OUTBOUND\n"
            + "AddNumbersLogicalHandler CLOSE\n"
            + "AddNumbersLogicalHandler2 CLOSE\n"
            + "AddNumbersProtocolHandler2 CLOSE\n"
            + "AddNumbersProtocolHandler CLOSE\n"
            + "AddNumbersProtocolHandler PRE_DESTROY\n";
        
        assertTrue("Expected : " + expected_calls + " but received " + log, expected_calls.equals(log));
        
        // The outbound service handler adds the stack trace to the 
        // message.  Make sure the stack trace contains the AddNumbersHandlerPortTypeImpl
        
        TestLogger.logger.debug("Expected Exception is " + 
                                expectedException.getMessage());
        
        SOAPFault fault = expectedException.getFault();
        assertTrue("A stack trace was not present in the returned exception's message:" + 
                   fault.getFaultString(),
                   fault.getFaultString().indexOf("AddNumbersHandlerPortTypeImpl") > 0);
                   

        TestLogger.logger.debug("----------------------------------");
        
    }
    
    /**
     * Client app sends MAXVALUE, MAXVALUE as params to add.
     * No client-side handlers are configured for this scenario.  
     * The endpoint method (addNumbersHandler) will detect the possible overflow and
     * throw an unchecked exception, NullPointerException.
     * 
     * The server-side AddNumbersProtocolHandler will
     * access the thrown exception using the "jaxws.webmethod.exception"
     * property and add the stack trace string to fault string.
     * 
     * The client should receive a SOAPFaultException that has a stack
     * trace as part of the message.
     * This test verifies the following:
     * 
     * 1)  Proper exception/fault processing when handlers are installed.
     * 2)  Access to the special "jaxws.webmethod.exception"
     * 3)  Proper exception call flow when an unchecked exception is thrown.
     */
    public void testAddNumbersHandler_WithHandlerException() throws Exception {

        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());

        AddNumbersHandlerService service = new AddNumbersHandlerService();
        AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();

        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
        SOAPFaultException expectedException = null;
        Throwable t = null;
        try {
            // Trigger protocol 2 to throw an exception
            AddNumbersProtocolHandler2.throwException = true;
            proxy.addNumbersHandler(-1000, Integer.MIN_VALUE);
            
        } catch (Throwable e) {
            // An exception is expected
            t = e;
            
        }  finally {
            AddNumbersProtocolHandler2.throwException = false;
        }
        
        // Make sure the proper exception is thrown
        if (t == null) {
            fail("Expected AddNumbersHandlerFault_Exception to be thrown");
        }
        
        if (t instanceof SOAPFaultException) {
            expectedException = (SOAPFaultException) t;
            String fault = ((SOAPFaultException)t).getFault().toString();
            assertTrue("Expected SOAPFaultException to be thrown with AddNumbersProtocolHandler2 exception " + fault,
                    fault.contains("AddNumbersProtocolHandler2"));
        } else {
            fail("Expected SOAPFaultException to be thrown, " +
                        "but the exception is: " + t);
        }
       
    }
    
    public void testAddNumbersHandlerDispatch() {
        try {
            QName serviceName =
                    new QName("http://org/test/addnumbershandler", "AddNumbersHandlerService");
            QName portName =
                    new QName("http://org/test/addnumbershandler", "AddNumbersHandlerPort");

            Service myService = Service.create(serviceName);
            
            myService.addPort(portName, null, axisEndpoint);
            Dispatch<Source> myDispatch = myService.createDispatch(portName, Source.class, 
                                                                   Service.Mode.MESSAGE);

            // set handler chain for binding provider
            Binding binding = ((BindingProvider) myDispatch).getBinding();

            // create a new list or use the existing one
            List<Handler> handlers = binding.getHandlerChain();
        
            if (handlers == null)
                handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler());
            handlers.add(new AddNumbersClientProtocolHandler());
            binding.setHandlerChain(handlers);
            
            //Invoke the Dispatch
            TestLogger.logger.debug(">> Invoking Async Dispatch");
            Source response = myDispatch.invoke(createRequestSource());
            String resString = getString(response);
            if (!resString.contains("<return>16</return>")) {
                fail("Response string should contain <return>16</return>, but does not.  The resString was: \"" + resString + "\"");
            }
            
            String log = readLogFile();
            String expected_calls = "AddNumbersClientLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "AddNumbersClientProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "AddNumbersClientProtocolHandler GET_HEADERS\n"
                    + "AddNumbersLogicalHandler2 POST_CONSTRUCT\n"
                    + "AddNumbersProtocolHandler2 GET_HEADERS\n"
                    + "AddNumbersProtocolHandler GET_HEADERS\n"
                    + "AddNumbersProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                    + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_INBOUND\n"
                    + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_INBOUND\n"
                    + "AddNumbersLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                    + "AddNumbersLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                    + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                    + "AddNumbersProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                    + "AddNumbersLogicalHandler CLOSE\n"
                    + "AddNumbersLogicalHandler2 CLOSE\n"
                    + "AddNumbersProtocolHandler2 CLOSE\n"
                    + "AddNumbersProtocolHandler CLOSE\n"
                    + "AddNumbersProtocolHandler PRE_DESTROY\n"
                    + "AddNumbersClientProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                    + "AddNumbersClientLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                    + "AddNumbersClientProtocolHandler CLOSE\n"
                    + "AddNumbersClientLogicalHandler CLOSE\n";
            assertEquals(expected_calls, log);
            
            TestLogger.logger.debug("----------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    public void testAddNumbersHandlerDispatchMyResolver() {
        try {
            QName serviceName =
                    new QName("http://org/test/addnumbershandler", "AddNumbersHandlerService");
            QName portName =
                    new QName("http://org/test/addnumbershandler", "AddNumbersHandlerPort");

            Service myService = Service.create(serviceName);
            
            myService.setHandlerResolver(new MyHandlerResolver());
            
            myService.addPort(portName, null, axisEndpoint);
            Dispatch<Source> myDispatch = myService.createDispatch(portName, Source.class, 
                                                                   Service.Mode.MESSAGE);

            //Invoke the Dispatch
            TestLogger.logger.debug(">> Invoking Async Dispatch");
            Source response = myDispatch.invoke(createRequestSource());
            String resString = getString(response);
            if (!resString.contains("<return>16</return>")) {
                fail("Response string should contain <return>16</return>, but does not.  The resString was: \"" + resString + "\"");
            }
            
            String log = readLogFile();
            String expected_calls = "AddNumbersClientLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientProtocolHandler GET_HEADERS\n"
                + "AddNumbersLogicalHandler2 POST_CONSTRUCT\n"
                + "AddNumbersProtocolHandler2 GET_HEADERS\n"
                + "AddNumbersProtocolHandler GET_HEADERS\n"
                + "AddNumbersProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersLogicalHandler CLOSE\n"
                + "AddNumbersLogicalHandler2 CLOSE\n"
                + "AddNumbersProtocolHandler2 CLOSE\n"
                + "AddNumbersProtocolHandler CLOSE\n"
                + "AddNumbersProtocolHandler PRE_DESTROY\n"
                + "AddNumbersClientProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersClientLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersClientProtocolHandler CLOSE\n"
                + "AddNumbersClientLogicalHandler CLOSE\n";
            assertEquals(expected_calls, log);
            
            TestLogger.logger.debug("----------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /*
     * JAXWS 9.2.1.1 conformance test
     */
    public void testAddNumbersHandlerResolver() {
        try {
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());

            AddNumbersHandlerService service = new AddNumbersHandlerService();

            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            service.setHandlerResolver(new MyHandlerResolver());

            BindingProvider p = (BindingProvider) proxy;
            
            /*
             * despite setting MyHandlerResolver on the service, we should get an empty
             * list from the getBinding().getHandlerChain() call below.  JAXWS 9.2.1.1 conformance
             */
            List<Handler> list = p.getBinding().getHandlerChain();
            
            assertTrue("List should be empty.  We've not conformed to JAXWS 9.2.1.1.", list.isEmpty());
            String log = readLogFile();
            assertNull("log should be empty, since no handlers are in the list and we never called a service", log);
            
            TestLogger.logger.debug("----------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testAddNumbersHandlerWithFault() {
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);  
            // value 99 triggers the handler to throw an exception, but does
            // NOT trigger the AddNumbersHandler.handlefault method.
            // The spec does not call the handlefault method of a handler that
            // causes a flow reversal
            int total = proxy.addNumbersHandler(99,10);
            
            fail("We should have got an exception due to the handler.");
        } catch(Exception e) {
            e.printStackTrace();
            assertTrue("Exception should be SOAPFaultException", e instanceof SOAPFaultException);
            assertEquals(((SOAPFaultException)e).getMessage(), "I don't like the value 99");
            
            String log = readLogFile();
            String expected_calls = "AddNumbersLogicalHandler2 POST_CONSTRUCT\n"
                + "AddNumbersProtocolHandler2 GET_HEADERS\n"
                + "AddNumbersProtocolHandler GET_HEADERS\n"
                + "AddNumbersProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler2 THROWING PROTOCOLEXCEPTION INBOUND\n"
                + "AddNumbersProtocolHandler2 HANDLE_FAULT_OUTBOUND\n"
                + "AddNumbersProtocolHandler HANDLE_FAULT_OUTBOUND\n"
                + "AddNumbersLogicalHandler2 CLOSE\n"
                + "AddNumbersProtocolHandler2 CLOSE\n"
                + "AddNumbersProtocolHandler CLOSE\n"
                + "AddNumbersProtocolHandler PRE_DESTROY\n";
            assertEquals(expected_calls, log);
        }
        TestLogger.logger.debug("----------------------------------");
    }


    /**
     * testAddNumbersClientHandler performs the same tests as testAddNumbersHandler, except
     * that two client-side handlers are also inserted into the flow.  The inbound AddNumbersClientLogicalHandler
     * checks that the properties set here in this method (the client app) and the properties set in the
     * outbound AddNumbersClientProtocolHandler are accessible.  These properties are also checked here in
     * the client app.  AddNumbersClientLogicalHandler also subtracts 1 from the sum on the inbound flow.
     */
    public void testAddNumbersClientHandler() {
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);
            p.getRequestContext().put("myClientKey", "myClientVal");

            List<Handler> handlers = p.getBinding().getHandlerChain();
            if (handlers == null)
                handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler());
            handlers.add(new AddNumbersClientProtocolHandler());
            p.getBinding().setHandlerChain(handlers);

            int total = proxy.addNumbersHandler(10,10);
            
            // see if I can get an APPLICATION scoped property set during outbound flow.  I should be able to do this according to 4.2.1
            
            // TODO:  assert is now commented out.  This property is set by a client outbound handler, and I don't think it
            // should be available on the request or response contexts.
            //assertNotNull("Should be able to retrieve APPLICATION scoped property, but could not.", ((String)p.getRequestContext().get("AddNumbersClientProtocolHandlerOutboundAppScopedProperty")));

            // should NOT be able to get this HANDLER scoped property though
            assertNull("Should not be able to retrieve HANDLER scoped property, but was able.", (String)p.getResponseContext().get("AddNumbersClientProtocolHandlerOutboundHandlerScopedProperty"));
            // should be able to get this APPLICATION scoped property set during inbound flow
            assertNotNull("Should be able to retrieve APPLICATION scoped property, but could not.", (String)p.getResponseContext().get("AddNumbersClientProtocolHandlerInboundAppScopedProperty"));
            // should NOT be able to get this HANDLER scoped property though
            assertNull("Should not be able to retrieve HANDLER scoped property, but was able.", (String)p.getResponseContext().get("AddNumbersClientProtocolHandlerInboundHandlerScopedProperty"));
            // should be able to get this APPLICATION scoped property set by this client
            assertNotNull("Should be able to retrieve APPLICATION scoped property, but could not.", (String)p.getRequestContext().get("myClientKey"));

            assertEquals("With handler manipulation, total should be 4 less than a proper sumation.", 16, total);
            TestLogger.logger.debug("Total (after handler manipulation) = " + total);
            
            String log = readLogFile();
            String expected_calls = "AddNumbersClientLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientProtocolHandler GET_HEADERS\n"
                + "AddNumbersLogicalHandler2 POST_CONSTRUCT\n"
                + "AddNumbersProtocolHandler2 GET_HEADERS\n"
                + "AddNumbersProtocolHandler GET_HEADERS\n"
                + "AddNumbersProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersLogicalHandler CLOSE\n"
                + "AddNumbersLogicalHandler2 CLOSE\n"
                + "AddNumbersProtocolHandler2 CLOSE\n"
                + "AddNumbersProtocolHandler CLOSE\n"
                + "AddNumbersProtocolHandler PRE_DESTROY\n"
                + "AddNumbersClientProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersClientLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersClientProtocolHandler CLOSE\n"
                + "AddNumbersClientLogicalHandler CLOSE\n";
            assertEquals(expected_calls, log);
            
            TestLogger.logger.debug("----------------------------------");
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /*
     * uses a custom HandlerResolver instead of the default.  MyHandlerResolver
     * puts the AddNumbersClientLogicalHandler and AddNumbersClientProtocolHandler
     * in the flow.  Results should be the same as testAddNumbersClientHandler.
     */
    public void testAddNumbersClientHandlerMyResolver() {
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            service.setHandlerResolver(new MyHandlerResolver());
            
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);

            int total = proxy.addNumbersHandler(10,10);
            
            assertEquals("With handler manipulation, total should be 4 less than a proper sumation.",
                         16,
                         total);
            TestLogger.logger.debug("Total (after handler manipulation) = " + total);
            
            String log = readLogFile();
            String expected_calls = "AddNumbersClientLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientProtocolHandler GET_HEADERS\n"
                + "AddNumbersLogicalHandler2 POST_CONSTRUCT\n"
                + "AddNumbersProtocolHandler2 GET_HEADERS\n"
                + "AddNumbersProtocolHandler GET_HEADERS\n"
                + "AddNumbersProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersLogicalHandler CLOSE\n"
                + "AddNumbersLogicalHandler2 CLOSE\n"
                + "AddNumbersProtocolHandler2 CLOSE\n"
                + "AddNumbersProtocolHandler CLOSE\n"
                + "AddNumbersProtocolHandler PRE_DESTROY\n"
                + "AddNumbersClientProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersClientLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersClientProtocolHandler CLOSE\n"
                + "AddNumbersClientLogicalHandler CLOSE\n";
            assertEquals(expected_calls, log);
            
            TestLogger.logger.debug("----------------------------------");
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    public void testAddNumbersClientProtoAndLogicalHandler() {
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);

            List<Handler> handlers = p.getBinding().getHandlerChain();
            if (handlers == null)
                handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler());
            handlers.add(new AddNumbersClientProtocolHandler());
            p.getBinding().setHandlerChain(handlers);

            // value 102 triggers an endpoint exception, which will run through the server outbound
            // handleFault methods, then client inbound handleFault methods
            int total = proxy.addNumbersHandler(102,10);
            
            fail("should have got an exception, but didn't");
        } catch(Exception e) {
            e.printStackTrace();
            assertTrue("Exception should be SOAPFaultException", e instanceof SOAPFaultException);
            //AXIS2-2417 - assertEquals(((SOAPFaultException)e).getMessage(), "AddNumbersLogicalHandler2 was here");
            assertTrue(((SOAPFaultException)e).getMessage().contains("Got value 101.  " +
            		"AddNumbersHandlerPortTypeImpl.addNumbersHandler method is " +
            		"correctly throwing this exception as part of testing"));
            
            String log = readLogFile();
            String expected_calls = "AddNumbersClientLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientProtocolHandler GET_HEADERS\n"
                + "AddNumbersLogicalHandler2 POST_CONSTRUCT\n"
                + "AddNumbersProtocolHandler2 GET_HEADERS\n"
                + "AddNumbersProtocolHandler GET_HEADERS\n"
                + "AddNumbersProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler HANDLE_FAULT_OUTBOUND\n"
                + "AddNumbersLogicalHandler2 HANDLE_FAULT_OUTBOUND\n"
                + "AddNumbersProtocolHandler2 HANDLE_FAULT_OUTBOUND\n"
                + "AddNumbersProtocolHandler HANDLE_FAULT_OUTBOUND\n"
                + "AddNumbersLogicalHandler CLOSE\n"
                + "AddNumbersLogicalHandler2 CLOSE\n"
                + "AddNumbersProtocolHandler2 CLOSE\n"
                + "AddNumbersProtocolHandler CLOSE\n"
                + "AddNumbersProtocolHandler PRE_DESTROY\n"
                + "AddNumbersClientProtocolHandler HANDLE_FAULT_INBOUND\n"
                + "AddNumbersClientLogicalHandler HANDLE_FAULT_INBOUND\n"
                + "AddNumbersClientProtocolHandler CLOSE\n"
                + "AddNumbersClientLogicalHandler CLOSE\n";
            assertEquals(expected_calls, log);
            
        }
        TestLogger.logger.debug("----------------------------------");
    }
    
    public void testAddNumbersClientHandlerWithFault() {
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);

            List<Handler> handlers = p.getBinding().getHandlerChain();
            if (handlers == null)
                handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler4());
            handlers.add(new AddNumbersClientLogicalHandler3());
            handlers.add(new AddNumbersClientLogicalHandler());
            
            p.getBinding().setHandlerChain(handlers);

            int total = proxy.addNumbersHandler(99,10);
            
            fail("Should have got an exception, but we didn't.");
        } catch(Exception e) {
            e.printStackTrace();
            assertTrue("Exception should be SOAPFaultException. Found " +e.getClass() + " "+ e.getMessage(), e instanceof SOAPFaultException);
            assertEquals("I don't like the value 99", ((SOAPFaultException)e).getMessage());
            
            String log = readLogFile();
            String expected_calls = "AddNumbersClientLogicalHandler4 HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientLogicalHandler3 HANDLE_FAULT_OUTBOUND\n"
                + "AddNumbersClientLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientLogicalHandler3 HANDLE_FAULT_INBOUND\n"
                + "AddNumbersClientLogicalHandler3 RETURNING FALSE INBOUND\n"
                + "AddNumbersClientLogicalHandler CLOSE\n"
                + "AddNumbersClientLogicalHandler3 CLOSE\n"
                + "AddNumbersClientLogicalHandler4 CLOSE\n";
            assertEquals(expected_calls, log);
            
        }
        TestLogger.logger.debug("----------------------------------");
    }

    public void testAddNumbersClientHandlerWithFalse() throws Exception {
        AddNumbersClientLogicalHandler2 clh = new AddNumbersClientLogicalHandler2();
        AddNumbersClientProtocolHandler  cph = new AddNumbersClientProtocolHandler();
        cph.setPivot(true);
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);

            List<Handler> handlers = p.getBinding().getHandlerChain();
            if (handlers == null) {
                handlers = new ArrayList<Handler>();
            }
            handlers.add(clh);
            handlers.add(cph);
            
            p.getBinding().setHandlerChain(handlers);

            int total = proxy.addNumbersHandler(99,10);
            
            // Note that a return of 0 indicates that the new message that was added to
            // in the client protocol handler was lost during handler processing.
            assertTrue("Expected a pivot and -99 to be returned. But it was "+ total, total == -99);
        } catch(Exception e) {
           throw e;
        } finally {
            cph.setPivot(false);
        }
        
        String log = readLogFile();
        String expected_calls = 
              "AddNumbersClientLogicalHandler2 HANDLE_MESSAGE_OUTBOUND\n"
            + "AddNumbersClientProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
            + "AddNumbersClientLogicalHandler2 HANDLE_MESSAGE_INBOUND\n"
            + "AddNumbersClientProtocolHandler CLOSE\n"
            + "AddNumbersClientLogicalHandler2 CLOSE\n";
        assertEquals(expected_calls, log);
            
        TestLogger.logger.debug("----------------------------------");
    }
    /**
     * test results should be the same as testAddNumbersClientHandler, except that
     * AddNumbersClientLogicalHandler2 doubles the first param on outbound.  Async, of course.
     *
     */
    public void testAddNumbersClientHandlerAsync() {
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);

            List<Handler> handlers = p.getBinding().getHandlerChain();
            if (handlers == null)
                handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler());
            handlers.add(new AddNumbersClientLogicalHandler2());
            handlers.add(new AddNumbersClientProtocolHandler());
            p.getBinding().setHandlerChain(handlers);

            
            AddNumbersHandlerAsyncCallback callback = new AddNumbersHandlerAsyncCallback();
            Future<?> future = proxy.addNumbersHandlerAsync(10, 10, callback);

            while (!future.isDone()) {
                Thread.sleep(1000);
                TestLogger.logger.debug("Async invocation incomplete");
            }
            
            int total = callback.getResponseValue();
            
            assertEquals("With handler manipulation, total should be 26.", 26, total);
            TestLogger.logger.debug("Total (after handler manipulation) = " + total);
            
            String log = readLogFile();
            String expected_calls = "AddNumbersClientLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientLogicalHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientProtocolHandler GET_HEADERS\n"
                + "AddNumbersLogicalHandler2 POST_CONSTRUCT\n"
                + "AddNumbersProtocolHandler2 GET_HEADERS\n"
                + "AddNumbersProtocolHandler GET_HEADERS\n"
                + "AddNumbersProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersProtocolHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersLogicalHandler CLOSE\n"
                + "AddNumbersLogicalHandler2 CLOSE\n"
                + "AddNumbersProtocolHandler2 CLOSE\n"
                + "AddNumbersProtocolHandler CLOSE\n"
                + "AddNumbersProtocolHandler PRE_DESTROY\n"
                + "AddNumbersClientProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersClientLogicalHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersClientLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersClientProtocolHandler CLOSE\n"
                + "AddNumbersClientLogicalHandler2 CLOSE\n"
                + "AddNumbersClientLogicalHandler CLOSE\n";
            assertEquals(expected_calls, log);
            
            TestLogger.logger.debug("----------------------------------");
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
    
    public void testAddNumbersHandlerHandlerResolver() {
        try {
            System.out.println("----------------------------------");
            System.out.println("test: " + getName());
            AddNumbersHandlerService service = new AddNumbersHandlerService(); // will give NPE:
            List<Handler> handlers = service.getHandlerResolver()
                    .getHandlerChain(null);
            assertNotNull(
                    "Default handlers list should not be null but empty.",
                    handlers);
            
            String log = readLogFile();
            assertNull("log should be null since we did not call any services", log);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        TestLogger.logger.debug("----------------------------------");
    } 
    
    public void testOneWay() {
        try {
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider bp = (BindingProvider) proxy;
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);
            proxy.oneWayInt(11);
            
            // one-way invocations run in their own thread,
            // and we can't tell here in the client when it
            // has completed.  So, we need to wait long enough
            // for the invocation to complete, so our log file
            // is fully populated.
            Thread.sleep(1000 * 5); // 5 seconds
            
            String log = readLogFile();
            String expected_calls = "AddNumbersLogicalHandler2 POST_CONSTRUCT\n"
                + "AddNumbersProtocolHandler2 GET_HEADERS\n"
                + "AddNumbersProtocolHandler GET_HEADERS\n"
                + "AddNumbersProtocolHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersProtocolHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler2 HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler HANDLE_MESSAGE_INBOUND\n"
                + "AddNumbersLogicalHandler CLOSE\n"
                + "AddNumbersLogicalHandler2 CLOSE\n"
                + "AddNumbersProtocolHandler2 CLOSE\n"
                + "AddNumbersProtocolHandler CLOSE\n";
            assertEquals(expected_calls, log);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        TestLogger.logger.debug("----------------------------------");
    }
    
    public void testOneWayWithProtocolException() {
        Exception exception = null;
        try {
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());

            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();

            BindingProvider p = (BindingProvider) proxy;

            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
            p.getRequestContext().put("myClientKey", "myClientVal");

            List<Handler> handlers = p.getBinding().getHandlerChain();
            if (handlers == null)
                handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler());
            handlers.add(new AddNumbersClientProtocolHandler());
            p.getBinding().setHandlerChain(handlers);
            
            BindingProvider bp = (BindingProvider) proxy;
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
            // value 99 will trigger exception from AddNumbersClientLogicalHandler
            proxy.oneWayInt(99);
        } catch (Exception e) {            
            e.printStackTrace();
            exception = e;
        }
        
        // exceptions on one-way invocations are suppressed by default
        assertNull(exception);
            
        // one-way invocations run in their own thread,
        // and we can't tell here in the client when it
        // has completed.  So, we need to wait long enough
        // for the invocation to complete, so our log file
        // is fully populated.
        try {
            Thread.sleep(1000 * 5); // 5 seconds
        } catch (InterruptedException ie) {
            // nothing
        }
            
        String log = readLogFile();
        String expected_calls = "AddNumbersClientLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientLogicalHandler CLOSE\n";
        assertEquals(expected_calls, log);
                    
        TestLogger.logger.debug("----------------------------------");
    }
    
    public void testOneWayWithRuntimeException() {
        Exception exception = null;
        try {
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());

            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();

            BindingProvider p = (BindingProvider) proxy;

            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
            p.getRequestContext().put("myClientKey", "myClientVal");

            List<Handler> handlers = p.getBinding().getHandlerChain();
            if (handlers == null)
                handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler());
            handlers.add(new AddNumbersClientProtocolHandler());
            p.getBinding().setHandlerChain(handlers);
            
            BindingProvider bp = (BindingProvider) proxy;
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
            // value 99 will trigger exception from AddNumbersClientLogicalHandler
            proxy.oneWayInt(999);
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }
        
        // exceptions on one-way invocations are suppressed by default
        assertNull(exception);            
            
        // one-way invocations run in their own thread,
        // and we can't tell here in the client when it
        // has completed.  So, we need to wait long enough
        // for the invocation to complete, so our log file
        // is fully populated.
        try {
            Thread.sleep(1000 * 5); // 5 seconds
        } catch (InterruptedException ie) {
            // nothing
        }
            
        String log = readLogFile();
        String expected_calls = "AddNumbersClientLogicalHandler HANDLE_MESSAGE_OUTBOUND\n"
                + "AddNumbersClientLogicalHandler CLOSE\n";
        assertEquals(expected_calls, log);
        
        TestLogger.logger.debug("----------------------------------");
    }


    /*
     * A callback implementation that can be used to collect the exceptions
     */
    class AddNumbersHandlerAsyncCallback implements AsyncHandler<AddNumbersHandlerResponse> {
     
        private Exception exception;
        private int retVal;
        
        public void handleResponse(Response<AddNumbersHandlerResponse> response) {
            try {
                TestLogger.logger.debug("FaultyAsyncHandler.handleResponse() was called");
                AddNumbersHandlerResponse r = response.get();
                TestLogger.logger.debug("No exception was thrown from Response.get()");
                retVal = r.getReturn();
            }
            catch (Exception e) {
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
    
    class MyHandlerResolver implements HandlerResolver {

        public List<Handler> getHandlerChain(PortInfo portinfo) {
            ArrayList<Handler> handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler());
            handlers.add(new AddNumbersClientProtocolHandler());
            return handlers;
        }

    }
    
    private String getString(Source source) throws Exception {
        if (source == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        Result result = new StreamResult(writer);
        t.transform(source, result);
        return writer.getBuffer().toString();

    }
    
    /**
     * Create a Source request to be used by Dispatch<Source>
     */
    private Source createRequestSource() throws IOException {
        FileInputStream fis = new FileInputStream(requestFile);
        return new StreamSource(fis);
    }
    
    protected void setUp() {
        File file = new File(filelogname);
        file.delete();  // yes, delete for each retrieval, which should only happen once per test
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
