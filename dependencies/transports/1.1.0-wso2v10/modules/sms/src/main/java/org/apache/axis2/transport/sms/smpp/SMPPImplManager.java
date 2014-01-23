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

import org.apache.axis2.transport.sms.SMSImplManager;
import org.apache.axis2.transport.sms.SMSTransportConstents;
import org.apache.axis2.transport.sms.SMSMessage;
import org.apache.axis2.transport.sms.SMSManager;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.BindParameter;
import org.jsmpp.bean.*;
import org.jsmpp.util.TimeFormatter;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;

import java.io.IOException;
import java.util.Date;

public class SMPPImplManager implements SMSImplManager {

     /** the reference to the actual commons logger to be used for log messages */
    protected Log log = LogFactory.getLog(this.getClass());
    private SMPPTransportInDetails smppTransportInDetails = SMPPTransportInDetails.getInstence();
    private SMPPTransportOutDetails smppTransportOutDetails = SMPPTransportOutDetails.getInstence();

    private volatile boolean stop=true;
    private SMSManager smsInManeger;

    private SMPPSession inSession;
    private SMPPSession outSession;
    private static TimeFormatter timeFormatter = new AbsoluteTimeFormatter();


    /**
     * SMPP implementation Constents
     */
    public static String SOURCE_ADDRESS_TON = "source_address_ton";
    public static String SOURCE_ADDRESS_NPI = "source_address_npi";

    public static String DESTINATION_ADDRESS_TON = "destination_address_ton";
    public static String DESTINATION_ADDRESS_NPI = "destination_address_npi";

    

    public void start() {
        inSession = new SMPPSession();
        try {
            inSession.connectAndBind(smppTransportInDetails.getHost(), smppTransportInDetails.getPort(), new BindParameter(BindType.BIND_RX, smppTransportInDetails.getSystemId(),
                        smppTransportInDetails.getPassword(), smppTransportInDetails.getSystemType() , TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, null));

            SMPPListener listener = new SMPPListener(smsInManeger);
            inSession.setMessageReceiverListener(listener);
            stop = false;
            System.out.println(" [Axis2] bind and connect to " + smppTransportInDetails.getHost()+" : " +
                    smppTransportInDetails.getPort() + " on SMPP Transport");

        } catch (IOException e) {
            log.error("Unable to conncet" + e);
        }

    }

    public void stop() {
        log.info("Stopping SMPP Transport ...");
        stop = true;

        if(inSession != null) {
            inSession.unbindAndClose();
        }

        if(outSession !=null) {
            outSession.unbindAndClose();
        }
    }


    public void setTransportInDetails(TransportInDescription transportInDetails) throws AxisFault {
        if(transportInDetails == null) {
            throw new AxisFault("No transport in details");
        }

        if(transportInDetails.getParameter(SMSTransportConstents.SYSTEM_TYPE) != null){
            smppTransportInDetails.setSystemType((String)transportInDetails.getParameter(
                    SMSTransportConstents.SYSTEM_TYPE).getValue());
        }

        if (transportInDetails.getParameter(SMSTransportConstents.SYSTEM_ID) != null) {
            smppTransportInDetails.setSystemId((String)transportInDetails.getParameter(SMSTransportConstents.SYSTEM_ID).
                    getValue());
        } else {
            throw new AxisFault("System Id not set");
        }

        if (transportInDetails.getParameter(SMSTransportConstents.PASSWORD) != null) {
            smppTransportInDetails.setPassword((String)transportInDetails.getParameter(SMSTransportConstents.PASSWORD).
                    getValue());
        } else {
            throw new AxisFault("password not set");
        }

        if(transportInDetails.getParameter(SMSTransportConstents.HOST) != null) {
            smppTransportInDetails.setHost((String)transportInDetails.getParameter(SMSTransportConstents.HOST).
                    getValue());
        }

        if(transportInDetails.getParameter(SMSTransportConstents.PORT) != null) {
            smppTransportInDetails.setPort(Integer.parseInt((String)transportInDetails.getParameter(
                    SMSTransportConstents.PORT).getValue()));
        }

        if(transportInDetails.getParameter(SMSTransportConstents.PHONE_NUMBER) != null) {
            smppTransportInDetails.setPhoneNumber((String)transportInDetails.getParameter(
                    SMSTransportConstents.PHONE_NUMBER).getValue());
        }
    }

