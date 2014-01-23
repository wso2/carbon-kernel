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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.sample.dlwminArrays.IGenericService;
import org.apache.axis2.jaxws.sample.dlwminArrays.WSUser;


import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

/**
 * 
 * Tests to verify Document/Literal Wrapped Minimal Scenarios
 * Document/Literal Wrapped is a JAX-WS style.
 * "Minimal" indicates that no wrapper beans are associated with the JAX-WS method.
 * In most enterprise scenarios, wrapper beans are packaged with the JAX-WS application.
 */
public class DLWMinArrayTests extends AbstractTestCase {

    private static final String NAMESPACE = "http://apache.org/axis2/jaxws/sample/dlwminArrays";
    private static final QName QNAME_SERVICE = new QName(
            NAMESPACE, "GenericService");
    private static final QName QNAME_PORT = new QName(
            NAMESPACE, "GenericServicePort");
    private static final String URL_ENDPOINT = "http://localhost:6060/axis2/services/GenericService.GenericServicePort";
    
    private static String FIRST = "first";
    private static String SECOND = "second";
	
    public static Test suite() {
        return getTestSetup(new TestSuite(DLWMinArrayTests.class));
    }

    private IGenericService getProxy(String action) {
        Service service = Service.create(QNAME_SERVICE);
        IGenericService proxy = service.getPort(QNAME_PORT, IGenericService.class);
        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(
                BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        p.getRequestContext().put(
                BindingProvider.SOAPACTION_URI_PROPERTY, action);
        p.getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY, URL_ENDPOINT);
        return proxy;
    }
    
    /**
     * Test sayHello method 
     */
    public void testHello() throws Exception {
        
        IGenericService proxy = getProxy("sayHello");
        
        String me = "Scheu";
        String response = proxy.sayHello(me);
        assertTrue("Hello Scheu".equals(response));
        
        // Try the call again
        response = proxy.sayHello(me);
        assertTrue("Hello Scheu".equals(response));
    }
    
    /**
     * Test method that returns a String[]
     */
    public void testGetSimpleArray() throws Exception {
        
        IGenericService proxy = getProxy("getSimpleArray");
        
        String[] response = proxy.getSimpleArray();
        assertTrue(response != null);
        assertTrue("Expected 2, Received " + response.length, response.length == 2);
        assertTrue("The first response is: " + response[0], response.length == 2);
        assertTrue(FIRST.equals(response[0]));
        assertTrue(SECOND.equals(response[1]));
        
        // Try the call again
        response = proxy.getSimpleArray();
        assertTrue(response != null);
        assertTrue(response.length == 2);
        assertTrue(FIRST.equals(response[0]));
        assertTrue(SECOND.equals(response[1]));
    }
    
    /**
     * Test method that returns a List<String>
     */
    public void testGetSimpleList() throws Exception {
        
        IGenericService proxy = getProxy("getSimpleList");
        
        List<String> response = proxy.getSimpleList();
        assertTrue(response != null);
        assertTrue(response.size() == 2);
        assertTrue(FIRST.equals(response.get(0)));
        assertTrue(SECOND.equals(response.get(1)));
        
        // Try the call again
        response = proxy.getSimpleList();
        assertTrue(response != null);
        assertTrue(response.size() == 2);
        assertTrue(FIRST.equals(response.get(0)));
        assertTrue(SECOND.equals(response.get(1)));
    }
    
    /**
     * Test method that returns a bean array (WSUser[])
     */
    public void testGetComplexArray() throws Exception {
        
        IGenericService proxy = getProxy("getComplexArray");
        
        WSUser[] response = proxy.getComplexArray();
        assertTrue(response != null);
        assertTrue("Expected 2, Received " + response.length, response.length == 2);
        assertTrue(FIRST.equals(response[0].getUserID()));
        assertTrue(SECOND.equals(response[1].getUserID()));
        
        // Try the call again
        response = proxy.getComplexArray();
        assertTrue(response != null);
        assertTrue(response.length == 2);
        assertTrue(FIRST.equals(response[0].getUserID()));
        assertTrue(SECOND.equals(response[1].getUserID()));
    }
    
    /**
     * Test method that returns a List of beans (List<WSUser>)
     */
    public void testGetComplexList() throws Exception {
        
        IGenericService proxy = getProxy("getComplexList");
        
        List<WSUser> response = proxy.getComplexList();
        assertTrue(response != null);
        assertTrue(response.size() == 2);
        assertTrue(FIRST.equals(response.get(0).getUserID()));
        assertTrue(SECOND.equals(response.get(1).getUserID()));
        
        // Try the call again
        response = proxy.getComplexList();
        assertTrue(response != null);
        assertTrue(response.size() == 2);
        assertTrue(FIRST.equals(response.get(0).getUserID()));
        assertTrue(SECOND.equals(response.get(1).getUserID()));
    }
    
