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

    @BeforeClass public void init() throws Exception {
        try (InputStream inputStream = new FileInputStream(
                new File(getTestResourceFile("xsd").getAbsolutePath() + File.separator + "carbon.xml"))) {
            carbonConfiguration = unmarshall(inputStream);
        }
    }

    @Test public void testGetId() throws Exception {
        Assert.assertEquals(carbonConfiguration.getId(), "carbon-kernel");
    }

    @Test public void testGetName() throws Exception {
        Assert.assertEquals(carbonConfiguration.getName(), "WSO2 Carbon Kernel");

    }

    @Test public void testGetVersion() throws Exception {
        Assert.assertEquals(carbonConfiguration.getVersion(), "5.0.0-SNAPSHOT");

    }

    @Test public void testGetPortsConfig() throws Exception {
        Assert.assertEquals(carbonConfiguration.getPortsConfig().getOffset(), 0);
    }

    @Test public void testGetDeploymentConfig() throws Exception {
        DeploymentConfig deploymentConfig = carbonConfiguration.getDeploymentConfig();
        Assert.assertEquals(deploymentConfig.getMode(), DeploymentModeEnum.scheduled);
        Assert.assertEquals(deploymentConfig.getRepositoryLocation(), "repository-path");
        Assert.assertEquals(deploymentConfig.getUpdateInterval(), 15);
    }
}
