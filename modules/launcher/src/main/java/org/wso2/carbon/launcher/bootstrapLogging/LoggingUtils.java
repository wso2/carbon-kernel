/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.launcher.bootstrapLogging;

import org.wso2.carbon.launcher.utils.Constants;

import java.util.logging.Level;

/**
 * Utility class for logging methods
 */
public class LoggingUtils {
    private static Level log4jCarbonLoggingLevel = null;

    /**
     *
     * @param level String value of log4j.properties under log4j.logger.org.wso2
     * @return java.util.logging Level correspond to log4j logLevel
     */
    private static Level getLogLevel(String level) {
        if (level.equalsIgnoreCase(Constants.LOG_LEVEL_OFF)) {
            return Level.OFF;
        } else if (level.equalsIgnoreCase(Constants.LOG_LEVEL_ERROR)) {
            return Level.SEVERE;
        } else if (level.equalsIgnoreCase(Constants.LOG_LEVEL_WARN)) {
            return Level.WARNING;
        } else if (level.equalsIgnoreCase(Constants.LOG_LEVEL_INFO)) {
            return Level.INFO;
        } else if (level.equalsIgnoreCase(Constants.LOG_LEVEL_DEBUG)) {
            return Level.FINE;
        } else {
            return Level.FINER;
        }
    }

    /**
     * Setter Method for log4jCarbonLoggingLevel
     * @param log4jCarbonLoggingLevel
     */
    private static void setLog4jCarbonLoggingLevel(Level log4jCarbonLoggingLevel) {
        LoggingUtils.log4jCarbonLoggingLevel = log4jCarbonLoggingLevel;
    }

}
