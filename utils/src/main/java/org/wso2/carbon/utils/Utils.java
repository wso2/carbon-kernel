/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.utils;

import java.lang.management.ManagementPermission;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Carbon utility methods.
 *
 * @since 5.0.0
 */
public class Utils {

    /**
     * Remove default constructor and make it not available to initialize.
     */

    private Utils() {
        throw new AssertionError("Instantiating utility class...");
    }

    /**
     * This method will return the carbon configuration directory path.
     * i.e ${carbon.home}/conf
     *
     * @return returns the Carbon Configuration directory path
     */
    public static Path getCarbonConfigHome() {
        return Paths.get(getCarbonHome().toString(), "conf");
    }

    /**
     * Returns the Carbon Home directory path. If {@code carbon.home} system property is not found, gets the
     * {@code CARBON_HOME_ENV} system property value and sets to the carbon home.
     *
     * @return returns the Carbon Home directory path
     */
    public static Path getCarbonHome() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        if (carbonHome == null) {
            carbonHome = System.getenv(Constants.CARBON_HOME_ENV);
            System.setProperty(Constants.CARBON_HOME, carbonHome);
        }
        return Paths.get(carbonHome);
    }

    /**
     * When the java security manager is enabled, the {@code checkSecurity} method can be used to protect/prevent
     * methods being executed by unsigned code.
     */
    public static void checkSecurity() {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            secMan.checkPermission(new ManagementPermission("control"));
        }
    }
}
