/*
 * Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.jdbc;

import org.apache.commons.lang.StringUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.exceptions.HashProviderException;
import org.wso2.carbon.user.core.hash.HashProvider;
import org.wso2.carbon.user.core.hash.HashProviderFactory;
import org.wso2.carbon.user.core.hash.PasswordHashProcessor;
import org.wso2.carbon.user.core.hash.SelfDescribingHashParser;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.internal.UserStoreMgtDataHolder;
import org.wso2.carbon.user.core.jdbc.caseinsensitive.JDBCCaseInsensitiveConstants;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.PASSWORD_HASH_DIGEST_FUNCTION;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.LEGACY_PASSWORD_HASH_DIGEST_FUNCTION;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.ENABLE_SELF_DESCRIBING_PASSWORD_HASH;

/**
 * Tests {@link JDBCUserStoreManager#migratePasswordHash(Connection, String, Object, String, String)} against a
 * real table, covering the integrity rules the rewrite has to honour: only a stored value without an
 * algorithm identifier is rewritten, the rewrite is skipped when the stored value changed after it was
 * verified, and the password lifecycle columns are left untouched.
 */
public class JDBCUserStoreManagerMigrationTest {

    private static final String PASSWORD = "CorrectPassword123!";
    private static final String SALT = "dGVzdC1zYWx0";
    private static final String USER_NAME = "legacyuser";
    private static final int TENANT_ID = -1234;

    private static final String LEGACY_ALGORITHM = "SHA-256";
    private static final String PLUGGABLE_ALGORITHM = "MIGRATIONTESTALG";
    private static final String SUPERSEDED_ALGORITHM = "MIGRATIONTESTOLDALG";

    private static final Timestamp CHANGED_TIME = Timestamp.valueOf("2020-01-02 03:04:05");

    private final TestHashProviderFactory factory = new TestHashProviderFactory();
    private JdbcDataSource dataSource;

