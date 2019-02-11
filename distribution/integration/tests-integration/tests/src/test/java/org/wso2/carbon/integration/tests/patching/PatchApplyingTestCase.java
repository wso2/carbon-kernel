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
/*
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;*/
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
/*import org.wso2.carbon.integration.tests.common.utils.PatchApplyingUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;*/

/**
 * This class tests a patch has been correctly applied or not by comparing md5sum
 * of jars.
 */
public class PatchApplyingTestCase extends CarbonIntegrationBaseTest {
	/*private static final Log log = LogFactory.getLog(PatchApplyingTestCase.class);

	private static final String CARBON_HOME = System.getProperty("carbon.home");
	private ServerConfigurationManager serverConfigurationManager;

	private String[] patches = { "patch9000", "patch9001" };
	private String[] filesToAdd = { "org.wso2.carbon.utils_", "org.wso2.carbon.logging_" };
	private List<String> jarNamesList = new ArrayList<>();
	private ArrayList<String> patchLogContents;

	@BeforeClass(alwaysRun = true)
	public void initTests() throws Exception {
		super.init();
		serverConfigurationManager = new ServerConfigurationManager(automationContext);
		if (patches.length == filesToAdd.length) {
			for (int i = 0; i < patches.length; i++) {
				jarNamesList.add(PatchApplyingUtil.buildPatch(patches[i], filesToAdd[i], CARBON_HOME));
			}
		}

		serverConfigurationManager.restartGracefully();
		super.init();
	}

	@Test(groups = { "wso2.as" }, description = "Compare md5sum of jars inside patches and inside plugins directory")
	public void testCheckSumOfJars() throws Exception {
		for (int i = 0; i < patches.length; i++) {
			//calculate md5sum of jar in a resources/patches
			String md5SumExpected = getMd5Sum(
					CARBON_HOME + File.separator + "repository" + File.separator + "components" + File.separator +
					File.separator + "patches" + File.separator + patches[i] + File.separator + jarNamesList.get(i));
			//calculate md5sum of the same jar after applying as a patch
			String md5SumActual = getMd5Sum(
					CARBON_HOME + File.separator + "repository" + File.separator + "components" + File.separator +
					"plugins" + File.separator + jarNamesList.get(i));
			assertEquals(md5SumActual, md5SumExpected, "Wrong md5sum was returned");
		}
	}

	@Test(groups = { "wso2.as" }, description = "Test patch log")
	public void testPatchLogforPatch9000() throws Exception {

		File file = new File(CARBON_HOME + File.separator + "repository" + File.separator + "logs" + File.separator +
		                     "patches.log");
		String fileContent = FileManager.readFile(file);
		patchLogContents = new ArrayList<>(Arrays.asList(fileContent.split(System.getProperty("line.separator"))));

		boolean statusPatchDetected = false;
		boolean statusPatched = false;
		for (String line : patchLogContents) {
			if (line.contains("New patch available - patch9000")) {
				statusPatchDetected = true;
			}
			if (line.contains("Patched " + jarNamesList.get(0))) {
				statusPatched = true;
				break;
			}
		}
		assertTrue(statusPatchDetected, "New patch 9000 is not detected");
		assertTrue(statusPatched, "Patch 9000 is not applied correctly");
	}

	@Test(groups = { "wso2.as" }, description = "Verify the patch log has been updated properly for patch 9001")
	public void testPatchLogforPatch9001() throws Exception {

		boolean statusPatchDetected = false;
		boolean statusPatched = false;
		for (String line : patchLogContents) {
			if (line.contains("New patch available - patch9001")) {
				statusPatchDetected = true;
			}
			if (line.contains("Patched " + jarNamesList.get(1))) {
				statusPatched = true;
				break;
			}
		}
		assertTrue(statusPatchDetected, "New patch 9001 is not detected");
		assertTrue(statusPatched, "Patch 9001 is not applied correctly");
	}

	@Test(groups = { "wso2.as" }, description = "Compare patch directory names prePatchedDir.txt")
	public void testPrePatchedDirUpdate() throws Exception {

		File file =
				new File(CARBON_HOME + File.separator + "repository" + File.separator + "components" + File.separator +
				         "patches" + File.separator + ".metadata" + File.separator + "prePatchedDir.txt");
		String fileContent = FileManager.readFile(file);
		ArrayList<String> patchDirList =
				new ArrayList<>(Arrays.asList(fileContent.split(System.getProperty("line.separator"))));

		//Check directory names
		for (String patch : patches) {
			assertTrue(patchDirList.contains(patch),
			           "prePatchedDir.txt is not updated correctly after applying patches");
		}
	}

	@Test(groups = { "wso2.as" }, description = "Check prePatchedJARs.txt update")
	public void testPrePatchedJARsUpdate() throws Exception {

		File file =
				new File(CARBON_HOME + File.separator + "repository" + File.separator + "components" + File.separator +
				         "patches" + File.separator + ".metadata" + File.separator + "prePatchedJARs.txt");
		String fileContent = FileManager.readFile(file);
		ArrayList<String> prePatchedJarsList =
				new ArrayList<>(Arrays.asList(fileContent.split(System.getProperty("line.separator"))));
		Map<String, String> md5SumMap = new HashMap<>();

		for (String line : prePatchedJarsList) {
			String[] maps = line.split(":");
			md5SumMap.put(maps[0], maps[1]);
		}

		for (int i = 0; i < patches.length; i++) {
			//Calculate md5sum of jar
			String jarPath =
					CARBON_HOME + File.separator + "repository" + File.separator + "components" + File.separator +
					"patches" + File.separator + patches[i] + File.separator + jarNamesList.get(i);
			//Check in hash map
			String md5SumExpected = getMd5Sum(jarPath);
			String md5SumActual = md5SumMap.get(jarNamesList.get(i));

			assertEquals(md5SumActual, md5SumExpected, "prePatchedJARs.txt is not updated");
		}
	}

	@Test(description = "Update previously applied patch and check md5sum", dependsOnGroups = "wso2.as")
	public void testCheckSumAfterPatchUpdate() throws Exception {

		PatchApplyingUtil.buildPatch("patch9002", "org.wso2.carbon.utils_", CARBON_HOME);
		serverConfigurationManager.restartGracefully();
		super.init();

		//calculate md5sum of jar in a resources/patches
		String md5SumExpected =
				getMd5Sum(CARBON_HOME + File.separator + "repository" + File.separator + "components" + File.separator +
				          File.separator + "patches" + File.separator +
				          "patch9002" + File.separator + jarNamesList.get(0));
		//calculate md5sum of the same jar after applying as a patch
		String md5SumActual =
				getMd5Sum(CARBON_HOME + File.separator + "repository" + File.separator + "components" + File.separator +
				          "plugins" + File.separator + jarNamesList.get(0));

		assertEquals(md5SumActual, md5SumExpected, "Wrong md5sum was returned");

	}

	@AfterClass(alwaysRun = true)
	void destroy() throws Exception {
		for (String patch : patches) {
			deletePatch(patch);
		}
		serverConfigurationManager.restartGracefully();
	}

	*//**
	 * This method return md5Sum of a given file
	 *
	 * @param filePath - Path of the jar file
	 * @return md5Sum - md5Sum of the given file
	 *//*
	private String getMd5Sum(String filePath) throws IOException {
		String md5Sum = "";
		//Try with resources. Will auto close file input stream
		try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
			md5Sum = DigestUtils.md5Hex(fileInputStream);
		}
		return md5Sum;
	}

	*//**
	 * This method deletes a given patch from repository/components/patches directory inside AS.
	 *
	 * @param patch - A patch to be deleted
	 *//*
	private void deletePatch(String patch) throws IOException {

		File file =
				new File(CARBON_HOME + File.separator + "repository" + File.separator + "components" + File.separator +
				         "patches" + File.separator + patch);
		FileUtils.deleteDirectory(file);
	}*/
}
