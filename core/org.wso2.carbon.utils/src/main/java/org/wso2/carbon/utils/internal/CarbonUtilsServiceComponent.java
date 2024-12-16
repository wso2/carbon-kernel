package org.wso2.carbon.utils.internal;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.deployment.GhostMetaArtifactsLoader;
import org.wso2.carbon.utils.multitenancy.GhostServiceMetaArtifactsLoader;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@Component(name = "org.wso2.carbon.utils.internal.CarbonUtilsServiceComponent", immediate = true)
public class CarbonUtilsServiceComponent {

    @Activate
    protected void activate(ComponentContext ctx) {
        GhostServiceMetaArtifactsLoader serviceMetaArtifactsLoader = new GhostServiceMetaArtifactsLoader();
        ctx.getBundleContext().registerService(GhostMetaArtifactsLoader.class.getName(), serviceMetaArtifactsLoader, null);
        // Read and set diagnostic logs config.
        CarbonUtils.setDiagnosticLogMode(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
        CarbonUtilsDataHolder.getInstance().setDataSource(getKeyStoreDataSource());
    }

    @Reference(name = "org.wso2.carbon.utils.ConfigurationContextService", cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC, unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        CarbonUtilsDataHolder.setConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        CarbonUtilsDataHolder.setConfigContext(null);
    }

    private static synchronized DataSource getKeyStoreDataSource() {

        String dataSourceName = CarbonUtils.getServerConfiguration().getFirstProperty(
                "KeyStoreDataPersistenceManager.DataSourceName");
        if (dataSourceName != null) {
            try {
                return InitialContext.doLookup(dataSourceName);
            } catch (NamingException e) {
                throw new RuntimeException("Error in looking up keystore data source.", e);
            }
        } else {
            throw new RuntimeException("Data source name is not configured for KeyStore Data Persistence Manager.");
        }
    }
}
