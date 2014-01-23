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


package org.apache.axis2.jaxws.description.builder;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderUtils;

import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Directly test the Description classes built via annotations without a WSDL file. These tests
 * focus on combinations of the following: - A generic service (no annotations) - A generated
 * service (annotations) - An SEI
 */
public class DescriptionBuilderTests extends TestCase {

    /* 
    * ========================================================================
    * ServiceDescription Tests
    * ========================================================================
    */
    public void testCreateWebServiceAnnot() {
        String name = "EchoServiceAnnotated";
        String targetNamespace = "http://description.jaxws.axis2.apache.org/";
        String serviceName = "EchoServiceName";
        String wsdlLocation = "http://EchoService/wsdl";
        String endpointInterface = "EchoServiceEndpointInterface";
        String portName = "EchoServiceAnnotatedPort";

        WebServiceAnnot webServiceAnnotImpl1 =
                WebServiceAnnot.createWebServiceAnnotImpl();

        WebServiceAnnot webServiceAnnotImpl2 =
                WebServiceAnnot.createWebServiceAnnotImpl(name,
                                                          targetNamespace,
                                                          serviceName,
                                                          wsdlLocation,
                                                          endpointInterface,
                                                          portName);

        DescriptionBuilderComposite descriptionBuilderComposite =
                new DescriptionBuilderComposite();

        descriptionBuilderComposite.setWebServiceAnnot(webServiceAnnotImpl2);


        WebService webServiceAnnotImpl3 = 
                descriptionBuilderComposite.getWebServiceAnnot();

        assertNotNull("WebService name not set", webServiceAnnotImpl3.name());
        assertNotNull("WebService targetNamespace not set", webServiceAnnotImpl3.targetNamespace());
        assertNotNull("WebService serviceName not set", webServiceAnnotImpl3.serviceName());
        assertNotNull("WebService wsdlLocation not set", webServiceAnnotImpl3.wsdlLocation());
        assertNotNull("WebService endpointInterface not set",
                      webServiceAnnotImpl3.endpointInterface());

        System.out.println("WebService name:" + webServiceAnnotImpl3.name());
    }

    public void testCreateWebServiceProviderAnnot() {
        String name = "EchoServiceAnnotated";
        String targetNamespace = "http://description.jaxws.axis2.apache.org/";
        String serviceName = "EchoServiceName";
        String wsdlLocation = "http://EchoService/wsdl";
        String endpointInterface = "EchoServiceEndpointInterface";
        String portName = "EchoServiceAnnotatedPort";

        WebServiceProviderAnnot webServiceProviderAnnot =
                WebServiceProviderAnnot.createWebServiceAnnotImpl();

        webServiceProviderAnnot.setPortName(portName);
        webServiceProviderAnnot.setServiceName(serviceName);
        webServiceProviderAnnot.setTargetNamespace(targetNamespace);
        webServiceProviderAnnot.setWsdlLocation(wsdlLocation);

        DescriptionBuilderComposite descriptionBuilderComposite =
                new DescriptionBuilderComposite();

        descriptionBuilderComposite.setWebServiceProviderAnnot(webServiceProviderAnnot);


        javax.xml.ws.WebServiceProvider webServiceProviderAnnot3 =
                descriptionBuilderComposite.getWebServiceProviderAnnot();

        assertEquals("WebServiceProvider port name not set properly",
                     webServiceProviderAnnot3.portName(), portName);
        assertEquals("WebServiceProvider targetNamespace not set properly",
                     webServiceProviderAnnot3.targetNamespace(), targetNamespace);
        assertEquals("WebServiceProvider serviceName not set properly",
                     webServiceProviderAnnot3.serviceName(), serviceName);
        assertEquals("WebServiceProvider wsdlLocation not set properly",
                     webServiceProviderAnnot3.wsdlLocation(), wsdlLocation);
        System.out.println("WebService name:" + webServiceProviderAnnot3.portName());
    }

