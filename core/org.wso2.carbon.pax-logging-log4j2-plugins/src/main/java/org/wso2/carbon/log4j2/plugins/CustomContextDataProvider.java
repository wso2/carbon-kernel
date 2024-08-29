/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.log4j2.plugins;

import org.apache.logging.log4j.core.impl.ThreadContextDataProvider;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;

import java.util.HashMap;
import java.util.Map;

public class CustomContextDataProvider extends ThreadContextDataProvider {
    String tenantDomain;
    String tenantId;

    @Override
    public Map<String, String> supplyContextData() {
        Map<String, String> contextData = new HashMap<>(super.supplyContextData());
        tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        tenantId = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantId());

        // can not use `tenantDomain` as it will be overwritten
        contextData.put(MultitenantConstants.CONTEXT_DATA_TENANT_DOMAIN_FOR_LOGS, tenantDomain);
        // can not use `tenantId` as it will be overwritten
        contextData.put(MultitenantConstants.CONTEXT_DATA_TENANT_ID_FOR_LOGS, tenantId);
        return contextData;
    }

    @Override
    public StringMap supplyStringMap() {
        StringMap map = new SortedArrayStringMap();
        StringMap originalMap = super.supplyStringMap();
        map.putAll(originalMap);

        tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        tenantId = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantId());

        map.putValue(MultitenantConstants.CONTEXT_DATA_TENANT_DOMAIN_FOR_LOGS, tenantDomain);
        map.putValue(MultitenantConstants.CONTEXT_DATA_TENANT_ID_FOR_LOGS, tenantId);
        return map;
    }
}
