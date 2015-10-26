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
package org.wso2.carbon.kernel.utils;


import org.wso2.carbon.kernel.Constants;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic Base Utility methods.
 *
 * @since 5.0.0
 */
public class Utils {
    private static final String VAR_REGEXP = "\\$\\{[^}]*}";
    private static final Pattern varPattern = Pattern.compile(VAR_REGEXP);

    /**
     * Remove default constructor and make it not available to initialize.
     */

    private Utils() {
        throw new AssertionError("Instantiating utility class...");

    }

    /**
     * This method will return the carbon configuration directory path.
     * i.e $carbon.home/repository/conf
     *
     * @return returns the Carbon Configuration directory path
     */
    public static Path getCarbonConfigHome() {
        Path configDirPath;
        String carbonRepoDirPath = System.getProperty(Constants.CARBON_REPOSITORY);
        if (carbonRepoDirPath == null) {
            carbonRepoDirPath = System.getenv(Constants.CARBON_REPOSITORY_PATH_ENV);
        }
        if (carbonRepoDirPath == null) {
            configDirPath = Paths.get(getCarbonHome().toString(), "repository", "conf");
        } else {
            configDirPath = Paths.get(carbonRepoDirPath, "conf");
        }
        return configDirPath;
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
     * Replace system property holders in the property values.
     * e.g. Replace ${carbon.home} with value of the carbon.home system property.
     */
    public static String substituteVars(String value) {
        //TODO this method is duplicated in org.wso2.carbon.launcher.utils package. FIX IT.

        String newValue = value;

        Matcher matcher = varPattern.matcher(value);
        while (matcher.find()) {
            String sysPropKey = value.substring(matcher.start() + 2, matcher.end() - 1);
            String sysPropValue = System.getProperty(sysPropKey);
            if (sysPropValue == null || sysPropValue.length() == 0) {
                throw new RuntimeException("System property " + sysPropKey + " cannot be null");
            }
            sysPropValue = sysPropValue.replace("\\", "\\\\");   // Due to reported bug under CARBON-14746
            newValue = newValue.replaceFirst(VAR_REGEXP, sysPropValue);
        }

        return newValue;
    }
}
