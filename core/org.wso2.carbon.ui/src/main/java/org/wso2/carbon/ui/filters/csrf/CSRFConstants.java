/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.ui.filters.csrf;

import java.util.regex.Pattern;

public class CSRFConstants {
    public static final String CSRF_TOKEN = "csrftoken";
    public static final String CSRF_TOKEN_PRNG = "SHA1PRNG";

    public static final String METHOD_POST = "POST";

    public static final Pattern HTML_HEAD_PATTERN = Pattern.compile("(?i)</head>");

    public static final class ConfigurationProperties {
        public static final String ENABLED = "Security.CSRFPreventionConfig.CSRFPreventionFilter.Enabled";
        public static final String SKIP_URL_PATTERN =
                "Security.CSRFPreventionConfig.CSRFPreventionFilter.SkipUrlPattern";

        private ConfigurationProperties() {
        }
    }

    public static final class JSTemplateToken {
        public static final String CSRF_TOKEN_NAME = "CSRF_TOKEN_NAME";
        public static final String CSRF_TOKEN_VALUE = "CSRF_TOKEN_VALUE";

        private JSTemplateToken() {
        }
    }
}
