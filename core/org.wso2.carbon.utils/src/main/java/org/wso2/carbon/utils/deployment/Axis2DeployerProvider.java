package org.wso2.carbon.utils.deployment;

import org.wso2.carbon.utils.component.xml.config.DeployerConfig;

import java.util.List;


public interface Axis2DeployerProvider {

    public List<DeployerConfig> getDeployerConfigs();
}
