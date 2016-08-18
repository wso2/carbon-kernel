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

package org.wso2.carbon.kernel.securevault.reader;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.securevault.MasterKey;
import org.wso2.carbon.kernel.securevault.MasterKeyReader;
import org.wso2.carbon.kernel.securevault.config.model.MasterKeyReaderConfiguration;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;
import org.wso2.carbon.kernel.utils.Utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * This service component is responsible for providing master keys to initialize the secret repositories. This provider
 * has four behaviours
 * 1. Reads the master keys from Environment variables.
 * 2. Reads the master keys from System properties.
 * 3. Reads the master keys from file
 * It looks for a property file with name "password" in server home directory, read the passwords and delete the
 * file. If the file has a property "permanent=true", the file will not be deleted.
 * 4. Reads the master keys from command line.
 * And this component registers a MasterKeyReader as an OSGi service.
 *
 * @since 5.2.0
 */
@Component(
        name = "org.wso2.carbon.kernel.securevault.reader.DefaultMasterKeyReader",
        immediate = true,
        property = {
                "capabilityName=org.wso2.carbon.kernel.securevault.MasterKeyReader"
        }
)
public class DefaultMasterKeyReader implements MasterKeyReader {
    private static Logger logger = LoggerFactory.getLogger(DefaultMasterKeyReader.class);
    private boolean isPermanentFile = false;

    @Activate
    public void activate() {
        logger.debug("Activating DefaultMasterKeyReader");
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Deactivating DefaultMasterKeyReader");
    }

    @Override
    public void init(MasterKeyReaderConfiguration masterKeyReaderConfiguration) throws SecureVaultException {
        // No initializations needed for the DefaultMasterKeyReader
    }

    @Override
    public void readMasterKeys(List<MasterKey> masterKeys) throws SecureVaultException {
        readMasterKeysFromEnvironment(masterKeys);
        readMasterKeysFromSystem(masterKeys);

        Path passwordFilePath = Paths.get(Utils.getCarbonHome().toString(), "password");
        if (Files.exists(passwordFilePath)) {
            readSecretsFile(passwordFilePath, masterKeys);
        }

        if (!fullyInitialized(masterKeys)) {
            readSecretsFromConsole(masterKeys);
        }
    }

    private void readMasterKeysFromEnvironment(List<MasterKey> masterKeys) throws SecureVaultException {
        masterKeys.forEach(masterKey -> {
            logger.debug("Reading master key '{}' from environment variables.", masterKey.getMasterKeyName());
            Optional.ofNullable(System.getenv(masterKey.getMasterKeyName()))
                    .ifPresent(s -> masterKey.setMasterKeyValue(s));
        });
    }

    private void readMasterKeysFromSystem(List<MasterKey> masterKeys) throws SecureVaultException {
        masterKeys.forEach(masterKey -> {
            logger.debug("Reading master key '{}' from system properties.", masterKey.getMasterKeyName());
            Optional.ofNullable(System.getProperty(masterKey.getMasterKeyName()))
                    .ifPresent(s -> masterKey.setMasterKeyValue(s));
        });
    }

    private void readSecretsFile(Path passwordFilePath, List<MasterKey> masterKeys) throws SecureVaultException {
        try (InputStream inputStream = new FileInputStream(passwordFilePath.toFile());
             BufferedReader bufferedReader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            Properties properties = new Properties();
            properties.load(bufferedReader);

            if (properties.isEmpty()) {
                throw new SecureVaultException("Password file is empty " + passwordFilePath.toFile());
            }

            String permanentFile = properties.getProperty("permanent");
            if (permanentFile != null && !permanentFile.isEmpty()) {
                isPermanentFile = Boolean.parseBoolean(permanentFile);
            }

            for (MasterKey masterKey : masterKeys) {
                logger.debug("Reading master key '{}' from file.", masterKey.getMasterKeyName());
                masterKey.setMasterKeyValue(properties.getProperty(masterKey.getMasterKeyName()));
            }

            inputStream.close();

            if (!isPermanentFile && !passwordFilePath.toFile().delete()) {
                passwordFilePath.toFile().deleteOnExit();
            }
        } catch (IOException e) {
            throw new SecureVaultException("Failed to load secret file " + passwordFilePath.toFile(), e);
        }
    }

    private void readSecretsFromConsole(List<MasterKey> masterKeys) throws SecureVaultException {
        Optional.ofNullable(System.console()).ifPresent(console ->
            masterKeys.stream()
                    .filter(masterKey -> !Optional.ofNullable(masterKey.getMasterKeyValue()).isPresent())
                    .forEach(uninitializedMasterKey -> {
                        logger.debug("Reading master key '{}' from console.",
                                uninitializedMasterKey.getMasterKeyName());
                        uninitializedMasterKey.setMasterKeyValue(
                                new String(console.readPassword("[%s]", "Enter master key '"
                                        + uninitializedMasterKey.getMasterKeyName() + "' :")));
                    })
        );
    }

    private boolean fullyInitialized(List<MasterKey> masterKeys) {
        return masterKeys.stream()
                .filter(masterKey -> {
                    if (masterKey.getMasterKeyValue().isPresent()) {
                        return false;
                    }
                    logger.debug("Mater key '{}' is not initialized from env, sys or file, hence needed to be read " +
                            "from console.", masterKey.getMasterKeyName());
                    return true;
                })
                .count() <= 0;
    }
}
