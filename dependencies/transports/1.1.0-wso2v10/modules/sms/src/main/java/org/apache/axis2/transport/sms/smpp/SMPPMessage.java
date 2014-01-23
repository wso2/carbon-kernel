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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SMPP message is a atomic object wich carries a SMPPMessage
 */
public class SMPPMessage {
   

    private String sender;
    private String receiver;
    private String content;
    private int direction;

    public static int IN_MESSAGE =1;
    public static int OUT_MESSAGE =2;
    public SMPPMessage(String sender ,String reciever, String content , int direction) throws AxisFault {
        this.sender = sender;
        this.content = content;
        this.receiver = reciever;
        if (direction == IN_MESSAGE || direction == OUT_MESSAGE ) {
            this.direction = direction;

        } else {
            throw new AxisFault("Message must be in or out");    
        }

    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public int getDirection() {
        return direction;
    }
}
