package org.wso2.carbon.sample.transport.jetty;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.kernel.startupresolver.CapabilityProvider;

/**
 * Jetty TransportServiceCapabilityProvider class that registers JettyHttpTransport as a service multiple times to test
 * the startup order resolver implementation.
 *
 * @since 5.0.0
 */
@Component(
        name = "org.wso2.carbon.sample.transport.jetty.JettyTransportServiceCapabilityProvider",
        immediate = true,
        property = "capability-name=org.wso2.carbon.sample.transport.mgt.Transport"
)
public class JettyTransportServiceCapabilityProvider implements CapabilityProvider {
    private static final int customTransportServiceCount = 3;

    @Activate
    protected void start(BundleContext bundleContext) {
        //do nothing as this is used test negative scenario of pending capability registration
    }

    @Override
    public int getCount() {
        return customTransportServiceCount;
    }
}
