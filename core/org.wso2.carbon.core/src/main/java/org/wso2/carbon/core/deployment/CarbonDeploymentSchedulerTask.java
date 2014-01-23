/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.core.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.deployment.RepositoryListener;
import org.apache.axis2.deployment.scheduler.SchedulerTask;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;
import org.wso2.carbon.utils.deployment.GhostMetaArtifactsLoader;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This task takes care of deployment in WSO2 Carbon servers.
 * <p/>
 * It will do a deployment synchronization, followed by hot deployment
 */
public class CarbonDeploymentSchedulerTask extends SchedulerTask {

    /**
     * Indicates whether a Deployment repo update has to be performed
     */
    public static final String REPO_UPDATE_REQUIRED = "repo.update.required";

    private static final Log log = LogFactory.getLog(CarbonDeploymentSchedulerTask.class);
    private int tenantId;
    private String tenantDomain;
    private boolean isInitialUpdateDone;
    private boolean isRepoUpdateFailed;
    private AxisConfiguration axisConfig;

    public CarbonDeploymentSchedulerTask(RepositoryListener listener,
                                         AxisConfiguration axisConfig,
                                         int tenantId,
                                         String tenantDomain) {
        super(listener, axisConfig);
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.axisConfig = axisConfig;
        try {
            axisConfig.addParameter(REPO_UPDATE_REQUIRED, new AtomicBoolean(false));
        } catch (AxisFault axisFault) {
            log.error("Cannot add repo.update.required parameter");
        }
    }

    public synchronized void runAxisDeployment() {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantId(tenantId);
        carbonContext.setTenantDomain(tenantDomain);
        carbonContext.setApplicationName(null);
        super.run();
        // this should run for tenants only
        if (GhostDeployerUtils.isGhostOn() && GhostDeployerUtils.isPartialUpdateEnabled() &&
                CarbonUtils.isWorkerNode() && tenantId > 0) {
            doInitialGhostArtifactDeployement();
        }
    }

    private void doInitialGhostArtifactDeployement() {
        BundleContext bundleContext = CarbonCoreDataHolder.getInstance().getBundleContext();
        ServiceReference reference = bundleContext.getServiceReference(GhostMetaArtifactsLoader.class.getName());
        if (reference != null) {
            ServiceTracker serviceTracker = new ServiceTracker(bundleContext,
                                                               GhostMetaArtifactsLoader.class.getName(),
                                                               null);
            try {
                serviceTracker.open();
                for (Object obj : serviceTracker.getServices()) {
                    GhostMetaArtifactsLoader artifactsLoader = (GhostMetaArtifactsLoader) obj;
                    if(log.isDebugEnabled()){
                        if(artifactsLoader.getClass().toString().contains("Service")) {
                            log.debug("Loading ghost service meta artifacts for tenant: " + tenantDomain);
                        } else if(artifactsLoader.getClass().toString().contains("Webapp")) {
                            log.debug("Loading ghost webapp meta artifacts for tenant: " + tenantDomain);
                        }
                    }
                    artifactsLoader.loadArtifacts(axisConfig, tenantDomain);
                }
            } catch (Throwable t) {
                log.error("Ghost meta artifacts loading for tenant " + tenantId + " failed", t);
            } finally {
                serviceTracker.close();
            }
        }
    }

    @Override
    public synchronized void run() {
        try {
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(tenantId);
            carbonContext.setTenantDomain(tenantDomain);
            carbonContext.setApplicationName(null);

            deploymentSyncUpdate();
            runAxisDeployment(); // artifact meta files which need to be committed may be generated during this super.run() call

            //skip depsync commit attempt in worker nodes (this would anyway fail from depsync level since autocommit=false for worker nodes)
            if(!CarbonUtils.isWorkerNode()) {
                boolean isRepoChanged = deploymentSyncCommit();

                if (isRepoChanged) {
                    sendRepositorySyncMessage();
                }
            }
        } catch(Throwable t){
            // we cannot let exceptions to be handled in the executor framework. It will kill the thread altogether
            log.error("Error while running deployment scheduler.. " ,t);
        }
    }

