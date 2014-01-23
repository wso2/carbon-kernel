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
 * Tests for Asynchrony in JAX-WS. Most of the simple invokeAsync/async
 * exceptions have been covered under jaxws.dispatch and jaxws.proxy test suites
 * 
 * ExecutionException tests are covered in jaxws.dispatch and jaxws.proxy
 */
public class ParallelAsyncTests extends AbstractTestCase {

    private static final String DOCLITWR_ASYNC_ENDPOINT =
        "http://localhost:6060/axis2/services/AsyncService.DocLitWrappedPortImplPort";

    // used for logging
    private String myClassName = "ParallelAsyncTests";

    public static Test suite() {
        return getTestSetup(new TestSuite(ParallelAsyncTests.class));
    }
    	
    public void testNOOP () {}

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

            if (sleepResp != null)
            {
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
     * @testStrategy Test for ordering an executor to shutdownNow while there
     *               is a request being processed.  Uses the default executor.
     *               
     */
    public void testService_ExecutorShutdownNow() throws Exception {
        final String MESSAGE = "testExecutorShutdownNow";

        String title = myClassName + " : " + getName() + " : ";

        AsyncService service = getService(null);
        AsyncPort port = getPort(service);

		// get the default executor and check to make sure it is an executor service
        ExecutorService ex = null;
        Executor executor = service.getExecutor();
        if ((executor != null) && (executor instanceof ExecutorService))
        {
            ex = (ExecutorService) executor;
        }
        else
        {
            TestLogger.logger.debug(title + " No executor service available. Nothing to test.");
            return;
        }


        // submit a request to the server that will wait until we ask for it
		CallbackHandler<SleepResponse> sleepCallbackHandler1 = new CallbackHandler<SleepResponse>();

        String request1 = "sleepAsync_with_Callback_1";

        TestLogger.logger.debug(title + " port.sleepAsync(" + request1 +
                ", callbackHander1)  #1 being submitted....");
		Future<?> sr1 = port.sleepAsync(request1, sleepCallbackHandler1);
        TestLogger.logger.debug(
                title + " port.sleepAsync(" + request1 + ", callbackHander1)  #1 .....submitted.");

        // wait a bit to make sure that the server has the request
        Thread.sleep(1000);

		// tell the executor to shutdown immediately, which 
        // attempts to stop all actively executing tasks via Thread.interrupt()
        // and should prevent new tasks from being submitted
        TestLogger.logger
                .debug(title + " shutting down executor [" + ex.getClass().getName() + "]");
        ex.shutdownNow();

        // check the waiting request 
        TestLogger.logger.debug(title + " port.isAsleep(" + request1 + ") #1 being submitted....");
        String asleepWithCallback1 = port.isAsleep(request1);
        TestLogger.logger.debug(
                title + " port.isAsleep(" + request1 + ") #1 = [" + asleepWithCallback1 + "]");

        // wakeup the waiting request
        TestLogger.logger.debug(title + " port.wakeUp(request1) #1 being submitted....");
        String wake1 = port.wakeUp(request1);
        TestLogger.logger.debug(title + " port.wakeUp(" + request1 + ") #1 = [" + wake1 + "]");

        // wait a bit..
        Thread.sleep(2000);

        // check the Future
        if (sr1.isDone())
        {
            TestLogger.logger.debug(title + " sr1.isDone[TRUE] ");
        }

        // try to get the response
        boolean gotException = false;
        try {

            SleepResponse sleepResp1 = sleepCallbackHandler1.get();

            if (sleepResp1 != null)
            {
                TestLogger.logger.debug(title + " request [" + request1 +
                        "] #1:  sleepResponse [NOT NULL] from callback handler");
                String result1 = sleepResp1.getMessage();
                TestLogger.logger.debug(
                        title + " request [" + request1 + "] #1:  result [" + result1 + "] ");
            }
            else
            {
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
            gotException = true;
        }

        assertTrue("Did not receive an exception from trying to access the response when the executor service is shutdown.",gotException);
    }


    /**
     * @testStrategy Test for ordering an executor to shutdownNow while there
     *               is a request being processed.  Uses an application executor
     *               service.
     */
    public void testService_ExecutorShutdownNow_2() throws Exception {
        final String MESSAGE = "testExecutorShutdownNow_2";

        String title = myClassName + " : " + getName() + " : ";

        AsyncService service = getService(null);
        AsyncPort port = getPort(service);

		// get the default executor and check to make sure it is an executor service
		ExecutorService ex = Executors.newSingleThreadExecutor();
		service.setExecutor(ex);


        // submit a request to the server that will wait until we ask for it
		CallbackHandler<SleepResponse> sleepCallbackHandler1 = new CallbackHandler<SleepResponse>();

        String request1 = "sleepAsync_with_Callback_1";

        TestLogger.logger.debug(title + " port.sleepAsync(" + request1 +
                ", callbackHander1)  #1 being submitted....");
		Future<?> sr1 = port.sleepAsync(request1, sleepCallbackHandler1);
        TestLogger.logger.debug(
                title + " port.sleepAsync(" + request1 + ", callbackHander1)  #1 .....submitted.");

        // wait a bit to make sure that the server has the request
        Thread.sleep(1000);

		// tell the executor to shutdown immediately, which 
        // attempts to stop all actively executing tasks via Thread.interrupt()
        // and should prevent new tasks from being submitted
        TestLogger.logger
                .debug(title + " shutting down executor [" + ex.getClass().getName() + "]");
        ex.shutdownNow();

        // check the waiting request 
        TestLogger.logger.debug(title + " port.isAsleep(" + request1 + ") #1 being submitted....");
        String asleepWithCallback1 = port.isAsleep(request1);
        TestLogger.logger.debug(
                title + " port.isAsleep(" + request1 + ") #1 = [" + asleepWithCallback1 + "]");

        // wakeup the waiting request
        TestLogger.logger.debug(title + " port.wakeUp(request1) #1 being submitted....");
        String wake1 = port.wakeUp(request1);
        TestLogger.logger.debug(title + " port.wakeUp(" + request1 + ") #1 = [" + wake1 + "]");

        // wait a bit..
        Thread.sleep(2000);

        // check the Future
        if (sr1.isDone())
        {
            TestLogger.logger.debug(title + " sr1.isDone[TRUE] ");
        }

        // try to get the response
        boolean gotException = false;
        try {

            SleepResponse sleepResp1 = sleepCallbackHandler1.get();

            if (sleepResp1 != null)
            {
                TestLogger.logger.debug(title + " request [" + request1 +
                        "] #1:  sleepResponse [NOT NULL] from callback handler");
                String result1 = sleepResp1.getMessage();
                TestLogger.logger.debug(
                        title + " request [" + request1 + "] #1:  result [" + result1 + "] ");
            }
            else
            {
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
            gotException = true;
        }

        assertTrue("Did not receive an exception from trying to access the response when the executor service is shutdown.",gotException);
    }

    /**
     * @testStrategy Test for ordering an executor to shutdownNow before there
     *               is a request.  Uses the default executor.
     *               
     */
    public void testService_ExecutorShutdownNow_3() throws Exception {
        final String MESSAGE = "testExecutorShutdownNow_3";

        String title = myClassName + " : " + getName() + " : ";

        AsyncService service = getService(null);
        AsyncPort port = getPort(service);

		// get the default executor and check to make sure it is an executor service
        ExecutorService ex = null;
        Executor executor = service.getExecutor();
        if ((executor != null) && (executor instanceof ExecutorService))
        {
            ex = (ExecutorService) executor;

            // tell the executor to shutdown immediately, which 
            // attempts to stop all actively executing tasks via Thread.interrupt()
            // and should prevent new tasks from being submitted
            TestLogger.logger
                    .debug(title + " shutting down executor [" + ex.getClass().getName() + "]");
            ex.shutdownNow();
        }
        else
        {
            TestLogger.logger.debug(title + " No executor service available. Nothing to test.");
            return;
        }


        boolean gotRequestException = false;

        String request1 = "sleepAsync_with_Callback_1";
        CallbackHandler<SleepResponse> sleepCallbackHandler1 = new CallbackHandler<SleepResponse>();
        Future<?> sr1 = null;

        try
        {
            // submit a request to the server that will wait until we ask for it
            TestLogger.logger.debug(title + " port.sleepAsync(" + request1 +
                    ", callbackHander1)  #1 being submitted....");
            sr1 = port.sleepAsync(request1, sleepCallbackHandler1);
            TestLogger.logger.debug(title + " port.sleepAsync(" + request1 +
                    ", callbackHander1)  #1 .....submitted.");
        }
        catch (Exception exc)
        {
            TestLogger.logger.debug(title + " request [" + request1 + "] :  got exception [" +
                    exc.getClass().getName() + "]  [" + exc.getMessage() + "] ");
            gotRequestException = true;
        }

        // if the request went through, continue processing to see if the response is stopped
        // this makes sure that the server doesn't keep the request forever
        boolean gotResponseException = false;

        if (!gotRequestException)
        {
            // wakeup the waiting request
            TestLogger.logger.debug(title + " port.wakeUp(request1) #1 being submitted....");
            String wake1 = port.wakeUp(request1);
            TestLogger.logger.debug(title + " port.wakeUp(" + request1 + ") #1 = [" + wake1 + "]");

            // try to get the response
            try {

                SleepResponse sleepResp1 = sleepCallbackHandler1.get();

                if (sleepResp1 != null)
                {
                    TestLogger.logger.debug(title + " request [" + request1 +
                            "] #1:  sleepResponse [NOT NULL] from callback handler");
                    String result1 = sleepResp1.getMessage();
                    TestLogger.logger.debug(
                            title + " request [" + request1 + "] #1:  result [" + result1 + "] ");
                }
                else
                {
                    TestLogger.logger.debug(title + " request [" + request1 +
                            "] #1:  sleepResponse [NULL] from callback handler");

                    // see what the Future says
                    TestLogger.logger.debug(title + " request [" + request1 +
                            "] #1:  ....check Future response...");
                    Object futureResult = sr1.get();
                    TestLogger.logger.debug(title + " request [" + request1 +
                            "] #1:  ....Future response [" + futureResult + "]...");
                }

            } catch (Exception exc) {

                TestLogger.logger.debug(title + " request [" + request1 + "] :  got exception [" +
                        exc.getClass().getName() + "]  [" + exc.getMessage() + "] ");
                gotResponseException = true;
            }
        }

        assertTrue("Did not receive an exception from trying to submit the request when the executor service is shutdown.",gotRequestException);

        //assertTrue("Did not receive an exception from trying to access the response when the executor service is shutdown.",gotResponseException);
    }




    /**
     * Auxiliary method used for doing isAsleep checks. Will perform isAsleep
     * up to a MAX_ISASLEEP_CHECK number of checks. Will sleep for
     * SLEEP_ISASLEEP_SEC seconds in between requests. If reaches maximum number
     * fo retries then will fail the test
     */
    private boolean isAsleepCheck(String MESSAGE, AsyncPort port) {
        boolean asleep = false;
        int check = 30;
        String msg = null;
        do {
            msg = port.isAsleep(MESSAGE);
            asleep = (msg != null);

            // fail the test if we ran out of checks
            if ((check--) == 0)
                fail("Serve did not receive sleep after several retries");

            // sleep for a bit
            try {
                Thread.sleep(30);
            } 
            catch (InterruptedException e) {
            }

        } while (!asleep);

        if (asleep) {
            assertTrue("Sleeping on an incorrect message", MESSAGE.equals(msg));
        }

        return true;
    }
    

    private AsyncService getService(Executor ex) {
        AsyncService service = new AsyncService();

        if (ex!= null)
            service.setExecutor(ex);
        
        if (service.getExecutor() == null)
        {
            TestLogger.logger.debug(myClassName + " : getService() : executor is null");
        }
        else
        {
            TestLogger.logger.debug(myClassName + " : getService() : executor is available ");
        }

        return service;
    }


    private AsyncPort getPort(AsyncService service) {

        AsyncPort port = service.getAsyncPort();
        assertNotNull("Port is null", port);

        Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                DOCLITWR_ASYNC_ENDPOINT);
        
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
        while (!monitor.isDone()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
}
