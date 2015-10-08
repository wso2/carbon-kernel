/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.launcher.utils;

import org.wso2.carbon.launcher.bootstrap.logging.BootstrapLogger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.carbon.launcher.utils.Constants.CARBON_HOME;

/**
 * Carbon launcher Utils
 */
public class Utils {

    private static final Logger logger = BootstrapLogger.getBootstrapLogger();

    private static final String VAR_REGEXP = "\\$\\{[^}]*}";
    private static final Pattern varPattern = Pattern.compile(VAR_REGEXP);

    /**
     * Replace system property holders in the property values
     * e.g Replace ${carbon.home} with value of the carbon.home system property
     *
     * @param value System variable value to be replaced
     * @return resolved system property value
     */
    public static String substituteVars(String value) {
        String newValue = value;

        Matcher matcher = varPattern.matcher(value);
        while (matcher.find()) {
            String sysPropKey = value.substring(matcher.start() + 2, matcher.end() - 1);
            String sysPropValue = System.getProperty(sysPropKey);
            if (sysPropValue == null || sysPropValue.length() == 0) {
                throw new RuntimeException("System property " + sysPropKey + " cannot be null");
            }
            newValue = newValue.replaceFirst(VAR_REGEXP, sysPropValue);
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Substitute Variables before: " + value + ", after: " + newValue);
        }

        return newValue;
    }

    public static String getRepositoryConfDir() {
        return Paths.get(System.getProperty(CARBON_HOME), Constants.REPOSITORY_CONF_DIR_PATH).toString();
    }

    public static String getRepositoryDir() {
        return Paths.get(System.getProperty(Constants.CARBON_HOME), Constants.REPOSITORY_DIR_PATH).toString();
    }

    public static String getLaunchConfigDir() {
        return Paths.get(System.getProperty(Constants.CARBON_HOME), Constants.LAUNCH_CONF_DIR_PATH).toString();
    }

    public static boolean checkForNullOrEmpty(String arg) {
        return (arg == null || arg.length() == 0);
    }

    public static URL getURLFromString(String arg) {
        File file = new File(arg);
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static String[] tokenize(String list, String delim) {
        if (checkForNullOrEmpty(list)) {
            return new String[0];
        }
        ArrayList<String> tokenList = new ArrayList<String>();
        StringTokenizer stringTokenizer = new StringTokenizer(list, delim);
        while (stringTokenizer.hasMoreElements()) {
            String token = stringTokenizer.nextToken().trim();
            if (!checkForNullOrEmpty(token)) {
                tokenList.add(token);
            }
        }
        return tokenList.toArray(new String[tokenList.size()]);
    }
}
