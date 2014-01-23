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
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.impl.DescriptionUtils;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Verify that handler chains specified using the HandlerChainsType in a sparse
 * composite are correctly applied to Services and Ports on the client requester. 
 */
public class ClientMetadataHandlerChainTest extends TestCase {
    
    static String namespaceURI = "http://www.apache.org/test/namespace";
    static String svcLocalPart = "DummyService";
    static private String portLocalPart = "DummyPort";
    private static int uniqueService = 0;
    
    /**
     *  Test creating a service without a sparse composite.  This verifies pre-existing default
     *  behavior.
     */
    public void testServiceAndPortNoComposite() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        QName portQName = new QName(namespaceURI, portLocalPart);

        Service service = Service.create(serviceQName);
        HandlerResolver resolver = service.getHandlerResolver();
        assertNotNull(resolver);
        PortInfo pi = new DummyPortInfo();
        List<Handler> list = resolver.getHandlerChain(pi);
        assertEquals(0, list.size());
        
        ClientMetadataHandlerChainTestSEI port = service.getPort(portQName, ClientMetadataHandlerChainTestSEI.class);
        // Verify that ports created under the service have no handlers from the sparse composite
        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = (Binding) bindingProvider.getBinding();
        List<Handler> portHandlers = binding.getHandlerChain();
        assertEquals(0, portHandlers.size());
    }

    /**
     * Test creating a service with a sparse composite that contains handler configuration
     * information for this service delegate.  Verify that the handlers are included in the 
     * chain.
     */
    public void testServiceWithComposite() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        QName portQName = new QName(namespaceURI, portLocalPart);
        // Create a composite with a JAXB Handler Config 
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        HandlerChainsType handlerChainsType = getHandlerChainsType();
        sparseComposite.setHandlerChainsType(handlerChainsType);

        ServiceDelegate.setServiceMetadata(sparseComposite);
        Service service = Service.create(serviceQName);
        ClientMetadataHandlerChainTestSEI port = service.getPort(portQName, ClientMetadataHandlerChainTestSEI.class);

        // Verify the HandlerResolver on the service knows about the handlers in the sparse composite
        HandlerResolver resolver = service.getHandlerResolver();
        assertNotNull(resolver);
        PortInfo pi = new DummyPortInfo();
        List<Handler> list = resolver.getHandlerChain(pi);
        assertEquals(2, list.size());
        
        // Verify that ports created under the service have handlers
        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = (Binding) bindingProvider.getBinding();
        List<Handler> portHandlers = binding.getHandlerChain();
        assertEquals(2, portHandlers.size());
        assertTrue(containSameHandlers(portHandlers, list));
        
        // Verify that a subsequent port are different and that they also gets the correct handlers
        ClientMetadataHandlerChainTestSEI port2 = service.getPort(portQName, ClientMetadataHandlerChainTestSEI.class);
        BindingProvider bindingProvider2 = (BindingProvider) port2;
        Binding binding2 = (Binding) bindingProvider2.getBinding();
        List<Handler> portHandlers2 = binding2.getHandlerChain();
        assertNotSame(port, port2);
        assertEquals(2, portHandlers2.size());
        assertTrue(containSameHandlers(portHandlers2, list));
    }
    
    /**
     * Set a sparse composite on a specific Port.  Verify that instances of that Port have the
     * correct handlers associated and other Ports do not.
     */
    public void testPortWithComposite() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        QName portQName = new QName(namespaceURI, portLocalPart);

        Service service = Service.create(serviceQName);
        
        // Create a composite with a JAXB Handler Config 
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        HandlerChainsType handlerChainsType = getHandlerChainsType();
        sparseComposite.setHandlerChainsType(handlerChainsType);
        ServiceDelegate.setPortMetadata(sparseComposite);
        ClientMetadataHandlerChainTestSEI port = service.getPort(portQName, ClientMetadataHandlerChainTestSEI.class);

        // Verify the HandlerResolver on the service knows about the handlers in the sparse composite
        HandlerResolver resolver = service.getHandlerResolver();
        assertNotNull(resolver);
        PortInfo pi = new DummyPortInfo();
        List<Handler> list = resolver.getHandlerChain(pi);
        assertEquals(2, list.size());
        
        // Verify that the port created with the sparse metadata has those handlers
        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = (Binding) bindingProvider.getBinding();
        List<Handler> portHandlers = binding.getHandlerChain();
        assertEquals(2, portHandlers.size());
        assertTrue(containSameHandlers(portHandlers, list));
        
        // Verify that a creating another instance of the same port also gets those handlers
        ClientMetadataHandlerChainTestSEI port2 = service.getPort(portQName, ClientMetadataHandlerChainTestSEI.class);
        BindingProvider bindingProvider2 = (BindingProvider) port2;
        Binding binding2 = (Binding) bindingProvider2.getBinding();
        List<Handler> portHandlers2 = binding2.getHandlerChain();
        assertNotSame(port, port2);
        assertEquals(2, portHandlers2.size());
        assertTrue(containSameHandlers(portHandlers2, list));
        
        // Verify that createing a different port doesn't get the handlers
        QName portQName3 = new QName(namespaceURI, portLocalPart + "3");
        ClientMetadataHandlerChainTestSEI port3 = service.getPort(portQName3, ClientMetadataHandlerChainTestSEI.class);
        BindingProvider bindingProvider3 = (BindingProvider) port3;
        Binding binding3 = (Binding) bindingProvider3.getBinding();
        List<Handler> portHandlers3 = binding3.getHandlerChain();
        assertEquals(0, portHandlers3.size());
        
        // Verify setting the metadata on a different port (a different QName) will get handlers.
        QName portQName4 = new QName(namespaceURI, portLocalPart + "4");
        ServiceDelegate.setPortMetadata(sparseComposite);
        ClientMetadataHandlerChainTestSEI port4 = service.getPort(portQName4, ClientMetadataHandlerChainTestSEI.class);
        BindingProvider bindingProvider4 = (BindingProvider) port4;
        Binding binding4 = (Binding) bindingProvider4.getBinding();
        List<Handler> portHandlers4 = binding4.getHandlerChain();
        assertEquals(2, portHandlers4.size());
        
        // Verify the service handler resolver knows about boths sets of handlers
        // attached to the two different port QNames and none are attached for the third port
        List<Handler> listForPort = resolver.getHandlerChain(pi);
        assertEquals(2, listForPort.size());

        PortInfo pi4 = new DummyPortInfo(portQName4);
        List<Handler> listForPort4 = resolver.getHandlerChain(pi4);
        assertEquals(2, listForPort4.size());
        
        PortInfo pi3 = new DummyPortInfo(portQName3);
        List<Handler> listForPort3 = resolver.getHandlerChain(pi3);
        assertEquals(0, listForPort3.size());
    }
    
    /**
     * Verify that handlers specified in a sparse compoiste on the service are only associated with 
     * that specific service delegate (i.e. Service instance), even if the QNames are the same 
     * across two instances of a Service.
     */
    public void testMultipleServiceDelgatesServiceComposite() {
        try {
            // Need to cache the ServiceDescriptions so that they are shared
            // across the two instances of the same Service.
            ClientMetadataTest.installCachingFactory();
            
            QName serviceQName = new QName(namespaceURI, svcLocalPart);
            PortInfo pi = new DummyPortInfo();

            // Create a Service specifying a sparse composite and verify the
            // ports under that service get the correct handlers associated.
            DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
            HandlerChainsType handlerChainsType = getHandlerChainsType();
            sparseComposite.setHandlerChainsType(handlerChainsType);
            ServiceDelegate.setServiceMetadata(sparseComposite);
            Service service1 = Service.create(serviceQName);

            // Create a second instance of the same Service, but without
            // metadata. Ports created under that service should not get handler's associated.
            Service service2 = Service.create(serviceQName);

            // No ports created yet, so there should be no relevant handler
            // chains.
            HandlerResolver resolver1 = service1.getHandlerResolver();
            List<Handler> list1 = resolver1.getHandlerChain(pi);
            assertEquals(0, list1.size());

            // Create the port, it should get handlers.
            QName portQName1 = new QName(namespaceURI, portLocalPart);
            ClientMetadataHandlerChainTestSEI port1 =
                    service1.getPort(portQName1, ClientMetadataHandlerChainTestSEI.class);
            BindingProvider bindingProvider1 = (BindingProvider) port1;
            Binding binding1 = (Binding) bindingProvider1.getBinding();
            List<Handler> portHandlers1 = binding1.getHandlerChain();
            assertEquals(2, portHandlers1.size());
            
            // Refresh the handler list from the resolver after the port is created
            list1 = resolver1.getHandlerChain(pi);
            assertTrue(containSameHandlers(portHandlers1, list1));

            // Make sure the 2nd Service instance doesn't have handlers
            // associated with it
            HandlerResolver resolver2 = service2.getHandlerResolver();
            List<Handler> list2 = resolver2.getHandlerChain(pi);
            assertEquals(0, list2.size());

            // Make sure the same port created under the 2nd service also
            // doesn't have handlers
            ClientMetadataHandlerChainTestSEI port2 =
                    service2.getPort(portQName1, ClientMetadataHandlerChainTestSEI.class);
            BindingProvider bindingProvider2 = (BindingProvider) port2;
            Binding binding2 = (Binding) bindingProvider2.getBinding();
            List<Handler> portHandlers2 = binding2.getHandlerChain();
            assertEquals(0, portHandlers2.size());
        }
        finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }

    /**
     * Verify that handlers specified in a sparse compoiste on the port are only associated with 
     * that port on that specific service delegate (i.e. Service instance), even if the QNames are the same 
     * across two instances of a Service.
     */
    public void testMultipleServiceDelgatesPortComposite() {
        try {
            // Need to cache the ServiceDescriptions so that they are shared
            // across the two instances of the same Service.
            ClientMetadataTest.installCachingFactory();
            
            QName serviceQName = new QName(namespaceURI, svcLocalPart);
            PortInfo pi = new DummyPortInfo();

            // Create two instances of the same Service
            Service service1 = Service.create(serviceQName);
            Service service2 = Service.create(serviceQName);

            // No ports created yet, so there should be no relevant handler
            // chains.
            HandlerResolver resolver1 = service1.getHandlerResolver();
            List<Handler> list1 = resolver1.getHandlerChain(pi);
            assertEquals(0, list1.size());

            // Create a Port specifying a sparse composite and verify the
            // port gets the correct handlers associated.
            DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
            HandlerChainsType handlerChainsType = getHandlerChainsType();
            sparseComposite.setHandlerChainsType(handlerChainsType);
            ServiceDelegate.setPortMetadata(sparseComposite);
            QName portQName1 = new QName(namespaceURI, portLocalPart);
            ClientMetadataHandlerChainTestSEI port1 =
                    service1.getPort(portQName1, ClientMetadataHandlerChainTestSEI.class);
            BindingProvider bindingProvider1 = (BindingProvider) port1;
            Binding binding1 = (Binding) bindingProvider1.getBinding();
            List<Handler> portHandlers1 = binding1.getHandlerChain();
            assertEquals(2, portHandlers1.size());
            
            // Refresh the handler list from the resolver after the port is created
            list1 = resolver1.getHandlerChain(pi);
            assertTrue(containSameHandlers(portHandlers1, list1));

            // Make sure the 2nd Service instance doesn't have handlers
            // associated with it
            HandlerResolver resolver2 = service2.getHandlerResolver();
            List<Handler> list2 = resolver2.getHandlerChain(pi);
            assertEquals(0, list2.size());

            // Make sure the same port created under the 2nd service also
            // doesn't have handlers
            ClientMetadataHandlerChainTestSEI port2 =
                    service2.getPort(portQName1, ClientMetadataHandlerChainTestSEI.class);
            BindingProvider bindingProvider2 = (BindingProvider) port2;
            Binding binding2 = (Binding) bindingProvider2.getBinding();
            List<Handler> portHandlers2 = binding2.getHandlerChain();
            assertEquals(0, portHandlers2.size());
        }
        finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }
    
    /**
     * Verify that the original functionality of specifying a HandlerChain annotation with a
     * file member works as it should. 
     */
    public void testHandlerChainOnSEI() {
        QName serviceQN = new QName(namespaceURI, svcLocalPart);

        Service service = Service.create(serviceQN);
        
        ClientMetadataHandlerChainTestSEIWithHC port = service.getPort(ClientMetadataHandlerChainTestSEIWithHC.class);
        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = (Binding) bindingProvider.getBinding();
        List<Handler> portHandlers = binding.getHandlerChain();
        assertEquals(1, portHandlers.size());
        assertTrue(containsHandlerChainAnnotationHandlers(portHandlers));
    }
    
    /**
     * Verify that handler information in a sparse composite on the Port will override any handler chain
     * annotation on the SEI. 
     */
    public void testSEIHandlerChainOverrideOnPort() {
        QName serviceQN = new QName(namespaceURI, svcLocalPart + uniqueService++);
        
        Service service = Service.create(serviceQN);

        // The SEI has a HandlerChain annotation, but the sparse metadata should override it
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        HandlerChainsType handlerChainsType = getHandlerChainsType();
        sparseComposite.setHandlerChainsType(handlerChainsType);
        ServiceDelegate.setPortMetadata(sparseComposite);
        ClientMetadataHandlerChainTestSEIWithHC port = service.getPort(ClientMetadataHandlerChainTestSEIWithHC.class);
        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = (Binding) bindingProvider.getBinding();
        List<Handler> portHandlers = binding.getHandlerChain();
        assertEquals(2, portHandlers.size());
        assertTrue(containsSparseCompositeHandlers(portHandlers));
    }
    
    /**
     * Verify that handler information in a sparse composite on the Service will override any handler chain
     * annotation on the SEI. 
     */
    public void testSEIHandlerChainOverrideOnService() {
        QName serviceQN = new QName(namespaceURI, svcLocalPart + uniqueService++);
        
        // The SEI has a HandlerChain annotation, but the sparse metadata should override it
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        HandlerChainsType handlerChainsType = getHandlerChainsType();
        sparseComposite.setHandlerChainsType(handlerChainsType);
        ServiceDelegate.setServiceMetadata(sparseComposite);
        Service service = Service.create(serviceQN);

        ClientMetadataHandlerChainTestSEIWithHC port = service.getPort(ClientMetadataHandlerChainTestSEIWithHC.class);
        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = (Binding) bindingProvider.getBinding();
        List<Handler> portHandlers = binding.getHandlerChain();
        assertEquals(2, portHandlers.size());
        assertTrue(containsSparseCompositeHandlers(portHandlers));
    }
    
    /**
     * Set different composites on the Service and the Port that specify different 
     * HandlerChainsType values.  
     */
    public void testCompositeOnServiceAndPort() {
        QName serviceQN = new QName(namespaceURI, svcLocalPart + uniqueService++);
        
        // Create a service with a composite specifying handlers
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        HandlerChainsType handlerChainsType = getHandlerChainsType();
        sparseComposite.setHandlerChainsType(handlerChainsType);
        ServiceDelegate.setServiceMetadata(sparseComposite);
        Service service = Service.create(serviceQN);

        // Create a port with a composite specifying different handlers
        DescriptionBuilderComposite portComposite = new DescriptionBuilderComposite();
        HandlerChainsType portHandlerChainsType = getHandlerChainsType("ClientMetadataHandlerChainTest.xml");
        portComposite.setHandlerChainsType(portHandlerChainsType);
        ServiceDelegate.setPortMetadata(portComposite);
        ClientMetadataHandlerChainTestSEI port = service.getPort(ClientMetadataHandlerChainTestSEI.class);
        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = (Binding) bindingProvider.getBinding();
        List<Handler> portHandlers = binding.getHandlerChain();

        // If there is a HandlerChainsType composite specified on both the Service and the Port,
        // then the composite specified on the Port should be the one used to associate the 
        // handlers for that Port.
        assertEquals(1, portHandlers.size());
        assertTrue(containsHandlerChainAnnotationHandlers(portHandlers));
    }
    
    /**
     * Verfiy that a HandlerChain annotation on the Service is associated with the Port 
     */
    public void testGeneratedServiceWithHC() {
        ClientMetadataHandlerChainTestServiceWithHC service = new ClientMetadataHandlerChainTestServiceWithHC();
        ClientMetadataHandlerChainTestSEI port = service.getPort(ClientMetadataHandlerChainTestSEI.class);

        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = (Binding) bindingProvider.getBinding();
        List<Handler> portHandlers = binding.getHandlerChain();
        assertEquals(1, portHandlers.size());
        assertTrue(containsHandlerChainAnnotationHandlers(portHandlers));

    }
    
    /**
     * Verfiy that given a HandlerChain annotation on the Service and a Port and a sparse composite
     * on the Port associates the handlers from the sparse composite on the Port.
     */
    public void testGeneratedServiceWithHCPortOverride() {
        ClientMetadataHandlerChainTestServiceWithHC service = new ClientMetadataHandlerChainTestServiceWithHC();

        // Set a HandlerChainsType on the sparse composite for the Port creation; it should override the 
        // HandlerChain annotation on the Service.
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        HandlerChainsType handlerChainsType = getHandlerChainsType();
        sparseComposite.setHandlerChainsType(handlerChainsType);
        ServiceDelegate.setPortMetadata(sparseComposite);
        ClientMetadataHandlerChainTestSEI port = service.getPort(ClientMetadataHandlerChainTestSEI.class);

        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = (Binding) bindingProvider.getBinding();
        List<Handler> portHandlers = binding.getHandlerChain();
        assertEquals(2, portHandlers.size());
        assertTrue(containsSparseCompositeHandlers(portHandlers));
    }

    // =============================================================================================
    // Helper methods and classes
    // =============================================================================================
    private boolean containsSparseCompositeHandlers(List<Handler> handlerList) {
        List<Class> inputHandlerClasses = handlerClasses(handlerList);

        // These are the handlers defined in the HandlerChainsType placed on the sparse composite
        List<Class> compositeHandlerClasses = new ArrayList<Class>();
        compositeHandlerClasses.add(org.apache.axis2.jaxws.spi.handler.DummySOAPHandler.class);
        compositeHandlerClasses.add(org.apache.axis2.jaxws.spi.handler.DummyLogicalHandler.class);

        if (inputHandlerClasses.size() != compositeHandlerClasses.size()) {
            return false;
        }
        
        if (inputHandlerClasses.containsAll(compositeHandlerClasses)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Answer if the List contains the same handlers as defined in the SEI
     * via the HandlerChain.file annotation memeber.
     * 
     * @param portHandlers List of handlers
     * @return true if the list matches what was defined on the SEI via the
     *   HandlerChain annotation; false otherwise.
     */
    private boolean containsHandlerChainAnnotationHandlers(List<Handler> portHandlers) {
        List<Class> portHandlerClasses = handlerClasses(portHandlers);
        List<Class> seiHandlerClasses = new ArrayList<Class>();
        seiHandlerClasses.add(ClientMetadataHandlerChainHandler.class);
        
        if (portHandlerClasses.size() != seiHandlerClasses.size()) {
            return false;
        }
        
        if (portHandlerClasses.containsAll(seiHandlerClasses)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Answer if two List<Handler> arguments contain the same handler Class files.
     * @param list1
     * @param list2
     * @return
     */
    private boolean containSameHandlers(List<Handler> list1, List<Handler> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        List<Class> list1HandlerClasses = handlerClasses(list1);
        List<Class> list2HandlerClasses = handlerClasses(list2);

        if (list1HandlerClasses.containsAll(list2HandlerClasses)) {
            return true;
        } else {
            return false;
        }
            
    }
    
    private List<Class> handlerClasses(List<Handler> listOfHandlers) {
        List<Class> handlerClasses = new ArrayList<Class>();
        Iterator<Handler> handlerIterator = listOfHandlers.iterator();
        while (handlerIterator.hasNext()) {
            handlerClasses.add(handlerIterator.next().getClass());
        }
        return handlerClasses;
    }
    
    private HandlerChainsType getHandlerChainsType() {
        return getHandlerChainsType("handler.xml");
    }
    private HandlerChainsType getHandlerChainsType(String fileName) {
        InputStream is = getXMLFileStream(fileName);
        assertNotNull(is);
        HandlerChainsType returnHCT = DescriptionUtils.loadHandlerChains(is, this.getClass().getClassLoader());
        assertNotNull(returnHCT);
        return returnHCT;
    }
    private InputStream getXMLFileStream(String fileName) {
        InputStream is = null;
        String configLoc = null;
        try {
            String sep = "/";
            configLoc = sep + "test-resources" + sep + "configuration" + sep + "handlers" + sep + fileName;
            String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
            is = new File(baseDir + configLoc).toURL().openStream();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return is;
    }

    public class DummyPortInfo implements PortInfo {
        private QName portQN;
        private QName serviceQN;
        
        public DummyPortInfo() {
            this.portQN = new QName("http://www.apache.org/test/namespace", "DummyPort");
            this.serviceQN = new QName("http://www.apache.org/test/namespace", "DummyService");
        }
        
        public DummyPortInfo(QName portQN) {
            this();
            this.portQN = portQN;
        }

        public String getBindingID() {
            return SOAPBinding.SOAP11HTTP_BINDING;
        }

        public QName getPortName() {
            return portQN;
        }
        
        public QName getServiceName() {
            return serviceQN;
        }
    }

}

@WebService
interface ClientMetadataHandlerChainTestSEI {
    public String echo(String toEcho);
}

@WebService
@HandlerChain(file="ClientMetadataHandlerChainTest.xml")
interface ClientMetadataHandlerChainTestSEIWithHC {
    public String echo(String toEcho);
}

@WebServiceClient
@HandlerChain(file="ClientMetadataHandlerChainTest.xml")
class ClientMetadataHandlerChainTestServiceWithHC extends javax.xml.ws.Service {
        public ClientMetadataHandlerChainTestServiceWithHC() {
            super(null,
                  new QName(ClientMetadataHandlerChainTest.namespaceURI, ClientMetadataHandlerChainTest.svcLocalPart));
        }
        public ClientMetadataHandlerChainTestServiceWithHC(URL wsdlLocation, QName serviceName) {
            super(wsdlLocation, serviceName);
        }
}
