/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.utils.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.ServiceDeployer;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.BaseTest;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Ghost Deployer Utils API related test cases.
 */
public class GhostDeployerUtilsTest extends BaseTest {

    @Test(groups = {"org.wso2.carbon.utils.deployment"})
    public void testDeployActualService() throws Exception {
        String serviceName = "Version";
        ConfigurationContext configurationContext = createTestConfigurationContext();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        GhostArtifactRepository ghostArtifactRepository = new GhostArtifactRepository(axisConfiguration);
        axisConfiguration.addParameter(CarbonConstants.GHOST_ARTIFACT_REPOSITORY, ghostArtifactRepository);
        AxisService ghostService = new AxisService(serviceName);
        AxisService actualService = axisConfiguration.getService(serviceName);
        actualService.addParameter(CarbonConstants.GHOST_SERVICE_PARAM, "true");
        axisConfiguration.addService(actualService);
        Deployer deployer = new ServiceDeployer();
        deployer.init(configurationContext);
        DeploymentFileData deploymentFileData = new DeploymentFileData(new File(actualService.getFileName().getPath()),
                deployer);
        ghostArtifactRepository.addDeploymentFileData(deploymentFileData, true);
        AxisService deployedService = GhostDeployerUtils.deployActualService(axisConfiguration, ghostService);
        assert deployedService != null;
        Assert.assertEquals(deployedService.getName(), actualService.getName());
    }

    @Test(groups = {"org.wso2.carbon.utils.deployment"})
    public void testIsPartialUpdateEnabled() throws Exception {
        String serviceName = "Version";
        ConfigurationContext configurationContext = createTestConfigurationContext();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        MessageContext messageContext = new MessageContext();
        messageContext.setConfigurationContext(configurationContext);
        GhostDeployerUtils.addServiceGroupToTransitMap(axisConfiguration.getServiceGroup(serviceName),
                axisConfiguration);
        messageContext.setTo(new EndpointReference("http://wso2.com/axis2/services/Version/getVersion"));
        Assert.assertNotNull(GhostDeployerUtils.dispatchServiceFromTransitGhosts(messageContext));
    }

    @Test(groups = {"org.wso2.carbon.utils.deployment"})
    public void testWaitForServiceToLeaveTransit() throws Exception {
        String serviceName = "Version";
        ConfigurationContext configurationContext = createTestConfigurationContext();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        GhostDeployerUtils.addServiceGroupToTransitMap(axisConfiguration.getServiceGroup(serviceName),
                axisConfiguration);
        Map<String, AxisService> transitGhostMap = GhostDeployerUtils.getTransitGhostServicesMap(axisConfiguration);
        TransitGhostServiceRemovalThread removalThread = new TransitGhostServiceRemovalThread(axisConfiguration,
                serviceName);
        removalThread.start();
        GhostDeployerUtils.waitForServiceToLeaveTransit(serviceName, axisConfiguration);
        Assert.assertFalse(transitGhostMap.containsKey(serviceName));
    }

    private static class TransitGhostServiceRemovalThread extends Thread {

        private final AxisConfiguration axisConfiguration;
        private final String serviceName;