    public void testCreateWebMethodAnnot() {
        String operationName = "echoStringMethod";
        String action = "urn:EchoStringMethod";
        boolean exclude = true;


        WebMethodAnnot webMethodAnnot = WebMethodAnnot.createWebMethodAnnotImpl();

        webMethodAnnot.setOperationName(operationName);
        webMethodAnnot.setAction(action);
        webMethodAnnot.setExclude(exclude);

        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();
        MethodDescriptionComposite mdc = new MethodDescriptionComposite();

        mdc.setWebMethodAnnot(webMethodAnnot);
        mdc.setMethodName(operationName);
        dbc.addMethodDescriptionComposite(mdc);

        WebMethodAnnot webMethodAnnot3 =
                dbc.getMethodDescriptionComposite(operationName, 1).getWebMethodAnnot();

        assertEquals("WebMethod operation name not set properly", webMethodAnnot3.operationName(),
                     operationName);
        assertEquals("WebMethod action not set properly", webMethodAnnot3.action(), action);
        assertEquals("WebMethod exclude flag not set properly", webMethodAnnot3.exclude(), exclude);
    }

    public void testCreateWebParamAnnot() {

        String name = "arg0";
        String partName = "sku";
        String targetNamespace = "http://description.jaxws.axis2.apache.org/";
        Mode mode = Mode.IN;
        boolean header = true;

        WebParamAnnot webParamAnnot = WebParamAnnot.createWebParamAnnotImpl();

        webParamAnnot.setName(name);
        webParamAnnot.setPartName(partName);
        webParamAnnot.setMode(mode);
        webParamAnnot.setTargetNamespace(targetNamespace);
        webParamAnnot.setHeader(header);

        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();

        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setWebParamAnnot(webParamAnnot);
        pdc.setParameterType("int");

        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        mdc.setMethodName("TestMethod1");

        try {
            //First test adds this pdc out of bounds
            mdc.addParameterDescriptionComposite(pdc, 1);
        } catch (IndexOutOfBoundsException e) {
            // Expected flow
        }
        catch (Exception e) {
            fail("Caught unexpected exception" + e);
        }

        try {
            //Now, add it at the proper position
            mdc.addParameterDescriptionComposite(pdc, 0);
        } catch (IndexOutOfBoundsException e) {
            // Expected flow
        }
        catch (Exception e) {
            fail("Caught unexpected exception" + e);
        }

        dbc.addMethodDescriptionComposite(mdc);

        WebParamAnnot webParamAnnot3 =
                dbc.getMethodDescriptionComposite("TestMethod1", 1)
                        .getParameterDescriptionComposite(0).getWebParamAnnot();

        assertEquals("WebMethod name not set properly", webParamAnnot3.name(), name);
        assertEquals("WebMethod PartName not set properly", webParamAnnot3.partName(), partName);
        assertEquals("WebMethod Mode flag not set properly", webParamAnnot3.mode(), mode);
        assertEquals("WebMethod Target Namespace not set properly",
                     webParamAnnot3.targetNamespace(), targetNamespace);
        assertEquals("WebMethod Header not set properly", webParamAnnot3.header(), header);

        assertEquals("Unable to convert string to parameterTypeClass",
                     pdc.getParameterTypeClass().getName(), "int");
    }
    
