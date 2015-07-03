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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.feature.mgt.stub.prov.data.FeatureInfo;
import org.wso2.carbon.feature.mgt.stub.prov.data.LicenseFeatureHolder;
import org.wso2.carbon.feature.mgt.stub.prov.data.ProfileHistory;
import org.wso2.carbon.feature.mgt.stub.prov.data.ProvisioningActionResultInfo;
import org.wso2.carbon.feature.mgt.ui.FeatureWrapper;
import org.wso2.carbon.integration.common.clients.FeatureAdminClient;
import org.wso2.carbon.integration.common.clients.RepositoryAdminClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;

import java.io.File;

import static org.testng.Assert.*;

public class FeatureMgtTestCase extends CarbonIntegrationBaseTest {
    private static final Log log = LogFactory.getLog(FeatureMgtTestCase.class);
    FeatureAdminClient featureAdminClient;
    RepositoryAdminClient repositoryAdminClient;
    String sampleP2RepoPath;
    protected ServerConfigurationManager serverManager;
    protected String[] featureIds = new String[]{"org.wso2.carbon.test.feature1.feature.group",
            "org.wso2.carbon.test.feature2.feature.group",
            "org.wso2.carbon.test.feature3.feature.group"};
    protected String[] featureVersions = new String[]{"1.0.0", "1.0.1"};
    protected String[] namespaces = new String[]{"http://component1.test.carbon.wso2.org",
            "http://component2.test.carbon.wso2.org",
            "http://component3.test.carbon.wso2.org"};

    @BeforeClass(alwaysRun = true)
    public void initTests() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        featureAdminClient = new FeatureAdminClient(contextUrls.getBackEndUrl(), sessionCookie);
        repositoryAdminClient = new RepositoryAdminClient(contextUrls.getBackEndUrl(), automationContext);

        String resourcePath = TestConfigurationProvider.getResourceLocation("CARBON");
        sampleP2RepoPath = resourcePath + File.separator + "sample-p2-repo1";
        repositoryAdminClient.addRepository(sampleP2RepoPath, "testRepo101", true);

