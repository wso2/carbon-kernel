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

import junit.framework.TestCase;
import java.util.List;
import org.apache.axis2.jaxws.utility.JavaUtils;

/**
 * Tests Namespace to Package Algorithm
 *
 */
public class NS2PkgTest extends TestCase {

    public void test01() throws Exception {
        String ns1 = "http://example.org/NewBusiness/";
        String expectedPkg1 = "org.example.newbusiness";
        
        // Test legacy utility method
        String packageString = JavaUtils.getPackageFromNamespace(ns1);
        assertTrue(expectedPkg1.equals(packageString));
        
        // Test new utility method
        List pkgs = JavaUtils.getPackagesFromNamespace(ns1);
        assertTrue(pkgs.size() == 1);
        assertTrue(expectedPkg1.equals(pkgs.get(0)));
        
        String ns2 = "http://interface.org/NewBusiness/";
        String expectedPkg2_jaxb = "org.interface_.newbusiness";
        String expectedPkg2_other = "org._interface.newbusiness";
        
        // Test new utility for a case where 2 packages will be returned.
        pkgs = JavaUtils.getPackagesFromNamespace(ns2);
        assertTrue(pkgs.size() == 2);
        assertTrue(expectedPkg2_jaxb.equals(pkgs.get(0)));
        assertTrue(expectedPkg2_other.equals(pkgs.get(1)));
    }
    
    public void test02() throws Exception {
        String ns1 = "urn://example-org/NewBusiness";
        String expectedPkg1 = "org.example.newbusiness";
        
        String pkg = JavaUtils.getPackageFromNamespace(ns1);
        assertTrue("Expected " + expectedPkg1 + "Received " +pkg, expectedPkg1.equals(pkg));
    }
    
    public void test03() throws Exception {
        String ns1 = "";
        String expectedPkg1 = "";
        
        String pkg = JavaUtils.getPackageFromNamespace(ns1);
        assertTrue("Expected " + expectedPkg1 + "Received " +pkg, expectedPkg1.equals(pkg));
    }
    public void test04() throws Exception {
        
        // In this cases the namespace is not a valid URL (urn) and
        // it has some additional trickiness (v1.0, camel case and java keyword).
        String ns2 = "urn:acme/interface/InsuranceReport/Detail/v1.0";
        String expectedPkg2_jaxb = "acme.interface_.insurancereport.detail.v1";
        String expectedPkg2_other = "acme._interface.insurancereport.detail.v1";
        
        // Test new utility for a case where 2 packages will be returned.
        List pkgs = JavaUtils.getPackagesFromNamespace(ns2);
        assertTrue(pkgs.size() == 2);
        assertTrue(expectedPkg2_jaxb.equals(pkgs.get(0)));
        assertTrue(expectedPkg2_other.equals(pkgs.get(1)));
    }
}
