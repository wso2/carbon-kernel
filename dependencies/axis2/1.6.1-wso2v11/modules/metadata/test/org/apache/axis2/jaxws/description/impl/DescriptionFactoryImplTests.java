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
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.CustomAnnotationInstance;
import org.apache.axis2.jaxws.description.builder.CustomAnnotationProcessor;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.converter.JavaClassToDBCConverter;
import org.apache.axis2.jaxws.description.impl.DescriptionFactoryImpl;
import org.apache.axis2.jaxws.description.validator.EndpointDescriptionValidator;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;
import org.apache.axis2.metadata.registry.MetadataFactoryRegistry;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DescriptionFactoryImplTests extends TestCase {
    
    private static final String namespaceURI =
            "http://org.apache.axis2.jaxws.description.ServiceDescriptionTests";
    private static final String localPart = "EchoService";
    private static final QName serviceQName = new QName(namespaceURI, localPart);

    
    public void testServiceDescriptionCaching() {        
        QName uniqueQName = new QName(namespaceURI, localPart + "_testValidServiceSubclass");
        
        ServiceDescription desc1 = 
            DescriptionFactoryImpl.createServiceDescription(null, uniqueQName, ServiceSubclass.class);
                  
        /*
        int size = 5;
        ServiceDescription desc2; 
        for (int i = 0; i < size; i++) {
            desc2 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName, ServiceSubclass.class);
            assertTrue("service description was not reused", desc1 == desc2);
        } 
        */    
    }      

    public void testClearServiceDescriptionCache() throws Exception {        
        QName uniqueQName1 = new QName(namespaceURI, localPart + "_testClearCache1");
        QName uniqueQName2 = new QName(namespaceURI, localPart + "_testClearCache2");
  
//        // the ClientConfigFactory instance is stored DescriptionFactoryImpl clientConfigFactory 
//        // field and for this test we need to clear it, so that a custom version of 
//        // ClientConfigurationFactory can be used.
//        resetClientConfigFactory();
        
        // install caching factory        
        ClientConfigurationFactory oldFactory = 
            (ClientConfigurationFactory)MetadataFactoryRegistry.getFactory(ClientConfigurationFactory.class);
        CachingClientContextFactory newFactory = new CachingClientContextFactory();
        MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, newFactory);
        
        try {
            ServiceDescription desc1 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName1, ServiceSubclass.class);
                        
            ServiceDescription desc2 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName1, ServiceSubclass.class);
            
            newFactory.reset();
            
            ServiceDescription desc3 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName2, ServiceSubclass.class);
            
            assertTrue(desc1 == desc2);
            assertTrue(desc1 != desc3);
                        
            // should clear one
            DescriptionFactoryImpl.clearServiceDescriptionCache(desc2.getAxisConfigContext());
            
            ServiceDescription desc4 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName1, ServiceSubclass.class);
                        
            ServiceDescription desc5 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName2, ServiceSubclass.class);
            
            assertTrue(desc1 != desc4);
            assertTrue(desc3 == desc5);
                       
            // should clear both
            DescriptionFactoryImpl.clearServiceDescriptionCache();
            
            ServiceDescription desc6 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName1, ServiceSubclass.class);
            
            ServiceDescription desc7 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName2, ServiceSubclass.class);
            
            assertTrue(desc4 != desc6);
            assertTrue(desc3 != desc7);
            
            // this should do nothing
            DescriptionFactoryImpl.clearServiceDescriptionCache(null);
            
        } finally {
            // restore old factory by updating the registry THEN clearing the cached factory
            // so it is retrieved from the table again.
            MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, oldFactory);
