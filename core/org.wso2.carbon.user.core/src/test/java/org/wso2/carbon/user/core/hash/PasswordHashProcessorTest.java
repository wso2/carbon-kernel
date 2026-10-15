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

package org.wso2.carbon.user.core.hash;

import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.user.core.exceptions.HashProviderException;
import org.wso2.carbon.user.core.exceptions.PasswordHashingException;
import org.wso2.carbon.user.core.internal.UserStoreMgtDataHolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.PASSWORD_HASH_ALGORITHM_PROPERTIES;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.PASSWORD_HASH_DIGEST_FUNCTION;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.PREVIOUS_PASSWORD_HASH_DIGEST_FUNCTION;

/**
 * Tests how {@link PasswordHashProcessor} routes verification between legacy hashes, which carry no
 * algorithm identifier, and self-describing hashes, which do.
 */
public class PasswordHashProcessorTest {

    private static final String PASSWORD = "CorrectPassword123!";
    private static final String WRONG_PASSWORD = "WrongPassword123!";
    private static final String SALT = "dGVzdC1zYWx0";

    private static final String LEGACY_ALGORITHM = "SHA-256";
    private static final String PLUGGABLE_ALGORITHM = "TESTALG";

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
     * A credential hashed before this feature existed is stored without an algorithm identifier.
     */
    private String legacyStoredHash() throws PasswordHashingException {

        Map<String, String> config = new HashMap<>();
        config.put(PASSWORD_HASH_DIGEST_FUNCTION, LEGACY_ALGORITHM);
        String storedHash = new PasswordHashProcessor(config).hashPassword(PASSWORD, SALT);

        Assert.assertFalse(storedHash.startsWith("{"),
                "A hash produced by the MessageDigest path must carry no algorithm identifier.");
        return storedHash;
    }

    private PasswordHashProcessor processor(String digestFunction, String previousDigestFunction,
                                            String algorithmProperties) throws PasswordHashingException {

        Map<String, String> config = new HashMap<>();
        config.put(PASSWORD_HASH_DIGEST_FUNCTION, digestFunction);
        if (previousDigestFunction != null) {
            config.put(PREVIOUS_PASSWORD_HASH_DIGEST_FUNCTION, previousDigestFunction);
        }
        if (algorithmProperties != null) {
            config.put(PASSWORD_HASH_ALGORITHM_PROPERTIES, algorithmProperties);
        }
        return new PasswordHashProcessor(config);
    }

    @Test
    public void testLegacyHashIsVerifiedAfterAlgorithmChange() throws Exception {

        String storedHash = legacyStoredHash();

        // The administrator switches new credentials to the pluggable algorithm and declares the
        // algorithm that produced the credentials already in the user store.
        PasswordHashProcessor processor = processor(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, null);

        Assert.assertTrue(processor.validatePassword(PASSWORD, storedHash, SALT),
                "An existing user must still authenticate after the configured algorithm changes.");
    }

    @Test
    public void testLegacyHashRejectsWrongPassword() throws Exception {

        String storedHash = legacyStoredHash();
        PasswordHashProcessor processor = processor(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, null);

        Assert.assertFalse(processor.validatePassword(WRONG_PASSWORD, storedHash, SALT),
                "A wrong password must be rejected against a legacy hash.");
    }

    @Test
    public void testLegacyHashIsVerifiedWhenPreviousAlgorithmIsNotConfigured() throws Exception {

        String storedHash = legacyStoredHash();

        // Deployments that never set the new property keep verifying against the configured algorithm.
        PasswordHashProcessor processor = processor(LEGACY_ALGORITHM, null, null);

        Assert.assertTrue(processor.validatePassword(PASSWORD, storedHash, SALT),
                "Verification must fall back to the configured algorithm when no legacy algorithm is set.");
        Assert.assertFalse(processor.validatePassword(WRONG_PASSWORD, storedHash, SALT),
                "A wrong password must be rejected when falling back to the configured algorithm.");
    }

