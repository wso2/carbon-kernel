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

/**
 * 
 */
package org.apache.axis2.jaxws.misc;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.TreeSet;

/**
 * Tests Namespace to Package Algorithm
 *
 */
public class JAXBContextTest extends AbstractTestCase {

    public static Test suite() {
        return getTestSetup(new TestSuite(JAXBContextTest.class));
    }

    /**
     * Test basic functionality of JAXBUtils pooling
     * @throws Exception
     */
    public void test01() throws JAXBException {
    	// Get a JAXBContext
    	TreeSet<String> context1 = new TreeSet<String>();
    	context1.add("org.test.addnumbers");
    	context1.add("org.test.anytype");

    	JAXBContext jaxbContext1 = JAXBUtils.getJAXBContext(context1);

    	// Assert that the JAXBContext was found and the context contains the two valid packages
    	assertTrue(jaxbContext1 != null);
    	assertTrue(context1.contains("org.test.addnumbers"));
    	assertTrue(context1.contains("org.test.anytype"));
    	String context1String = jaxbContext1.toString();
        assertTrue(context1String.contains("org.test.addnumbers"));
        assertTrue(context1String.contains("org.test.anytype")); 

    	// Repeat with the same packages
    	TreeSet<String> context2 = new TreeSet<String>();
    	context2.add("org.test.addnumbers");
    	context2.add("org.test.anytype");

    	JAXBContext jaxbContext2 = JAXBUtils.getJAXBContext(context2);

    	// The following assertion is probably true,but GC may have wiped out the weak reference
    	//assertTrue(jaxbContext2 == jaxbContext1);
    	assertTrue(jaxbContext2 != null);
    	assertTrue(jaxbContext2.toString().equals(jaxbContext1.toString()));
    	assertTrue(context2.contains("org.test.addnumbers"));
    	assertTrue(context2.contains("org.test.anytype"));
    	String context2String = jaxbContext2.toString();
        assertTrue(context2String.contains("org.test.addnumbers"));
        assertTrue(context2String.contains("org.test.anytype")); 
        

    	// Repeat with the same packages + an invalid package
    	TreeSet<String> context3 = new TreeSet<String>();
    	context3.add("org.test.addnumbers");
    	context3.add("org.test.anytype");
    	context3.add("my.grandma.loves.jaxws");

    	JAXBContext jaxbContext3 = JAXBUtils.getJAXBContext(context3);

    	// The following assertion is probably true,but GC may have wiped out the weak reference
    	//assertTrue(jaxbContext3 == jaxbContext1);
    	assertTrue(jaxbContext3 != null);

    	assertTrue(context3.contains("org.test.addnumbers"));
    	assertTrue(context3.contains("org.test.anytype")); 
    	assertTrue(context3.contains("my.grandma.loves.jaxws")); 
    	String context3String = jaxbContext3.toString();
    	assertTrue(context3String.contains("org.test.addnumbers"));
        assertTrue(context3String.contains("org.test.anytype")); 
        assertTrue(!context3String.contains("my.grandma.loves.jaxws")); 

    	// Repeat with a subset of packages
    	TreeSet<String> context4 = new TreeSet<String>();
    	context4.add("org.test.addnumbers");


    	JAXBContext jaxbContext4 = JAXBUtils.getJAXBContext(context4);

    	assertTrue(jaxbContext4 != null);
    	assertTrue(jaxbContext4 != jaxbContext3);
    	assertTrue(context4.contains("org.test.addnumbers"));
    }
    
