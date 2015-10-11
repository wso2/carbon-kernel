/*
 *  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.base.LoggingConfiguration;
import org.wso2.carbon.internal.kernel.config.XMLBasedConfigProvider;
import org.wso2.carbon.internal.kernel.context.CarbonRuntimeFactory;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.config.CarbonConfigProvider;

import java.util.Map;

@Component(
        name = "org.wso2.carbon.internal.CarbonCoreDSComponent",
        immediate = true
)
public class CarbonCoreDSComponent {
    private static final Logger logger = LoggerFactory.getLogger(LoggingConfiguration.class);
    private LoggingConfiguration loggingConfiguration = LoggingConfiguration.getInstance();

    @Activate
    public void start(BundleContext bundleContext) throws Exception {
        DataHolder.getInstance().setBundleContext(bundleContext);

        // 1) Find to initialize the Carbon configuration provider
        CarbonConfigProvider configProvider = new XMLBasedConfigProvider();

        // 2) Creates the CarbonRuntime instance using the Carbon configuration provider.
        CarbonRuntime carbonRuntime = CarbonRuntimeFactory.createCarbonRuntime(configProvider);

        // 3) Register CarbonRuntime instance as an OSGi bundle.
        bundleContext.registerService(CarbonRuntime.class.getName(), carbonRuntime, null);
    }

    @Reference (
        name = "config.admin.managed.service",
                service = ManagedService.class,
                cardinality = ReferenceCardinality.MANDATORY,
                policy = ReferencePolicy.DYNAMIC,
                unbind = "unRegisterLoggingConfig"
    )
    protected void registerLoggingConfig(ManagedService managedService, Map<String, ?> properties) {
        String pid = (String) properties.get(Constants.SERVICE_PID);
        if (pid == null) {
            return;
        }
        loggingConfiguration.setConfigurationAdminService(managedService);
        try {
            loggingConfiguration.registerConfigurations(pid);
        } catch (Throwable e) {
            logger.error("Cannot load logging configuration", e);
        }
    }

    protected void unRegisterLoggingConfig(ManagedService managedService) {
        loggingConfiguration.setConfigurationAdminService(null);
    }
}