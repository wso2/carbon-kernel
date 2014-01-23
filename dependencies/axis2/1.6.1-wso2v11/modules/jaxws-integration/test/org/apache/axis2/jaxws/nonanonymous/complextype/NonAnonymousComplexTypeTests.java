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
package org.apache.axis2.jaxws.nonanonymous.complextype;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.nonanonymous.complextype.sei.EchoMessagePortType;
import org.apache.axis2.jaxws.nonanonymous.complextype.sei.EchoMessageService;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

public class NonAnonymousComplexTypeTests extends AbstractTestCase {

    String axisEndpoint = "http://localhost:6060/axis2/services/EchoMessageService.EchoMessageImplPort";
	
    public static Test suite() {
        return getTestSetup(new TestSuite(NonAnonymousComplexTypeTests.class));
    }

    public void testSimpleProxy() {
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
        try {
            String msg = "Hello Server";
            EchoMessagePortType myPort = (new EchoMessageService()).getEchoMessagePort();
            BindingProvider p = (BindingProvider) myPort;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);

            String response = myPort.echoMessage(msg);
            TestLogger.logger.debug(response);
            
            // Try a second time to verify
            response = myPort.echoMessage(msg);
            TestLogger.logger.debug(response);
            TestLogger.logger.debug("------------------------------");
        } catch (WebServiceException webEx) {
            webEx.printStackTrace();
            fail();
        }
    }

		    


}
