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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.addressing.EndpointReference;

import java.util.ArrayList;

public class SMSTransportUtils {

    /**
     * Given the array list of parameters and a name of the parameter
     * it will return the Value of that Parameter
     *
     * @param list
     * @param name
     * @return Object that stores the value of that Parameter
     */
    public static Object getParameterValue(ArrayList<Parameter> list, String name) {
        if (name == null) {
            return null;
        }
        for (Parameter p : list) {
            if (name.equals(p.getName())) {
                return p.getValue();
            }
        }
        return null;
    }

    /**
     * given the EPR it will return the phone number that the EPR represents
     * this assumes a EPR of format
     * sms://<phone_number>/
     *
     * @param epr
     * @return
     */
    public static String getPhoneNumber(EndpointReference epr) throws Exception {

        String eprAddress = epr.getAddress();
        String protocal = eprAddress.substring(0, 3);
        if (protocal != null && protocal.equals("sms")) {
            String parts[] = eprAddress.split("/");
            String phoneNumber = parts[2];
            return phoneNumber;
        } else {
            throw new Exception("Invalid Epr for the SMS Transport : Epr must be of format sms://<phone_number>/");
        }
    }


}

