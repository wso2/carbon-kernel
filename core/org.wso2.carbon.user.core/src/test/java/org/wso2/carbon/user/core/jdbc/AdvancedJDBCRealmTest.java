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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.common.SampleAbstractUserManagementErrorListener;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.config.TestRealmConfigBuilder;
import org.wso2.carbon.user.core.constants.UserCoreErrorConstants;
import org.wso2.carbon.user.core.internal.UMListenerServiceComponent;
import org.wso2.carbon.user.core.listener.UserManagementErrorEventListener;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class AdvancedJDBCRealmTest extends BaseTestCase {

    private static Log log = LogFactory.getLog(AdvancedJDBCRealmTest.class);
    private UserRealm realm = null;
    private String TEST_URL = "jdbc:h2:./target/advjdbctest/CARBON_TEST";
    private SampleAbstractUserManagementErrorListener sampleAbstractUserManagementErrorListener;

    public void testStuff() throws Exception {
        DatabaseUtil.closeDatabasePoolConnection();
        initRealmStuff();
        doUserStuff();
        doUserRoleStuff();
        doAuthorizationStuff();
        doClaimStuff();
    }

    public void initRealmStuff() throws Exception {

        String dbFolder = "target/advjdbctest";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl(TEST_URL);

        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();

        realm = new DefaultRealm();
        InputStream inStream = this.getClass().getClassLoader().getResource(JDBCRealmTest.JDBC_TEST_USERMGT_XML).openStream();
        RealmConfiguration realmConfig = TestRealmConfigBuilder
                .buildRealmConfigWithJDBCConnectionUrl(inStream, TEST_URL);
        realm.init(realmConfig, ClaimTestUtil.getClaimTestData(), ClaimTestUtil
                .getProfileTestData(), MultitenantConstants.SUPER_TENANT_ID);
        sampleAbstractUserManagementErrorListener = new SampleAbstractUserManagementErrorListener();
        Method method = UMListenerServiceComponent.class.getDeclaredMethod("setUserManagementErrorEventListenerService",
                UserManagementErrorEventListener.class);
        method.setAccessible(true);
        method.invoke(new UMListenerServiceComponent(), sampleAbstractUserManagementErrorListener);
        method.setAccessible(false);
    }

    public void doUserStuff() throws Exception {

        UserStoreManager admin = realm.getUserStoreManager();

        Map<String, String> userProps = new HashMap<String, String>();
        userProps.put(ClaimTestUtil.CLAIM_URI1, "1claim1Value");
        userProps.put(ClaimTestUtil.CLAIM_URI2, "2claim2Value");

        Permission[] permisions = new Permission[2];
        permisions[0] = new Permission("high security", "read");
        permisions[1] = new Permission("low security", "write");


        //add USER
        admin.addUser("dimuthu", "credential", null, null, null, false);
        try {
            admin.addUser(null, null, null, null, null, false);
            TestCase.assertTrue(false);
        } catch (Exception ex) {
            Assert.assertEquals("Relevant listener related with error handling is not called during addUser Failure", 1,
                    sampleAbstractUserManagementErrorListener.getAddUserFailureCount());
            Assert.assertTrue("Error code does not match with the exact errorneous scenario", ex.getMessage()
                    .startsWith(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_INVALID_USER_NAME.getCode()));
            //expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try {
            admin.addUser("dimuthu", null, null, null, null, false);
            TestCase.assertTrue(false);
        } catch (Exception ex) {
            Assert.assertEquals(
                    "Relevant listener related with error handling is not called during addUser " + "Failure", 2,
                    sampleAbstractUserManagementErrorListener.getAddUserFailureCount());
            Assert.assertTrue(
                    "Error code does not match with the exact errorneous scenario, actual message " + ex.getMessage(),
                    ex.getMessage()
                            .startsWith(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_INVALID_PASSWORD.getCode()));
            //expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try {
            admin.addUser(null, "credential", null, null, null, false);
            TestCase.assertTrue(false);
        } catch (Exception ex) {
            //expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try {
            admin.addUser(" ", "credential", null, null, null, false);
            TestCase.assertTrue(false);
        } catch (Exception ex) {
            //expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try {
            admin.addUser("dimuthu", "credential", null, null, null, false);
            fail("Exception at adding the same user again");
        } catch (Exception ex) {
            //expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }


        // add ROLE
        admin.addRole("role1", new String[]{"dimuthu"}, permisions);//dimuthu added to the role
        try {
            admin.addRole(null, null, null);
            fail("Exception at defining a roll with No information");
        } catch (Exception ex) {
            Assert.assertEquals("Relevant method in event listener is not called when there is a failure while "
                    + "adding the role", 1, sampleAbstractUserManagementErrorListener.getAddRoleFailureCount());
            Assert.assertTrue("Error code does not match with the exact errorneous scenario, actual message is " + ex
                    .getMessage(), ex.getMessage()
                    .startsWith(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_CANNOT_ADD_EMPTY_ROLE.getCode()));
            //expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try {
            admin.addRole(null, new String[]{"dimuthu"}, permisions);
            fail("Exception at adding user to a non specified role");
        } catch (Exception ex) {
            //expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try {
            admin.addRole("role1", new String[]{"isuru"}, permisions);
            fail("Exception at adding a non existing user to the role");
        } catch (Exception ex) {
            //expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }


        //add USER to a ROLE
        admin.addUser("vajira", "credential", new String[]{"role1"}, userProps, null, false);
        try {
            admin.addUser("Bence", "credential", new String[]{"rolexxx"}, userProps, null, false);
            fail("Exception at adding user to a Non-existing role");
        } catch (Exception ex) {
            //expected user
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try {
            admin.addUser(null, "credential", new String[]{"role1"}, userProps, null, false);
            fail("Exception at adding user to a role with no user name");
        } catch (Exception ex) {
            //expected user
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try {
            admin.addUser("vajira", "credential", new String[]{"role1"}, userProps, null, false);
            fail("Exception at adding same user to the same roll");
        } catch (Exception ex) {
            //expected user
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }


        //Authenticate USER
        assertTrue(admin.authenticate("dimuthu", "credential"));
        assertFalse(admin.authenticate(null, "credential"));
        Assert.assertEquals("Relevant event listener is not called, when there is an authentication failure", 1,
                sampleAbstractUserManagementErrorListener.getAuthenticationFailureCount());
        assertFalse(admin.authenticate("dimuthu", null));

        //update by ADMIN
        admin.updateCredentialByAdmin("dimuthu", "topsecret");
        assertTrue(admin.authenticate("dimuthu", "topsecret"));

        // Test whether relevant event listeners related with faulty scenario is called, in the event of failure.
        try {
            admin.updateCredentialByAdmin("dimuthu", new SampleAbstractUserManagementErrorListener());
            fail("Update Credential By Admin succeeded even for an invalid format of credentials");
        } catch (UserStoreException ex) {
            Assert.assertEquals("Relevant event listener related with ,update credential by admin was not called for"
                            + " a failure scenario", 1,
                    sampleAbstractUserManagementErrorListener.getUpdateCredentialByAdminFailureCount());
        }

        //isExistingUser
        assertTrue(admin.isExistingUser("dimuthu"));
        assertFalse(admin.isExistingUser("muhaha"));


        // update by USER
        admin.updateCredential("dimuthu", "password", "topsecret");
        //assertTrue(admin.authenticate("dimuthu", "password")); //TO DO
        assertFalse(admin.authenticate("dimuthu", "credential"));
        try {
            admin.updateCredential("dimuthu", "password", "xxx");
            TestCase.assertTrue(false);
        } catch (Exception ex) {
            Assert.assertEquals("Relevant event listener related with ,update credential by admin was not called for"
                            + " a failure scenario", 1,
                    sampleAbstractUserManagementErrorListener.getUpdateCredentialFailureCount());
            Assert.assertTrue(
                    "Error message does not match with the exact cause for the error. Actual " + ex.getMessage(),
                    ex.getMessage().startsWith(
                            UserCoreErrorConstants.ErrorMessages.ERROR_CODE_OLD_CREDENTIAL_DOES_NOT_MATCH.getCode()));
            //expected exception
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }

        String[] names = admin.listUsers("*", 100);
        assertEquals(3, names.length);

        String[] names1 = admin.listUsers("*", 0);
        assertEquals(0, names1.length);

        String[] names2 = admin.listUsers("*", 2);
        assertEquals(2, names2.length);

        String[] names3 = admin.listUsers("di?uthu", 100);
        assertEquals(1, names3.length);

        String[] names4 = admin.listUsers("is?ru", 100);
        assertEquals(0, names4.length);

        // Test getUserList errorneous testing.
        try {
            admin.getUserList(null, null, null);
        } catch (UserStoreException ex) {
            Assert.assertEquals(
                    "Relevant error listeners were not called during a failre while trying get user " + "list ", 1,
                    sampleAbstractUserManagementErrorListener.getGetUserListFailureCount());
        }

        String[] roleNames = admin.getRoleNames();
        assertEquals(3, roleNames.length);


        // delete
        admin.deleteUser("vajira");
        assertFalse(admin.isExistingUser("vajira"));
        assertFalse(admin.authenticate("vajira", "credential"));

        // Try to delete the non-existing user, to check the errorneous scenario.
        try {
            admin.deleteUser("non-existing user");
            Assert.fail("When trying to delete a non-existing user, exception is not thrown");
        } catch (UserStoreException e) {
            Assert.assertEquals("Relevant error listener is not called during a errorneous scenario ", 1,
                    sampleAbstractUserManagementErrorListener.getDeleteUserFailureCount());
            Assert.assertTrue(
                    "Error message does not match with exact errorneous scenario, actual error, " + e.getMessage(),
                    e.getMessage()
                            .startsWith(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode()));
        }

        //delete ROLE
        admin.addUser("vajira", "credential", new String[]{"role1"}, userProps, null, false);
        assertTrue(admin.isExistingUser("vajira"));
        admin.deleteRole("role1");


        //add role
        admin.addRole("role1", new String[]{"dimuthu"}, permisions);

    }

    public void doUserRoleStuff() throws Exception {
        UserStoreManager admin = realm.getUserStoreManager();

        InputStream inStream = this.getClass().getClassLoader().getResource(JDBCRealmTest.
                JDBC_TEST_USERMGT_XML).openStream();
        RealmConfigXMLProcessor realmConfigProcessor = new RealmConfigXMLProcessor();
        RealmConfiguration realmConfig = realmConfigProcessor.buildRealmConfiguration(inStream);

        admin.addRole("role2", null, null);
        admin.addRole("role3", null, null);
        admin.addRole("role4", null, null);
        assertEquals(6, admin.getRoleNames().length);//admin,everyone,role1,role2,role3,role4

        //Test delete role method
        assertTrue(admin.isExistingRole("role3"));
        admin.deleteRole("role3");
        admin.deleteRole("role4");
        assertFalse(admin.isExistingRole("role3"));
        admin.addRole("role3", null, null);
        admin.addRole("role4", null, null);
        admin.addRole("roleShared", null, null);

        // Test errorneous scenario when deleting a role.
        try {
            admin.deleteRole("Internal/everyone");
        } catch (UserStoreException ex) {
            Assert.assertEquals("Relevant event listener is not called during a failure scenario while trying to "
                            + "delete Internal/Everyone role", 1,
                    sampleAbstractUserManagementErrorListener.getDeleteRoleFailureCount());
        }

        //add users
        admin.addUser("saman", "pass1", null, null, null, false);
        admin.addUser("amara", "pass2", null, null, null, false);
        admin.addUser("sunil", "pass3", null, null, null, false);

        //update the ROLE list of USERS
        admin.updateRoleListOfUser("saman", null, new String[]{"role2"});
        admin.updateRoleListOfUser("saman", new String[]{"role2"}, new String[]{"role4",
                "role3"});
        try {
            admin.updateRoleListOfUser("saman", new String[]{"role2"}, new String[]{"role4",
                    "role3"});
        } catch (UserStoreException e) {
            fail("Cannot assign same role to user again.");
        }

        try {
            admin.updateRoleListOfUser(null, null, new String[]{"role2"});
            fail("Exceptions at missing user name");
        } catch (Exception ex) {
            //expected user
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }

        // Renaming Role
        admin.updateRoleName("role4", "role5");

        try {
            // updating to invalid role name
            admin.updateRoleName("roleShared", "role@12#$");
            if ("true".equalsIgnoreCase(realmConfig.getUserStoreProperty("SharedGroupEnabled"))) {
                fail("Able to rename role with invalid characters");
            }
        } catch (UserStoreException e) {
            Assert.assertEquals(
                    "Relevant event listener was not called when there is a failure while updating " + "role name ", 1,
                    sampleAbstractUserManagementErrorListener.getUpdateRoleNameFailureCount());
            Assert.assertTrue(
                    "Error message does not match with actual error scenario, actual message : " + e.getMessage(),
                    e.getMessage()
                            .startsWith(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getCode()));
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }

        String[] rolesOfSaman = admin.getRoleListOfUser("saman");
        assertEquals(3, rolesOfSaman.length);

        // according to new implementation, getRoleListOfUser method would return everyone role name for all users
        boolean userExist = admin.isExistingUser("isuru");
        if (userExist) {
            TestCase.assertTrue(false);
        } else {
            String[] rolesOfisuru = admin.getRoleListOfUser("isuru");
            assertEquals(1, rolesOfisuru.length);
            assertEquals(admin.getRealmConfiguration().getEveryOneRoleName(), rolesOfisuru[0]);
        }

        admin.updateUserListOfRole("role2", new String[]{"saman"}, null);
        admin.updateUserListOfRole("role3", null, new String[]{"amara", "sunil"});

        String[] userOfRole5 = admin.getUserListOfRole("role5");
        assertEquals(1, userOfRole5.length);

        String[] userOfRole4 = admin.getUserListOfRole("role4");
        assertEquals(0, userOfRole4.length);

        try {
            admin.updateUserListOfRole("rolexx", null, new String[]{"amara", "sunil"});
            TestCase.assertTrue(false);
        } catch (Exception e) {
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }
        try {
            admin.updateUserListOfRole("role2", null, new String[]{"d"});
            TestCase.assertTrue(false);
        } catch (Exception e) {
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }

        try {
            admin.updateRoleListOfUser("saman", new String[]{"x"}, new String[]{"y"});
            TestCase.assertTrue(false);
        } catch (Exception e) {
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }

        try {
            admin.updateUserListOfRole(realmConfig.getAdminRoleName(), null,
                    new String[]{realmConfig.getAdminUserName()});
            TestCase.assertTrue(false);
        } catch (Exception e) {
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }

        int count = sampleAbstractUserManagementErrorListener.getUpdateRoleListOfUserFailureCount();
        try {
            admin.updateRoleListOfUser(realmConfig.getAdminUserName(), new String[]{realmConfig.
                    getAdminRoleName()}, null);
            TestCase.assertTrue(false);
        } catch (Exception e) {
            Assert.assertEquals(
                    "Relevant error listeners are not called during a failure while updating role list of a user",
                    count + 1, sampleAbstractUserManagementErrorListener.getUpdateRoleListOfUserFailureCount());
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }

        count = sampleAbstractUserManagementErrorListener.getUpdateUserRoleListFailureCount();
        try {
            admin.updateUserListOfRole(realmConfig.getEveryOneRoleName(), new String[]{"saman"},
                    null);
            TestCase.assertTrue(false);
        } catch (Exception e) {
            Assert.assertEquals(
                    "Relevant error listeners are not called during a failure while updating user list of role",
                    count + 1, sampleAbstractUserManagementErrorListener.getUpdateUserRoleListFailureCount());
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }

        try {
            admin.updateRoleListOfUser("sunil", new String[]{realmConfig.getEveryOneRoleName()},
                    null);
            TestCase.assertTrue(false);
        } catch (Exception e) {
            // expected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }

        try {
            admin.updateRoleName("role2", "role5");
            TestCase.assertTrue(false);
        } catch (Exception e) {
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }

    }

    public void doAuthorizationStuff() throws Exception {
        AuthorizationManager authMan = realm.getAuthorizationManager();
        UserStoreManager usWriter = realm.getUserStoreManager();

        usWriter.addRole("rolex", new String[]{"saman", "amara"}, null);
        usWriter.addRole("roley", null, null);
        authMan.authorizeRole("rolex", "wall", "write");
        authMan.authorizeRole("roley", "table", "write");

        try {
            authMan.authorizeRole(null, "wall", "write");
            fail("Exception at authorizing a role with Null role");
        } catch (Exception e) {
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }
        try {
            authMan.authorizeRole("rollee", null, "write");
            fail("Exception at authorizing a role with Null resourceID");
        } catch (Exception e) {
            // exptected error in negative testing
        }
        try {
            authMan.authorizeRole("rollee", "wall", null);
            fail("Exception at authorizing a role with Null action");
        } catch (Exception e) {
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }
        try {
            authMan.authorizeRole("rolleex", "wall", "run");
            fail("Exception at authorizing a role with Invalid action");
        } catch (Exception e) {
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }

        //***authorize user
        authMan.authorizeUser("sunil", "wall", "read");
        try {
            authMan.authorizeUser(null, "wall", "read");
            fail("Exception at authorizing a user with Null name");
        } catch (Exception e) {
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }
        try {
            authMan.authorizeUser("isuru", null, "read");
            fail("Exception at authorizing a user with Null resourceID");
        } catch (Exception e) {
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }
        try {
            authMan.authorizeUser("isuru", "wall", null);
            fail("Exception at authorizing a user with Null action");
        } catch (Exception e) {
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }
        try {
            authMan.authorizeUser("isuru", "wall", "run");
            fail("Exception at authorizing a user with Invalid action");
        } catch (Exception e) {
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }

        assertTrue(authMan.isUserAuthorized("saman", "wall", "write"));
        assertTrue(authMan.isUserAuthorized("sunil", "wall", "read"));
        assertTrue(authMan.isRoleAuthorized("roley", "table", "write"));
        assertFalse(authMan.isRoleAuthorized("roley", "chair", "write"));
        assertFalse(authMan.isUserAuthorized("saman", "wall", "read"));
        assertFalse(authMan.isUserAuthorized("sunil", "wall", "write"));
        assertFalse(authMan.isUserAuthorized("isuru", "wall", "write"));
        try {
            boolean b = authMan.isUserAuthorized("isuru", "wall", "run");
            fail("Exception at check authorization of a user with Invalid action");
        } catch (Exception e) {
            // exptected error in negative testing
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }

        String[] AllowedRolesForResource = authMan.getAllowedRolesForResource("wall", "write");
        assertEquals(1, AllowedRolesForResource.length);
        //assertEquals(2,authMan.getAllowedUsersForResource("wall", "write").length);
        //String[] AllowedUsersForResource = authMan.getAllowedUsersForResource("wall", "read");
        //assertEquals(1, AllowedUsersForResource.length);

        authMan.clearUserAuthorization("sunil", "wall", "read");
        try {
            authMan.clearUserAuthorization("isuru", "wall", "run");
            fail("Exception at clear user authorization");
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }
        try {
            authMan.clearUserAuthorization(null, "wall", "run");
            fail("Exception at clear user authorization");
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }

        }
        try {
            authMan.clearUserAuthorization("isuru", null, "run");
            fail("Exception at clear user authorization");
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }

        }
        try {
            authMan.clearUserAuthorization("isuru", "wall", null);
            fail("Exception at clear user authorization");
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }

        }

        authMan.clearRoleAuthorization("roley", "table", "write");
        try {
            authMan.clearRoleAuthorization(null, "table", "write");
            fail("Exception at clear role authorization");
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }

        }
        try {
            authMan.clearRoleAuthorization("roleee", null, "write");
            fail("Exception at clear role authorization");
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }

        }
        try {
            authMan.clearRoleAuthorization("roleee", "table", null);
            fail("Exception at clear role authorization");
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }

        }

        authMan.clearResourceAuthorizations("wall");
        try {
            authMan.clearResourceAuthorizations(null);
            fail("Exception at clear Resource Authorizations");
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }

        }

        assertFalse(authMan.isUserAuthorized("saman", "wall", "write"));
        assertFalse(authMan.isUserAuthorized("sunil", "wall", "read"));
        assertFalse(authMan.isRoleAuthorized("roley", "table", "write"));
    }

    public void doClaimStuff() throws Exception {
        UserStoreManager usWriter = realm.getUserStoreManager();
        String[] allClaims = {ClaimTestUtil.CLAIM_URI1, ClaimTestUtil.CLAIM_URI2,
                ClaimTestUtil.CLAIM_URI3};

        // add DEFAULT
        usWriter.setUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, "claim1default", null);
        try {
            usWriter.setUserClaimValue(null, ClaimTestUtil.CLAIM_URI1, "claim1default", null);
            fail("Exception at set claim values to null users");
        } catch (Exception e) {
            //expected exception
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }
        try {
            usWriter.setUserClaimValue("isuru", null, "claim1default", null);
            fail("Exception at set claim values to null claimURI");
        } catch (Exception e) {
            Assert.assertEquals("Relevant event listeners are not called during a failure of setUserClaim Value", 1,
                    sampleAbstractUserManagementErrorListener.getSetUserClaimValueFailureCount());
            Assert.assertTrue("Relevant exception is not thrown, actual message, " + e.getMessage(), e.getMessage()
                    .startsWith(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode()));
            //expected exception
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }
        try {
            usWriter.setUserClaimValue("isuru", ClaimTestUtil.CLAIM_URI1, null, null);
            fail("Exception at set claim values to null claimValue");
        } catch (Exception e) {
            //expected exception
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }

        String value = usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, null);
        assertEquals("claim1default", value);
        //Non existing user
        try {
            usWriter.getUserClaimValue("isuru", ClaimTestUtil.CLAIM_URI1, null);
        } catch (UserStoreException e) {
            Assert.assertEquals("Relevant event listener was not called when there is a failure while getting the "
                            + "user claim value", 1,
                    sampleAbstractUserManagementErrorListener.getGetUserClaimValueFailureCount());
            // contains the 'UserNotFound' error code in the error.
            assertTrue(e.getMessage().contains("UserNotFound"));
        }

        // Test for a non-existing user.
        try {
            usWriter.getUserClaimValues("isuru", null);
        } catch (UserStoreException e) {
            Assert.assertEquals("Relevant event listener was not called when there is a failure while getting the "
                            + "user claim values", 1,
                    sampleAbstractUserManagementErrorListener.getGetUserClaimValuesFailureCount());
        }

        // update default
        usWriter.setUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, "dimzi lee", null);
        value = usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, null);
        assertEquals("dimzi lee", value);

        // multiple additions
        Map<String, String> map = new HashMap<String, String>();
        map.put(ClaimTestUtil.CLAIM_URI1, "lee");
        map.put(ClaimTestUtil.CLAIM_URI3, "muthu");

        usWriter.setUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI2, "claim2default", null);
        assertEquals("dimzi lee", usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, null));
        assertEquals("claim2default", usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI2, null));
        assertNull(usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI3, null));

        usWriter.setUserClaimValues("dimuthu", map, ClaimTestUtil.HOME_PROFILE_NAME);
        assertEquals("lee", usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, ClaimTestUtil.HOME_PROFILE_NAME));
        assertNull(usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI2, ClaimTestUtil.HOME_PROFILE_NAME));
        assertEquals("muthu", usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI3, ClaimTestUtil.HOME_PROFILE_NAME));

        usWriter.setUserClaimValue("dimuthu", UserCoreConstants.PROFILE_CONFIGURATION,
                ClaimTestUtil.HOME_PROFILE_NAME, ClaimTestUtil.HOME_PROFILE_NAME);
        Map<String, String> obtained = usWriter.getUserClaimValues("dimuthu", allClaims,
                ClaimTestUtil.HOME_PROFILE_NAME);
        //assertNull(obtained.get(ClaimTestUtil.CLAIM_URI1)); // hidden
        //assertEquals("claim2default", obtained.get(ClaimTestUtil.CLAIM_URI2)); // overridden
        assertEquals("muthu", obtained.get(ClaimTestUtil.CLAIM_URI3)); // normal

        //UPDATE
        map.put(ClaimTestUtil.CLAIM_URI3, "muthulee");
        usWriter.setUserClaimValues("dimuthu", map, ClaimTestUtil.HOME_PROFILE_NAME);
        value = usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI3,
                ClaimTestUtil.HOME_PROFILE_NAME);
        assertEquals("muthulee", value);

        // Try failure scenario to make sure relevant event listener method is called during a failure.
        try {
            usWriter.setUserClaimValues("non-existing-user", map, ClaimTestUtil.HOME_PROFILE_NAME);
        } catch (UserStoreException ex) {
            Assert.assertEquals(
                    "Relevant event listener is not called during a failure while setting user claim values", 1,
                    sampleAbstractUserManagementErrorListener.getSetUserClaimValuesFailureCount());
            Assert.assertTrue("Relevant error code does not match with the current errorneous scenario. Actual "
                    + "error message " + ex.getMessage(), ex.getMessage()
                    .startsWith(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode()));
        }

        //DELETE
        usWriter.deleteUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, null);
        value = usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, null);
        assertNull(value);
        try {
            usWriter.deleteUserClaimValue("dimuthu", null, null);
            fail("Exception at null Claim URI");
        } catch (Exception e) {
            Assert.assertEquals("Relevant listener method is not called when there is a failure while deleting "
                            + "User Claim value", 1,
                    sampleAbstractUserManagementErrorListener.getDeleteUserClaimValueFailureCount());
            //expected exception
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }
        try {
            usWriter.deleteUserClaimValue(null, ClaimTestUtil.CLAIM_URI1, null);
            fail("Exception at giving null user name to delete user claim values");
        } catch (Exception e) {
            //expected exception
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", e);
            }
        }

        usWriter.deleteUserClaimValues("dimuthu", allClaims, ClaimTestUtil.HOME_PROFILE_NAME);

        // Try to delete a claim values of a non-existing user.
        try {
            usWriter.deleteUserClaimValues("non-existing-user", allClaims, ClaimTestUtil.HOME_PROFILE_NAME);
            fail("Exception is not thrown for a invalid delete user claim values request");
        } catch (UserStoreException ex) {
            Assert.assertEquals("Relevant error listeners are not called during a failure scenario while trying to "
                            + "delete user claim values", 1,
                    sampleAbstractUserManagementErrorListener.getDeleteUserClaimValuesFailureCount());
            Assert.assertTrue("Relevant exception is not thrown with the correct error message, actual message, " + ex
                    .getMessage(), ex.getMessage()
                    .startsWith(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode()));
        }
        obtained = usWriter.getUserClaimValues("dimuthu", allClaims,
                ClaimTestUtil.HOME_PROFILE_NAME);
        assertNull(obtained.get(ClaimTestUtil.CLAIM_URI2)); // overridden
        assertNull(obtained.get(ClaimTestUtil.CLAIM_URI3));

        //UserStoreManager admin = realm.getUserStoreManager();
        //admin.deleteUser("dimuthu");
    }


}

