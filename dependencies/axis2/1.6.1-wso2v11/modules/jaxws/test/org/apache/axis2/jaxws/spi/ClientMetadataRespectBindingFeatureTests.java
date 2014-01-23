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
package org.apache.axis2.jaxws.spi;

import org.apache.axis2.jaxws.binding.SOAPBinding;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.axis2.jaxws.description.builder.MTOMAnnot;
import org.apache.axis2.jaxws.description.builder.RespectBindingAnnot;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Validate the setting up of the RespectBinding WebServiceFeature on the client side via meta-data 
 * (such as a deployment descriptor)
 */
public class ClientMetadataRespectBindingFeatureTests extends TestCase {
    static final String namespaceURI = "http://description.jaxws.axis2.apache.org";
    static final String svcLocalPart = "svcLocalPart";
    static final String multiPortWsdl = "ClientMetadataMultiPort.wsdl";
    static final String multiPortWsdl_portLocalPart1 = "portLocalPartMulti1";
    static final String multiPortWsdl_portLocalPart2 = "portLocalPartMulti2";
    
    /**
     * Validate that RespectBinding can be enabled via a sparse composite on the service.
     */
    public void testRespectBindingEnabled() {
        Service service = createService();
        ClientMetadataRespectBindingPortSEI port = service.getPort(ClientMetadataRespectBindingPortSEI.class);
        
        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertTrue("RespectBinding is not enabled", soapBinding.isRespectBindingEnabled());
    }
    
    /**
     * Validate that RespectBinding is disabled (by default) for a port that does not specify the feature.
     */
    public void testRespectBindingDefault() {
        Service service = createService();
        ClientMetadataRespectBindingPortSEI2 port = service.getPort(ClientMetadataRespectBindingPortSEI2.class);
        
        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertFalse("RespectBinding is enabled and should not be", soapBinding.isRespectBindingEnabled());
        
    }
    
    /**
     * Validate that RespectBinding can be enabled on some ports and not others by explicitly setting the features.
     */
    public void testRespectBindingEnableSomeNotOthersExplicitly() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Map<String, List<Annotation>> map = new HashMap();
        
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        // Enable RespectBinding explicitly on one port
        RespectBindingAnnot respectBindingFeature = new RespectBindingAnnot();
        respectBindingFeature.setEnabled(true);
        wsFeatures.add(respectBindingFeature);
        map.put(ClientMetadataRespectBindingPortSEI.class.getName(), wsFeatures);
        
