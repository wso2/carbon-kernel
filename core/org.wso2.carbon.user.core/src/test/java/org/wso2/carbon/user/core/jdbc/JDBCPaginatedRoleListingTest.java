/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.jdbc;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.NotImplementedException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.util.JDBCRealmUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests the paginated role listing of the JDBC user store.
 */
public class JDBCPaginatedRoleListingTest {

    private static final String DB_URL = "jdbc:h2:mem:paginatedRoleListingTest;DB_CLOSE_DELAY=-1";
    private static final String DOMAIN = "SECONDARY";
    private static final int TENANT_ID = -1234;
    private static final int GROUP_COUNT = 25;
    private static final String CUSTOM_ROLE_LIST_SQL = "SELECT ROLE_NAME, UM_TENANT_ID, IS_SHARED FROM CUSTOM_ROLE "
            + "WHERE ROLE_NAME LIKE ? AND UM_TENANT_ID=? AND IS_SHARED=FALSE ORDER BY ROLE_NAME";
    private static final String CUSTOM_PAGINATED_SQL = "SELECT ROLE_NAME FROM CUSTOM_ROLE WHERE ROLE_NAME LIKE ? "
            + "AND UM_TENANT_ID=? AND IS_SHARED=FALSE ORDER BY ROLE_NAME LIMIT ? OFFSET ?";
    private static final String CUSTOM_PAGINATED_COUNT_SQL = "SELECT COUNT(ROLE_NAME) FROM CUSTOM_ROLE "
            + "WHERE ROLE_NAME LIKE ? AND UM_TENANT_ID=? AND IS_SHARED=FALSE";

    private Connection keepAlive;
    private UniqueIDJDBCUserStoreManager jdbcUserStoreManager;
    private AbstractUserStoreManager userStoreManager;

    @BeforeClass
    public void setUp() throws Exception {

        Class.forName("org.h2.Driver");
        keepAlive = DriverManager.getConnection(DB_URL);
        try (Statement statement = keepAlive.createStatement()) {
            statement.execute("CREATE TABLE UM_ROLE (UM_ID INTEGER AUTO_INCREMENT, UM_ROLE_NAME VARCHAR(255) NOT NULL, "
                    + "UM_TENANT_ID INTEGER DEFAULT 0, UM_SHARED_ROLE BOOLEAN DEFAULT FALSE, "
                    + "PRIMARY KEY (UM_ID, UM_TENANT_ID), UNIQUE(UM_ROLE_NAME, UM_TENANT_ID))");
        }
        // Inserted out of order, so the listing has to sort.
        for (int i = GROUP_COUNT; i >= 1; i--) {
            insertRole(String.format("group%02d", i), TENANT_ID, false);
        }
        insertRole("admin", TENANT_ID, false);
        insertRole("group99", TENANT_ID, true);
        insertRole("group50", 1, false);
        // Stands in for a custom user store schema; it only exposes the group1x roles.
        try (Statement statement = keepAlive.createStatement()) {
            statement.execute("CREATE VIEW CUSTOM_ROLE AS SELECT UM_ROLE_NAME AS ROLE_NAME, UM_TENANT_ID, "
                    + "UM_SHARED_ROLE AS IS_SHARED FROM UM_ROLE WHERE UM_ROLE_NAME LIKE 'group1%'");
        }

        jdbcUserStoreManager = buildJDBCUserStoreManager(new HashMap<>());

        Map<String, UserStoreManager> userStoreManagerHolder = new HashMap<>();
        userStoreManagerHolder.put(DOMAIN, jdbcUserStoreManager);
        userStoreManagerHolder.put("NOPAGING", mock(AbstractUserStoreManager.class, CALLS_REAL_METHODS));
        Map<String, String> customListSQL = new HashMap<>();
        customListSQL.put(JDBCRealmConstants.GET_ROLE_LIST_H2, CUSTOM_ROLE_LIST_SQL);
        userStoreManagerHolder.put("CUSTOM", buildJDBCUserStoreManager(customListSQL));
        userStoreManager = mock(AbstractUserStoreManager.class, CALLS_REAL_METHODS);
        setField("userStoreManagerHolder", userStoreManager, userStoreManagerHolder);
        setField("readGroupsEnabled", userStoreManager, true);
    }

    @AfterClass
    public void tearDown() throws Exception {

        keepAlive.close();
    }

