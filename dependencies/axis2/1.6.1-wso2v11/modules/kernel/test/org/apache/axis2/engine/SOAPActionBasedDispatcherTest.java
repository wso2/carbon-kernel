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

import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.dispatchers.SOAPActionBasedDispatcher;

import javax.xml.namespace.QName;

public class SOAPActionBasedDispatcherTest extends TestCase {

    public void testFindOperation() throws Exception {
        ConfigurationContext cc = new ConfigurationContext(new AxisConfiguration());
        MessageContext messageContext = cc.createMessageContext();
        AxisService as = new AxisService("Service1");
        messageContext.setAxisService(as);

        AxisOperation operation1 = new InOnlyAxisOperation(new QName("operation1"));
        operation1.setSoapAction("urn:org.apache.axis2.dispatchers.test:operation1");

        AxisOperation operation2 = new InOnlyAxisOperation(new QName("operation2"));
        operation2.setSoapAction("urn:org.apache.axis2.dispatchers.test:operation2");

        as.addOperation(operation1);
        as.addOperation(operation2);

        messageContext.setSoapAction("urn:org.apache.axis2.dispatchers.test:operation1");

        SOAPActionBasedDispatcher soapActionDispatcher = new SOAPActionBasedDispatcher();
        soapActionDispatcher.invoke(messageContext);
        assertEquals(operation1, messageContext.getAxisOperation());
    }

    public void testEmptyAction() throws Exception {
        // We shouldn't be able to route on an emtpy-string action.
        MessageContext messageContext = new MessageContext();
        AxisService as = new AxisService("Service1");
        messageContext.setAxisService(as);

        AxisOperation operation1 = new InOnlyAxisOperation(new QName("operation1"));
        operation1.setSoapAction("");

        AxisOperation operation2 = new InOnlyAxisOperation(new QName("operation2"));
        operation2.setSoapAction("");

        as.addOperation(operation1);
        as.addOperation(operation2);

        messageContext.setSoapAction("");

        SOAPActionBasedDispatcher soapActionDispatcher = new SOAPActionBasedDispatcher();
        soapActionDispatcher.invoke(messageContext);
        assertNull(messageContext.getAxisOperation());
    }

    public void testNullAction() throws Exception {
        // We shouldn't be able to route on a null action.
        MessageContext messageContext = new MessageContext();
        AxisService as = new AxisService("Service1");
        messageContext.setAxisService(as);

        AxisOperation operation1 = new InOnlyAxisOperation(new QName("operation1"));
        operation1.setSoapAction(null);

        AxisOperation operation2 = new InOnlyAxisOperation(new QName("operation2"));
        operation2.setSoapAction(null);

        as.addOperation(operation1);
        as.addOperation(operation2);

        messageContext.setSoapAction(null);

        SOAPActionBasedDispatcher soapActionDispatcher = new SOAPActionBasedDispatcher();
        soapActionDispatcher.invoke(messageContext);
        assertNull(messageContext.getAxisOperation());
    }

    public void testDuplicateAction() throws Exception {
        // We shouldn't be able to route on a SOAPAction that is a duplicate.
        MessageContext messageContext = new MessageContext();
        AxisService as = new AxisService("Service1");
        messageContext.setAxisService(as);

        AxisOperation operation1 = new InOnlyAxisOperation(new QName("operation1"));
        operation1.setSoapAction("urn:org.apache.axis2.dispatchers.test:operation1");

        AxisOperation operation2 = new InOnlyAxisOperation(new QName("operation2"));
        operation2.setSoapAction("urn:org.apache.axis2.dispatchers.test:operation2");

        as.addOperation(operation1);
        as.addOperation(operation2);

        messageContext.setSoapAction("urn:org.apache.axis2.dispatchers.test:operation2");

        SOAPActionBasedDispatcher soapActionDispatcher = new SOAPActionBasedDispatcher();
        soapActionDispatcher.invoke(messageContext);
        assertEquals(operation2, messageContext.getAxisOperation());

        // Now add a duplicate action, then validate we can't route on it anymore.
        AxisOperation operation3 = new InOnlyAxisOperation(new QName("operation3"));
        // Note that the SOAPAction is intentionally duplicated with operation 2 above.
        operation3.setSoapAction("urn:org.apache.axis2.dispatchers.test:operation2");
        as.addOperation(operation3);

        messageContext = new MessageContext();
        messageContext.setAxisService(as);
        messageContext.setSoapAction("urn:org.apache.axis2.dispatchers.test:operation2");
        soapActionDispatcher.invoke(messageContext);
        assertNull(messageContext.getAxisOperation());

        // Now verify that adding another operation with the duplicate SOAPAction 
        // doesn't somehow get it added back into the valid alias map
        AxisOperation operation4 = new InOnlyAxisOperation(new QName("operation4"));
        // Note that the SOAPAction is intentionally duplicated with operation 2 above.
        operation4.setSoapAction("urn:org.apache.axis2.dispatchers.test:operation2");
        as.addOperation(operation4);

        messageContext = new MessageContext();
        messageContext.setAxisService(as);
        messageContext.setSoapAction("urn:org.apache.axis2.dispatchers.test:operation2");
        soapActionDispatcher.invoke(messageContext);
        assertNull(messageContext.getAxisOperation());

        // And finally, verify that after all the above, we can still route on a valid
        // SOAPAction for operation 1 (whose SOAPAction was never duplicated, so should still be
        // valid)
        messageContext = new MessageContext();
        messageContext.setAxisService(as);
        messageContext.setSoapAction("urn:org.apache.axis2.dispatchers.test:operation1");
        soapActionDispatcher.invoke(messageContext);
        assertEquals(operation1, messageContext.getAxisOperation());


    }
        
