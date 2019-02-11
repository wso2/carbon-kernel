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
package org.wso2.carbon.integration.tests.common.utils;

import org.apache.commons.io.FilenameUtils;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.IOException;

/**
 * This utility class contains reusable methods for patch applying
 */
public class PatchApplyingUtil {
	/**
	 * This method will build a jar with different md5sum and build the patch, then copy patch to the repository,
	 * components, patches.
	 *
	 * @param patchName  - Patch name eg: patch0005
	 * @param jarName    - Jar file name without the version and extension (eg: org.wso2.carbon.utils_)
	 *                      this jar file will be used to create the patch
	 * @param carbonHome - The location where the carbon server zip file is extracted
	 * @return fileNameWithExt - Returns the copied jar file name as a patch
	 */
	public static String buildPatch(String patchName, String jarName, String carbonHome) throws IOException {

		String kernelPluggingLocation = carbonHome + File.separator + "repository" + File.separator + "components" +
		                                File.separator + "plugins";
		String kernelPatchLocation = carbonHome + File.separator + "repository" + File.separator + "components" +
		                             File.separator + "patches";

		String tempPatchLocation =
				System.getProperty("basedir", ".") + File.separator + "target" + File.separator + "resources" +
				File.separator + "artifacts" + File.separator + "CARBON" + File.separator + "patches" + File.separator +
				patchName;

		//Get matching jar files from plugins directory
		File[] jarFiles = FileManipulator.getMatchingFiles(kernelPluggingLocation, jarName, ".jar");

		String fileNameWithExt = jarFiles[0].getName();
		String fileNameWithOutExt = FilenameUtils.removeExtension(fileNameWithExt);

		//Extract jar file to temp location
		ArchiveManipulator archiveManipulator = new ArchiveManipulator();
		archiveManipulator.extract(kernelPluggingLocation + File.separator + fileNameWithExt,
		                           tempPatchLocation + File.separator + fileNameWithOutExt);

		//Archive extracted jar as a jar again to change the md5sum
		archiveManipulator.archiveDir(tempPatchLocation + File.separator + fileNameWithExt,
		                              tempPatchLocation + File.separator + fileNameWithOutExt);

		FileManipulator.copyFileToDir(new File(tempPatchLocation + File.separator + fileNameWithExt),
		                              new File(kernelPatchLocation + File.separator + patchName));

		FileManipulator.deleteDir(tempPatchLocation);
		return fileNameWithExt;
	}
}
