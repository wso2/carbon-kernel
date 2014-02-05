package org.wso2.carbon.runtime;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import org.wso2.carbon.runtime.exception.RuntimeServiceException;
import org.wso2.carbon.runtime.runtime.CustomRuntime;

public class CustomRuntimeTest {
    CustomRuntime customRuntime;

    public CustomRuntimeTest() {

    }

    @BeforeTest
    public void setup() throws RuntimeServiceException {
        customRuntime = new CustomRuntime();
    }

    @Test
    public void testInitRuntime() {
        customRuntime.init();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.INACTIVE);
    }

    @Test(dependsOnMethods = {"testInitRuntime"})
    public void testRuntimeStart() throws RuntimeServiceException {
        customRuntime.start();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.ACTIVE);
    }

    @Test(dependsOnMethods = {"dependsOnMethods"})
    public void testRuntimeStartMaintenance() throws RuntimeServiceException {
        customRuntime.beginMaintenance();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.INACTIVE);
    }

    @Test(dependsOnMethods = {"dependsOnMethods"})
    public void testRuntimeStopMaintenance() throws RuntimeServiceException {
        customRuntime.endMaintenance();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.INACTIVE);
    }

    @Test(dependsOnMethods = {"dependsOnMethods"})
    public void testRuntimeStop() throws RuntimeServiceException {
        customRuntime.stop();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.ACTIVE);
    }


}
