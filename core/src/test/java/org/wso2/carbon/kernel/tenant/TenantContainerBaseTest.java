package org.wso2.carbon.kernel.tenant;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.deployment.BaseTest;

import java.util.Map;

public class TenantContainerBaseTest extends BaseTest {

    //for testing setters
    private TenantContainerBase tenantContainerBaseInstance1;

    private TenantContainerBase tenantContainerBaseInstance2;
    private TenantContainerBase tenantContainerBaseInstance3;
    private TenantContainerBase tenantContainerBaseInstance4;

    /**
     * @param testName
     */
    public TenantContainerBaseTest(String testName) {
        super(testName);
    }

    @BeforeClass public void init() {
        tenantContainerBaseInstance1 = new TenantContainerBase();
        tenantContainerBaseInstance2 = new TenantContainerBase();
        tenantContainerBaseInstance3 = new TenantContainerBase();
        tenantContainerBaseInstance4 = new TenantContainerBase();

        tenantContainerBaseInstance1.setId("testId1");
        tenantContainerBaseInstance2.setId("testId2");
        tenantContainerBaseInstance3.setId("testId3");
        tenantContainerBaseInstance4.setId("testId4");

        tenantContainerBaseInstance1.setParent(tenantContainerBaseInstance2);
        tenantContainerBaseInstance1.setDepthOfHierarchy(1);
        tenantContainerBaseInstance1.addChild(tenantContainerBaseInstance3);
        tenantContainerBaseInstance1.addChild(tenantContainerBaseInstance4);
    }

    @Test public void testTenantContainerBase() {
        Assert.assertEquals(tenantContainerBaseInstance1.getId(), "testId1");
        Assert.assertEquals(tenantContainerBaseInstance1.getParent(), tenantContainerBaseInstance2);
        Assert.assertEquals(tenantContainerBaseInstance1.getDepthOfHierarchy(), 1);

        Map<String, TenantContainer> childMap = tenantContainerBaseInstance1.getChildren();

        Assert.assertEquals(childMap.size(), 2);
        Assert.assertTrue(childMap.containsKey("testId3"));
        Assert.assertTrue(childMap.containsKey("testId4"));
        Assert.assertEquals(childMap.get("testId3"), tenantContainerBaseInstance3);
        Assert.assertEquals(childMap.get("testId4"), tenantContainerBaseInstance4);

    }

}
