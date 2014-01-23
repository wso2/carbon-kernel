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

/**
 * Holds the SMPP implimantations transport out details
 */
public class SMPPTransportOutDetails {
    //smpp system type
    private String systemType ="cp";
    private String systemId;
    private String password;
    private String host="127.0.0.1";
    private String phoneNumber="0000";
    private int port = 2775;
    private int enquireLinkTimer = 50000;
    private int transactionTimer = 100000;

    private static SMPPTransportOutDetails smppTransportOutDetails;

    private SMPPTransportOutDetails(){

    }

    /**
     * Get the referrence to the Singleton instence of the SMPP Transport Out details
     * at the first request it will create a empty object
     * @return SMPPTransportOutDetails instence
     */
    public static SMPPTransportOutDetails getInstence() {
        if(smppTransportOutDetails ==null) {
            smppTransportOutDetails = new SMPPTransportOutDetails();
        }
        return smppTransportOutDetails;
    }

    public int getTransactionTimer() {
        return transactionTimer;
    }

    public void setTransactionTimer(int transactionTimer) {
        this.transactionTimer = transactionTimer;
    }

    public int getEnquireLinkTimer() {
        return enquireLinkTimer;
    }

    public void setEnquireLinkTimer(int enquireLinkTimer) {
        this.enquireLinkTimer = enquireLinkTimer;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getSystemType() {
        return systemType;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    @Override
    public String toString() {
        return "TransportOutDetails::  SystemId: " +systemId +" systemType: "+ systemType + " password: "
                +password+" host: "+host+" port: " +port+" phone NUmber" + phoneNumber;
    }
}
