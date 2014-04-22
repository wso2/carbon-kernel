package org.wso2.carbon.kernel.region.hook;

import org.osgi.framework.hooks.resolver.ResolverHook;
import org.osgi.framework.hooks.resolver.ResolverHookFactory;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@Component (name = "org.wso2.carbon.kernel.region.hook.RegionResolverHookFactoryDSComponent")
public class RegionResolverHookFactory implements ResolverHookFactory {

    private static final Logger logger = LoggerFactory.getLogger(RegionResolverHookFactory.class);
    @Override
    public ResolverHook begin(Collection<BundleRevision> bundleRevisions) {
        logger.info("Creating RegionResolverHook for bundle revisions : {}", bundleRevisions);
        return new RegionResolverHook();
    }
}
