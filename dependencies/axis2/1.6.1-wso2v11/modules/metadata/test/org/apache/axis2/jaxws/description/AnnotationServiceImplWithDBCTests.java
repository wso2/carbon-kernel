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
import org.apache.axis2.jaxws.description.builder.WebMethodAnnot;
import org.apache.axis2.jaxws.description.builder.WebParamAnnot;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;
import org.apache.axis2.jaxws.description.builder.converter.JavaClassToDBCConverter;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests the creation of the Description classes based on a service implementation bean and various
 * combinations of annotations
 */
public class AnnotationServiceImplWithDBCTests extends TestCase {
    /**
     * Create the description classes with a service implementation that contains the @WebService
     * JSR-181 annotation which references an SEI.
     */

    //Test creation of a Service Description from DBC, using a basic list.
    //An implicit SEI that extends only java.lang.object
    public void testServiceImplAsImplicitSEI() {
        //org.apache.log4j.BasicConfigurator.configure();

        //Build a Hashmap of DescriptionBuilderComposites that contains the serviceImpl and
        //all necessary associated DBC's possibly including SEI and superclasses
        HashMap<String, DescriptionBuilderComposite> dbcMap =
                new HashMap<String, DescriptionBuilderComposite>();

        DescriptionBuilderComposite dbc = buildDBCNoEndpointInterface();

        dbcMap.put(dbc.getClassName(), dbc);

        // TODO: This test is invalid as is.  It does not specify WSDL, and WSDL generator is not currently available in Open Source
        //       So, an exception is currently being thrown.  It may be the correct fix is to NOT always try to generate WSDL; and only
        //       try to generate it if it is asked for.
        try {
            List<ServiceDescription> serviceDescList =
                    DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
            assertNotNull(serviceDescList.get(0));

            //We know this list contains only one SD, so no need to loop
            EndpointDescription[] endpointDesc = serviceDescList.get(0).getEndpointDescriptions();
            assertNotNull(endpointDesc);
            assertEquals(endpointDesc.length, 1);

            // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
            EndpointInterfaceDescription endpointIntfDesc =
                    endpointDesc[0].getEndpointInterfaceDescription();
            assertNotNull(endpointIntfDesc);

        } catch (WebServiceException e) {
        }

        /*
        * Deprecated -- this used to be the test for
        * EndpointInterfaceDescription.getOperationForJavaMethod
        */
/*
        OperationDescription[] operations = endpointIntfDesc.getOperations();
        
        String[] paramTypes = operations[0].getJavaParameters();
        assertNotNull(paramTypes);
        assertEquals(paramTypes.length, 1);
        assertEquals("javax.xml.ws.Holder", paramTypes[0]);
        
        // Test RequestWrapper annotations
        assertEquals(operations[0].getRequestWrapperLocalName(), "Echo");
        assertEquals(operations[0].getRequestWrapperTargetNamespace(), "http://ws.apache.org/axis2/tests");
        assertEquals(operations[0].getRequestWrapperClassName(), "org.apache.ws.axis2.tests.Echo");
        
        // Test ResponseWrapper annotations
        assertEquals(operations[0].getResponseWrapperLocalName(), "EchoResponse");
        assertEquals(operations[0].getResponseWrapperTargetNamespace(), "http://ws.apache.org/axis2/tests");
        assertEquals(operations[0].getResponseWrapperClassName(), "org.apache.ws.axis2.tests.EchoResponse");
        
        // Test SOAPBinding default; that annotation is not present in the SEI
        // Note that annotation could occur on the operation or the type
        // (although on this SEI it doesn't occur either place).
        assertEquals(SOAPBinding.Style.DOCUMENT, operations[0].getSoapBindingStyle());
        assertEquals(SOAPBinding.Style.DOCUMENT, endpointIntfDesc.getSoapBindingStyle());
*/
    }

    //TODO: Basic Test with EndpointInterface set to something valid, so another DBC must
    // exist in list

    //TODO: Basic Test for Provider

    //TODO: Validation Tests
    // - Setting WS and WSP
    // - Fail just one in List, But allow successful ones to pass

    /*
    * Method to return the endpoint interface description for a given implementation class.
    */