    /**
     * Test basic functionality of JAXBUtils pooling
     * @throws Exception
     */
    public void test02() throws JAXBException {
        // Get a JAXBContext
        TreeSet<String> context1 = new TreeSet<String>();
        context1.add("org.test.addnumbers");
        context1.add("org.test.anytype");
        context1.add("org.apache.axis2.jaxws.misc.jaxbexclude");

        JAXBContext jaxbContext1 = JAXBUtils.getJAXBContext(context1);

        // Assert that the JAXBContext was found and the context contains the two valid packages
        assertTrue(jaxbContext1 != null);
        assertTrue(context1.contains("org.test.addnumbers"));
        assertTrue(context1.contains("org.test.anytype"));
        assertTrue(context1.contains("org.apache.axis2.jaxws.misc.jaxbexclude"));
        String context1String = jaxbContext1.toString();
        assertTrue(context1String.contains("org.test.addnumbers"));
        assertTrue(context1String.contains("org.test.anytype")); 
        assertTrue(!context1String.contains("org.apache.axis2.jaxws.misc.jaxbexclude")); 

        // Repeat with the same packages
        TreeSet<String> context2 = new TreeSet<String>();
        context2.add("org.test.addnumbers");
        context2.add("org.test.anytype");
        context2.add("org.apache.axis2.jaxws.misc.jaxbexclude");

        JAXBContext jaxbContext2 = JAXBUtils.getJAXBContext(context2);

        // The following assertion is probably true,but GC may have wiped out the weak reference
        //assertTrue(jaxbContext2 == jaxbContext1);
        assertTrue(jaxbContext2 != null);
        assertTrue(jaxbContext2.toString().equals(jaxbContext1.toString()));
        assertTrue(context2.contains("org.test.addnumbers"));
        assertTrue(context2.contains("org.test.anytype"));
        String context2String = jaxbContext2.toString();
        assertTrue(context2String.contains("org.test.addnumbers"));
        assertTrue(context2String.contains("org.test.anytype")); 
        assertTrue(!context2String.contains("org.apache.axis2.jaxws.misc.jaxbexclude")); 

        // Repeat with the same packages + an invalid package
        TreeSet<String> context3 = new TreeSet<String>();
        context3.add("org.test.addnumbers");
        context3.add("org.test.anytype");
        context3.add("my.grandma.loves.jaxws");
        context3.add("org.apache.axis2.jaxws.misc.jaxbexclude");

        JAXBContext jaxbContext3 = JAXBUtils.getJAXBContext(context3);

        // The following assertion is probably true,but GC may have wiped out the weak reference
        //assertTrue(jaxbContext3 == jaxbContext1);
        assertTrue(jaxbContext3 != null);
        assertTrue(jaxbContext3.toString().equals(jaxbContext3.toString()));
        assertTrue(context3.contains("org.test.addnumbers"));
        assertTrue(context3.contains("org.test.anytype")); 
        assertTrue(context3.contains("my.grandma.loves.jaxws"));  
        String context3String = jaxbContext3.toString();
        assertTrue(context3String.contains("org.test.addnumbers"));
        assertTrue(context3String.contains("org.test.anytype")); 
        assertTrue(!context3String.contains("my.grandma.loves.jaxws")); 
        assertTrue(!context3String.contains("org.apache.axis2.jaxws.misc.jaxbexclude")); 

        // Repeat with a subset of packages
        TreeSet<String> context4 = new TreeSet<String>();
        context4.add("org.test.addnumbers");


        JAXBContext jaxbContext4 = JAXBUtils.getJAXBContext(context4);

        assertTrue(jaxbContext4 != null);
        assertTrue(jaxbContext4 != jaxbContext3);
        assertTrue(context4.contains("org.test.addnumbers"));
    }
    
    public void test03() throws JAXBException {
        // Simulate a web services that references a.Bean2 and b.Bean1
        // Note that both these beans are in the same namespace
        // but not the same package.
        // Also note that there are other colliding classes in the
        // packages.
        TreeSet<String> context = new TreeSet<String>();
        context.add("org.apache.axis2.jaxws.misc.a");
        context.add("org.apache.axis2.jaxws.misc.b");
        context.add("[org.apache.axis2.jaxws.misc.a.Bean2]");
        context.add("[org.apache.axis2.jaxws.misc.b.Bean1]");

         
        JAXBContext jaxbContext = JAXBUtils.getJAXBContext(context); 
       
        // Ensure that the jaxbContext IS produced and contains
        // both a.Bean2 and b.Bean1.
        String jaxbContextString = jaxbContext.toString();
        
        assertTrue("The JAXBContext should contain a.Bean2: " + jaxbContextString,
                jaxbContextString.indexOf("org.apache.axis2.jaxws.misc.a.Bean2") > 0);
        assertTrue("The JAXBContext should contain b.Bean1: " + jaxbContextString,
                jaxbContextString.indexOf("org.apache.axis2.jaxws.misc.b.Bean1") > 0);
        
    }
}