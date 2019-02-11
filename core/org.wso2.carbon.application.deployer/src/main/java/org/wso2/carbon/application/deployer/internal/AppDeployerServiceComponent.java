/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.application.deployer.internal;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.AxisFault;
import org.osgi.framework.*;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.application.deployer.*;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.application.deployer.handler.DefaultAppDeployer;
import org.wso2.carbon.application.deployer.handler.RegistryResourceDeployer;
import org.wso2.carbon.application.deployer.service.ApplicationManagerService;
import org.wso2.carbon.application.deployer.service.CappDeploymentService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;


import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @scr.component name="application.deployer.dscomponent" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="org.wso2.carbon.configCtx"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContext" unbind="unsetConfigurationContext"
 * @scr.reference name="app.handler" interface="org.wso2.carbon.application.deployer.handler.AppDeploymentHandler"
 * cardinality="0..n" policy="dynamic" bind="setAppHandler" unbind="unsetAppHandler"
 */

public class AppDeployerServiceComponent implements ServiceListener {

    private static RegistryService registryService;

    private static BundleContext bundleContext;
    private static ServiceRegistration appManagerRegistration;
    private static Map<String, List<Feature>> requiredFeatures;
    private List<AppDeploymentHandler> appHandlers = new ArrayList<AppDeploymentHandler>();

    private List<String> requiredServices = new ArrayList<String>();
    private ConfigurationContext configCtx;

    private static final Log log = LogFactory.getLog(AppDeployerServiceComponent.class);
    private Timer pendingServicesObservationTimer = new Timer();

