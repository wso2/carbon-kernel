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

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.apache.axis2.jaxws.addressing.SubmissionEndpointReferenceBuilder;
import org.apache.axis2.jaxws.client.InterceptableClientTestCase;
import org.apache.axis2.jaxws.client.TestClientInvocationController;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.transport.http.HTTPConstants;


public class DispatchSharedSessionTest extends InterceptableClientTestCase {
    
    private W3CEndpointReference w3cEPR;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        W3CEndpointReferenceBuilder w3cBuilder = new W3CEndpointReferenceBuilder();
        w3cBuilder = w3cBuilder.address("http://somewhere.com/somehow");
        w3cBuilder = w3cBuilder.serviceName(new QName("http://test", "TestService"));
        w3cBuilder = w3cBuilder.endpointName(new QName("http://test", "TestPort"));
        w3cEPR = w3cBuilder.build();
        
        SubmissionEndpointReferenceBuilder subBuilder = new SubmissionEndpointReferenceBuilder();
        subBuilder = subBuilder.address("http://somewhere.com/somehow");
        subBuilder = subBuilder.serviceName(new QName("http://test", "TestService"));
        subBuilder = subBuilder.endpointName(new QName("http://test", "TestPort"));
        subBuilder.build();
    }

    public void testSharedSessionDispatch() {
        Service svc = Service.create(new QName("http://test", "TestService"));
        svc.addPort(new QName("http://test", "TestPort"), SOAPBinding.SOAP11HTTP_BINDING, "http://localhost");
        Dispatch<Source> d = svc.createDispatch(w3cEPR, Source.class, Service.Mode.PAYLOAD);
        
        d.getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        d.getRequestContext().put(HTTPConstants.COOKIE_STRING, "MyCookie");
        
        d.invoke(null);
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        testController.invoke(ic);

        assertNotNull("Invocation of the proxy object should have caused COOKIE_STRING keyed property to be on the ServiceContext", ic.getServiceClient().getServiceContext().getProperty(HTTPConstants.HEADER_COOKIE));
        
    }
    
    public void testSharedSessionDispatchAsync() {
        Service svc = Service.create(new QName("http://test", "TestService"));
        svc.addPort(new QName("http://test", "TestPort"), SOAPBinding.SOAP11HTTP_BINDING, "http://localhost");
        Dispatch<Source> d = svc.createDispatch(w3cEPR, Source.class, Service.Mode.PAYLOAD);
        
        d.getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        d.getRequestContext().put(HTTPConstants.COOKIE_STRING, "MyCookie");
        
        d.invokeAsync(null);
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        testController.invoke(ic);

        assertNotNull("Invocation of the proxy object should have caused COOKIE_STRING keyed property to be on the ServiceContext", ic.getServiceClient().getServiceContext().getProperty(HTTPConstants.HEADER_COOKIE));
        
    }
    
    public void testSharedSessionDispatchAsyncCallback() {
        Service svc = Service.create(new QName("http://test", "TestService"));
        svc.addPort(new QName("http://test", "TestPort"), SOAPBinding.SOAP11HTTP_BINDING, "http://localhost");
        Dispatch<Source> d = svc.createDispatch(w3cEPR, Source.class, Service.Mode.PAYLOAD);
        
        d.getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        d.getRequestContext().put(HTTPConstants.COOKIE_STRING, "MyCookie");
        
        d.invokeAsync(null, new DummyAsyncHandler());
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        testController.invoke(ic);

        assertNotNull("Invocation of the proxy object should have caused COOKIE_STRING keyed property to be on the ServiceContext", ic.getServiceClient().getServiceContext().getProperty(HTTPConstants.HEADER_COOKIE));
        
    }
    
    public class DummyResponse {}
    
    public class DummyAsyncHandler implements AsyncHandler<Source> {

		public void handleResponse(Response<Source> arg0) {
			
		}

    }
	
}
