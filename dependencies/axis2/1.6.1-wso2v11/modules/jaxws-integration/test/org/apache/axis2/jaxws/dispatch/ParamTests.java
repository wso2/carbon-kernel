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
import org.apache.axis2.jaxws.framework.AbstractTestCase;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;

/**
 * A suite for some tests for specific behavior in the Dispatch with 
 * null and invalid params.
 */
public class ParamTests extends AbstractTestCase {
    
    public static Test suite() {
        return getTestSetup(new TestSuite(ParamTests.class));
    }
    
    public void testNullSoapParamWithMessageMode() {
        QName serviceName = new QName("http://test", "MyService");
        QName portName = new QName("http://test", "MyPort");
        
        Service svc = Service.create(serviceName);
        svc.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost");
        
        Dispatch<Source> dispatch = svc.createDispatch(portName, 
                Source.class, Mode.PAYLOAD);
        
        boolean handled = false;
        try {
            dispatch.invoke(null);    
        }
        catch (WebServiceException wse) {
            handled = true;
        }        
        
        assertTrue("A WebServiceException should be thrown for this null param", handled);
    }
    
    public void testNullHttpParamWithPayloadMode() {
        // fill in this test when we add XML/HTTP Binding support
    }
    
    public void testNullHttpParamWithMessageMode() {
        // fill in this test when we add XML/HTTP Binding support        
    }
}
