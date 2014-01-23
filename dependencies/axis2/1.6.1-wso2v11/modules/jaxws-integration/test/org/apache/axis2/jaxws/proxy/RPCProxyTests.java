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

package org.apache.axis2.jaxws.proxy;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.proxy.rpclit.RPCLitImpl;
import org.apache.axis2.jaxws.proxy.rpclit.sei.RPCFault;
import org.apache.axis2.jaxws.proxy.rpclit.sei.RPCLit;
import org.test.proxy.rpclit.ComplexAll;
import org.test.proxy.rpclit.Enum;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

public class RPCProxyTests extends AbstractTestCase {

    private QName serviceName = new QName(
            "http://org.apache.axis2.jaxws.proxy.rpclit", "RPCLitService");
    private String axisEndpoint = "http://localhost:6060/axis2/services/RPCLitService.RPCLitImplPort";
    private QName portName = new QName("http://org.apache.axis2.jaxws.proxy.rpclit",
            "RPCLit");
    private String wsdlLocation = System.getProperty("basedir",".")+"/"+"test/org/apache/axis2/jaxws/proxy/rpclit/META-INF/RPCLit.wsdl";
    
    public static Test suite() {
        return getTestSetup(new TestSuite(RPCProxyTests.class));
    }
    
    /**
     * Utility method to get the proxy
     * @return RPCLit proxy
     * @throws MalformedURLException
     */
    public RPCLit getProxy() throws MalformedURLException {
        File wsdl= new File(wsdlLocation); 
        URL wsdlUrl = wsdl.toURL(); 
        Service service = Service.create(null, serviceName);
        Object proxy =service.getPort(portName, RPCLit.class);
        BindingProvider p = (BindingProvider)proxy; 
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
        
        return (RPCLit)proxy;
    }
    
    /**
     * Utility Method to get a Dispatch<String>
     * @return
     * @throws MalformedURLException
     */
    public Dispatch<String> getDispatch() throws MalformedURLException {
        File wsdl= new File(wsdlLocation); 
        URL wsdlUrl = wsdl.toURL(); 
        Service service = Service.create(null, serviceName);
        service.addPort(portName, null, axisEndpoint);
        Dispatch<String> dispatch = service.createDispatch(portName, String.class, Service.Mode.PAYLOAD);
        return dispatch;
    }
    
