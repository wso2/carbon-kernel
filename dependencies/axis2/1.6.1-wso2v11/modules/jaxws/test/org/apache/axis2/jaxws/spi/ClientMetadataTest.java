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

package org.apache.axis2.jaxws.spi;

import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.jaxws.catalog.impl.OASISCatalogManager;
import org.apache.axis2.jaxws.description.DescriptionTestUtils2;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.WebServiceClientAnnot;
import org.apache.axis2.jaxws.description.impl.DescriptionFactoryImpl;
import org.apache.axis2.metadata.registry.MetadataFactoryRegistry;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

/**
 * Test creation of a generic Service and generated Service with and without additional
 * metadata specified during the creation of the service.  Additional metadata could be supplied
 * by a runtime in order to support JSR-109 deployment descriptors or injection of WebServiceRef
 * annotations. 
 */
public class ClientMetadataTest extends TestCase {
    static final String namespaceURI = "http://description.jaxws.axis2.apache.org";
    static final String svcLocalPart = "svcLocalPart";

    static final String originalWsdl = "ClientMetadata.wsdl";
    static final String overridenWsdl = "ClientMetadataOverriden.wsdl";
    static final String otherWsdl = "ClientMetadataOther.wsdl";

    static final String originalWsdl_portLocalPart = "portLocalPart";
    static final String overridenWsdl_portLocalPart = "portLocalPartOverriden";
    static final String otherWsdl_portLocalPart = "portLocalPartOther";
    
    static final String uniqueCatalog = "test-resources/unique-catalog.xml";

    /**
     * Test Service.create(QName) with no composite specified 
     */
    public void test1ArgServiceWithoutComposite() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        Service service = Service.create(serviceQName);
        assertNotNull(service);
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertNotNull(dbcInServiceDesc);
        assertEquals(Service.class, dbcInServiceDesc.getCorrespondingClass());
        // Since this is a generic Service with no overrides, there will be no WebServiceClient annotation
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNull(wsClient);
        
