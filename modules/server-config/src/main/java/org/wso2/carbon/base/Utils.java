/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.wso2.carbon.base;

import java.io.File;

/**
 * Generic Base Utility methods
 */
public class Utils {

	/**
	 * Remove default constructor and make it not available to initialize.
	 */

	private Utils() {
		throw new AssertionError("Instantiating utility class...");

	}

	public static String getServerXml() {
		String carbonXML = System
				.getProperty(Constants.CARBON_REPOSITORY);
		/*
		 * if user set the system property telling where is the configuration
		 * directory
		 */
		if (carbonXML == null) {
			return getCarbonConfigDirPath() + File.separator + "carbon.xml";
		}
		return carbonXML + File.separator + "carbon.xml";
	}

	public static String getCarbonConfigDirPath() {
		String carbonConfigDirPath = System
				.getProperty(Constants.CARBON_REPOSITORY);
		if (carbonConfigDirPath == null) {
			carbonConfigDirPath = System
					.getenv(Constants.CARBON_REPOSITORY_PATH_ENV);
			if (carbonConfigDirPath == null) {
				return getCarbonHome() + File.separator + "repository"
						+ File.separator + "conf";
			}
		}
		return carbonConfigDirPath;
	}

	public static String getCarbonHome() {
		String carbonHome = System.getProperty(Constants.CARBON_HOME);
		if (carbonHome == null) {
			carbonHome = System.getenv(Constants.CARBON_HOME_ENV);
			System.setProperty(Constants.CARBON_HOME, carbonHome);
		}
		return carbonHome;
	}
}
