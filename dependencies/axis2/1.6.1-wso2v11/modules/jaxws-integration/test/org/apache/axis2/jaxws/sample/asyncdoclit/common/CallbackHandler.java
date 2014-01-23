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

package org.apache.axis2.jaxws.sample.asyncdoclit.common;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * Generic Async callback handler. The get method emulates Response.get by
 * throwning an exception if one is received. 
 */
public class CallbackHandler<T> implements AsyncHandler<T> {

    private T response = null;
    private Response<T> resp = null;
    
    private Exception exception = null;

    public void handleResponse(Response<T> response) {

        try {
            T res = (T) response.get();
            this.response = res;
            this.resp = response;
        } catch (Exception e) {
            this.exception = e;
        }
    }

    public Response<T> getResponse(){
        return this.resp;
    }
    
    public T get() throws Exception {

        if (exception != null)
            throw exception;
        return this.response;
    }

    /**
     * Auxiliary method used to wait for a monitor for a certain amount of time
     * before timing out
     * 
     * @param monitor
     */
    public void waitBlocking(Future<?> monitor) throws Exception {
        // wait for request to complete
        int sec = 20; //Constants.CLIENT_MAX_SLEEP_SEC;
        while (!monitor.isDone()) {
            Thread.sleep(1000);
            sec--;
            if (sec <= 0) break;
        }

        if (sec <= 0)
            throw new TimeoutException("Stopped waiting for Async response after "
                    + 20 /*Constants.CLIENT_MAX_SLEEP_SEC*/ + " sec");
    }   
    
}
