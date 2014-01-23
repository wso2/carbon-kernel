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

package org.apache.axis2.jaxws.sample;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.sample.dlwmin.sei.Greeter;
import org.apache.axis2.jaxws.sample.dlwmin.sei.TestException;
import org.apache.axis2.jaxws.sample.dlwmin.sei.TestException2;
import org.apache.axis2.jaxws.sample.dlwmin.sei.TestException3;
import org.apache.axis2.jaxws.sample.dlwmin.types.TestBean;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

/**
 * 
 * Tests to verify Document/Literal Wrapped Minimal Scenarios
 * Document/Literal Wrapped is a JAX-WS style.
 * "Minimal" indicates that no wrapper beans are associated with the JAX-WS method.
 * In most enterprise scenarios, wrapper beans are packaged with the JAX-WS application.
 */
public class DLWMinTests extends AbstractTestCase {

    private static final String NAMESPACE = "http://apache.org/axis2/jaxws/sample/dlwmin";
    private static final QName QNAME_SERVICE = new QName(
            NAMESPACE, "GreeterService");
    private static final QName QNAME_PORT = new QName(
            NAMESPACE, "GreeterPort");
    private static final String URL_ENDPOINT = "http://localhost:6060/axis2/services/GreeterService.GreeterImplPort";
	
    public static Test suite() {
        return getTestSetup(new TestSuite(DLWMinTests.class));
    }

    private Greeter getProxy(String action) {
        Service service = Service.create(QNAME_SERVICE);
        Greeter proxy = service.getPort(QNAME_PORT, Greeter.class);
        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(
                BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        p.getRequestContext().put(
                BindingProvider.SOAPACTION_URI_PROPERTY, action);
        p.getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY, URL_ENDPOINT);
        return proxy;
    }
    
