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

/**
 * Keeps the constets that are need for SMS Transport.Currently The code of the Class is divided in to three sections
 * for the keep the clarity
 * 1) SMS Transport Constents : add the constents to this section if you need to add new constents to the gneric
 *    SMSTransport level.
 * 2) SMPP Constents : SMPP is one implimentation of SMSTransport.Add Constents to this section if you need to add
 *    constents related to SMPP section
 * 3) GSM Constets : This a another implimentation. Add Constents to this section if you need to add constents related
 *    to GSM section
 *
 * if you are going to add a another SMS implimentation add a another section to this class eg:"XXX Transport Constents"
 */
public class SMSTransportConstents {
    /**
     * SMS Transport Constents
     */
    public static String IMPLIMENTAION_CLASS = "smsImplClass";
    public static String BUILDER_CLASS = "builderClass";
    public static String FORMATTER_CLASS ="formatterClass";
    public static String SEND_TO="sms_sender";
    public static String DESTINATION = "sms_destination";
    /**
     * if this paprameter is set true in the Transport sender configuration.
     * sender will use message source specific parameters as destination parameters when sending the message
     * the default value is true.
     *
     * eg: in a SMPP Transport message
     * SOURCE_ADDRESS_TON will be used as the DESTINATION_ADDRESS_TON is this parameter is not set to false.
     */
    public static String INVERT_SOURCE_AND_DESTINATION = "invert_source_and_destination";
    public static String PHONE_NUMBER = "phoneNumber";

    /**
     * SMPP constents
     */
    public static String SYSTEM_TYPE = "systemType";
    public static String SYSTEM_ID = "systemId";
    public static String PASSWORD = "password";
    public static String HOST = "host";
    public static String PORT = "port";

    /**
     * GSM Constents
     */
    
    public static String MODEM_GATEWAY_ID = "gateway_id";
    public static String COM_PORT = "com_port";
    public static String BAUD_RATE = "baud_rate";
    public static String MANUFACTURER = "manufacturer";
    public static String MODEL = "model";
    public static String MODEM_POLL_INTERVAL="modem_poll_interval";

}
