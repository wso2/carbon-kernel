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

package org.apache.axis2.jaxws.addressing;

import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.jaxws.client.InterceptableClientTestCase;
import org.apache.axis2.jaxws.client.TestClientInvocationController;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.OperationDescription;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.Service;
import javax.xml.ws.WebFault;

/**
 * This suite of tests is for the Action annotation
 */
public class ProxyActionTests extends InterceptableClientTestCase {
    private static final String ns = "http://jaxws.axis2.apache.org/metadata/addressing/action";
    
    /*
     * Make sure WS-Addressing Default Action Pattern is used.
     */
    public void testNoActionAnnotation() throws Exception {
        Service svc = Service.create(new QName("http://test", "ProxyAddressingService"));
        ProxyAddressingService proxy = svc.getPort(ProxyAddressingService.class);
        assertNotNull(proxy);
        
        proxy.doSomething("12345");
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext request = ic.getRequestMessageContext();
        
        OperationDescription od = request.getOperationDescription();
        AxisOperation axisOperation = (AxisOperation) od.getAxisOperation();
        assertEquals("http://jaxws.axis2.apache.org/metadata/addressing/action/Service1/doSomethingRequest", axisOperation.getOutputAction());
        assertEquals("", axisOperation.getInputAction());
        assertEquals("http://jaxws.axis2.apache.org/metadata/addressing/action/Service1/doSomething/Fault/TestException", axisOperation.getFaultAction());
    }
    
    /*
     * Test the use of the Action annotation.
     */
    public void testActionAnnotation() throws Exception {
        Service svc = Service.create(new QName("http://test", "ProxyAddressingService"));
        ProxyAddressingServiceWithAction proxy = svc.getPort(ProxyAddressingServiceWithAction.class);
        assertNotNull(proxy);
        
        proxy.doSomething("12345");
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext request = ic.getRequestMessageContext();
        
        OperationDescription od = request.getOperationDescription();
        AxisOperation axisOperation = (AxisOperation) od.getAxisOperation();
        assertEquals("http://test/input", axisOperation.getOutputAction());
        //assertEquals("http://test/output", axisOperation.getInputAction()); //todo: uncomment and fix
        assertEquals("http://test/fault", axisOperation.getFaultAction());
    }
    
    @WebService(name="Service1", targetNamespace=ns)
    interface ProxyAddressingService {
        
        public String doSomething(String id) throws TestException;
        
    }
    
    @WebService(name="Service2", targetNamespace=ns)
    interface ProxyAddressingServiceWithAction {
        
        @Action(input="http://test/input", output="http://test/output",
                fault={ @FaultAction(className=TestException.class, value="http://test/fault") })    
        public String doSomething(String id) throws TestException;
        
    }
    
    @WebFault(name="TestException", targetNamespace=ns)
    class TestException extends Exception {
    }
}
