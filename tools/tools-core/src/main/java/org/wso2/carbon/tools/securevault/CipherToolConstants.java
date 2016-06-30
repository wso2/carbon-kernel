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
package org.wso2.carbon.tools.securevault;

/**
 * This class defines the constants used for cipher-tool.
 *
 * @since 5.1.0
 */
public class CipherToolConstants {

    public static final String ENCRYPT_TEXT = "encryptText";
    public static final String CONSOLE_PASSWORD_PARAM = "keystorePassword";
    public static final String UTF8 = "UTF-8";
    public static final String CONF_DIR = "conf";
    public static final String CARBON_CONFIG_FILE = "security.yml"; //todo this should change to carbon.yml
    public static final String SECURITY_DIR = "security";
    public static final String SECRETS_FILE = "secrets.properties";
    public static final String SECRET_PROPERTY_FILE = "secret.conf.properties.file";
    public static final String SECRET_YAML_FILE = "secret-vault.yml";

    /**
     * Constants for Secure-vault.yml configuration.
     */
    public static final class SecureVault {
        public static final String TYPE = "type";
        public static final String LOCATION = "location";

        public static final String SECRET_REPOSITORIES = "secretRepositories";
        public static final String SECRET_REPOSITORY = "repository";
        public static final String SECRET_REPO_FILE_TYPE = "file";
        public static final String SECRET_FILE_PROVIDER = "provider";
        public static final String SECRET_FILE_BASE_PROVIDER_CLASS =
                "org.wso2.carbon.kernel.securevault.FileBaseSecretRepository";

        public static final String KEYSTORE = "keystore";
        public static final String KEYSTORE_ALIAS = "alias";
        public static final String KEYSTORE_STORE_PASSWORD = "password";
    }
}

