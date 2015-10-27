package org.wso2.carbon.osgi.runtime;

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.runtime.*;
import org.wso2.carbon.kernel.runtime.Runtime;
import org.wso2.carbon.kernel.runtime.exception.RuntimeServiceException;

import javax.inject.Inject;

/**
 * PluggableRuntimeServiceOSGiTest class is to test the availability and the functionality of the Pluggable
 * Runtime OSGi Service.
 *
 * @since 5.0.0
 */

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class PluggableRuntimeServiceOSGiTest {

    @Inject
    BundleContext bundleContext;

    @Inject
    RuntimeService runtimeService;

    @Test
    public void testPulggableRuntimeService() throws RuntimeServiceException {
        Assert.assertNotNull(bundleContext, "BundleContext instance is null");
        Assert.assertNotNull(runtimeService, "Pluggable Runtime Service is null");

        CustomRuntime customRuntime = new CustomRuntime();
        customRuntime.init();
        bundleContext.registerService(Runtime.class, customRuntime, null);

        runtimeService.startRuntimes();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.ACTIVE);
        runtimeService.beginMaintenance();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.MAINTENANCE);
        runtimeService.endMaintenance();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.INACTIVE);
        runtimeService.startRuntimes();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.ACTIVE);
        runtimeService.stopRuntimes();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.INACTIVE);
    }
}
