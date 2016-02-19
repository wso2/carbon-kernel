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
package org.wso2.carbon.jmx.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.jmx.internal.config.JMXConfiguration;
import org.wso2.carbon.jmx.internal.config.YAMLJMXConfigurationBuilder;
import org.wso2.carbon.jmx.security.CarbonJMXAuthenticator;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;

/**
 * CarbonJMXComponent
 *
 * @since 1.0.0
 */
@Component(
        name = "org.wso2.carbon.jmx.internal.CarbonJMXComponent",
        immediate = true
)
public class CarbonJMXComponent {
    private static final Logger logger = LoggerFactory.getLogger(CarbonJMXComponent.class);

    /**
     * This is the activation method of CarbonJMXComponent. This will be called when all the references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     */
    @Activate
    protected void start(BundleContext bundleContext) {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            JMXConfiguration jmxConfiguration = YAMLJMXConfigurationBuilder.build();
            LocateRegistry.createRegistry(jmxConfiguration.getRmiRegistryPort());

            String jmxURL = "service:jmx:rmi://" + jmxConfiguration.getHostName() + ":" +
                    jmxConfiguration.getRmiServerPort() + "/jndi/rmi://" + jmxConfiguration.getHostName() + ":" +
                    jmxConfiguration.getRmiRegistryPort() + "/jmxrmi";
            JMXServiceURL jmxServiceURL = new JMXServiceURL(jmxURL);

            HashMap<String, CarbonJMXAuthenticator> environment = new HashMap<>();
            environment.put(JMXConnectorServer.AUTHENTICATOR, new CarbonJMXAuthenticator());

            JMXConnectorServer jmxConnectorServer =
                    JMXConnectorServerFactory.newJMXConnectorServer(jmxServiceURL, environment, mBeanServer);
            jmxConnectorServer.start();
        } catch (Throwable throwable) {
            logger.error("Failed to start CarbonJMXComponent.", throwable);
        }
    }

    /**
     * This is the deactivation method of CarbonJMXComponent. This will be called when this component
     * is being stopped or references are un-satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {

    }
}
