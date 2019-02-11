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

package org.wso2.carbon.core.services.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import javax.naming.AuthenticationException;

/**
 * This class contains helper methods that can be used within authenticators.
 */
public class AuthenticatorHelper {

    private static final Log log = LogFactory.getLog(AbstractAuthenticator.class);

    public static UserRealm getUserRealm(int tenantId, RealmService realmService, RegistryService registryService)
            throws AuthenticationException{
        if (realmService == null || registryService == null) {
            return null;
        }

        try {
            String tenantDomain = realmService.getTenantManager().getDomain(tenantId);
            return AnonymousSessionUtil.getRealmByTenantDomain(registryService,
                    realmService, tenantDomain);
        } catch (UserStoreException e) {
            String msg = "Unable to retrieve tenant domain for tenant id " + tenantId;
            log.error(msg, e);
        } catch (CarbonException e) {
            log.error("Unable to retrieve realm for tenant id " + tenantId,e);
        }

        return null;
    }
}
