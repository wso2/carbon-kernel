/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.base.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.base.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base utility class.
 *
 * @since 5.1.0
 */
public class BaseUtils {
    private static final Logger logger = LoggerFactory.getLogger(BaseUtils.class);

    private BaseUtils() {
    }

    /**
     * A util method to read a given java properties file and return the populated properties objects. This will also
     * substitute any system or environment properties in any property value, if found.
     *
     * @param file the property file to read.
     * @return the populated java properties object.
     */
    public static Properties readProperties(File file) {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            prop.load(fis);
            prop.keySet()
                    .forEach(key -> {
                        String value = substituteVariables((String) prop.get(key));
                        prop.replace(key, value);
                    });
        } catch (IOException e) {
            logger.error("Fail to read properties from file : " + file.getAbsolutePath(), e);
        }
        return prop;
    }

    private static String substituteVariables(String value) {
        Matcher matcher = Pattern.compile("\\$\\{([^}]*)}").matcher(value);
        boolean found = matcher.find();
        if (!found) {
            return value;
        }
        StringBuffer sb = new StringBuffer();
        do {
            String sysPropKey = matcher.group(1);
            String sysPropValue = getSystemVariableValue(sysPropKey);
            if (sysPropValue == null) {
                continue;
            }
            sysPropValue = sysPropValue.replace("\\", "\\\\");
            matcher.appendReplacement(sb, sysPropValue);
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String getSystemVariableValue(String variableName) {
        return Optional.ofNullable(Optional.ofNullable(System.getProperty(variableName))
                .orElseGet(() -> System.getenv(variableName)))
                .orElse(null);
    }

    /**
     * This method will return the carbon configuration directory path.
     * i.e ${carbon.home}/conf. If {@code carbon.home} system property is not found, gets the
     * {@code CARBON_HOME_ENV} system property value and sets to the carbon home.
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
     * @return returns the Carbon Home directory path.
     */
    public static Path getCarbonHome() {
        String carbonHome = Optional.ofNullable(Optional.ofNullable(System.getProperty(Constants.CARBON_HOME))
                .orElseGet(() -> System.getenv(Constants.CARBON_HOME_ENV)))
                .map(carbonEnvHome -> System.setProperty(Constants.CARBON_HOME, carbonEnvHome))
                .orElseThrow(() -> new IllegalStateException("CARBON_HOME system property is not set"));
        return Paths.get(carbonHome);
    }
}
