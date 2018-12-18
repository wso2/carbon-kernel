package org.wso2.carbon.utils.internal;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.deployment.GhostMetaArtifactsLoader;
import org.wso2.carbon.utils.multitenancy.GhostServiceMetaArtifactsLoader;

@Component(name = "org.wso2.carbon.utils.internal.CarbonUtilsServiceComponent", immediate = true)
public class CarbonUtilsServiceComponent {

    @Activate
    protected void activate(ComponentContext ctx) {
        GhostServiceMetaArtifactsLoader serviceMetaArtifactsLoader = new GhostServiceMetaArtifactsLoader();
        ctx.getBundleContext().registerService(GhostMetaArtifactsLoader.class.getName(), serviceMetaArtifactsLoader, null);
    }

    @Reference(name = "org.wso2.carbon.utils.ConfigurationContextService", cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC, unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        CarbonUtilsDataHolder.setConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        CarbonUtilsDataHolder.setConfigContext(null);
    }
}
