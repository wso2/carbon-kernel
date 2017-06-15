/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.test.multitenant;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.BeforeClass;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.registry.core.test.utils.MultiTenantTestClaimUtil;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.common.DefaultRealmService;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.tenant.JDBCTenantManager;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MultiTenantTest extends BaseTestCase {

    protected static EmbeddedRegistryService embeddedRegistryService = null;
    private static String TEST_URL = "jdbc:h2:./target/Tenanttest/TENANT_TEST";
    public static final String DB_DRIVER = "org.h2.Driver";
    public static final String JDBC_TEST_USERMGT_XML = "user-mgt-test.xml";
    DefaultRealm realm = new DefaultRealm();

    @BeforeClass
    public void setUp() {
        super.setUp();
        if (embeddedRegistryService != null) {
            return;
        }
        try {
            embeddedRegistryService = ctx.getEmbeddedRegistryService();
            RealmUnawareRegistryCoreServiceComponent comp =
                    new RealmUnawareRegistryCoreServiceComponent();
            comp.registerBuiltInHandlers(embeddedRegistryService);

        } catch (RegistryException e) {
            fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
        try {
            this.addTenants();
        } catch (Exception e) {
            fail("Failed to add tenants. Caused by: " + e.getMessage());
        }
    }

    // Test each registry has different virtual roots
    public void testVirtualRoots() throws Exception {

        RealmConfiguration realmConfig = ctx.getRealmService().getBootstrapRealmConfiguration();
        UserRegistry registry1 =
                embeddedRegistryService.getUserRegistry(realmConfig.getAdminUserName(), 
                		MultitenantConstants.SUPER_TENANT_ID);
        Resource r = registry1.newResource();
        registry1.put("/test", r);

        r = registry1.get("/");
        r.addProperty("name", "value");
        registry1.put("/", r);

        UserRegistry registry2 =
                embeddedRegistryService.getUserRegistry(realmConfig.getAdminUserName(), 1);
        r = registry2.get("/");
        Properties p = r.getProperties();
        assertEquals("The properties in the second registry should be 0", p.size(), 0);

        boolean notExist = false;
        try {
            registry2.get("/test");
        } catch (ResourceNotFoundException e) {
            notExist = true;
        }
        assertTrue("The /test should be null in the second registry", notExist);

        UserRealm userRealm1 = registry1.getUserRealm();
        try {
            userRealm1.getUserStoreManager().addUser("don1", "password", null, null, null);
        } catch (UserStoreException e) {
            throw new RegistryException("Error in adding a user", e);
        }

        Registry registry3 = embeddedRegistryService.getUserRegistry("don1", "password", 
        		MultitenantConstants.SUPER_TENANT_ID);
        r = registry3.get("/");
        assertEquals("The property name should be value", r.getProperty("name"), "value");

        String[] children = (String[]) r.getContent();
        assertEquals("child should be /test", children[0], "/test");


    }


    // Test each registry has different has different user stores
    public void testUserStores() throws RegistryException {
        RealmConfiguration realmConfig = OSGiDataHolder.getInstance().getUserRealmService().getBootstrapRealmConfiguration();
        // first we will fill the user store for tenant 0
        UserRegistry registry1 =
                embeddedRegistryService.getUserRegistry(realmConfig.getAdminUserName(), 
                		MultitenantConstants.SUPER_TENANT_ID);

        Resource r = registry1.newResource();
        registry1.put("/test2", r);

        UserRealm userRealm1 = registry1.getUserRealm();
        UserStoreManager userStoreManager1;
        try {
            userStoreManager1 = userRealm1.getUserStoreManager();
        } catch (UserStoreException e) {
            throw new RegistryException("Error in retrieving UserStoreManager.");
        }
        AuthorizationManager authorizationManager1;
        try {
            authorizationManager1 = userRealm1.getAuthorizationManager();
        } catch (UserStoreException e) {
            throw new RegistryException("Error in retrieving AuthorizationManager.");
        }

        // adding some users
        try {
            userStoreManager1.addUser("user1t0", "password", null, null, null);
        } catch (UserStoreException e) {
            throw new RegistryException("Error in adding user user1t0.");
        }
        try {
            userStoreManager1.addUser("user2t0", "password", null, null, null);
        } catch (UserStoreException e) {
            throw new RegistryException("Error in adding user user2t0.");
        }
        try {
            userStoreManager1.addUser("user3t0", "password", null, null, null);
        } catch (UserStoreException e) {
            throw new RegistryException("Error in adding user user3t0.");
        }

        // adding more users we are going to add roles with
        try {
            userStoreManager1.addUser("user4t0", "password", null, null, null);
        } catch (UserStoreException e) {
            throw new RegistryException("Error in adding user user4t0.");
        }
        try {
            userStoreManager1.addUser("user5t0", "password", null, null, null);
        } catch (UserStoreException e) {
            throw new RegistryException("Error in adding user user5t0.");
        }
        try {
            userStoreManager1.addUser("user6t0", "password", null, null, null);
        } catch (UserStoreException e) {
            throw new RegistryException("Error in adding user user6t0.");
        }

        // adding some roles
        try {
            userStoreManager1.addRole("role1t0", null, null);
        } catch (UserStoreException e) {
            throw new RegistryException("Error in adding role role1t0.");
        }
        try {
            userStoreManager1.addRole("role2t0", null, null);
        } catch (UserStoreException e) {
            throw new RegistryException("Error in adding role role2t0.");
        }
        try {
            userStoreManager1.addRole("role3t0", null, null);
        } catch (UserStoreException e) {
            throw new RegistryException("Error in adding role role3t0.");
        }
//
//        // now assign authorizations to first set of users.
//        try {
//            authorizationManager1.authorizeUser("user1t0", "/test2", ActionConstants.PUT);
//        } catch (UserStoreException e) {
//            throw new RegistryException("Error in authorizing user1t0.");
//        }
//        try {
//            authorizationManager1.authorizeUser("user2t0", "/test2", ActionConstants.DELETE);
//        } catch (UserStoreException e) {
//            throw new RegistryException("Error in authorizing user2t0.");
//        }
//        try {
//            authorizationManager1.authorizeUser("user2t0", "/test2", "authorize");
//        } catch (UserStoreException e) {
//            throw new RegistryException("Error in authorizing user3t0.");
//        }

        // then assign roles to the second set of users.
        try {
            userStoreManager1.updateRoleListOfUser("user4t0", null, new String[]{"role1t0"});
        } catch (UserStoreException e) {
            throw new RegistryException("Error in adding user user4t0 to role1t0 role.");
        }
        try {
            userStoreManager1.updateRoleListOfUser("user5t0", null, new String[]{"role2t0"});
        } catch (UserStoreException e) {
            throw new RegistryException("Error in adding user user5t0 to role2t0 role.");
        }
        try {
            userStoreManager1.updateRoleListOfUser("user6t0", null, new String[]{"role3t0"});
        } catch (UserStoreException e) {
            throw new RegistryException("Error in adding user user6t0 to role3t0 role.");
        }

        // now giving authorizations to the role.
        try {
            authorizationManager1.authorizeRole("role1t0", "/test2", ActionConstants.PUT);
        } catch (UserStoreException e) {
            throw new RegistryException("Error in authorizing role1t0.");
        }
        try {
            authorizationManager1.authorizeRole("role2t0", "/test2", ActionConstants.DELETE);
        } catch (UserStoreException e) {
            throw new RegistryException("Error in authorizing role2t0.");
        }
        try {
            authorizationManager1.authorizeRole("role3t0", "/test2", "authorize");
        } catch (UserStoreException e) {
            throw new RegistryException("Error in authorizing role3t0.");
        }

        // secondly we will check the user store from tenant 1 and verify no overlaps with tenant 0 
        UserRegistry registry2 =
                embeddedRegistryService.getUserRegistry(realmConfig.getAdminUserName(), 1);

        UserRealm userRealm2 = registry2.getUserRealm();
        UserStoreManager userStoreManager2;
        try {
            userStoreManager2 = userRealm2.getUserStoreManager();
        } catch (UserStoreException e) {
            throw new RegistryException("Error in retrieving UserStoreManager.");
        }
        AuthorizationManager authorizationManager2;
        try {
            authorizationManager2 = userRealm2.getAuthorizationManager();
        } catch (UserStoreException e) {
            throw new RegistryException("Error in retrieving AuthorizationManager.");
        }

        String[] users;
        try {
            users = userStoreManager2.listUsers("*", 10);
        } catch (UserStoreException e) {
            throw new RegistryException("Error in retrieving UserStoreManager.");
        }
        // check the existence of the user
        assertFalse("UserStore for tenant1 should not have user1t0",
                Arrays.binarySearch(users, "user1t0") >= 0);
        assertFalse("UserStore for tenant1 should not have user2t0",
                Arrays.binarySearch(users, "user2t0") >= 0);
        assertFalse("UserStore for tenant1 should not have user3t0",
                Arrays.binarySearch(users, "user3t0") >= 0);
        assertFalse("UserStore for tenant1 should not have user4t0",
                Arrays.binarySearch(users, "user4t0") >= 0);
        assertFalse("UserStore for tenant1 should not have user5t0",
                Arrays.binarySearch(users, "user5t0") >= 0);
        assertFalse("UserStore for tenant1 should not have user6t0",
                Arrays.binarySearch(users, "user6t0") >= 0);

        // check the existence of the role
        assertFalse("UserStore for tenant1 should not have role1t0",
                Arrays.binarySearch(users, "role1t0") >= 0);
        assertFalse("UserStore for tenant1 should not have role2t0",
                Arrays.binarySearch(users, "role2t0") >= 0);
        assertFalse("UserStore for tenant1 should not have role3t0",
                Arrays.binarySearch(users, "role3t0") >= 0);

        // check the user authorizations
        assertFalse("UserStore for tenant1 should not have user1t0",
                Arrays.binarySearch(users, "user1t0") >= 0);
        assertFalse("UserStore for tenant1 should not have user2t0",
                Arrays.binarySearch(users, "user2t0") >= 0);
        assertFalse("UserStore for tenant1 should not have user3t0",
                Arrays.binarySearch(users, "user3t0") >= 0);

        // check the authorization of the users.
        try {
            assertFalse("UserStore for tenant1 should not have authorizations for user1t0",
                    authorizationManager2
                            .isUserAuthorized("user1t0", "/test2", ActionConstants.PUT));
            assertFalse("UserStore for tenant1 should not have authorizations for user2t0",
                    authorizationManager2
                            .isUserAuthorized("user2t0", "/test2", ActionConstants.DELETE));
            assertFalse("UserStore for tenant1 should not have authorizations for user3t0",
                    authorizationManager2.isUserAuthorized("user3t0", "/test2", "authorize"));
        } catch (UserStoreException e) {
            throw new RegistryException("Error in checking authorizations.");
        }

        // check the user is added to the role
        try {
            assertFalse("UserStore for tenant1 should not have user4t0 user in role role1t0",
                    Arrays.binarySearch(userStoreManager2.getRoleListOfUser("user5t0"),
                            "role1t0") >= 0);
            assertFalse("UserStore for tenant1 should not have user5t0 user in role role2t0",
                    Arrays.binarySearch(userStoreManager2.getRoleListOfUser("user5t0"),
                            "role2t0") >= 0);
            assertFalse("UserStore for tenant1 should not have user6t0 user in role role3t0",
                    Arrays.binarySearch(userStoreManager2.getRoleListOfUser("user6t0"),
                            "role3t0") >= 0);
        } catch (UserStoreException e) {
            throw new RegistryException("Error in checking authorizations.");
        }
    }


    // Test adding tenants
    private void addTenants() throws Exception {
        TenantManager tenantManager;
        String dbFolder = "target/Tenanttest";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(DB_DRIVER);
        ds.setUrl(TEST_URL);

        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();

        InputStream inStream = this.getClass().getClassLoader()
                .getResource("user-test" + File.separator + JDBC_TEST_USERMGT_XML).openStream();
        RealmConfiguration realmConfig = buildRealmConfigWithJDBCConnectionUrl(inStream, TEST_URL);
        realm.init(realmConfig, MultiTenantTestClaimUtil.getClaimTestData(),
                MultiTenantTestClaimUtil.getProfileTestData(), 0);

        tenantManager = new JDBCTenantManager(ds, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

        DefaultRealmService defaultRealmService = new DefaultRealmService(realmConfig, tenantManager);

        OSGiDataHolder.getInstance().setUserRealmService(defaultRealmService);

        Tenant tenant = new Tenant();
        tenant.setDomain("WSO2.org");
        tenant.setRealmConfig(realmConfig);
        int tenantId = tenantManager.addTenant(tenant);
        Tenant tenant2 = (Tenant) tenantManager.getTenant(tenantId);

        // checking if the everything is stored.
        assertEquals("tenant domain should be equal.", tenant.getDomain().toLowerCase(), tenant2.getDomain().toLowerCase());

        Tenant tenant3 = new Tenant();
        tenant3.setDomain("abc.org");
        tenant3.setRealmConfig(realmConfig);
        tenantManager.addTenant(tenant3);


        Tenant[] tenants = (Tenant[]) tenantManager.getAllTenants();

        // check the get all tenants
        assertEquals("tenants length should be 2", tenants.length, 2);
        assertTrue("tenants should contain wso2.org",
                (tenants[0].getDomain() + tenants[1].getDomain()).contains("wso2.org"));
        assertTrue("tenants should contain abc.org",
                (tenants[0].getDomain() + tenants[1].getDomain()).contains("abc.org"));

        // if the domain exists
        int tempTenantId = tenantManager.getTenantId("wso2.org");
        assertTrue("wso2.org should exist", (tempTenantId != MultitenantConstants.INVALID_TENANT_ID &&
        		tempTenantId != MultitenantConstants.SUPER_TENANT_ID));
        tempTenantId = tenantManager.getTenantId("pqr.org");
        assertTrue("pqr.org should not exists",  tempTenantId == MultitenantConstants.INVALID_TENANT_ID);

        int tenantId4 = tenantManager.getTenantId("wso2.org");
        assertEquals("tenant domain should be wso2.org", "wso2.org",
                tenantManager.getDomain(tenantId4));

        // check the update
        Tenant tenant4 = (Tenant) tenantManager.getTenant(tenantId4);
        tenant4.setDomain("wso2.com");
        tenantManager.updateTenant(tenant4);

        // checking the updated values
        Tenant tenant5 = (Tenant) tenantManager.getTenant(tenantId4);
        assertEquals("tenant domain should be equal.", tenant5.getDomain(), "wso2.com");

        Tenant[] tenants2 = (Tenant[]) tenantManager.getAllTenants();
        // check the updated values get all tenants
        assertEquals("tenants length should be 2", tenants.length, 2);
        assertTrue("tenants should contain wso2.com",
                (tenants2[0].getDomain() + tenants2[1].getDomain()).contains("wso2.com"));
        assertTrue("tenants should contain abc.org",
                (tenants2[0].getDomain() + tenants2[1].getDomain()).contains("abc.org"));

    }

    private String getUserManagementConfigurationPath() {
        String projectDirectory = System.getProperty("project.basedir");

        if (projectDirectory == null) {
            projectDirectory = new File(".").getAbsolutePath();
        }

        StringBuilder filePath = new StringBuilder(projectDirectory).append(File.separator).append("src").
                append(File.separator).append("test").append(File.separator).append("resources").append(File.separator).
                append("user-test").append(File.separator).append("user-mgt-multi-tenant.xml");

        String userMgtFile = filePath.toString();

        if (!new File(userMgtFile).exists()) {
            fail("User manager configuration file is not found at " + userMgtFile);
        }

        return userMgtFile;
    }

    public void testClaims() throws RegistryException, UserStoreException {
        // first we will fill the user store for tenant 0
        RealmConfiguration realmConfig = OSGiDataHolder.getInstance().getUserRealmService().getBootstrapRealmConfiguration();
        UserRegistry userRegistry1 =
                embeddedRegistryService.getUserRegistry(realmConfig.getAdminUserName(), 1);

        UserStoreManager userStoreManager = userRegistry1.getUserRealm().getUserStoreManager();

        Map<String, String> claimMap = new HashMap<String, String>();
        claimMap.put("http://wso2.org/claims/givenname", "admin123");
        claimMap.put("http://wso2.org/claims/emailaddress", "admin@wso2.org");

        userStoreManager.setUserClaimValues("admin", claimMap, "home-profile");


        Map<String, String> obtained = userStoreManager.getUserClaimValues("admin", new String[]{
                "http://wso2.org/claims/givenname", "http://wso2.org/claims/emailaddress"},
                "home-profile");
        assertEquals("The email should be same",
                obtained.get("http://wso2.org/claims/emailaddress"), "admin@wso2.org");
        assertEquals("The name should be same", obtained.get("http://wso2.org/claims/givenname"),
                "admin123");

        userStoreManager.addUser("another-admin", "another-admin123", null, null, null);

        // we will check another user in the same tenant have access to the claims
        UserRegistry userRegistry2 = embeddedRegistryService.getUserRegistry("another-admin", 1);
        UserStoreManager userStoreManager2 = userRegistry2.getUserRealm().getUserStoreManager();

        Map<String, String> userRegistryObtained =
                userStoreManager2.getUserClaimValues("admin", new String[]{
                        "http://wso2.org/claims/givenname", "http://wso2.org/claims/emailaddress"},
                        "home-profile");
        assertEquals("The email should be same",
                userRegistryObtained.get("http://wso2.org/claims/emailaddress"), "admin@wso2.org");
        assertEquals("The name should be same",
                userRegistryObtained.get("http://wso2.org/claims/givenname"), "admin123");

    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    private RealmConfiguration buildRealmConfigWithJDBCConnectionUrl(InputStream inStream, String connectionUrl)
            throws org.wso2.carbon.user.core.UserStoreException {

        String JDBC_URL_PROPERTY_NAME = "url";

        RealmConfigXMLProcessor builder = new RealmConfigXMLProcessor();
        RealmConfiguration realmConfig = builder.buildRealmConfiguration(inStream);
        Map<String, String> map = realmConfig.getRealmProperties();
        map.put(JDBC_URL_PROPERTY_NAME, connectionUrl);
        return realmConfig;
    }
}
