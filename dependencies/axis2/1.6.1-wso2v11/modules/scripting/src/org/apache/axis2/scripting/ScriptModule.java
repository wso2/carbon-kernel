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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.modules.Module;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Axis2 module to initialize script services. Uses a ScriptDeploymentEngine to
 * find all the scripts in the scriptServices directory and deploy them as Axis2
 * services.
 */
public class ScriptModule implements Module {

    private static final Log log = LogFactory.getLog(ScriptModule.class);

    static String defaultEncoding = new OutputStreamWriter(System.out).getEncoding();

    /**
     * Init by creating and deploying AxisServices for each script
     */
    public void init(ConfigurationContext configContext, AxisModule module) throws AxisFault {

        log.debug("script services init");

        AxisConfiguration axisConfig = configContext.getAxisConfiguration();
        if(axisConfig.getRepository() == null) {
            log.error("AxisConfiguration getRepository returns null, cannot deploy scripts");
        } else {
            File scriptServicesDirectory = getScriptServicesDirectory(axisConfig);
            ScriptDeploymentEngine deploymentEngine = new ScriptDeploymentEngine(axisConfig);
            deploymentEngine.loadRepository(scriptServicesDirectory);
            deploymentEngine.loadServices();
        }
        log.debug("script module activated");
    }

    /**
     * Gets the repo directory for the scripts. The scripts directory is a
     * sub-directory of the Axis2 repository directory. Its name may be defined
     * by a 'scriptServicesDir' property in the axis2.xml otherwise it defaults
     * a to a directory named 'scriptServices'.
     */
    protected File getScriptServicesDirectory(AxisConfiguration axisConfig) throws DeploymentException {
        try {

            URL axis2Repository = axisConfig.getRepository();
            Parameter scriptsDirParam = axisConfig.getParameter("scriptServicesDir");
            String scriptsDir = scriptsDirParam == null ? "scriptServices" : (String)scriptsDirParam.getValue();

            String path = URLDecoder.decode(axis2Repository.getPath(), defaultEncoding);
            java.io.File repoDir =
                    new java.io.File(path.replace('/', File.separatorChar).replace('|', ':'));

            return new File(repoDir, scriptsDir);
        } catch (UnsupportedEncodingException e) {
            throw new DeploymentException("UnsupportedEncodingException getting script service directory", e);
        }
    }

    // --- the following are unused methods on the Module interface ---

    public void applyPolicy(Policy policy, AxisDescription axisDescription) throws AxisFault {
    }

    public boolean canSupportAssertion(Assertion assertion) {
        return false;
    }

    public void engageNotify(AxisDescription axisDescription) throws AxisFault {
    }

    public void shutdown(ConfigurationContext configurationContext) throws AxisFault {
    }

}
