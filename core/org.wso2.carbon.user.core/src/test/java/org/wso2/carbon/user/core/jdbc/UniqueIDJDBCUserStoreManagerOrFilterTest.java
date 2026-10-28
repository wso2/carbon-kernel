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

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.config.TestRealmConfigBuilder;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.model.ExpressionAttribute;
import org.wso2.carbon.user.core.model.ExpressionCondition;
import org.wso2.carbon.user.core.model.ExpressionOperation;
import org.wso2.carbon.user.core.model.OperationalCondition;
import org.wso2.carbon.user.core.model.OperationalOperation;
import org.wso2.carbon.user.core.model.SqlBuilder;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests conditional user filtering where the filter expressions are combined with the OR operation, against an H2
 * backed JDBC user store.
 */
public class UniqueIDJDBCUserStoreManagerOrFilterTest {

    private static final String JDBC_TEST_USERMGT_XML = "user-mgt-test-uniqueId.xml";
    private static final String DB_FOLDER = "target/OrFilterUniqueIDJDBCDatabaseTest";
    private static final String TEST_URL = "jdbc:h2:./" + DB_FOLDER + "/CARBON_TEST";

    // Attributes the test claims are mapped to. See ClaimTestUtil.
    private static final String ATTRIBUTE_1 = "attr1";
    private static final String ATTRIBUTE_2 = "attr2";

    private AbstractUserStoreManager userStoreManager;

    @BeforeClass
    public void setUp() throws Exception {

        File carbonHome = new File("src/test/resources/dbscripts/group_uuid_disable");
        if (carbonHome.exists()) {
            System.setProperty("carbon.home", carbonHome.getAbsolutePath());
        }
        CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME = true;
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        deleteDirectory(new File(DB_FOLDER));
        DatabaseUtil.closeDatabasePoolConnection();

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        dataSource.setUrl(TEST_URL);
        new DatabaseCreator(dataSource).createRegistryDatabase();

        UserRealm realm = new DefaultRealm();
        try (InputStream inStream = this.getClass().getClassLoader().getResource(JDBC_TEST_USERMGT_XML).openStream()) {
            RealmConfiguration realmConfig =
                    TestRealmConfigBuilder.buildRealmConfigWithJDBCConnectionUrl(inStream, TEST_URL);
            realm.init(realmConfig, ClaimTestUtil.getClaimTestData(), ClaimTestUtil.getProfileTestData(),
                    MultitenantConstants.SUPER_TENANT_ID);
        }
        dataSource.close();
        DatabaseUtil.closeDatabasePoolConnection();

        userStoreManager = (AbstractUserStoreManager) realm.getUserStoreManager();
        addFixtures();
    }

    private void addFixtures() throws Exception {

        userStoreManager.addRole("orRole1", null, null);
        userStoreManager.addUser("orUser1", "orPass1", new String[]{"orRole1"}, null, null, false);
        userStoreManager.addUser("orUser2", "orPass2", null, null, null, false);
        userStoreManager.addUser("orUser3", "orPass3", null, null, null, false);

        userStoreManager.setUserClaimValue("orUser1", ClaimTestUtil.CLAIM_URI1, "alpha", null);
        userStoreManager.setUserClaimValue("orUser1", ClaimTestUtil.CLAIM_URI2, "alphaToo", null);
        userStoreManager.setUserClaimValue("orUser2", ClaimTestUtil.CLAIM_URI1, "beta", null);
        userStoreManager.setUserClaimValue("orUser3", ClaimTestUtil.CLAIM_URI2, "gamma", null);
    }

    @Test
    public void testFilterUsersWithOrOnUsername() throws UserStoreException {

        Condition condition = or(usernameEquals("orUser1"), usernameEquals("orUser2"));
        assertEquals(filteredUsernames(condition, 10, 1), Arrays.asList("orUser1", "orUser2"));
    }

    @Test
    public void testFilterUsersWithOrOnClaims() throws UserStoreException {

        Condition condition = or(claimEquals(ATTRIBUTE_1, "beta"), claimEquals(ATTRIBUTE_2, "gamma"));
        assertEquals(filteredUsernames(condition, 10, 1), Arrays.asList("orUser2", "orUser3"));
    }

