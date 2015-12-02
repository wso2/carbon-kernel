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
package org.wso2.carbon.launcher.utils;

import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.bootstrap.logging.BootstrapLogger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Carbon launcher Utils.
 *
 * @since 5.0.0
 */
public class Utils {

    private static final Logger logger = BootstrapLogger.getCarbonLogger(Utils.class.getName());

    private static final String VAR_REGEXP = "\\$\\{[^}]*}";
    private static final Pattern varPattern = Pattern.compile(VAR_REGEXP);

    /**
     * Replace system property holders in the property values.
     * e.g Replace ${carbon.home} with value of the carbon.home system property.
     *
     * @param value System variable value to be replaced
     * @return resolved system property value
     */
    public static String initializeSystemProperties(String value) {
        //TODO this method is duplicated in org.wso2.carbon.kernel.utils.Utils class. FIX IT.
        String newValue = value;
        Matcher matcher = varPattern.matcher(value);
        while (matcher.find()) {
            String sysPropKey = value.substring(matcher.start() + 2, matcher.end() - 1);
            String sysPropValue = getSystemVariableValue(sysPropKey, null);
            if (isNullOrEmpty(sysPropValue)) {
                throw new RuntimeException("System property " + sysPropKey + " cannot be null");
            }

            // Due to reported bug under CARBON-14746
            sysPropValue = sysPropValue.replace("\\", "\\\\");
            newValue = newValue.replaceFirst(VAR_REGEXP, sysPropValue);
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Substitute Variables before: " + value + ", after: " + newValue);
        }

        return newValue;
    }

    /**
     * A utility which allows reading variables from the environment or System properties.
     * If the variable in available in the environment as well as a System property, the System property takes
     * precedence.
     *
     * @param variableName System/environment variable name
     * @param defaultValue default value to be returned if the specified system variable is not specified.
     * @return value of the system/environment variable
     */
    public static String getSystemVariableValue(String variableName, String defaultValue) {
        String value;
        if (System.getProperty(variableName) != null) {
            value = System.getProperty(variableName);
        } else if (System.getenv(variableName) != null) {
            value = System.getenv(variableName);
        } else {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Read repository path from properties.
     *
     * @return repository location path
     */
    public static Path getRepositoryDirectory() {
        return Paths.get(System.getProperty(Constants.CARBON_HOME), Constants.REPOSITORY_DIR_PATH);
    }

    /**
     * Read launch configuration directory path from properties.
     *
     * @return launch configuration directory path
     */
    public static Path getLaunchConfigDirectory() {
        return Paths.get(System.getProperty(Constants.CARBON_HOME), Constants.LAUNCH_CONF_DIR_PATH);
    }

    /**
     * Checks the string value is null or empty.
     *
     * @param arg string value to check for null
     * @return true if value is null or false otherwise
     */
    public static boolean isNullOrEmpty(String arg) {
        return (arg == null || arg.length() == 0);
    }

    /**
     * Tokenize a String with a given delimiter.
     *
     * @param list      String to be tokenize
     * @param delimiter delimiter
     * @return Array of tokens
     */
    public static String[] tokenize(String list, String delimiter) {
        if (isNullOrEmpty(list)) {
            return new String[0];
        }
        List<String> tokenList = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(list, delimiter);
        while (stringTokenizer.hasMoreElements()) {
            String token = stringTokenizer.nextToken().trim();
            if (!isNullOrEmpty(token)) {
                tokenList.add(token);
            }
        }
        return tokenList.toArray(new String[tokenList.size()]);
    }
}
