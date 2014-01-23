/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.sample;

import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.sample.doclitbarenoarg.sei.BareDocLitNoArgService;
import org.apache.axis2.jaxws.sample.doclitbarenoarg.sei.DocLitBareNoArgPortType;

import javax.xml.ws.BindingProvider;

import junit.framework.Test;
import junit.framework.TestSuite;

public class BareNoArgTests extends AbstractTestCase {

    static final String ENDPOINT_URL = 
        "http://localhost:6060/axis2/services/BareDocLitNoArgService.DocLitBareNoArgPortTypeImplPort";

    public static Test suite() {
        return getTestSetup(new TestSuite(BareNoArgTests.class));
    }
    
    public void testTwoWayEmpty_With_SOAPACTION() {
        BareDocLitNoArgService service = new BareDocLitNoArgService();
        DocLitBareNoArgPortType proxy = service.getBareDocLitNoArgPort();
        BindingProvider p = (BindingProvider) proxy;

        p.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        p.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "twoWayEmpty");
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_URL);
        
        String response = proxy.twoWayEmpty();
        assertTrue("Did not receive expected response value", 
                   "org.apache.axis2.jaxws.sample.doclitbarenoarg.DocLitBareNoArgPortTypeImpl.twoWayEmpty".equals(response));
    }

    public void testTwoWayEmpty_No_SOAPACTION() {
        BareDocLitNoArgService service = new BareDocLitNoArgService();
        DocLitBareNoArgPortType proxy = service.getBareDocLitNoArgPort();
        BindingProvider p = (BindingProvider) proxy;

        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_URL);
        
        String response = proxy.twoWayEmpty();
        assertTrue("Did not receive expected response value", 
                   "org.apache.axis2.jaxws.sample.doclitbarenoarg.DocLitBareNoArgPortTypeImpl.twoWayEmpty".equals(response));
    }
}
