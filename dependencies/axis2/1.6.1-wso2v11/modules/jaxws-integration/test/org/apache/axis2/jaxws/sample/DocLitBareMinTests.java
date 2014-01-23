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

/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.sample.doclitbaremin.sei.BareDocLitMinService;
import org.apache.axis2.jaxws.sample.doclitbaremin.sei.DocLitBareMinPortType;

import javax.xml.ws.BindingProvider;


public class DocLitBareMinTests extends AbstractTestCase {
    String axisEndpoint = "http://localhost:6060/axis2/services/DocLitBareMinPortTypeImplService.DocLitBareMinPortTypeImplPort";

    public static Test suite() {
        return getTestSetup(new TestSuite(DocLitBareMinTests.class));
    }
    	
	
    public void testEcho() throws Exception {
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
        
        
        BareDocLitMinService service = new BareDocLitMinService();
        DocLitBareMinPortType proxy = service.getBareDocLitMinPort();
        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
        p.getRequestContext().put(
                BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        p.getRequestContext().put(
                BindingProvider.SOAPACTION_URI_PROPERTY, "echo");
        String request = "dlroW elloH";
        String response = proxy.echo(request);
        
        assertTrue(request.equals(response));
        
        // Try the call again to verify behavior
        response = proxy.echo(request);
        
        assertTrue(request.equals(response));
        
    }
}
