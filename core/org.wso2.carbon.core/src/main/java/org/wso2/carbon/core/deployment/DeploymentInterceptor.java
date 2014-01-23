/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.core.deployment;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.Axis2ModuleNotFound;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.core.persistence.PersistenceUtils;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.*;

/**
 * This deployment interceptor will be called whenever before a module is initialized or service is
 * deployed.
 *
 * @see AxisObserver
 */
public class DeploymentInterceptor implements AxisObserver {
    private static final Log log = LogFactory.getLog(DeploymentInterceptor.class);
    
    private static volatile String[] httpAdminServicesList = null;
    private static volatile boolean allAdminServicesHttp = false;
    private static volatile boolean isFirstCheck = true;

    private final Map<String, Parameter> paramMap = new HashMap<String, Parameter>();

    private final HashMap<String, HashMap<String, AxisDescription>> faultyServicesDueToModules =
            new HashMap<String, HashMap<String, AxisDescription>>();

    private PersistenceFactory pf;

    private Registry registry;
    private int tenantId = -1;
    private String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME; // TODO: intitializing the tenant domain
    private CarbonCoreDataHolder dataHolder = CarbonCoreDataHolder.getInstance();

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public void init(AxisConfiguration axisConfig) {
        extractTenantInfo(axisConfig);
        try {
            pf = PersistenceFactory.getInstance(axisConfig);
            //axisConfig.addParameter(Resources.PERSISTENCE_FACTORY_PARAM_NAME, pf);
            if (registry == null) {
                registry =
                        dataHolder.getRegistryService().getConfigSystemRegistry();
            }
        } catch (AxisFault e) {
            log.error("Error while adding PersistenceFactory parameter to axisConfig", e);
        } catch (Exception e) {
            log.error("Error while obtaining registry instance for the deployment interceptor", e);
        }
    }

