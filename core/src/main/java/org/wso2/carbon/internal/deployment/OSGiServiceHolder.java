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
package org.wso2.carbon.internal.deployment;

import org.wso2.carbon.kernel.CarbonRuntime;

/**
 * A singleton data holder pattern class for holding OSGi related service instances that are used within the scope
 * of this bundle. This class is internal to this bundle only.
 */
public class OSGiServiceHolder {
    private static OSGiServiceHolder instance = new OSGiServiceHolder();

    private DeploymentEngine carbonDeploymentEngine;
    private CarbonRuntime carbonRuntime;

    /**
     * This method will return the singleton instance of this holder.
     *
     * @return OSGiServiceHolder singleton instance
     */
    public static OSGiServiceHolder getInstance() {
        return instance;
    }

    /**
     * This method used within this bundle scope to get the currently held carbonDeploymentEngine service instance
     * within this holder.
     *
     * @return this will return the carbonDeploymentEngine service instance.
     */
    public DeploymentEngine getCarbonDeploymentEngine() {
        return carbonDeploymentEngine;
    }

    /**
     * This method is called by the relevant service component that acquires the carbonDeploymentEngine service
     * instance and will be stored for future look-ups.
     *
     * @param carbonDeploymentEngine the carbonDeploymentEngine to be stored by this holder
     */
    public void setCarbonDeploymentEngine(DeploymentEngine carbonDeploymentEngine) {
        this.carbonDeploymentEngine = carbonDeploymentEngine;
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
