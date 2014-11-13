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
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.feature.mgt.core.operations.OperationFactory;
import org.wso2.carbon.feature.mgt.stub.prov.data.Feature;
import org.wso2.carbon.feature.mgt.stub.prov.data.FeatureInfo;
import org.wso2.carbon.feature.mgt.stub.prov.data.LicenseInfo;
import org.wso2.carbon.feature.mgt.stub.prov.data.ProvisioningActionResultInfo;
import org.wso2.carbon.feature.mgt.ui.FeatureWrapper;
import org.wso2.carbon.integration.clients.FeatureAdminClient;
import org.wso2.carbon.integration.clients.RepositoryAdminClient;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import java.util.Arrays;
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
    RepositoryAdminClient repositoryAdminClient;
    FeatureAdminClient featureAdminClient;
    String p2RepoPath;
    String P2_REPO_NAME = "localRepo";
    public static final String FEATURE_REPO_PATH_KEY = "p2-repo-path";
    FeatureInfo[] featureInfos;
    LoginLogoutUtil loginLogoutUtil;
    AutomationContext automationContext;

    public FeatureManager(List<FeatureInfo> featureList, AutomationContext automationContext) throws Exception {
        
        this.automationContext = automationContext;
        featureInfos = featureList.toArray(new FeatureInfo[featureList.size()]);

        backendURL = automationContext.getContextUrls().getBackEndUrl();

        repositoryAdminClient = new RepositoryAdminClient(backendURL, automationContext);
        featureAdminClient = new FeatureAdminClient(backendURL, automationContext);
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
        //todo
//        featureAdminClient.removeAllFeaturesWithProperty(featureInfos[0].getFeatureID());
    }

    /*protected String login(String userName, String domain, String password, String backendUrl, String hostName) throws
            RemoteException, LoginAuthenticationExceptionException, XPathExpressionException {
        AuthenticatorClient loginClient = new AuthenticatorClient(backendUrl);
        if (!domain.equals(AutomationConfiguration.getConfigurationValue(Constants.SUPER_TENANT_DOMAIN_NAME))) {
            userName += "@" + domain;
        }
        return loginClient.login(userName, password, hostName);
    }*/

    public void checkInstalledFeatures(boolean afterRestart) throws Exception {
        if (afterRestart) {
            sessionCookie = loginLogoutUtil.login();
            featureAdminClient = new FeatureAdminClient(backendURL, automationContext);
        }
        FeatureWrapper[] featuresWrappers = featureAdminClient.getInstalledFeatures();

        int counter = 0;
        Arrays.asList(featuresWrappers);
        int length = featuresWrappers.length;
        for (FeatureWrapper featureWrapper : featuresWrappers) {
            Feature feature = featureWrapper.getWrappedFeature();
            for (FeatureInfo featureInfo : featureInfos) {
                if (featureInfo.getFeatureID().equals(feature.getFeatureID()) &&
                        featureInfo.getFeatureVersion().equals(feature.getFeatureVersion())) {
                    counter++;
                }
            }
        }
        assert counter == featureInfos.length;
    }
}


