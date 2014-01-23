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
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;
import org.apache.axis2.jaxws.util.WSDLWrapper;

import javax.jws.WebService;
import javax.wsdl.Definition;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * These tests are intended to test various aspects of the OperationDescription.
 */

public class DBCwithReduceWSDLMemoryParmsTests extends TestCase {

    /**
     * This test will confirm that when reduceWSDLMemoryCache is true 
     * and reduceWSDLMemoryType=2
     *  The WsdlDefinitionWrapper will construct WSDLWrapperReloadImpl classes  
     *
     */
    public static String basedir;
    static {
        basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = new File(".").getAbsolutePath();
        }
    }

    public void testDBCwithReduceWSDLMemoryParms() {
        
        // I used OperationDescriptionTests as a template for this test

        String axis2xml = basedir +
            "/test-resources/axis2.xml";
        ConfigurationContext cc;
        try {

            cc = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, axis2xml);
            if (cc !=null) {
                String parm = (String)cc.getAxisConfiguration().getParameterValue("reduceWSDLMemoryCache") ;
                //System.out.println("configContext parm:" + parm );
            } else {
                //System.out.println("configContext: is null" );
            }
            assertNotNull(axis2xml);

            String wsdlRelativeLocation = "test-resources/wsdl/";
            String wsdlFileName = "BindingNamespace.wsdl";
            String targetNamespace = "http://nonanonymous.complextype.test.org";
            String wsdlLocation = wsdlRelativeLocation + wsdlFileName;

            // Build up a DBC, including the WSDL Definition and the annotation information for 
            // the impl class.
            DescriptionBuilderComposite dbc = new DescriptionBuilderComposite(cc);

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
            //dbc.setWsdlDefinition(wsdlDefn);
            //System.out.println("DBCwithConfigContextTests setWsdlURL" );
            dbc.setwsdlURL(wsdlURL);
            //System.out.println("DBCwithConfigContextTests new HashMap" );

            HashMap<String, DescriptionBuilderComposite> dbcMap =
                new HashMap<String, DescriptionBuilderComposite>();
            dbcMap.put(dbc.getClassName(), dbc);
            //System.out.println("**** DescriptionFactory.createServiceDescriptionFromDBCMap-DBC-toString" + 
            //                      dbc.toString() + " toString-END" );
            List<ServiceDescription> serviceDescList =
                DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap, cc);
            //System.out.println("**** DBCwithConfigContextTests tests:" + dbc.toString() +" toString-END" );
            assertEquals(1, serviceDescList.size());
            //System.out.println("DBCwithConfigContextTests get ServiceDescription" );
            ServiceDescriptionWSDL sdw = (ServiceDescriptionWSDL)serviceDescList.get(0);
            assertNotNull(sdw);

            //System.out.println("DBCwithConfigContextTests ServiceDescriptionWSDL not null" );

            WSDLWrapper w = sdw.getWSDLWrapper();
            assertNotNull(w);
            //System.out.println("DBCwithConfigContextTests WSDLWrapper not null" );

            Definition d = w.getDefinition();
            assertNotNull(d);
            //System.out.println("DBCwithConfigContextTests Definition not null" );
            assertTrue(d.toString().startsWith("org.apache.axis2.wsdl.util.WSDLWrapperReloadImpl") );


            //System.out.println("d:" + d.getClass().getName() + "\n" + d.toString() ) ;

            /*
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
             */
        } catch (AxisFault e) {
            fail("This should not fail with this " + e);
        }
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
