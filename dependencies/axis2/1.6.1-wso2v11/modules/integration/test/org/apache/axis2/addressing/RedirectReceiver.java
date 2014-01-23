/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.addressing;

import org.apache.axiom.om.OMElement;

public class RedirectReceiver {

    private static boolean responseRecieved = false;

    public static boolean hasReceivedResponse() {
        return responseRecieved;
    }

    private static boolean faultRecieved = false;

    public static boolean hasReceivedFault() {
        return faultRecieved;
    }

    public void echoOMElementResponse(OMElement omEle) {
        System.out.println("echoOMElementResponse: " + omEle);
        responseRecieved = true;
    }

    public void fault(OMElement omEle) {
        System.out.println("fault: " + omEle);
        faultRecieved = true;
    }
}
