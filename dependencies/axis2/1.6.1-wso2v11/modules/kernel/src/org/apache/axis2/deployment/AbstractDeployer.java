/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.apache.axis2.deployment;

import org.apache.axis2.deployment.repository.util.DeploymentFileData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AbstractDeployer class which can be extended by all Axis2 deployers
 */
public abstract class AbstractDeployer implements Deployer{

    /**
     * The Map<String absoluteFilePath, DeploymentFileData data> of all artifacts deployed by this
     * deployer. 
     */
    protected Map<String, DeploymentFileData> deploymentFileDataMap
            = new ConcurrentHashMap<String, DeploymentFileData>();

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        deploymentFileDataMap.put(deploymentFileData.getAbsolutePath(), deploymentFileData);
    }

    public void undeploy(String fileName) throws DeploymentException {
        deploymentFileDataMap.remove(fileName);
    }

    public void cleanup() throws DeploymentException {
        // Deployers which require cleaning up should override this method
    }
}
