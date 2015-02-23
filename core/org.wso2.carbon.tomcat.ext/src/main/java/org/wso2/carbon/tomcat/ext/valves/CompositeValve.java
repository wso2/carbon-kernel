package org.wso2.carbon.tomcat.ext.valves;

import org.apache.catalina.Realm;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.registry.core.ghostregistry.GhostRegistry;
import org.wso2.carbon.tomcat.ext.internal.CarbonRealmServiceHolder;
import org.wso2.carbon.tomcat.ext.internal.Utils;
import org.wso2.carbon.tomcat.ext.realms.CarbonTomcatRealm;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserRealmService;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * tomcat does not allow us to engage a valve programmatically once it is started. Hence we register this
 * {@link CompositeValve} instance that delegates the {@link #invoke(org.apache.catalina.connector.Request, org.apache.catalina.connector.Response)}
 * calls to CarbonTomcatValves.
 * <p/>
 *
 * @see CarbonTomcatValve
 */
@SuppressWarnings("unused")
public class CompositeValve extends ValveBase {
    private static Log log = LogFactory.getLog(CompositeValve.class);
    private static final String ENABLE_SAAS = "carbon.enable.saas"; //deprecated.

    public CompositeValve() {
        //enable async support
        super(true);
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        try {

            String enableSaaSParam =
                    request.getContext().findParameter(ENABLE_SAAS);
            Realm realm = request.getContext().getRealm();

            // deprecation notice since Carbon 4.4. Users should configure SaaS mode by adding the CarbonTomcatRealm to the
            // META-INF/context.xml. See javadocs at @org.wso2.carbon.tomcat.ext.realms.CarbonTomcatRealm.
            // Remove this check in a future release.
            if (realm instanceof CarbonTomcatRealm) {
                //user has set saas context-param but not configured the new way of configuring saas via context.xml
                if (enableSaaSParam != null && ((CarbonTomcatRealm) realm).getSaasRules() == null)  {
                    String contextName = request.getContext().getName();
                    log.warn("To enable SaaS mode for the webapp, " + contextName +
                             ", configure the CarbonTomcatRealm in META-INF/context.xml.");
                }

            }

            TomcatValveContainer.invokeValves(request, response, this);
            // ------------ Absolutely no code below this line -----------------------
            // --------- Valve chaining happens from here onwards --------------------

        } catch (Exception e) {
            log.error("Could not handle request: " + request.getRequestURI(), e);
        }
    }

    public void continueInvocation(Request request, Response response) {
        //setting the tenant and Realm in CarbonContext in case url mapping request
        // got loaded with tenants and Mappings Map in TenantLazyLoaderValve.
        try {
            String requestedHostName = request.getHost().getName();
            String defaultHost = URLMappingHolder.getInstance().getDefaultHost();
            if (!requestedHostName.equalsIgnoreCase(defaultHost)) {
                String tenantDomain = Utils.getTenantDomainFromURLMapping(request);
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                if (!tenantDomain.equalsIgnoreCase(carbonContext.getTenantDomain()))
                    carbonContext.setTenantDomain(tenantDomain);
                carbonContext.
                        setApplicationName(Utils.getAppNameForURLMapping(request));
                UserRealmService userRealmService = CarbonRealmServiceHolder.getRealmService();
                TenantManager tenantManager = userRealmService.getTenantManager();
                int tenantId = tenantManager.getTenantId(tenantDomain);
                carbonContext.setTenantId(tenantId);
                carbonContext.setUserRealm(userRealmService.getTenantUserRealm(tenantId));

                RegistryService registryService = CarbonRealmServiceHolder.getRegistryService();
                carbonContext.setRegistry(RegistryType.SYSTEM_CONFIGURATION,
                                          new GhostRegistry(registryService, tenantId, RegistryType.SYSTEM_CONFIGURATION));
                carbonContext.setRegistry(RegistryType.SYSTEM_GOVERNANCE,
                                          new GhostRegistry(registryService, tenantId, RegistryType.SYSTEM_GOVERNANCE));
            }
            int status = response.getStatus();
            if (status != Response.SC_MOVED_TEMPORARILY && status != Response.SC_FORBIDDEN) {
                // See  http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
                getNext().invoke(request, response);
            }
        } catch (Exception e) {
            log.error("Could not handle request: " + request.getRequestURI(), e);
        }
    }

}

