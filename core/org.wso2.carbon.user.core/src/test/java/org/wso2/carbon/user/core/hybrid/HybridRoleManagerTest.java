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
package org.wso2.carbon.user.core.hybrid;

import org.apache.commons.dbcp.BasicDataSource;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.config.TestRealmConfigBuilder;
import org.wso2.carbon.user.core.jdbc.JDBCRealmTest;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.InputStream;

import static org.wso2.carbon.user.core.UserStoreConfigConstants.PRIMARY;

public class HybridRoleManagerTest extends BaseTestCase {

    private static final String TARGET_BASIC_HYBRID_ROLE_TEST = "target/HybridRoleTest";
    private HybridRoleManager hybridRoleMan;
    private UserRealm realm = null;
    private static String TEST_URL = "jdbc:h2:./target/HybridRoleTest/CARBON_TEST";
    private static final String JDBC_TEST_CASE_INSENSITIVE_USERMGT_XML = "user-mgt-test-caseinsensitive.xml";
    private BasicDataSource ds;

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testHybridRoleManager() throws Exception {
        initDataSource(TEST_URL);
        initRealmStuff(JDBCRealmTest.JDBC_TEST_USERMGT_XML);
        doHybridRoleOperations();
        ds.close();
        DatabaseUtil.closeDatabasePoolConnection();
    }

    private void initRealmStuff(String userMgtxml) throws Exception {

        InputStream inStream = this.getClass().getClassLoader().getResource(userMgtxml).openStream();
        RealmConfiguration realmConfig = TestRealmConfigBuilder
                .buildRealmConfigWithJDBCConnectionUrl(inStream, TEST_URL);
        realm = new DefaultRealm();
        realm.init(realmConfig, ClaimTestUtil.getClaimTestData(), ClaimTestUtil
                .getProfileTestData(), MultitenantConstants.SUPER_TENANT_ID);
        hybridRoleMan = new HybridRoleManager(ds, MultitenantConstants.SUPER_TENANT_ID, realmConfig, realm);

    }

    private void initDataSource(String dbUrl) throws Exception {

        String dbFolder = TARGET_BASIC_HYBRID_ROLE_TEST;
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl(dbUrl);

        DatabaseCreator creator = new DatabaseCreator(ds);
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String resourcesPath = new File("src/test/resources").getAbsolutePath();
        System.setProperty(ServerConstants.CARBON_HOME, resourcesPath);
        creator.createRegistryDatabase();
        System.setProperty(ServerConstants.CARBON_HOME, carbonHome);
        UserCoreUtil.persistDomain(PRIMARY, MultitenantConstants.SUPER_TENANT_ID, ds);
    }

    private void doHybridRoleOperations() throws Exception {

        UserStoreManager admin = realm.getUserStoreManager();
        // Populate users
        admin.addUser("Lionel", "credential", null, null, null, false);
        admin.addUser("Chitarah", "credential", null, null, null, false);
        admin.addUser("Willykat", "credential", null, null, null, false);
        admin.addUser("Willykit", "credential", null, null, null, false);

        int numberOfHybridRoles = hybridRoleMan.getHybridRoles("*").length;

        // Test add new internal role
        hybridRoleMan.addHybridRole("ThunderCats", null);
        assertTrue(hybridRoleMan.isExistingRole("ThunderCats"));
        numberOfHybridRoles += 1;

        // Assign internal role to users
        hybridRoleMan.updateHybridRoleListOfUser("Lionel", null, new String[]{"ThunderCats"});
        hybridRoleMan.updateHybridRoleListOfUser("Willykat", null, new String[]{"ThunderCats"});
        hybridRoleMan.updateHybridRoleListOfUser("Willykit", null, new String[]{"ThunderCats"});

        // Add hybrid role with users
        hybridRoleMan.addHybridRole("Siblings", new String[]{"Willykat", "Willykit"});
        hybridRoleMan.addHybridRole("Friends", new String[]{"Lionel", "Willykit"});
        numberOfHybridRoles += 2;

        // Check all existing hybrid roles created
        assertEquals(numberOfHybridRoles, hybridRoleMan.getHybridRoles("*").length);

        // Check whether hybrid role assigned to user
        assertEquals(3, hybridRoleMan.getHybridRoleListOfUser("Lionel", "*").length);

        // Check whether users assigned to hybrid role
        assertEquals(3, hybridRoleMan.getUserListOfHybridRole("ThunderCats").length);

        // Update users of hybrid role add/remove
        hybridRoleMan.updateUserListOfHybridRole("ThunderCats", new String[]{"Willykat",
                "Willykit"}, new String[]{"Snarf"});
        assertEquals(2, hybridRoleMan.getUserListOfHybridRole("ThunderCats").length);

        // Update user with lower case remove user; entries should not delete as case sensitive
        hybridRoleMan.updateUserListOfHybridRole("ThunderCats", new String[]{"snarf"}, null);
        assertEquals(2, hybridRoleMan.getUserListOfHybridRole("ThunderCats").length);

        // Delete hybrid role
        hybridRoleMan.deleteHybridRole("ThunderCats");
        assertEquals(2, hybridRoleMan.getHybridRoleListOfUser("Lionel", "").length);

        // Delete user with lower case; entries should not delete as case sensitive
        hybridRoleMan.deleteUser("lionel");
        assertEquals(2, hybridRoleMan.getUserListOfHybridRole("Friends").length);

        // Change realm to pick CaseInSensitive User store configurations
        initRealmStuff(JDBC_TEST_CASE_INSENSITIVE_USERMGT_XML);

        // Delete user with lower case; entries should not delete as case in-sensitive
        hybridRoleMan.deleteUser("lionel");
        assertEquals(1, hybridRoleMan.getUserListOfHybridRole("Friends").length);
    }
}
