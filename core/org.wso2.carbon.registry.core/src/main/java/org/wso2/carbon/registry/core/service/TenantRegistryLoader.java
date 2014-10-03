/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.service;

import org.wso2.carbon.CarbonException;
import org.wso2.carbon.utils.AuthenticationObserver;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * Initializes the tenant's registry space. This interface also implements the
 * {@link Axis2ConfigurationContextObserver} as well as the {@link AuthenticationObserver}, which
 * will ensure that the registry will be automatically initialized during the lazy loading of a
 * tenant, or during the process of authentication.
 */
public interface TenantRegistryLoader {

    /**
     * Load the Registry for the given tenant. This will automatically populate mounts, media types
     * and handlers for the tenant. It is a must that this method is called foe each tenant before
     * accessing the registry instance.
     * <p/>
     * For the super-tenant, calling this method will have no impact, as the super-tenant will
     * always be loaded at the start-up.
     *
     * @param tenantId the tenant id of the system. The tenant id '0', corresponds to the super
     *                 tenant of the system, whereas identifiers greater than '0' correspond to
     *                 valid tenants.
     */
    void loadTenantRegistry(int tenantId) throws CarbonException;

}
