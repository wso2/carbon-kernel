package org.wso2.osgi.spi.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

import java.util.ArrayList;
import java.util.List;

public class ConsumerBundle {

    private Bundle consumerBundle;
    private boolean isVisibilityRestricted;

    public ConsumerBundle(Bundle consumerBundle) {
        this.consumerBundle = consumerBundle;
        this.isVisibilityRestricted = false;
        this.processVisibilityRequirements();
    }

    public Bundle getConsumerBundle() {
        return consumerBundle;
    }

    private void processVisibilityRequirements() {

        BundleWiring bundleWiring = consumerBundle.adapt(BundleWiring.class);
        if (bundleWiring == null) {
            return;
        }
        List<BundleRequirement> visibilityRequirements = bundleWiring.getRequirements(Constants.SERVICELOADER_NAMESPACE);

        if (visibilityRequirements != null && !visibilityRequirements.isEmpty()) {
            isVisibilityRestricted = true;
        }

    }

    public boolean isVisibilityRestricted() {
        return isVisibilityRestricted;
    }

    public List<Bundle> getVisibleBundles() {
        BundleWiring bundleWiring = consumerBundle.adapt(BundleWiring.class);
        List<BundleWire> requiredWires = bundleWiring.getRequiredWires(Constants.SERVICELOADER_NAMESPACE);

        List<Bundle> visibleBundles = new ArrayList<>();

        for (BundleWire requiredWire : requiredWires) {
            Bundle visibleBundle = requiredWire.getProvider().getBundle();
            if (!visibleBundles.contains(visibleBundle)) {
                visibleBundles.add(requiredWire.getProvider().getBundle());
            }
        }

        return visibleBundles;

    }

    public boolean hasPermission(Object permission) {
        return consumerBundle.hasPermission(permission);
    }
}
