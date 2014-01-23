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

package org.apache.axis2.jaxws.provider;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;

import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

public class StringMessageProviderTests extends ProviderTestCase {

    String endpointUrl = "http://localhost:6060/axis2/services/StringMessageProviderService.StringMessageProviderPort";
    String xmlString = "<test>test input</test>";
    private QName serviceName = new QName("http://ws.apache.org/axis2", "StringMessageProviderService");

    public static Test suite() {
        return getTestSetup(new TestSuite(StringMessageProviderTests.class));
    }
        
    public void testProviderString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, endpointUrl);
        
        Dispatch<String> dispatch = svc
                .createDispatch(portName, String.class, Service.Mode.PAYLOAD);

        TestLogger.logger.debug(">> Invoking Dispatch<String> StringMessageProviderService");
        String retVal = dispatch.invoke(xmlString);
        TestLogger.logger.debug(">> Response [" + retVal + "]");
        
        
        // Try again to verify
        TestLogger.logger.debug(">> Invoking Dispatch<String> StringMessageProviderService");
        retVal = dispatch.invoke(xmlString);
        TestLogger.logger.debug(">> Response [" + retVal + "]");
    }
}
