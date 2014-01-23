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

package org.apache.axis2.jaxws.proxy;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.proxy.soap12.Echo;
import org.apache.axis2.jaxws.proxy.soap12.SOAP12EchoService;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

/**
 * A suite of tests to test dynamic proxy clients sending SOAP 1.2
 * requests.  The endpoint can accept different keys to determine
 * what it should send back.
 */
public class SOAP12ProxyTests extends AbstractTestCase {

    private static final String SEND_SOAP11_RESPONSE = "RESPONSE-SOAP11";
    private static final String SEND_SOAP12_RESPONSE = "RESPONSE-SOAP12";
    String axisEndpoint = "http://localhost:6060/axis2/services/SOAP12EchoService.EchoPort";
	
    public static Test suite() {
        return getTestSetup(new TestSuite(SOAP12ProxyTests.class));
    }

    /**
     * Send a SOAP 1.2 request and expect a SOAP 1.2 response.
     */
    public void testSOAP12RequestSOAP12Response() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // create the proxy instance.  the WSDL used by this proxy
        // should have a proper SOAP 1.2 binding configured
        SOAP12EchoService service = new SOAP12EchoService();
        Echo proxy = service.getPort(new QName("http://jaxws.axis2.apache.org/proxy/soap12", "EchoPort"), Echo.class);
        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);

        // invoke the remote operation.  send a key that tells the 
        // service send back a SOAP 1.2 response.
        String response = proxy.echo(SEND_SOAP12_RESPONSE);
        TestLogger.logger.debug("response returned [" + response + "]");
        
        // validate the results
        assertNotNull(response);
        assertTrue(!response.equals("FAIL"));
        
        // Try a second time
        response = proxy.echo(SEND_SOAP12_RESPONSE);
        TestLogger.logger.debug("response returned [" + response + "]");
        
        // validate the results
        assertNotNull(response);
        assertTrue(!response.equals("FAIL"));
    }
    
    /**
     * Send a SOAP 1.2 request, but have the server send back a SOAP 1.1
     * response.  This should result in an exception.
     */
    // TODO fix and re-enable
    public void _testSOAP12RequestSOAP11Response() {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // create the proxy instance.  the WSDL used by this proxy
        // should have a proper SOAP 1.2 binding configured
        SOAP12EchoService service = new SOAP12EchoService();
        Echo proxy = service.getPort(new QName("http://jaxws.axis2.apache.org/proxy/soap12", "EchoPort"), Echo.class);
        
        // invoke the remote operation.  send a key that tells the 
        // service send back a SOAP 1.1 response.  this should result
        // in an error.
        try {
            String response = proxy.echo(SEND_SOAP11_RESPONSE);
            TestLogger.logger.debug("response returned [" + response + "]");
            
            // if we've gotten this far, then something went wrong.
            fail();
        }
        catch (WebServiceException wse) {
            TestLogger.logger.debug("an exception was thrown, as expected");
            TestLogger.logger.debug(wse.getMessage());
        }
        
        // Now do it a second time to confirm the same behavior
        try {
            String response = proxy.echo(SEND_SOAP11_RESPONSE);
            TestLogger.logger.debug("response returned [" + response + "]");
            
            // if we've gotten this far, then something went wrong.
            fail();
        }
        catch (WebServiceException wse) {
            TestLogger.logger.debug("an exception was thrown, as expected");
            TestLogger.logger.debug(wse.getMessage());
        }
    } 
    
    public void test() {
        // NOOP
    }

}
