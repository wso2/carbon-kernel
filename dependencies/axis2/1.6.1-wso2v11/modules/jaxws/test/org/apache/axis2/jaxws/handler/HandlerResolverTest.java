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

package org.apache.axis2.jaxws.handler;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.impl.DescriptionUtils;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Verify the HandlerResolverImpl functionality.
 */
public class HandlerResolverTest extends TestCase {
    private String namespaceURI = "http://www.apache.org/test/namespace";
    private String svcLocalPart = "DummyService";
    private String portLocalPart = "DummyPort";
    private String portWrongLocalPart = "WrongPort";
    
    /**
     *  Test that setting the handler chain type on a sparse composite, but not 
     *  specifying that composite during construction of the HandlerResolver (i.e. no
     *  Delegate key specified) results in no hanlders returned from this resolver. 
     */
    public void testHandlerResolverNoKey() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        // Create a composite with a JAXB Handler Config 
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();

        HandlerChainsType handlerChainsType = getHandlerChainsType();
        sparseComposite.setHandlerChainsType(handlerChainsType);
        Object serviceDelegateKey = "CompositeKey";
        
        ServiceDescription serviceDesc = 
            DescriptionFactory.createServiceDescription(null, serviceQName, HandlerResolverTestService.class, sparseComposite, serviceDelegateKey);
        HandlerResolver resolver = new HandlerResolverImpl(serviceDesc);
        assertNotNull(resolver);
        PortInfo pi = new DummyPortInfo();
        List<Handler> list = resolver.getHandlerChain(pi);
        assertEquals(0, list.size());
    }
    
    /**
     * The sparse composite has handler config information for the key that the HandlerResolver
     * is created with, so that handler resolver contains those handlers.  However, the 
     * portInfo specified on the getHandlerChain does NOT match the QName in the config file
     * so no handlers should be returned.
     */
    public void testHandlerResolverInvalidPortInfo() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        QName portQName = new QName(namespaceURI, portWrongLocalPart);
        // Create a composite with a JAXB Handler Config 
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();

        HandlerChainsType handlerChainsType = getHandlerChainsType();
        sparseComposite.setHandlerChainsType(handlerChainsType);
        Object serviceDelegateKey = "CompositeKey";
        
        // The getHandlerChain will do handler lifecycle management as well, so there needs to be
        // and EnpdointDescription (representing the Port) under the ServiceDescription
        ServiceDescription serviceDesc = 
            DescriptionFactory.createServiceDescription(null, serviceQName, HandlerResolverTestService.class, sparseComposite, serviceDelegateKey);
        EndpointDescription endpointDesc = 
            DescriptionFactory.updateEndpoint(serviceDesc, HandlerResolverTestSEI.class, portQName, DescriptionFactory.UpdateType.GET_PORT);
        HandlerResolver resolver = new HandlerResolverImpl(serviceDesc, serviceDelegateKey);
        assertNotNull(resolver);
        PortInfo pi = new DummyPortInfo();
        List<Handler> list = resolver.getHandlerChain(pi);
        assertEquals(0, list.size());
    }

    /**
     * The sparse composite has handler config information for the key that the HandlerResolver
     * is created with, so that handler resolver contains those handlers.  
     */
    public void testHandlerResolverValidPortInfo() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        QName portQName = new QName(namespaceURI, portLocalPart);
        // Create a composite with a JAXB Handler Config 
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();

        HandlerChainsType handlerChainsType = getHandlerChainsType();
        sparseComposite.setHandlerChainsType(handlerChainsType);
        Object serviceDelegateKey = "CompositeKey";
        
        // The getHandlerChain will do handler lifecycle management as well, so there needs to be
        // and EnpdointDescription (representing the Port) under the ServiceDescription
        ServiceDescription serviceDesc = 
            DescriptionFactory.createServiceDescription(null, serviceQName, HandlerResolverTestService.class, sparseComposite, serviceDelegateKey);
        EndpointDescription endpointDesc = 
            DescriptionFactory.updateEndpoint(serviceDesc, HandlerResolverTestSEI.class, portQName, DescriptionFactory.UpdateType.GET_PORT);
        HandlerResolver resolver = new HandlerResolverImpl(serviceDesc, serviceDelegateKey);
        assertNotNull(resolver);
        PortInfo pi = new DummyPortInfo();
        List<Handler> list = resolver.getHandlerChain(pi);
        assertEquals(2, list.size());
        
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
            configLoc = sep + "test-resources" + sep + "configuration" + sep + "handlers" + sep + "handler.xml";
            String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
            is = new File(baseDir + configLoc).toURL().openStream();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return is;
    }

    public class DummyPortInfo implements PortInfo {

        public String getBindingID() {
            return SOAPBinding.SOAP11HTTP_BINDING;
        }

        public QName getPortName() {
            return new QName("http://www.apache.org/test/namespace", "DummyPort");
        }

        public QName getServiceName() {
            return new QName("http://www.apache.org/test/namespace", "DummyService");
        }
    }

}

@WebServiceClient
class HandlerResolverTestService extends javax.xml.ws.Service {
    protected HandlerResolverTestService(URL wsdlDocumentLocation, QName serviceName) {
        super(wsdlDocumentLocation, serviceName);
    }
}

@WebService
interface HandlerResolverTestSEI {
    public String echo(String toEcho);
}
