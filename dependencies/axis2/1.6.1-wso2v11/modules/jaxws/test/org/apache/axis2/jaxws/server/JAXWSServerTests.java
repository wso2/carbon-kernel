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

import junit.framework.TestCase;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.registry.InvocationListenerRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JAXWSServerTests extends TestCase {
    
    InvocationListenerFactory fac1 = new TestInvocationProcessorFactory1();
    InvocationListenerFactory fac2 = new TestInvocationProcessorFactory2();
    
    public void setUp() {
        InvocationListenerRegistry.addFactory(fac1);
        InvocationListenerRegistry.addFactory(fac2);
    }
    
    /**
     * This verifies that multiple InvocationProcessorFactories can be
     * registered with the FactoryRegistry.
     */
    public void testRegisterFactories() {
        Collection<InvocationListenerFactory> factories = InvocationListenerRegistry.getFactories();
        assertNotNull(factories);
        assertEquals(factories.size(), 2);
    }
    
    /**
     * This will verify that the JAXWSMessageReceiver is able to find and
     * store InvocationProcessorFactories on the EndpointInvocationContext.
     */
    public void testAddFactoriesToEIC() {
        EndpointInvocationContext eic = new EndpointInvocationContextImpl();
        JAXWSMessageReceiver receiver = new JAXWSMessageReceiver();
        receiver.addInvocationListenerFactories(eic);
        Collection<InvocationListenerFactory> factories = eic.getInvocationListenerFactories();
        assertNotNull(factories);
        assertEquals(factories.size(), 2);
    }

    /**
     * This will tests that registered InvocationListeners are properly called
     * by the JAX-WS server-side code. This test approximates a synchronous
     * message request
     */
    public void testSyncInvocationListener() {
        EndpointController controller = new EndpointController();
        EndpointInvocationContext eic = new EndpointInvocationContextImpl();
        MessageContext request = new MessageContext();
        MessageContext response = new MessageContext();
        eic.setRequestMessageContext(request);
        eic.setResponseMessageContext(response);
        JAXWSMessageReceiver receiver = new JAXWSMessageReceiver();
        receiver.addInvocationListenerFactories(eic);
        controller.requestReceived(eic);
        assertNotNull(request.getProperty("requestReceived"));
        assertTrue((Boolean) request.getProperty("requestReceived"));
        controller.responseReady(eic);
        assertNotNull(response.getProperty("responseReady"));
        assertTrue((Boolean) response.getProperty("responseReady"));
    }
    
    /**
     * This will tests that registered InvocationListeners are properly called
     * by the JAX-WS server-side code. This test approximates an asynchronous
     * message request
     */
    public void testAsyncInvocationListener() {
        EndpointController controller = new EndpointController();
        EndpointCallback callback = new EndpointCallback();
        EndpointInvocationContext eic = new EndpointInvocationContextImpl();
        MessageContext request = new MessageContext();
        MessageContext response = new MessageContext();
        eic.setRequestMessageContext(request);
        eic.setResponseMessageContext(response);
        JAXWSMessageReceiver receiver = new JAXWSMessageReceiver();
        receiver.addInvocationListenerFactories(eic);
        controller.requestReceived(eic);
        assertNotNull(request.getProperty("requestReceived"));
        assertTrue((Boolean) request.getProperty("requestReceived"));
        callback.responseReady(eic);
        assertNotNull(response.getProperty("responseReady"));
        assertTrue((Boolean) response.getProperty("responseReady"));
    }
    
    /**
     * This will test that exceptions are properly handed down to the registered
     * instances of InvocationListener objects.
     */
    public void testHandleException() {
        EndpointController controller = new EndpointController();
        EndpointCallback callback = new EndpointCallback();
        EndpointInvocationContext eic = new EndpointInvocationContextImpl();
        MessageContext request = new MessageContext();
        eic.setRequestMessageContext(request);
        eic.addInvocationListener(new TestInvocationListener());
        Exception e = new Exception();
        InvocationHelper.callListenersForException(e, eic);
        assertNotNull(request.getProperty(org.apache.axis2.jaxws.spi.Constants.MAPPED_EXCEPTION));
        
        // now test that in the case this happens on a response, we set values 
        // on the response message context
        MessageContext response = new MessageContext();
        eic.setResponseMessageContext(response);
        eic.addInvocationListener(new TestInvocationListener());
        e = new Exception();
        InvocationHelper.callListenersForException(e, eic);
        assertNotNull(response.getProperty(org.apache.axis2.jaxws.spi.Constants.MAPPED_EXCEPTION));
        
        // now test the InvocationHelper method that accepts a throwable and
        // MessageContext
        controller = new EndpointController();
        callback = new EndpointCallback();
        eic = new EndpointInvocationContextImpl();
        request = new MessageContext();
        eic.setRequestMessageContext(request);
        eic.addInvocationListener(new TestInvocationListener());
        e = new Exception();
        request.setProperty(org.apache.axis2.jaxws.spi.Constants.INVOCATION_LISTENER_LIST, 
                            eic.getInvocationListeners());
        InvocationHelper.callListenersForException(e, request);
        assertNotNull(request.getProperty(org.apache.axis2.jaxws.spi.Constants.MAPPED_EXCEPTION));
        
    }
    
    /**
     * This test will verify that the InvocationHelper.determineMappedException method is 
     * capable of correctly identifying the method to be thrown on the server-side.
     */
    public void testDetermineException() {
        
        // test the signature of determineMappedException that takes an EndpointInvocationContext
        EndpointController controller = new EndpointController();
        EndpointInvocationContext eic = new EndpointInvocationContextImpl();
        MessageContext request = new MessageContext();
        eic.setRequestMessageContext(request);
        eic.addInvocationListener(new TestInvocationListener());
        Throwable t = InvocationHelper.determineMappedException(new ArrayIndexOutOfBoundsException(), eic);
        assertNotNull(t);
        assertTrue(t.getClass().getName().equals(NullPointerException.class.getName()));
        
        // test the signature of determineMappedException that takes a MessageContext
        controller = new EndpointController();
        request = new MessageContext();
        eic.setRequestMessageContext(request);
        List<InvocationListener> invocationListeners = new ArrayList<InvocationListener>();
        invocationListeners.add(new TestInvocationListener());
        request.setProperty(org.apache.axis2.jaxws.spi.Constants.INVOCATION_LISTENER_LIST, 
                            invocationListeners);
        t = InvocationHelper.determineMappedException(new ArrayIndexOutOfBoundsException(), request);
        assertNotNull(t);
        assertTrue(t.getClass().getName().equals(NullPointerException.class.getName()));
    }
    
    static class TestInvocationProcessorFactory1 implements InvocationListenerFactory {
        public InvocationListener createInvocationListener(MessageContext context) {
            return new TestInvocationListener();
        }
    }
    
    static class TestInvocationProcessorFactory2 implements InvocationListenerFactory {
        public InvocationListener createInvocationListener(MessageContext context) {
            return new TestInvocationListener();
        }
    }
    
    
    static class TestInvocationListener implements InvocationListener {
        
        public void notify(InvocationListenerBean bean) {
            if(bean.getState().equals(InvocationListenerBean.State.REQUEST)) {
                bean.getEndpointInvocationContext().getRequestMessageContext().
                    setProperty("requestReceived", true);
            }
            else if (bean.getState().equals(InvocationListenerBean.State.RESPONSE)){
                bean.getEndpointInvocationContext().getResponseMessageContext().
                    setProperty("responseReady", true);
            }
        }
        
        public void notifyOnException(InvocationListenerBean bean) {
            if(bean.getState().equals(InvocationListenerBean.State.REQUEST)) {
                bean.getEndpointInvocationContext().getRequestMessageContext().
                    setProperty(org.apache.axis2.jaxws.spi.Constants.MAPPED_EXCEPTION, 
                                new NullPointerException());
            }
            else if (bean.getState().equals(InvocationListenerBean.State.RESPONSE)){
                bean.getEndpointInvocationContext().getResponseMessageContext().
                    setProperty(org.apache.axis2.jaxws.spi.Constants.MAPPED_EXCEPTION, 
                                new NullPointerException());
            }
        }
    }
}
