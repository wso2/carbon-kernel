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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;

/**
 * By the time the control comes to this handler, the dispatching must have happened
 * so that the message context contains the AxisServiceGroup, AxisService and
 * AxisOperation.
 * This will then try to find the Contexts of ServiceGroup, Service and the Operation.
 * @deprecated The functionality of this class has moved into the DispatchPhase postconditions
 */
public class InstanceDispatcher extends AbstractHandler {

    /**
     * This doesn't do anything, as the functionality is now in DispatchPhase.checkPostConditions()
     * The class remains for backwards compatibility of axis2.xml files, but it should go away after
     * 1.3.
     *
     * @param msgContext the <code>MessageContext</code> to process with this <code>Handler</code>.
     * @return An InvocationResponse that indicates what the next step in the message processing
     *         should be.
     * @throws org.apache.axis2.AxisFault if the handler encounters an error
     */
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        return InvocationResponse.CONTINUE;
    }
}
