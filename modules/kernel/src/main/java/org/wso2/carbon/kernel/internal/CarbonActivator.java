package org.wso2.carbon.kernel.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wso2.carbon.kernel.config.CarbonConfigProvider;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.internal.config.XMLBasedConfigProvider;
import org.wso2.carbon.kernel.internal.context.CarbonRuntimeFactory;
import org.wso2.carbon.kernel.region.BundleToRegionManager;
import org.wso2.carbon.kernel.region.RegionManager;

import java.util.ArrayList;
import java.util.Collection;


public class CarbonActivator implements BundleActivator {

    private Collection<ServiceRegistration<?>> registrations = new ArrayList<>();

    public void start(BundleContext bundleContext) throws Exception {

        OSGiServiceHolder.getInstance().setBundleContext(bundleContext);

        // 1) Find to initialize the Carbon configuration provider
        CarbonConfigProvider configProvider = new XMLBasedConfigProvider();

        // 2) Creates the CarbonRuntime instance using the Carbon configuration provider.
        CarbonRuntime carbonRuntime = CarbonRuntimeFactory.createCarbonRuntime(configProvider);

        // 3) Register CarbonRuntime instance as an OSGi bundle.
        registrations.add(bundleContext.registerService(CarbonRuntime.class.getName(),
                                                        carbonRuntime, null));

        // Register Region manager
        registrations.add(bundleContext.registerService(RegionManager.class.getName(),
                                                        new BundleToRegionManager(), null));

    }

    public void stop(BundleContext bundleContext) throws Exception {
        OSGiServiceHolder.getInstance().setBundleContext(null);

        for (ServiceRegistration registration : registrations) {
            registration.unregister();
        }
    }
}
