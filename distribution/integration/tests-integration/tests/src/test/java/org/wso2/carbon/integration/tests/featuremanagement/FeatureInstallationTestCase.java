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
package org.wso2.carbon.integration.tests.featuremanagement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.utils.ClientConnectionUtil;
import org.wso2.carbon.feature.mgt.stub.prov.data.FeatureInfo;
import org.wso2.carbon.integration.common.clients.ServerAdminClient;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.FeatureManagementUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This performs installing a given set of features and checking if they are properly installed after
 * restart.
 */
public class FeatureInstallationTestCase extends CarbonIntegrationBaseTest {
    private static final Log log = LogFactory.getLog(FeatureInstallationTestCase.class);
    private List<FeatureInfo> featureList;
    private ServerAdminClient serverAdminClient;

    @BeforeClass(alwaysRun = true)
    public void initiate() throws Exception {
        if (isP2RepoAvailable()) {
            throw new SkipException("p2-repo system variable(p2-repo-path) not found to refer p2-repo");
        }
        super.init();
        featureList = new ArrayList<FeatureInfo>();
        FeatureInfo featureInfo = new FeatureInfo();
        featureInfo.setFeatureID("org.wso2.carbon.student.mgt.feature.group");
        featureInfo.setFeatureVersion("4.4.1.SNAPSHOT");
        featureList.add(featureInfo);
    }

    @Test(groups = {"carbon.core.graceful.restart.test"})
    public void testGracefulServerRestart() throws Exception {
        FeatureManagementUtil featureManager = new FeatureManagementUtil(featureList, automationContext);
        featureManager.addFeatureRepo();
        featureManager.reviewInstallFeatures();
        featureManager.getLicensingInformation();
        featureManager.installFeatures();
        log.info("Feature :" + featureList.get(0).getFeatureID() + " installed successfully");
        serverAdminClient = new ServerAdminClient(automationContext);
        log.info("Going to restart the server");
        serverAdminClient.restartGracefully();
        Thread.sleep(10000);
        ClientConnectionUtil.waitForPort(Integer.parseInt(
                automationContext.getDefaultInstance().getPorts().get("https")), 300000
                , true, automationContext.getDefaultInstance().getHosts().get("https"));

        ClientConnectionUtil.waitForLogin(automationContext);

        featureManager = new FeatureManagementUtil(featureList, automationContext);
        super.init();
        Assert.assertTrue(featureManager.isFeatureInstalled(), "Feature not installed successfully");
    }

    // Remove the populated users on execution finish of the test
    @AfterClass(alwaysRun = true)
    public void testTearDown() throws Exception {
        if (isP2RepoAvailable()) {
            throw new SkipException("p2-repo system variable(p2-repo-path) not found to refer p2-repo");
        }
        FeatureManagementUtil featureManager = new FeatureManagementUtil(featureList, automationContext);
        featureManager.removeFeatures();
        serverAdminClient = new ServerAdminClient(automationContext);
        log.info("Going to restart the server");
        serverAdminClient.restartGracefully();
        Thread.sleep(10000);
        ClientConnectionUtil.waitForPort(Integer.parseInt(
                automationContext.getDefaultInstance().getPorts().get("https")), 240000, true, "localhost");
        ClientConnectionUtil.waitForLogin(automationContext);

    }

    private boolean isP2RepoAvailable() {
        return (System.getProperty(FeatureManagementUtil.FEATURE_REPO_PATH_KEY) == null ||
                System.getProperty(FeatureManagementUtil.FEATURE_REPO_PATH_KEY).isEmpty());
    }

}
