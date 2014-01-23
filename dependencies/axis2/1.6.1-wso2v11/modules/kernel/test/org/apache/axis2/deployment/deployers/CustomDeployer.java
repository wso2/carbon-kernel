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

package org.apache.axis2.deployment.deployers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;

/**
 * A custom Deployer instance for testing.  This coordinates with the CustomDeployerTest and
 * the CustomDeployerRepo/ repository in test-resources/.  It's going to be set up to deploy
 * "svc" files, both in the services/ directory and in the widgets/ directory.  After the
 * config is complete, we should have 2 deployed items, and should have found both "Mary" and
 * "George" (see the repo for test data).  We use static fields to keep all this information
 * in a way that the test can access it.
 */
public class CustomDeployer extends AbstractDeployer {
    protected static final Log log = LogFactory.getLog(CustomDeployer.class);

    /** Has init() been called? */
    public static boolean initCalled;
    /** This is set to the last argument to setDirectory() */
    public static String directory;
    /** This is set to the last argument to setExtension() */
    public static String extension;
    /** The count of deployed items */
    public static int deployedItems;
    /** Set to true if "George" has been deployed */
    public static boolean georgeDeployed;
    /** Set to true if "Mary" has been deployed */
    public static boolean maryDeployed;

    public static final String PARAM_NAME = "customDeployerParam";
    public static final String PARAM_VAL = "customDeployer parameter value";

    /**
     * Initialize the Deployer
     *
     * @param configCtx our ConfigurationContext
     */
    public void init(ConfigurationContext configCtx) {
        initCalled = true;

        // Set a property on the AxisConfig just to make sure we end up with the right one
        try {
            configCtx.getAxisConfiguration().addParameter(PARAM_NAME, PARAM_VAL);
        } catch (AxisFault axisFault) {
            // Two comments - 1) why the heck does addParameter throw AxisFault?
            //                2) why doesn't init() throw AxisFault?
        }
    }

    /**
     * Process a file and add it to the configuration
     *
     * @param deploymentFileData the DeploymentFileData object to deploy
     * @throws org.apache.axis2.deployment.DeploymentException
     *          if there is a problem
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        log.info("Deploying - " + deploymentFileData.getName());
        try {
            FileInputStream fis = new FileInputStream(deploymentFileData.getFile());
            int x= fis.available();
            byte b[]= new byte[x];
            fis.read(b);
            String content = new String(b);
            if (content.indexOf("George") > -1) georgeDeployed = true;
            if (content.indexOf("Mary") > -1) maryDeployed = true;
            deployedItems++;
            super.deploy(deploymentFileData);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Set the directory
     *
     * @param directory directory name
     */
    public void setDirectory(String directory) {
        CustomDeployer.directory = directory;
    }

    /**
     * Set the extension to look for TODO: Support multiple extensions?
     *
     * @param extension the file extension associated with this Deployer
     */
    public void setExtension(String extension) {
        CustomDeployer.extension = extension;
    }

    /**
     * Remove a given file from the configuration
     *
     * @param fileName name of item to remove
     * @throws org.apache.axis2.deployment.DeploymentException
     *          if there is a problem
     */
    public void undeploy(String fileName) throws DeploymentException {
        // Undeploy
        super.undeploy(fileName);
    }
}
