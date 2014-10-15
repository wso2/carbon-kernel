/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.integration.featuremgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.configurations.AutomationConfiguration;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.feature.mgt.core.operations.OperationFactory;
import org.wso2.carbon.feature.mgt.stub.prov.data.FeatureInfo;
import org.wso2.carbon.feature.mgt.stub.prov.data.LicenseInfo;
import org.wso2.carbon.feature.mgt.stub.prov.data.ProvisioningActionResultInfo;
import org.wso2.carbon.integration.clients.FeatureAdminClient;
import org.wso2.carbon.integration.clients.RepositoryAdminClient;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.admin.client.TenantManagementServiceClient;
import org.wso2.carbon.integration.common.extensions.utils.ExtensionCommonConstants;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;

import javax.xml.xpath.XPathExpressionException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This class is responsible for adding tenants and users
 * defined under userManagement entry in automation.xml to servers.
 */
public class FeatureManager {

    private static final Log log = LogFactory.getLog(FeatureManager.class);
    String sessionCookie;
    String backendURL;
    List<String> tenantsList;
    TenantManagementServiceClient tenantStub;
    String productGroupName;
    String instanceName;
    RepositoryAdminClient repositoryAdminClient;
    FeatureAdminClient featureAdminClient;
    String p2RepoPath;
    String P2_REPO_NAME = "localRepo";
    public static final String FEATURE_REPO_PATH_KEY = "p2-repo-path";
    FeatureInfo[] featureInfos;


    public FeatureManager(String productGroupName, String instanceName, List<FeatureInfo> featureList) throws Exception {
        this.productGroupName = productGroupName;
        this.instanceName = instanceName;

        featureInfos = featureList.toArray(new FeatureInfo[featureList.size()]);

        AutomationContext automationContext = new AutomationContext(productGroupName, instanceName,
                TestUserMode.SUPER_TENANT_ADMIN);
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        LoginLogoutClient loginLogoutUtil = new LoginLogoutClient(automationContext);
        sessionCookie = loginLogoutUtil.login();

        repositoryAdminClient = new RepositoryAdminClient(backendURL, sessionCookie);
        featureAdminClient = new FeatureAdminClient(backendURL, sessionCookie);
    }

    public void addfeatureRepo() throws Exception {
        p2RepoPath = System.getProperty(FEATURE_REPO_PATH_KEY);
        repositoryAdminClient.addRepository(p2RepoPath, P2_REPO_NAME, true);
    }

    public ProvisioningActionResultInfo reviewInstallFeatures() throws Exception {

        return featureAdminClient.reviewInstallFeaturesAction(featureInfos);
    }

    public LicenseInfo[] getLicensingInformation() throws Exception {
        return featureAdminClient.getLicensingInformation();
    }

    public void installFeatures() throws Exception {
        featureAdminClient.performInstallation(OperationFactory.INSTALL_ACTION);
    }

    public void removeFeatures() throws Exception {
//        featureAdminClient.removeAllFeaturesWithProperty(featureInfos[0].getFeatureID());
    }

    protected String login(String userName, String domain, String password, String backendUrl, String hostName) throws
            RemoteException, LoginAuthenticationExceptionException, XPathExpressionException {
        AuthenticatorClient loginClient = new AuthenticatorClient(backendUrl);
        if (!domain.equals(AutomationConfiguration.getConfigurationValue(ExtensionCommonConstants.SUPER_TENANT_DOMAIN_NAME))) {
            userName += "@" + domain;
        }
        return loginClient.login(userName, password, hostName);
    }
}


