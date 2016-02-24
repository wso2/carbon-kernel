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
package org.wso2.carbon.security.ui.client;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.security.mgt.stub.config.ActivateUsernameTokenAuthentication;
import org.wso2.carbon.security.mgt.stub.config.ApplyKerberosSecurityPolicy;
import org.wso2.carbon.security.mgt.stub.config.ApplySecurity;
import org.wso2.carbon.security.mgt.stub.config.DisableSecurityOnService;
import org.wso2.carbon.security.mgt.stub.config.GetScenarios;
import org.wso2.carbon.security.mgt.stub.config.GetScenariosResponse;
import org.wso2.carbon.security.mgt.stub.config.GetSecurityConfigData;
import org.wso2.carbon.security.mgt.stub.config.GetSecurityConfigDataResponse;
import org.wso2.carbon.security.mgt.stub.config.GetSecurityScenario;
import org.wso2.carbon.security.mgt.stub.config.GetSecurityScenarioResponse;
import org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceStub;
import org.wso2.carbon.security.mgt.stub.config.xsd.SecurityConfigData;
import org.wso2.carbon.security.mgt.stub.config.xsd.SecurityScenarioData;
import org.wso2.carbon.security.mgt.stub.config.xsd.SecurityScenarioDataWrapper;

public class SecurityAdminClient {

    private static Log log = LogFactory.getLog(SecurityAdminClient.class);
    private SecurityAdminServiceStub stub = null;

    public SecurityAdminClient(String cookie, String url, ConfigurationContext configContext)
            throws java.lang.Exception {
        try {
            String serviceEndPoint = url + "SecurityAdminService";
            this.stub = new SecurityAdminServiceStub(configContext, serviceEndPoint);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (java.lang.Exception e) {
            log.error("Error in creating SecurityAdminClient", e);
            throw e;
        }
    }

    public void activateUsernameTokenAuthentication(String serviceName, String[] userGroups)
            throws java.lang.Exception {
        try {
            ActivateUsernameTokenAuthentication request = new ActivateUsernameTokenAuthentication();
            request.setServiceName(serviceName);
            request.setUserGroups(userGroups);
            stub.activateUsernameTokenAuthentication(request);
        } catch (java.lang.Exception e) {
            log.error("Error in activating username token authentication.", e);
            throw e;
        }
    }

    public void disableSecurityOnService(String serviceName) throws java.lang.Exception {
        try {
            DisableSecurityOnService request = new DisableSecurityOnService();
            request.setServiceName(serviceName);
            stub.disableSecurityOnService(request);
        } catch (java.lang.Exception e) {
            log.error("Error in disabling security on service", e);
            throw e;
        }
    }

    public void applyKerberosSecurity(String serviceName, String scenarioId, String servicePrincipal,
                                      String password) throws java.lang.Exception {
        try {
            ApplyKerberosSecurityPolicy request = new ApplyKerberosSecurityPolicy();
            request.setServiceName(serviceName);
            request.setPolicyId(scenarioId);
            request.setServicePrincipalName(servicePrincipal);
            request.setServicePrincipalPassword(password);

            stub.applyKerberosSecurityPolicy(request);
        } catch (java.lang.Exception e) {
            log.error("Error in applying kerberos security.", e);
            throw e;
        }
    }


    public void applySecurity(String serviceName, String scenarioId, String policyPath, String[] trustedStores,
                              String privateStore, String[] userGroups) throws java.lang.Exception {
        try {
            ApplySecurity request = new ApplySecurity();
            request.setServiceName(serviceName);
            request.setPolicyId(scenarioId);
            request.setPolicyPath(policyPath);
            request.setTrustedStores(trustedStores);
            request.setPrivateStore(privateStore);
            request.setUserGroupNames(userGroups);
            stub.applySecurity(request);
        } catch (java.lang.Exception e) {
            log.error("Error in applying security.", e);
            throw e;
        }
    }

    public SecurityScenarioDataWrapper getScenarios(String serviceName) throws java.lang.Exception {
        try {
            GetScenarios request = new GetScenarios();
            request.setServiceName(serviceName);
            GetScenariosResponse response = stub.getScenarios(request);
            return response.get_return();
        } catch (java.lang.Exception e) {
            log.error("Error in getting scenarios", e);
            throw e;
        }
    }

    public SecurityScenarioData getSecurityScenario(String serviceId) throws java.lang.Exception {
        try {
            GetSecurityScenario request = new GetSecurityScenario();
            request.setSceneId(serviceId);
            GetSecurityScenarioResponse response = stub.getSecurityScenario(request);
            return response.get_return();
        } catch (java.lang.Exception e) {
            log.error("Error in getting security scenarios", e);
            throw e;
        }
    }

    public SecurityConfigData getSecurityConfigData(String serviceName, String scenrioId, String policyPath)
            throws java.lang.Exception {
        try {
            GetSecurityConfigData request = new GetSecurityConfigData();
            request.setServiceName(serviceName);
            request.setScenarioId(scenrioId);
            request.setPolicyPath(policyPath);
            GetSecurityConfigDataResponse response = stub.getSecurityConfigData(request);
            return response.get_return();
        } catch (java.lang.Exception e) {
            log.error("Error in getting security config data", e);
            throw e;
        }
    }
}
