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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class TransportDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(TransportDeployer.class);

    private ConfigurationContext configCtx;
    private AxisConfiguration axisConfig;

    public void init(ConfigurationContext configCtx) {
        this.configCtx = configCtx;
        axisConfig = configCtx.getAxisConfiguration();
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        boolean isDirectory = deploymentFileData.getFile().isDirectory();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            deploymentFileData.setClassLoader(isDirectory,
                    axisConfig.getModuleClassLoader(),
                    (File) axisConfig.getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR),
                    axisConfig.isChildFirstClassLoading());

            ClassLoader loader = deploymentFileData.getClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            InputStream xmlStream = loader.getResourceAsStream("META-INF/transport.xml");
            OMElement element = (OMElement) XMLUtils.toOM(xmlStream);
            element.build();
            AxisConfigBuilder builder = new AxisConfigBuilder(axisConfig);
            // Processing Transport Receivers
            Iterator trs_Reivers =
                    element.getChildrenWithName(new QName(DeploymentConstants.TAG_TRANSPORT_RECEIVER));
            ArrayList transportReceivers = builder.processTransportReceivers(trs_Reivers);
            for (int i = 0; i < transportReceivers.size(); i++) {
                TransportInDescription transportInDescription = (TransportInDescription) transportReceivers.get(i);
                Parameter paramter = transportInDescription.getParameter("AutoStart");
                if (paramter != null) {
                    configCtx.getListenerManager().addListener(transportInDescription, false);
                    log.info("starting the transport : " + transportInDescription.getName());
                }
            }

            // Processing Transport Senders
            Iterator trs_senders =
                    element.getChildrenWithName(new QName(DeploymentConstants.TAG_TRANSPORT_SENDER));

            builder.processTransportSenders(trs_senders);
            super.deploy(deploymentFileData);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public void setDirectory(String directory) {
    }

    public void setExtension(String extension) {
    }

    public void undeploy(String fileName) throws DeploymentException {
        super.undeploy(fileName);
    }
}
