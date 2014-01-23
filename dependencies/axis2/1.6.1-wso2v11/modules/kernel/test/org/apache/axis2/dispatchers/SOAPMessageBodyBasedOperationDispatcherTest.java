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

package org.apache.axis2.dispatchers;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;

import javax.xml.namespace.QName;

public class SOAPMessageBodyBasedOperationDispatcherTest extends TestCase {

    public void testFindOperation() throws AxisFault {
        MessageContext messageContext;
        AxisService as1 = new AxisService("Service1");


        AxisOperation operation1 = new InOnlyAxisOperation(new QName("operation1"));
        AxisOperation operation2 = new InOnlyAxisOperation(new QName("operation2"));
        as1.addOperation(operation1);
        as1.addOperation(operation2);

        ConfigurationContext cc = ConfigurationContextFactory.createEmptyConfigurationContext();
        AxisConfiguration ac = cc.getAxisConfiguration();
        ac.addService(as1);
        messageContext = cc.createMessageContext();

        messageContext.setAxisService(as1);

        SOAPEnvelope se = OMAbstractFactory.getSOAP11Factory().createSOAPEnvelope();
        SOAPBody sb = OMAbstractFactory.getSOAP11Factory().createSOAPBody(se);
        sb.addChild(OMAbstractFactory.getSOAP11Factory().createOMElement("operation2",
                                                                         "http://test", "pfx"));
        messageContext.setEnvelope(se);


        SOAPMessageBodyBasedOperationDispatcher ruisd =
                new SOAPMessageBodyBasedOperationDispatcher();
        ruisd.invoke(messageContext);

        assertEquals(operation2, messageContext.getAxisOperation());
    }

}
