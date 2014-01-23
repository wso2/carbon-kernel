/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.core.persistence.metadata;


import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DeploymentArtifactMetadataFactory {

    private static final Log log = LogFactory.getLog(DeploymentArtifactMetadataFactory.class);

    public static final String UNIFIED_METADATA_FACTORY = "UnifiedMetadataFactory";
    private ArtifactMetadataManager manager;
    private AxisConfiguration axisConfig;

    public DeploymentArtifactMetadataFactory(AxisConfiguration axisConfig) {
        this.axisConfig = axisConfig;
    }

    public static DeploymentArtifactMetadataFactory getInstance(AxisConfiguration axisConfig) throws AxisFault{

        Object obj = axisConfig.getParameterValue(UNIFIED_METADATA_FACTORY);
        DeploymentArtifactMetadataFactory deploymentArtifactMetadataFactory;
        if (obj instanceof DeploymentArtifactMetadataFactory) {
            deploymentArtifactMetadataFactory = (DeploymentArtifactMetadataFactory) obj;
        } else {
            deploymentArtifactMetadataFactory = new DeploymentArtifactMetadataFactory(axisConfig);
            axisConfig.addParameter(UNIFIED_METADATA_FACTORY, deploymentArtifactMetadataFactory);
        }
        return deploymentArtifactMetadataFactory;
    }
    

    public ArtifactMetadataManager getMetadataManager() {
        if (manager == null) {
            try {
                manager = new ArtifactMetadataManager(axisConfig);
            } catch (ArtifactMetadataException e) {
                log.error("Error while initializing the " +
                        "ServiceGroupPersistenceManager instance", e);
            }
        }
        return manager;

    }
}
