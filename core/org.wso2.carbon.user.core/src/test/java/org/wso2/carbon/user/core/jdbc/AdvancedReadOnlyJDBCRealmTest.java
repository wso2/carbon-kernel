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

import org.apache.commons.dbcp.BasicDataSource;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

public class AdvancedReadOnlyJDBCRealmTest extends BaseTestCase {

    private UserRealm realm;

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testStuff() throws Exception {
        DatabaseUtil.closeDatabasePoolConnection();
        initRealmStuff();
        doRoleStuff();
        /*commenting out following since
         1. earlier cached stuff by other test cases causes test failure.
         2. there is no way to clear authorization cache from the test case*/
        //doAuthorizationStuff();
    }

    public void initRealmStuff() throws Exception {
        String dbFolder = "target/advjdbcrotest";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl("jdbc:h2:target/advjdbcrotest/CARBON_TEST");

        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();
        
        this.addIntialData(ds);
        RealmConfigXMLProcessor builder = new RealmConfigXMLProcessor();
        InputStream inStream = this.getClass().getClassLoader().getResource(
                "adv-jdbc-readonly-test.xml").openStream();
        RealmConfiguration realmConfig = builder.buildRealmConfiguration(inStream);
        inStream.close();
        realm = new DefaultRealm();
        realm.init(realmConfig, ClaimTestUtil.getClaimTestData(), ClaimTestUtil
                .getProfileTestData(), 0);
        assertTrue(realm.getUserStoreManager().isExistingRole("adminx"));    
    }
    
    public void doRoleStuff() throws Exception {
        UserStoreManager admin = realm.getUserStoreManager();

        admin.addRole("Internal/role2", null, null);
        admin.addRole("Internal/role3", null, null);
        admin.addRole("Internal/role4", null, null);
        try{
           admin.addRole(null, null, null);
           fail("Exception at Null role name");
        }catch(Exception e){
            //caught Exception
        }

        admin.updateRoleListOfUser("saman", null, new String[] { "Internal/role2" });
        admin.updateRoleListOfUser("saman", new String[] { "Internal/role2" }, new String[] { "Internal/role4",
                "Internal/role3" });
        try{
           admin.updateRoleListOfUser(null, null, new String[] { "Internal/role2" });
           fail("Exceptions at missing user name");
        }catch(Exception ex){
           //expected user
        }

        assertEquals(3, admin.getRoleListOfUser("saman").length);

        // negative
        admin.updateUserListOfRole("Internal/role2", new String[] { "saman" }, null);
        admin.updateUserListOfRole("Internal/role3", null, new String[] { "amara", "sunil" });
        try {
            admin.updateUserListOfRole(null, null, new String[] { "d" });
            fail("Exception thrown at null Roll name failed");
        }catch (Exception e) {
         // exptected error in negative testing
        }
        try {
            admin.updateUserListOfRole("rolexx", null, new String[] { "amara", "sunil" });
            fail("Exception thrown at invalid Roll names failed");
        } catch (Exception e) {
            // exptected error in negative testing
        }
        try{
            admin.updateRoleListOfUser("saman", new String[] { "x" }, new String[] { "y" });
            fail("Exception thrown at invalid Roll names failed");
         } catch (Exception e) {
            // exptected error in negative testing
         }

        //wrong users - must pass because we don't know the external users.
        admin.updateUserListOfRole("Internal/role2", null, new String[] { "d" });
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
          // caught exception
        }
        try {
          authMan.authorizeRole("rollee", null, "write");
          fail("Exception at authorizing a role with Null resourceID");
        } catch (Exception e) {
          // caught exception
        }
        try {
          authMan.authorizeRole("rollee","wall",null);
          fail("Exception at authorizing a role with Null action");
        } catch (Exception e) {
          // caught exception
        }
        try {
          authMan.authorizeRole("rolleex","wall","run");
          fail("Exception at authorizing a role with Invalid action");
        } catch (Exception e) {
          // caught exception
        }

        authMan.authorizeUser("sunil", "wall", "read");
        try {
          authMan.authorizeUser(null, "wall", "read");
          fail("Exception at authorizing a user with Null name");
        } catch (Exception e) {
          //caught exception
        }
        try {
          authMan.authorizeUser("isuru", null, "read");
          fail("Exception at authorizing a user with Null resourceID");
        } catch (Exception e) {
          //caught exception
        }
        try {
          authMan.authorizeUser("isuru","wall",null);
          fail("Exception at authorizing a user with Null action");
        } catch (Exception e) {
          //caught exception
        }
        try {
          authMan.authorizeUser("isuru","wall","run");
          fail("Exception at authorizing a user with Invalid action");
        } catch (Exception e) {
          //caught exception
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
          //caught exception
        }

