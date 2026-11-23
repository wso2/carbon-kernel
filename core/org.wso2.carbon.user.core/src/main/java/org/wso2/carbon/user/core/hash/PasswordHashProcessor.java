/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.user.core.hash;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.user.core.exceptions.HashProviderException;
import org.wso2.carbon.user.core.exceptions.PasswordHashingClientException;
import org.wso2.carbon.user.core.exceptions.PasswordHashingException;
import org.wso2.carbon.user.core.exceptions.PasswordHashingServerException;
import org.wso2.carbon.user.core.internal.UserStoreMgtDataHolder;
import org.wso2.carbon.utils.Secret;
import org.wso2.carbon.utils.UnsupportedSecretTypeException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.PASSWORD_HASH_ALGORITHM_PROPERTIES;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.PASSWORD_HASH_DIGEST_FUNCTION;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.PASSWORD_HASH_METHOD_PLAIN_TEXT;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.LEGACY_PASSWORD_HASH_DIGEST_FUNCTION;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.LEGACY_PASSWORD_HASH_ALGORITHM_PROPERTIES;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.ENABLE_SELF_DESCRIBING_PASSWORD_HASH;

/**
 * Class responsible for hashing passwords based on the configured algorithm.
 * This class supports both legacy MessageDigest-based hashing and pluggable HashProvider-based hashing.
 * <p>
 * It reads hashing configuration from a user store properties map and uses secure mechanisms
 * (such as {@link Secret}) to handle passwords in memory.
 * </p>
 */
public class PasswordHashProcessor {

    private final HashProvider hashProvider;
    private final HashProvider legacyHashProvider;
    private final Map<String, String> hashConfigProperties;
    private final boolean selfDescribingHashEnabled;

    /**
     * Constructs a PasswordHashProcessor instance using the provided hashing configuration properties.
     *
     * @param hashConfigProperties A map containing hashing-related properties such as
     *                             digest function, algorithm-specific parameters, etc.
     * @throws PasswordHashingException If initialization of the hash provider fails.
     */
    public PasswordHashProcessor(Map<String, String> hashConfigProperties) throws PasswordHashingException {

        this.hashConfigProperties = hashConfigProperties;

        // Read before the algorithms are resolved, because the legacy algorithm only applies while
        // this is on. Absent means the property was never configured, which carries the declared
        // default of false: a store that configures nothing keeps writing the bare payload it wrote
        // before this format existed. Boolean.parseBoolean reads a null and an explicit "false"
        // alike, so the two need no separate handling.
        this.selfDescribingHashEnabled =
                Boolean.parseBoolean(hashConfigProperties.get(ENABLE_SELF_DESCRIBING_PASSWORD_HASH));

        String digestFunction = hashConfigProperties.get(PASSWORD_HASH_DIGEST_FUNCTION);
        String legacyDigestFunction = resolveLegacyDigestFunction(digestFunction);

        this.hashProvider = initializeHashProvider(digestFunction, PASSWORD_HASH_ALGORITHM_PROPERTIES);
        this.legacyHashProvider = initializeLegacyHashProvider(digestFunction, legacyDigestFunction);
    }

    /**
     * Builds the {@link HashProvider} that verifies stored hashes carrying no algorithm identifier.
     * Such a value records no parameters, so they are taken from the declared legacy parameters.
     *
     * @param digestFunction       The currently configured digest function.
     * @param legacyDigestFunction The digest function resolved for legacy hashes.
     * @return The legacy HashProvider, or null if no factory is registered for the legacy algorithm.
     * @throws PasswordHashingException If the provider cannot be initialized.
     */
    private HashProvider initializeLegacyHashProvider(String digestFunction, String legacyDigestFunction)
            throws PasswordHashingException {

        // Checked by what it parses to rather than its text, because the console default of {}
        // declares nothing. Ignored while the format is off, like the legacy algorithm.
        if (selfDescribingHashEnabled
                && !getHashProviderInitConfigs(LEGACY_PASSWORD_HASH_ALGORITHM_PROPERTIES).isEmpty()) {
            return initializeHashProvider(legacyDigestFunction, LEGACY_PASSWORD_HASH_ALGORITHM_PROPERTIES);
        }
        // When the legacy algorithm is the configured one, the configured provider already carries the
        // right parameters. Otherwise the configured parameters belong to a different algorithm, so the
        // legacy provider is created with its own defaults.
        return StringUtils.equals(digestFunction, legacyDigestFunction)
                ? this.hashProvider
                : initializeHashProvider(legacyDigestFunction, null);
    }

