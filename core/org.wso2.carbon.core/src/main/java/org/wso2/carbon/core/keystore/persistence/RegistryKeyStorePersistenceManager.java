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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.core.security.KeyStoreMetadata;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.registry.api.Association;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Collection;
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
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This implementation handles the keystore storage/persistence related logics in the Registry.
 */
public class RegistryKeyStorePersistenceManager implements KeyStorePersistenceManager {

    private static final Log LOG = LogFactory.getLog(RegistryKeyStorePersistenceManager.class);
    private static final String REGISTRY_PATH_SEPARATOR = "/";
    private static final String ASSOCIATION_TENANT_KS_PUB_KEY = "assoc.tenant.ks.pub.key";
    private static final String PROP_TENANT_PUB_KEY_FILE_NAME_APPENDER = "tenant.pub.key.file.name.appender";

    /**
     * Add the key store to the registry.
     *
     * @param keyStoreName           Name of the key store.
     * @param keystoreContent        Content of the key store.
     * @param provider               Provider of the key store.
     * @param keyStorePasswordChar   Key Store Password as a character array.
     * @param privateKeyPasswordChar Private key password as a character array.
     * @param tenantId               Tenant ID.
     * @throws SecurityException If an error occurs while adding the key store.
     */
    public void addKeystore(String keyStoreName, byte[] keystoreContent, String provider, String type,
                            char[] keyStorePasswordChar, char[] privateKeyPasswordChar, int tenantId)
            throws SecurityException {

        Registry registry = getGovernanceRegistry(tenantId);
        if (isKeyStoreExistsInRegistry(keyStoreName, registry)) {
            throw new SecurityException("Key store " + keyStoreName + " already available");
        }

        boolean isTenantPrimaryKeyStore = false;
        try (InputStream inputStream = new ByteArrayInputStream(keystoreContent)) {
            if (tenantId != MultitenantConstants.SUPER_TENANT_ID &&
                    !registry.resourceExists(RegistryResources.SecurityManagement.KEY_STORES)) {
                isTenantPrimaryKeyStore = true;
            }

            KeyStore keyStore = KeystoreUtils.getKeystoreInstance(type);
            keyStore.load(inputStream, keyStorePasswordChar);
            String pvtKeyAlias = KeyStoreUtil.getPrivateKeyAlias(keyStore);

            Resource resource = registry.newResource();
            resource.addProperty(RegistryResources.SecurityManagement.PROP_PASSWORD,
                    encryptPassword(keyStorePasswordChar));
            resource.addProperty(RegistryResources.SecurityManagement.PROP_PROVIDER, provider);
            resource.addProperty(RegistryResources.SecurityManagement.PROP_TYPE, keyStore.getType());
            resource.setContent(keystoreContent);

            if (StringUtils.isNotBlank(pvtKeyAlias)) {
                resource.addProperty(RegistryResources.SecurityManagement.PROP_PRIVATE_KEY_ALIAS, pvtKeyAlias);
                if (ArrayUtils.isNotEmpty(privateKeyPasswordChar)) {
                    resource.addProperty(RegistryResources.SecurityManagement.PROP_PRIVATE_KEY_PASS,
                            encryptPassword(privateKeyPasswordChar));
                    // Check weather private key password is correct.
                    keyStore.getKey(pvtKeyAlias, privateKeyPasswordChar);
                }
            }
            registry.put(getKeyStorePath(keyStoreName), resource);

            if (isTenantPrimaryKeyStore) {
                // Create the public key resource for tenant's primary keystore.
                addTenantPublicKey(keyStoreName, (X509Certificate) keyStore.getCertificate(pvtKeyAlias), registry);
            }
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException |
                 UnrecoverableKeyException | CertificateException | RegistryException e) {
            throw new SecurityException("Error adding key store " + keyStoreName, e);
        }
    }

