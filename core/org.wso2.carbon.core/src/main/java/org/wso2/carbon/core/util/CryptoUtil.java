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
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.crypto.api.CryptoContext;
import org.wso2.carbon.crypto.api.CryptoService;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

/**
 * The utility class to encrypt/decrypt passwords to be stored in the
 * database.
 */
public class CryptoUtil {

    private static final String CIPHER_TRANSFORMATION_SYSTEM_PROPERTY = "org.wso2.CipherTransformation";
    private static Log log = LogFactory.getLog(CryptoUtil.class);
    private ServerConfigurationService serverConfigService;
    private RegistryService registryService;
    private Gson gson = new Gson();
    private static CryptoUtil instance = null;
    private static final char[] HEX_CHARACTERS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
                                                            'C', 'D', 'E', 'F'};

    private static final String DEFAULT_CRYPTO_ALGORITHM = "RSA";

    private static final String CRYPTO_API_PROVIDER_BC = "BC";

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

            // Set the default crypto algorithm.
            String algorithm = DEFAULT_CRYPTO_ALGORITHM;

            if (!StringUtils.isBlank(cipherTransformation)) {
                algorithm = cipherTransformation;
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Cipher transformation is enabled. Crypto algorithm: '%s'", algorithm));
                }
            }

            if (StringUtils.isNotBlank(cipherTransformation) && plainTextBytes.length == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Plaintext is empty. An empty array will be used as the ciphertext bytes.");
                }
                encryptedKey = StringUtils.EMPTY.getBytes();
            }else{
                encryptedKey = cryptoService.encrypt(plainTextBytes, algorithm, CRYPTO_API_PROVIDER_BC);
            }

            if (StringUtils.isNotBlank(cipherTransformation) && returnSelfContainedCipherText) {

                Certificate certificate = cryptoService.getCertificate(CryptoContext.buildEmptyContext(
                        MultitenantConstants.SUPER_TENANT_ID, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));

                encryptedKey = createSelfContainedCiphertext(encryptedKey, algorithm, certificate);
            }

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

            // Set the default crypto algorithm to be used when a cipher transformation is not found.
            String algorithm = DEFAULT_CRYPTO_ALGORITHM;

            String cipherTransformation = System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY);

            if (cipherTransformation != null) {
                CipherHolder cipherHolder = cipherTextToCipherHolder(cipherTextBytes);
                if (cipherHolder != null) {
                    //cipher with meta data
                    if (log.isDebugEnabled()) {
                        log.debug("Cipher transformation for decryption : " + cipherHolder.getTransformation());
                    }
                    algorithm = cipherHolder.getTransformation();
                    cipherTextBytes = cipherHolder.getCipherBase64Decoded();
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
                decryptedValue = cryptoService.decrypt(cipherTextBytes, algorithm, CRYPTO_API_PROVIDER_BC);
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

            // Set the default crypto algorithm to be used when a cipher transformation is not found.
            String algorithm = DEFAULT_CRYPTO_ALGORITHM;

            if (cipherTransformation != null) {
                algorithm = cipherTransformation;
            }

            if (cipherTextBytes.length == 0) {
                decryptedValue = StringUtils.EMPTY.getBytes();
                if (log.isDebugEnabled()) {
                    log.debug("Ciphertext is empty. An empty array will be used as the plaintext bytes.");
                }
            }else {
                decryptedValue = cryptoService.decrypt(cipherTextBytes, algorithm, CRYPTO_API_PROVIDER_BC);
            }

            return decryptedValue;

        } catch (Exception e) {
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
     * @param originalCipher ciphertext need to wrap with metadata
     * @param transformation transformation used to encrypt ciphertext
     * @param certificate certificate that holds relevant keys used to encrypt
     * @return setf-contained ciphertext
     * @throws CertificateEncodingException
     * @throws NoSuchAlgorithmException
     */
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
     *
     * @param cipherText cipher text as a byte array
     * @return if cipher text is not a cipher with meta data
     */
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
}