    private void extractTenantInfo(AxisConfiguration axisConfig) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        tenantId = carbonContext.getTenantId();
        tenantDomain = carbonContext.getTenantDomain();
    }

    private String getTenantIdAndDomainString() {
        return (tenantId != -1 && tenantId != MultitenantConstants.SUPER_TENANT_ID) ?
                " {" + tenantDomain + "[" + tenantId + "]}" : " {super-tenant}";
    }

    public void serviceGroupUpdate(AxisEvent axisEvent, AxisServiceGroup axisServiceGroup) {
        if(CarbonUtils.isWorkerNode()){
            if (log.isDebugEnabled()){
                log.debug("Skip deployment intercepting in worker nodes.");
            }
            return;
        }
        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(tenantId);
            carbonContext.setTenantDomain(tenantDomain);
            carbonContext.setApplicationName(axisServiceGroup.getServiceGroupName());
            // We do not persist Admin service events
            if (SystemFilter.isFilteredOutService(axisServiceGroup)) {
                return;
            }
            int eventType = axisEvent.getEventType();
            // we only process ghost services when it is removed..
            if (SystemFilter.isGhostServiceGroup(axisServiceGroup) &&
                    eventType != AxisEvent.SERVICE_REMOVE) {
                return;
            }
            if (eventType == AxisEvent.SERVICE_DEPLOY) {
                if (log.isDebugEnabled()) {
                    log.debug("Deploying service group : " +
                            axisServiceGroup.getServiceGroupName() + getTenantIdAndDomainString());
                }

                OMElement serviceGroupOMElement = null;
                if (pf.getServiceGroupFilePM().elementExists(axisServiceGroup.getServiceGroupName(),
                        Resources.ServiceGroupProperties.ROOT_XPATH)) {
                    try {
                        serviceGroupOMElement = pf.getServiceGroupPM()
                                .getServiceGroup(axisServiceGroup.getServiceGroupName());
                    } catch (Exception e) {
                        log.error("Couldn't read service group resource." +
                                getTenantIdAndDomainString(), e);
                    }
                }

                if (serviceGroupOMElement == null) {
                    //treat this as a new service addition
                    addServiceGroup(axisServiceGroup);
                } else {
                    try {
                        String hashFromServiceFile = CarbonUtils.computeServiceHash(axisServiceGroup);

                        // Check whether the artifact has been updated, if so we need to purge all
                        // database entries and treat this as a new service group addition
                        AXIOMXPath xpathExpression = new AXIOMXPath(Resources.ServiceGroupProperties.ROOT_XPATH +
                                "@" + Resources.ServiceGroupProperties.HASH_VALUE + "[1]");
                        OMAttribute isSuccessAttr = (OMAttribute) xpathExpression.
                                selectSingleNode(serviceGroupOMElement);
                        String hashFromMetaFile = null;
                        if (isSuccessAttr != null) {
                            hashFromMetaFile = isSuccessAttr.getAttributeValue();
                        }

                        if (hashFromServiceFile != null && hashFromMetaFile != null &&
                                !hashFromMetaFile.equals(hashFromServiceFile)) {
                            log.warn("The service artifact of the " +
                                    axisServiceGroup.getServiceGroupName() +
                                    " service group has changed. Removing all registry entries and " +
                                    "handling this as a new service addition." +
                                    getTenantIdAndDomainString());
                            try {
                                deleteServiceGroup(axisServiceGroup);
                                addServiceGroup(axisServiceGroup);
                            } catch (Exception e) {
                                String msg = "Unable to remove all registry entries and handle new" +
                                        "service addition [" + axisServiceGroup.getServiceGroupName() +
                                        "]" + getTenantIdAndDomainString();
                                try {
                                    pf.getServiceGroupFilePM().
                                            rollbackTransaction(axisServiceGroup.getServiceGroupName());
                                    // We need to catch the exception that is generated by
                                    // rollbackTransaction(), should there be any, as this method won't
                                    // throw exceptions.
                                    log.error(msg, e);
                                } catch (Exception ex) {
                                    msg += ". Unable to rollback transaction.";
                                    log.error(msg, ex);
                                }
                            }
                        } else {
                            try {
                                pf.getServiceGroupPM()
                                        .handleExistingServiceGroupInit(axisServiceGroup);

                            } catch (Axis2ModuleNotFound e) {
                                addFaultyServiceDueToModule(e.getModuleName(), axisServiceGroup);
                                stopServiceGroup(axisServiceGroup,
                                        axisServiceGroup.getAxisConfiguration());
                                log.warn("ServiceGroup: " + axisServiceGroup.getServiceGroupName() +
                                        "is stopped due to the missing module : " + e.getModuleName() +
                                        getTenantIdAndDomainString());
                            } catch (Exception e) {
                                String msg = "Could not handle initialization of existing service " +
                                        "group [" + axisServiceGroup.getServiceGroupName() + "]" +
                                        getTenantIdAndDomainString();
                                log.error(msg, e);
                            }
                        }
                    } catch (JaxenException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            } else if (eventType == AxisEvent.SERVICE_REMOVE) {
                Parameter svcHistoryParam = axisServiceGroup.getParameter(
                        CarbonConstants.KEEP_SERVICE_HISTORY_PARAM);
                if (svcHistoryParam == null || svcHistoryParam.getValue() == null ||
                        JavaUtils.isFalse(svcHistoryParam.getValue())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Removing service group : " +
                                axisServiceGroup.getServiceGroupName() +
                                getTenantIdAndDomainString());
                    }

                    deleteServiceGroup(axisServiceGroup);
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void addServiceGroup(AxisServiceGroup axisServiceGroup) {
        try {
            pf.getServiceGroupPM().handleNewServiceGroupAddition(axisServiceGroup);
        } catch (Exception e) {
            String msg = "Could not handle initialization of new service group [" +
                    axisServiceGroup.getServiceGroupName() + "]";
            log.error(msg, e);
        }
    }

    private void deleteServiceGroup(AxisServiceGroup axisServiceGroup) {
        try {
            pf.getServiceGroupPM().deleteServiceGroup(axisServiceGroup);
        } catch (Exception e) {
            log.error("Could not delete service group " + axisServiceGroup.getServiceGroupName() +
                    getTenantIdAndDomainString(), e);
        }
    }

    public void serviceUpdate(AxisEvent axisEvent, AxisService axisService) {
        if(CarbonUtils.isWorkerNode()){
            if (log.isDebugEnabled()){
                log.debug("Skip deployment intercepting in worker nodes.");
            }
            return;
        }
        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(tenantId);
            carbonContext.setTenantDomain(tenantDomain);
            carbonContext.setApplicationName(axisService.getName());
            // We do not persist Admin service events
			if (SystemFilter.isFilteredOutService((AxisServiceGroup) axisService.getParent())) {
				// here we expose some admin services in HTTP
				if (isHttpAdminService(axisService.getName())) {
					changeAdminServiceTransport(axisService);
				}
				return;
			}

            if (axisService.isClientSide()) {
                return;
            }
            int eventType = axisEvent.getEventType();
            // we only process ghost services when it is removed..
            if (GhostDeployerUtils.isGhostService(axisService) &&
                    eventType != AxisEvent.SERVICE_REMOVE) {
                return;
            }
            String serviceName = axisService.getName();
            try {
                OMElement service = pf.getServicePM().getService(axisService);

                // if (eventType == AxisEvent.SERVICE_STOP) do nothing

                if (eventType == AxisEvent.SERVICE_DEPLOY) {
                    if (!JavaUtils.isTrue(axisService.getParameterValue(
                            CarbonConstants.HIDDEN_SERVICE_PARAM_NAME))) {
                        log.info("Deploying Axis2 service: " + serviceName +
                                getTenantIdAndDomainString());
                    } else if (log.isDebugEnabled()) {
                        log.debug("Deploying hidden Axis2 service : " + serviceName +
                                getTenantIdAndDomainString());
                    }

                    if (service == null) {
                        pf.getServicePM().handleNewServiceAddition(axisService);
                    } else {
                        pf.getServicePM().handleExistingServiceInit(axisService);
                    }
                } else if (eventType == AxisEvent.SERVICE_START) {
                    service.addAttribute(Resources.ServiceProperties.ACTIVE, "true", null);
                } else if (eventType == AxisEvent.SERVICE_STOP && service != null) {
                    // in a shared registry scenario the resource could have been already removed
                    // by some other node
                    service.addAttribute(Resources.ServiceProperties.ACTIVE, "false", null);
                } else if (eventType == AxisEvent.SERVICE_REMOVE) {
                    if (service != null) {
                        try {
                            Parameter svcHistoryParam = axisService.getParameter(
                                    CarbonConstants.KEEP_SERVICE_HISTORY_PARAM);
                            if (svcHistoryParam == null || svcHistoryParam.getValue() == null ||
                                    JavaUtils.isFalse(svcHistoryParam.getValue())) {
                                pf.getServicePM().deleteService(axisService);
                                log.info("Removing Axis2 Service: " + axisService.getName() + getTenantIdAndDomainString());
                            }
                        } catch (Exception e) {
                            String msg = "Cannot delete service [" + serviceName + "]" +
                                    getTenantIdAndDomainString();
                            log.error(msg, e);
                        }
                    }
                }

//                if (service != null) {
//                    service.discard();
//                }

            } catch (Axis2ModuleNotFound e) {
                addFaultyServiceDueToModule(e.getModuleName(), axisService);
                stopService(axisService, axisService.getAxisConfiguration());
                log.warn("Service " + axisService.getName() +
                        " is stopped due to the missing module: " + e.getModuleName() +
                        getTenantIdAndDomainString());
            } catch (Exception e) {
                String msg = "Exception occurred while handling service update event." +
                        getTenantIdAndDomainString();
                log.error(msg, e);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void moduleUpdate(AxisEvent axisEvent, AxisModule axisModule) {
        if(CarbonUtils.isWorkerNode()){
            if (log.isDebugEnabled()){
                log.debug("Skip deployment intercepting in worker nodes.");
            }
            return;
        }
        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(tenantId);
            carbonContext.setTenantDomain(tenantDomain);
            //TODO: Check whether we can ignore AdminModules - SystemFilter.isFilteredOutModule
            String moduleName = axisModule.getName();
            /*if (moduleName.equals(ServerConstants.ADMIN_MODULE) ||
                moduleName.equals(ServerConstants.TRACER_MODULE) ||
                moduleName.equals(ServerConstants.STATISTICS_MODULE)) {
                return;
            }*/

            // Handle.MODULE_DEPLOY event. This may be a new or existing module
            if (axisEvent.getEventType() == AxisEvent.MODULE_DEPLOY) {
                String moduleVersion;
                if (axisModule.getVersion() == null) {
                    log.warn("A valid Version not found for the module : '" + moduleName + "'" +
                            getTenantIdAndDomainString());
                    moduleVersion = Resources.ModuleProperties.UNDEFINED;
                } else {
                    moduleVersion = axisModule.getVersion().toString();
                }
                if (!SystemFilter.isFilteredOutModule(axisModule)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Deploying Axis2 module: " + axisModule.getArchiveName() +
                                  getTenantIdAndDomainString());
                    }
                }

                OMElement module = null;
                if (pf.getModuleFilePM().elementExists(moduleName,
                        PersistenceUtils.getResourcePath(axisModule))) {
                    try {
                        //todo this is unnecessary. This does unnecessary parsing that is not needed. Remove this just the above condition is enough
                        module = (OMElement) pf.getModuleFilePM().
                                get(moduleName, Resources.ModuleProperties.VERSION_XPATH +
                                        PersistenceUtils.getXPathAttrPredicate(
                                                Resources.ModuleProperties.VERSION_ID, moduleVersion));
                    } catch (Exception e) {
                        log.error("Couldn't read the module resource" +
                                getTenantIdAndDomainString(), e);
                    }
                }

                if (module != null) {
                    try {
                        pf.getModulePM().handleExistingModuleInit(module, axisModule);
                    } catch (Exception e) {
                        log.error("Could not handle initialization of existing module" +
                                getTenantIdAndDomainString(), e);
                    }
                } else { // this is a new module which has not been registered in the DB yet
                    try {
                        pf.getModulePM().handleNewModuleAddition(axisModule, moduleName,
                                moduleVersion);
                    } catch (Exception e) {
                        log.error("Could not handle addition of new module" +
                                getTenantIdAndDomainString(),
                                e);
                    }
                }

                synchronized (faultyServicesDueToModules) {
                    //Check whether there are faulty services due to this module
                    HashMap<String, AxisDescription> faultyServices =
                            getFaultyServicesDueToModule(moduleName);
                    //noinspection unchecked
                    faultyServices = (HashMap<String, AxisDescription>) faultyServices.clone();

                    // Here iterating a cloned hash-map and modifying the original hash-map.
                    // To avoid the ConcurrentModificationException.
                    for (AxisDescription axisDescription : faultyServices.values()) {
                        removeFaultyServiceDueToModule(moduleName,
                                (String) axisDescription.getKey());

//                        OMElement axisDescriptionResource;
                        try {
                            //Recover the faulty serviceGroup or service.
                            if (axisDescription instanceof AxisServiceGroup) {
                                AxisServiceGroup axisServiceGroup =
                                        (AxisServiceGroup) axisDescription;
//                                axisDescriptionResource = pf.getServiceGroupPM().getServiceGroup(
//                                        axisServiceGroup.getServiceGroupName());
                                pf.getServiceGroupPM().handleExistingServiceGroupInit(axisServiceGroup);

                                //Start all the services in this serviceGroup and remove the special
                                // parameter
                                startServiceGroup(axisServiceGroup,
                                        axisServiceGroup.getAxisConfiguration());
                                log.info("Recovered and Deployed axis2 service group: " +
                                        axisServiceGroup.getServiceGroupName() +
                                        getTenantIdAndDomainString());

                            } else if (axisDescription instanceof AxisService) {
                                AxisService axisService = (AxisService) axisDescription;
//                                axisDescriptionResource = pf.getServicePM().getService(axisService);
                                pf.getServicePM().handleExistingServiceInit(axisService);

                                //Start this axisService and remove the special parameter.
                                startService(axisService, axisService.getAxisConfiguration());
                                log.info("Recovered and Deployed axis2 service: " +
                                        axisService.getName() +
                                        getTenantIdAndDomainString());
                            }

                        } catch (Axis2ModuleNotFound e) {
                            addFaultyServiceDueToModule(e.getModuleName(), axisDescription);
                        } catch (Exception e) {
                            String msg = "Could not handle initialization of existing service " +
                                    "group [" + axisDescription.getKey() + "]" +
                                    getTenantIdAndDomainString();
                            log.error(msg, e);
                        }
                    }
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void addParameter(Parameter parameter) throws AxisFault {
        paramMap.put(parameter.getName(), parameter);
    }

    public void removeParameter(Parameter param) throws AxisFault {
        paramMap.remove(param.getName());
    }

    public void deserializeParameters(OMElement omElement) throws AxisFault {
        //No need to do anything here
    }

    public Parameter getParameter(String paramName) {
        return paramMap.get(paramName);
    }

    public ArrayList<Parameter> getParameters() {
        Collection<Parameter> collection = paramMap.values();
        ArrayList<Parameter> arr = new ArrayList<Parameter>();
        for (Parameter aCollection : collection) {
            arr.add(aCollection);
        }
        return arr;
    }

    public boolean isParameterLocked(String paramName) {
        return (paramMap.get(paramName)).isLocked();
    }

    /**
     * Updates the map that keeps track of faulty services due to modules
     *
     * @param moduleName      This service has become faulty due this module.
     * @param axisDescription Data that are required when recovering the faulty service.
     */
    private void addFaultyServiceDueToModule(String moduleName, AxisDescription axisDescription) {
        HashMap<String, AxisDescription> faultyServicesMap;
        synchronized (faultyServicesDueToModules) {
            if (faultyServicesDueToModules.containsKey(moduleName)) {
                faultyServicesMap = faultyServicesDueToModules.get(moduleName);
                faultyServicesMap.put((String) axisDescription.getKey(), axisDescription);
            } else {
                faultyServicesMap = new HashMap<String, AxisDescription>();
                faultyServicesMap.put((String) axisDescription.getKey(), axisDescription);
                faultyServicesDueToModules.put(moduleName, faultyServicesMap);
            }
        }
    }

    private HashMap<String, AxisDescription> getFaultyServicesDueToModule(String moduleName) {
        if (faultyServicesDueToModules.containsKey(moduleName)) {
            return faultyServicesDueToModules.get(moduleName);
        }
        return new HashMap<String, AxisDescription>(1);
    }


    private void removeFaultyServiceDueToModule(String moduleName, String serviceGroupName) {
        synchronized (faultyServicesDueToModules) {
            HashMap<String, AxisDescription> faultyServices =
                    faultyServicesDueToModules.get(moduleName);
            if (faultyServices != null) {
                faultyServices.remove(serviceGroupName);
                if (faultyServices.isEmpty()) {
                    faultyServicesDueToModules.remove(moduleName);
                }
            }
        }
    }

    public void startServiceGroup(AxisServiceGroup serviceGroup,
                                  AxisConfiguration axisConfiguration) {
        for (Iterator itr = serviceGroup.getServices(); itr.hasNext(); ) {
            startService((AxisService) itr.next(), axisConfiguration);
        }
    }

    public void stopServiceGroup(AxisServiceGroup serviceGroup,
                                 AxisConfiguration axisConfiguration) {
        for (Iterator itr = serviceGroup.getServices(); itr.hasNext(); ) {
            stopService((AxisService) itr.next(), axisConfiguration);
        }
    }

    public void startService(AxisService axisService, AxisConfiguration axisConfiguration) {
        String serviceName = axisService.getName();

        if (log.isDebugEnabled()) {
            log.debug("Activating service: " + serviceName + getTenantIdAndDomainString());
        }

        try {
            axisConfiguration.startService(serviceName);
            //Removing the special special property
            Parameter param = axisService.getParameter(CarbonConstants.CARBON_FAULTY_SERVICE);
            if (param != null) {
                axisService.removeParameter(param);
            }
        } catch (AxisFault e) {
            String msg = "Cannot start service : " + serviceName + getTenantIdAndDomainString();
            log.error(msg, e);
        }
    }

    public void stopService(AxisService axisService, AxisConfiguration axisConfiguration) {
        String serviceName = axisService.getName();

        if (log.isDebugEnabled()) {
            log.debug("Deactivating service: " + serviceName + getTenantIdAndDomainString());
        }

        try {
            axisConfiguration.stopService(serviceName);
            axisService.addParameter(CarbonConstants.CARBON_FAULTY_SERVICE,
                    CarbonConstants.CARBON_FAULTY_SERVICE_DUE_TO_MODULE);
        } catch (AxisFault e) {
            String msg = "Cannot stop service: " + serviceName + getTenantIdAndDomainString();
            log.error(msg, e);
        }
    }
    
	/**
	 * This method is used to expose admin services in HTTP
	 * 
	 * @param axisService
	 */
	private void changeAdminServiceTransport(AxisService axisService) {
		axisService.addExposedTransport("http");
		if (log.isDebugEnabled()) {
			log.debug("AdminService " + axisService.getName() + " exposed in HTTP");
		}
	}
    
	/**
	 * This method checks the service name against the list of services to be
	 * exposed in HTTP
	 * 
	 * @param serviceName
	 * @return
	 */
	private boolean isHttpAdminService(String serviceName) {

		if (!isFirstCheck && !allAdminServicesHttp && httpAdminServicesList == null) {
			return false;
		}
		// set all admin services to http
		if (allAdminServicesHttp) {
			return true;
		}
		// in this if block we set the config
		if (isFirstCheck) {
			String httpAdminServices =
			                           CarbonCoreDataHolder.getInstance()
			                                               .getServerConfigurationService()
			                                               .getFirstProperty(CarbonConstants.AXIS2_CONFIG_PARAM +
			                                                                         "." +
			                                                                         CarbonConstants.HTTP_ADMIN_SERVICES);
			// here we set the configs in memory
			if (httpAdminServices != null && !"".equals(httpAdminServices)) {
				if (httpAdminServices.equals("*")) {
					allAdminServicesHttp = true;
					isFirstCheck = false;
					return true;
				}
				httpAdminServicesList = httpAdminServices.split(",");
				isFirstCheck = false;
			} else {
				return isFirstCheck = false;
			}
		}
		// no admin service will be exposed in http
		if (httpAdminServicesList == null) {
			return false;
		}
		// now look in the list
		for (String httpAdminService : httpAdminServicesList) {
			if (serviceName.equals(httpAdminService))
				return true; // this is a http admin service
		}

		return false;
	}

//    private void sendReloadArtifactMessage(String serviceName) {
//        // For sending clustering messages we need to use the super-tenant's AxisConfig (Main Server
//        // AxisConfiguration) because we are using the clustering facility offered by the ST in the
//        // tenants
//        ClusteringAgent clusteringAgent =
//                CarbonCoreDataHolder.getInstance().getMainServerConfigContext().
//                        getAxisConfiguration().getClusteringAgent();
//        if(clusteringAgent != null) {
//            try {
//                clusteringAgent.sendMessage(new ReloadArtifactMessage(tenantId, serviceName), true);
//            } catch (ClusteringFault e) {
//                log.error("Could not send ReloadArtifactMessage for tenant " + tenantId, e);
//            }
//        }
//    }

}