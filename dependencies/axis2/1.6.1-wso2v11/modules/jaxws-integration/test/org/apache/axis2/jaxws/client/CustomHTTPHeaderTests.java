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

package org.apache.axis2.jaxws.client;

import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.sample.addnumbers.AddNumbersPortType;
import org.apache.axis2.jaxws.sample.addnumbers.AddNumbersService;

public class CustomHTTPHeaderTests extends AbstractTestCase {

    String axisEndpoint = "http://localhost:6060/axis2/services/AddNumbersService.AddNumbersPortTypeImplPort";
    
    public static Test suite() {
        return getTestSetup(new TestSuite(CustomHTTPHeaderTests.class));
    }
    
    public void testPort() throws Exception {        
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("MY_HEADER_1", Collections.singletonList("hello"));
        headers.put("MY_HEADER_2", Arrays.asList("value1", "value2"));
        
        AddNumbersService service = new AddNumbersService();
        AddNumbersPortType port = service.getAddNumbersPort();
                
        BindingProvider p = (BindingProvider) port;
        
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
        p.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, headers);
        
        assertEquals(777, port.addNumbers(333, 444));
    }
}
