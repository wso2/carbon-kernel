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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.common.LoginIdentifier;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.config.TestRealmConfigBuilder;
import org.wso2.carbon.user.core.model.ExpressionAttribute;
import org.wso2.carbon.user.core.model.ExpressionCondition;
import org.wso2.carbon.user.core.model.ExpressionOperation;
import org.wso2.carbon.user.core.model.UniqueIDUserClaimSearchEntry;
import org.wso2.carbon.user.core.model.UserClaimSearchEntry;
import org.wso2.carbon.user.core.util.DatabaseUtil;
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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UniqueIDJDBCRealmPrimaryUserStoreTest extends BaseTestCase {

    public static final String JDBC_TEST_USERMGT_XML = "user-mgt-test-uniqueId.xml";

    private static String TEST_URL = "jdbc:h2:./target/BasicUniqueIDJDBCDatabaseTest/CARBON_TEST";
    private AbstractUserStoreManager admin = null;
    private static String userId1;
    private static String userId2;

    public void setUp() throws Exception {

        super.setUp();
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
                null, "pass1").getAuthenticationStatus());
        assertEquals(AuthenticationResult.AuthenticationStatus.SUCCESS, admin.authenticateWithID(userId2,
                null, "pass2").getAuthenticationStatus());
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
                null, "pass1").getAuthenticationStatus());
        assertEquals(AuthenticationResult.AuthenticationStatus.SUCCESS, admin.authenticateWithID(userId1,
                null, "pass11").getAuthenticationStatus());
    }

    public void test188UpdateCredentialByAdminWithID() throws UserStoreException {

        admin.updateCredentialByAdminWithID(userId1, "pass22");
        assertEquals(AuthenticationResult.AuthenticationStatus.FAIL, admin.authenticateWithID(userId1,
                null, "pass11").getAuthenticationStatus());
        assertEquals(AuthenticationResult.AuthenticationStatus.SUCCESS, admin.authenticateWithID(userId1,
                null, "pass22").getAuthenticationStatus());
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
}