    protected void activate(ComponentContext ctxt) {

        try {
            bundleContext = ctxt.getBundleContext();
            ApplicationManager applicationManager = ApplicationManager.getInstance();
            applicationManager.init(); // this will allow application manager to register deployment handlers

            // register ApplicationManager as a service
            appManagerRegistration = ctxt.getBundleContext().registerService(
                    ApplicationManagerService.class.getName(), applicationManager, null);

            // read required-features.xml
            URL reqFeaturesResource = bundleContext.getBundle()
                    .getResource(AppDeployerConstants.REQ_FEATURES_XML);
            if (reqFeaturesResource != null) {
                InputStream xmlStream = reqFeaturesResource.openStream();
                requiredFeatures = AppDeployerUtils
                        .readRequiredFeaturs(new StAXOMBuilder(xmlStream).getDocumentElement());
            }

            if (log.isDebugEnabled()) {
                log.debug("Carbon Application Deployer is activated..");
            }

        } catch (Throwable e) {
            log.error("Failed to activate Carbon Application Deployer", e);
        }

        try {
            populateRequiredServices();

            if (requiredServices.isEmpty()) {
                completeInitialization(bundleContext);
            } else {

                StringBuffer ldapFilter = new StringBuffer("(|");
                for (String service : requiredServices) {
                    ldapFilter.append("(").append(Constants.OBJECTCLASS).append("=").append(service).append(")");
                }
                ldapFilter.append(")");

                bundleContext.addServiceListener(this, ldapFilter.toString());
                ServiceReference[] serviceReferences =
                        bundleContext.getServiceReferences((String) null, ldapFilter.toString());
                if (serviceReferences != null) {
                    for (ServiceReference reference : serviceReferences) {
                        String service = ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
                        requiredServices.remove(service);
                        if (log.isDebugEnabled()) {
                            log.debug("Removed pending service " + service);
                        }
                    }
                }
                if (requiredServices.isEmpty()) {
                    completeInitialization(bundleContext);
                } else {
                    schedulePendingServicesObservationTimer();
                }
            }
        } catch (Throwable e) {
            log.fatal("Cannot activate StartupFinalizerServiceComponent", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (appManagerRegistration != null) {
            appManagerRegistration.unregister();
        }
    }

    protected void setRegistryService(RegistryService regService) {
        registryService = regService;
    }

    protected void unsetRegistryService(RegistryService regService) {
        registryService = null;
    }

    protected void setAppHandler(AppDeploymentHandler handler) {
        ApplicationManager.getInstance().registerDeploymentHandler(handler);
    }

    protected void unsetAppHandler(AppDeploymentHandler handler) {
        ApplicationManager.getInstance().unregisterDeploymentHandler(handler);
    }

    public static RegistryService getRegistryService() throws Exception {
        if (registryService == null) {
            String msg = "Before activating Carbon Application deployer bundle, an instance of "
                    + "RegistryService should be in existance";
            log.error(msg);
            throw new Exception(msg);
        }
        return registryService;
    }

    public static BundleContext getBundleContext() {
        if (bundleContext == null) {
            log.error("Application Deployer has not started. Therefore Bundle context is null");
        }
        return bundleContext;
    }

    public static Map<String, List<Feature>> getRequiredFeatures() {
        return requiredFeatures;
    }

    protected void setConfigurationContext(ConfigurationContextService configCtx) {
        this.configCtx = configCtx.getServerConfigContext();
    }

    protected void unsetConfigurationContext(ConfigurationContextService configCtx) {
        this.configCtx = null;
    }

    @Override
    public void serviceChanged(ServiceEvent serviceEvent) {
        if (serviceEvent.getType() == ServiceEvent.REGISTERED) {
            String service =
                    ((String[]) serviceEvent.getServiceReference().getProperty(Constants.OBJECTCLASS))[0];
            requiredServices.remove(service);
            if (log.isDebugEnabled()) {
                log.debug("Removed pending service " + service);
            }
            if (requiredServices.isEmpty()) {
                completeInitialization(bundleContext);
            }
        }
    }

    private void populateRequiredServices() {
        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            String requiredServiceList =
                    (String) bundle.getHeaders().
                            get(CarbonConstants.CarbonManifestHeaders.CAPP_MANGER_INIT_REQUIRED_SERVICE );
            if (requiredServiceList != null) {
                String[] values = requiredServiceList.split(",");
                for (String value : values) {
                    requiredServices.add(value);
                }
            }
        }
    }


    private void schedulePendingServicesObservationTimer() {
        pendingServicesObservationTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!requiredServices.isEmpty()) {
                    StringBuffer services = new StringBuffer();
                    for (String service : requiredServices) {
                        services.append(service).append(",");
                    }
                    log.warn("Waiting for required OSGi services: " + services.toString());
                }
            }
        }, 60000, 60000);
    }

    private void completeInitialization(BundleContext bundleContext) {
        // Initialize CApp deployer here
        this.addCAppDeployer(configCtx.getAxisConfiguration());
        registerCappdeploymentService();
    }

    public boolean addCAppDeployer(AxisConfiguration axisConfiguration) {
        boolean successfullyAdded = true;
        try {
            String appsRepo = "carbonapps";
            // Initialize CApp deployer here
            Class deployerClass = Class.
                    forName("org.wso2.carbon.application.deployer.CappAxis2Deployer");

            Deployer deployer = (Deployer) deployerClass.newInstance();
            deployer.setDirectory(appsRepo);
            deployer.setExtension("car");

            //Add the deployer to deployment engine
            DeploymentEngine deploymentEngine =
                    (DeploymentEngine) axisConfiguration.getConfigurator();
            deploymentEngine.addDeployer(deployer, appsRepo, "car");
        } catch (Exception e) {
            successfullyAdded = false;
        }
        return successfullyAdded;
    }

    private void registerCappdeploymentService(){
        try {
            AppDeployerServiceComponent.getBundleContext().registerService(CappDeploymentService.class.getName(),
                    new CappDeploymentServiceImpl(), null);

            if(log.isDebugEnabled()) {
                log.debug("Carbon CApp Services bundle is activated ");
            }
        } catch (Throwable e) {
            log.error("Failed to activate Carbon CApp Services bundle ", e);
        }

    }

}
