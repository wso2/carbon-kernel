package org.wso2.carbon.base;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;

public class UtilsTest extends BaseTest {
    String carbonHome;

    public UtilsTest(String testName) {
        super(testName);
    }

    @BeforeTest
    public void doBeforeTest() {
        carbonHome = BaseTest.carbonHome;
    }

    @Test
    public void testCarbonHome() {
        Assert.assertEquals(carbonHome, getCarbonHome().getAbsolutePath());
    }

    @Test
    public void testServerXml() {
        String serverXml = carbonHome + File.separator + "repository"
                + File.separator + "conf" + File.separator + "carbon.xml";
        Assert.assertEquals(serverXml, getServerXml().getAbsolutePath());
    }

}
