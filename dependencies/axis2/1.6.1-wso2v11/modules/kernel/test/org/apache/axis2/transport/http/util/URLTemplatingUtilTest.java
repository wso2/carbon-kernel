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

package org.apache.axis2.transport.http.util;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.WSDL2Constants;

import java.net.MalformedURLException;
import java.net.URL;

public class URLTemplatingUtilTest extends TestCase {


    private MessageContext messageContext;


    protected void setUp() throws Exception {
        messageContext = new MessageContext();

        SOAPFactory soapFactory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope defaultEnvelope = soapFactory.getDefaultEnvelope();

        messageContext.setEnvelope(defaultEnvelope);

        OMElement bodyFirstElement = soapFactory.createOMElement("TestOperation", null);
        defaultEnvelope.getBody().addChild(bodyFirstElement);

        soapFactory.createOMElement("FirstName", null, bodyFirstElement).setText("Foo");
        soapFactory.createOMElement("LastName", null, bodyFirstElement).setText("Bar");

    }

    public void testGetTemplatedURL() throws AxisFault, MalformedURLException {
        URL testURL =
                new URL("http://locahost:8080/paramOne");
        messageContext.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, "{FirstName}?test=1&lastName={LastName}");
        URL modifiedURL = URLTemplatingUtil.getTemplatedURL(testURL, messageContext, true);

        System.out.println("original = " + testURL);
        System.out.println("modifiedURL = " + modifiedURL);

        String expectedURL = "http://locahost:8080/paramOne/Foo?test=1&lastName=Bar";
        assertEquals(modifiedURL.toString(), expectedURL);

    }

    public void testAppendParametersToURL() throws MalformedURLException, AxisFault {
        URL testURL = new URL("http://locahost:8080/paramOne");
        messageContext.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, null);
        URL modifiedURL = URLTemplatingUtil.appendQueryParameters(messageContext,testURL);

        System.out.println("original = " + testURL);
        System.out.println("modifiedURL = " + modifiedURL);

        String expectedURL = "http://locahost:8080/paramOne?FirstName=Foo&LastName=Bar";
        assertEquals(modifiedURL.toString(), expectedURL);
    }

    public void testQueryParameterSeperator() throws MalformedURLException, AxisFault {
        URL testURL = new URL("http://locahost:8080/paramOne");
        messageContext.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, null);
        messageContext.setProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,";");
        URL modifiedURL = URLTemplatingUtil.appendQueryParameters(messageContext,testURL);

        System.out.println("original = " + testURL);
        System.out.println("modifiedURL = " + modifiedURL);

        String expectedURL = "http://locahost:8080/paramOne?FirstName=Foo;LastName=Bar";
        assertEquals(modifiedURL.toString(), expectedURL);
    }

    public void testIgnoreUncitedFalse() throws MalformedURLException, AxisFault {

        URL testURL = new URL("http://locahost:8080/paramOne/Foo?test=1");
        messageContext.setProperty(WSDL2Constants.ATTR_WHTTP_IGNORE_UNCITED,"false");
        messageContext.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, null);
        URL modifiedURL = URLTemplatingUtil.appendQueryParameters(messageContext,testURL);

        System.out.println("original = " + testURL);
        System.out.println("modifiedURL = " + modifiedURL);

        String expectedURL = "http://locahost:8080/paramOne/Foo?test=1&FirstName=Foo&LastName=Bar";
        assertEquals(modifiedURL.toString(), expectedURL);

    }

}
