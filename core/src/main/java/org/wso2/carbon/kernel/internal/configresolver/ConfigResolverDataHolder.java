package org.wso2.carbon.kernel.internal.configresolver;

import org.wso2.carbon.kernel.securevault.SecureVault;

import java.util.Optional;

/**
 * Config Resolver Data Holder.
 *
 * @since 5.2.0
 */
public class ConfigResolverDataHolder {
    private static ConfigResolverDataHolder instance = new ConfigResolverDataHolder();
    Optional<SecureVault> optSecureVault = Optional.empty();

    public static ConfigResolverDataHolder getInstance() {
        return instance;
    }

    private ConfigResolverDataHolder() {
    }

    public Optional<SecureVault> getOptSecureVault() {
        return optSecureVault;
    }

    public void setOptSecureVault(Optional<SecureVault> optSecureVault) {
        this.optSecureVault = optSecureVault;
    }
}
