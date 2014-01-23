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

package org.apache.axis2.scripting;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.repository.util.WSInfo;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDLToAxisServiceBuilder;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An Axis2 DeploymentEngine subclass for deploying script services 
 * supporting hot deploy and hot update.
 */
public class ScriptDeploymentEngine extends DeploymentEngine {

    private static final Log log = LogFactory.getLog(ScriptModule.class);

    public static final String SCRIPT_HOT_UPDATE_ = "scripts.hotupdate";

    private AxisConfiguration realAxisConfig;

    public ScriptDeploymentEngine(AxisConfiguration realAxisConfig) {
        this.axisConfig = new AxisConfiguration();
        this.realAxisConfig = realAxisConfig;
    }

    public void loadRepository(File scriptsRepoFile) throws DeploymentException {
        this.repoListener = new ScriptRepositoryListener(this);
        this.servicesDir = scriptsRepoFile;

        Parameter scriptsHotUpdateParameter = axisConfig.getParameter(SCRIPT_HOT_UPDATE_);
        this.hotUpdate = scriptsHotUpdateParameter == null || "true".equals(scriptsHotUpdateParameter.getValue());
    }

    public void doDeploy() {
        List alreadyDeployed = new ArrayList();
        for (int i = 0; i < wsToDeploy.size(); i++) {
            try {

            	DeploymentFileData archiveFileData = (DeploymentFileData)wsToDeploy.get(i);
                deployService(alreadyDeployed, archiveFileData);

            } catch (Exception e) {
                e.printStackTrace();
                log.warn("Exception deploying script service: " + ((DeploymentFileData)wsToDeploy.get(i)).getName());
            }
        }
        wsToDeploy.clear();
    }

    private void deployService(List alreadyDeployed, DeploymentFileData archiveFileData) throws AxisFault {
        File file = archiveFileData.getFile();

        File wsdlFile;
        File scriptFile;
        if (file.toString().endsWith(".wsdl")) {
            wsdlFile = file;
            scriptFile = getScriptForWSDL(wsdlFile);
        } else {
            scriptFile = file;
            wsdlFile = getWSDLForScript(scriptFile);
        }

        if (scriptFile != null && wsdlFile != null && !alreadyDeployed.contains(scriptFile.toURI()) && scriptFile.exists() && wsdlFile.exists()) {
            AxisService axisService = createService(wsdlFile, scriptFile);
            AxisServiceGroup axisServiceGroup = new AxisServiceGroup(axisConfig);
            axisServiceGroup.setServiceGroupClassLoader(axisService.getClassLoader());
            axisServiceGroup.setServiceGroupName(axisService.getName());
            axisServiceGroup.addService(axisService);
            realAxisConfig.addServiceGroup(axisServiceGroup);
            alreadyDeployed.add(scriptFile.toURI());
            log.info("Deployed script service '" + axisService.getName() + "' for script: " + scriptFile.getName());
        }
    }

