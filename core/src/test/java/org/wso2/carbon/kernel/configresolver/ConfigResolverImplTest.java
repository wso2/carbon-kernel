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
package org.wso2.carbon.kernel.configresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.configresolver.configfiles.AbstractConfigFile;
import org.wso2.carbon.kernel.configresolver.configfiles.Properties;
import org.wso2.carbon.kernel.configresolver.configfiles.XML;
import org.wso2.carbon.kernel.configresolver.configfiles.YAML;
import org.wso2.carbon.kernel.internal.configresolver.ConfigResolverImpl;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * This class is to demonstrate the sample uses of the ConfigUtil class.
 *
 * @since 5.2.0
 */
public class ConfigResolverImplTest {

    private static Logger logger = LoggerFactory.getLogger(ConfigResolverImplTest.class.getName());
    private static String basedir = System.getProperty("basedir");
    private ConfigResolver configResolver;

    @BeforeTest
    public void setup() {
        setUpEnvironment();
        configResolver = new ConfigResolverImplEx();
        if (basedir == null) {
            basedir = Paths.get("").toAbsolutePath().toString();
        }
    }

    @Test(description = "This test will test functionality when using xml config file")
    public void xmlExample() {
        setUpEnvironment();
        try {
            Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example.xml");
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

            XML configXml = configResolver.getConfig(file, XML.class);
            String value = configXml.getContent();

            Source xmlInput = new StreamSource(new StringReader(value));
            jaxbContext = JAXBContext.newInstance(Configurations.class);
            unmarshaller = jaxbContext.createUnmarshaller();
            configurations = (Configurations) unmarshaller.unmarshal(xmlInput);

            //Transport 1
            Assert.assertEquals(configurations.getTenant(), "new_tenant");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getName(), "abc");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPort(), 8001);
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).isSecure(), "true");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getDesc(),
                    "This transport will use 8000 as its port");
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
        } catch (JAXBException e) {
            logger.error(e.toString());
            Assert.fail();
        }
    }

    @Test(description = "This test will test functionality when using yaml config file")
    public void yamlExample() {
        setUpEnvironment();
        try {
            Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example.yaml");
            File file = resourcePath.toFile();
            FileInputStream fileInputStream = new FileInputStream(file);
            Yaml yaml = new Yaml();
            Map map = yaml.loadAs(fileInputStream, Map.class);
            ArrayList transports = (ArrayList) map.get("transports");
            LinkedHashMap transport1 = (LinkedHashMap) ((LinkedHashMap) transports.get(0)).get("transport");
            LinkedHashMap transport2 = (LinkedHashMap) ((LinkedHashMap) transports.get(1)).get("transport");
            LinkedHashMap transport3 = (LinkedHashMap) ((LinkedHashMap) transports.get(2)).get("transport");

            Assert.assertEquals(map.get("tenant"), "tenant");
            //Transport 1
            Assert.assertEquals(transport1.get("name"), "abc");
            Assert.assertEquals(transport1.get("port"), 8000);
            Assert.assertEquals(transport1.get("secure"), false);
            Assert.assertEquals(transport1.get("desc"), "This transport will use 8000 as its port");
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

            fileInputStream = new FileInputStream(file);
            YAML configYaml = configResolver.getConfig(fileInputStream, file.getName(), YAML.class);
            yaml = new Yaml();
            String value = configYaml.getContent();

            map = yaml.loadAs(value, Map.class);
            transports = (ArrayList) map.get("transports");
            transport1 = (LinkedHashMap) ((LinkedHashMap) transports.get(0)).get("transport");
            transport2 = (LinkedHashMap) ((LinkedHashMap) transports.get(1)).get("transport");
            transport3 = (LinkedHashMap) ((LinkedHashMap) transports.get(2)).get("transport");

            Assert.assertEquals(map.get("tenant"), "new_tenant");
            //Transport 1
            Assert.assertEquals(transport1.get("name"), "abc");
            Assert.assertEquals(transport1.get("port"), 8001);
            Assert.assertEquals(transport1.get("secure"), true);
            Assert.assertEquals(transport1.get("desc"), "This transport will use 8000 as its port");
            //Transport 2
            Assert.assertEquals(transport2.get("name"), "pqr");
            Assert.assertEquals(transport2.get("port"), 8501);
            Assert.assertEquals(transport2.get("secure"), true);
            Assert.assertEquals(transport2.get("desc"), "This transport will use 8501 as its port. Secure - true");
            //Transport 3
            Assert.assertEquals(transport3.get("name"), "xyz");
            Assert.assertEquals(transport3.get("port"), 9000);
            Assert.assertEquals(transport3.get("secure"), true);
            Assert.assertEquals(transport3.get("desc"), "This transport will use 8888 as its port");
        } catch (FileNotFoundException e) {
            logger.error(e.toString());
            Assert.fail();
        }
    }

    @Test(description = "This test will test functionality when using properties config file")
    public void propertiesExample() {
        setUpEnvironment();
        FileInputStream fileInputStream = null;
        try {
            Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example.properties");
            File file = resourcePath.toFile();
            fileInputStream = new FileInputStream(file);
            java.util.Properties properties = new java.util.Properties();
            properties.load(fileInputStream);

            Assert.assertEquals(properties.get("tenant"), "tenant");
            //Transport 1
            Assert.assertEquals(properties.get("transport.abc.port"), "8000");
            Assert.assertEquals(properties.get("transport.abc.secure"), "false");
            Assert.assertEquals(properties.get("transport.abc.desc"), "This transport will use 8000 as its port");
            //Transport 2
            Assert.assertEquals(properties.get("transport.pqr.port"), "${env:pqr.http.port}");
            Assert.assertEquals(properties.get("transport.pqr.secure"), "${sys:pqr.secure}");
            Assert.assertEquals(properties.get("transport.pqr.desc"),
                    "This transport will use ${env:pqr.http.port} as its port. Secure - ${sys:pqr.secure}");
            //Transport 3
            Assert.assertEquals(properties.get("transport.xyz.port"), "${env:xyz.http.port,9000}");
            Assert.assertEquals(properties.get("transport.xyz.secure"), "${sys:xyz.secure,true}");
            Assert.assertEquals(properties.get("transport.xyz.desc"),
                    "This transport will use ${env:xyz.http.port,8888} as its port");

            fileInputStream = new FileInputStream(file);
            Properties configProperties = configResolver.getConfig(fileInputStream, file.getName(),
                    Properties.class);
            properties = new java.util.Properties();
            String value = configProperties.getContent();

            properties.load(new StringReader(value));
            Assert.assertEquals(properties.get("tenant"), "new_tenant");
            //Transport 1
            Assert.assertEquals(properties.get("transport.abc.port"), "8001");
            Assert.assertEquals(properties.get("transport.abc.secure"), "true");
            Assert.assertEquals(properties.get("transport.abc.desc"), "This transport will use 8000 as its port");

            //Transport 2
            Assert.assertEquals(properties.get("transport.pqr.port"), "8501");
            Assert.assertEquals(properties.get("transport.pqr.secure"), "true");
            Assert.assertEquals(properties.get("transport.pqr.desc"),
                    "This transport will use 8501 as its port. Secure - true");

            //Transport 3
            Assert.assertEquals(properties.get("transport.xyz.port"), "9000");
            Assert.assertEquals(properties.get("transport.xyz.secure"), "true");
            Assert.assertEquals(properties.get("transport.xyz.desc"), "This transport will use 8888 as its port");
        } catch (IOException e) {
            logger.error(e.toString());
            Assert.fail();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    logger.warn("Error occurred while closing the InputStream.", e);
                }
            }
        }
    }

    @Test(description = "This test will test functionality of getConfig method")
    public void getConfigTest1() {
        setUpEnvironment();
        String newValue = configResolver.getConfig("[Example.xml]/configurations/tenant");
        Assert.assertEquals(newValue, "new_tenant");
        newValue = configResolver.getConfig("[Example.xml]/configurations/transports/transport/port");
        Assert.assertEquals(newValue, "8001");
        newValue = configResolver.getConfig(
                "[Example.xml]/configurations/transports/transport[name='abc']/@secure");
        Assert.assertEquals(newValue, "true");
        newValue = configResolver.getConfig("[Example.xml]/configurations/transports/transport[name='pqr']/port");
        Assert.assertEquals(newValue, null);
        newValue = configResolver.getConfig("[Example.xml]/configurations/transports/transport[2]/@secure");
        Assert.assertEquals(newValue, null);

        newValue = configResolver.getConfig("[Example.yaml]/tenant");
        Assert.assertEquals(newValue, "new_tenant");
        newValue = configResolver.getConfig("[Example.yaml]/transports/transport/port");
        Assert.assertEquals(newValue, "8001");
        newValue = configResolver.getConfig("[Example.yaml]/transports/transport[name='abc']/secure");
        Assert.assertEquals(newValue, "true");
        newValue = configResolver.getConfig("[Example.yaml]/transports/transport[name='pqr']/port");
        Assert.assertEquals(newValue, null);
        newValue = configResolver.getConfig("[Example.yaml]/transports[2]/transport/secure");
        Assert.assertEquals(newValue, null);

        newValue = configResolver.getConfig("[Example.properties]/tenant");
        Assert.assertEquals(newValue, "new_tenant");
        newValue = configResolver.getConfig("[Example.properties]/transport.abc.port");
        Assert.assertEquals(newValue, "8001");
        newValue = configResolver.getConfig("[Example.properties]/transport.abc.secure");
        Assert.assertEquals(newValue, "true");
        newValue = configResolver.getConfig("[Example.properties]/transport.pqr.port");
        Assert.assertEquals(newValue, null);
        newValue = configResolver.getConfig("[Example.properties]/transport.pqr.secure");
        Assert.assertEquals(newValue, null);
    }

    @Test(expectedExceptions = RuntimeException.class, description = "This method tries to get the value of a key "
            + "where value is a System property placeholder and the placeholder is not found")
    public void getConfigTest2() {
        setCarbonHome();
        System.clearProperty("abc.http.port");
        String newValue = configResolver.getConfig("[Example.xml]/configurations/transports/transport/port");
        logger.debug(newValue);
    }

    @Test(expectedExceptions = RuntimeException.class, description = "This method tries to get the value of a key "
            + "where value is a Environment variable placeholder and the placeholder is not found")
    public void getConfigTest3() {
        setCarbonHome();
        String newValue = configResolver.getConfig("[Example.xml]/configurations/transports/transport/port");
        logger.debug(newValue);
    }

    @Test(expectedExceptions = RuntimeException.class, description = "This method tries to get the value of a key "
            + "where the key is invalid xpath")
    public void getConfigTest4() {
        setCarbonHome();
        String newValue = configResolver.getConfig("[Example.xml]configurations");
        logger.debug(newValue);
    }

    @Test(expectedExceptions = RuntimeException.class, description = "Test the situations where deployment.properies "
            + "have additional configs which is not in the original config file")
    public void invalidConfigTest() {
        setUpEnvironment();
        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example2.xml");
        File file = resourcePath.toFile();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            XML configXml = configResolver.getConfig(fileInputStream, "Example.xml", XML.class);
            logger.debug(configXml.getContent());
        } catch (FileNotFoundException e) {
            logger.warn("File Not found: " + file.getAbsolutePath());
            Assert.fail();
        }
    }

    @Test(expectedExceptions = RuntimeException.class, description = "Test the situations where the util tries to " +
            "process xml config file with errors")
    public void invalidXmlTest() {
        setUpEnvironment();
        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example4.xml");
        File file = resourcePath.toFile();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            XML configXml = configResolver.getConfig(fileInputStream, "Example.xml", XML.class);
            logger.debug(configXml.getContent());
        } catch (FileNotFoundException e) {
            logger.warn("File Not found: " + file.getAbsolutePath());
            Assert.fail();
        }
    }

    @Test(expectedExceptions = RuntimeException.class, description = "Test the util when trying to read a non existing"
            + " config file")
    public void invalidFileTest() {
        setUpEnvironment();
        Path resourcePath = Paths.get(basedir, "Example3.xml");
        File file = resourcePath.toFile();
        XML configXml = configResolver.getConfig(file, XML.class);
        logger.debug(configXml.getContent());
    }

    @Test(description = "Test for keys that is not in the deployment.properties file")
    public void invalidFileNameTest() {
        setUpEnvironment();
        String tenant = configResolver.getConfig("[Example2.xml]/configurations/tenant/name");
        Assert.assertNull(tenant);
    }

    @Test(expectedExceptions = RuntimeException.class, description = "Test configs with not initialized System "
            + "properties")
    public void invalidSysPropTest() {
        setUpEnvironment();
        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example3.xml");
        File file = resourcePath.toFile();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            XML configs = configResolver.getConfig(fileInputStream, "Example3.xml", XML.class);
            logger.debug(configs.getContent());
        } catch (FileNotFoundException e) {
            Assert.fail();
        }
    }

    @Test(expectedExceptions = RuntimeException.class, description = "Test configs with not initialized Environment "
            + "variables")
    public void invalidEnvVarTest() {
        setUpEnvironment();
        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example3.xml");
        File file = resourcePath.toFile();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            XML configs = configResolver.getConfig(fileInputStream, "Example4.xml", XML.class);
            logger.debug(configs.getContent());
        } catch (FileNotFoundException e) {
            Assert.fail();
        }
    }

    @Test(description = "Test configs with not initialized System properties, but have default values")
    public void validSysPropWithDefaultTest() {
        setUpEnvironment();
        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example3.xml");
        File file = resourcePath.toFile();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            XML configs = configResolver.getConfig(fileInputStream, "Example6.xml", XML.class);
            Assert.assertNotNull(configs.getContent());
        } catch (FileNotFoundException e) {
            Assert.fail();
        }
    }

    @Test(description = "Test configs with not initialized Environment variables, but have default values")
    public void validEnvVarWithDefaultTest() {
        setUpEnvironment();
        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example3.xml");
        File file = resourcePath.toFile();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            XML configs = configResolver.getConfig(fileInputStream, "Example5.xml", XML.class);
            Assert.assertNotNull(configs.getContent());
        } catch (FileNotFoundException e) {
            Assert.fail();
        }
    }

    @Test(expectedExceptions = RuntimeException.class, description = "Test what happens when custom file type is "
            + "provided")
    public void invalidFileType() {
        setCarbonHome();
        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example3.xml");
        File file = resourcePath.toFile();
        TestFile configs = configResolver.getConfig(file, TestFile.class);
        logger.debug(configs.getContent());
    }

    @Test(priority = 1, description = "Test the functionality when deployment.properties file is not available")
    public void depFileNotAvailable() {
        setUpEnvironment();
        reloadDeploymentPropertiesFile();
        Path carbonHome = Paths.get("");
        System.setProperty(Constants.CARBON_HOME, carbonHome.toString());
        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example.xml");
        File file = resourcePath.toFile();
        XML configs = configResolver.getConfig(file, XML.class);
        Assert.assertNotNull(configs);
    }

    @Test(priority = 2, description = "Test what happens when deployment.conf system property is empty")
    public void deploymentConfSysPropEmptyTest() {
        setUpEnvironment();
        System.setProperty("deployment.conf", "");
        reloadDeploymentPropertiesFile();

        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example2.xml");
        File file = resourcePath.toFile();
        try {
            XML configXml = configResolver.getConfig(file, XML.class);
            String value = configXml.getContent();
            Source xmlInput = new StreamSource(new StringReader(value));
            JAXBContext jaxbContext = JAXBContext.newInstance(Configurations.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Configurations configurations = (Configurations) unmarshaller.unmarshal(xmlInput);
            Assert.assertEquals(configurations.getTenant(), "tenant");
        } catch (JAXBException e) {
            Assert.fail();
        }
    }

    @Test(priority = 2, description = "Test what happens when deployment.conf found in System Properties")
    public void deploymentConfSysPropTest() {
        Path depConfPath = Paths.get(basedir, "src", "test", "resources", "conf", "dep_sys.properties");
        System.setProperty("deployment.conf", depConfPath.toString());
        reloadDeploymentPropertiesFile();

        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example2.xml");
        File file = resourcePath.toFile();
        try {
            XML configXml = configResolver.getConfig(file, XML.class);
            String value = configXml.getContent();
            Source xmlInput = new StreamSource(new StringReader(value));
            JAXBContext jaxbContext = JAXBContext.newInstance(Configurations.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Configurations configurations = (Configurations) unmarshaller.unmarshal(xmlInput);
            Assert.assertEquals(configurations.getTenant(), "new_tenant_sys");
        } catch (JAXBException e) {
            Assert.fail();
        }
    }

    @Test(priority = 3, description = "Test what happens when deployment.conf Environment variable is empty")
    public void deploymentConfEnvVarEmptyTest() {
        setUpEnvironment();
        Map<String, String> envVarMap = new HashMap<>();
        envVarMap.put("deployment.conf", "");
        setEnv(envVarMap);
        System.setProperty("deployment.conf", "");
        reloadDeploymentPropertiesFile();

        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example2.xml");
        File file = resourcePath.toFile();
        try {
            XML configXml = configResolver.getConfig(file, XML.class);
            String value = configXml.getContent();
            Source xmlInput = new StreamSource(new StringReader(value));
            JAXBContext jaxbContext = JAXBContext.newInstance(Configurations.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Configurations configurations = (Configurations) unmarshaller.unmarshal(xmlInput);
            Assert.assertEquals(configurations.getTenant(), "tenant");
        } catch (JAXBException e) {
            Assert.fail();
        }
    }

    @Test(priority = 3, description = "Test what happens when deployment.conf found in Environment Variables")
    public void deploymentConfEnvVarTest() {
        Path depConfPath = Paths.get(basedir, "src", "test", "resources", "conf", "dep_env.properties");
        Map<String, String> envVarMap = new HashMap<>();
        envVarMap.put("deployment.conf", depConfPath.toString());
        setEnv(envVarMap);
        reloadDeploymentPropertiesFile();

        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example2.xml");
        File file = resourcePath.toFile();
        try {
            XML configXml = configResolver.getConfig(file, XML.class);
            String value = configXml.getContent();
            Source xmlInput = new StreamSource(new StringReader(value));
            JAXBContext jaxbContext = JAXBContext.newInstance(Configurations.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Configurations configurations = (Configurations) unmarshaller.unmarshal(xmlInput);
            Assert.assertEquals(configurations.getTenant(), "new_tenant_env");
        } catch (JAXBException e) {
            Assert.fail();
        }
    }

    @Test(priority = 4, expectedExceptions = RuntimeException.class, description =
            "This will try to read deployment.properties file with invalid key. "
                    + "This will result in an XPathExpressionException")
    public void getConfigTest5() {
        Path depConfPath = Paths.get(basedir, "src", "test", "resources", "conf", "error_dep.properties");
        Map<String, String> envVarMap = new HashMap<>();
        envVarMap.put("deployment.conf", depConfPath.toString());
        setEnv(envVarMap);
        reloadDeploymentPropertiesFile();

        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example2.xml");
        File file = resourcePath.toFile();
        XML configXml = configResolver.getConfig(file, XML.class);
        String value = configXml.getContent();
        logger.debug(value);
    }

    @Test(priority = 5, description = "Test what happens when invalid file path is provided")
    public void invalidFilePath() {
        Path depConfPath = Paths.get(basedir, "src", "test", "resources", "conf", "dep.properties");
        Map<String, String> envVarMap = new HashMap<>();
        envVarMap.put("deployment.conf", depConfPath.toString());
        setEnv(envVarMap);
        reloadDeploymentPropertiesFile();

        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configresolver", "Example2.xml");
        File file = resourcePath.toFile();
        XML configXml = configResolver.getConfig(file, XML.class);
        String value = configXml.getContent();
        logger.debug(value);
    }

    //This method is used to reload the config from deployment.properties. This helps to improve test coverage by
    // reloading the configs at runtime
    private void reloadDeploymentPropertiesFile() {
        try {
            Method method = ConfigResolverImpl.class.getDeclaredMethod("loadConfigs");
            method.setAccessible(true);
            method.invoke(null);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Assert.fail();
        }
    }

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
        setEnv(envVarMap);
        //This is how to set System properties
        System.setProperty("abc.http.port", "8001");
        System.setProperty("sample.xyz.port", "9091");
        System.setProperty("pqr.secure", "true");
    }

    @SuppressWarnings("unchecked")
    private static void setEnv(Map<String, String> newenv) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField(
                    "theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for (Class cl : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newenv);
                    }
                }
            } catch (Exception e2) {
                logger.debug(e2.toString());
            }
        } catch (Exception e1) {
            logger.debug(e1.toString());
        }
    }

    class TestFile extends AbstractConfigFile {

        public TestFile(String value) {
            super(value);
        }
    }
}


