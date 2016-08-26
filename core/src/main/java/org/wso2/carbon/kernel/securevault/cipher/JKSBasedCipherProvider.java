/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.kernel.securevault.cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.securevault.MasterKey;
import org.wso2.carbon.kernel.securevault.SecureVaultUtils;
import org.wso2.carbon.kernel.securevault.config.model.SecretRepositoryConfiguration;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;
import org.wso2.carbon.kernel.utils.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Optional;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

/**
 * This class is responsible for providing encryption and decryption capabilities based on the JKS.
 *
 * @since 5.2.0
 */
public class JKSBasedCipherProvider {
    private static Logger logger = LoggerFactory.getLogger(JKSBasedCipherProvider.class);
    private static final String LOCATION = "keystoreLocation";
    private static final String ALIAS = "privateKeyAlias";
    public static final String KEY_STORE_PASSWORD = "keyStorePassword";
    public static final String PRIVATE_KEY_PASSWORD = "privateKeyPassword";
    private static final String JKS = "JKS";
    private Cipher encryptionCipher;
    private Cipher decryptionCipher;

    public void init(SecretRepositoryConfiguration secretRepositoryConfiguration, List<MasterKey> masterKeys)
            throws SecureVaultException {
        String keystoreLocation = secretRepositoryConfiguration.getParameter(LOCATION)
                .orElseThrow(() -> new SecureVaultException("Key store location is mandatory"));

        String privateKeyAlias = secretRepositoryConfiguration.getParameter(ALIAS)
                .orElseThrow(() -> new SecureVaultException("Private key alias is mandatory"));

        MasterKey keyStorePassword = SecureVaultUtils.getSecret(masterKeys, KEY_STORE_PASSWORD);
        MasterKey privateKeyPassword = SecureVaultUtils.getSecret(masterKeys, PRIVATE_KEY_PASSWORD);

        KeyStore keyStore = loadKeyStore(keystoreLocation, keyStorePassword.getMasterKeyValue()
                .orElseThrow(() -> new SecureVaultException("Key store password is mandatory")));

        encryptionCipher = getEncryptionCipher(keyStore, privateKeyAlias);
        decryptionCipher = getDecryptionCipher(keyStore, privateKeyAlias, privateKeyPassword.getMasterKeyValue()
                .orElseThrow(() -> new SecureVaultException("Private key password is mandatory")));
        logger.debug("JKSBasedCipherProvider initialized successfully.");
    }

    public byte[] encrypt(byte[] plainText) throws SecureVaultException {
        return doCipher(encryptionCipher, plainText);
    }

    public byte[] decrypt(byte[] cipherText) throws SecureVaultException {
        return doCipher(decryptionCipher, cipherText);
    }

    private KeyStore loadKeyStore(String keyStorePath, char[] keyStorePassword) throws SecureVaultException {
            try (BufferedInputStream bufferedInputStream = new BufferedInputStream(
                    new FileInputStream(Paths.get(Utils.getCarbonHome().toString(), keyStorePath).toString()))) {
            KeyStore keyStore;
            try {
                keyStore = KeyStore.getInstance(JKS);
                keyStore.load(bufferedInputStream, keyStorePassword);

                logger.debug("Keystore at path : '{}', loaded successfully.", keyStorePath);

                return keyStore;
            } catch (CertificateException e) {
                throw new SecureVaultException("Failed to load certificates from keystore : '" + keyStorePath + "'", e);
            } catch (NoSuchAlgorithmException e) {
                throw new SecureVaultException("Failed to load keystore algorithm at : '" + keyStorePath + "'", e);
            } catch (KeyStoreException e) {
                throw new SecureVaultException("Failed to initialize keystore at : '" + keyStorePath + "'", e);
            }
        } catch (IOException e) {
            throw new SecureVaultException("Unable to find keystore at '" + keyStorePath + "'", e);
        }
    }

    private Cipher getEncryptionCipher(KeyStore keyStore, String alias)
            throws SecureVaultException {
        Certificate certificate;
        try {
            certificate = Optional.ofNullable(keyStore.getCertificate(alias))
                    .orElseThrow(() ->
                            new SecureVaultException("No certificate found with the given alias : " + alias));
        } catch (KeyStoreException e) {
            throw new SecureVaultException("Failed to get certificate for alias '" + alias + "'", e);
        }

        try {
            Cipher cipher = Cipher.getInstance(certificate.getPublicKey().getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, certificate);

            logger.debug("Successfully created an encryption cipher with alias : '{}'", alias);

            return cipher;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecureVaultException("Failed to initialize Cipher for mode '" + Cipher.ENCRYPT_MODE + "'", e);
        }
    }

    private Cipher getDecryptionCipher(KeyStore keyStore, String alias, char[] privateKeyPassword)
            throws SecureVaultException {
        PrivateKey privateKey;
        try {
            privateKey = Optional.ofNullable((PrivateKey) keyStore.getKey(alias, privateKeyPassword))
                    .orElseThrow(() -> new SecureVaultException("No key found with the given alias : " + alias));
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new SecureVaultException("Failed to get private key for alias '" + alias + "'", e);
        }

        try {
            Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            logger.debug("Successfully created a decryption cipher with alias : '{}'", alias);

            return cipher;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecureVaultException("Failed to initialize Cipher for mode '" + Cipher.DECRYPT_MODE + "'", e);
        }
    }

    private byte[] doCipher(Cipher cipher, byte[] original) throws SecureVaultException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             CipherOutputStream cipherOutputStream = new CipherOutputStream(byteArrayOutputStream, cipher);
             InputStream inputStream = new ByteArrayInputStream(original)
        ) {
            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                cipherOutputStream.write(buffer, 0, length);
            }
            cipherOutputStream.flush();
            cipherOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new SecureVaultException("Failed to decrypt the password", e);
        }
    }
}
