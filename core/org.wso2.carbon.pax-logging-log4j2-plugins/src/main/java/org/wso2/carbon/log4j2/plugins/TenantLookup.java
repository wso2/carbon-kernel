/*
 * Copyright 2020 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.log4j2.plugins;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.xml.StringUtils;

/**
 * This class filters log messages based on tenant.
 */
@Plugin(name = "TenantLookup", category = StrLookup.CATEGORY)
public class TenantLookup implements StrLookup {
    String tenantDomain;

    @Override
    public String lookup(String s) {
        tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        return tenantDomain;
    }

    @Override
    public String lookup(LogEvent logEvent, String s) {
        ReadOnlyStringMap contextData = logEvent.getContextData();
        if (contextData != null && contextData.size() != 0) {
            tenantDomain = contextData.getValue(MultitenantConstants.CONTEXT_DATA_TENANT_DOMAIN_FOR_LOGS);
        }
        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return tenantDomain;
    }
}
