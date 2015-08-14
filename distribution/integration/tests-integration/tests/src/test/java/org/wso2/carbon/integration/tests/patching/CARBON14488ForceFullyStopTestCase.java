/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.integration.tests.patching;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.extensions.servers.carbonserver.CarbonServerManager;
import org.wso2.carbon.automation.extensions.servers.utils.InputStreamHandler;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.PatchApplyingUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * When patch is being processing if server is forcefully shut down there can be a situation
 * where errors can occur. This class tests for this.
 */
public class CARBON14488ForceFullyStopTestCase extends CarbonIntegrationBaseTest {

	private static final Log log = LogFactory.getLog(CARBON14488ForceFullyStopTestCase.class);
	private final int PORT_OFFSET = 10;
	private final String PATCH_VERIFICATION_MESSAGE = "Backed up plugins to patch0000";
	private String carbonHome;
	private AutomationContext context;
	private Process process;

	@BeforeClass(alwaysRun = true)
	public void initTests() throws Exception {
		super.init();
		context = new AutomationContext();

		//Create CarbonServerManager instance to start new AS instance
		CarbonServerManager carbonServerManager = new CarbonServerManager(context);

		//Get AS zip file location
		String carbonZipLocation = System.getProperty("carbon.zip");
		//Extract Carbon pack in to temp directory
		carbonHome = carbonServerManager.setUpCarbonHome(carbonZipLocation);

		PatchApplyingUtil.buildPatch("patch9000", "org.wso2.carbon.utils_", carbonHome);

		process = startServer(PORT_OFFSET);

		readInputStream(process);

		//Delete patch0000 directory and check
		deletePatch("patch0000");

		process = startServer(PORT_OFFSET);

		readInputStream(process);

		//Kill process if not terminated properly earlier
		killProcess();

	}

	@Test(groups = "wso2.as", description = "Read patch log to detect errors")
	public void testForceFullyStop() throws Exception {

		String serverLog = readPatchLogs();
		Assert.assertTrue(!serverLog.contains("Error occurred while applying patches"),
		                  "Error occurred while stopping server when patch applying process is in progress");
	}

	/**
	 * This method create a process to run new AS instance in new port
	 *
	 * @param portOffset - Patch to be copied
	 * @return process -    Process which executes the AS
	 */
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

	/**
	 * This method read and returns content of the patch.log file
	 */
	private String readPatchLogs() throws Exception {
		File patchFile = new File(carbonHome + File.separator + "repository" + File.separator +
		                          "logs" + File.separator + "patches.log");

		return new Scanner(patchFile).useDelimiter("\\A").next();
	}

	/**
	 * This method reads logs of running AS and when patch verification starts this will kill the process
	 * of running AS
	 *
	 * @param process - Process which executes AS
	 */
	private void readInputStream(Process process) throws Exception {
		InputStreamHandler errorStreamHandler = new InputStreamHandler("errorStream", process.getErrorStream());
		InputStreamHandler inputStreamHandler = new InputStreamHandler("inputStream", process.getInputStream());

		// start the stream readers
		inputStreamHandler.start();
		errorStreamHandler.start();

		long time = System.currentTimeMillis() + 20 * 1000;

		String output = inputStreamHandler.getOutput();

		//Time duration is to avoid infinitely running the loop
		while (!output.contains(PATCH_VERIFICATION_MESSAGE) && System.currentTimeMillis() < time) {
			output = inputStreamHandler.getOutput();
		}

		if (output.contains(PATCH_VERIFICATION_MESSAGE)) {
			killProcess();
		}
		process.destroy();
	}

	/**
	 * This method kills the process of running AS instance by finding process id from wso2carbon.pid
	 */
	private void killProcess() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(carbonHome + File.separator + "wso2carbon.pid"));
		String pid = reader.readLine();
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			Runtime.getRuntime().exec("taskkill /pid " + pid);
		} else {
			Runtime.getRuntime().exec("kill -15 " + pid);
		}
	}

	/**
	 * This method deletes a given patch from repository/components/patches directory inside AS.
	 *
	 * @param patch - A patch to be deleted
	 */
	private void deletePatch(String patch) throws IOException {

		File file =
				new File(carbonHome + File.separator + "repository" + File.separator + "components" + File.separator +
				         "patches" + File.separator + patch);
		FileUtils.deleteDirectory(file);
	}

}