        // Disable RespectBinding explicitly on a different port
        ArrayList<Annotation> wsFeatures2 = new ArrayList<Annotation>();
        RespectBindingAnnot respectBindingFeatureDisable = new RespectBindingAnnot();
        respectBindingFeatureDisable.setEnabled(false);
        wsFeatures2.add(respectBindingFeatureDisable);
        map.put(ClientMetadataRespectBindingPortSEI2.class.getName(), wsFeatures2);
        
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);

        // Validate that RespectBinding is enabled on one port and disabled on the other
        QName portQN1 = new QName(namespaceURI, multiPortWsdl_portLocalPart1);
        QName portQN2 = new QName(namespaceURI, multiPortWsdl_portLocalPart2);

        ClientMetadataRespectBindingPortSEI enabledPort = service.getPort(portQN1, ClientMetadataRespectBindingPortSEI.class);
        BindingProvider enabledBindingProvider = (BindingProvider) enabledPort;
        SOAPBinding enabledSoapBinding = (SOAPBinding) enabledBindingProvider.getBinding();
        assertTrue("RespectBinding is not enabled", enabledSoapBinding.isRespectBindingEnabled());
        
        ClientMetadataRespectBindingPortSEI2 disabledPort = service.getPort(portQN2, ClientMetadataRespectBindingPortSEI2.class);
        BindingProvider disabledBindingProvider = (BindingProvider) disabledPort;
        SOAPBinding disabledSoapBinding = (SOAPBinding) disabledBindingProvider.getBinding();
        assertFalse("RespectBinding is enabled and should not be", disabledSoapBinding.isRespectBindingEnabled());
    }
    
    /**
     * Validate that RespectBinding can be enabled on some ports and not others by not specifying the RespectBinding feature
     * on the ports it should not be enabled on.
     */
    public void testRespectBindingEnableSomeNotOthersUnspecified() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Map<String, List<Annotation>> map = new HashMap();
        
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        // Enable RespectBinding explicitly on one port
        RespectBindingAnnot respectBindingFeature = new RespectBindingAnnot();
        respectBindingFeature.setEnabled(true);
        wsFeatures.add(respectBindingFeature);
        map.put(ClientMetadataRespectBindingPortSEI.class.getName(), wsFeatures);
        
        // Do not specify the RespectBinding feature for the other port
        
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);

        // Validate that RespectBinding is enabled on one port and disabled on the other
        QName portQN1 = new QName(namespaceURI, multiPortWsdl_portLocalPart1);
        QName portQN2 = new QName(namespaceURI, multiPortWsdl_portLocalPart2);

        ClientMetadataRespectBindingPortSEI enabledPort = service.getPort(portQN1, ClientMetadataRespectBindingPortSEI.class);
        BindingProvider enabledBindingProvider = (BindingProvider) enabledPort;
        SOAPBinding enabledSoapBinding = (SOAPBinding) enabledBindingProvider.getBinding();
        assertTrue("RespectBinding is not enabled", enabledSoapBinding.isRespectBindingEnabled());
        
        ClientMetadataRespectBindingPortSEI2 disabledPort = service.getPort(portQN2, ClientMetadataRespectBindingPortSEI2.class);
        BindingProvider disabledBindingProvider = (BindingProvider) disabledPort;
        SOAPBinding disabledSoapBinding = (SOAPBinding) disabledBindingProvider.getBinding();
        assertFalse("RespectBinding is enabled and should not be", disabledSoapBinding.isRespectBindingEnabled());
    }
    
    /**
     * Validate if there are multiple instances of the same feature for a port in the list indicating
     * enablement, the last one is used.
     */
    public void testMultipleFeaturesForPortEnable() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Map<String, List<Annotation>> map = new HashMap();
        
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        // Enable RespectBinding explicitly then add another feature that disables it explicitly.
        RespectBindingAnnot respectBindingFeature = new RespectBindingAnnot();
        respectBindingFeature.setEnabled(true);
        wsFeatures.add(respectBindingFeature);
        
        RespectBindingAnnot mtomFeature2 = new RespectBindingAnnot();
        mtomFeature2.setEnabled(false);
        wsFeatures.add(mtomFeature2);

        map.put(ClientMetadataRespectBindingPortSEI.class.getName(), wsFeatures);

        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);

        QName portQN1 = new QName(namespaceURI, multiPortWsdl_portLocalPart1);

        ClientMetadataRespectBindingPortSEI enabledPort = service.getPort(portQN1, ClientMetadataRespectBindingPortSEI.class);
        BindingProvider enabledBindingProvider = (BindingProvider) enabledPort;
        SOAPBinding enabledSoapBinding = (SOAPBinding) enabledBindingProvider.getBinding();
        assertFalse("RespectBinding is enabled and should not be", enabledSoapBinding.isRespectBindingEnabled());
    }

    
    /**
     * Create a service as would be done via injection or lookup, including a sparse composite that 
     * contains features (as might be set by a deployment descriptor).
     * 
     * @return a Service created as done via injection or lookup.
     */
    private Service createService() {
        // Even for a port injection or lookup, the service will also be treated as an injection or lookup
        // So we need to setup the sparse DBC to create the service
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Map<String, List<Annotation>> map = new HashMap();
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        RespectBindingAnnot respectBindingFeature = new RespectBindingAnnot();
        respectBindingFeature.setEnabled(true);
        wsFeatures.add(respectBindingFeature);
        map.put(ClientMetadataRespectBindingPortSEI.class.getName(), wsFeatures);
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);
        return service;
    }

}

@WebService(name="EchoMessagePortType", targetNamespace="http://description.jaxws.axis2.apache.org")
interface ClientMetadataRespectBindingPortSEI {
    public String echoMessage(String string);
}

@WebService(name="EchoMessagePortType", targetNamespace="http://description.jaxws.axis2.apache.org")
interface ClientMetadataRespectBindingPortSEI2 {
    public String echoMessage(String string);
}