        TransitGhostServiceRemovalThread(AxisConfiguration axisConfiguration, String serviceName) {
            this.axisConfiguration = axisConfiguration;
            this.serviceName = serviceName;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                GhostDeployerUtils.removeServiceGroupFromTransitMap(axisConfiguration.getServiceGroup(serviceName),
                        axisConfiguration);
            } catch (AxisFault | InterruptedException axisFault) {
                //ignore
            }
        }
    }

    @Test(groups = {"org.wso2.carbon.utils.deployment"})
    public void testIsGhostService() throws Exception {
        String serviceName = "Version";
        AxisService service = new AxisService(serviceName);
        Assert.assertFalse(GhostDeployerUtils.isGhostService(service));
        service.addParameter(CarbonConstants.GHOST_SERVICE_PARAM, "true");
        Assert.assertTrue(GhostDeployerUtils.isGhostService(service));
    }

    @Test(groups = {"org.wso2.carbon.utils.deployment"})
    public void testCreateGhostServiceGroup() throws Exception {
        String serviceName = "Version";
        File ghostMetaFile = Paths.get(testDir, "axis2services_Version.xml").toFile();
        URL serviceFile = Paths.get(testDir, "axis2-repo", "services", "Version.aar").toFile().toURI().toURL();
        ConfigurationContext configurationContext = createTestConfigurationContext();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        AxisServiceGroup axisServiceGroup = GhostDeployerUtils.createGhostServiceGroup(axisConfiguration,
                ghostMetaFile, serviceFile);
        Assert.assertNotNull(axisServiceGroup);
        Assert.assertEquals(axisServiceGroup.getServiceGroupName(), serviceName);
        Deployer deployer = new ServiceDeployer();
        deployer.init(configurationContext);
        DeploymentFileData deploymentFileData = new DeploymentFileData(new File(serviceFile.getPath()), deployer);

        GhostArtifactRepository ghostArtifactRepository = new GhostArtifactRepository(axisConfiguration);
        GhostDeployerUtils.setGhostArtifactRepository(ghostArtifactRepository, axisConfiguration);
        ghostArtifactRepository.addDeploymentFileData(deploymentFileData, true);

        GhostDeployerUtils.deployGhostServiceGroup(ghostMetaFile, deploymentFileData, axisConfiguration);
    }

    @Test(groups = {"org.wso2.carbon.utils.deployment"})
    public void testGhostService() throws Exception {
        String serviceName = "Version";
        copyArtifacts();
        ConfigurationContext configurationContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(testSampleDirectory.getAbsolutePath(),
                        Paths.get(testDir, "axis2.xml").toString());
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        File serviceFile = Paths.get(testSampleDirectory.getAbsolutePath(), "services", "Version.aar").toFile();
        Deployer deployer = new ServiceDeployer();
        deployer.init(configurationContext);
        DeploymentFileData deploymentFileData = new DeploymentFileData(serviceFile, deployer);
        GhostArtifactRepository ghostArtifactRepository = new GhostArtifactRepository(axisConfiguration);
        GhostDeployerUtils.setGhostArtifactRepository(ghostArtifactRepository, axisConfiguration);
        ghostArtifactRepository.addDeploymentFileData(deploymentFileData, true);

        Set<AxisService> axisServices = new HashSet<>(axisConfiguration.getServices().values());
        GhostDeployerUtils.serializeServiceGroup(axisServices, deploymentFileData, axisConfiguration);
        String ghostMetafilePath = CarbonUtils.getGhostMetafileDir(axisConfiguration);
        assert ghostMetafilePath != null;
        String ghostMetaFilesPath = Paths.get(ghostMetafilePath, CarbonConstants.GHOST_SERVICES_FOLDER).toString();
        Assert.assertTrue(Paths.get(ghostMetaFilesPath, "services_Version.xml").toFile().exists());
        File ghostMetaFile = GhostDeployerUtils.getGhostFile(serviceFile.getAbsolutePath(), axisConfiguration);
        assert ghostMetaFile != null;
        Assert.assertTrue(ghostMetaFile.exists());

        axisConfiguration.removeServiceGroup(serviceName);
        axisConfiguration.removeService(serviceName);
        GhostDeployerUtils.deployGhostArtifacts(axisConfiguration);

        GhostDeployerUtils.removeGhostFile(serviceFile.getAbsolutePath(), axisConfiguration);
        Assert.assertFalse(ghostMetaFile.exists());
    }

    private void copyArtifacts() throws IOException {
        Path target = Paths.get(testSampleDirectory.getAbsolutePath(), "services");
        target.toFile().mkdirs();
        Path source = Paths.get(testDir, "axis2-repo", "services", "Version.aar");
        Files.copy(source, Paths.get(target.toString(), "Version.aar"), StandardCopyOption.REPLACE_EXISTING);
    }
}
