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

import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;
import org.wso2.carbon.kernel.annotations.Ignore;
import org.wso2.carbon.kernel.configprovider.utils.ConfigurationUtils;
import org.wso2.carbon.kernel.internal.config.JMXConfiguration;

import java.util.Properties;

/**
 * CarbonConfiguration class holds static configuration parameters.
 *
 * @since 5.0.0
 */
@Configuration(namespace = "wso2.carbon", description = "Carbon Configuration Parameters")
public class CarbonConfiguration {

    public CarbonConfiguration() {
        // Reads the {@value Constants#PROJECT_DEFAULTS_PROPERTY_FILE} property file and assign project version.
        Properties properties = ConfigurationUtils.loadProjectProperties();
        version = properties.getProperty(Constants.MAVEN_PROJECT_VERSION);
    }

    @Element(description = "value to uniquely identify a server")
    private String id = "carbon-kernel";

    @Element(description = "server name")
    private String name = "WSO2 Carbon Kernel";

    @Ignore
    private String version;

    private String tenant = Constants.DEFAULT_TENANT;

    @Element(description = "ports used by this server")
    private PortsConfig ports = new PortsConfig();

    @Element(description = "StartupOrderResolver related configurations")
    private StartupResolverConfig startupResolver = new StartupResolverConfig();

    @Element(description = "JMX Configuration")
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

    public StartupResolverConfig getStartupResolverConfig() {
        return startupResolver;
    }

    public JMXConfiguration getJmxConfiguration() {
        return jmx;
    }
}
