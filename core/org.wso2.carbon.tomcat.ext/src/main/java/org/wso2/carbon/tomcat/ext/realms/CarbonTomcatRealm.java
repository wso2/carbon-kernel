/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.tomcat.ext.realms;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.tomcat.ext.internal.CarbonRealmServiceHolder;
import org.wso2.carbon.tomcat.ext.saas.TenantSaaSRules;
import org.wso2.carbon.user.api.UserRealmService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a custom Tomcat realm that uses Carbon realm inside.
 * <p/>
 * It is outside the OSGi container so creates a RealmService and
 * RegistryService separately.
 * <p/>
 * Registry is needed because it is where we store the user-mgt.xml of each
 * tenant.
 * <p/>
 * A classic demonstration of Adaptor Pattern.
 *
 * <p>
 * To enable SaaS for a webapp, add the following to the META-INF/context.xml file
 *
 * {@code
 * <Realm className="org.wso2.carbon.tomcat.ext.realms.CarbonTomcatRealm"
 * isSaaSEnabled="true" saasRules="*"  /> }
 *
 * 1. All tenants can access this app
 * {@code
 * <Realm className="org.wso2.carbon.tomcat.ext.realms.CarbonTomcatRealm"
 * isSaaSEnabled="true" saasRules="*"  /> }
 *
 * 2. All tenants except foo.com & bar.com can access this app
 * {@code
 * <Realm className="org.wso2.carbon.tomcat.ext.realms.CarbonTomcatRealm"
 * isSaaSEnabled="true" saasRules="*;!foo.com;!bar.com"  /> }
 *
 * 3. Only foo.com & bar.com (all users) can access this app
 * {@code
 * <Realm className="org.wso2.carbon.tomcat.ext.realms.CarbonTomcatRealm"
 * isSaaSEnabled="true" saasRules="foo.com;bar.com"  /> }
 *
 * 4. Only users bob & admin in tenant foo.com & all users in tenant bar.com can access this app
 * {@code
 * <Realm className="org.wso2.carbon.tomcat.ext.realms.CarbonTomcatRealm"
 * isSaaSEnabled="true" saasRules="foo.com:users=bob,admin;bar.com"  /> }
 *
 * 5. Only user admin in tenant foo.com can access this app and bob from tenant foo.com can't access the app.
 *    All users in bar.com can access the app except bob.
 * {@code
 * <Realm className="org.wso2.carbon.tomcat.ext.realms.CarbonTomcatRealm"
 * isSaaSEnabled="true" saasRules="foo.com:users=!bob,admin;bar.com:users=*,!bob"  /> }
 *
 * * 6. Only users alice,bob in tenant foo.com can access this app. Also users who belongs to role devops in
 *    tenant foo.com also can access the app and users who belongs to role developers in tenant foo.com can't
 *    access the app. All users belong all roles in bar.com can access the app except users belongs to devops.
 * {@code
 * <Realm className="org.wso2.carbon.tomcat.ext.realms.CarbonTomcatRealm"
 * isSaaSEnabled="true" saasRules="foo.com:roles=!developers,devops:users=alice,bob;bar.com:roles=*,!devops"  /> }
 *
 * Note: Denial rules will take precedence.
 * </p>
 */
public class CarbonTomcatRealm extends RealmBase {

    private static Log log = LogFactory.getLog(CarbonTomcatRealm.class);

    /**
     * variable to keep SaaS rule data of a webapp which is currently used.
     */
    private Map<String, TenantSaaSRules> tenantSaaSRulesMap = null;

    private boolean isSaaSEnabled = false;

    public boolean getEnableSaaS() {
        return isSaaSEnabled;
    }

    public void setEnableSaaS(boolean enableSaaS) {
        isSaaSEnabled = enableSaaS;
    }

    public CarbonTomcatRealm() throws Exception {
    }

    public Map getSaasRules() {
        return tenantSaaSRulesMap;
    }

    public void setSaasRules(String saaSRules) {
        tenantSaaSRulesMap = getProcessedSaaSRules(saaSRules);
    }

    protected String getName() {
        return getClass().getSimpleName();
    }

    protected String getPassword(String username) {
        throw new IllegalStateException("When CarbonTomcatRealm is in operation " +
                                        "this method getPassword(String) should never be called");
    }

    public Principal authenticate(String username, String response, String nonce, String nc,
                                  String cNonce, String qop, String realmName, String md5) {
        // Carbon has SHA-256 but Digested Authentication is MD5
        throw new IllegalStateException("Carbon doesn't use MD5 hashes. Can't do " +
                                        "digest authentication");
    }