    @Test
    public void testPagesWalkEveryRoleInOrder() throws Exception {

        List<String> listed = new ArrayList<>();
        for (int offset = 0; offset < GROUP_COUNT; offset += 10) {
            String[] page = jdbcUserStoreManager.doGetRoleNames("group*", 10, offset);
            assertEquals(page.length, Math.min(10, GROUP_COUNT - offset));
            listed.addAll(Arrays.asList(page));
        }
        List<String> expected = new ArrayList<>();
        for (int i = 1; i <= GROUP_COUNT; i++) {
            expected.add(String.format(DOMAIN + "/group%02d", i));
        }
        assertEquals(listed, expected);
        assertEquals(jdbcUserStoreManager.doGetRoleNames("group*", 10, GROUP_COUNT).length, 0);
    }

    @Test
    public void testPageIsNotCappedByMaxRoleNameListLength() throws Exception {

        assertEquals(jdbcUserStoreManager.doGetRoleNames("*", 100, 0).length, GROUP_COUNT + 1);
    }

    @DataProvider(name = "roleFilters")
    public Object[][] roleFilters() {

        return new Object[][]{
                {null, GROUP_COUNT + 1},
                {"*", GROUP_COUNT + 1},
                {"group*", GROUP_COUNT},
                {"group1*", 10},
                {"*5", 3},
                {"group0?", 9},
                {"admin", 1},
                {"group50", 0},
                {"group99", 0}
        };
    }

    @Test(dataProvider = "roleFilters")
    public void testCountAgreesWithListing(String filter, int expected) throws Exception {

        assertEquals(jdbcUserStoreManager.doCountRoleNames(filter), expected);
        assertEquals(jdbcUserStoreManager.doGetRoleNames(filter, 100, 0).length, expected);
    }

    @Test
    public void testNonPositiveLimitAndNegativeOffset() throws Exception {

        assertEquals(jdbcUserStoreManager.doGetRoleNames("*", 0, 0).length, 0);
        assertEquals(jdbcUserStoreManager.doGetRoleNames("*", -1, 0).length, 0);
        assertEquals(jdbcUserStoreManager.doGetRoleNames("group*", 2, -5),
                new String[]{DOMAIN + "/group01", DOMAIN + "/group02"});
    }

    @Test
    public void testListAndCountThroughDomainQualifiedFilter() throws Exception {

        assertEquals(userStoreManager.listRoleNames(DOMAIN + "/group*", 3, 22),
                new String[]{DOMAIN + "/group23", DOMAIN + "/group24", DOMAIN + "/group25"});
        assertEquals(userStoreManager.listRoleNames("secondary/group2*", 10, 0).length, 6);
        assertEquals(userStoreManager.countRoleNames(DOMAIN + "/group*"), GROUP_COUNT);
    }

    @DataProvider(name = "unpageableFilters")
    public Object[][] unpageableFilters() {

        return new Object[][]{
                {null},
                {"group*"},
                {"/group*"},
                {"Internal/*"},
                {"Application/*"},
                {"Workflow/*"},
                {"NOPAGING/*"},
                {"CUSTOM/*"}
        };
    }

    @Test(dataProvider = "unpageableFilters")
    public void testUnpageableFilterReportsNotImplemented(String filter) {

        assertNotImplemented(() -> userStoreManager.listRoleNames(filter, 10, 0));
        assertNotImplemented(() -> userStoreManager.countRoleNames(filter));
    }

    @Test(expectedExceptions = UserStoreClientException.class)
    public void testUnknownDomainIsClientError() throws Exception {

        userStoreManager.listRoleNames("UNKNOWN/*", 10, 0);
    }

    @Test
    public void testGroupReadingDisabledReportsNotImplemented() throws Exception {

        setField("readGroupsEnabled", userStoreManager, false);
        try {
            assertNotImplemented(() -> userStoreManager.listRoleNames(DOMAIN + "/*", 10, 0));
        } finally {
            setField("readGroupsEnabled", userStoreManager, true);
        }
    }

    @Test
    public void testShippedSQLSetExplicitlyStillPages() throws Exception {

        Map<String, String> properties = new HashMap<>();
        properties.put(JDBCRealmConstants.GET_ROLE_LIST, "\n  " + JDBCRealmConstants.GET_ROLE_LIST_SQL
                .replace(" FROM ", "\n        FROM ") + "  \n");
        properties.put(JDBCRealmConstants.GET_ROLE_LIST_H2, JDBCRealmConstants.GET_ROLE_LIST_SQL_H2
                .replace(" WHERE ", "\t WHERE "));
        properties.put(JDBCRealmConstants.GET_ROLE_FILTER_PAGINATED_H2,
                JDBCRealmConstants.GET_ROLE_FILTER_PAGINATED_SQL_H2);
        UniqueIDJDBCUserStoreManager storeManager = buildJDBCUserStoreManager(properties);

        assertEquals(storeManager.doGetRoleNames("group*", 5, 20).length, 5);
        assertEquals(storeManager.doCountRoleNames("group*"), GROUP_COUNT);
    }