        authMan.clearUserAuthorization("sunil", "wall", "read");
        try{
            authMan.clearUserAuthorization("isuru", "wall", "run");
            fail("Exception at clear user authorization");
        }catch(Exception e){

        }
        try{
            authMan.clearUserAuthorization(null, "wall", "read");
            fail("Exception at clear user authorization");
        }catch(Exception e){

        }
        try{
            authMan.clearUserAuthorization("isuru", null, "read");
            fail("Exception at clear user authorization");
        }catch(Exception e){

        }
        try{
            authMan.clearUserAuthorization("isuru","wall", null);
            fail("Exception at clear user authorization");
        }catch(Exception e){

        }

        authMan.clearRoleAuthorization("roley", "table", "write");
        try{
            authMan.clearRoleAuthorization(null, "table", "write");
            fail("Exception at clear role authorization");
        }catch(Exception e){
            //caught exception
        }
        try{
            authMan.clearRoleAuthorization("roleee", null, "write");
            fail("Exception at clear role authorization");
        }catch(Exception e){
           //caught exception
        }
        try{
            authMan.clearRoleAuthorization("roleee", "table", null);
            fail("Exception at clear role authorization");
        }catch(Exception e){
           //caught exception
        }
        //authMan.isRoleAuthorized("roley", "table", "write");
        
        authMan.clearResourceAuthorizations("wall");
        try{
            authMan.clearResourceAuthorizations(null);
            fail("Exception at clear Resource Authorizations");
        }catch(Exception e){

        }

        assertFalse(authMan.isUserAuthorized("saman", "wall", "write"));
        assertFalse(authMan.isUserAuthorized("sunil", "wall", "read"));
        assertFalse(authMan.isRoleAuthorized("roley", "table", "write"));
    }

    private void addIntialData(DataSource ds) throws Exception {
        String sql = "INSERT INTO UM_USER (UM_USER_NAME, UM_USER_PASSWORD, UM_CHANGED_TIME) VALUES (?, ?, ?)";
        Connection dbCon = ds.getConnection();
        dbCon.setAutoCommit(false);
        PreparedStatement stmt = dbCon.prepareStatement(sql);
        stmt.setString(1, "system");
        stmt.setString(2, "topsecret");
        stmt.setTimestamp(3, new Timestamp((new Date().getTime())));
        stmt.addBatch();
        stmt.setString(1, "adminx");
        stmt.setString(2, "adminy");
        stmt.setTimestamp(3, new Timestamp((new Date().getTime())));
        stmt.addBatch();
        stmt.setString(1, "anonx");
        stmt.setString(2, "nopassx");
        stmt.setTimestamp(3, new Timestamp((new Date().getTime())));
        stmt.addBatch();
        stmt.setString(1, "saman");
        stmt.setString(2, "pass1");
        stmt.setTimestamp(3, new Timestamp((new Date().getTime())));
        stmt.addBatch();
        stmt.setString(1, "amara");
        stmt.setString(2, "pass2");
        stmt.setTimestamp(3, new Timestamp((new Date().getTime())));
        stmt.addBatch();
        stmt.setString(1, "sunil");
        stmt.setString(2, "pass3");
        stmt.setTimestamp(3, new Timestamp((new Date().getTime())));
        stmt.addBatch();
        int[] count = stmt.executeBatch();
        assertEquals(6, count.length);

        sql = "INSERT INTO UM_DOMAIN (UM_DOMAIN_NAME, UM_IS_HYBRID) VALUES (?, ?)";
        stmt = dbCon.prepareStatement(sql);
        stmt.setString(1, "PRIMARY");
        stmt.setBoolean(2, false);
        stmt.addBatch();
        stmt.setString(1, "INTERNAL");
        stmt.setBoolean(2, true);
        stmt.addBatch();
        stmt.executeBatch();
        dbCon.commit();

        sql = "INSERT INTO UM_HYBRID_ROLE (UM_DOMAIN_ID, UM_ROLE_NAME) VALUES (?, ?)";
        stmt = dbCon.prepareStatement(sql);
        stmt.setInt(1, 1);
        stmt.setString(2, "adminx");
        stmt.addBatch();
        stmt.setInt(1, 2);
        stmt.setString(2, "everyonex");
        stmt.addBatch();
        count = stmt.executeBatch();
        assertEquals(2, count.length);
        dbCon.commit();
        
        dbCon.close();
    }
}

