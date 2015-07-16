/*
 * Copyright 2005,2013 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.base.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.wso2.carbon.base.LoggingConfiguration;

/**
 * Activator of the {@link org.wso2.carbon.base} bundle
 */
public class CarbonBaseActivator implements BundleActivator {
    private static final Logger logger = LoggerFactory.getLogger(CarbonBaseActivator.class);
    private static final Marker marker = MarkerFactory.getMarker("FATAL");
    private ServiceRegistration registration;
    private ServiceTracker m_configTracker;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        //register pax logging on carbon server
        LoggingConfiguration loggingConfiguration = LoggingConfiguration.getInstance();
        m_configTracker = new ConfigAdminServiceTracker(bundleContext, loggingConfiguration);
        m_configTracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();

        m_configTracker.close();
        m_configTracker = null;
    }
}
