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

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

public class AsyncCallback<T> implements AsyncHandler<T> {

    private T value;
    private Throwable exception;
    
    public void handleResponse(Response<T> response) {
        try {
            value = response.get();
        } catch (Throwable t) {
            exception = t;
        }
    }
    
    public boolean hasError() {
        return (exception != null);
    }
    
    public Throwable getError() {
        return exception;
    }
    
    public T getValue() {
        return value;
    }
}
