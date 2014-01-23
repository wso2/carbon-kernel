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
package org.apache.axis2.jaxws.client.proxy;

import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.jaxws.client.InterceptableClientTestCase;
import org.apache.axis2.jaxws.client.TestClientInvocationController;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.builder.AddressingAnnot;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.axis2.jaxws.spi.ServiceDelegate;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.AddressingFeature.Responses;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validate that addressing can be configured via metadata (such as via a deployment descriptor)
 */
public class ProxyAddressingMetadataTest extends InterceptableClientTestCase {
    
    /**
     * Validate when no addressing-related metadata is set.
     */
    public void testAddressingNoMetadata() {
        Service svc = Service.create(new QName("http://test", "ProxyAddressingService"));
        ProxyAddressingService proxy = svc.getPort(ProxyAddressingService.class);
        assertNotNull(proxy);
        
        proxy.doSomething("12345");
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext request = ic.getRequestMessageContext();
        
        String version = (String) request.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        Boolean disabled = (Boolean) request.getProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        String responses = (String) request.getProperty(AddressingConstants.WSAM_INVOCATION_PATTERN_PARAMETER_NAME);
        String required = (String) request.getProperty(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER);
        
        assertNull(version);
        assertTrue(disabled);
        assertNull(responses);
        assertNull(required);
    }

    /**
     * Validate correct behavior when addressing-related metadata is specified in a sparse composite, such as
     * would be used to represent configuration via a Deployment Descriptor.
     */
    public void testAddressingMetadata() {
        Map<String, List<Annotation>> map = new HashMap();
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        AddressingAnnot addressingFeature = new AddressingAnnot();
        addressingFeature.setEnabled(true);
        addressingFeature.setRequired(true);
        addressingFeature.setResponses(Responses.NON_ANONYMOUS);
        wsFeatures.add(addressingFeature);
        map.put(ProxyAddressingService.class.getName(), wsFeatures);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        ServiceDelegate.setServiceMetadata(serviceDBC);

        Service svc = Service.create(new QName("http://test", "ProxyAddressingService"));
        ProxyAddressingService proxy = svc.getPort(ProxyAddressingService.class);
        assertNotNull(proxy);
        
        proxy.doSomething("12345");
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext request = ic.getRequestMessageContext();
        
        String version = (String) request.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        assertNotNull("Version not set", version);
        assertEquals("Wrong addressing version", Final.WSA_NAMESPACE, version);
        
        String required = (String) request.getProperty(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER);
        assertNotNull("Required not set", required);
        assertEquals("Wrong addressing required", AddressingConstants.ADDRESSING_REQUIRED, required);


        Boolean disabled = (Boolean) request.getProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        assertNotNull("Disabled not set", disabled);
        assertFalse("Addressing disabled", disabled);

        String responses = (String) request.getProperty(AddressingConstants.WSAM_INVOCATION_PATTERN_PARAMETER_NAME);
        assertNotNull("Responses not set", responses);
        assertEquals("Wrong responses value", AddressingConstants.WSAM_INVOCATION_PATTERN_ASYNCHRONOUS, responses);
        
    }

    /**
     * Validate correct behavior when addressing is disabled via addressing-related metadata specified in a sparse composite, such as
     * would be used to represent configuration via a Deployment Descriptor.
     */
    public void testAddressingMetadataDisabled() {
        Map<String, List<Annotation>> map = new HashMap();
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        AddressingAnnot addressingFeature = new AddressingAnnot();
        addressingFeature.setEnabled(false);
        addressingFeature.setRequired(true);
        addressingFeature.setResponses(Responses.NON_ANONYMOUS);
        wsFeatures.add(addressingFeature);
        map.put(ProxyAddressingService.class.getName(), wsFeatures);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        ServiceDelegate.setServiceMetadata(serviceDBC);

        Service svc = Service.create(new QName("http://test", "ProxyAddressingService"));
        ProxyAddressingService proxy = svc.getPort(ProxyAddressingService.class);
        assertNotNull(proxy);
        
        proxy.doSomething("12345");
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext request = ic.getRequestMessageContext();
        
        // If addressing is not enabled the version should not be set
        String version = (String) request.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        assertNull("Version set", version);
        
        Boolean disabled = (Boolean) request.getProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        assertNotNull("Disabled not set", disabled);
        assertTrue("Addressing disabled", disabled);

        // Even though required=true above, per the addressing developers, they want to leave it set as unspecified when
        // addressing is not enabled.
        String required = (String) request.getProperty(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER);
        assertNotNull("Required not set", required);
        assertEquals("Wrong addressing required", AddressingConstants.ADDRESSING_UNSPECIFIED, required);

        String responses = (String) request.getProperty(AddressingConstants.WSAM_INVOCATION_PATTERN_PARAMETER_NAME);
        assertNotNull("Responses not set", responses);
        assertEquals("Wrong responses value", AddressingConstants.WSAM_INVOCATION_PATTERN_ASYNCHRONOUS, responses);
        
    }

    @WebService()
    public interface ProxyAddressingService {
    
        public String doSomething(String id);
        
    }

}
