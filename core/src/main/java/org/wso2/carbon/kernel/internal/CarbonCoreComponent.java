package org.wso2.carbon.kernel.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;
import org.wso2.carbon.kernel.internal.context.CarbonRuntimeFactory;


/**
 * This service component creates a carbon runtime based on the carbon configuration file and registers it as a
 * ${@link CarbonRuntime}.
 *
 * @since 5.2.0
 */
@Component(
        name = "org.wso2.carbon.kernel.internal.CarbonCoreComponent",
        immediate = true
)
public class CarbonCoreComponent {
    private static final Logger logger = LoggerFactory.getLogger(CarbonCoreComponent.class);

    @Activate
    public void activate() {
        try {
            logger.debug("Activating CarbonCoreComponent");

            // 1) Get config provider from data holder
            ConfigProvider configProvider = DataHolder.getInstance().getConfigProvider();

            // 2) Creates the CarbonRuntime instance using the Carbon configuration provider.
            CarbonRuntime carbonRuntime = CarbonRuntimeFactory.createCarbonRuntime(configProvider);

            // 3) Register CarbonRuntime instance as an OSGi bundle.
            DataHolder.getInstance().getBundleContext()
                    .registerService(CarbonRuntime.class.getName(), carbonRuntime, null);

        } catch (Throwable throwable) {
            logger.error("Error while activating CarbonCoreComponent");
        }
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Deactivating CarbonCoreComponent");
    }

    @Reference(
            name = "carbon.core.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        DataHolder.getInstance().setConfigProvider(configProvider);
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        DataHolder.getInstance().setConfigProvider(null);
    }
}
