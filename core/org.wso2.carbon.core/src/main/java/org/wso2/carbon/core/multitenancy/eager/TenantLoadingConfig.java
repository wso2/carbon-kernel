/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.core.multitenancy.eager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Bean class to hold configurations of EagerLoading tenants
 */
public class TenantLoadingConfig {

    private static final Log logger = LogFactory.getLog(TenantLoadingConfig.class);
    public static final String TENANT_IDLE_TIME = "tenant.idle.time";
    private LinkedHashSet<String> includeTenantList = new LinkedHashSet<String>();
    private LinkedHashSet<String> excludeTenantList = new LinkedHashSet<String>();
    private boolean includeAllTenants;
    //Tenant element in carbon.xml is optional, so that if it is not specified,
    // we fallback on the tenant lazy loading mechanism.
    private boolean isOptional;

    public void init() {
        ServerConfigurationService serverConfigurationService =
                CarbonCoreDataHolder.getInstance().getServerConfigurationService();
        String eagerLoadingString =
                serverConfigurationService.getFirstProperty("Tenant.LoadingPolicy.EagerLoading.Include");

        // If user defined eager loading configurations we'll configure tenant Eager Loading for those domains
        if (eagerLoadingString != null) {
            // The eagerLoadingString could be something like; *,!wso2.com,!test.com
            // Todo
            if (eagerLoadingString == null || eagerLoadingString.trim().isEmpty()) {
                isOptional = true;
                logger.info("No domains defined under EagerLoading config - Switching to default Tenant lazy loading mechanism ...");
                return;
            }
            isOptional = false;
            logger.info("Using tenant eager loading policy...");
            String[] tenants = eagerLoadingString.split(",");
            for (String tenant : tenants) {
                tenant = tenant.trim();
                if (tenant.equals("*")) {
                    includeAllTenants = true;
                } else if (tenant.contains("!")) {
                    if (tenant.contains("*")) {
                        throw new IllegalArgumentException(tenant + " is not a valid tenant domain");
                    }
                    excludeTenantList.add(tenant.replace("!", ""));
                } else {
                    includeTenantList.add(tenant);
                }
            }
        }
    }

    public boolean isEagerLoadingEnabled() {
        // If the excludeTenantList or includeTenantList is not empty, it means that we are using the eager loading
        // policy
        return includeAllTenants || !excludeTenantList.isEmpty() || !includeTenantList.isEmpty();
    }

    public List<String> getExcludeTenantList() {
        return new ArrayList<>(excludeTenantList);
    }

    public List<String> getIncludeTenantList() {
        return new ArrayList<>(includeTenantList);
    }

    public boolean includeAllTenants() {
        return includeAllTenants;
    }

    public boolean isOptional() {
        return isOptional;
    }

}
