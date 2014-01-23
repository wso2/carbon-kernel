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
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;

/**
 * 
 */
public class HandlerChainConfigFileTests extends TestCase {
    public void testValidConfigFile() {
        ServiceDescription svcDesc = DescriptionFactory.createServiceDescription(ValidConfigFileImpl.class);
        EndpointDescription[] epDescs = svcDesc.getEndpointDescriptions();
        assertEquals(1, epDescs.length);
        EndpointDescription epDesc = epDescs[0];
        HandlerChainsType hct = epDesc.getHandlerChain();
        assertNotNull(hct);
    }
    
    public void testMissingRelativeConfigFile() {
        try {
            ServiceDescription svcDesc = DescriptionFactory.createServiceDescription(InvalidConfigFileImpl.class);
            EndpointDescription[] epDescs = svcDesc.getEndpointDescriptions();
            assertEquals(1, epDescs.length);
            EndpointDescription epDesc = epDescs[0];
            HandlerChainsType hct = epDesc.getHandlerChain();
            fail("Should have caught exception for a missing handler config file");
        }
        catch (WebServiceException e) {
            // Expected path        }
            String message = e.toString();
        }
        catch (Exception e) {
            fail("Expected a WebServiceException, but caught: " + e);
        }
    }

    public void testMissingAbsoluteConfigFile() {
        try {
            ServiceDescription svcDesc = DescriptionFactory.createServiceDescription(InvalidAbsoluteConfigFileImpl.class);
            EndpointDescription[] epDescs = svcDesc.getEndpointDescriptions();
            assertEquals(1, epDescs.length);
            EndpointDescription epDesc = epDescs[0];
            HandlerChainsType hct = epDesc.getHandlerChain();
            fail("Should have caught exception for a missing handler config file");
        }
        catch (WebServiceException e) {
            // Expected path.  We can't check for explicit details in this case because which failure
            // occurs depends on the enivronment.  Some get "ConnectionRefused" and some get "FileNotFound".
            String message = e.toString();
        }
        catch (Exception e) {
            fail("Expected a WebServiceException, but caught: " + e);
        }
    }

}

@WebService()
@HandlerChain(file = "HandlerConfigFile.xml")
class ValidConfigFileImpl {
    
}

@WebService()
@HandlerChain(file = "MissingHandlerConfigFile.xml")
class InvalidConfigFileImpl {
    
}

@WebService()
@HandlerChain(file = "http://localhost/will/not/find/MissingHandlerConfigFile.xml")
class InvalidAbsoluteConfigFileImpl {
    
}
