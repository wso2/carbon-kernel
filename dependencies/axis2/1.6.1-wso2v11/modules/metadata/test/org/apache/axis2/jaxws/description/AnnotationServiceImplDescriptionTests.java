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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.echo.EchoServiceImplWithSEI;
import org.apache.axis2.jaxws.description.impl.DescriptionFactoryImpl;
import org.apache.axis2.jaxws.description.impl.EndpointDescriptionImpl;
import org.apache.axis2.jaxws.description.impl.EndpointInterfaceDescriptionImpl;
import org.apache.axis2.jaxws.description.impl.LegacyMethodRetrieverImpl;
import org.apache.axis2.jaxws.util.WSToolingUtils;
import org.apache.log4j.BasicConfigurator;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;

import java.io.IOException;
import java.util.Iterator;

/**
 * Tests the creation of the Description classes based on a service implementation bean and various
 * combinations of annotations
 */
public class AnnotationServiceImplDescriptionTests extends TestCase {
    static {
        // Note you will probably need to increase the java heap size, for example
        // -Xmx512m.  This can be done by setting maven.junit.jvmargs in project.properties.
        // To change the settings, edit the log4j.property file
        // in the test-resources directory.
        BasicConfigurator.configure();
    }

    /**
     * Create the description classes with a service implementation that contains the @WebService
     * JSR-181 annotation which references an SEI.
     */
    public void testServiceImplWithSEI() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc =
                DescriptionFactory.createServiceDescription(EchoServiceImplWithSEI.class);
        assertNotNull(serviceDesc);

        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(endpointDesc.length, 1);

        assertNotNull(endpointDesc[0].getAxisService());

        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointInterfaceDescription endpointIntfDesc =
                endpointDesc[0].getEndpointInterfaceDescription();
        assertNotNull(endpointIntfDesc);

        OperationDescription[] operations =
                endpointIntfDesc.getOperationForJavaMethod("badMethodName");
        assertNull(operations);
        operations = endpointIntfDesc.getOperationForJavaMethod("");
        assertNull(operations);
        operations = endpointIntfDesc.getOperationForJavaMethod((String)null);
        assertNull(operations);
        operations = endpointIntfDesc.getOperationForJavaMethod("echo");
        assertNotNull(operations);
        assertEquals(operations.length, 1);
        assertEquals(operations[0].getJavaMethodName(), "echo");

        String[] paramTypes = operations[0].getJavaParameters();
        assertNotNull(paramTypes);
        assertEquals(paramTypes.length, 1);
        assertEquals("javax.xml.ws.Holder<java.lang.String>", paramTypes[0]);

        // Test RequestWrapper annotations
        assertEquals(operations[0].getRequestWrapperLocalName(), "Echo");
        assertEquals(operations[0].getRequestWrapperTargetNamespace(),
                     "http://ws.apache.org/axis2/tests");
        assertEquals(operations[0].getRequestWrapperClassName(), "org.apache.ws.axis2.tests.Echo");

        // Test ResponseWrapper annotations
        assertEquals(operations[0].getResponseWrapperLocalName(), "EchoResponse");
        assertEquals(operations[0].getResponseWrapperTargetNamespace(),
                     "http://ws.apache.org/axis2/tests");
        assertEquals(operations[0].getResponseWrapperClassName(),
                     "org.apache.ws.axis2.tests.EchoResponse");

