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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.DescriptionTestUtils;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.WebServiceClientAnnot;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceClient;
import java.net.URL;

/**
 * Test client sparse composite support in the MDQ layer at the Service creation level.
 */
public class ClientDBCSupportTests extends TestCase {
    private String namespaceURI = "http://org.apache.axis2.jaxws.description.impl.ClientDBCSupportTests";
    private String svcLocalPart = "svcLocalPart";
    private String portLocalPart = "portLocalPart";
    
    /**
     * Test the previous way of constructing a ServiceDescription and then updating it with an 
     * Endpoint.  This is verifying that the previous APIs work as expected.
     */
    public void testServiceAndSeiClass() {
        
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        
        ServiceDescription svcDesc = DescriptionFactory.createServiceDescription(null, serviceQName, ClientDBCSupportServiceSubclass.class);
        assertNotNull(svcDesc);
        ServiceDescriptionImpl svcDescImpl = (ServiceDescriptionImpl) svcDesc;
        DescriptionBuilderComposite svcDescComposite = svcDescImpl.getDescriptionBuilderComposite();
        assertNotNull(svcDescComposite);
        
        Class testServiceClass = svcDescComposite.getCorrespondingClass();
        assertNotNull(testServiceClass);
        assertEquals(ClientDBCSupportServiceSubclass.class, testServiceClass);
        
        // Now update with an SEI
        QName portQName = new QName(namespaceURI, portLocalPart);
        EndpointDescription epDesc = 
            DescriptionFactory.updateEndpoint(svcDesc, ClientDBCSupportSEI.class, portQName, DescriptionFactory.UpdateType.GET_PORT);
        assertNotNull(epDesc);
        EndpointDescriptionImpl epDescImpl = (EndpointDescriptionImpl) epDesc;
        DescriptionBuilderComposite epDescComposite = epDescImpl.getDescriptionBuilderComposite();
        Class seiClass = epDescComposite.getCorrespondingClass();
        assertEquals(ClientDBCSupportSEI.class, seiClass);
        // Make sure we didn't overwrite the class in the ServiceDesc composite
        assertEquals(ClientDBCSupportServiceSubclass.class, 
                     svcDescComposite.getCorrespondingClass());
    }
    
    /**
     * Create a ServiceDescription with a composite.  Nothing in the composite is overriden; validate
     * the values from the annotions in the Service class.
     */
    public void testClientServiceClassComposite() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        Object compositeKey = "CompositeKey";
        ServiceDescription svcDesc = 
            new ServiceDescriptionImpl(null, serviceQName, 
                                       ClientDBCSupportServiceSubclass.class, 
                                       composite, compositeKey);
        assertNotNull(svcDesc);
        ServiceDescriptionImpl svcDescImpl = (ServiceDescriptionImpl) svcDesc;
        DescriptionBuilderComposite svcDescComposite = svcDescImpl.getDescriptionBuilderComposite();
        assertNotNull(svcDescComposite);
        assertNotSame(composite, svcDescComposite);
        assertSame(composite, svcDescComposite.getSparseComposite(compositeKey));
        
