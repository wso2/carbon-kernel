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
 *
 * @since 5.0.0
 */
@Component(
        name = "org.wso2.carbon.sample.transport.http.CustomTransportServiceCapabilityProvider",
        immediate = true,
        property = "capability-name=org.wso2.carbon.sample.transport.mgt.Transport"
)
public class CustomTransportServiceCapabilityProvider implements CapabilityProvider {
    private static final int customTransportServiceCount = 3;

    @Activate
    protected void start(BundleContext bundleContext) {
        IntStream.range(0, customTransportServiceCount).forEach(
                count -> bundleContext.registerService(Transport.class, new CustomTransport(), null)
        );
    }

    @Override
    public int getCount() {
        return customTransportServiceCount;
    }
}
