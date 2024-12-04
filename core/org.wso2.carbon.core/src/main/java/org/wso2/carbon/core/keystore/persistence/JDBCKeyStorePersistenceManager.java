/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.core.keystore.persistence;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.core.keystore.persistence.dao.KeyStoreDAO;
import org.wso2.carbon.core.keystore.persistence.model.KeyStoreModel;
import org.wso2.carbon.core.security.KeyStoreMetadata;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class JDBCKeyStorePersistenceManager implements KeyStorePersistenceManager {

    private static final KeyStoreDAO keyStoreDAO = new KeyStoreDAO();

    /**
     * Add the key store to the database.
     *
     * @param keyStoreName           Name of the key store.
     * @param keystoreContent        Content of the key store.
     * @param provider               Provider of the key store.
     * @param keyStorePasswordChar   Key Store Password as a character array.
     * @param privateKeyPasswordChar Private key password as a character array.
     * @param tenantId               Tenant ID.
     * @throws SecurityException If an error occurs while adding the key store.
     */
    @Override
    public void addKeystore(String keyStoreName, byte[] keystoreContent, String provider, String type,
                            char[] keyStorePasswordChar, char[] privateKeyPasswordChar, int tenantId) {

        if (isKeyStoreExistsInDatabase(keyStoreName, tenantId)) {
            throw new SecurityException("Key store " + keyStoreName + " already available");
        }

        boolean isTenantPrimaryKeyStore = false;
        try (InputStream inputStream = new ByteArrayInputStream(keystoreContent)) {
            if (tenantId != MultitenantConstants.SUPER_TENANT_ID &&
                    !keyStoreDAO.isTenantPrimaryKeyStoreExists(tenantId)) {
                isTenantPrimaryKeyStore = true;
            }

            KeyStore keyStore = KeystoreUtils.getKeystoreInstance(type);
            keyStore.load(inputStream, keyStorePasswordChar);
            String pvtKeyAlias = KeyStoreUtil.getPrivateKeyAlias(keyStore);

            KeyStoreModel keyStoreModel = new KeyStoreModel();
            keyStoreModel.setName(keyStoreName);
            keyStoreModel.setType(type);
            keyStoreModel.setProvider(provider);
            keyStoreModel.setTenantId(tenantId);
            keyStoreModel.setEncryptedPassword(encryptPassword(keyStorePasswordChar));
            keyStoreModel.setContent(keystoreContent);

            if (StringUtils.isNotBlank(pvtKeyAlias)) {
                keyStoreModel.setPrivateKeyAlias(pvtKeyAlias);
                if (ArrayUtils.isNotEmpty(privateKeyPasswordChar)) {
                    keyStoreModel.setEncryptedPrivateKeyPass(encryptPassword(privateKeyPasswordChar));
                    // Check weather private key password is correct.
                    keyStore.getKey(pvtKeyAlias, privateKeyPasswordChar);
                }
            }

            if (isTenantPrimaryKeyStore) {
                // Generate the public cert ID for the tenant primary key store public cert.
                keyStoreModel.setPublicCertId(generatePublicCertId());
            }
            keyStoreDAO.addKeystore(keyStoreModel);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException |
                 UnrecoverableKeyException | CertificateException e) {
            throw new SecurityException("Error adding key store " + keyStoreName, e);
        }
    }

    /**
     * Get the key store from the database.
     *
     * @param keyStoreName Name of the key store.
     * @param tenantId     Tenant Id.
     * @return Key store.
     * @throws SecurityException If an error occurs while getting the key store.
     */
    @Override
    public KeyStore getKeyStore(String keyStoreName, int tenantId) throws SecurityException {

        Optional<KeyStoreModel> keyStoreModel = keyStoreDAO.getKeyStore(keyStoreName, tenantId);
        if (!keyStoreModel.isPresent()) {
            throw new SecurityException("Key Store with a name: " + keyStoreName + " does not exist.");
        }

        char[] passwordChar = new char[0];
        try {
            byte[] keyStoreContent = keyStoreModel.get().getContent();
            String keyStoreType = keyStoreModel.get().getType();
            KeyStore keyStore = KeystoreUtils.getKeystoreInstance(keyStoreType);
            String encryptedPassword = keyStoreModel.get().getEncryptedPassword();
            passwordChar = decryptPassword(encryptedPassword);
            try (ByteArrayInputStream stream = new ByteArrayInputStream(keyStoreContent)) {
                keyStore.load(stream, passwordChar);
                return keyStore;
            }
        } catch (KeyStoreException | NoSuchProviderException | IOException | CertificateException |
                 NoSuchAlgorithmException e) {
            throw new SecurityException("Error getting key store: " + keyStoreName, e);
        } finally {
            clearPasswordCharArray(passwordChar);
        }
    }

    /**
     * Method to retrieve list of keystore metadata of all the keystores in a tenant.
     *
     * @param tenantId Tenant Id.
     * @return List of KeyStoreMetaData objects.
     * @throws SecurityException If an error occurs while retrieving the keystore data.
     */
    @Override
    public List<KeyStoreMetadata> listKeyStores(int tenantId) throws SecurityException {

        List<KeyStoreMetadata> metadataList = new ArrayList<>();

        List<KeyStoreModel> keyStoreModels = keyStoreDAO.listKeyStores(tenantId);
        for (KeyStoreModel keyStoreModel : keyStoreModels) {
            KeyStoreMetadata metadata = getKeyStoreMetaDataFromKeyStoreModel(keyStoreModel);
            metadataList.add(metadata);
        }
        return metadataList;
    }

    /**
     * Update the key store in the database.
     *
     * @param keyStoreName Key store name.
     * @param keyStore     Updated key store.
     * @param tenantId     Tenant Id.
     */
    @Override
    public void updateKeyStore(String keyStoreName, KeyStore keyStore, int tenantId) {

        char[] passwordChar = new char[0];
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            String encodedPassword = keyStoreDAO.getEncryptedKeyStorePassword(keyStoreName, tenantId);
            if (encodedPassword == null) {
                throw new SecurityException("Key store " + keyStoreName + " does not exist.");
            }
            passwordChar = decryptPassword(encodedPassword);
            keyStore.store(outputStream, passwordChar);
            outputStream.flush();

            KeyStoreModel keyStoreModel = new KeyStoreModel();
            keyStoreModel.setName(keyStoreName);
            keyStoreModel.setContent(outputStream.toByteArray());
            keyStoreModel.setTenantId(tenantId);
            keyStoreDAO.updateKeyStore(keyStoreModel);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new SecurityException("Error updating tenanted key store: " + keyStoreName, e);
        } finally {
            clearPasswordCharArray(passwordChar);
        }
    }

    /**
     * Delete the key store from the database.
     *
     * @param keyStoreName Name of the key store.
     * @param tenantId     Tenant Id.
     * @throws SecurityException If an error occurs while deleting the key store.
     */
    @Override
    public void deleteKeyStore(String keyStoreName, int tenantId) throws SecurityException {

        keyStoreDAO.deleteKeyStore(keyStoreName, tenantId);

    }

    /**
     * Get the last modified date of the key store.
     *
     * @param keyStoreName Key store name.
     * @param tenantId     Tenant Id.
     * @return Last modified date of the key store.
     */
    @Override
    public Date getKeyStoreLastModifiedDate(String keyStoreName, int tenantId) {

        return keyStoreDAO.getKeyStoreLastModifiedDate(keyStoreName, tenantId);
    }

    /**
     * Get the password for the given key store name.
     *
     * @param keyStoreName Key store name.
     * @return KeyStore Password.
     * @throws SecurityException If there is an error while getting the key store password.
     */
    @Override
    public char[] getKeyStorePassword(String keyStoreName, int tenantId) throws SecurityException {

        String encodedPassword = keyStoreDAO.getEncryptedKeyStorePassword(keyStoreName, tenantId);
        if (encodedPassword == null) {
            throw new SecurityException("Key store " + keyStoreName + " does not exist.");
        }
        return decryptPassword(encodedPassword);
    }

    /**
     * Get the private key password as a character array for the given key store name.
     *
     * @param keyStoreName Key store name.
     * @param tenantId     Tenant Id.
     * @return Private key password as a character array.
     * @throws SecurityException If an error occurs while getting the private key password.
     */
    @Override
    public char[] getPrivateKeyPassword(String keyStoreName, int tenantId) throws SecurityException {

        String encodedPassword = keyStoreDAO.getEncryptedPrivateKeyPassword(keyStoreName, tenantId);
        if (encodedPassword == null) {
            return new char[0];
        }
        return decryptPassword(encodedPassword);
    }

    private boolean isKeyStoreExistsInDatabase(String keyStoreName, int tenantId) throws SecurityException {

        Optional<KeyStoreModel> keyStoreModel = keyStoreDAO.getKeyStore(keyStoreName, tenantId);
        return keyStoreModel.isPresent();
    }

    /**
     * This method encrypts the given passwordChar using the default crypto util.
     *
     * @param passwordChar Password to be encrypted as a character array.
     * @return encrypted password.
     */
    private String encryptPassword(char[] passwordChar) {

        try {
            return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(String.valueOf(passwordChar).getBytes());
        } catch (CryptoException e) {
            throw new SecurityException("Error encrypting the passwordChar", e);
        }
    }

    /**
     * This method decrypts the given encrypted password using the default crypto util.
     *
     * @param encryptedPassword Encrypted password.
     * @return decrypted password as a character array.
     */
    private char[] decryptPassword(String encryptedPassword) {

        try {
            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            return new String(cryptoUtil.base64DecodeAndDecrypt(encryptedPassword)).toCharArray();
        } catch (CryptoException e) {
            throw new SecurityException("Error decrypting the password", e);
        }
    }

    /**
     * Generates an ID for the public certificate, which is used as a file name suffix for the certificate.
     * e.g. If keystore name is 'example-com.jks', public cert name will be 'example-com-343743.cert'.
     *
     * @return generated id to be used as a file name appender.
     */
    private static String generatePublicCertId() {

        String uuid = UUIDGenerator.getUUID();
        return uuid.substring(uuid.length() - 6, uuid.length() - 1);
    }

    private static void clearPasswordCharArray(char[] passwordChar) {

        Arrays.fill(passwordChar, '\0');
    }

    private KeyStoreMetadata getKeyStoreMetaDataFromKeyStoreModel(KeyStoreModel keyStoreModel) {

        KeyStoreMetadata metadata = new KeyStoreMetadata();
        metadata.setKeyStoreName(keyStoreModel.getName());
        metadata.setProvider(keyStoreModel.getProvider());
        metadata.setKeyStoreType(keyStoreModel.getType());
        metadata.setPrivateStore(keyStoreModel.getPrivateKeyAlias() != null);
        String publicCertId = keyStoreModel.getPublicCertId();

        if (StringUtils.isNotBlank(publicCertId)) {
            metadata.setPublicCertId(keyStoreModel.getPublicCertId());
            byte[] keystoreContent = keyStoreModel.getContent();
            try (InputStream inputStream = new ByteArrayInputStream(keystoreContent)) {
                KeyStore keyStore = KeyStore.getInstance(keyStoreModel.getType());
                keyStore.load(inputStream, decryptPassword(keyStoreModel.getEncryptedPassword()));
                X509Certificate publicCert =
                        (X509Certificate) keyStore.getCertificate(keyStoreModel.getPrivateKeyAlias());
                metadata.setPublicCert(publicCert.getEncoded());
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
                throw new SecurityException("Error getting public cert from keystore", e);
            }
        }
        return metadata;
    }
}
