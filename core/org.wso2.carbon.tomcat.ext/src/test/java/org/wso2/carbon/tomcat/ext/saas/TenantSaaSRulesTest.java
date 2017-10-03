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

package org.wso2.carbon.tomcat.ext.saas;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * TenantSaaSRulesTest includes test scenarios for
 * [1] function, isTenantRulesDefined () of TenantSaaSRules.
 * [2] properties, tenants, users and roles of TenantSaaSRules.
 * @since 4.4.19
 */
public class TenantSaaSRulesTest {

    private static final Logger log = Logger.getLogger("TenantSaaSRulesTest");

    @Test(groups = {"org.wso2.carbon.tomcat.ext.saas"},
            description = "Testing getters and setters for tenant.")
    public void testTenant () throws Exception {
        TenantSaaSRules tenantSaaSRules = new TenantSaaSRules();
        // calling set method
        tenantSaaSRules.setTenant("abc.com");
        // checking retrieved values
        log.info("Testing getters and setters for tenant");
        Assert.assertEquals(tenantSaaSRules.getTenant(), "abc.com",
                "Retrieved value did not match with set value for tenant");
    }

    @Test(groups = {"org.wso2.carbon.tomcat.ext.saas"},
            description = "Testing getters and setters for users.")
    public void testUsers () throws Exception {
        TenantSaaSRules tenantSaaSRules = new TenantSaaSRules();
        // setting input
        ArrayList<String> users = new ArrayList<>();
        users.add("bob");
        users.add("alice");
        // calling set method
        tenantSaaSRules.setUsers(users);
        // checking retrieved values
        log.info("Testing getters and setters for users");
        Assert.assertEquals(tenantSaaSRules.getUsers(), users,
                "Retrieved value did not match with set value for users");
    }

    @Test(groups = {"org.wso2.carbon.tomcat.ext.saas"},
            description = "Testing getters and setters for roles.")
    public void testRoles () throws Exception {
        TenantSaaSRules tenantSaaSRules = new TenantSaaSRules();
        // setting input
        ArrayList<String> roles = new ArrayList<>();
        roles.add("hr-manager");
        roles.add("sales-manager");
        // calling set method
        tenantSaaSRules.setRoles(roles);
        // checking retrieved values
        log.info("Testing getters and setters for roles");
        Assert.assertEquals(tenantSaaSRules.getRoles(), roles,
                "Retrieved value did not match with set value for roles");
    }

    /**
     * Checks isTenantRulesDefined with Case 1.
     * Case 1: No users or roles defined. Then the method should return false.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.saas"})
    public void testIsTenantRulesDefinedWithCase1 () {
        TenantSaaSRules tenantSaaSRules = new TenantSaaSRules();
        // checking isTenantRulesDefined with Case 1
        log.info("Testing isTenantRulesDefined () with Case 1");
        Assert.assertEquals(tenantSaaSRules.isTenantRulesDefined(), false,
                "Retrieved value did not match with expected value, false");
    }

    /**
     * Checks isTenantRulesDefined with Case 2.
     * Case 2: Users defined, but no roles defined. Then the method should return true.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.saas"})
    public void testIsTenantRulesDefinedWithCase2 () {
        TenantSaaSRules tenantSaaSRules = new TenantSaaSRules();
        // setting users
        ArrayList<String> users = new ArrayList<>();
        users.add("bob");
        users.add("alice");
        // calling set method
        tenantSaaSRules.setUsers(users);
        // checking isTenantRulesDefined with Case 2
        log.info("Testing isTenantRulesDefined () with Case 2");
        Assert.assertEquals(tenantSaaSRules.isTenantRulesDefined(), true,
                "Retrieved value did not match with expected value, true");
    }

    /**
     * Checks isTenantRulesDefined with Case 3.
     * Case 3: Users not defined, but roles defined. Then the method should return true.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.saas"})
    public void testIsTenantRulesDefinedWithCase3 () {
        TenantSaaSRules tenantSaaSRules = new TenantSaaSRules();
        // setting roles
        ArrayList<String> roles = new ArrayList<>();
        roles.add("hr-manager");
        roles.add("sales-manager");
        // calling set method
        tenantSaaSRules.setRoles(roles);
        // checking isTenantRulesDefined with Case 3
        log.info("Testing isTenantRulesDefined () with Case 3");
        Assert.assertEquals(tenantSaaSRules.isTenantRulesDefined(), true,
                "Retrieved value did not match with expected value, true");
    }

    /**
     * Checks isTenantRulesDefined with Case 4.
     * Case 4: Users defined, roles also defined. Then the method should return true.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.saas"})
    public void testIsTenantRulesDefinedWithCase4 () {
        TenantSaaSRules tenantSaaSRules = new TenantSaaSRules();
        // setting users
        ArrayList<String> users = new ArrayList<>();
        users.add("bob");
        users.add("alice");
        // calling set method
        tenantSaaSRules.setUsers(users);
        // setting roles
        ArrayList<String> roles = new ArrayList<>();
        roles.add("hr-manager");
        roles.add("sales-manager");
        // calling set method
        tenantSaaSRules.setRoles(roles);
        // checking isTenantRulesDefined with Case 4
        log.info("Testing isTenantRulesDefined () with Case 4");
        Assert.assertEquals(tenantSaaSRules.isTenantRulesDefined(), true,
                "Retrieved value did not match with expected value, true");
    }
}
