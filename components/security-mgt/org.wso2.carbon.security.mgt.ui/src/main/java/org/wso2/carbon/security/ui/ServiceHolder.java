/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security.ui;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.registry.core.service.RegistryService;

public class ServiceHolder {

    private static ServiceHolder instance = null;

    private static BundleContext bundleContext;

    private static ServiceTracker registryTracker = null;

    private ServiceHolder() {

    }


    public static void init(BundleContext context) {
        bundleContext = context;
        registryTracker = new ServiceTracker(bundleContext, RegistryService.class.getName(), null);
        registryTracker.open();
        instance = new ServiceHolder();

    }

    public static ServiceHolder getInstance() {
        return instance;
    }

    public RegistryService getRegistryService() {
        return (RegistryService) registryTracker.getService();
    }


}