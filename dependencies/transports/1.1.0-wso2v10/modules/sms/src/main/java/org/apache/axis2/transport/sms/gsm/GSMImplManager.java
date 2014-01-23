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

import org.apache.axis2.transport.sms.SMSImplManager;
import org.apache.axis2.transport.sms.SMSMessage;
import org.apache.axis2.transport.sms.SMSTransportConstents;
import org.apache.axis2.transport.sms.SMSManager;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smslib.*;
import org.smslib.modem.SerialModemGateway;

/**
 * Manage the GSM implimentation of the SMS Transport
 * To use this with the axis2 to it must be spcified as a implimentation class in the Axis2 XML
 * following is a Sample configuration
 * <transportReceiver name="sms"
 *                      class="org.apache.axis2.transport.sms.SMSMessageReciever">
 *
 *       	<parameter name="smsImplClass">org.apache.axis2.transport.sms.gsm.GSMImplManager</parameter>
 *		<parameter name="com_port">/dev/ttyUSB0</parameter>
 *		<parameter name="gateway_id">modem.ttyUSB0</parameter>
 *		<parameter name="baud_rate">115200</parameter>
 *		<parameter name="manufacturer">HUAWEI</parameter>
 *		<parameter name="model">E220</parameter>
 *	</transportReceiver>
 *
 *
 */
