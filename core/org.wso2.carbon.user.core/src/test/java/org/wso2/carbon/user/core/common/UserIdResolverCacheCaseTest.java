/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.user.core.common;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.config.TestRealmConfigBuilder;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.InputStream;

import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_ID_FROM_USER_NAME_CACHE_NAME;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_NAME_FROM_UNIQUE_USER_ID_CACHE_NAME;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_NAME_FROM_USER_ID_CACHE_NAME;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_UNIQUE_ID_FROM_USER_NAME_CACHE_NAME;

/**
 * Tests that {@link AbstractUserStoreManager#getUserIDFromUserName(String)} caches
 * the DB-stored username rather than the caller-provided input value.
 */
public class UserIdResolverCacheCaseTest extends BaseTestCase {

    private static final String TARGET_DB_FOLDER = "target/UserIdResolverCacheCaseTest";
    private static final String TEST_URL = "jdbc:h2:./target/UserIdResolverCacheCaseTest/CARBON_TEST";
    private static final String CASE_INSENSITIVE_UNIQUE_ID_XML = "user-mgt-test-caseinsensitive-uniqueId.xml";

    private AbstractUserStoreManager admin;

    public void setUp() throws Exception {

        super.setUp();
        clearUserIdResolverCache();
        DatabaseUtil.closeDatabasePoolConnection();
    }

    public void testGetUserIDFromUserNameCachesDBStoredCase() throws Exception {

        initRealmStuff();
        doTestCacheCaseCorrectness();
        DatabaseUtil.closeDatabasePoolConnection();
    }

    private void initRealmStuff() throws Exception {

        if ((new File(TARGET_DB_FOLDER)).exists()) {
            deleteDir(new File(TARGET_DB_FOLDER));
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl(TEST_URL);
        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();
        UserRealm realm = new DefaultRealm();
        InputStream inStream = this.getClass().getClassLoader()
                .getResource(CASE_INSENSITIVE_UNIQUE_ID_XML).openStream();
        RealmConfiguration realmConfig = TestRealmConfigBuilder
                .buildRealmConfigWithJDBCConnectionUrl(inStream, TEST_URL);
        realm.init(realmConfig, ClaimTestUtil.getClaimTestData(),
                ClaimTestUtil.getProfileTestData(), MultitenantConstants.SUPER_TENANT_ID);
        admin = (AbstractUserStoreManager) realm.getUserStoreManager();
        ds.close();
    }

    private void doTestCacheCaseCorrectness() throws Exception {

        // Test 1: Create user with UPPERCASE, look up with lowercase.
        admin.addUser("TESTUSER", "password", null, null, null, false);
        clearUserIdResolverCache();

        String userId = admin.getUserIDFromUserName("testuser");
        assertNotNull("getUserIDFromUserName should find user with different case", userId);

        String resolvedName = admin.getUserNameFromUserID(userId);
        assertEquals("Cache should store DB value 'TESTUSER', not input 'testuser'",
                "TESTUSER", resolvedName);

        // Test 2: Create user with lowercase, look up with UPPERCASE.
        admin.addUser("lowercaseuser", "password", null, null, null, false);
        clearUserIdResolverCache();

        userId = admin.getUserIDFromUserName("LOWERCASEUSER");
        assertNotNull("getUserIDFromUserName should find lowercase user via uppercase input", userId);

        resolvedName = admin.getUserNameFromUserID(userId);
        assertEquals("Cache should store DB value 'lowercaseuser', not input 'LOWERCASEUSER'",
                "lowercaseuser", resolvedName);

        // Test 3: Create user with MixedCase, look up with all lowercase.
        admin.addUser("MixedCaseUser", "password", null, null, null, false);
        clearUserIdResolverCache();

        userId = admin.getUserIDFromUserName("mixedcaseuser");
        assertNotNull("getUserIDFromUserName should find mixed-case user via lowercase input", userId);

        resolvedName = admin.getUserNameFromUserID(userId);
        assertEquals("Cache should store DB value 'MixedCaseUser', not input 'mixedcaseuser'",
                "MixedCaseUser", resolvedName);

        // Test 4: Exact case match — cache should still store correctly.
        clearUserIdResolverCache();
        userId = admin.getUserIDFromUserName("TESTUSER");
        assertNotNull(userId);

        resolvedName = admin.getUserNameFromUserID(userId);
        assertEquals("Exact case lookup should also cache DB value correctly",
                "TESTUSER", resolvedName);

        // Test 5: Repeated lookups with different cases should all resolve to DB-stored value.
        clearUserIdResolverCache();
        String userId1 = admin.getUserIDFromUserName("Testuser");
        clearUserIdResolverCache();
        String userId2 = admin.getUserIDFromUserName("TESTUSER");
        clearUserIdResolverCache();
        String userId3 = admin.getUserIDFromUserName("testuser");

        assertEquals("All case variants should resolve to the same user ID", userId1, userId2);
        assertEquals("All case variants should resolve to the same user ID", userId2, userId3);
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
