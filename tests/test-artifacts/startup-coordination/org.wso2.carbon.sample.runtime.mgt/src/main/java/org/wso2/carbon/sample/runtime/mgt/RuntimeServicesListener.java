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
package org.wso2.carbon.sample.runtime.mgt;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

/**
 * Sample Runtime Service Listener Service Component class.
 *
 * @since 5.0.0
 */
@Component(
        name = "org.wso2.carbon.sample.runtime.mgt.RuntimeServicesListener",
        immediate = true,
        service = RequiredCapabilityListener.class,
        property = "capability-name=org.wso2.carbon.sample.runtime.mgt.Runtime"
)
public class RuntimeServicesListener implements RequiredCapabilityListener {
    private static final Logger logger = LoggerFactory.getLogger(RuntimeServicesListener.class);
    private BundleContext bundleContext;

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        logger.info("All required services are available for : " + this.getClass().getName());
        bundleContext.registerService(RuntimeManager.class, new RuntimeManager(), null);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Deactivate
    public void deactivate(BundleContext bundleContext) {

    }
}
