/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.integration.tests.featuremanagement;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.feature.mgt.stub.prov.data.FeatureInfo;
import org.wso2.carbon.feature.mgt.stub.prov.data.RepositoryInfo;
import org.wso2.carbon.feature.mgt.ui.FeatureWrapper;
import org.wso2.carbon.feature.mgt.ui.ProvisioningAdminClient;
import org.wso2.carbon.feature.mgt.ui.RepositoryAdminServiceClient;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.LoginLogoutUtil;

import java.io.File;
import java.util.Locale;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test case which tests Feature Management functionality
 */
public class FeatureRepositoryMgtTestCase extends CarbonIntegrationBaseTest {
    private LoginLogoutUtil loginLogoutUtil;
    private String backEndURL;
    private RepositoryAdminServiceClient repositoryAdminServiceClient;
    private ProvisioningAdminClient provisioningAdminClient;

    String[] repoNickNames = {"testRepo101", "testRepo102", "testRepo103"};
    String sampleP2RepoPath;
    String sampleP2RepoCopy1Path;
    String sampleP2RepoCopy2Path;

    @BeforeClass(alwaysRun = true)
    public void initTests() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        String resourcePath = TestConfigurationProvider.getResourceLocation("CARBON");
        sampleP2RepoPath = resourcePath + File.separator + "sample-p2-repo1";
        sampleP2RepoCopy1Path = resourcePath + File.separator + "sample-p2-repo1-copy1";
        sampleP2RepoCopy2Path = resourcePath + File.separator + "sample-p2-repo1-copy2";

        loginLogoutUtil  = new LoginLogoutUtil();
        backEndURL = contextUrls.getBackEndUrl();

