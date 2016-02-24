/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security.deployment;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.security.SecurityServiceHolder;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.util.Map;

public class SecurityDeploymentListener extends AbstractAxis2ConfigurationContextObserver {

    private static Log log = LogFactory.getLog(SecurityDeploymentListener.class);

    @Override
    public void createdConfigurationContext(ConfigurationContext configCtx) {
        AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        //Register SecurityDeploymentInterceptor as an AxisObserver in tenant's AxisConfig.

        SecurityDeploymentInterceptor secDeployInterceptor = new SecurityDeploymentInterceptor();
        secDeployInterceptor.init(axisConfig);
        axisConfig.addObservers(secDeployInterceptor);

        //Store the policy resources in newly created tenant's config. registry
        Map<String, Resource> policyResourceMap = SecurityServiceHolder.getPolicyResourceMap();
        try {
            Registry registry = SecurityServiceHolder.getRegistryService().getConfigSystemRegistry(
                    tenantId);
            boolean transactionStarted = Transaction.isStarted();
            if (!transactionStarted) {
                registry.beginTransaction();
            }
            for (String resourceLoc : policyResourceMap.keySet()) {
                if (!registry.resourceExists(resourceLoc)) {
                    registry.put(resourceLoc, policyResourceMap.get(resourceLoc));
                }
            }
            if (!transactionStarted) {
                registry.commitTransaction();
            }
        } catch (Exception e) {
            String errorMsg = "Error when storing the policy resource in registry for tenant : " +
                    tenantId;
            log.error(errorMsg, e);
        }
    }

}
