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
package org.wso2.carbon.axis2.runtime.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.kernel.transports.CarbonTransport;

/**
 * Netty Axis2 Runtime DataHolder.
 *
 * @since 1.0.0
 */
public class DataHolder {
    private static DataHolder instance = new DataHolder();
    private BundleContext bundleContext = null;
    private ConfigurationContext configurationContext = null;
    private CarbonTransport carbonTransport = null;

    public static DataHolder getInstance() {
        return instance;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public CarbonTransport getCarbonTransport() {
        return carbonTransport;
    }

    public void setCarbonTransport(CarbonTransport carbonTransport) {
        this.carbonTransport = carbonTransport;
    }
}
