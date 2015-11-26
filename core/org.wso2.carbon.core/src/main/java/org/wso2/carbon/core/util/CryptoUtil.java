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

import com.sun.xml.internal.bind.v2.TODO;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.core.internal.CarbonCoreServiceComponent;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.i18n.Messages;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Properties;

/**
 * The utility class to encrypt/decrypt passwords to be stored in the
 * database.
 */
public class CryptoUtil {

    private static Log log = LogFactory.getLog(CryptoUtil.class);

    private String keyAlias;

    private String keyPass;

    private ServerConfigurationService serverConfigService;

    private RegistryService registryService;

    private static CryptoUtil instance = null;

    private static SecretKey symmetricKey = null;
    private static boolean isSymmetricKeyFromFile = false;
    private static String symmetricKeyEncryptAlgoDefault = "AES";
    private static String symmetricKeySecureVaultAliasDefault = "symmetric.key.value";
    private String propertyKey = "symmetric.key";
    private String symmetricKeyEncryptEnabled;
    private String symmetricKeyEncryptAlgo;
    private String symmetricKeySecureVaultAlias;

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
        this.keyAlias = this.serverConfigService.getFirstProperty("Security.KeyStore.KeyAlias");
        this.keyPass = this.serverConfigService.getFirstProperty("Security.KeyStore.KeyPassword");
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
     * @return The cipher text bytes
     * @throws CryptoException On error during encryption
     */
    public byte[] encrypt(byte[] plainTextBytes) throws CryptoException {

        byte[] encryptedKey;

        try {
            if (Boolean.valueOf(symmetricKeyEncryptEnabled)) {
                encryptedKey = encryptWithSymmetricKey(plainTextBytes);
            } else {
                KeyStoreManager keyMan = KeyStoreManager.getInstance(
                        MultitenantConstants.SUPER_TENANT_ID,
                        this.getServerConfigService(),
                        this.getRegistryService());
                KeyStore keyStore = keyMan.getPrimaryKeyStore();

                Certificate[] certs = keyStore.getCertificateChain(keyAlias);
                Cipher cipher = Cipher.getInstance("RSA", "BC");
                cipher.init(Cipher.ENCRYPT_MODE, certs[0].getPublicKey());

                encryptedKey = cipher.doFinal(plainTextBytes);
            }
        } catch (Exception e) {
            throw new
                    CryptoException(Messages.getMessage("erorDuringEncryption"), e);
        }
        return encryptedKey;
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

        byte[] decyptedValue;

        try {
            if (Boolean.valueOf(symmetricKeyEncryptEnabled)) {
                decyptedValue = decryptWithSymmetricKey(cipherTextBytes);
            } else {
                KeyStoreManager keyMan = KeyStoreManager.getInstance(
                        MultitenantConstants.SUPER_TENANT_ID,
                        this.getServerConfigService(),
                        this.getRegistryService());
                KeyStore keyStore = keyMan.getPrimaryKeyStore();
                PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias,
                        keyPass.toCharArray());

                Cipher cipher = Cipher.getInstance("RSA", "BC");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);

                decyptedValue = cipher.doFinal(cipherTextBytes);
            }
        } catch (Exception e) {
            throw new CryptoException("errorDuringDecryption", e);
        }
        return decyptedValue;
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

    private void generateSymmetricKey() throws CryptoException {

        FileInputStream fileInputStream = null;
        OutputStream output = null;
        KeyGenerator generator = null;
        String secretAlias;
        String encryptionAlgo;
        Properties properties;

        try {
            ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
            symmetricKeyEncryptEnabled = serverConfiguration.getFirstProperty("SymmetricEncryption.IsEnabled");
            symmetricKeyEncryptAlgo = serverConfiguration.getFirstProperty("SymmetricEncryption.Algorithm");
            symmetricKeySecureVaultAlias = serverConfiguration.getFirstProperty("SymmetricEncryption.SecureVaultAlias");

            String filePath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources" +
                    File.separator + "security" + File.separator + "symmetric-key.properties";

            File file = new File(filePath);
            if (file.exists()) {
                fileInputStream = new FileInputStream(file);
                properties = new Properties();
                properties.load(fileInputStream);

                SecretResolver secretResolver = SecretResolverFactory.create(properties);
                if (symmetricKeySecureVaultAlias == null) {
                    secretAlias = symmetricKeySecureVaultAliasDefault;
                } else {
                    secretAlias = symmetricKeySecureVaultAlias;
                }

                if (symmetricKeyEncryptAlgo == null) {
                    encryptionAlgo = symmetricKeyEncryptAlgoDefault;
                } else {
                    encryptionAlgo = symmetricKeyEncryptAlgo;
                }

                if (secretResolver != null && secretResolver.isInitialized()) {
                    if (secretResolver.isTokenProtected(secretAlias)) {
                        symmetricKey = new SecretKeySpec(Base64.decode(secretResolver.resolve(secretAlias)), 0,
                                Base64.decode(secretResolver.resolve(secretAlias)).length, encryptionAlgo);
                    } else {
                        symmetricKey = new SecretKeySpec(Base64.decode((String) properties.get(secretAlias)), 0,
                                Base64.decode((String) properties.get(secretAlias)).length, encryptionAlgo);
                    }
                } else if (properties.containsKey(propertyKey)) {
                    symmetricKey = new SecretKeySpec(Base64.decode(properties.getProperty(propertyKey)), 0,
                            Base64.decode(properties.getProperty(propertyKey)).length, encryptionAlgo);
                }

                if (symmetricKey != null) {
                    isSymmetricKeyFromFile = true;
                }
            }

            if (!isSymmetricKeyFromFile) {
                throw new CryptoException("Error in generating symmetric key. Symmetric key is not available.");
                /*generator = KeyGenerator.getInstance(encryptionAlgo);
                SecretKey key = generator.generateKey();
                symmetricKey = key;
                byte[] symmetricKey = key.getEncoded();
                String stringKey = Base64.encode(symmetricKey);

                // TODO : store in registry (since this file may not be sync with other nodes)
                if (!file.exists()) {
                    file.createNewFile();
                }
                output = new FileOutputStream(file);
                fileInputStream = new FileInputStream(file);
                properties = new Properties();
                properties.load(fileInputStream);
                if (!properties.containsKey(propertyKey)) {
                    properties.setProperty(propertyKey, stringKey);
                    properties.store(output, null);
                }*/
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new CryptoException("Error in generating symmetric key", e);
        }
    }

    private byte[] encryptWithSymmetricKey(byte[] plainText) throws CryptoException {
        Cipher c = null;
        byte[] encryptedData = null;
        String encryptionAlgo;
        try {
            if (symmetricKeyEncryptAlgo == null) {
                encryptionAlgo = symmetricKeyEncryptAlgoDefault;
            } else {
                encryptionAlgo = symmetricKeyEncryptAlgo;
            }
            c = Cipher.getInstance(encryptionAlgo);
            c.init(Cipher.ENCRYPT_MODE, symmetricKey);
            encryptedData = c.doFinal(plainText);
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException |
                NoSuchPaddingException | InvalidKeyException e) {
            throw new CryptoException("Error when encrypting data.", e);
        }
        return encryptedData;
    }

    private byte[] decryptWithSymmetricKey(byte[] encryptionBytes) throws CryptoException {
        Cipher c = null;
        byte[] decryptedData = null;
        String encryptionAlgo;
        try {
            if (symmetricKeyEncryptAlgo == null) {
                encryptionAlgo = symmetricKeyEncryptAlgoDefault;
            } else {
                encryptionAlgo = symmetricKeyEncryptAlgo;
            }
            c = Cipher.getInstance(encryptionAlgo);
            c.init(Cipher.DECRYPT_MODE, symmetricKey);
            decryptedData = c.doFinal(encryptionBytes);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException |
                NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new CryptoException("Error when decrypting data.", e);
        }
        return decryptedData;
    }
}

