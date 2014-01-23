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
 */
public class CarbonTomcatRealm extends RealmBase {

    private static Log log = LogFactory.getLog(CarbonTomcatRealm.class);

    /**
     * ThreadLocal variables to keep SaaS rule data of a webapp which is currently used.
     */
    private static ThreadLocal<HashMap> tenantSaaSRulesMap = new ThreadLocal<HashMap>();

    private static ThreadLocal<Boolean> isSaaSEnabled = new ThreadLocal<Boolean>();

    public boolean isSaaSEnabled() {
        return isSaaSEnabled.get();
    }

    public void setSaaSEnabled(boolean saaSEnabled) {
        isSaaSEnabled.set(saaSEnabled);
    }

    public CarbonTomcatRealm() throws Exception {
    }

    public void setSaaSRules(HashMap<String, TenantSaaSRules> tenantSaaSRulesMap) {
        CarbonTomcatRealm.tenantSaaSRulesMap.set(tenantSaaSRulesMap);
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
        String tenantDomain = null;
        tenantDomain = MultitenantUtils.getTenantDomain(userName);
        String tenantLessUserName;
        if (userName.lastIndexOf('@') > -1) {
            tenantLessUserName = userName.substring(0, userName.lastIndexOf('@'));
        } else if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            tenantLessUserName = userName;
            userName = userName + "@" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        } else {
            tenantLessUserName = userName;
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

            return getPrincipal(userName);
        } catch (UserStoreException e) {
            // not logging because already logged.
            throw new RuntimeException(e.getMessage(), e);
        }
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
        if(!isSaaSEnabled()){
            return false;
        }
        HashMap<String, TenantSaaSRules> tenantSaaSRulesMap = CarbonTomcatRealm.tenantSaaSRulesMap.get();
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
            } else if (roles != null && userRolesList != null) {
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
            super(name, null);
            tenantDomain = null;
            if (name.contains("@")) {
                tenantDomain = name.substring(name.indexOf('@') + 1);
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