    /**
     * Resolves the hashing algorithm used to verify stored hashes that carry no algorithm identifier.
     * Falls back to the configured algorithm when no legacy algorithm is configured, which preserves
     * the behaviour of deployments that have not set the property.
     *
     * @param digestFunction The currently configured digest function.
     * @return The digest function to use for legacy hashes.
     */
    private String resolveLegacyDigestFunction(String digestFunction) {

        // A declared legacy algorithm only names something while the self-describing format is on,
        // because only then does a stored value without an identifier mean one written before the
        // change. With the format off nothing records an identifier, so every stored value is the
        // configured algorithm's own output and honouring a different legacy algorithm would leave
        // the store unable to verify what it had just written.
        if (!selfDescribingHashEnabled) {
            return digestFunction;
        }
        String legacyDigestFunction = hashConfigProperties.get(LEGACY_PASSWORD_HASH_DIGEST_FUNCTION);
        return StringUtils.isNotBlank(legacyDigestFunction) ? legacyDigestFunction : digestFunction;
    }

    /**
     * Returns the hashing algorithm used to verify stored hashes that carry no algorithm identifier.
     *
     * @return The digest function to use for legacy hashes.
     */
    public String getLegacyDigestFunction() {

        return resolveLegacyDigestFunction(hashConfigProperties.get(PASSWORD_HASH_DIGEST_FUNCTION));
    }

    /**
     * Indicates whether new credentials are stored in the self-describing format.
     *
     * @return True if the self-describing format is enabled.
     */
    public boolean isSelfDescribingHashEnabled() {

        return selfDescribingHashEnabled;
    }

    /**
     * Hashes the given password using the configured hashing algorithm and optional salt.
     * <p>
     * If a pluggable {@link HashProvider} is configured, it will be used. Otherwise,
     * the method falls back to standard {@link MessageDigest} hashing or plaintext as configured.
     * </p>
     *
     * @param password  The password object to be hashed (can be a char[], String, etc.).
     * @param saltValue Optional salt value to use during hashing.
     * @return Base64-encoded hashed password string.
     * @throws PasswordHashingException If hashing fails due to an unsupported type, algorithm error, or config error.
     */
    public String hashPassword(Object password, String saltValue) throws PasswordHashingException {

        Secret credentialObj = getSecret(password);
        try {
            String digestFunction = hashConfigProperties.get(PASSWORD_HASH_DIGEST_FUNCTION);
            if (digestFunction == null) {
                return new String(credentialObj.getChars());
            }
            if (hashProvider == null) {
                return hashWithMessageDigest(credentialObj, saltValue, digestFunction);
            } else {
                return hashWithProvider(credentialObj, saltValue);
            }
        } finally {
            credentialObj.clear();
        }
    }

