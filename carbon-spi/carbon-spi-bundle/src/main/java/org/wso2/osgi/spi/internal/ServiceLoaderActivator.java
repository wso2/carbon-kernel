package org.wso2.osgi.spi.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.osgi.spi.registrar.ServiceRegistrar;

public class ServiceLoaderActivator implements BundleActivator {

    private static ServiceLoaderActivator instance = null;

    private ServiceBundleTracker serviceBundleTracker = null;

    private long bundleId;

    public void start(BundleContext context) throws Exception {

        System.out.println("Mediator Bundle Starting");
        instance = this;
        bundleId = context.getBundle().getBundleId();

        int trackStates = Bundle.STARTING | Bundle.STOPPING | Bundle.RESOLVED | Bundle.INSTALLED | Bundle.UNINSTALLED | Bundle.ACTIVE;
        serviceBundleTracker = new ServiceBundleTracker(context, trackStates);
        serviceBundleTracker.open();

        System.out.println("Mediator Bundle Started");
    }

    public void stop(BundleContext context) throws Exception {
        serviceBundleTracker.close();
        ServiceRegistrar.unregisterAll();
        System.out.println("Mediator Bundle Stopped");
    }

    public static ServiceLoaderActivator getInstance() {
        return instance;
    }

    public ServiceBundleTracker getServiceBundleTracker() {
        return serviceBundleTracker;
    }

    public long getBundleId() {
        return bundleId;
    }
}
