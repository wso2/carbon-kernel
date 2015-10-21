/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.base;


import java.nio.file.Paths;

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

    /**
     * @return returns the Carbon Configuration directory path
     */
    public static String getCarbonConfigDirPath() {
        String configDirPath;
        String carbonRepoDirPath = System.getProperty(Constants.CARBON_REPOSITORY);
        if (carbonRepoDirPath == null) {
            carbonRepoDirPath = System.getenv(Constants.CARBON_REPOSITORY_PATH_ENV);
        }
        if (carbonRepoDirPath == null) {
            configDirPath = Paths.get(getCarbonHome(), "repository", "conf").toString();
        } else {
            configDirPath = Paths.get(carbonRepoDirPath, "conf").toString();
        }
        return configDirPath;
    }

    /**
     * Returns the Carbon Home directory path. If {@code carbon.home} system property is not found, gets the
     * {@code CARBON_HOME_ENV} system property value and sets to the carbon home.
     *
     * @return returns the Carbon Home directory path
     */
    public static String getCarbonHome() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        if (carbonHome == null) {
            carbonHome = System.getenv(Constants.CARBON_HOME_ENV);
            System.setProperty(Constants.CARBON_HOME, carbonHome);
        }
        return carbonHome;
    }
}