        // Test SOAPBinding default; that annotation is not present in the SEI
        // Note that annotation could occur on the operation or the type
        // (although on this SEI it doesn't occur either place).
        assertEquals(SOAPBinding.Style.DOCUMENT, operations[0].getSoapBindingStyle());
        assertEquals(SOAPBinding.Style.DOCUMENT, endpointIntfDesc.getSoapBindingStyle());

    }

    public void testOverloadedServiceImplWithSEI() {
        ServiceDescription serviceDesc =
                DescriptionFactory.createServiceDescription(DocLitWrappedImplWithSEI.class);
        assertNotNull(serviceDesc);

        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(endpointDesc.length, 1);
        // TODO: Using hardcoded endpointDesc[0] from ServiceDesc
        EndpointInterfaceDescription endpointIntfDesc =
                endpointDesc[0].getEndpointInterfaceDescription();
        assertNotNull(endpointIntfDesc);

        // Test for overloaded methods
        // SEI defines two Java methods with this name
        OperationDescription[] operations =
                endpointIntfDesc.getOperationForJavaMethod("invokeAsync");
        assertNotNull(operations);
        assertEquals(operations.length, 2);
        assertEquals(operations[0].getJavaMethodName(), "invokeAsync");
        assertEquals(operations[1].getJavaMethodName(), "invokeAsync");

        // Check the Java parameters, WebParam names, and WebResult (actually lack thereof) for each of these operations

        // Note regarding WebParam names:
        //In the client Async Call the the WebParam name will not remove JAX-WS AsyncHandler.
        //Proxy invoking the the Async Call will check the input method object and if
        //its of type JAX-WS AsyncHandler then that WebParam will be skipped.
        //This is done because AsyncHandler is NOT part of the contract, and thus it is NOT part of
        // the JAXB object constructed for the method invocation.  The AsyncHandler is part of the 
        // JAX-WS programming model to support an asynchronous callback to receive the response.

        // Note regarding WebResult annotation:
        // The async methods on this SEI do not carry a WebResult annotations.
        boolean twoArgSignatureChecked = false;
        boolean oneArgSignatureChecked = false;
        for (OperationDescription operation : operations) {
            String[] checkParams = operation.getJavaParameters();
            String[] webParamNames = operation.getParamNames();
            if (checkParams.length == 1) {
                // Check the one arguement signature
                if (oneArgSignatureChecked) {
                    fail("One Arg signature occured more than once");
                } else {
                    oneArgSignatureChecked = true;
                    // Check the Java parameter
                    assertEquals(checkParams[0], "java.lang.String");
                    // Check the WebParam Names (see note above) 
                    assertEquals(1, webParamNames.length);
                    assertEquals("invoke_str", webParamNames[0]);
                    // Check the lack of a WebResult annotation and the default values for
                    // SOAPBinding Style=DOCUMENT, Use=LITERAL, ParamStyle=WRAPPED
                    // Note that the SOAPBinding annotation is also not present and thus fully defaulted.
                    assertNull(((OperationDescriptionJava)operation).getAnnoWebResult());
                    assertEquals("return", operation.getResultName());
                    assertEquals("return", operation.getResultPartName());
                    assertEquals("", operation.getResultTargetNamespace());
                    assertFalse(operation.isResultHeader());
                }
            } else if (checkParams.length == 2) {
                // Check the two arguement signature
                if (twoArgSignatureChecked) {
                    fail("Two Arg signature occured more than once");
                } else {
                    twoArgSignatureChecked = true;
                    // Check the Java parameter
                    assertEquals(checkParams[0], "java.lang.String");
                    assertEquals(checkParams[1],
                                 "javax.xml.ws.AsyncHandler<org.test.proxy.doclitwrapped.ReturnType>");
                    // Check the WebParam Names (see note above) 
                    assertEquals(2, webParamNames.length);
                    assertEquals("invoke_str", webParamNames[0]);
                    // Check the lack of a WebResult annotation and the default values for
                    // SOAPBinding Style=DOCUMENT, Use=LITERAL, ParamStyle=WRAPPED
                    // Note that the SOAPBinding annotation is also not present and thus fully defaulted.
                    assertNull(((OperationDescriptionJava)operation).getAnnoWebResult());
                    assertEquals("return", operation.getResultName());
                    assertEquals("return", operation.getResultPartName());
                    assertEquals("", operation.getResultTargetNamespace());
                    assertFalse(operation.isResultHeader());
                }
            } else {
                fail("Wrong number of parameters returned");
            }
        }

        // Test for a method with parameters of primitive types.  Note
        // this method IS overloaded
        operations = endpointIntfDesc.getOperationForJavaMethod("twoWayHolderAsync");
        assertNotNull(operations);
        assertEquals(operations.length, 2);
        assertEquals(operations[0].getJavaMethodName(), "twoWayHolderAsync");
        assertEquals(operations[1].getJavaMethodName(), "twoWayHolderAsync");

        // Check the parameters for each operation
        twoArgSignatureChecked = false;
        boolean threeArgSignatureChecked = false;
        for (OperationDescription operation : operations) {
            String[] checkParams = operation.getJavaParameters();
            String[] webParamNames = operation.getParamNames();
            if (checkParams.length == 3) {
                // Check the one arguement signature
                if (threeArgSignatureChecked) {
                    fail("Three Arg signature occured more than once");
                } else {
                    threeArgSignatureChecked = true;
                    assertEquals(checkParams[0], "java.lang.String");
                    assertEquals(checkParams[1], "int");
                    assertEquals(checkParams[2],
                                 "javax.xml.ws.AsyncHandler<org.test.proxy.doclitwrapped.TwoWayHolder>");
                    // Check the WebParam Names (see note above) 
                    assertEquals(3, webParamNames.length);
                    assertEquals("twoWayHolder_str", webParamNames[0]);
                    assertEquals("twoWayHolder_int", webParamNames[1]);
                    // Check the lack of a WebResult annotation and the default values for
                    // SOAPBinding Style=DOCUMENT, Use=LITERAL, ParamStyle=WRAPPED
                    // Note that the SOAPBinding annotation is also not present and thus fully defaulted.
                    assertNull(((OperationDescriptionJava)operation).getAnnoWebResult());
                    assertEquals("return", operation.getResultName());
                    assertEquals("return", operation.getResultPartName());
                    assertEquals("", operation.getResultTargetNamespace());
                    assertFalse(operation.isResultHeader());
                }
            } else if (checkParams.length == 2) {
                // Check the two arguement signature
                if (twoArgSignatureChecked) {
                    fail("Two Arg signature occured more than once");
                } else {
                    twoArgSignatureChecked = true;
                    assertEquals(checkParams[0], "java.lang.String");
                    assertEquals(checkParams[1], "int");
                    // Check the WebParam Names (see note above) 
                    assertEquals(2, webParamNames.length);
                    assertEquals("twoWayHolder_str", webParamNames[0]);
                    assertEquals("twoWayHolder_int", webParamNames[1]);
                    // Check the lack of a WebResult annotation and the default values for
                    // SOAPBinding Style=DOCUMENT, Use=LITERAL, ParamStyle=WRAPPED
                    // Note that the SOAPBinding annotation is also not present and thus fully defaulted.
                    assertNull(((OperationDescriptionJava)operation).getAnnoWebResult());
                    assertEquals("return", operation.getResultName());
                    assertEquals("return", operation.getResultPartName());
                    assertEquals("", operation.getResultTargetNamespace());
                    assertFalse(operation.isResultHeader());
                }
            } else {
                fail("Wrong number of parameters returned");
            }
        }

        // Test for a one-way, void method with no parameters which also is not overloaded
        operations = endpointIntfDesc.getOperationForJavaMethod("oneWayVoid");
        assertNotNull(operations);
        assertEquals(operations.length, 1);
        assertEquals(operations[0].getJavaMethodName(), "oneWayVoid");
        String[] checkEmptyParams = operations[0].getJavaParameters();
        assertNotNull(checkEmptyParams);
        assertEquals(checkEmptyParams.length, 0);
        assertEquals(true, operations[0].isOneWay());
        // Check the lack of a WebResult annotation and the default values for
        // a ONE-WAY / VOID operation with a SOAPBinding Style=DOCUMENT, Use=LITERAL, ParamStyle=WRAPPED
        // Note that the SOAPBinding annotation is also not present and thus fully defaulted.
        assertNull(((OperationDescriptionJava)operations[0]).getAnnoWebResult());
        assertFalse(((OperationDescriptionJava)operations[0]).isWebResultAnnotationSpecified());
        assertFalse(operations[0].isOperationReturningResult());
        assertEquals(null, operations[0].getResultName());
        assertEquals(null, operations[0].getResultPartName());
        assertEquals(null, operations[0].getResultTargetNamespace());
        assertFalse(operations[0].isResultHeader());

        // Test two-way method for lack of OneWay annotation and WebResult annotation
        operations = endpointIntfDesc.getOperationForJavaMethod("invoke");
        assertNotNull(operations);
        assertEquals(1, operations.length);
        assertEquals(false, operations[0].isOneWay());
        assertNotNull(((OperationDescriptionJava)operations[0]).getAnnoWebResult());
        assertEquals("return_str", operations[0].getResultName());
        assertEquals("return_str", operations[0].getResultPartName());
        assertEquals("", operations[0].getResultTargetNamespace());
        assertFalse(operations[0].isResultHeader());
    }

    // ===========================================
    // The following tests use implementation classes defined below
    // in order to test various specific annotation settings
    // ===========================================

    public void testSOAPBindingDefault() {
        EndpointInterfaceDescription testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(SOAPBindingDefaultTestImpl.class);

        assertNull(
                ((EndpointInterfaceDescriptionJava)testEndpointInterfaceDesc).getAnnoSoapBinding());
        assertEquals(javax.jws.soap.SOAPBinding.Style.DOCUMENT,
                     testEndpointInterfaceDesc.getSoapBindingStyle());
        assertEquals(javax.jws.soap.SOAPBinding.Use.LITERAL,
                     testEndpointInterfaceDesc.getSoapBindingUse());
        assertEquals(javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED,
                     testEndpointInterfaceDesc.getSoapBindingParameterStyle());

        OperationDescription operationDesc =
                testEndpointInterfaceDesc.getOperationForJavaMethod("echoString")[0];
        // Verify WebResult annotation default values for DOC/LIT/WRAPPED from a defaulted SOAPBinding
        assertNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertEquals("return", operationDesc.getResultName());
        assertEquals("return", operationDesc.getResultPartName());
        assertEquals("", operationDesc.getResultTargetNamespace());
        assertFalse(operationDesc.isResultHeader());

    }

    public void testSOAPBindingDocEncBare() {
        //verify that we throw an exception when SOAPBinding.Use == ENCODED is specified
        try {
            EndpointInterfaceDescription testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(SOAPBindingDocEncBareTestImpl.class);
            fail("Should have caused exception");
        } catch (Exception e) {
            // Expected path, SOAPBinding.Use == ENCODED is not supported
        }
    }
    
    public void testSOAPBindingDocLitBare() {
        EndpointInterfaceDescription testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(SOAPBindingDocLitBareTestImpl.class);

        assertNotNull(
                ((EndpointInterfaceDescriptionJava)testEndpointInterfaceDesc).getAnnoSoapBinding());
        assertEquals(javax.jws.soap.SOAPBinding.Style.DOCUMENT,
                     testEndpointInterfaceDesc.getSoapBindingStyle());
        assertEquals(javax.jws.soap.SOAPBinding.Use.LITERAL,
                     testEndpointInterfaceDesc.getSoapBindingUse());
        assertEquals(javax.jws.soap.SOAPBinding.ParameterStyle.BARE,
                     testEndpointInterfaceDesc.getSoapBindingParameterStyle());
    }

    public void testSOAPBindingMethodAnnotation() {
        // Verify that an impl without the method annotation uses the settings from the type
        EndpointInterfaceDescription testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(SOAPBindingDocLitBareTestImpl.class);

        assertNotNull(
                ((EndpointInterfaceDescriptionJava)testEndpointInterfaceDesc).getAnnoSoapBinding());
        assertEquals(javax.jws.soap.SOAPBinding.Style.DOCUMENT,
                     testEndpointInterfaceDesc.getSoapBindingStyle());
        assertEquals(javax.jws.soap.SOAPBinding.Use.LITERAL,
                     testEndpointInterfaceDesc.getSoapBindingUse());
        assertEquals(javax.jws.soap.SOAPBinding.ParameterStyle.BARE,
                     testEndpointInterfaceDesc.getSoapBindingParameterStyle());

        OperationDescription operationDesc =
                testEndpointInterfaceDesc.getOperationForJavaMethod("echoString")[0];
        assertNotNull(operationDesc);
        assertNull(((OperationDescriptionJava)operationDesc).getAnnoSoapBinding());
        assertEquals(javax.jws.soap.SOAPBinding.Style.DOCUMENT,
                     operationDesc.getSoapBindingStyle());
        assertEquals(javax.jws.soap.SOAPBinding.Use.LITERAL, operationDesc.getSoapBindingUse());
        assertEquals(javax.jws.soap.SOAPBinding.ParameterStyle.BARE,
                     operationDesc.getSoapBindingParameterStyle());

        // Verify that the method annotation setting overrides the type annotatino setting
        testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(SOAPBindingDefaultMethodTestImpl.class);

        assertNull(
                ((EndpointInterfaceDescriptionJava)testEndpointInterfaceDesc).getAnnoSoapBinding());
        assertEquals(javax.jws.soap.SOAPBinding.Style.DOCUMENT,
                     testEndpointInterfaceDesc.getSoapBindingStyle());
        assertEquals(javax.jws.soap.SOAPBinding.Use.LITERAL,
                     testEndpointInterfaceDesc.getSoapBindingUse());
        assertEquals(javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED,
                     testEndpointInterfaceDesc.getSoapBindingParameterStyle());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("echoString")[0];
        assertNotNull(operationDesc);
        assertNotNull(((OperationDescriptionJava)operationDesc).getAnnoSoapBinding());
        assertEquals(javax.jws.soap.SOAPBinding.Style.DOCUMENT,
                     operationDesc.getSoapBindingStyle());
        assertEquals(javax.jws.soap.SOAPBinding.Use.LITERAL, operationDesc.getSoapBindingUse());
        assertEquals(javax.jws.soap.SOAPBinding.ParameterStyle.BARE,
                     operationDesc.getSoapBindingParameterStyle());
    }

    public void testDefaultReqRspWrapper() {

        // Test paramaterStyle = WRAPPED set a the type level with various combinations of method annotation setting
        EndpointInterfaceDescription testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(DefaultReqRspWrapperTestImpl.class);
        OperationDescription operationDesc =
                testEndpointInterfaceDesc.getOperationForJavaMethod("wrappedParams")[0];
        assertNotNull(operationDesc);
        assertEquals("wrappedParams", operationDesc.getRequestWrapperLocalName());
        assertEquals("wrappedParamsResponse", operationDesc.getResponseWrapperLocalName());
        assertEquals("http://description.jaxws.axis2.apache.org/",
                     operationDesc.getRequestWrapperTargetNamespace());
        assertEquals("http://description.jaxws.axis2.apache.org/",
                     operationDesc.getResponseWrapperTargetNamespace());
        assertNull(operationDesc.getRequestWrapperClassName());
        assertNull(operationDesc.getResponseWrapperClassName());
        // Test WebResult annotation defaults
        assertNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertFalse(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertTrue(operationDesc.isOperationReturningResult());
        assertEquals("return", operationDesc.getResultName());
        assertEquals("return", operationDesc.getResultPartName());
        assertEquals("", operationDesc.getResultTargetNamespace());
        assertFalse(operationDesc.isResultHeader());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("bareParams")[0];
        assertNotNull(operationDesc);
        assertNull(operationDesc.getRequestWrapperLocalName());
        assertNull(operationDesc.getResponseWrapperLocalName());
        assertNull(operationDesc.getRequestWrapperTargetNamespace());
        assertNull(operationDesc.getResponseWrapperTargetNamespace());
        assertNull(operationDesc.getRequestWrapperClassName());
        assertNull(operationDesc.getResponseWrapperClassName());
        // Test WebResult annotation defaults
        assertNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertFalse(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertTrue(operationDesc.isOperationReturningResult());
        assertEquals("bareParamsResponse", operationDesc.getResultName());
        assertEquals("bareParamsResponse", operationDesc.getResultPartName());
        assertEquals("http://description.jaxws.axis2.apache.org/",
                     operationDesc.getResultTargetNamespace());
        assertFalse(operationDesc.isResultHeader());

        // Test paramaterStyle = BARE set a the type level with various combinations of method annotation setting
        testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(DefaultReqRspWrapperBareTestImpl.class);
        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("wrappedParams")[0];
        assertNotNull(operationDesc);
        assertEquals("wrappedParams", operationDesc.getRequestWrapperLocalName());
        assertEquals("wrappedParamsResponse", operationDesc.getResponseWrapperLocalName());
        assertEquals("http://description.jaxws.axis2.apache.org/",
                     operationDesc.getRequestWrapperTargetNamespace());
        assertEquals("http://description.jaxws.axis2.apache.org/",
                     operationDesc.getResponseWrapperTargetNamespace());
        assertNull(operationDesc.getRequestWrapperClassName());
        assertNull(operationDesc.getResponseWrapperClassName());
        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("bareParams")[0];
        assertNotNull(operationDesc);
        assertNull(operationDesc.getRequestWrapperLocalName());
        assertNull(operationDesc.getResponseWrapperLocalName());
        assertNull(operationDesc.getRequestWrapperTargetNamespace());
        assertNull(operationDesc.getResponseWrapperTargetNamespace());
        assertNull(operationDesc.getRequestWrapperClassName());
        assertNull(operationDesc.getResponseWrapperClassName());
    }

    public void testReqRspWrapper() {
        EndpointInterfaceDescription testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(ReqRspWrapperTestImpl.class);
        OperationDescription operationDesc =
                testEndpointInterfaceDesc.getOperationForJavaMethod("method1")[0];
        assertNotNull(operationDesc);
        assertEquals("method1ReqWrapper", operationDesc.getRequestWrapperLocalName());
        assertEquals("method1RspWrapper", operationDesc.getResponseWrapperLocalName());
        assertEquals("http://a.b.c.method1ReqTNS",
                     operationDesc.getRequestWrapperTargetNamespace());
        assertEquals("http://a.b.c.method1RspTNS",
                     operationDesc.getResponseWrapperTargetNamespace());
        assertEquals(
                "org.apache.axis2.jaxws.description.AnnotationServiceImplDescriptionTests.ReqRspWrapperTestImpl.method1ReqWrapper",
                operationDesc.getRequestWrapperClassName());
        assertEquals(
                "org.apache.axis2.jaxws.description.AnnotationServiceImplDescriptionTests.ReqRspWrapperTestImpl.method1RspWrapper",
                operationDesc.getResponseWrapperClassName());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method2")[0];
        assertEquals("method2", operationDesc.getRequestWrapperLocalName());
        assertEquals("method2RspWrapper", operationDesc.getResponseWrapperLocalName());
        assertEquals("http://a.b.c.method2ReqTNS",
                     operationDesc.getRequestWrapperTargetNamespace());
        assertEquals("http://a.b.c.method2RspTNS",
                     operationDesc.getResponseWrapperTargetNamespace());
        assertEquals(
                "org.apache.axis2.jaxws.description.AnnotationServiceImplDescriptionTests.ReqRspWrapperTestImpl.method2ReqWrapper",
                operationDesc.getRequestWrapperClassName());
        assertNull(operationDesc.getResponseWrapperClassName());
    }

    public void testWebMethod() {
        EndpointInterfaceDescription testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(WebMethodTestImpl.class);

        // Test results from method with no annotation
        OperationDescription[] operationDescs =
                testEndpointInterfaceDesc.getOperationForJavaMethod("method1");
        
        if(isLegacyBehavior()){
            assertNull(operationDescs);
        }else{
            assertNotNull(operationDescs);
        }

        OperationDescription operationDesc =
                testEndpointInterfaceDesc.getOperationForJavaMethod("method2")[0];
        assertNotNull(operationDesc);
        assertEquals("renamedMethod2", operationDesc.getOperationName());
        assertEquals("", operationDesc.getAction());
        assertFalse(operationDesc.isExcluded());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method3")[0];
        assertNotNull(operationDesc);
        assertEquals("method3", operationDesc.getOperationName());
        assertEquals("ActionMethod3", operationDesc.getAction());
        assertFalse(operationDesc.isExcluded());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method4")[0];
        assertNotNull(operationDesc);
        assertEquals("renamedMethod4", operationDesc.getOperationName());
        assertEquals("ActionMethod4", operationDesc.getAction());
        assertFalse(operationDesc.isExcluded());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method4")[0];
        assertNotNull(operationDesc);
        assertEquals("renamedMethod4", operationDesc.getOperationName());
        assertEquals("ActionMethod4", operationDesc.getAction());
        assertFalse(operationDesc.isExcluded());

        operationDescs = testEndpointInterfaceDesc.getOperationForJavaMethod("method5");
        assertNull(operationDescs);

    }
    
    
    //Verify that 
    // 1. Wsgen version 2.1.6 is valid for using the new behavior
    // 2. Something greater than 2.1.6 is also valid
    // 3. Something less that 2.1.6 is not valid
    public void testValidWsgenVersion() {
        
        assertTrue(WSToolingUtils.isValidVersion(MDQConstants.SUN_WEB_METHOD_BEHAVIOR_CHANGE_VERSION));

        //This should be true because it converts to something greater than 2.1.6
        assertTrue(WSToolingUtils.isValidVersion("2.12.6.1"));
        
        //Less than 2.1.6 so should fail
        assertFalse(WSToolingUtils.isValidVersion("2.1.1"));
    }
    
    public void testWebMethodOldBehavior3() {

        System.setProperty(MDQConstants.USE_LEGACY_WEB_METHOD_RULES, "true");
        EndpointInterfaceDescriptionImpl testEndpointInterfaceDesc =
            (EndpointInterfaceDescriptionImpl)getEndpointInterfaceDesc(WebMethodLegacyCheck.class);
        
        DescriptionBuilderComposite dbc = DescriptionTestUtils.getServiceDescriptionComposite(testEndpointInterfaceDesc.getEndpointDescription().getServiceDescription());
        dbc.setCorrespondingClass(WebMethodLegacyCheck.class);
        LegacyMethodRetrieverImpl retrive = new LegacyMethodRetrieverImpl(dbc, testEndpointInterfaceDesc);
        Iterator<MethodDescriptionComposite> iter = retrive.retrieveMethods();
        while(iter.hasNext()){
            MethodDescriptionComposite mdc = iter.next();
            String name = mdc.getMethodName();
            assertNotNull(name);
        }

    }
    //This test verifies default webMethod behavior
    public void testWebMethodDefaultBehavior() {
        
        try {
            //If the version is valid then try the new behavior
            if (!WSToolingUtils.isValidVersion(WSToolingUtils.getWsGenVersion())) {
                //No point in testing this case if new behavior is not supported.
                //default is the new tooling behavior.
                return;
                
            }
        } catch (ClassNotFoundException e) {
        } catch (IOException ioex) {
        }
      //default behavior
        System.setProperty(MDQConstants.USE_LEGACY_WEB_METHOD_RULES, "");
        
        EndpointInterfaceDescription testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(WebMethodTestImpl.class);
        

        // Test results from method with no annotation
        OperationDescription[] operationDescs =
                testEndpointInterfaceDesc.getOperationForJavaMethod("method1");

        assertNotNull(operationDescs);

        OperationDescription operationDesc =
                testEndpointInterfaceDesc.getOperationForJavaMethod("method2")[0];
        assertNotNull(operationDesc);
        assertEquals("renamedMethod2", operationDesc.getOperationName());
        assertEquals("", operationDesc.getAction());
        assertFalse(operationDesc.isExcluded());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method3")[0];
        assertNotNull(operationDesc);
        assertEquals("method3", operationDesc.getOperationName());
        assertEquals("ActionMethod3", operationDesc.getAction());
        assertFalse(operationDesc.isExcluded());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method4")[0];
        assertNotNull(operationDesc);
        assertEquals("renamedMethod4", operationDesc.getOperationName());
        assertEquals("ActionMethod4", operationDesc.getAction());
        assertFalse(operationDesc.isExcluded());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method4")[0];
        assertNotNull(operationDesc);
        assertEquals("renamedMethod4", operationDesc.getOperationName());
        assertEquals("ActionMethod4", operationDesc.getAction());
        assertFalse(operationDesc.isExcluded());

        operationDescs = testEndpointInterfaceDesc.getOperationForJavaMethod("method5");
        assertNull(operationDescs);

    }

    //This test verifies old webmethod behavior
    public void testWebMethodOldBehavior() {
        
        //Weather new tooling is supported or not, Legacy behavior should
        //always work if LegacyWebMethod property is set.
        
        System.setProperty(MDQConstants.USE_LEGACY_WEB_METHOD_RULES, "true");
        EndpointInterfaceDescription testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(WebMethodTestImpl.class);
        

        // Test results from method with no annotation
        OperationDescription[] operationDescs =
                testEndpointInterfaceDesc.getOperationForJavaMethod("method1");
        //check legacy behavior
        assertNull(operationDescs);

        OperationDescription operationDesc =
                testEndpointInterfaceDesc.getOperationForJavaMethod("method2")[0];
        assertNotNull(operationDesc);
        assertEquals("renamedMethod2", operationDesc.getOperationName());
        assertEquals("", operationDesc.getAction());
        assertFalse(operationDesc.isExcluded());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method3")[0];
        assertNotNull(operationDesc);
        assertEquals("method3", operationDesc.getOperationName());
        assertEquals("ActionMethod3", operationDesc.getAction());
        assertFalse(operationDesc.isExcluded());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method4")[0];
        assertNotNull(operationDesc);
        assertEquals("renamedMethod4", operationDesc.getOperationName());
        assertEquals("ActionMethod4", operationDesc.getAction());
        assertFalse(operationDesc.isExcluded());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method4")[0];
        assertNotNull(operationDesc);
        assertEquals("renamedMethod4", operationDesc.getOperationName());
        assertEquals("ActionMethod4", operationDesc.getAction());
        assertFalse(operationDesc.isExcluded());

        operationDescs = testEndpointInterfaceDesc.getOperationForJavaMethod("method5");
        assertNull(operationDescs);

    }
    
    //This test verifies that, if we do have the appropriate JDK installed and we do set the
    //system property flag, then the new behavior will be realized. 
    //NOTE: This method can be forced to run thru the tests by temporarily changing
    //the "newSunBehavior" flag inside of EndpointInterfaceDescriptionImpl.getMethodRetriever()
    //    and
    // in this method, by forcing "validVersion" to true. Realize that by doing this other tests
    // will fail...So this should be considered temporary for testing this method
    public void testWebMethodNewBehavior() {
        
        try {
            boolean validVersion = WSToolingUtils.isValidVersion(WSToolingUtils.getWsGenVersion());
            //If the version is valid then try the new behavior
            
            if (validVersion) {
                //Try new behavior
                System.setProperty(MDQConstants.USE_LEGACY_WEB_METHOD_RULES, "false");
            } else {
                //At this point, just return since the test would be guaranteed to fail as we
                //testing new tooling rules and it seems new tooling is not supported here.     
                return;
            }
        } catch (ClassNotFoundException e) {    
        } catch (IOException ioex) {
        }
        System.setProperty(MDQConstants.USE_LEGACY_WEB_METHOD_RULES, "true");
        EndpointInterfaceDescription testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(WebMethodTestImpl1.class);
        
        // All tests here will be using the new behavior

        //We should be able to see this even though there are false exclusions
        OperationDescription[] operationDescs =
            testEndpointInterfaceDesc.getOperationForJavaMethod("method1");

        assertNotNull(operationDescs);
        
        operationDescs =
            testEndpointInterfaceDesc.getOperationForJavaMethod("shouldBeExcludedMethod");

        assertNull(operationDescs);
        
        operationDescs =
            testEndpointInterfaceDesc.getOperations();
        assertEquals(operationDescs.length, 4);
        
        //Need to make sure we don't mess up the other tests, so set this back to using the
        //legacy behavior
        System.setProperty(MDQConstants.USE_LEGACY_WEB_METHOD_RULES, "true");

    }



    public void testWebResult() {
        EndpointInterfaceDescription testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(WebResultTestImpl.class);

        // DOCUMENT / LITERAL / WRAPPED methods

        OperationDescription[] operationDescs =
                testEndpointInterfaceDesc.getOperationForJavaMethod("method0");
        if(isLegacyBehavior()){
            assertNull(operationDescs);
        }else{
            assertNotNull(operationDescs);
        }

        OperationDescription operationDesc =
                testEndpointInterfaceDesc.getOperationForJavaMethod("method1")[0];
        assertNotNull(operationDesc);
        assertNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertFalse(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertTrue(operationDesc.isOperationReturningResult());
        assertEquals("return", operationDesc.getResultName());
        assertEquals("return", operationDesc.getResultPartName());
        assertEquals("", operationDesc.getResultTargetNamespace());
        assertFalse(operationDesc.isResultHeader());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method2")[0];
        assertNotNull(operationDesc);
        assertNotNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertTrue(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertTrue(operationDesc.isOperationReturningResult());
        assertEquals("return", operationDesc.getResultName());
        assertEquals("return", operationDesc.getResultPartName());
        assertEquals("", operationDesc.getResultTargetNamespace());
        assertFalse(operationDesc.isResultHeader());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method3")[0];
        assertNotNull(operationDesc);
        assertNotNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertTrue(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertTrue(operationDesc.isOperationReturningResult());
        assertEquals("resultName", operationDesc.getResultName());
        assertEquals("resultName", operationDesc.getResultPartName());
        assertEquals("", operationDesc.getResultTargetNamespace());
        assertFalse(operationDesc.isResultHeader());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method4")[0];
        assertNotNull(operationDesc);
        assertNotNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertTrue(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertTrue(operationDesc.isOperationReturningResult());
        assertEquals("resultName", operationDesc.getResultName());
        assertEquals("partName", operationDesc.getResultPartName());
        assertEquals("http://result.test.target.namespace/",
                     operationDesc.getResultTargetNamespace());
        assertFalse(operationDesc.isResultHeader());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method5")[0];
        assertNotNull(operationDesc);
        assertNotNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertTrue(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertTrue(operationDesc.isOperationReturningResult());
        assertEquals("resultName5", operationDesc.getResultName());
        assertEquals("partName5", operationDesc.getResultPartName());
        assertEquals("http://result.test.target.namespace.5/",
                     operationDesc.getResultTargetNamespace());
        assertTrue(operationDesc.isResultHeader());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method6")[0];
        assertNotNull(operationDesc);
        assertNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertFalse(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertFalse(operationDesc.isOperationReturningResult());
        assertEquals(null, operationDesc.getResultName());
        assertEquals(null, operationDesc.getResultPartName());
        assertEquals(null, operationDesc.getResultTargetNamespace());
        assertFalse(operationDesc.isResultHeader());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method7")[0];
        assertNotNull(operationDesc);
        assertNotNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertTrue(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertTrue(operationDesc.isOperationReturningResult());
        assertEquals("resultName7", operationDesc.getResultName());
        assertEquals("partName7", operationDesc.getResultPartName());
        assertEquals("http://service.test.target.namespace/",
                     operationDesc.getResultTargetNamespace());
        assertTrue(operationDesc.isResultHeader());

        // DOCUMENT / LITERAL / BARE methods

        operationDescs = testEndpointInterfaceDesc.getOperationForJavaMethod("method0_bare");
        if(isLegacyBehavior()){
            assertNull(operationDescs);
        }else{
            assertNotNull(operationDescs);
        }

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method1_bare")[0];
        assertNotNull(operationDesc);
        assertNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertFalse(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertTrue(operationDesc.isOperationReturningResult());
        assertEquals("renamedMethod1BareResponse", operationDesc.getResultName());
        assertEquals("renamedMethod1BareResponse", operationDesc.getResultPartName());
        assertEquals("http://service.test.target.namespace/",
                     operationDesc.getResultTargetNamespace());
        assertFalse(operationDesc.isResultHeader());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method2_bare")[0];
        assertNotNull(operationDesc);
        assertNotNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertTrue(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertTrue(operationDesc.isOperationReturningResult());
        assertEquals("renamedMethod2BareResponse", operationDesc.getResultName());
        assertEquals("renamedMethod2BareResponse", operationDesc.getResultPartName());
        assertEquals("http://service.test.target.namespace/",
                     operationDesc.getResultTargetNamespace());
        assertFalse(operationDesc.isResultHeader());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method3_bare")[0];
        assertNotNull(operationDesc);
        assertNotNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertTrue(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertTrue(operationDesc.isOperationReturningResult());
        assertEquals("resultName", operationDesc.getResultName());
        assertEquals("resultName", operationDesc.getResultPartName());
        assertEquals("http://service.test.target.namespace/",
                     operationDesc.getResultTargetNamespace());
        assertFalse(operationDesc.isResultHeader());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method4_bare")[0];
        assertNotNull(operationDesc);
        assertNotNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertTrue(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertTrue(operationDesc.isOperationReturningResult());
        assertEquals("resultName", operationDesc.getResultName());
        assertEquals("partName", operationDesc.getResultPartName());
        assertEquals("http://result.bare.test.target.namespace/",
                     operationDesc.getResultTargetNamespace());
        assertFalse(operationDesc.isResultHeader());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method5_bare")[0];
        assertNotNull(operationDesc);
        assertNotNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertTrue(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertTrue(operationDesc.isOperationReturningResult());
        assertEquals("resultName5", operationDesc.getResultName());
        assertEquals("partName5", operationDesc.getResultPartName());
        assertEquals("http://result.bare.test.target.namespace.5/",
                     operationDesc.getResultTargetNamespace());
        assertTrue(operationDesc.isResultHeader());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method6_bare")[0];
        assertNotNull(operationDesc);
        assertNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertFalse(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertFalse(operationDesc.isOperationReturningResult());
        assertEquals(null, operationDesc.getResultName());
        assertEquals(null, operationDesc.getResultPartName());
        assertEquals(null, operationDesc.getResultTargetNamespace());
        assertFalse(operationDesc.isResultHeader());

        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method7")[0];
        assertNotNull(operationDesc);
        assertNotNull(((OperationDescriptionJava)operationDesc).getAnnoWebResult());
        assertTrue(((OperationDescriptionJava)operationDesc).isWebResultAnnotationSpecified());
        assertTrue(operationDesc.isOperationReturningResult());
        assertEquals("resultName7", operationDesc.getResultName());
        assertEquals("partName7", operationDesc.getResultPartName());
        assertEquals("http://service.test.target.namespace/",
                     operationDesc.getResultTargetNamespace());
        assertTrue(operationDesc.isResultHeader());
    }

    public void testWebParamWrapped() {
        
        EndpointInterfaceDescription testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(WebParamTestImpl.class);

        // DOCUMENT / LITERAL / WRAPPED methods

        // method0
        OperationDescription[] operationDescs =
                testEndpointInterfaceDesc.getOperationForJavaMethod("method0");
        if(isLegacyBehavior()){
            assertNull(operationDescs);
        }else{
            assertNotNull(operationDescs);
        }

        // method00
        operationDescs = testEndpointInterfaceDesc.getOperationForJavaMethod("method00");
        if(isLegacyBehavior()){
            assertNull(operationDescs);
        }else{
            assertNotNull(operationDescs);
        }


        // method1
        OperationDescription operationDesc =
                testEndpointInterfaceDesc.getOperationForJavaMethod("method1")[0];
        assertNotNull(operationDesc);
        ParameterDescription[] paramDesc = operationDesc.getParameterDescriptions();
        assertEquals(1, paramDesc.length);
        ParameterDescription checkParamDesc = paramDesc[0];
        assertNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("arg0", checkParamDesc.getParameterName());
        assertEquals("arg0", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(0));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("arg0"));
        assertEquals(String.class, checkParamDesc.getParameterType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        // method2
        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method2")[0];
        assertNotNull(operationDesc);
        paramDesc = operationDesc.getParameterDescriptions();
        assertEquals(1, paramDesc.length);
        checkParamDesc = paramDesc[0];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("arg0", checkParamDesc.getParameterName());
        assertEquals("arg0", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(0));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("arg0"));
        assertEquals(String.class, checkParamDesc.getParameterType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        // method3
        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method3")[0];
        assertNotNull(operationDesc);
        paramDesc = operationDesc.getParameterDescriptions();
        assertEquals(5, paramDesc.length);
        checkParamDesc = paramDesc[0];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("param0NameMethod3", checkParamDesc.getParameterName());
        assertEquals("param0NameMethod3", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(0));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("param0NameMethod3"));
        assertEquals(String.class, checkParamDesc.getParameterType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[1];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("param1NameMethod3", checkParamDesc.getParameterName());
        assertEquals("param1NameMethod3", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(1));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("param1NameMethod3"));
        assertEquals(Holder.class, checkParamDesc.getParameterType());
        assertEquals(Integer.class, checkParamDesc.getParameterActualType());
        assertTrue(checkParamDesc.isHolderType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.INOUT, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[2];
        assertNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("arg2", checkParamDesc.getParameterName());
        assertEquals("arg2", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(2));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("arg2"));
        assertEquals(Object.class, checkParamDesc.getParameterType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[3];
        assertNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("arg3", checkParamDesc.getParameterName());
        assertEquals("arg3", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(3));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("arg3"));
        assertEquals(Integer.TYPE, checkParamDesc.getParameterType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[4];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("lastParamNameMethod3", checkParamDesc.getParameterName());
        assertEquals("lastParamNameMethod3", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(4));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("lastParamNameMethod3"));
        assertEquals(String.class, checkParamDesc.getParameterType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        // method4
        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method4")[0];
        assertNotNull(operationDesc);
        paramDesc = operationDesc.getParameterDescriptions();
        assertEquals(5, paramDesc.length);

        checkParamDesc = paramDesc[0];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("param0NameMethod4", checkParamDesc.getParameterName());
        assertEquals("param0PartName", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(0));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("param0NameMethod4"));
        assertEquals(String.class, checkParamDesc.getParameterType());
        assertEquals("http://param.4.0.result.test.target.namespace/",
                     checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[1];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("param1NameMethod4", checkParamDesc.getParameterName());
        assertEquals("param1PartName", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(1));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("param1NameMethod4"));
        assertEquals(Integer.TYPE, checkParamDesc.getParameterType());
        assertEquals("http://param.4.1.result.test.target.namespace/",
                     checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[2];
        assertNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("arg2", checkParamDesc.getParameterName());
        assertEquals("arg2", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(2));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("arg2"));
        assertEquals(Object.class, checkParamDesc.getParameterType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[3];
        assertNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("arg3", checkParamDesc.getParameterName());
        assertEquals("arg3", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(3));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("arg3"));
        assertEquals(Holder.class, checkParamDesc.getParameterType());
        assertEquals(Integer.class, checkParamDesc.getParameterActualType());
        assertTrue(checkParamDesc.isHolderType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.INOUT, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[4];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("lastParamNameMethod4", checkParamDesc.getParameterName());
        assertEquals("lastParamNameMethod4", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(4));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("lastParamNameMethod4"));
        assertEquals(String.class, checkParamDesc.getParameterType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        // method5
        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method5")[0];
        assertNotNull(operationDesc);
        paramDesc = operationDesc.getParameterDescriptions();
        assertEquals(5, paramDesc.length);

        checkParamDesc = paramDesc[0];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("param0NameMethod5", checkParamDesc.getParameterName());
        assertEquals("param0PartName", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(0));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("param0NameMethod5"));
        assertEquals(String.class, checkParamDesc.getParameterType());
        assertFalse(checkParamDesc.isHolderType());
        assertEquals("http://param.5.0.result.test.target.namespace/",
                     checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertTrue(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[1];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("param1NameMethod5", checkParamDesc.getParameterName());
        assertEquals("param1PartName", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(1));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("param1NameMethod5"));
        assertEquals(Integer.TYPE, checkParamDesc.getParameterType());
        assertFalse(checkParamDesc.isHolderType());
        assertEquals("http://param.5.1.result.test.target.namespace/",
                     checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[2];
        assertNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("arg2", checkParamDesc.getParameterName());
        assertEquals("arg2", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(2));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("arg2"));
        assertEquals(Holder.class, checkParamDesc.getParameterType());
        assertEquals(Object.class, checkParamDesc.getParameterActualType());
        assertTrue(checkParamDesc.isHolderType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.INOUT, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[3];
        assertNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("arg3", checkParamDesc.getParameterName());
        assertEquals("arg3", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(3));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("arg3"));
        assertEquals(Integer.TYPE, checkParamDesc.getParameterType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[4];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("lastParamNameMethod5", checkParamDesc.getParameterName());
        assertEquals("lastParamNameMethod5", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(4));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("lastParamNameMethod5"));
        assertEquals(Holder.class, checkParamDesc.getParameterType());
        assertEquals(String.class, checkParamDesc.getParameterActualType());
        assertTrue(checkParamDesc.isHolderType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.OUT, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        // method6
        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method6")[0];
        assertNotNull(operationDesc);
        paramDesc = operationDesc.getParameterDescriptions();
        assertEquals(5, paramDesc.length);

        checkParamDesc = paramDesc[0];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("param0NameMethod6", checkParamDesc.getParameterName());
        assertEquals("param0PartName", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(0));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("param0NameMethod6"));
        assertEquals(String.class, checkParamDesc.getParameterType());
        assertEquals("http://param.service.test.target.namespace/",
                     checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertTrue(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[1];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("param1NameMethod6", checkParamDesc.getParameterName());
        assertEquals("param1PartName", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(1));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("param1NameMethod6"));
        assertEquals(Integer.TYPE, checkParamDesc.getParameterType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[2];
        assertNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("arg2", checkParamDesc.getParameterName());
        assertEquals("arg2", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(2));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("arg2"));
        assertEquals(Object.class, checkParamDesc.getParameterType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[3];
        assertNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("arg3", checkParamDesc.getParameterName());
        assertEquals("arg3", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(3));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("arg3"));
        assertEquals(Integer.TYPE, checkParamDesc.getParameterType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        checkParamDesc = paramDesc[4];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("lastParamNameMethod6", checkParamDesc.getParameterName());
        assertEquals("lastParamNameMethod6", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(4));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("lastParamNameMethod6"));
        assertEquals(String.class, checkParamDesc.getParameterType());
        assertEquals("", checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());
    }

    public void testWebParamBare() {
        EndpointInterfaceDescription testEndpointInterfaceDesc =
                getEndpointInterfaceDesc(WebParamTestImpl.class);

        // DOCUMENT / LITERAL / BARE methods
        OperationDescription[] operationDescs =
                testEndpointInterfaceDesc.getOperationForJavaMethod("method0_bare");
        if(isLegacyBehavior()){
            assertNull(operationDescs);
        }else{
            assertNotNull(operationDescs);
        }

        // method00
        operationDescs = testEndpointInterfaceDesc.getOperationForJavaMethod("method00_bare");
        if(isLegacyBehavior()){
            assertNull(operationDescs);
        }else{
            assertNotNull(operationDescs);
        }
        // method1
        OperationDescription operationDesc =
                testEndpointInterfaceDesc.getOperationForJavaMethod("method1_bare")[0];
        assertNotNull(operationDesc);
        ParameterDescription[] paramDesc = operationDesc.getParameterDescriptions();
        assertEquals(1, paramDesc.length);
        ParameterDescription checkParamDesc = paramDesc[0];
        assertNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("renamedMethod1", checkParamDesc.getParameterName());
        assertEquals("renamedMethod1", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(0));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("renamedMethod1"));
        assertEquals(String.class, checkParamDesc.getParameterType());
        assertEquals("http://param.service.test.target.namespace/",
                     checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        // method2
        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method2_bare")[0];
        assertNotNull(operationDesc);
        paramDesc = operationDesc.getParameterDescriptions();
        assertEquals(1, paramDesc.length);
        checkParamDesc = paramDesc[0];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("renamedMethod2", checkParamDesc.getParameterName());
        assertEquals("renamedMethod2", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(0));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("renamedMethod2"));
        assertEquals(String.class, checkParamDesc.getParameterType());
        assertEquals("http://param.service.test.target.namespace/",
                     checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.IN, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        // method3
        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method3_bare")[0];
        assertNotNull(operationDesc);
        paramDesc = operationDesc.getParameterDescriptions();
        assertEquals(1, paramDesc.length);
        checkParamDesc = paramDesc[0];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("param1NameMethod3", checkParamDesc.getParameterName());
        assertEquals("param1NameMethod3", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(0));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("param1NameMethod3"));
        assertEquals(Holder.class, checkParamDesc.getParameterType());
        assertEquals(Integer.class, checkParamDesc.getParameterActualType());
        assertTrue(checkParamDesc.isHolderType());
        assertEquals("http://param.service.test.target.namespace/",
                     checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.INOUT, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        // method4
        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method4_bare")[0];
        assertNotNull(operationDesc);
        paramDesc = operationDesc.getParameterDescriptions();
        assertEquals(1, paramDesc.length);

        checkParamDesc = paramDesc[0];
        assertNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("renamedMethod4", checkParamDesc.getParameterName());
        assertEquals("renamedMethod4", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(0));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("renamedMethod4"));
        assertEquals(Holder.class, checkParamDesc.getParameterType());
        assertEquals(Integer.class, checkParamDesc.getParameterActualType());
        assertEquals("http://param.service.test.target.namespace/",
                     checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.INOUT, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        // method5
        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method5_bare")[0];
        assertNotNull(operationDesc);
        paramDesc = operationDesc.getParameterDescriptions();
        assertEquals(1, paramDesc.length);

        checkParamDesc = paramDesc[0];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("lastParamNameMethod5", checkParamDesc.getParameterName());
        assertEquals("lastParamNameMethod5", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(0));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("lastParamNameMethod5"));
        assertEquals(Holder.class, checkParamDesc.getParameterType());
        assertEquals(String.class, checkParamDesc.getParameterActualType());
        assertTrue(checkParamDesc.isHolderType());
        assertEquals("http://method5.bare.result.test.target.namespace.5/",
                     checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.OUT, checkParamDesc.getMode());
        assertFalse(checkParamDesc.isHeader());

        // method6
        operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method6_bare")[0];
        assertNotNull(operationDesc);
        paramDesc = operationDesc.getParameterDescriptions();
        assertEquals(1, paramDesc.length);

        checkParamDesc = paramDesc[0];
        assertNotNull(((ParameterDescriptionJava)checkParamDesc).getAnnoWebParam());
        assertEquals("param0NameMethod6", checkParamDesc.getParameterName());
        assertEquals("param0PartName", checkParamDesc.getPartName());
        assertEquals(checkParamDesc, operationDesc.getParameterDescription(0));
        assertEquals(checkParamDesc, operationDesc.getParameterDescription("param0NameMethod6"));
        assertEquals(Holder.class, checkParamDesc.getParameterType());
        assertEquals(String.class, checkParamDesc.getParameterActualType());
        assertEquals("http://param.service.test.target.namespace/",
                     checkParamDesc.getTargetNamespace());
        assertEquals(WebParam.Mode.INOUT, checkParamDesc.getMode());
        assertTrue(checkParamDesc.isHeader());
    }

    /**
     * This method tests the new property updates in JAX-WS2.2 specification.
     * @RequestWrapper(PartName)
     * @ResponseWrapper(PartName)
     * @WebFault(MessageName)
     * We will ensure that these new properties are successfully set in the Metadata layer
     */
    public void testJAXWS22AnnotationPropertyUpdate(){
    	EndpointInterfaceDescription testEndpointInterfaceDesc =
    		getEndpointInterfaceDesc(ReqRspWrapperTestImpl.class);
    	OperationDescription operationDesc =
    		testEndpointInterfaceDesc.getOperationForJavaMethod("method1")[0];
    	assertNotNull(operationDesc);
    	
    	assertEquals("jaxws22.RequestPartName",
    			operationDesc.getRequestWrapperPartName());
    	
    	assertEquals("jaxws22.ResponsePartName",
    			operationDesc.getResponseWrapperPartName());


    	operationDesc = testEndpointInterfaceDesc.getOperationForJavaMethod("method2")[0];
    	assertNotNull(operationDesc);
    	FaultDescription faultDescription = operationDesc.getFaultDescriptions()[0];
    	
    	assertEquals("jaxws22MessageName", faultDescription.getMessageName());
    }
    
    private boolean isLegacyBehavior(){
    
        boolean legacyBehavior=false;
        try {
            //If the version is valid then try the new behavior
            if (!WSToolingUtils.isValidVersion(WSToolingUtils.getWsGenVersion())) {
                legacyBehavior=true;
            }
        } catch (ClassNotFoundException e) {
        } catch (IOException ioex) {
        }
        return legacyBehavior;
    }

    /*
    * Method to return the endpoint interface description for a given implementation class.
    */
    private EndpointInterfaceDescription getEndpointInterfaceDesc(Class implementationClass) {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc =
                DescriptionFactory.createServiceDescription(implementationClass);
        assertNotNull(serviceDesc);

        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);

        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescription testEndpointDesc = endpointDesc[0];
        EndpointInterfaceDescription testEndpointInterfaceDesc =
                testEndpointDesc.getEndpointInterfaceDescription();
        assertNotNull(testEndpointInterfaceDesc);

        return testEndpointInterfaceDesc;
    }
}

// ============================================================================
// SOAPBindingDefaultTest service implementation class
// @SOAPBinding values: style=DOCUMENT, use=LITERAL, paramaterStyle=WRAPPED
// @WebResult values: are all defaulted
// ============================================================================

@WebService()
class SOAPBindingDefaultTestImpl {
    public String echoString(String s) {
        return s;
    }
}
// ============================================================================
// SOAPBindingDocLiBareTestImpl service implementation class
// Note that Style should default
// ============================================================================

@WebService()
@SOAPBinding(use = javax.jws.soap.SOAPBinding.Use.LITERAL,
             parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
class SOAPBindingDocLitBareTestImpl {
    public String echoString(String s) {
        return s;
    }
}

//============================================================================
//SOAPBindingDocEncBareTestImpl service implementation class
//Note that Style should default
//============================================================================

@WebService()
@SOAPBinding(use = javax.jws.soap.SOAPBinding.Use.ENCODED,
          parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
class SOAPBindingDocEncBareTestImpl {
 public String echoString(String s) {
     return s;
 }
}
// ============================================================================
// SOAPBindingDefaultMethodTest service implementation class
// Note that style will default to DOCUMENT based on Type annotation
// ============================================================================

@WebService()
class SOAPBindingDefaultMethodTestImpl {
    @SOAPBinding(use = javax.jws.soap.SOAPBinding.Use.LITERAL,
                 parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
    public String echoString(String s) {
        return s;
    }
}

// =============================================================================
// testDefaultReqRspWrapper service implementation classes
// =============================================================================

@WebService
//Note the default parameterStyle is WRAPPED, so no type-level annotation is required.
class DefaultReqRspWrapperTestImpl {
    public String wrappedParams(String s) {
        return s;
    }

    @SOAPBinding(parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
    public String bareParams(String s) {
        return s;
    }
}

@WebService
@SOAPBinding(parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
class DefaultReqRspWrapperBareTestImpl {
    @SOAPBinding(parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED)
    public String wrappedParams(String s) {
        return s;
    }

    public String bareParams(String s) {
        return s;
    }
}

// =============================================================================
// testReqRspWrapper service implementation class
// =============================================================================

@WebService
//Note the default parameterStyle is WRAPPED, so no type-level annotation is required.
class ReqRspWrapperTestImpl {
    @RequestWrapper(localName = "method1ReqWrapper", targetNamespace = "http://a.b.c.method1ReqTNS",
                    className = "org.apache.axis2.jaxws.description.AnnotationServiceImplDescriptionTests.ReqRspWrapperTestImpl.method1ReqWrapper",
                    partName = "jaxws22.RequestPartName")
    @ResponseWrapper(localName = "method1RspWrapper",
                     targetNamespace = "http://a.b.c.method1RspTNS",
                     className = "org.apache.axis2.jaxws.description.AnnotationServiceImplDescriptionTests.ReqRspWrapperTestImpl.method1RspWrapper",
                     partName = "jaxws22.ResponsePartName")
    public String method1(String s) {
        return s;
    }

    @RequestWrapper(targetNamespace = "http://a.b.c.method2ReqTNS",
                    className = "org.apache.axis2.jaxws.description.AnnotationServiceImplDescriptionTests.ReqRspWrapperTestImpl.method2ReqWrapper")
    @ResponseWrapper(localName = "method2RspWrapper",
                     targetNamespace = "http://a.b.c.method2RspTNS")
    public String method2(String s) throws ReqRspWrapperException{
        return s;
    }

    public class method1ReqWrapper {

    }

    public class method2ReqWrapper {

    }

    public class method1RspWrapper {

    }
}

@WebFault(messageName="jaxws22MessageName")
class ReqRspWrapperException extends Exception{
	private String message = null;
	public ReqRspWrapperException(){
		
	}
	public ReqRspWrapperException(String message){
		this.message = message;
	}

}
//=============================================================================
//testWebMethod service implementaiton class
//=============================================================================

//This an implied SEI which will be used to test the new Sun Behavior
@WebService
class WebMethodTestImpl1 {
// No web method annotation
public String method1(String s) {
   return s;
}

@WebMethod(operationName = "renamedMethod2")
public String method2(String s) {
   return s;
}

@WebMethod(action = "ActionMethod3")
public String method3(String s) {
   return s;
}

@WebMethod(operationName = "renamedMethod4", action = "ActionMethod4")
public String method4(String s) {
   return s;
}

@WebMethod(operationName = "shouldBeExcludedMethod", exclude = true)
public String method5(String s) {
   return s;
}
}




// =============================================================================
// testWebMethod service implementaiton class
// =============================================================================
@WebService
class WebMethodLegacyCheck {

    @WebMethod()
    public String echoanno(String s){return s;}

    // under old jax-ws (default) behavior, legacywm=true, this unannotated method would not be exposed,
    // but under the new rules, it is exposed.
    //@WebMethod()
    public String echonoanno(String s){return s;}
}

@WebService
class WebMethodTestImpl {
    // No web method annotation
    public String method1(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod2")
    public String method2(String s) {
        return s;
    }

    @WebMethod(action = "ActionMethod3")
    public String method3(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod4", action = "ActionMethod4")
    public String method4(String s) {
        return s;
    }

    @WebMethod(exclude = true)
    public String method5(String s) {
        return s;
    }
}

// ===============================================
// Web Result 
// ===============================================

@WebService(targetNamespace = "http://service.test.target.namespace/")
// Default SOAPBinding Style=DOCUMENT, Use=LITERAL, ParamStyle=WRAPPED
class WebResultTestImpl {
    // DOCUMENT / LITERAL / WRAPPED methods
    public String method0(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod1")
    public String method1(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod2")
    @WebResult()
    public String method2(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod3")
    @WebResult(name = "resultName")
    public String method3(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod4")
    @WebResult(name = "resultName", partName = "partName",
               targetNamespace = "http://result.test.target.namespace/")
    public String method4(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod5")
    @WebResult(name = "resultName5", partName = "partName5",
               targetNamespace = "http://result.test.target.namespace.5/", header = true)
    public String method5(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod6")
    @Oneway
    public void method6(String s) {
        return;
    }

    @WebMethod(operationName = "renamedMethod7")
    @WebResult(name = "resultName7", partName = "partName7", header = true)
    public String method7(String s) {
        return s;
    }

    // DOCUMENT / LITERAL / BARE methods
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public String method0_bare(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod1Bare")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public String method1_bare(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod2Bare")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult()
    public String method2_bare(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod3Bare")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult(name = "resultName")
    public String method3_bare(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod4Bare")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult(name = "resultName", partName = "partName",
               targetNamespace = "http://result.bare.test.target.namespace/")
    public String method4_bare(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod5Bare")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult(name = "resultName5", partName = "partName5",
               targetNamespace = "http://result.bare.test.target.namespace.5/", header = true)
    public String method5_bare(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod6Bare")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @Oneway
    public void method6_bare(String s) {
        return;
    }

    @WebMethod(operationName = "renamedMethod7Bare")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult(name = "resultName7", partName = "partName7", header = true)
    public String method7_bare(String s) {
        return s;
    }
}

// ===============================================
// Web Param test impl
// ===============================================

@WebService(targetNamespace = "http://param.service.test.target.namespace/")
//Default SOAPBinding Style=DOCUMENT, Use=LITERAL, ParamStyle=WRAPPED
class WebParamTestImpl {

    // DOCUMENT / LITERAL / WRAPPED methods
    public String method0(String s) {
        return s;
    }

    public void method00() {
        return;
    }

    @WebMethod(operationName = "renamedMethod1")
    public String method1(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod2")
    @WebResult()
    public String method2(
            @WebParam() String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod3")
    @WebResult(name = "resultName")
    public String method3(
            @WebParam(name = "param0NameMethod3") String s,
            @WebParam(name = "param1NameMethod3") javax.xml.ws.Holder<Integer> holderInteger,
            Object objNoWebParamAnno,
            int intNoWebParamAnno,
            @WebParam(name = "lastParamNameMethod3") String last) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod4")
    @WebResult(name = "resultName", partName = "partName",
               targetNamespace = "http://result.test.target.namespace/")
    public String method4(
            @WebParam(name = "param0NameMethod4", partName = "param0PartName",
                      targetNamespace = "http://param.4.0.result.test.target.namespace/") String s,
            @WebParam(name = "param1NameMethod4", partName = "param1PartName",
                      targetNamespace = "http://param.4.1.result.test.target.namespace/") int i,
            Object objNoWebParamAnno,
            javax.xml.ws.Holder<Integer> intNoWebParamAnno,
            @WebParam(name = "lastParamNameMethod4") String last) {

        return s;
    }

    @WebMethod(operationName = "renamedMethod5")
    @WebResult(name = "resultName5", partName = "partName5",
               targetNamespace = "http://result.test.target.namespace.5/", header = true)
    public String method5(
            @WebParam(name = "param0NameMethod5", partName = "param0PartName",
                      targetNamespace = "http://param.5.0.result.test.target.namespace/",
                      header = true) String s,
            @WebParam(name = "param1NameMethod5", partName = "param1PartName",
                      targetNamespace = "http://param.5.1.result.test.target.namespace/",
                      header = false) int i,
            javax.xml.ws.Holder<Object> objNoWebParamAnno,
            int intNoWebParamAnno,
            @WebParam(name = "lastParamNameMethod5", mode = WebParam.Mode.OUT)
            javax.xml.ws.Holder<String> last) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod6")
    @WebResult(name = "resultName5", partName = "partName5", header = true)
    public String method6(
            @WebParam(name = "param0NameMethod6", partName = "param0PartName", header = true)
            String s,
            @WebParam(name = "param1NameMethod6", partName = "param1PartName", header = false)
            int i,
            Object objNoWebParamAnno,
            int intNoWebParamAnno,
            @WebParam(name = "lastParamNameMethod6") String last) {
        return s;
    }

    // DOCUMENT / LITERAL / BARE methods
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public String method0_bare(String s) {
        return s;
    }

    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void method00_bare() {
        return;
    }

    @WebMethod(operationName = "renamedMethod1")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public String method1_bare(String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod2")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult()
    public String method2_bare(
            @WebParam() String s) {
        return s;
    }

    @WebMethod(operationName = "renamedMethod3")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult(name = "resultName")
    public String method3_bare(
            @WebParam(name = "param1NameMethod3") javax.xml.ws.Holder<Integer> holderInteger) {
        return null;
    }

    @WebMethod(operationName = "renamedMethod4")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult(name = "resultName", partName = "partName",
               targetNamespace = "http://result.test.target.namespace/")
    public String method4_bare(
            javax.xml.ws.Holder<Integer> intNoWebParamAnno) {

        return null;
    }

    @WebMethod(operationName = "renamedMethod5")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult(name = "resultName5", partName = "partName5",
               targetNamespace = "http://result.test.target.namespace.5/", header = true)
    public void method5_bare(
            @WebParam(name = "lastParamNameMethod5", mode = WebParam.Mode.OUT,
                      targetNamespace = "http://method5.bare.result.test.target.namespace.5/")
            javax.xml.ws.Holder<String> last) {
        return;
    }

    @WebMethod(operationName = "renamedMethod6")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult(name = "resultName5", partName = "partName5", header = true)
    public String method6_bare(
            @WebParam(name = "param0NameMethod6", partName = "param0PartName", header = true)
            javax.xml.ws.Holder<String> s) {
        return null;
    }
}
