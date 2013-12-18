/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.deployment.spi;

import org.wso2.carbon.deployment.exception.CarbonDeploymentException;

/**
 * This interface is used to provide the custom deployment mechanism in carbon, where you
 * can write your own Deployer to process a particular artifact type.
 *
 * A developer who wants write a deployer to process an artifact in carbon and add it to a
 * runtime configuration, should implement this.
 */

public interface Deployer {

    /**
     * Initialize the Deployer
     *
     * This will contain all the code that need to be called when the deployer is initialized
     */
    void init();

    /**
     * Process a deployable artifact and add it to the relevant runtime configuration
     *
     * @param artifact the Artifact object to deploy
     * @return returns a key to uniquely identify an artifact within a runtime
     * @throws CarbonDeploymentException - when an error occurs while doing the deployment
     */
    Object deploy(Artifact artifact) throws CarbonDeploymentException;

    /**
     * Remove a given artifact from the relevant runtime configuration
     *
     * @param key the key of the deployed artifact used for undeploying it from the relevant runtime
     * @throws CarbonDeploymentException - when an error occurs while running the undeployment
     */
    void undeploy(Object key) throws CarbonDeploymentException;

    /**
     * Returns the deploy directory associated with the deployer
     *      Eg : webapps, dataservices, sequences
     *
     * @return deployer directory
     */
    String getDirectory();

}
