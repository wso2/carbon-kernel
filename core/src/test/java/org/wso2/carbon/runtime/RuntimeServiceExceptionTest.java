package org.wso2.carbon.runtime;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.runtime.exception.RuntimeServiceException;

public class RuntimeServiceExceptionTest {

    @Test
    public void runtimeServiceException1() {
        String errorMsg = "Test Runtime Exception";
        RuntimeServiceException runtimeServiceException = new RuntimeServiceException(errorMsg);
        Assert.assertEquals(errorMsg, runtimeServiceException.getMessage());
    }

    @Test
    public void runtimeServiceException2() {
        String errorMsg = "Test Runtime Exception";
        RuntimeServiceException runtimeServiceException = new RuntimeServiceException(errorMsg, new Exception());
        Assert.assertEquals(errorMsg, runtimeServiceException.getMessage());
    }
}
