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
package org.wso2.carbon.kernel.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.internal.runtime.RuntimeManager;

/**
 * Carbon kernel DataHolder.
 *
 * @since 5.0.0
 */
public class DataHolder {
    private static DataHolder instance = new DataHolder();
    private BundleContext bundleContext;

    private RuntimeManager runtimeManager = null;

    private CarbonRuntime carbonRuntime;

    public static DataHolder getInstance() {
        return instance;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * Getter method of RuntimeManager instance.
     *
     * @return RuntimeManager   returns runtime manager instance
     */
    public RuntimeManager getRuntimeManager() {
        return runtimeManager;
    }

    /**
     * setter method of RuntimeManager.
     *
     * @param runtimeManager - RuntimeManager instance to be set
     */
    public void setRuntimeManager(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }

    /**
     * This method used within this bundle (carbon.core) scope to get the currently held carbonRuntime service instance
     * within this holder.
     *
     * @return this will return the carbonRuntime service instance.
     */
    public CarbonRuntime getCarbonRuntime() {
        return carbonRuntime;
    }

    /**
     * This method is called by the relevant service component that acquires the carbonRuntime service
     * instance and will be stored for future look-ups.
     *
     * @param carbonRuntime the carbonRuntime to be stored with this holder
     */
    public void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = carbonRuntime;
    }
}
