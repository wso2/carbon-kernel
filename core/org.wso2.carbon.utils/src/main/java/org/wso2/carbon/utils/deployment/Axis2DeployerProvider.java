package org.wso2.carbon.utils.deployment;

import org.wso2.carbon.utils.component.xml.config.DeployerConfig;


public interface Axis2DeployerProvider {

    public DeployerConfig[] getDeployerConfigs();
}
