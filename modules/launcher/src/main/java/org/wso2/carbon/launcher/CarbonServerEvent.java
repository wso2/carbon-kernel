/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.launcher;

import org.wso2.carbon.launcher.config.CarbonLaunchConfig;

/**
 * An event from the CarbonServer
 */
public class CarbonServerEvent {

    /**
     * The CarbonServer is in the process of starting
     *
     * This event is fired just before the CarbonServer launches the OSGi framework.
     *
     */
    public final static int	STARTING							= 0x00000001;

    /**
     * The CarbonServer is in the process of stopping
     *
     * This event is fired just after the CarbonServer stops the OSGi framework.
     */
    public final static int	STOPPING							= 0x00000002;

    /**
     * Type of event.
     */
    private final int type;

    /**
     * Carbon launch configuration object
     */
    private final CarbonLaunchConfig<String, String> config;

    public CarbonServerEvent(int type, CarbonLaunchConfig<String, String> config){
        this.type = type;
        this.config = config;
    }

    /**
     * Returns the type of the event.
     * @return type.
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the Carbon launch configurations.
     * @return config
     */
    public CarbonLaunchConfig<String, String> getConfig() {
        return config;
    }
}
