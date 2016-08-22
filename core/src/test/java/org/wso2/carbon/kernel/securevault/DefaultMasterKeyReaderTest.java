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

package org.wso2.carbon.kernel.securevault;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.securevault.config.model.masterkey.MasterKeyConfiguration;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;
import org.wso2.carbon.kernel.securevault.reader.DefaultMasterKeyReader;
import org.wso2.carbon.kernel.utils.ClassUtils;
import org.wso2.carbon.kernel.utils.EnvironmentUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Unit tests class for org.wso2.carbon.kernel.securevault.reader.DefaultMasterKeyReader.
 *
 * @since 5.2.0
 */
public class DefaultMasterKeyReaderTest {
    private static final Path secureVaultResourcesPath = Paths.get("src", "test", "resources", "securevault");
    private static final Path secureVaultTargetPath = Paths.get("target");
    MasterKeyReader masterKeyReader;

    @BeforeTest
    public void setup() {
        masterKeyReader = new DefaultMasterKeyReader();
    }

    @Test
    public void testReadMasterKeys() {
        System.setProperty(Constants.CARBON_HOME, Paths.get(secureVaultResourcesPath.toString(),
                "nonExisting").toString());

        List<MasterKey> masterKeys = new ArrayList<>();
        masterKeys.add(new MasterKey("MasterKey1"));
        try {
            masterKeyReader.readMasterKeys(masterKeys);
            Assert.assertEquals(masterKeys.get(0).getMasterKeyValue(), Optional.empty());
        } catch (SecureVaultException e) {
            Assert.fail("An exception occurred while reading master keys.");
        }
    }

    @Test(dependsOnMethods = {"testReadMasterKeys"})
    public void testReadMasterKeysFromEnvironment() {
        System.setProperty(Constants.CARBON_HOME, Paths.get(secureVaultResourcesPath.toString(),
                "nonExisting").toString());

        EnvironmentUtils.setEnv("MasterKey1", "MyPasswordFromEnv");

        List<MasterKey> masterKeys = new ArrayList<>();
        masterKeys.add(new MasterKey("MasterKey1"));
        try {
            masterKeyReader.readMasterKeys(masterKeys);
            Assert.assertEquals(new String(masterKeys.get(0).getMasterKeyValue().get()), "MyPasswordFromEnv");
        } catch (SecureVaultException e) {
            Assert.fail("An exception occurred while reading master keys.");
        }
    }

    @Test(dependsOnMethods = {"testReadMasterKeysFromEnvironment"})
    public void testReadMasterKeysFromSystem() {
        System.setProperty(Constants.CARBON_HOME, Paths.get(secureVaultResourcesPath.toString(),
                "nonExisting").toString());

        EnvironmentUtils.setEnv("MasterKey1", "MyPasswordFromEnv");
        System.setProperty("MasterKey1", "MyPasswordFromSys");

        List<MasterKey> masterKeys = new ArrayList<>();
        masterKeys.add(new MasterKey("MasterKey1"));
        try {
            masterKeyReader.readMasterKeys(masterKeys);
            Assert.assertEquals(new String(masterKeys.get(0).getMasterKeyValue().get()), "MyPasswordFromSys");
        } catch (SecureVaultException e) {
            Assert.fail("An exception occurred while reading master keys.");
        }
    }

    @Test(expectedExceptions = SecureVaultException.class, dependsOnMethods = {"testReadMasterKeysFromSystem"})
    public void testReadMasterKeysFromEmptyFile() throws SecureVaultException {
        System.setProperty(Constants.CARBON_HOME, secureVaultTargetPath.toString());

        EnvironmentUtils.setEnv("MasterKey1", "MyPasswordFromEnv");
        System.setProperty("MasterKey1", "MyPasswordFromSys");

        File tempFile = new File(Paths.get(secureVaultTargetPath.toString(), "master-keys.yaml").toString());
        try {
            tempFile.createNewFile();
            tempFile.deleteOnExit();
        } catch (IOException e) {
            Assert.fail("Failed to create temp password file");
        }

        List<MasterKey> masterKeys = new ArrayList<>();
        masterKeys.add(new MasterKey("MasterKey1"));

        masterKeyReader.readMasterKeys(masterKeys);

    }

