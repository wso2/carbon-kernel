package org.wso2.carbon.kernel.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.bundle.CollisionHook;
import org.osgi.framework.hooks.bundle.EventHook;
import org.osgi.framework.hooks.bundle.FindHook;
import org.osgi.framework.hooks.resolver.ResolverHookFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.kernel.region.KernelRegion;
import org.wso2.carbon.kernel.region.RegionManager;
import org.wso2.carbon.kernel.region.hooks.RegionBundleCollisionHook;
import org.wso2.carbon.kernel.region.hooks.RegionBundleEventHook;
import org.wso2.carbon.kernel.region.hooks.RegionBundleFindHook;
import org.wso2.carbon.kernel.region.hooks.RegionResolverHookFactory;

import java.util.ArrayList;
import java.util.Collection;

@Component(
        name = "org.wso2.carbon.kernel.internal.CarbonKernelServiceComponent",
        immediate = true
)
public class CarbonKernelServiceComponent {

    private KernelRegion kernelRegion;
    private RegionManager regionManager;

    private Collection<ServiceRegistration<?>> registrations = new ArrayList<>();

    @Activate
    protected void start(BundleContext bundleContext) {
        //Populate kernel region
        kernelRegion = new KernelRegion("Server");
        for (Bundle bundle : bundleContext.getBundles()) {
            try {
                kernelRegion.addBundle(bundle);
            } catch (BundleException e) {
                e.printStackTrace();
            }
        }
        OSGiServiceHolder.getInstance().setKernelRegion(kernelRegion);

        // Register region related hooks
        registrations.add(bundleContext.registerService(CollisionHook.class.getName(),
                                                        new RegionBundleCollisionHook(), null));
        registrations.add(bundleContext.registerService(ResolverHookFactory.class.getName(),
                                                        new RegionResolverHookFactory(), null));
        registrations.add(bundleContext.registerService(FindHook.class.getName(),
                                                        new RegionBundleFindHook(), null));
        registrations.add(bundleContext.registerService(EventHook.class.getName(),
                                                        new RegionBundleEventHook(), null));
    }

    @Deactivate
    protected void stop(BundleContext bundleContext) {
        OSGiServiceHolder.getInstance().setKernelRegion(null);
        regionManager.dissociateRegion(kernelRegion);
        for (ServiceRegistration registration : registrations) {
            registration.unregister();
        }
    }

    @Reference(
            name = "carbon.tenant.region.manager.service",
            service = RegionManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC,
            unbind = "unsetTenantRegionManagerService"
    )

    public void setTenantRegionManagerService(RegionManager regionManager) {
        OSGiServiceHolder.getInstance().setRegionManager(regionManager);
        this.regionManager = regionManager;
    }

    public void unsetTenantRegionManagerService(RegionManager regionManager) {
        OSGiServiceHolder.getInstance().setRegionManager(null);
        this.regionManager = null;
    }
}
