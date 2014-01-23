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
import org.apache.axis2.AxisFault;

import java.util.ArrayList;

/**
 * Interface for Specefic SMS implmentations
 * SMS Manager will manage these implmentations
 */
public interface SMSImplManager {
    /**
     * start the Underlying SMS implmentation
     */
    public void start();

    /**
     * stop the Underlying SMS implementation
     */
    public void stop();

    /**
     * set the Transport out details that is needed for the implementation manager
     * @param transportOutDetails
     * @throws AxisFault
     */
    public void setTransportOutDetails(TransportOutDescription transportOutDetails) throws AxisFault;

    /**
     * set the Transport in details that is needed for the implementation manager
     * @param transportInDetails
     * @throws AxisFault
     */
    public void setTransportInDetails(TransportInDescription transportInDetails) throws AxisFault;

    /**
     * send the SMS  that is passed as a parameter using the current implimentation (To know the Content of the SMSMessage Look in to the documantaion of
     * the SMSMessage )
     * @param sm  SMSMessage to be send
     */
    public void sendSMS(SMSMessage sm);

    /**
     * set the SMS manager that carries out SMS In task to the SMSImplimentaion
     * @param manager
     */
    public void setSMSInManager(SMSManager manager);

    /**
     * get the refferance to the SMSMeneger instance that the implimentaion has
     * @return
     */
    public SMSManager getSMSInManager();
}
