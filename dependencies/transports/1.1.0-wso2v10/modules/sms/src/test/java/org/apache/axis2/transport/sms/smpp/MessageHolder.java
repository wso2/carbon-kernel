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

import java.util.concurrent.Semaphore;

/**
 * Message Hoder will hold the incomming shotmessages so that they can be processed.
 */
public class MessageHolder implements SMSCMessageObserver{

    private boolean haveMessage = false;
    private String sms = null;

    private Semaphore control = new Semaphore(0);
    public void messsageIn(SubmitSm msg) {
        sms = new String(msg.getShortMessage());
        sms = sms.trim();
        haveMessage = true;
        control.release();
    }

    public boolean isHaveMessage() {
        return haveMessage;
    }

    public void setHaveMessage(boolean haveMessage) {
        this.haveMessage = haveMessage;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public Semaphore getControl() {
        return control;
    }
}
