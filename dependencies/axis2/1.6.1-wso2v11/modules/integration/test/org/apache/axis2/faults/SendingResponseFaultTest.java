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

package org.apache.axis2.faults;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.integration.LocalTestCase;
import org.apache.axis2.client.ServiceClient;

import javax.xml.namespace.QName;

public class SendingResponseFaultTest extends LocalTestCase {
    public static class Service {
        public OMElement getBadResponse(OMElement e) {
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMElement elem = fac.createOMElement(new QName("http://ns", "text"));
            String badText = "this char is bad - " + '\10';
            elem.setText(badText);
            return elem;
        }
    }

    public void testBadResponse() throws Exception {
        String SERVICE = "service";
        deployClassAsService(SERVICE, Service.class);
        ServiceClient client = getClient(SERVICE, "getBadResponse");
        try {
            client.sendReceive(null);
        } catch (AxisFault axisFault) {
            // Make sure this is NOT the "real" fault (which should have been swallowed while
            // writing the response)
            assertFalse("Got unexpected fault",
                        axisFault.getMessage().contentEquals(new StringBuffer("Invalid white space character")));
        }
    }
}