        WebServiceClient wsClient = svcDescComposite.getWebServiceClientAnnot();
        assertNotNull(wsClient);
        assertEquals("originalWsdlLocation", wsClient.wsdlLocation());
        assertEquals("originalTNS", wsClient.targetNamespace());
        // We're testing the composite, not the metadata layer, so none of the defaulting logic
        // is exercised.
        assertEquals("", wsClient.name());
        
    }
    
    /**
     * Create a ServiceDescription using a sparse composite that overrides the wsdlLocation on the
     * WebServiceClient annotation.  Validate the override only affects the wsdlLocation and not
     * the other annotations members.
     */
    public void testServiceClientWSDLLocationOverride() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        // Create a composite with a WebServiceClient override of the WSDL location.
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        String overridenWsdlLocation = DescriptionTestUtils.getWSDLLocation("ClientEndpointMetadata.wsdl");
        WebServiceClientAnnot wsClientAnno = WebServiceClientAnnot.createWebServiceClientAnnotImpl(null, null, overridenWsdlLocation);
        composite.setWebServiceClientAnnot(wsClientAnno);
        Object compositeKey = "CompositeKey";
        ServiceDescription svcDesc = 
            new ServiceDescriptionImpl(null, serviceQName, 
                                       ClientDBCSupportServiceSubclass.class, 
                                       composite, compositeKey);
        assertNotNull(svcDesc);
        ServiceDescriptionImpl svcDescImpl = (ServiceDescriptionImpl) svcDesc;
        DescriptionBuilderComposite svcDescComposite = svcDescImpl.getDescriptionBuilderComposite();
        assertNotNull(svcDescComposite);
        assertNotSame(composite, svcDescComposite);
        assertSame(composite, svcDescComposite.getSparseComposite(compositeKey));
        // The client annot we set as a sparse composite should be the same.
        assertSame(wsClientAnno, svcDescComposite.getSparseComposite(compositeKey).getWebServiceClientAnnot());
        // The WebServiceClient annot on the service desc should represent the wsdl override from the
        // sparse composite 
        WebServiceClient wsClient = svcDescComposite.getWebServiceClientAnnot(compositeKey);
        assertEquals(overridenWsdlLocation, wsClient.wsdlLocation());
        // Make sure the non-overridden values still come from the service class annotation
        assertEquals("originalTNS", wsClient.targetNamespace());
        assertEquals("", wsClient.name());
        
    }
    
    /**
     * Test the ability to set a prefered port on a service description via  a sparse composite.
     */
    public void testPreferredPort() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        QName preferredPort = new QName("preferredTNS", "preferredLP");
        composite.setPreferredPort(preferredPort);
        Object compositeKey = "CompositeKey";
        ServiceDescription svcDesc = 
            DescriptionFactory.createServiceDescription(null, serviceQName, 
                                                        ClientDBCSupportServiceSubclass.class,
                                                        composite, compositeKey);
        DescriptionBuilderComposite svcDescComposite = DescriptionTestUtils.getServiceDescriptionComposite(svcDesc);
        assertNotNull(svcDescComposite);
        assertNull(svcDescComposite.getPreferredPort());
        DescriptionBuilderComposite svcDescSparseComposite = svcDescComposite.getSparseComposite(compositeKey);
        assertNotNull(svcDescSparseComposite);
        assertSame(preferredPort, svcDescSparseComposite.getPreferredPort());
        assertSame(preferredPort, svcDescComposite.getPreferredPort(compositeKey));
    }

    /**
     * Test the ability to set MTOM enablement on a service description via a sparse composite.
     */
    public void testMTOMEnablement() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        composite.setIsMTOMEnabled(true);
        Object compositeKey = "CompositeKey";
        ServiceDescription svcDesc = 
            DescriptionFactory.createServiceDescription(null, serviceQName, 
                                                        ClientDBCSupportServiceSubclass.class,
                                                        composite, compositeKey);
        DescriptionBuilderComposite svcDescComposite = DescriptionTestUtils.getServiceDescriptionComposite(svcDesc);
        assertNotNull(svcDescComposite);
        assertFalse(svcDescComposite.isMTOMEnabled());
        DescriptionBuilderComposite svcDescSparseComposite = svcDescComposite.getSparseComposite(compositeKey);
        assertNotNull(svcDescSparseComposite);
        assertTrue(svcDescSparseComposite.isMTOMEnabled());
        assertTrue(svcDescComposite.isMTOMEnabled(compositeKey));
    }

}

@WebServiceClient(targetNamespace="originalTNS", wsdlLocation="originalWsdlLocation")
class ClientDBCSupportServiceSubclass extends javax.xml.ws.Service {

    protected ClientDBCSupportServiceSubclass(URL wsdlDocumentLocation, QName serviceName) {
        super(wsdlDocumentLocation, serviceName);
    }
}

@WebService
interface ClientDBCSupportSEI {
    public String echo (String string);
}

@WebService(serviceName = "EchoService", endpointInterface="org.apache.ws.axis2.tests.EchoPort")
class ClientDBCSupportEchoServiceImplWithSEI {
    public void echo(Holder<String> text) {
        text.value = "Echo " + text.value;
    }

}
