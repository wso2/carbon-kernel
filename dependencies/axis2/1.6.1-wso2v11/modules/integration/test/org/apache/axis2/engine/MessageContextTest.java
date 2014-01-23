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

package org.apache.axis2.engine;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;

public class MessageContextTest extends AbstractTestCase {
    public MessageContextTest(String testName) {
        super(testName);
    }

    public void testMesssageContext() throws AxisFault,
            SOAPProcessingException {
        AxisConfiguration er = new AxisConfiguration();
        AxisService servicesDesc = new AxisService();
        servicesDesc.setName("testService");
        er.addService(servicesDesc);

        ConfigurationContext engineContext = new ConfigurationContext(er);

        MessageContext msgctx = engineContext.createMessageContext();

        SOAPFactory omFac = OMAbstractFactory.getSOAP11Factory();

        msgctx.setEnvelope(omFac.getDefaultEnvelope());
        assertNotNull(msgctx.getEnvelope());

        msgctx.setFaultTo(null);
        assertNull(msgctx.getFaultTo());

        msgctx.setFrom(null);
        assertNull(msgctx.getFrom());

        msgctx.setReplyTo(null);
        assertNull(msgctx.getReplyTo());
    }
}
