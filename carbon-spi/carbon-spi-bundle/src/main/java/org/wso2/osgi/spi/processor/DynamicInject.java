package org.wso2.osgi.spi.processor;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;
import org.osgi.framework.wiring.BundleWiring;
import org.wso2.osgi.spi.internal.ConsumerBundle;
import org.wso2.osgi.spi.internal.ServiceBundleTracker;
import org.wso2.osgi.spi.internal.ProviderBundle;
import org.wso2.osgi.spi.internal.ServiceLoaderActivator;
import org.wso2.osgi.spi.security.Permissions;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

public class DynamicInject {

    static ThreadLocal<ClassLoader> storedClassLoaders = new ThreadLocal<>();

    public static void storeContextClassloader() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                storedClassLoaders.set(Thread.currentThread().getContextClassLoader());
                return null;
            }
        });

    }

    public static void restoreContextClassloader() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                Thread.currentThread().setContextClassLoader(storedClassLoaders.get());
                storedClassLoaders.set(null);
                return null;
            }
        });
    }

    public static void fixContextClassloader(Class<?> serviceType, ClassLoader consumerBundleLoader) {

        if (!(consumerBundleLoader instanceof BundleReference)) {
            return;
        }

        BundleReference consumerBundleReference = ((BundleReference) consumerBundleLoader);

        final ClassLoader contextClassloader = findContextClassloader(consumerBundleReference.getBundle(), serviceType);
        if (contextClassloader != null) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    Thread.currentThread().setContextClassLoader(contextClassloader);
                    return null;
                }
            });
        }
    }

    private static ClassLoader findContextClassloader(Bundle consumerBundle, Class<?> serviceType) {

        if(ServiceLoaderActivator.getInstance() == null){
            return null;
        }
        ConsumerBundle consumer = ServiceLoaderActivator.getInstance().getServiceBundleTracker().getConsumer(consumerBundle);

        if(!Permissions.canConsumeService(consumer,serviceType.getName())){
            return null;
        }

        ServiceBundleTracker s = ServiceLoaderActivator.getInstance().getServiceBundleTracker();

        List<ProviderBundle> providerBundles = s.getMatchingProviders(serviceType.getName(), consumer);

        if (providerBundles.size() == 0) {
            return null;
        } else if (providerBundles.size() == 1) {
            return getProviderBundleClassLoader(providerBundles.get(0));
        } else {
            List<ClassLoader> loaders = new ArrayList<>();
            for (ProviderBundle providerBundle : providerBundles) {
                loaders.add(getProviderBundleClassLoader(providerBundle));
            }
            return new CombinedClassLoader(loaders);
        }
    }

    private static ClassLoader getProviderBundleClassLoader(final ProviderBundle providerBundle) {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return providerBundle.getProviderBundle().adapt(BundleWiring.class).getClassLoader();
            }
        });
    }

}
