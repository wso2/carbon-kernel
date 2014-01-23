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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.sample.parallelasync.common.CallbackHandler;
import org.apache.axis2.jaxws.sample.parallelasync.server.AsyncPort;
import org.apache.axis2.jaxws.sample.parallelasync.server.AsyncService;
import org.test.parallelasync.CustomAsyncResponse;
import org.test.parallelasync.SleepResponse;
import org.test.parallelasync.WakeUpResponse;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Tests using Asynchronous Callback and an Executor in a multithreaded environment. 
 * 
 * Note there are other basic JAX-WS async tests: 
 * @see ParallelAsyncTests 
 * 
 */
public class AsyncExecutorTests extends AbstractTestCase {

    private static final String DOCLITWR_ASYNC_ENDPOINT =
        "http://localhost:6060/axis2/services/AsyncService.DocLitWrappedPortImplPort";

    // used for logging
    private String myClassName = "AsyncExecutorTests";

    public static Test suite() {
        return getTestSetup(new TestSuite(AsyncExecutorTests.class));
    }

    /**
     * @testStrategy Check that the web service is up and running 
     *               before running any other tests
     */
    public void testService_isAlive() throws Exception {
        final String MESSAGE = "testServiceAlive";

        String title = myClassName + " : " + getName() + " : ";

        AsyncPort port = getPort((Executor)null);

        String req1base = "sleepAsync";
        String req2base = "remappedAsync";

        String request1 = null;
        String request2 = null;

        for (int i = 0; i < 10; i++) {
            
            request1 = req1base + "_" + i;
            request2 = req2base + "_" + i;

            TestLogger.logger.debug(title + "iteration [" + i + "] using request1 [" + request1 +
                    "]  request2 [" + request2 + "]");

            // submit request #1 to the server-side web service that 
            // the web service will keep until we ask for it
            Response<SleepResponse> resp1 = port.sleepAsync(request1);

            // submit request #2 to the server that essentially processes
            // without delay
            Response<CustomAsyncResponse> resp2 = port.remappedAsync(request2);

            // wait until the response for request #2 is done 
            waitBlocking(resp2);

            // check the waiting request #1
            String asleep = port.isAsleep(request1);
            //System.out.println(title+"iteration ["+i+"]   port.isAsleep(request1 ["+request1+"]) = ["+asleep+"]");

            // wakeup the waiting request #1
            String wake = port.wakeUp(request1);
            //System.out.println(title+"iteration ["+i+"]   port.wakeUp(request1 ["+request1+"]) = ["+wake+"]");

            // wait until the response for request #1 is done
            waitBlocking(resp1);
        
            // get the responses
            String req1_result = null;
            String req2_result = null;

            try {
                req1_result = resp1.get().getMessage();
                req2_result = resp2.get().getResponse();
            } catch (Exception e) {
                TestLogger.logger.debug(
                        title + "iteration [" + i + "] using request1 [" + request1 +
                                "]  request2 [" + request2 + "] :  got exception [" +
                                e.getClass().getName() + "]  [" + e.getMessage() + "] ");
                e.printStackTrace();
                fail(e.toString());
            }

            // check status on request #1
            assertEquals("sleepAsync did not sleep as expected", request1, asleep);
            assertEquals("sleepAsync did not return expected response ", request1, req1_result);

            // check status on request #2
            assertEquals("remappedAsync did not return expected response", request2, req2_result);

            // Calling get() again should return the same object as the first call to get()
            assertEquals("sleepAsync did not return expected response ", request1, resp1.get().getMessage());
            assertEquals("remappedAsync did not return expected response", request2, resp2.get().getResponse());
        }
        
        // check the callback operation
		CallbackHandler<SleepResponse> sleepCallbackHandler = new CallbackHandler<SleepResponse>();

        request1 = req1base + "_with_Callback";
        //System.out.println(title+" port.sleepAsync("+request1+", callbackHander)  being submitted....");
		Future<?> sr = port.sleepAsync(request1, sleepCallbackHandler);

        // wait a bit for the server to process the request ...
        Thread.sleep(500);

        // check the waiting request 
        String asleepWithCallback = port.isAsleep(request1);
        //System.out.println(title+" port.isAsleep("+request1+") = ["+asleepWithCallback+"]");

        // wakeup the waiting request
        String wake = port.wakeUp(request1);
        //System.out.println(title+" port.wakeUp("+request1+") = ["+wake+"]");

        // wait a bit..
        Thread.sleep(500);

        // get the response
        String req_cb_result = null;

        try {
            SleepResponse sleepResp = sleepCallbackHandler.get();

            if (sleepResp != null) { 
                req_cb_result = sleepResp.getMessage();
                TestLogger.logger.debug(
                        title + " request [" + request1 + "] :  result [" + req_cb_result + "] ");
            }

        } catch (Exception ex) {
            TestLogger.logger.debug(title + " request [" + request1 + "] :  got exception [" +
                    ex.getClass().getName() + "]  [" + ex.getMessage() + "] ");
            ex.printStackTrace();
            fail(ex.toString());
        }

        // check status on request
        assertEquals("sleepAsync with callback did not sleep as expected", request1, req_cb_result);
    }
    