        // No WSDL should have been used, so no Ports should be found
        assertTrue("Wrong WSDL used", validatePort(service, null));
    }    

    /**
     * Service.create(URL, QName) with no composite specified
     */
    public void test2ArgServiceWithoutComposite() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = getWsdlURL(otherWsdl);
        Service service = Service.create(wsdlUrl, serviceQName);
        assertNotNull(service);
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertNotNull(dbcInServiceDesc);
        assertEquals(Service.class, dbcInServiceDesc.getCorrespondingClass());
        // Since this is a generic Service with no overrides, there will be no WebServiceClient annotation
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNull(wsClient);

        // WSDL was specified on the create, so make sure the right one was used by checking the ports
        assertTrue("Wrong WSDL used", validatePort(service, otherWsdl_portLocalPart));
    }
    
    /**
     * Service.create(QName) with a composite specified but no override in the composite 
     */
    public void test1ArgServiceWithComposite() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        assertNull(ServiceDelegate.getServiceMetadata());
        // Use the proprietary SPI to create a service with additional metadata specified
        ServiceDelegate.setServiceMetadata(composite);
        
        Service service = Service.create(serviceQName);
        assertNotNull(service);
        
        // Verify that the composite has been reset so that it would not affect the next Service
        assertNull(ServiceDelegate.getServiceMetadata());
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(Service.class, dbcInServiceDesc.getCorrespondingClass());
        // Since this is a generic Service with no overrides, there will be no WebServiceClient annotation
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNull(wsClient);
        
        // No WSDL should have been used, so no Ports should be found
        assertTrue("Wrong WSDL used", validatePort(service, null));

    }
    
    /**
     * Service.create(URL, QName) with a composite specified but no override in the composite 
     */
    public void test2ArgServiceWithComposite() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);

        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        URL wsdlUrl = getWsdlURL(otherWsdl);
        // Use the proprietary SPI to create a service with additional metadata specified
        ServiceDelegate.setServiceMetadata(composite);
        Service service = Service.create(wsdlUrl, serviceQName);
        assertNotNull(service);
        // Verify that the composite has been reset so that it would not affect the next Service
        assertNull(ServiceDelegate.getServiceMetadata());
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(Service.class, dbcInServiceDesc.getCorrespondingClass());
        // Since this is a generic Service with no overrides, there will be no WebServiceClient annotation
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNull(wsClient);
        
        // WSDL was specified on the create, so make sure the right one was used by checking the ports
        assertTrue("Wrong WSDL used", validatePort(service, otherWsdl_portLocalPart));
    }
    
    /**
     * Generated service constructor() with no composite specified 
     */
    public void testNoArgGeneratedService() {
        Service service = new ClientMetadataGeneratedService();
        assertNotNull(service);

        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertEquals(ClientMetadataGeneratedService.class, dbcInServiceDesc.getCorrespondingClass());
        // There is WebServiceClient on the generated Service
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNotNull(wsClient);
        assertEquals(originalWsdl, wsClient.wsdlLocation());
        assertEquals("originalTNS", wsClient.targetNamespace());
        assertEquals("", wsClient.name());
        
        // WSDL not specified, so generated Service should use the annotation value
        assertTrue("Wrong WSDL Used", validatePort(service, originalWsdl_portLocalPart));
    }
    
    /**
     * Generated service constructor(URL, QName) with no composite specified 
     */
    public void test2ArgGeneratedService() {
        
        Service service = new ClientMetadataGeneratedService(getWsdlURL(otherWsdl), 
                                                              new QName(namespaceURI, svcLocalPart));
        assertNotNull(service);

        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertEquals(ClientMetadataGeneratedService.class, dbcInServiceDesc.getCorrespondingClass());
        // There is WebServiceClient on the generated Service
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNotNull(wsClient);

        assertEquals(originalWsdl, wsClient.wsdlLocation());
        assertEquals("originalTNS", wsClient.targetNamespace());
        assertEquals("", wsClient.name());

        // WSDL was specified on the generated Service constructor, 
        // so make sure the right one was used by checking the ports
        assertTrue("Wrong WSDL used", validatePort(service, otherWsdl_portLocalPart));
    }
    
    /**
     * Generated service constructor() with composite specified but no override in composite 
     */
    public void testNoArgGeneratedServiceWithComposite() {
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        ServiceDelegate.setServiceMetadata(composite);

        Service service = new ClientMetadataGeneratedService();
        assertNotNull(service);
        assertNull(ServiceDelegate.getServiceMetadata());
        
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(ClientMetadataGeneratedService.class, dbcInServiceDesc.getCorrespondingClass());
        // There is WebServiceClient on the generated Service and it wasn't overriden in the composite
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNotNull(wsClient);
        assertEquals(originalWsdl, wsClient.wsdlLocation());
        assertEquals("originalTNS", wsClient.targetNamespace());
        assertEquals("", wsClient.name());
        
        // No WSDL override specified in the composite, so generated Service should use the annotation value
        assertTrue("Wrong WSDL Used", validatePort(service, originalWsdl_portLocalPart));

    }

    /**
     * Generated service constructor(URL, QName) with composite specified but no override in composite 
     */
    public void test2ArgGeneratedServiceWithComposite() {
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        ServiceDelegate.setServiceMetadata(composite);

        Service service = new ClientMetadataGeneratedService(getWsdlURL(otherWsdl),
                                                             new QName(namespaceURI, svcLocalPart));
        assertNotNull(service);
        assertNull(ServiceDelegate.getServiceMetadata());
        
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(ClientMetadataGeneratedService.class, dbcInServiceDesc.getCorrespondingClass());
        // There is WebServiceClient on the generated Service and it wasn't overriden in the composite
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNotNull(wsClient);
        assertEquals(originalWsdl, wsClient.wsdlLocation());
        assertEquals("originalTNS", wsClient.targetNamespace());
        assertEquals("", wsClient.name());

        // WSDL was specified on the generated Service constructor, and none in the composite
        // so should get the WSDL specified on the constructor
        assertTrue("Wrong WSDL used", validatePort(service, otherWsdl_portLocalPart));
    }
    
    /**
     * Service.create(QName) with a composite that specifies a wsdlLocation override 
     */
    public void test1ArgServiceOverrideWsdlLocation() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        WebServiceClientAnnot wsClientAnno = 
            WebServiceClientAnnot.createWebServiceClientAnnotImpl(null, null, getWsdlLocation(overridenWsdl));
        composite.setWebServiceClientAnnot(wsClientAnno);
        // Use the proprietary SPI to create a service with additional metadata specified
        ServiceDelegate.setServiceMetadata(composite);
        
        Service service = Service.create(serviceQName);
        
        assertNotNull(service);
        // Verify that the composite has been reset so that it would not affect the next Service
        assertNull(ServiceDelegate.getServiceMetadata());
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(Service.class, dbcInServiceDesc.getCorrespondingClass());
        // This is a generic Service with overrides, there will be WebServiceClient annotation
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNull(wsClient);
        wsClient = dbcInServiceDesc.getWebServiceClientAnnot(serviceDelegate);
        assertNotNull(wsClient);
        assertEquals(getWsdlLocation(overridenWsdl), wsClient.wsdlLocation());
        assertNull(wsClient.targetNamespace());
        assertNull(wsClient.name());
        
        // WSDL override specified in the composite
        assertTrue("Wrong WSDL used", validatePort(service, overridenWsdl_portLocalPart));
    }
    
    /**
     * Service.create(QName) with a composite that overrides the TargetNamespace.  Most of the
     * other tests override the WSDL Location since that is what a JSR-109 DD can specify.  This
     * test makes sure other values can be overridden as well
     */
    public void test1ArgServiceOverrideTNS() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        WebServiceClientAnnot wsClientAnno = 
            WebServiceClientAnnot.createWebServiceClientAnnotImpl(null, "overrideTNS", null);
        composite.setWebServiceClientAnnot(wsClientAnno);
        // Use the proprietary SPI to create a service with additional metadata specified
        ServiceDelegate.setServiceMetadata(composite);
        
        Service service = Service.create(serviceQName);
        
        assertNotNull(service);
        // Verify that the composite has been reset so that it would not affect the next Service
        assertNull(ServiceDelegate.getServiceMetadata());
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(Service.class, dbcInServiceDesc.getCorrespondingClass());
        // The target namespace for the key should be overriden; it should not be overriden for
        // no key.
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNull(wsClient);
        
        WebServiceClient wsClientKeyed = dbcInServiceDesc.getWebServiceClientAnnot(serviceDelegate);
        assertNotNull(wsClientKeyed);
        assertNull(wsClientKeyed.wsdlLocation());
        assertEquals("overrideTNS", wsClientKeyed.targetNamespace());
        assertNull(wsClientKeyed.name());

    }
    
    /**
     * Service.create(QName) with a composite that specifies a CatalogManager override 
     */
    public void test1ArgServiceOverrideCatalogManager() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        OASISCatalogManager catalogManager = new OASISCatalogManager();
        catalogManager.setCatalogFiles(getCatalogLocation(uniqueCatalog));
        composite.setCatalogManager(catalogManager);
        // Use the proprietary SPI to create a service with additional metadata specified
        ServiceDelegate.setServiceMetadata(composite);
        Service service = Service.create(serviceQName);
        
        assertNotNull(service);
        // Verify that the composite has been reset so that it would not affect the next Service
        assertNull(ServiceDelegate.getServiceMetadata());
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(Service.class, dbcInServiceDesc.getCorrespondingClass());
        // Verify that the CatalogManager for the Service uses the unique catalog file.
        String serviceCatalogFile = (String) dbcInServiceDesc.getSparseComposite(serviceDelegate).getCatalogManager().getCatalogFiles().get(0);
        assertEquals(serviceCatalogFile, getCatalogLocation(uniqueCatalog));
    }
    
    /**
     * Service.create(URL, QName) with a composite that specifies a wsdlLocation override 
     */
    public void test2ArgServiceOverrideWsdlLocation() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = getWsdlURL(otherWsdl);
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        WebServiceClientAnnot wsClientAnno = WebServiceClientAnnot.createWebServiceClientAnnotImpl(null, null, getWsdlLocation(overridenWsdl));
        composite.setWebServiceClientAnnot(wsClientAnno);
        // Use the proprietary SPI to create a service with additional metadata specified
        ServiceDelegate.setServiceMetadata(composite);
        Service service = Service.create(wsdlUrl, serviceQName);
        
        assertNotNull(service);
        // Verify that the composite has been reset so that it would not affect the next Service
        assertNull(ServiceDelegate.getServiceMetadata());
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(Service.class, dbcInServiceDesc.getCorrespondingClass());
        // Since this is a generic Service with  overrides, there will be a WebServiceClient annotation
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNull(wsClient);
        wsClient = dbcInServiceDesc.getWebServiceClientAnnot(serviceDelegate);
        assertNotNull(wsClient);
        assertEquals(getWsdlLocation(overridenWsdl), wsClient.wsdlLocation());
        assertNull(wsClient.targetNamespace());
        assertNull(wsClient.name());

        // WSDL override specified in the composite
        assertTrue("Wrong WSDL used", validatePort(service, overridenWsdl_portLocalPart));

    }
    
    /**
     * Service.create(URL, QName) with a composite that specifies a CatalogManager override 
     */
    public void test2ArgServiceOverrideCatalogManager() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = getWsdlURL(otherWsdl);
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        OASISCatalogManager catalogManager = new OASISCatalogManager();
        catalogManager.setCatalogFiles(getCatalogLocation(uniqueCatalog));
        composite.setCatalogManager(catalogManager);
        // Use the proprietary SPI to create a service with additional metadata specified
        ServiceDelegate.setServiceMetadata(composite);
        Service service = Service.create(wsdlUrl, serviceQName);
        
        assertNotNull(service);
        // Verify that the composite has been reset so that it would not affect the next Service
        assertNull(ServiceDelegate.getServiceMetadata());
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(Service.class, dbcInServiceDesc.getCorrespondingClass());

        // Verify that the CatalogManager for the Service uses the unique catalog file.
        String serviceCatalogFile = (String) dbcInServiceDesc.getSparseComposite(serviceDelegate).getCatalogManager().getCatalogFiles().get(0);
        assertEquals(serviceCatalogFile, getCatalogLocation(uniqueCatalog));
    }

    /**
     * Generated service constructor() with a composite that specifies a wsdlLocation override
     */
    public void testNoArgGeneratedServiceOverrideWsdlLocation() {
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        WebServiceClientAnnot wsClientAnno = WebServiceClientAnnot.createWebServiceClientAnnotImpl(null, null, getWsdlLocation(overridenWsdl));
        composite.setWebServiceClientAnnot(wsClientAnno);
        ServiceDelegate.setServiceMetadata(composite);

        Service service = new ClientMetadataGeneratedService();

        assertNotNull(service);
        assertNull(ServiceDelegate.getServiceMetadata());
        
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(ClientMetadataGeneratedService.class, dbcInServiceDesc.getCorrespondingClass());
        // There is WebServiceClient on the generated Service and it was overriden in the composite
        // for this key, however the keyless composite should not have any overrides.
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNotNull(wsClient);
        assertEquals(originalWsdl, wsClient.wsdlLocation());
        assertEquals("originalTNS", wsClient.targetNamespace());
        assertEquals("", wsClient.name());
        
        WebServiceClient wsClientKeyed = dbcInServiceDesc.getWebServiceClientAnnot(serviceDelegate);
        assertNotSame(wsClient, wsClientKeyed);
        assertEquals(getWsdlLocation(overridenWsdl), wsClientKeyed.wsdlLocation());
        assertEquals("originalTNS", wsClientKeyed.targetNamespace());
        assertEquals("", wsClientKeyed.name());
        
        // WSDL override specified in the composite
        assertTrue("Wrong WSDL used", validatePort(service, overridenWsdl_portLocalPart));
    }
    
    /**
     * Generated service constructor() with a composite that specifies a CatalogManager override
     */
    public void testNoArgGeneratedServiceOverrideCatalogManager() {
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        OASISCatalogManager catalogManager = new OASISCatalogManager();
        catalogManager.setCatalogFiles(getCatalogLocation(uniqueCatalog));
        composite.setCatalogManager(catalogManager);
        ServiceDelegate.setServiceMetadata(composite);

        Service service = new ClientMetadataGeneratedService();

        assertNotNull(service);
        assertNull(ServiceDelegate.getServiceMetadata());
        
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(ClientMetadataGeneratedService.class, dbcInServiceDesc.getCorrespondingClass());

        // Verify that the CatalogManager for the Service uses the unique catalog file.
        String serviceCatalogFile = (String) dbcInServiceDesc.getSparseComposite(serviceDelegate).getCatalogManager().getCatalogFiles().get(0);
        assertEquals(serviceCatalogFile, getCatalogLocation(uniqueCatalog));
    }
    
    /**
     * Generated service constructor(URL, QName) with a composite that specifies a wsdlLocation override
     */
    public void test2ArgGeneratedServiceOverrideWsdlLocation() {
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        WebServiceClientAnnot wsClientAnno = WebServiceClientAnnot.createWebServiceClientAnnotImpl(null, null, getWsdlLocation(overridenWsdl));
        composite.setWebServiceClientAnnot(wsClientAnno);
        ServiceDelegate.setServiceMetadata(composite);

        Service service = new ClientMetadataGeneratedService(getWsdlURL(otherWsdl),
                                                             new QName(namespaceURI, svcLocalPart));

        assertNotNull(service);
        assertNull(ServiceDelegate.getServiceMetadata());
        
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(ClientMetadataGeneratedService.class, dbcInServiceDesc.getCorrespondingClass());
        // There is WebServiceClient on the generated Service and it was overriden in the composite
        // for this key, however the keyless composite should not have any overrides.
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNotNull(wsClient);
        assertEquals(originalWsdl, wsClient.wsdlLocation());
        assertEquals("originalTNS", wsClient.targetNamespace());
        assertEquals("", wsClient.name());

        WebServiceClient wsClientKeyed = dbcInServiceDesc.getWebServiceClientAnnot(serviceDelegate);
        assertNotSame(wsClient, wsClientKeyed);
        
        assertEquals(getWsdlLocation(overridenWsdl), wsClientKeyed.wsdlLocation());
        assertEquals("originalTNS", wsClientKeyed.targetNamespace());
        assertEquals("", wsClientKeyed.name());

        // WSDL override specified in the composite
        assertTrue("Wrong WSDL used", validatePort(service, overridenWsdl_portLocalPart));
    }
    
    /**
     * Generated service constructor(URL, QName) with a composite that specifies a 
     * target Namespace override.  Most of the other tests are based on wsdlLocation since
     * that is what JSR-109 DDs override.  This test verifies that other members can also
     * be override.
     */
    public void test2ArgGeneratedServiceOverrideTNS() {
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        WebServiceClientAnnot wsClientAnno = WebServiceClientAnnot.createWebServiceClientAnnotImpl(null, "overrideTNS", getWsdlLocation(overridenWsdl));
        composite.setWebServiceClientAnnot(wsClientAnno);
        ServiceDelegate.setServiceMetadata(composite);

        Service service = new ClientMetadataGeneratedService(getWsdlURL(otherWsdl),
                                                             new QName(namespaceURI, svcLocalPart));

        assertNotNull(service);
        assertNull(ServiceDelegate.getServiceMetadata());
        
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(ClientMetadataGeneratedService.class, dbcInServiceDesc.getCorrespondingClass());
        // There is WebServiceClient on the generated Service and it was overriden in the composite
        // for this key, however the keyless composite should not have any overrides.
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNotNull(wsClient);
        assertEquals(originalWsdl, wsClient.wsdlLocation());
        assertEquals("originalTNS", wsClient.targetNamespace());
        assertEquals("", wsClient.name());

        WebServiceClient wsClientKeyed = dbcInServiceDesc.getWebServiceClientAnnot(serviceDelegate);
        assertNotSame(wsClient, wsClientKeyed);
        
        assertEquals(getWsdlLocation(overridenWsdl), wsClientKeyed.wsdlLocation());
        assertEquals("overrideTNS", wsClientKeyed.targetNamespace());
        assertEquals("", wsClientKeyed.name());

        // WSDL override specified in the composite
        assertTrue("Wrong WSDL used", validatePort(service, overridenWsdl_portLocalPart));
    }
    
    /**
     * Generated service constructor(URL, QName) with a composite that specifies a 
     * Catalog Manager override.  
     */
    public void test2ArgGeneratedServiceOverrideCatalogManager() {
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        OASISCatalogManager catalogManager = new OASISCatalogManager();
        catalogManager.setCatalogFiles(getCatalogLocation(uniqueCatalog));
        composite.setCatalogManager(catalogManager);
        ServiceDelegate.setServiceMetadata(composite);

        Service service = new ClientMetadataGeneratedService(getWsdlURL(otherWsdl),
                                                             new QName(namespaceURI, svcLocalPart));

        assertNotNull(service);
        assertNull(ServiceDelegate.getServiceMetadata());
        
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(ClientMetadataGeneratedService.class, dbcInServiceDesc.getCorrespondingClass());

        // Verify that the CatalogManager for the Service uses the unique catalog file.
        String serviceCatalogFile = (String) dbcInServiceDesc.getSparseComposite(serviceDelegate).getCatalogManager().getCatalogFiles().get(0);
        assertEquals(serviceCatalogFile, getCatalogLocation(uniqueCatalog));
    }

    /**
     * Generated service constructor(URL, QName) with a composite that specifies a wsdlLocation override
     * where the override is a fully specifed URL to a file.
     */
    public void test2ArgGeneratedServiceOverrideWsdlLocationWithProtocol() {
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        // If the wsdlLocation in the composite specifies a protocol (like file: or http:) then
        // it should be used as-is.  Otherwise (as shown by the other tests), it is treated as
        // a path on the local filesystem.
        String wsdlLocation = getWsdlLocation(overridenWsdl);
        // This check is necessary because Unix/Linux file paths begin
        // with a '/'. When adding the prefix 'jar:file:/' we may end
        // up with '//' after the 'file:' part. This causes the URL 
        // object to treat this like a remote resource
        if(wsdlLocation.indexOf("/") == 0) {
            wsdlLocation = wsdlLocation.substring(1, wsdlLocation.length());
        }

        String fullWsdlLocation = "file:/" + wsdlLocation;
        WebServiceClientAnnot wsClientAnno = WebServiceClientAnnot.createWebServiceClientAnnotImpl(null, null, fullWsdlLocation);
        composite.setWebServiceClientAnnot(wsClientAnno);
        ServiceDelegate.setServiceMetadata(composite);

        Service service = new ClientMetadataGeneratedService(getWsdlURL(otherWsdl),
                                                             new QName(namespaceURI, svcLocalPart));

        assertNotNull(service);
        assertNull(ServiceDelegate.getServiceMetadata());
        
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertSame(composite, dbcInServiceDesc.getSparseComposite(serviceDelegate));
        assertEquals(ClientMetadataGeneratedService.class, dbcInServiceDesc.getCorrespondingClass());
        // There is WebServiceClient on the generated Service and it was overriden in the composite
        // for the key.  The annotation with no key should be unchanged.
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNotNull(wsClient);
        assertEquals(originalWsdl, wsClient.wsdlLocation());
        assertEquals("originalTNS", wsClient.targetNamespace());
        assertEquals("", wsClient.name());

        WebServiceClient wsClientKeyed = dbcInServiceDesc.getWebServiceClientAnnot(serviceDelegate);
        assertNotSame(wsClient, wsClientKeyed);
        assertEquals(fullWsdlLocation, wsClientKeyed.wsdlLocation());
        assertEquals("originalTNS", wsClientKeyed.targetNamespace());
        assertEquals("", wsClientKeyed.name());

        // WSDL override specified in the composite
        assertTrue("Wrong WSDL used", validatePort(service, overridenWsdl_portLocalPart));
    }
    
    /**
     * Test override WSDL file that is full specified
     */
    public void testInvalidWsdlLocationOverrideWithProtocol() {
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        String wsdlLocation = getWsdlLocation("InvalidFileName.wsdl");
        // This check is necessary because Unix/Linux file paths begin
        // with a '/'. When adding the prefix 'jar:file:/' we may end
        // up with '//' after the 'file:' part. This causes the URL 
        // object to treat this like a remote resource
        if(wsdlLocation.indexOf("/") == 0) {
            wsdlLocation = wsdlLocation.substring(1, wsdlLocation.length());
        }

        String fullWsdlLocation = "http:/" + wsdlLocation;
        
        WebServiceClientAnnot wsClientAnno = WebServiceClientAnnot.createWebServiceClientAnnotImpl(null, null, fullWsdlLocation);
        composite.setWebServiceClientAnnot(wsClientAnno);
        ServiceDelegate.setServiceMetadata(composite);

        try {
            Service service = new ClientMetadataGeneratedService();
            fail("Should have caught exception for invalid WSDL file name in override");
        } catch (WebServiceException ex) {
            // Expected path
        }
    }

    /**
     * The overide WSDL file doesn't exist; should catch an error 
     */
    public void testInvalidWsdlLocationOverride() {
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        WebServiceClientAnnot wsClientAnno = WebServiceClientAnnot.createWebServiceClientAnnotImpl(null, null, getWsdlLocation("InvalidFileName.wsdl"));
        composite.setWebServiceClientAnnot(wsClientAnno);
        ServiceDelegate.setServiceMetadata(composite);

        try {
            Service service = new ClientMetadataGeneratedService();
            fail("Should have caught exception for invalid WSDL file name in override");
        } catch (WebServiceException ex) {
            // Expected path
        }
    }

    /**
     * Create multiple instances of the same service.  Validate that the service delegates are
     * unique and the ServiceDescription is shared.  Note we have to install a special factory
     * so that the ServiceDescriptions are cached; the default factory will not cause them to be
     * cached.
     */
    public void testMultipleServices() {
        try {
            installCachingFactory();
            QName serviceQName = new QName(namespaceURI, svcLocalPart);
            QName portQN = new QName(namespaceURI, otherWsdl_portLocalPart);
            URL wsdlUrl = ClientMetadataTest.getWsdlURL(otherWsdl);

            // Create the first service 
            Service service1 = Service.create(wsdlUrl, serviceQName);
            ServiceDelegate serviceDelegate1 = DescriptionTestUtils2.getServiceDelegate(service1);
            assertNull(ServiceDelegate.getServiceMetadata());
            ServiceDescription serviceDesc1 = serviceDelegate1.getServiceDescription();
            validatePort(service1, otherWsdl_portLocalPart);
            
            // Create the second service 
            Service service2 = Service.create(wsdlUrl, serviceQName);
            ServiceDelegate serviceDelegate2 = DescriptionTestUtils2.getServiceDelegate(service2);
            assertNull(ServiceDelegate.getServiceMetadata());
            ServiceDescription serviceDesc2 = serviceDelegate2.getServiceDescription();
            validatePort(service2, otherWsdl_portLocalPart);
            
            assertNotSame(serviceDelegate1, serviceDelegate2);
            // Since we installed a caching factory, the service descriptions WILL be cached.
            // Without that factory, they would not have been cached.  The reason is that the
            // AxisConfiguration instance is part of the key.  The default factory in this 
            // environment always returns a new instance.  The test factory does not.
            assertSame(serviceDesc1, serviceDesc2);
        } finally {
            restoreOriginalFactory();
        }
        
        // Sanity check that the factory WAS restored.  Do the same thing as above, but this time
        // the service descs should NOT be the same since they weren't cached.
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        QName portQN = new QName(namespaceURI, otherWsdl_portLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(otherWsdl);

        // Create the first service 
        Service service1 = Service.create(wsdlUrl, serviceQName);
        ServiceDelegate serviceDelegate1 = DescriptionTestUtils2.getServiceDelegate(service1);
        assertNull(ServiceDelegate.getServiceMetadata());
        ServiceDescription serviceDesc1 = serviceDelegate1.getServiceDescription();
        validatePort(service1, otherWsdl_portLocalPart);
        
        // Create the second service 
        Service service2 = Service.create(wsdlUrl, serviceQName);
        ServiceDelegate serviceDelegate2 = DescriptionTestUtils2.getServiceDelegate(service2);
        assertNull(ServiceDelegate.getServiceMetadata());
        ServiceDescription serviceDesc2 = serviceDelegate2.getServiceDescription();
        validatePort(service2, otherWsdl_portLocalPart);
        
        assertNotSame(serviceDelegate1, serviceDelegate2);
        assertNotSame("Client Configuration factory NOT restored; subsequent tests may be affected!", serviceDesc1, serviceDesc2);
    }
    
    /**
     * Create two services such that the ServiceDescriptions should be cached and shared.
     * Create the first without a composite, the second with a composite, then validate the second 
     * service's composite.  Note that we have to use a special factory so that the service
     * descriptions are cached.  
     */
    public void testMultipleServicesMixedComposite() {
        try {
            installCachingFactory();
            QName serviceQName = new QName(namespaceURI, svcLocalPart);
            URL wsdlUrl = ClientMetadataTest.getWsdlURL(otherWsdl);
            
            // Create the first service 
            Service service1 = Service.create(wsdlUrl, serviceQName);
            ServiceDelegate serviceDelegate1 = DescriptionTestUtils2.getServiceDelegate(service1);
            assertNull(ServiceDelegate.getServiceMetadata());
            ServiceDescription serviceDesc1 = serviceDelegate1.getServiceDescription();
            validatePort(service1, otherWsdl_portLocalPart);

            // Create the second service specifiying a sparse composite
            DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
            ServiceDelegate.setServiceMetadata(sparseComposite);
            Service service2 = Service.create(wsdlUrl, serviceQName);
            ServiceDelegate serviceDelegate2 = DescriptionTestUtils2.getServiceDelegate(service2);
            assertNull(ServiceDelegate.getServiceMetadata());
            ServiceDescription serviceDesc2 = serviceDelegate2.getServiceDescription();
            validatePort(service2, otherWsdl_portLocalPart);
            
            assertNotSame(serviceDelegate1, serviceDelegate2);
            // Since we installed a caching factory, the service descriptions WILL be cached.
            // Without that factory, they would not have been cached.  The reason is that the
            // AxisConfiguration instance is part of the key.  The default factory in this 
            // environment always returns a new instance.  The test factory does not.
            assertSame(serviceDesc1, serviceDesc2);
            
            // There should not be a sparse composite for the first service delegate and there
            // should be one for the second service delegate
            assertNull(DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc2).getSparseComposite(serviceDelegate1));
            assertSame(sparseComposite, DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc2).getSparseComposite(serviceDelegate2));
        } finally {
            restoreOriginalFactory();
        }
        
    }
    
    /**
     * Create two services such that the ServiceDescriptions should be cached and shared.
     * Create the first with a composite, the second with a composite, then validate both 
     * composites.  Note that we have to use a special factory so that the service
     * descriptions are cached.  
     */
    public void testMultipleServicesMultipleComposites() {
        try {
            installCachingFactory();
            QName serviceQName = new QName(namespaceURI, svcLocalPart);
            URL wsdlUrl = ClientMetadataTest.getWsdlURL(otherWsdl);
            
            // Create the first service 
            DescriptionBuilderComposite sparseComposite1 = new DescriptionBuilderComposite();
            ServiceDelegate.setServiceMetadata(sparseComposite1);
            Service service1 = Service.create(wsdlUrl, serviceQName);
            ServiceDelegate serviceDelegate1 = DescriptionTestUtils2.getServiceDelegate(service1);
            assertNull(ServiceDelegate.getServiceMetadata());
            ServiceDescription serviceDesc1 = serviceDelegate1.getServiceDescription();
            validatePort(service1, otherWsdl_portLocalPart);

            // Create the second service specifiying a sparse composite
            DescriptionBuilderComposite sparseComposite2 = new DescriptionBuilderComposite();
            ServiceDelegate.setServiceMetadata(sparseComposite2);
            Service service2 = Service.create(wsdlUrl, serviceQName);
            ServiceDelegate serviceDelegate2 = DescriptionTestUtils2.getServiceDelegate(service2);
            assertNull(ServiceDelegate.getServiceMetadata());
            ServiceDescription serviceDesc2 = serviceDelegate2.getServiceDescription();
            validatePort(service2, otherWsdl_portLocalPart);
            
            assertNotSame(serviceDelegate1, serviceDelegate2);
            // Since we installed a caching factory, the service descriptions WILL be cached.
            // Without that factory, they would not have been cached.  The reason is that the
            // AxisConfiguration instance is part of the key.  The default factory in this 
            // environment always returns a new instance.  The test factory does not.
            assertSame(serviceDesc1, serviceDesc2);
            
            // There should not be a sparse composite for the first service delegate and there
            // should be one for the second service delegate
            assertSame(sparseComposite1, DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc2).getSparseComposite(serviceDelegate1));
            assertSame(sparseComposite2, DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc2).getSparseComposite(serviceDelegate2));
        } finally {
            restoreOriginalFactory();
        }        
    }
    
    /**
     * Create two generated services such that the ServiceDescriptions should be cached and shared.
     * Create the first with a composite, the second with a composite, then validate both 
     * composites.  Note that we have to use a special factory so that the service
     * descriptions are cached.  
     */
    public void testMultipleGeneratedServiceWithMultipleComposite() {
        try {
            installCachingFactory();
            
            // Create the first service with a sparse composite
            DescriptionBuilderComposite sparseComposite1 = new DescriptionBuilderComposite();
            ServiceDelegate.setServiceMetadata(sparseComposite1);
            Service service1 = new ClientMetadataGeneratedService(getWsdlURL(otherWsdl),
                                                                 new QName(namespaceURI, svcLocalPart));
            assertNotNull(service1);
            assertNull(ServiceDelegate.getServiceMetadata());
            
            // Create the second service with a sparse composite
            DescriptionBuilderComposite sparseComposite2 = new DescriptionBuilderComposite();
            ServiceDelegate.setServiceMetadata(sparseComposite2);
            Service service2 = new ClientMetadataGeneratedService(getWsdlURL(otherWsdl),
                                                                 new QName(namespaceURI, svcLocalPart));
            assertNotNull(service2);
            assertNull(ServiceDelegate.getServiceMetadata());
            
            // Verifiy the service delegates are different and the service descriptions are the same
            // since we installed a caching factory above.
            ServiceDelegate serviceDelegate1 = DescriptionTestUtils2.getServiceDelegate(service1);
            assertNotNull(serviceDelegate1);
            ServiceDescription serviceDesc1 = serviceDelegate1.getServiceDescription();
            assertNotNull(serviceDesc1);
            
            ServiceDelegate serviceDelegate2 = DescriptionTestUtils2.getServiceDelegate(service2);
            assertNotNull(serviceDelegate2);
            ServiceDescription serviceDesc2 = serviceDelegate2.getServiceDescription();
            assertNotNull(serviceDesc2);
            
            assertNotSame(serviceDelegate1, serviceDelegate2);
            assertSame(serviceDesc1, serviceDesc2);

            // There should be a sparse composite for the first service delegate and
            // one for the second service delegate
            assertSame(sparseComposite1, 
                       DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc2).getSparseComposite(serviceDelegate1));
            assertSame(sparseComposite2, DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc2).getSparseComposite(serviceDelegate2));

            
            DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc1);
            assertEquals(ClientMetadataGeneratedService.class, dbcInServiceDesc.getCorrespondingClass());

            // WSDL was specified on the generated Service constructor, and none in the composite
            // so should get the WSDL specified on the constructor
            assertTrue("Wrong WSDL used", validatePort(service1, otherWsdl_portLocalPart));
            assertTrue("Wrong WSDL used", validatePort(service2, otherWsdl_portLocalPart));
            
            
            
            
        } finally {
            restoreOriginalFactory();
        }        

    }
    
    // =============================================================================================
    // Utility methods
    // =============================================================================================

    /**
     * Prepends the base directory and the path where the test WSDL lives to a filename.
     * @param wsdlFileName
     * @return
     */
    static String getWsdlLocation(String wsdlFileName) {
        String wsdlLocation = null;
        String baseDir = System.getProperty("basedir",".");
        wsdlLocation = baseDir + "/test-resources/wsdl/" + wsdlFileName;
        return wsdlLocation;
    }
    
    /**
     * Prepends the base directory and the path where the test Catalog lives to a filename.
     * @param catalogFileName
     * @return
     */
    static String getCatalogLocation(String catalogFileName) {
        String wsdlLocation = null;
        String baseDir = System.getProperty("basedir",".");
        wsdlLocation = baseDir + "/test-resources/catalog/" + catalogFileName;
        return catalogFileName;
    }
    
    /**
     * Given a simple file name (with no base dictory or path), returns a URL to the WSDL file
     * with the base directory and path prepended.
     * 
     * @param wsdlFileName
     * @return
     */
    static URL getWsdlURL(String wsdlFileName) {
        URL url = null;
        String wsdlLocation = getWsdlLocation(wsdlFileName);
        try {
            File file = new File(wsdlLocation);
            url = file.toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            fail("Exception converting WSDL file to URL: " + e.toString());
        }
        return url;
    }
    
    /**
     * Validate that the Service contains the expected port.  If the portLocalPart is null then
     * no ports are expected to exist under the service.  
     * @param service The service to check for the specified port
     * @param portLocalPart If null means no ports are expected; otherwise the localname of the
     *    single port expected under the service
     * @return
     */
    static boolean validatePort(Service service, String portLocalPart) {
        boolean isValid = false;

        if (service == null) {
            return false;
        }
        
        // Each service in the WSDLs for this test have a single port
        boolean portNameValid = false;
        int expectedNumberOfPorts = 1;  
        int numberOfPorts = 0;
        
        Iterator<QName> portIterator = service.getPorts();
        while (portIterator.hasNext()) {
            numberOfPorts++;
            QName checkPort = portIterator.next();
            // If we haven't found a match for the port yet, see if this one matches
            if (!portNameValid) {
                portNameValid = portLocalPart.equals(checkPort.getLocalPart());
            }
        }
        if (portLocalPart == null && numberOfPorts == 0) {
            // No ports expected (i.e. no WSDL should have been used)
            isValid = true;
        }
        else if ((expectedNumberOfPorts == numberOfPorts) && portNameValid) {
            isValid = true;
        }
        else {
            isValid = false;
        }
        return isValid;
    }

    /**
     * Methods to install a client configuration factory that will return the same AxisConfiguration
     * each time.  This is used so that the ServiceDescriptions will be cached in the DescriptionFactory.
     * 
     * IMPORTANT!!!
     * If you install a caching factory, you MUST restore the original factory before your test
     * exits, otherwise it will remain installed when subsequent tests run and cause REALLY STRANGE
     * failures.  Use restoreOriginalFactory() INSIDE A finally() block to restore the factory.
     */
    static private ClientConfigurationFactory originalFactory = null;
    public static void installCachingFactory() {
        // install caching factory
        if (originalFactory != null) {
            throw new UnsupportedOperationException("Attempt to install the caching factory when the original factory has already been overwritten");
        }
        originalFactory = 
            (ClientConfigurationFactory)MetadataFactoryRegistry.getFactory(ClientConfigurationFactory.class);
        MetadataTestCachingClientContextFactory newFactory = new MetadataTestCachingClientContextFactory();
        MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, newFactory);
        resetClientConfigFactory();
    }
    public static void restoreOriginalFactory() {
        if (originalFactory == null) {
            throw new UnsupportedOperationException("Attempt to restore original factory to a null value");
        }
        MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, originalFactory);
        resetClientConfigFactory();
        originalFactory = null;
    }
    static void resetClientConfigFactory() {
//        Field field;
//        try {
//            field = DescriptionFactoryImpl.class.getDeclaredField("clientConfigFactory");
//            field.setAccessible(true);
//            field.set(null, null);
//        } catch (Exception e) {
//            throw new UnsupportedOperationException("Unable to reset client config factory; caught " + e);
//        }
    }
    
}

@WebServiceClient(targetNamespace="originalTNS", wsdlLocation=ClientMetadataTest.originalWsdl)
class ClientMetadataGeneratedService extends javax.xml.ws.Service {
    public ClientMetadataGeneratedService() {
        super(ClientMetadataTest.getWsdlURL(ClientMetadataTest.originalWsdl),
              new QName(ClientMetadataTest.namespaceURI, ClientMetadataTest.svcLocalPart));
    }
    public ClientMetadataGeneratedService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }
}


class MetadataTestCachingClientContextFactory extends ClientConfigurationFactory {
    ConfigurationContext context;
    
    public ConfigurationContext getClientConfigurationContext() {
        if (context == null) {
            context = super.getClientConfigurationContext();
        }
//        System.out.println("Test version of MetadataTestCachingClientContextFactory: " + context);
        return context;
    }
    
    public void reset() {
        context = null;
    }
}
