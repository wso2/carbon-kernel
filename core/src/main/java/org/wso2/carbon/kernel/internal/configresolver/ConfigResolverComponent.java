package org.wso2.carbon.kernel.internal.configresolver;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.configresolver.ConfigResolver;

/**
 * This service component is responsible for registering ConfigResolver OSGi service.
 *
 * @since 5.2.0
 */
@Component(
        name = "org.wso2.carbon.kernel.internal.configresolver.ConfigResolverComponent",
        immediate = true
)
public class ConfigResolverComponent {
    private static final Logger logger = LoggerFactory.getLogger(ConfigResolverComponent.class);

    @Activate
    protected void start(BundleContext bundleContext) {
        try {
            ConfigResolver configResolver = new ConfigResolverImpl();
            bundleContext.registerService(ConfigResolver.class, configResolver, null);
            logger.debug("ConfigResolver OSGi service registered");
        } catch (Throwable throwable) {
            logger.error("An error occurred while activating ConfigResolverComponent", throwable);
        }
    }

    @Deactivate
    protected void stop() {
        logger.debug("Stopping ConfigResolverComponent");
    }
}
