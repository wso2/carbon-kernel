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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.exceptions.HashProviderException;
import org.wso2.carbon.user.core.hash.HashProvider;
import org.wso2.carbon.user.core.hash.HashProviderFactory;
import org.wso2.carbon.user.core.hash.PasswordHashProcessor;
import org.wso2.carbon.user.core.internal.UserStoreMgtDataHolder;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.PASSWORD_HASH_DIGEST_FUNCTION;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.LEGACY_PASSWORD_HASH_DIGEST_FUNCTION;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.ENABLE_SELF_DESCRIBING_PASSWORD_HASH;

/**
 * Tests that {@link JDBCUserStoreManager#validatePassword(Object, String, String)} delegates every
 * stored hash to the {@link PasswordHashProcessor}, rather than recomputing the configured hash and
 * comparing strings. A stored hash selects the algorithm that verifies it, so a string comparison
 * against a freshly computed hash cannot be correct once stored hashes carry algorithm identifiers.
 */
public class JDBCUserStoreManagerValidatePasswordTest {

    private static final String PASSWORD = "CorrectPassword123!";
    private static final String WRONG_PASSWORD = "WrongPassword123!";
    private static final String SALT = "dGVzdC1zYWx0";

    private static final String LEGACY_ALGORITHM = "SHA-256";
    private static final String PLUGGABLE_ALGORITHM = "JDBCTESTALG";

    private final TestHashProviderFactory factory = new TestHashProviderFactory();

    @BeforeClass
    public void setUp() {

        if (StringUtils.isBlank(System.getProperty("carbon.home"))) {
            System.setProperty("carbon.home",
                    new File("../../distribution/kernel/carbon-home").getAbsolutePath());
        }
        UserStoreMgtDataHolder.getInstance().setHashProviderFactory(factory);
    }

    @AfterClass
    public void tearDown() {

        UserStoreMgtDataHolder.getInstance().unbindHashProviderFactory(factory);
    }

    /**
     * Builds a user store manager backed by a processor with the given hashing configuration. The
     * no-argument constructor leaves the processor unset, so it is injected directly; nothing in the
     * code under test touches the database.
     */
    private JDBCUserStoreManager managerFor(String digestFunction, String legacyDigestFunction)
            throws Exception {

        JDBCUserStoreManager userStoreManager = new JDBCUserStoreManager();
        Field processorField = JDBCUserStoreManager.class.getDeclaredField("passwordHashProcessor");
        processorField.setAccessible(true);
        processorField.set(userStoreManager, new PasswordHashProcessor(config(digestFunction, legacyDigestFunction)));
        return userStoreManager;
    }

    private Map<String, String> config(String digestFunction, String legacyDigestFunction) {

        Map<String, String> config = new HashMap<>();
        config.put(PASSWORD_HASH_DIGEST_FUNCTION, digestFunction);
        // The self-describing format is what these tests are about, so they opt into it.
        config.put(ENABLE_SELF_DESCRIBING_PASSWORD_HASH, "true");
        if (legacyDigestFunction != null) {
            config.put(LEGACY_PASSWORD_HASH_DIGEST_FUNCTION, legacyDigestFunction);
        }
        return config;
    }

    /**
     * A credential hashed before this feature existed is stored without an algorithm identifier.
     */
    private String legacyStoredHash() throws Exception {

        String storedHash = new PasswordHashProcessor(config(LEGACY_ALGORITHM, null))
                .hashPassword(PASSWORD, SALT);
        Assert.assertFalse(storedHash.startsWith("{"),
                "A hash produced by the MessageDigest path must carry no algorithm identifier.");
        return storedHash;
    }

    @Test
    public void testLegacyHashIsVerifiedAfterAlgorithmChange() throws Exception {

        // The provider has no native validation, so before this change the user store manager
        // short-circuited to a string comparison against a freshly computed, prefixed hash.
        Assert.assertFalse(new TestHashProvider().supportsValidateHash(),
                "This test only covers the path taken when the provider has no native validation.");

        String storedHash = legacyStoredHash();
        JDBCUserStoreManager userStoreManager = managerFor(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM);

        Assert.assertTrue(userStoreManager.validatePassword(PASSWORD, storedHash, SALT),
                "An existing user must still authenticate after the configured algorithm changes.");
    }

    @Test
    public void testLegacyHashRejectsWrongPassword() throws Exception {

        String storedHash = legacyStoredHash();
        JDBCUserStoreManager userStoreManager = managerFor(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM);

        Assert.assertFalse(userStoreManager.validatePassword(WRONG_PASSWORD, storedHash, SALT));
    }