    /**
     * Test method that echos a List of beans (List<WSUser>)
     * Two items are echo'd.
     */
    public void testEchoComplexList2() throws Exception {
        
        IGenericService proxy = getProxy("echoComplexList");
        
        List<WSUser> in = new ArrayList<WSUser>();
        WSUser wsUser = new WSUser();
        wsUser.setUserID("Hello");
        in.add(wsUser);
        wsUser = new WSUser();
        wsUser.setUserID("World");
        in.add(wsUser);
        
        List<WSUser> response = proxy.echoComplexList(in);
        assertTrue(response != null);
        assertTrue(response.size() == 2);
        assertTrue("Hello".equals(response.get(0).getUserID()));
        assertTrue("World".equals(response.get(1).getUserID()));
        
        // Try the call again
        response = proxy.echoComplexList(in);
        assertTrue(response != null);
        assertTrue(response.size() == 2);
        assertTrue("Hello".equals(response.get(0).getUserID()));
        assertTrue("World".equals(response.get(1).getUserID()));
    }
    
    /**
     * Test method that echos a List of beans (List<WSUser>)
     * One item is echo'd.
     */
    public void testEchoComplexList1() throws Exception {
        
        IGenericService proxy = getProxy("echoComplexList");
        
        List<WSUser> in = new ArrayList<WSUser>();
        WSUser wsUser = new WSUser();
        wsUser.setUserID("Hello");
        in.add(wsUser);
        
        List<WSUser> response = proxy.echoComplexList(in);
        assertTrue(response != null);
        assertTrue(response.size() == 1);
        assertTrue("Hello".equals(response.get(0).getUserID()));
        
        
        // Try the call again
        response = proxy.echoComplexList(in);
        assertTrue(response != null);
        assertTrue(response.size() == 1);
        assertTrue("Hello".equals(response.get(0).getUserID()));
    }
    
    /**
     * Test method that echos a List of beans (List<WSUser>)
     * The list contains no items.
     */
    public void testEchoComplexList0() throws Exception {
        
        IGenericService proxy = getProxy("echoComplexList");
        
        List<WSUser> in = new ArrayList<WSUser>();
        
        List<WSUser> response = proxy.echoComplexList(in);
        assertTrue(response != null);
        assertTrue(response.size() == 0);
        
        // Try the call again
        response = proxy.echoComplexList(in);
        assertTrue(response != null);
        assertTrue(response.size() == 0);
        
    }
    
    /**
     * Test method that echos a List of beans (List<WSUser>)
     * The list contains no items.
     */
    public void testEchoComplexListNull() throws Exception {
        
        IGenericService proxy = getProxy("echoComplexList");
        
        // There really is no discernible difference between
        // an empty array and null over the wire.  Sometimes users
        // will pass in a null on the client or server.
        
        List<WSUser> response = proxy.echoComplexList(null);
        assertTrue(response != null);
        assertTrue(response.size() == 0);
        
        // Try the call again
        response = proxy.echoComplexList(null);
        assertTrue(response != null);
        assertTrue(response.size() == 0);
        
        // Now try force the server to return a null argument
        List<WSUser> in = new ArrayList<WSUser>();
        WSUser wsUser = new WSUser();
        wsUser.setUserID("FORCENULL");
        in.add(wsUser);
        
        response = proxy.echoComplexList(in);
        assertTrue(response != null);
        assertTrue(response.size() == 0);
        
        // Try the call again
        response = proxy.echoComplexList(in);
        assertTrue(response != null);
        assertTrue(response.size() == 0);
        
        
    }
    
    /**
     * Test method that echos a List of beans (List<WSUser>)
     * and echos a List<String> as an inout parameter.
     * 2 WSUsers are echo'd
     * 2 Strings are echo'd
     */
    public void testEcho22() throws Exception {
        
        IGenericService proxy = getProxy("echo");
        
        List<WSUser> in = new ArrayList<WSUser>();
        WSUser wsUser = new WSUser();
        wsUser.setUserID("James Bond");
        in.add(wsUser);
        wsUser = new WSUser();
        wsUser.setUserID("Dr. Evil");
        in.add(wsUser);
        
        List<String> id_in = new ArrayList<String>();
        id_in.add("jbond");
        id_in.add("evil");
        Holder<List<String>> inout = new Holder<List<String>>(id_in);
        
        List<WSUser> response = proxy.echo(in, inout);
        assertTrue(response != null);
        assertTrue(response.size() == 2);
        assertTrue("James Bond".equals(response.get(0).getUserID()));
        assertTrue("Dr. Evil".equals(response.get(1).getUserID()));
        List<String> id_out = inout.value;
        assertTrue(id_out.size() == 2);
        assertTrue("JBOND".equals(id_out.get(0)));
        assertTrue("EVIL".equals(id_out.get(1)));
        
        // Try the call again
        inout = new Holder<List<String>>(id_in);
        response = proxy.echo(in, inout);
        assertTrue(response != null);
        assertTrue(response.size() == 2);
        assertTrue("James Bond".equals(response.get(0).getUserID()));
        assertTrue("Dr. Evil".equals(response.get(1).getUserID()));
        id_out = inout.value;
        assertTrue(id_out.size() == 2);
        assertTrue("JBOND".equals(id_out.get(0)));
        assertTrue("EVIL".equals(id_out.get(1)));
    }
    