    public void testGenericAnnotationInstancesAndProcessor() {
        TestProcessor annotationProcessor = new TestProcessor();
        annotationProcessor.setAnnotationInstanceClassName(CustomAnnotation.class.getName());
        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();
        dbc.addCustomAnnotationProcessor(annotationProcessor);
        Map<String, CustomAnnotationProcessor> processors = dbc.getCustomAnnotationProcessors();
        assertNotNull(processors);
        assertNotNull(processors.values());
        assertEquals(processors.values().size(), 1);
        CustomAnnotationProcessor instance = processors.get(CustomAnnotation.class.getName());
        assertNotNull(instance);
        assertEquals(instance.getClass().getName(), TestProcessor.class.getName());
        assertEquals(instance.getAnnotationInstanceClassName(), CustomAnnotation.class.getName());
        CustomAnnotation annotation = new CustomAnnotation();
        annotation.setAnnotationClassName(Custom.class.getName());
        annotation.setTarget(ElementType.TYPE);
        annotation.addParameterData("name", annotation.getClass().getSimpleName());
        dbc.addCustomAnnotationInstance(annotation);
        List<CustomAnnotationInstance> gaiList = dbc.getCustomAnnotationInstances();
        assertNotNull(gaiList);
        assertEquals(1, gaiList.size());
        CustomAnnotationInstance gai = gaiList.get(0);
        assertNotNull(gai);
        assertEquals(gai.getClass().getName(), CustomAnnotation.class.getName());
        assertEquals(gai.getAnnotationClassName(), Custom.class.getName());
        assertEquals(gai.getTarget(), ElementType.TYPE);
        assertEquals(gai.getParameterData("name"), gai.getClass().getSimpleName());
        IllegalArgumentException ex = null;
        try {
            gai.getParameterData("unknown");
        }
        catch(IllegalArgumentException iae) {
            ex = iae;
        }
        assertNotNull(ex);
        try {
            gai.addParameterData("unknown", null);
        }
        catch(IllegalArgumentException iae) {
            ex = iae;
        }
        assertNotNull(ex);
    }
    
    public void testDescriptionBuilderUtilsReparseIfArray() {
        String expected = "[Lmy.Class;";
        String out = DescriptionBuilderUtils.reparseIfArray("[my.Class");
        assertEquals("DescriptionBuilderUtils reparseIfArray method did not reformat binary class name properly", expected, out);
    }       
    
    class TestProcessor implements CustomAnnotationProcessor {
        
        private String annotationInstanceClassName;

        public String getAnnotationInstanceClassName() {
            return annotationInstanceClassName;
        }
        
        public void setAnnotationInstanceClassName(String annotationInstanceClassName) {
            this.annotationInstanceClassName = annotationInstanceClassName;
        }

        public void processTypeLevelAnnotation(EndpointDescription ed, CustomAnnotationInstance annotation) {
            // do nothing, testing purproses only
        }
        
    }
    
    class CustomAnnotation implements CustomAnnotationInstance {

        private Map<String, Object> dataMap = new HashMap<String, Object>();
        
        private ElementType elementType;
        
        List<String> knownParamNames;
        
        private String annotationClassName;
        
        CustomAnnotation(List<String> knownParamNames) {
            this.knownParamNames = knownParamNames;
        }
        
        CustomAnnotation() {
            knownParamNames = new ArrayList<String>();
            knownParamNames.add("name");
        }
        
        public void setAnnotationClassName(String annotationClassName) {
            this.annotationClassName = annotationClassName;
        }
        
        public String getAnnotationClassName() {
            return annotationClassName;
        }
        
        public void addParameterData(String paramName, Object value) throws IllegalArgumentException {
            checkParamName(paramName);
            dataMap.put(paramName, value);
        }

        public Object getParameterData(String paramName) throws IllegalArgumentException {
            checkParamName(paramName);
            return dataMap.get(paramName);
        }
        
        public void setTarget(ElementType elementType) {
            this.elementType = elementType;
        }

        public ElementType getTarget() {
            return elementType;
        }
        
        private void checkParamName(String paramName) throws IllegalArgumentException {
            if(knownParamNames != null 
                    && 
                    !knownParamNames.isEmpty() 
                    && 
                    !knownParamNames.contains(paramName)) {
                throw new IllegalArgumentException("The parameter " + paramName +
                                " is an unknown parameter for the CustomAnnotation type.");
            }
        }
    }
    
    @interface Custom {
        String name() default "";
    }

    @WebService
    @Custom
    class AnnotatedService {
        public String echo(String echoString) {
            return echoString;
        }
    }
}
