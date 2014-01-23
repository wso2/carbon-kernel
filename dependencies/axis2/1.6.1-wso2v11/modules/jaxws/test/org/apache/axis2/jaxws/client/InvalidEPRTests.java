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
package org.apache.axis2.jaxws.client;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import junit.framework.TestCase;

/**
 * Test for the correct exceptions if an invalid EPR is specified.
 */
public class InvalidEPRTests extends TestCase {

    private QName serviceName = new QName("http://ws.apache.org/axis2", "StringProviderService");
    private QName portName = new QName("http://ws.apache.org/axis2", "SimpleProviderServiceSOAP11port0");

    private String CANNOT_INFER_EXCEPTION = "The system cannot infer the transport information from the";
    
    /**
     * Validate that we get a reaonsably desriptive exception if the TransportOut
     * can't be found for a bad EPR format.
     */
    public void testInvalidEPRFormat() {
        String endpointUrl = "Invalid_EPR jms:/queue?destination=jms/SwapQueue&connectionFactory=jms/SwapQCF&targetService=SwapJMSBean&initialContextFactory=com.ibm.websphere.naming.WsnInitialContextFactory&jndiProviderURL=corbaloc:iiop:sat.hursley.ibm.com:9810,iiop:sat.hursley.ibm.com:9811";
        Dispatch<String> dispatch = getDispatch(endpointUrl);
        
        String request = "<invoke>hello world</invoke>";
        try {
            String response = dispatch.invoke(request);
            fail("Should have caught exception for invalid EPR " + endpointUrl);
        } catch (WebServiceException ex) {
            assertTrue(ex.toString().contains(CANNOT_INFER_EXCEPTION));
        }
    }
    private Dispatch<String> getDispatch(String endpointUrl) {
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, endpointUrl);
        
        Dispatch<String> dispatch = svc
                .createDispatch(portName, String.class, Service.Mode.PAYLOAD);
        
        // Force soap action because we are passing junk over the wire
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,"http://stringprovider.sample.test.org/echoString");
        
        return dispatch;
    }
}
