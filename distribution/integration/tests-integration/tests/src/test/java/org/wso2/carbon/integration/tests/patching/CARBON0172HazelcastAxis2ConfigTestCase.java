package org.wso2.carbon.integration.tests.patching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.ContextXpathConstants;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.servers.carbonserver.CarbonServerManager;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationConstants;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Providing the support to configure HazelCast clustering directly through HazelCast xml instead of through axis2.xml.
 * This test case is for the above improvement.
 */
public class CARBON0172HazelcastAxis2ConfigTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(CARBON0172HazelcastConfigTestCase.class);
    private static final String OUT_FILE_PATH_SYS_PROP = "hazelcastTesterPath";
    private static final String SERVER_STARTUP_MESSAGE = "Mgt Console URL";
    private static final String SUCCESS_OUT_MSG = "SUCCESS";

    private static final int PORT_OFFSET_1 = 8;
    CarbonServerCtrl carbonServer1Ctrl;

    private static final int PORT_OFFSET_2 = 9;
    CarbonServerCtrl carbonServer2Ctrl;
    Path server2OutPath = null;

    @BeforeClass(alwaysRun = true)
    public void testStartServers() throws Exception {
        String carbonZipLocation = System.getProperty("carbon.zip");

        final Path resultsDir = Files.createTempDirectory(String.valueOf(System.nanoTime()));

        carbonServer1Ctrl = new CarbonServerCtrl(carbonZipLocation);
        copyAxis2Config1(carbonServer1Ctrl.getCarbonHome());
        copyWriteBundle(carbonServer1Ctrl.getCarbonHome());
        Map<String, String> startupParameterMapOne = new HashMap<>();
        startupParameterMapOne.put("-DportOffset", PORT_OFFSET_1 + "");
        carbonServer1Ctrl.startUp(startupParameterMapOne);

        carbonServer2Ctrl = new CarbonServerCtrl(carbonZipLocation);
        copyAxis2Config2(carbonServer2Ctrl.getCarbonHome());
        copyReadBundle(carbonServer2Ctrl.getCarbonHome());
        Map<String, String> startupParameterMapTwo = new HashMap<>();
        startupParameterMapTwo.put("-DportOffset", PORT_OFFSET_2 + "");
        //startupParameterMapTwo.put("cmdArg", "-debug 5006");
        server2OutPath = resultsDir.resolve(UUID.randomUUID().toString());
        startupParameterMapTwo.put("-D" + OUT_FILE_PATH_SYS_PROP, server2OutPath.toString());
        carbonServer2Ctrl.startUp(startupParameterMapTwo);

        for (int i = 0; i < 5; i++) {
            log.info("Waiting for cluster communication..");
            TimeUnit.SECONDS.sleep(2);
        }

        carbonServer1Ctrl.shutDown();
        carbonServer2Ctrl.shutDown();
    }

    @Test(groups = "carbon.core", description = "Test hazelcast based clustering")
    public void test() {
        boolean isTestPassed = false;
        try {
            String output = new String(Files.readAllBytes(server2OutPath));
            if (SUCCESS_OUT_MSG.equals(output)) {
                isTestPassed = true;
            }
        } catch (IOException e) {
        }
        Assert.assertTrue(isTestPassed, "Hazelcast clustering should work");
    }

    private void copyAxis2Config1(String server1) throws IOException {
        Path source = Paths.get(TestConfigurationProvider.getResourceLocation(), "artifacts", "CARBON",
                "patching", "CARBON0172", "axis2only", "node1", "axis2.xml");
        Files.copy(source, Paths.get(server1, "repository", "conf", "axis2", "axis2.xml")
                , StandardCopyOption.REPLACE_EXISTING);
    }

    private void copyAxis2Config2(String server1) throws IOException {
        Path source = Paths.get(TestConfigurationProvider.getResourceLocation(), "artifacts", "CARBON",
                "patching", "CARBON0172", "axis2only", "node2", "axis2.xml");
        Files.copy(source, Paths.get(server1, "repository", "conf", "axis2", "axis2.xml")
                , StandardCopyOption.REPLACE_EXISTING);
    }

    private void copyReadBundle(String server) throws IOException {
        Path source = Paths.get(TestConfigurationProvider.getResourceLocation()
                , "..", "..", "..", "..", "..",
                "artifacts", "carbon-0172-hazelcast-test-bundles", "value-reader", "target", "hazelcast-reader-bundle-1.0.jar").normalize();
        Files.copy(source, Paths.get(server, "repository", "components", "dropins", "hazelcast-reader-bundle-1.0.jar"),
                StandardCopyOption.REPLACE_EXISTING);
    }

    private void copyWriteBundle(String server) throws IOException {
        Path source = Paths.get(TestConfigurationProvider.getResourceLocation()
                , "..", "..", "..", "..", "..",
                "artifacts", "carbon-0172-hazelcast-test-bundles", "value-writer", "target", "hazelcast-writer-bundle-1.0.jar").normalize();
        Files.copy(source, Paths.get(server, "repository", "components", "dropins", "hazelcast-writer-bundle-1.0.jar"),
                StandardCopyOption.REPLACE_EXISTING);
    }

    private class CarbonServerCtrl {

        private final String carbonHome;
        private AutomationContext context;
        private CarbonServerManager serverManager;
        private int portOffset = 0;

        private CarbonServerCtrl(String carbonZipLocation)
                throws XPathExpressionException, IOException, AutomationFrameworkException {
            context = new AutomationContext(CarbonIntegrationConstants.PRODUCT_GROUP,
                    CarbonIntegrationConstants.INSTANCE,
                    ContextXpathConstants.SUPER_TENANT,
                    ContextXpathConstants.SUPER_ADMIN);
            serverManager = new CarbonServerManager(context);
            this.carbonHome = serverManager.setUpCarbonHome(carbonZipLocation);

            System.setProperty("javax.net.ssl.trustStore",
                    Paths.get(this.carbonHome, "repository", "resources", "security", "wso2carbon.jks").toString());
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        }

        public String getCarbonHome() {
            return carbonHome;
        }

        public void shutDown() {
            try {
                serverManager.serverShutdown(portOffset);
            } catch (AutomationFrameworkException e) {
                log.error("Error while shutting down server", e);
            }
        }

        public void startUp(Map<String, String> commandMap)
                throws AutomationFrameworkException, InterruptedException {
            String portOffsetStr = commandMap.get("-DportOffset");
            if (portOffsetStr != null) {
                portOffset = Integer.parseInt(portOffsetStr);
            }
            serverManager.startServerUsingCarbonHome(carbonHome, commandMap);
        }

    }

}
