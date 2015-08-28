/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.deployment.Artifact;
import org.wso2.carbon.deployment.ArtifactType;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;

import java.net.URL;

/**
 * This interface is used to provide the custom deployment mechanism in carbon, where you
 * can write your own Deployer to process a particular {@link ArtifactType}
 * <p>
 * A developer who wants write a deployer to process an artifact in carbon and add it to a
 * runtime configuration, should implement this. The implementation should then be registered
 * as an OSGi service using the Deployer interface for the DeploymentEngine to find and add
 * to the configuration
 *
 * @since 5.0.0
 */

public interface Deployer {

    /**
     * Initialize the Deployer
     * <p>
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
     * Updates a already deployed artifact and update its relevant runtime configuration
     *
     * @param artifact the Artifact object to deploy
     * @return returns a key to uniquely identify an artifact within a runtime
     * @throws CarbonDeploymentException - when an error occurs while doing the deployment
     */
    Object update(Artifact artifact) throws CarbonDeploymentException;

    /**
     * Returns the deploy directory location associated with the deployer.
     * <p>
     * It can be relative to CARBON_HOME or an abosolute path
     * Eg : webapps, dataservices, sequences  or
     * /dev/wso2/deployment/repository/  or
     * file:/dev/wso2/deployment/repository/
     *
     * @return deployer directory location
     */
    URL getLocation();

    /**
     * Returns the type of the artifact that the deployer is capable of deploying
     * Eg : webapp, dataservice
     *
     * @return ArtifactType object which contains info about the artifact type
     * @see ArtifactType
     */
    ArtifactType getArtifactType();

}
