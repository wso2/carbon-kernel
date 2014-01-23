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
import org.apache.axis2.jaxws.message.databinding.JAXBUtilsMonitor;
import org.apache.axis2.jaxws.proxy.gorilla_dlw.sei.GorillaInterface;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class GorillaDLWProxyTests extends AbstractTestCase {

    private QName serviceName = new QName(
            "http://org.apache.axis2.jaxws.proxy.gorilla_dlw", "GorillaService");
    private String axisEndpoint = "http://localhost:6060/axis2/services/GorillaService.GorillaProxyImplPort";
    private QName portName = new QName("http://org.apache.axis2.jaxws.proxy.rpclit",
            "GorillaPort");
    private String wsdlLocation = System.getProperty("basedir",".")+"/"+"test/org/apache/axis2/jaxws/proxy/gorilla_dlw/META-INF/gorilla_dlw.wsdl";
    
    public static Test suite() {
        return getTestSetup(new TestSuite(GorillaDLWProxyTests.class));
    }
    
    /**
     * Utility method to get the proxy
     * @return GorillaInterface proxy
     * @throws MalformedURLException
     */
    public GorillaInterface getProxy() throws MalformedURLException {
        File wsdl= new File(wsdlLocation); 
        URL wsdlUrl = wsdl.toURL(); 
        Service service = Service.create(null, serviceName);
        Object proxy =service.getPort(portName, GorillaInterface.class);
        BindingProvider p = (BindingProvider)proxy; 
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
        
        return (GorillaInterface)proxy;
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
     * Simple test that ensures that we can echo a string.
     * If this test fails, it usually means that there are connection
     * problems or deploy problems.
     */
    public void testEchoString() throws Exception {
        try{ 
            GorillaInterface proxy = getProxy();
            
            // Straight Forward Test
            String request = "Hello World";
           
            String response = proxy.echoString(request);
            assertTrue(response != null);
            assertEquals(response, request);
            
            // Try again to verify
            response = proxy.echoString(request);
            assertTrue(response != null);
            assertEquals(response, request);
            
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    /**
     * Tests that we can echo a null
     */
    public void testEchoStringNull() throws Exception {
        try{ 
            GorillaInterface proxy = getProxy();
            String request = null;  // Null is an appropriate input
           
            String response = proxy.echoString(request);
            assertTrue(response == null);
            
            // Try again to verify
            response = proxy.echoString(request);
            assertTrue(response == null);
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    /**
     * Test whether the @XmlSeeAlso that was added to the SEI
     * is used to construct the JAXBContext
     */
    public void testXmlSeeAlso() throws Exception {
        try{
            // Set up the JAXBUtils monitor
            JAXBUtilsMonitor.setMonitoring(true);
            JAXBUtilsMonitor.clear();
            
            GorillaInterface proxy = getProxy();
            String request = "Hello World";
           
            String response = proxy.echoString(request);
            assertTrue(response != null);
            assertEquals(response, request);
            
            // Now query the monitor
            List<String> keys = JAXBUtilsMonitor.getPackageKeys();
            assertTrue(keys != null && keys.size() > 0);
            for (int i=0; i<keys.size(); i++) {
                String observedKey = keys.get(i);
                TestLogger.logger.debug("Observed Key =" + observedKey);
                // Check for one of the expected (referenced) packages
                assertTrue(observedKey.contains("org.apache.axis2.jaxws.proxy.gorilla_dlw.data"));
                // Check for the package referenced only by an @XmlSeeAlso
                assertTrue(observedKey.contains("org.test.stock2"));
                // Check for the package referenced in the return type List<> generic
                assertTrue(observedKey.contains("org.test.stock1"));
            }
            
            
            // Try again to verify
            JAXBUtilsMonitor.clear();
            response = proxy.echoString(request);
            assertTrue(response != null);
            assertEquals(response, request);
            
            // Now query the monitor
            keys = JAXBUtilsMonitor.getPackageKeys();
            assertTrue(keys != null && keys.size() > 0);
            for (int i=0; i<keys.size(); i++) {
                String observedKey = keys.get(i);
                TestLogger.logger.debug("Observed Key =" + observedKey);
                // Check for one of the expected (referenced) packages
                assertTrue(observedKey.contains("org.apache.axis2.jaxws.proxy.gorilla_dlw.data"));
                // Check for the package referenced only by an @XmlSeeAlso
                assertTrue(observedKey.contains("org.test.stock2"));
                // Check for the package referenced in the return type List<> generic
                assertTrue(observedKey.contains("org.test.stock1"));
            }
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        } finally {
            JAXBUtilsMonitor.setMonitoring(false);
        }
    }
    
    /**
     * Testing of StringList (xsd:list of string)
     */
    public void testEchoStringList() throws Exception {
        // Run the test multiple times to verify correct behavior
        _testEchoStringList();
        _testEchoStringList();
        _testEchoStringList();
    }
    public void _testEchoStringList() throws Exception {
        try{ 
            GorillaInterface proxy = getProxy();
            
            // Test sending Hello World
            List<String> request1 = new ArrayList<String>();
            request1.add("Hello");
            request1.add("World");
            List<String> response1 = proxy.echoStringList(request1);
            assertTrue(response1 != null);
            assertTrue(compareLists(request1, response1));
            
            // Test with empty list
            List<String> request2 = new ArrayList<String>();
            List<String> response2 = proxy.echoStringList(request2);
            assertTrue(response2 != null);
            assertTrue(compareLists(request2, response2));
            
            // Test with null
            // Note that the response will be an empty array because
            // the JAXB bean will never represent List<String> as a null.  This is expected.
            List<String> request3 = null;
            List<String> response3 = proxy.echoStringList(request3);
            assertTrue(response3 != null && response3.size() == 0);
            
            // Test sending Hello null World
            // Note that the null is purged by JAXB.  This is expected.
            List<String> request4 = new ArrayList<String>();
            request4.add("Hello");
            request4.add(null);
            request4.add("World");
            List<String> response4 = proxy.echoStringList(request4);
            assertTrue(response4!= null);
            assertTrue(compareLists(request1, response4));  // Response 4 should be the same as Request 1
            
            // Test sending "Hello World"
            // Note that the Hello World is divided into two items.
            // This is due to the xsd:list serialization. This is expected.
            List<String> request5 = new ArrayList<String>();
            request5.add("Hello World");
            List<String> response5 = proxy.echoStringList(request5);
            assertTrue(response5!= null);
            assertTrue(compareLists(request1, response5)); // Response 5 should be the same as Request 1
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    /**
     * Testing of StringList (xsd:list of string)
     * SEI is mapped to String[] instead of List<String>
     */
    public void testEchoStringListAlt() throws Exception {
        
        // Run the test multiple times to verify correct behavior
        _testEchoStringListAlt();
        _testEchoStringListAlt();
        _testEchoStringListAlt();
    }
    public void _testEchoStringListAlt() throws Exception {
        try{ 
            GorillaInterface proxy = getProxy();
            
            // Test sending Hello World
            String[] request1 = new String[] {"Hello", "World"};
            String[] response1 = proxy.echoStringListAlt(request1);
            assertTrue(response1 != null);
            assertTrue(compareArrays(request1, response1));
            
            // Test with empty array
            String[] request2 = new String[] {};
            String[] response2 = proxy.echoStringListAlt(request2);
            assertTrue(response2 != null);
            assertTrue(compareArrays(request2, response2));
            
            // Test with null
            // Note that the response will be an empty array because
            // the JAXB bean will never represent List<String> as a null.  This is expected.
            String[] request3 = null;
            String[] response3 = proxy.echoStringListAlt(request3);
            assertTrue(response3 != null && response3.length == 0);
            
            // Test sending Hello null World
            // Note that the null is purged by JAXB.  This is expected.
            String[] request4 = new String[] {"Hello", null, "World"};
            String[] response4 = proxy.echoStringListAlt(request4);
            assertTrue(response4!= null);
            assertTrue(compareArrays(request1, response4));  // Response 4 should be the same as Request 1
            
            // Test sending "Hello World"
            // Note that the Hello World is divided into two items.
            // This is due to the xsd:list serialization. This is expected.
            String[] request5 = new String[] {"Hello World"};
            String[] response5 = proxy.echoStringListAlt(request5);
            assertTrue(response5!= null);
            assertTrue(compareArrays(request1, response5)); // Response 5 should be the same as Request 1
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    /**
     * Test of String Array (string maxOccurs=unbounded)
     * @throws Exception
     */
    public void testEchoIndexedStringArray() throws Exception {
        // Run the test multiple times to verify correct behavior
        _testEchoIndexedStringArray();
        _testEchoIndexedStringArray();
        _testEchoIndexedStringArray();
    }
    public void _testEchoIndexedStringArray() throws Exception {
        try{ 
            GorillaInterface proxy = getProxy();
            
            // Test sending Hello World
            List<String> request1 = new ArrayList<String>();
            request1.add("Hello");
            request1.add("World");
            List<String> response1 = proxy.echoIndexedStringArray(request1);
            assertTrue(response1 != null);
            assertTrue(compareLists(request1, response1));
            
            // Test with empty list
            List<String> request2 = new ArrayList<String>();
            List<String> response2 = proxy.echoIndexedStringArray(request2);
            assertTrue(response2 != null);
            assertTrue(compareLists(request2, response2));
            
            // Test with null
            // Note that the response will be an empty array because
            // the JAXB bean will never represent List<String> as a null.  This is expected.
            List<String> request3 = null;
            List<String> response3 = proxy.echoIndexedStringArray(request3);
            assertTrue(response3 != null && response3.size() == 0);
            
            // Test sending Hello null World
            // Note that the null is preserved and the request and response
            // are the same..note that this is different than the xsd:list processing (see testStringList above)
            // This is expected.
            List<String> request4 = new ArrayList<String>();
            request4.add("Hello");
            request4.add(null);
            request4.add("World");
            List<String> response4 = proxy.echoIndexedStringArray(request4);
            assertTrue(response4!= null);
            assertTrue(compareLists(request4, response4));  // Response 4 should be the same as Request 1
            
            // Test sending "Hello World"
            // Note that the Hello World remains one item.
            List<String> request5 = new ArrayList<String>();
            request5.add("Hello World");
            List<String> response5 = proxy.echoIndexedStringArray(request5);
            assertTrue(response5!= null);
            assertTrue(compareLists(request5, response5)); // Response 5 should be the same as Request 1
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    /**
     * Test of String Array (string maxOccurs=unbounded)
     * @throws Exception
     */
    public void testEchoStringArray() throws Exception {
        
        // Run the test multiple times to verify correct behavior
        _testEchoStringArray();
        _testEchoStringArray();
        _testEchoStringArray();
    }
    public void _testEchoStringArray() throws Exception {
        try{ 
            GorillaInterface proxy = getProxy();
            
            // Test sending Hello World
            List<String> request1 = new ArrayList<String>();
            request1.add("Hello");
            request1.add("World");
            List<String> response1 = proxy.echoStringArray(request1);
            assertTrue(response1 != null);
            assertTrue(compareLists(request1, response1));
            
            // Test with empty list
            List<String> request2 = new ArrayList<String>();
            List<String> response2 = proxy.echoStringArray(request2);
            assertTrue(response2 != null);
            assertTrue(compareLists(request2, response2));
            
            // Test with null
            // Note that the response will be an empty array because
            // the JAXB bean will never represent List<String> as a null.  This is expected.
            List<String> request3 = null;
            List<String> response3 = proxy.echoStringArray(request3);
            assertTrue(response3 != null && response3.size() == 0);
            
            // Test sending Hello null World
            // Note that the null is preserved and the request and response
            // are the same..note that this is different than the xsd:list processing (see testStringList above)
            // This is expected.
            List<String> request4 = new ArrayList<String>();
            request4.add("Hello");
            request4.add(null);
            request4.add("World");
            List<String> response4 = proxy.echoStringArray(request4);
            assertTrue(response4!= null);
            assertTrue(compareLists(request4, response4));  // Response 4 should be the same as Request 1
            
            // Test sending "Hello World"
            // Note that the Hello World remains one item.
            List<String> request5 = new ArrayList<String>();
            request5.add("Hello World");
            List<String> response5 = proxy.echoStringArray(request5);
            assertTrue(response5!= null);
            assertTrue(compareLists(request5, response5)); // Response 5 should be the same as Request 1
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    /**
     * Test of String Array (string maxOccurs=unbounded) which is mapped to String[]
     * @throws Exception
     */
    public void testEchoStringArrayAlt() throws Exception {
        
        // Run the test multiple times to verify correct behavior
        _testEchoStringArrayAlt();
        _testEchoStringArrayAlt();
        _testEchoStringArrayAlt();
    }
    public void _testEchoStringArrayAlt() throws Exception {
        try{ 
            GorillaInterface proxy = getProxy();
            
            // Test sending Hello World
            String[] request1 = new String[] {"Hello", "World"};
            String[] response1 = proxy.echoStringArrayAlt(request1);
            assertTrue(response1 != null);
            assertTrue(compareArrays(request1, response1));
            
            // Test with empty list
            String[] request2 = new String[] {};
            String[] response2 = proxy.echoStringArrayAlt(request2);
            assertTrue(response2 != null);
            assertTrue(compareArrays(request2, response2));
            
            // Test with null
            // Note that the response will be an empty array because
            // the JAXB bean will never represent List<String> as a null.  This is expected.
            String[] request3 = null;
            String[] response3 = proxy.echoStringArrayAlt(request3);
            assertTrue(response3 != null && response3.length == 0);
            
            // Test sending Hello null World
            // Note that the null is preserved and the request and response
            // are the same..note that this is different than the xsd:list processing (see testStringList above)
            // This is expected.
            String[] request4 = new String[] {"Hello", null, "World"};
            String[] response4 = proxy.echoStringArrayAlt(request4);
            assertTrue(response4!= null);
            assertTrue(compareArrays(request4, response4));  // Response 4 should be the same as Request 1
            
            // Test sending "Hello World"
            // Note that the Hello World remains one item.
            String[] request5 = new String[] {"Hello World"};
            String[] response5 = proxy.echoStringArrayAlt(request5);
            assertTrue(response5!= null);
            assertTrue(compareArrays(request5, response5)); // Response 5 should be the same as Request 1
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    public void testEchoDate() throws Exception{
    	try{
    		System.out.println("TestEchoDate");
	    	GorillaInterface proxy = getProxy();
	    	GregorianCalendar cal = new GregorianCalendar(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH);
	    	DatatypeFactory javaxtypeFactory = DatatypeFactory.newInstance();
	    	XMLGregorianCalendar request=  javaxtypeFactory.newXMLGregorianCalendar(cal);
	    	System.out.println("Javax Factory Clazz Name = "+request.getClass().getName());
	    	Duration d = javaxtypeFactory.newDuration(System.currentTimeMillis());
	    	XMLGregorianCalendar response = proxy.echoDate(request, d);
	    	System.out.println(response.toString());
	    	assertNotNull(response);
    	}catch(Exception e){
    		e.printStackTrace();
    		fail("Exception in testEchoDate :"+e);
    	}
    }
    
    public void testPolymorphicDate() throws Exception{
    	try{
	    	GorillaInterface proxy = getProxy();
	    	GregorianCalendar cal = new GregorianCalendar(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH);
	    	DatatypeFactory typeFactory = DatatypeFactory.newInstance();
	    	XMLGregorianCalendar request =  typeFactory.newXMLGregorianCalendar(cal);
	        proxy.echoPolymorphicDate(request);
	    	
    	}catch(Exception e){
    		e.printStackTrace();
    		fail("Exception in testEchoDate :"+e);
    	}
    }
    
    private boolean compareLists(List in, List out) {
        if (in.size() != out.size()) {
            TestLogger.logger.debug("Size mismatch " + in.size() + "!=" + out.size());
            return false;
        }
        for (int i=0; i<in.size(); i++) {
            Object inItem = in.get(i);
            Object outItem = out.get(i);
            if (inItem != null && !inItem.equals(outItem) ||
                (inItem == null && inItem != outItem)) {
                TestLogger.logger.debug("Item " + i + " mismatch " + inItem + "!=" + outItem);
                return false;
            }
                
        }
        return true;
    }
    
    private boolean compareArrays(String[] in, String[] out) {
        if (in.length != out.length) {
            TestLogger.logger.debug("Size mismatch " + in.length + "!=" + out.length);
            return false;
        }
        for (int i=0; i<in.length; i++) {
            Object inItem = in[i];
            Object outItem = out[i];
            if (inItem != null && !inItem.equals(outItem) ||
                (inItem == null && inItem != outItem)) {
                TestLogger.logger.debug("Item " + i + " mismatch " + inItem + "!=" + outItem);
                return false;
            }
                
        }
        return true;
    }
}