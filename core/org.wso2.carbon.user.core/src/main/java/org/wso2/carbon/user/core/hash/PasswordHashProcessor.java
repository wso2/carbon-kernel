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

/**
 * Class responsible for hashing passwords based on the configured algorithm.
 * This class supports both legacy MessageDigest-based hashing and pluggable HashProvider-based hashing.
 * <p>
 * It reads hashing configuration from a user store properties map and uses secure mechanisms
 * (such as {@link Secret}) to handle passwords in memory.
 * </p>
 */
public class PasswordHashProcessor {

    private HashProvider hashProvider;
    private final Map<String, String> hashConfigProperties;

    /**
     * Constructs a PasswordHashProcessor instance using the provided hashing configuration properties.
     *
     * @param hashConfigProperties A map containing hashing-related properties such as
     *                             digest function, algorithm-specific parameters, etc.
     * @throws PasswordHashingException If initialization of the hash provider fails.
     */
    public PasswordHashProcessor(Map<String, String> hashConfigProperties) throws PasswordHashingException {

        this.hashConfigProperties = hashConfigProperties;
        initializeHashProvider(hashConfigProperties);
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
                if (saltValue != null) {
                    credentialObj.addChars(saltValue.toCharArray());
                }
                MessageDigest messageDigest = MessageDigest.getInstance(digestFunction);
                byte[] byteValue = messageDigest.digest(credentialObj.getBytes());
                return Base64.encode(byteValue);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new PasswordHashingClientException("Error occurred while preparing password.", e);
        }
    }

    /**
     * Performs password hashing using a pluggable {@link HashProvider} implementation.
     *
     * @param credentialObj Secret-wrapped password object.
     * @param saltValue     Optional salt value to apply.
     * @return Base64-encoded hashed password.
     * @throws PasswordHashingServerException If the hashing process fails at runtime.
     */
    private String hashWithProvider(Secret credentialObj, String saltValue)
            throws PasswordHashingServerException {

        try {
            byte[] hashByteArray = hashProvider.calculateHash(credentialObj.getChars(), saltValue);
            return Base64.encode(hashByteArray);
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
     * If a pluggable {@link HashProvider} is configured, it will be used for validation.
     * Otherwise, the method falls back to standard {@link MessageDigest} validation.
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

        if (hashedPassword == null) {
            return false;
        }
        Secret credentialObj = getSecret(password);
        try {
            String digestFunction = hashConfigProperties.get(PASSWORD_HASH_DIGEST_FUNCTION);
            if (digestFunction == null || digestFunction.equals(PASSWORD_HASH_METHOD_PLAIN_TEXT)) {
                return hashedPassword.equals(new String(credentialObj.getChars()));
            }
            if (hashProvider != null && hashProvider.supportsValidateHash()) {
                return validatePasswordWithProvider(credentialObj, hashedPassword, saltValue);
            } else {
                return hashedPassword.equals(hashPassword(password, saltValue));
            }
        } finally {
            credentialObj.clear();
        }
    }

    /**
     * Performs password validation using a pluggable {@link HashProvider} implementation.
     *
     * @param credentialObj  Secret-wrapped password object.
     * @param hashedPassword The stored password hash as a String.
     * @param saltValue      Optional salt value to apply.
     * @return True if the password is valid, false otherwise.
     * @throws PasswordHashingServerException If the validation process fails at runtime.
     */
    private boolean validatePasswordWithProvider(Secret credentialObj, String hashedPassword, String saltValue)
            throws PasswordHashingServerException {

        try {
            return hashProvider.validateHash(credentialObj.getChars(), Base64.decode(hashedPassword), saltValue);
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
     * Initializes the {@link HashProvider} using the provided configuration map.
     * <p>
     * If algorithm-specific metadata is required, it is parsed and applied before
     * obtaining the provider instance. If no such configuration is needed, a default
     * provider is used.
     * </p>
     *
     * @param hashConfigProperties A map of password hashing configuration properties.
     * @throws PasswordHashingException If the provider cannot be initialized.
     */
    private void initializeHashProvider(Map<String, String> hashConfigProperties) throws PasswordHashingException {

        String digestFunction = hashConfigProperties.get(PASSWORD_HASH_DIGEST_FUNCTION);
        HashProviderFactory hashProviderFactory = getHashProviderFactory(digestFunction);

        if (hashProviderFactory == null) {
            return;
        }

        Set<String> metaProperties = hashProviderFactory.getHashProviderConfigProperties();
        if (!metaProperties.isEmpty()) {
            Map<String, Object> hashProviderPropertiesMap = getHashProviderInitConfigs(hashConfigProperties);

            if (!hashProviderPropertiesMap.isEmpty()) {
                try {
                    hashProvider = hashProviderFactory.getHashProvider(hashProviderPropertiesMap);
                    return;
                } catch (HashProviderException e) {
                    throw new PasswordHashingServerException("Error occurred while initializing the hashProvider.", e);
                }
            }
        }

        // If meta props are empty or no config provided, fall back
        hashProvider = hashProviderFactory.getHashProvider();
    }

    /**
     * Extracts the algorithm-specific configuration properties from the provided configuration map.
     * These are required by some {@link HashProvider} implementations such as PBKDF2.
     *
     * @param hashConfigProperties The full hashing config map.
     * @return A simplified map containing only the algorithm-specific initialization parameters.
     */
    private Map<String, Object> getHashProviderInitConfigs(Map<String, String> hashConfigProperties) {

        String hashingAlgorithmProperties = hashConfigProperties.get(PASSWORD_HASH_ALGORITHM_PROPERTIES);
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