    /**
     * Add tenant's public certificate to the database.
     *
     * @param keyStoreName Name of the key store.
     * @param publicCert   Public certificate of the tenant.
     * @throws SecurityException If an error occurs while adding the tenant's public certificate.
     */
    private void addTenantPublicKey(String keyStoreName, X509Certificate publicCert, Registry registry)
            throws SecurityException {

        try {
            // Create the public key resource.
            Resource pubKeyResource = registry.newResource();
            pubKeyResource.setContent(publicCert.getEncoded());
            pubKeyResource.addProperty(PROP_TENANT_PUB_KEY_FILE_NAME_APPENDER, generatePublicCertId());
            registry.put(RegistryResources.SecurityManagement.TENANT_PUBKEY_RESOURCE, pubKeyResource);

            // Associate the public key with the keystore.
            registry.addAssociation(
                    RegistryResources.SecurityManagement.KEY_STORES + REGISTRY_PATH_SEPARATOR + keyStoreName,
                    RegistryResources.SecurityManagement.TENANT_PUBKEY_RESOURCE, ASSOCIATION_TENANT_KS_PUB_KEY);
        } catch (RegistryException | CertificateEncodingException e) {
            throw new SecurityException("Error when writing the keystore public cert to registry for keystore: " +
                    keyStoreName, e);
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

    /**
     * Get the key store from the registry.
     *
     * @param keyStoreName Name of the key store.
     * @param tenantId     Tenant Id.
     * @return Key store.
     * @throws SecurityException If an error occurs while getting the key store.
     */
    public KeyStore getKeyStore(String keyStoreName, int tenantId) throws SecurityException {

        Registry registry = getGovernanceRegistry(tenantId);
        if (!isKeyStoreExistsInRegistry(keyStoreName, registry)) {
            throw new SecurityException("Key Store with a name: " + keyStoreName + " does not exist.");
        }

        char[] passwordChar = new char[0];
        try {
            Resource resource = registry.get(getKeyStorePath(keyStoreName));
            byte[] bytes = (byte[]) resource.getContent();
            KeyStore keyStore = KeystoreUtils.getKeystoreInstance(resource
                    .getProperty(RegistryResources.SecurityManagement.PROP_TYPE));
            passwordChar = getKeyStorePasswordFromRegistryResource(resource, keyStoreName);
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            keyStore.load(stream, passwordChar);
            resource.discard();
            return keyStore;
        } catch (RegistryException | KeyStoreException | NoSuchProviderException | IOException | CertificateException |
                 NoSuchAlgorithmException e) {
            throw new SecurityException("Error getting key store: " + keyStoreName, e);
        } finally {
            clearPasswordCharArray(passwordChar);
        }
    }

    private String getKeyStorePath(String keyStoreName) {

        return RegistryResources.SecurityManagement.KEY_STORES + REGISTRY_PATH_SEPARATOR + keyStoreName;
    }

    /**
     * Method to retrieve list of keystore metadata of all the keystores in a tenant.
     *
     * @param tenantId Tenant Id.
     * @return List of KeyStoreMetaData objects.
     * @throws SecurityException If an error occurs while retrieving the keystore data.
     */
    public List<KeyStoreMetadata> listKeyStores(int tenantId) throws SecurityException {

        Registry registry = getGovernanceRegistry(tenantId);
        List<KeyStoreMetadata> metadataList = new ArrayList<>();
        try {
            if (!registry.resourceExists(RegistryResources.SecurityManagement.KEY_STORES)) {
                return metadataList;
            }

            Collection keyStoreCollection = (Collection) registry.get(RegistryResources.SecurityManagement.KEY_STORES);
            String[] keyStorePaths = keyStoreCollection.getChildren();

            for (String keyStorePath : keyStorePaths) {
                if (RegistryResources.SecurityManagement.PRIMARY_KEYSTORE_PHANTOM_RESOURCE.equals(keyStorePath)) {
                    continue;
                }
                metadataList.add(getKeyStoreMetadata(keyStorePath, registry, tenantId));
            }
            return metadataList;
        } catch (RegistryException e) {
            throw new SecurityException("Error when getting keyStore metadata.", e);
        }
    }

    private KeyStoreMetadata getKeyStoreMetadata(String keyStorePath, Registry registry, int tenantId)
            throws RegistryException {

        Resource keyStoreResource = registry.get(keyStorePath);
        int lastIndex = keyStorePath.lastIndexOf(REGISTRY_PATH_SEPARATOR);
        String name = keyStorePath.substring(lastIndex + 1);
        String type = keyStoreResource.getProperty(RegistryResources.SecurityManagement.PROP_TYPE);
        String provider = keyStoreResource.getProperty(RegistryResources.SecurityManagement.PROP_PROVIDER);
        String alias = keyStoreResource.getProperty(RegistryResources.SecurityManagement.PROP_PRIVATE_KEY_ALIAS);

        KeyStoreMetadata keyStoreMetadata = new KeyStoreMetadata();
        keyStoreMetadata.setKeyStoreName(name);
        keyStoreMetadata.setKeyStoreType(type);
        keyStoreMetadata.setProvider(provider);
        keyStoreMetadata.setPrivateStore(alias != null);

        if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            Association[] associations = registry.getAssociations(keyStorePath, ASSOCIATION_TENANT_KS_PUB_KEY);
            if (associations != null && associations.length > 0) {
                Resource pubKeyResource = registry.get(associations[0].getDestinationPath());
                keyStoreMetadata.setPublicCertId(pubKeyResource.getProperty(PROP_TENANT_PUB_KEY_FILE_NAME_APPENDER));
                keyStoreMetadata.setPublicCert((byte[]) pubKeyResource.getContent());
            }
        }

        return keyStoreMetadata;
    }

    /**
     * Update the key store in the registry.
     *
     * @param keyStoreName Key store name.
     * @param keyStore     Updated key store.
     * @param tenantId     Tenant Id.
     */
    public void updateKeyStore(String keyStoreName, KeyStore keyStore, int tenantId) {

        Registry registry = getGovernanceRegistry(tenantId);
        char[] passwordChar = new char[0];
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            String path = getKeyStorePath(keyStoreName);
            Resource resource = registry.get(path);
            passwordChar = getKeyStorePasswordFromRegistryResource(resource, keyStoreName);
            keyStore.store(outputStream, passwordChar);
            outputStream.flush();
            resource.setContent(outputStream.toByteArray());
            registry.put(path, resource);
            resource.discard();
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException |
                 RegistryException e) {
            throw new SecurityException("Error updating tenanted key store: " + keyStoreName, e);
        } finally {
            clearPasswordCharArray(passwordChar);
        }
    }

