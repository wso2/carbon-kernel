package org.wso2.carbon.kernel.internal.configresolver;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.configresolver.ConfigResolver;
import org.wso2.carbon.kernel.securevault.SecureVault;

import java.util.Optional;

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

    @Reference(
            name = "config.resolver.secure.vault",
            service = SecureVault.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterSecureVault"
    )
    protected void registerSecureVault(SecureVault secureVault) {
        ConfigResolverDataHolder.getInstance().setOptSecureVault(Optional.ofNullable(secureVault));
    }

    protected void unRegisterSecureVault(SecureVault secureVault) {
        ConfigResolverDataHolder.getInstance().setOptSecureVault(Optional.empty());
    }
}
