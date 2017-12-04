/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.tomcat.ext.realms;

import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.tomcat.ext.internal.CarbonRealmServiceHolder;
import org.wso2.carbon.tomcat.ext.saas.TenantSaaSRules;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * CarbonTomcatRealmTest includes test scenarios for
 * [1] functions, getName (), getPassword () and authenticate () of CarbonTomcatRealm.
 * [2] properties, isSaaSEnabled of CarbonTomcatRealm.
 * @since 4.4.19
 */
@PrepareForTest(MultitenantUtils.class)
public class CarbonTomcatRealmTest extends PowerMockTestCase {

    private static final Logger log = Logger.getLogger("CarbonTomcatRealmTest");

    /**
     * Configure TestNG to use the PowerMock object factory.
     * @return IObjectFactory
     */
    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"},
            description = "Testing getters and setters for isSaaSEnabled.")
    public void testEnableSaaS () throws Exception {
        CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
        log.info("Testing getters and setters for isSaaSEnabled");
        carbonTomcatRealm.setEnableSaaS(true);
        Assert.assertEquals(carbonTomcatRealm.getEnableSaaS(), true,
                "retrieved value did not match with set value");
        carbonTomcatRealm.setEnableSaaS(false);
        Assert.assertEquals(carbonTomcatRealm.getEnableSaaS(), false,
                "retrieved value did not match with set value");
    }

    /**
     * Checks getName() for its expected behaviour.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testGetName () throws Exception {
        CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
        log.info("Testing getters and setters for isSaaSEnabled");
        Assert.assertEquals(carbonTomcatRealm.getName(), "CarbonTomcatRealm",
                "retrieved name did not match 'CarbonTomcatRealm'");
    }

    /**
     * Checks getPassword () with its expected behaviour of throwing an illegal state exception.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"}, expectedExceptions = IllegalStateException.class)
    public void testGetPasswordForDefaultBehaviour () throws Exception {
        CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
        log.info("Testing getPassword () for its expected behaviour when called");
        carbonTomcatRealm.getPassword("bob");
    }

    /**
     * Checks for correct tenantSaaSRulesMap value with its setters and getters for Case 1.
     * Case 1 : SaasRules contain one tenant and its two users.
     * @throws Exception An exception is thrown according to CarbonTomcatRealm constructor definition.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testTenantSaaSRulesMapWithCase1 () throws Exception {
        CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
        log.info("Testing tenantSaaSRulesMap with case 1");
        carbonTomcatRealm.setSaasRules("carbon.super:users=admin,bob");
        Map saasRules = carbonTomcatRealm.getSaasRules();
        // Testing tenantSaaSRulesMap for correct number of tenants
        Assert.assertEquals(saasRules.size(), 1,
                "Received number of tenants does not match expected size");
        // Testing tenantSaaSRulesMap for correct number of tenant users
        Assert.assertEquals(((TenantSaaSRules)saasRules.get("carbon.super")).getUsers().size(), 2,
                "Received number of tenant users does not match expected size");
    }

    /**
     * Checks for correct tenantSaaSRulesMap value with its setters and getters for Case 2.
     * Case 2 : SaasRules contain two tenants and their roles.
     * @throws Exception An exception is thrown according to CarbonTomcatRealm constructor definition.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testTenantSaaSRulesMapWithCase2 () throws Exception {
        CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
        log.info("Testing tenantSaaSRulesMap with case 2");
        carbonTomcatRealm.setSaasRules("carbon.super:roles=admin;abc.com:roles=admin,manager,clerk");
        Map saasRules = carbonTomcatRealm.getSaasRules();
        // Testing tenantSaaSRulesMap for correct number of tenants
        Assert.assertEquals(saasRules.size(), 2,
                "Received number of tenants does not match expected size");
        // Testing tenantSaaSRulesMap for correct number of tenant users
        Assert.assertEquals(((TenantSaaSRules)saasRules.get("abc.com")).getRoles().size(), 3,
                "Received number of tenant users does not match expected size");
    }

    /**
     * Checks for correct tenantSaaSRulesMap value with its setters and getters for Case 3.
     * Case 3 : SaasRules contain two tenants, their roles and users.
     * @throws Exception An exception is thrown according to CarbonTomcatRealm constructor definition.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testTenantSaaSRulesMapWithCase3 () throws Exception {
        CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
        log.info("Testing tenantSaaSRulesMap with case 3");
        carbonTomcatRealm.setSaasRules("carbon.super:roles=admin;abc.com:users=admin,bob,smith");
        Map saasRules = carbonTomcatRealm.getSaasRules();
        // Testing tenantSaaSRulesMap for correct number of tenants
        Assert.assertEquals(saasRules.size(), 2,
                "Received number of tenants does not match expected size");
        // Testing tenantSaaSRulesMap for correct number of tenant roles
        Assert.assertEquals(((TenantSaaSRules)saasRules.get("carbon.super")).getRoles().size(), 1,
                "Received number of tenant roles does not match expected size");
        // Testing tenantSaaSRulesMap for correct number of tenant users
        Assert.assertEquals(((TenantSaaSRules)saasRules.get("abc.com")).getUsers().size(), 3,
                "Received number of tenant users does not match expected size");
    }

    /**
     * Checks for correct tenantSaaSRulesMap value with its setters and getters for Case 4.
     * Case 4 : SaasRules contain only tenants, but no roles or users.
     * @throws Exception An exception is thrown according to CarbonTomcatRealm constructor definition.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testTenantSaaSRulesMapWithCase4 () throws Exception {
        CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
        log.info("Testing tenantSaaSRulesMap with case 4");
        carbonTomcatRealm.setSaasRules("carbon.super;abc.com;xyz.com");
        Map saasRules = carbonTomcatRealm.getSaasRules();
        // Testing tenantSaaSRulesMap for correct number of tenants
        Assert.assertEquals(saasRules.size(), 3,
                "Received number of tenants does not match expected size");
        // Testing tenantSaaSRulesMap for correct number of tenant roles
        Assert.assertEquals(((TenantSaaSRules)saasRules.get("carbon.super")).getRoles(), null,
                "Received number of tenant roles does not match expected size");
        // Testing tenantSaaSRulesMap for correct number of tenant users
        Assert.assertEquals(((TenantSaaSRules)saasRules.get("abc.com")).getUsers(), null,
                "Received number of tenant users does not match expected size");
    }

    /**
     * Checks authenticate () with its expected behaviour
     * of throwing an illegal state exception.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"}, expectedExceptions = IllegalStateException.class)
    public void testAuthenticateForDefaultBehaviour () throws Exception {
        CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
        log.info("Testing authenticate () for its expected behaviour when called");
        carbonTomcatRealm.authenticate("username-string", "response-string", "nonce-string",
                "nc-string", "cNonce-string", "qop-string", "realmName-string",
                "md5-string");
    }

    /**
     * Check for expected output for authenticate () with Case 1
     * Case 1: When a username with an invalid tenant domain is provided, checking if method returns null.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testAuthenticateWithCase1 () throws Exception {
        CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
        RealmService userRealmService = mock(RealmService.class, Mockito.RETURNS_DEEP_STUBS);
        CarbonRealmServiceHolder.setRealmService(userRealmService);
        mockStatic(MultitenantUtils.class);
        // mocking input
        when(MultitenantUtils.getTenantDomain("bob@abc.com")).thenReturn("abc.com");
        when(userRealmService.getTenantManager().getTenantId("abc.com")).thenReturn(-1);
        log.info("Testing authenticate () method with case 1");
        Assert.assertEquals(carbonTomcatRealm.authenticate("bob@abc.com", "123456"), null,
                "When a username with an invalid tenant domain is provided, method did not return null, " +
                        "the expected output");
    }

    /**
     * Check for expected output for authenticate () with Case 2
     * Case 2: When a username with a valid tenant domain is provided and SAAS access is not enabled,
     * checking if method returns null for a request coming to a different tenant.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testAuthenticateWithCase2 () throws Exception {
        System.setProperty("carbon.home", "/home/dummy/wso2server");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
            // mocking input
            mockStatic(MultitenantUtils.class);
            when(MultitenantUtils.getTenantDomain("bob@abc.com")).thenReturn("abc.com");
            RealmService userRealmService = mock(RealmService.class, Mockito.RETURNS_DEEP_STUBS);
            CarbonRealmServiceHolder.setRealmService(userRealmService);
            when(userRealmService.getTenantManager().getTenantId("abc.com")).thenReturn(1);

            CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
            // set saas access to false by blacklisting abc.com in saas tenants
            carbonTomcatRealm.setEnableSaaS(true);
            carbonTomcatRealm.setSaasRules("carbon.super:users=admin;!abc.com:users=bob");
            String[] roles = new String[] {"admin", "manager"};
            when(userRealmService.getTenantUserRealm(1).getUserStoreManager()
                    .getRoleListOfUser("bob")).thenReturn(roles);

            log.info("Testing authenticate () method with case 2");
            Assert.assertEquals(carbonTomcatRealm.authenticate("bob@abc.com", "123456"), null,
                    "When a username with a valid tenant domain is provided and SAAS access is not enabled, " +
                            "for a request coming to a different tenant by this user, method did not return null, " +
                                "the expected output");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Check for expected output for authenticate () with Case 3
     * Case 3: When a saas blacklisted user with a valid tenant domain is provided,
     * checking if method returns null for a request coming to a different tenant.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testAuthenticateWithCase3 () throws Exception {
        System.setProperty("carbon.home", "/home/dummy/wso2server");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
            // mocking input
            mockStatic(MultitenantUtils.class);
            when(MultitenantUtils.getTenantDomain("bob@abc.com")).thenReturn("abc.com");
            RealmService userRealmService = mock(RealmService.class, Mockito.RETURNS_DEEP_STUBS);
            CarbonRealmServiceHolder.setRealmService(userRealmService);
            when(userRealmService.getTenantManager().getTenantId("abc.com")).thenReturn(1);

            CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
            // set saas access to false by blacklisting bob under saas tenant rules
            carbonTomcatRealm.setEnableSaaS(true);
            carbonTomcatRealm.setSaasRules("carbon.super:users=admin;abc.com:users=!bob");
            String[] roles = new String[] {"admin", "manager"};
            when(userRealmService.getTenantUserRealm(1).getUserStoreManager()
                    .getRoleListOfUser("bob")).thenReturn(roles);

            log.info("Testing authenticate () method with case 3");
            Assert.assertEquals(carbonTomcatRealm.authenticate("bob@abc.com", "123456"), null,
                    "When a saas blacklisted user with a valid tenant domain is provided and SAAS access is not " +
                            "enabled, for a request coming to a different tenant by this user, method did not " +
                                "return null, the expected output");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Check for expected output for authenticate () with Case 4
     * Case 4: When a user with saas blacklisted role of a valid tenant domain is provided,
     * checking if method returns null for a request coming to a different tenant.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testAuthenticateWithCase4 () throws Exception {
        System.setProperty("carbon.home", "/home/dummy/wso2server");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
            // mocking input
            mockStatic(MultitenantUtils.class);
            when(MultitenantUtils.getTenantDomain("bob@abc.com")).thenReturn("abc.com");
            RealmService userRealmService = mock(RealmService.class, Mockito.RETURNS_DEEP_STUBS);
            CarbonRealmServiceHolder.setRealmService(userRealmService);
            when(userRealmService.getTenantManager().getTenantId("abc.com")).thenReturn(1);

            CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
            // set saas access to false by blacklisting bob under saas tenant rules
            carbonTomcatRealm.setEnableSaaS(true);
            carbonTomcatRealm.setSaasRules("carbon.super:roles=admin;abc.com:roles=!manager");
            String[] roles = new String[] {"manager"};
            when(userRealmService.getTenantUserRealm(1).getUserStoreManager()
                    .getRoleListOfUser("bob")).thenReturn(roles);

            log.info("Testing authenticate () method with case 4");
            Assert.assertEquals(carbonTomcatRealm.authenticate("bob@abc.com", "123456"), null,
                    "When a user with saas blacklisted role of a valid tenant domain is provided and " +
                            "SAAS access is not enabled, for a request coming to a different tenant by this user, " +
                                "method did not return null, the expected output");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Check for expected output for authenticate () with Case 5
     * Case 5: When a username with a valid tenant domain is provided and SAAS access is not enabled,
     * checking if method returns null for a request coming to the same tenant with invalid credentials.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testAuthenticateWithCase5 () throws Exception {
        System.setProperty("carbon.home", "/home/dummy/wso2server");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
            // mocking input
            mockStatic(MultitenantUtils.class);
            when(MultitenantUtils.getTenantDomain("bob")).thenReturn("carbon.super");
            RealmService userRealmService = mock(RealmService.class, Mockito.RETURNS_DEEP_STUBS);
            CarbonRealmServiceHolder.setRealmService(userRealmService);
            when(userRealmService.getTenantManager().getTenantId("carbon.super")).thenReturn(-1234);
            when(userRealmService.getTenantUserRealm(-1234).getUserStoreManager()
                    .authenticate("bob", "123456")).thenReturn(false);
            CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
            carbonTomcatRealm.setEnableSaaS(false);
            log.info("Testing authenticate () method with case 5");
            Assert.assertEquals(carbonTomcatRealm.authenticate("bob", "123456"), null,
                    "When a username with a valid tenant domain is provided and SAAS access is not enabled, " +
                            "for a request coming to the same tenant with invalid credentials, method did not " +
                                "return null, the expected output");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Check for expected output for authenticate () with Case 6
     * Case 6: When a username with a valid tenant domain is provided and SAAS access is not enabled,
     * checking if method returns success for a request coming to the same tenant with valid credentials.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testAuthenticateWithCase6 () throws Exception {
        System.setProperty("carbon.home", "/home/dummy/wso2server");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
            // mocking input
            mockStatic(MultitenantUtils.class);
            when(MultitenantUtils.getTenantDomain("bob")).thenReturn("carbon.super");
            RealmService userRealmService = mock(RealmService.class, Mockito.RETURNS_DEEP_STUBS);
            CarbonRealmServiceHolder.setRealmService(userRealmService);
            when(userRealmService.getTenantManager().getTenantId("carbon.super")).thenReturn(-1234);
            when(userRealmService.getTenantUserRealm(-1234).getUserStoreManager()
                    .authenticate("bob", "123456")).thenReturn(true);
            String[] roles = new String[] {"admin", "manager"};
            when(userRealmService.getTenantUserRealm(-1234).getUserStoreManager()
                    .getRoleListOfUser("bob")).thenReturn(roles);

            CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
            carbonTomcatRealm.setEnableSaaS(false);
            // calling authenticate method
            carbonTomcatRealm.authenticate("bob", "123456");
            log.info("Testing authenticate () method with case 6");
            Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername(), "bob",
                    "When a username with a valid tenant domain is provided and SAAS access is not enabled, " +
                            "for a request coming to the same tenant with invalid credentials, method did not " +
                                "return null, the expected output");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Check for expected output for authenticate () with Case 7
     * Case 7: When a saas whitelisted user with a valid tenant domain is provided and SAAS access is enabled,
     * checking if method returns null for a request coming to the same tenant with invalid credentials.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testAuthenticateWithCase7 () throws Exception {
        System.setProperty("carbon.home", "/home/dummy/wso2server");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
            // mocking input
            mockStatic(MultitenantUtils.class);
            when(MultitenantUtils.getTenantDomain("bob")).thenReturn("carbon.super");
            RealmService userRealmService = mock(RealmService.class, Mockito.RETURNS_DEEP_STUBS);
            CarbonRealmServiceHolder.setRealmService(userRealmService);
            when(userRealmService.getTenantManager().getTenantId("carbon.super")).thenReturn(-1234);

            CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
            // set saas access to false by blacklisting bob under saas tenant rules
            carbonTomcatRealm.setEnableSaaS(true);
            carbonTomcatRealm.setSaasRules("carbon.super:users=bob;abc.com:users=!peter");
            String[] roles = new String[] {"admin"};
            when(userRealmService.getTenantUserRealm(-1234).getUserStoreManager()
                    .getRoleListOfUser("bob")).thenReturn(roles);

            when(userRealmService.getTenantUserRealm(-1234).getUserStoreManager()
                    .authenticate("bob", "123456")).thenReturn(false);

            // calling authenticate method
            carbonTomcatRealm.authenticate("bob", "123456");
            log.info("Testing authenticate () method with case 7");
            Assert.assertEquals(carbonTomcatRealm.authenticate("bob", "123456"), null,
                    "When a username with a valid tenant domain is provided and SAAS access is enabled, " +
                            "checking if method returns null for a request coming to the same tenant " +
                                "with invalid credentials");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Check for expected output for authenticate () with Case 8
     * Case 8: When a user with saas whitelisted role for a valid tenant domain is provided and
     * SAAS access is enabled, checking if method returns null for a request coming to
     * the same tenant with invalid credentials.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testAuthenticateWithCase8 () throws Exception {
        System.setProperty("carbon.home", "/home/dummy/wso2server");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
            // mocking input
            mockStatic(MultitenantUtils.class);
            when(MultitenantUtils.getTenantDomain("bob")).thenReturn("carbon.super");
            RealmService userRealmService = mock(RealmService.class, Mockito.RETURNS_DEEP_STUBS);
            CarbonRealmServiceHolder.setRealmService(userRealmService);
            when(userRealmService.getTenantManager().getTenantId("carbon.super")).thenReturn(-1234);

            CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
            // set saas access to false by blacklisting bob under saas tenant rules
            carbonTomcatRealm.setEnableSaaS(true);
            carbonTomcatRealm.setSaasRules("carbon.super:roles=admin;abc.com:roles=!manager");
            String[] roles = new String[] {"admin"};
            when(userRealmService.getTenantUserRealm(-1234).getUserStoreManager()
                    .getRoleListOfUser("bob")).thenReturn(roles);

            when(userRealmService.getTenantUserRealm(-1234).getUserStoreManager()
                    .authenticate("bob", "123456")).thenReturn(false);

            // calling authenticate method
            carbonTomcatRealm.authenticate("bob", "123456");
            log.info("Testing authenticate () method with case 8");
            Assert.assertEquals(carbonTomcatRealm.authenticate("bob", "123456"), null,
                    "When a user with saas whitelisted role for a valid tenant domain is provided and " +
                            "SAAS access is enabled, checking if method returns null for a request coming to the " +
                                "same tenant with invalid credentials");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Check for expected output for authenticate () with Case 9
     * Case 9: When a user of a valid tenant domain with any (*) saas rule for roles is provided and
     * SAAS access is enabled, checking if method returns null for a request coming to
     * a different tenant with invalid credentials.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testAuthenticateWithCase9 () throws Exception {
        System.setProperty("carbon.home", "/home/dummy/wso2server");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
            // mocking input
            mockStatic(MultitenantUtils.class);
            when(MultitenantUtils.getTenantDomain("bob@abc.com")).thenReturn("abc.com");
            RealmService userRealmService = mock(RealmService.class, Mockito.RETURNS_DEEP_STUBS);
            CarbonRealmServiceHolder.setRealmService(userRealmService);
            when(userRealmService.getTenantManager().getTenantId("abc.com")).thenReturn(1);

            CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
            // set saas access to false by blacklisting bob under saas tenant rules
            carbonTomcatRealm.setEnableSaaS(true);
            carbonTomcatRealm.setSaasRules("carbon.super:roles=admin;abc.com:roles=*");
            String[] roles = new String[] {"manager"};
            when(userRealmService.getTenantUserRealm(1).getUserStoreManager()
                    .getRoleListOfUser("bob")).thenReturn(roles);

            when(userRealmService.getTenantUserRealm(1).getUserStoreManager()
                    .authenticate("bob", "123456")).thenReturn(false);

            log.info("Testing authenticate () method with case 9");
            Assert.assertEquals(carbonTomcatRealm.authenticate("bob@abc.com", "123456"), null,
                    "When a user of a valid tenant domain with any (*) saas rule for roles is provided and " +
                            "SAAS access is enabled, checking if method returns null for a request coming to " +
                                "a different tenant with invalid credentials");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Check for expected output for authenticate () with Case 10
     * Case 10: When a user for a valid tenant domain with any (*) saas rule for users is provided and
     * SAAS access is enabled, checking if method returns null for a request coming to
     * a different tenant with invalid credentials.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testAuthenticateWithCase10 () throws Exception {
        System.setProperty("carbon.home", "/home/dummy/wso2server");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
            // mocking input
            mockStatic(MultitenantUtils.class);
            when(MultitenantUtils.getTenantDomain("bob@abc.com")).thenReturn("abc.com");
            RealmService userRealmService = mock(RealmService.class, Mockito.RETURNS_DEEP_STUBS);
            CarbonRealmServiceHolder.setRealmService(userRealmService);
            when(userRealmService.getTenantManager().getTenantId("abc.com")).thenReturn(1);

            CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
            // set saas access to false by blacklisting bob under saas tenant rules
            carbonTomcatRealm.setEnableSaaS(true);
            carbonTomcatRealm.setSaasRules("carbon.super:users=admin;abc.com:users=*");
            String[] roles = new String[] {"manager"};
            when(userRealmService.getTenantUserRealm(1).getUserStoreManager()
                    .getRoleListOfUser("bob")).thenReturn(roles);

            when(userRealmService.getTenantUserRealm(1).getUserStoreManager()
                    .authenticate("bob", "123456")).thenReturn(false);

            log.info("Testing authenticate () method with case 10");
            Assert.assertEquals(carbonTomcatRealm.authenticate("bob@abc.com", "123456"), null,
                    "When a user for a valid tenant domain with any (*) saas rule for users is provided and " +
                            "SAAS access is enabled, checking if method returns null for a request coming to " +
                                "a different tenant with invalid credentials");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Check for expected output for authenticate () with Case 11
     * Case 11: An internal execution logic throws UserStoreException.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"}, expectedExceptions = RuntimeException.class)
    public void testAuthenticateWithCase11 () throws Exception {
        System.setProperty("carbon.home", "/home/dummy/wso2server");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
            // mocking input
            mockStatic(MultitenantUtils.class);
            when(MultitenantUtils.getTenantDomain("bob@abc.com")).thenReturn("abc.com");
            RealmService userRealmService = mock(RealmService.class, Mockito.RETURNS_DEEP_STUBS);
            CarbonRealmServiceHolder.setRealmService(userRealmService);
            when(userRealmService.getTenantManager().getTenantId("abc.com")).thenReturn(1);

            CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
            // set saas access to false by blacklisting bob under saas tenant rules
            carbonTomcatRealm.setEnableSaaS(true);
            carbonTomcatRealm.setSaasRules("carbon.super:users=admin;abc.com:users=*");

            // mocking UserStoreException
            when(userRealmService.getTenantUserRealm(1).getUserStoreManager()
                    .getRoleListOfUser("bob")).thenThrow(UserStoreException.class);

            when(userRealmService.getTenantUserRealm(1).getUserStoreManager()
                    .authenticate("bob", "123456")).thenReturn(false);

            log.info("Testing authenticate () method with case 11");
            carbonTomcatRealm.authenticate("bob@abc.com", "123456");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
