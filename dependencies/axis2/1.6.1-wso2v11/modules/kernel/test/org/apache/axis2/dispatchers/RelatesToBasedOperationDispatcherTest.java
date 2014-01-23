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
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;

import javax.xml.namespace.QName;

public class RelatesToBasedOperationDispatcherTest extends TestCase {

    public void testFindOperation() throws AxisFault {

        MessageContext messageContext;
        AxisService as1 = new AxisService("Service1");
        AxisConfiguration ac = new AxisConfiguration();
        ac.addService(as1);

        AxisOperation operation1 = new InOnlyAxisOperation(new QName("operation1"));
        AxisOperation operation2 = new InOnlyAxisOperation(new QName("operation2"));
        as1.addOperation(operation1);
        as1.addOperation(operation2);

        ConfigurationContext cc = new ConfigurationContext(ac);
        ServiceGroupContext sgc = cc.createServiceGroupContext(as1.getAxisServiceGroup());
        ServiceContext serviceContext = sgc.getServiceContext(as1);
        OperationContext oc1 = serviceContext.createOperationContext(operation1);
        OperationContext oc2 = serviceContext.createOperationContext(operation2);
        cc.registerOperationContext("urn:org.apache.axis2.dispatchers.messageid:123", oc1);
        cc.registerOperationContext("urn:org.apache.axis2.dispatchers.messageid:456", oc2);
        messageContext = cc.createMessageContext();
        messageContext
                .addRelatesTo(new RelatesTo("urn:org.apache.axis2.dispatchers.messageid:456"));
        messageContext.setAxisService(as1);

        RelatesToBasedOperationDispatcher ruisd = new RelatesToBasedOperationDispatcher();
        ruisd.invoke(messageContext);

        assertEquals(operation2, messageContext.getAxisOperation());
    }

}
