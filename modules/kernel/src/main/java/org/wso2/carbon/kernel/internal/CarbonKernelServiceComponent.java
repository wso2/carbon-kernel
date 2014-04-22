package org.wso2.carbon.kernel.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.kernel.region.BundleToRegionManager;
import org.wso2.carbon.kernel.region.KernelRegion;
import org.wso2.carbon.kernel.region.Region;
import org.wso2.carbon.kernel.region.RegionManager;

@Component(
        name = "org.wso2.carbon.kernel.internal.CarbonKernelServiceComponent",
        immediate = true
)
public class CarbonKernelServiceComponent {

    @Activate
    protected void start(BundleContext bundleContext) {
        //Populate kernel region
        KernelRegion kernelRegion = new KernelRegion("Server");
        for (Bundle bundle : bundleContext.getBundles()) {
            try {
                kernelRegion.addBundle(bundle);
            } catch (BundleException e) {
                e.printStackTrace();
            }
        }
        OSGiServiceHolder.getInstance().setKernelRegion(kernelRegion);
    }

    @Deactivate
    protected void stop(BundleContext bundleContext) {
        OSGiServiceHolder.getInstance().setKernelRegion(null);
    }

    @Reference(
            name = "carbon.tenant.region.manager.service",
            service = RegionManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTenantRegionManagerService"
    )

    public void setTenantRegionManagerService(RegionManager regionManager) {
        OSGiServiceHolder.getInstance().setRegionManager(regionManager);
    }

    public void unsetTenantRegionManagerService(RegionManager regionManager) {
        OSGiServiceHolder.getInstance().setRegionManager(null);
    }
}