    private void deploymentSyncUpdate() {
        if (log.isDebugEnabled()) {
            log.debug("Running deployment synchronizer update... tenant : " + tenantDomain);
        }
        BundleContext bundleContext = CarbonCoreDataHolder.getInstance().getBundleContext();
        if(bundleContext == null){
            return;
        }
        ServiceReference reference = bundleContext.getServiceReference(DeploymentSynchronizer.class.getName());
        if (reference != null) {
            ServiceTracker serviceTracker = new ServiceTracker(bundleContext,
                    DeploymentSynchronizer.class.getName(),
                    null);
            try {
                serviceTracker.open();
                for (Object obj : serviceTracker.getServices()) {
                    DeploymentSynchronizer depsync = (DeploymentSynchronizer) obj;
                    boolean repoUpdateRequired = isRepoUpdateRequired();
                    if (!isInitialUpdateDone || isRepoUpdateFailed || repoUpdateRequired) {
                        // Check if this is a partial update request
                        if (GhostDeployerUtils.isGhostOn() && GhostDeployerUtils.isPartialUpdateEnabled() &&
                            CarbonUtils.isWorkerNode() && tenantId > 0 && repoUpdateRequired) {
                            String repoPath = MultitenantUtils.getAxis2RepositoryPath(tenantId);
                            depsync.update(repoPath, repoPath, 3);
                        } else { // do the normal update
                            depsync.update(tenantId);
                        }
                        isInitialUpdateDone = true;
                        isRepoUpdateFailed = false;
                    }
                }
            } catch (Exception e) {
                log.error("Deployment synchronization update for tenant " + tenantId + " failed", e);
                setRepoUpdateFailed();
            } finally {
                serviceTracker.close();
            }
        }
    }

    /**
     * This method checks and returns whether the repo.update.required parameter is set to true.
     * If the parameter is already set to false, then it will return false.
     * The parameter is accessed and set in SynchronizeRepositoryRequest class.
     * @return true or false based on comparing the current value of this parameter
     */
    private boolean isRepoUpdateRequired() {
        AtomicBoolean value = (AtomicBoolean) axisConfig.getParameter(REPO_UPDATE_REQUIRED).getValue();
        return value.compareAndSet(true, false);
    }


    private boolean deploymentSyncCommit() {
        if (log.isDebugEnabled()) {
            log.debug("Running deployment synchronizer commit... tenant : " + tenantDomain);
        }
        boolean isFilesCommitted = false;
        BundleContext bundleContext = CarbonCoreDataHolder.getInstance().getBundleContext();
        ServiceReference reference = bundleContext.getServiceReference(DeploymentSynchronizer.class.getName());
        if (reference != null) {
            ServiceTracker serviceTracker = new ServiceTracker(bundleContext,
                    DeploymentSynchronizer.class.getName(),
                    null);
            try {
                serviceTracker.open();
                for (Object obj : serviceTracker.getServices()) {
                    DeploymentSynchronizer depsync = (DeploymentSynchronizer) obj;
                    isFilesCommitted = depsync.commit(tenantId);
                    if (isFilesCommitted) {
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Deployment synchronization commit for tenant " + tenantId + " failed", e);
            } finally {
                serviceTracker.close();
            }
        }
        return isFilesCommitted;
    }

    private void sendRepositorySyncMessage() {
        // For sending clustering messages we need to use the super-tenant's AxisConfig (Main Server
        // AxisConfiguration) because we are using the clustering facility offered by the ST in the
        // tenants
        ClusteringAgent clusteringAgent =
                CarbonCoreDataHolder.getInstance().getMainServerConfigContext().
                        getAxisConfiguration().getClusteringAgent();
        if (clusteringAgent != null) {
            int numberOfRetries = 0;
            SynchronizeRepositoryRequest request =
                    new SynchronizeRepositoryRequest(tenantId, tenantDomain);
            while (numberOfRetries < 60) {
                try {
                    clusteringAgent.sendMessage(request, true);
                    log.info("Sent [" + request + "]");
                    break;
                } catch (ClusteringFault e) {
                    numberOfRetries++;
                    if (numberOfRetries < 60) {
                        log.warn("Could not send SynchronizeRepositoryRequest for tenant " +
                                tenantId + ". Retry will be attempted in 2s. Request: " + request, e);
                    } else {
                        log.error("Could not send SynchronizeRepositoryRequest for tenant " +
                                tenantId + ". Several retries failed. Request:" + request, e);
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }

    public void setRepoUpdateFailed() {
        this.isRepoUpdateFailed = true;
    }
}
