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

package org.apache.axis2.jaxws.sample.parallelasync.server;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.sample.parallelasync.common.Constants;
import org.test.parallelasync.AnotherResponse;
import org.test.parallelasync.CustomAsyncResponse;
import org.test.parallelasync.InvokeAsyncResponse;
import org.test.parallelasync.PingResponse;
import org.test.parallelasync.SleepResponse;

import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;
import java.util.Hashtable;
import java.util.concurrent.Future;

/**
 * Async endpoint used for Async client side tests. Clients will invokeAsync
 * sleep method to force the server to block until wakeUp is called. The client
 * can call isAsleep to verify that sleep has been called by the async thread.
 */
/*
@WebService(
        serviceName="AsyncService",
        portName="AsyncPort",
        targetNamespace = "http://org/test/parallelasync",
        endpointInterface = "org.test.parallelasync.AsyncPort",
        wsdlLocation="WEB-INF/wsdl/async_doclitwr.wsdl")
        */
@WebService(serviceName="AsyncService",
			endpointInterface="org.apache.axis2.jaxws.sample.parallelasync.server.AsyncPort")
public class DocLitWrappedPortImpl implements AsyncPort {

    private static final boolean DEBUG = false;

    // in order to allow multiple sleeping requests to be held at the same time
    // use a table where 
    //       the key is the request string
    //       the value is the object used to block on
    //
    private static Hashtable sleepers = new Hashtable();

    // intended to flag the need to cancel current requests being held (ie, sleeping)
    // does not stop new requests
    // not settable yet
    // need to determine when to reset it when dealing with multiple operations
    // currently reset when the sleepers table doesn't have any more requests
    private static boolean doCancell = false;

    // strings used for logging
    private String myClassName = "DocLitWrappedPortImpl.";



    /**
     * This operation takes the request and holds it in the web service until
     * <UL>
     * <LI>the client asks for it via wakeUp()
     * <LI>the operation times out
     * <LI>the operation is interrupted
     * </UL>
     * 
     * @param request The request identifier
     */
    public void sleep(Holder<String> request) {

        boolean cancelRequested = false;

        String key = new String(request.value);
        String msg = request.value;

        String title = myClassName+"sleep("+msg+"): ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "] ";
        //if (DEBUG)
        //{
        //    System.out.println(title + tid + "Enter");
        //}

        if (sleepers.get(key) != null)
        {
            // already holding this request id
            return;
        }

        Thread myThread = Thread.currentThread();
        long threadID = myThread.getId();

        // add this request to our list
        sleepers.put(key,myThread);

        // set the timeout value
        long sec = Constants.SERVER_SLEEP_SEC;

