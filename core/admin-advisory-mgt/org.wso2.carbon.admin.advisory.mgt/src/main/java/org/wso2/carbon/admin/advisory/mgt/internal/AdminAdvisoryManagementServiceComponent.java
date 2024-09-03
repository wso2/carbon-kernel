/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.admin.advisory.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.admin.advisory.mgt.service.AdminAdvisoryManagementService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * OSGi declarative services component which handles
 * registration and unregistration of AdminAdvisoryManagementServiceComponent.
 */
@Component(name = "admin.advisory.mgt.service.component", immediate = true)
public class AdminAdvisoryManagementServiceComponent {

    private static final Log log = LogFactory.getLog(AdminAdvisoryManagementServiceComponent.class);
    private AdminAdvisoryManagementDataHolder dataHolder = AdminAdvisoryManagementDataHolder.getInstance();

    @Activate
    protected void activate(ComponentContext context) {

        BundleContext bundleContext = context.getBundleContext();
        try {
            bundleContext.registerService(AdminAdvisoryManagementService.class.getName(),
                    new AdminAdvisoryManagementService(), null);
            log.debug("AdminAdvisoryManagement bundle is activated");
        } catch (Throwable e) {
            log.error("Failed to activate AdminAdvisoryManagement bundle", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        log.debug("AdminAdvisoryManagement bundle is deactivated");
    }

    @Reference(
            name = "registry.service",
            service = RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService"
    )
    protected void setRegistryService(RegistryService registryService) {

        dataHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {

        dataHolder.setRegistryService(null);
    }

    @Reference(
            name = "user.realmservice.default",
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        dataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        dataHolder.setRealmService(null);
    }
}
