/*
*Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.integration.tests.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.feature.mgt.core.operations.OperationFactory;
import org.wso2.carbon.feature.mgt.stub.prov.data.Feature;
import org.wso2.carbon.feature.mgt.stub.prov.data.FeatureInfo;
import org.wso2.carbon.feature.mgt.stub.prov.data.LicenseInfo;
import org.wso2.carbon.feature.mgt.stub.prov.data.ProvisioningActionResultInfo;
import org.wso2.carbon.feature.mgt.ui.FeatureWrapper;
import org.wso2.carbon.integration.common.clients.FeatureAdminClient;
import org.wso2.carbon.integration.common.clients.RepositoryAdminClient;

import java.util.Arrays;
import java.util.List;

/**
 * This class is responsible for adding tenants and users
 */
public class FeatureManagementUtil {

    private static final Log log = LogFactory.getLog(FeatureManagementUtil.class);
    String sessionCookie;
    String backendURL;
    RepositoryAdminClient repositoryAdminClient;
    FeatureAdminClient featureAdminClient;
    String p2RepoPath;
    String P2_REPO_NAME = "localRepo";
    public static final String FEATURE_REPO_PATH_KEY = "p2-repo-path";
    FeatureInfo[] featureInfos;
    LoginLogoutUtil loginLogoutUtil;
    AutomationContext automationContext;

    public FeatureManagementUtil(List<FeatureInfo> featureList, AutomationContext automationContext)
            throws Exception {

        this.automationContext = automationContext;
        featureInfos = featureList.toArray(new FeatureInfo[featureList.size()]);

        backendURL = automationContext.getContextUrls().getBackEndUrl();

        loginLogoutUtil = new LoginLogoutUtil();
        String sessionCookie = loginLogoutUtil.login(
                automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword().toCharArray(), backendURL);

        repositoryAdminClient = new RepositoryAdminClient(backendURL, automationContext);
        featureAdminClient = new FeatureAdminClient(backendURL, sessionCookie);
    }

    public void addFeatureRepo() throws Exception {
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
        log.info("remove all server features ");
//        featureAdminClient.removeAllFeaturesWithProperty("org.wso2.carbon.p2.category.type", "server");
        log.info("remove all console features ");
//        featureAdminClient.removeAllFeaturesWithProperty("org.wso2.carbon.p2.category.type", "console");
    }

    public boolean isFeatureInstalled() throws Exception {
        sessionCookie = loginLogoutUtil.login(
                automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword().toCharArray(), backendURL);
        featureAdminClient = new FeatureAdminClient(backendURL, sessionCookie);
        FeatureWrapper[] featuresWrappers = featureAdminClient.getInstalledFeatures();

        boolean isInstalledFeatureFound = false;
        Arrays.asList(featuresWrappers);
        int length = featuresWrappers.length;
        for (FeatureWrapper featureWrapper : featuresWrappers) {
            Feature feature = featureWrapper.getWrappedFeature();
            for (FeatureInfo featureInfo : featureInfos) {
                if (featureInfo.getFeatureID().equals(feature.getFeatureID()) &&
                    featureInfo.getFeatureVersion().equals(feature.getFeatureVersion())) {
                    log.info("installed feature found " + feature.getFeatureID());
                    isInstalledFeatureFound = true;
                    break;
                }
            }
        }
        return isInstalledFeatureFound;
    }
}


