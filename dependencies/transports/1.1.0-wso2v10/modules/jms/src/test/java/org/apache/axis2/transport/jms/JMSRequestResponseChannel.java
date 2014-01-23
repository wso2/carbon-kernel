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

package org.apache.axis2.transport.jms;

import javax.jms.Destination;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.testkit.axis2.client.AxisTestClientConfigurator;
import org.apache.axis2.transport.testkit.channel.RequestResponseChannel;
import org.apache.axis2.transport.testkit.name.Key;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;

public class JMSRequestResponseChannel extends JMSChannel implements RequestResponseChannel, AxisTestClientConfigurator {
    private final String replyDestinationType;
    private @Transient String replyDestinationName;
    private @Transient String replyJndiName;
    private @Transient Destination replyDestination;
    
    public JMSRequestResponseChannel(String name, String destinationType, String replyDestinationType, ContentTypeMode contentTypeMode) {
        super(name, destinationType, contentTypeMode);
        this.replyDestinationType = replyDestinationType;
    }
    
    public JMSRequestResponseChannel(String destinationType, String replyDestinationType, ContentTypeMode contentTypeMode) {
        this(null, destinationType, replyDestinationType, contentTypeMode);
    }
    
    @Setup @SuppressWarnings("unused")
    private void setUp(JMSTestEnvironment env) throws Exception {
        replyDestinationName = buildDestinationName("response", replyDestinationType);
        replyJndiName = buildJndiName("response", replyDestinationType);
        replyDestination = env.createDestination(replyDestinationType, replyDestinationName);
        context.bind(replyJndiName, replyDestination);
    }

    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        context.unbind(replyJndiName);
        env.deleteDestination(replyDestination);
    }

    @Override
    public void setupService(AxisService service, boolean isClientSide) throws Exception {
        super.setupService(service, isClientSide);
        service.addParameter(JMSConstants.PARAM_REPLY_DEST_TYPE, replyDestinationType);
        service.addParameter(JMSConstants.PARAM_REPLY_DESTINATION, replyJndiName);
    }

    public void setupRequestMessageContext(MessageContext msgContext) {
//        msgContext.setProperty(JMSConstants.JMS_REPLY_TO, replyDestinationName);
    }

    @Override
    public EndpointReference getEndpointReference() throws Exception {
        String address = super.getEndpointReference().getAddress();
        return new EndpointReference(address + "&" + JMSConstants.PARAM_REPLY_DEST_TYPE + "=" + replyDestinationType + "&" + JMSConstants.PARAM_REPLY_DESTINATION + "=" + replyJndiName);
    }

    @Key("replyDestType")
    public String getReplyDestinationType() {
        return replyDestinationType;
    }

    public Destination getReplyDestination() {
        return replyDestination;
    }
}
