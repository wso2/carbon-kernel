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

package org.apache.axis2.jaxws.databinding;

import junit.framework.TestCase;

import javax.xml.namespace.QName;

/**
 * A suite of tests for reading the binding from a WSDL file and
 * making sure we are configuring the Protocol correctly for 
 * messages based on that binding ID.
 */
public class BindingToProtocolTests extends TestCase {
    
    private static final String SOAP11_TEST_NS = "http://jaxws.axis2.apache.org/bindingtest/soap11";
    private static final String SOAP12_TEST_NS = "http://jaxws.axis2.apache.org/bindingtest/soap12";
    private static final QName SOAP11_SVC_QNAME = new QName(SOAP11_TEST_NS, "SOAP11EchoService");
    private static final QName SOAP12_SVC_QNAME = new QName(SOAP12_TEST_NS, "SOAP12EchoService");
    
    public BindingToProtocolTests(String name) {
        super(name);
    }
    
    /**
     * Test to see if we can read the SOAP 1.1 binding transport URL
     * in a WSDL, as specified by JAX-WS section 10.4.1.
     */
    public void testReadJAXWSSOAP11Binding() throws Exception {
        /*
        // Get the WSDL with the JAX-WS binding url
        URL wsdlUrl = new URL("file:./test-resources/wsdl/SOAP11Binding-JAXWS.wsdl");
        
        // TODO: There should be an easier way to do this without
        // requring the creating of a Service object.  Should the
        // ServiceDescription be constructed from just a WSDL file?
        Service svc = Service.create(wsdlUrl, SOAP11_SVC_QNAME);
        ServiceDelegate delegate = DescriptionTestUtils.getServiceDelegate(svc);
        ServiceDescription sd = delegate.getServiceDescription();
        
        EndpointDescription[] eds = sd.getEndpointDescriptions();
        for (int i = 0; i < eds.length; i++) {
            System.out.println("port [" + eds[i].getTargetNamespace() + ":" + eds[i].getName() + "]");
        }
        
        EndpointDescription ed = sd.getEndpointDescription(new QName(SOAP11_TEST_NS, "EchoPort"));
        assertNotNull("The EndpointDescription was not created.", ed);
        
        String bindingID = ed.getClientBindingID();
        assertNotNull("The binding ID was null.", bindingID);
        
        Protocol p = Protocol.getProtocolForBinding(bindingID);
        assertTrue("Protocol configured incorrectly", p.equals(Protocol.soap11));
        */
    }
    
    /**
     * Test to see if we can read the SOAP 1.1 binding transport URL
     * in a WSDL, as specified by WS-I Basic Profile 1.1.
     */
    public void testReadWSISOAP11Binding() throws Exception {
        /*
        // Get the WSDL with the JAX-WS binding url
        URL wsdlUrl = new URL("file:./test-resources/wsdl/SOAP11Binding-WSI.wsdl");
        
        // TODO: There should be an easier way to do this without
        // requring the creating of a Service object.  Should the
        // ServiceDescription be constructed from just a WSDL file?
        Service svc = Service.create(wsdlUrl, SOAP11_SVC_QNAME);
        ServiceDelegate delegate = DescriptionTestUtils.getServiceDelegate(svc);
        ServiceDescription sd = delegate.getServiceDescription();
        
        EndpointDescription[] eds = sd.getEndpointDescriptions();
        for (int i = 0; i < eds.length; i++) {
            System.out.println("port [" + eds[i].getTargetNamespace() + ":" + eds[i].getName() + "]");
        }
        
        EndpointDescription ed = sd.getEndpointDescription(new QName(SOAP11_TEST_NS, "EchoPort"));
        assertNotNull("The EndpointDescription was not created.", ed);
        
        String bindingID = ed.getClientBindingID();
        assertNotNull("The binding ID was null.", bindingID);
        
        Protocol p = Protocol.getProtocolForBinding(bindingID);
        assertTrue("Protocol configured incorrectly", p.equals(Protocol.soap11));
        */
    }
    
    /**
     * Test to see if we can read the SOAP 1.2 binding transport URL
     * in a WSDL, as specified by JAX-WS.
     */
    public void testReadJAXWSSOAP12Binding() throws Exception {
        /*
        // Get the WSDL with the JAX-WS binding url
        URL wsdlUrl = new URL("file:./test-resources/wsdl/SOAP12Binding-JAXWS.wsdl");
        
        // TODO: There should be an easier way to do this without
        // requring the creating of a Service object.  Should the
        // ServiceDescription be constructed from just a WSDL file?
        Service svc = Service.create(wsdlUrl, SOAP12_SVC_QNAME);
        ServiceDelegate delegate = DescriptionTestUtils.getServiceDelegate(svc);
        ServiceDescription sd = delegate.getServiceDescription();
        
        EndpointDescription[] eds = sd.getEndpointDescriptions();
        for (int i = 0; i < eds.length; i++) {
            System.out.println("port [" + eds[i].getTargetNamespace() + ":" + eds[i].getName() + "]");
        }
        
        EndpointDescription ed = sd.getEndpointDescription(new QName(SOAP12_TEST_NS, "EchoPort"));
        assertNotNull("The EndpointDescription was not created.", ed);
        
        String bindingID = ed.getClientBindingID();
        assertNotNull("The binding ID was null.", bindingID);
        
        Protocol p = Protocol.getProtocolForBinding(bindingID);
        assertTrue("Protocol configured incorrectly", p.equals(Protocol.soap12));
        */
    }
    
    /**
     * Test to see if we are defaulting the soap binding to SOAP 1.1
     * correctly in the absence of a WSDL document.
     */
    public void testDefaultBindingNoWSDL() {
        
    }
}
