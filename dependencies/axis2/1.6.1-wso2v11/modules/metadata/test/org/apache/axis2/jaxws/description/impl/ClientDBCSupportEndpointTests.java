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
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.i18n.Messages;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Test client sparse composite support in the MDQ layer at the Endpoint creation level.
 */
/**
 * 
 */
public class ClientDBCSupportEndpointTests extends TestCase {
    private String namespaceURI = "http://org.apache.axis2.jaxws.description.impl.ClientDBCSupportEndpointTests";
    private String svcLocalPart = "svcLocalPart";
    private String portLocalPart = "portLocalPart";
    
    /**
     * Verify that the code that doesn't use a composite continues to work correctly. 
     */
    public void testPreDBCFunctionality() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        
        ServiceDescription svcDesc = DescriptionFactory.createServiceDescription(null, serviceQName, ClientDBCSupportEndpointServiceSubclass.class);
        assertNotNull(svcDesc);
        ServiceDescriptionImpl svcDescImpl = (ServiceDescriptionImpl) svcDesc;
        DescriptionBuilderComposite svcDescComposite = svcDescImpl.getDescriptionBuilderComposite();
        assertNotNull(svcDescComposite);
        
        Class testServiceClass = svcDescComposite.getCorrespondingClass();
        assertNotNull(testServiceClass);
        assertEquals(ClientDBCSupportEndpointServiceSubclass.class, testServiceClass);
        
