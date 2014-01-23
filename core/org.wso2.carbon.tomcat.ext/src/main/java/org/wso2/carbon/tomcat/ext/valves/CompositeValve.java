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
import org.wso2.carbon.tomcat.ext.saas.TenantSaaSRules;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserRealmService;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
    public static final String ENABLE_SAAS = "carbon.enable.saas";

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        try {

            /**
             * To enable SaaS for webapp, add the following to the web.xml file
             *
             * 1. All tenants can access this app
             * <context-param>
             * <param-name>carbon.saas.tenants</param-name>
             * <param-value>*</param-value>
             * </context-param>
             *
             * 2. All tenants except foo.com & bar.com can access this app
             *
             * <context-param>
             * <param-name>carbon.saas.tenants</param-name>
             * <param-value>*;!foo.com;!bar.com</param-value>
             * </context-param>
             *
             * 3. Only foo.com & bar.com (all users) can access this app
             *
             * <context-param>
             * <param-name>carbon.saas.tenants</param-name>
             * <param-value>foo.com;bar.com</param-value>
             * </context-param>
             *
             * 4. Only users azeez & admin in tenant foo.com & all users in tenant bar.com can access this app
             *
             * <context-param>
             * <param-name>carbon.saas.tenants</param-name>
             * <param-value>foo.com:users=azeez,admin;bar.com</param-value>
             * </context-param>
             *
             * 5. Only user admin in tenant foo.com can access this app and bob from tenant foo.com can't access the app.
             *    All users in bar.com can access the app except bob.
             *
             * <context-param>
             * <param-name>carbon.saas.tenants</param-name>
             * <param-value>foo.com:users=!azeez,admin;bar.com:users=*,!bob</param-value>
             * </context-param>
             *
             * * 6. Only users azeez,bob in tenant foo.com can access this app. Also users who belongs to role devops in
             *    tenant foo.com also can access the app and users who belongs to role developers in tenant foo.com can't
             *    access the app. All users belong all roles in bar.com can access the app except users belongs to devops.
             *
             * <context-param>
             * <param-name>carbon.saas.tenants</param-name>
             * <param-value>foo.com:roles=!developers,devops:users=azeez,bob;bar.com:roles=*,!devops</param-value>
             * </context-param>
             *
             * Note: Denial rules will take precedence.
             */
            String enableSaaSParam =
                    request.getContext().findParameter(ENABLE_SAAS);
            Realm realm = request.getContext().getRealm();
            if (enableSaaSParam != null) {
                // Set the SaaS enabled ThreadLocal variable
                if (realm instanceof CarbonTomcatRealm) {
                    // replaceAll("\\s","") is to remove all whitespaces
                    String[] enableSaaSParams = enableSaaSParam.replaceAll("\\s", "").split(";");
                    //Store SaaS rules for tenants
                    HashMap<String, TenantSaaSRules> tenantSaaSRulesMap = new HashMap<String, TenantSaaSRules>();

                    for (String saaSParam : enableSaaSParams) {
                        String[] saaSSubParams = saaSParam.split(":");
                        String tenant = saaSSubParams[0];
                        TenantSaaSRules tenantSaaSRules = new TenantSaaSRules();
                        ArrayList<String> users = null;
                        ArrayList<String> roles = null;
                        if (saaSSubParams.length > 1) {
                            tenantSaaSRules.setTenant(tenant);
                            //This will include users or roles
                            for (int i = 1; i < saaSSubParams.length; i++) {
                                String[] saaSTypes = saaSSubParams[i].split("=");
                                if ("users".equals(saaSTypes[0]) && saaSTypes.length == 2) {
                                    users = new ArrayList<String>();
                                    users.addAll(Arrays.asList(saaSTypes[1].split(",")));
                                } else if ("roles".equals(saaSTypes[0]) && saaSTypes.length == 2) {
                                    roles = new ArrayList<String>();
                                    roles.addAll(Arrays.asList(saaSTypes[1].split(",")));
                                }
                            }
                        }
                        if (users != null) {
                            tenantSaaSRules.setUsers(users);
                        }
                        if (roles != null) {
                            tenantSaaSRules.setRoles(roles);
                        }
                        tenantSaaSRulesMap.put(tenant, tenantSaaSRules);
                    }
                    ((CarbonTomcatRealm) realm).setSaaSRules(tenantSaaSRulesMap);
                    ((CarbonTomcatRealm) realm).setSaaSEnabled(Boolean.TRUE);
                }
            } else {
                if (realm instanceof CarbonTomcatRealm) {
                    ((CarbonTomcatRealm) realm).setSaaSEnabled(Boolean.FALSE);
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