    public void unDeploy() {
        String serviceName = null;
        List undeployed = new ArrayList();
        for (int i = 0; i < wsToUnDeploy.size(); i++) {
            try {
                WSInfo wsInfo = (WSInfo)wsToUnDeploy.get(i);
                String fileName = Utils.getShortFileName(wsInfo.getFileName());
//                if (wsInfo.getType() == TYPE_SERVICE) {
                    if (isHotUpdate()) {
                        try {
                            serviceName = getAxisServiceName(fileName);
                            if (!undeployed.contains(serviceName)) {
                                realAxisConfig.removeServiceGroup(serviceName);
                                undeployed.add(serviceName);
                                // TODO: need a way to also remove the ServiceGroup from the ConfigContext.applicationSessionServiceGroupContextTable
                                log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED, serviceName));
                            }
                        } catch (AxisFault axisFault) {
                            // May be a faulty service
                            realAxisConfig.removeFaultyService(serviceName);
                            log.debug("removeFaultyService: " + fileName);
                        }
                    } else {
                        realAxisConfig.removeFaultyService(serviceName);
                        log.debug("not hotUpdate, removeFaultyService: " + fileName);
                    }
//                }
            } catch (Exception e) {
                log.warn(e);
            }
        }
        wsToUnDeploy.clear();
    }

    /*
     * Override the DeploymentEngine method return an empty modules directory as
     * its not required for script services
     */
    public File getModulesDir() {
        return new File("");
    }

    /**
     * Gets the script associated with the wsdl. The associated script is the
     * file with the same name as the wsdl file excluding the file suffix, for
     * example, stockquote.js and stockquote.wsdl.
     */
    protected File getScriptForWSDL(File wsdlFile) {
        String wsdlFileName = wsdlFile.getName();
        String fileSuffix = wsdlFileName.substring(0, wsdlFileName.lastIndexOf('.') );
        File wsdlFileDir = wsdlFile.getParentFile();
        String[] files = wsdlFileDir.list();
        for (int i = 0; i < files.length; i++) {
            if (!files[i].equals(wsdlFileName) && files[i].startsWith(fileSuffix)) {
                File scriptFile = new File(wsdlFileDir, files[i]);
                return scriptFile;
            }
        }
        return null;
    }

    /**
     * Gets the WSDL associated with the script. The associated WSDL is the file
     * with the same name as the script file excluding the file suffix, for
     * example, stockquote.js and stockquote.wsdl.
     */
    protected File getWSDLForScript(File scriptFile) {
        String scriptFileName = scriptFile.getName();
        String fileSuffix = scriptFileName.substring(0, scriptFileName.lastIndexOf('.'));
        File scriptFileDir = scriptFile.getParentFile();
        File wsdlFile = new File(scriptFileDir, fileSuffix + ".wsdl");
        return wsdlFile;
    }

    /**
     * Creates an Axis2 service for the script
     */
    protected AxisService createService(File wsdlFile, File scriptFile) {
        AxisService axisService = null;
        try {

            InputStream definition;
            try {
                definition = wsdlFile.toURL().openStream();
            } catch (Exception e) {
                throw new AxisFault("exception opening wsdl", e);
            }

            WSDLToAxisServiceBuilder builder = new WSDL11ToAxisServiceBuilder(definition);
            builder.setServerSide(true);
            axisService = builder.populateService();

            //axisService.setScope(Constants.SCOPE_APPLICATION);
            Parameter userWSDL = new Parameter("useOriginalwsdl", "true");
            axisService.addParameter(userWSDL);

            Parameter scriptSrc = new Parameter(ScriptReceiver.SCRIPT_SRC_PROP, readScriptSource(scriptFile));
            axisService.addParameter(scriptSrc);

            ScriptReceiver scriptReceiver = new ScriptReceiver();
            axisService.addMessageReceiver("http://www.w3.org/2004/08/wsdl/in-out", scriptReceiver);

            // TODO: Shouldn't this be done by WSDLToAxisServiceBuilder.populateService?
            for (Iterator it = axisService.getOperations(); it.hasNext();) {
                AxisOperation operation = (AxisOperation)it.next();
                operation.setMessageReceiver(scriptReceiver);
            }

            Parameter scriptParam = new Parameter(ScriptReceiver.SCRIPT_ATTR, scriptFile.toString());
            axisService.addParameter(scriptParam);

            axisService.setName(scriptFile.getName().substring(0, scriptFile.getName().lastIndexOf('.')));

        } catch (Throwable e) {
            log.warn("AxisFault creating script service", e);
            e.printStackTrace();
        }

        return axisService;
    }

    /**
     * Reads the complete script source code into a String
     */
    protected String readScriptSource(File scriptFile) throws AxisFault {
        InputStream is;
        try {
            is = scriptFile.toURL().openStream();
        } catch (IOException e) {
            throw new AxisFault("IOException opening script: " + scriptFile, e);
        }
        try {
            Reader reader = new InputStreamReader(is, "UTF-8");
            char[] buffer = new char[1024];
            StringBuffer source = new StringBuffer();
            int count;
            while ((count = reader.read(buffer)) > 0) {
                source.append(buffer, 0, count);
            }
            return source.toString();
        } catch (IOException e) {
            throw new AxisFault("IOException reading script: " + scriptFile, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new AxisFault("IOException closing script: " + scriptFile, e);
            }
        }
    }

}
