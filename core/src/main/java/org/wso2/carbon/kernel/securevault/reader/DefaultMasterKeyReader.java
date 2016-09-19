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
import org.wso2.carbon.kernel.securevault.config.model.masterkey.MasterKeyConfiguration;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;
import org.wso2.carbon.kernel.utils.Utils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * This service component is responsible for providing master keys to initialize the secret repositories. This provider
 * has four behaviours
 * 1. Reads the master keys from Environment variables.
 * 2. Reads the master keys from System properties.
 * 3. Reads the master keys from file
 * It looks for the master-keys.yaml file in server home directory, read the master keys and delete the
 * file. If the file has the property "permanent:true", the file will not be deleted.
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
    private static final String MASTER_KEYS_FILE_NAME = "master-keys.yaml";
    private Set<String> relocationPaths = new HashSet<>();

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

        Path masterKeysFilePath = Paths.get(Utils.getCarbonHome().toString(), MASTER_KEYS_FILE_NAME);
        if (Files.exists(masterKeysFilePath)) {
            readMasterKeysFile(masterKeysFilePath, masterKeys);
        }

        if (!fullyInitialized(masterKeys)) {
            readMasterKeysFromConsole(masterKeys);
        }
    }

    private void readMasterKeysFromEnvironment(List<MasterKey> masterKeys) throws SecureVaultException {
        masterKeys.forEach(masterKey -> {
            logger.debug("Reading master key '{}' from environment variables.", masterKey.getMasterKeyName());
            Optional.ofNullable(System.getenv(masterKey.getMasterKeyName()))
                    .ifPresent(s -> masterKey.setMasterKeyValue(s.toCharArray()));
        });
    }

    private void readMasterKeysFromSystem(List<MasterKey> masterKeys) throws SecureVaultException {
        masterKeys.forEach(masterKey -> {
            logger.debug("Reading master key '{}' from system properties.", masterKey.getMasterKeyName());
            Optional.ofNullable(System.getProperty(masterKey.getMasterKeyName()))
                    .ifPresent(s -> masterKey.setMasterKeyValue(s.toCharArray()));
        });
    }

    private void readMasterKeysFile(Path masterKeysFilePath, List<MasterKey> masterKeys) throws SecureVaultException {
        try (InputStream inputStream = new FileInputStream(masterKeysFilePath.toFile());
             BufferedReader bufferedReader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            Yaml yaml = new Yaml(new CustomClassLoaderConstructor(MasterKeyConfiguration.class,
                    MasterKeyConfiguration.class.getClassLoader()));
            yaml.setBeanAccess(BeanAccess.FIELD);
            MasterKeyConfiguration masterKeyConfiguration =
                    Optional.ofNullable(yaml.loadAs(bufferedReader, MasterKeyConfiguration.class))
                            .orElseThrow(() -> new SecureVaultException("Unable to load master-keys.yaml file"));

            if (!masterKeyConfiguration.getRelocation().isEmpty()) {
                logger.debug("Master key relocation : {}", masterKeyConfiguration.getRelocation());
                if (relocationPaths.contains(masterKeysFilePath.toString())) {
                    throw new SecureVaultException("Cyclic dependency detected on Master Key relocation path");
                }
                relocationPaths.add(masterKeysFilePath.toString());
                Path path = Paths.get(masterKeyConfiguration.getRelocation());
                readMasterKeysFile(path, masterKeys);
                return;
            }

            Properties properties = Optional.ofNullable(masterKeyConfiguration.getMasterKeys())
                    .orElseThrow(() -> new SecureVaultException("masterKeys section of master-keys.yaml is empty"));
            if (properties.isEmpty()) {
                throw new SecureVaultException("master-keys.yaml file is doesn't contain any master key "
                        + masterKeysFilePath.toFile());
            }

            for (MasterKey masterKey : masterKeys) {
                logger.debug("Reading master key '{}' from file.", masterKey.getMasterKeyName());
                masterKey.setMasterKeyValue(Optional.ofNullable(
                        properties.getProperty(masterKey.getMasterKeyName()))
                        .orElseThrow(() -> new SecureVaultException("Master Key value not found for : "
                                + masterKey.getMasterKeyName())).toCharArray());
            }

            inputStream.close();

            if (!(!relocationPaths.isEmpty() || masterKeyConfiguration.isPermanent())
                    && !masterKeysFilePath.toFile().delete()) {
                masterKeysFilePath.toFile().deleteOnExit();
            }
        } catch (IOException e) {
            throw new SecureVaultException("Failed to load master-keys.yaml file " + masterKeysFilePath.toFile(), e);
        }
    }

    private void readMasterKeysFromConsole(List<MasterKey> masterKeys) throws SecureVaultException {
        Optional.ofNullable(System.console()).ifPresent(console ->
                masterKeys.stream()
                        .filter(masterKey -> !masterKey.getMasterKeyValue().isPresent())
                        .forEach(uninitializedMasterKey -> {
                            logger.debug("Reading master key '{}' from console.",
                                    uninitializedMasterKey.getMasterKeyName());
                            uninitializedMasterKey.setMasterKeyValue(console.readPassword("[%s]", "Enter master key '"
                                            + uninitializedMasterKey.getMasterKeyName() + "' :"));
                        })
        );
    }

    private boolean fullyInitialized(List<MasterKey> masterKeys) {
        return !masterKeys.parallelStream()
                .filter(masterKey -> !masterKey.getMasterKeyValue().isPresent())
                .findFirst().isPresent();
    }
}
