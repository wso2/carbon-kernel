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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.feature.mgt.stub.prov.data.Feature;
import org.wso2.carbon.feature.mgt.stub.prov.data.FeatureInfo;
import org.wso2.carbon.feature.mgt.stub.prov.data.RepositoryInfo;
import org.wso2.carbon.integration.common.clients.RepositoryAdminClient;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test case which tests Feature Management functionality
 */
public class FeatureRepositoryMgtTestCase extends CarbonIntegrationBaseTest {
    private RepositoryAdminClient repositoryAdminClient;

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

        repositoryAdminClient = new RepositoryAdminClient(backendURL, automationContext);
    }

    @Test(groups = {"carbon.core"}, description = "Add 3 different repositories with different nick names")
    public void testAddDifferentRepositoriesWithDifferentNickNames() throws Exception {
        repositoryAdminClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        repositoryAdminClient.addRepository(sampleP2RepoCopy1Path, repoNickNames[1], true);
        repositoryAdminClient.addRepository(sampleP2RepoCopy2Path, repoNickNames[2], true);
        RepositoryInfo[] repositoryInfos = repositoryAdminClient.getAllRepositories();

        assertEquals(repositoryInfos.length, 3);

        int count = 0;
        for (String repoNickName : repoNickNames) {
            for (RepositoryInfo repositoryInfo : repositoryInfos) {
                if (repositoryInfo != null && repositoryInfo.getNickName().equals(repoNickName) &&
                        repositoryInfo.getEnabled() == true) {
                    count++;
                    break;
                }
            }
        }
        assertEquals(count, 3);

        removeRepositories(new String[]{sampleP2RepoPath, sampleP2RepoCopy1Path, sampleP2RepoCopy2Path});
    }

    @Test(groups = {"carbon.core"}, description = "Add 3 different repositories with same nick name",
            dependsOnMethods = {"testAddDifferentRepositoriesWithDifferentNickNames"})
    public void testAddDifferentRepositoriesWithSameNickName() throws Exception {
        repositoryAdminClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        repositoryAdminClient.addRepository(sampleP2RepoCopy1Path, repoNickNames[0], true);
        repositoryAdminClient.addRepository(sampleP2RepoCopy2Path, repoNickNames[0], true);
        RepositoryInfo[] repositoryInfos = repositoryAdminClient.getAllRepositories();

        int count = 0;
        for (RepositoryInfo repositoryInfo : repositoryInfos) {
            if (repositoryInfo != null && repositoryInfo.getNickName().equals(repoNickNames[0]) &&
                    repositoryInfo.getEnabled() == true) {
                count++;
            }
        }
        assertEquals(count, 3);

        removeRepositories(new String[]{sampleP2RepoPath, sampleP2RepoCopy1Path, sampleP2RepoCopy2Path});
    }

    @Test(groups = {"carbon.core"}, description = "Add same repositories with 3 different nick names",
            dependsOnMethods = {"testAddDifferentRepositoriesWithSameNickName"})
    public void testAddSameRepositoryWithDifferentNickNames() throws Exception {
        repositoryAdminClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        repositoryAdminClient.addRepository(sampleP2RepoPath, repoNickNames[1], true);
        repositoryAdminClient.addRepository(sampleP2RepoPath, repoNickNames[2], true);
        RepositoryInfo[] repositoryInfos = repositoryAdminClient.getAllRepositories();

        assertEquals(repositoryInfos.length, 1);
        assertTrue(repositoryInfos[0] != null && repositoryInfos[0].getNickName().equals(repoNickNames[0]) &&
                repositoryInfos[0].getEnabled() == true);

        removeRepositories(new String[]{sampleP2RepoPath});
    }

    @Test(groups = {"carbon.core"}, description = "Test remove repositories",
            dependsOnMethods = {"testAddSameRepositoryWithDifferentNickNames"})
    public void testRemoveRepository() throws Exception {
        repositoryAdminClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        RepositoryInfo[] repositoryInfos = repositoryAdminClient.getAllRepositories();
        assertEquals(repositoryInfos.length, 1);

        removeRepositories(new String[]{sampleP2RepoPath});
        repositoryInfos = repositoryAdminClient.getAllRepositories();
        assertTrue(repositoryInfos == null);
    }

    @Test(groups = {"carbon.core"}, description = "Enables and Disables a repository",
            dependsOnMethods = {"testRemoveRepository"})
    public void testDisableAndEnableRepository() throws Exception {
        repositoryAdminClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        repositoryAdminClient.addRepository(sampleP2RepoCopy1Path, repoNickNames[1], true);
        repositoryAdminClient.addRepository(sampleP2RepoCopy2Path, repoNickNames[2], true);
        RepositoryInfo[] repositoryInfos = repositoryAdminClient.getAllRepositories();

        assertEquals(repositoryInfos.length, 3);

        repositoryAdminClient.enableRepository("file://" + sampleP2RepoCopy1Path, Boolean.FALSE.toString());
        repositoryInfos = repositoryAdminClient.getEnabledRepositories();
        assertEquals(repositoryInfos.length, 3 - 1);

        int count = 0;
        for (RepositoryInfo repositoryInfo : repositoryInfos) {
            if (repositoryInfo != null && repositoryInfo.getEnabled() == true) {
                count++;
            }
        }
        assertEquals(count, 2);

        repositoryAdminClient.enableRepository("file://" + sampleP2RepoCopy1Path, Boolean.TRUE.toString());
        repositoryInfos = repositoryAdminClient.getEnabledRepositories();
        assertEquals(repositoryInfos.length, 3);

        count = 0;
        for (RepositoryInfo repositoryInfo : repositoryInfos) {
            if (repositoryInfo != null && repositoryInfo.getEnabled() == true) {
                count++;
            }
        }
        assertEquals(count, 3);

        removeRepositories(new String[]{sampleP2RepoPath, sampleP2RepoCopy1Path, sampleP2RepoCopy2Path});
    }

    @Test(groups = {"carbon.core"}, description = "Update repository path and name",
            dependsOnMethods = {"testDisableAndEnableRepository"})
    public void testUpdateRepository() throws Exception {
        repositoryAdminClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        RepositoryInfo[] repositoryInfos = repositoryAdminClient.getAllRepositories();
        assertTrue(repositoryInfos[0] != null && repositoryInfos[0].getNickName().equals(repoNickNames[0]) &&
                repositoryInfos[0].getEnabled() == true);

        repositoryAdminClient.updateRepository("file://" + sampleP2RepoPath, repoNickNames[0],
                "file://" + sampleP2RepoCopy1Path, repoNickNames[1]);

        Thread.sleep(5 * 1000);

        repositoryInfos = repositoryAdminClient.getAllRepositories();
        assertTrue(repositoryInfos[0] != null && repositoryInfos[0].getNickName().equals(repoNickNames[1]) &&
                repositoryInfos[0].getEnabled() == true);

        removeRepositories(new String[]{sampleP2RepoCopy1Path});
    }

    @Test(groups = {"carbon.core"}, description = "Retrieve installable features from the repo",
            dependsOnMethods = {"testUpdateRepository"})
    public void testGetInstallableFeatures() throws Exception {
        repositoryAdminClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        Feature[] features = repositoryAdminClient.
                getInstallableFeatures("file://" + sampleP2RepoPath, false, true, false);

        assertTrue(features.length > 0);

        removeRepositories(new String[]{sampleP2RepoPath});
    }

    @Test(groups = {"carbon.core"}, description = "Retrieve installable feature details from the repo",
            dependsOnMethods = {"testGetInstallableFeatures"})
    public void testGetInstallableFeatureDetails() throws Exception {
        repositoryAdminClient.addRepository(sampleP2RepoPath, repoNickNames[0], true);
        Feature[] features = repositoryAdminClient.
                getInstallableFeatures("file://" + sampleP2RepoPath, false, true, false);
        assertTrue(features.length > 0);

        FeatureInfo featureInfo = repositoryAdminClient.getInstallableFeatureInfo(
                features[0].getFeatureID(),
                features[0].getFeatureVersion());

        assertTrue(featureInfo != null);
        assertEquals(featureInfo.getFeatureID(), features[0].getFeatureID());
        assertEquals(featureInfo.getFeatureVersion(), features[0].getFeatureVersion());

        removeRepositories(new String[]{sampleP2RepoPath});
    }

    private void removeRepositories(String[] repoPaths) throws Exception {
        for (String repoPath : repoPaths) {
            repositoryAdminClient.removeRepository("file://" + repoPath);
        }
    }
}