    //private EndpointInterfaceDescription getEndpointInterfaceDesc(Class implementationClass) {
    //    // Use the description factory directly; this will be done within the JAX-WS runtime
    //    return (new EndpointInterfaceDescription()); 
    //	//return testEndpointInterfaceDesc;
    //}

    static public DescriptionBuilderComposite buildDBCNoEndpointInterface() {

        //Create a WebServiceAnnot
        String WSName = "EchoServiceAnnotated";
        String WSTargetNamespace = "http://description.jaxws.axis2.apache.org/";
        String WSServiceName = "EchoServiceName";
        //String WSWsdlLocation = "http://EchoService/wsdl";
        String WSWsdlLocation = "";
        String WSEndpointInterface = "";
        String WSPortName = "EchoServiceAnnotatedPort";

        WebServiceAnnot webServiceAnnot =
                WebServiceAnnot.createWebServiceAnnotImpl(
                        WSName,
                        WSTargetNamespace,
                        WSServiceName,
                        WSWsdlLocation,
                        WSEndpointInterface,
                        WSPortName);

        //Create a WebMethodAnnot
        String operationName = "echoStringMethod";
        String action = "urn:EchoStringMethod";
        boolean exclude = false;

        WebMethodAnnot webMethodAnnot = WebMethodAnnot.createWebMethodAnnotImpl();
        webMethodAnnot.setOperationName(operationName);
        webMethodAnnot.setAction(action);
        webMethodAnnot.setExclude(exclude);

        //Create the WebParamAnnot
        String WPName = "arg0";
        String WPPartName = "sku";
        String WPTargetNamespace = "http://description.jaxws.axis2.apache.org/";
        WebParam.Mode WPMode = WebParam.Mode.IN;
        boolean WPHeader = true;

        WebParamAnnot webParamAnnot = WebParamAnnot.createWebParamAnnotImpl();
        webParamAnnot.setName(WPName);
        webParamAnnot.setPartName(WPPartName);
        webParamAnnot.setMode(WPMode);
        webParamAnnot.setTargetNamespace(WPTargetNamespace);
        webParamAnnot.setHeader(WPHeader);

        //Build up the the DBC and all necessary composites
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType("java.lang.String");
        pdc.setWebParamAnnot(webParamAnnot);

        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        mdc.setWebMethodAnnot(webMethodAnnot);
        mdc.setMethodName(operationName);
        mdc.addParameterDescriptionComposite(pdc, 0);

        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();
        dbc.setClassName("org.apache.axis2.samples.EchoServiceAnnotated");
        dbc.setWebServiceAnnot(webServiceAnnot);
        dbc.addMethodDescriptionComposite(mdc);

        return dbc;
    }
    
    public void testLoadWSDLImpl() {
        String wsdlLocation = getEchoMessageServiceWSDLLocation();

        // Build up a DBC, including the WSDL Definition and the annotation information for 
        // the impl class.
        JavaClassToDBCConverter converter = new JavaClassToDBCConverter(EchoMessageService.class);
        HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
        assertNotNull(dbcMap);
        DescriptionBuilderComposite dbc = dbcMap.get(EchoMessageService.class.getName());
        assertNotNull(dbc);
        dbc.setClassLoader(this.getClass().getClassLoader());

        WebServiceAnnot webServiceAnnot = dbc.getWebServiceAnnot();
        assertNotNull(webServiceAnnot);
        webServiceAnnot.setWsdlLocation(wsdlLocation);
        dbc.setWebServiceAnnot(webServiceAnnot);
        dbcMap.put(EchoMessageService.class.getName(), dbc);
        
        List<ServiceDescription> serviceDescList =
            DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
        assertEquals(1, serviceDescList.size());
        ServiceDescription sd = serviceDescList.get(0);
        assertNotNull(sd);
        
        // make sure the WSDL definition was read in from the appropriate location
        assertNotNull(((ServiceDescriptionWSDL) sd).getWSDLDefinition());
    }
    
