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

/**
 * holds the TransportIn details thats needed for the GSM implimentation
 */
public class GSMTransportInDetails {
    private static GSMTransportInDetails ourInstance;

    private String gatewayId;
    private String comPort;
    private int baudRate;
    private String manufacturer;
    private String model;
    private long modemPollInterval = 5000;

    public static GSMTransportInDetails getInstance() {
        if (ourInstance == null) {
            ourInstance = new GSMTransportInDetails();
        }
        return ourInstance;
    }

    private GSMTransportInDetails() {

    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getComPort() {
        return comPort;
    }

    public void setComPort(String comPort) {
        this.comPort = comPort;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public long getModemPollInterval() {
        return modemPollInterval;
    }

    public void setModemPollInterval(long modemPollInterval) {
        this.modemPollInterval = modemPollInterval;
    }
}
