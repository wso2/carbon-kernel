/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.crypto.api.CipherMetaDataHolder;
import org.wso2.carbon.crypto.api.CryptoService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ServerConstants;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * The utility class to encrypt/decrypt passwords to be stored in the
 * database.
 */
public class CryptoUtil {

    private static final String CIPHER_TRANSFORMATION_SYSTEM_PROPERTY = "org.wso2.CipherTransformation";
    private static Log log = LogFactory.getLog(CryptoUtil.class);
    private ServerConfigurationService serverConfigService;

    // ---- Any-size (large-secret) encryption support ------------------------------------------------------
    // Self-describing marker for the chunked format: rsachunk:v1:<b64(block0)>;<b64(block1)>;...
    private static final String CHUNK_MARKER = "rsachunk:v1:";
    private static final String CHUNK_DELIMITER = ";";
    // ServerConfiguration key naming the internal crypto provider (carbon.xml CryptoService element).
    private static final String INTERNAL_CRYPTO_PROVIDER_CONFIG = "CryptoService.InternalCryptoProviderClassName";
    // Class-name suffix of the symmetric (AES-GCM) internal provider, which has no block-size limit.
    private static final String SYMMETRIC_PROVIDER_SUFFIX = "SymmetricKeyInternalCryptoProvider";
    // Plaintext bytes per RSA block: the smallest realistic RSA-2048 single-shot limit (OAEP-SHA256 = 190;
    // OAEP-SHA1 = 214; PKCS1 = 245), so a block always fits regardless of the configured padding. Assumes a
    // >= 2048-bit internal keystore (the WSO2 default).
    private static final int MAX_RSA_PLAINTEXT_CHUNK_SIZE = 190;
    private RegistryService registryService;
    private String cryptoProviderIdentifier;
    private Gson gson = new Gson();
    private static CryptoUtil instance = null;
    private static final char[] HEX_CHARACTERS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
                                                            'C', 'D', 'E', 'F'};

    private static final String DEFAULT_CRYPTO_ALGORITHM = "RSA";

    /**
     * This method returns CryptoUtil object, where this should only be used at runtime,
     * after the server is properly initialized, or else, use the overloaded method,
     * CryptoUtil#getDefaultCryptoUtil(ServerConfigurationService).
     *
     * @return
     */
    public static CryptoUtil getDefaultCryptoUtil() {
        return getDefaultCryptoUtil(CarbonCoreDataHolder.getInstance().
                getServerConfigurationService(), lookupRegistryService());
    }

    public static RegistryService lookupRegistryService() {
        try {
            return CarbonCoreDataHolder.getInstance().getRegistryService();
        } catch (Exception e) {
            log.error("Error in getting RegistryService from CarbonCoreDataHolder: " +
                    e.getMessage(), e);
            return null;
        }
    }

    /**
     * This method is used to get the CryptoUtil object given the ServerConfigurationService
     * service. This approach must be used if the CryptoUtil class is used in the server startup,
     * where the ServerConfigurationService may not be available at CarbonCoreDataHolder.
     * The same is also for RegistryService.
     *
     * @param serverConfigService The ServerConfigurationService object
     * @param registryService     The RegistryService object
     * @return The created or cached CryptoUtil instance
     */
    public synchronized static CryptoUtil getDefaultCryptoUtil(
            ServerConfigurationService serverConfigService,
            RegistryService registryService) {
        if (instance == null) {
            instance = new CryptoUtil(serverConfigService, registryService);
        }
        return instance;
    }

    private CryptoUtil(ServerConfigurationService serverConfigService,
                       RegistryService registryService) {
        this.serverConfigService = serverConfigService;
        this.registryService = registryService;
        this.cryptoProviderIdentifier = getPreferredJceProviderIdentifier();
    }

    public ServerConfigurationService getServerConfigService() {
        return serverConfigService;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    /**
     * Encrypt a given plain text
     *
     * @param plainTextBytes The plaintext bytes to be encrypted
     * @param cipherTransformation The transformation that need to encrypt. If it is null, RSA is used as default
     * @param returnSelfContainedCipherText Create self-contained cipher text if true, return simple encrypted
     *                                      ciphertext otherwise.
     * @return The cipher text bytes
     * @throws CryptoException On error during encryption
     */
    public byte[] encrypt(byte[] plainTextBytes, String cipherTransformation, boolean returnSelfContainedCipherText)
            throws CryptoException {

        if (plainTextBytes == null) {
            throw new CryptoException("Plaintext can't be null.");
        }

        byte[] encryptedKey;
        try {

            CryptoService cryptoService = CarbonCoreDataHolder.getInstance().getCryptoService();

            if(cryptoService == null){
                throw new CryptoException("A crypto service implementation has not been registered.");
            }

            String algorithm = null;
            if (!StringUtils.isBlank(cipherTransformation)) {
                algorithm = cipherTransformation;
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Cipher transformation is enabled. Crypto algorithm: '%s'", algorithm));
                }
            }
            encryptedKey = cryptoService
                    .encrypt(plainTextBytes, algorithm, cryptoProviderIdentifier, returnSelfContainedCipherText);
        } catch (Exception e) {
            throw new CryptoException("An error occurred while encrypting data.", e);
        }
        return encryptedKey;
    }

    /**
     * Encrypt a given plain text
     *
     * @param plainTextBytes The plaintext bytes to be encrypted
     * @return The cipher text bytes (self-contained ciphertext)
     * @throws CryptoException On error during encryption
     */
    public byte[] encrypt(byte[] plainTextBytes) throws CryptoException {
        //encrypt with transformation configured in carbon.properties as self contained ciphertext
        return encrypt(plainTextBytes, System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY), true);
    }

    /**
     * Encrypt a given plain text with the provided algorithm and internal crypto provider type.
     *
     * @param plainTextBytes                plain text bytes that need to be encrypted.
     * @param algorithm                     The encryption algorithm.
     * @param returnSelfContainedCipherText Create self-contained cipher text if true, return simple encrypted ciphertext otherwise.
     * @param internalCryptoProviderType    The {@link org.wso2.carbon.crypto.api.InternalCryptoProvider} type.
     * @return The cipher text bytes.
     * @throws CryptoException
     */
    public byte[] encrypt(byte[] plainTextBytes, String algorithm, boolean returnSelfContainedCipherText,
                          String internalCryptoProviderType)
            throws CryptoException {

        failIfEncryptDecryptInputsAreInvalid(plainTextBytes, algorithm, internalCryptoProviderType);

        byte[] encryptedKey;
        try {

            CryptoService cryptoService = CarbonCoreDataHolder.getInstance().getCryptoService();

            if (cryptoService == null) {
                throw new CryptoException("A crypto service implementation has not been registered.");
            }
            if (plainTextBytes.length == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Plaintext is empty. An empty array will be used as the ciphertext bytes.");
                }
                encryptedKey = StringUtils.EMPTY.getBytes();
            } else {
                if (log.isDebugEnabled()) {
                    if (returnSelfContainedCipherText) {
                        log.debug(String.format(
                                "Encrypting data in self contained format, with algorithm: '%s' and internal crypto " +
                                        "provider: '%s'", algorithm, internalCryptoProviderType));
                    } else {
                        log.debug(String.format(
                                "Encrypting data with algorithm: '%s' and internal crypto " +
                                        "provider: '%s'", algorithm, internalCryptoProviderType));
                    }
                }
                encryptedKey = cryptoService
                        .encrypt(plainTextBytes, algorithm, cryptoProviderIdentifier, returnSelfContainedCipherText,
                                internalCryptoProviderType);

            }

        } catch (org.wso2.carbon.crypto.api.CryptoException e) {
            throw new CryptoException("An error occurred while encrypting data.", e);
        }
        return encryptedKey;
    }

    /**
     * Encrypt the given plain text with given transformation and base64 encode the encrypted content.
     *
     * @param plainText The plaintext value to be encrypted and base64
     *                  encoded
     * @param transformation The transformation used for encryption
     * @param returnSelfContainedCipherText Create self-contained cipher text if true, return simple encrypted
     *                                      ciphertext otherwise.
     * @return The base64 encoded cipher text
     * @throws CryptoException On error during encryption
     */
    public String encryptAndBase64Encode(byte[] plainText, String transformation, boolean returnSelfContainedCipherText)
            throws CryptoException {
        return Base64.encode(encrypt(plainText, transformation, returnSelfContainedCipherText));
    }

    /**
     * Encrypt the given plain text and base64 encode the encrypted content.
     *
     * @param plainText The plaintext value to be encrypted and base64
     *                  encoded
     * @return The base64 encoded cipher text
     * @throws CryptoException On error during encryption
     */
    public String encryptAndBase64Encode(byte[] plainText) throws
            CryptoException {
        return Base64.encode(encrypt(plainText));
    }

    /**
     * Encrypt the given plain text and base64 encode the encrypted content.
     *
     * @param plainText                     Plain text bytes that need to be encrypted.
     * @param algorithm                     The encryption algorithm.
     * @param returnSelfContainedCipherText Create self-contained cipher text if true, return simple encrypted
     *                                      ciphertext otherwise.
     * @param internalCryptoProviderType    The {@link org.wso2.carbon.crypto.api.InternalCryptoProvider} type.
     * @return The cipher text bytes.
     * @throws CryptoException
     */
    public String encryptAndBase64Encode(byte[] plainText, String algorithm,
                                         boolean returnSelfContainedCipherText, String internalCryptoProviderType)
            throws CryptoException {

        return Base64.encode(encrypt(plainText, algorithm, returnSelfContainedCipherText, internalCryptoProviderType));
    }

    /**
     * Decrypt the given cipher text value using the WSO2 WSAS key
     *
     * @param cipherTextBytes The cipher text to be decrypted
     * @return Decrypted bytes
     * @throws CryptoException On an error during decryption
     */
    public byte[] decrypt(byte[] cipherTextBytes) throws CryptoException {


        if (cipherTextBytes == null) {
            throw new CryptoException("Ciphertext can't be null.");
        }

        byte[] decryptedValue;

        try {
            CryptoService cryptoService = CarbonCoreDataHolder.getInstance().getCryptoService();

            if(cryptoService == null){
                throw new CryptoException("A crypto service implementation has not been registered.");
            }

            String algorithm = null;

            String cipherTransformation = System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY);

            if (cipherTransformation != null) {
                CipherMetaDataHolder
                        cipherMetaDataHolder = cipherTextToCipherMetaDataHolder(cipherTextBytes);
                if (cipherMetaDataHolder != null) {
                    //cipher with meta data
                    if (log.isDebugEnabled()) {
                        log.debug("Cipher transformation for decryption : " + cipherMetaDataHolder.getTransformation());
                    }
                    algorithm = cipherMetaDataHolder.getTransformation();
                    cipherTextBytes = cipherMetaDataHolder.getCipherBase64Decoded();
                } else {
                    algorithm = cipherTransformation;
                }
            }

            if (cipherTextBytes.length == 0) {
                decryptedValue = StringUtils.EMPTY.getBytes();
                if (log.isDebugEnabled()) {
                    log.debug("Ciphertext is empty. An empty array will be used as the plaintext bytes.");
                }
            } else {
                decryptedValue = cryptoService.decrypt(cipherTextBytes, algorithm, cryptoProviderIdentifier);
            }

            return decryptedValue;

        } catch (Exception e) {
            throw new CryptoException("An error occurred while decrypting data.", e);
        }
    }


    /**
     * Decrypt the given cipher text value using the WSO2 WSAS key.
     *
     * IMPORTANT: Since this decrypt method is provided to force required transformation, this will not decrypt
     * self-contained ciphertexts. To decrypt self-contained ciphertext use decrypt(byte[] cipherTextBytes)
     *
     * @param cipherTextBytes The cipher text to be decrypted
     * @param cipherTransformation The transformation that need to decrypt. If it is null, RSA is used as default.
     *                             NOTE: If symmetric encryption enabled, cipherTransformation parameter will be ignored
     * @return Decrypted bytes
     * @throws CryptoException On an error during decryption
     */
    public byte[] decrypt(byte[] cipherTextBytes, String cipherTransformation) throws CryptoException {

        if (cipherTextBytes == null) {
            throw new CryptoException("Ciphertext can't be null.");
        }

        byte[] decryptedValue;

        try {
            CryptoService cryptoService = CarbonCoreDataHolder.getInstance().getCryptoService();

            if(cryptoService == null){
                throw new CryptoException("A crypto service implementation has not been registered.");
            }

            String algorithm = null;

            if (cipherTransformation != null) {
                algorithm = cipherTransformation;
            }

            if (cipherTextBytes.length == 0) {
                decryptedValue = StringUtils.EMPTY.getBytes();
                if (log.isDebugEnabled()) {
                    log.debug("Ciphertext is empty. An empty array will be used as the plaintext bytes.");
                }
            }else {
                decryptedValue = cryptoService.decrypt(cipherTextBytes, algorithm, cryptoProviderIdentifier);
            }

            return decryptedValue;

        } catch (Exception e) {
            throw new CryptoException("An error occurred while decrypting data.", e);
        }
    }

    /**
     * Decrypt the cipher text using the given algorithm and internal crypto provider type.
     *
     * @param cipherTextBytes            The cipher text to be decrypted.
     * @param algorithm                  The algorithm used for decryption.
     * @param internalCryptoProviderType The {@link org.wso2.carbon.crypto.api.InternalCryptoProvider} type.
     * @return Decrypted bytes.
     * @throws CryptoException
     */
    public byte[] decrypt(byte[] cipherTextBytes, String algorithm, String internalCryptoProviderType)
            throws CryptoException {

        failIfEncryptDecryptInputsAreInvalid(cipherTextBytes, algorithm, internalCryptoProviderType);

        byte[] decryptedValue;

        try {
            CryptoService cryptoService = CarbonCoreDataHolder.getInstance().getCryptoService();

            if (cryptoService == null) {
                throw new CryptoException("A crypto service implementation has not been registered.");
            }

            CipherMetaDataHolder
                    cipherMetaDataHolder = cipherTextToCipherMetaDataHolder(cipherTextBytes);
            if (cipherMetaDataHolder != null) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            String.format("Cipher text is in self contained format. Retrieve the actual cipher from " +
                                    "the self contained cipher text."));
                }
                cipherTextBytes = cipherMetaDataHolder.getCipherBase64Decoded();
                algorithm = cipherMetaDataHolder.getTransformation();
            }

            if (cipherTextBytes.length == 0) {
                decryptedValue = StringUtils.EMPTY.getBytes();
                if (log.isDebugEnabled()) {
                    log.debug("Ciphertext is empty. An empty array will be used as the plaintext bytes.");
                }
            } else {
                decryptedValue = cryptoService.decrypt(cipherTextBytes, algorithm, cryptoProviderIdentifier,
                        internalCryptoProviderType);
            }
            return decryptedValue;
        } catch (org.wso2.carbon.crypto.api.CryptoException e) {
            throw new CryptoException("An error occurred while decrypting data.", e);
        }

    }

    /**
     * Base64 decode the given value and decrypt using the WSO2 WSAS key
     *
     * @param base64CipherText Base64 encoded cipher text
     * @return Base64 decoded, decrypted bytes
     * @throws CryptoException On an error during decryption
     */
    public byte[] base64DecodeAndDecrypt(String base64CipherText) throws
            CryptoException {
        return decrypt(Base64.decode(base64CipherText));
    }

    /**
     * Base64 decode the given value and decrypt using the WSO2 WSAS key.
     *
     * IMPORTANT: Since this decrypt method is provided to force required transformation, this will not decrypt
     * self-contained ciphertexts. To decrypt self-contained ciphertext use base64DecodeAndDecrypt(byte[] cipherTextBytes)
     *
     * @param base64CipherText Base64 encoded cipher text
     * @param transformation The transformation used for encryption
     * @return Base64 decoded, decrypted bytes
     * @throws CryptoException On an error during decryption
     */
    public byte[] base64DecodeAndDecrypt(String base64CipherText, String transformation) throws
            CryptoException {
        return decrypt(Base64.decode(base64CipherText), transformation);
    }

    /**
     * Base64 decode the given value and decrypt using given algorithm and internal crypto provider type.
     *
     * @param base64CipherText           Base64 encoded cipher text
     * @param transformation             The algorithm that need to be used for decryption.
     * @param internalCryptoProviderType The {@link org.wso2.carbon.crypto.api.InternalCryptoProvider} type.
     * @return Base64 decoded, decrypted bytes
     * @throws CryptoException
     */
    public byte[] base64DecodeAndDecrypt(String base64CipherText, String transformation,
                                         String internalCryptoProviderType) throws
            CryptoException {

        return decrypt(Base64.decode(base64CipherText), transformation, internalCryptoProviderType);
    }

    /**
     * Function to validate whether provided is self-contained ciphertext
     *
     * @param cipherBytes interested cipher text byte array
     * @return true if provided cipher is encripted using custom transformation, false if it is RSA
     */
    public boolean isSelfContainedCipherText(byte[] cipherBytes) {
        return cipherTextToCipherHolder(cipherBytes) != null;
    }

    /**
     * Function to Base64 decode the given value and validate whether provided is self-contained ciphertext
     *
     * @param base64CipherText interested cipher text byte array
     * @return true if provided cipher is self-contained cipher text
     */
    public boolean base64DecodeAndIsSelfContainedCipherText(String base64CipherText) throws
            CryptoException {
        return isSelfContainedCipherText(Base64.decode(base64CipherText));
    }

    /**
     * Encrypts and base64-encodes a secret of any size using the configured internal crypto provider.
     * <p>
     * A symmetric (AES) provider has no block-size limit, so the value is encrypted in a single shot and stored
     * exactly like {@link #encryptAndBase64Encode(byte[])}. An asymmetric (RSA) provider is block-limited, so the
     * plaintext is split into {@value #MAX_RSA_PLAINTEXT_CHUNK_SIZE}-byte blocks, each encrypted independently and
     * joined with {@code ';'} behind a self-describing {@code rsachunk:v1:} marker. Reverse with
     * {@link #base64DecodeAndDecryptLargeData(String)}.
     *
     * @param plainText the plaintext bytes to encrypt (may be null/empty)
     * @return a single-shot ciphertext (symmetric provider) or a {@code rsachunk:v1:} value (RSA provider)
     * @throws CryptoException on error during encryption
     */
    public String encryptAndBase64EncodeLargeData(byte[] plainText) throws CryptoException {

        if (plainText == null || plainText.length == 0) {
            return encryptAndBase64Encode(plainText);
        }
        if (isSymmetricInternalProvider()) {
            // No block-size limit: single shot, stored like any other secret.
            return encryptAndBase64Encode(plainText);
        }
        // RSA (block-limited): encrypt in blocks and mark the value as chunked.
        List<String> encodedChunks = new ArrayList<>();
        for (int offset = 0; offset < plainText.length; offset += MAX_RSA_PLAINTEXT_CHUNK_SIZE) {
            int length = Math.min(MAX_RSA_PLAINTEXT_CHUNK_SIZE, plainText.length - offset);
            byte[] chunk = new byte[length];
            System.arraycopy(plainText, offset, chunk, 0, length);
            encodedChunks.add(encryptAndBase64Encode(chunk));
        }
        return CHUNK_MARKER + String.join(CHUNK_DELIMITER, encodedChunks);
    }

    /**
     * Base64-decodes and decrypts a value produced by {@link #encryptAndBase64EncodeLargeData(byte[])}. Routes on
     * the {@code rsachunk:v1:} marker, so the provider need not be detected on read: a marked value is decoded
     * block-by-block, anything else is decrypted single-shot.
     *
     * @param cipherText the stored ciphertext (single-shot or {@code rsachunk:v1:} chunked)
     * @return the decrypted plaintext bytes
     * @throws CryptoException on error during decryption
     */
    public byte[] base64DecodeAndDecryptLargeData(String cipherText) throws CryptoException {

        if (cipherText == null) {
            throw new CryptoException("Ciphertext can't be null.");
        }
        if (!isChunkedCipherText(cipherText)) {
            return base64DecodeAndDecrypt(cipherText);
        }
        String[] encodedChunks = cipherText.substring(CHUNK_MARKER.length()).split(CHUNK_DELIMITER);
        ByteArrayOutputStream plainTextStream = new ByteArrayOutputStream();
        try {
            for (String encodedChunk : encodedChunks) {
                if (encodedChunk.isEmpty()) {
                    continue;
                }
                byte[] decrypted = base64DecodeAndDecrypt(encodedChunk);
                plainTextStream.write(decrypted, 0, decrypted.length);
            }
        } catch (Exception e) {
            throw new CryptoException("Error occurred while reassembling chunked plaintext.", e);
        }
        return plainTextStream.toByteArray();
    }

    /**
     * @param value a stored ciphertext value
     * @return {@code true} if the value is in the chunked ({@code rsachunk:v1:}) format
     */
    public boolean isChunkedCipherText(String value) {

        return value != null && value.startsWith(CHUNK_MARKER);
    }

    /**
     * Positively identifies the symmetric (AES) internal crypto provider from ServerConfiguration. Anything else -
     * the RSA keystore provider, or an unset/unreadable value - returns {@code false}, so the caller uses the
     * size-safe chunked path.
     */
    private boolean isSymmetricInternalProvider() {

        try {
            String providerClass = (serverConfigService == null) ? null
                    : serverConfigService.getFirstProperty(INTERNAL_CRYPTO_PROVIDER_CONFIG);
            return providerClass != null && providerClass.trim().endsWith(SYMMETRIC_PROVIDER_SUFFIX);
        } catch (Exception e) {
            log.warn("Unable to determine the internal crypto provider; using the size-safe (chunked) "
                    + "encryption path.", e);
            return false;
        }
    }

    /**
     * This util method will extract the original cipher text content from self-contained cipher
     *
     * @param cipher cipher text in as a byte array
     * @return returns
     */
    public byte[] extractOriginalCipher(byte[] cipher) {
        CipherHolder cipherHolder = cipherTextToCipherHolder(cipher);
        if (cipherHolder != null) {
            return cipherHolder.getCipherBase64Decoded();
        }
        return cipher;
    }

    /**
     * This function will create self-contained ciphertext with metadata
     *
     * @deprecated since 4.6.1.
     * @param originalCipher ciphertext need to wrap with metadata
     * @param transformation transformation used to encrypt ciphertext
     * @param certificate certificate that holds relevant keys used to encrypt
     * @return setf-contained ciphertext
     * @throws CertificateEncodingException
     * @throws NoSuchAlgorithmException
     */
    @Deprecated
    public byte[] createSelfContainedCiphertext(byte[] originalCipher, String transformation, Certificate certificate)
            throws CertificateEncodingException, NoSuchAlgorithmException {

        CipherHolder cipherHolder = new CipherHolder();
        cipherHolder.setCipherText(Base64.encode(originalCipher));
        cipherHolder.setTransformation(transformation);
        cipherHolder.setThumbPrint(calculateThumbprint(certificate, "SHA-1"), "SHA-1");
        String cipherWithMetadataStr = gson.toJson(cipherHolder);
        if (log.isDebugEnabled()) {
            log.debug("Cipher with meta data : " + cipherWithMetadataStr);
        }
        return cipherWithMetadataStr.getBytes(Charset.defaultCharset());
    }

    /**
     * Function to convert cipher byte array to {@link CipherHolder}
     * @deprecated  use {@link #cipherTextToCipherMetaDataHolder(byte[])} instead.
     *
     * @param cipherText cipher text as a byte array
     * @return if cipher text is not a cipher with meta data
     */
    @Deprecated
    public CipherHolder cipherTextToCipherHolder(byte[] cipherText) {

        String cipherStr = new String(cipherText, Charset.defaultCharset());
        try {
            return gson.fromJson(cipherStr, CipherHolder.class);
        } catch (JsonSyntaxException e) {
            if (log.isDebugEnabled()) {
                log.debug("Deserialization failed since cipher string is not representing cipher with metadata");
            }
            return null;
        }
    }

    /**
     * Function to convert cipher byte array to {@link CipherMetaDataHolder}
     *
     * @param cipherText cipher text as a byte array
     * @return if cipher text is not a cipher with meta data
     */
    public CipherMetaDataHolder cipherTextToCipherMetaDataHolder(byte[] cipherText) {

        String cipherStr = new String(cipherText, Charset.defaultCharset());
        try {
            return gson.fromJson(cipherStr, CipherMetaDataHolder.class);
        } catch (JsonSyntaxException e) {
            if (log.isDebugEnabled()) {
                log.debug("Deserialization failed since cipher string is not representing cipher with metadata");
            }
            return null;
        }
    }

    private String calculateThumbprint(Certificate certificate, String digest)
            throws NoSuchAlgorithmException, CertificateEncodingException {

        MessageDigest messageDigest = MessageDigest.getInstance(digest);
        messageDigest.update(certificate.getEncoded());
        byte[] digestByteArray = messageDigest.digest();

        // convert digest in form of byte array to hex format
        StringBuffer strBuffer = new StringBuffer();

        for (int i = 0; i < digestByteArray.length; i++) {
            int leftNibble = (digestByteArray[i] & 0xF0) >> 4;
            int rightNibble = (digestByteArray[i] & 0x0F);
            strBuffer.append(HEX_CHARACTERS[leftNibble]).append(HEX_CHARACTERS[rightNibble]);
        }

        return strBuffer.toString();
    }

    private void failIfEncryptDecryptInputsAreInvalid(byte[] data, String algorithm,
                                                      String internalCryptoProviderType)
            throws CryptoException {

        if (data == null) {
            throw new CryptoException("Plaintext can't be null.");
        }

        if (StringUtils.isBlank(algorithm)) {
            throw new CryptoException("Encryption algorithm can't be null.");
        }

        if (StringUtils.isBlank(internalCryptoProviderType)) {
            throw new CryptoException("Internal crypto provider can't be null.");
        }
    }

    /**
     * This method returns the preferred JCE provider identifier to be used.
     *
     * @return jce provider identifier name
     */
    private String getPreferredJceProviderIdentifier() {
        String provider = System.getProperty(ServerConstants.JCE_PROVIDER_PARAMETER);
        if (ServerConstants.BOUNCY_CASTLE_FIPS_PROVIDER_IDENTIFIER.equalsIgnoreCase(provider)) {
            return ServerConstants.BOUNCY_CASTLE_FIPS_PROVIDER_IDENTIFIER;
        }
        return ServerConstants.BOUNCY_CASTLE_PROVIDER_IDENTIFIER;
    }
}

