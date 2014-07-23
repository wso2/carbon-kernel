package org.wso2.carbon.core.persistence.file.deployer;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.persistence.ModulePersistenceManager;
import org.wso2.carbon.core.persistence.PersistenceDataNotFoundException;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.core.persistence.PersistenceUtils;
import org.wso2.carbon.core.persistence.ServiceGroupPersistenceManager;
import org.wso2.carbon.core.persistence.ServicePersistenceManager;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Iterator;

@Deprecated
public class PersistenceMetaDataDeployer extends AbstractDeployer {
    private static final Log log = LogFactory.getLog(PersistenceMetaDataDeployer.class);

    private AxisConfiguration axisConfig;
    String metaDataDir;
    private PersistenceFactory persistenceFactory;

    public void init(ConfigurationContext configurationContext) {
        axisConfig = configurationContext.getAxisConfiguration();
        try {
            persistenceFactory = PersistenceFactory.getInstance(axisConfig);
        } catch (AxisFault e) {
            log.error("Error obtaining persistence factory.", e);
        }
    }

    public void setDirectory(String directory) {
        metaDataDir = directory;
    }

    public void setExtension(String extension) {
    }

    /**
     * Axis2 deployment engine will call this method when a .xml meta file is changed. Then the
     * changes will get applied to respective services and modules.
     *
     * @param deploymentFileData - info about the deployed file
     * @throws DeploymentException - error while deploying cApp
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        String filePath = deploymentFileData.getAbsolutePath();
        String name = filePath.substring(filePath.indexOf(metaDataDir) + metaDataDir.length() + 1);
        name = name.substring(0, name.lastIndexOf(".xml"));

            if (Resources.SERVICES_METAFILES_DIR.equals(metaDataDir)) {
            if(log.isDebugEnabled()){
                log.debug("Detected Service Meta File change.." + name);
            }

            AxisServiceGroup serviceGroup = axisConfig.getServiceGroup(name);
            if (serviceGroup == null) {
                if(log.isDebugEnabled()) {
                    log.debug("Service not available for the meta file - " + name);
                }
                return;
            }
            if(persistenceFactory.getServiceGroupFilePM().isUserModification(serviceGroup.getServiceGroupName())){
                if(log.isDebugEnabled()){
                    log.debug("User modified service : " + serviceGroup.getServiceGroupName());
                }
                return;
            }

            ServiceGroupPersistenceManager serviceGroupPM = persistenceFactory.getServiceGroupPM();
            ServicePersistenceManager servicePM = persistenceFactory.getServicePM();

            try {
                serviceGroupPM.handleExistingServiceGroupInit(serviceGroup);
                AxisEvent serviceGroupUpdateEvent = new AxisEvent(AxisEvent.SERVICE_DEPLOY, serviceGroup);
                axisConfig.notifyObservers(serviceGroupUpdateEvent, serviceGroup);
                for (Iterator itr = serviceGroup.getServices(); itr.hasNext(); ) {
                    AxisService axisService = (AxisService) itr.next();
                    OMElement serviceEle = persistenceFactory.getServicePM().getService(axisService);
                    if (serviceEle != null) {
                        if(CarbonUtils.isWorkerNode() && log.isDebugEnabled()){
                            log.debug("Service Meta Deployer - " + axisService.getName());
                        }

                        servicePM.handleExistingServiceInit(axisService);
                        AxisEvent serviceUpdateEvent = new AxisEvent(AxisEvent.SERVICE_DEPLOY,
                                                             axisService);
                        axisConfig.notifyObservers(serviceUpdateEvent ,axisService);
                    }
                }

            } catch (AxisFault e) {
                log.error(e.getMessage(), e);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new DeploymentException(e);
            }

        } else if (Resources.MODULES_METAFILES_DIR.equals(metaDataDir)) {
            if(log.isDebugEnabled()){
                log.debug("Detected Module Meta File change.." + name);
            }

            AxisModule module = axisConfig.getModule(name);
            if(module == null){ //checking for null. eg:  CARBON-13556
                if(log.isDebugEnabled()){
                    log.debug("Module " + name + " not available.");
                }
                return;
            }
            String version = PersistenceUtils.getModuleVersion(module);
            try {
                OMElement moduleEle = (OMElement) persistenceFactory.getModuleFilePM().get(name,
                                                   Resources.ModuleProperties.VERSION_XPATH +
                                                   PersistenceUtils.getXPathAttrPredicate(
                                                           Resources.ModuleProperties.VERSION_ID,
                                                           version));
                ModulePersistenceManager mpm = persistenceFactory.getModulePM();

                if(persistenceFactory.getModuleFilePM().isUserModification(module.getName())){
                    if(log.isDebugEnabled()){
                        log.debug("user modified module : " + module.getName());
                    }
                    return;
                }

                if (moduleEle != null) {
                    if(CarbonUtils.isWorkerNode()){
                        if (log.isDebugEnabled()) {
                            log.debug("Module Meta Deployer - " + module.getName() + version);
                        }
                    }

                    mpm.handleExistingModuleInit(moduleEle, module);
                    AxisEvent moduleUpdateEvent = new AxisEvent(AxisEvent.MODULE_DEPLOY, null);
                    axisConfig.notifyObservers(moduleUpdateEvent, module);
                }

            } catch (PersistenceDataNotFoundException e) {
                log.error(e.getMessage(), e);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
    }

    public void undeploy(String filePath) throws DeploymentException {
        //do nothing
    }

    public void cleanup() throws DeploymentException {
        // do nothing
    }
}