    private Dispatch<String> getDispatch(String action) {
        // Get a dispatch
        Service svc = Service.create(QNAME_SERVICE);
        svc.addPort(QNAME_PORT, null, URL_ENDPOINT);
        Dispatch<String> dispatch = svc.createDispatch(QNAME_PORT, 
                String.class, Service.Mode.PAYLOAD);
        BindingProvider p = (BindingProvider) dispatch;
        p.getRequestContext().put(
                BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        p.getRequestContext().put(
                BindingProvider.SOAPACTION_URI_PROPERTY, action);
        return dispatch;
    }
    
    /**
     * Test simple greetMe method 
     * with style doc/lit wrapped without the presence of wrapper classes.
     */
    public void testGreetMe() {
        
        Greeter proxy = getProxy("greetMe");
        
        String me = "Scheu";
        String response = proxy.greetMe(me);
        assertTrue("Hello Scheu".equals(response));
        
        // Try the call again
        response = proxy.greetMe(me);
        assertTrue("Hello Scheu".equals(response));
    }
    
    /**
     * Test simple greetMe method 
     * with style doc/lit wrapped without the presence of wrapper classes.
     * Passing a null input and receiving a null return
     */
    public void testGreetMe_Null() {
        
        
        Greeter proxy = getProxy("greetMe");
        
        String me = null;
        String response = proxy.greetMe(me);
        assertTrue("Expected null but received " + response, response == null);
        
        // Try the call again
        response = proxy.greetMe(me);
        assertTrue("Expected null but received " + response, response == null);
    }
    
    /**
     * Test simple greetMe method with dispatch 
     * with style doc/lit wrapped without the presence of wrapper classes.
     */
    public void testGreetMe_Dispatch() {
       
        Dispatch<String> dispatch = getDispatch("greetMe");
        
        String request =
            "<pre:greetMe xmlns:pre='http://apache.org/axis2/jaxws/sample/dlwmin'>" +
            "<pre:requestType>Scheu</pre:requestType>" +
            "</pre:greetMe>";
        TestLogger.logger.debug("Doc/Lit Wrapped Minimal Request =" + request);
        String response = dispatch.invoke(request);
        TestLogger.logger.debug("Doc/Lit Wrapped Minimal Response =" + response);
        
        assertTrue(response.contains("Hello Scheu"));
        assertTrue(response.contains("dlwmin:greetMeResponse"));
        assertTrue(response.contains(":responseType") ||
                   response.contains("responseType xmlns="));  // assert that response type is a qualified element
        assertTrue(!response.contains("xsi:type")); // xsi:type should not be used
        
        // Try the call again
        response = dispatch.invoke(request);
        TestLogger.logger.debug("Doc/Lit Wrapped Minimal Response =" + response);
        
        assertTrue(response.contains("Hello Scheu"));
        assertTrue(response.contains("dlwmin:greetMeResponse"));
        assertTrue(response.contains(":responseType") ||
                   response.contains("responseType xmlns="));  // assert that response type is a qualified element
        assertTrue(!response.contains("xsi:type")); // xsi:type should not be used
    }
    
    /**
     * Test simpleTest method 
     * with style doc/lit wrapped without the presence of wrapper classes.
     */
    public void testSimpleTest() {
        
        Greeter proxy = getProxy("simpleTest");
        
        String name = "user1";
        byte[] bytes = new byte[5];
        for (int i=0; i< bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        String response = proxy.simpleTest(name, bytes);
        System.out.println(response);
        assertTrue(response.contains("name=user1"));
        assertTrue(response.contains("numbytes=5"));
        
        // Try the call again
        response = proxy.simpleTest(name, bytes);
        System.out.println(response);
        assertTrue(response.contains("name=user1"));
        assertTrue(response.contains("numbytes=5"));
    }
    
    
    /**
     * Test simpleTest method 
     * with style doc/lit wrapped without the presence of wrapper classes.
     */
    public void testSimpleTestNoName() {
        
        Greeter proxy = getProxy("simpleTest");
        
        
        // Try with a no name
        String name = null;
        byte[] bytes = new byte[100];
        String response = proxy.simpleTest(name, bytes);
        System.out.println(response);
        assertTrue(response.contains("name=null"));
        assertTrue(response.contains("numbytes=100"));
        
        // Try the call again
        response = proxy.simpleTest(name, bytes);
        System.out.println(response);
        assertTrue(response.contains("name=null"));
        assertTrue(response.contains("numbytes=100"));
    }
    
    /**
     * Test simpleTest method with dispatch 
     * with style doc/lit wrapped without the presence of wrapper classes.
     */
    public void testSimple_Dispatch() {
       
        Dispatch<String> dispatch = getDispatch("simpleTest");
        
        String request =
            "<pre:simpleTest xmlns:pre='http://apache.org/axis2/jaxws/sample/dlwmin'>" +
            "<pre:name>user1</pre:name>" +
            "<pre:bytes>010203</pre:bytes>" +
            "</pre:simpleTest>";
        TestLogger.logger.debug("Doc/Lit Wrapped Minimal Request =" + request);
        String response = dispatch.invoke(request);
        System.out.println(response);
        TestLogger.logger.debug("Doc/Lit Wrapped Minimal Response =" + response);
        
        assertTrue(response.contains("name=user1"));
        assertTrue(response.contains("numbytes=3"));
        assertTrue(response.contains("dlwmin:simpleTestResponse"));
        
        // Try the call again
        response = dispatch.invoke(request);
        TestLogger.logger.debug("Doc/Lit Wrapped Minimal Response =" + response);
        System.out.println(response);
        
        assertTrue(response.contains("name=user1"));
        assertTrue(response.contains("numbytes=3"));
        assertTrue(response.contains("dlwmin:simpleTestResponse"));
    }
    
    /**
     * Test simpleTest method with dispatch 
     * with style doc/lit wrapped without the presence of wrapper classes.
     */
    public void testSimpleNoName_Dispatch() {
       
        Dispatch<String> dispatch = getDispatch("simpleTest");
        
        String request =
            "<pre:simpleTest xmlns:pre='http://apache.org/axis2/jaxws/sample/dlwmin'>" +
            "<pre:bytes>010203</pre:bytes>" +
            "</pre:simpleTest>";
        TestLogger.logger.debug("Doc/Lit Wrapped Minimal Request =" + request);
        String response = dispatch.invoke(request);
        System.out.println(response);
        TestLogger.logger.debug("Doc/Lit Wrapped Minimal Response =" + response);
        
        assertTrue(response.contains("name=null"));
        assertTrue(response.contains("numbytes=3"));
        assertTrue(response.contains("dlwmin:simpleTestResponse"));
        
        // Try the call again
        response = dispatch.invoke(request);
        TestLogger.logger.debug("Doc/Lit Wrapped Minimal Response =" + response);
        System.out.println(response);
        
        assertTrue(response.contains("name=null"));
        assertTrue(response.contains("numbytes=3"));
        assertTrue(response.contains("dlwmin:simpleTestResponse"));
    }
    
    
    /**
     * Test simpleUnqualified method 
     * with style doc/lit wrapped without the presence of wrapper classes.
     */
    public void testUnqualified() {
        
        Greeter proxy = getProxy("testUnqualified");
        
        String request = "hello world";
        String response = proxy.testUnqualified(request);
        assertTrue("hello world".equals(response));
        
        // Try the call again
        response = proxy.testUnqualified(request);
        assertTrue("hello world".equals(response));
    }
    
    /**
     * Test simpleUnqualified method with dispatch 
     * with style doc/lit wrapped without the presence of wrapper classes.
     */
    public void testUnqualified_Dispatch() {
       
        Dispatch<String> dispatch = getDispatch("testUnqualified");
        
        String request =
            "<pre:unqualifiedTestResponse xmlns:pre='http://apache.org/axis2/jaxws/sample/dlwmin'>" +
            "<unqualifiedRequest>hello world</unqualifiedRequest>" +
            "</pre:unqualifiedTestResponse>";
        TestLogger.logger.debug("Doc/Lit Wrapped Minimal Request =" + request);
        String response = dispatch.invoke(request);
        TestLogger.logger.debug("Doc/Lit Wrapped Minimal Response =" + response);
        
        assertTrue(response.contains("hello world"));
        assertTrue(response.contains("dlwmin:testUnqualifiedResponse"));
        assertTrue(response.contains("<unqualifiedResponse"));  // assert that the child element is an uqualified element
        assertTrue(!response.contains("xsi:type")); // xsi:type should not be used
        
        
        // Try the call again to verify
        response = dispatch.invoke(request);
        TestLogger.logger.debug("Doc/Lit Wrapped Minimal Response =" + response);
        
        assertTrue(response.contains("hello world"));
        assertTrue(response.contains("dlwmin:testUnqualifiedResponse"));
        assertTrue(response.contains("<unqualifiedResponse"));  // assert that the child element is an uqualified element
        assertTrue(!response.contains("xsi:type")); // xsi:type should not be used
    }
    
    /**
     * Test echo with complexType 
     */
    public void testProcess_Echo()  throws Exception {
        
        Greeter proxy = getProxy("process");
        
        TestBean request = new TestBean();
        request.setData1("hello world");
        request.setData2(10);
        TestBean response = proxy.process(0, request);
        assertTrue(response != null);
        assertTrue(response.getData1().equals("hello world"));
        assertTrue(response.getData2() == 10);
        
        
        // Try the call again to verify
        response = proxy.process(0, request);
        assertTrue(response != null);
        assertTrue(response.getData1().equals("hello world"));
        assertTrue(response.getData2() == 10);
    }
    
    /**
     * Test throwing checked exception w/o a JAXB Bean
     */
    public void testProcess_CheckException()  throws Exception {
        
        Greeter proxy = getProxy("process");
        
        TestBean request = new TestBean();
        request.setData1("hello world");
        request.setData2(10);
        try {
            TestBean response = proxy.process(1, request);
            fail("Expected TestException thrown");
        } catch (WebServiceException wse) {
            // Currently there is no support if the fault bean is missing
            assertTrue(wse.getMessage().contains("User fault processing is not supported"));
        } catch (TestException te) {
            assertTrue(te.getMessage().equals("TestException thrown"));
            assertTrue(te.getFlag() == 123);
        } catch (Exception e) {
            fail("Expected TestException thrown but found " + e.getClass());
        }
        
        // Try the call again to verify
        try {
            TestBean response = proxy.process(1, request);
            fail("Expected TestException thrown");
        } catch (WebServiceException wse) {
            // Currently there is no support if the fault bean is missing
            assertTrue(wse.getMessage().contains("User fault processing is not supported"));
        } catch (TestException te) {
            assertTrue(te.getMessage().equals("TestException thrown"));
            assertTrue(te.getFlag() == 123);
        } catch (Exception e) {
            fail("Expected TestException thrown but found " + e.getClass());
        }
    }
    
    /**
     * Test throwing checked exception that has a JAXB Bean
     */
    public void testProcess_CheckException2()  throws Exception {
        
        Greeter proxy = getProxy("process");
        
        TestBean request = new TestBean();
        request.setData1("hello world");
        request.setData2(10);
        try {
            TestBean response = proxy.process(4, request);
            fail("Expected TestException2 thrown");
        } catch (TestException2 te) {
            assertTrue(te.getMessage().equals("TestException2 thrown"));
            assertTrue(te.getFlag() == 456);
        } catch (Exception e) {
            fail("Expected TestException2 thrown but found " + e.getClass());
        }
        
        // Try the call again to verify the same behavior
        try {
            TestBean response = proxy.process(4, request);
            fail("Expected TestException2 thrown");
        } catch (TestException2 te) {
            assertTrue(te.getMessage().equals("TestException2 thrown"));
            assertTrue(te.getFlag() == 456);
        } catch (Exception e) {
            fail("Expected TestException2 thrown but found " + e.getClass());
        }
    }
    
    /**
     * Test throwing checked exception that is a compliant JAXWS exception
     */
    public void testProcess_CheckException3()  throws Exception {
        
        Greeter proxy = getProxy("process");
        
        TestBean request = new TestBean();
        request.setData1("hello world");
        request.setData2(10);
        try {
            TestBean response = proxy.process(5, request);
            fail("Expected TestException3 thrown");
        } catch (TestException3 te) {
            assertTrue(te.getMessage().equals("TestException3 thrown"));
            assertTrue(te.getFaultInfo().getFlag() == 789);
        } catch (Exception e) {
            fail("Expected TestException3 thrown but found " + e.getClass());
        }
        
        // Try the call again to verify the same behavior
        try {
            TestBean response = proxy.process(5, request);
            fail("Expected TestException3 thrown");
        } catch (TestException3 te) {
            assertTrue(te.getMessage().equals("TestException3 thrown"));
            assertTrue(te.getFaultInfo().getFlag() == 789);
        } catch (Exception e) {
            fail("Expected TestException3 thrown but found " + e.getClass());
        }
    }
    
    /**
     * Test throwing WebServiceException
     */
    public void testProcess_WebServiceException()  throws Exception {
        
        Greeter proxy = getProxy("process");
        
        TestBean request = new TestBean();
        request.setData1("hello world");
        request.setData2(10);
        try {
            TestBean response = proxy.process(2, request);
            fail("Expected WebServiceException thrown");
        } catch (WebServiceException wse) {
            assertTrue(wse.getMessage().equals("WebServiceException thrown"));
        } catch (Exception e) {
            fail("Expected WebServiceException thrown but found " + e.getClass());
        }
        
        // Try the call again to verify the same behavior
        try {
            TestBean response = proxy.process(2, request);
            fail("Expected WebServiceException thrown");
        } catch (WebServiceException wse) {
            assertTrue(wse.getMessage().equals("WebServiceException thrown"));
        } catch (Exception e) {
            fail("Expected WebServiceException thrown but found " + e.getClass());
        }
    }
    
    /**
     * Test throwing NPE
     */
    public void testProcess_NPE()  throws Exception {
        
        Greeter proxy = getProxy("process");
        
        TestBean request = new TestBean();
        request.setData1("hello world");
        request.setData2(10);
        try {
            TestBean response = proxy.process(3, request);
            fail("Expected NullPointerException thrown");
        } catch (WebServiceException wse) {
            assertTrue(wse.getMessage().equals("NPE thrown"));
        } catch (Exception e) {
            fail("Expected NullPointerException thrown but found " + e.getClass());
        }
        
        // Try the call again to verify the same behavior
        try {
            TestBean response = proxy.process(3, request);
            fail("Expected NullPointerException thrown");
        } catch (WebServiceException wse) {
            assertTrue(wse.getMessage().equals("NPE thrown"));
        } catch (Exception e) {
            fail("Expected NullPointerException thrown but found " + e.getClass());
        }
    }
    
}
