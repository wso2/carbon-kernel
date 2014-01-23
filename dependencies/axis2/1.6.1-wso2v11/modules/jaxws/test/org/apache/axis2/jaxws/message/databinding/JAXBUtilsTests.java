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
package org.apache.axis2.jaxws.message.databinding;

import org.apache.axis2.jaxws.message.databinding.JAXBUtils.CONSTRUCTION_TYPE;
import org.apache.ws.jaxb.a.BadData1;
import org.apache.ws.jaxb.a.BadData2;
import org.apache.ws.jaxb.a.Data1;
import org.apache.ws.jaxb.a.Data2;
import org.apache.ws.jaxb.a.Data3;
import org.apache.ws.jaxb.b.BadData3;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.Holder;

import java.util.TreeSet;

import junit.framework.TestCase;

/**
 * Test JAXBUtils functionality
 */
public class JAXBUtilsTests extends TestCase {
    
    /**
     * We have encountered situations where users have intermingled
     * JAXB and non-JAXB classes in the same package.  This practice is
     * strongly discouraged; however it can happen.  
     * The JAXBUtils code (actually JAXBContextFromClasses) contains 
     * an algorithm to try and find the minimal set of valid classes 
     * in these cases.  
     * 
     * This test validates the code.  Several good JAXB classes
     * (all named Data*) are intermingled in packaes with non-JAXB classes
     * (all named BadData*).  This test passes if the JAXBContext contains
     * the Data classes and excludes the BadData classes.
     * @throws Exception
     */
    public void testMixedPackages() throws Exception {
        
        // Create a JAXBContext
        TreeSet<String> contextPackages = new TreeSet<String>();
        contextPackages.add("org.apache.ws.jaxb.a");
        contextPackages.add("org.apache.ws.jaxb.b");
        Holder<CONSTRUCTION_TYPE>constructionType = new Holder<CONSTRUCTION_TYPE>();
        
        JAXBContext jbc = JAXBUtils.getJAXBContext(contextPackages, constructionType, 
                                                   contextPackages.toString());
        
        // The toString method lists all of the contained classes.
        String jbcString = jbc.toString();
        
        // Make sure the good Data is in the JAXBContext
        assertTrue(jbcString.indexOf(Data1.class.getName()) > 0);
        assertTrue(jbcString.indexOf(Data2.class.getName()) > 0);
        assertTrue(jbcString.indexOf(Data3.class.getName()) > 0);
        
        // Make sure the bad Data is not in the JAXBContext
        assertTrue(jbcString.indexOf(BadData1.class.getName()) < 0);
        assertTrue(jbcString.indexOf(BadData2.class.getName()) < 0);
        assertTrue(jbcString.indexOf(BadData3.class.getName()) < 0);
    }
}
