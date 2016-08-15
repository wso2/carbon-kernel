/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.user.core.jdbc;

import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.authman.AdvancedPermissionTreeTest;
import org.wso2.carbon.user.core.authorization.JDBCAuthorizationManager;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.config.TestRealmConfigBuilder;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class JDBCRealmTest extends BaseTestCase {

    private UserRealm realm = null;

    public static final String JDBC_TEST_USERMGT_XML = "user-mgt-test.xml";

    private static String TEST_URL = "jdbc:h2:target/BasicJDBCDatabaseTest/CARBON_TEST";

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testStuff() throws Exception {

        DatabaseUtil.closeDatabasePoolConnection();
        initRealmStuff(TEST_URL);
        doUserStuff();
        doUserRoleStuff();
        doAuthorizationStuff();
        doClaimStuff();
    }

    public void initRealmStuff(String dbUrl) throws Exception {

        String dbFolder = "target/BasicJDBCDatabaseTest";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl(dbUrl);
        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();

        realm = new DefaultRealm();
        InputStream inStream = this.getClass().getClassLoader().getResource(
                JDBCRealmTest.JDBC_TEST_USERMGT_XML).openStream();
        RealmConfiguration realmConfig = TestRealmConfigBuilder
                .buildRealmConfigWithJDBCConnectionUrl(inStream, TEST_URL);
        realm.init(realmConfig, ClaimTestUtil.getClaimTestData(), ClaimTestUtil
                .getProfileTestData(), MultitenantConstants.SUPER_TENANT_ID);
        ds.close();
    }

    public void testAuthorizationClearence() throws Exception{
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl("jdbc:h2:target/clear-resources/WSO2CARBON_DB_CLEAR");
        ds.setUsername("wso2carbon");
        ds.setPassword("wso2carbon");

        realm = new DefaultRealm();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(UserCoreConstants.DATA_SOURCE, ds);

        RealmConfigXMLProcessor xmlProcessor = new RealmConfigXMLProcessor();
        InputStream stream = new FileInputStream("target/clear-resources/user-mgt-clear.xml");
        RealmConfiguration configuration = xmlProcessor.buildRealmConfiguration(stream);

        JDBCAuthorizationManager jdbcAuthnManager = new JDBCAuthorizationManager(configuration, properties, null, null, realm, 0);

        String[] roles = jdbcAuthnManager.getAllowedRolesForResource("/permission/admin", "ui.execute");
        assertEquals(roles.length,1);
        
        
        jdbcAuthnManager.clearPermissionTree();
        
        //the tree should automatically be loaded on next call
        roles = jdbcAuthnManager.getAllowedRolesForResource("/permission/admin", "ui.execute");
        assertEquals(roles.length,1);
    }

    public void doUserStuff() throws Exception {
        UserStoreManager admin = realm.getUserStoreManager();

        Map<String, String> userProps = new HashMap<String, String>();
        userProps.put(ClaimTestUtil.CLAIM_URI1, "1claim1Value");
        userProps.put(ClaimTestUtil.CLAIM_URI2, "2claim2Value");

        Permission[] permisions = new Permission[2];
        permisions[0] = new Permission("high security", "read");
        permisions[1] = new Permission("low security", "write");

        // add
        admin.addUser("dimuthu", "credential", null, null, null, false);
        admin.addRole("role1", new String[] { "dimuthu" }, permisions);
        admin.addUser("vajira", "credential", new String[] { "role1" }, userProps, null, false);
        int id = admin.getUserId("dimuthu");
        int tenatId = admin.getTenantId("dimuthu");
        
        // authenticate
        assertTrue(admin.authenticate("dimuthu", "credential"));

        admin.updateCredentialByAdmin("dimuthu", "topsecret");
        assertTrue(admin.authenticate("dimuthu", "topsecret"));

        assertTrue(admin.isExistingUser("dimuthu"));
        assertFalse(admin.isExistingUser("muhaha"));

        // update
        admin.updateCredential("dimuthu", "password", "topsecret");
        assertFalse(admin.authenticate("dimuthu", "credential"));
        assertTrue(admin.authenticate("dimuthu", "password"));

        String[] names = admin.listUsers("*", 100);
        assertEquals(3, names.length);

        String[] roleNames = admin.getRoleNames();
        assertEquals(3, roleNames.length);

        // delete
        admin.deleteUser("vajira");
        assertFalse(admin.authenticate("vajira", "credential"));
        admin.addUser("vajira", "credential", new String[] { "role1" }, userProps, null, false);
        admin.deleteRole("role1");
        admin.addRole("role1", new String[] { "dimuthu" }, permisions);
    }

    public void doUserRoleStuff() throws Exception {
        UserStoreManager admin = realm.getUserStoreManager();

        admin.addRole("role2", null, null);
        admin.addRole("role3", null, null);
        admin.addRole("role4", null, null);
        admin.addUser("saman", "pass1", null, null, null, false);
        admin.addUser("amara", "pass2", null, null, null, false);
        admin.addUser("sunil", "pass3", null, null, null, false);

        admin.updateRoleListOfUser("saman", null, new String[] { "role2" });
        admin.updateRoleListOfUser("saman", new String[] { "role2" }, new String[] { "role4",
                "role3" });

        String[] rolesOfSaman = admin.getRoleListOfUser("saman");
        assertEquals(3, rolesOfSaman.length);

        // negative
        admin.updateUserListOfRole("role2", new String[] { "saman" }, null);
        admin.updateUserListOfRole("role3", null, new String[] { "amara", "sunil" });

        String[] users = admin.getUserListOfRole("role3");
        assertEquals(3, users.length);

        // negative
        try {
            admin.updateRoleListOfUser("saman", new String[] { "x" }, new String[] { "y" });
            TestCase.assertTrue(false);
        } catch (Exception e) {
            // exptected error in negative testing

        }
        try {
            admin.updateUserListOfRole("role2", null, new String[] { "d" });
            TestCase.assertTrue(false);
        } catch (Exception e) {
            // exptected error in negative testing
        }

    }

    public void doAuthorizationStuff() throws Exception {
        AuthorizationManager authMan = realm.getAuthorizationManager();
        UserStoreManager usWriter = realm.getUserStoreManager();

        usWriter.addRole("rolex", new String[] { "saman", "amara" }, null);
        usWriter.addRole("roley", null, null);
        authMan.authorizeRole("rolex", "wall", "write");
        authMan.authorizeRole("roley", "table", "write");
        authMan.authorizeUser("sunil", "wall", "read");

        assertTrue(authMan.isUserAuthorized("saman", "wall", "write"));
        assertTrue(authMan.isUserAuthorized("sunil", "wall", "read"));
        //assertTrue(authMan.isRoleAuthorized("primary/roley", "table", "write"));
        assertTrue(authMan.isRoleAuthorized("roley", "table", "write"));
        assertFalse(authMan.isUserAuthorized("saman", "wall", "read"));
        assertFalse(authMan.isUserAuthorized("sunil", "wall", "write"));
        assertEquals(1, authMan.getAllowedRolesForResource("wall", "write").length);
        assertEquals(1, authMan.getExplicitlyAllowedUsersForResource("wall", "read").length);

        authMan.denyRole("rolex", "wall", "write");
        //assertFalse(authMan.isRoleAuthorized("primary/rolex", "wall", "write"));
        assertFalse(authMan.isRoleAuthorized("rolex", "wall", "write"));

        authMan.denyUser("saman", "wall", "read");
        assertFalse(authMan.isUserAuthorized("saman", "wall", "read"));

        assertEquals(1, authMan.getDeniedRolesForResource("wall", "write").length);
        assertEquals(1, authMan.getExplicitlyDeniedUsersForResource("wall", "read").length);

        authMan.clearUserAuthorization("sunil", "wall", "read");
        //authMan.clearRoleAuthorization("primary/roley", "table", "write");
        authMan.clearRoleAuthorization("roley", "table", "write");
        authMan.clearResourceAuthorizations("wall");

        assertFalse(authMan.isUserAuthorized("saman", "wall", "write"));
        assertFalse(authMan.isUserAuthorized("sunil", "wall", "read"));
        assertFalse(authMan.isRoleAuthorized("roley", "table", "write"));
        //assertFalse(authMan.isRoleAuthorized("primary/roley", "table", "write"));
    }

    public void doClaimStuff() throws Exception {
        UserStoreManager usWriter = realm.getUserStoreManager();
        String[] allClaims = { ClaimTestUtil.CLAIM_URI1, ClaimTestUtil.CLAIM_URI2,
                ClaimTestUtil.CLAIM_URI3 };

        // add default
        usWriter.setUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, "claim1default", null);
        String value = usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, null);
        assertEquals("claim1default", value);

        // update default
        usWriter.setUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, "dimzi lee", null);
        value = usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, null);
        assertEquals("dimzi lee", value);

        // multiple additions
        Map<String, String> map = new HashMap<String, String>();
        map.put(ClaimTestUtil.CLAIM_URI1, "lee");
        map.put(ClaimTestUtil.CLAIM_URI3, "muthu");

        usWriter.setUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI2, "claim2default", null);
        usWriter.setUserClaimValues("dimuthu", map, ClaimTestUtil.HOME_PROFILE_NAME);
        usWriter.setUserClaimValue("dimuthu", UserCoreConstants.PROFILE_CONFIGURATION,
                ClaimTestUtil.HOME_PROFILE_NAME, ClaimTestUtil.HOME_PROFILE_NAME);

        Map<String, String> obtained = usWriter.getUserClaimValues("dimuthu", allClaims,
                ClaimTestUtil.HOME_PROFILE_NAME);

        //assertNull(obtained.get(ClaimTestUtil.CLAIM_URI1)); // hidden
        //assertEquals("claim2default", obtained.get(ClaimTestUtil.CLAIM_URI2)); // overridden
        assertEquals("muthu", obtained.get(ClaimTestUtil.CLAIM_URI3)); // normal

        // update
        map.put(ClaimTestUtil.CLAIM_URI3, "muthulee");
        usWriter.setUserClaimValues("dimuthu", map, ClaimTestUtil.HOME_PROFILE_NAME);
        value = usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI3,
                ClaimTestUtil.HOME_PROFILE_NAME);
        assertEquals("muthulee", value);

        // delete
        usWriter.deleteUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, null);
        value = usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, null);
        assertNull(value);

        usWriter.deleteUserClaimValues("dimuthu", allClaims, ClaimTestUtil.HOME_PROFILE_NAME);
        obtained = usWriter.getUserClaimValues("dimuthu", allClaims,
                ClaimTestUtil.HOME_PROFILE_NAME);
        assertNull(obtained.get(ClaimTestUtil.CLAIM_URI2)); // overridden

    }
}
