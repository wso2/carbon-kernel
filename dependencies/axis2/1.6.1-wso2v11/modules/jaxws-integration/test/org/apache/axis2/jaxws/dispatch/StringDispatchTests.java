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

package org.apache.axis2.jaxws.dispatch;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.testutils.AllTestsWithRuntimeIgnore;
import org.junit.runner.RunWith;

import javax.xml.ws.Dispatch;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RunWith(AllTestsWithRuntimeIgnore.class)
public class StringDispatchTests extends AbstractTestCase {

    
    public static Test suite() {
        return getTestSetup(new TestSuite(StringDispatchTests.class));
    }
	
	/**
     * Invoke a sync Dispatch<String> in PAYLOAD mode
     */
    public void testSyncPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.PAYLOAD);
        
        // Invoke the Dispatch
        TestLogger.logger.debug(">> Invoking sync Dispatch");
        String response = dispatch.invoke(DispatchTestConstants.sampleBodyContent);

        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(response.contains("echoStringResponse"));
        
        // Invoke a second time to verify
        // Invoke the Dispatch
        TestLogger.logger.debug(">> Invoking sync Dispatch");
        response = dispatch.invoke(DispatchTestConstants.sampleBodyContent);

        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(response.contains("echoStringResponse"));
	}
    
    /**
     * Invoke a sync Dispatch<String> in PAYLOAD mode
     * Server response with exception.  Section 4.3.2
     * says we should get a ProtocolException, not a
     * WebServiceException.
     */
    public void testSyncPayloadMode_exception() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.PAYLOAD);
        
        // Invoke the Dispatch
        TestLogger.logger.debug(">> Invoking sync Dispatch");
        Exception e = null;
        try {
            // The _bad string passes "THROW EXCEPTION", which causes the echo function on the
            // server to throw a RuntimeException.  We should get a ProtocolException here on the client
            String response = dispatch.invoke(DispatchTestConstants.sampleBodyContent_bad);
        } catch (Exception ex) {
            e = ex;
        }

        assertNotNull("No exception received", e);
        assertTrue("'e' should be of type ProtocolException", e instanceof ProtocolException);
        
        // Invoke a second time to verify
        
        // Invoke the Dispatch
        TestLogger.logger.debug(">> Invoking sync Dispatch");
        e = null;
        try {
            // The _bad string passes "THROW EXCEPTION", which causes the echo function on the
            // server to throw a RuntimeException.  We should get a ProtocolException here on the client
            String response = dispatch.invoke(DispatchTestConstants.sampleBodyContent_bad);
        } catch (Exception ex) {
            e = ex;
        }

        assertNotNull("No exception received", e);
        assertTrue("'e' should be of type ProtocolException", e instanceof ProtocolException);

    }
    
    /**
     * Invoke a sync Dispatch<String> in MESSAGE mode
     */
    public void testSyncWithMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.MESSAGE);
        
        // Invoke the Dispatch
        TestLogger.logger.debug(">> Invoking sync Dispatch");
        String response = dispatch.invoke(DispatchTestConstants.sampleSoapMessage);

        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(response.contains("soap"));
        assertTrue(response.contains("Envelope"));
        assertTrue(response.contains("Body"));
        assertTrue(response.contains("echoStringResponse"));
        
        
        // Invoke a second time to verify
        // Invoke the Dispatch
        TestLogger.logger.debug(">> Invoking sync Dispatch");
        response = dispatch.invoke(DispatchTestConstants.sampleSoapMessage);

        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(response.contains("soap"));
        assertTrue(response.contains("Envelope"));
        assertTrue(response.contains("Body"));
        assertTrue(response.contains("echoStringResponse"));
	}
    
    /**
     * Invoke a Dispatch<String> using the async callback API in PAYLOAD mode
     */
    public void testAsyncCallbackPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.PAYLOAD);

        // Create the callback for async responses
        AsyncCallback<String> callback = new AsyncCallback<String>();

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        Future<?> monitor = dispatch.invokeAsync(DispatchTestConstants.sampleBodyContent, callback);
	        
        while (!monitor.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        String response = callback.getValue();
        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(response.contains("echoStringResponse"));
        
        
        // Invoke a second time to verify
        // Create the callback for async responses
        callback = new AsyncCallback<String>();

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        monitor = dispatch.invokeAsync(DispatchTestConstants.sampleBodyContent, callback);
                
        while (!monitor.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        response = callback.getValue();
        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(response.contains("echoStringResponse"));
	}
    
    /**
     * Invoke a Dispatch<String> using the async callback API in MESSAGE mode
     */
    public void testAsyncCallbackMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.MESSAGE);

        // Create the callback for async responses
        AsyncCallback<String> callback = new AsyncCallback<String>();

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch with Message Mode");
        Future<?> monitor = dispatch.invokeAsync(DispatchTestConstants.sampleSoapMessage, callback);
    
        while (!monitor.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        String response = callback.getValue();
        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(response.contains("soap"));
        assertTrue(response.contains("Envelope"));
        assertTrue(response.contains("Body"));
        assertTrue(response.contains("echoStringResponse"));
        
        // Invoke a second time to verify
        // Create the callback for async responses
        callback = new AsyncCallback<String>();

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch with Message Mode");
        monitor = dispatch.invokeAsync(DispatchTestConstants.sampleSoapMessage, callback);
    
        while (!monitor.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        response = callback.getValue();
        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(response.contains("soap"));
        assertTrue(response.contains("Envelope"));
        assertTrue(response.contains("Body"));
        assertTrue(response.contains("echoStringResponse"));
	}
    
    /**
     * Invoke a Dispatch<String> using the async polling API in PAYLOAD mode
     */
    public void testAsyncPollingPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.PAYLOAD);

        TestLogger.logger.debug(">> Invoking async (polling) Dispatch");
        Response<String> asyncResponse = dispatch.invokeAsync(DispatchTestConstants.sampleBodyContent);
            
        while (!asyncResponse.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        String response = asyncResponse.get();
        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(response.contains("echoStringResponse"));
        
        // Invoke a second time to verify
        TestLogger.logger.debug(">> Invoking async (polling) Dispatch");
        asyncResponse = dispatch.invokeAsync(DispatchTestConstants.sampleBodyContent);
            
        while (!asyncResponse.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        response = asyncResponse.get();
        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(response.contains("echoStringResponse"));
    }
    
    /**
     * Invoke a Dispatch<String> using the async polling API in MESSAGE mode
     */
    public void testAsyncPollingMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.MESSAGE);

        TestLogger.logger.debug(">> Invoking async (polling) Dispatch with Message Mode");
        Response<String> asyncResponse = dispatch.invokeAsync(DispatchTestConstants.sampleSoapMessage);
    
        while (!asyncResponse.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        String response = asyncResponse.get();
        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(response.contains("soap"));
        assertTrue(response.contains("Envelope"));
        assertTrue(response.contains("Body"));
        assertTrue(response.contains("echoStringResponse"));
        
        // Invoke a second time to verify
        TestLogger.logger.debug(">> Invoking async (polling) Dispatch with Message Mode");
        asyncResponse = dispatch.invokeAsync(DispatchTestConstants.sampleSoapMessage);
    
        while (!asyncResponse.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        response = asyncResponse.get();
        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(response.contains("soap"));
        assertTrue(response.contains("Envelope"));
        assertTrue(response.contains("Body"));
        assertTrue(response.contains("echoStringResponse"));
    }
	
    /**
     * Invoke a Dispatch<String> one-way in PAYLOAD mode 
     */
    public void testOneWayPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.PAYLOAD);

        TestLogger.logger.debug(">> Invoking one-way Dispatch");
        dispatch.invokeOneWay(DispatchTestConstants.sampleBodyContent);
        
        // Invoke a second time to verify
        TestLogger.logger.debug(">> Invoking one-way Dispatch");
        dispatch.invokeOneWay(DispatchTestConstants.sampleBodyContent);
    }
    
    /**
     * Invoke a Dispatch<String> one-way in MESSAGE mode 
	 */
    public void testOneWayMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.MESSAGE);

        TestLogger.logger.debug(">> Invoking one-way Dispatch");
        dispatch.invokeOneWay(DispatchTestConstants.sampleSoapMessage);
        
        // Invoke a second time to verify
        TestLogger.logger.debug(">> Invoking one-way Dispatch");
        dispatch.invokeOneWay(DispatchTestConstants.sampleSoapMessage);
	}
    
    
    public void testSyncPayloadMode_badHostName() throws Exception {
        checkUnknownHostURL(DispatchTestConstants.BADURL);
        
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.BADURL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.PAYLOAD);
        
        // Invoke the Dispatch
        Throwable ttemp = null;
        try {
            TestLogger.logger.debug(">> Invoking sync Dispatch");
            String response = dispatch.invoke(DispatchTestConstants.sampleBodyContent);
        } catch (Throwable t) {
            assertTrue(t instanceof WebServiceException);
            assertTrue(t.getCause() instanceof UnknownHostException);
            ttemp = t;
        }
        assertNotNull(ttemp);
        
        // Invoke a second time to verify
        // Invoke the Dispatch
        ttemp = null;
        try {
            TestLogger.logger.debug(">> Invoking sync Dispatch");
            String response = dispatch.invoke(DispatchTestConstants.sampleBodyContent);
        } catch (Throwable t) {
            assertTrue(t instanceof WebServiceException);
            assertTrue(t.getCause() instanceof UnknownHostException);
            ttemp = t;
        }
        assertNotNull(ttemp);

    }
    
    public void testAsyncCallbackMessageMode_badHostName() throws Exception {
        checkUnknownHostURL(DispatchTestConstants.BADURL);
        
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.BADURL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.MESSAGE);

        // Create the callback for async responses
        AsyncCallback<String> callback = new AsyncCallback<String>();

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch with Message Mode");
        Future<?> monitor = dispatch.invokeAsync(DispatchTestConstants.sampleSoapMessage, callback);
    
        while (!monitor.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        if (callback.hasError()) {
            Throwable t = callback.getError();
            t.printStackTrace();
            
            assertTrue(t.getClass().getName() + " does not match expected type ExecutionException", t instanceof ExecutionException);
            
            Throwable cause = t.getCause();
            assertNotNull("There must be a cause under the ExecutionException", cause);
            assertTrue(cause.getClass().getName() + " does not match expected type WebServiceException" ,cause instanceof WebServiceException);
            
            Throwable hostException = t.getCause().getCause();
            assertNotNull("There must be a cause under the WebServiceException", hostException);
            assertTrue(hostException.getClass().getName() + " does not match expected type UnknownHostException", hostException instanceof UnknownHostException);
        } else {
            fail("No fault thrown.  Should have retrieved an UnknownHostException from callback");
        }
        
        // Invoke a second time to verify
        // Create the callback for async responses
        callback = new AsyncCallback<String>();

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch with Message Mode");
        monitor = dispatch.invokeAsync(DispatchTestConstants.sampleSoapMessage, callback);
    
        while (!monitor.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        if (callback.hasError()) {
            Throwable t = callback.getError();
            t.printStackTrace();
            
            assertTrue(t.getClass().getName() + " does not match expected type ExecutionException", t instanceof ExecutionException);
            
            Throwable cause = t.getCause();
            assertNotNull("There must be a cause under the ExecutionException", cause);
            assertTrue(cause.getClass().getName() + " does not match expected type WebServiceException" ,cause instanceof WebServiceException);
            
            Throwable hostException = t.getCause().getCause();
            assertNotNull("There must be a cause under the WebServiceException", hostException);
            assertTrue(hostException.getClass().getName() + " does not match expected type UnknownHostException", hostException instanceof UnknownHostException);
        } else {
            fail("No fault thrown.  Should have retrieved an UnknownHostException from callback");
        }
    }
    
    public void testAsyncPollingPayloadMode_badHostName() throws Exception {
        checkUnknownHostURL(DispatchTestConstants.BADURL);
        
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.BADURL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.PAYLOAD);

        TestLogger.logger.debug(">> Invoking async (polling) Dispatch");
        Response<String> asyncResponse = dispatch.invokeAsync(DispatchTestConstants.sampleBodyContent);
            
        while (!asyncResponse.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Throwable ttemp = null;
        try {
            asyncResponse.get();
        } catch (Throwable t) {
            assertTrue(t instanceof ExecutionException);
            assertTrue(t.getCause() instanceof WebServiceException);
            assertTrue(t.getCause().getCause() instanceof UnknownHostException);
            ttemp = t;
        }
        assertNotNull(ttemp);
        
        
        // Invoke a second time to verify
        TestLogger.logger.debug(">> Invoking async (polling) Dispatch");
        asyncResponse = dispatch.invokeAsync(DispatchTestConstants.sampleBodyContent);
            
        while (!asyncResponse.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        ttemp = null;
        try {
            asyncResponse.get();
        } catch (Throwable t) {
            assertTrue(t instanceof ExecutionException);
            assertTrue(t.getCause() instanceof WebServiceException);
            assertTrue(t.getCause().getCause() instanceof UnknownHostException);
            ttemp = t;
        }
        assertNotNull(ttemp);
    }
    
}
