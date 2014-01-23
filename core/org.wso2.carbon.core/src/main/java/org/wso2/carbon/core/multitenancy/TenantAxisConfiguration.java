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
package org.wso2.carbon.core.multitenancy;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * The tenant specific AxisConfiguration
 */
public class TenantAxisConfiguration extends AxisConfiguration {
    private static final String REPOSITORY = "carbon.repository";
    private Map<String, Boolean> restrictedItems = new HashMap<String, Boolean>();

    private TenantAxisConfiguration() {
    }

    public static TenantAxisConfiguration createInstance() {
        // We need to add the CarbonContext to the TenantAxisConfiguration before setting up the
        // restrictions. This is to prevent a recursive call in the #checkRestrictedItem(String)
        // method. Therefore, we have introduced this method, so that necessary initialization can
        // be done.
        TenantAxisConfiguration tenantAxisConfiguration = new TenantAxisConfiguration();
        // Then initialize it.
        tenantAxisConfiguration.init();
        return tenantAxisConfiguration;
    }

    private void init() {
        // The CarbonContextHolder parameter is restricted. We have hardcoded the string here, since
        // we don't want to make this publicly available. See
        // CarbonContextHolder.CARBON_CONTEXT_HOLDER for more information.
        restrictedItems.put("carbonContextHolder", true);
        restrictedItems.put(REPOSITORY, false);
    }

    @Override
    public void setRepository(URL axis2Repository) {
        checkRestrictedItem(REPOSITORY);
        super.setRepository(axis2Repository);
    }

    @Override
    public void addParameter(Parameter param) throws AxisFault {
        checkRestrictedItem(param.getName());
        super.addParameter(param);
    }

    @Override
    public void addParameter(String paramName, Object value) throws AxisFault {
        checkRestrictedItem(paramName);
        super.addParameter(paramName, value);
    }

    @Override
    public void removeParameter(Parameter param) throws AxisFault {
        checkRestrictedItem(param.getName());
        super.removeParameter(param);
    }

    /**
     * Restrict setting of critical items so that they can be set only once.
     *
     * @param itemName The item that needs to be checked
     */
    private void checkRestrictedItem(String itemName) {
        if (restrictedItems.containsKey(itemName)) {
            Boolean isSet = restrictedItems.get(itemName);
            if (isSet) {
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                throw new SecurityException("Malicious code detected! Trying to override " +
                        "restricted item: " + itemName + ". An incident has been logged for " +
                        "tenant " + tenantDomain + "[" + tenantId + "]");
            } else {
                restrictedItems.put(itemName, true);
            }
        }
    }
}
