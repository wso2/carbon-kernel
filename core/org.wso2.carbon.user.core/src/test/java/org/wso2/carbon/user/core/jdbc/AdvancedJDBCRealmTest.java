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
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.config.TestRealmConfigBuilder;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;


public class AdvancedJDBCRealmTest extends BaseTestCase {

    private static Log log = LogFactory.getLog(AdvancedJDBCRealmTest.class);
    private UserRealm realm = null;
    private String TEST_URL = "jdbc:h2:target/advjdbctest/CARBON_TEST";

    public void testStuff() throws Exception {
        DatabaseUtil.closeDatabasePoolConnection();
    	initRealmStuff();
        doUserStuff();
        doUserRoleStuff();
        doAuthorizationStuff();
        doClaimStuff();
    }

    public void initRealmStuff() throws Exception {

        String dbFolder =  "target/advjdbctest";
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
        try{
        	admin.addUser(null, null, null, null, null, false);
        	TestCase.assertTrue(false);
        }catch(Exception ex){
        	//expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try{
        	admin.addUser("dimuthu", null, null, null, null, false);
        	TestCase.assertTrue(false);
        }catch(Exception ex){
        	//expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try{
        	admin.addUser(null, "credential", null, null, null, false);
        	TestCase.assertTrue(false);
        }catch(Exception ex){
        	//expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try{
        	admin.addUser(" ", "credential", null, null, null, false);
        	TestCase.assertTrue(false);
        }catch(Exception ex){
        	//expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try{
        	admin.addUser("dimuthu", "credential", null, null, null, false);
        	fail("Exception at adding the same user again");
        }catch(Exception ex){
        	//expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }


        // add ROLE
        admin.addRole("role1", new String[] { "dimuthu" }, permisions);//dimuthu added to the role
        try{
        	admin.addRole(null, null, null);
        	fail("Exception at defining a roll with No information");
        }catch(Exception ex){
        	//expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try{
        	admin.addRole(null, new String[] { "dimuthu" }, permisions);
            fail("Exception at adding user to a non specified role");
        }catch(Exception ex){
        	//expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try{
        	admin.addRole("role1", new String[] { "isuru" }, permisions);
        	fail("Exception at adding a non existing user to the role");
        }catch(Exception ex){                                      
        	//expected error
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }


        //add USER to a ROLE
        admin.addUser("vajira", "credential", new String[] { "role1" }, userProps, null, false);
        try{
        	admin.addUser("Bence", "credential", new String[] { "rolexxx" }, userProps, null, false);
        	fail("Exception at adding user to a Non-existing role");
        }catch(Exception ex){
        	//expected user
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try{
        	admin.addUser(null, "credential", new String[] { "role1" }, userProps, null, false);
        	fail("Exception at adding user to a role with no user name");
        }catch(Exception ex){
        	//expected user
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }
        try{
        	admin.addUser("vajira", "credential", new String[] { "role1" }, userProps, null, false);
        	fail("Exception at adding same user to the same roll");
        }catch(Exception ex){
        	//expected user
            if (log.isDebugEnabled()) {
                log.debug("Expected error, hence ignored", ex);
            }
        }

        
        //Authenticate USER
        assertTrue(admin.authenticate("dimuthu", "credential"));
        assertFalse(admin.authenticate(null, "credential"));
        assertFalse(admin.authenticate("dimuthu",null));

        //update by ADMIN
        admin.updateCredentialByAdmin("dimuthu", "topsecret");
        assertTrue(admin.authenticate("dimuthu", "topsecret"));

        //isExistingUser
        assertTrue(admin.isExistingUser("dimuthu"));
        assertFalse(admin.isExistingUser("muhaha"));

        
        // update by USER
        admin.updateCredential("dimuthu", "password", "topsecret");
        //assertTrue(admin.authenticate("dimuthu", "password")); //TO DO
        assertFalse(admin.authenticate("dimuthu", "credential"));
        try{
        	admin.updateCredential("dimuthu", "password", "xxx");
        	TestCase.assertTrue(false);
        }catch(Exception ex){
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
       
        String[] roleNames = admin.getRoleNames();
        assertEquals(3, roleNames.length);


        // delete
        admin.deleteUser("vajira");
        assertFalse(admin.isExistingUser("vajira"));
        assertFalse(admin.authenticate("vajira", "credential"));


        //delete ROLE
        admin.addUser("vajira", "credential", new String[] { "role1" }, userProps, null, false);
        assertTrue(admin.isExistingUser("vajira"));
        admin.deleteRole("role1");

        
        //add role
        admin.addRole("role1", new String[] { "dimuthu" }, permisions);
        
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


           //add users
           admin.addUser("saman", "pass1", null, null, null, false);
           admin.addUser("amara", "pass2", null, null, null, false);
           admin.addUser("sunil", "pass3", null, null, null, false);

           //update the ROLE list of USERS
           admin.updateRoleListOfUser("saman", null, new String[] { "role2" });
           admin.updateRoleListOfUser("saman", new String[] { "role2" }, new String[] { "role4",
                   "role3" });
            try{
               admin.updateRoleListOfUser(null, null, new String[] { "role2" });
               fail("Exceptions at missing user name");
            }catch(Exception ex){
                    //expected user
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", ex);
                }
            }

           // Renaming Role
           admin.updateRoleName("role4", "role5");
           

           String[] rolesOfSaman = admin.getRoleListOfUser("saman");
           assertEquals(3, rolesOfSaman.length);

           // according to new implementation, getRoleListOfUser method would return everyone role name for all users
           boolean  userExist = admin.isExistingUser("isuru");
           if(userExist){
               TestCase.assertTrue(false);
           } else {
               String[] rolesOfisuru = admin.getRoleListOfUser("isuru");
               assertEquals(1, rolesOfisuru.length);
               assertEquals(admin.getRealmConfiguration().getEveryOneRoleName(), rolesOfisuru[0]);
           }

           admin.updateUserListOfRole("role2", new String[] { "saman" }, null);
           admin.updateUserListOfRole("role3", null, new String[] { "amara", "sunil" });

           String[] userOfRole5 = admin.getUserListOfRole("role5");
           assertEquals(1, userOfRole5.length);

           String[] userOfRole4 = admin.getUserListOfRole("role4");
           assertEquals(0, userOfRole4.length);

           try {
            admin.updateUserListOfRole("rolexx", null, new String[] { "amara", "sunil" });
            TestCase.assertTrue(false);
           } catch (Exception e) {
            // exptected error in negative testing
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }
           }
           try {
             admin.updateUserListOfRole("role2", null, new String[] { "d" });
             TestCase.assertTrue(false);
           } catch (Exception e) {
            // exptected error in negative testing
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }
           }

           try {
               admin.updateRoleListOfUser("saman", new String[] { "x" }, new String[] { "y" });
               TestCase.assertTrue(false);
           } catch (Exception e) {
               // exptected error in negative testing
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }
           }

