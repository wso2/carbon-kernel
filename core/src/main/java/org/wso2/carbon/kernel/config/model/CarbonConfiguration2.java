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
package org.wso2.carbon.kernel.config.model;

import org.wso2.carbon.configuration.annotations.Configuration;
import org.wso2.carbon.configuration.annotations.Element;
import org.wso2.carbon.configuration.annotations.Reference;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.internal.config.JMXConfiguration;

/**
 * CarbonConfiguration class holds static configuration parameters specified in the carbon.yaml file.
 *
 * @since 5.0.0
 */
@Configuration(key = "wso2.carbon", level = 0)
public class CarbonConfiguration2 {

    @Element(name = "id", value = "carbon-kernel")
    private String id = "carbon-kernel";

    @Element(name = "name", value = "WSO2 Carbon Kernel")
    private String name = "WSO2 Carbon Kernel";

    @Element(name = "version", value = "5.0.0")
    private String version = "5.0.0";

    @Element(name = "tenant", value = "default")
    private String tenant = Constants.DEFAULT_TENANT;

    @Reference(name = "ports", value = PortsConfig.class)
    private PortsConfig ports = new PortsConfig();

    @Reference(name = "deployment", value = DeploymentConfig.class)
    private DeploymentConfig deployment = new DeploymentConfig();

    @Reference(name = "startupResolver", value = StartupResolverConfig.class)
    private StartupResolverConfig startupResolver = new StartupResolverConfig();

    @Reference(name = "jmx", value = JMXConfiguration.class)
    private JMXConfiguration jmx = new JMXConfiguration();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getTenant() {
        return tenant;
    }

    public PortsConfig getPortsConfig() {
        return ports;
    }

    public DeploymentConfig getDeploymentConfig() {
        return deployment;
    }

    public StartupResolverConfig getStartupResolverConfig() {
        return startupResolver;
    }

    public JMXConfiguration getJmxConfiguration() {
        return jmx;
    }
}
