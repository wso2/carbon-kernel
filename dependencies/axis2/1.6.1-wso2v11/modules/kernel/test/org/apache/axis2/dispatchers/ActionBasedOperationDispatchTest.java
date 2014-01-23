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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;

import javax.xml.namespace.QName;
import java.util.ArrayList;

public class ActionBasedOperationDispatchTest extends TestCase {

    public void testFindOperation() throws Exception {
        MessageContext messageContext = new MessageContext();
        AxisService as = new AxisService("Service1");
        messageContext.setAxisService(as);

        AxisOperation operation1 = new InOnlyAxisOperation(new QName("operation1"));
        ArrayList op1actions = new ArrayList();
        op1actions.add("urn:org.apache.axis2.dispatchers.test:operation1");
        operation1.setWsamappingList(op1actions);

        AxisOperation operation2 = new InOnlyAxisOperation(new QName("operation2"));
        ArrayList op2actions = new ArrayList();
        op2actions.add("urn:org.apache.axis2.dispatchers.test:operation2");
        operation2.setWsamappingList(op2actions);

        as.addOperation(operation1);
        as.addOperation(operation2);

        as.mapActionToOperation("urn:org.apache.axis2.dispatchers.test:operation1", operation1);
        as.mapActionToOperation("urn:org.apache.axis2.dispatchers.test:operation2", operation2);

        messageContext.setWSAAction("urn:org.apache.axis2.dispatchers.test:operation2");

        ActionBasedOperationDispatcher abod = new ActionBasedOperationDispatcher();
        abod.invoke(messageContext);
        assertEquals(operation2, messageContext.getAxisOperation());
    }

}
