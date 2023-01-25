/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.utils.logging.central.logger;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.ServerConfiguration;

import java.util.regex.Pattern;

/**
 * Utils class of central logger.
 */
public class LoggerUtils {

    public static final String MASKING_CHARACTER = "*";
    public static final String ENABLE_LOG_MASKING = "maskingLogsEnabled";
    public static final Pattern LOG_MASKING_PATTERN = Pattern.compile("(?<=.).(?=.)");
    public static boolean isLogMaskingEnable = true;


    /**
     * Get the log masking config value from config file.
     */
    public static boolean getLogMaskingConfigValue() {

        if(ServerConfiguration.getInstance().getProperties(ENABLE_LOG_MASKING).length>0) {
            isLogMaskingEnable =
                    Boolean.parseBoolean(ServerConfiguration.getInstance().getProperties(ENABLE_LOG_MASKING)[0]);
            return isLogMaskingEnable;
        }
        return isLogMaskingEnable;
    }

    /**
     * Util function to mask content.
     *
     * @param content Content that needs to be masked.
     * @return masked content.
     */
    public static String getMaskedContent(String content) {

        if(!isLogMaskingEnable) {
            return content;
        }

        if (StringUtils.isNotEmpty(content)) {
            return LOG_MASKING_PATTERN.matcher(content).replaceAll(MASKING_CHARACTER);
        }
        return content;
    }
}
