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
package org.wso2.carbon.launcher;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.launcher.config.CarbonLaunchConfig;

/**
 * An event from the CarbonServer.
 *
 * @since 5.0.0
 */
public class CarbonServerEvent {

    /**
     * The CarbonServer is in the process of starting.
     * <p>
     * This event is fired just before the CarbonServer launches the OSGi framework.
     */
    public static final int STARTING = 0x00000001;

    /**
     * The CarbonServer is in the process of starting.
     * <p>
     * This event is fired just before the CarbonServer launches the OSGi framework.
     */
    public static final int BEFORE_LOADING_INITIAL_BUNDLES = 0x00000004;

    /**
     * The CarbonServer is in the process of starting.
     * <p>
     * This event is fired just before the CarbonServer launches the OSGi framework.
     */
    public static final int AFTER_LOADING_INITIAL_BUNDLES = 0x00000008;

    /**
     * The CarbonServer is in the process of stopping.
     * <p>
     * This event is fired just after the CarbonServer stops the OSGi framework.
     */
    public static final int STOPPING = 0x00000002;

    /**
     * Type of event.
     */
    private final int type;

    /**
     * Carbon launch configuration object.
     */
    private final CarbonLaunchConfig config;

    /**
     * Carbon launch configuration object.
     */
    private BundleContext systemBundleContext;

    /**
     * Constructor.
     *
     * @param type   event type
     * @param config Carbon Launch Configuration
     */
    public CarbonServerEvent(int type, CarbonLaunchConfig config) {
        this.type = type;
        this.config = config;
    }

    /**
     * Returns the type of the event.
     *
     * @return type.
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the Carbon launch configurations.
     *
     * @return config
     */
    public CarbonLaunchConfig getConfig() {
        return config;
    }

    /**
     * returns the OSGi system bundle context.
     * @return system bundle context
     */
    public BundleContext getSystemBundleContext() {
        return systemBundleContext;
    }

    // Sets the system bundle context for the CarbonServerEvent
    void setSystemBundleContext(BundleContext systemBundleContext) {
        this.systemBundleContext = systemBundleContext;
    }
}
