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

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.i18n.Messages;
import sun.security.krb5.KrbCryptoException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
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

    private static String algorithm = "AES";

    /**
     * This method returns CryptoUtil object, where this should only be used at runtime,
     * after the server is properly initialized, or else, use the overloaded method,
     * CryptoUtil#getDefaultCryptoUtil(ServerConfigurationService).
     *
     * @return
     */
    public static CryptoUtil getDefaultCryptoUtil() throws CryptoException {
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
            RegistryService registryService) throws CryptoException {
        if (instance == null) {
            instance = new CryptoUtil(serverConfigService, registryService);
        }
        return instance;
    }

    private CryptoUtil(ServerConfigurationService serverConfigService,
                       RegistryService registryService) throws CryptoException {
        this.serverConfigService = serverConfigService;
        this.registryService = registryService;
        this.keyAlias = this.serverConfigService.getFirstProperty("Security.KeyStore.KeyAlias");
        this.keyPass = this.serverConfigService.getFirstProperty("Security.KeyStore.KeyPassword");
        generateEncryptedSymmetricKey();
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
        try {

            KeyStoreManager keyMan = KeyStoreManager.getInstance(
                    MultitenantConstants.SUPER_TENANT_ID,
                    this.getServerConfigService(),
                    this.getRegistryService());
            KeyStore keyStore = keyMan.getPrimaryKeyStore();

            Certificate[] certs = keyStore.getCertificateChain(keyAlias);
            Cipher cipher = Cipher.getInstance("RSA", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, certs[0].getPublicKey());

            return cipher.doFinal(plainTextBytes);

        } catch (Exception e) {
            e.printStackTrace();
            throw new
                    CryptoException(Messages.getMessage("erorDuringEncryption"), e);
        }
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
        return Base64.encode(encryptWithSymmetricKey(plainText));
    }

    /**
     * Decrypt the given cipher text value using the WSO2 WSAS key
     *
     * @param cipherTextBytes The cipher text to be decrypted
     * @return Decrypted bytes
     * @throws CryptoException On an error during decryption
     */
    public byte[] decrypt(byte[] cipherTextBytes) throws CryptoException {
        try {

            KeyStoreManager keyMan = KeyStoreManager.getInstance(
                    MultitenantConstants.SUPER_TENANT_ID,
                    this.getServerConfigService(),
                    this.getRegistryService());
            KeyStore keyStore = keyMan.getPrimaryKeyStore();
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias,
                    keyPass.toCharArray());

            Cipher cipher = Cipher.getInstance("RSA", "BC");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            return cipher.doFinal(cipherTextBytes);

        } catch (Exception e) {
            e.printStackTrace();
            throw new CryptoException("errorDuringDecryption", e);
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
        return decryptWithSymmetricKey(Base64.decode(base64CipherText));
    }

    private void generateEncryptedSymmetricKey() throws CryptoException {
        //Generate Symmetric key
        KeyGenerator generator = null;
        byte[] encryptedSymmetricKey = null;
        try {
            generator = KeyGenerator.getInstance(algorithm);
            generator.init(128);
            SecretKey key = generator.generateKey();
            symmetricKey = key;
            byte[] symmetricKey = key.getEncoded();
            encryptedSymmetricKey = encrypt(symmetricKey);
            storeEncryptedSymmetricKey(encryptedSymmetricKey);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Error in generating symmetric key", e);
        }
    }

/*    private byte[] decryptSymmetricKey(){
        //get the symmetric key from file/registry
        byte[] decryptedSymmetricKey = new byte[0];
        try {
            decryptedSymmetricKey = decrypt(new byte[0]);
        } catch (CryptoException e) {
            e.printStackTrace();
        }
        return decryptedSymmetricKey;
    }*/

    private void storeEncryptedSymmetricKey(byte[] encryptedKey) throws CryptoException {
        FileInputStream fileInputStream = null;
        OutputStream output = null;
        String symmetricKey;

        String configPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "conf" +
                File.separator + "symmetric-key.properties";

        symmetricKey = Base64.encode(encryptedKey);

        File registryXML = new File(configPath);
        try {
            if(!registryXML.exists()) {
                registryXML.createNewFile();
            }

            output = new FileOutputStream(registryXML);
            Properties properties = new Properties();
            if(!properties.containsKey("symmetric.key") || properties.getProperty("symmetric.key") == null || properties.getProperty("symmetric.key") == "") {
                properties.setProperty("symmetric.key", symmetricKey);
                properties.store(output, null);
            }
        } catch (IOException e) {
            throw new CryptoException("Error in storing symmetric key", e);
        }

       /* if (registryXML.exists()) {
            try {
                fileInputStream = new FileInputStream(registryXML);
                Properties properties = new Properties();
                properties.load(fileInputStream);
                SecretResolver secretResolver = SecretResolverFactory.create(properties);
                //Resolved the secret password.
                String secretAlias = "symmetric.key.value";

                if (secretResolver != null && secretResolver.isInitialized()) {
                    if (secretResolver.isTokenProtected(secretAlias)) {
                        symmetricKey = secretResolver.resolve(secretAlias);
                    } else {
                        symmetricKey = (String) properties.get(secretAlias);
                    }
                }
                if(!StringUtils.isEmpty(symmetricKey)) {
                   output
                }
            } catch (IOException e) {
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        log.error("Failed to close the FileInputStream, file : " + configPath);
                    }
                }
            }
        }*/
    }

/*    private void retrieveSymmetricKey(byte[] encryptedKey){
        // retrieve the symmetric key from memory
    }*/

    private byte[] encryptWithSymmetricKey(byte[] plainText) throws CryptoException {
        Cipher c = null;
        byte[] encryptedData = null;
        try {
            c = Cipher.getInstance(algorithm);
            c.init(Cipher.ENCRYPT_MODE, symmetricKey);
            encryptedData = c.doFinal(plainText);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Error when encrypting data.", e);
        } catch (IllegalBlockSizeException e) {
            throw new CryptoException("Error when encrypting data.", e);
        } catch (BadPaddingException e) {
            throw new CryptoException("Error when encrypting data.", e);
        } catch (NoSuchPaddingException e) {
            throw new CryptoException("Error when encrypting data.", e);
        } catch (InvalidKeyException e) {
            throw new CryptoException("Error when encrypting data.", e);
        }
        return encryptedData;
    }

    private byte[] decryptWithSymmetricKey(byte[] encryptionBytes) throws CryptoException {
        Cipher c = null;
        byte[] decryptedData = null;
        try {
            c.init(Cipher.DECRYPT_MODE, symmetricKey);
            decryptedData = c.doFinal(encryptionBytes);
        } catch (InvalidKeyException e) {
            throw new CryptoException("Error when decrypting data.", e);
        } catch (BadPaddingException e) {
            throw new CryptoException("Error when decrypting data.", e);
        } catch (IllegalBlockSizeException e) {
            throw new CryptoException("Error when decrypting data.", e);
        }
        return decryptedData;
    }
}

