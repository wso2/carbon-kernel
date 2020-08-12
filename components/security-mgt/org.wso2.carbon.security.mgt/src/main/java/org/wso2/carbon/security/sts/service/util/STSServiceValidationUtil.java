/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.security.sts.service.util;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.security.sts.service.STSAdminServiceInterface;

/**
 * Util class used to validate if the STS services are deployed.
 * Used by Management Console UI to hide fields on service unavailability.
 */
public class STSServiceValidationUtil {

    /**
     * Check if the WS-Trust service is deployed.
     *
     * @return True if the service is deployed false if else.
     */
    public static boolean isWSTrustAvailable() {

        try {
            STSAdminServiceInterface stsAdminService =
                    (STSAdminServiceInterface) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .getOSGiService(STSAdminServiceInterface.class, null);
            return stsAdminService != null;
        } catch (NullPointerException exception) {
            return false;
        }
    }
}
