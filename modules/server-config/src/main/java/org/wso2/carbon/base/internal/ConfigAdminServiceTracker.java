/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.base.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.base.LoggingConfiguration;

public class ConfigAdminServiceTracker extends ServiceTracker {
    private static final String CONFIG_ADMIN_SERVICE_NAME = ConfigurationAdmin.class.getName();
    private static final Logger logger = LoggerFactory.getLogger(ConfigAdminServiceTracker.class);
    private LoggingConfiguration loggingConfiguration;

    @SuppressWarnings("unchecked")
    ConfigAdminServiceTracker(BundleContext bundleContext, LoggingConfiguration configuration) {
        super(bundleContext, CONFIG_ADMIN_SERVICE_NAME, null);
        loggingConfiguration = configuration;
    }

    @Override
    public final Object addingService(ServiceReference serviceReference) {
        @SuppressWarnings("unchecked") ConfigurationAdmin service = (ConfigurationAdmin) super.addingService(serviceReference);
        loggingConfiguration.setConfigurationAdminService(service);

        try {
            loggingConfiguration.registerConfigurations(null);
        } catch (Throwable e) {
            logger.error("Cannot load logging configuration", e);
        }
        return service;
    }

    @Override
    public void removedService(ServiceReference serviceReference, Object service) {
        loggingConfiguration.setConfigurationAdminService(null);
        context.ungetService(serviceReference);
    }

}
