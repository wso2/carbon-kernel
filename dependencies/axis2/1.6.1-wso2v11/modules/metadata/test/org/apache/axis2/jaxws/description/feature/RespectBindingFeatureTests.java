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

package org.apache.axis2.jaxws.description.feature;

import junit.framework.TestCase;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.jaxws.common.config.AddressingWSDLExtensionValidator;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.converter.JavaClassToDBCConverter;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.axis2.jaxws.util.WSDLExtensionValidatorUtil;
import org.apache.axis2.util.JavaUtils;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.RespectBinding;
import javax.xml.ws.soap.Addressing;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class RespectBindingFeatureTests extends TestCase {

    private static final String ns = "http://jaxws.axis2.apache.org/metadata/feature/respectbinding";
    
    private static final String serviceName = "EchoMessageService";
    private static final String portTypeName = "EchoMessagePortType";
    
    private static final String plainServicePortName = "PlainServicePort";
    private static final String disabledServicePortName = "DisabledServicePort";
    private static final String defaultServicePortName = "DefaultServicePort";
    private static final String completeServicePortName = "CompleteServicePort";
    
    private static final String wsdlLocation = "test-resources/wsdl/RespectBinding.wsdl";

    /*
     * RespectBinding processing should fail because a WSDL file was not included.
     */

    public void testPlain() throws Exception {
        JavaClassToDBCConverter converter = new JavaClassToDBCConverter(PlainService.class);
        HashMap<String, DescriptionBuilderComposite> map = converter.produceDBC();
        
        DescriptionBuilderComposite composite = map.get(PlainService.class.getName());
        
        URL wsdlUrl = new URL("file:./" + wsdlLocation);
        WSDL4JWrapper wrapper = new WSDL4JWrapper(wsdlUrl, false, 0);
        
        composite.setwsdlURL(wsdlUrl);
        composite.setWsdlDefinition(wrapper.getDefinition());
        
        List<ServiceDescription> sdList = null;
        try {
            sdList = DescriptionFactory.createServiceDescriptionFromDBCMap(map, null, true);
        }
        catch (Exception e) {
            // An exception is expected.
        }
        
        assertTrue("The ServiceDescriptions should not have been built.", sdList == null);
    }
    
    public void testRespectBindingDisabled() throws Exception {
        JavaClassToDBCConverter converter = new JavaClassToDBCConverter(DisabledService.class);
        HashMap<String, DescriptionBuilderComposite> map = converter.produceDBC();
        
        DescriptionBuilderComposite composite = map.get(DisabledService.class.getName());
        
        URL wsdlUrl = new URL("file:./" + wsdlLocation);
        WSDL4JWrapper wrapper = new WSDL4JWrapper(wsdlUrl, false, 0);
        
        composite.setwsdlURL(wsdlUrl);
        composite.setWsdlDefinition(wrapper.getDefinition());
        
        List<ServiceDescription> sdList = DescriptionFactory.createServiceDescriptionFromDBCMap(map, null, true);
        ServiceDescription sd = sdList.get(0);
        
        EndpointDescription ed = sd.getEndpointDescription(new QName(ns, disabledServicePortName));
        assertTrue("The EndpointDescription should not be null.", ed != null);

        boolean respect = ed.respectBinding();
        assertFalse("Strict binding support should be DISABLED.", respect);
    }
    
    public void testRespectBindingDefault() throws Exception {
        JavaClassToDBCConverter converter = new JavaClassToDBCConverter(DefaultService.class);
        HashMap<String, DescriptionBuilderComposite> map = converter.produceDBC();
        
        DescriptionBuilderComposite composite = map.get(DefaultService.class.getName());
        
        URL wsdlUrl = new URL("file:./" + wsdlLocation);
        WSDL4JWrapper wrapper = new WSDL4JWrapper(wsdlUrl, false, 0);
        
        composite.setwsdlURL(wsdlUrl);
        composite.setWsdlDefinition(wrapper.getDefinition());
        
        List<ServiceDescription> sdList = null;
        try {
            sdList = DescriptionFactory.createServiceDescriptionFromDBCMap(map, null, true);
        }
        catch (Exception e) {
            // An exception is expected.
        }
        
        assertTrue("The ServiceDescriptions should not have been built.", sdList == null);
    }
    
    public void testRespectBindingComplete() throws Exception {
        JavaClassToDBCConverter converter = new JavaClassToDBCConverter(CompleteService.class);
        HashMap<String, DescriptionBuilderComposite> map = converter.produceDBC();
        
        DescriptionBuilderComposite composite = map.get(CompleteService.class.getName());
        
        
        //Register 
        URL wsdlUrl = new URL("file:./" + wsdlLocation);
        WSDL4JWrapper wrapper = new WSDL4JWrapper(wsdlUrl, false, 0);
        
        composite.setwsdlURL(wsdlUrl);
        composite.setWsdlDefinition(wrapper.getDefinition());
        ConfigurationContext configCtx = null;
        
        //Create ConfigContext here, we need it so we can register the WSDLAddressingExtensionValditor.
        try{
            List<ServiceDescription> sdList = DescriptionFactory.createServiceDescriptionFromDBCMap(map, null, false);
            ServiceDescription sd = sdList.get(0);
            sd.getAxisConfigContext();
            //Register Addressing WSDL Extensions
            configCtx = sd.getAxisConfigContext();
            WSDLExtensionValidatorUtil.addWSDLExtensionValidator(configCtx, new AddressingWSDLExtensionValidator());
        }catch(Exception e){
            String stack = JavaUtils.callStackToString();
            fail(e.getMessage() + " \n stack trace = "+stack);
        }
        
        //Create ServiceDescription
        List<ServiceDescription> sdList = DescriptionFactory.createServiceDescriptionFromDBCMap(map, configCtx, true);
        ServiceDescription sd = sdList.get(0);
        EndpointDescription ed = sd.getEndpointDescription(new QName(ns, completeServicePortName));
        assertTrue("The EndpointDescription should not be null.", ed != null);

        boolean respect = ed.respectBinding();
        assertTrue("Strict binding support should be ENABLED.", respect);
    }
    
    @WebService(targetNamespace=ns, portName=plainServicePortName)
    @RespectBinding
    class PlainService {
        public String echo(String input) {
            return "";
        }
    }
    
    @WebService(targetNamespace=ns, 
        serviceName=serviceName,
        portName=defaultServicePortName, 
        name=portTypeName, 
        wsdlLocation=wsdlLocation)
    @RespectBinding
    class DefaultService {
        public String echo(String input) {
            return "";
        }
    }
    
    @WebService(targetNamespace=ns,
        serviceName=serviceName,
        portName=disabledServicePortName,
        name=portTypeName,
        wsdlLocation=wsdlLocation)
    @RespectBinding(enabled=false)
    class DisabledService {
        public String echo(String input) {
            return "";
        }
    }
    
    @WebService(targetNamespace=ns,
        serviceName=serviceName,
        portName=completeServicePortName,
        name=portTypeName,
        wsdlLocation=wsdlLocation)
    @RespectBinding
    @Addressing
    class CompleteService {
        public String echo(String input) {
            return "";
        }
    }
}


