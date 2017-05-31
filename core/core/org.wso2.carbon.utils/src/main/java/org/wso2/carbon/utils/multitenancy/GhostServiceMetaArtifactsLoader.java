package org.wso2.carbon.utils.multitenancy;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;
import org.wso2.carbon.utils.deployment.GhostMetaArtifactsLoader;


public class GhostServiceMetaArtifactsLoader implements GhostMetaArtifactsLoader {
    private static Log log = LogFactory.getLog(GhostServiceMetaArtifactsLoader.class);


    @Override
    public void loadArtifacts(AxisConfiguration axisConfiguration, String tenantDomain) {
        try {
            GhostDeployerUtils.deployGhostArtifacts(axisConfiguration);
        } catch (DeploymentException e) {
            log.error("Service ghost meta artifact loading failed for tenant : " + tenantDomain);
        }
    }
}
