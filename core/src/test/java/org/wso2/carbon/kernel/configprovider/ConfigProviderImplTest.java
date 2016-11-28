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
package org.wso2.carbon.kernel.configprovider;

import org.easymock.EasyMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.internal.configprovider.ConfigProviderDataHolder;
import org.wso2.carbon.kernel.internal.configprovider.ConfigProviderImpl;
import org.wso2.carbon.kernel.securevault.SecureVault;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;
import org.wso2.carbon.kernel.utils.EnvironmentUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;



/**
 * This class is to demonstrate the sample uses of the ConfigProvider.
 *
 * @since 5.2.0
 */
public class ConfigProviderImplTest {

    private static Logger logger = LoggerFactory.getLogger(ConfigProviderImplTest.class.getName());
    private String basedir = System.getProperty("basedir");
    private static final String PASSWORD = "n3wP4s5w0r4";

    @BeforeTest
    public void setup() throws SecureVaultException {
        setUpEnvironment();
        if (basedir == null) {
            basedir = Paths.get("").toAbsolutePath().toString();
        }

        SecureVault secureVault = EasyMock.mock(SecureVault.class);
        ConfigProviderDataHolder.getInstance().setSecureVault(secureVault);
        EasyMock.expect(secureVault.resolve(EasyMock.anyString())).andReturn(PASSWORD.toCharArray()).anyTimes();
        EasyMock.replay(secureVault);
    }
    @BeforeMethod
    private void setCarbonHome() {
        Path carbonHome = Paths.get("");
        carbonHome = Paths.get(carbonHome.toString(), "src", "test", "resources");
        System.setProperty(Constants.CARBON_HOME, carbonHome.toString());
        logger.debug("Carbon Home Absolute path set to: " + carbonHome.toAbsolutePath());
    }

    private void setUpEnvironment() {
        setCarbonHome();
        //This is how to set Environment Variables
        Map<String, String> envVarMap = new HashMap<>();
        envVarMap.put("pqr.http.port", "8501");
        envVarMap.put("sample.abc.port", "8081");
        EnvironmentUtils.setEnv(envVarMap);
        //This is how to set System properties
        System.setProperty("abc.http.port", "8001");
        System.setProperty("sample.xyz.port", "9091");
        System.setProperty("pqr.secure", "true");
    }

    @BeforeMethod
    private void clearDeploymentConfiguration() {
        try {
            Class providerClass = Class.forName("org.wso2.carbon.kernel.internal.configprovider.ConfigProviderImpl");
            ConfigFileReader fileReader = new XMLBasedConfigFileReader("Example.xml");
            Constructor<?> cons = providerClass.getConstructor(ConfigFileReader.class);
            Object providerObject = cons.newInstance(fileReader);
            Field field = providerClass.getDeclaredField("deploymentConfigs");
            field.setAccessible(true);
            field.set(providerObject, null);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException | InstantiationException |
                NoSuchMethodException | InvocationTargetException e) {
            logger.error(e.toString(), e);
        }
    }

