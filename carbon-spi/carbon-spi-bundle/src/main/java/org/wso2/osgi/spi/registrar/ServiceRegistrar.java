package org.wso2.osgi.spi.registrar;

import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleCapability;
import org.wso2.osgi.spi.internal.Constants;
import org.wso2.osgi.spi.internal.ProviderBundle;
import org.wso2.osgi.spi.internal.ServiceLoaderActivator;
import org.wso2.osgi.spi.security.Permissions;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistrar {

    private static Map<ProviderBundle, List<ServiceRegistration>> serviceRegistrations = new ConcurrentHashMap<>();

    public static void register(ProviderBundle providerBundle) {
        if (!providerBundle.requireRegistrar()) {
            return;
        }

        for (BundleCapability serviceCapability : providerBundle.getServiceCapabilities()) {

            if (!serviceCapability.getAttributes().containsKey(Constants.SERVICELOADER_NAMESPACE)) {
                continue;
            }

            String serviceType = serviceCapability.getAttributes().get(Constants.SERVICELOADER_NAMESPACE).toString();

            if (!providerBundle.getAdvertisedServices().containsKey(serviceType)) {
                continue;
            }

            List<String> advertisedServiceProviders = providerBundle.getAdvertisedServices().get(serviceType);
            final Hashtable<String, ?> serviceProperties = getServiceProperties(serviceCapability);

            if (serviceCapability.getAttributes().containsKey(Constants.CAPABILITY_REGISTER_DIRECTIVE)) {
                String serviceProvider = serviceCapability.getAttributes()
                        .get(Constants.CAPABILITY_REGISTER_DIRECTIVE).toString();

                if (advertisedServiceProviders.contains(serviceProvider)) {
                    registerServiceProvider(providerBundle, serviceType, serviceProvider, serviceProperties);
                } else {
                    // TODO: 2/6/16 throw exception meta inf file invalid or empty register directive
                }

            } else {
                for (String serviceProvider : advertisedServiceProviders) {
                    registerServiceProvider(providerBundle, serviceType, serviceProvider, serviceProperties);
                }
            }
        }
    }

    private static void registerServiceProvider(ProviderBundle providerBundle, String serviceType,
                                                String serviceProvider, Hashtable<String, ?> properties) {

        if (Permissions.canRegisterService(providerBundle, serviceType)) {
            try {
                Class<?> clazz = providerBundle.getServiceProviderClass(serviceProvider);
                ServiceRegistration registration = providerBundle.getBundleContext()
                        .registerService(serviceType, new ServiceProviderFactory<>(clazz), properties);

                if (!serviceRegistrations.containsKey(providerBundle)) {
                    serviceRegistrations.put(providerBundle, new ArrayList<>());
                }
                serviceRegistrations.get(providerBundle).add(registration);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            // todo throw exception access denied
        }
    }

    private static Hashtable<String, ?> getServiceProperties(BundleCapability serviceCapability) {

        Hashtable<String, Object> serviceProperties = new Hashtable<>();
        Map<String, Object> capabilityAttributes = serviceCapability.getAttributes();

        for (Map.Entry<String, Object> attribute : capabilityAttributes.entrySet()) {
            String key = attribute.getKey();
            if (key.startsWith(".") || key.equals(Constants.SERVICELOADER_NAMESPACE) || key.equals(Constants.CAPABILITY_REGISTER_DIRECTIVE)) {
                continue;
            }
            serviceProperties.put(key, attribute.getValue());
        }

        serviceProperties.put(Constants.SERVICELOADER_MEDIATOR_PROPERTY, ServiceLoaderActivator.getInstance().getBundleId());

        return serviceProperties;
    }

    public static void unregister(ProviderBundle providerBundle) {
        List<ServiceRegistration> registrations = serviceRegistrations.remove(providerBundle);
        if (registrations != null) {
            for (ServiceRegistration registration : registrations) {
                registration.unregister();
            }
        }
    }

    public static void unregisterAll() {

        for (Map.Entry<ProviderBundle, List<ServiceRegistration>> entry : serviceRegistrations.entrySet()) {
            List<ServiceRegistration> registrations = entry.getValue();
            for (ServiceRegistration registration : registrations) {
                registration.unregister();
            }
        }
        serviceRegistrations.clear();
    }
}