    /**
     * Performs password hashing using the configured {@link MessageDigest} algorithm.
     *
     * @param credentialObj  Secret-wrapped password object.
     * @param saltValue      Optional salt value to append.
     * @param digestFunction The digest algorithm to use (e.g., SHA-256, SHA-512).
     * @return Base64-encoded hashed password.
     * @throws PasswordHashingClientException If the specified algorithm is invalid or unsupported.
     */
    private String hashWithMessageDigest(Secret credentialObj, String saltValue, String digestFunction)
            throws PasswordHashingClientException {

        try {
            if (digestFunction.equals(PASSWORD_HASH_METHOD_PLAIN_TEXT)) {
                return new String(credentialObj.getChars());
            } else {
                // Salted on a copy, because the caller's credential is used again for the hash upgrade.
                Secret saltedCredential = getSecret(credentialObj.getChars());
                try {
                    if (saltValue != null) {
                        saltedCredential.addChars(saltValue.toCharArray());
                    }
                    MessageDigest messageDigest = MessageDigest.getInstance(digestFunction);
                    byte[] byteValue = messageDigest.digest(saltedCredential.getBytes());
                    return Base64.encode(byteValue);
                } finally {
                    saltedCredential.clear();
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new PasswordHashingClientException("Error occurred while preparing password.", e);
        }
    }

    /**
     * Performs password hashing using a pluggable {@link HashProvider} implementation.
     *
     * <p>
     * When the self-describing format is enabled, the payload is stored together with the
     * provider's algorithm identifier and the parameters that produced it, so the stored value
     * names what has to verify it. When it is disabled, the bare payload is returned, which is
     * what the store wrote before this format existed.
     * </p>
     *
     * @param credentialObj Secret-wrapped password object.
     * @param saltValue     Optional salt value to apply.
     * @return The self-describing value, or the bare payload when the format is disabled.
     * @throws PasswordHashingServerException If the hashing process fails at runtime.
     */
    private String hashWithProvider(Secret credentialObj, String saltValue)
            throws PasswordHashingServerException {

        try {
            byte[] hashByteArray = hashProvider.calculateHash(credentialObj.getChars(), saltValue);
            String payload = Base64.encode(hashByteArray);
            if (!selfDescribingHashEnabled) {
                return payload;
            }
            // The parameters are read from the provider rather than from configuration, so that a
            // credential records what actually produced it. A store that configures nothing still
            // gets the provider's own defaults recorded, which keeps the credential verifiable if
            // a later release changes them.
            return SelfDescribingHashParser.format(hashProvider.getAlgorithm(), payload,
                    hashProvider.getParameters());
        } catch (HashProviderException e) {
            throw new PasswordHashingServerException("Error occurred while preparing password.", e);
        }
    }

    /**
     * Checks if a pluggable {@link HashProvider} is configured and supports
     * its own built-in validation logic.
     * <p>
     * This method allows callers to determine whether they should delegate the
     * password validation to this processor or handle it manually.
     * </p>
     *
     * @return true if a custom provider with a supported validation method is configured;
     * false otherwise.
     */
    public boolean hasCustomValidator() {

        return hashProvider != null && hashProvider.supportsValidateHash();
    }

    /**
     * Validates a given password against a stored hashed password using a salt.
     * <p>
     * A stored hash that carries an algorithm identifier is verified by the provider that identifier
     * names. A stored hash without one predates self-describing hashes and is verified with the
     * configured legacy algorithm.
     * </p>
     *
     * @param password       The password object to be validated (e.g., char[], String).
     * @param hashedPassword The stored password hash as a String.
     * @param saltValue      Optional salt value used during hashing.
     * @return True if the password is valid, false otherwise.
     * @throws PasswordHashingException If validation fails due to an unsupported algorithm,
     * a configuration error, or a runtime issue with the provider.
     */
    public boolean validatePassword(Object password, String hashedPassword, String saltValue)
            throws PasswordHashingException {

        SelfDescribingHashParser.ParseResult parseResult = SelfDescribingHashParser.parse(hashedPassword);

        Secret credentialObj = getSecret(password);
        try {
            // A plaintext store holds the credential itself rather than a hash, so a password that
            // happens to look like a stored value is still only a password and is compared
            // literally. This is checked before the value is judged well formed, because a password
            // such as {hello opens an object without being one and would otherwise be rejected.
            if (isPlainTextConfigured() && hashedPassword != null
                    && hashedPassword.equals(new String(credentialObj.getChars()))) {
                return true;
            }
            if (!parseResult.isValid()) {
                return false;
            }
            if (parseResult.isLegacy()) {
                return validateLegacyPassword(credentialObj, parseResult.getPayload(), saltValue);
            }
            return validateSelfDescribingPassword(credentialObj, parseResult, saltValue);
        } finally {
            credentialObj.clear();
        }
    }

    /**
     * Indicates whether stored values in this user store may be credentials held as plaintext, which
     * is the case when the algorithm used for values without an identifier stores them unhashed.
     *
     * @return True if stored legacy values are plaintext credentials.
     */
    private boolean isPlainTextConfigured() {

        String legacyDigestFunction =
                resolveLegacyDigestFunction(hashConfigProperties.get(PASSWORD_HASH_DIGEST_FUNCTION));
        return legacyDigestFunction == null || legacyDigestFunction.equals(PASSWORD_HASH_METHOD_PLAIN_TEXT);
    }

    /**
     * Validates a password against a stored hash that carries no algorithm identifier.
     * <p>
     * Such hashes were written before self-describing hashes were introduced, so they are verified
     * with the configured legacy algorithm rather than the algorithm configured for new credentials.
     * </p>
     *
     * @param credentialObj Secret-wrapped password object.
     * @param storedHash    The stored hash, which is the complete stored value for a legacy entry.
     * @param saltValue     Optional salt value used during hashing.
     * @return True if the password is valid, false otherwise.
     * @throws PasswordHashingException If validation fails due to an unsupported algorithm, or a
     * runtime issue with the provider.
     */
    private boolean validateLegacyPassword(Secret credentialObj, String storedHash, String saltValue)
            throws PasswordHashingException {

        String legacyDigestFunction =
                resolveLegacyDigestFunction(hashConfigProperties.get(PASSWORD_HASH_DIGEST_FUNCTION));

        if (legacyDigestFunction == null || legacyDigestFunction.equals(PASSWORD_HASH_METHOD_PLAIN_TEXT)) {
            return storedHash.equals(new String(credentialObj.getChars()));
        }
        if (legacyHashProvider == null) {
            return storedHash.equals(hashWithMessageDigest(credentialObj, saltValue, legacyDigestFunction));
        }
        return validateWithProvider(legacyHashProvider, credentialObj, storedHash, saltValue);
    }

    /**
     * Validates a password against a stored hash that declares its own algorithm identifier.
     * <p>
     * An identifier with no registered provider fails verification and is never retried with the
     * legacy algorithm, so an unknown identifier can never be mistaken for a legacy hash.
     * </p>
     *
     * @param credentialObj Secret-wrapped password object.
     * @param parseResult   The parsed stored hash.
     * @param saltValue     Optional salt value used during hashing.
     * @return True if the password is valid, false otherwise.
     * @throws PasswordHashingServerException If the validation process fails at runtime.
     */
    private boolean validateSelfDescribingPassword(Secret credentialObj,
                                                   SelfDescribingHashParser.ParseResult parseResult,
                                                   String saltValue) throws PasswordHashingServerException {

        HashProvider provider = resolveProviderForAlgorithm(parseResult.getAlgorithm(), parseResult.getProperties());
        if (provider == null) {
            return false;
        }
        return validateWithProvider(provider, credentialObj, parseResult.getPayload(), saltValue);
    }

    /**
     * Resolves the {@link HashProvider} that verifies a stored hash declaring the given algorithm.
     * <p>
     * The configured provider is reused when it matches, so its configured parameters apply and no
     * provider is constructed per authentication. Otherwise the provider for the stored algorithm is
     * built with its own defaults, since the configured parameters belong to a different algorithm.
     * </p>
     *
     * @param algorithm The algorithm identifier read from the stored hash.
     * @return The matching HashProvider, or null if no provider is registered for the algorithm.
     */
    private HashProvider resolveProviderForAlgorithm(String algorithm, Map<String, Object> properties)
            throws PasswordHashingServerException {

        // A credential that recorded its parameters is verified against those rather than against
        // whatever is configured now, so raising an iteration count does not strand the credentials
        // written before the change. The configured provider is only reused when the stored value
        // carries no parameters of its own.
        if (properties == null || properties.isEmpty()) {
            if (hashProvider != null && algorithm.equals(hashProvider.getAlgorithm())) {
                return hashProvider;
            }
            HashProviderFactory hashProviderFactory = getHashProviderFactory(algorithm);
            return hashProviderFactory == null ? null : hashProviderFactory.getHashProvider();
        }

        HashProviderFactory hashProviderFactory = getHashProviderFactory(algorithm);
        if (hashProviderFactory == null) {
            return null;
        }
        try {
            // A provider is built per verification rather than cached by parameter set. Building
            // one measures at roughly 400ns against the several milliseconds of the hash it then
            // computes, so a cache would trade an unmeasurable saving for a keyed structure that
            // has to be bounded against the parameter sets a user store happens to hold.
            return hashProviderFactory.getHashProvider(properties);
        } catch (HashProviderException e) {
            throw new PasswordHashingServerException(
                    "Error occurred while initializing the hashProvider for algorithm: " + algorithm, e);
        }
    }

    /**
     * Performs password validation using a pluggable {@link HashProvider} implementation.
     *
     * @param provider      The HashProvider to validate with.
     * @param credentialObj Secret-wrapped password object.
     * @param storedHash    The stored hash payload, without any algorithm identifier prefix.
     * @param saltValue     Optional salt value to apply.
     * @return True if the password is valid, false otherwise.
     * @throws PasswordHashingServerException If the validation process fails at runtime.
     */
    private boolean validateWithProvider(HashProvider provider, Secret credentialObj, String storedHash,
                                         String saltValue) throws PasswordHashingServerException {

        try {
            return provider.validateHash(credentialObj.getChars(), Base64.decode(storedHash), saltValue);
        } catch (HashProviderException e) {
            throw new PasswordHashingServerException("Error occurred while validating password with HashProvider.", e);
        }
    }

    /**
     * Converts the provided password object to a {@link Secret} for secure handling.
     *
     * @param password The password object (char[], String, etc.).
     * @return A Secret-wrapped representation of the password.
     * @throws PasswordHashingClientException If the object type is unsupported.
     */
    private Secret getSecret(Object password) throws PasswordHashingClientException {

        try {
            return Secret.getSecret(password);
        } catch (UnsupportedSecretTypeException e) {
            throw new PasswordHashingClientException("Unsupported credential type.", e);
        }
    }

    /**
     * Builds a {@link HashProvider} for the given digest function.
     * <p>
     * If algorithm-specific metadata is required, it is parsed and applied before
     * obtaining the provider instance. If no such configuration is needed, a default
     * provider is used.
     * </p>
     *
     * @param digestFunction     The digest function to build a provider for.
     * @param parametersProperty The user store property holding its parameters, or null for defaults.
     * @return The HashProvider, or null if no factory is registered for the digest function.
     * @throws PasswordHashingException If the provider cannot be initialized.
     */
    private HashProvider initializeHashProvider(String digestFunction, String parametersProperty)
            throws PasswordHashingException {

        HashProviderFactory hashProviderFactory = getHashProviderFactory(digestFunction);

        if (hashProviderFactory == null) {
            return null;
        }

        if (parametersProperty != null) {
            Set<String> metaProperties = hashProviderFactory.getHashProviderConfigProperties();
            if (!metaProperties.isEmpty()) {
                Map<String, Object> hashProviderPropertiesMap = getHashProviderInitConfigs(parametersProperty);

                if (!hashProviderPropertiesMap.isEmpty()) {
                    try {
                        return hashProviderFactory.getHashProvider(hashProviderPropertiesMap);
                    } catch (HashProviderException e) {
                        throw new PasswordHashingServerException(
                                "Error occurred while initializing the hashProvider.", e);
                    }
                }
            }
        }

        // If meta props are empty or no config provided, fall back
        return hashProviderFactory.getHashProvider();
    }

    /**
     * Extracts the algorithm-specific configuration properties held by the given user store property.
     * These are required by some {@link HashProvider} implementations such as PBKDF2.
     *
     * @param parametersProperty The user store property to read the parameters from.
     * @return A simplified map containing only the algorithm-specific initialization parameters.
     */
    private Map<String, Object> getHashProviderInitConfigs(String parametersProperty) {

        String hashingAlgorithmProperties = hashConfigProperties.get(parametersProperty);
        Map<String, Object> hashProviderInitConfigsMap = new HashMap<>();
        if (StringUtils.isNotBlank(hashingAlgorithmProperties)) {
            Gson gson = new Gson();
            JsonObject hashPropertyJSON = gson.fromJson(hashingAlgorithmProperties, JsonObject.class);
            Set<String> hashPropertyJSONKey = hashPropertyJSON.keySet();
            for (String hashProperty : hashPropertyJSONKey) {
                hashProviderInitConfigsMap.put(hashProperty, hashPropertyJSON.get(hashProperty).getAsString());
            }
        }
        return hashProviderInitConfigsMap;
    }

    /**
     * Retrieves the {@link HashProviderFactory} associated with the specified digest function.
     *
     * @param digestFunction The name of the digest function (e.g., "PBKDF2").
     * @return The corresponding {@link HashProviderFactory} instance, or {@code null} if not found.
     */
    private HashProviderFactory getHashProviderFactory(String digestFunction) {

        return UserStoreMgtDataHolder.getInstance().getHashProviderFactory(digestFunction);
    }
}
