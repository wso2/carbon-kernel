/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.core.multitenancy.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Utility methods for tenant operations
 *
 * @deprecated use {@link MultitenantUtils} and {@link TenantAxisUtils}
 */
public class TenantUtils {

    /**
     * @deprecated use
     * {@link MultitenantUtils#getTenantDomainFromUrl(String)}
     */
    public static String getTenantFromUrl(String url) {
        return MultitenantUtils.getTenantDomainFromUrl(url);
    }

    /**
     * Get tenant ID from config context
     *
     * @param configCtx The config context
     *
     * @return The tenant ID
     *
     * @deprecated use {@link MultitenantUtils#getTenantId(ConfigurationContext)}
     */
    public static int getTenantId(ConfigurationContext configCtx) {
        return MultitenantUtils.getTenantId(configCtx);
    }

    /**
     * @deprecated use
     * {@link TenantAxisUtils#getTenantAxisConfiguration(String, ConfigurationContext)}
     */
    public static AxisConfiguration getTenantAxisConfiguration(String tenant,
                                                               ConfigurationContext mainConfigCtx) {
        return TenantAxisUtils.getTenantAxisConfiguration(tenant, mainConfigCtx);
    }

    /**
     * @deprecated use
     * {@link TenantAxisUtils#getTenantConfigurationContextFromUrl(String, ConfigurationContext)}
     */
    public static ConfigurationContext
    getTenantConfigurationContextFromUrl(String url, ConfigurationContext mainConfigCtx) {
        return TenantAxisUtils.getTenantConfigurationContextFromUrl(url, mainConfigCtx);
    }

    /**
     * @deprecated use
     * {@link TenantAxisUtils#getTenantConfigurationContext(String, ConfigurationContext)}
     */
    public static ConfigurationContext
    getTenantConfigurationContext(String tenant, ConfigurationContext mainConfigCtx) {
        return TenantAxisUtils.getTenantConfigurationContext(tenant, mainConfigCtx);
    }

    /**
     * @param url               will have pattern <some-string>/t/<tenant>/<service>?<some-params>
     * @param mainConfigContext The main ConfigurationContext from the server
     *
     * @return The tenant's AxisService
     * @throws org.apache.axis2.AxisFault If an error occurs while retrieving the AxisService
     * @deprecated use
     * {@link TenantAxisUtils#getAxisService(String, ConfigurationContext)}
     */
    public static AxisService getAxisService(String url, ConfigurationContext mainConfigContext)
            throws AxisFault {
        return TenantAxisUtils.getAxisService(url, mainConfigContext);
    }

    /**
     * Set the transports for the tenants
     *
     * @param mainConfigCtx The main config context
     *
     * @throws AxisFault If an error occurs while initializing tenant transports
     * @deprecated use
     * {@link TenantAxisUtils#initializeTenantTransports(ConfigurationContext)}
     */
    @SuppressWarnings("unchecked")
    public static void initializeTenantTransports(ConfigurationContext mainConfigCtx)
            throws AxisFault {
        TenantAxisUtils.initializeTenantTransports(mainConfigCtx);
    }

    /**
     * @deprecated use
     * {@link TenantAxisUtils#setTenantTransports(AxisConfiguration, String, AxisConfiguration)}
     */
    public static void setTenantTransports(AxisConfiguration mainAxisConfig, String tenantDomain,
                                           AxisConfiguration tenantAxisConfig) throws AxisFault {
        TenantAxisUtils.setTenantTransports(mainAxisConfig, tenantDomain, tenantAxisConfig);
    }
}