    @Test(dependsOnMethods = {"testReadMasterKeysFromEmptyFile"})
    public void testReadMasterKeysFromFile() {
        System.setProperty(Constants.CARBON_HOME, secureVaultTargetPath.toString());

        EnvironmentUtils.setEnv("MasterKey1", "MyPasswordFromEnv");
        System.setProperty("MasterKey1", "MyPasswordFromSys");

        MasterKeyConfiguration masterKeyConfiguration = new MasterKeyConfiguration();
        Properties properties = new Properties();
        properties.setProperty("MasterKey1", "MyPasswordFromFile");
        ClassUtils.setToPrivateField(masterKeyConfiguration, "masterKeys", properties);

        File tempFile = new File(Paths.get(secureVaultTargetPath.toString(), "master-keys.yaml").toString());
        createMasterKeyFile(tempFile, masterKeyConfiguration);

        List<MasterKey> masterKeys = new ArrayList<>();
        masterKeys.add(new MasterKey("MasterKey1"));
        try {
            masterKeyReader.readMasterKeys(masterKeys);
            Assert.assertEquals(new String(masterKeys.get(0).getMasterKeyValue().get()), "MyPasswordFromFile");
        } catch (SecureVaultException e) {
            Assert.fail("An exception occurred while reading master keys.");
        }
    }

    @Test(dependsOnMethods = {"testReadMasterKeysFromFile"}, expectedExceptions = {SecureVaultException.class})
    public void testReadMasterKeysFromFileWithNoMasterKeys() throws SecureVaultException {
        System.setProperty(Constants.CARBON_HOME, secureVaultTargetPath.toString());

        EnvironmentUtils.setEnv("MasterKey1", "MyPasswordFromEnv");
        System.setProperty("MasterKey1", "MyPasswordFromSys");

        MasterKeyConfiguration masterKeyConfiguration = new MasterKeyConfiguration();
        Properties properties = new Properties();
        ClassUtils.setToPrivateField(masterKeyConfiguration, "masterKeys", properties);

        File tempFile = new File(Paths.get(secureVaultTargetPath.toString(), "master-keys.yaml").toString());
        createMasterKeyFile(tempFile, masterKeyConfiguration);

        List<MasterKey> masterKeys = new ArrayList<>();
        masterKeys.add(new MasterKey("MasterKey1"));
        masterKeyReader.readMasterKeys(masterKeys);
        Assert.assertEquals(new String(masterKeys.get(0).getMasterKeyValue().get()), "MyPasswordFromFile");
    }

    @Test(dependsOnMethods = {"testReadMasterKeysFromFileWithNoMasterKeys"})
    public void testReadMasterKeysFromPermanentFile() {
        System.setProperty(Constants.CARBON_HOME, secureVaultTargetPath.toString());

        EnvironmentUtils.setEnv("MasterKey1", "MyPasswordFromEnv");
        System.setProperty("MasterKey1", "MyPasswordFromSys");

        MasterKeyConfiguration masterKeyConfiguration = new MasterKeyConfiguration();
        Properties properties = new Properties();
        properties.setProperty("MasterKey1", "MyPasswordFromFile");
        ClassUtils.setToPrivateField(masterKeyConfiguration, "masterKeys", properties);
        ClassUtils.setToPrivateField(masterKeyConfiguration, "permanent", true);

        File tempFile = new File(Paths.get(secureVaultTargetPath.toString(), "master-keys.yaml").toString());
        createMasterKeyFile(tempFile, masterKeyConfiguration);

        List<MasterKey> masterKeys = new ArrayList<>();
        masterKeys.add(new MasterKey("MasterKey1"));
        try {
            masterKeyReader.readMasterKeys(masterKeys);
            Assert.assertEquals(new String(masterKeys.get(0).getMasterKeyValue().get()), "MyPasswordFromFile");
        } catch (SecureVaultException e) {
            Assert.fail("An exception occurred while reading master keys.");
        }
    }

    @Test(dependsOnMethods = {"testReadMasterKeysFromPermanentFile"})
    public void testReadMasterKeysViaRelocation() {
        System.setProperty(Constants.CARBON_HOME, secureVaultTargetPath.toString());

        MasterKeyConfiguration masterKeyConfiguration = new MasterKeyConfiguration();
        Properties properties = new Properties();
        properties.setProperty("MasterKey1", "MyPasswordFromFile");
        ClassUtils.setToPrivateField(masterKeyConfiguration, "masterKeys", properties);
        ClassUtils.setToPrivateField(masterKeyConfiguration, "permanent", true);

        File tempFile = new File(Paths.get(secureVaultTargetPath.toString(), "new-master-keys.yaml").toString());
        createMasterKeyFile(tempFile, masterKeyConfiguration);

        MasterKeyConfiguration masterKeyConfiguration1 = new MasterKeyConfiguration();
        ClassUtils.setToPrivateField(masterKeyConfiguration1, "relocation", tempFile.getAbsolutePath());

        File tempFile1 = new File(Paths.get(secureVaultTargetPath.toString(), "master-keys.yaml").toString());
        createMasterKeyFile(tempFile1, masterKeyConfiguration1);

        List<MasterKey> masterKeys = new ArrayList<>();
        masterKeys.add(new MasterKey("MasterKey1"));
        try {
            masterKeyReader.readMasterKeys(masterKeys);
            Assert.assertEquals(new String(masterKeys.get(0).getMasterKeyValue().get()), "MyPasswordFromFile");
        } catch (SecureVaultException e) {
            Assert.fail("An exception occurred while reading master keys.");
        }
    }