    public void testActionVsOpName() throws Exception {
        // Should route based on action before trying to route based on operation name
        // - method operation1, action = operation2
        // - method operation2, action = operation1
        // Incoming action should route to:
        // - action = operation2, route to operation1
        // - action = operation1, route to operation2
        MessageContext messageContext = new MessageContext();
        AxisService as = new AxisService("Service1");
        messageContext.setAxisService(as);
        
        AxisOperation operation1 = new InOnlyAxisOperation(new QName("operation1"));
        operation1.setSoapAction("operation2");
        
        AxisOperation operation2 = new InOnlyAxisOperation(new QName("operation2"));
        operation2.setSoapAction("operation1");
        
        as.addOperation(operation1);
        as.addOperation(operation2);
        
        messageContext.setSoapAction("operation2");
        
        SOAPActionBasedDispatcher soapActionDispatcher = new SOAPActionBasedDispatcher();
        soapActionDispatcher.invoke(messageContext);
        // Since Axis2 uses a single alias table for SOAPActions and operation names (and
        // WS-Addressing actions and input message names), having an action with the same name
        // as a different operation will cause that alias to be unusable for routing.
//        assertEquals(operation1, messageContext.getAxisOperation());
        assertNull(messageContext.getAxisOperation());
        
        // Make sure the other action works also
        messageContext = new MessageContext();
        messageContext.setAxisService(as);
        messageContext.setSoapAction("operation1");
        soapActionDispatcher = new SOAPActionBasedDispatcher();
        soapActionDispatcher.invoke(messageContext);
//        assertEquals(operation2, messageContext.getAxisOperation());
        assertNull(messageContext.getAxisOperation());
        
    }
    
    public void testInvalidActionVsOpName() throws Exception {
        // Shouldn't route on action because it is invalid (used > 1 time) even though it matches
        // an operation name
        // - method operation1, action = operation2
        // - method operation2, action = operation2
        // Incoming action should route to:
        // - action = operation2, null
        // - action = operation1, route to operation1 (based on method name match to action)
        // 
        MessageContext messageContext = new MessageContext();
        AxisService as = new AxisService("Service1");
        messageContext.setAxisService(as);
        
        AxisOperation operation1 = new InOnlyAxisOperation(new QName("operation1"));
        operation1.setSoapAction("operation2");
        
        AxisOperation operation2 = new InOnlyAxisOperation(new QName("operation2"));
        operation2.setSoapAction("operation2");
        
        as.addOperation(operation1);
        as.addOperation(operation2);
        
        messageContext.setSoapAction("operation2");
        
        SOAPActionBasedDispatcher soapActionDispatcher = new SOAPActionBasedDispatcher();
        soapActionDispatcher.invoke(messageContext);
        // Verify that we can't route on the SOAPAction because it has been specified > 1 time
        assertNull(messageContext.getAxisOperation());
        
        // Now verify that a SOAPAction that happens to map to an operation name can still be routed
        messageContext = new MessageContext();
        messageContext.setAxisService(as);
        messageContext.setSoapAction("operation1");
        soapActionDispatcher = new SOAPActionBasedDispatcher();
        soapActionDispatcher.invoke(messageContext);
        assertEquals(operation1, messageContext.getAxisOperation());
    }
}
