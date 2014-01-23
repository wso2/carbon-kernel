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

package org.apache.axis2.jaxws.description.impl;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;

import javax.xml.ws.WebServiceException;
import java.util.HashMap;

/**
 * 
 */
public class ServiceDescriptionImplValidationTests extends TestCase {

    public void testValidateImplMethodsVsSEI() {

        DescriptionBuilderComposite seiComposite = new DescriptionBuilderComposite();
        seiComposite.setClassName("org.apache.axis2.jaxws.description.impl.MySEI");
        MethodDescriptionComposite seiMDC = new MethodDescriptionComposite();
        seiMDC.setMethodName("seiMethod");
        seiComposite.addMethodDescriptionComposite(seiMDC);
        WebServiceAnnot seiWebServiceAnnot = WebServiceAnnot.createWebServiceAnnotImpl();
        seiWebServiceAnnot.setName(null);
        seiComposite.setWebServiceAnnot(seiWebServiceAnnot);

        DescriptionBuilderComposite implComposite = new DescriptionBuilderComposite();
        implComposite.setClassName("org.apache.axis2.jaxws.description.impl.MyImpl");
        MethodDescriptionComposite implMDC = new MethodDescriptionComposite();
        implMDC.setMethodName("notSeiMethod");
        implComposite.addMethodDescriptionComposite(implMDC);
        WebServiceAnnot webServiceAnnot = WebServiceAnnot.createWebServiceAnnotImpl();
        webServiceAnnot.setName(null);
        webServiceAnnot.setEndpointInterface("org.apache.axis2.jaxws.description.impl.MySEI");
        implComposite.setWebServiceAnnot(webServiceAnnot);
        implComposite.setIsInterface(false);

        HashMap<String, DescriptionBuilderComposite> dbcList =
                new HashMap<String, DescriptionBuilderComposite>();
        dbcList.put(seiComposite.getClassName(), seiComposite);
        dbcList.put(implComposite.getClassName(), implComposite);

        try {
            ServiceDescriptionImpl serviceDescImpl =
                    new ServiceDescriptionImpl(dbcList, implComposite);
            fail("Should have caught exception for validation errors");
        }
        catch (WebServiceException ex) {
            // Expected path
        }
        catch (Exception ex) {
            fail("Caught wrong type of exception " + ex.toString());
        }


    }

    public void testValidateOverloadedImplMethodsVsSEI() {
        
        // Tests that overloaded methods validate correctly.  Note that the methods are 
        // intentionally added to the implementation and sei in a different order so that they
        // don't coincidently pass validation just because they are in the same order in both
        // collections.
        //   seiMethod()
        //   seiMethod(String)
        
        // Build up the SEI
        DescriptionBuilderComposite seiComposite = new DescriptionBuilderComposite();
        seiComposite.setClassName("org.apache.axis2.jaxws.description.impl.MySEI");

        MethodDescriptionComposite seiMDC = new MethodDescriptionComposite();
        seiMDC.setMethodName("seiMethod");
        seiComposite.addMethodDescriptionComposite(seiMDC);
        
        MethodDescriptionComposite seiMDC2 = new MethodDescriptionComposite();
        seiMDC2.setMethodName("seiMethod");
        ParameterDescriptionComposite seiPDC = new ParameterDescriptionComposite();
        seiPDC.setParameterType("java.lang.String");
        seiMDC2.addParameterDescriptionComposite(seiPDC);
        seiComposite.addMethodDescriptionComposite(seiMDC2);
        
        WebServiceAnnot seiWebServiceAnnot = WebServiceAnnot.createWebServiceAnnotImpl();
        seiWebServiceAnnot.setName(null);
        seiComposite.setWebServiceAnnot(seiWebServiceAnnot);

        // Build up the Impl, but put the overloaded MDCs on in a different order than the SEI 
        // so they don't just happen to pass validation
        DescriptionBuilderComposite implComposite = new DescriptionBuilderComposite();
        implComposite.setClassName("org.apache.axis2.jaxws.description.impl.MyImpl");

        MethodDescriptionComposite implMDC = new MethodDescriptionComposite();
        implMDC.setMethodName("seiMethod");
        ParameterDescriptionComposite implPDC = new ParameterDescriptionComposite();
        implPDC.setParameterType("java.lang.String");
        implMDC.addParameterDescriptionComposite(implPDC);
        implComposite.addMethodDescriptionComposite(implMDC);

        MethodDescriptionComposite implMDC2 = new MethodDescriptionComposite();
        implMDC2.setMethodName("seiMethod");
        implComposite.addMethodDescriptionComposite(implMDC2);
        
        WebServiceAnnot webServiceAnnot = WebServiceAnnot.createWebServiceAnnotImpl();
        webServiceAnnot.setName(null);
        webServiceAnnot.setEndpointInterface("org.apache.axis2.jaxws.description.impl.MySEI");
        implComposite.setWebServiceAnnot(webServiceAnnot);
        implComposite.setIsInterface(false);

        HashMap<String, DescriptionBuilderComposite> dbcList =
                new HashMap<String, DescriptionBuilderComposite>();
        dbcList.put(seiComposite.getClassName(), seiComposite);
        dbcList.put(implComposite.getClassName(), implComposite);

        try {
            ServiceDescriptionImpl serviceDescImpl =
                    new ServiceDescriptionImpl(dbcList, implComposite);
        }
        catch (WebServiceException ex) {
            fail("Should NOT have caught exception for validation errors: " + ex);
        }
        catch (Exception ex) {
            fail("Caught wrong type of exception " + ex.toString());
        }


    }

