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

public class ReadOnlyJDBCRealmTest extends BaseTestCase {

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
        String dbFolder = "target/ReadOnlyTest";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl("jdbc:h2:target/ReadOnlyTest/CARBON_TEST");

        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();
        
        this.addIntialData(ds);
        RealmConfigXMLProcessor builder = new RealmConfigXMLProcessor();
        InputStream inStream = this.getClass().getClassLoader().getResource(
                "jdbc-readonly-test.xml").openStream();
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
