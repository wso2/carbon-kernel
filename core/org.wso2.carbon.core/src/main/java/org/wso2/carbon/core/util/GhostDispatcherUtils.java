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

package org.wso2.carbon.core.util;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.deployment.DeploymentSynchronizer;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.utils.deployment.GhostDeployer;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;

import java.io.File;
import java.net.URL;

public class GhostDispatcherUtils {

    private static Log log = LogFactory.getLog(GhostDispatcherUtils.class);

    private GhostDispatcherUtils() {
    }

    /**
     * This method handles the partial synchronizing/updating of the service file
     * synchronized to avoid concurrent dep sycnh updates of the same file
     * @param axisConfig - axis configuration of the tenant
     * @param axisService - ghost service
     */
    public synchronized static void handleDepSynchUpdate(AxisConfiguration axisConfig,
                                                         AxisService axisService) {
        String deploymentDir = CarbonConstants.SERVICES_HOTDEPLOYMENT_DIR, extension = "aar";
        String repoPath = axisConfig.getRepository().getPath();
        // this method should run only in tenant mode
        if (repoPath.contains(CarbonConstants.TENANTS_REPO)) {
            Parameter serviceType = axisService.
                    getParameter(CarbonConstants.GHOST_ATTR_SERVICE_TYPE);
            // check for different service types
            if (serviceType != null) {
                if (serviceType.getValue().equals("js_service")) {
                    deploymentDir = CarbonConstants.JS_SERVICES_HOTDEPLOYMENT_DIR;
                    extension = "js";
                } else if (serviceType.getValue().equals("data_service")) {
                    deploymentDir = CarbonConstants.DSS_SERVICES_HOTDEPLOYMENT_DIR;
                    extension = "dbs";
                }
            }

            String fileName = axisService.getFileName().getFile();
            String serviceGroupName = axisService.getAxisServiceGroup().getServiceGroupName();
            String firstDepthDir = "";
            if (fileName != null && !(fileName.contains(CarbonConstants.SERVICES_HOTDEPLOYMENT_DIR)
                    || fileName.contains(CarbonConstants.JS_SERVICES_HOTDEPLOYMENT_DIR)
                    || fileName.contains(CarbonConstants.DSS_SERVICES_HOTDEPLOYMENT_DIR))) {
                String filePath;
                if (serviceGroupName.contains("/")) {
                    firstDepthDir = serviceGroupName.substring(0, serviceGroupName.indexOf('/'));
                    filePath = repoPath + File.separator + deploymentDir + File.separator +
                               firstDepthDir;
                } else {
                    filePath = repoPath + File.separator + deploymentDir + File.separator + fileName;
                }

                BundleContext bundleContext = CarbonCoreDataHolder.getInstance().getBundleContext();
                ServiceReference reference = bundleContext.
                        getServiceReference(DeploymentSynchronizer.class.getName());
                if (reference != null) {
                    ServiceTracker serviceTracker =
                            new ServiceTracker(bundleContext,
                                               DeploymentSynchronizer.class.getName(), null);
                    try {
                        serviceTracker.open();
                        for (Object obj : serviceTracker.getServices()) {
                            DeploymentSynchronizer depsync = (DeploymentSynchronizer) obj;

                            // update service file
                            depsync.update(repoPath, filePath, 3);

                            // update service metafile
                            String serviceMetaFileDirPath = repoPath + File.separator +
                                                CarbonConstants.SERVICE_METAFILE_HOTDEPLOYMENT_DIR;
                            String serviceMetaFilePath;
                            String absoluteFilePath = filePath;
                            if (serviceGroupName.contains("/")) {
                                serviceMetaFilePath = serviceMetaFileDirPath + File.separator +
                                                      firstDepthDir;
                                absoluteFilePath = repoPath + File.separator + deploymentDir +
                                                   File.separator + serviceGroupName.substring(0,
                                                   serviceGroupName.lastIndexOf('/')) +
                                                   File.separator + fileName;
                            } else {
                                serviceMetaFilePath = serviceMetaFileDirPath + File.separator +
                                                      axisService.getAxisServiceGroup().
                                                              getServiceGroupName() + ".xml";
                            }
                            depsync.update(repoPath, serviceMetaFilePath, 3);
                            File fileToUpdate = new File(absoluteFilePath);

                            if (fileToUpdate.exists()) {
                                axisService.setFileName(new URL("file:" + absoluteFilePath));
                                DeploymentFileData dfd = new DeploymentFileData(fileToUpdate);
                                GhostDeployer ghostDeployer = GhostDeployerUtils.
                                        getGhostDeployer(axisConfig);
                                if (ghostDeployer != null && ghostDeployer.
                                        getFileData(filePath) == null) {
                                    dfd.setDeployer(ghostDeployer.
                                            getDeployer(deploymentDir, extension));
                                    if (log.isDebugEnabled()) {
                                        log.debug("Adding deployment file data to ghostdeployer :" +
                                                  fileName);
                                    }
                                    ghostDeployer.addFileData(dfd);
                                }
                            }
                        }
                    } catch (Throwable t) {
                        log.error("Deployment synchronization update failed", t);
                    } finally {
                        serviceTracker.close();
                    }
                }
            }
        }
    }

    public static void deployServiceMetaFile(String serviceGroupName,
                                             AxisConfiguration axisConfig) {
        if (serviceGroupName != null) {
            synchronized (serviceGroupName.intern()) {
                String axisRepo = axisConfig.getRepository().getPath();
                String serviceMetaFileDirPath = axisRepo + File.separator +
                                                CarbonConstants.SERVICE_METAFILE_HOTDEPLOYMENT_DIR;
                String serviceMetaFilePath = serviceMetaFileDirPath + File.separator +
                                             serviceGroupName + ".xml";

                GhostDeployer ghostDeployer = GhostDeployerUtils.getGhostDeployer(axisConfig);
                File serviceMetaFile = new File(serviceMetaFilePath);
                if (serviceMetaFile.exists() && ghostDeployer != null) {
                    DeploymentFileData metaFileDFD = new DeploymentFileData(serviceMetaFile);
                    metaFileDFD.setDeployer(ghostDeployer.
                            getDeployer(CarbonConstants.SERVICE_METAFILE_HOTDEPLOYMENT_DIR, "xml"));
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("Deploying service metafile : " + serviceMetaFile);
                        }
                        metaFileDFD.deploy();
                    } catch (DeploymentException e) {
                        log.error("Error while deploying service metafile : " + serviceMetaFile);
                    }
                }
            }
        }
    }
}