           try {
               admin.updateUserListOfRole (realmConfig.getAdminRoleName(),null,
                                           new String[] {realmConfig.getAdminUserName()});
               TestCase.assertTrue(false);
           } catch (Exception e) {
               // exptected error in negative testing
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }
           }

           try {
               admin.updateRoleListOfUser(realmConfig.getAdminUserName(),new String[]{realmConfig.
                                          getAdminRoleName()},null);
               TestCase.assertTrue(false);
           } catch (Exception e) {
               // exptected error in negative testing
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }
           }

           try {
               admin.updateUserListOfRole(realmConfig.getEveryOneRoleName(),new String[] {"saman"},
                                          null);
               TestCase.assertTrue(false);
           } catch (Exception e) {
               // exptected error in negative testing
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }
           }

           try {
               admin.updateRoleListOfUser("sunil",new String[]{realmConfig.getEveryOneRoleName()},
                                          null);
               TestCase.assertTrue(false);
           } catch (Exception e) {
               // exptected error in negative testing
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

        usWriter.addRole("rolex", new String[] { "saman", "amara" }, null);
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
          authMan.authorizeRole("rollee","wall",null);
          fail("Exception at authorizing a role with Null action");
        } catch (Exception e) {
          // exptected error in negative testing
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }
        }
        try {
          authMan.authorizeRole("rolleex","wall","run");
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
          authMan.authorizeUser("isuru","wall",null);
          fail("Exception at authorizing a user with Null action");
        } catch (Exception e) {
          // exptected error in negative testing
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }
        }
        try {
          authMan.authorizeUser("isuru","wall","run");
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
          boolean b=authMan.isUserAuthorized("isuru", "wall", "run");
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
        try{
            authMan.clearUserAuthorization("isuru", "wall", "run");
            fail("Exception at clear user authorization");
        }catch(Exception e){
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }
        }
        try{
            authMan.clearUserAuthorization(null, "wall", "run");
            fail("Exception at clear user authorization");
        }catch(Exception e){
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }

        }
        try{
            authMan.clearUserAuthorization("isuru", null, "run");
            fail("Exception at clear user authorization");
        }catch(Exception e){
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }

        }
        try{
            authMan.clearUserAuthorization("isuru","wall", null);
            fail("Exception at clear user authorization");
        }catch(Exception e){
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }

        }

        authMan.clearRoleAuthorization("roley", "table", "write");
         try{
            authMan.clearRoleAuthorization(null, "table", "write");
            fail("Exception at clear role authorization");
        }catch(Exception e){
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }

        }
        try{
            authMan.clearRoleAuthorization("roleee", null, "write");
            fail("Exception at clear role authorization");
        }catch(Exception e){
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }

        }
        try{
            authMan.clearRoleAuthorization("roleee", "table", null);
            fail("Exception at clear role authorization");
        }catch(Exception e){
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }

        }
        
        authMan.clearResourceAuthorizations("wall");
        try{
            authMan.clearResourceAuthorizations(null);
            fail("Exception at clear Resource Authorizations");
        }catch(Exception e){
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
        String[] allClaims = { ClaimTestUtil.CLAIM_URI1, ClaimTestUtil.CLAIM_URI2,
                ClaimTestUtil.CLAIM_URI3 };

        // add DEFAULT
        usWriter.setUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, "claim1default", null);
        try{
            usWriter.setUserClaimValue(null, ClaimTestUtil.CLAIM_URI1, "claim1default", null);
            fail("Exception at set claim values to null users");
        }catch(Exception e){
            //expected exception
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }
        }
        try{
            usWriter.setUserClaimValue("isuru", null, "claim1default", null);
            fail("Exception at set claim values to null claimURI");
        }catch(Exception e){
            //expected exception
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }
        }
        try{
            usWriter.setUserClaimValue("isuru", ClaimTestUtil.CLAIM_URI1, null, null);
            fail("Exception at set claim values to null claimValue");
        }catch(Exception e){
            //expected exception
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }
        }

        String value = usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, null);
        assertEquals("claim1default",value);
        //Non existing user
        try {
            String value1 = usWriter.getUserClaimValue("isuru", ClaimTestUtil.CLAIM_URI1, null);
        } catch ( UserStoreException e ) {
            // contains the 'UserNotFound' error code in the error.
            assertTrue(e.getMessage().contains("UserNotFound"));
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
        assertEquals("dimzi lee",usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1,null));
        assertEquals("claim2default",usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI2,null));
        assertNull(usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI3,null));

        usWriter.setUserClaimValues("dimuthu", map, ClaimTestUtil.HOME_PROFILE_NAME);
        assertEquals("lee",usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1,ClaimTestUtil.HOME_PROFILE_NAME));
        assertNull(usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI2,ClaimTestUtil.HOME_PROFILE_NAME));
        assertEquals("muthu",usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI3,ClaimTestUtil.HOME_PROFILE_NAME));

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

        //DELETE
        usWriter.deleteUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, null);
        value = usWriter.getUserClaimValue("dimuthu", ClaimTestUtil.CLAIM_URI1, null);
        assertNull(value);
        try{
             usWriter.deleteUserClaimValue("dimuthu", null, null);
             fail("Exception at null Claim URI");
        }catch(Exception e){
            //expected exception
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }
        }
        try{
            usWriter.deleteUserClaimValue(null,ClaimTestUtil.CLAIM_URI1, null);
            fail("Exception at giving null user name to delete user claim values");
        }catch(Exception e){
            //expected exception
                if (log.isDebugEnabled()) {
                    log.debug("Expected error, hence ignored", e);
                }
        }

        usWriter.deleteUserClaimValues("dimuthu", allClaims, ClaimTestUtil.HOME_PROFILE_NAME);
        obtained = usWriter.getUserClaimValues("dimuthu", allClaims,
                ClaimTestUtil.HOME_PROFILE_NAME);
        assertNull(obtained.get(ClaimTestUtil.CLAIM_URI2)); // overridden
        assertNull(obtained.get(ClaimTestUtil.CLAIM_URI3));

         //UserStoreManager admin = realm.getUserStoreManager();
         //admin.deleteUser("dimuthu");
    }

  
}
   
