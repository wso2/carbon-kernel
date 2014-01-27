package org.wso2.carbon.base.test;


import junit.framework.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.base.Constants;
import org.wso2.carbon.base.Utils;

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

    @Test (expectedExceptions = Exception.class)
    public void testGetCarbonConfigDirPath() {
        Utils.getCarbonConfigDirPath();
    }

    @Test (expectedExceptions = Exception.class)
    public void testGetCarbonHome() {
        // this will throw null since it require system property carbon.home
        Utils.getCarbonHome();
    }

    @Test
    public void testCarbonHome() {
        Assert.assertEquals(carbonHome, getCarbonHome().getAbsolutePath());
    }

    @Test (expectedExceptions = Exception.class)
    public void testGetServerXml() {
        Utils.getServerXml();
    }

    @Test
    public void testServerXml() {
        String serverXml = carbonHome + File.separator + "repository"
                + File.separator + "conf" + File.separator + "carbon.xml";
        Assert.assertEquals(serverXml, getServerXml().getAbsolutePath());
    }

}
