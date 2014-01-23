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

package org.apache.axis2.jaxws.description;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;

import javax.jws.WebService;
import javax.wsdl.Definition;
import javax.xml.ws.WebServiceException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/** Tests validation against the WSDL for invalid configurations */
public class ValidateWSDLTests extends TestCase {

    /** The SEI used by the service impl does not contain all the methods defined on the PortType */
    public void testValidatePortType() {
        String wsdlRelativeLocation =
                System.getProperty("basedir", ".") + "/" + "test-resources/wsdl/";
        String wsdlFileName = "ValidateWSDL1.wsdl";

        String targetNamespace = "http://serverPartial1.checkexception.webfault.annotations/";
        String wsdlLocation = wsdlRelativeLocation + wsdlFileName;

        // Build up a DBC, including the WSDL Definition and the annotation information for the impl class.
        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();

        URL wsdlURL = DescriptionTestUtils.getWSDLURL(wsdlFileName);
        Definition wsdlDefn = DescriptionTestUtils.createWSDLDefinition(wsdlURL);
        assertNotNull(wsdlDefn);

        WebServiceAnnot webServiceAnnot = WebServiceAnnot.createWebServiceAnnotImpl();
        assertNotNull(webServiceAnnot);
        webServiceAnnot.setWsdlLocation(wsdlLocation);
        webServiceAnnot.setTargetNamespace(targetNamespace);

        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        mdc.setMethodName("addTwoNumbers");
        mdc.setReturnType("int");

        ParameterDescriptionComposite pdc1 = new ParameterDescriptionComposite();
        pdc1.setParameterType("int");
        ParameterDescriptionComposite pdc2 = new ParameterDescriptionComposite();
        pdc1.setParameterType("int");

        mdc.addParameterDescriptionComposite(pdc1);
        mdc.addParameterDescriptionComposite(pdc2);

        dbc.addMethodDescriptionComposite(mdc);
        dbc.setWebServiceAnnot(webServiceAnnot);
        dbc.setClassName(ValidateWSDLImpl1.class.getName());
        dbc.setWsdlDefinition(wsdlDefn);
        dbc.setwsdlURL(wsdlURL);

        HashMap<String, DescriptionBuilderComposite> dbcMap =
                new HashMap<String, DescriptionBuilderComposite>();
        dbcMap.put(ValidateWSDLImpl1.class.getName(), dbc);

        try {
            List<ServiceDescription> serviceDescList =
                    DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
            
            //Removing the fail() call here as we removed the Valdiation Error from
            //EndpointInterfaceDescriptionValidator.
            
            //Expected code path
            
        }
        catch (WebServiceException e) {
            // Expected code path
        }
    }

    /**
     * Reference a Port (which does not exist) under a Service that does.  This is not a valid
     * partial WSDL case and so is an error.
     */
    public void testValidatePort() {
        String wsdlRelativeLocation = "test-resources/wsdl/";
        String wsdlFileName = "ValidateWSDL1.wsdl";

        String targetNamespace = "http://serverPartial1.checkexception.webfault.annotations/";
        String wsdlLocation = wsdlRelativeLocation + wsdlFileName;

        // Build up a DBC, including the WSDL Definition and the annotation information for the impl class.
        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();

        URL wsdlURL = DescriptionTestUtils.getWSDLURL(wsdlFileName);
        Definition wsdlDefn = DescriptionTestUtils.createWSDLDefinition(wsdlURL);
        assertNotNull(wsdlDefn);

        WebServiceAnnot webServiceAnnot = WebServiceAnnot.createWebServiceAnnotImpl();
        assertNotNull(webServiceAnnot);
        webServiceAnnot.setWsdlLocation(wsdlLocation);
        webServiceAnnot.setTargetNamespace(targetNamespace);
        webServiceAnnot.setServiceName("ValidateWSDLImpl1ServiceInvalidPort");

        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        mdc.setMethodName("addTwoNumbers");
        mdc.setReturnType("int");

        ParameterDescriptionComposite pdc1 = new ParameterDescriptionComposite();
        pdc1.setParameterType("int");
        ParameterDescriptionComposite pdc2 = new ParameterDescriptionComposite();
        pdc1.setParameterType("int");

        mdc.addParameterDescriptionComposite(pdc1);
        mdc.addParameterDescriptionComposite(pdc2);

        dbc.addMethodDescriptionComposite(mdc);
        dbc.setWebServiceAnnot(webServiceAnnot);
        dbc.setClassName(ValidateWSDLImpl2.class.getName());
        dbc.setWsdlDefinition(wsdlDefn);
        dbc.setwsdlURL(wsdlURL);

        HashMap<String, DescriptionBuilderComposite> dbcMap =
                new HashMap<String, DescriptionBuilderComposite>();
        dbcMap.put(ValidateWSDLImpl1.class.getName(), dbc);

        try {
            List<ServiceDescription> serviceDescList =
                    DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
            fail();
        }
        catch (WebServiceException e) {
            // Expected code path
        }
    }
}

@WebService(wsdlLocation = "test-resources/wsdl/ValidateWSDL2.wsdl",
            targetNamespace = "http://serverPartial1.checkexception.webfault.annotations/")
class ValidateWSDLImpl1 {
    public int addTwoNumbers(int number1, int number2) {
        return number1 + number2;
    }
}

@WebService(serviceName = "ValidateWSDLImpl1ServiceInvalidPort",
            wsdlLocation = "test-resources/wsdl/ValidateWSDL2.wsdl",
            targetNamespace = "http://serverPartial1.checkexception.webfault.annotations/")
class ValidateWSDLImpl2 {
    public int addTwoNumbers(int number1, int number2) {
        return number1 + number2;
    }
}
