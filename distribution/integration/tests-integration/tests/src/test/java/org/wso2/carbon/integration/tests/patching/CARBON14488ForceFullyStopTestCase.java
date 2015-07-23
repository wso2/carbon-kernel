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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.enums.OperatingSystems;
import org.wso2.carbon.automation.extensions.servers.utils.ServerLogReader;
import org.wso2.carbon.integration.tests.common.exception.CarbonToolsIntegrationTestException;
import org.wso2.carbon.integration.tests.common.utils.CarbonCommandToolsUtil;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.PatchApplyingUtil;
import org.wso2.carbon.integration.tests.integration.test.servers.CarbonTestServerManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * This test is for the fix given in issue CARBON-14488
 * Server gives a IOException when server stopped forcefully while patches are being process
 */
public class CARBON14488ForceFullyStopTestCase extends CarbonIntegrationBaseTest {

	private final int PORT_OFFSET = 1;
	private final String PATCH_VERIFICATION_MESSAGE = "Backed up plugins to patch0000";
	private String carbonHome;
	private Process process;

	@BeforeClass(alwaysRun = true)
	public void initTests() throws Exception {
		super.init();

		if (CarbonTestServerManager.isServerRunning()) {
			carbonHome = CarbonTestServerManager.getCarbonHome();
			CarbonTestServerManager.stop();
		} else {
			CarbonTestServerManager.start(PORT_OFFSET);
			carbonHome = CarbonTestServerManager.getCarbonHome();
			CarbonTestServerManager.stop();
		}
		PatchApplyingUtil.buildPatch("patch9000", "org.wso2.carbon.utils_", carbonHome);

		process = startServer(PORT_OFFSET);

		interruptPatchApply(process);

		//Delete patch0000 directory and again do a check
		deletePatch("patch0000");

		process = startServer(PORT_OFFSET);

		interruptPatchApply(process);

	}

	@Test(groups = "wso2.as", description = "Read patch log to detect errors")
	public void testForceFullyStop() throws Exception {

		String serverLog = readPatchLogs();
		Assert.assertTrue(!serverLog.contains("Error occurred while applying patches"),
		                  "Error occurred while stopping server when patch applying process is in progress");
	}

	@AfterClass(alwaysRun = true)
	public void stopServer() throws Exception {
		if (process != null) {
			killProcess();
		}
	}

	/**
	 * This method create a process to run new Server instance in new port
	 *
	 * @param portOffset - Patch to be copied
	 * @return process -   Process which executes the Server
	 */
	private Process startServer(int portOffset) throws CarbonToolsIntegrationTestException {
		Process process;
		String[] cmdArray;

		if ((CarbonCommandToolsUtil.getCurrentOperatingSystem()
		                           .contains(OperatingSystems.WINDOWS.name().toLowerCase()))) {
			cmdArray = new String[] { "cmd.exe", "wso2server.bat", "-DportOffset=" + portOffset };
		} else {
			cmdArray = new String[] { "sh", "wso2server.sh", "-DportOffset=" + portOffset };
		}
		process = CarbonCommandToolsUtil.runScript(carbonHome + File.separator + "bin", cmdArray);
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
	 * This method reads logs of running Server and when patch verification starts this will kill the process
	 * of running Server
	 *
	 * @param process - Process which executes the Server
	 */
	private void interruptPatchApply(Process process) throws IOException {
		ServerLogReader errorStreamHandler = new ServerLogReader("errorStream", process.getErrorStream());
		ServerLogReader inputStreamHandler = new ServerLogReader("inputStream", process.getInputStream());

		// start the stream readers
		inputStreamHandler.start();
		errorStreamHandler.start();

		long time = System.currentTimeMillis() + 20 * 1000;

		String output = inputStreamHandler.getOutput();

		//Time duration is to avoid infinitely running the loop
		while (!output.contains(PATCH_VERIFICATION_MESSAGE) && System.currentTimeMillis() < time) {
			output = inputStreamHandler.getOutput();
		}

		try {
			//Kill the process in the middle of the patch applying process
			if (output.contains(PATCH_VERIFICATION_MESSAGE)) {
				killProcess();
			}
		} finally {
			process.destroy();
			inputStreamHandler.stop();
			errorStreamHandler.stop();
		}
	}

	/**
	 * This method kills the process of running Server instance by finding process id from wso2carbon.pid
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
	 * This method deletes a given patch from repository/components/patches directory.
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