/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.common.LoginIdentifier;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.common.UserIdResolverCache;
import org.wso2.carbon.user.core.config.TestRealmConfigBuilder;
import org.wso2.carbon.user.core.model.ExpressionAttribute;
import org.wso2.carbon.user.core.model.ExpressionCondition;
import org.wso2.carbon.user.core.model.ExpressionOperation;
import org.wso2.carbon.user.core.model.OperationalCondition;
import org.wso2.carbon.user.core.model.UniqueIDUserClaimSearchEntry;
import org.wso2.carbon.user.core.model.UserClaimSearchEntry;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_ID_FROM_USER_NAME_CACHE_NAME;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_NAME_FROM_UNIQUE_USER_ID_CACHE_NAME;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_NAME_FROM_USER_ID_CACHE_NAME;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_UNIQUE_ID_FROM_USER_NAME_CACHE_NAME;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UniqueIDJDBCRealmPrimaryUserStoreTest extends BaseTestCase {

    public static final String JDBC_TEST_USERMGT_XML = "user-mgt-test-uniqueId.xml";

    private static String TEST_URL = "jdbc:h2:./target/BasicUniqueIDJDBCDatabaseTest/CARBON_TEST";
    private AbstractUserStoreManager admin = null;
    private static String userId1;
    private static String userId2;

    public void setUp() throws Exception {

        super.setUp();
        clearUserIdResolverCache();
        DatabaseUtil.closeDatabasePoolConnection();
        initRealmStuff(TEST_URL);
        DatabaseUtil.closeDatabasePoolConnection();
    }

    public void initRealmStuff(String dbUrl) throws Exception {

        String dbFolder = "target/BasicUniqueIDJDBCDatabaseTest";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl(dbUrl);
        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();
        UserRealm realm = new DefaultRealm();
        InputStream inStream = this.getClass().getClassLoader().getResource(JDBC_TEST_USERMGT_XML).openStream();
        RealmConfiguration realmConfig = TestRealmConfigBuilder
                .buildRealmConfigWithJDBCConnectionUrl(inStream, TEST_URL);
        realm.init(realmConfig, ClaimTestUtil.getClaimTestData(), ClaimTestUtil
                .getProfileTestData(), MultitenantConstants.SUPER_TENANT_ID);
        admin = (AbstractUserStoreManager) realm.getUserStoreManager();
        ds.close();
    }

    public void test100AdRole() throws UserStoreException {

        admin.addRole("role1", null, null);
        admin.addRole("role3", null, null);
        admin.addRole("role4", null, null);
        //admin, Internal/everyone, role1, role3, role4
        Assert.assertEquals(5, admin.getRoleNames().length);
    }

    public void test101AddUser() throws UserStoreException {

        admin.addUser("user2", "pass2", null, null, null, false);
        admin.addUser("user3", "pass3", null, null, null, false);
        admin.addUser("user4", "pass4", null, null, null, false);
        //  user2,user3,user4 + admin
        Assert.assertEquals(4, admin.listUsers("*", 100).length);
    }

    public void test102AdRoleWithUser() throws UserStoreException {

        admin.addRole("role2", new String[]{"user2"}, null);
        //admin, Internal/everyone, role1, role2, role3, role4
        Assert.assertEquals(6, admin.getRoleNames().length);
    }

    public void test103AddUserWithRole() throws UserStoreException {

        admin.addUser("user1", "pass1", new String[]{"role1"}, null, null, false);
        //  user1,user2,user3,user4 + admin
        Assert.assertEquals(5, admin.listUsers("*", 100).length);
    }

    public void test104Authenticate() throws UserStoreException {

        assertTrue(admin.authenticate("user1", "pass1"));
        assertTrue(admin.authenticate("user2", "pass2"));
    }

    public void test105UpdateCredential() throws UserStoreException {

        admin.updateCredential("user1", "pass11", "pass1");
        assertFalse(admin.authenticate("user1", "pass1"));
        assertTrue(admin.authenticate("user1", "pass11"));
    }

    public void test106UpdateCredentialByAdmin() throws UserStoreException {

        admin.updateCredentialByAdmin("user2", "pass22");
        assertFalse(admin.authenticate("user2", "pass2"));
        assertTrue(admin.authenticate("user2", "pass22"));
    }

    public void test107SetUserClaimValueInDefaultProfile() throws UserStoreException {

        admin.setUserClaimValue("user1", ClaimTestUtil.CLAIM_URI1, "usergivenname1", null);
        assertEquals("usergivenname1", admin.getUserClaimValue("user1", ClaimTestUtil.CLAIM_URI1, null));
    }

    public void test108SetUserClaimValuesInDefaultProfile() throws UserStoreException {

        //Test Set/Get User Claim Values in default profile
        Map<String, String> map = new HashMap<>();
        map.put(ClaimTestUtil.CLAIM_URI1, "usergivenname2");
        map.put(ClaimTestUtil.CLAIM_URI3, "usergivenname3");

        admin.setUserClaimValues("user2", map, null);

        String[] allClaims = {ClaimTestUtil.CLAIM_URI1, ClaimTestUtil.CLAIM_URI2,
                ClaimTestUtil.CLAIM_URI3};

        Map<String, String> obtained = admin.getUserClaimValues("user2", allClaims, null);
        assertEquals("usergivenname2", obtained.get(ClaimTestUtil.CLAIM_URI1));
        assertEquals("usergivenname3", obtained.get(ClaimTestUtil.CLAIM_URI3));
        assertNull(obtained.get(ClaimTestUtil.CLAIM_URI2));
    }

    public void test109SetUserClaimValueInCustomProfile() throws UserStoreException {

        //Test Set/Get User Claim Values in home profile
        admin.setUserClaimValue("user1", ClaimTestUtil.CLAIM_URI1, "usergivenname1_home",
                ClaimTestUtil.HOME_PROFILE_NAME);
        assertEquals("usergivenname1_home", admin.getUserClaimValue("user1", ClaimTestUtil.CLAIM_URI1, ClaimTestUtil.HOME_PROFILE_NAME));
        assertEquals("usergivenname1", admin.getUserClaimValue("user1", ClaimTestUtil.CLAIM_URI1, null));
    }

    public void test110SetUserClaimValuesInCustomProfile() throws UserStoreException {

        Map<String, String> map = new HashMap<>();
        map.put(ClaimTestUtil.CLAIM_URI1, "usergivenname2_home");
        map.put(ClaimTestUtil.CLAIM_URI3, "usergivenname3_home");

        admin.setUserClaimValues("user2", map, ClaimTestUtil.HOME_PROFILE_NAME);
        String[] allClaims = {ClaimTestUtil.CLAIM_URI1, ClaimTestUtil.CLAIM_URI2,
                ClaimTestUtil.CLAIM_URI3};
        Map<String, String> obtained = admin.getUserClaimValues("user2", allClaims, ClaimTestUtil.HOME_PROFILE_NAME);
        assertEquals("usergivenname2_home", obtained.get(ClaimTestUtil.CLAIM_URI1));
        assertEquals("usergivenname3_home", obtained.get(ClaimTestUtil.CLAIM_URI3));
        assertNull(obtained.get(ClaimTestUtil.CLAIM_URI2));

        obtained = admin.getUserClaimValues("user2", allClaims, null);
        assertEquals("usergivenname2", obtained.get(ClaimTestUtil.CLAIM_URI1));
        assertEquals("usergivenname3", obtained.get(ClaimTestUtil.CLAIM_URI3));
        assertNull(obtained.get(ClaimTestUtil.CLAIM_URI2));

        // With the username and userID claim in default profile.
        assertEquals(4, admin.getUserClaimValues("user2", null).length);
        assertEquals(2, admin.getUserClaimValues("user2", ClaimTestUtil.HOME_PROFILE_NAME).length);
    }

    public void test111GetUserIDFromUsernameAndUserNameFromUserId() throws UserStoreException {
        // Check UserIDFromUsername and UserNameFromUserID.
        String userId = admin.getUserIDFromUserName("user2");
        assertNotNull(userId);
        assertEquals("user2", admin.getUserNameFromUserID(userId));
    }

    public void test112GetUserListInDefaultProfile() throws UserStoreException {

        assertEquals(1, admin.getUserList(ClaimTestUtil.CLAIM_URI1, "usergivenname2", null).length);
    }

    public void test113GetUserListInCustomProfile() throws UserStoreException {

        assertEquals(1, admin.getUserList(ClaimTestUtil.CLAIM_URI1, "usergivenname2_home", ClaimTestUtil.HOME_PROFILE_NAME).length);
    }

    public void test114GetUserListWithPagination() throws UserStoreException {

        assertEquals(1, admin.getUserList(ClaimTestUtil.CLAIM_URI1, "usergivenname2", null, 10, 1).length);
        assertEquals(0, admin.getUserList(ClaimTestUtil.CLAIM_URI1, "usergivenname2", null, 10, 2).length);
    }

    public void test115GetUserListWithCondition() throws UserStoreException {

        ExpressionCondition expressionCondition = new ExpressionCondition(ExpressionOperation.EQ.toString(),
                ExpressionAttribute.USERNAME.toString(), "user2");
        assertEquals(1, admin.getUserList(expressionCondition, null, null, 10, 0, null, null).length);
        assertEquals(0, admin.getUserList(expressionCondition, null, null, 10, 2, null, null).length);
    }

    public void test116GetRoleListOfUser() throws UserStoreException {

        assertEquals(2, admin.getRoleListOfUser("user1").length);
    }

    public void test117UpdateRoleListOfUser() throws UserStoreException {

        admin.updateRoleListOfUser("user1", null, new String[]{"role2"});
        admin.updateRoleListOfUser("user1", new String[]{"role2"}, new String[]{"role4",
                "role3"});

        String[] rolesOfUser = admin.getRoleListOfUser("user1");
        assertEquals(4, rolesOfUser.length);

        admin.updateRoleListOfUser("user1", new String[]{"role3"}, null);

        String[] rolesOfUserNew = admin.getRoleListOfUser("user1");
        assertEquals(3, rolesOfUserNew.length);

        //negative
        try {
            admin.updateRoleListOfUser("user1", new String[]{"role2"}, new String[]{"role4",
                    "no_role1"});
        } catch (UserStoreException e) {
            // Expected
            assertEquals("The role: no_role1 does not exist.", e.getMessage());
        }
    }

    public void test118UpdateUserListOfRole() throws UserStoreException {

        admin.updateUserListOfRole("role2", new String[]{"user1"}, null);
        admin.updateUserListOfRole("role3", null, new String[]{"user1", "user2"});
        //negative
        try {
            admin.updateUserListOfRole("role3", null, new String[]{"nouser1", "nouser2"});
        } catch (UserStoreException e) {
            // Expected
            assertEquals("User nouser1 does not exit in the system.", e.getMessage());
        }
        String[] users = admin.getUserListOfRole("role3");
        assertEquals(2, users.length);

        admin.updateUserListOfRole("role3", new String[]{"user1"}, null);

        String[] usersNew = admin.getUserListOfRole("role3");
        assertEquals(1, usersNew.length);
    }

    public void test119IsUserInRole() throws UserStoreException {

        assertFalse(admin.isUserInRole("user1", "role3"));
        assertTrue(admin.isUserInRole("user2", "role3"));
    }

    public void test120IsUserExists() throws UserStoreException {

        assertTrue(admin.isExistingUser("user2"));
        assertFalse(admin.isExistingUser("no-user2"));
    }

    public void test121IsRoleExists() throws UserStoreException {

        assertTrue(admin.isExistingRole("role3"));
        assertFalse(admin.isExistingRole("no-role3"));
    }

    public void test122GetUsersClaimValues() throws UserStoreException {

        String[] allClaims = {ClaimTestUtil.CLAIM_URI1, ClaimTestUtil.CLAIM_URI2,
                ClaimTestUtil.CLAIM_URI3};

        UserClaimSearchEntry[] obtained = admin.getUsersClaimValues(new String[]{"user1", "user2"}, allClaims, null);

        assertEquals(2, obtained.length);
        assertNotNull(obtained[0].getClaims().get("http://wso2.org/givenname"));
        assertNotNull(obtained[1].getClaims().get("http://wso2.org/givenname"));
    }

    public void test123UpdateRoleName() throws UserStoreException {

        String[] usersBefore = admin.getUserListOfRole("role3");
        admin.updateRoleName("role3", "newrole3");
        String[] usersAfter = admin.getUserListOfRole("newrole3");
        assertEquals(usersBefore.length, usersAfter.length);

        String[] usersBefore1 = admin.getUserListOfRole("newrole3");
        admin.updateRoleName("newrole3", "NewRole3");
        String[] usersAfter1 = admin.getUserListOfRole("NewRole3");
        assertEquals(usersBefore1.length, usersAfter1.length);
    }

    public void test124DeleteUserClaimValue() throws UserStoreException {

        assertNotNull(admin.getUserClaimValue("user1", "http://wso2.org/givenname", null));
        admin.deleteUserClaimValue("user1", "http://wso2.org/givenname", null);
        assertNull(admin.getUserClaimValue("user1", "http://wso2.org/givenname", null));
    }

    public void test125DeleteUserClaimValues() throws UserStoreException {

        assertNotNull(admin.getUserClaimValue("user2", "http://wso2.org/givenname3", null));
        admin.deleteUserClaimValues("user2", new String[]{"http://wso2.org/givenname3"}, null);
        assertNull(admin.getUserClaimValue("user2", "http://wso2.org/givenname3", null));
    }

    public void test126DeleteRole() throws UserStoreException {

        String[] users = admin.getUserListOfRole("role1");
        for (String user : users) {
            assertTrue(ArrayUtils.contains(admin.getRoleListOfUser(user), "role1"));
        }
        admin.deleteRole("role1");
        assertFalse(ArrayUtils.contains(admin.getRoleNames(), "role1"));

        for (String user : users) {
            assertFalse(ArrayUtils.contains(admin.getRoleListOfUser(user), "role1"));
        }
    }

    public void test176DeleteUser() throws UserStoreException {

        String[] roles = admin.getRoleListOfUser("user1");
        for (String role : roles) {
            assertTrue(ArrayUtils.contains(admin.getUserListOfRole(role), "user1"));
        }
        admin.deleteUser("user1");
        assertFalse(ArrayUtils.contains(admin.listUsers("*", 100), "role1"));

        for (String role : roles) {
            assertFalse(ArrayUtils.contains(admin.getUserListOfRole(role), "user1"));
        }
    }


    public void test177AdRoleWithID() throws UserStoreException {

        admin.addRoleWithID("role1WithID", null, null, false);
        //admin, Internal/everyone, role1WithID, role3, role4
        Assert.assertEquals(6, admin.getRoleNames().length);
    }

    public void test178AddUserWithID() throws UserStoreException {

        User user2WithID = admin.addUserWithID("user2WithID", "pass2", null, null, null);
        assertNotNull(user2WithID);
        userId2 = user2WithID.getUserID();
        assertNotNull(admin.addUserWithID("user3WithID", "pass3", null, null, null));
        assertNotNull(admin.addUserWithID("user4WithID", "pass4", null, null, null));
        //  user2,user3,user4 + admin + user2WithID,user3WithID,user4WithID
        Assert.assertEquals(7, admin.listUsersWithID("*", 100).size());
    }

    public void test179AdRoleWithUserWithID() throws UserStoreException {

        admin.addRoleWithID("role2WithID", new String[]{userId2}, null, false);
        //admin, Internal/everyone, role1, role2, role3, role4,role1WithID
        Assert.assertEquals(7, admin.getRoleNames().length);
    }

    public void test180AddUserWithRoleWithID() throws UserStoreException {

        User user1WithID = admin.addUserWithID("user1WithID", "pass1", new String[]{"role1WithID"},
                null, null);
        assertNotNull(user1WithID);
        userId1 = user1WithID.getUserID();
        //  user2,user3,user4 + admin + user2WithID,user3WithID,user4WithID,user1WithID
        Assert.assertEquals(8, admin.listUsersWithID("*", 100).size());
    }

    // TODO: 12/18/19 this is failing. Needs to be fixed

    public void test181AdRoleWithUseNegativerWithID() {
        //add role with an invalid user id.
        try {
            admin.addRoleWithID("role11WithID", new String[]{"invalid_user_id", userId2}, null, false);
        } catch (UserStoreException e) {
            // TODO: 12/18/19 Need to write a proper assert
            // Expect UserStoreException. Specially check for SQL exceptoins in negative cases. Should send proper
            // messages.
//            assertFalse(e.getMessage().contains("Error occurred while getting database type from DB connection"));
        }
    }

    public void test182SetUserClaimValuesWithIDInDefaultProfile() throws UserStoreException {

        //Test Set/Get User Claim Values in default profile.
        admin.setUserClaimValueWithID(userId1, ClaimTestUtil.CLAIM_URI1, "usergivenname1_with_id", null);
        assertEquals("usergivenname1_with_id", admin.getUserClaimValueWithID(userId1, ClaimTestUtil.CLAIM_URI1,  null));
    }

    public void test183AuthenticateWithID() throws UserStoreException {

        assertEquals(AuthenticationResult.AuthenticationStatus.SUCCESS, admin.authenticateWithID(userId1,
                "pass1").getAuthenticationStatus());
        assertEquals(AuthenticationResult.AuthenticationStatus.SUCCESS, admin.authenticateWithID(userId2,
                "pass2").getAuthenticationStatus());
    }

    public void test184AuthenticateWithIDPreferredUsername() throws UserStoreException {

        assertEquals(AuthenticationResult.AuthenticationStatus.SUCCESS,
                admin.authenticateWithID(ClaimTestUtil.CLAIM_URI1, "usergivenname1_with_id",
                        "pass1", null).getAuthenticationStatus());
    }

    public void test185SetUserClaimValuesWithIDInDefaultProfile() throws UserStoreException {

        //Test Set/Get User Claim Values in default profile
        Map<String, String> map = new HashMap<>();
        map.put(ClaimTestUtil.CLAIM_URI1, "usergivenname2withId");
        map.put(ClaimTestUtil.CLAIM_URI3, "usergivenname3withId");

        admin.setUserClaimValuesWithID(userId2, map, null);

        String[] allClaims = {ClaimTestUtil.CLAIM_URI1, ClaimTestUtil.CLAIM_URI2,
                ClaimTestUtil.CLAIM_URI3};

        Map<String, String> obtained = admin.getUserClaimValuesWithID(userId2, allClaims, null);
        assertEquals("usergivenname2withId", obtained.get(ClaimTestUtil.CLAIM_URI1));
        assertEquals("usergivenname3withId", obtained.get(ClaimTestUtil.CLAIM_URI3));
        assertNull(obtained.get(ClaimTestUtil.CLAIM_URI2));
    }

    public void test186AuthenticateWithIDLoginIdentifier() throws UserStoreException {

        LoginIdentifier loginIdentifier1 = new LoginIdentifier(ClaimTestUtil.CLAIM_URI1,
                "usergivenname2withId", null, LoginIdentifier.LoginIdentifierType.CLAIM_URI);
        LoginIdentifier loginIdentifier2 = new LoginIdentifier(ClaimTestUtil.CLAIM_URI3,
                "usergivenname3withId", null, LoginIdentifier.LoginIdentifierType.CLAIM_URI);
        List<LoginIdentifier> loginIdentifiers = new ArrayList<>();
        loginIdentifiers.add(loginIdentifier1);
        loginIdentifiers.add(loginIdentifier2);
        assertEquals(AuthenticationResult.AuthenticationStatus.SUCCESS,
                admin.authenticateWithID(loginIdentifiers, null, "pass2").getAuthenticationStatus());
    }

    public void test187UpdateCredentialWithID() throws UserStoreException {

        admin.updateCredentialWithID(userId1, "pass11", "pass1");
        assertEquals(AuthenticationResult.AuthenticationStatus.FAIL, admin.authenticateWithID(userId1,
                "pass1").getAuthenticationStatus());
        assertEquals(AuthenticationResult.AuthenticationStatus.SUCCESS, admin.authenticateWithID(userId1,
                "pass11").getAuthenticationStatus());
    }

    public void test188UpdateCredentialByAdminWithID() throws UserStoreException {

        admin.updateCredentialByAdminWithID(userId1, "pass22");
        assertEquals(AuthenticationResult.AuthenticationStatus.FAIL, admin.authenticateWithID(userId1,
                "pass11").getAuthenticationStatus());
        assertEquals(AuthenticationResult.AuthenticationStatus.SUCCESS, admin.authenticateWithID(userId1,
                "pass22").getAuthenticationStatus());
    }

    public void test189SetUserClaimValuesInCustomProfile() throws UserStoreException {

        //Test Set/Get User Claim Values in default profile
        Map<String, String> map = new HashMap<>();
        map.put(ClaimTestUtil.CLAIM_URI1, "usergivenname2WithID");
        map.put(ClaimTestUtil.CLAIM_URI3, "usergivenname2WithID");

        admin.setUserClaimValuesWithID(userId2, map, ClaimTestUtil.HOME_PROFILE_NAME);

        String[] allClaims = {ClaimTestUtil.CLAIM_URI1, ClaimTestUtil.CLAIM_URI2,
                ClaimTestUtil.CLAIM_URI3};

        Map<String, String> obtained = admin.getUserClaimValuesWithID(userId2, allClaims, ClaimTestUtil.HOME_PROFILE_NAME);
        assertEquals("usergivenname2WithID", obtained.get(ClaimTestUtil.CLAIM_URI1));
        assertEquals("usergivenname2WithID", obtained.get(ClaimTestUtil.CLAIM_URI3));
        assertNull(obtained.get(ClaimTestUtil.CLAIM_URI2));
    }

    public void test190SetUserClaimValuesWithIDInCustomProfile() throws UserStoreException {

        //Test Set/Get User Claim Values in home profile
        admin.setUserClaimValueWithID(userId1, ClaimTestUtil.CLAIM_URI1, "usergivenname1_home_with_id",
                ClaimTestUtil.HOME_PROFILE_NAME);
        assertEquals("usergivenname1_home_with_id", admin.getUserClaimValueWithID(userId1, ClaimTestUtil.CLAIM_URI1,
                ClaimTestUtil.HOME_PROFILE_NAME));
    }

    public void test191GetUserListWithIDInDefaultProfile() throws UserStoreException {

        assertEquals(1, admin.getUserListWithID(ClaimTestUtil.CLAIM_URI1, "usergivenname2withId", null).size());
    }

    public void test192GetUserListWithIDInCustomProfile() throws UserStoreException {

        assertEquals(1, admin.getUserListWithID(ClaimTestUtil.CLAIM_URI1, "usergivenname1_home_with_id",
                ClaimTestUtil.HOME_PROFILE_NAME).size());
    }

    public void test193GetUserListWithIDWithPagination() throws UserStoreException {

        assertEquals(1, admin.getUserListWithID(ClaimTestUtil.CLAIM_URI1, "usergivenname2withId",
                null, 10, 0).size());
        assertEquals(1, admin.getUserListWithID(ClaimTestUtil.CLAIM_URI1, "usergivenname1_home_with_id",
                ClaimTestUtil.HOME_PROFILE_NAME, 10, 0).size());
        assertEquals(0, admin.getUserListWithID(ClaimTestUtil.CLAIM_URI1, "usergivenname2withId",
                null, 10, 2).size());
    }

    public void test194GetUserListWithIDCondition() throws UserStoreException {

        ExpressionCondition expressionCondition = new ExpressionCondition(ExpressionOperation.EQ.toString(),
                ExpressionAttribute.USERNAME.toString(), "user2WithID");
        assertEquals(1, admin.getUserListWithID(expressionCondition, null, null, 10, 0, null, null).size());
        assertEquals(0, admin.getUserListWithID(expressionCondition, null, null, 10, 2, null, null).size());
    }

    public void test194GetUserListWithIDConditionCursor() throws Exception {

        // Current user list  admin, user1WithID, user2, user2WithID, user3, user3WithID, user4, user4WithID.

        // userName sw use.
        ExpressionCondition expressionCondition = new ExpressionCondition(ExpressionOperation.SW.toString(),
                ExpressionAttribute.USERNAME.toString(), "use");
        ExpressionCondition noConditions = new ExpressionCondition(ExpressionOperation.SW.toString(),
                ExpressionAttribute.USERNAME.toString(), "");

        //Making a List of expressionConditions for multi-attribute filtering.
        // userName co WithID.
        ExpressionCondition expressionCondition2 = new ExpressionCondition(ExpressionOperation.CO.toString(),
                ExpressionAttribute.USERNAME.toString(), "WithID");
        // userName sw use and userName co WithID.
        OperationalCondition multiFiltering =
                new OperationalCondition("AND", expressionCondition, expressionCondition2);

        // No filtering.
        assertEquals(8, admin.getUserListWithID(noConditions, null, null, 10, "", "NEXT", null, null).size());

        // Single userName filtering initial query (no cursor).
        assertEquals(7, admin.getUserListWithID(expressionCondition, null, null, 10, "", "NEXT", null, null).size());

        // Single userName filtering - Forwards cursor pagination.
        assertEquals(4, admin.getUserListWithID(expressionCondition, null, null, 10, "user2WithID", "NEXT", null, null)
                .size());

        // Single userName filtering - Backwards cursor pagination.
        assertEquals(2, admin.getUserListWithID(expressionCondition, null, null, 10, "user2WithID", "PREVIOUS",
                null, null).size());

        // Multiple filter conditions with userName attribute - Forwards cursor pagination.
        assertEquals(4, admin.getUserListWithID(multiFiltering, null, null, 10, "", "NEXT", null, null).size());

        // Multiple filter conditions with userName attribute - Backwards cursor pagination.
        assertEquals(1, admin.getUserListWithID(multiFiltering, null, null, 10, "user2WithID", "PREVIOUS", null, null)
                .size());

        // Setup for claim filtering.
        Map<String, String> claims = new HashMap<>();
        claims.put(ClaimTestUtil.CLAIM_URI1, "Alucard");
        claims.put(ClaimTestUtil.CLAIM_URI6, "Tepes, Alucard");
        admin.addUser("Alucard", "pass100", null, claims, null, false);
        claims.clear();
        claims.put(ClaimTestUtil.CLAIM_URI1, "Akira");
        claims.put(ClaimTestUtil.CLAIM_URI6, "Toriyama, Akira");
        admin.addUser("Akira", "pass100", null, claims, null, false);
        claims.clear();
        claims.put(ClaimTestUtil.CLAIM_URI1, "Alphonse");
        claims.put(ClaimTestUtil.CLAIM_URI6, "Elrich, Alphonse");
        admin.addUser("Alphonse", "pass100", null, claims, null, false);
        claims.clear();
        claims.put(ClaimTestUtil.CLAIM_URI1, "Annie");
        claims.put(ClaimTestUtil.CLAIM_URI6, "Leonhart, Annie");
        admin.addUser("Annie", "pass100", null, claims, null, false);

        // New user list: admin, Akira, Alphonse, Alucard, Annie, user1WithID, user2, user2WithID, user3, user3WithID,
        // user4, user4WithID.
        // Having claims: Akira, Alphonse, Alucard, Annie.

        // givenname sw A.
        ExpressionCondition claimFiltering1 = new ExpressionCondition(ExpressionOperation.SW.toString(), "attr1", "A");
        // fullname sw T.
        ExpressionCondition claimFiltering2 = new ExpressionCondition(ExpressionOperation.SW.toString(), "fullname",
                "T");
        // givenname sw A and fullname sw T.
        OperationalCondition multiClaimFiltering = new OperationalCondition("AND", claimFiltering1, claimFiltering2);

        // Single claim filtering forward cursor pagination.
        assertEquals(4, admin.getUserListWithID(claimFiltering1, null, null, 10, "", "NEXT", null, null).size());

        // Single claim filtering backwards cursor pagination.
        assertEquals(2, admin.getUserListWithID(claimFiltering1, null, null, 10, "Alucard", "PREVIOUS", null, null)
                .size());

        // Multi claim filtering forwards cursor pagination.
        assertEquals(2, admin.getUserListWithID(multiClaimFiltering, null, null, 10, "", "NEXT", null, null).size());

        // Multi claim filtering backwards cursor pagination.
        assertEquals(1, admin.getUserListWithID(multiClaimFiltering, null, null, 10, "Alucard", "PREVIOUS", null, null)
                .size());

        // Role configuration for group filtering.

        admin.addRole("Manager", new String[]{"Alucard", "Akira", "Alphonse", "Annie", "user2", "user2WithID", "user3",
                "user3WithID"}, null);
        admin.addRole("HeadManager", new String[]{"Alucard", "Akira", "Alphonse", "Annie", "user2", "user2WithID",
                "user3", "user4"}, null);
        // New role list: admin, Internal/everyone, role1WithID, role3, role4, Manager, HeadManager.

        // groups eq Manager.
        ExpressionCondition groupFiltering = new ExpressionCondition(ExpressionOperation.EQ.toString(),
                ExpressionAttribute.ROLE.toString(), "Manager");
        // group eq HeadManager.
        ExpressionCondition groupFiltering2 = new ExpressionCondition(ExpressionOperation.EQ.toString(),
                ExpressionAttribute.ROLE.toString(), "HeadManager");
        // groups eq Manager and groups eq HeadManager.
        OperationalCondition multiGroupFiltering = new OperationalCondition("AND", groupFiltering, groupFiltering2);

        // Group Filtering forwards cursor pagination.
        assertEquals(3, admin.getUserListWithID(groupFiltering, null, null, 10, "user2", "NEXT", null, null).size());

        // Group Filtering backwards cursor pagination.
        assertEquals(6, admin.getUserListWithID(groupFiltering, null, null, 10, "user3", "PREVIOUS", null, null)
                .size());

        // Multi-group filtering forwards cursor pagination.
        assertEquals(2, admin.getUserListWithID(multiGroupFiltering, null, null, 10, "user2", "NEXT", null, null)
                .size());

        // Multi-group filtering backwards cursor pagination.
        assertEquals(3, admin.getUserListWithID(multiGroupFiltering, null, null, 10, "Annie", "PREVIOUS", null, null)
                .size());

        // groups eq Manager and groups eq HeadManager and userName sw use.
        OperationalCondition groupAndUserNameFilter =
                new OperationalCondition("AND", multiGroupFiltering, expressionCondition);

        // Multi-group filtering and userName filtering forward cursor pagination.
        assertEquals(3, admin.getUserListWithID(groupAndUserNameFilter, null, null, 10, "", "NEXT", null, null).size());

        // Multi-group filtering and userName filtering backwards cursor pagination.
        assertEquals(1, admin.getUserListWithID(groupAndUserNameFilter, null, null, 10, "user2WithID", "PREVIOUS",
                null, null).size());

        // givenname sw A and groups eq Manager.
        OperationalCondition singleClaimAndGroupFilter =
                new OperationalCondition("AND", claimFiltering1, groupFiltering);
        // givenname sw A and groups eq Manager and groups eq HeadManager.
        OperationalCondition singleClaimAndMultiGroupFilter =
                new OperationalCondition("AND", claimFiltering1, multiGroupFiltering);
        // givenname sw A and fullname sw T and groups eq Manager and groups eq HeadManager.
        OperationalCondition multiGroupAndMultiClaimFilter =
                new OperationalCondition("AND", multiClaimFiltering, multiGroupFiltering);

        // Having claims + In the manager and HeadManager groups: Akira, Alphonse, Alucard, Annie.

        // Single group and claim filtering forward cursor pagination.
        assertEquals(3, admin.getUserListWithID(singleClaimAndGroupFilter, null, null, 10, "Akira", "NEXT", null, null)
                .size());

        // Single group and claim filtering backward cursor pagination.
        assertEquals(0, admin.getUserListWithID(singleClaimAndGroupFilter, null, null, 10, "Akira", "PREVIOUS", null,
                        null).size());

        // Multi-group filtering and single claim filtering forward cursor pagination.
        assertEquals(4, admin.getUserListWithID(singleClaimAndMultiGroupFilter, null, null, 10, "", "NEXT", null, null)
                .size());

        // Multi-group filtering and single claim filtering backward cursor pagination.
        assertEquals(3, admin.getUserListWithID(singleClaimAndMultiGroupFilter, null, null, 10, "Annie", "PREVIOUS", null,
                        null).size());

        // Multi-group filtering and multi-claim filtering forward cursor pagination.
        assertEquals(1, admin.getUserListWithID(multiGroupAndMultiClaimFilter, null, null, 10, "Akira", "NEXT",
                null, null).size());

        // Multi-group filtering and multi-claim filtering backward cursor pagination.
        assertEquals(1, admin.getUserListWithID(multiGroupAndMultiClaimFilter, null, null, 10, "Alucard", "PREVIOUS",
                null, null).size());
    }

    public void test195GetRoleListOfUserWithID() throws UserStoreException {

        assertEquals(2, admin.getRoleListOfUserWithID(userId1).size());
    }

    public void test196UpdateRoleListOfUserWithID() throws UserStoreException {

        admin.updateRoleListOfUserWithID(userId1, null, new String[]{"role2"});
        admin.updateRoleListOfUserWithID(userId1, new String[]{"role2"}, new String[]{"role4",
                "role1WithID"});

        List<String> rolesOfUser = admin.getRoleListOfUserWithID(userId1);
        assertEquals(3, rolesOfUser.size());

        admin.updateRoleListOfUserWithID(userId1, new String[]{"role1WithID"}, null);

        List<String> rolesOfUserNew = admin.getRoleListOfUserWithID(userId1);
        assertEquals(2, rolesOfUserNew.size());

        //negative
        try {
            admin.updateRoleListOfUserWithID("user1", new String[]{"role2"}, new String[]{"role4", "no_role1"});
        } catch (UserStoreException e) {
            // Expected
            assertEquals("The role: no_role1 does not exist.", e.getMessage());
        }
    }

    public void test197UpdateUserListOfRoleWithID() throws UserStoreException {

        List<User> usersOfRole = admin.getUserListOfRoleWithID("role1WithID");
        assertEquals(0, usersOfRole.size());

        admin.updateUserListOfRoleWithID("role1WithID", new String[]{userId1}, null);
        admin.updateUserListOfRoleWithID("role1WithID", null, new String[]{userId1, userId2});
        //negative
        try {
            admin.updateUserListOfRole("role1WithID", null, new String[]{"nouser1", "nouser2"});
        } catch (UserStoreException e) {
            // Expected
            assertEquals("User nouser1 does not exit in the system.", e.getMessage());
        }
        List<User> users = admin.getUserListOfRoleWithID("role1WithID");
        assertEquals(2, users.size());

        admin.updateUserListOfRoleWithID("role1WithID", new String[]{userId1}, null);

        List<User> usersNew = admin.getUserListOfRoleWithID("role1WithID");
        assertEquals(1, usersNew.size());
    }

    public void test198IsUserInRoleWithID() throws UserStoreException {

        assertFalse(admin.isUserInRoleWithID(userId1, "role1WithID"));
        assertTrue(admin.isUserInRoleWithID(userId2, "role1WithID"));
    }

    public void test199IsUserExists() throws UserStoreException {

        assertTrue(admin.isExistingUserWithID(userId1));
        assertFalse(admin.isExistingUserWithID("no-user2"));
    }

    public void test200GetUsersClaimValuesWithID() throws UserStoreException {

        String[] allClaims = {
                ClaimTestUtil.CLAIM_URI1, ClaimTestUtil.CLAIM_URI2, ClaimTestUtil.CLAIM_URI3
        };

        List<UniqueIDUserClaimSearchEntry> obtained = admin.getUsersClaimValuesWithID(
                Arrays.stream(new String[] { userId1, userId2 }).collect(Collectors.toList()),
                Arrays.stream(allClaims).collect(Collectors.toList()), null);

        assertEquals(2, obtained.size());
        assertNotNull(obtained.get(0).getClaims().get(ClaimTestUtil.CLAIM_URI1));
        assertNotNull(obtained.get(1).getClaims().get(ClaimTestUtil.CLAIM_URI1));
    }


    public void test201DeleteUserClaimValueWithID() throws UserStoreException {

        assertNotNull(admin.getUserClaimValueWithID(userId1, ClaimTestUtil.CLAIM_URI1, null));
        admin.deleteUserClaimValueWithID(userId1, ClaimTestUtil.CLAIM_URI1, null);
        assertNull(admin.getUserClaimValueWithID(userId1, ClaimTestUtil.CLAIM_URI1, null));
    }

    public void test202DeleteUserClaimValuesWithID() throws UserStoreException {

        assertNotNull(admin.getUserClaimValueWithID(userId2, ClaimTestUtil.CLAIM_URI3, null));
        admin.deleteUserClaimValuesWithID(userId2, new String[]{ClaimTestUtil.CLAIM_URI3}, null);
        assertNull(admin.getUserClaimValueWithID(userId2, ClaimTestUtil.CLAIM_URI3, null));
    }

    public void test203DeleteUserWithID() throws UserStoreException {

        List<String> roles = admin.getRoleListOfUserWithID(userId1);
        for (String role : roles) {
            assertTrue(admin.getUserListOfRoleWithID(role).stream().map(User::getUserID).collect(Collectors.toList()).contains(userId1));
        }
        admin.deleteUserWithID(userId1);
        assertFalse(ArrayUtils.contains(admin.listUsers("*", 100), userId1));

        for (String role : roles) {
            assertFalse(admin.getUserListOfRoleWithID(role).stream().map(User::getUserID).collect(Collectors.toList()).contains(userId1));
        }
    }

    public void test204GetProfileOfNonExistentUser() {

        try {
            admin.getProfileNames("nonExistentUser");
        } catch (Exception e) {
            // Expected exception.
            assertTrue(e instanceof UserStoreException);
            assertEquals("30007 - UserNotFound: User nonExistentUser does not exist in: PRIMARY",
                    e.getMessage());
        }
    }

    public void test205GetDisplayNameOfUser() throws UserStoreException {

        // Create user in User Store.
        User user6WithID = admin.addUserWithID("user6WithID", "pass1",
                null, null, null);
        assertNotNull(user6WithID);

        // Check UserID is not null.
        String userID = user6WithID.getUserID();
        assertNotNull(userID);

        // Set display name attribute in user-store properties.
        Map<String, String> userStoreProperties = admin.getRealmConfiguration().getUserStoreProperties();
        userStoreProperties.put(JDBCUserStoreConstants.DISPLAY_NAME_ATTRIBUTE, "usergivenname2withId");
        admin.getRealmConfiguration().setUserStoreProperties(userStoreProperties);
        String displayNameAttribute = admin.getRealmConfiguration().getUserStoreProperty(JDBCUserStoreConstants.DISPLAY_NAME_ATTRIBUTE);
        assertEquals("usergivenname2withId", displayNameAttribute);

        // Get Claim value for the display name.
        Map<String, String> map = new HashMap<>();
        map.put(ClaimTestUtil.CLAIM_URI1, "usergivenname2withId");
        admin.setUserClaimValuesWithID(userID, map, null);
        String[] displayNameClaim = {ClaimTestUtil.CLAIM_URI1};
        Map<String, String> obtained = admin.getUserClaimValuesWithID(userID, displayNameClaim, null);
        assertEquals("usergivenname2withId", obtained.get(ClaimTestUtil.CLAIM_URI1));

        // Append display name to user name.
        String username = user6WithID.getUsername();
        username = UserCoreUtil.getCombinedName(user6WithID.getUserStoreDomain(), username, obtained.get(ClaimTestUtil.CLAIM_URI1));
        assertEquals("user6WithID$_USERNAME_SEPARATOR_$usergivenname2withId", username);
    }

    private void clearUserIdResolverCache() {

        UserIdResolverCache.getInstance()
                .clear(RESOLVE_USER_ID_FROM_USER_NAME_CACHE_NAME, MultitenantConstants.SUPER_TENANT_ID);
        UserIdResolverCache.getInstance()
                .clear(RESOLVE_USER_NAME_FROM_USER_ID_CACHE_NAME, MultitenantConstants.SUPER_TENANT_ID);
        UserIdResolverCache.getInstance()
                .clear(RESOLVE_USER_UNIQUE_ID_FROM_USER_NAME_CACHE_NAME, MultitenantConstants.SUPER_TENANT_ID);
        UserIdResolverCache.getInstance()
                .clear(RESOLVE_USER_NAME_FROM_UNIQUE_USER_ID_CACHE_NAME, MultitenantConstants.SUPER_TENANT_ID);
    }
}
