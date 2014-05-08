package org.wso2.carbon.kernel.region.hooks;

import org.osgi.framework.hooks.resolver.ResolverHook;
import org.osgi.framework.hooks.resolver.ResolverHookFactory;
import org.osgi.framework.wiring.BundleRevision;

import java.util.Collection;

public class RegionResolverHookFactory implements ResolverHookFactory {

    @Override
    public ResolverHook begin(Collection<BundleRevision> bundleRevisions) {
        return new RegionResolverHook();
    }
}
