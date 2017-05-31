package org.wso2.carbon.utils.internal;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.deployment.GhostMetaArtifactsLoader;
import org.wso2.carbon.utils.multitenancy.GhostServiceMetaArtifactsLoader;

/**
* @scr.component name="org.wso2.carbon.utils.internal.CarbonUtilsServiceComponent" immediate="true"
* @scr.reference name="configuration.context.service"
* interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
* policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
*/

public class CarbonUtilsServiceComponent {

    protected void activate(ComponentContext ctx) {
        GhostServiceMetaArtifactsLoader serviceMetaArtifactsLoader = new GhostServiceMetaArtifactsLoader();
        ctx.getBundleContext().registerService(GhostMetaArtifactsLoader.class.getName(), serviceMetaArtifactsLoader, null);
    }


    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        CarbonUtilsDataHolder.setConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        CarbonUtilsDataHolder.setConfigContext(null);
    }
}
