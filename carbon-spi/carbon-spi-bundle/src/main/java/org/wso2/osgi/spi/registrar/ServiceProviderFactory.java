package org.wso2.osgi.spi.registrar;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class ServiceProviderFactory<S> implements ServiceFactory<S> {

    private final Class<S> serviceProviderClass;

    public ServiceProviderFactory(Class<S> clazz) {
        serviceProviderClass = clazz;
    }

    public S getService(Bundle bundle, ServiceRegistration<S> registration) {
        System.out.println("Service Factory: " + bundle.getSymbolicName() + "--Reg :" + registration.toString());
        try {
            return serviceProviderClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void ungetService(Bundle bundle, ServiceRegistration<S> registration, S service) {
        System.out.println("Service Factory unregister: " + bundle.getSymbolicName() + "--Reg :" + registration.toString());

    }


}
