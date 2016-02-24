/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.security.config.service;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.SecurityScenario;
import org.wso2.carbon.security.SecurityScenarioDatabase;
import org.wso2.carbon.security.config.SecurityConfigAdmin;
import org.wso2.carbon.user.core.UserRealm;

import java.util.Collection;

public class SecurityConfigAdminService extends AbstractAdmin {

    @Override
    protected UserRealm getUserRealm() {
        return (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
    }

    public void activateUsernameTokenAuthentication(String serviceName, String[] userGroups)
            throws SecurityConfigException {
        SecurityConfigAdmin admin = new SecurityConfigAdmin(getUserRealm(), getConfigSystemRegistry(), getAxisConfig());
        admin.activateUsernameTokenAuthentication(serviceName, userGroups);

    }

    public void disableSecurityOnService(String serviceName) throws SecurityConfigException {

        SecurityConfigAdmin admin = new SecurityConfigAdmin(getUserRealm(), getConfigSystemRegistry(), getAxisConfig());
        admin.disableSecurityOnService(serviceName);
    }

    public void applySecurity(String serviceName, String policyId, String policyPath, String[] trustedStores,
                              String privateStore, String[] userGroupNames) throws SecurityConfigException {
        SecurityConfigAdmin admin = new SecurityConfigAdmin(getUserRealm(), getConfigSystemRegistry(), getAxisConfig());
        admin.applySecurity(serviceName, policyId, policyPath, trustedStores, privateStore, userGroupNames);
    }

    /**
     * This method will apply Kerberos security policy to a given service.
     *
     * @param serviceName              Name of the service that security policy is applied.
     * @param policyId                 The scenario id.
     * @param servicePrincipalName     Service principal name.
     * @param servicePrincipalPassword Service principal password.
     * @throws org.wso2.carbon.security.SecurityConfigException If unable to add kerberos attributes.
     */
    public void applyKerberosSecurityPolicy(String serviceName, String policyId, String servicePrincipalName,
                                            String servicePrincipalPassword)
            throws SecurityConfigException {

        if (servicePrincipalName == null || StringUtils.equals("".trim(),servicePrincipalName)) {
            throw new SecurityConfigException("Please specify a valid service principal. " +
                    "Service principal should not be null");
        }

        if (servicePrincipalPassword == null || StringUtils.equals("".trim(),servicePrincipalPassword)) {
            throw new SecurityConfigException("Please specify a valid service principal password. " +
                    "Service principal password should not be null");
        }

        SecurityConfigAdmin admin = new SecurityConfigAdmin(getUserRealm(), getConfigSystemRegistry(), getAxisConfig());

        KerberosConfigData kerberosConfigurations = new KerberosConfigData();
        kerberosConfigurations.setServicePrincipleName(servicePrincipalName);
        kerberosConfigurations.setServicePrinciplePassword(servicePrincipalPassword);

        admin.applySecurity(serviceName, policyId, kerberosConfigurations);

    }


    public SecurityScenarioDataWrapper getScenarios(String serviceName) throws SecurityConfigException {
        Collection<SecurityScenario> scenarios = SecurityScenarioDatabase.getAllScenarios();
        SecurityScenarioData[] scenarioData = new SecurityScenarioData[scenarios.size()];
        int count = 0;
        for (SecurityScenario scenario : scenarios) {
            if (scenario.getGeneralPolicy()) {
                SecurityScenarioData data = new SecurityScenarioData();
                data.setCategory(scenario.getCategory());
                data.setCurrentScenario(scenario.getIsCurrentScenario());
                data.setDescription(scenario.getDescription());
                data.setScenarioId(scenario.getScenarioId());
                data.setSummary(scenario.getSummary());
                data.setType(scenario.getType());
                scenarioData[count++] = data;
            }
        }
        SecurityScenarioDataWrapper scenarioDataWrapper = new SecurityScenarioDataWrapper();
        scenarioDataWrapper.setScenarios(scenarioData);
        scenarioDataWrapper.setCurrentScenario(getCurrentScenario(serviceName));
        return scenarioDataWrapper;
    }

    private SecurityScenarioData getCurrentScenario(String serviceName)
            throws SecurityConfigException {
        SecurityConfigAdmin admin = new SecurityConfigAdmin(getUserRealm(), getConfigSystemRegistry(), getAxisConfig());
        admin.forceActualServiceDeployment(serviceName);
        return admin.getCurrentScenario(serviceName);
    }

    public SecurityScenarioData getSecurityScenario(String sceneId) throws SecurityConfigException {
        SecurityConfigAdmin admin = new SecurityConfigAdmin(getUserRealm(), getConfigSystemRegistry(), getAxisConfig());
        return admin.getSecurityScenario(sceneId);
    }

    public SecurityConfigData getSecurityConfigData(String serviceName, String scenarioId, String policyPath)
            throws SecurityConfigException {
        SecurityConfigAdmin admin = new SecurityConfigAdmin(getUserRealm(), getConfigSystemRegistry(), getAxisConfig());
        return admin.getSecurityConfigData(serviceName, scenarioId, policyPath);
    }
}
