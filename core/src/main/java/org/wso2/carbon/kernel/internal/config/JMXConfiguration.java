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
package org.wso2.carbon.kernel.internal.config;


import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

/**
 * JMX Configuration bean.
 *
 * @since 5.1.0
 */
@Configuration(description = "JMX Configuration")
public class JMXConfiguration {
    @Element(description = "To enable JMX Monitoring, change this value to true")
    private boolean enabled = false;
    @Element(description = "Server HostName")
    private String hostName = "127.0.0.1";
    @Element(description = "The port RMI server should be exposed")
    private int rmiServerPort = 11111;
    @Element(description = "The port RMI registry is exposed")
    private int rmiRegistryPort = 9999;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getRmiServerPort() {
        return rmiServerPort;
    }

    public void setRmiServerPort(int rmiServerPort) {
        this.rmiServerPort = rmiServerPort;
    }

    public int getRmiRegistryPort() {
        return rmiRegistryPort;
    }

    public void setRmiRegistryPort(int rmiRegistryPort) {
        this.rmiRegistryPort = rmiRegistryPort;
    }
}
