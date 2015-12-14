package org.wso2.carbon.osgi.transport;

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.transports.CarbonTransport;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;

/**
 * OSGi tests class to test org.wso2.carbon.kernel.transports.CarbonTransport as OSGi service registration.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TransportServiceOSGiTest {
    private static final String TRANSPORT_ID = "DummyTransport";

    @Inject
    BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Test
    public void testRegisterTransport() {
        ServiceRegistration serviceRegistration = bundleContext.registerService(CarbonTransport.class,
                new CustomCarbonTransport(TRANSPORT_ID), null);
        ServiceReference reference = bundleContext.getServiceReference(CarbonTransport.class.getName());
        Assert.assertNotNull(reference, "Custom Carbon Transport Service Reference is null");
        CustomCarbonTransport customCarbonTransport = (CustomCarbonTransport) bundleContext.getService(reference);
        Assert.assertNotNull(customCarbonTransport, "Custom Carbon Transport Service is null");
        serviceRegistration.unregister();
        reference = bundleContext.getServiceReference(CustomCarbonTransport.class.getName());
        Assert.assertNull(reference, "Custom Carbon Transport Service Reference should be unregistered and null");
    }
}
