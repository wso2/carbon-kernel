package org.wso2.osgi.spi.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.BundleTracker;
import org.wso2.osgi.spi.junk.Junk;
import org.wso2.osgi.spi.registrar.ServiceRegistrar;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class ServiceBundleTracker<T> extends BundleTracker<T> {

    private List<ConsumerBundle> consumers = new ArrayList<>();
    private List<ProviderBundle> providers = new ArrayList<>();

    private final BundleCapability mediatorProcessorCapability;
    private final BundleCapability mediatorRegistrarCapability;
    private boolean isTracked = false;

    public ServiceBundleTracker(BundleContext context, int stateMask) {

        super(context, stateMask, null);
        BundleWiring mediatorBundleWiring = context.getBundle().adapt(BundleWiring.class);

        List<BundleCapability> mediatorCapabilities = mediatorBundleWiring.
                getCapabilities(Constants.EXTENDER_CAPABILITY_NAMESPACE);

        BundleCapability processorCapability = null;
        BundleCapability registrarCapability = null;

        for (BundleCapability mediatorCapability : mediatorCapabilities) {

            if (mediatorCapability.getAttributes().containsKey(Constants.EXTENDER_CAPABILITY_NAMESPACE)) {
                String extenderCapabilityType = mediatorCapability.getAttributes()
                        .get(Constants.EXTENDER_CAPABILITY_NAMESPACE).toString();

                if (extenderCapabilityType.equals(Constants.PROCESSOR_EXTENDER_NAME)) {
                    processorCapability = mediatorCapability;
                } else if (extenderCapabilityType.equals(Constants.REGISTRAR_EXTENDER_NAME)) {
                    registrarCapability = mediatorCapability;
                }
            }
        }
        // TODO: 2/8/16 throw runtime exception if null capability found
        this.mediatorProcessorCapability = processorCapability;
        this.mediatorRegistrarCapability = registrarCapability;
    }

    @Override
    public T addingBundle(Bundle bundle, BundleEvent event) {

        isTracked = false;
        findConsumers(bundle);
        findProviders(bundle);

        if (isTracked) {
            System.out.println("TrackerCustom Added: " + bundle.getSymbolicName() + " Event: " + Junk.typeAsString(event));
            return super.addingBundle(bundle, event);
        }

        return null;

    }

    private void findConsumers(Bundle bundle) {

        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        if (bundleWiring == null) {
            return;
        }

        List<BundleRequirement> requirements = bundleWiring.getRequirements(Constants.EXTENDER_CAPABILITY_NAMESPACE);

        for (BundleRequirement requirement : requirements) {
            if (requirement.matches(mediatorProcessorCapability)) {
                consumers.add(new ConsumerBundle(bundle));
                isTracked = true;
                break;
            }
        }
    }

    private void findProviders(Bundle bundle) {

        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        if (bundleWiring == null) {
            return;
        }

        List<BundleCapability> capabilities = bundleWiring.getCapabilities(Constants.SERVICELOADER_NAMESPACE);
        boolean requireRegistrar = false;
        boolean isProvider = false;

        if (!capabilities.isEmpty()) {
            isProvider = true;
        }

        List<BundleRequirement> requirements = bundleWiring.getRequirements(Constants.EXTENDER_CAPABILITY_NAMESPACE);
        for (BundleRequirement requirement : requirements) {
            if (requirement.matches(mediatorRegistrarCapability)) {
                requireRegistrar = true;
                break;
            }
        }

        if(isProvider) {
            providers.add(new ProviderBundle(bundle,requireRegistrar));
            isTracked =true;
        }
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent event, T object) {

        super.modifiedBundle(bundle, event, object);
        System.out.println("TrackerCustom Modified: " + bundle.getSymbolicName() + " Event: " + Junk.typeAsString(event));

        if (this.isProvider(bundle)) {
            ProviderBundle providerBundle = this.getProvider(bundle);
            if (event.getType() == BundleEvent.STARTING && providerBundle.requireRegistrar()) {
                ServiceRegistrar.register(providerBundle);
            } else if (event.getType() == BundleEvent.STOPPING && providerBundle.requireRegistrar()) {
                ServiceRegistrar.unregister(providerBundle);
            }
        }

    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, T object) {
        super.removedBundle(bundle, event, object);
        System.out.println("Tracker Custom Remove: " + bundle.getSymbolicName() + " Event: " + Junk.typeAsString(event));

    }


    public boolean isConsumer(Bundle bundle) {
        for (ConsumerBundle consumerBundle : consumers) {
            if (bundle.equals(consumerBundle.getConsumerBundle())) {
                return true;
            }
        }
        return false;
    }

    public boolean isProvider(Bundle bundle) {
        for (ProviderBundle providerBundle : providers) {
            if (bundle.equals(providerBundle.getProviderBundle())) {
                return true;
            }
        }
        return false;
    }


    public ConsumerBundle getConsumer(Bundle bundle) {
        for (ConsumerBundle consumerBundle : consumers) {
            if (bundle.equals(consumerBundle.getConsumerBundle())) {
                return consumerBundle;
            }
        }
        return null;
    }

    public ProviderBundle getProvider(Bundle bundle) {
        for (ProviderBundle providerBundle : providers) {
            if (bundle.equals(providerBundle.getProviderBundle())) {
                return providerBundle;
            }
        }
        return null;
    }

    public List<ProviderBundle> getMatchingProviders(String requestingServiceType, ConsumerBundle consumerBundle) {

        List<ProviderBundle> selectedProviders = new ArrayList<>();

        if (consumerBundle.isVisibilityRestricted()) {

            List<Bundle> visibleBundles = consumerBundle.getVisibleBundles();

            for (Bundle visibleBundle : visibleBundles) {
                if (isProvider(visibleBundle)) {
                    selectedProviders.add(getProvider(visibleBundle));
                }
            }

        } else {
            selectedProviders.addAll(providers);
        }


        return selectedProviders;
    }


}
