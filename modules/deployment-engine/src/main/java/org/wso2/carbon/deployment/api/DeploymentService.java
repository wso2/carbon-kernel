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

package org.wso2.carbon.deployment.api;

import org.wso2.carbon.deployment.ArtifactType;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;

/**
 * User level API's for consuming DeploymentEngine functionality.
 * This will be registered as an OSGI service so that users can reference this in their component.
 * <p>
 * If the given artifact type is not recognized at DeploymentEngine level or there is no deployer
 * associated with the give artifact type, then these API's will result in unknown artifact type error.
 *
 * @since 5.0.0
 */

public interface DeploymentService {
    /**
     * User can call this method externally to deploy an artifact by giving the artifact deployment
     * directory and the path
     *
     * @param artifactPath path where the artifact resides. This has to be the full qualified path
     *                     of the artifact
     * @param artifactType the type of the artifact going to be dpeloyed
     *                     Eg : webapp, dataservice, sequence
     * @throws CarbonDeploymentException - on error while trying deploy the given artifact info
     * @see ArtifactType
     */
    void deploy(String artifactPath, ArtifactType artifactType) throws CarbonDeploymentException;

    /**
     * When you want to undeploy an artifact, this method can be called by giving the key,
     * which uniquely identifies an artifact in a runtime and the artifact deployment
     * directory
     *
     * @param key artifact key to uniquely identify an artifact in a runtime
     *            Eg: for webapps this can be webapp context such as /foo , /bar, etc
     *                for service this can be service name such as EchoService, VersionService, etc
     * @param artifactType the type of the artifact going to be dpeloyed
     *                     Eg : webapp, dataservice, sequence
     * @throws CarbonDeploymentException - on error while trying undeploy the given artifact info
     * @see ArtifactType
     */
    void undeploy(Object key, ArtifactType artifactType) throws CarbonDeploymentException;

    /**
     * When you want to redeploy/update an artifact, this method can be called by giving the key,
     * which uniquely identifies an artifact in a runtime and the artifact deployment
     * directory
     *
     * @param key artifact key to uniquely identify an artifact in a runtime
     *            Eg: for webapps this can be webapp context such as /foo , /bar, etc
     *                for service this can be service name such as EchoService, VersionService, etc
     * @param artifactType the type of the artifact going to be dpeloyed
     *                     Eg : webapp, dataservice, sequence
     * @throws CarbonDeploymentException - on error while trying redeploy the given artifact info
     * @see ArtifactType
     */
    void redeploy(Object key, ArtifactType artifactType) throws CarbonDeploymentException;
}
