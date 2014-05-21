package org.wso2.carbon.kernel;

import junit.framework.Assert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.internal.OSGiServiceHolder;
import org.wso2.carbon.kernel.region.DefaultRegionManager;
import org.wso2.carbon.kernel.region.KernelRegion;
import org.wso2.carbon.kernel.region.RegionBundle;
import org.wso2.carbon.kernel.region.RegionManager;
import org.wso2.carbon.kernel.region.TenantRegion;

import java.util.UUID;

public class RegionTestCase {
    private RegionManager regionManager;
    @BeforeTest
    public void init() {
        regionManager = new DefaultRegionManager();
        OSGiServiceHolder.getInstance().setRegionManager(regionManager);
    }

    @Test (groups = {"wso2.carbon.kernel.region"}, description = "sample tenant region creation test")
    public void createRegionTest() throws BundleException {
        String tenantId = UUID.randomUUID().toString();
        TenantRegion tenantRegion = new TenantRegion(tenantId);
        Bundle bundle = new RegionBundle(1);
        tenantRegion.addBundle(bundle);
        Assert.assertEquals(1, tenantRegion.getBundles().size());
        for (Bundle regionBundle : tenantRegion.getBundles()) {
            Assert.assertEquals(bundle.getBundleId(), regionBundle.getBundleId());
        }
        Assert.assertEquals(tenantRegion, regionManager.getRegion(bundle.getBundleId()));
        tenantRegion.removeBundle(bundle);
        regionManager.dissociateRegion(tenantRegion);
    }

    @Test (groups = {"wso2.carbon.kernel.region"}, description = "multiple tenant region creation test")
    public void createMultipleRegionTest() throws BundleException {
        String tenantId1 = UUID.randomUUID().toString();
        String tenantId2 = UUID.randomUUID().toString();

        TenantRegion region1 = new TenantRegion(tenantId1);
        TenantRegion region2 = new TenantRegion(tenantId2);

        Bundle bundle1 = new RegionBundle(2);
        Bundle bundle2 = new RegionBundle(3);

        region1.addBundle(bundle1);
        region2.addBundle(bundle2);

        Assert.assertNotSame(regionManager.getRegion(bundle1.getBundleId()),
                             regionManager.getRegion(bundle2.getBundleId()));

        try {
            region1.addBundle(bundle2);
        } catch (BundleException e) {
            Assert.assertEquals("Bundle : " + bundle2.getBundleId() + " is already associated " +
                                "with region : " + region2, e.getMessage());
        }
    }

    @Test (groups = {"wso2.carbon.kernel.region"}, description = "kernel region creation test")
    public void kernelRegionTest() throws BundleException {
        KernelRegion kernelRegion = new KernelRegion("Server");
        Bundle kernelBundle = new RegionBundle(4);
        kernelRegion.addBundle(kernelBundle);
        Assert.assertEquals(1, kernelRegion.getBundles().size());
        for (Bundle regionBundle : kernelRegion.getBundles()) {
            Assert.assertEquals(kernelBundle.getBundleId(), regionBundle.getBundleId());
        }
        Assert.assertEquals(kernelRegion, regionManager.getRegion(kernelBundle.getBundleId()));
        kernelRegion.removeBundle(kernelBundle);
        regionManager.dissociateRegion(kernelRegion);
    }

    @Test (groups = {"wso2.carbon.kernel.region"}, description = "region manager specific test")
    public void regionManagerTest() {
        String tenantId = UUID.randomUUID().toString();
        TenantRegion tenantRegion = new TenantRegion(tenantId);
        String tenantDomain = "a";
        regionManager.associateTenantWithRegion(tenantDomain, tenantRegion);
        Assert.assertEquals(tenantRegion, regionManager.getRegion(tenantDomain));
    }
}
