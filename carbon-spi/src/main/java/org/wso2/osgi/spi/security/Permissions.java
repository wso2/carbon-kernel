package org.wso2.osgi.spi.security;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.ServicePermission;
import org.wso2.osgi.spi.internal.ConsumerBundle;
import org.wso2.osgi.spi.internal.ProviderBundle;

public class Permissions {

    public static boolean canRegisterService(ProviderBundle providerBundle,String serviceType) {
        return providerBundle.hasPermission(new ServicePermission(serviceType, ServicePermission.REGISTER));
    }

    public static boolean canConsumeService(ConsumerBundle consumerBundle, String serviceType) {
        return consumerBundle.hasPermission(new ServicePermission(serviceType, ServicePermission.GET));
    }

    public static boolean checkAdminPermissions(ProviderBundle providerBundle,String serviceType) {
        return providerBundle.hasPermission(new AdminPermission());
    }
}
