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
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceClient;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * Test client composite support for handler chains specified via sparse composites
 * applied to services and ports.
 */
public class ClientDBCSupportHandlersTests extends TestCase {
    private String namespaceURI = "http://org.apache.axis2.jaxws.description.impl.ClientDBCSupportHandlersTests";
    private String svcLocalPart = "svcLocalPart";
    private String svcLocalPart2 = "svcLocalPart2";
    private String portLocalPart = "portLocalPart";

    /**
     * Create a ServiceDescription specifying a HandlerChains Type in a sparse composite
     */
    public void testHandlersOnService() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        // Create a composite with a JAXB Handler Config 
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();

        HandlerChainsType handlerChainsType = getHandlerChainsType();
        composite.setHandlerChainsType(handlerChainsType);
        Object compositeKey = "CompositeKey";
        ServiceDescription svcDesc = 
            DescriptionFactory.createServiceDescription(null, serviceQName, 
                                       ClientDBCSupportHandlersService.class, 
                                       composite, compositeKey);
        assertNotNull(svcDesc);
        // There should only be a handler chain for the given key.
        assertNull(svcDesc.getHandlerChain());
        assertNotNull(svcDesc.getHandlerChain("CompositeKey"));
        assertNull(svcDesc.getHandlerChain("WrongKey"));
        
    }
    
    /**
     * Create an EndpointDescritoin specifying a HandlerChainsType in a sparse composite.
     */
    public void testHandlersOnEndpoint() {
        // Note that Unit tests in the Maven environment run within a single instance of the JVM
        // which means that the ServiceDescription crated by previous tests still exists.  So
        // we have to use a different service QName to be sure to always get a new ServiceDescription
        // or we could pick up a sparse composite with key "CompositeKey" set on a ServiceDescription
        // by another test, causing the assertNull(svcDesc.getHandlerChain("CompositeKey")) below
        // to fail
        QName serviceQName = new QName(namespaceURI, svcLocalPart2);
        QName portQName = new QName(namespaceURI, portLocalPart);

        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();

        HandlerChainsType handlerChainsType = getHandlerChainsType();
        composite.setHandlerChainsType(handlerChainsType);
        Object compositeKey = "CompositeKey";

        ServiceDescription svcDesc = 
            DescriptionFactory.createServiceDescription(null, serviceQName,
                                       ClientDBCSupportHandlersService.class, 
                                       null, null);
        assertNotNull(svcDesc);
        
        EndpointDescription epDesc = 
            DescriptionFactory.updateEndpoint(svcDesc, ClientDBCSupportHandlersSEI.class, portQName, 
                                              DescriptionFactory.UpdateType.GET_PORT,
                                              composite, compositeKey);
        assertNotNull(epDesc);
        
        // There should be no handler info on the Service, but there should be on the Endpoint 
        assertNull(svcDesc.getHandlerChain());
        assertNull(svcDesc.getHandlerChain("CompositeKey"));
        assertNull(svcDesc.getHandlerChain("WrongKey"));
        
        assertNull(epDesc.getHandlerChain());
        assertNotNull(epDesc.getHandlerChain("CompositeKey"));
        assertNull(epDesc.getHandlerChain("WrongKey"));
    }

    private HandlerChainsType getHandlerChainsType() {
        InputStream is = getXMLFileStream();
        assertNotNull(is);
        HandlerChainsType returnHCT = DescriptionUtils.loadHandlerChains(is, this.getClass().getClassLoader());
        assertNotNull(returnHCT);
        return returnHCT;
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

}

@WebServiceClient
class ClientDBCSupportHandlersService extends javax.xml.ws.Service {
    protected ClientDBCSupportHandlersService(URL wsdlDocumentLocation, QName serviceName) {
        super(wsdlDocumentLocation, serviceName);
    }
}

@WebService
interface ClientDBCSupportHandlersSEI {
    public String echo(String toEcho);
}

