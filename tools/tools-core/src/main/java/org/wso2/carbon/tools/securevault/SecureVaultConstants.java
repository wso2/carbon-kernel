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
public class SecureVaultConstants {

    public static final String CONFIGURE = "configure";
    public static final String CHANGE = "change";
    public static final String CONSOLE_PASSWORD_PARAM = "password";
    public static final String UTF8 = "UTF-8";
    public static final String CONF_DIR = "conf";
    public static final String CARBON_CONFIG_FILE = "carbon.yml";
    public static final String SECURITY_DIR = "security";
    public static final String SECRETS_FILE = "secrets.properties";
    public static final String SECRET_PROPERTY_FILE = "secret.conf.properties.file";
    public static final String SECRET_YAML_FILE = "secret-vault.yml";

    /**
     * Constants for Secure-vault configuration.
     */
    public static final class SecureVault {
        public static final String ATTRIBUTE = "provider";
        public static final String SECRET_PROVIDER_CLASS =
                "org.wso2.securevault.secret.handler.SecretManagerSecretCallbackHandler";
        public static final String CARBON_DEFAULT_SECRET_PROVIDER =
                "org.wso2.carbon.securevault.DefaultSecretCallbackHandler";
        public static final String ALIAS = "svns:secretAlias";
        public static final String SECRET_REPOSITORIES = "secretRepositories";
        public static final String CARBON_SECRET_PROVIDER = "carbon.secretProvider";
        public static final String SECRET_FILE_PROVIDER = "secretRepositories.file.provider";
        public static final String SECRET_FILE_BASE_PROVIDER_CLASS =
                "org.wso2.carbon.kernel.security.repository.FileBaseSecretRepositoryProvider";
        public static final String SECRET_FILE_LOCATION = "secretRepositories.file.location";
        public static final String KEYSTORE_LOCATION = "keystore.identity.location";
        public static final String KEYSTORE_TYPE = "keystore.identity.type";
        public static final String KEYSTORE_ALIAS = "keystore.identity.alias";
        public static final String KEYSTORE_STORE_PASSWORD = "keystore.identity.store.password";
        public static final String IDENTITY_STORE_PASSWORD = "identity.store.password";
        public static final String KEYSTORE_STORE_SECRET_PROVIDER = "keystore.identity.store.secretProvider";
        public static final String KEYSTORE_KEY_PASSWORD = "keystore.identity.key.password";
        public static final String IDENTITY_KEY_PASSWORD = "identity.key.password";
        public static final String KEYSTORE_KEY_SECRET_PROVIDER = "keystore.identity.key.secretProvider";
    }
}