        serverManager = new ServerConfigurationManager(automationContext);
    }

    @Test(groups = {"carbon.core"}, description = "Installing a feature")
    public void testInstallFeature() throws Exception {
        FeatureInfo featureInfo = getFeatureInfo(featureIds[0], featureVersions[0]);
        featureAdminClient.reviewInstallFeaturesAction(new FeatureInfo[]{featureInfo});
        LicenseFeatureHolder[] licenseFeatureHolders = featureAdminClient.getFeatureLicenseInfo();
        assertNotNull(licenseFeatureHolders);
        assertEquals(licenseFeatureHolders.length, 1);
        assertNotNull(licenseFeatureHolders[0].getLicenseInfo());
        featureAdminClient.performInstallation(featureAdminClient.getInstallActionType());

        serverManager.restartGracefully();
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        featureAdminClient = new FeatureAdminClient(contextUrls.getBackEndUrl(), sessionCookie);

        boolean installed = false;
        FeatureWrapper[] featureWrappers = featureAdminClient.getInstalledFeatures();
        for (FeatureWrapper featureWrapper : featureWrappers) {
            if (featureWrapper.getWrappedFeature().getFeatureID().equals(featureIds[0])) {
                installed = true;
                break;
            }
        }
        assertTrue(installed);

        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement response = axisServiceClient.sendReceive(getPayload(namespaces[0]), getServiceUrl("Component1"), "getName");
        log.info("Response : " + response);
        assertTrue(response.toString().contains("<ns:return>org.wso2.carbon.test.component1.Component1-v1.0.0</ns:return>"));
    }

    @Test(groups = {"carbon.core"}, description = "View installed feature details", dependsOnMethods = {"testInstallFeature"})
    public void testViewInstalledFeatureDetails() throws Exception {
        FeatureInfo featureInfo = featureAdminClient.getInstalledFeatureDetails(featureIds[0], featureVersions[0]);
        assertTrue(featureInfo.getFeatureID().equals(featureIds[0]));
        assertTrue(featureInfo.getFeatureVersion().equals(featureVersions[0]));
    }

    @Test(groups = {"carbon.core"}, description = "uninstalling a feature",
            dependsOnMethods = {"testInstallFeature", "testViewInstalledFeatureDetails"},
            expectedExceptions = {org.apache.axis2.AxisFault.class},
            expectedExceptionsMessageRegExp = ".*The service cannot be found for the endpoint.*")
    public void testUnInstallFeature() throws Exception {
        FeatureInfo featureInfo = getFeatureInfo(featureIds[0], featureVersions[0]);
        featureAdminClient.reviewUninstallFeaturesAction(new FeatureInfo[]{featureInfo});
        featureAdminClient.performInstallation(featureAdminClient.getUninstallActionType());

        serverManager.restartGracefully();
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        featureAdminClient = new FeatureAdminClient(contextUrls.getBackEndUrl(), sessionCookie);

        boolean installed = false;
        FeatureWrapper[] featureWrappers = featureAdminClient.getInstalledFeatures();
        for (FeatureWrapper featureWrapper : featureWrappers) {
            if (featureWrapper.getWrappedFeature().getFeatureID().equals(featureIds[0])) {
                installed = true;
                break;
            }
        }
        assertFalse(installed);

        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement response = axisServiceClient.sendReceive(getPayload(namespaces[0]), getServiceUrl("Component1"), "getName");
        log.info("Response : " + response);
    }

    @Test(groups = {"carbon.core"}, description = "Reverting profiles to a previous timestamp",
            dependsOnMethods = {"testUnInstallFeature"})
    public void testRevertProfile() throws Exception {
        FeatureInfo featureInfo = getFeatureInfo(featureIds[0], featureVersions[0]);
        featureAdminClient.reviewInstallFeaturesAction(new FeatureInfo[]{featureInfo});
        featureAdminClient.performInstallation(featureAdminClient.getInstallActionType());

        serverManager.restartGracefully();
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        featureAdminClient = new FeatureAdminClient(contextUrls.getBackEndUrl(), sessionCookie);

        ProfileHistory[] profileHistories = featureAdminClient.getProfileHistory();

        assertNotNull(profileHistories);
        assertTrue(profileHistories.length > 1);

        featureAdminClient.reviewRevertPlan(Long.toString(profileHistories[profileHistories.length - 2].getTimestamp()));
        featureAdminClient.performInstallation(featureAdminClient.getRevertActionType());

        serverManager.restartGracefully();
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        featureAdminClient = new FeatureAdminClient(contextUrls.getBackEndUrl(), sessionCookie);

        boolean installed = false;
        FeatureWrapper[] featureWrappers = featureAdminClient.getInstalledFeatures();
        for (FeatureWrapper featureWrapper : featureWrappers) {
            if (featureWrapper.getWrappedFeature().getFeatureID().equals(featureIds[0])) {
                installed = true;
                break;
            }
        }
        assertFalse(installed);
    }

    @Test(groups = {"carbon.core"}, description = "Installing and updating features", dependsOnMethods = {"testRevertProfile"})
    public void testInstallAndUpdateFeature() throws Exception {
        FeatureInfo featureInfo1 = getFeatureInfo(featureIds[0], featureVersions[0]);
        FeatureInfo featureInfo2 = getFeatureInfo(featureIds[1], featureVersions[0]);
        FeatureInfo featureInfo3 = getFeatureInfo(featureIds[2], featureVersions[0]);
        featureAdminClient.reviewInstallFeaturesAction(new FeatureInfo[]{featureInfo1, featureInfo2, featureInfo3});
        featureAdminClient.performInstallation(featureAdminClient.getInstallActionType());

        serverManager.restartGracefully();
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        featureAdminClient = new FeatureAdminClient(contextUrls.getBackEndUrl(), sessionCookie);

        FeatureWrapper[] featureWrappers = featureAdminClient.getInstalledFeatures();
        int count = 0;
        for (String featureId : featureIds) {
            for (FeatureWrapper featureWrapper : featureWrappers) {
                if (featureWrapper != null && featureWrapper.getWrappedFeature().getFeatureID().equals(featureId) &&
                        featureWrapper.getWrappedFeature().getFeatureVersion().equals(featureVersions[0])){
                    count++;
                    break;
                }
            }
        }
        assertEquals(count, 3);

        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement response = axisServiceClient.sendReceive(getPayload(namespaces[0]), getServiceUrl("Component1"), "getName");
        log.info("Response : " + response);
        assertTrue(response.toString().contains("<ns:return>org.wso2.carbon.test.component1.Component1-v1.0.0</ns:return>"));

        response = axisServiceClient.sendReceive(getPayload(namespaces[1]), getServiceUrl("Component2"), "getName");
        log.info("Response : " + response);
        assertTrue(response.toString().contains("<ns:return>org.wso2.carbon.test.component2.Component2-v1.0.0</ns:return>"));

        response = axisServiceClient.sendReceive(getPayload(namespaces[2]), getServiceUrl("Component3"), "getName");
        log.info("Response : " + response);
        assertTrue(response.toString().contains("<ns:return>org.wso2.carbon.test.component3.Component3-v1.0.0</ns:return>"));

        FeatureInfo featureInfo4 = getFeatureInfo(featureIds[0], featureVersions[1]);
        FeatureInfo featureInfo5 = getFeatureInfo(featureIds[1], featureVersions[1]);
        FeatureInfo featureInfo6 = getFeatureInfo(featureIds[2], featureVersions[1]);
        featureAdminClient.reviewInstallFeaturesAction(new FeatureInfo[]{featureInfo4, featureInfo5, featureInfo6});
        featureAdminClient.performInstallation(featureAdminClient.getInstallActionType());

        serverManager.restartGracefully();
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        featureAdminClient = new FeatureAdminClient(contextUrls.getBackEndUrl(), sessionCookie);

        featureWrappers = featureAdminClient.getInstalledFeatures();
        count = 0;
        for (String featureId : featureIds) {
            for (FeatureWrapper featureWrapper : featureWrappers) {
                if (featureWrapper != null && featureWrapper.getWrappedFeature().getFeatureID().equals(featureId) &&
                        featureWrapper.getWrappedFeature().getFeatureVersion().equals(featureVersions[1])){
                    count++;
                    break;
                }
            }
        }
        assertEquals(count, 3);

        response = axisServiceClient.sendReceive(getPayload(namespaces[0]), getServiceUrl("Component1"), "getName");
        log.info("Response : " + response);
        assertTrue(response.toString().contains("<ns:return>org.wso2.carbon.test.component1.Component1-v1.0.1</ns:return>"));

        response = axisServiceClient.sendReceive(getPayload(namespaces[1]), getServiceUrl("Component2"), "getName");
        log.info("Response : " + response);
        assertTrue(response.toString().contains("<ns:return>org.wso2.carbon.test.component2.Component2-v1.0.1</ns:return>"));

        response = axisServiceClient.sendReceive(getPayload(namespaces[2]), getServiceUrl("Component3"), "getName");
        log.info("Response : " + response);
        assertTrue(response.toString().contains("<ns:return>org.wso2.carbon.test.component3.Component3-v1.0.1</ns:return>"));
    }

    @Test(groups = {"carbon.core"}, description = "Install a feature without license",
            dependsOnMethods = {"testInstallAndUpdateFeature"}, enabled = false)
    public void testInstallAFeatureWithoutLicense() throws Exception {
        FeatureInfo featureInfo = getFeatureInfo("org.wso2.carbon.test.unlicensed-feature1.feature.group", featureVersions[0]);
        featureAdminClient.reviewInstallFeaturesAction(new FeatureInfo[]{featureInfo});

        LicenseFeatureHolder[] licenseFeatureHolders = featureAdminClient.getFeatureLicenseInfo();
        assertNotNull(licenseFeatureHolders);
        assertEquals(licenseFeatureHolders.length, 1);
        assertNull(licenseFeatureHolders[0].getLicenseInfo());

        // TODO: This should not get installed
        //featureAdminClient.performInstallation(featureAdminClient.getInstallActionType());
    }

    @Test(groups = {"carbon.core"}, description = "Install a features with and without license",
            dependsOnMethods = {"testInstallAFeatureWithoutLicense"}, enabled = false)
    public void testInstallAFeatureHavingManyComponentsWithAndWithoutLicense() throws Exception {
        FeatureInfo featureInfo = getFeatureInfo("org.wso2.carbon.test.licensed.unlicensed.feature.feature.group", featureVersions[0]);
        featureAdminClient.reviewInstallFeaturesAction(new FeatureInfo[]{featureInfo});
        LicenseFeatureHolder[] licenseFeatureHolders = featureAdminClient.getFeatureLicenseInfo();

        // TODO: This should be null, as one of the features doesn't have license
        assertNull(licenseFeatureHolders);
    }

    @Test(groups = {"carbon.core"}, description = "Install a features with invalid name",
            dependsOnMethods = {"testInstallAFeatureHavingManyComponentsWithAndWithoutLicense"}, enabled = false)
    public void testInstallNonExistingFeature() throws Exception {
        FeatureInfo featureInfo = getFeatureInfo("org.wso2.carbon.test.non.existing.feature.group", featureVersions[0]);
        featureAdminClient.reviewInstallFeaturesAction(new FeatureInfo[]{featureInfo});

        // TODO: Need to implement this test case once the issue is fixed
    }

    @Test(groups = {"carbon.core"}, description = "Install a features invalid version",
            dependsOnMethods = {"testInstallNonExistingFeature"}, enabled = false)
    public void testInstallAFeatureWithInvalidVersion() throws Exception {
        FeatureInfo featureInfo = getFeatureInfo(featureIds[0], "99.99.99");
        featureAdminClient.reviewInstallFeaturesAction(new FeatureInfo[]{featureInfo});

        // TODO: Need to implement this test case once the issue is fixed
    }

    private FeatureInfo getFeatureInfo(String featureId, String version) {
        FeatureInfo featureInfo = new FeatureInfo();
        featureInfo.setFeatureID(featureId);
        featureInfo.setFeatureVersion(version);
        return featureInfo;
    }

    private OMElement getPayload(String namespace) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(namespace, "ns");
        OMElement getOme = fac.createOMElement("getName", omNs);
        return getOme;
    }
}
