package org.wso2.carbon.integration.tests.patching;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.extensions.servers.carbonserver.CarbonServerManager;
import org.wso2.carbon.automation.extensions.servers.utils.InputStreamHandler;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.tests.common.exception.CarbonToolsIntegrationTestException;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.testng.Assert.fail;

public class CARBON15340CustomMembershipSchemeTestCase extends CarbonIntegrationBaseTest {
    private final int PORT_OFFSET = 10;
    private String carbonHome;
    private AutomationContext context;
    private Process process;

    @BeforeClass(alwaysRun = true) public void initTests() throws Exception {
        super.init();
        context = new AutomationContext();

        //Create CarbonServerManager instance to start new Carbon instance
        CarbonServerManager carbonServerManager = new CarbonServerManager(context);

        //Get Carbon zip file location
        String carbonZipLocation = System.getProperty("carbon.zip");
        //Extract Carbon pack in to temp directory
        carbonHome = carbonServerManager.setUpCarbonHome(carbonZipLocation);

        copyCustomMemberShipSchemeBundleToDropins();

        replaceAxis2XML();

        process = startServer(PORT_OFFSET);

    }

    private void replaceAxis2XML() throws IOException {
        String source = TestConfigurationProvider.getResourceLocation() + File.separator +
                "artifacts" + File.separator + "CARBON" + File.separator + "CARBON15340" + File.separator + "axis2.xml";

        String target = carbonHome + File.separator + "repository" +
                File.separator + "conf" + File.separator + "axis2" + File.separator + "axis2.xml";

        Files.copy(Paths.get(source), Paths.get(target), StandardCopyOption.REPLACE_EXISTING);

    }

    private void copyCustomMemberShipSchemeBundleToDropins() throws IOException, CarbonToolsIntegrationTestException {
        String source = TestConfigurationProvider.getResourceLocation() + File.separator +
                "artifacts" + File.separator + "CARBON" + File.separator + "CARBON15340" +
                File.separator + "org.wso2.membershipScheme.dummy-1.0-SNAPSHOT.jar";

        String target = carbonHome + File.separator + "repository" +
                File.separator + "components" + File.separator + "dropins" + File.separator
                + "org.wso2.membershipScheme.dummy-1.0-SNAPSHOT.jar";

        Files.copy(Paths.get(source), Paths.get(target));
    }

    @Test(groups = "carbon.core", description = "Test custom membership scheme")
    public void verifyPatchApplication() throws Exception {
        InputStreamHandler errorStreamHandler = new InputStreamHandler("errorStream", process.getErrorStream());
        InputStreamHandler inputStreamHandler = new InputStreamHandler("inputStream", process.getInputStream());

        // start the stream readers
        inputStreamHandler.start();
        errorStreamHandler.start();

        long time = System.currentTimeMillis() + 20 * 1000;

        String output = inputStreamHandler.getOutput();

        while (!output.contains("Using dummy based membership management scheme")
                && System.currentTimeMillis() < time) {
            output = inputStreamHandler.getOutput();
        }

        if (!output.contains("Using dummy based membership management scheme")) {
            fail("Custom membership scheme is not being picked up");
        }
    }

    private Process startServer(int portOffset) throws IOException {
        Process process;
        File commandDir = new File(carbonHome + File.separator + "bin");
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            String[] cmdArray;
            cmdArray = new String[] { "cmd.exe", "wso2server.bat", "-DportOffset=" + portOffset };

            process = Runtime.getRuntime().exec(cmdArray, null, commandDir);
        } else {
            String[] cmdArray;
            cmdArray = new String[] { "sh", "wso2server.sh", "-DportOffset=" + portOffset };

            process = Runtime.getRuntime().exec(cmdArray, null, commandDir);
        }
        return process;
    }
}
