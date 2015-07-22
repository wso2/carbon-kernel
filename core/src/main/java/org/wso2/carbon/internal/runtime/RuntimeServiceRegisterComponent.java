/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.internal.runtime;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.internal.DataHolder;
import org.wso2.carbon.runtime.api.RuntimeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class is responsible of waiting till all runtime services get registered
 * once all runtime services get registered it will register RuntimeService OSGI service
 * This service can be used by carbon server to finalize start-up
 */

@Component(
        name = "org.wso2.carbon.runtime.internal.RuntimeServiceRegisterComponent",
        immediate = true
)

public class RuntimeServiceRegisterComponent implements ServiceListener {
    private static Logger logger = LoggerFactory.getLogger(RuntimeServiceRegisterComponent.class);
    private ServiceRegistration serviceRegistration;

    private static BundleContext bundleContext;
    private List<String> requiredRuntimes = new ArrayList<String>();
    private Timer pendingRuntimeObservationTimer = new Timer();
    public static final String REQUIRED_RUNTIME_SERVICE = "Runtime-Manager-RequiredServices";

    @Activate
    protected void activate(ComponentContext componentContext) {
        bundleContext = componentContext.getBundleContext();

        logger.debug("Starting Carbon Runtime Manager");
        RuntimeManager runtimeManager = new RuntimeManager();
        // Add runtime manager to the data holder for later usages/references of this object
        DataHolder.getInstance().setRuntimeManager(runtimeManager);
        try {
            populateRequiredServices();

            if (requiredRuntimes.isEmpty()) {
                completeRuntimeInitialization(bundleContext);
            } else {
                StringBuffer serviceList = new StringBuffer("(|");
                for (String service : requiredRuntimes) {
                    serviceList.append("(").append(Constants.OBJECTCLASS).append("=").append(service).append(")");
                }
                serviceList.append(")");

                bundleContext.addServiceListener(this, serviceList.toString());
                ServiceReference[] serviceReferences =
                        bundleContext.getServiceReferences((String) null, serviceList.toString());
                if (serviceReferences != null) {
                    for (ServiceReference reference : serviceReferences) {
                        String service = ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
                        requiredRuntimes.remove(service);
                        logger.debug("Removed pending service " + service);
                    }
                }
                if (requiredRuntimes.isEmpty()) {
                    completeRuntimeInitialization(bundleContext);
                } else {
                    schedulePendingServicesObservationTimer();
                }
            }
        } catch (Throwable e) {
            logger.error("Cannot initialize RuntimeManager Component", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        serviceRegistration.unregister();
    }

    /**
     * populate the list of required services under the manifest header REQUIRED_RUNTIME_SERVICE
     */
    private void populateRequiredServices() {
        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            String requiredServiceList =
                    (String) bundle.getHeaders().get(REQUIRED_RUNTIME_SERVICE);
            if (requiredServiceList != null) {
                String[] values = requiredServiceList.split(",");
                for (String value : values) {
                    requiredRuntimes.add(value);
                }
            }
        }
    }

    private void schedulePendingServicesObservationTimer() {
        pendingRuntimeObservationTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!requiredRuntimes.isEmpty()) {
                    StringBuffer services = new StringBuffer();
                    for (String service : requiredRuntimes) {
                        services.append(service).append(",");
                    }
                    logger.warn("Waiting for required Runtime services : " + services.toString());
                }
            }
        }, 60000, 60000);
    }

    @Override
    public void serviceChanged(ServiceEvent serviceEvent) {
        if (serviceEvent.getType() == ServiceEvent.REGISTERED) {
            String service =
                    ((String[]) serviceEvent.getServiceReference().getProperty(Constants.OBJECTCLASS))[0];
            requiredRuntimes.remove(service);
            logger.debug("Removed pending service " + service);
            if (requiredRuntimes.isEmpty()) {
                completeRuntimeInitialization(bundleContext);
            }
        }
    }

    /**
     * Complete Runtime initialization if all required runtime get registered
     * Then the RuntimeService will be registered
     *
     * @param bundleContext : bundle context object
     */
    private void completeRuntimeInitialization(BundleContext bundleContext) {
        // Register RuntimeService
        RuntimeService runtimeService =
                new CarbonRuntimeService(DataHolder.getInstance().getRuntimeManager());
        serviceRegistration = bundleContext.registerService(RuntimeService.class.getName(),
                runtimeService, null);
        logger.debug("Registered Runtime Service : " + CarbonRuntimeService.class.getName());
    }

}