//            resetClientConfigFactory();
        }                          
    }
    
    public void testCustomAnnotationSupport() {
        JavaClassToDBCConverter converter = new JavaClassToDBCConverter(AnnotatedService.class);
        HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
        DescriptionBuilderComposite dbc = dbcMap.get(AnnotatedService.class.getName());
        assertNotNull(dbc);
        SampleAnnotation sampleAnnotation = new SampleAnnotation();
        sampleAnnotation.setAnnotationClassName(Custom.class.getName());
        dbc.addCustomAnnotationInstance(sampleAnnotation);
        SampleAnnotationProcessor saProcessor = new SampleAnnotationProcessor();
        saProcessor.setAnnotationInstanceClassName(sampleAnnotation.getClass().getName());
        dbc.addCustomAnnotationProcessor(saProcessor);
        WebService webService = dbc.getWebServiceAnnot();
        assertNotNull(webService);
        String pn = webService.portName();
        String tns = webService.targetNamespace();
        assertNotNull(pn);
        assertNotNull(tns);
        QName portQName = new QName(tns, pn);
        List<ServiceDescription> sdList = DescriptionFactoryImpl.createServiceDescriptionFromDBCMap(dbcMap, null);
        assertNotNull(sdList);
        assertEquals(sdList.size(), 1);
        ServiceDescription sd = sdList.get(0);
        assertNotNull(sd);
        EndpointDescription ed = sd.getEndpointDescription(portQName);
        assertNotNull(ed);
        // for testing purposes we want to make a cast b/c some of the methods
        // we are accessing are protected in EndpointDescriptionImpl
        if(ed instanceof EndpointDescriptionImpl) {
            EndpointDescriptionImpl edImpl = (EndpointDescriptionImpl) ed;
            List<CustomAnnotationInstance> customAnnotationList = edImpl.getCustomAnnotationInstances();
            assertNotNull(customAnnotationList);
            assertEquals(customAnnotationList.size(), 1);
            CustomAnnotationInstance annotationInstance = customAnnotationList.get(0);
            assertNotNull(annotationInstance);
            assertEquals(annotationInstance.getClass().getName(), SampleAnnotation.class.getName());
            CustomAnnotationProcessor processor = edImpl.getCustomAnnotationProcessor(annotationInstance.getClass().getName());
            assertNotNull(processor);
            AxisService axisService = ed.getAxisService();
            assertNotNull(axisService);
            String name = (String) axisService.getParameterValue(SampleAnnotation.class.getName());
            assertNotNull(name);
            assertEquals(SampleAnnotationProcessor.class.getName(), name);
        }
    }
    
    public void testHandlerChainType() {
    	JavaClassToDBCConverter converter = new JavaClassToDBCConverter(AnnotatedService.class);
        HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
        DescriptionBuilderComposite dbc = dbcMap.get(AnnotatedService.class.getName());
        assertNotNull(dbc);
        InputStream is = getXMLFileStream();
        assertNotNull(is);
        HandlerChainsType hct = DescriptionUtils.loadHandlerChains(is, this.getClass().getClassLoader());
        dbc.setHandlerChainsType(hct);
        List<ServiceDescription> sdList = DescriptionFactoryImpl.createServiceDescriptionFromDBCMap(dbcMap, null);
        assertNotNull(sdList);
        assertTrue(sdList.size() > 0);
        ServiceDescription sd = sdList.get(0);
        assertNotNull(sd.getEndpointDescriptions_AsCollection());
        Collection<EndpointDescription> edColl = sd.getEndpointDescriptions_AsCollection();
        assertNotNull(edColl);
        assertTrue(edColl.size() > 0);
        EndpointDescription ed = edColl.iterator().next();
        assertNotNull(ed);
        assertNotNull(ed.getHandlerChain());
    }
    
    /**
     * This will verify that properties are correctly copied from a DBC to an
     * EndpointDescription instance by a helper method in DescriptionFactoryImpl.
     */
    public void testSetPropertiesOnEndpointDesc() {
        
        // first get an EndpointDescription instance
        JavaClassToDBCConverter converter = new JavaClassToDBCConverter(AnnotatedService.class);
        HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
        DescriptionBuilderComposite dbc = dbcMap.get(AnnotatedService.class.getName());
        assertNotNull(dbc);
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("testKey", "testValue");
        dbc.setProperties(properties);
        List<ServiceDescription> sdList = DescriptionFactoryImpl.createServiceDescriptionFromDBCMap(dbcMap, null);
        assertNotNull(sdList);
        assertEquals(sdList.size(), 1);
        ServiceDescription sd = sdList.get(0);
        assertNotNull(sd);
        assertNotNull(dbc.getWebServiceAnnot());
        String pn = dbc.getWebServiceAnnot().portName();
        String tns = dbc.getWebServiceAnnot().targetNamespace();
        assertNotNull(pn);
        assertNotNull(tns);
        QName portQName = new QName(tns, pn);
        EndpointDescription ed = sd.getEndpointDescription(portQName);
        assertNotNull(ed);
        
        // now test the setPropertiesOnEndpointDesc method
        DescriptionFactoryImpl.setPropertiesOnEndpointDesc(ed, dbc);
        assertNotNull(ed.getProperty("testKey"));
        assertEquals(ed.getProperty("testKey"), "testValue");
        
    }
    
    private InputStream getXMLFileStream() {
    	InputStream is = null;
    	String configLoc = null;
        try {
            String sep = "/";
            configLoc = sep + "test-resources" + sep + "test-handler.xml";
            String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
            is = new File(baseDir + configLoc).toURL().openStream();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    	return is;
    }
    
    
    private void resetClientConfigFactory() throws Exception {
        Field field = DescriptionFactoryImpl.class.getDeclaredField("clientConfigFactory");
        field.setAccessible(true);
        field.set(null, null);
    }
    
    private static class ServiceSubclass extends javax.xml.ws.Service {

        protected ServiceSubclass(URL wsdlDocumentLocation, QName serviceName) {
            super(wsdlDocumentLocation, serviceName);
        }
    }
    
    private static class CachingClientContextFactory extends ClientConfigurationFactory {
        ConfigurationContext context;
        
        public ConfigurationContext getClientConfigurationContext() {
            if (context == null) {
                context = super.getClientConfigurationContext();
            }
            System.out.println("Test version of CachingClientContextFactory: " + context);
            return context;
        }
        
        public void reset() {
            context = null;
        }
        
    }
    
    class SampleAnnotation implements CustomAnnotationInstance {

        private Map<String, Object> dataMap = new HashMap<String, Object>();
        
        private ElementType elementType;
        
        List<String> knownParamNames;
        
        private String annotationClassName;
        
        SampleAnnotation(List<String> knownParamNames) {
            this.knownParamNames = knownParamNames;
        }
        
        public void setAnnotationClassName(String annotationClassName) {
            this.annotationClassName = annotationClassName;
        }
        
        public String getAnnotationClassName() {
            return annotationClassName;
        }
        
        SampleAnnotation() {
            knownParamNames = new ArrayList<String>();
            knownParamNames.add("name");
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

    class SampleAnnotationProcessor implements CustomAnnotationProcessor {
        
        private String annotationInstanceClassName;

        public String getAnnotationInstanceClassName() {
            return annotationInstanceClassName;
        }
        
        public void setAnnotationInstanceClassName(String annotationInstanceClassName) {
            this.annotationInstanceClassName = annotationInstanceClassName;
        }

        public void processTypeLevelAnnotation(EndpointDescription ed, CustomAnnotationInstance annotation) {
                AxisService axisService = ed.getAxisService();
                if(axisService != null) {
                    try {
                        axisService.addParameter(SampleAnnotation.class.getName(), 
                                                 SampleAnnotationProcessor.class.getName());
                    }
                    catch(AxisFault af) {
                        // nothing here
                    }
                }
        }
        
    }

    @interface Custom {
        String name() default "";
    }

    @WebService(targetNamespace="http://org.apache.example", serviceName="AnnotatedService", portName="AnnotatedPort")
    @Custom(name="AnnotatedService")
    class AnnotatedService {
        public String echo(String echoString) {
            return echoString;
        }
    }
    
}
