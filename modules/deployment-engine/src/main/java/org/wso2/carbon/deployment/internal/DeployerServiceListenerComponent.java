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

package org.wso2.carbon.deployment.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.wso2.carbon.deployment.CarbonDeploymentEngine;
import org.wso2.carbon.deployment.spi.Deployer;


@Component (
        name = "org.wso2.carbon.deployment.internal.DeployerServiceListenerComponent",
        description = "This service  component is responsible for retrieving the Deployer OSGi " +
                      "service and register each deployer with deployment engine",
        immediate = true
)
@Reference (
        name = "carbon.deployer.service",
        referenceInterface = Deployer.class,
        cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        bind = "registerDeployer",
        unbind = "unRegisterDeployer"
)
public class DeployerServiceListenerComponent {
    private static Log log = LogFactory.getLog(DeployerServiceListenerComponent.class);


    private CarbonDeploymentEngine carbonDeploymentEngine = CarbonDeploymentDataHolder.
            getInstance().getCarbonDeploymentEngine();

    protected void registerDeployer(Deployer deployer) {

        try {
            carbonDeploymentEngine.registerDeployer(deployer);
        } catch (Exception e) {
            log.error("Error while adding deployer to the deployment engine", e);
        }

    }

    protected void unRegisterDeployer(Deployer deployer) {
        try {
            carbonDeploymentEngine.unRegisterDeployer(deployer.getType());
        } catch (Exception e) {
            log.error("Error while removing deployer from deployment engine", e);
        }
    }

}
