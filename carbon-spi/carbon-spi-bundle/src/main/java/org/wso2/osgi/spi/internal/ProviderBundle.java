package org.wso2.osgi.spi.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWiring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderBundle {

    private Bundle providerBundle;
    private List<BundleCapability> serviceCapabilities = new ArrayList<>();
    private Map<String, List<String>> advertisedServices = new ConcurrentHashMap<>();
    private boolean requireRegistrar = false;

    public ProviderBundle(Bundle providerBundle, boolean requireRegistrar) {
        this.providerBundle = providerBundle;
        this.requireRegistrar = requireRegistrar;
        this.processProvidedServices();
        this.processMetaServiceDescriptor();

    }

    public Bundle getProviderBundle() {
        return providerBundle;
    }

    private void processProvidedServices() {

        BundleWiring bundleWiring = providerBundle.adapt(BundleWiring.class);

        if (bundleWiring == null) {
            return;
        }
        serviceCapabilities = bundleWiring.getCapabilities(Constants.SERVICELOADER_NAMESPACE);

    }

    public boolean hasServiceType(String className) {

        for (BundleCapability serviceCapability : serviceCapabilities) {
            String serviceTypeName = serviceCapability.getAttributes().get(Constants.SERVICELOADER_NAMESPACE).toString();
            if (serviceTypeName.equals(className)) {
                return true;
            }
        }
        return false;
    }

    public boolean requireRegistrar() {
        return requireRegistrar;
    }

    private void processMetaServiceDescriptor() {
        List<URL> serviceDescriptors = Collections.list(providerBundle.findEntries(Constants.METAINF_SERVICES, "*", false));

        for (URL serviceDescriptor : serviceDescriptors) {

            try {
                String serviceFilePath = serviceDescriptor.toString();
                String serviceType = serviceFilePath.substring(serviceFilePath.lastIndexOf("/") + 1);
                String providerClassName = null;
                List<String> providerClassNames = new ArrayList<>();
                BufferedReader serviceReader = new BufferedReader(new InputStreamReader(serviceDescriptor.openStream()));


                while ((providerClassName = serviceReader.readLine()) != null) {
                    providerClassName = providerClassName.trim();

                    if (providerClassName.length() == 0)
                        continue; // empty line

                    if (providerClassName.startsWith("#"))
                        continue; // a comment

                    providerClassNames.add(providerClassName);
                }

                advertisedServices.put(serviceType, providerClassNames);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    public List<BundleCapability> getServiceCapabilities() {
        return serviceCapabilities;
    }

    public Map<String, List<String>> getAdvertisedServices() {
        return advertisedServices;
    }


    public Class<?> getServiceProviderClass(String serviceProviderName) throws ClassNotFoundException {
        return providerBundle.loadClass(serviceProviderName);
    }

    public BundleContext getBundleContext() {
        return providerBundle.getBundleContext();
    }

    public boolean hasPermission(Object permission) {
        return providerBundle.hasPermission(permission);
    }
}
