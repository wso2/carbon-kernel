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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.tomcat.ext.saas.TenantSaaSRules;

import java.util.Map;
import java.util.logging.Logger;

/**
 * CarbonTomcatRealmTest includes test scenarios for
 * [1] functions, getName (), getPassword () and authenticate () of CarbonTomcatRealm.
 * [2] properties, isSaaSEnabled of CarbonTomcatRealm.
 * @since 4.4.19
 */
public class CarbonTomcatRealmTest {

    private static final Logger log = Logger.getLogger("CarbonTomcatRealmTest");

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
     * Checks authenticate () with its expected behaviour of throwing an illegal state exception.
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
     * Checks for correct tenantSaaSRulesMap value with its setters and getters for Case 1.
     * Case 1 : SaasRules contain one tenant and its two users.
     * @throws Exception An exception is thrown according to CarbonTomcatRealm constructor definition.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.realms"})
    public void testTenantSaaSRulesMapWithCase1 () throws Exception {
        CarbonTomcatRealm carbonTomcatRealm = new CarbonTomcatRealm();
        log.info("Testing getters and setters for tenantSaaSRulesMap with users");
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
        log.info("Testing getters and setters for tenantSaaSRulesMap with roles");
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
        log.info("Testing getters and setters for tenantSaaSRulesMap with roles and users");
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
        log.info("Testing getters and setters for tenantSaaSRulesMap with no roles and users");
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
}
