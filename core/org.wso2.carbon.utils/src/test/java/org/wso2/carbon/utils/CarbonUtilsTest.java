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
package org.wso2.carbon.utils;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.ServiceDeployer;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Header;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.BaseTest;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.component.xml.config.DeployerConfig;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Test class for CarbonUtils API usage.
 */
@Test(dependsOnGroups = {"org.wso2.carbon.utils.deployment"})
public class CarbonUtilsTest extends BaseTest {

    @Test(groups = {"org.wso2.carbon.utils.base"})
    public void testGetServerConfiguration() {
        Assert.assertNotNull(CarbonUtils.getServerConfiguration());
        String carbonConfigHome = Paths.get(testDir).toString();
        System.setProperty(ServerConstants.CARBON_CONFIG_DIR_PATH, carbonConfigHome);
        Assert.assertEquals(CarbonUtils.getServerConfiguration().getFirstProperty("Version"), "4.4.18");
        System.clearProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetServerConfiguration")
    public void testIsAdminConsoleEnabled() throws Exception {
        ServerConfiguration.getInstance().overrideConfigurationProperty("Management.EnableConsole", null);
        Assert.assertFalse(CarbonUtils.isAdminConsoleEnabled());
        String serverConfigPath = Paths.get(testDir, "carbon.xml").toString();
        ServerConfiguration.getInstance().forceInit(serverConfigPath, false);
        Assert.assertTrue(CarbonUtils.isAdminConsoleEnabled());
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testIsAdminConsoleEnabled")
    public void testGetTransportPort() throws Exception {
        String httpTransport = "http";
        String httpsTransport = "https";
        System.clearProperty(httpTransport);
        ConfigurationContext configurationContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(Paths.get(testDir).toString(),
                        Paths.get(testDir, "axis2.xml").toString());
        Assert.assertEquals(-1, CarbonUtils.getTransportPort(configurationContext, null));
        System.setProperty(httpTransport + "Port", String.valueOf(1234));
        Assert.assertEquals(1234, CarbonUtils.getTransportPort(configurationContext, httpTransport));

        Assert.assertEquals(9443, CarbonUtils.getTransportPort(configurationContext.getAxisConfiguration(),
                httpsTransport));

    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetTransportPort")
    public void testGetTransportProxyPort() throws Exception {
        String httpTransport = "http";
        String httpsTransport = "https";
        ConfigurationContext configurationContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(Paths.get(testDir).toString(),
                        Paths.get(testDir, "axis2.xml").toString());
        Assert.assertEquals(-1, CarbonUtils.getTransportProxyPort(configurationContext, httpTransport));
        Assert.assertEquals(-1, CarbonUtils.getTransportProxyPort(configurationContext, httpsTransport));
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetTransportProxyPort")
    public void testGetAxis2Xml() throws Exception {
        String axis2XmlPath = Paths.get(testSampleDirectory.getAbsolutePath(), "repository", "conf", "axis2",
                "axis2.xml").toString();
        Assert.assertEquals(axis2XmlPath, CarbonUtils.getAxis2Xml());
        ServerConfiguration.getInstance().overrideConfigurationProperty("Axis2Config.ConfigurationFile", null);
        System.setProperty(Constants.AXIS2_CONF, "custom/axis2.xml");
        Assert.assertEquals("custom/axis2.xml", CarbonUtils.getAxis2Xml());
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetAxis2Xml")
    public void testGetAxis2XRepo() throws Exception {
        String axis2Repo = "repository/conf/axis2";
        System.setProperty(ServerConstants.AXIS2_REPO, axis2Repo);
        Assert.assertEquals(axis2Repo, CarbonUtils.getAxis2Repo());
        System.clearProperty(ServerConstants.AXIS2_REPO);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetAxis2XRepo")
    public void testGetRegistryXMLPath() throws Exception {
        String registryXmlPath = Paths.get(testSampleDirectory.getAbsolutePath(), "repository", "conf", "registry.xml").
                toString();
        Assert.assertEquals(registryXmlPath, CarbonUtils.getRegistryXMLPath());
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetRegistryXMLPath")
    public void testGetUserMgtXMLPath() throws Exception {
        String userMgtXmlPath = Paths.get(testSampleDirectory.getAbsolutePath(), "repository", "conf", "user-mgt.xml").
                toString();
        Assert.assertEquals(userMgtXmlPath, CarbonUtils.getUserMgtXMLPath());
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetUserMgtXMLPath")
    public void testIsURL() throws Exception {
        String url = "https://www.google.com";
        Assert.assertTrue(CarbonUtils.isURL(url));
        Assert.assertFalse(CarbonUtils.isURL("blah"));
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testIsURL")
    public void testLastUpdatedTimeOfAxis2Service() throws Exception {
        ConfigurationContext configurationContext = createTestConfigurationContext();
        Assert.assertNotNull(CarbonUtils.lastUpdatedTime(configurationContext.getAxisConfiguration().
                getServiceGroup("Version")));
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testLastUpdatedTimeOfAxis2Service")
    public void testComputeServiceHashOfAxis2Service() throws Exception {
        ConfigurationContext configurationContext = createTestConfigurationContext();
        Assert.assertNotNull(CarbonUtils.computeServiceHash(configurationContext.getAxisConfiguration().
                getServiceGroup("Version")));
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testComputeServiceHashOfAxis2Service")
    public void testGetTmpDir() {
        try {
            String tmpDir = testSampleDirectory.getAbsolutePath();
            System.setProperty("java.io.tmpdir", tmpDir);
            Assert.assertEquals(tmpDir, CarbonUtils.getTmpDir());
        } finally {
            System.clearProperty("java.io.tmpdir");
        }
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetTmpDir")
    public void testGetTenantTmpDir() throws Exception {
        String tmpDir = testSampleDirectory.getAbsolutePath();
        System.setProperty("java.io.tmpdir", tmpDir);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("testTenant");
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(123);
        Assert.assertEquals(Paths.get(tmpDir, "tenants", "123").toString(),
                CarbonUtils.getTenantTmpDirPath(createTestConfigurationContext().getAxisConfiguration()));
        PrivilegedCarbonContext.destroyCurrentContext();
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetTenantTmpDir")
    public void testGetCommandListenerPort() throws Exception {
        String serverConfigPath = Paths.get(testDir, "carbon.xml").toString();
        ServerConfiguration.getInstance().forceInit(serverConfigPath);
        Assert.assertEquals(123, CarbonUtils.getCommandListenerPort());
        ServerConfiguration.getInstance().overrideConfigurationProperty("Ports.CommandListener", null);
        Assert.assertEquals(-1, CarbonUtils.getCommandListenerPort());
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetCommandListenerPort")
    public void testGetCarbonCatalinaHome() throws Exception {
        String carbonCatalinaHome = Paths.get(testSampleDirectory.getAbsolutePath(), "lib", "tomcat", "work",
                "Catalina").toString();
        Assert.assertEquals(carbonCatalinaHome, CarbonUtils.getCarbonCatalinaHome());
        System.setProperty(ServerConstants.CARBON_CATALINA_HOME, testDir);
        Assert.assertEquals(testDir, CarbonUtils.getCarbonCatalinaHome());
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetCarbonCatalinaHome")
    public void testGetCarbonTenantsDirPath() throws Exception {
        String carbonTenantDirPath = Paths.get(testSampleDirectory.getAbsolutePath(), "repository", "tenants").toString();
        Assert.assertEquals(carbonTenantDirPath, CarbonUtils.getCarbonTenantsDirPath());
        System.setProperty(ServerConstants.CARBON_TENANTS_DIR_PATH, testDir);
        Assert.assertEquals(testDir, CarbonUtils.getCarbonTenantsDirPath());
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetCarbonTenantsDirPath")
    public void testGetCarbonConfigDirPath() throws Exception {
        String carbonConfigHome = Paths.get(testSampleDirectory.getAbsolutePath(), "repository", "conf").toString();
        Assert.assertEquals(carbonConfigHome, CarbonUtils.getCarbonConfigDirPath());
        System.setProperty(ServerConstants.CARBON_CONFIG_DIR_PATH, testDir);
        Assert.assertEquals(testDir, CarbonUtils.getCarbonConfigDirPath());
        System.clearProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetCarbonConfigDirPath")
    public void testGetEtcCarbonConfigDirPath() throws Exception {
        String carbonEtcHome = Paths.get(testSampleDirectory.getAbsolutePath(), "repository", "conf", "etc").toString();
        Assert.assertEquals(carbonEtcHome, CarbonUtils.getEtcCarbonConfigDirPath());
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetEtcCarbonConfigDirPath")
    public void testGetCarbonSecurityConfigDirPath() throws Exception {
        String carbonSecurityHome = Paths.get(testSampleDirectory.getAbsolutePath(), "repository", "conf", "security").
                toString();
        Assert.assertEquals(carbonSecurityHome, CarbonUtils.getCarbonSecurityConfigDirPath());
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetCarbonSecurityConfigDirPath")
    public void testGetCarbonLogsPath() throws Exception {
        String carbonLogsHome = Paths.get(testSampleDirectory.getAbsolutePath(), "repository", "logs").toString();
        Assert.assertEquals(carbonLogsHome, CarbonUtils.getCarbonLogsPath());
        System.setProperty(ServerConstants.CARBON_LOGS_PATH, testDir);
        Assert.assertEquals(testDir, CarbonUtils.getCarbonLogsPath());
        System.clearProperty(ServerConstants.CARBON_LOGS_PATH);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetCarbonLogsPath")
    public void testGetCarbonPluginsRepo() throws Exception {
        String carbonPluginsHome = Paths.get(testSampleDirectory.getAbsolutePath(), "repository", "components",
                "plugins").toString();
        Assert.assertEquals(CarbonUtils.getComponentsRepo(), carbonPluginsHome);
        System.setProperty(ServerConstants.COMPONENT_REP0, testDir);
        Assert.assertEquals(CarbonUtils.getComponentsRepo(), testDir);
        System.clearProperty(ServerConstants.COMPONENT_REP0);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetCarbonPluginsRepo")
    public void testGetCarbonDropinsRepo() throws Exception {
        String carbonDropinsHome = Paths.get(testSampleDirectory.getAbsolutePath(), "repository", "components",
                "dropins").toString();
        Assert.assertEquals(CarbonUtils.getCarbonOSGiDropinsDir(), carbonDropinsHome);
        System.setProperty(CarbonBaseConstants.CARBON_DROPINS_DIR_PATH, testDir);
        Assert.assertEquals(CarbonUtils.getCarbonOSGiDropinsDir(), testDir);
        System.clearProperty(CarbonBaseConstants.CARBON_DROPINS_DIR_PATH);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetCarbonDropinsRepo")
    public void testRegisterFaultyService() throws Exception {
        String serviceArtifactPath = Paths.get(testDir, "axis2-repo", "Echo.aar").toString();
        ConfigurationContext configurationContext = createTestConfigurationContext();
        CarbonUtils.registerFaultyService(serviceArtifactPath, "Axis2", configurationContext);
        AxisService faultyService = CarbonUtils.getFaultyService(serviceArtifactPath, configurationContext);
        assert faultyService != null;
        Assert.assertEquals(faultyService.getName(), "Echo");
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testRegisterFaultyService")
    public void testGetRegistryTypeFromServerConfig() throws Exception {
        Assert.assertFalse(CarbonUtils.isRemoteRegistry());
        ServerConfiguration.getInstance().overrideConfigurationProperty("Registry.Type", "remote");
        Assert.assertTrue(CarbonUtils.isRemoteRegistry());
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetRegistryTypeFromServerConfig")
    public void testGetCarbonRepository() throws Exception {
        String carbonRepoLocation = Paths.get(testDir, "axis2-repo").toString();
        Assert.assertEquals(CarbonUtils.getCarbonRepository(), carbonRepoLocation);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetCarbonRepository")
    public void testIsMasterOrChildNode() throws Exception {
        Assert.assertFalse(CarbonUtils.isMasterNode());
        Assert.assertFalse(CarbonUtils.isChildNode());
        Assert.assertFalse(CarbonUtils.isMultipleInstanceCase());
        Assert.assertFalse(CarbonUtils.isRunningInStandaloneMode());
        System.setProperty("instance", "true");
        Assert.assertTrue(CarbonUtils.isChildNode());
        Assert.assertTrue(CarbonUtils.isReadOnlyNode());
        System.setProperty("master", "true");
        Assert.assertTrue(CarbonUtils.isMasterNode());
        Assert.assertFalse(CarbonUtils.isReadOnlyNode());
        System.setProperty("instances.value", "2");
        Assert.assertTrue(CarbonUtils.isMultipleInstanceCase());
        System.setProperty(ServerConstants.STANDALONE_MODE, "true");
        Assert.assertTrue(CarbonUtils.isRunningInStandaloneMode());
        System.clearProperty("instance");
        System.clearProperty("master");
        System.clearProperty("instances.value");
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testIsMasterOrChildNode")
    public void testUseRegistryBasedRepository() throws Exception {
        Assert.assertFalse(CarbonUtils.useRegistryBasedRepository());
        System.setProperty("carbon.use.registry.repo", "true");
        Assert.assertTrue(CarbonUtils.useRegistryBasedRepository());
        System.clearProperty("carbon.use.registry.repo");
    }

    @Test(groups = {"org.wso2.carbon.utils.base"})
    public void testGetServerURL() throws Exception {
        ConfigurationContext configurationContext = createTestConfigurationContext();
        Assert.assertEquals(CarbonUtils.getServerURL(null, configurationContext), "local://services/");
        ServerConfiguration.getInstance().overrideConfigurationProperty("WebContextRoot", "/test");
        Assert.assertEquals(CarbonUtils.getServerURL(null, configurationContext), "local://test/services/");

        String urlForSession = "https://wso2.com/services/from/session";
        String urlForServlet = "https://wso2.com/services/from/servlet";

        HttpSession httpSession = Mockito.mock(HttpSession.class);
        ServletContext servletContext = Mockito.mock(ServletContext.class);

        when(servletContext.getAttribute(CarbonConstants.SERVER_URL)).thenReturn(urlForServlet);
        Assert.assertEquals(CarbonUtils.getServerURL(servletContext, httpSession, configurationContext), urlForServlet);

        when(httpSession.getAttribute(CarbonConstants.SERVER_URL)).thenReturn(urlForSession);
        Assert.assertEquals(CarbonUtils.getServerURL(servletContext, httpSession, configurationContext), urlForSession);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetServerURL")
    public void testGetBackendHttpPort() throws Exception {
        ConfigurationContext configurationContext = createTestConfigurationContext();
        Assert.assertEquals("9763", CarbonUtils.getBackendHttpPort(configurationContext));
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetBackendHttpPort")
    public void testGetPortFromServerConfig() {
        int port = 10389;
        int portOffSet = 10;
        String portProperty = "${Ports.EmbeddedLDAP.LDAPServerPort}";
        Assert.assertEquals(CarbonUtils.getPortFromServerConfig(portProperty), port);
        System.setProperty("portOffset", String.valueOf(portOffSet));
        Assert.assertEquals(CarbonUtils.getPortFromServerConfig(portProperty),
                port + portOffSet);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetPortFromServerConfig")
    public void testIsFilteredOutAxis2Service() throws Exception {
        ConfigurationContext configurationContext = createTestConfigurationContext();
        AxisService axisService = configurationContext.getAxisConfiguration().getService("Version");
        Assert.assertFalse(CarbonUtils.isFilteredOutService(axisService));
        axisService.addParameter(CarbonConstants.ADMIN_SERVICE_PARAM_NAME, "true");
        Assert.assertTrue(CarbonUtils.isFilteredOutService(axisService));
        axisService.removeParameter(new Parameter(CarbonConstants.ADMIN_SERVICE_PARAM_NAME, "true"));
        axisService.addParameter(CarbonConstants.HIDDEN_SERVICE_PARAM_NAME, "true");
        Assert.assertTrue(CarbonUtils.isFilteredOutService(axisService));
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testIsFilteredOutAxis2Service")
    public void testGetAxis2ServicesDirectory() throws Exception {
        String serviceRepo = "axis2-service-repo";
        AxisConfiguration axisConfiguration = createTestConfigurationContext().getAxisConfiguration();
        axisConfiguration.addParameter(DeploymentConstants.SERVICE_DIR_PATH, serviceRepo);
        Assert.assertEquals(CarbonUtils.getAxis2ServicesDir(axisConfiguration), serviceRepo);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetAxis2ServicesDirectory")
    public void testSetBasicAccessSecurityHeadersUsingServiceClient() throws Exception {
        ServiceClient serviceClient = new ServiceClient();
        String userName = "user";
        String password = "pass";
        String userNamePassword = userName + ":" + password;
        String encodedString = Base64Utils.encode(userNamePassword.getBytes(StandardCharsets.UTF_8));
        CarbonUtils.setBasicAccessSecurityHeaders("user", "pass", true, serviceClient);
        List<Header> headers = (List<Header>) serviceClient.getOptions().getProperty(HTTPConstants.HTTP_HEADERS);
        assert headers != null;
        Assert.assertTrue(headers.contains(new Header("Authorization", "Basic " + encodedString)));
    }

    @Test(groups = {"org.wso2.carbon.utils.base"},
            dependsOnMethods = "testSetBasicAccessSecurityHeadersUsingServiceClient")
    public void testSetBasicAccessSecurityHeadersUsingMessageContext() throws Exception {
        MessageContext messageContext = new MessageContext();
        String userName = "user";
        String password = "pass";
        String userNamePassword = userName + ":" + password;
        String encodedString = Base64Utils.encode(userNamePassword.getBytes(StandardCharsets.UTF_8));
        CarbonUtils.setBasicAccessSecurityHeaders("user", "pass", true, messageContext);
        List<Header> headers = (List<Header>) messageContext.getOptions().getProperty(HTTPConstants.HTTP_HEADERS);
        assert headers != null;
        Assert.assertTrue(headers.contains(new Header("Authorization", "Basic " + encodedString)));
    }

    @Test(groups = {"org.wso2.carbon.utils.base"},
            dependsOnMethods = "testSetBasicAccessSecurityHeadersUsingMessageContext")
    public void testAddCAppDeployer() throws Exception {
        AxisConfiguration axisConfiguration = createTestConfigurationContext().getAxisConfiguration();
        DeployerConfig[] deployerConfigs = new DeployerConfig[0];
        DeployerConfig[] newDeployerConfigs = CarbonUtils.addCappDeployer(deployerConfigs, axisConfiguration);
        Assert.assertTrue(newDeployerConfigs.length > 0);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testAddCAppDeployer",
            dependsOnGroups = {"org.wso2.carbon.utils.logging"})
    public void testReplaceSystemVariablesInXml() throws IOException, CarbonException, ServerConfigurationException {
        String serverRoleName = "CarbonTestServer";
        Path serverConfigPath = Paths.get(testDir, "carbon.xml");
        Assert.assertNotNull(CarbonUtils.replaceSystemVariablesInXml(new String(Files.readAllBytes(serverConfigPath),
                StandardCharsets.UTF_8)));
        System.setProperty("product.key", "Carbon");
        System.setProperty("carbon.server.role", serverRoleName);
        System.setProperty("rmi.server.port", "11111");
        String serverConfig = CarbonUtils.replaceSystemVariablesInXml(new String(Files.readAllBytes(serverConfigPath),
                StandardCharsets.UTF_8));
        ServerConfiguration.getInstance().forceInit(new ByteArrayInputStream(serverConfig.
                getBytes(StandardCharsets.UTF_8)));
        Assert.assertEquals(ServerConfiguration.getInstance().getFirstProperty("ServerRoles.Role"), serverRoleName);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testReplaceSystemVariablesInXml")
    public void testIsDepSyncEnabled() throws ServerConfigurationException {
        String serverConfigPath = Paths.get(testDir, "carbon.xml").toString();
        ServerConfiguration.getInstance().forceInit(serverConfigPath);
        Assert.assertFalse(CarbonUtils.isDepSyncEnabled());
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testIsDepSyncEnabled")
    public void testGetGhostMetafileDir() throws AxisFault {
        ConfigurationContext configurationContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(testSampleDirectory.getAbsolutePath(),
                        Paths.get(testDir, "axis2.xml").toString());
        String ghostMetaArtifactsPath = Paths.get(configurationContext.getAxisConfiguration().getRepository().getPath(),
                "ghostmetafiles").toString();
        Assert.assertEquals(CarbonUtils.getGhostMetafileDir(configurationContext.getAxisConfiguration()),
                ghostMetaArtifactsPath);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetGhostMetafileDir")
    public void testGetDeployer() throws CarbonException {
        Deployer deployer;
        try {
            CarbonUtils.getDeployer("com.fake.Deployer");
        } catch (CarbonException e) {
            Assert.assertTrue(e.getMessage().contains("Deployer class not found"));
        }
        try {
            CarbonUtils.getDeployer(MockDeployer.class.getName());
        } catch (CarbonException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot create new deployer instance"));
        }
        deployer = CarbonUtils.getDeployer("org.apache.axis2.deployment.ServiceDeployer");
        Assert.assertTrue(deployer instanceof ServiceDeployer);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"}, dependsOnMethods = "testGetGhostMetafileDir")
    public void testGetProxyContextPath() {
        String workerProxyContextPath = "/appserver/worker";
        String managerProxyContextPath = "/appserver/mgt";
        Assert.assertEquals(CarbonUtils.getProxyContextPath(false), managerProxyContextPath);
        Assert.assertEquals(CarbonUtils.getProxyContextPath(true), workerProxyContextPath);
    }

    @Test(groups = {"org.wso2.carbon.utils.base"})
    public void testArrayCopyOf() {
        Integer[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        Assert.assertEquals(CarbonUtils.arrayCopyOf(numbers), numbers);
    }
}

