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

import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.sms.smpp.SMPPImplManager;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.engine.AxisEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * SMS manager will manage all SMS implementation managers
 * and it will dispatch the Message to the Axis2 Engine
 */
public class SMSManager {

    private SMSImplManager currentImplimentation;
    private boolean inited;
    private ConfigurationContext configurationContext;
    private SMSMessageBuilder messageBuilder;
    private SMSMessageFormatter messageFormatter;
    private String phoneNumber = null;
    private boolean invertSourceAndDestination = true;

     /** the reference to the actual commons logger to be used for log messages */
    protected Log log = LogFactory.getLog(this.getClass());

    

    /**
     * initialize the SMS manager with TransportinDiscription
     * if Manager is already inited it will only set the TransportInDiscription
     * in the current Implimentation manager
     * @param transportInDescription
     * @param configurationContext
     * @throws AxisFault
     */
    public void init(TransportInDescription transportInDescription ,ConfigurationContext  configurationContext) throws
            AxisFault {
        if (!inited) {
            basicInit(transportInDescription , configurationContext);
        }


        Parameter builderClass = transportInDescription.getParameter(SMSTransportConstents.BUILDER_CLASS);

        if(builderClass == null) {
            messageBuilder = new DefaultSMSMessageBuilderImpl();
        } else {
            try {
                messageBuilder = (SMSMessageBuilder)Class.forName((String)builderClass.getValue()).newInstance();

            } catch (Exception e) {
               throw new AxisFault("Error while instentiating class " + builderClass.getValue() , e );
            }
        }
        currentImplimentation.setTransportInDetails(transportInDescription);
           // get the Axis phone number form the configuration file
        Parameter phoneNum = transportInDescription.getParameter(SMSTransportConstents.PHONE_NUMBER);
        if(phoneNum != null) {
            this.phoneNumber = (String)phoneNum.getValue();
        }
        inited = true;
    }

    /**
     * Initialize the SMS Maneger with TransportOutDiscription
     * if the Maneger is already inited  it will set the Transport Outdetails
     * in the Current Implimentation Manage
     * @param transportOutDescription
     * @param configurationContext
     */
    public void init(TransportOutDescription transportOutDescription , ConfigurationContext configurationContext) throws
            AxisFault {
        if(!inited) {
            basicInit(transportOutDescription , configurationContext);
        }

        Parameter formatterClass = transportOutDescription.getParameter(SMSTransportConstents.FORMATTER_CLASS);

        if(formatterClass == null) {
            messageFormatter = new DefaultSMSMessageFormatterImpl();
        }else {
            try {
                messageFormatter = (SMSMessageFormatter)Class.forName((String)formatterClass.getValue()).newInstance();
            } catch (Exception e) {
                throw new AxisFault("Error while instentiating the Class: " +formatterClass.getValue() ,e);
            }
        }
        currentImplimentation.setTransportOutDetails(transportOutDescription);

        Parameter invertS_n_D = transportOutDescription.getParameter(
                SMSTransportConstents.INVERT_SOURCE_AND_DESTINATION);
        if(invertS_n_D != null) {
            String val = (String)invertS_n_D.getValue();
            if("false".equals(val)) {
                invertSourceAndDestination = false;
            } else if("true".equals(val)) {
                invertSourceAndDestination = true;
            } else {
                log.warn("Invalid parameter value set to the parameter invert_source_and_destination," +
                        "setting the default value :true ");
                invertSourceAndDestination = true;
            }
        }
        inited = true;
    }

    private void basicInit(ParameterInclude transportDescription, ConfigurationContext configurationContext) throws
            AxisFault {
        this.configurationContext = configurationContext;
        Parameter p = transportDescription.getParameter(SMSTransportConstents.IMPLIMENTAION_CLASS);

        if (p == null) {
            currentImplimentation = new SMPPImplManager();
        } else {
            String implClass = (String) p.getValue();

            try {

                currentImplimentation = (SMSImplManager) Class.forName(implClass).newInstance();
            } catch (Exception e) {
                throw new AxisFault("Error while instentiating class " + implClass, e);
            }
        }
        currentImplimentation.setSMSInManager(this);

    }
    /**
     * Dispatch the SMS message to Axis2 Engine
     * @param sms
     */
    public void dispatchToAxis2(SMSMessage sms)  {
        try {
            MessageContext msgctx = messageBuilder.buildMessaage(sms,configurationContext);
            msgctx.setReplyTo(new EndpointReference("sms://"+sms.getSender()+"/"));
            AxisEngine.receive(msgctx);
        } catch (InvalidMessageFormatException e) {
            log.debug("Invalid message format " + e);

        } catch (AxisFault axisFault) {
            log.debug(axisFault);
        } catch (Throwable e) {
            log.debug("Unknown Exception " , e);
        }

    }

    /**
     * send a SMS form the message comming form the Axis2 Engine
     * @param messageContext that is comming form the Axis2
     */
    public void sendSMS(MessageContext messageContext) {
        try {
            SMSMessage sms = messageFormatter.formatSMS(messageContext);
            sms.addProperty(SMSTransportConstents.INVERT_SOURCE_AND_DESTINATION ,"" + invertSourceAndDestination);
            currentImplimentation.sendSMS(sms);
        } catch (Exception e) {
            log.error("Error while sending the SMS " , e);
        }

    }

    /**
     * send the information SMS messages other than messages comming form the Axis2 Engine
     * @param sms
     */
    public void sentInfo(SMSMessage sms) {
        currentImplimentation.sendSMS(sms);
    }

    public SMSImplManager getCurrentImplimentation() {
        return currentImplimentation;
    }

    public void setCurrentImplimentation(SMSImplManager currentImplimentation) {
        this.currentImplimentation = currentImplimentation;
    }

    public void start() {
        currentImplimentation.start();
    }

    public void stop() {
        currentImplimentation.stop();
    }

    public boolean isInited() {
        return inited;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isInvertSourceAndDestination() {
        return invertSourceAndDestination;
    }

    public void setInvertSourceAndDestination(boolean invertSourceAndDestination) {
        this.invertSourceAndDestination = invertSourceAndDestination;
    }
}
