/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.kernel.utils;

import java.util.Optional;

/**
 * This class contains utility methods for Strings.
 *
 * @since 5.1.0
 */
public class StringUtils {

    /**
     * Check whether the given {@code String} is null or empty.
     *
     * @param str input string
     * @return true if the given {@code String} is null or empty.
     */
    @SuppressWarnings("unused")
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Check whether the given {@code String} is null of empty after the trim operation.
     *
     * @param str input string.
     * @return 'true' if the given {@code String} is null or empty after trim.
     */
    public static boolean isNullOrEmptyAfterTrim(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Returns non-empty {@code Optional<String>}, if the given {@code String} is non-empty after
     * applying trim() operation.
     *
     * @param str input {@code String}.
     * @return {@code Optional<String>}, if the given {@code String} is non-empty after
     * applying trim() operation. If not an empty {@code Optional<String>} will be returned.
     */
    public static Optional<String> getNonEmptyStringAfterTrim(String str) {
        if (isNullOrEmptyAfterTrim(str)) {
            return Optional.empty();
        }
        return Optional.of(str.trim());
    }


}
