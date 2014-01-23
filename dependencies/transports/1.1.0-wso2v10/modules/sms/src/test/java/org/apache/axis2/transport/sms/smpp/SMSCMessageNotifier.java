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
package org.apache.axis2.transport.sms.smpp;

import org.jsmpp.bean.SubmitSm;

import java.util.ArrayList;

/**
 * Manage the SMSCMessageObservers and notify them when a new SMSC message is came in
 */
public class SMSCMessageNotifier {

    private ArrayList<SMSCMessageObserver> observers = new ArrayList<SMSCMessageObserver>();

    private static SMSCMessageNotifier smsMessageNotifier = new SMSCMessageNotifier();
    private SMSCMessageNotifier() {

    }
    public static SMSCMessageNotifier getInstence() {
        return smsMessageNotifier;
    }

    /**
     * Notify the registered Observer about the incomming shotMessage
     * @param sm
     */
    public void notifyObservers(SubmitSm sm) {
        for(SMSCMessageObserver o : observers) {
            o.messsageIn(sm);    
        }
    }

    /**
     * register the SMSCMessageObserver with the SMSCMessageNotifier so that observer will get notified
     * when the new shot message came from the SMSC
     * @param o
     */
    public void addObserver(SMSCMessageObserver o) {
        if(o != null) {
            observers.add(o);
        }
    }

    /**
     * Un register the SMSCMessageObserver so that Observer will not notifed of new Shot Message arrivals after that
     * @param o
     */
    public void removeObserver(SMSCMessageObserver o) {

        observers.remove(o);
    }

}
