/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.multitenancy.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.multitenancy.DefaultTenantStore;
import org.wso2.carbon.multitenancy.TenantRuntime;
import org.wso2.carbon.multitenancy.api.TenantListener;
import org.wso2.carbon.multitenancy.api.TenantStore;
import org.wso2.carbon.multitenancy.exception.TenantStoreException;

/**
 * TenantManagement Declarative Services Component.
 *
 * @since 1.0.0
 */
@Component(
        name = "org.wso2.carbon.multitenancy.internal.TenantListenerComponent",
        immediate = true,
        service = RequiredCapabilityListener.class,
        property = {
                "capability-name=org.wso2.carbon.multitenancy.api.TenantStore",
                "component-key=carbon-tenant-listener"
        }
)
public class TenantListenerComponent implements RequiredCapabilityListener {
    private static final Logger logger = LoggerFactory.getLogger(TenantListenerComponent.class);
    private TenantStore tenantStore = new DefaultTenantStore();

    @Activate
    protected void activate(BundleContext bundleContext) {

    }

    @Reference(
            service = TenantListener.class,
            policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.MULTIPLE,
            unbind = "removeListener"
    )
    protected void addListener(TenantListener tenantListener) {
        OSGiServiceHolder.getInstance().addTenantListener(tenantListener);
    }

    protected void removeListener(TenantListener tenantListener) {
        OSGiServiceHolder.getInstance().removeTenantListener(tenantListener);
    }

    @Reference(
            service = TenantStore.class,
            policy = ReferencePolicy.STATIC,
            cardinality = ReferenceCardinality.MULTIPLE
    )
    protected void setTenantStore(TenantStore tenantStore) {
        this.tenantStore = tenantStore;
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        TenantRuntime tenantRuntime;
        try {
            logger.debug("Initializing Tenant runtime with tenant store {}", tenantStore.getClass().getName());
            tenantStore.init();
            tenantRuntime = new TenantRuntime(tenantStore);
        } catch (TenantStoreException e) {
            logger.error("Error while initializing Tenant Runtime", e);
            return;
        }
        OSGiServiceHolder.getInstance().setTenantRuntime(tenantRuntime);
        final TenantRuntime finalTenantRuntime = tenantRuntime;
        OSGiServiceHolder.getInstance().getBundleContext()
                .ifPresent(bundleContext -> bundleContext.registerService(TenantRuntime.class,
                        finalTenantRuntime, null));
    }
}