        try {

            //if (DEBUG)
            //    System.out.println(title + "Starting to sleep on "
            //            + " threadID ["+ threadID + "]" );

            // hold this request until
            //  - the wait time expires
            //  - the client explicits asks for this request via wakeUp()
            //  - the wait is interrupted
            //  - a cancel occurs

            while (sec > 0 && !doCancell) {
                if (DEBUG)
                    TestLogger.logger.debug(title + "Sleeping on "
                            + " threadID [" + threadID + "]"
                            + " timeLeft=" + sec);
                sec--;

                //msg.wait(500);
                myThread.sleep(500);
            }

        } 
        catch (InterruptedException e) {

            TestLogger.logger.debug(title + "Sleep interrupted on "
                    + " threadID [" + threadID + "]"
                    + " timeLeft=[" + sec + "]");

        } 
        finally {

            if (DEBUG)
                TestLogger.logger.debug(title + "final processing for "
                        + " threadID [" + threadID + "]");

            // remove this request from the list
            sleepers.remove(key);

            // for now, reset the cancellation flag when the list of 
            // waiting requests go to zero
            if (sleepers.isEmpty())
            {
                doCancell = false;
            }
        }

    }

    /**
     * Checks the specified request to determine whether
     * it is still being held by the web service.
     * 
     * @param request The request identifier to check
     * @return The String being used as a wait object if the
     *         request is still being held by the server
     *         or NULL if the request is not held by the server
     */
    public String isAsleep(String request) {

        String title = myClassName+"isAsleep("+request+"): ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "] ";

        if (sleepers.isEmpty())
        {
            if (DEBUG)
                TestLogger.logger.debug(title + tid + " Not sleeping");
            return null;
        }

        Thread value = (Thread) sleepers.get(request);

        if (value == null)
        {
            if (DEBUG)
                TestLogger.logger.debug(title + tid + " Not sleeping");

            return null;
        }

        if (DEBUG)
            TestLogger.logger.debug(title + tid + " sleeping on [" + request + "]");

        return request;
    }



    public String wakeUp(String request) {

        String title = myClassName+"wakeUp(): ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";

        if (sleepers.isEmpty())
        {
            if (DEBUG)
                TestLogger.logger.debug(title + tid + " No one to wake up");

            return null;
        }

        Thread value = (Thread) sleepers.get(request);

        if (value == null)
        {
            if (DEBUG)
                TestLogger.logger.debug(title + tid + " Thread not available. No one to wake up.");

            return null;
        }

        if (DEBUG)
            TestLogger.logger.debug(title + tid + " Interrupting "
                    + " threadID [" + value.getId() + "]");

        // interrupt the sleeper
        try
        {
            value.interrupt();
        }
        catch (Exception e)
        {
            if (DEBUG)
                TestLogger.logger.debug(title + tid + " Interrupting "
                        + " threadID [" + value.getId() + "]  got Exception [" +
                        e.getClass().getName() + "]   [" + e.getMessage() + "]");
        }

        return request;
    }


    /**
     * client side tests for remapping operation names, on the server side all
     * we need to do is roundtrip the message
     */

    public String invokeAsync(String request) {
        String title = myClassName+"invokeAsync("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return request;
    }

    public String customAsync(String request) {
        String title = myClassName+"customeAsync("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return request + "from customAsync method";
    }

    public String another(String request) {
        String title = myClassName+"another("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return request;
    }

    public String ping(String request) {
        String title = myClassName+"ping("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return request;
    }


    public String remapped(String request) {
        // TODO Auto-generated method stub
        String title = myClassName+"remapped("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return request;
    }


    // NOT USED:
    
    public String anotherAsync(String request) {
        // TODO Auto-generated method stub
        String title = myClassName+"anotherAsync("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return null;
    }

    public Future<?> anotherAsyncAsync(String request, AsyncHandler<AnotherResponse> asyncHandler) {
        // TODO Auto-generated method stub
        String title = myClassName+" Future<?> anotherAsyncAsync("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return null;
    }

    public Response<AnotherResponse> anotherAsyncAsync(String request) {
        // TODO Auto-generated method stub
        String title = myClassName+" Response<AnotherResponse> anotherAsyncAsync("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return null;
    }

    public Future<?> invokeAsyncAsync(String request, AsyncHandler<InvokeAsyncResponse> asyncHandler) {
        // TODO Auto-generated method stub
        String title = myClassName+" Future<?> invokeAsyncAsync("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return null;
    }

    public Response<InvokeAsyncResponse> invokeAsyncAsync(String request) {
        // TODO Auto-generated method stub
        String title = myClassName+" Response<InvokeAsyncResponse> invokeAsyncAsync("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return null;
    }

    public Future<?> pingAsync(String request, AsyncHandler<PingResponse> asyncHandler) {
        // TODO Auto-generated method stub
        String title = myClassName+" Future<?> pingAsync("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return null;
    }

    public Response<PingResponse> pingAsync(String request) {
        // TODO Auto-generated method stub
        String title = myClassName+" Response<PingResponse> pingAsync("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return null;
    }

    public Future<?> remappedAsync(String request, AsyncHandler<CustomAsyncResponse> asyncHandler) {
        // TODO Auto-generated method stub
        String title = myClassName+" Future<?> remappedAsync("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return null;
    }

    public Response<CustomAsyncResponse> remappedAsync(String request) {
        // TODO Auto-generated method stub
        String title = myClassName+" Response<CustomAsyncResponse> remappedAsync("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return null;
    }

    public Future<?> sleepAsync(String request, AsyncHandler<SleepResponse> asyncHandler) {
        // TODO Auto-generated method stub
        String title = myClassName+" Future<?> sleepAsync("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return null;
    }

    public Response<SleepResponse> sleepAsync(String request) {
        // TODO Auto-generated method stub
        String title = myClassName+" Response<SleepResponse> sleepAsync("+request+") : ";
        String tid = " threadID ["+ Thread.currentThread().getId() + "]";
        if (DEBUG)
            TestLogger.logger.debug(title + "Enter" + tid);

        return null;
    }
    
}