    /**
     * Test method that echos a List of beans (List<WSUser>)
     * and echos a List<String> as an inout parameter.
     * 1 WSUsers is echo'd
     * 1 Strings is echo'd
     */
    public void testEcho11() throws Exception {
        
        IGenericService proxy = getProxy("echo");
        
        List<WSUser> in = new ArrayList<WSUser>();
        WSUser wsUser = new WSUser();
        wsUser.setUserID("James Bond");
        in.add(wsUser);
        
        
        List<String> id_in = new ArrayList<String>();
        id_in.add("jbond");
        Holder<List<String>> inout = new Holder<List<String>>(id_in);
        
        List<WSUser> response = proxy.echo(in, inout);
        assertTrue(response != null);
        assertTrue(response.size() == 1);
        assertTrue("James Bond".equals(response.get(0).getUserID()));
        List<String> id_out = inout.value;
        assertTrue(id_out.size() == 1);
        assertTrue("JBOND".equals(id_out.get(0)));
        
        // Try the call again
        inout = new Holder<List<String>>(id_in);
        response = proxy.echo(in, inout);
        assertTrue(response != null);
        assertTrue(response.size() == 1);
        assertTrue("James Bond".equals(response.get(0).getUserID()));
        id_out = inout.value;
        assertTrue(id_out.size() == 1);
        assertTrue("JBOND".equals(id_out.get(0)));
    }
    
    /**
     * Test method that echos a List of beans (List<WSUser>)
     * and echos a List<String> as an inout parameter.
     * 1 WSUsers is echo'd
     * 0 Strings are echo'd
     */
    public void testEcho10() throws Exception {
        
        IGenericService proxy = getProxy("echo");
        
        List<WSUser> in = new ArrayList<WSUser>();
        WSUser wsUser = new WSUser();
        wsUser.setUserID("James Bond");
        in.add(wsUser);
        
        List<String> id_in = new ArrayList<String>();
        Holder<List<String>> inout = new Holder<List<String>>(id_in);
        
        List<WSUser> response = proxy.echo(in, inout);
        assertTrue(response != null);
        assertTrue(response.size() == 1);
        assertTrue("James Bond".equals(response.get(0).getUserID()));
        List<String> id_out = inout.value;
        assertTrue(id_out.size() == 0);
        
        // Try the call again
        inout = new Holder<List<String>>(id_in);
        response = proxy.echo(in, inout);
        assertTrue(response != null);
        assertTrue(response.size() == 1);
        assertTrue("James Bond".equals(response.get(0).getUserID()));
        id_out = inout.value;
        assertTrue(id_out.size() == 0);
    }
    
    /**
     * Test method that echos a List of beans (List<WSUser>)
     * and echos a List<String> as an inout parameter.
     * 0 WSUsers are echo'd
     * 1 Strings is echo'd
     */
    public void testEcho01() throws Exception {
        
        IGenericService proxy = getProxy("echo");
        
        List<WSUser> in = new ArrayList<WSUser>();
        
        List<String> id_in = new ArrayList<String>();
        id_in.add("jbond");
        Holder<List<String>> inout = new Holder<List<String>>(id_in);
        
        List<WSUser> response = proxy.echo(in, inout);
        assertTrue(response != null);
        assertTrue(response.size() == 0);
        List<String> id_out = inout.value;
        assertTrue(id_out.size() == 1);
        assertTrue("JBOND".equals(id_out.get(0)));
        
        // Try the call again
        inout = new Holder<List<String>>(id_in);
        response = proxy.echo(in, inout);
        assertTrue(response != null);
        assertTrue(response.size() == 0);
        id_out = inout.value;
        assertTrue(id_out.size() == 1);
        assertTrue("JBOND".equals(id_out.get(0)));
    }
    
    /**
     * Test method that echos a List of beans (List<WSUser>)
     * and echos a List<String> as an inout parameter.
     * 0 WSUsers are echo'd
     * 0 Strings are echo'd
     */
    public void testEcho00() throws Exception {
        
        IGenericService proxy = getProxy("echo");
        
        List<WSUser> in = new ArrayList<WSUser>();
        
        List<String> id_in = new ArrayList<String>();
        Holder<List<String>> inout = new Holder<List<String>>(id_in);
        
        List<WSUser> response = proxy.echo(in, inout);
        assertTrue(response != null);
        assertTrue(response.size() == 0);
        List<String> id_out = inout.value;
        assertTrue(id_out.size() == 0);
        
        // Try the call again
        inout = new Holder<List<String>>(id_in);
        response = proxy.echo(in, inout);
        assertTrue(response != null);
        assertTrue(response.size() == 0);
        id_out = inout.value;
        assertTrue(id_out.size() == 0);
    }
}