public class GSMImplManager implements SMSImplManager {
    /** the reference to the actual commons logger to be used for log messages */
    protected Log log = LogFactory.getLog(this.getClass());
    private GSMTransportInDetails gsmTransportInDetails = GSMTransportInDetails.getInstance();
    private GSMTransportOutDetails gsmTransportOutDetails = GSMTransportOutDetails.getInstance();
    private GSMDispatcher dispatcher;
    private Service service = null;
    private SerialModemGateway gateway;
    private GSMServiceRepository serviceRepo = GSMServiceRepository.getInstence();
    private SMSManager smsInManeger;
    public void start() {

        if(serviceRepo.gatewayInUse(gsmTransportInDetails.getGatewayId())) {
            service = serviceRepo.getService(gsmTransportInDetails.getGatewayId());
            return;
        }
        service = new Service();

        gateway= new SerialModemGateway(gsmTransportInDetails.getGatewayId(), gsmTransportInDetails.getComPort(),
                gsmTransportInDetails.getBaudRate(),gsmTransportInDetails.getManufacturer(),
                gsmTransportInDetails.getModel());

			// Set the modem protocol to PDU (alternative is TEXT). PDU is the default, anyway...
			gateway.setProtocol(AGateway.Protocols.PDU);

			// Do we want the Gateway to be used for Inbound messages?
			gateway.setInbound(true);

			// Do we want the Gateway to be used for Outbound messages?
			gateway.setOutbound(true);

			// Let SMSLib know which is the SIM PIN.
			gateway.setSimPin("0000");



        try {
            // Add the Gateway to the Service object.
            this.service.addGateway(gateway);

            // Start! (i.e. connect to all defined Gateways)
            this.service.startService();
            serviceRepo.addService(gsmTransportInDetails.getGatewayId(), service);
            dispatcher = new GSMDispatcher(service , smsInManeger);
            dispatcher.setPollInterval(gsmTransportInDetails.getModemPollInterval());
            Thread thread = new Thread(dispatcher);
            thread.start();
            System.out.println("[Axis2] Started in Port :" + gsmTransportInDetails.getComPort() +" ");
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void stop() {

        try {
            dispatcher.stopPolling();
            service.stopService();
            if(serviceRepo.gatewayInUse(gsmTransportInDetails.getGatewayId())) {
                serviceRepo.removeService(gsmTransportInDetails.getGatewayId());
            }

            if(serviceRepo.gatewayInUse(gsmTransportOutDetails.getGatewayId())) {
                serviceRepo.removeService(gsmTransportOutDetails.getGatewayId());
            }

        } catch (Exception e) {
            log.error(e);
        }
    }

    public void setTransportOutDetails(TransportOutDescription transportOutDetails) throws AxisFault {

         if (transportOutDetails.getParameter(SMSTransportConstents.MODEM_GATEWAY_ID) != null) {
            gsmTransportOutDetails.setGatewayId((String) transportOutDetails.getParameter(
                    SMSTransportConstents.MODEM_GATEWAY_ID).getValue());
        } else {
            throw new AxisFault("GATEWAY ID NOT SET for the SMS transprot");
        }

        if (transportOutDetails.getParameter(SMSTransportConstents.COM_PORT) != null) {
            gsmTransportOutDetails.setComPort((String) transportOutDetails.getParameter(
                    SMSTransportConstents.COM_PORT).getValue());
        } else {
            throw new AxisFault("COM PORT NOT SET for the SMS transprot");
        }
        if (transportOutDetails.getParameter(SMSTransportConstents.BAUD_RATE) != null) {
            int bRate = Integer.parseInt((String) transportOutDetails.getParameter(
                    SMSTransportConstents.BAUD_RATE).getValue());
            gsmTransportOutDetails.setBaudRate(bRate);
        } else {
            throw new AxisFault("BAUD RATE NOT SET for the SMS transprot");
        }
        if (transportOutDetails.getParameter(SMSTransportConstents.MANUFACTURER) != null) {
            gsmTransportOutDetails.setManufacturer((String) transportOutDetails.getParameter(
                    SMSTransportConstents.MANUFACTURER).getValue());
        } else {
            throw new AxisFault("Manufactuer NOT SET for the SMS transprot");
        }

        if (transportOutDetails.getParameter(SMSTransportConstents.MODEL) != null) {
            gsmTransportOutDetails.setModel((String) transportOutDetails.getParameter(
                    SMSTransportConstents.MODEL).getValue());
        } else {
            throw new AxisFault("Model NOT SET for the SMS transprot");
        }


    }

    public void setTransportInDetails(TransportInDescription transportInDetails) throws AxisFault {
        if (transportInDetails.getParameter(SMSTransportConstents.MODEM_GATEWAY_ID) != null) {
            gsmTransportInDetails.setGatewayId((String) transportInDetails.getParameter(
                    SMSTransportConstents.MODEM_GATEWAY_ID).getValue());
        } else {
            throw new AxisFault("GATEWAY ID NOT SET for the SMS transprot");
        }

        if (transportInDetails.getParameter(SMSTransportConstents.COM_PORT) != null) {
            gsmTransportInDetails.setComPort((String) transportInDetails.getParameter(
                    SMSTransportConstents.COM_PORT).getValue());
        } else {
            throw new AxisFault("COM PORT NOT SET for the SMS transprot");
        }
        if (transportInDetails.getParameter(SMSTransportConstents.BAUD_RATE) != null) {
            int bRate = Integer.parseInt((String) transportInDetails.getParameter(
                    SMSTransportConstents.BAUD_RATE).getValue());
            gsmTransportInDetails.setBaudRate(bRate);
        } else {
            throw new AxisFault("BAUD RATE NOT SET for the SMS transprot");
        }
        if (transportInDetails.getParameter(SMSTransportConstents.MANUFACTURER) != null) {
            gsmTransportInDetails.setManufacturer((String) transportInDetails.getParameter(
                    SMSTransportConstents.MANUFACTURER).getValue());
        } else {
            throw new AxisFault("Manufactuer NOT SET for the SMS transprot");
        }

        if (transportInDetails.getParameter(SMSTransportConstents.MODEL) != null) {
            gsmTransportInDetails.setModel((String) transportInDetails.getParameter(
                    SMSTransportConstents.MODEL).getValue());
        } else {
            throw new AxisFault("Model NOT SET for the SMS transprot");
        }

        if (transportInDetails.getParameter(SMSTransportConstents.MODEM_POLL_INTERVAL) != null) {
            String pollTime =  (String) transportInDetails.getParameter(SMSTransportConstents.MODEM_POLL_INTERVAL).
                    getValue();
            gsmTransportInDetails.setModemPollInterval(Long.parseLong(pollTime));
        }
    }

    public void sendSMS(SMSMessage sm) {
        if (service == null && !serviceRepo.gatewayInUse(gsmTransportOutDetails.getGatewayId())) {
            //Operating in the Out Only mode
            service = new Service();
            gateway = new SerialModemGateway(gsmTransportOutDetails.getGatewayId(), gsmTransportOutDetails.getComPort(),
                    gsmTransportOutDetails.getBaudRate(), gsmTransportOutDetails.getManufacturer(),
                    gsmTransportOutDetails.getModel());

            // Set the modem protocol to PDU (alternative is TEXT). PDU is the default, anyway...
            gateway.setProtocol(AGateway.Protocols.PDU);


            // Do we want the Gateway to be used for Outbound messages?
            gateway.setOutbound(true);

            // Let SMSLib know which is the SIM PIN.
            gateway.setSimPin("0000");
            try {
                // Add the Gateway to the Service object.
                this.service.addGateway(gateway);

                // Similarly, you may define as many Gateway objects, representing
                // various GSM modems, add them in the Service object and control all of them.

                // Start! (i.e. connect to all defined Gateways)
                this.service.startService();

            } catch (Exception e) {
                log.error(e);
            }

        } else if(serviceRepo.gatewayInUse(gsmTransportOutDetails.getGatewayId())) {
            service = serviceRepo.getService(gsmTransportOutDetails.getGatewayId());    
        }

        OutboundMessage msg =  new OutboundMessage(sm.getReceiver(), sm.getContent());
        try {
            // a blocking call.This will be blocked untill the message is sent.
            // normal rate is about 6msgs per minute
            service.sendMessage(msg);
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void setSMSInManager(SMSManager manager) {
        this.smsInManeger = manager;
    }

    public SMSManager getSMSInManager() {
        return smsInManeger;
    }

}
