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
import java.net.URL;
import java.util.HashMap;
import java.util.List;


/**
 * These tests are intended to test various aspects of the OperationDescription.
 */

public class OperationDescriptionTests extends TestCase {

    /**
     * This test will confirm that the getBindingInputNamespace and getBindingOutputNamespace
     * methods of the OperationDescription function correctly.
     *
     */

    public void testBindingNamespace() {
        String wsdlRelativeLocation = "test-resources/wsdl/";
        String wsdlFileName = "BindingNamespace.wsdl";
        String targetNamespace = "http://nonanonymous.complextype.test.org";
        String wsdlLocation = wsdlRelativeLocation + wsdlFileName;

        // Build up a DBC, including the WSDL Definition and the annotation information for 
        // the impl class.
        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();

        URL wsdlURL = DescriptionTestUtils.getWSDLURL(wsdlFileName);
        Definition wsdlDefn = DescriptionTestUtils.createWSDLDefinition(wsdlURL);
        assertNotNull(wsdlDefn);

        WebServiceAnnot webServiceAnnot = WebServiceAnnot.createWebServiceAnnotImpl();
        assertNotNull(webServiceAnnot);
        webServiceAnnot.setWsdlLocation(wsdlLocation);
        webServiceAnnot.setTargetNamespace(targetNamespace);
        webServiceAnnot.setServiceName("EchoMessageService");
        webServiceAnnot.setPortName("EchoMessagePort");

        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        mdc.setMethodName("echoMessage");
        mdc.setReturnType("java.lang.String");

        ParameterDescriptionComposite pdc1 = new ParameterDescriptionComposite();
        pdc1.setParameterType("java.lang.String");

        mdc.addParameterDescriptionComposite(pdc1);

        dbc.addMethodDescriptionComposite(mdc);
        dbc.setWebServiceAnnot(webServiceAnnot);
        dbc.setClassName(BindingNSImpl.class.getName());
        dbc.setWsdlDefinition(wsdlDefn);
        dbc.setwsdlURL(wsdlURL);

        HashMap<String, DescriptionBuilderComposite> dbcMap =
                new HashMap<String, DescriptionBuilderComposite>();
        dbcMap.put(dbc.getClassName(), dbc);
        List<ServiceDescription> serviceDescList =
                DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
        assertEquals(1, serviceDescList.size());
        ServiceDescription sd = serviceDescList.get(0);
        assertNotNull(sd);

        EndpointDescription[] edArray = sd.getEndpointDescriptions();
        assertNotNull(edArray);
        assertEquals(1, edArray.length);
        EndpointDescription ed = edArray[0];
        assertNotNull(ed);

        EndpointInterfaceDescription eid = ed.getEndpointInterfaceDescription();
        assertNotNull(eid);

        OperationDescription[] odArray = eid.getOperations();
        assertNotNull(odArray);
        assertEquals(1, odArray.length);
        OperationDescription od = odArray[0];
        assertNotNull(od);
        assertEquals("http://org.apache.binding.ns", od.getBindingInputNamespace());
        assertEquals("http://org.apache.binding.ns", od.getBindingOutputNamespace());

    }


    public void testBindingNamespaceDefaults() {
        String wsdlRelativeLocation = "test-resources/wsdl/";
        String wsdlFileName = "BindingNamespaceDefaults.wsdl";
        String targetNamespace = "http://nonanonymous.complextype.test.org";
        String wsdlLocation = wsdlRelativeLocation + wsdlFileName;

        // Build up a DBC, including the WSDL Definition and the annotation information for 
        // the impl class.
        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();

        URL wsdlURL = DescriptionTestUtils.getWSDLURL(wsdlFileName);
        Definition wsdlDefn = DescriptionTestUtils.createWSDLDefinition(wsdlURL);
        assertNotNull(wsdlDefn);

        WebServiceAnnot webServiceAnnot = WebServiceAnnot.createWebServiceAnnotImpl();
        assertNotNull(webServiceAnnot);
        webServiceAnnot.setWsdlLocation(wsdlLocation);
        webServiceAnnot.setTargetNamespace(targetNamespace);
        webServiceAnnot.setServiceName("EchoMessageService");
        webServiceAnnot.setPortName("EchoMessagePort");

        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        mdc.setMethodName("echoMessage");
        mdc.setReturnType("java.lang.String");

        ParameterDescriptionComposite pdc1 = new ParameterDescriptionComposite();
        pdc1.setParameterType("java.lang.String");

        mdc.addParameterDescriptionComposite(pdc1);

        dbc.addMethodDescriptionComposite(mdc);
        dbc.setWebServiceAnnot(webServiceAnnot);
        dbc.setClassName(BindingNSImpl.class.getName());
        dbc.setWsdlDefinition(wsdlDefn);
        dbc.setwsdlURL(wsdlURL);
        HashMap<String, DescriptionBuilderComposite> dbcMap =
                new HashMap<String, DescriptionBuilderComposite>();
        dbcMap.put(dbc.getClassName(), dbc);
        List<ServiceDescription> serviceDescList =
                DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
        assertEquals(1, serviceDescList.size());
        ServiceDescription sd = serviceDescList.get(0);
        assertNotNull(sd);

        EndpointDescription[] edArray = sd.getEndpointDescriptions();
        assertNotNull(edArray);
        assertEquals(1, edArray.length);
        EndpointDescription ed = edArray[0];
        assertNotNull(ed);

        EndpointInterfaceDescription eid = ed.getEndpointInterfaceDescription();
        assertNotNull(eid);

        OperationDescription[] odArray = eid.getOperations();
        assertNotNull(odArray);
        assertEquals(1, odArray.length);
        OperationDescription od = odArray[0];
        assertNotNull(od);
        assertEquals("http://nonanonymous.complextype.test.org", od.getBindingInputNamespace());
        assertEquals("http://nonanonymous.complextype.test.org", od.getBindingOutputNamespace());

    }


    @WebService(serviceName = "EchoMessageService", portName = "EchoMessagePort", targetNamespace = "http://nonanonymous.complextype.test.org", wsdlLocation = "test-resources/wsdl/BindingNamespace.wsdl")
    public class BindingNSImpl {
        public String echoMessage(String arg) {
            return arg;
        }
    }


    @WebService(serviceName = "EchoMessageService", portName = "EchoMessagePort", targetNamespace = "http://nonanonymous.complextype.test.org", wsdlLocation = "test-resources/wsdl/BindingNamespaceDefaults.wsdl")
    public class BindingNSDefaultsImpl {
        public String echoMessage(String arg) {
            return arg;
        }
    }

}
