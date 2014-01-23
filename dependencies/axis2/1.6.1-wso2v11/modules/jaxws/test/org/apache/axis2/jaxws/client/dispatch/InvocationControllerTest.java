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

package org.apache.axis2.jaxws.client.dispatch;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.controller.InvocationController;
import org.apache.axis2.jaxws.core.controller.InvocationControllerFactory;
import org.apache.axis2.jaxws.core.controller.impl.AxisInvocationController;
import org.apache.axis2.jaxws.core.controller.impl.InvocationControllerFactoryImpl;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.util.concurrent.Future;

public class InvocationControllerTest extends TestCase {

    private QName svcQname = new QName("http://test", "TestService");
    private QName portQname = new QName("http://test", "TestPort");
    
    public void testDefaultInvocationController() {
        Service svc = Service.create(svcQname);
        svc.addPort(portQname, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost");
        Dispatch d = svc.createDispatch(portQname, Source.class, Service.Mode.PAYLOAD);
        
        BaseDispatch bd = (BaseDispatch) d;
        
        assertTrue("An InvocationController instance was not created", bd.ic != null);
        assertTrue("The default InvocationController type was incorrect.", 
            AxisInvocationController.class.isAssignableFrom(bd.ic.getClass()));
    }
    
    public void testPluggableInvocationController() {
        FactoryRegistry.setFactory(InvocationControllerFactory.class, new TestInvocationControllerFactory());
        
        Service svc = Service.create(svcQname);
        svc.addPort(portQname, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost");
        Dispatch d = svc.createDispatch(portQname, Source.class, Service.Mode.PAYLOAD);
        
        BaseDispatch bd = (BaseDispatch) d;
        
        // Set it back to the default so we don't break other tests.
        FactoryRegistry.setFactory(InvocationControllerFactory.class, new InvocationControllerFactoryImpl());
        
        assertTrue("An InvocationController instance was not created", bd.ic != null);
        assertTrue("The default InvocationController type was incorrect.", 
            TestInvocationController.class.isAssignableFrom(bd.ic.getClass()));
    }
}

class TestInvocationControllerFactory implements InvocationControllerFactory {
    public InvocationController getInvocationController() {
        return new TestInvocationController(); 
    }
}

class TestInvocationController implements InvocationController {

    public InvocationContext invoke(InvocationContext ic) {
        return null;
    }

    public Future<?> invokeAsync(InvocationContext ic, AsyncHandler asyncHandler) {
        return null;
    }

    public Response invokeAsync(InvocationContext ic) {
        return null;
    }

    public void invokeOneWay(InvocationContext ic) throws Exception {}    
}
