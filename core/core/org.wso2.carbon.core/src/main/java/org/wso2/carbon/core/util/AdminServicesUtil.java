/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This is the class should be used by Admin service authors to get the Registry
 * and Realms.
 */
public class AdminServicesUtil {

    private static Log log = LogFactory.getLog(AdminServicesUtil.class);

    public static boolean isSuperTenant() throws CarbonException {
        return CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID;
    }

    /**
     * @deprecated
     */
    public static UserRegistry getSystemRegistry() throws CarbonException {
        return (UserRegistry) CarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.SYSTEM_CONFIGURATION);
    }

    /**
     * @deprecated
     */
    public static UserRegistry getUserRegistry() throws CarbonException {
        return (UserRegistry) CarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.USER_CONFIGURATION);
    }

    public static UserRealm getUserRealm() throws CarbonException {
        return (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
    }
    


}
