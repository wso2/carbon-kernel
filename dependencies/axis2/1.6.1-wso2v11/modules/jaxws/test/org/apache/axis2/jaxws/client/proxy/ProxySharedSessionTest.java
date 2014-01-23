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

package org.apache.axis2.jaxws.client.proxy;

import java.util.concurrent.Future;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.jaxws.addressing.SubmissionEndpointReference;
import org.apache.axis2.jaxws.addressing.SubmissionEndpointReferenceBuilder;
import org.apache.axis2.jaxws.client.InterceptableClientTestCase;
import org.apache.axis2.jaxws.client.TestClientInvocationController;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;

/**
 * Testing shared session property
 */
public class ProxySharedSessionTest extends InterceptableClientTestCase {
    private static final OMFactory OMF = OMAbstractFactory.getOMFactory();
    
    private W3CEndpointReference w3cEPR;
    private SubmissionEndpointReference subEPR;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
       
        W3CEndpointReferenceBuilder w3cBuilder = new W3CEndpointReferenceBuilder();
        w3cBuilder = w3cBuilder.address("http://somewhere.com/somehow");
        w3cBuilder = w3cBuilder.serviceName(new QName("http://test", "ProxySessionService"));
        w3cBuilder = w3cBuilder.endpointName(new QName("http://test", "TestPort"));
        w3cEPR = w3cBuilder.build();
        
        SubmissionEndpointReferenceBuilder subBuilder = new SubmissionEndpointReferenceBuilder();
        subBuilder = subBuilder.address("http://somewhere.com/somehow");
        subBuilder = subBuilder.serviceName(new QName("http://test", "ProxySessionService"));
        subBuilder = subBuilder.endpointName(new QName("http://test", "TestPort"));
        subEPR = subBuilder.build();
    }


    public void testCookieCopiedToServiceContext() {
        Service svc = Service.create(new QName("http://test", "ProxySessionService"));
        ProxySessionService proxy = svc.getPort(ProxySessionService.class);
        assertNotNull(proxy);
        
        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        p.getRequestContext().put(HTTPConstants.COOKIE_STRING, "MyCookie");
        
        proxy.doSomething("12345");
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        assertNotNull("Invocation of the proxy object should have caused COOKIE_STRING keyed property to be on the ServiceContext", ic.getServiceClient().getServiceContext().getProperty(HTTPConstants.HEADER_COOKIE));
        
    }

    
    public void testCookieCopiedToServiceContextAsync() {
        Service svc = Service.create(new QName("http://test", "ProxySessionService"));
        ProxySessionService proxy = svc.getPort(ProxySessionService.class);
        assertNotNull(proxy);
        
        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        p.getRequestContext().put(HTTPConstants.COOKIE_STRING, "MyCookie");
        
        proxy.doSomethingAsync("12345");
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        testController.invoke(ic);

        assertNotNull("Invocation of the proxy object should have caused COOKIE_STRING keyed property to be on the ServiceContext", ic.getServiceClient().getServiceContext().getProperty(HTTPConstants.HEADER_COOKIE));
        
    }
    
    public void testCookieCopiedToServiceContextAsyncCallback() {
        Service svc = Service.create(new QName("http://test", "ProxySessionService"));
        ProxySessionService proxy = svc.getPort(ProxySessionService.class);
        assertNotNull(proxy);
        
        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        p.getRequestContext().put(HTTPConstants.COOKIE_STRING, "MyCookie");
        
        proxy.doSomethingAsync("12345", new DummyAsyncHandler());
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        testController.invoke(ic);

        assertNotNull("Invocation of the proxy object should have caused COOKIE_STRING keyed property to be on the ServiceContext", ic.getServiceClient().getServiceContext().getProperty(HTTPConstants.HEADER_COOKIE));
        
    }
    

    
    @WebService()
    public interface ProxySessionService {
    
        public String doSomething(String id);

        public Future<?> doSomethingAsync(String id, AsyncHandler<DummyResponse> asyncHandler);
        
        public Response<DummyResponse> doSomethingAsync(String id);
        
    }
    
    public class DummyResponse {}
    
    public class DummyAsyncHandler implements AsyncHandler<DummyResponse> {

		public void handleResponse(Response<DummyResponse> arg0) {
			
		}

    }
    
}
