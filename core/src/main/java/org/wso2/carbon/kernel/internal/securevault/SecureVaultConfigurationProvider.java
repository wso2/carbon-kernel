/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.kernel.internal.securevault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.securevault.SecureVaultUtils;
import org.wso2.carbon.kernel.securevault.config.model.SecureVaultConfiguration;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.nio.file.Paths;

/**
 * This class takes care of parsing the secure-vault.yaml file and creating the SecureVaultConfiguration object model.
 *
 * @since 5.2.0
 */
public class SecureVaultConfigurationProvider {
    private static final Logger logger = LoggerFactory.getLogger(SecureVaultConfiguration.class);
    private static final SecureVaultConfigurationProvider INSTANCE = new SecureVaultConfigurationProvider();
    private boolean initialized = false;
    private SecureVaultConfiguration secureVaultConfiguration;

    private SecureVaultConfigurationProvider() {
    }

    private static SecureVaultConfigurationProvider getInstance() throws SecureVaultException {
        if (INSTANCE.initialized) {
            return INSTANCE;
        }

        synchronized (INSTANCE) {
            if (!INSTANCE.initialized) {
                INSTANCE.init();
            }
        }
        return INSTANCE;
    }

    public static SecureVaultConfiguration getConfiguration() throws SecureVaultException {
        return getInstance().secureVaultConfiguration;
    }

    private void init() throws SecureVaultException {
        String configFileLocation = SecureVaultUtils.getSecureVaultYAMLLocation();
        String resolvedFileContent = SecureVaultUtils.resolveFileToString(Paths.get(configFileLocation).toFile());

        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(SecureVaultConfiguration.class,
                SecureVaultConfiguration.class.getClassLoader()));
        yaml.setBeanAccess(BeanAccess.FIELD);
        secureVaultConfiguration = yaml.loadAs(resolvedFileContent, SecureVaultConfiguration.class);

        initialized = true;
        logger.debug("Secure vault configurations loaded successfully.");
    }
}
