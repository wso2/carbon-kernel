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
package org.wso2.carbon.application.deployer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.deployer.internal.AppDeployerServiceComponent;
import org.wso2.carbon.application.deployer.internal.ApplicationManager;
import org.wso2.carbon.application.deployer.service.CappDeploymentService;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CappAxis2Deployer extends AbstractDeployer {

    private static final Log log = LogFactory.getLog(CappAxis2Deployer.class);

    private AxisConfiguration axisConfig;

    private String cAppDir;

    private boolean isAlreadyRegistered = false;

    private List<String> pendingCAppList = new ArrayList<String>();

    public void init(ConfigurationContext configurationContext) {
        // create the cApp hot directory
        if (cAppDir != null && !"".equals(cAppDir)) {
            File cAppDirFile = new File(cAppDir);
            if (!cAppDirFile.exists() && !cAppDirFile.mkdir()) {
                log.warn("Couldn't create directory : " + cAppDirFile.getAbsolutePath());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Initializing Capp Axis2 Deployer..");
        }
        this.axisConfig = configurationContext.getAxisConfiguration();
        // load the existing Carbon apps from tenant registry space
//        loadPersistedApps();

        populatePendingCAppList();

        if(pendingCAppList.isEmpty() && !isAlreadyRegistered){
            registerCappdeploymentService();
            isAlreadyRegistered = true;
        }
    }

    /**
     * Populate the names of the carbon apps to be deployed
     *
     * @param
     * @throws
     */
    private void populatePendingCAppList(){

        File cAppDirFile = new File(CarbonUtils.getCarbonRepository() + File.separator + cAppDir);

        if(cAppDirFile.exists()){
            File [] cAppFiles = cAppDirFile.listFiles();
            for(int i = 0 ; i< cAppFiles.length ; i++){
                String extension = "";
                String fileName = cAppFiles[i].getName();
                int index = fileName.lastIndexOf('.');
                if (index > 0) {
                    extension = fileName.substring(index+1);
                    if(extension.equalsIgnoreCase("car")){
                        pendingCAppList.add(fileName);
                    }
                }
            }
        }

    }

    /**
     * Axis2 deployment engine will call this method when a .car archive is deployed. So
     * we only have to call the applicationManager to deploy it using the absolute path of
     * the deployed .car file.
     *
     * @param deploymentFileData - info about the deployed file
     * @throws DeploymentException - error while deploying cApp
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        /**
         * Before each cApp deployment, we load the existing apps from registry. This is to fix
         * an issue which occurs in a cluster with deployment synchronizer.
         */
//        loadPersistedApps();
        String artifactPath = deploymentFileData.getAbsolutePath();
        try {
            ApplicationManager.getInstance().deployCarbonApp(artifactPath, axisConfig);
        } catch (Exception e) {
            log.error("Error while deploying carbon application " + artifactPath, e);
        }

        super.deploy(deploymentFileData);

        pendingCAppList.remove(deploymentFileData.getName());

        if(pendingCAppList.isEmpty() && !isAlreadyRegistered){
            //If there are no pending Capps register CAPP Deployer Service
            registerCappdeploymentService();
            isAlreadyRegistered = true;
        }
    }

    public void setDirectory(String s) {
        this.cAppDir = s;
    }

    public void setExtension(String s) {

    }

    private void registerCappdeploymentService(){
        try {
            AppDeployerServiceComponent.getBundleContext().registerService(CappDeploymentService.class.getName(),
                    new CappDeploymentServiceImpl(), null);
            log.debug("Carbon CAPP Services bundle is activated ");
        } catch (Throwable e) {
            log.error("Failed to activate Carbon CAPP Services bundle ", e);
        }

    }

    /**
     * Undeploys the cApp from system when the .car file is deleted from the repository. Find
     * the relevant cApp using the file path and call the undeploy method on applicationManager.
     *
     * @param filePath - deleted .car file path
     * @throws DeploymentException
     */
    public void undeploy(String filePath) throws DeploymentException {
        String tenantId = AppDeployerUtils.getTenantIdString(axisConfig);
        String artifactPath = AppDeployerUtils.formatPath(filePath);
        CarbonApplication existingApp = null;
        for (CarbonApplication carbonApp : ApplicationManager
                .getInstance().getCarbonApps(tenantId)) {
            if (artifactPath.equals(carbonApp.getAppFilePath())) {
                existingApp = carbonApp;
                break;
            }
        }
        if (existingApp != null) {
            ApplicationManager.getInstance().undeployCarbonApp(existingApp, axisConfig);
        } else {
            log.info("Undeploying Faulty Carbon Application On : " + filePath);
            removeFaultyCAppOnUndeploy(filePath);
        }
        super.undeploy(filePath);
    }

    private void removeFaultyCAppOnUndeploy(String filePath) {
        String tenantId = AppDeployerUtils.getTenantIdString(axisConfig);
        //check whether this application file name already exists in faulty app list
        for (String faultyAppPath : ApplicationManager.getInstance().getFaultyCarbonApps(tenantId).keySet()) {
            if (filePath.equals(faultyAppPath)) {
                ApplicationManager.getInstance().removeFaultyCarbonApp(tenantId,faultyAppPath);
                break;
            }
        }
    }

    public void cleanup() throws DeploymentException {
        // do nothing        
    }

//    private void loadPersistedApps() {
//        try {
//            // create a persistence manager for particular tenant
//            CarbonAppPersistenceManager capm = ApplicationManager.getInstance()
//                    .getPersistenceManager(axisConfig);
//            // load persisted cApps for this tenant
//            capm.loadApps();
//        } catch (Exception e) {
//            log.error("Error while trying to load persisted cApps from registry", e);
//        }
//    }

}
