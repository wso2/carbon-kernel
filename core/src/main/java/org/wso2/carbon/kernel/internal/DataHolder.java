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
import org.wso2.carbon.kernel.internal.runtime.RuntimeManager;

/**
 * Carbon kernel DataHolder
 */
public class DataHolder {
    private static DataHolder instance = new DataHolder();
    private BundleContext bundleContext;

    private RuntimeManager runtimeManager = null;

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
     * Getter method of RuntimeManager instance
     *
     * @return RuntimeManager   returns runtime manager instance
     */
    public RuntimeManager getRuntimeManager() {
        return runtimeManager;
    }

    /**
     * setter method of RuntimeManager
     *
     * @param runtimeManager - RuntimeManager instance to be set
     */
    public void setRuntimeManager(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }
}
