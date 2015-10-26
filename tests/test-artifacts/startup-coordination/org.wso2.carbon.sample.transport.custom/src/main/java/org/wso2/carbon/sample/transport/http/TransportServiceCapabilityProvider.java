package org.wso2.carbon.sample.transport.http;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.kernel.startupresolver.CapabilityProvider;
import org.wso2.carbon.sample.transport.mgt.Transport;

import java.util.stream.IntStream;

/**
 * Sample TransportServiceCapabilityProvider class that registers CustomTransport as a service multiple times to test
 * the startup order resolver implementation.
 */
@Component(
        name = "org.wso2.carbon.sample.transport.http.TransportServiceComponent",
        immediate = true
)
public class TransportServiceCapabilityProvider implements CapabilityProvider {
    private static final int customTransportServiceCount = 3;

    @Activate
    protected void start(BundleContext bundleContext) {
        IntStream.range(0, customTransportServiceCount).forEach(
                count -> bundleContext.registerService(Transport.class, new CustomTransport(), null)
        );
    }

    @Override
    public String getName() {
        return Transport.class.getName();
    }

    @Override
    public int getCount() {
        return customTransportServiceCount;
    }
}
