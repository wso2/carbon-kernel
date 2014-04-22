package org.wso2.carbon.kernel.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.kernel.config.CarbonConfigProvider;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.internal.config.XMLBasedConfigProvider;
import org.wso2.carbon.kernel.internal.context.CarbonRuntimeFactory;


public class CarbonActivator implements BundleActivator {

    public void start(BundleContext bundleContext) throws Exception {

        // 1) Find to initialize the Carbon configuration provider
        CarbonConfigProvider configProvider = new XMLBasedConfigProvider();

        // 2) Creates the CarbonRuntime instance using the Carbon configuration provider.
        CarbonRuntime carbonRuntime = CarbonRuntimeFactory.createCarbonRuntime(configProvider);

        // 3) Register CarbonRuntime instance as an OSGi bundle.
        bundleContext.registerService(CarbonRuntime.class.getName(), carbonRuntime, null);
        OSGiServiceHolder.getInstance().setBundleContext(bundleContext);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        OSGiServiceHolder.getInstance().setBundleContext(null);
    }
}