    /**
     * Simple test that ensures that we can echo a string to an rpc/lit web service
     */
    public void testSimple() throws Exception {
        try{ 
            RPCLit proxy = getProxy();
            String request = "This is a test...";
           
            String response = proxy.testSimple(request);
            assertTrue(response != null);
            assertTrue(response.equals(request));
            
            // Try a second time
            response = proxy.testSimple(request);
            assertTrue(response != null);
            assertTrue(response.equals(request));
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    /**
     * Simple test that ensures that we can echo a string to an rpc/lit web service
     */
    public void testSimpleInOut() throws Exception {
        try{ 
            RPCLit proxy = getProxy();
            String request = "This is a test...";
            Holder<String> requestParam = new Holder<String>();
            requestParam.value = request;
           
            String response = proxy.testSimpleInOut(requestParam);
            assertTrue(response != null);
            assertTrue(response.equals(request));
            assertTrue(requestParam.value.equals(request));
            
            // Try a second time
            response = proxy.testSimpleInOut(requestParam);
            assertTrue(response != null);
            assertTrue(response.equals(request));
            assertTrue(requestParam.value.equals(request));
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    /**
     * Simple test that ensures that we can echo a string to an rpc/lit web service
     */
    public void testSimple2() throws Exception {
        try{ 
            RPCLit proxy = getProxy();
            String request1 = "hello";
            String request2 = "world";
           
            String response = proxy.testSimple2(request1, request2);
            assertTrue(response != null);
            assertTrue(response.equals("helloworld"));
            
            // Try a second time
            response = proxy.testSimple2(request1, request2);
            assertTrue(response != null);
            assertTrue(response.equals("helloworld"));
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    /**
     * Simple test that ensures that we can echo a string to an rpc/lit web service.
     * This test passes the information in headers
     */
    public void testHeader() throws Exception {
        RPCLit proxy = getProxy();
        String request1 = "hello";
        String request2 = "world";
        
        String response = proxy.testHeader(request1, request2);
        assertTrue(response != null);
        assertTrue(response.equals("helloworld"));
        
        // Try a second time
        response = proxy.testHeader(request1, request2);
        assertTrue(response != null);
        assertTrue(response.equals("helloworld"));
        
    }
    
    /**
     * Simple test that ensures that a service fault is thrown correctly
     */
    public void testFault() throws Exception {
        RPCLit proxy = getProxy();
        try{ 
            proxy.testFault();
            fail("Expected RPCFault");
        } catch(RPCFault rpcFault){ 
            assertTrue(rpcFault.getMessage().equals("Throw RPCFault"));
            assertTrue(rpcFault.getFaultInfo() == 123);
        } catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
        
        // Try a second time
        try{ 
            proxy.testFault();
            fail("Expected RPCFault");
        } catch(RPCFault rpcFault){ 
            assertTrue(rpcFault.getMessage().equals("Throw RPCFault"));
            assertTrue(rpcFault.getFaultInfo() == 123);
        } catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    /**
     * Simple test that ensures that we can echo a string to an rpc/lit web service
     */
    public void testForNull() throws Exception {
        RPCLit proxy = getProxy();
        try{   
            String request = null;
           
            String response = proxy.testSimple(request);
            fail("RPC/LIT should throw webserviceException when operation is invoked with null input parameter");
        }catch(Exception e){ 
            assertTrue(e instanceof WebServiceException);
            TestLogger.logger.debug(e.getMessage());
        }
        
        // Try a second time
        try{   
            String request = null;
           
            String response = proxy.testSimple(request);
            fail("RPC/LIT should throw webserviceException when operation is invoked with null input parameter");
        }catch(Exception e){ 
            assertTrue(e instanceof WebServiceException);
            TestLogger.logger.debug(e.getMessage());
        }
    }
    
    /**
     * Simple test that ensures that we can echo a string to an rpc/lit web service
     */
    public void testForNullReturn() throws Exception {
        
        RPCLit proxy = getProxy();
        try{ 
            String response = proxy.testSimple("returnNull");
            fail("RPC/LIT should throw webserviceException when operation is invoked with null out parameter");
        }catch(Exception e){ 
            assertTrue(e instanceof WebServiceException);
            TestLogger.logger.debug(e.getMessage());
        }
        
        // Try a second time
        try{ 
            String response = proxy.testSimple("returnNull");
            fail("RPC/LIT should throw webserviceException when operation is invoked with null out parameter");
        }catch(Exception e){ 
            assertTrue(e instanceof WebServiceException);
            TestLogger.logger.debug(e.getMessage());
        }
    }
    
    
    
    public void testSimple_Dispatch() throws Exception {
        // Send a payload that simulates
        // the rpc message
        String request = "<tns:testSimple xmlns:tns='http://org/apache/axis2/jaxws/proxy/rpclit'>" +
        "<simpleIn xsi:type='xsd:string' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
        "PAYLOAD WITH XSI:TYPE" +
        "</simpleIn></tns:testSimple>";
        Dispatch<String> dispatch = getDispatch();
        String response = dispatch.invoke(request);

        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(!response.contains("Fault"));
        assertTrue(response.contains("simpleOut"));
        assertTrue(!response.contains(":simpleOut"));  // Make sure simple out is not namespace qualified
        assertTrue(response.contains(":testSimpleResponse"));  // Make sure response is namespace qualified  
        assertTrue(response.contains("PAYLOAD WITH XSI:TYPE"));
        
        // Try a second time
        response = dispatch.invoke(request);

        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(!response.contains("Fault"));
        assertTrue(response.contains("simpleOut"));
        assertTrue(!response.contains(":simpleOut"));  // Make sure simple out is not namespace qualified
        assertTrue(response.contains(":testSimpleResponse"));  // Make sure response is namespace qualified  
        assertTrue(response.contains("PAYLOAD WITH XSI:TYPE"));
    }
    
    public void testSimple2_DispatchWithoutXSIType() throws Exception {
        // Send a payload that simulates
        // the rpc message
        String request = "<tns:testSimple2 xmlns:tns='http://org/apache/axis2/jaxws/proxy/rpclit'>" +
        "<simple2In1>" +
        "HELLO" +
        "</simple2In1>" +
        "<simple2In2>" +
        "WORLD" +
        "</simple2In2></tns:testSimple2>";
        Dispatch<String> dispatch = getDispatch();
        String response = dispatch.invoke(request);
        

        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(!response.contains("Fault"));
        assertTrue(response.contains("simple2Out"));
        assertTrue(!response.contains(":simple2Out"));// Make sure simpleOut is not namespace qualified
        assertTrue(response.contains(":testSimple2Response")); 
        assertTrue(response.contains("HELLOWORLD"));
        
        
        // Try a second time
        response = dispatch.invoke(request);
        

        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(!response.contains("Fault"));
        assertTrue(response.contains("simple2Out"));
        assertTrue(!response.contains(":simple2Out"));// Make sure simpleOut is not namespace qualified
        assertTrue(response.contains(":testSimple2Response")); 
        assertTrue(response.contains("HELLOWORLD"));
    }
    
    public void testSimple_DispatchWithoutXSIType() throws Exception {
        // Send a payload that simulates
        // the rpc message
        String request = "<tns:testSimple xmlns:tns='http://org/apache/axis2/jaxws/proxy/rpclit'>" +
        "<simpleIn>" +
        "PAYLOAD WITHOUT XSI:TYPE" +
        "</simpleIn></tns:testSimple>";
        Dispatch<String> dispatch = getDispatch();
        String response = dispatch.invoke(request);
        

        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(!response.contains("Fault"));
        assertTrue(response.contains("simpleOut"));
        assertTrue(!response.contains(":simpleOut"));  // Make sure simpleOut is not namespace qualified
        assertTrue(response.contains(":testSimpleResponse")); 
        assertTrue(response.contains("PAYLOAD WITHOUT XSI:TYPE"));
        
        
        // Try a second time
        response = dispatch.invoke(request);
        

        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(!response.contains("Fault"));
        assertTrue(response.contains("simpleOut"));
        assertTrue(!response.contains(":simpleOut"));  // Make sure simpleOut is not namespace qualified
        assertTrue(response.contains(":testSimpleResponse")); 
        assertTrue(response.contains("PAYLOAD WITHOUT XSI:TYPE"));
    }
    
    /**
     * Simple test that ensures that we can echo a string to an rpc/lit web service.
     */
    public void testStringList() throws Exception {
        try{ 
            RPCLit proxy = getProxy();
            String[] request = new String[] {"Hello" , "World"};
           
            String[] response = proxy.testStringList2(request);
            assertTrue(response != null);
            assertTrue(response.length==2);
            assertTrue(response[0].equals("Hello"));
            assertTrue(response[1].equals("World"));
            
            // Try a second time
            response = proxy.testStringList2(request);
            assertTrue(response != null);
            assertTrue(response.length==2);
            assertTrue(response[0].equals("Hello"));
            assertTrue(response[1].equals("World"));
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    public void testStringList_Dispatch() throws Exception {
        // Send a payload that simulates
        // the rpc message
        String request = "<tns:testStringList2 xmlns:tns='http://org/apache/axis2/jaxws/proxy/rpclit'>" +
        //"<tns:arg_2_0 xmlns:tns='http://org/apache/axis2/jaxws/proxy/rpclit' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:type='tns:StringList' >" +
        "<tns:arg_2_0>" +
        "Hello World" +
        "</tns:arg_2_0></tns:testStringList2>";
        Dispatch<String> dispatch = getDispatch();
        String response = dispatch.invoke(request);

        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(!response.contains("Fault"));
        assertTrue(response.contains("testStringList2Return"));
        assertTrue(response.contains("testStringList2Response"));
        assertTrue(response.contains("Hello World"));
        
        
        // Try a second time
        response = dispatch.invoke(request);

        assertNotNull("dispatch invoke returned null", response);
        TestLogger.logger.debug(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(!response.contains("Fault"));
        assertTrue(response.contains("testStringList2Return"));
        assertTrue(response.contains("testStringList2Response"));
        assertTrue(response.contains("Hello World"));
    }
    /**
     * Currently not enabled due to problems with 
     * marshaling an xsd:list of QName.
     * 
     * Users should use document/literal processing if they 
     * need such complicated scenarios.
     */
    public void _testLists() {
        try{ 
            RPCLit proxy = getProxy();
            QName[] request = new QName[] {RPCLitImpl.qname1, RPCLitImpl.qname2};
           
            QName[] qNames = proxy.testLists(request,
                    new XMLGregorianCalendar[0],
                    new String[0],
                    new BigInteger[0],
                    new Long[0],
                    new Enum[0],
                    new String[0],
                    new ComplexAll());
            assertTrue(qNames.length==2);
            assertTrue(qNames[0].equals(RPCLitImpl.qname1));
            assertTrue(qNames[1].equals(RPCLitImpl.qname2));
            
            
            //Try a second time
            qNames = proxy.testLists(request,
                                     new XMLGregorianCalendar[0],
                                     new String[0],
                                     new BigInteger[0],
                                     new Long[0],
                                     new Enum[0],
                                     new String[0],
                                     new ComplexAll());
                             assertTrue(qNames.length==2);
                             assertTrue(qNames[0].equals(RPCLitImpl.qname1));
                             assertTrue(qNames[1].equals(RPCLitImpl.qname2));
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    /**
     * Currently not enabled due to problems marshalling/unmarshalling
     * an xsd:list of XMLGregorianCalendar.
     * 
     * Users should use document/literal processing if they 
     * need such complicated scenarios.
     */
    public void _testCalendars() {
        try{ 
            RPCLit proxy = getProxy();
            XMLGregorianCalendar[] request = new XMLGregorianCalendar[] {RPCLitImpl.bday, RPCLitImpl.holiday};
           
            XMLGregorianCalendar[] cals  = proxy.testCalendarList1(request);
            assertTrue(cals.length == 2);
            assertTrue(cals[0].compare(RPCLitImpl.bday) == 0);
            assertTrue(cals[1].compare(RPCLitImpl.holiday) == 0);
            
            // Try a second time
            cals  = proxy.testCalendarList1(request);
            assertTrue(cals.length == 2);
            assertTrue(cals[0].compare(RPCLitImpl.bday) == 0);
            assertTrue(cals[1].compare(RPCLitImpl.holiday) == 0);
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    public void testBigIntegers() {
        try{ 
            RPCLit proxy = getProxy();
            BigInteger[] request = new BigInteger[] {RPCLitImpl.bigInt1, RPCLitImpl.bigInt2};
           
            BigInteger[] ints  = proxy.testBigIntegerList3(request);
            assertTrue(ints.length==2);
            assertTrue(ints[0].compareTo(RPCLitImpl.bigInt1) == 0);
            assertTrue(ints[1].compareTo(RPCLitImpl.bigInt2) == 0);
            
            // Try a second time
            ints  = proxy.testBigIntegerList3(request);
            assertTrue(ints.length==2);
            assertTrue(ints[0].compareTo(RPCLitImpl.bigInt1) == 0);
            assertTrue(ints[1].compareTo(RPCLitImpl.bigInt2) == 0);
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    public void testLongs() {
        try{ 
            RPCLit proxy = getProxy();
            Long[] request = new Long[] {new Long(0), new Long(1), new Long(2)};
           
            Long[] longs  = proxy.testLongList4(request);
            assertTrue(longs.length==3);
            assertTrue(longs[0] == 0);
            assertTrue(longs[1] == 1);
            assertTrue(longs[2] == 2);
            
            // Try a second time
            longs  = proxy.testLongList4(request);
            assertTrue(longs.length==3);
            assertTrue(longs[0] == 0);
            assertTrue(longs[1] == 1);
            assertTrue(longs[2] == 2);
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    public void testEnums() {
        try{ 
            RPCLit proxy = getProxy();
            Enum[] request = new Enum[] {Enum.ONE, Enum.TWO, Enum.THREE};
           
            Enum[] enums  = proxy.testEnumList5(request);
            assertTrue(enums.length==3);
            assertTrue(enums[0] == Enum.ONE);
            assertTrue(enums[1] == Enum.TWO);
            assertTrue(enums[2] == Enum.THREE);
            
            
            // Try a second time
            enums  = proxy.testEnumList5(request);
            assertTrue(enums.length==3);
            assertTrue(enums[0] == Enum.ONE);
            assertTrue(enums[1] == Enum.TWO);
            assertTrue(enums[2] == Enum.THREE);
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
}