    /**
     * Delete the key store from the registry.
     *
     * @param keyStoreName Name of the key store.
     * @param tenantId     Tenant Id.
     * @throws SecurityException If an error occurs while deleting the key store.
     */
    public void deleteKeyStore(String keyStoreName, int tenantId) throws SecurityException {

        Registry registry = getGovernanceRegistry(tenantId);
        try {
            String path = getKeyStorePath(keyStoreName);
            Association[] allAssociations = registry.getAllAssociations(path);
            if (allAssociations != null && allAssociations.length > 0) {
                throw new SecurityException("Key store: " + keyStoreName + " is already in use and can't be deleted");
            }
            registry.delete(path);
        } catch (RegistryException e) {
            throw new SecurityException("Error deleting key store: " + keyStoreName, e);
        }
    }

    /**
     * Get the last modified date of the key store.
     *
     * @param keyStoreName Key store name.
     * @param tenantId     Tenant Id.
     * @return Last modified date of the key store.
     */
    public Date getKeyStoreLastModifiedDate(String keyStoreName, int tenantId) {

        Registry registry = getGovernanceRegistry(tenantId);
        try {
            return registry.get(getKeyStorePath(keyStoreName)).getLastModified();
        } catch (RegistryException e) {
            LOG.error("Error while getting the last modified date of the key store: " + keyStoreName, e);
            return null;
        }
    }

    /**
     * Get the password for the given key store name.
     *
     * @param keyStoreName Key store name.
     * @return KeyStore Password.
     * @throws SecurityException If there is an error while getting the key store password.
     */
    public char[] getKeyStorePassword(String keyStoreName, int tenantId) throws SecurityException {

        Registry registry = getGovernanceRegistry(tenantId);
        if (!isKeyStoreExistsInRegistry(keyStoreName, registry)) {
            throw new SecurityException("Key Store with a name: " + keyStoreName + " does not exist.");
        }

        try {
            Resource resource = registry.get(getKeyStorePath(keyStoreName));
            return getKeyStorePasswordFromRegistryResource(resource, keyStoreName);
        } catch (RegistryException e) {
            throw new SecurityException("Error getting password for key store: " + keyStoreName, e);
        }
    }

    /**
     * Get the private key password as a character array for the given key store name.
     *
     * @param keyStoreName Key store name.
     * @param tenantId     Tenant Id.
     * @return Private key password as a character array.
     * @throws SecurityException If an error occurs while getting the private key password.
     */
    public char[] getPrivateKeyPassword(String keyStoreName, int tenantId) throws SecurityException {

        Registry registry = getGovernanceRegistry(tenantId);
        try {
            Resource resource = registry.get(getKeyStorePath(keyStoreName));
            String encryptedPassword = resource.getProperty(RegistryResources.SecurityManagement.PROP_PRIVATE_KEY_PASS);
            if (encryptedPassword == null) {
                throw new SecurityException("Private Key Password of " + keyStoreName + " does not exist.");
            }
            return decryptPassword(encryptedPassword);
        } catch (RegistryException e) {
            throw new SecurityException("Error getting private key password for key store: " + keyStoreName, e);
        }
    }

    private boolean isKeyStoreExistsInRegistry(String keyStoreName, Registry registry) throws SecurityException {

        try {
            return registry.resourceExists(getKeyStorePath(keyStoreName));
        } catch (RegistryException e) {
            throw new SecurityException("Error checking the existence of key store: " + keyStoreName, e);
        }
    }

    private char[] getKeyStorePasswordFromRegistryResource(Resource resource, String keyStoreName) {

        String encryptedPassword = resource.getProperty(RegistryResources.SecurityManagement.PROP_PASSWORD);
        if (encryptedPassword == null) {
            throw new SecurityException("Key Store Password of " + keyStoreName + " does not exist.");
        }
        return decryptPassword(encryptedPassword);
    }

    private Registry getGovernanceRegistry(int tenantId) {

        try {
            return CarbonCoreDataHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);
        } catch (Exception e) {
            throw new SecurityException("Error while getting the registry for tenant: " + tenantId, e);
        }
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

    private static void clearPasswordCharArray(char[] passwordChar) {

        Arrays.fill(passwordChar, '\0');
    }
}