    public void testValidateImplMethodsVsSEIInheritence() {

        DescriptionBuilderComposite superSeiComposite = new DescriptionBuilderComposite();
        superSeiComposite.setClassName("org.apache.axis2.jaxws.description.impl.MySuperSEI");
        MethodDescriptionComposite superSeiMDC = new MethodDescriptionComposite();
        superSeiMDC.setMethodName("superSeiMethod");
        superSeiComposite.addMethodDescriptionComposite(superSeiMDC);

        DescriptionBuilderComposite seiComposite = new DescriptionBuilderComposite();
        seiComposite.setClassName("org.apache.axis2.jaxws.description.impl.MySEI");
        MethodDescriptionComposite seiMDC = new MethodDescriptionComposite();
        seiMDC.setMethodName("seiMethod");
        seiComposite.addMethodDescriptionComposite(seiMDC);
        seiComposite.setSuperClassName("org.apache.axis2.jaxws.description.impl.MySuperSEI");

        DescriptionBuilderComposite implComposite = new DescriptionBuilderComposite();
        implComposite.setClassName("org.apache.axis2.jaxws.description.impl.MyImpl");
        MethodDescriptionComposite implMDC = new MethodDescriptionComposite();
        implMDC.setMethodName("superSeiMethod");
        implComposite.addMethodDescriptionComposite(implMDC);
        WebServiceAnnot webServiceAnnot = WebServiceAnnot.createWebServiceAnnotImpl();
        webServiceAnnot.setName(null);
        webServiceAnnot.setEndpointInterface("org.apache.axis2.jaxws.description.impl.MySEI");
        implComposite.setWebServiceAnnot(webServiceAnnot);
        implComposite.setIsInterface(false);

        HashMap<String, DescriptionBuilderComposite> dbcList =
                new HashMap<String, DescriptionBuilderComposite>();
        dbcList.put(seiComposite.getClassName(), seiComposite);
        dbcList.put(superSeiComposite.getClassName(), superSeiComposite);
        dbcList.put(implComposite.getClassName(), implComposite);

        try {
            ServiceDescriptionImpl serviceDescImpl =
                    new ServiceDescriptionImpl(dbcList, implComposite);
            fail("Should have caught validation failure exception");
        }
        catch (WebServiceException ex) {
            // Expected path
        }
        catch (Exception ex) {
            fail("Caught wrong exception " + ex.toString());
        }
    }

    // The tests below progressively setup an identical DBC, gradually setting more values until
    // the last one does not cause a validation error.

    // FAILURE #1: No WebMethod annotation set on the SEI
    public void testInvalidDBC1() {
        DescriptionBuilderComposite seiComposite = new DescriptionBuilderComposite();
        seiComposite.setClassName("org.apache.axis2.jaxws.description.impl.MySEI");
        MethodDescriptionComposite seiMDC = new MethodDescriptionComposite();
        seiMDC.setMethodName("seiMethod");
        seiComposite.addMethodDescriptionComposite(seiMDC);

        DescriptionBuilderComposite superImplComposite = new DescriptionBuilderComposite();
        superImplComposite.setClassName("org.apache.axis2.jaxws.description.impl.MySuperImpl");
        MethodDescriptionComposite superImplMDC = new MethodDescriptionComposite();
        superImplMDC.setMethodName("seiMethod");
        superImplComposite.addMethodDescriptionComposite(superImplMDC);

        DescriptionBuilderComposite implComposite = new DescriptionBuilderComposite();
        implComposite.setClassName("org.apache.axis2.jaxws.description.impl.MyImpl");
        MethodDescriptionComposite implMDC = new MethodDescriptionComposite();
        implMDC.setMethodName("notSeiMethod");
        implComposite.addMethodDescriptionComposite(implMDC);
        WebServiceAnnot webServiceAnnot = WebServiceAnnot.createWebServiceAnnotImpl();
        webServiceAnnot.setName(null);
        webServiceAnnot.setEndpointInterface("org.apache.axis2.jaxws.description.impl.MySEI");
        implComposite.setWebServiceAnnot(webServiceAnnot);
        implComposite.setIsInterface(false);
        implComposite.setSuperClassName("org.apache.axis2.jaxws.description.impl.MySuperImpl");

        HashMap<String, DescriptionBuilderComposite> dbcList =
                new HashMap<String, DescriptionBuilderComposite>();
        dbcList.put(seiComposite.getClassName(), seiComposite);
        dbcList.put(implComposite.getClassName(), implComposite);
        dbcList.put(superImplComposite.getClassName(), superImplComposite);

        try {
            ServiceDescriptionImpl serviceDescImpl =
                    new ServiceDescriptionImpl(dbcList, implComposite);
            fail("Did not catch expected exception");
        }
        catch (WebServiceException ex) {
            // Expected path
        }
        catch (Exception ex) {
            fail("Unexpected exception received " + ex.toString());
        }
    }
}