    @Test(description = "This test will test functionality when using xml config file")
    public void xmlFileConfigObjectTestCase() throws IOException, SecureVaultException {
        try {
            Path resourcePath = Paths.get(basedir, "src", "test", "resources", "conf", "Example.xml");
            File file = resourcePath.toFile();

            JAXBContext jaxbContext = JAXBContext.newInstance(Configurations.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Configurations configurations = (Configurations) unmarshaller.unmarshal(file);

            //Transport 1
            Assert.assertEquals(configurations.getTenant(), "tenant");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getName(), "abc");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPort(), 8000);
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).isSecure(), "false");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getDesc(),
                    "This transport will use 8000 as its port");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPassword(),
                    "${sec:conn.auth.password}");

            //Transport 2
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getName(), "pqr");
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getPort(), 0);
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).isSecure(), "${sys:pqr.secure}");
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getDesc(),
                    "This transport will use ${env:pqr.http.port} as its port. Secure - ${sys:pqr.secure}");
            //Transport 3
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getName(), "xyz");
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getPort(), 0);
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).isSecure(),
                    "${sys:xyz.secure,true}");
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getDesc(),
                    "This transport will use ${env:xyz.http.port,8888} as its port");

            ConfigFileReader fileReader = new XMLBasedConfigFileReader("Example.xml");
            ConfigProvider configProvider = new ConfigProviderImpl(fileReader);
            configurations = configProvider.getConfigurationObject(Configurations.class);

            //Transport 1
            Assert.assertEquals(configurations.getTenant(), "tenant");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getName(), "abc");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPort(), 8000);
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).isSecure(), "false");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getDesc(),
                    "This transport will use 8000 as its port");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPassword(), PASSWORD);

            //Transport 2
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getName(), "pqr");
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getPort(), 8501);
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).isSecure(), "true");
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getDesc(),
                    "This transport will use 8501 as its port. Secure - true");
            //Transport 3
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getName(), "xyz");
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getPort(), 9000);
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).isSecure(), "true");
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getDesc(),
                    "This transport will use 8888 as its port");
        } catch (JAXBException | CarbonConfigurationException e) {
            logger.error(e.toString(), e);
            Assert.fail();
        }
    }

    @Test(description = "This test will test functionality when using xml config file and configuration map")
    public void xmlFileConfigMapTestCase() throws IOException, SecureVaultException {
        try {
            Path resourcePath = Paths.get(basedir, "src", "test", "resources", "conf", "Example.xml");
            File file = resourcePath.toFile();

            JAXBContext jaxbContext = JAXBContext.newInstance(Configurations.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Configurations configurations = (Configurations) unmarshaller.unmarshal(file);

            //Transport 1
            Assert.assertEquals(configurations.getTenant(), "tenant");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getName(), "abc");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPort(), 8000);
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).isSecure(), "false");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getDesc(),
                    "This transport will use 8000 as its port");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPassword(),
                    "${sec:conn.auth.password}");

            //Transport 2
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getName(), "pqr");
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getPort(), 0);
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).isSecure(), "${sys:pqr.secure}");
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getDesc(),
                    "This transport will use ${env:pqr.http.port} as its port. Secure - ${sys:pqr.secure}");
            //Transport 3
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getName(), "xyz");
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getPort(), 0);
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).isSecure(),
                    "${sys:xyz.secure,true}");
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getDesc(),
                    "This transport will use ${env:xyz.http.port,8888} as its port");

            ConfigFileReader fileReader = new XMLBasedConfigFileReader("Example.xml");
            ConfigProvider configProvider = new ConfigProviderImpl(fileReader);
            Map configurationMap = configProvider.getConfigurationMap("configurations");

            Map transportsMap = (Map) configurationMap.get("transports");
            ArrayList transportList = (ArrayList) transportsMap.get("transport");
            LinkedHashMap transport1 = (LinkedHashMap) transportList.get(0);
            LinkedHashMap transport2 = (LinkedHashMap) transportList.get(1);
            LinkedHashMap transport3 = (LinkedHashMap) transportList.get(2);

            Assert.assertEquals(configurationMap.get("tenant"), "tenant");
            //Transport 1
            Assert.assertEquals(transport1.get("name"), "abc");
            Assert.assertEquals(transport1.get("port"), 8000);
            Assert.assertEquals(transport1.get("secure"), false);
            Assert.assertEquals(transport1.get("desc"), "This transport will use 8000 as its port");
            Assert.assertEquals(transport1.get("password"), PASSWORD);
            //Transport 2
            Assert.assertEquals(transport2.get("name"), "pqr");
            Assert.assertEquals(transport2.get("port"), 8501);
            Assert.assertEquals(transport2.get("secure"), true);
            Assert.assertEquals(transport2.get("desc"),
                    "This transport will use 8501 as its port. Secure - true");
            //Transport 3
            Assert.assertEquals(transport3.get("name"), "xyz");
            Assert.assertEquals(transport3.get("port"), 9000);
            Assert.assertEquals(transport3.get("secure"), true);
            Assert.assertEquals(transport3.get("desc"),
                    "This transport will use 8888 as its port");
        } catch (JAXBException | CarbonConfigurationException e) {
            logger.error(e.toString(), e);
            Assert.fail();
        }
    }

    @Test(expectedExceptions = CarbonConfigurationException.class, description = "This test will test functionality " +
            "when " +
            "xml config file not found")
    public void xmlFileNotFoundTestCase() throws CarbonConfigurationException {
        ConfigFileReader fileReader = new XMLBasedConfigFileReader("Example1.xml");
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader);
        Configurations configurations = configProvider.getConfigurationObject(Configurations.class);
        Assert.assertNull(configurations, "configurations object should be null");
    }


    @Test(description = "This test will test functionality when using yaml config file")
    public void yamlFileConfigObjectTestCase() throws IOException {
        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "conf", "Example.yaml");
        File file = resourcePath.toFile();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Map map = yaml.loadAs(fileInputStream, Map.class);
            Map configurationMap = (Map) map.get("configurations");
            Map transportsMap = (Map) configurationMap.get("transports");
            ArrayList transportList = (ArrayList) transportsMap.get("transport");
            LinkedHashMap transport1 = (LinkedHashMap) transportList.get(0);
            LinkedHashMap transport2 = (LinkedHashMap) transportList.get(1);
            LinkedHashMap transport3 = (LinkedHashMap) transportList.get(2);

            Assert.assertEquals(configurationMap.get("tenant"), "tenant");
            //Transport 1
            Assert.assertEquals(transport1.get("name"), "abc");
            Assert.assertEquals(transport1.get("port"), 8000);
            Assert.assertEquals(transport1.get("secure"), false);
            Assert.assertEquals(transport1.get("desc"), "This transport will use 8000 as its port");
            Assert.assertEquals(transport1.get("password"), "${sec:conn.auth.password}");
            //Transport 2
            Assert.assertEquals(transport2.get("name"), "pqr");
            Assert.assertEquals(transport2.get("port"), "${env:pqr.http.port}");
            Assert.assertEquals(transport2.get("secure"), "${sys:pqr.secure}");
            Assert.assertEquals(transport2.get("desc"),
                    "This transport will use ${env:pqr.http.port} as its port. Secure - ${sys:pqr.secure}");
            //Transport 3
            Assert.assertEquals(transport3.get("name"), "xyz");
            Assert.assertEquals(transport3.get("port"), "${env:xyz.http.port,9000}");
            Assert.assertEquals(transport3.get("secure"), "${sys:xyz.secure,true}");
            Assert.assertEquals(transport3.get("desc"),
                    "This transport will use ${env:xyz.http.port,8888} as its port");
        } catch (FileNotFoundException e) {
            logger.error(e.toString());
            Assert.fail();
        }

        try {
            ConfigFileReader fileReader = new YAMLBasedConfigFileReader("Example.yaml");
            ConfigProvider configProvider = new ConfigProviderImpl(fileReader);
            Configurations configurations = configProvider.getConfigurationObject(Configurations.class);

            //Transport 1
            Assert.assertEquals(configurations.getTenant(), "tenant");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getName(), "abc");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPort(), 8000);
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).isSecure(), "false");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getDesc(),
                    "This transport will use 8000 as its port");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPassword(), PASSWORD);

            //Transport 2
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getName(), "pqr");
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getPort(), 8501);
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).isSecure(), "true");
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getDesc(),
                    "This transport will use 8501 as its port. Secure - true");
            //Transport 3
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getName(), "xyz");
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getPort(), 9000);
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).isSecure(), "true");
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getDesc(),
                    "This transport will use 8888 as its port");
        } catch (CarbonConfigurationException e) {
            logger.error(e.toString());
            Assert.fail();
        }
    }

    @Test(description = "This test will test functionality when using yaml config file and configuration map")
    public void yamlFileConfigMapTestCase() throws IOException {
        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "conf", "Example.yaml");
        File file = resourcePath.toFile();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Map map = yaml.loadAs(fileInputStream, Map.class);
            Map configurationMap = (Map) map.get("configurations");
            Map transportsMap = (Map) configurationMap.get("transports");
            ArrayList transportList = (ArrayList) transportsMap.get("transport");
            LinkedHashMap transport1 = (LinkedHashMap) transportList.get(0);
            LinkedHashMap transport2 = (LinkedHashMap) transportList.get(1);
            LinkedHashMap transport3 = (LinkedHashMap) transportList.get(2);

            Assert.assertEquals(configurationMap.get("tenant"), "tenant");
            //Transport 1
            Assert.assertEquals(transport1.get("name"), "abc");
            Assert.assertEquals(transport1.get("port"), 8000);
            Assert.assertEquals(transport1.get("secure"), false);
            Assert.assertEquals(transport1.get("desc"), "This transport will use 8000 as its port");
            Assert.assertEquals(transport1.get("password"), "${sec:conn.auth.password}");
            //Transport 2
            Assert.assertEquals(transport2.get("name"), "pqr");
            Assert.assertEquals(transport2.get("port"), "${env:pqr.http.port}");
            Assert.assertEquals(transport2.get("secure"), "${sys:pqr.secure}");
            Assert.assertEquals(transport2.get("desc"),
                    "This transport will use ${env:pqr.http.port} as its port. Secure - ${sys:pqr.secure}");
            //Transport 3
            Assert.assertEquals(transport3.get("name"), "xyz");
            Assert.assertEquals(transport3.get("port"), "${env:xyz.http.port,9000}");
            Assert.assertEquals(transport3.get("secure"), "${sys:xyz.secure,true}");
            Assert.assertEquals(transport3.get("desc"),
                    "This transport will use ${env:xyz.http.port,8888} as its port");
        } catch (FileNotFoundException e) {
            logger.error(e.toString());
            Assert.fail();
        }

        try {
            ConfigFileReader fileReader = new YAMLBasedConfigFileReader("Example.yaml");
            ConfigProvider configProvider = new ConfigProviderImpl(fileReader);
            Map configurationMap = configProvider.getConfigurationMap("configurations");

            Map transportsMap = (Map) configurationMap.get("transports");
            ArrayList transportList = (ArrayList) transportsMap.get("transport");
            LinkedHashMap transport1 = (LinkedHashMap) transportList.get(0);
            LinkedHashMap transport2 = (LinkedHashMap) transportList.get(1);
            LinkedHashMap transport3 = (LinkedHashMap) transportList.get(2);

            Assert.assertEquals(configurationMap.get("tenant"), "tenant");
            //Transport 1
            Assert.assertEquals(transport1.get("name"), "abc");
            Assert.assertEquals(transport1.get("port"), 8000);
            Assert.assertEquals(transport1.get("secure"), false);
            Assert.assertEquals(transport1.get("desc"), "This transport will use 8000 as its port");
            Assert.assertEquals(transport1.get("password"), PASSWORD);
            //Transport 2
            Assert.assertEquals(transport2.get("name"), "pqr");
            Assert.assertEquals(transport2.get("port"), 8501);
            Assert.assertEquals(transport2.get("secure"), true);
            Assert.assertEquals(transport2.get("desc"),
                    "This transport will use 8501 as its port. Secure - true");
            //Transport 3
            Assert.assertEquals(transport3.get("name"), "xyz");
            Assert.assertEquals(transport3.get("port"), 9000);
            Assert.assertEquals(transport3.get("secure"), true);
            Assert.assertEquals(transport3.get("desc"),
                    "This transport will use 8888 as its port");
        } catch (CarbonConfigurationException e) {
            logger.error(e.toString());
            Assert.fail();
        }
    }

    @Test(expectedExceptions = CarbonConfigurationException.class, description = "This test will test functionality " +
            "when " +
            "yaml config file not found")
    public void yamlFileNotFoundTestCase() throws CarbonConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader("Example1.yaml");
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader);
        Configurations configurations = configProvider.getConfigurationObject(Configurations.class);
        Assert.assertNull(configurations, "configurations object should be null");
    }

    @Test(description = "This test will test functionality when configurations are not found in yaml file and " +
            "configuration map")
    public void invalidYAMLConfigMapTestCase() throws CarbonConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader("invalidconfiguration.yaml");
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader);
        Map configurationMap = configProvider.getConfigurationMap("configurations");
        Assert.assertNull(configurationMap, "configurations map should be null, since no configuration found in yaml");
    }

    @Test(description = "This test will test functionality when configurations are not found in yaml file and " +
            "configuration object")
    public void invalidYAMLConfigObjectTestCase() throws CarbonConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader("invalidconfiguration.yaml");
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader);
        Configurations configurations = configProvider.getConfigurationObject(Configurations.class);

        Assert.assertEquals(configurations.getTenant(), "default");
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).getName(), "default transport");
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPort(), 8000);
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).isSecure(), "false");
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).getDesc(),
                "Default Transport Configurations");
        Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPassword(), "zzz");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "System property.*",
            description = "This test will test functionality when configurations are not found in yaml file and " +
            "configuration object")
    public void yamlConfigWithoutSystemValueTestCase() throws CarbonConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader("systemconfigwithoutdefaults.yaml");
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader);
        Configurations configurations = configProvider.getConfigurationObject(Configurations.class);
        Assert.assertNull(configurations, "configurations object should be null");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Environment variable.*",
            description = "This test will test functionality when configurations are not found in yaml file and " +
                    "configuration object")
    public void yamlConfigWithoutEnvValueTestCase() throws CarbonConfigurationException {
        ConfigFileReader fileReader = new YAMLBasedConfigFileReader("envconfigwithoutdefaults.yaml");
        ConfigProvider configProvider = new ConfigProviderImpl(fileReader);
        Configurations configurations = configProvider.getConfigurationObject(Configurations.class);
        Assert.assertNull(configurations, "configurations object should be null");
    }
}