        // Now update with an SEI
        QName portQName = new QName(namespaceURI, portLocalPart);
        EndpointDescription epDesc = 
            DescriptionFactory.updateEndpoint(svcDesc, ClientDBCSupportEndpointSEI.class, portQName, DescriptionFactory.UpdateType.GET_PORT);
        assertNotNull(epDesc);
        EndpointDescriptionImpl epDescImpl = (EndpointDescriptionImpl) epDesc;
        DescriptionBuilderComposite epDescComposite = epDescImpl.getDescriptionBuilderComposite();
        Class seiClass = epDescComposite.getCorrespondingClass();
        assertEquals(ClientDBCSupportEndpointSEI.class, seiClass);
        // Make sure we didn't overwrite the class in the ServiceDesc composite
        assertEquals(ClientDBCSupportEndpointServiceSubclass.class, 
                     svcDescComposite.getCorrespondingClass());
        
    }

    /**
     * Verify that the code that uses a simple empty sparse composite to create an endpoint.
     */
    public void testSimpleComposite() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        
        ServiceDescription svcDesc = DescriptionFactory.createServiceDescription(null, serviceQName, ClientDBCSupportEndpointServiceSubclass.class);
        assertNotNull(svcDesc);
        ServiceDescriptionImpl svcDescImpl = (ServiceDescriptionImpl) svcDesc;
        DescriptionBuilderComposite svcDescComposite = svcDescImpl.getDescriptionBuilderComposite();
        assertNotNull(svcDescComposite);
        
        Class testServiceClass = svcDescComposite.getCorrespondingClass();
        assertNotNull(testServiceClass);
        assertEquals(ClientDBCSupportEndpointServiceSubclass.class, testServiceClass);
        
        // Now update with an SEI
        QName portQName = new QName(namespaceURI, portLocalPart);
        DescriptionBuilderComposite setEpDescComposite = new DescriptionBuilderComposite();
        Object compositeKey = "Key1";
        EndpointDescription epDesc = 
            DescriptionFactory.updateEndpoint(svcDesc, ClientDBCSupportEndpointSEI.class, portQName, DescriptionFactory.UpdateType.GET_PORT,
                                              setEpDescComposite, compositeKey);
        assertNotNull(epDesc);
        EndpointDescriptionImpl epDescImpl = (EndpointDescriptionImpl) epDesc;
        DescriptionBuilderComposite epDescComposite = epDescImpl.getDescriptionBuilderComposite();
        // The sparse composite should NOT be equal to the composite in the EndpointDescription
        // The sparse composite SHOULD be equal to the sparse composite contained in the EndpointDescription
        assertNotSame(setEpDescComposite, epDescComposite);
        assertEquals(setEpDescComposite, epDescComposite.getSparseComposite(compositeKey));
        Class seiClass = epDescComposite.getCorrespondingClass();
        assertEquals(ClientDBCSupportEndpointSEI.class, seiClass);
        // Make sure we didn't overwrite the class in the ServiceDesc composite
        assertEquals(ClientDBCSupportEndpointServiceSubclass.class, 
                     svcDescComposite.getCorrespondingClass());
        
    }
    
    /**
     * A composite can not be specified when doing an AddPort 
     */
    public void testAddPort() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        
        ServiceDescription svcDesc = DescriptionFactory.createServiceDescription(null, serviceQName, ClientDBCSupportEndpointServiceSubclass.class);
        assertNotNull(svcDesc);
        ServiceDescriptionImpl svcDescImpl = (ServiceDescriptionImpl) svcDesc;
        DescriptionBuilderComposite svcDescComposite = svcDescImpl.getDescriptionBuilderComposite();
        assertNotNull(svcDescComposite);
        
        Class testServiceClass = svcDescComposite.getCorrespondingClass();
        assertNotNull(testServiceClass);
        assertEquals(ClientDBCSupportEndpointServiceSubclass.class, testServiceClass);
        
        // Now update with an SEI
        QName portQName = new QName(namespaceURI, portLocalPart);
        DescriptionBuilderComposite setEpDescComposite = new DescriptionBuilderComposite();
        Object compositeKey = "Key1";
        try {
            EndpointDescription epDesc = 
                DescriptionFactory.updateEndpoint(svcDesc, ClientDBCSupportEndpointSEI.class, portQName, DescriptionFactory.UpdateType.ADD_PORT,
                                                  setEpDescComposite, compositeKey);
            fail("Should have caught an exception");
        }
        catch (WebServiceException e) {
            // Expected path
        }
        
    }
    
    /**
     * Composite can not be specified with a CREATE_DISPATCH
     */
    public void testCreateDispatch() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        
        ServiceDescription svcDesc = DescriptionFactory.createServiceDescription(null, serviceQName, ClientDBCSupportEndpointServiceSubclass.class);
        assertNotNull(svcDesc);
        ServiceDescriptionImpl svcDescImpl = (ServiceDescriptionImpl) svcDesc;
        DescriptionBuilderComposite svcDescComposite = svcDescImpl.getDescriptionBuilderComposite();
        assertNotNull(svcDescComposite);
        
        Class testServiceClass = svcDescComposite.getCorrespondingClass();
        assertNotNull(testServiceClass);
        assertEquals(ClientDBCSupportEndpointServiceSubclass.class, testServiceClass);
        
        // Now update with an SEI
        QName portQName = new QName(namespaceURI, portLocalPart);
        DescriptionBuilderComposite setEpDescComposite = new DescriptionBuilderComposite();
        Object compositeKey = "Key1";
        try {
            EndpointDescription epDesc = 
                DescriptionFactory.updateEndpoint(svcDesc, null /* SEI can't be specified */, 
                                                  portQName, 
                                                  DescriptionFactory.UpdateType.CREATE_DISPATCH,
                                                  setEpDescComposite, compositeKey);
            fail("Should have caught an exception");
        }
        catch (WebServiceException e) {
            // Expected path
            String msg = Messages.getMessage("serviceDescErr6");
            assertTrue(e.toString().contains(msg));
        }
    }
    
    /**
     * Update a port that was created from WSDL (i.e. a declared port) with a composite.  To get 
     * into a state where a declared port would need to be updated:
     * 1) Do a CREATE_DISPATCH on a declared wsdl port
     * 2) Do a GET_PORT on that port, providing an SEI.
     * In this case, the EndpointDescription is shared between the two, but the sparse composite
     * specified on the GET_PORT should be unique to it.
     */
    public void testUpdateDeclaredPort() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        
        ServiceDescription svcDesc = DescriptionFactory.createServiceDescription(getWsdlURL(), serviceQName, Service.class);

        QName portQName = new QName(namespaceURI, portLocalPart);

        // First we do a CREATE_DISPATCH to create the EndpointDescription for the WSDL port.
        // Note that we don't specify a composite (because that isn't allowed for CREATE_DISPATCH)
        // and one will be created by the runtime
        EndpointDescription epDescForCreate = 
            DescriptionFactory.updateEndpoint(svcDesc, null /* SEI can't be specified */, 
                                              portQName, 
                                              DescriptionFactory.UpdateType.CREATE_DISPATCH);
        EndpointDescriptionImpl epDescImpl = (EndpointDescriptionImpl) epDescForCreate;
        DescriptionBuilderComposite createDispatchComposite = epDescImpl.getDescriptionBuilderComposite();
        assertNotNull(createDispatchComposite);
        // There really shouldn't be any sparse composites at this point; make sure by checking a 
        // couple obvious things that might be used as a key.
        assertNull(epDescImpl.getDescriptionBuilderComposite().getSparseComposite(createDispatchComposite));
        assertNull(epDescImpl.getDescriptionBuilderComposite().getSparseComposite(null));

        DescriptionBuilderComposite updateEndpointComposite = new DescriptionBuilderComposite();
        Object compositeKey = "KEY1";
        EndpointDescription epDescForGetPort = 
            DescriptionFactory.updateEndpoint(svcDesc, ClientDBCSupportEndpointSEI.class, portQName, DescriptionFactory.UpdateType.GET_PORT,
                                              updateEndpointComposite, compositeKey);
        assertEquals(epDescForCreate, epDescForGetPort);
        
        // The update needs to have set the sparse composite (like for a given ServiceDelegate
        // instance) without having lost the different composite for the create.  The EndpointDescripton 
        // can be shared, but the sparse composite overrides they contain need to be unique (like to the ServiceDelegates)
        assertTrue(createDispatchComposite != updateEndpointComposite);
        assertEquals(updateEndpointComposite, epDescImpl.getDescriptionBuilderComposite().getSparseComposite(compositeKey));
        // Make sure this didn't change any of the sparse information (which is none) for the previous create
        assertNull(epDescImpl.getDescriptionBuilderComposite().getSparseComposite(createDispatchComposite));
        assertNull(epDescImpl.getDescriptionBuilderComposite().getSparseComposite(null));
        
    }
    
    /**
     * Do multiple GET_PORT on the same service.  This should share the same EndpointDescription
     * but the sparse composite specified on GET_PORT should be unique to each key.
     */
    public void testMultipleGetPort() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        ServiceDescription svcDesc = DescriptionFactory.createServiceDescription(getWsdlURL(), serviceQName, Service.class);
        QName portQName = new QName(namespaceURI, portLocalPart);
        
        // Do the first GetPort using sd1 as a key for the sparse composite
        DescriptionBuilderComposite sd1Composite = new DescriptionBuilderComposite();
        Object sd1 = "SD1";
        EndpointDescription epDescForGetPort1 = 
            DescriptionFactory.updateEndpoint(svcDesc, ClientDBCSupportEndpointSEI.class, portQName, DescriptionFactory.UpdateType.GET_PORT,
                                              sd1Composite, sd1);
        assertNotNull(epDescForGetPort1);
        EndpointDescriptionImpl epDescImpl1 = (EndpointDescriptionImpl) epDescForGetPort1;
        assertEquals(sd1Composite, epDescImpl1.getDescriptionBuilderComposite().getSparseComposite(sd1));
        
        // Do the second GetPort using sd2 as a key for the sparse composite
        DescriptionBuilderComposite sd2Composite = new DescriptionBuilderComposite();
        Object sd2 = "SD2";
        EndpointDescription epDescForGetPort2 = 
            DescriptionFactory.updateEndpoint(svcDesc, ClientDBCSupportEndpointSEI.class, portQName, DescriptionFactory.UpdateType.GET_PORT,
                                              sd2Composite, sd2);
        assertNotNull(epDescForGetPort2);
        assertEquals(epDescForGetPort1, epDescForGetPort2);
        EndpointDescriptionImpl epDescImpl2 = (EndpointDescriptionImpl) epDescForGetPort2;
        assertNotSame(sd1Composite, sd2Composite);
        assertEquals(sd2Composite, epDescImpl2.getDescriptionBuilderComposite().getSparseComposite(sd2));
        assertEquals(sd1Composite, epDescImpl1.getDescriptionBuilderComposite().getSparseComposite(sd1));
    }
    
    static URL getWsdlURL() {
        URL url = null;
        String wsdlLocation = null;
        try {
            try{
                String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
                
                wsdlLocation = new File(baseDir + "/test-resources/wsdl/ClientEndpointMetadata.wsdl").getAbsolutePath();
            }catch(Exception e){
                e.printStackTrace();
                fail("Exception creating File(WSDL): " + e.toString());
            }
            File file = new File(wsdlLocation);
            url = file.toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            fail("Exception converting WSDL file to URL: " + e.toString());
        }
        return url;
    }

}

@WebServiceClient(targetNamespace="originalTNS", wsdlLocation="originalWsdlLocation")
class ClientDBCSupportEndpointServiceSubclass extends javax.xml.ws.Service {

    protected ClientDBCSupportEndpointServiceSubclass(URL wsdlDocumentLocation, QName serviceName) {
        super(wsdlDocumentLocation, serviceName);
    }
}

@WebService
interface ClientDBCSupportEndpointSEI {
    public String echo (String string);
}

@WebService(serviceName = "EchoService", endpointInterface="org.apache.ws.axis2.tests.EchoPort")
class ClientDBCSupportEndpointEchoServiceImplWithSEI {
    public void echo(Holder<String> text) {
        text.value = "Echo " + text.value;
    }
}
