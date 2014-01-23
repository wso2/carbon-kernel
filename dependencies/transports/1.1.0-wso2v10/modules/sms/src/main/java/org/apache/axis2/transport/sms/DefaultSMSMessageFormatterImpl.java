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

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.sms.smpp.SMPPTransportOutDetails;
import org.apache.axis2.description.Parameter;
import org.apache.axiom.om.OMElement;

import java.util.Iterator;

public class DefaultSMSMessageFormatterImpl implements SMSMessageFormatter{


    public SMSMessage formatSMS(MessageContext messageContext) throws Exception {
        String sendTo;
        //phone number set at the Transport configuration get the precidence
        String axis2PhoneNumber = SMPPTransportOutDetails.getInstence().getPhoneNumber() ;
        Object s= messageContext.getProperty(SMSTransportConstents.SEND_TO);
        if (s != null) {
           sendTo  = (String)s;

        } else {
             sendTo = SMSTransportUtils.getPhoneNumber(messageContext.getTo());
        }
        OMElement elem = messageContext.getEnvelope().getBody();
        String content = "Empty responce";
        boolean cont = true;
        while(cont) {

            content = elem.getFirstElement().getText();
            if("".equals(content) || content == null) {
                elem = elem.getFirstElement();
                if(elem == null) {
                    cont = false;
                    content = "Empty responce";
                }
            } else {
                cont = false;
            }
        }

        //if not configured in the Transport configuration
        if("0000".equals(axis2PhoneNumber)) {
            String axisPhone  = (String)messageContext.getProperty(SMSTransportConstents.DESTINATION);
            if(axisPhone != null) {
                axis2PhoneNumber = axisPhone;
            }
        }
        SMSMessage sms = new SMSMessage( axis2PhoneNumber, sendTo , content ,SMSMessage.OUT_MESSAGE);
        handleMessageContextProperties(sms,messageContext);
        return sms;

    }

    private void handleMessageContextProperties(SMSMessage sms , MessageContext messageContext) {
       
        Iterator<String> it = messageContext.getPropertyNames();

        while(it.hasNext()) {
            String key = it.next();
            sms.addProperty(key , messageContext.getProperty(key));
        }


    }


}
