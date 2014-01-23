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
 * Validate the setting up of the MTOM WebServiceFeature on the client side via meta-data 
 * (such as a deployment descriptor)
 */
public class ClientMetadataMTOMFeatureTests extends TestCase {
    static final String namespaceURI = "http://description.jaxws.axis2.apache.org";
    static final String svcLocalPart = "svcLocalPart";
    static final String multiPortWsdl = "ClientMetadataMultiPort.wsdl";
    static final String multiPortWsdl_portLocalPart1 = "portLocalPartMulti1";
    static final String multiPortWsdl_portLocalPart2 = "portLocalPartMulti2";


    public static final int MTOM_THRESHOLD = 3000;
    
    /**
     * Validate the default value of MTOM Threshold if MTOM is not enabled.
     */
    public void testDefaultThresholdWhenMTOMNotEnabled() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        Service service = Service.create(wsdlUrl, serviceQName);

        ClientMetadataMTOMPortSEI port = service.getPort(ClientMetadataMTOMPortSEI.class);

        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertFalse("MTOM is enabled but should not be", soapBinding.isMTOMEnabled());
        assertEquals("MTOM threshold default incorrect", 0, soapBinding.getMTOMThreshold());
    }

    /**
     * Validate the default value of MTOM Threshold if MTOM is enabled.
     */
    public void testDefaultThresholdWhenMTOMEnabled() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Map<String, List<Annotation>> map = new HashMap();
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        MTOMAnnot mtomFeature = new MTOMAnnot();
        // MTOM is enabled, but a threshold is not set
        mtomFeature.setEnabled(true);
        wsFeatures.add(mtomFeature);
        map.put(ClientMetadataMTOMPortSEI.class.getName(), wsFeatures);
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);
        
        ClientMetadataMTOMPortSEI port = service.getPort(ClientMetadataMTOMPortSEI.class);
        
        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertTrue("MTOM is not enabled but should be", soapBinding.isMTOMEnabled());
        assertEquals("MTOM threshold default incorrect", 0, soapBinding.getMTOMThreshold());
    }

    /**
     * Enable MTOM with the "old" way by setting MTOM isENabled directly on the service DBC.
     * Note this way will eventually be deprecated. 
     */
    public void testMTOMEnabledOldMTOMEnabled() {
        Service service = createService(true);
        ClientMetadataMTOMPortSEI port = service.getPort(ClientMetadataMTOMPortSEI.class);
        
        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertTrue("MTOM is not enabled", soapBinding.isMTOMEnabled());
    }
    
    /**
     * Validate that MTOM threshold can be set using the new feature property when MTOM is 
     * enabled the "old" way.
     */
    public void testMTOMThresholdOldMTOMEnabled() {
        Service service = createService(true);
        ClientMetadataMTOMPortSEI port = service.getPort(ClientMetadataMTOMPortSEI.class);
        
        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertEquals("MTOM threshold not set", MTOM_THRESHOLD, soapBinding.getMTOMThreshold());
    }
    
    /**
     * Validate that MTOM can be enabled using the new feature property without having to use the
     * old method of setting isMTOMEnabled directly on the service DBC
     */
    public void testMTOMEnablement() {
        Service service = createService(false);
        ClientMetadataMTOMPortSEI port = service.getPort(ClientMetadataMTOMPortSEI.class);
        
        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertTrue("MTOM is not enabled", soapBinding.isMTOMEnabled());
        
    }
    
    /**
     * Validate that if the NEW way of setting MTOM explcitly says MTOM is disabled and the OLD
     * way says it is enabled, we use the new setting and MTOM is disabled.
     */
    public void testMTOMEnablementConflict_NewOff_OldOn() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Map<String, List<Annotation>> map = new HashMap();
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        MTOMAnnot mtomFeature = new MTOMAnnot();
        // Explicitly disable MTOM with the new way of enabling it
        mtomFeature.setEnabled(false);
        mtomFeature.setThreshold(MTOM_THRESHOLD);
        wsFeatures.add(mtomFeature);
        map.put(ClientMetadataMTOMPortSEI.class.getName(), wsFeatures);
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        // Enable MTOM with the old way of enabling it
        serviceDBC.setIsMTOMEnabled(true);
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);
        
        ClientMetadataMTOMPortSEI port = service.getPort(ClientMetadataMTOMPortSEI.class);
        
        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertFalse("MTOM is enabled but should not be", soapBinding.isMTOMEnabled());
    }

    /**
     * Validate that if the NEW way of setting MTOM is not explcitly specified for a port and the OLD
     * way says it is enabled, we MTOM is enabled.
     */
    public void testMTOMEnablementConflict_NewUnspec_OldOn() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Map<String, List<Annotation>> map = new HashMap();
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        MTOMAnnot mtomFeature = new MTOMAnnot();
        // The port we do the get on will not have any feautres specified for it.
        mtomFeature.setEnabled(false);
        mtomFeature.setThreshold(MTOM_THRESHOLD);
        wsFeatures.add(mtomFeature);
        // Set the feature on a different port name than we will be using in getPort
        map.put(ClientMetadataMTOMPortSEI2.class.getName(), wsFeatures);
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        // Enable MTOM with the old way of enabling it
        serviceDBC.setIsMTOMEnabled(true);
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);
        
        ClientMetadataMTOMPortSEI port = service.getPort(ClientMetadataMTOMPortSEI.class);
        
        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertTrue("MTOM is not enabled", soapBinding.isMTOMEnabled());
    }
    
    /**
     * Validate that MTOM can be enabled on some ports and not others by explicitly setting the features.
     */
    public void testMTOMEnableSomeNotOthersExplicitly() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Map<String, List<Annotation>> map = new HashMap();
        
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        // Enable MTOM explicitly on one port
        MTOMAnnot mtomFeature = new MTOMAnnot();
        mtomFeature.setEnabled(true);
        mtomFeature.setThreshold(MTOM_THRESHOLD);
        wsFeatures.add(mtomFeature);
        map.put(ClientMetadataMTOMPortSEI.class.getName(), wsFeatures);
        
        // Disable MTOM explicitly on a different port
        ArrayList<Annotation> wsFeatures2 = new ArrayList<Annotation>();
        MTOMAnnot mtomFeatureDisable = new MTOMAnnot();
        mtomFeatureDisable.setEnabled(false);
        wsFeatures2.add(mtomFeatureDisable);
        map.put(ClientMetadataMTOMPortSEI2.class.getName(), wsFeatures2);
        
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);

        // Validate that MTOM is enabled on one port and disabled on the other
        QName portQN1 = new QName(namespaceURI, multiPortWsdl_portLocalPart1);
        QName portQN2 = new QName(namespaceURI, multiPortWsdl_portLocalPart2);

        ClientMetadataMTOMPortSEI enabledPort = service.getPort(portQN1, ClientMetadataMTOMPortSEI.class);
        BindingProvider enabledBindingProvider = (BindingProvider) enabledPort;
        SOAPBinding enabledSoapBinding = (SOAPBinding) enabledBindingProvider.getBinding();
        assertTrue("MTOM is not enabled", enabledSoapBinding.isMTOMEnabled());
        assertEquals("Threashold value incorrect", MTOM_THRESHOLD, enabledSoapBinding.getMTOMThreshold());
        
        ClientMetadataMTOMPortSEI2 disabledPort = service.getPort(portQN2, ClientMetadataMTOMPortSEI2.class);
        BindingProvider disabledBindingProvider = (BindingProvider) disabledPort;
        SOAPBinding disabledSoapBinding = (SOAPBinding) disabledBindingProvider.getBinding();
        assertFalse("MTOM is enabled and should not be", disabledSoapBinding.isMTOMEnabled());
        assertEquals("Threashold value incorrect", 0, disabledSoapBinding.getMTOMThreshold());
        
    }
    
    /**
     * Validate that MTOM can be enabled on some ports and not others by not specificing the MTOM feature
     * on the ports it should not be enabled on.
     */
    public void testMTOMEnableSomeNotOthersUnspecified() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Map<String, List<Annotation>> map = new HashMap();
        
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        // Enable MTOM explicitly on one port
        MTOMAnnot mtomFeature = new MTOMAnnot();
        mtomFeature.setEnabled(true);
        mtomFeature.setThreshold(MTOM_THRESHOLD);
        wsFeatures.add(mtomFeature);
        map.put(ClientMetadataMTOMPortSEI.class.getName(), wsFeatures);
        
        // Do not specify the MTOM feature for the other port
        
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);

        // Validate that MTOM is enabled on one port and disabled on the other
        QName portQN1 = new QName(namespaceURI, multiPortWsdl_portLocalPart1);
        QName portQN2 = new QName(namespaceURI, multiPortWsdl_portLocalPart2);

        ClientMetadataMTOMPortSEI enabledPort = service.getPort(portQN1, ClientMetadataMTOMPortSEI.class);
        BindingProvider enabledBindingProvider = (BindingProvider) enabledPort;
        SOAPBinding enabledSoapBinding = (SOAPBinding) enabledBindingProvider.getBinding();
        assertTrue("MTOM is not enabled", enabledSoapBinding.isMTOMEnabled());
        assertEquals("Threashold value incorrect", MTOM_THRESHOLD, enabledSoapBinding.getMTOMThreshold());
        
        ClientMetadataMTOMPortSEI2 disabledPort = service.getPort(portQN2, ClientMetadataMTOMPortSEI2.class);
        BindingProvider disabledBindingProvider = (BindingProvider) disabledPort;
        SOAPBinding disabledSoapBinding = (SOAPBinding) disabledBindingProvider.getBinding();
        assertFalse("MTOM is enabled and should not be", disabledSoapBinding.isMTOMEnabled());
        assertEquals("Threashold value incorrect", 0, disabledSoapBinding.getMTOMThreshold());
    }
    
    /**
     * Validate if there are multiple instances of the same feature for a port in the list indicating
     * a threshold, the last one is used.
     */
    public void testMultipleFeaturesForPortThreshold() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Map<String, List<Annotation>> map = new HashMap();
        
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        // Enable MTOM explicitly then add another feature that enables it with a different threshold.
        MTOMAnnot mtomFeature = new MTOMAnnot();
        mtomFeature.setEnabled(true);
        mtomFeature.setThreshold(MTOM_THRESHOLD + 10);
        wsFeatures.add(mtomFeature);
        
        MTOMAnnot mtomFeature3 = new MTOMAnnot();
        mtomFeature3.setEnabled(true);
        mtomFeature3.setThreshold(MTOM_THRESHOLD);
        wsFeatures.add(mtomFeature3);
        map.put(ClientMetadataMTOMPortSEI.class.getName(), wsFeatures);

        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);

        QName portQN1 = new QName(namespaceURI, multiPortWsdl_portLocalPart1);

        ClientMetadataMTOMPortSEI enabledPort = service.getPort(portQN1, ClientMetadataMTOMPortSEI.class);
        BindingProvider enabledBindingProvider = (BindingProvider) enabledPort;
        SOAPBinding enabledSoapBinding = (SOAPBinding) enabledBindingProvider.getBinding();
        assertTrue("MTOM is not enabled", enabledSoapBinding.isMTOMEnabled());
        assertEquals("Threashold value incorrect", MTOM_THRESHOLD, enabledSoapBinding.getMTOMThreshold());
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
        // Enable MTOM explicitly then add another feature that disables it explicitly.
        MTOMAnnot mtomFeature = new MTOMAnnot();
        mtomFeature.setEnabled(true);
        mtomFeature.setThreshold(MTOM_THRESHOLD);
        wsFeatures.add(mtomFeature);
        
        MTOMAnnot mtomFeature2 = new MTOMAnnot();
        mtomFeature2.setEnabled(false);
        wsFeatures.add(mtomFeature2);

        map.put(ClientMetadataMTOMPortSEI.class.getName(), wsFeatures);

        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);

        QName portQN1 = new QName(namespaceURI, multiPortWsdl_portLocalPart1);

        ClientMetadataMTOMPortSEI enabledPort = service.getPort(portQN1, ClientMetadataMTOMPortSEI.class);
        BindingProvider enabledBindingProvider = (BindingProvider) enabledPort;
        SOAPBinding enabledSoapBinding = (SOAPBinding) enabledBindingProvider.getBinding();
        assertFalse("MTOM is enabled and should not be", enabledSoapBinding.isMTOMEnabled());
    }

    /**
     * Create a service as would be done via injection or lookup, including a sparse composite that 
     * contains features (as might be set by a deployment descriptor).
     * 
     * @param oldMTOMEnablement use the old style of enabling MTOM if true
     * 
     * @return a Service created as done via injection or lookup.
     */
    private Service createService(boolean oldMTOMEnablement) {
        // Even for a port injection or lookup, the service will also be treated as an injection or lookup
        // So we need to setup the sparse DBC to create the service
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Map<String, List<Annotation>> map = new HashMap();
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        MTOMAnnot mtomFeature = new MTOMAnnot();
        mtomFeature.setEnabled(true);
        mtomFeature.setThreshold(MTOM_THRESHOLD);
        wsFeatures.add(mtomFeature);
        map.put(ClientMetadataMTOMPortSEI.class.getName(), wsFeatures);
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        if (oldMTOMEnablement) {
            serviceDBC.setIsMTOMEnabled(true);
        }
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);
        return service;
    }
}

@WebService(name="EchoMessagePortType", targetNamespace="http://description.jaxws.axis2.apache.org")
interface ClientMetadataMTOMPortSEI {
    public String echoMessage(String string);
}

@WebService(name="EchoMessagePortType2", targetNamespace="http://description.jaxws.axis2.apache.org")
interface ClientMetadataMTOMPortSEI2 {
    public String echoMessage(String string);
}