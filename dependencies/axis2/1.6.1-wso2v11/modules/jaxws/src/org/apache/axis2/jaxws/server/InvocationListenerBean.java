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

package org.apache.axis2.jaxws.server;

/**
 * An instance of this class will be passed to the InvocationListener 
 * instances in both the request and response flow. This bean will 
 * hold data that allows the InvocationListener to determine the message
 * flow (request vs. response) as well as other context information.
 *
 */
public class InvocationListenerBean {
    
    private Throwable throwable;
    
    public InvocationListenerBean() {
        
    }
    
    public InvocationListenerBean(EndpointInvocationContext eic, State state) {
        this.eic = eic;
        this.state = state;
    }
    
    public static enum State {
        REQUEST,
        RESPONSE,
        EXCEPTION
    }
    
    private EndpointInvocationContext eic;
    
    private State state;

    public EndpointInvocationContext getEndpointInvocationContext() {
        return eic;
    }

    public void setEndpointInvocationContext(EndpointInvocationContext eic) {
        this.eic = eic;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
    
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
    
    public Throwable getThrowable() {
        return throwable;
    }
    
}
