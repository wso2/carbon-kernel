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

package org.apache.axis2.transport.mail;

import java.util.Map;

import junit.framework.Assert;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.testkit.axis2.MessageContextValidator;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.Transient;

public class MailMessageContextValidator extends Assert implements MessageContextValidator {
    public static final MailMessageContextValidator INSTANCE = new MailMessageContextValidator();
    
    private @Transient MailChannel channel;
    
    private MailMessageContextValidator() {}
    
    @Setup @SuppressWarnings("unused")
    private void setUp(MailChannel channel) {
        this.channel = channel;
    }
    
    public void validate(MessageContext msgContext, boolean isResponse) throws Exception {
        Map<?,?> trpHeaders = (Map<?,?>)msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
        String from = (String)trpHeaders.get(MailConstants.MAIL_HEADER_FROM);
        String to = (String)trpHeaders.get(MailConstants.MAIL_HEADER_TO);
        if (isResponse) {
            // TODO: The transport headers in the response message context are not set correctly.
            //       There are two issues:
            //        * SynchronousCallback doesn't propagate the transport headers
            //        * OutInAxisOperation#send(MessageContext) overwrites the TRANSPORT_HEADERS
            //          property with the value from the request message context.
//            assertEquals(channel.getSender().getAddress(), to);
//            assertEquals(channel.getRecipient().getAddress(), from);
        } else {
            assertEquals(channel.getSender().getAddress(), from);
            assertEquals(channel.getRecipient().getAddress(), to);
        }
    }
}