    @Test
    public void testSelfDescribingHashIsVerified() throws Exception {

        JDBCUserStoreManager userStoreManager = managerFor(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM);
        String storedHash = new PasswordHashProcessor(config(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM))
                .hashPassword(PASSWORD, SALT);

        Assert.assertTrue(storedHash.startsWith("{\"algorithm\":\"" + PLUGGABLE_ALGORITHM + "\""),
                "A new credential must be stored with the algorithm identifier of its provider.");
        Assert.assertTrue(userStoreManager.validatePassword(PASSWORD, storedHash, SALT));
        Assert.assertFalse(userStoreManager.validatePassword(WRONG_PASSWORD, storedHash, SALT));
    }

    @Test
    public void testLegacyHashIsVerifiedWhenLegacyAlgorithmIsNotConfigured() throws Exception {

        // Deployments that never set the new property keep verifying against the configured algorithm.
        String storedHash = legacyStoredHash();
        JDBCUserStoreManager userStoreManager = managerFor(LEGACY_ALGORITHM, null);

        Assert.assertTrue(userStoreManager.validatePassword(PASSWORD, storedHash, SALT));
        Assert.assertFalse(userStoreManager.validatePassword(WRONG_PASSWORD, storedHash, SALT));
    }

    @Test
    public void testNullStoredPasswordIsRejected() throws Exception {

        JDBCUserStoreManager userStoreManager = managerFor(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM);

        Assert.assertFalse(userStoreManager.validatePassword(PASSWORD, null, SALT));
    }

    @Test
    public void testOverriddenPreparePasswordIsHonouredWhileFormatIsOff() throws Exception {

        // A subclass that hashes its own way wrote its stored values through preparePassword, so with
        // the format off they must still be verified by recomputing them through that override.
        JDBCUserStoreManager userStoreManager = new JDBCUserStoreManager() {
            @Override
            protected String preparePassword(Object password, String saltValue) {

                return "custom:" + password + ":" + saltValue;
            }
        };
        Map<String, String> config = config(LEGACY_ALGORITHM, null);
        config.remove(ENABLE_SELF_DESCRIBING_PASSWORD_HASH);
        Field processorField = JDBCUserStoreManager.class.getDeclaredField("passwordHashProcessor");
        processorField.setAccessible(true);
        processorField.set(userStoreManager, new PasswordHashProcessor(config));

        String storedHash = "custom:" + PASSWORD + ":" + SALT;
        Assert.assertTrue(userStoreManager.validatePassword(PASSWORD, storedHash, SALT));
        Assert.assertFalse(userStoreManager.validatePassword(WRONG_PASSWORD, storedHash, SALT));
    }

    @Test
    public void testOverriddenPreparePasswordWithBracePrefixIsHonouredWhileFormatIsOff() throws Exception {

        // An LDAP-style {SSHA} value opens with a brace without being JSON. With the format off it was
        // written by the override, so it must still be compared through it rather than rejected.
        JDBCUserStoreManager userStoreManager = new JDBCUserStoreManager() {
            @Override
            protected String preparePassword(Object password, String saltValue) {

                return "{SSHA}" + password + ":" + saltValue;
            }
        };
        Map<String, String> config = config(LEGACY_ALGORITHM, null);
        config.remove(ENABLE_SELF_DESCRIBING_PASSWORD_HASH);
        Field processorField = JDBCUserStoreManager.class.getDeclaredField("passwordHashProcessor");
        processorField.setAccessible(true);
        processorField.set(userStoreManager, new PasswordHashProcessor(config));

        String storedHash = "{SSHA}" + PASSWORD + ":" + SALT;
        Assert.assertTrue(userStoreManager.validatePassword(PASSWORD, storedHash, SALT));
        Assert.assertFalse(userStoreManager.validatePassword(WRONG_PASSWORD, storedHash, SALT));
    }

    @Test
    public void testSelfDescribingHashIsVerifiedAfterFormatIsTurnedOff() throws Exception {

        String storedHash = new PasswordHashProcessor(config(PLUGGABLE_ALGORITHM, null))
                .hashPassword(PASSWORD, SALT);
        Map<String, String> config = config(PLUGGABLE_ALGORITHM, null);
        config.put(ENABLE_SELF_DESCRIBING_PASSWORD_HASH, "false");
        JDBCUserStoreManager userStoreManager = new JDBCUserStoreManager();
        Field processorField = JDBCUserStoreManager.class.getDeclaredField("passwordHashProcessor");
        processorField.setAccessible(true);
        processorField.set(userStoreManager, new PasswordHashProcessor(config));

        Assert.assertTrue(userStoreManager.validatePassword(PASSWORD, storedHash, SALT),
                "A value stored in the self-describing format must stay verifiable once the format is off.");
        Assert.assertFalse(userStoreManager.validatePassword(WRONG_PASSWORD, storedHash, SALT));
    }

    @Test(expectedExceptions = UserStoreException.class)
    public void testUninitializedProcessorIsReported() throws Exception {

        new JDBCUserStoreManager().validatePassword(PASSWORD, "whatever", SALT);
    }

    /**
     * A deterministic HashProvider without native validation, matching how PBKDF2 behaves.
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
