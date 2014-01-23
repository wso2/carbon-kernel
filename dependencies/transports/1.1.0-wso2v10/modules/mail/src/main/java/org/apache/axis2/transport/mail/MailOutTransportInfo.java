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

import org.apache.axis2.transport.OutTransportInfo;

import javax.mail.internet.InternetAddress;

/**
 * The Mail OutTransportInfo is a holder of information to send an outgoing message
 * (e.g. a Response or a request) to a Mail destination. Thus at a minimum a
 * destination email address is held
 */
public class MailOutTransportInfo implements OutTransportInfo {

    /** The address of the destination */
    private InternetAddress[] targetAddresses = null;
    /** The address of the service that is replying */
    private InternetAddress fromAddress = null;
    /** A list of email addresses to which the reply must be copied */
    private InternetAddress[] ccAddresses = null;
    /** The subject for the reply message */
    private String subject = null;
    /** The message ID of the request message, when this refers to a reply to it */
    private String requestMessageID = null;

    MailOutTransportInfo(InternetAddress fromAddress) {
        this.fromAddress = fromAddress;
    }

    public void setContentType(String contentType) {
    }

    public InternetAddress[] getTargetAddresses() {
        return targetAddresses;
    }

    public void setTargetAddresses(InternetAddress[] targetAddresses) {
        this.targetAddresses = targetAddresses;
    }

    public InternetAddress getFromAddress() {
        return fromAddress;
    }

    public InternetAddress[] getCcAddresses() {
        return ccAddresses;
    }

    public void setCcAddresses(InternetAddress[] ccAddresses) {
        this.ccAddresses = ccAddresses;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRequestMessageID() {
        return requestMessageID;
    }

    public void setRequestMessageID(String requestMessageID) {
        this.requestMessageID = requestMessageID;
    }
}
