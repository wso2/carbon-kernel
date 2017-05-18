/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.kernel.config.model;


import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * Config bean for pendingCapabilityTimer.
 */
@Configuration(description = "Configuration for the timer task which checks " +
        "for satisfiable RequiredCapabilityListeners periodically")
public class CapabilityListenerTimer {

    @Element(description = "delay in milliseconds before task is to be executed")
    private long delay = 20;

    @Element(description = "time in milliseconds between successive task executions")
    private long period = 20;

    public long getDelay() {
        return delay;
    }

    public long getPeriod() {
        return period;
    }
}