    public void setTransportOutDetails(TransportOutDescription transportOutDetails) throws AxisFault{
         if(transportOutDetails == null) {
            throw new AxisFault("No transport in details");
        }

        if(transportOutDetails.getParameter(SMSTransportConstents.SYSTEM_TYPE) != null){
            smppTransportOutDetails.setSystemType((String)transportOutDetails.getParameter(
                    SMSTransportConstents.SYSTEM_TYPE).getValue());
        }

        if (transportOutDetails.getParameter(SMSTransportConstents.SYSTEM_ID) != null) {
            smppTransportOutDetails.setSystemId((String)transportOutDetails.getParameter(SMSTransportConstents.SYSTEM_ID).
                    getValue());
        } else {
            throw new AxisFault("System Id not set");
        }

        if (transportOutDetails.getParameter(SMSTransportConstents.PASSWORD) != null) {
            smppTransportOutDetails.setPassword((String)transportOutDetails.getParameter(SMSTransportConstents.PASSWORD).
                    getValue());
        } else {
            throw new AxisFault("password not set");
        }

        if(transportOutDetails.getParameter(SMSTransportConstents.HOST) != null) {
            smppTransportOutDetails.setHost((String)transportOutDetails.getParameter(SMSTransportConstents.HOST).
                    getValue());
        }

        if(transportOutDetails.getParameter(SMSTransportConstents.PORT) != null) {
            smppTransportOutDetails.setPort(Integer.parseInt((String)transportOutDetails.getParameter(
                    SMSTransportConstents.PORT).getValue()));
        }

        if(transportOutDetails.getParameter(SMSTransportConstents.PHONE_NUMBER) != null) {
            smppTransportOutDetails.setPhoneNumber((String)transportOutDetails.getParameter(
                    SMSTransportConstents.PHONE_NUMBER).getValue());
        }
    }

    public void sendSMS(SMSMessage sm) {
        TypeOfNumber sourceTon =TypeOfNumber.UNKNOWN;
        NumberingPlanIndicator sourceNpi = NumberingPlanIndicator.UNKNOWN;

        TypeOfNumber destTon = TypeOfNumber.UNKNOWN;
        NumberingPlanIndicator destNpi = NumberingPlanIndicator.UNKNOWN;
        try {
            if (outSession == null) {
                outSession = new SMPPSession();
                outSession.setEnquireLinkTimer(smppTransportOutDetails.getEnquireLinkTimer());
                outSession.setTransactionTimer(smppTransportOutDetails.getTransactionTimer());
                outSession.connectAndBind(smppTransportOutDetails.getHost(), smppTransportOutDetails.getPort(),
                        new BindParameter(BindType.BIND_TX, smppTransportOutDetails.getSystemId(),
                                smppTransportOutDetails.getPassword(), smppTransportOutDetails.getSystemType(),
                                TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, null));

                log.debug("Conected and bind to " + smppTransportOutDetails.getHost());
            }

            boolean invert = true;

            if("false".equals(sm.getProperties().get(SMSTransportConstents.INVERT_SOURCE_AND_DESTINATION))){
                invert = false;
            }

            if(invert) {
                if(sm.getProperties().get(DESTINATION_ADDRESS_NPI) != null) {
                    sourceNpi = NumberingPlanIndicator.valueOf((String)sm.getProperties().get(DESTINATION_ADDRESS_NPI));
                }
                      
                if(sm.getProperties().get(DESTINATION_ADDRESS_TON) != null) {
                    sourceTon = TypeOfNumber.valueOf((String)sm.getProperties().get(DESTINATION_ADDRESS_TON));
                }
                if(sm.getProperties().get(SOURCE_ADDRESS_NPI) != null) {
                    destNpi = NumberingPlanIndicator.valueOf((String)sm.getProperties().get(SOURCE_ADDRESS_NPI));
                }
                if(sm.getProperties().get(SOURCE_ADDRESS_TON) != null) {
                    destTon = TypeOfNumber.valueOf((String)sm.getProperties().get(SOURCE_ADDRESS_TON));
                }


            } else {

                if(sm.getProperties().get(DESTINATION_ADDRESS_NPI) != null) {
                    destNpi = NumberingPlanIndicator.valueOf((String)sm.getProperties().get(DESTINATION_ADDRESS_NPI));
                }

                if(sm.getProperties().get(DESTINATION_ADDRESS_TON) != null) {
                    destTon = TypeOfNumber.valueOf((String)sm.getProperties().get(DESTINATION_ADDRESS_TON));
                }

                if(sm.getProperties().get(SOURCE_ADDRESS_NPI) != null) {
                    sourceNpi = NumberingPlanIndicator.valueOf((String)sm.getProperties().get(SOURCE_ADDRESS_NPI));
                }

                if(sm.getProperties().get(SOURCE_ADDRESS_TON) != null) {
                    sourceTon = TypeOfNumber.valueOf((String)sm.getProperties().get(SOURCE_ADDRESS_TON));
                }

            }



            String messageId = outSession.submitShortMessage(
                    "CMT",
                    sourceTon,
                    sourceNpi,
                    sm.getSender(),
                    destTon,
                    destNpi,
                    sm.getReceiver(),
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    timeFormatter.format(new Date()),
                    null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0,
                    new GeneralDataCoding(false, false, MessageClass.CLASS1,Alphabet.ALPHA_DEFAULT),
                    (byte) 0,
                    sm.getContent().getBytes()
            );

            log.debug("Message Submited with id" + messageId);
        } catch (IOException e) {
            log.error("Unable to Connect ", e);
        } catch (InvalidResponseException e) {
            log.debug("Invalid responce Exception", e);
        } catch (PDUException e) {
            log.debug("PDU Exception", e);
        } catch (NegativeResponseException e) {
            log.debug(e);
        } catch (ResponseTimeoutException e) {
            log.debug(e);
        }

    }

    public void setSMSInManager(SMSManager manager) {
        this.smsInManeger = manager;    
    }

    public SMSManager getSMSInManager() {
        return smsInManeger;
    }
}
