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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.multitenancy.api.TenantListener;

/**
 * TenantManagement Declarative Services Component.
 *
 * @since 1.0.0
 */
@Component(
        name = "org.wso2.carbon.multitenancy.internal.TenantListenerComponent",
        immediate = true
)
public class TenantListenerComponent {

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
}
