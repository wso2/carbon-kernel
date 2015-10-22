package org.wso2.carbon.internal.runtime;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.deployment.exception.DeployerRegistrationException;
import org.wso2.carbon.deployment.exception.DeploymentEngineException;
import org.wso2.carbon.runtime.spi.Runtime;

import java.util.List;

public class RuntimeManagerTest {

    private RuntimeManager runtimeManager;
    private Runtime runtime;
    @BeforeTest
    public void setup() throws DeploymentEngineException, DeployerRegistrationException {
        runtimeManager = new RuntimeManager();
        runtime =  new CustomRuntime();
        runtimeManager.registerRuntime(runtime);
    }

    @Test
    public void testGetRuntimesList() {
        List<Runtime> runtimeList = runtimeManager.getRuntimeList();
        Assert.assertTrue(runtimeList.size() == 1 && runtimeList.get(0) == runtime);
    }

    @Test(dependsOnMethods = {"testGetRuntimesList"})
    public void testUnRegisterRuntime() {
        runtimeManager.unRegisterRuntime(runtime);
        Assert.assertTrue(runtimeManager.getRuntimeList().size() == 0);
    }
}