    @Test
    public void testNewHashIsSelfDescribingAndVerifies() throws Exception {

        PasswordHashProcessor processor = processor(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, null);
        String storedHash = processor.hashPassword(PASSWORD, SALT);

        Assert.assertTrue(storedHash.startsWith("{" + PLUGGABLE_ALGORITHM + "}"),
                "A new credential must carry the algorithm identifier of the provider that produced it.");
        Assert.assertTrue(processor.validatePassword(PASSWORD, storedHash, SALT));
        Assert.assertFalse(processor.validatePassword(WRONG_PASSWORD, storedHash, SALT));
    }

    @Test
    public void testConfiguredAlgorithmParametersSurviveVerification() throws Exception {

        // The provider is configured with non-default parameters. Verification has to reuse them,
        // otherwise the recomputed hash differs from the stored one.
        PasswordHashProcessor processor =
                processor(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, "{\"rounds\":\"3\"}");
        String storedHash = processor.hashPassword(PASSWORD, SALT);

        Assert.assertTrue(processor.validatePassword(PASSWORD, storedHash, SALT),
                "Verification must use the configured algorithm parameters, not the provider defaults.");
    }

    @Test
    public void testUnknownAlgorithmIdentifierIsNotRetriedAsLegacy() throws Exception {

        // The payload on its own would verify as a legacy hash. The unknown identifier must still
        // fail verification rather than fall through to the legacy algorithm.
        String payload = legacyStoredHash();
        PasswordHashProcessor processor = processor(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, null);

        Assert.assertFalse(processor.validatePassword(PASSWORD, "{NOSUCHALG}" + payload, SALT),
                "An unregistered algorithm identifier must fail verification.");
    }

    @Test
    public void testMalformedStoredValueIsRejected() throws Exception {

        PasswordHashProcessor processor = processor(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, null);

        Assert.assertFalse(processor.validatePassword(PASSWORD, "{" + PLUGGABLE_ALGORITHM, SALT));
        Assert.assertFalse(processor.validatePassword(PASSWORD, "{}payload", SALT));
        Assert.assertFalse(processor.validatePassword(PASSWORD, null, SALT));
    }

    /**
     * A deterministic HashProvider whose output depends on a configurable round count, so that a
     * provider built with default parameters produces a different hash from a configured one.
     */
    private static class TestHashProvider implements HashProvider {

        private static final String ROUNDS = "rounds";
        private int rounds = 1;

        @Override
        public void init() {

            rounds = 1;
        }

        @Override
        public void init(Map<String, Object> initProperties) {

            Object configuredRounds = initProperties.get(ROUNDS);
            if (configuredRounds != null) {
                rounds = Integer.parseInt(configuredRounds.toString());
            }
        }

        @Override
        public byte[] calculateHash(char[] plainText, String salt) throws HashProviderException {

            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] value = (new String(plainText) + (salt == null ? "" : salt))
                        .getBytes(StandardCharsets.UTF_8);
                for (int round = 0; round < rounds; round++) {
                    value = messageDigest.digest(value);
                }
                return value;
            } catch (NoSuchAlgorithmException e) {
                throw new HashProviderException("Could not calculate the test hash.", e);
            }
        }

        @Override
        public Map<String, Object> getParameters() {

            return Collections.singletonMap(ROUNDS, rounds);
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
        public HashProvider getHashProvider(Map<String, Object> initProperties) throws HashProviderException {

            TestHashProvider hashProvider = new TestHashProvider();
            hashProvider.init(initProperties);
            return hashProvider;
        }

        @Override
        public Set<String> getHashProviderConfigProperties() {

            return Collections.singleton(TestHashProvider.ROUNDS);
        }

        @Override
        public String getAlgorithm() {

            return PLUGGABLE_ALGORITHM;
        }
    }
}
