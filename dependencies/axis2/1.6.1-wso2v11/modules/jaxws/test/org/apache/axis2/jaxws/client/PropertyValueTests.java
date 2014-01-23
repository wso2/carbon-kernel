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

import junit.framework.TestCase;

import org.apache.axis2.jaxws.unitTest.TestLogger;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import java.util.Map;

/**
 * A suite to test the validation of the client request/response context properties
 */
public class PropertyValueTests extends TestCase {
    
    public PropertyValueTests(String name) {
        super(name);
    }
    
    public void testSetInvalidClientProperties() throws Exception {
        Service svc = Service.create(new QName("http://test", "TestService"));
        QName portQName = new QName("http://test", "TestPort");
        svc.addPort(portQName, null, null);
        Dispatch dispatch = svc.createDispatch(portQName, String.class, Mode.PAYLOAD);
        
        Map<String, Object> map = dispatch.getRequestContext();
        
        try {
            map.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, new Integer(4));
            fail();
        }
        catch (WebServiceException wse) {
            TestLogger.logger.debug("[pass] - exception thrown as expected");
        }

        try {
            map.put(BindingProvider.USERNAME_PROPERTY, new Integer(4));
            fail();
        }
        catch (WebServiceException wse) {
            TestLogger.logger.debug("[pass] - exception thrown as expected");
        }
        
        try {
            map.put(BindingProvider.PASSWORD_PROPERTY, new Integer(4));
            fail();
        }
        catch (WebServiceException wse) {
            TestLogger.logger.debug("[pass] - exception thrown as expected");
        }

        try {
            map.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, "true");
            fail();
        }
        catch (WebServiceException wse) {
            TestLogger.logger.debug("[pass] - exception thrown as expected");
        }
        
        try {
            map.put(BindingProvider.SOAPACTION_USE_PROPERTY, "true");
            fail();
        }
        catch (WebServiceException wse) {
            TestLogger.logger.debug("[pass] - exception thrown as expected");
        }

        try {
            map.put(BindingProvider.SOAPACTION_URI_PROPERTY, new Integer(4));
            fail();
        }
        catch (WebServiceException wse) {
            TestLogger.logger.debug("[pass] - exception thrown as expected");
        }
    }

}