        ConfigurationContext configurationContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(FrameworkPathUtil.getSystemResourceLocation() + File.separator + "client", null);
        String sessionCookie = loginLogoutUtil.login(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword().toCharArray(), backEndURL);
        repositoryAdminServiceClient = new RepositoryAdminServiceClient(sessionCookie,
                backEndURL, configurationContext, new Locale("en"));
        provisioningAdminClient = new ProvisioningAdminClient(sessionCookie,
                backEndURL, configurationContext, new Locale("en"));
        repositoryAdminServiceClient.repositoryAdminServiceStub._getServiceClient().engageModule("addressing");
    }

    @Test(groups = {"carbon.core"}, description = "Add 3 different repositories with different nick names")
    public void testAddDifferentRepositoriesWithDifferentNickNames() throws Exception {
        repositoryAdminServiceClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        repositoryAdminServiceClient.addRepository(sampleP2RepoCopy1Path, repoNickNames[1], true);
        repositoryAdminServiceClient.addRepository(sampleP2RepoCopy2Path, repoNickNames[2], true);
        RepositoryInfo[] repositoryInfos = repositoryAdminServiceClient.getAllRepositories();

        assertEquals(repositoryInfos.length, repoNickNames.length);

        int count = 0;
        for (String repoNickName : repoNickNames) {
            for (RepositoryInfo repositoryInfo : repositoryInfos) {
                if (repositoryInfo != null && repositoryInfo.getNickName().equals(repoNickName) &&
                        repositoryInfo.getEnabled() == true){
                    count++;
                    break;
                }
            }
        }
        assertEquals(count, 3);

        removeRepositories(new String[] {sampleP2RepoPath, sampleP2RepoCopy1Path, sampleP2RepoCopy2Path});
    }

    @Test(groups = {"carbon.core"}, description = "Add 3 different repositories with same nick name",
            dependsOnMethods = {"testAddDifferentRepositoriesWithDifferentNickNames"})
    public void testAddDifferentRepositoriesWithSameNickName() throws Exception {
        repositoryAdminServiceClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        repositoryAdminServiceClient.addRepository(sampleP2RepoCopy1Path, repoNickNames[0], true);
        repositoryAdminServiceClient.addRepository(sampleP2RepoCopy2Path, repoNickNames[0], true);
        RepositoryInfo[] repositoryInfos = repositoryAdminServiceClient.getAllRepositories();

        int count = 0;
        for (RepositoryInfo repositoryInfo : repositoryInfos) {
            if (repositoryInfo != null && repositoryInfo.getNickName().equals(repoNickNames[0]) &&
                    repositoryInfo.getEnabled() == true){
                count++;
            }
        }
        assertEquals(count, 3);

        removeRepositories(new String[] {sampleP2RepoPath, sampleP2RepoCopy1Path, sampleP2RepoCopy2Path});
    }

    @Test(groups = {"carbon.core"}, description = "Add same repositories with 3 different nick names",
            dependsOnMethods = {"testAddDifferentRepositoriesWithSameNickName"})
    public void testAddSameRepositoryWithDifferentNickNames() throws Exception {
        repositoryAdminServiceClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        repositoryAdminServiceClient.addRepository(sampleP2RepoPath, repoNickNames[1], true);
        repositoryAdminServiceClient.addRepository(sampleP2RepoPath, repoNickNames[2], true);
        RepositoryInfo[] repositoryInfos = repositoryAdminServiceClient.getAllRepositories();

        assertEquals(repositoryInfos.length, 1);
        assertTrue(repositoryInfos[0] != null && repositoryInfos[0].getNickName().equals(repoNickNames[0]) &&
                repositoryInfos[0].getEnabled() == true);

        removeRepositories(new String[] {sampleP2RepoPath});
    }

    @Test(groups = {"carbon.core"}, description = "Test remove repositories",
            dependsOnMethods = {"testAddSameRepositoryWithDifferentNickNames"})
    public void testRemoveRepository() throws Exception {
        repositoryAdminServiceClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        RepositoryInfo[] repositoryInfos = repositoryAdminServiceClient.getAllRepositories();
        assertEquals(repositoryInfos.length, 1);

        removeRepositories(new String[] {sampleP2RepoPath});
        repositoryInfos = repositoryAdminServiceClient.getAllRepositories();
        assertTrue(repositoryInfos == null);
    }

    @Test(groups = {"carbon.core"}, description = "Enables and Disables a repository",
            dependsOnMethods = {"testRemoveRepository"})
    public void testDisableAndEnableRepository() throws Exception {
        repositoryAdminServiceClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        repositoryAdminServiceClient.addRepository(sampleP2RepoCopy1Path, repoNickNames[1], true);
        repositoryAdminServiceClient.addRepository(sampleP2RepoCopy2Path, repoNickNames[2], true);
        RepositoryInfo[] repositoryInfos = repositoryAdminServiceClient.getAllRepositories();

        assertEquals(repositoryInfos.length, repoNickNames.length);

        repositoryAdminServiceClient.enableRepository("file://" + sampleP2RepoCopy1Path, RepositoryAdminServiceClient.DISABLED);
        repositoryInfos = repositoryAdminServiceClient.getEnabledRepositories();
        assertEquals(repositoryInfos.length, repoNickNames.length - 1);

        int count = 0;
        for (RepositoryInfo repositoryInfo : repositoryInfos) {
            if (repositoryInfo != null && repositoryInfo.getEnabled() == true){
                count++;
            }
        }
        assertEquals(count, 2);

        repositoryAdminServiceClient.enableRepository("file://" + sampleP2RepoCopy1Path, RepositoryAdminServiceClient.ENABLED);
        repositoryInfos = repositoryAdminServiceClient.getEnabledRepositories();
        assertEquals(repositoryInfos.length, repoNickNames.length);

        count = 0;
        for (RepositoryInfo repositoryInfo : repositoryInfos) {
            if (repositoryInfo != null && repositoryInfo.getEnabled() == true){
                count++;
            }
        }
        assertEquals(count, repoNickNames.length);

        removeRepositories(new String[] {sampleP2RepoPath, sampleP2RepoCopy1Path, sampleP2RepoCopy2Path});
    }

    @Test(groups = {"carbon.core"}, description = "Update repository path and name",
            dependsOnMethods = {"testDisableAndEnableRepository"})
    public void testUpdateRepository() throws Exception {
        repositoryAdminServiceClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        RepositoryInfo[] repositoryInfos = repositoryAdminServiceClient.getAllRepositories();
        assertTrue(repositoryInfos[0] != null && repositoryInfos[0].getNickName().equals(repoNickNames[0]) &&
                repositoryInfos[0].getEnabled() == true);

        repositoryAdminServiceClient.updateRepository("file://" + sampleP2RepoPath, repoNickNames[0],
                "file://" + sampleP2RepoCopy1Path, repoNickNames[1]);

        Thread.sleep(5 * 1000);

        repositoryInfos = repositoryAdminServiceClient.getAllRepositories();
        assertTrue(repositoryInfos[0] != null && repositoryInfos[0].getNickName().equals(repoNickNames[1]) &&
                repositoryInfos[0].getEnabled() == true);

        removeRepositories(new String[] {sampleP2RepoCopy1Path});
    }

    @Test(groups = {"carbon.core"}, description = "Retrieve installable features from the repo",
            dependsOnMethods = {"testUpdateRepository"})
    public void testGetInstallableFeatures() throws Exception {
        repositoryAdminServiceClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        FeatureWrapper[] featureWrappers = repositoryAdminServiceClient.
                getInstallableFeatures("file://" + sampleP2RepoPath, false, true, false);

        assertTrue(featureWrappers.length > 0);

        removeRepositories(new String[] {sampleP2RepoPath});
    }

    @Test(groups = {"carbon.core"}, description = "Retrieve installable feature details from the repo",
            dependsOnMethods = {"testGetInstallableFeatures"})
    public void testGetInstallableFeatureDetails() throws Exception {
        repositoryAdminServiceClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        FeatureWrapper[] featureWrappers = repositoryAdminServiceClient.
                getInstallableFeatures("file://" + sampleP2RepoPath, false, true, false);
        assertTrue(featureWrappers.length > 0);

        FeatureInfo featureInfo = repositoryAdminServiceClient.getInstallableFeatureDetails(
                featureWrappers[0].getWrappedFeature().getFeatureID(),
                featureWrappers[0].getWrappedFeature().getFeatureVersion());

        assertTrue(featureInfo != null);
        assertEquals(featureInfo.getFeatureID(), featureWrappers[0].getWrappedFeature().getFeatureID());
        assertEquals(featureInfo.getFeatureVersion(), featureWrappers[0].getWrappedFeature().getFeatureVersion());

        removeRepositories(new String[]{sampleP2RepoPath});
    }

    private void removeRepositories(String[] repoPaths) throws Exception {
        for (String repoPath : repoPaths) {
            repositoryAdminServiceClient.removeRepository("file://" + repoPath);
        }
    }
}
