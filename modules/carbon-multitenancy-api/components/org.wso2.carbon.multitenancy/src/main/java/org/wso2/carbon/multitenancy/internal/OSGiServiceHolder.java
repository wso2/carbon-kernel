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

import org.wso2.carbon.multitenancy.TenantRuntime;
import org.wso2.carbon.multitenancy.api.TenantListener;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class OSGiServiceHolder {

    private static OSGiServiceHolder serviceHolder = new OSGiServiceHolder();

    private List<TenantListener> tenantListeners = new ArrayList<>();
    private TenantRuntime tenantRuntime;

    public static OSGiServiceHolder getInstance() {
        return serviceHolder;
    }

    private OSGiServiceHolder() {
    }


    public List<TenantListener> getTenantListeners() {
        return tenantListeners;
    }

    public void addTenantListener(TenantListener tenantListener) {
        tenantListeners.add(tenantListener);
    }

    public void removeTenantListener(TenantListener tenantListener) {
        tenantListeners.remove(tenantListener);
    }

    public void setTenantRuntime(TenantRuntime tenantRuntime) {
        this.tenantRuntime = tenantRuntime;
    }

    public TenantRuntime getTenantRuntime() {
        return tenantRuntime;
    }

}

