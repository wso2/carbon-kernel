/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.axis2.endpoint;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.transport.testkit.axis2.MessageContextValidator;
import org.apache.axis2.transport.testkit.endpoint.AsyncEndpoint;
import org.apache.axis2.transport.testkit.endpoint.InOnlyEndpointSupport;
import org.apache.axis2.transport.testkit.message.AxisMessage;
import org.apache.axis2.transport.testkit.message.IncomingMessage;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.Transient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AxisAsyncEndpoint extends AxisTestEndpoint implements AsyncEndpoint<AxisMessage> {
    private static Log log = LogFactory.getLog(AxisAsyncEndpoint.class);
    
    private @Transient AxisTestEndpointContext context;
    private @Transient MessageContextValidator[] validators;
    private @Transient InOnlyEndpointSupport<AxisMessage> support;
    
    @Setup @SuppressWarnings("unused")
    private void setUp(AxisTestEndpointContext context, MessageContextValidator[] validators) {
        this.context = context;
        this.validators = validators;
        support = new InOnlyEndpointSupport<AxisMessage>();
    }
    
    @Override
    protected InOnlyAxisOperation createOperation() {
        InOnlyAxisOperation operation = new InOnlyAxisOperation(new QName("default"));
        operation.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) throws AxisFault {
                AxisAsyncEndpoint.this.receive(messageCtx);
            }
        });
        return operation;
    }

    void receive(MessageContext messageCtx) throws AxisFault {
        log.debug("MessageReceiver has been invoked");
        final AxisMessage messageData;
        try {
            Assert.assertTrue(messageCtx.isServerSide());
            
            TransportInDescription transportIn = messageCtx.getTransportIn();
            Assert.assertNotNull("transportIn not set on message context", transportIn);
            Assert.assertEquals(context.getTransportName(), transportIn.getName());
            
            Assert.assertEquals(context.getTransportName(), messageCtx.getIncomingTransportName());
            
            for (MessageContextValidator validator : validators) {
                validator.validate(messageCtx, false);
            }
            messageData = new AxisMessage(messageCtx);
        }
        catch (Throwable ex) {
            support.putException(ex);
            return;
        }
        support.putMessage(null, messageData);
    }

    @Override
    protected void onTransportError(Throwable ex) {
        support.putException(ex);
    }
    
    public void clear() throws Exception {
        support.clear();
    }

    public IncomingMessage<AxisMessage> waitForMessage(int timeout) throws Throwable {
        return support.waitForMessage(timeout);
    }
}