    /**
     * @testStrategy Test that a client on one thread can issue an AsyncCallback call and exit 
     * immediately and not affect the request being sent to the service and the response
     * being received by an executor  
     */
    public void testMultithreadedCallback() {
        TestThreadMonitor monitor = startupTestThreads();
        
        // Make sure neither thread encountered an error and that both of them were released
        // and run to completion
        assertNull("Receiver thread encountered an exception", monitor.receiverException);
        assertNull("Sender thread encountered an exception", monitor.senderException);
        
        assertTrue("Sender thread not run to completion", monitor.senderThreadExitTime != 0);
        assertTrue("Receiver thread not run to completion", monitor.receiverThreadRequestReceivedTime != 0);

        // Make sure the sender thread completed before the receiver thread found the
        // request had been received by the service endpoint
        assertTrue("Sender thread did not exit before Receiver thread verified request", 
                monitor.senderThreadExitTime < monitor.receiverThreadRequestReceivedTime);
    }

    private static int THREAD_TIMEOUT = 900000;
    private TestThreadMonitor startupTestThreads() {
        
        TestThreadMonitor monitor = new TestThreadMonitor();
        
        Thread sendClientRequestThread = new Thread(new SendClientRequest(monitor));
        Thread receiveClientResponseThread = new Thread(new ReceiveClientResponse(monitor));
        try {
            sendClientRequestThread.start();
            receiveClientResponseThread.start();
            
            sendClientRequestThread.join(THREAD_TIMEOUT);
            receiveClientResponseThread.join(THREAD_TIMEOUT);
        } catch (Exception e) {
            fail("Threads didn't get started: " + e.toString());
        }
        return monitor;
    }
    
    private AsyncService getService(Executor ex) {
        AsyncService service = new AsyncService();

        if (ex!= null)
            service.setExecutor(ex);
        
        if (service.getExecutor() == null) {
            TestLogger.logger.debug(myClassName + " : getService() : executor is null");
        }
        else {
            TestLogger.logger.debug(myClassName + " : getService() : executor is available ");
        }

        return service;
    }

    private AsyncPort getPort(AsyncService service) {

        AsyncPort port = service.getAsyncPort();
        assertNotNull("Port is null", port);

        Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, DOCLITWR_ASYNC_ENDPOINT);
        
