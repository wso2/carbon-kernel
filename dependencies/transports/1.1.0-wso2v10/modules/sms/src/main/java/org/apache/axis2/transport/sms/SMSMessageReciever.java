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
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SMSMessageReciever implements TransportListener {

    private SMSManager smsManeger;
     /** the reference to the actual commons logger to be used for log messages */
    protected Log log = LogFactory.getLog(this.getClass());
    public void init(ConfigurationContext configurationContext, TransportInDescription transportInDescription) throws AxisFault {

        smsManeger = new SMSManager();
        smsManeger.init(transportInDescription , configurationContext);

    }

    public void start() throws AxisFault {
        if(smsManeger.isInited())
        {
            smsManeger.start();
        }

    }

    public void stop() throws AxisFault {

        if(smsManeger.isInited()) {
            smsManeger.stop();
        }
    }

    public EndpointReference getEPRForService(String s, String s1) throws AxisFault {

        return null;
    }

    public EndpointReference[] getEPRsForService(String s, String s1) throws AxisFault {
         if (smsManeger.getPhoneNumber() != null) {
                // need to change this after sms transport have a proper standered epr
                return new EndpointReference[]{
                        new EndpointReference("sms://"+smsManeger.getPhoneNumber()+"/")};

         } else {
            log.debug("Unable to generate EPR for the transport sms");   
         }
        return null;

    }

    public SessionContext getSessionContext(MessageContext messageContext) {
        return null;
    }

    public void destroy() {

    }
}
