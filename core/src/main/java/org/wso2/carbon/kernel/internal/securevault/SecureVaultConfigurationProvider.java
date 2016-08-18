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
import org.wso2.carbon.kernel.internal.utils.Utils;
import org.wso2.carbon.kernel.securevault.config.model.SecureVaultConfiguration;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
        String configFileLocation = Utils.getSecureVaultYAMLLocation();
        try (InputStream inputStream = new FileInputStream(configFileLocation);
             BufferedReader bufferedReader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            // TODO : pass the inputStream to deployment properties to get the updated values before creating the Yaml
            // ConfigUtil.parse(inputStream);

            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            secureVaultConfiguration = yaml.loadAs(bufferedReader, SecureVaultConfiguration.class);

            initialized = true;
            logger.debug("Secure vault configurations loaded successfully.");
        } catch (IOException e) {
            throw new SecureVaultException("Failed to read secure vault configuration file : " + configFileLocation, e);
        }
    }
}
