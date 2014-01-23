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
package org.apache.axis2.transport.sms;

import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.TransportOutDescription;

/**
 * Transport sender that sends the SMS messages form the Axis2 Engine
 */
public class SMSSender extends AbstractTransportSender {
    private SMSManager smsManager;



    public void cleanup(MessageContext msgContext) throws AxisFault {

    }

    public void sendMessage(MessageContext msgCtx, String targetEPR, OutTransportInfo outTransportInfo) throws AxisFault {

        smsManager.sendSMS(msgCtx);

    }

    public void init(ConfigurationContext confContext, TransportOutDescription transportOut) throws AxisFault {
        smsManager = new SMSManager();
        smsManager.init(transportOut, confContext);
    }

    public void stop() {
        smsManager.stop();
    }
}