    public void testLoadWSDLSEI() {
        String wsdlLocation = getEchoMessageServiceWSDLLocation();

        // Build up a DBC, including the WSDL Definition and the annotation information for 
        // the impl class.
        JavaClassToDBCConverter converter = new JavaClassToDBCConverter(EchoMessageServiceSEI.class);
        HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
        assertNotNull(dbcMap);
        DescriptionBuilderComposite dbc = dbcMap.get(EchoMessageServiceSEI.class.getName());
        assertNotNull(dbc);
        DescriptionBuilderComposite seiDBC = dbcMap.get(EchoMessageServiceInterface.class.getName());
        assertNotNull(seiDBC);
        dbc.setClassLoader(this.getClass().getClassLoader());

        WebServiceAnnot webServiceAnnot = seiDBC.getWebServiceAnnot();
        assertNotNull(webServiceAnnot);
        webServiceAnnot.setWsdlLocation(wsdlLocation);
        seiDBC.setWebServiceAnnot(webServiceAnnot);
        dbcMap.put(EchoMessageServiceInterface.class.getName(), seiDBC);
        
        List<ServiceDescription> serviceDescList =
            DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
        assertEquals(1, serviceDescList.size());
        ServiceDescription sd = serviceDescList.get(0);
        assertNotNull(sd);
        
        // make sure the WSDL definition was read in from the appropriate location
        assertNotNull(((ServiceDescriptionWSDL) sd).getWSDLDefinition());
    }
    
    /**
     * This is intended to verify  that properties set on a DescriptionBuilderComposite
     * instance are carried over to the EndpointDescription object that is created 
     * from the DBC.
     */
    public void testSetProperties() {
        
        //Build a Hashmap of DescriptionBuilderComposites that contains the serviceImpl and
        //all necessary associated DBC's possibly including SEI and superclasses
        HashMap<String, DescriptionBuilderComposite> dbcMap =
                new HashMap<String, DescriptionBuilderComposite>();

        DescriptionBuilderComposite dbc = buildDBCNoEndpointInterface();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("prop1", "prop1");
        props.put("prop2", "prop2");
        dbc.setProperties(props);

        dbcMap.put(dbc.getClassName(), dbc);
        try {
            List<ServiceDescription> serviceDescList =
                DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
            assertNotNull(serviceDescList.get(0));

            //We know this list contains only one SD, so no need to loop
            EndpointDescription[] endpointDesc = serviceDescList.get(0).getEndpointDescriptions();
            assertNotNull(endpointDesc);
            assertEquals(endpointDesc.length, 1);
            
            // get the EndpointDescription so we can make sure the properties were
            // propogated to the underlying Axis runtime objects
            EndpointDescription ed = endpointDesc[0];          
            // make sure we find the two properties that were set on the DBC
            assertNotNull(ed.getProperty("prop1"));
            assertTrue(ed.getProperty("prop1").equals("prop1"));
            assertNotNull(ed.getProperty("prop2"));
            assertTrue(ed.getProperty("prop2").equals("prop2"));
            
        }
        catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
    }
    
    private String getEchoMessageServiceWSDLLocation() {
    	String loc = null;
    	String sep = java.io.File.separator;
        loc = sep + "test-resources" + sep + "wsdl" + sep + "EchoMessageService.wsdl";
        try {
        	String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
            loc = baseDir + loc;
        }
        catch(IOException ioe) {
        	ioe.printStackTrace();
        }
    	return loc;
    }
    
    @WebService(serviceName = "EchoMessageService", portName = "EchoMessagePort", targetNamespace = "http://nonanonymous.complextype.test.org", wsdlLocation = "test-resources/wsdl/EchoMessageService.wsdl")
    public class EchoMessageService {
        public String echoMessage(String arg) {
            return arg;
        }
    }
    
    @WebService(serviceName = "EchoMessageService", endpointInterface=
    		"org.apache.axis2.jaxws.description.AnnotationServiceImplWithDBCTests$EchoMessageServiceInterface")
    public class EchoMessageServiceSEI {
        public String echoMessage(String arg) {
            return arg;
        }
    }
    
    @WebService(portName = "EchoMessagePort", targetNamespace = "http://nonanonymous.complextype.test.org", wsdlLocation = "test-resources/wsdl/EchoMessageService.wsdl")
    public interface EchoMessageServiceInterface {
        public String echoMessage(String arg);
    }
}



