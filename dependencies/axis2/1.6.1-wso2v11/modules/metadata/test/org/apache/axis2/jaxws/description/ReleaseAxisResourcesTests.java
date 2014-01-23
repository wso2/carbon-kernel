/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;
import org.apache.axis2.jaxws.description.impl.DescriptionFactoryImpl;

import javax.jws.WebService;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

/**
 * Test that with the configuration property set in axis2.xml, the AxisService
 * resources are released after they are no longer needed.  The configuation
 * value in axis2.xml is:
 *     <parameter name="reduceWSDLMemoryCache">true</parameter>
 * 
 * NOTE: the tests currently use the axis2.xml from modules/kernel/conf (see
 * build.xml).  There is an axis2.xml in the test-resource directory which
 * enables the necessary property.  This axis2.xml is used by explicitly creating a
 * configuration context to point to it.
 * 
 * ALSO NOTE: The default behavior should be OFF!  When the resources are cleaned from
 * the AxisService, ?xsd will not work.
 * 
 * This test is based heavily on modules/metadata/test/org/apache/axis2/jaxws/description/DBCwithReduceWSDLMemoryParmsTests.java
 */
public class ReleaseAxisResourcesTests extends TestCase {
    private static String basedir;
    
    static {
        basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = new File(".").getAbsolutePath();
        }
    }

    /**
     * Test that the default behavior is to NOT clenup AxisService resources.  The axis2.xml
     * file used will be the default one, in target/repository/conf.  The propery should not
     * be set in that file.
     */
    public void testServerSideRelease_Default_OFF() {

        // Since we aren't expliclty creating a ConfigurationContext using a differetn axis2.xml,
        // the file target/repository/conf/axis2.xml will be used.  This one
        // will not have the property set which causes AxisService resources to be cleaned up.
        
        String wsdlRelativeLocation = "test-resources/wsdl/";
        String wsdlFileName = "BindingNamespace.wsdl";
        String targetNamespace = "http://nonanonymous.complextype.test.org";
        String wsdlLocation = wsdlRelativeLocation + wsdlFileName;

        // Build up a DBC, including the WSDL Definition and the annotation information for 
        // the impl class.
        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite(/*configContext*/);

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
        dbc.setwsdlURL(wsdlURL);

        HashMap<String, DescriptionBuilderComposite> dbcMap =
                new HashMap<String, DescriptionBuilderComposite>();
        dbcMap.put(dbc.getClassName(), dbc);

        List<ServiceDescription> serviceDescList =
                DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap /*, configContext*/);
        assertEquals(1, serviceDescList.size());
        ServiceDescription serviceDesc = (ServiceDescription) serviceDescList.get(0);

        assertNotNull(serviceDesc);
        EndpointDescription endpointDesc = serviceDesc.getEndpointDescriptions()[0];
        assertNotNull(endpointDesc);
        AxisService axisService = endpointDesc.getAxisService();
        assertNotNull(axisService);

        assertNotNull(axisService.getSchema());
        assertTrue(axisService.getSchema().size() > 0);
    }
    
    /**
     * Tests that when the DescriptionBuilderComposite sets the 'Reduce WSDL Cache' property
     * to false that the Axis resources are not cleaned up.
     */
    public void testServerSideRelease_Explicit_OFF() {

        
        String wsdlRelativeLocation = "test-resources/wsdl/";
        String wsdlFileName = "BindingNamespace.wsdl";
        String targetNamespace = "http://nonanonymous.complextype.test.org";
        String wsdlLocation = wsdlRelativeLocation + wsdlFileName;

        // Build up a DBC, including the WSDL Definition and the annotation information for 
        // the impl class.
        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();
        dbc.getProperties().put(Constants.Configuration.REDUCE_WSDL_MEMORY_CACHE, false);

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
        dbc.setwsdlURL(wsdlURL);

        HashMap<String, DescriptionBuilderComposite> dbcMap =
                new HashMap<String, DescriptionBuilderComposite>();
        dbcMap.put(dbc.getClassName(), dbc);

        List<ServiceDescription> serviceDescList =
                DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap /*, configContext*/);
        assertEquals(1, serviceDescList.size());
        ServiceDescription serviceDesc = (ServiceDescription) serviceDescList.get(0);

        assertNotNull(serviceDesc);
        EndpointDescription endpointDesc = serviceDesc.getEndpointDescriptions()[0];
        assertNotNull(endpointDesc);
        AxisService axisService = endpointDesc.getAxisService();
        assertNotNull(axisService);

        assertNotNull(axisService.getSchema());
        assertTrue(axisService.getSchema().size() > 0);
    }
    
    public void testServerSideRelease() {
        String axis2xml = basedir + "/test-resources/axis2.xml";
        ConfigurationContext configContext = null;
        try {
            configContext =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(null,
                                                                                         axis2xml);
        } catch (AxisFault e) {
            fail("Got fault trying to create config context" + e);
        }
        assertNotNull(configContext);
        String param =
                (String) configContext.getAxisConfiguration()
                                      .getParameterValue("reduceWSDLMemoryCache");
        assertNotNull(param);

        String wsdlRelativeLocation = "test-resources/wsdl/";
        String wsdlFileName = "BindingNamespace.wsdl";
        String targetNamespace = "http://nonanonymous.complextype.test.org";
        String wsdlLocation = wsdlRelativeLocation + wsdlFileName;

        // Build up a DBC, including the WSDL Definition and the annotation information for 
        // the impl class.
        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite(configContext);

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
        dbc.setwsdlURL(wsdlURL);

        HashMap<String, DescriptionBuilderComposite> dbcMap =
                new HashMap<String, DescriptionBuilderComposite>();
        dbcMap.put(dbc.getClassName(), dbc);
        List<ServiceDescription> serviceDescList =
                DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap, configContext);
        assertEquals(1, serviceDescList.size());
        ServiceDescription serviceDesc = (ServiceDescription) serviceDescList.get(0);

        assertNotNull(serviceDesc);
        EndpointDescription endpointDesc = serviceDesc.getEndpointDescriptions()[0];
        assertNotNull(endpointDesc);
        AxisService axisService = endpointDesc.getAxisService();
        assertNotNull(axisService);

        assertNotNull(axisService.getSchema());
        assertTrue(axisService.getSchema().size() == 0);
    }
    
    /**
     * This tests that when the 'Reduce WSDL cache' property is set on the DBC that
     * the Axis resources are cleaned up.
     */
    public void testServerSideReleaseDBC() {

        String wsdlRelativeLocation = "test-resources/wsdl/";
        String wsdlFileName = "BindingNamespace.wsdl";
        String targetNamespace = "http://nonanonymous.complextype.test.org";
        String wsdlLocation = wsdlRelativeLocation + wsdlFileName;

        // Build up a DBC, including the WSDL Definition and the annotation information for 
        // the impl class.
        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();
        
        // should have the same effect as the parameter set to 'true' in the axis2.xml
        dbc.getProperties().put(Constants.Configuration.REDUCE_WSDL_MEMORY_CACHE, true);

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
        dbc.setwsdlURL(wsdlURL);

        HashMap<String, DescriptionBuilderComposite> dbcMap =
                new HashMap<String, DescriptionBuilderComposite>();
        dbcMap.put(dbc.getClassName(), dbc);
        List<ServiceDescription> serviceDescList =
                DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
        assertEquals(1, serviceDescList.size());
        ServiceDescription serviceDesc = (ServiceDescription) serviceDescList.get(0);

        assertNotNull(serviceDesc);
        EndpointDescription endpointDesc = serviceDesc.getEndpointDescriptions()[0];
        assertNotNull(endpointDesc);
        AxisService axisService = endpointDesc.getAxisService();
        assertNotNull(axisService);

        assertNotNull(axisService.getSchema());
        assertTrue(axisService.getSchema().size() == 0);
    }
    
    @WebService(serviceName = "EchoMessageService", portName = "EchoMessagePort", targetNamespace = "http://nonanonymous.complextype.test.org", wsdlLocation = "test-resources/wsdl/BindingNamespace.wsdl")
    public class BindingNSImpl {
        public String echoMessage(String arg) {
            return arg;
        }
    }
}