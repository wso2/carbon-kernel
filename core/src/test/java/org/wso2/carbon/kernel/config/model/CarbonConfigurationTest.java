/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.kernel.config.model;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.deployment.BaseTest;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 * This class tests the functionality of org.wso2.carbon.kernel.config.model.CarbonConfiguration.
 */
public class CarbonConfigurationTest extends BaseTest {

    private CarbonConfiguration carbonConfiguration;

    public CarbonConfigurationTest(String testName) {
        super(testName);
    }

    private static CarbonConfiguration unmarshall(InputStream stream) throws JAXBException, IOException, SAXException {
        String xmlString = IOUtils.toString(stream, "UTF-8");

        JAXBContext jaxbContext = JAXBContext.newInstance(CarbonConfiguration.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        return (CarbonConfiguration) jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(xmlString)));
    }

    @BeforeClass
    public void init() throws Exception {
        try (InputStream inputStream = new FileInputStream(
                new File(getTestResourceFile("xsd").getAbsolutePath() + File.separator + "carbon.xml"))) {
            carbonConfiguration = unmarshall(inputStream);
        }
    }

    @Test
    public void testGetId() throws Exception {
        Assert.assertEquals(carbonConfiguration.getId(), "carbon-kernel");
    }

    @Test
    public void testGetName() throws Exception {
        Assert.assertEquals(carbonConfiguration.getName(), "WSO2 Carbon Kernel");

    }

    @Test
    public void testGetVersion() throws Exception {
        Assert.assertEquals(carbonConfiguration.getVersion(), "1.2.3");

    }

    @Test
    public void testGetPortsConfig() throws Exception {
        Assert.assertEquals(carbonConfiguration.getPortsConfig().getOffset(), 0);
    }

    @Test
    public void testGetDeploymentConfig() throws Exception {
        DeploymentConfig deploymentConfig = carbonConfiguration.getDeploymentConfig();
        Assert.assertEquals(deploymentConfig.getMode(), DeploymentModeEnum.scheduled);
        Assert.assertEquals(deploymentConfig.getRepositoryLocation(), "repository-path");
        Assert.assertEquals(deploymentConfig.getUpdateInterval(), 15);
    }
}
