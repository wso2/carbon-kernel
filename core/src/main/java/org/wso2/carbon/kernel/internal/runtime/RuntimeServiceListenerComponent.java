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
package org.wso2.carbon.kernel.internal.runtime;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.internal.DataHolder;
import org.wso2.carbon.kernel.runtime.Runtime;
import org.wso2.carbon.kernel.runtime.RuntimeService;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

/**
 * This service  component is responsible for retrieving the Runtime OSGi service and register each runtime
 * with runtime manager. It also acts as a RequiredCapabilityListener for all the Runtime capabilities, and
 * once they are available, it registers the RuntimeService as an OSGi service.
 *
 * @since 5.0.0
 */
@Component(
        name = "org.wso2.carbon.kernel.internal.runtime.RuntimeServiceListenerComponent",
        immediate = true,
        property = {"capability-name=org.wso2.carbon.kernel.runtime.Runtime",
                "component-key=carbon-runtime-mgt"}
)
public class RuntimeServiceListenerComponent implements RequiredCapabilityListener {
    private static Logger logger = LoggerFactory.getLogger(RuntimeServiceListenerComponent.class);
    private RuntimeManager runtimeManager = new RuntimeManager();
    private BundleContext bundleContext;

    @Activate
    protected void start(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        DataHolder.getInstance().setRuntimeManager(runtimeManager);
    }

    /**
     * Register the runtime instance.
     *
     * @param runtime - runtime instance
     */

    @Reference(
            name = "carbon.runtime.service",
            service = Runtime.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterRuntime"
    )
    protected void registerRuntime(Runtime runtime) {
        try {
            runtimeManager.registerRuntime(runtime);
        } catch (Exception e) {
            logger.error("Error while adding runtime to the Runtime manager", e);
        }
    }

    /**
     * Un-register the runtime instance.
     *
     * @param runtime - runtime instance
     */
    protected void unRegisterRuntime(Runtime runtime) {
        try {
            runtimeManager.unRegisterRuntime(runtime);
        } catch (Exception e) {
            logger.error("Error while removing runtime from Runtime manager", e);
        }
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        if (logger.isDebugEnabled()) {
            logger.debug("Registering RuntimeService as an OSGi service");
        }
        RuntimeService runtimeService = new CarbonRuntimeService(runtimeManager);
        bundleContext.registerService(RuntimeService.class, runtimeService, null);
    }
}