    public Principal authenticate(String userName, String credential) {
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        String tenantLessUserName = getTenantLessUserName(userName);

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            userName = userName + "@" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        try {

            UserRealmService userRealmService = CarbonRealmServiceHolder.getRealmService();
            int tenantId = userRealmService.getTenantManager().getTenantId(tenantDomain);
            if(tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                return null;
            }
            String[] roles = userRealmService.getTenantUserRealm(tenantId).getUserStoreManager().getRoleListOfUser(tenantLessUserName);

            // If SaaS is not enabled, do not allow users from other tenants to call this secured webapp
            if (!checkSaasAccess(tenantDomain, tenantLessUserName, roles)) {
                String requestTenantDomain =
                        CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                if (tenantDomain != null &&
                    !tenantDomain.equals(requestTenantDomain)) {
                    if (requestTenantDomain.trim().length() == 0) {
                        requestTenantDomain = "0";
                    }
                    log.warn("Illegal access attempt by " + userName +
                             " to secured resource hosted by tenant " + requestTenantDomain);
                    return null;
                }
            }

            if (!userRealmService.getTenantUserRealm(tenantId).getUserStoreManager().
                    authenticate(tenantLessUserName, credential)) {
                return null;
            }

            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setUsername(tenantLessUserName);

            return getPrincipal(userName);
        } catch (UserStoreException e) {
            // not logging because already logged.
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static String getTenantLessUserName(String userName) {
        String tenantLessUserName;
        if (userName.lastIndexOf('@') > -1) {
            tenantLessUserName = userName.substring(0, userName.lastIndexOf('@'));
        } else {
            tenantLessUserName = userName;
        }

        return tenantLessUserName;
    }

    /**
     *
     * @param saaSRules saas rules string
     * @return processed saas rules as a map. key contains the list of tenants allowed to access the webapp
     */
    private Map<String, TenantSaaSRules> getProcessedSaaSRules(String saaSRules) {
        // replaceAll("\\s","") is to remove all whitespaces
        String[] enableSaaSParams = saaSRules.replaceAll("\\s", "").split(";");
        //Store SaaS rules for tenants
        Map<String, TenantSaaSRules> tenantSaaSRulesMap = new HashMap<String, TenantSaaSRules>();

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

        return tenantSaaSRulesMap;
    }

    /**
     * Check if saas mode enabled and access granted for the given tenant
     * Denial Rules are given precedency.
     *
     * @param tenantDomain - tenant
     * @param userName     - name of the user(without tenant part)
     * @param userRoles    - user roles of the tenant
     * @return false if saas mode denied.
     */
    private boolean checkSaasAccess(String tenantDomain, String userName, String[] userRoles) {
        if(!isSaaSEnabled){
            return false;
        }

        Set saaSTenants = tenantSaaSRulesMap.keySet();
        List<String> userRolesList = Arrays.asList(userRoles);
        boolean isUserAccepted = false;
        boolean isRoleAccepted = false;
        boolean isTenantAccepted = false;

        if (userName == null || tenantDomain == null) {
            return false;
        }

        if (saaSTenants.contains("!".concat(tenantDomain))) {
            return false;
        } else if (saaSTenants.contains(tenantDomain)) {
            TenantSaaSRules tenantSaaSRules = tenantSaaSRulesMap.get(tenantDomain);
            ArrayList<String> users = tenantSaaSRules.getUsers();
            ArrayList<String> roles = tenantSaaSRules.getRoles();
            if (users != null && users.contains("!".concat(userName))) {
                return false;
            } else if (roles != null) {
                boolean contains = false;
                for (String userRole : userRolesList) {
                    if (roles.contains("!".concat(userRole))) {
                        return false;
                    } else if (roles.contains(userRole)) {
                        contains = true;
                    }
                }
                if (contains || roles.contains("*")) {
                    isRoleAccepted = true;
                }
            } else if (users != null && (users.contains(userName) || users.contains("*"))) {
                isUserAccepted = true;
            }
        } else if (saaSTenants.contains(tenantDomain) && !tenantSaaSRulesMap.get(tenantDomain).isTenantRulesDefined() ||
                   saaSTenants.contains("*")) {
            isTenantAccepted = true;
        }

        return (isUserAccepted || isTenantAccepted || isRoleAccepted);
    }

    protected Principal getPrincipal(String userNameWithTenant) {
        return new GenericCarbonPrincipal(userNameWithTenant);
    }

    /**
     * Carbon java.security.Principal implementation
     * 
     * @see java.security.Principal
     * @see org.apache.catalina.realm.GenericPrincipal
     */
    private static class GenericCarbonPrincipal extends GenericPrincipal {
        private String tenantDomain = null;

        public GenericCarbonPrincipal(String name) {
            super(name, null, getCarbonRoles(name));
            tenantDomain = null;
            if (name.contains("@")) {
                tenantDomain = name.substring(name.indexOf('@') + 1);
            }
        }

        private static List<String> getCarbonRoles(String userName) {
            try {

                String tenantDomain = null;
                if (userName.contains("@")) {
                    tenantDomain = userName.substring(userName.indexOf('@') + 1);
                }

                UserRealmService userRealmService = CarbonRealmServiceHolder.getRealmService();
                int tenantId = userRealmService.getTenantManager().getTenantId(tenantDomain);
                String tenantLessUserName = getTenantLessUserName(userName);
                String[] roles = userRealmService.getTenantUserRealm(tenantId).
                        getUserStoreManager().getRoleListOfUser(tenantLessUserName);

                return Arrays.asList(roles);
            } catch (UserStoreException e) {
                log.error("Error occurred while retrieving the roles of the user - " + userName, e);
                return null;
            }

        }

        // Carbon realm does not give the password out

        public String getPassword() {
            throw new IllegalStateException("When CarbonTomcatRealm is in operation " +
                                            "this method Principal.getPassword() should never be called");
        }

        public boolean hasRole(String role) {
            try {
                UserRealmService realmService = CarbonRealmServiceHolder.getRealmService();
                int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                int indexOfAt = name.lastIndexOf('@');
                String tenantLessUserName = (indexOfAt == -1) ? name : name.substring(0, indexOfAt);
                String[] roles = 
                                 CarbonRealmServiceHolder.getRealmService().
                                                           getTenantUserRealm(tenantId).
                                                           getUserStoreManager().
                                                           getRoleListOfUser(tenantLessUserName);
                Arrays.sort(roles);
                return Arrays.binarySearch(roles, role) > -1;
            } catch (UserStoreException e) {
                log.error("Cannot check role", e);
            }
            return false;
        }
    }
}
