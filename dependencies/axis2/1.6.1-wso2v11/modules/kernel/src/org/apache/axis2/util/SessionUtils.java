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

package org.apache.axis2.util;

import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;

import java.util.Iterator;

public class SessionUtils {

    /**
     * Walk through the list of services and use the maximum of the scopes as the scope
     * for the whole service group
     *
     * @param axisServiceGroup the service group
     * @return scope for the service group
     */
    public static String calculateMaxScopeForServiceGroup(AxisServiceGroup axisServiceGroup) {
        Iterator<AxisService> servics = axisServiceGroup.getServices();
        int maxScope = 1;
        while (servics.hasNext()) {
            AxisService axisService = (AxisService) servics.next();
            int scopeIntValue = getScopeIntValue(axisService.getScope());
            if (maxScope < scopeIntValue) {
                maxScope = scopeIntValue;
            }
        }
        return getScopeString(maxScope);
    }

    /**
     * convert scope into a numerical value
     *
     * @param scope scope as a string
     * @return integer the scope as a number
     */
    private static int getScopeIntValue(String scope) {
        if (Constants.SCOPE_REQUEST.equals(scope)) {
            return 1;
        } else if (Constants.SCOPE_TRANSPORT_SESSION.equals(scope)) {
            return 2;
        } else if (Constants.SCOPE_SOAP_SESSION.equals(scope)) {
            return 3;
        } else if (Constants.SCOPE_APPLICATION.equals(scope)) {
            return 4;
        } else {
            return 2;
        }
    }

    /**
     * Get the actual scope string given the numerical value
     *
     * @param scope scope as a number
     * @return string scope as a string
     */
    private static String getScopeString(int scope) {
        switch (scope) {
            case 1 : {
                return Constants.SCOPE_REQUEST;
            }
            case 2 : {
                return Constants.SCOPE_TRANSPORT_SESSION;
            }
            case 3 : {
                return Constants.SCOPE_SOAP_SESSION;
            }
            case 4 : {
                return Constants.SCOPE_APPLICATION;
            }
            default : {
                return Constants.SCOPE_TRANSPORT_SESSION;
            }
        }
    }
}
