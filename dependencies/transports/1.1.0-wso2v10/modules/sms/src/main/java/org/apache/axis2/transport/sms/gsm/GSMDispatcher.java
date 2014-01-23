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
package org.apache.axis2.transport.sms.gsm;

import org.smslib.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import org.apache.axis2.transport.sms.SMSMessage;
import org.apache.axis2.transport.sms.SMSManager;

/**
 * GSMDispatcher will access the gsm modem and dispatch the incomming SMS s to the Axis2
 */
public class GSMDispatcher implements Runnable{

    protected Log log = LogFactory.getLog(this.getClass());
    private boolean keepPolling = true;

    private Service service;
    private long pollInterval=5000;
    private SMSManager smsManager;
    /**
     * To create a GSMDispatcher a service object that is created for the current GSM modem is needed
     * @param service
     * @param manager
     */
    public GSMDispatcher(Service service , SMSManager manager) {
        this.service = service;
        this.smsManager = manager;
    }

    public void run() {

        while(keepPolling) {
            List<InboundMessage> arrayList = new ArrayList<InboundMessage>();
            try {
                service.readMessages(arrayList, InboundMessage.MessageClasses.UNREAD) ;

                for(InboundMessage msg : arrayList) {
                    SMSMessage sms =null;
                    synchronized (this) {
                        sms= new SMSMessage(msg.getOriginator(),null,msg.getText() ,SMSMessage.IN_MESSAGE);
                    }
                    smsManager.dispatchToAxis2(sms);
                    //delete the message form inbox
                    service.deleteMessage(msg);
                }
            } catch (Exception ex) {
                log.error("Error Occured while reading messages",ex);
            }

            try {
                Thread.sleep(pollInterval);
            } catch (InterruptedException e) {

            }
        }
    }


    public void stopPolling(){
        keepPolling = false;
    }

    /**
     * set the modem Polling time
     * modem polling time will be the time interval that the modem will be polled to get the unread messages
     * form the inbox.
     * the optimal value for this will be diffrent form modem to modem.
     * Keep the default value of you are not aware of it
     * @param pollInterval
     */
    public void setPollInterval(long pollInterval) {
        this.pollInterval = pollInterval;
    }
}
