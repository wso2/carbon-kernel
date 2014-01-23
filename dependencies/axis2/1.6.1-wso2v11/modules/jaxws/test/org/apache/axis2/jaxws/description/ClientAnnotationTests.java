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

package org.apache.axis2.jaxws.description;

import junit.framework.TestCase;

import javax.jws.HandlerChain;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import java.net.URL;

/**
 * Test that the annotations valid on a service requester can be 
 * processed. 
 */
public class ClientAnnotationTests extends TestCase {
    private String namespaceURI = "http://org.apache.axis2.jaxws.description.ClientAnnotationTests";
    private String svcLocalPart = "svcLocalPart";
    private String portLocalPart = "portLocalPart";

    public void testSEIAnnotations() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        Service service = new ClientAnnotationTestsService(null, serviceQName);
        QName portQName = new QName(namespaceURI, portLocalPart);
        ClientAnnotationTestsSEI port = service.getPort(portQName, ClientAnnotationTestsSEI.class);
    }

}

@WebServiceClient
class ClientAnnotationTestsService extends javax.xml.ws.Service {
    protected ClientAnnotationTestsService(URL wsdlDocumentLocation, QName serviceName) {
        super(wsdlDocumentLocation, serviceName);
    }
}

@WebService
@HandlerChain(file="ClientAnnotationTestsHandler.xml")
interface ClientAnnotationTestsSEI {
 
    @WebMethod
    @WebResult
    public String echo(@WebParam
                       String arg);
    
    @Oneway
    public void oneWay();
    
    @ResponseWrapper
    @RequestWrapper
    public String echoWrap(String art);
}
