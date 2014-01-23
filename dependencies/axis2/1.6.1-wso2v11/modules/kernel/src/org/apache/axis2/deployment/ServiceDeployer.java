/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ServiceDeployer extends AbstractDeployer {
    private static final Log log = LogFactory.getLog(ServiceDeployer.class);
    private AxisConfiguration axisConfig;
    private ConfigurationContext configCtx;
    private String directory;

    //To initialize the deployer
    public void init(ConfigurationContext configCtx) {
        this.configCtx = configCtx;
        this.axisConfig = this.configCtx.getAxisConfiguration();
    }

    //Will process the file and add that to axisConfig

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        boolean isDirectory = deploymentFileData.getFile().isDirectory();
        ArchiveReader archiveReader;
        StringWriter errorWriter = new StringWriter();
        archiveReader = new ArchiveReader();
        String serviceStatus = "";
        try {
            deploymentFileData.setClassLoader(isDirectory,
                                              axisConfig.getServiceClassLoader(),
                    (File)axisConfig.getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR),
                    axisConfig.isChildFirstClassLoading());
            HashMap<String,AxisService> wsdlservice = archiveReader.processWSDLs(deploymentFileData);
            if (wsdlservice != null && wsdlservice.size() > 0) {
                for (AxisService service : wsdlservice.values()) {
                    Iterator<AxisOperation> operations = service.getOperations();
                    while (operations.hasNext()) {
                        AxisOperation axisOperation = operations.next();
                        axisConfig.getPhasesInfo().setOperationPhases(axisOperation);
                    }
                }
            }
            AxisServiceGroup serviceGroup = new AxisServiceGroup(axisConfig);
            serviceGroup.setServiceGroupClassLoader(deploymentFileData.getClassLoader());
            ArrayList<AxisService> serviceList = archiveReader.processServiceGroup(
                    deploymentFileData.getAbsolutePath(), deploymentFileData,
                    serviceGroup, isDirectory, wsdlservice,
                    configCtx);
            URL location = deploymentFileData.getFile().toURL();

            // Add the hierarchical path to the service group
            if (location != null) {
                String serviceHierarchy = Utils.getServiceHierarchy(location.getPath(),
                        this.directory);
                if (serviceHierarchy != null && !"".equals(serviceHierarchy)) {
                    serviceGroup.setServiceGroupName(serviceHierarchy
                            + serviceGroup.getServiceGroupName());
                    for (AxisService axisService : serviceList) {
                        axisService.setName(serviceHierarchy + axisService.getName());
                    }
                }
            }
            DeploymentEngine.addServiceGroup(serviceGroup,
                                             serviceList,
                                             location,
                                             deploymentFileData,
                                             axisConfig);

            super.deploy(deploymentFileData);
        } catch (DeploymentException de) {
            de.printStackTrace();
            log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_SERVICE,
                                          deploymentFileData.getName(),
                                          de.getMessage()),
                      de);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            de.printStackTrace(error_ptintWriter);
            serviceStatus = "Error:\n" + errorWriter.toString();

            throw de;

        } catch (AxisFault axisFault) {
            log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_SERVICE,
                                          deploymentFileData.getName(),
                                          axisFault.getMessage()),
                      axisFault);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            axisFault.printStackTrace(error_ptintWriter);
            serviceStatus = "Error:\n" + errorWriter.toString();

            throw new DeploymentException(axisFault);

        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                log.info(Messages.getMessage(
                        DeploymentErrorMsgs.INVALID_SERVICE,
                        deploymentFileData.getName(),
                        sw.getBuffer().toString()));
            }
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            e.printStackTrace(error_ptintWriter);
            serviceStatus = "Error:\n" + errorWriter.toString();

            throw new DeploymentException(e);

        } catch (Throwable t) {
            if (log.isInfoEnabled()) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                log.info(Messages.getMessage(
                        DeploymentErrorMsgs.INVALID_SERVICE,
                        deploymentFileData.getName(),
                        sw.getBuffer().toString()));
            }
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            t.printStackTrace(error_ptintWriter);
            serviceStatus = "Error:\n" + errorWriter.toString();

            throw new DeploymentException(new Exception(t));

        } finally {
            if (serviceStatus.startsWith("Error:")) {
                axisConfig.getFaultyServices().put(deploymentFileData.getFile().getAbsolutePath(),
                                                   serviceStatus);
            }
        }
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setExtension(String extension) {
    }

    public void undeploy(String fileName) throws DeploymentException {
        try {
            //find the hierarchical part of the service group name
            String serviceHierarchy = Utils.getServiceHierarchy(fileName, this.directory);
            fileName = Utils.getShortFileName(fileName);
            fileName = DeploymentEngine.getAxisServiceName(fileName);

            //attach the hierarchical part if it is not null
            if (serviceHierarchy != null) {
                fileName = serviceHierarchy + fileName;
            }
            AxisServiceGroup serviceGroup = axisConfig.removeServiceGroup(fileName);
            //Fixed - https://issues.apache.org/jira/browse/AXIS2-4610
            if (serviceGroup != null) {
                for (Iterator services = serviceGroup.getServices(); services.hasNext();) {
                AxisService axisService = (AxisService) services.next();
                ServiceLifeCycle serviceLifeCycle = axisService.getServiceLifeCycle();
                if (serviceLifeCycle != null) {
                    serviceLifeCycle.shutDown(configCtx, axisService);
                }
            }
                configCtx.removeServiceGroupContext(serviceGroup);
                log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED,
                        fileName));
            } else {
                axisConfig.removeFaultyService(fileName);
            }
            super.undeploy(fileName);
        } catch (AxisFault axisFault) {
            //May be a faulty service
            axisConfig.removeFaultyService(fileName);

            throw new DeploymentException(axisFault);
        }
    }
}
