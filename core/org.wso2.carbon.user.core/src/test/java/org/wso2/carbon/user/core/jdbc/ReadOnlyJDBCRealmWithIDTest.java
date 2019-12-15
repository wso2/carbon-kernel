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

import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Assert;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public class ReadOnlyJDBCRealmWithIDTest extends BaseTestCase {

    private UserRealm realm;

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testStuff() throws Exception {
        DatabaseUtil.closeDatabasePoolConnection();
        initRealmStuff();
        doRoleStuff();
        doUserClaimValuesStuff();
        DatabaseUtil.closeDatabasePoolConnection();
        /*commenting out following since
         1. earlier cached stuff by other test cases causes test failure.
         2. there is no way to clear authorization cache from the test case*/
        //doAuthorizationStuff();
    }

    public void initRealmStuff() throws Exception {
        String dbFolder = "target/ReadOnlyTestID";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl("jdbc:h2:./target/ReadOnlyTestID/CARBON_TEST");

        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();

        this.addIntialData(ds);
        RealmConfigXMLProcessor builder = new RealmConfigXMLProcessor();
        InputStream inStream = this.getClass().getClassLoader().getResource(
                "jdbc-readonly-test-uniqueid.xml").openStream();
        RealmConfiguration realmConfig = builder.buildRealmConfiguration(inStream);
        inStream.close();
        realm = new DefaultRealm();
        realm.init(realmConfig, ClaimTestUtil.getClaimTestData(), ClaimTestUtil
                .getProfileTestData(), -1234);
        assertTrue(realm.getUserStoreManager().isExistingRole("adminx"));
    }

    public void doRoleStuff() throws Exception {
        UserStoreManager admin = realm.getUserStoreManager();

        admin.addRole("Internal/role2", null, null);
        admin.addRole("Internal/role3", null, null);
        admin.addRole("Internal/role4", null, null);

        admin.updateRoleListOfUser("saman", null, new String[] { "Internal/role2" });
		admin.updateRoleListOfUser("saman", new String[] { "Internal/role2" }, new String[] {
				"Internal/role4", "Internal/role3" });

        String[] rolesOfSaman = admin.getRoleListOfUser("saman");
        assertEquals(3, rolesOfSaman.length);

        // negative
        admin.updateUserListOfRole("Internal/role2", new String[] { "saman" }, null);
        admin.updateUserListOfRole("Internal/role3", null, new String[] { "amara", "sunil" });

        // negative
        try {
            //wrong roles
            admin.updateRoleListOfUser("saman", new String[] { "x" }, new String[] { "y" });
            TestCase.assertTrue(false);
        } catch (Exception e) {
            // exptected error in negative testing

        }
        //wrong users - must pass because we don't know the external users.
        admin.updateUserListOfRole("Internal/role2", null, new String[] { "d" });
    }

    public void doUserClaimValuesStuff() throws Exception {

        UserStoreManager userStoreManager = realm.getUserStoreManager();
        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(ClaimTestUtil.CLAIM_URI1, "John");

        try {
            userStoreManager.setUserClaimValues("saman", claimsMap, ClaimTestUtil.HOME_PROFILE_NAME);
        } catch (UserStoreException e) {
            Assert.assertTrue("Failed to receive the expected invalid operation exception.",
                    e.getMessage().contains("InvalidOperation"));
        }
        try {
            userStoreManager.setUserClaimValues("saman", new HashMap<String, String>(),
                    ClaimTestUtil.HOME_PROFILE_NAME);
            Assert.assertTrue(true);
        } catch (UserStoreException e) {
            Assert.fail("Unexpected error while updating user claims with empty claims map.");
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
        assertTrue(authMan.isRoleAuthorized("roley", "table", "write"));
        assertFalse(authMan.isUserAuthorized("saman", "wall", "read"));
        assertFalse(authMan.isUserAuthorized("sunil", "wall", "write"));

        authMan.clearUserAuthorization("sunil", "wall", "read");
        authMan.clearRoleAuthorization("roley", "table", "write");
        authMan.clearResourceAuthorizations("wall");

        assertFalse(authMan.isUserAuthorized("saman", "wall", "write"));
        assertFalse(authMan.isUserAuthorized("sunil", "wall", "read"));
        assertFalse(authMan.isRoleAuthorized("roley", "table", "write"));
    }

    private void addIntialData(DataSource ds) throws Exception {
        String sql = "INSERT INTO UM_USER (UM_USER_ID, UM_USER_NAME, UM_USER_PASSWORD, UM_CHANGED_TIME, UM_TENANT_ID) "
                + "VALUES (?, ?, ?, ?, ?)";
        Connection dbCon = ds.getConnection();
        dbCon.setAutoCommit(false);
        PreparedStatement stmt = dbCon.prepareStatement(sql);
        stmt.setString(1, "system_id");
        stmt.setString(2, "system_user");
        stmt.setString(3, "topsecret");
        stmt.setTimestamp(4, new Timestamp((new Date().getTime())));
        stmt.setInt(5, -1234);
        stmt.addBatch();
        stmt.setString(1, "adminx_id");
        stmt.setString(2, "adminx");
        stmt.setString(3, "adminy");
        stmt.setTimestamp(4, new Timestamp((new Date().getTime())));
        stmt.setInt(5, -1234);
        stmt.addBatch();
        stmt.setString(1, "anonx_id");
        stmt.setString(2, "anonx");
        stmt.setString(3, "nopassx");
        stmt.setTimestamp(4, new Timestamp((new Date().getTime())));
        stmt.setInt(5, -1234);
        stmt.addBatch();
        stmt.setString(1, "saman_id");
        stmt.setString(2, "saman");
        stmt.setString(3, "pass1");
        stmt.setTimestamp(4, new Timestamp((new Date().getTime())));
        stmt.setInt(5, -1234);
        stmt.addBatch();
        stmt.setString(1, "amara_id");
        stmt.setString(2, "amara");
        stmt.setString(3, "pass2");
        stmt.setTimestamp(4, new Timestamp((new Date().getTime())));
        stmt.setInt(5, -1234);
        stmt.addBatch();
        stmt.setString(1, "sunil_id");
        stmt.setString(2, "sunil");
        stmt.setString(3, "pass3");
        stmt.setTimestamp(4, new Timestamp((new Date().getTime())));
        stmt.setInt(5, -1234);
        stmt.addBatch();
        int[] count = stmt.executeBatch();
        assertEquals(6, count.length);


        sql = "INSERT INTO UM_HYBRID_ROLE (UM_ROLE_NAME) VALUES (?)";
        stmt = dbCon.prepareStatement(sql);
        stmt.setString(1, "adminx");
        stmt.addBatch();
        stmt.setString(1, "everyonex");
        stmt.addBatch();
        count = stmt.executeBatch();
        assertEquals(2, count.length);

        sql = "INSERT INTO UM_USER_ATTRIBUTE (UM_ATTR_NAME,UM_ATTR_VALUE,UM_TENANT_ID,UM_USER_ID,UM_PROFILE_ID) " +
                "VALUES (?,?,?,?,?)";
        stmt = dbCon.prepareStatement(sql);
        stmt.setString(1, "uid");
        stmt.setString(2, "adminx");
        stmt.setInt(3, -1234);
        stmt.setInt(4, 2);
        stmt.setString(5, "default");
        stmt.addBatch();
        stmt.setString(1, "uid");
        stmt.setString(2, "saman");
        stmt.setInt(3, -1234);
        stmt.setInt(4, 4);
        stmt.setString(5, "default");
        stmt.addBatch();
        stmt.executeBatch();

        dbCon.commit();
        dbCon.close();
    }
}