        return port;
    }

    /**
     * Auxiliary method used for obtaining a proxy pre-configured with a
     * specific Executor
     */
    private AsyncPort getPort(Executor ex) {
        AsyncService service = getService(ex);

        AsyncPort port = service.getAsyncPort();
        assertNotNull("Port is null", port);

        Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                DOCLITWR_ASYNC_ENDPOINT);
        
        return port;
    }
    
    private void waitBlocking(Future<?> monitor){
        while (!monitor.isDone()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
    
    /**
     * Object used to synchronize and communicate between the thread that sends the request and
     * the thread that processes the response.
     */
    class TestThreadMonitor {
        String request;
        CallbackHandler<SleepResponse> callbackHandler;
        Future<?> futureResponse;
        boolean requestSent = false;
        
        Exception senderException = null; 
        Exception receiverException = null;
        
        long senderThreadExitTime = 0;
        long receiverThreadRequestReceivedTime = 0;
    }
    
    /**
     * Thread to send a JAXWS AsyncCallback request and then exit immediately.  Another thread will 
     * verify that the request was received by the service and process the response.  Note that
     * the sleep operation invoked by this thread will add the String it receives to a list and
     * then suspend processing until a wakeup operation is issued on the service.  That 
     * wakeup will be issued by a different thread in this test.
     * @see {@link ReceiveClientResponse}
     */
    class SendClientRequest implements Runnable {
        private TestThreadMonitor monitor;
        SendClientRequest(TestThreadMonitor monitor) {
            this.monitor = monitor;
        }
        public void run() {
            try {
                sendClientRequest();
                monitor.senderThreadExitTime = System.currentTimeMillis();
                TestLogger.logger.debug("Send Client Thread sent request and now returning");
            } catch (Exception e) {
                monitor.senderException = e;
                TestLogger.logger.error("Send thread caught exception", e);
            } catch (AssertionError e) {
                monitor.senderException = new Exception(e);
                TestLogger.logger.error("Send thread assertion failure", e);
            }
        }
        
        public void sendClientRequest() throws Exception {
            String title = myClassName + " : SendClientRequest : ";

            AsyncService service = new AsyncService();
            AsyncPort port = getPort(service);

            // Setup an executor for response processing to run on a different thread
            ExecutorService ex = Executors.newSingleThreadExecutor();
            service.setExecutor(ex);

            // submit a request to the server that will wait until we ask for it
            // Note that this thread is not going to ask for it; another thread will do that. 
            CallbackHandler<SleepResponse> sleepCallbackHandler1 = new CallbackHandler<SleepResponse>();
            monitor.callbackHandler = sleepCallbackHandler1;
            monitor.request = "sleepAsync_sendClientRequest";

            String request1 = monitor.request;

            TestLogger.logger.debug(title + " port.sleepAsync(" + request1 +
                    ", callbackHander1)  #1 being submitted....");
            Future<?> sr1 = port.sleepAsync(request1, sleepCallbackHandler1);
            monitor.futureResponse = sr1;
            synchronized(monitor) {
                monitor.requestSent = true;
                monitor.notifyAll();
            }
            TestLogger.logger.debug(
                    title + " port.sleepAsync(" + request1 + ", callbackHander1)  #1 .....submitted.");
            
            // Return from this thread immediately
        }
    }
    
    /**
     * Thread that verifies the request sent by a different thread was recieved by the service and
     * then verify the response.  Another thread will have previously sent the request. Note that
     * the sleep operation invoked by the other thread will have added the String it receives to 
     * a list and then suspend processing until a wakeup operation is issued by this thread.
     * @see {@link SendClientRequest}
     */
    class ReceiveClientResponse implements Runnable {
        private TestThreadMonitor monitor;
        ReceiveClientResponse(TestThreadMonitor monitor) {
            this.monitor = monitor;
        }
        public void run() {
            try {
                // Wait until the sender thread has sent the request
                synchronized(monitor) {
                    while (!monitor.requestSent) {
                        monitor.wait();
                    }
                }
                TestLogger.logger.debug("Receiver thread released by sender thread to start working");
                receiveResponse();
                TestLogger.logger.debug("Receiver thread done and about to exit");
            } catch (Exception e) {
                monitor.receiverException = e;
                TestLogger.logger.error("Receive thread caught exception", e);
            } catch (AssertionError e) {
                monitor.receiverException = new Exception(e);
                TestLogger.logger.error("Receive thread assertion failure", e);
            }
        }

        private void receiveResponse() throws Exception {
            String title = myClassName + " : ReceiveClientResponse : ";
            String request1 = monitor.request;
            
            AsyncService service = new AsyncService();
            AsyncPort port = getPort(service);

            // wait a bit to make sure that the server has the request;
            Thread.sleep(1000);
            
            // check the waiting request 
            TestLogger.logger.debug(title + " port.isAsleep(" + request1 + ") #1 being submitted....");
            String asleepWithCallback1 = port.isAsleep(request1);
            TestLogger.logger.debug(
                    title + " port.isAsleep(" + request1 + ") #1 = [" + asleepWithCallback1 + "]");
            assertEquals(request1, asleepWithCallback1);
            
            // wakeup the waiting request
            TestLogger.logger.debug(title + " port.wakeUp(request1) #1 being submitted....");
            String wake1 = port.wakeUp(request1);
            monitor.receiverThreadRequestReceivedTime = System.currentTimeMillis();
            assertEquals(request1, wake1);
            TestLogger.logger.debug(title + " port.wakeUp(" + request1 + ") #1 = [" + wake1 + "]");

            // wait a bit..
            Thread.sleep(2000);

            // check the Future
            Future<?> sr1 = monitor.futureResponse;
            CallbackHandler<SleepResponse> sleepCallbackHandler1 = monitor.callbackHandler;
            assertTrue("Response is not done!", sr1.isDone());

            // try to get the response
            try {
                SleepResponse sleepResp1 = sleepCallbackHandler1.get();
                if (sleepResp1 != null) {
                    TestLogger.logger.debug(title + " request [" + request1 +
                            "] #1:  sleepResponse [NOT NULL] from callback handler");
                    String result1 = sleepResp1.getMessage();
                    assertEquals(request1, result1);
                    TestLogger.logger.debug(
                            title + " request [" + request1 + "] #1:  result [" + result1 + "] ");
                }
                else {
                    TestLogger.logger.debug(title + " request [" + request1 +
                            "] #1:  sleepResponse [NULL] from callback handler");

                    // see what the Future says
                    TestLogger.logger.debug(
                            title + " request [" + request1 + "] #1:  ....check Future response...");
                    Object futureResult = sr1.get();
                    TestLogger.logger.debug(
                            title + " request [" + request1 + "] #1:  ....Future response [" +
                                    futureResult + "]...");
                }
            } catch (Exception exc) {
                TestLogger.logger.debug(title + " request [" + request1 + "] :  got exception [" +
                        exc.getClass().getName() + "]  [" + exc.getMessage() + "] ");
                monitor.receiverException = exc;
            }
        }
    }
}