    @DataProvider(name = "roleListSQLProperties")
    public Object[][] roleListSQLProperties() {

        return new Object[][]{
                {JDBCRealmConstants.GET_ROLE_LIST},
                {JDBCRealmConstants.GET_ROLE_LIST_H2},
                {JDBCRealmConstants.GET_ROLE_LIST_WITH_ESCAPE},
                {JDBCRealmConstants.GET_ROLE_LIST_WITH_ESCAPE_H2}
        };
    }

    @Test(dataProvider = "roleListSQLProperties")
    public void testCustomRoleListSQLWithoutPaginatedSQLDeclines(String property) throws Exception {

        Map<String, String> properties = new HashMap<>();
        properties.put(property, CUSTOM_ROLE_LIST_SQL);
        UniqueIDJDBCUserStoreManager storeManager = buildJDBCUserStoreManager(properties);

        assertNotImplemented(() -> storeManager.doGetRoleNames("*", 10, 0));
        assertNotImplemented(() -> storeManager.doCountRoleNames("*"));
    }

    @Test
    public void testCustomRoleListSQLWithoutPaginatedCountSQLDeclines() throws Exception {

        Map<String, String> properties = new HashMap<>();
        properties.put(JDBCRealmConstants.GET_ROLE_LIST_H2, CUSTOM_ROLE_LIST_SQL);
        properties.put(JDBCRealmConstants.GET_ROLE_FILTER_PAGINATED_H2, CUSTOM_PAGINATED_SQL);
        UniqueIDJDBCUserStoreManager storeManager = buildJDBCUserStoreManager(properties);

        assertNotImplemented(() -> storeManager.doGetRoleNames("*", 10, 0));
        assertNotImplemented(() -> storeManager.doCountRoleNames("*"));
    }

    @Test
    public void testCustomRoleListSQLWithPaginatedSQLPages() throws Exception {

        Map<String, String> properties = new HashMap<>();
        properties.put(JDBCRealmConstants.GET_ROLE_LIST_H2, CUSTOM_ROLE_LIST_SQL);
        properties.put(JDBCRealmConstants.GET_ROLE_FILTER_PAGINATED_H2, CUSTOM_PAGINATED_SQL);
        properties.put(JDBCRealmConstants.GET_ROLE_FILTER_PAGINATED_COUNT_H2, CUSTOM_PAGINATED_COUNT_SQL);
        UniqueIDJDBCUserStoreManager storeManager = buildJDBCUserStoreManager(properties);

        assertEquals(storeManager.doGetRoleNames("*", 4, 8),
                new String[]{DOMAIN + "/group18", DOMAIN + "/group19"});
        assertEquals(storeManager.doCountRoleNames("*"), 10);
    }

    private UniqueIDJDBCUserStoreManager buildJDBCUserStoreManager(Map<String, String> configuredProperties)
            throws Exception {

        Map<String, String> properties = JDBCRealmUtil.getSQL(new HashMap<>(configuredProperties));
        properties.put(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME, DOMAIN);
        properties.put(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST, "5");
        RealmConfiguration realmConfiguration = new RealmConfiguration();
        realmConfiguration.setUserStoreProperties(properties);

        UniqueIDJDBCUserStoreManager storeManager = mock(UniqueIDJDBCUserStoreManager.class, CALLS_REAL_METHODS);
        setField("realmConfig", storeManager, realmConfiguration);
        setField("tenantId", storeManager, TENANT_ID);
        doAnswer(invocation -> DriverManager.getConnection(DB_URL)).when(storeManager).getDBConnection();
        return storeManager;
    }

    private void insertRole(String roleName, int tenantId, boolean shared) throws Exception {

        try (PreparedStatement statement = keepAlive.prepareStatement(
                "INSERT INTO UM_ROLE (UM_ROLE_NAME, UM_TENANT_ID, UM_SHARED_ROLE) VALUES (?, ?, ?)")) {
            statement.setString(1, roleName);
            statement.setInt(2, tenantId);
            statement.setBoolean(3, shared);
            statement.executeUpdate();
        }
    }

    private static void setField(String name, Object target, Object value) throws Exception {

        Field field = AbstractUserStoreManager.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void assertNotImplemented(ThrowingCall call) {

        try {
            call.run();
        } catch (NotImplementedException e) {
            return;
        } catch (Exception e) {
            throw new AssertionError("Expected NotImplementedException but got " + e, e);
        }
        assertTrue(false, "Expected NotImplementedException.");
    }

    private interface ThrowingCall {

        void run() throws Exception;
    }
}