    @Test
    public void testFilterUsersWithOrOnUsernameAndClaim() throws UserStoreException {

        Condition condition = or(usernameEquals("orUser1"), claimEquals(ATTRIBUTE_2, "gamma"));
        assertEquals(filteredUsernames(condition, 10, 1), Arrays.asList("orUser1", "orUser3"));
    }

    @Test
    public void testFilterUsersWithOrOnGroupAndClaim() throws UserStoreException {

        Condition condition = or(groupEquals("orRole1"), claimEquals(ATTRIBUTE_2, "gamma"));
        assertEquals(filteredUsernames(condition, 10, 1), Arrays.asList("orUser1", "orUser3"));
    }

    @Test
    public void testFilterUsersWithNestedOr() throws UserStoreException {

        Condition condition = or(usernameEquals("orUser1"),
                or(claimEquals(ATTRIBUTE_1, "beta"), claimEquals(ATTRIBUTE_2, "gamma")));
        assertEquals(filteredUsernames(condition, 10, 1), Arrays.asList("orUser1", "orUser2", "orUser3"));
    }

    @Test
    public void testFilterUsersWithOrDoesNotDuplicateUsers() throws UserStoreException {

        // orUser1 matches both expressions and must still be returned exactly once.
        Condition condition = or(claimEquals(ATTRIBUTE_1, "alpha"), claimEquals(ATTRIBUTE_2, "alphaToo"));
        assertEquals(filteredUsernames(condition, 10, 1), Collections.singletonList("orUser1"));
    }

    @Test
    public void testFilterUsersWithOrOnPartialMatchOperations() throws UserStoreException {

        Condition condition = or(new ExpressionCondition(ExpressionOperation.SW.toString(),
                        ExpressionAttribute.USERNAME.toString(), "orUser1"),
                new ExpressionCondition(ExpressionOperation.CO.toString(), ATTRIBUTE_1, "et"));
        assertEquals(filteredUsernames(condition, 10, 1), Arrays.asList("orUser1", "orUser2"));
    }

    @Test
    public void testFilterUsersWithOrOnNotEqualClaim() throws UserStoreException {

        /*
         * A claim NE under OR has to match the users that do not carry the value at all, and not only the ones that
         * carry a different value. Hence orUser2, which has no attr2 value, is expected in the result.
         */
        Condition condition = or(claimNotEquals(ATTRIBUTE_2, "gamma"), usernameEquals("orUser3"));
        List<String> usernames = filteredUsernames(condition, 100, 1);
        assertTrue(usernames.contains("orUser1"));
        assertTrue(usernames.contains("orUser2"));
        assertTrue(usernames.contains("orUser3"));
        assertEquals(usernames.size(), (int) usernames.stream().distinct().count());
    }

    @Test
    public void testFilterUsersWithOrOnNotEqualUsername() throws UserStoreException {

        Condition condition = or(new ExpressionCondition(ExpressionOperation.NE.toString(),
                ExpressionAttribute.USERNAME.toString(), "orUser1"), usernameEquals("orUser1"));
        List<String> usernames = filteredUsernames(condition, 100, 1);
        assertTrue(usernames.containsAll(Arrays.asList("orUser1", "orUser2", "orUser3")));
    }

    @Test
    public void testFilterUsersWithOrIsPaginated() throws UserStoreException {

        Condition condition = or(usernameEquals("orUser1"),
                or(usernameEquals("orUser2"), usernameEquals("orUser3")));
        assertEquals(filteredUsernames(condition, 2, 1), Arrays.asList("orUser1", "orUser2"));
        assertEquals(filteredUsernames(condition, 2, 2), Arrays.asList("orUser2", "orUser3"));
        assertEquals(filteredUsernames(condition, 10, 3), Collections.singletonList("orUser3"));
    }

    @Test
    public void testCountUsersWithOr() throws UserStoreException {

        Condition condition = or(usernameEquals("orUser1"),
                or(usernameEquals("orUser2"), usernameEquals("orUser3")));
        assertEquals(userStoreManager.getUsersCount(condition, null, null, 10, 1, true), 3);
    }

