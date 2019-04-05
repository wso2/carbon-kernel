/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.nextgen.config;

/**
 * Constants for Configuration.
 */
public class ConfigConstants {
    public static final String ENABLE_SEC_VAULT = "secVault.enabled";
    public static final String SECRET_PROPERTY_MAP_NAME = "secrets";

    public static final String SECRETS_SECTION = "[secrets]";
    public static final String SECTION_PREFIX = "[";
    public static final String SECTION_SUFFIX = "]";

    public static final String KEY_VALUE_SEPERATOR = "=";
    public static final String ENCRYPT_SECRETS = "encryptSecrets";

}
