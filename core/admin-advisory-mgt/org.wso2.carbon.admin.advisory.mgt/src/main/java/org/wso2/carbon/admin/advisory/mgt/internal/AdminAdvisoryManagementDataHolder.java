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

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * This singleton data holder contains all the data required by the admin advisory management OSGi bundle.
 */
public class AdminAdvisoryManagementDataHolder {

    private static AdminAdvisoryManagementDataHolder instance = new AdminAdvisoryManagementDataHolder();
    private RegistryService registryService;
    private RealmService realmService;

    /**
     * Get the AdminAdvisoryManagementDataHolder instance.
     *
     * @return AdminAdvisoryManagementDataHolder instance.
     */
    public static AdminAdvisoryManagementDataHolder getInstance() {

        return instance;
    }

    /**
     * Get the registry service.
     *
     * @return RegistryService instance.
     */
    public RegistryService getRegistryService() {

        return registryService;
    }

    /**
     * Set the registry service.
     *
     * @param registryService RegistryService instance.
     */
    public void setRegistryService(RegistryService registryService) {

        this.registryService = registryService;
    }

    /**
     * Get the realm service.
     *
     * @return RealmService instance.
     */
    public RealmService getRealmService() {

        return realmService;
    }

    /**
     * Set the realm service.
     *
     * @param realmService RealmService instance.
     */
    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }
}
