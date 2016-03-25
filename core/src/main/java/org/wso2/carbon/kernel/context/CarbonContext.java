/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.kernel.context;

import org.wso2.carbon.kernel.internal.context.CarbonContextHolder;
import org.wso2.carbon.kernel.tenant.Tenant;

import java.security.Principal;

/**
 * This provides the API for thread local based programming for carbon based products. Each CarbonContext will utilize
 * an underlying {@link org.wso2.carbon.kernel.internal.context.CarbonContextHolder} instance, which will store the
 * actual data.
 *
 * @since 5.1.0
 */
public abstract class CarbonContext {
    private CarbonContextHolder carbonContextHolder = null;

    protected CarbonContext(CarbonContextHolder carbonContextHolder) {
        this.carbonContextHolder = carbonContextHolder;
    }

    protected CarbonContextHolder getCarbonContextHolder() {
        return carbonContextHolder;
    }

    /**
     * Method to get the currently executing tenant in the server space. The tenant domain in which the server is bound
     * is set either via a system property or a an environment property. If no value is set, the server will be
     * executing as the default tenant.
     *
     * @return the tenant instance for this server.
     */
    public Tenant getServerTenant() {
        return getCarbonContextHolder().getServerTenant();
    }

    /**
     * The current jass user principal set with this carbon context instance. If no principal is set, a null value will
     * be returned.
     *
     * @return the jass user principal in the carbon context, null if no value is already set.
     */
    public Principal getUserPrincipal() {
        return getCarbonContextHolder().getUserPrincipal();
    }

    /**
     * Method the lookup currently stored property with the carbon context instance using the given property key name.
     *
     * @param name property key name to lookup.
     * @return the value stored using the given key, or null if no value is already set.
     */
    public Object getProperty(String name) {
        return getCarbonContextHolder().getProperty(name);
    }
}
