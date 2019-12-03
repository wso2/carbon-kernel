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
package org.wso2.carbon.user.core.authman;

import org.apache.commons.dbcp.BasicDataSource;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.config.TestRealmConfigBuilder;
import org.wso2.carbon.user.core.jdbc.JDBCRealmTest;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AdvancedPermissionTreeTest extends BaseTestCase {
    private UserRealm realm = null;
    private UserStoreManager admin = null;
    AuthorizationManager authMan = null;
    private String TEST_URL = "jdbc:h2:./target/permTreetest/CARBON_TEST";

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testStuff() throws Exception {
        DatabaseUtil.closeDatabasePoolConnection();
        initRealmStuff();
        admin = realm.getUserStoreManager();
        authMan = realm.getAuthorizationManager();
        dorolestuff();
        doAuthorizationstuff();
        doTestUserRoleCachingInCaseInsensitiveUsername();
        DatabaseUtil.closeDatabasePoolConnection();
    }

    public void initRealmStuff() throws Exception {
        String dbFolder = "target/permTreetest";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl(TEST_URL);

        DatabaseCreator creator = new DatabaseCreator(ds);

        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String resourcesPath = new File("src/test/resources").getAbsolutePath();
        System.setProperty(ServerConstants.CARBON_HOME, resourcesPath);
        creator.createRegistryDatabase();
        System.setProperty(ServerConstants.CARBON_HOME, carbonHome);

        realm = new DefaultRealm();
        InputStream inStream = this.getClass().getClassLoader().getResource(JDBCRealmTest.JDBC_TEST_USERMGT_XML).openStream();
        RealmConfiguration realmConfig = TestRealmConfigBuilder
                .buildRealmConfigWithJDBCConnectionUrl(inStream, TEST_URL);
        realm.init(realmConfig, ClaimTestUtil.getClaimTestData(), ClaimTestUtil
                .getProfileTestData(), -1234);
        ds.close();
    }

    public void dorolestuff() throws Exception {

        Permission[] permisions = new Permission[2];
        permisions[0] = new Permission("high security", "read");
        permisions[1] = new Permission("low security", "write");
        assertEquals("high security", permisions[0].getResourceId());
        assertEquals("write", permisions[1].getAction());

        admin.addUser("dimuthu", "pass1", null, null, null, false);
        admin.addUser("isuru", "pass2", null, null, null, false);
        admin.addUser("ajith", "pass3", null, null, null, false);
        admin.addUser("Kalpa", "pass4", null, null, null, false);
        admin.addUser("Lahiru", "pass5", null, null, null, false);
        admin.addUser("indunil", "pass6", null, null, null, false);

        admin.addRole("role1", new String[]{"dimuthu"}, permisions);
        admin.addRole("role2", new String[]{"isuru", "ajith"}, permisions);
        admin.addRole("role3", new String[]{"Kalpa"}, permisions);
        admin.addRole("role4", new String[]{"Lahiru"}, permisions);
        admin.addRole("Internal/role1", new String[]{"indunil"}, permisions);
    }

    public void doAuthorizationstuff() throws Exception {

        //Role Authorization
        authMan.authorizeRole("role1", "/s", "read");
        assertTrue(authMan.isRoleAuthorized("role1", "/s", "read"));
        assertTrue(authMan.isRoleAuthorized("role1", "/s/t/u/v", "read"));
        assertTrue(authMan.isUserAuthorized("dimuthu", "/s", "read"));
        assertTrue(authMan.isUserAuthorized("dimuthu", "/s/t/u/v", "read"));

        authMan.denyRole("role1", "/s/t/u", "read");
        assertFalse(authMan.isRoleAuthorized("role1", "/s/t/u", "read"));
        assertFalse(authMan.isRoleAuthorized("role1", "s/t/u/v/w", "read"));
        assertFalse(authMan.isUserAuthorized("dimuthu", "s/t/u/v/w", "read"));
        assertTrue(authMan.isRoleAuthorized("role1", "/s/t", "read"));
        assertTrue(authMan.isUserAuthorized("dimuthu", "/s/t", "read"));

        authMan.authorizeRole("role1", "/s/t/u/v/w/x", "read");
        assertTrue(authMan.isRoleAuthorized("role1", "/s/t/u/v/w/x/y", "read"));
        assertTrue(authMan.isRoleAuthorized("role1", "/s/t", "read"));
        assertFalse(authMan.isRoleAuthorized("role1", "/s/t/u/v/w", "read"));

        authMan.authorizeRole("Internal/role1", "/s/t/u/v/w/x", "read");
        assertTrue(authMan.isRoleAuthorized("role1", "/s/t/u/v/w/x/y", "read"));
        assertTrue(authMan.isRoleAuthorized("role1", "/s/t", "read"));
        assertFalse(authMan.isRoleAuthorized("role1", "/s/t/u/v/w", "read"));
        assertTrue(authMan.isUserAuthorized("indunil", "/s/t/u/v/w/x/y", "read"));
    }

    public void doTestUserRoleCachingInCaseInsensitiveUsername() throws UserStoreException {
        admin.deleteRole("Internal/role1");
        assertFalse(authMan.isUserAuthorized("indunil", "/s/t/u/v/w/x/y", "read"));
    }

}