    @BeforeClass
    public void setUpClass() throws Exception {

        if (StringUtils.isBlank(System.getProperty("carbon.home"))) {
            System.setProperty("carbon.home",
                    new File("../../distribution/kernel/carbon-home").getAbsolutePath());
        }
        UserStoreMgtDataHolder.getInstance().setHashProviderFactory(factory);

        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:migrationtest;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE UM_USER ("
                    + "UM_ID INT AUTO_INCREMENT PRIMARY KEY, "
                    + "UM_USER_NAME VARCHAR(255), "
                    + "UM_USER_PASSWORD VARCHAR(1024), "
                    + "UM_SALT_VALUE VARCHAR(255), "
                    + "UM_REQUIRE_CHANGE BOOLEAN, "
                    + "UM_CHANGED_TIME TIMESTAMP, "
                    + "UM_TENANT_ID INT)");
        }
    }

    @AfterClass
    public void tearDownClass() {

        UserStoreMgtDataHolder.getInstance().unbindHashProviderFactory(factory);
    }

    @BeforeMethod
    public void resetTable() throws Exception {

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM UM_USER");
        }
    }

    /**
     * A user store manager wired to the in-memory table, with the hashing configuration supplied
     * directly. The no-argument constructor leaves both unset, so nothing else is initialised.
     */
    private JDBCUserStoreManager userStoreManager(String digestFunction, String legacyDigestFunction,
                                                  String upgradeEnabled) throws Exception {

        return userStoreManager(digestFunction, legacyDigestFunction, upgradeEnabled, false);
    }

    private JDBCUserStoreManager userStoreManager(String digestFunction, String legacyDigestFunction,
                                                  String upgradeEnabled, boolean caseInsensitiveUserName)
            throws Exception {

        return userStoreManager(digestFunction, legacyDigestFunction, upgradeEnabled,
                caseInsensitiveUserName, "true");
    }

    private JDBCUserStoreManager userStoreManager(String digestFunction, String legacyDigestFunction,
                                                  String upgradeEnabled, boolean caseInsensitiveUserName,
                                                  String selfDescribingEnabled)
            throws Exception {

        Map<String, String> properties = new HashMap<>();
        properties.put(UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME, String.valueOf(caseInsensitiveUserName));
        properties.put(PASSWORD_HASH_DIGEST_FUNCTION, digestFunction);
        properties.put(LEGACY_PASSWORD_HASH_DIGEST_FUNCTION, legacyDigestFunction);
        // The rewrite only has somewhere to go once the self-describing format is on, so these
        // tests opt into it. That the rewrite stops when it is off is covered separately, below.
        properties.put(ENABLE_SELF_DESCRIBING_PASSWORD_HASH, selfDescribingEnabled);
        properties.put(JDBCRealmConstants.UPDATE_USER_PASSWORD_HASH,
                JDBCRealmConstants.UPDATE_USER_PASSWORD_HASH_SQL);
        properties.put(JDBCCaseInsensitiveConstants.UPDATE_USER_PASSWORD_HASH_CASE_INSENSITIVE,
                JDBCCaseInsensitiveConstants.UPDATE_USER_PASSWORD_HASH_SQL_CASE_INSENSITIVE);
        if (upgradeEnabled != null) {
            properties.put(JDBCRealmConstants.ENABLE_PASSWORD_HASH_UPGRADE, upgradeEnabled);
        }

        RealmConfiguration realmConfiguration = new RealmConfiguration();
        realmConfiguration.setUserStoreProperties(properties);

        JDBCUserStoreManager userStoreManager = new JDBCUserStoreManager();
        setField(userStoreManager, JDBCUserStoreManager.class, "jdbcds", dataSource);
        setField(userStoreManager, JDBCUserStoreManager.class, "passwordHashProcessor",
                new PasswordHashProcessor(properties));
        setField(userStoreManager, userStoreManager.getClass().getSuperclass(), "realmConfig", realmConfiguration);
        setField(userStoreManager, userStoreManager.getClass().getSuperclass(), "tenantId", TENANT_ID);
        return userStoreManager;
    }

    private void setField(Object target, Class<?> declaringClass, String name, Object value) throws Exception {

        Field field = declaringClass.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Writes the credential exactly as the MessageDigest path would have, which is a Base64 hash with
     * no algorithm identifier.
     */
    private String insertUser(String storedPassword) throws Exception {

        return insertUser(USER_NAME, storedPassword);
    }

    private String insertUser(String userName, String storedPassword) throws Exception {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO UM_USER (UM_USER_NAME, "
                     + "UM_USER_PASSWORD, UM_SALT_VALUE, UM_REQUIRE_CHANGE, UM_CHANGED_TIME, UM_TENANT_ID) "
                     + "VALUES (?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, userName);
            statement.setString(2, storedPassword);
            statement.setString(3, SALT);
            statement.setBoolean(4, true);
            statement.setTimestamp(5, CHANGED_TIME);
            statement.setInt(6, TENANT_ID);
            statement.executeUpdate();
        }
        return storedPassword;
    }

    private String legacyHash() throws Exception {

        Map<String, String> legacyConfig = new HashMap<>();
        legacyConfig.put(PASSWORD_HASH_DIGEST_FUNCTION, LEGACY_ALGORITHM);
        String storedHash = new PasswordHashProcessor(legacyConfig).hashPassword(PASSWORD, SALT);

        Assert.assertFalse(storedHash.startsWith("{"),
                "A hash produced by the MessageDigest path must carry no algorithm identifier.");
        return storedHash;
    }

    private String storedPassword() throws Exception {

        return readColumn("UM_USER_PASSWORD", String.class);
    }

    /**
     * Migrates through a connection that does not auto-commit, as the authentication flow does. The
     * result is read back through another connection, so a missing commit fails the test.
     */
    private void migrate(JDBCUserStoreManager userStoreManager, String userName, Object credential,
                         String storedHash, String saltValue) throws Exception {

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            userStoreManager.migratePasswordHash(connection, userName, credential, storedHash, saltValue);
        }
    }

    private <T> T readColumn(String column, Class<T> type) throws Exception {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + column + " FROM UM_USER WHERE UM_USER_NAME=?")) {
            statement.setString(1, USER_NAME);
            try (ResultSet resultSet = statement.executeQuery()) {
                Assert.assertTrue(resultSet.next(), "The test user row is missing.");
                return resultSet.getObject(1, type);
            }
        }
    }

    @Test
    public void testLegacyHashIsRewrittenInTheSelfDescribingFormat() throws Exception {

        String storedHash = insertUser(legacyHash());
        JDBCUserStoreManager userStoreManager =
                userStoreManager(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, "true");

        migrate(userStoreManager, USER_NAME, PASSWORD, storedHash, SALT);

        Assert.assertTrue(storedPassword().startsWith("{\"algorithm\":\"" + PLUGGABLE_ALGORITHM + "\""),
                "A verified legacy hash must be rewritten with the configured algorithm's identifier.");
    }

    @Test
    public void testRewrittenHashStillVerifies() throws Exception {

        String storedHash = insertUser(legacyHash());
        JDBCUserStoreManager userStoreManager =
                userStoreManager(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, "true");

        migrate(userStoreManager, USER_NAME, PASSWORD, storedHash, SALT);

        Assert.assertTrue(userStoreManager.validatePassword(PASSWORD, storedPassword(), SALT),
                "The user must still authenticate with the same password after the rewrite.");
    }

    @Test
    public void testHashInTheConfiguredAlgorithmIsLeftAlone() throws Exception {

        // Rule 5: a value the configured algorithm already produced is verified and left untouched.
        String storedHash = insertUser(SelfDescribingHashParser.format(PLUGGABLE_ALGORITHM, "already-migrated"));
        JDBCUserStoreManager userStoreManager =
                userStoreManager(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, "true");

        migrate(userStoreManager, USER_NAME, PASSWORD, storedHash, SALT);

        Assert.assertEquals(storedPassword(), storedHash,
                "A value already produced by the configured algorithm must not be rewritten.");
    }

    @Test
    public void testHashNamingASupersededAlgorithmIsRewritten() throws Exception {

        // Rule 5: a self-describing value naming an algorithm other than the configured one is
        // rewritten, so a user store converges on the configured algorithm as its users sign in.
        String storedHash = insertUser(SelfDescribingHashParser.format(SUPERSEDED_ALGORITHM, "produced-by-the-old-algorithm"));
        JDBCUserStoreManager userStoreManager =
                userStoreManager(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, "true");

        migrate(userStoreManager, USER_NAME, PASSWORD, storedHash, SALT);

        Assert.assertNotEquals(storedPassword(), storedHash,
                "A value naming a superseded algorithm must be rewritten.");
        Assert.assertTrue(storedPassword().startsWith("{\"algorithm\":\"" + PLUGGABLE_ALGORITHM + "\""),
                "The rewrite must carry the identifier of the configured algorithm.");
    }

    @Test
    public void testUpgradeIsSkippedWhenDisabled() throws Exception {

        String storedHash = insertUser(legacyHash());
        JDBCUserStoreManager userStoreManager =
                userStoreManager(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, "false");

        migrate(userStoreManager, USER_NAME, PASSWORD, storedHash, SALT);

        Assert.assertEquals(storedPassword(), storedHash,
                "No rewrite may happen while the upgrade is switched off.");
    }

    @Test
    public void testRewriteStopsWhenTheSelfDescribingFormatIsOff() throws Exception {

        // The upgrade property is left at its default of true, so only the format being off can
        // stop the rewrite. With it off the configured algorithm produces no identifier, which the
        // rewrite detects and abandons rather than writing a value it cannot route later.
        String storedHash = insertUser(legacyHash());
        JDBCUserStoreManager userStoreManager =
                userStoreManager(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, null, false, "false");

        migrate(userStoreManager, USER_NAME, PASSWORD, storedHash, SALT);

        Assert.assertEquals(storedPassword(), storedHash,
                "No rewrite may happen while the self-describing format is switched off.");
    }

    @Test
    public void testUpgradeAppliesWhenThePropertyIsNotConfigured() throws Exception {

        // The property declares a default of true, so a user store that never set it still upgrades.
        String storedHash = insertUser(legacyHash());
        JDBCUserStoreManager userStoreManager = userStoreManager(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, null);

        migrate(userStoreManager, USER_NAME, PASSWORD, storedHash, SALT);

        Assert.assertTrue(storedPassword().startsWith("{\"algorithm\":\"" + PLUGGABLE_ALGORITHM + "\""),
                "An unset upgrade property must behave as the declared default of true.");
    }

    @Test
    public void testStoredValueChangedSinceVerificationIsNotOverwritten() throws Exception {

        // Rule 4: the rewrite applies only while the stored value is still the one that was verified.
        insertUser(SelfDescribingHashParser.format(PLUGGABLE_ALGORITHM, "written-by-a-concurrent-password-change"));
        JDBCUserStoreManager userStoreManager =
                userStoreManager(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, "true");

        migrate(userStoreManager, USER_NAME, PASSWORD, legacyHash(), SALT);

        Assert.assertEquals(storedPassword(), SelfDescribingHashParser.format(PLUGGABLE_ALGORITHM, "written-by-a-concurrent-password-change"),
                "A credential changed after verification must not be overwritten by the rewrite.");
    }

    @Test
    public void testPasswordLifecycleColumnsArePreserved() throws Exception {

        // Rule 2: the rewrite touches the stored hash and nothing else.
        String storedHash = insertUser(legacyHash());
        JDBCUserStoreManager userStoreManager =
                userStoreManager(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, "true");

        migrate(userStoreManager, USER_NAME, PASSWORD, storedHash, SALT);

        Assert.assertNotEquals(storedPassword(), storedHash, "The hash should have been rewritten.");
        Assert.assertEquals(readColumn("UM_CHANGED_TIME", Timestamp.class), CHANGED_TIME,
                "UM_CHANGED_TIME drives password expiry and must survive the rewrite.");
        Assert.assertEquals(readColumn("UM_REQUIRE_CHANGE", Boolean.class), Boolean.TRUE,
                "UM_REQUIRE_CHANGE must survive the rewrite.");
        Assert.assertEquals(readColumn("UM_SALT_VALUE", String.class), SALT,
                "The salt is reused, so UM_SALT_VALUE must be left as it is.");
    }

    @Test
    public void testCaseInsensitiveUserStoreRewritesTheHash() throws Exception {

        // The stored name differs in case from the one authentication carried, which only the
        // case-insensitive statement matches.
        String storedHash = insertUser("LegacyUser", legacyHash());
        JDBCUserStoreManager userStoreManager =
                userStoreManager(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, "true", true);

        migrate(userStoreManager, "legacyuser", PASSWORD, storedHash, SALT);

        Assert.assertTrue(onlyStoredPassword().startsWith("{\"algorithm\":\"" + PLUGGABLE_ALGORITHM + "\""),
                "A user store with case insensitive user names must still have its hash rewritten.");
    }

    @Test
    public void testCaseSensitiveUserStoreDoesNotMatchADifferentlyCasedName() throws Exception {

        // The same inputs against a case sensitive user store must not match, which shows the two
        // statements are not interchangeable.
        String storedHash = insertUser("LegacyUser", legacyHash());
        JDBCUserStoreManager userStoreManager =
                userStoreManager(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, "true", false);

        migrate(userStoreManager, "legacyuser", PASSWORD, storedHash, SALT);

        Assert.assertEquals(onlyStoredPassword(), storedHash,
                "A case sensitive user store must not rewrite a row whose user name differs in case.");
    }

    /**
     * Reads the stored hash of the single row each test inserts, without assuming how the user name
     * was cased.
     */
    private String onlyStoredPassword() throws Exception {

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT UM_USER_PASSWORD FROM UM_USER")) {
            Assert.assertTrue(resultSet.next(), "The test user row is missing.");
            return resultSet.getString(1);
        }
    }

    /**
     * A deterministic HashProvider, matching how PBKDF2 behaves.
     */
    private static class TestHashProvider implements HashProvider {

        @Override
        public void init() {

        }

        @Override
        public void init(Map<String, Object> initProperties) {

        }

        @Override
        public byte[] calculateHash(char[] plainText, String salt) throws HashProviderException {

            try {
                return MessageDigest.getInstance("SHA-256").digest(
                        (new String(plainText) + (salt == null ? "" : salt)).getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                throw new HashProviderException("Could not calculate the test hash.", e);
            }
        }

        @Override
        public Map<String, Object> getParameters() {

            return Collections.emptyMap();
        }

        @Override
        public String getAlgorithm() {

            return PLUGGABLE_ALGORITHM;
        }
    }

    private static class TestHashProviderFactory implements HashProviderFactory {

        @Override
        public HashProvider getHashProvider() {

            return new TestHashProvider();
        }

        @Override
        public HashProvider getHashProvider(Map<String, Object> initProperties) {

            return new TestHashProvider();
        }

        @Override
        public Set<String> getHashProviderConfigProperties() {

            return Collections.emptySet();
        }

        @Override
        public String getAlgorithm() {

            return PLUGGABLE_ALGORITHM;
        }
    }
}
