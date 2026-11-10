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
import org.wso2.carbon.utils.Secret;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.PASSWORD_HASH_ALGORITHM_PROPERTIES;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.PASSWORD_HASH_DIGEST_FUNCTION;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.LEGACY_PASSWORD_HASH_DIGEST_FUNCTION;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.ENABLE_SELF_DESCRIBING_PASSWORD_HASH;

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
    private static final String NATIVE_ALGORITHM = "NATIVEVALIDATINGALG";
    private static final String PLAIN_TEXT_ALGORITHM = "PLAIN_TEXT";

    private final TestHashProviderFactory factory = new TestHashProviderFactory();
    private final NativeValidatingHashProviderFactory nativeFactory = new NativeValidatingHashProviderFactory();

    @BeforeClass
    public void setUp() {

        if (StringUtils.isBlank(System.getProperty("carbon.home"))) {
            System.setProperty("carbon.home",
                    new File("../../distribution/kernel/carbon-home").getAbsolutePath());
        }
        UserStoreMgtDataHolder.getInstance().setHashProviderFactory(factory);
        UserStoreMgtDataHolder.getInstance().setHashProviderFactory(nativeFactory);
    }

    @AfterClass
    public void tearDown() {

        UserStoreMgtDataHolder.getInstance().unbindHashProviderFactory(factory);
        UserStoreMgtDataHolder.getInstance().unbindHashProviderFactory(nativeFactory);
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

    private PasswordHashProcessor processor(String digestFunction, String legacyDigestFunction,
                                            String algorithmProperties) throws PasswordHashingException {

        Map<String, String> config = new HashMap<>();
        config.put(PASSWORD_HASH_DIGEST_FUNCTION, digestFunction);
        // These tests exercise the self-describing format, which a store has to opt into. The
        // default-off behaviour is covered separately, below.
        config.put(ENABLE_SELF_DESCRIBING_PASSWORD_HASH, "true");
        if (legacyDigestFunction != null) {
            config.put(LEGACY_PASSWORD_HASH_DIGEST_FUNCTION, legacyDigestFunction);
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
    public void testLegacyHashIsVerifiedWhenLegacyAlgorithmIsNotConfigured() throws Exception {

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

        Assert.assertTrue(storedHash.startsWith("{\"algorithm\":\"" + PLUGGABLE_ALGORITHM + "\""),
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
    public void testVerificationDoesNotMutateTheSuppliedCredential() throws Exception {

        String storedHash = legacyStoredHash();
        PasswordHashProcessor processor = processor(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, null);

        // Callers hand in a Secret, and Secret.getSecret() returns it by reference rather than
        // copying, so anything verification does to it is visible to the caller afterwards.
        Secret credential = Secret.getSecret(PASSWORD);

        Assert.assertTrue(processor.validatePassword(credential, storedHash, SALT));
        Assert.assertEquals(new String(credential.getChars()), PASSWORD,
                "Verification must leave the credential the caller passed in unchanged.");
    }

    @Test
    public void testPlainTextCredentialBeginningWithAnIdentifierStillVerifies() throws Exception {

        // A plaintext store holds the credential itself, so a password such as {curly}Password1 is
        // stored verbatim and must not be mistaken for a value carrying an algorithm identifier.
        String password = "{curly}Password1";
        PasswordHashProcessor processor = processor(PLAIN_TEXT_ALGORITHM, null, null);
        String storedHash = processor.hashPassword(password, null);

        Assert.assertEquals(storedHash, password, "A plaintext store holds the credential unchanged.");
        Assert.assertTrue(processor.validatePassword(password, storedHash, null),
                "A plaintext credential that begins like an identifier must still verify.");
        Assert.assertFalse(processor.validatePassword(WRONG_PASSWORD, storedHash, null));
    }

    @Test
    public void testPlainTextCredentialThatOpensAnObjectWithoutBeingOneStillVerifies() throws Exception {

        // A password such as {hello opens a JSON object without being one, so it parses as a
        // damaged value rather than a self-describing one. In a plaintext store it is still only a
        // password, and must be compared literally before the stored value is judged well formed.
        String password = "{hello";
        PasswordHashProcessor processor = processor(PLAIN_TEXT_ALGORITHM, null, null);
        String storedHash = processor.hashPassword(password, null);

        Assert.assertEquals(storedHash, password, "A plaintext store holds the credential unchanged.");
        Assert.assertTrue(processor.validatePassword(password, storedHash, null),
                "A plaintext credential that is not well formed JSON must still verify.");
        Assert.assertFalse(processor.validatePassword(WRONG_PASSWORD, storedHash, null));
    }

    @Test
    public void testStoredHashRecordsTheParametersThatProducedIt() throws Exception {

        PasswordHashProcessor processor = processor(PLUGGABLE_ALGORITHM, null, "{\"rounds\":\"3\"}");

        String storedHash = processor.hashPassword(PASSWORD, SALT);

        SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse(storedHash);
        Assert.assertEquals(result.getProperties().get("rounds"), "3",
                "A new credential must record the parameters the provider actually used.");
    }

    @Test
    public void testStoredHashRecordsProviderDefaultsWhenNothingIsConfigured() throws Exception {

        // Recording the defaults is what keeps a credential verifiable if a later release raises
        // them: the stored value names the parameters it was made with, not the ones in force now.
        PasswordHashProcessor processor = processor(PLUGGABLE_ALGORITHM, null, null);

        String storedHash = processor.hashPassword(PASSWORD, SALT);

        SelfDescribingHashParser.ParseResult result = SelfDescribingHashParser.parse(storedHash);
        Assert.assertEquals(result.getProperties().get("rounds"), "1",
                "A store that configures nothing must still record the provider's own defaults.");
    }

    @Test
    public void testCredentialStillVerifiesAfterTheConfiguredParametersChange() throws Exception {

        // The point of recording parameters: a credential written at three rounds must keep
        // verifying once the store is reconfigured to five, without any action from the user.
        PasswordHashProcessor atThreeRounds = processor(PLUGGABLE_ALGORITHM, null, "{\"rounds\":\"3\"}");
        String storedHash = atThreeRounds.hashPassword(PASSWORD, SALT);

        PasswordHashProcessor atFiveRounds = processor(PLUGGABLE_ALGORITHM, null, "{\"rounds\":\"5\"}");

        Assert.assertTrue(atFiveRounds.validatePassword(PASSWORD, storedHash, SALT),
                "A credential must stay verifiable after the configured parameters change.");
        Assert.assertFalse(atFiveRounds.validatePassword(WRONG_PASSWORD, storedHash, SALT),
                "Verifying against the recorded parameters must not weaken the comparison.");
    }

    @Test
    public void testCredentialWrittenAtTheNewParametersAlsoVerifies() throws Exception {

        // The reconfigured store keeps writing at its own parameters, so both generations coexist.
        PasswordHashProcessor atFiveRounds = processor(PLUGGABLE_ALGORITHM, null, "{\"rounds\":\"5\"}");

        String storedHash = atFiveRounds.hashPassword(PASSWORD, SALT);

        Assert.assertEquals(SelfDescribingHashParser.parse(storedHash).getProperties().get("rounds"), "5");
        Assert.assertTrue(atFiveRounds.validatePassword(PASSWORD, storedHash, SALT));
    }

    @Test
    public void testDamagedStoredValueFailsVerificationWhenHashingIsConfigured() throws Exception {

        // Outside a plaintext store the same shape is a damaged record, and failing closed is the
        // only safe reading of it: there is no identifier to route verification to.
        PasswordHashProcessor processor = processor(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, null);

        Assert.assertFalse(processor.validatePassword(PASSWORD, "{hello", SALT),
                "A damaged stored value must not verify any credential.");
    }

    @Test
    public void testSelfDescribingHashStillRoutesWhileLegacyValuesArePlainText() throws Exception {

        // Migrating away from plaintext: new credentials carry an identifier and must still be
        // routed by it, even though legacy values in the same store are plaintext.
        PasswordHashProcessor processor = processor(PLUGGABLE_ALGORITHM, PLAIN_TEXT_ALGORITHM, null);
        String storedHash = processor.hashPassword(PASSWORD, SALT);

        Assert.assertTrue(storedHash.startsWith("{\"algorithm\":\"" + PLUGGABLE_ALGORITHM + "\""));
        Assert.assertTrue(processor.validatePassword(PASSWORD, storedHash, SALT),
                "A self-describing value must still reach its provider in a plaintext legacy store.");
        Assert.assertFalse(processor.validatePassword(WRONG_PASSWORD, storedHash, SALT));
    }

    @Test
    public void testMalformedStoredValueIsRejected() throws Exception {

        PasswordHashProcessor processor = processor(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, null);

        Assert.assertFalse(processor.validatePassword(PASSWORD, "{" + PLUGGABLE_ALGORITHM, SALT));
        Assert.assertFalse(processor.validatePassword(PASSWORD, "{}payload", SALT));
        Assert.assertFalse(processor.validatePassword(PASSWORD, null, SALT));
    }

    @Test
    public void testNativeValidatingProviderProducesADifferentHashEachTime() throws Exception {

        // BCrypt generates its own salt per call, so recomputing and comparing could never verify
        // such a hash. This is why verification goes through validateHash rather than a comparison.
        PasswordHashProcessor processor = processor(NATIVE_ALGORITHM, LEGACY_ALGORITHM, null);

        Assert.assertNotEquals(processor.hashPassword(PASSWORD, SALT), processor.hashPassword(PASSWORD, SALT),
                "A provider that salts internally must produce a different hash on each call.");
    }

    @Test
    public void testNativeValidatingProviderHashIsSelfDescribingAndVerifies() throws Exception {

        // The payload survives the Base64 round trip the stored format puts it through, and reaches
        // the provider's own validateHash in one piece.
        PasswordHashProcessor processor = processor(NATIVE_ALGORITHM, LEGACY_ALGORITHM, null);
        String storedHash = processor.hashPassword(PASSWORD, SALT);

        Assert.assertTrue(storedHash.startsWith("{\"algorithm\":\"" + NATIVE_ALGORITHM + "\""));
        Assert.assertTrue(processor.validatePassword(PASSWORD, storedHash, SALT),
                "A provider with native validation must verify its own stored hash.");
        Assert.assertFalse(processor.validatePassword(WRONG_PASSWORD, storedHash, SALT));
    }

    @Test
    public void testLegacyHashIsVerifiedWhileNativeValidatingProviderIsConfigured() throws Exception {

        // The algorithm change this feature exists for, with a provider that validates natively.
        String storedHash = legacyStoredHash();
        PasswordHashProcessor processor = processor(NATIVE_ALGORITHM, LEGACY_ALGORITHM, null);

        Assert.assertTrue(processor.validatePassword(PASSWORD, storedHash, SALT));
        Assert.assertFalse(processor.validatePassword(WRONG_PASSWORD, storedHash, SALT));
    }

    @Test
    public void testHashOfOneProviderIsNotVerifiedByAnother() throws Exception {

        // The identifier, not the user store configuration, decides which provider verifies a value.
        String nativeHash = processor(NATIVE_ALGORITHM, LEGACY_ALGORITHM, null).hashPassword(PASSWORD, SALT);
        PasswordHashProcessor processor = processor(PLUGGABLE_ALGORITHM, LEGACY_ALGORITHM, null);

        Assert.assertTrue(processor.validatePassword(PASSWORD, nativeHash, SALT),
                "A stored hash must be routed to the provider its identifier names, whatever is configured.");
    }

    /**
     * Builds a processor with a declared legacy algorithm that is genuinely different from the
     * configured one, which is what a store looks like while its algorithm is being changed.
     * <p>
     * The rounds are raised above the provider's default of one on purpose: at one round the fake
     * provider digests exactly as the legacy SHA-256 path does, so the two would agree by accident
     * and a value verified against the wrong algorithm would still pass.
     * </p>
     *
     * @param selfDescribing The value to configure, or null to leave the property unset.
     */
    private PasswordHashProcessor processorWithFormat(String selfDescribing) throws PasswordHashingException {

        Map<String, String> config = new HashMap<>();
        config.put(PASSWORD_HASH_DIGEST_FUNCTION, PLUGGABLE_ALGORITHM);
        config.put(LEGACY_PASSWORD_HASH_DIGEST_FUNCTION, LEGACY_ALGORITHM);
        config.put(PASSWORD_HASH_ALGORITHM_PROPERTIES, "{\"rounds\":\"2\"}");
        if (selfDescribing != null) {
            config.put(ENABLE_SELF_DESCRIBING_PASSWORD_HASH, selfDescribing);
        }
        return new PasswordHashProcessor(config);
    }

    @Test
    public void testTheFakeProviderDiffersFromTheLegacyAlgorithm() throws Exception {

        // Guards the tests below: at the provider's default of one round its output is byte for byte
        // the legacy SHA-256 digest, which would let a value verified against the wrong algorithm
        // pass. The raised rounds are what makes the two distinguishable.
        String pluggable = processorWithFormat("false").hashPassword(PASSWORD, SALT);

        Assert.assertNotEquals(pluggable, legacyStoredHash(),
                "The configured and legacy algorithms must produce different values for these tests "
                        + "to be able to tell them apart.");
    }

    @Test
    public void testFormatIsOffWhenTheStoreConfiguresNothing() throws Exception {

        // An existing store carries no such property, and must keep writing what it wrote before.
        String storedHash = processorWithFormat(null).hashPassword(PASSWORD, SALT);

        Assert.assertFalse(storedHash.startsWith("{"),
                "A store that has not opted in must keep writing the bare payload.");
    }

    @Test
    public void testFormatIsOffWhenExplicitlyDisabled() throws Exception {

        String storedHash = processorWithFormat("false").hashPassword(PASSWORD, SALT);

        Assert.assertFalse(storedHash.startsWith("{"),
                "Turning the property off must produce the bare payload.");
    }

    @Test
    public void testCredentialWrittenWithTheFormatOffStillVerifies() throws Exception {

        PasswordHashProcessor processor = processorWithFormat("false");
        String storedHash = processor.hashPassword(PASSWORD, SALT);

        Assert.assertTrue(processor.validatePassword(PASSWORD, storedHash, SALT),
                "A credential written with the format off must verify through the legacy path.");
        Assert.assertFalse(processor.validatePassword(WRONG_PASSWORD, storedHash, SALT),
                "A wrong password must still be rejected with the format off.");
    }

    @Test
    public void testSelfDescribingCredentialSurvivesTheFormatBeingTurnedOff() throws Exception {

        // A store enables the format, writes credentials, then turns it off again. Verification is
        // deliberately not gated on the property, so those credentials must not become unusable.
        String storedHash = processorWithFormat("true").hashPassword(PASSWORD, SALT);
        Assert.assertTrue(storedHash.startsWith("{"), "Guard: the value under test must be self-describing.");

        Assert.assertTrue(processorWithFormat("false").validatePassword(PASSWORD, storedHash, SALT),
                "A self-describing credential must stay verifiable after the format is turned off.");
    }

    /**
     * A HashProvider that generates its own salt and carries it inside its output, the way BCrypt
     * does, and therefore validates natively instead of by recomputing and comparing.
     */
    private static class NativeValidatingHashProvider implements HashProvider {

        private static final char SEPARATOR = '$';
        private static final SecureRandom RANDOM = new SecureRandom();

        @Override
        public void init() {

        }

        @Override
        public void init(Map<String, Object> initProperties) {

        }

        @Override
        public byte[] calculateHash(char[] plainText, String salt) throws HashProviderException {

            String ownSalt = Long.toHexString(RANDOM.nextLong());
            return (ownSalt + SEPARATOR + digest(plainText, ownSalt)).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public boolean supportsValidateHash() {

            return true;
        }

        @Override
        public boolean validateHash(char[] plainText, byte[] hashedPassword, String salt)
                throws HashProviderException {

            if (plainText == null || hashedPassword == null) {
                return false;
            }
            String stored = new String(hashedPassword, StandardCharsets.UTF_8);
            int separator = stored.indexOf(SEPARATOR);
            if (separator < 0) {
                return false;
            }
            String ownSalt = stored.substring(0, separator);
            return stored.substring(separator + 1).equals(digest(plainText, ownSalt));
        }

        private String digest(char[] plainText, String ownSalt) throws HashProviderException {

            try {
                byte[] value = MessageDigest.getInstance("SHA-256")
                        .digest((new String(plainText) + ownSalt).getBytes(StandardCharsets.UTF_8));
                StringBuilder hex = new StringBuilder();
                for (byte b : value) {
                    hex.append(String.format("%02x", b));
                }
                return hex.toString();
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

            return NATIVE_ALGORITHM;
        }
    }

    private static class NativeValidatingHashProviderFactory implements HashProviderFactory {

        @Override
        public HashProvider getHashProvider() {

            return new NativeValidatingHashProvider();
        }

        @Override
        public HashProvider getHashProvider(Map<String, Object> initProperties) {

            return new NativeValidatingHashProvider();
        }

        @Override
        public Set<String> getHashProviderConfigProperties() {

            return Collections.emptySet();
        }

        @Override
        public String getAlgorithm() {

            return NATIVE_ALGORITHM;
        }
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

            // Shipped providers accept a parameter only when it arrives as a string and otherwise
            // keep their default silently, so this fake does the same. A stored value that recorded
            // its parameters as anything else would therefore verify against the wrong ones, which
            // is the failure these tests exist to catch.
            Object configuredRounds = initProperties.get(ROUNDS);
            if (configuredRounds instanceof String) {
                rounds = Integer.parseInt((String) configuredRounds);
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
