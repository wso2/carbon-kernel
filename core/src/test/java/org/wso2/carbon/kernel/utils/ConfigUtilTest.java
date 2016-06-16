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
package org.wso2.carbon.kernel.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.utils.configfiletypes.Properties;
import org.wso2.carbon.kernel.utils.configfiletypes.XML;
import org.wso2.carbon.kernel.utils.configfiletypes.YAML;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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
public class ConfigUtilTest {

    private static Logger logger = LoggerFactory.getLogger(ConfigUtilTest.class.getName());
    private static String basedir;

    @BeforeTest
    public void setup() {
        basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get("").toAbsolutePath().toString();
        }
    }

    @Test
    public void xmlExample() {
        setUpEnvironment();
        try {
            Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configutil", "Example.xml");
            File file = new File(resourcePath.toString());

            JAXBContext jaxbContext = JAXBContext.newInstance(Configurations.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Configurations configurations = (Configurations) unmarshaller.unmarshal(file);

            //Transport 1
            Assert.assertEquals(configurations.getTenant(), "tenant");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getName(), "abc");
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPort(), 8000);
            Assert.assertEquals(configurations.getTransports().getTransport().get(0).isSecure(), "false");
            //Transport 2
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getName(), "pqr");
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).getPort(), 0);
            Assert.assertEquals(configurations.getTransports().getTransport().get(1).isSecure(), "$sys:pqr.secure");
            //Transport 3
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getName(), "xyz");
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).getPort(), 0);
            Assert.assertEquals(configurations.getTransports().getTransport().get(2).isSecure(),
                    "$sys:xyz.secure,true");

            XML configXml = ConfigUtil.getConfig(file, XML.class);

            Optional<String> value = configXml.getValue();
            if (value.isPresent()) {
                Source xmlInput = new StreamSource(new StringReader(value.get()));
                jaxbContext = JAXBContext.newInstance(Configurations.class);
                unmarshaller = jaxbContext.createUnmarshaller();
                configurations = (Configurations) unmarshaller.unmarshal(xmlInput);

                //Transport 1
                Assert.assertEquals(configurations.getTenant(), "new_tenant");
                Assert.assertEquals(configurations.getTransports().getTransport().get(0).getName(), "abc");
                Assert.assertEquals(configurations.getTransports().getTransport().get(0).getPort(), 8001);
                Assert.assertEquals(configurations.getTransports().getTransport().get(0).isSecure(), "true");
                //Transport 2
                Assert.assertEquals(configurations.getTransports().getTransport().get(1).getName(), "pqr");
                Assert.assertEquals(configurations.getTransports().getTransport().get(1).getPort(), 8501);
                Assert.assertEquals(configurations.getTransports().getTransport().get(1).isSecure(), "true");
                //Transport 3
                Assert.assertEquals(configurations.getTransports().getTransport().get(2).getName(), "xyz");
                Assert.assertEquals(configurations.getTransports().getTransport().get(2).getPort(), 9000);
                Assert.assertEquals(configurations.getTransports().getTransport().get(2).isSecure(), "true");
            }
        } catch (JAXBException e) {
            logger.error(e.toString());
            Assert.fail();
        }
    }

    @Test
    public void ymlExample() {
        setUpEnvironment();
        try {
            Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configutil", "Example.yml");
            File file = new File(resourcePath.toString());
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
            //Transport 2
            Assert.assertEquals(transport2.get("name"), "pqr");
            Assert.assertEquals(transport2.get("port"), "$env:pqr.http.port");
            Assert.assertEquals(transport2.get("secure"), "$sys:pqr.secure");
            //Transport 3
            Assert.assertEquals(transport3.get("name"), "xyz");
            Assert.assertEquals(transport3.get("port"), "$env:xyz.http.port,9000");
            Assert.assertEquals(transport3.get("secure"), "$sys:xyz.secure,true");

            fileInputStream = new FileInputStream(file);
            YAML configYml = ConfigUtil.getConfig(fileInputStream, file.getName(), YAML.class);
            yaml = new Yaml();
            Optional<String> value = configYml.getValue();
            if (value.isPresent()) {
                map = yaml.loadAs(value.get(), Map.class);
                transports = (ArrayList) map.get("transports");
                transport1 = (LinkedHashMap) ((LinkedHashMap) transports.get(0)).get("transport");
                transport2 = (LinkedHashMap) ((LinkedHashMap) transports.get(1)).get("transport");
                transport3 = (LinkedHashMap) ((LinkedHashMap) transports.get(2)).get("transport");

                Assert.assertEquals(map.get("tenant"), "new_tenant");
                //Transport 1
                Assert.assertEquals(transport1.get("name"), "abc");
                Assert.assertEquals(transport1.get("port"), 8001);
                Assert.assertEquals(transport1.get("secure"), true);
                //Transport 2
                Assert.assertEquals(transport2.get("name"), "pqr");
                Assert.assertEquals(transport2.get("port"), 8501);
                Assert.assertEquals(transport2.get("secure"), true);
                //Transport 3
                Assert.assertEquals(transport3.get("name"), "xyz");
                Assert.assertEquals(transport3.get("port"), 9000);
                Assert.assertEquals(transport3.get("secure"), true);
            }
        } catch (FileNotFoundException e) {
            logger.error(e.toString());
            Assert.fail();
        }
    }

    @Test
    public void propertiesExample() {
        setUpEnvironment();
        try {
            Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configutil", "Example.properties");
            File file = new File(resourcePath.toString());
            FileInputStream fileInputStream = new FileInputStream(file);
            java.util.Properties properties = new java.util.Properties();
            properties.load(fileInputStream);

            Assert.assertEquals(properties.get("tenant"), "tenant");
            //Transport 1
            Assert.assertEquals(properties.get("transport.abc.port"), "8000");
            Assert.assertEquals(properties.get("transport.abc.secure"), "false");
            //Transport 2
            Assert.assertEquals(properties.get("transport.pqr.port"), "$env:pqr.http.port");
            Assert.assertEquals(properties.get("transport.pqr.secure"), "$sys:pqr.secure");
            //Transport 3
            Assert.assertEquals(properties.get("transport.xyz.port"), "$env:xyz.http.port,9000");
            Assert.assertEquals(properties.get("transport.xyz.secure"), "$sys:xyz.secure,true");

            fileInputStream = new FileInputStream(file);
            Properties configProperties = ConfigUtil.getConfig(fileInputStream, file.getName(), Properties.class);
            properties = new java.util.Properties();
            Optional<String> value = configProperties.getValue();
            if (value.isPresent()) {
                properties.load(new StringReader(value.get()));
                Assert.assertEquals(properties.get("tenant"), "new_tenant");
                //Transport 1
                Assert.assertEquals(properties.get("transport.abc.port"), "8001");
                Assert.assertEquals(properties.get("transport.abc.secure"), "true");
                //Transport 2
                Assert.assertEquals(properties.get("transport.pqr.port"), "8501");
                Assert.assertEquals(properties.get("transport.pqr.secure"), "true");
                //Transport 3
                Assert.assertEquals(properties.get("transport.xyz.port"), "9000");
                Assert.assertEquals(properties.get("transport.xyz.secure"), "true");
            }
        } catch (IOException e) {
            logger.error(e.toString());
            Assert.fail();
        }
    }

    @Test
    public void getConfigTest() {

        setUpEnvironment();

        String newValue = ConfigUtil.getConfig("[Example.xml]/configurations/tenant");
        Assert.assertEquals(newValue, "new_tenant");
        newValue = ConfigUtil.getConfig("[Example.xml]/configurations/transports/transport/port");
        Assert.assertEquals(newValue, "$sys:abc.http.port");
        newValue = ConfigUtil.getConfig("[Example.xml]/configurations/transports/transport[name='abc']/@secure");
        Assert.assertEquals(newValue, "true");
        newValue = ConfigUtil.getConfig("[Example.xml]/configurations/transports/transport[name='pqr']/port");
        Assert.assertEquals(newValue, null);
        newValue = ConfigUtil.getConfig("[Example.xml]/configurations/transports/transport[2]/@secure");
        Assert.assertEquals(newValue, null);

        newValue = ConfigUtil.getConfig("[Example.yml]/tenant");
        Assert.assertEquals(newValue, "new_tenant");
        newValue = ConfigUtil.getConfig("[Example.yml]/transports/transport/port");
        Assert.assertEquals(newValue, "$sys:abc.http.port");
        newValue = ConfigUtil.getConfig("[Example.yml]/transports/transport[name='abc']/secure");
        Assert.assertEquals(newValue, "true");
        newValue = ConfigUtil.getConfig("[Example.yml]/transports/transport[name='pqr']/port");
        Assert.assertEquals(newValue, null);
        newValue = ConfigUtil.getConfig("[Example.yml]/transports[2]/transport/secure");
        Assert.assertEquals(newValue, null);

        newValue = ConfigUtil.getConfig("[Example.properties]/tenant");
        Assert.assertEquals(newValue, "new_tenant");
        newValue = ConfigUtil.getConfig("[Example.properties]/transport.abc.port");
        Assert.assertEquals(newValue, "$sys:abc.http.port");
        newValue = ConfigUtil.getConfig("[Example.properties]/transport.abc.secure");
        Assert.assertEquals(newValue, "$sys:abc.port.secure,true");
        newValue = ConfigUtil.getConfig("[Example.properties]/transport.pqr.port");
        Assert.assertEquals(newValue, null);
        newValue = ConfigUtil.getConfig("[Example.properties]/transport.pqr.secure");
        Assert.assertEquals(newValue, null);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void invalidConfigTest() {
        setUpEnvironment();
        Path resourcePath = Paths.get(basedir, "src", "test", "resources", "configutil", "Example2.xml");
        File file = new File(resourcePath.toString());
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            XML configXml = ConfigUtil.getConfig(fileInputStream, "Example.xml", XML.class);
            logger.debug(configXml.getValue().orElse("No data"));
        } catch (FileNotFoundException e) {
            logger.warn("File Not found: " + file.getAbsolutePath());
            Assert.fail();
        }
    }

    private void setUpEnvironment() {
        Path carbonHome = Paths.get("");
        carbonHome = Paths.get(carbonHome.toString(), "src", "test", "resources");
        System.setProperty(Constants.CARBON_HOME, carbonHome.toString());
        logger.debug("Carbon Home Absolute path set to: " + carbonHome.toAbsolutePath());

        //This is how to set Environment Variables
        Map<String, String> envVarMap = new HashMap<>();
        envVarMap.put("pqr.http.port", "8501");
        setEnv(envVarMap);
        //This is how to set System properties
        System.setProperty("abc.http.port", "8001");
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
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
                    .getDeclaredField("theCaseInsensitiveEnvironment");
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
}