    @Test(dependsOnMethods = {"testReadMasterKeysViaRelocation"}, expectedExceptions = {SecureVaultException.class})
    public void testReadMasterKeysViaRelocationNonExistingFile() throws SecureVaultException {
        System.setProperty(Constants.CARBON_HOME, secureVaultTargetPath.toString());

        MasterKeyConfiguration masterKeyConfiguration = new MasterKeyConfiguration();
        Properties properties = new Properties();
        properties.setProperty("MasterKey1", "MyPasswordFromFile");
        ClassUtils.setToPrivateField(masterKeyConfiguration, "masterKeys", properties);
        ClassUtils.setToPrivateField(masterKeyConfiguration, "permanent", true);

        File tempFile = new File(Paths.get(secureVaultTargetPath.toString(), "new-master-keys.yaml").toString());
        createMasterKeyFile(tempFile, masterKeyConfiguration);

        MasterKeyConfiguration masterKeyConfiguration1 = new MasterKeyConfiguration();
        ClassUtils.setToPrivateField(masterKeyConfiguration1, "relocation",
                Paths.get(secureVaultTargetPath.toString(), "nonExisingPath", "master-keys.yaml").toString());

        File tempFile1 = new File(Paths.get(secureVaultTargetPath.toString(), "master-keys.yaml").toString());
        createMasterKeyFile(tempFile1, masterKeyConfiguration1);

        List<MasterKey> masterKeys = new ArrayList<>();
        masterKeys.add(new MasterKey("MasterKey1"));

        masterKeyReader.readMasterKeys(masterKeys);
        Assert.assertEquals(new String(masterKeys.get(0).getMasterKeyValue().get()), "MyPasswordFromFile");
    }

    @Test(expectedExceptions = {SecureVaultException.class},
            dependsOnMethods = {"testReadMasterKeysViaRelocationNonExistingFile"})
    public void testReadMasterKeysViaRelocationCyclicDependency() throws SecureVaultException {
        System.setProperty(Constants.CARBON_HOME, secureVaultTargetPath.toString());

        File tempFile = new File(Paths.get(secureVaultTargetPath.toString(), "new-master-keys.yaml").toString());
        File tempFile1 = new File(Paths.get(secureVaultTargetPath.toString(), "master-keys.yaml").toString());
        MasterKeyConfiguration masterKeyConfiguration = new MasterKeyConfiguration();
        Properties properties = new Properties();
        properties.setProperty("MasterKey1", "MyPasswordFromFile");
        ClassUtils.setToPrivateField(masterKeyConfiguration, "relocation", tempFile1.getAbsolutePath());
        ClassUtils.setToPrivateField(masterKeyConfiguration, "masterKeys", properties);
        ClassUtils.setToPrivateField(masterKeyConfiguration, "permanent", true);


        createMasterKeyFile(tempFile, masterKeyConfiguration);

        MasterKeyConfiguration masterKeyConfiguration1 = new MasterKeyConfiguration();
        ClassUtils.setToPrivateField(masterKeyConfiguration1, "relocation", tempFile.getAbsolutePath());

        createMasterKeyFile(tempFile1, masterKeyConfiguration1);

        List<MasterKey> masterKeys = new ArrayList<>();
        masterKeys.add(new MasterKey("MasterKey1"));
        masterKeyReader.readMasterKeys(masterKeys);
        Assert.assertEquals(new String(masterKeys.get(0).getMasterKeyValue().get()), "MyPasswordFromFile");
    }

    private void createMasterKeyFile(File file, MasterKeyConfiguration masterKeyConfiguration) {
        try {
            file.createNewFile();
            file.deleteOnExit();
            FileWriter fileWriter = new FileWriter(file);

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

            Representer representer = new Representer();
            representer.addClassTag(MasterKeyConfiguration.class, Tag.MAP);
            Yaml yaml = new Yaml(representer, options);

            yaml.setBeanAccess(BeanAccess.FIELD);
            yaml.dump(masterKeyConfiguration, fileWriter);
        } catch (IOException e) {
            Assert.fail("Failed to create temp password file");
        }
    }
}