    @Test
    public void testOrQueryShapeForDefaultDatabase() throws Exception {

        // The user store under test is case sensitive, hence the predicates carry no LOWER(..).
        SqlBuilder sqlBuilder = buildOrQuery("h2");
        assertEquals(sqlBuilder.getQuery(),
                "SELECT U.UM_USER_ID, U.UM_USER_NAME FROM UM_USER U WHERE U.UM_TENANT_ID = ? AND ("
                        + "U.UM_USER_NAME = ?"
                        + " OR EXISTS (SELECT 1 FROM UM_USER_ATTRIBUTE UA WHERE UA.UM_USER_ID = U.UM_ID "
                        + "AND UA.UM_TENANT_ID = ? AND UA.UM_PROFILE_ID = ? AND UA.UM_ATTR_NAME = ? "
                        + "AND UA.UM_ATTR_VALUE = ?)"
                        + " OR NOT EXISTS (SELECT 1 FROM UM_USER_ROLE UR INNER JOIN UM_ROLE R "
                        + "ON R.UM_ID = UR.UM_ROLE_ID WHERE UR.UM_USER_ID = U.UM_ID AND UR.UM_TENANT_ID = ? "
                        + "AND R.UM_TENANT_ID = ? AND R.UM_ROLE_NAME = ?)"
                        + ") ORDER BY UM_USER_NAME ASC LIMIT ? OFFSET ?");
        /*
         * Tenant id, the username value, the four parameters of the claim sub query, the three parameters of the role
         * sub query and the limit/offset pair.
         */
        assertEquals(sqlBuilder.getOrderedParameters().size(), 11);
    }

    @Test
    public void testOrQueryIsPaginatedPerDatabaseType() throws Exception {

        assertTrue(buildOrQuery("mssql").getQuery()
                .endsWith(") ORDER BY UM_USER_NAME ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY"));
        assertTrue(buildOrQuery("oracle").getQuery()
                .endsWith(") ORDER BY UM_USER_NAME) where rownum <= ?) WHERE  rnum > ?"));
        assertTrue(buildOrQuery("db2").getQuery().endsWith(") AS p) WHERE rn BETWEEN ? AND ?"));
    }

    private SqlBuilder buildOrQuery(String dbType) throws UserStoreException {

        List<ExpressionCondition> expressionConditions = Arrays.asList(
                usernameEquals("orUser1"),
                claimEquals(ATTRIBUTE_1, "alpha"),
                new ExpressionCondition(ExpressionOperation.NE.toString(), ExpressionAttribute.ROLE.toString(),
                        "orRole1"));
        return ((UniqueIDJDBCUserStoreManager) userStoreManager)
                .getQueryStringForOrOperation(expressionConditions, 10, 0, "default", dbType);
    }

    @Test
    public void testFilterUsersWithMixedAndOrIsNotSupported() {

        Condition condition = new OperationalCondition(OperationalOperation.AND.toString(),
                usernameEquals("orUser1"), or(usernameEquals("orUser2"), usernameEquals("orUser3")));
        try {
            userStoreManager.getUserListWithID(condition, null, null, 10, 1, null, null);
            fail("A filter mixing the AND and OR operations should not be accepted.");
        } catch (UserStoreException e) {
            // Expected.
        }
    }

    private Condition or(Condition left, Condition right) {

        return new OperationalCondition(OperationalOperation.OR.toString(), left, right);
    }

    private ExpressionCondition usernameEquals(String username) {

        return new ExpressionCondition(ExpressionOperation.EQ.toString(), ExpressionAttribute.USERNAME.toString(),
                username);
    }

    private ExpressionCondition groupEquals(String groupName) {

        return new ExpressionCondition(ExpressionOperation.EQ.toString(), ExpressionAttribute.ROLE.toString(),
                groupName);
    }

    private ExpressionCondition claimEquals(String attributeName, String attributeValue) {

        return new ExpressionCondition(ExpressionOperation.EQ.toString(), attributeName, attributeValue);
    }

    private ExpressionCondition claimNotEquals(String attributeName, String attributeValue) {

        return new ExpressionCondition(ExpressionOperation.NE.toString(), attributeName, attributeValue);
    }

    private List<String> filteredUsernames(Condition condition, int limit, int offset) throws UserStoreException {

        return userStoreManager.getUserListWithID(condition, null, null, limit, offset, null, null).stream()
                .map(User::getUsername).collect(Collectors.toList());
    }

    private static void deleteDirectory(File directory) {

        if (!directory.exists()) {
            return;
        }
        if (directory.isDirectory()) {
            File[] children = directory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        if (!directory.delete()) {
            directory.deleteOnExit();
        }
    }
}
