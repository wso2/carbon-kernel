package org.wso2.carbon.kernel.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.kernel.region.BundleToRegionManager;
import org.wso2.carbon.kernel.region.RegionManager;

@Component(
        name = "org.wso2.carbon.kernel.internal.CarbonKernelServiceComponent",
        immediate = true
)
public class CarbonKernelServiceComponent {

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
