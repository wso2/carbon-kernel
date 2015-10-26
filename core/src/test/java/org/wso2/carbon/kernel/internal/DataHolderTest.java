package org.wso2.carbon.kernel.internal;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.internal.runtime.RuntimeManager;

public class DataHolderTest {
    private DataHolder dataHolder = null;
    private RuntimeManager runtimeManager = null;

    @BeforeClass
    public void setup() {
       dataHolder = DataHolder.getInstance();
        runtimeManager = new RuntimeManager();
    }

    @Test
    public void testDataHolderGetInstance() {
        Assert.assertNotNull(dataHolder);
    }

    @Test
    public void testRuntimeManager() {
        dataHolder.setRuntimeManager(runtimeManager);
        Assert.assertEquals(runtimeManager, dataHolder.getRuntimeManager());
    }
}
