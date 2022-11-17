/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.logging.correlation.utils;

import org.wso2.carbon.utils.xml.StringUtils;

import java.util.Arrays;

/**
 * Correlation log utility class.
 */
public class CorrelationLogUtil {

    /**
     * Convert comma-separated string into an array.
     *
     * @param string Comma-separated string
     * @return
     */
    public static String[] toArray(String string) {
        if (StringUtils.isEmpty(string)) {
            return new String[0];
        }
        String[] arr = string.split(",");
        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i].trim();
        }
        return arr;
    }

    /**
     * Check if the given component is existing is the components array.
     *
     * @param component Component name
     * @param components Components array
     * @return
     */
    public static boolean isComponentAllowed(String component, String[] components) {
        return Arrays.stream(components).anyMatch(s -> s.equals(component));
    }
}
