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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

/**
 * SMS message is a atomic object wich carries a SMS
 * SMS has can be either a IN message or a OUT message
 * which will have the details sender , receiver ,Content and properties
 * sender , receiver has a implied meaning with the Message direction
 * <br>
 * eg:
 * in a IN_MESSAGE sender  : the phone number of the phone that sms has been sent to axis2
 *               receiver  : the phone number given from the SMSC to the Axis2
 * in a OUT_MESSAGE sender : the phone number given from the SMSC to the Axis2
 *                receiver : the phone number of the phone that sms created from Axis2 must deliver to
 */
public class SMSMessage {


    private String sender;
    private String receiver;
    private String content;
    private int direction;
    private Map<String ,Object> properties = new HashMap<String , Object>();
    

    public static int IN_MESSAGE =1;
    public static int OUT_MESSAGE =2;

    /**
     *  
     * @param sender
     * @param reciever
     * @param content
     * @param direction
     * @throws AxisFault
     */
    public SMSMessage(String sender ,String reciever, String content , int direction) throws AxisFault {
        this.sender = sender;
        this.content = content;
        this.receiver = reciever;
        if (direction == IN_MESSAGE || direction == OUT_MESSAGE ) {
            this.direction = direction;

        } else {
            throw new AxisFault("Message must be in or out");
        }

    }

    /**
     * Retuen the Phone Number of the Sender
     * @return String that contain the senders phone Number
     */
    public String getSender() {
        return sender;
    }

    /**
     * Return the phone Number that SMS must be received to
     * @return String  that Contain the receivers phone Number
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * Return The Contect that will be send with the SMS
     * @return String that contain the content
     */
    public String getContent() {
        return content;
    }

    /**
     * return the Message Direction of the SMSMessage
     * That should be either SMS_IN_MESSAGE :1
     * Or SMS_OUT_MESSAGE : 2
     * @return int that will infer the message direction
     */
    public int getDirection() {
        return direction;
    }

    /**
     * add the Implementation level properties that properties will be add to the Axis2 Message Context
     * @param key
     * @param value
     */
    public void addProperty(String key, Object value) {
        if(key != null && value != null) {
            properties.put(key,value);
        }
    }

    /**
     * Return the properties of the SMS message
     * @return
     */
    public Map<String , Object> getProperties() {
        return properties;
    }
}
