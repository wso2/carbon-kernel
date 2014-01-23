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

package org.apache.axis2.jaxws.addressing;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.axis2.addressing.AddressingConstants;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

public class UsingAddressingTests extends TestCase {

    public QName serviceQName;

    public QName portQName;

    public UsingAddressingTests(String name) {
        super(name);

        serviceQName = new QName("http://test", "TestService");
        portQName = new QName("http://test", "TestPort");
    }

    public void testDispatchAddressingDisabledByDefault() {
        Service svc = Service.create(serviceQName);
        svc.addPort(portQName, SOAPBinding.SOAP11HTTP_BINDING, "");

        Dispatch dsp = svc.createDispatch(portQName, Source.class,
                Service.Mode.MESSAGE);

        org.apache.axis2.jaxws.spi.BindingProvider bp = (org.apache.axis2.jaxws.spi.BindingProvider) dsp;

        Boolean addressingDisabled = (Boolean) bp.getRequestContext().get(
                AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        assertTrue("Unexpected disableAddressingForOutMessages value: "
                + addressingDisabled, addressingDisabled);
    }

    public void testProxyAddressingDisabledByDefault() {
        Service svc = Service.create(serviceQName);
        Sample s = svc.getPort(Sample.class);

        org.apache.axis2.jaxws.spi.BindingProvider bp = (org.apache.axis2.jaxws.spi.BindingProvider) s;

        Boolean addressingDisabled = (Boolean) bp.getRequestContext().get(
                AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        assertTrue("Unexpected disableAddressingForOutMessages value: "
                + addressingDisabled, addressingDisabled);
    }

    // Check addressing is disabled when <wsaw:UsingAddressing /> is NOT used in
    // the WSDL
    public void testWithoutUsingAddressing() {
        String wsdlLocation = getClass().getResource("/wsdl/Test.wsdl").toExternalForm();
        try {
            Service svc = Service.create(new URL(wsdlLocation), serviceQName);
            Sample s = svc.getPort(portQName, Sample.class);

            org.apache.axis2.jaxws.spi.BindingProvider bp = (org.apache.axis2.jaxws.spi.BindingProvider) s;

            Boolean addressingDisabled = (Boolean) bp.getRequestContext().get(
                    AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
            assertTrue("Unexpected disableAddressingForOutMessages value: "
                    + addressingDisabled, addressingDisabled);
        } catch (MalformedURLException e) {
            System.out.println("Error in WSDL : " + wsdlLocation);
            System.out.println("Exception: " + e.toString());
            fail("Caught exception " + e.toString());
        }
    }

    // Check addressing is enabled when <wsaw:UsingAddressing /> is used in the
    // WSDL.
    public void testUsingAddressing() {
        String wsdlLocation = getClass().getResource("/wsdl/UsingAddressing.wsdl").toExternalForm();
        try {
            Service svc = Service.create(new URL(wsdlLocation), serviceQName);
            Sample s = svc.getPort(portQName, Sample.class);

            org.apache.axis2.jaxws.spi.BindingProvider bp = (org.apache.axis2.jaxws.spi.BindingProvider) s;

            Boolean addressingDisabled = (Boolean) bp.getRequestContext().get(
                    AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
            // Do NOT change this to assertFalse since this allows us to
            // differentiate from addressing being enabled by JAX-WS 2.1
            // features and annotations which override the WSDL.
            assertNull("Unexpected disableAddressingForOutMessages value: "
                    + addressingDisabled, addressingDisabled);
        } catch (MalformedURLException e) {
            System.out.println("Error in WSDL : " + wsdlLocation);
            System.out.println("Exception: " + e.toString());
            fail("Caught exception " + e.toString());
        }
    }

    // Check addressing is enabled when <wsaw:UsingAddressing
    // wsdl:required="false" /> is used in the WSDL
    public void testUsingAddressingOptional() {
        String wsdlLocation = getClass().getResource("/wsdl/UsingAddressingOptional.wsdl").toExternalForm();
        try {
            Service svc = Service.create(new URL(wsdlLocation), serviceQName);
            Sample s = svc.getPort(portQName, Sample.class);

            org.apache.axis2.jaxws.spi.BindingProvider bp = (org.apache.axis2.jaxws.spi.BindingProvider) s;

            Boolean addressingDisabled = (Boolean) bp.getRequestContext().get(
                    AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
            // Do NOT change this to assertFalse since this allows us to
            // differentiate from addressing being enabled by JAX-WS 2.1
            // features and annotations which override the WSDL.
            assertNull("Unexpected disableAddressingForOutMessages value: "
                    + addressingDisabled, addressingDisabled);
        } catch (MalformedURLException e) {
            System.out.println("Error in WSDL : " + wsdlLocation);
            System.out.println("Exception: " + e.toString());
            fail("Caught exception " + e.toString());
        }
    }

    // Check addressing is enabled when <wsaw:UsingAddressing
    // wsdl:required="true" /> is used in the WSDL
    public void testUsingAddressingRequired() {
        String wsdlLocation = getClass().getResource("/wsdl/UsingAddressingRequired.wsdl").toExternalForm();
        try {
            Service svc = Service.create(new URL(wsdlLocation), serviceQName);
            Sample s = svc.getPort(portQName, Sample.class);

            org.apache.axis2.jaxws.spi.BindingProvider bp = (org.apache.axis2.jaxws.spi.BindingProvider) s;

            Boolean addressingDisabled = (Boolean) bp.getRequestContext().get(
                    AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
            // Do NOT change this to assertFalse since this allows us to
            // differentiate from addressing being enabled by JAX-WS 2.1
            // features and annotations which override the WSDL.
            assertNull("Unexpected disableAddressingForOutMessages value: "
                    + addressingDisabled, addressingDisabled);
        } catch (MalformedURLException e) {
            System.out.println("Error in WSDL : " + wsdlLocation);
            System.out.println("Exception: " + e.toString());
            fail("Caught exception " + e.toString());
        }
    }
}
