/*
 * Copyright (c) 2010, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.HashMap;
import java.util.Map;

public class SecurityServiceHolder {

    private static RealmService realmService;

    private static ConfigurationContextService ccService;

    private static Map<String, Resource> policyResourceMap = new HashMap<>();

    private SecurityServiceHolder() {

    }

    public static RealmService getRealmService() throws Exception {
        if (realmService == null) {
            throw new SecurityConfigException("The main user realm is null");
        }
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        SecurityServiceHolder.realmService = realmService;
    }

    public static ConfigurationContext getConfigurationContext() throws Exception {
        if (ccService == null) {
            throw new SecurityConfigException("CC service is null");
        }
        return ccService.getClientConfigContext();
    }

    public static void setConfigurationContextService(ConfigurationContextService ccService) {
        SecurityServiceHolder.ccService = ccService;
    }

    public static void addPolicyResource(String location, Resource resource) {
        policyResourceMap.put(location, resource);
    }
}
