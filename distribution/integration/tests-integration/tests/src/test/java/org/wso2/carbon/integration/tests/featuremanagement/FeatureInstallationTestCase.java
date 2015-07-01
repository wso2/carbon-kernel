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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.feature.mgt.stub.prov.data.FeatureInfo;
import org.wso2.carbon.integration.common.clients.ServerAdminClient;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.FeatureManagementUtil;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;

/**
 * This performs installing a given set of features and checking if they are properly installed after
 * restart.
 */
public class FeatureInstallationTestCase extends CarbonIntegrationBaseTest {
    private static final Log log = LogFactory.getLog(FeatureInstallationTestCase.class);
    private List<FeatureInfo> featureList;
    ServerAdminClient serverAdminClient;

    @BeforeClass(alwaysRun = true)
    public void initiate() throws XPathExpressionException {
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
        serverAdminClient = new ServerAdminClient(automationContext);
        serverAdminClient.restartGracefully();

        featureManager = new FeatureManagementUtil(featureList, automationContext);
        super.init();
        Assert.assertTrue(featureManager.isFeatureInstalled(), "Feature not installed successfully");
    }

    // Remove the populated users on execution finish of the test
    @AfterClass(alwaysRun = true)
    public void onExecutionFinish() throws Exception {
        FeatureManagementUtil featureManager = new FeatureManagementUtil(featureList, automationContext);
        featureManager.removeFeatures();

    }

}
