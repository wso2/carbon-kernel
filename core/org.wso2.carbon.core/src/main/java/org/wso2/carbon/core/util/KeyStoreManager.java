/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.core.util;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.core.keystore.persistence.KeyStorePersistenceManager;
import org.wso2.carbon.core.keystore.persistence.KeyStorePersistenceManagerFactory;
import org.wso2.carbon.core.security.KeyStoreMetadata;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The purpose of this class is to centrally manage the key stores.
 * Load key stores only once.
 * Reloading them over and over result a in a performance penalty.
 */
public class KeyStoreManager {

    private KeyStore primaryKeyStore = null;
    private KeyStore registryKeyStore = null;
    private KeyStore internalKeyStore = null;
    private KeyStore trustStore = null;
    private static ConcurrentHashMap<String, KeyStoreManager> mtKeyStoreManagers =
            new ConcurrentHashMap<String, KeyStoreManager>();
    private static Log log = LogFactory.getLog(KeyStoreManager.class);

    private final KeyStorePersistenceManager keyStorePersistenceManager;
    private ConcurrentHashMap<String, KeyStoreBean> tenantKeyStores = null;
    private ConcurrentHashMap<String, KeyStore> customKeyStores = null;
    private int tenantId = MultitenantConstants.SUPER_TENANT_ID;

    private ServerConfigurationService serverConfigService;

    private RegistryService registryService;

    private static final String KEY_STORE_NAME_NULL_ERROR = "Key store name is null or empty.";
    private static final String PERMISSION_DENIED_ERROR = "Permission denied for accessing %s. The %s is " +
            "available only for the super tenant.";

    /**
     * Private Constructor of the KeyStoreManager
     *
     * @param tenantId
     * @param serverConfigService
     * @param registryService
     */
    private KeyStoreManager(int tenantId, ServerConfigurationService serverConfigService,
                            RegistryService registryService) {

        this.serverConfigService = serverConfigService;
        this.registryService = registryService;
        this.keyStorePersistenceManager = KeyStorePersistenceManagerFactory.getKeyStorePersistenceManager();
        tenantKeyStores = new ConcurrentHashMap<String, KeyStoreBean>();
        customKeyStores = new ConcurrentHashMap<String, KeyStore>();
        this.tenantId = tenantId;
    }

    public ServerConfigurationService getServerConfigService() {
        return serverConfigService;
    }

    /**
     * Get the instance of the RegistryService
     *
     * @return RegistryService instance
     * @deprecated Use CryptoUtil#lookupRegistryService() instead.
     */
    @Deprecated
    public RegistryService getRegistryService() {
        return registryService;
    }

    /**
     * Get a KeyStoreManager instance for that tenant. This method will return an KeyStoreManager
     * instance if exists, or creates a new one. Only use this at runtime, or else,
     * use KeyStoreManager#getInstance(UserRegistry, ServerConfigurationService).
     *
     * @param tenantId id of the corresponding tenant
     * @return KeyStoreManager instance for that tenant
     */
    public static KeyStoreManager getInstance(int tenantId) {

        return initializeKeyStoreManager(tenantId, CarbonCoreDataHolder.getInstance().getServerConfigurationService(),
                CryptoUtil.lookupRegistryService());
    }

    /**
     * Get a KeyStoreManager instance for that tenant. This method will return an KeyStoreManager
     * instance if exists, or creates a new one. Only use this at runtime, or else,
     * use KeyStoreManager#getInstance(UserRegistry, ServerConfigurationService).
     *
     * @param tenantId            id of the corresponding tenant.
     * @param serverConfigService ServerConfigurationService instance.
     * @param registryService     RegistryService instance.
     * @return KeyStoreManager instance for that tenant.
     * @deprecated Use KeyStoreManager#getInstance(int) instead.
     */
    @Deprecated
    public static KeyStoreManager getInstance(int tenantId,
                                              ServerConfigurationService serverConfigService,
                                              RegistryService registryService) {

        return initializeKeyStoreManager(tenantId, serverConfigService, registryService);
    }

    private static KeyStoreManager initializeKeyStoreManager(int tenantId,
                                                             ServerConfigurationService serverConfigService,
                                                             RegistryService registryService) {

        log.debug("KeyStoreManager Instance created for tenant id: " + tenantId);
        CarbonUtils.checkSecurity();
        String tenantIdStr = Integer.toString(tenantId);
        if (!mtKeyStoreManagers.containsKey(tenantIdStr)) {
            mtKeyStoreManagers.put(tenantIdStr, new KeyStoreManager(tenantId,
                    serverConfigService, registryService));
        }
        return mtKeyStoreManagers.get(tenantIdStr);
    }

    /**
     * Add new key store.
     *
     * @param keystoreContent   Key store object.
     * @param filename          File name of the key store.
     * @param password          Password of the key store.
     * @param provider          Provider of the key store.
     * @param type              Keys store type (JKS, PKCS12, etc).
     * @param privateKeyPass    Password of the private key.
     * @throws SecurityException  If an error occurs while adding the key store.
     * @deprecated Use {@link #addKeyStore(byte[], String, char[], String, String, char[])} instead.
     */
    @Deprecated
    public void addKeyStore(byte[] keystoreContent, String filename, String password, String provider,
                            String type, String privateKeyPass) throws SecurityException {

        char[] passwordChar = new char[0];
        char[] privateKeyPasswordChar = new char[0];
        try {
            if (password == null) {
                throw new SecurityException("Key store password can't be null");
            } else if (privateKeyPass == null) {
                passwordChar = password.toCharArray();
                addKeyStore(keystoreContent, filename, passwordChar, provider, type, null);
            } else {
                passwordChar = password.toCharArray();
                privateKeyPasswordChar = privateKeyPass.toCharArray();
                addKeyStore(keystoreContent, filename, passwordChar, provider, type, privateKeyPasswordChar);
            }
        } finally {
            Arrays.fill(passwordChar, '\0');
            Arrays.fill(privateKeyPasswordChar, '\0');
        }
    }

    /**
     * Add new key store.
     *
     * @param keystoreContent        Key store object.
     * @param filename               File name of the key store.
     * @param passwordChar           Password of the key store as a character array.
     * @param provider               Provider of the key store.
     * @param type                   Keys store type (JKS, PKCS12, etc).
     * @param privateKeyPasswordChar Password of the private key as a character array.
     * @throws SecurityException If an error occurs while adding the key store.
     */
    public void addKeyStore(byte[] keystoreContent, String filename, char[] passwordChar, String provider, String type,
                            char[] privateKeyPasswordChar) throws SecurityException {

        if (StringUtils.isBlank(filename)) {
            throw new SecurityException(KEY_STORE_NAME_NULL_ERROR);
        }
        if (KeyStoreUtil.isPrimaryStore(filename) || KeyStoreUtil.isTrustStore(filename)) {
            throw new SecurityException("Key store " + filename + " already available");
        }
        keyStorePersistenceManager.addKeystore(filename, keystoreContent, provider, type, passwordChar,
                privateKeyPasswordChar, tenantId);
    }

    /**
     * Delete the given key store.
     *
     * @param keyStoreName  Name of the key store.
     * @throws SecurityException  If an error occurs while deleting the key store.
     */
    public void deleteStore(String keyStoreName) throws SecurityException {

        if (StringUtils.isBlank(keyStoreName)) {
            throw new SecurityException("Key Store name can't be null");
        }
        if (KeyStoreUtil.isPrimaryStore(keyStoreName)) {
            throw new SecurityException("Not allowed to delete the primary key store : " + keyStoreName);
        }
        if (KeyStoreUtil.isTrustStore(keyStoreName)) {
            throw new SecurityException("Not allowed to delete the trust store : " + keyStoreName);
        }
        keyStorePersistenceManager.deleteKeyStore(keyStoreName, tenantId);
        deleteKeyStoreFromCache(keyStoreName);
    }

    /**
     * Get the key store object for the given key store name.
     *
     * @param keyStoreName  Key store name.
     * @return KeyStore object.
     * @throws Exception    If there is an error when retrieving the given keystore.
     */
    public KeyStore getKeyStore(String keyStoreName) throws Exception {

        if (StringUtils.isEmpty(keyStoreName)) {
            throw new SecurityException(KEY_STORE_NAME_NULL_ERROR);
        }

        if (KeyStoreUtil.isPrimaryStore(keyStoreName)) {
            return getPrimaryKeyStore();
        }

        if (KeyStoreUtil.isCustomKeyStore(keyStoreName)) {
            return getCustomKeyStore(keyStoreName);
        }

        return getTenantKeyStore(keyStoreName);
    }

    /**
     * Method to retrieve keystore metadata of all the keystores in a tenant.
     *
     * @param isSuperTenant Indication whether the querying super tenant data.
     * @return KeyStoreMetaData[] Array of KeyStoreMetaData objects.
     * @throws SecurityException If an error occurs while retrieving the keystore data.
     */
    public KeyStoreMetadata[] getKeyStoresMetadata(boolean isSuperTenant) throws SecurityException {

        CarbonUtils.checkSecurity();
        List<KeyStoreMetadata> metadataList = keyStorePersistenceManager.listKeyStores(tenantId);
        if (isSuperTenant) {
            metadataList.add(getPrimaryKeyStoreMetadata());
        }
        return metadataList.toArray(new KeyStoreMetadata[0]);
    }

    private KeyStoreMetadata getPrimaryKeyStoreMetadata() {

        ServerConfiguration config = ServerConfiguration.getInstance();
        String fileName = config.getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE);
        String type = config.getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_TYPE);
        String name = KeyStoreUtil.getKeyStoreFileName(fileName);

        KeyStoreMetadata primaryKeyStoreMetadata = new KeyStoreMetadata();
        primaryKeyStoreMetadata.setKeyStoreName(name);
        primaryKeyStoreMetadata.setKeyStoreType(type);
        primaryKeyStoreMetadata.setProvider(" ");
        primaryKeyStoreMetadata.setPrivateStore(true);
        return primaryKeyStoreMetadata;
    }

    /**
     * This method loads the private key of a given key store.
     *
     * @param keyStoreName  Name of the key store.
     * @param alias         Alias of the private key.
     * @return Private key corresponding to the alias.
     * @throws Exception    If there is an error when retriving the private key from given keystore.
     */
    public Key getPrivateKey(String keyStoreName, String alias) {

        if (StringUtils.isEmpty(keyStoreName)) {
            throw new SecurityException(KEY_STORE_NAME_NULL_ERROR);
        }

        try {
            if (KeyStoreUtil.isPrimaryStore(keyStoreName)) {
                return getDefaultPrivateKey();
            }

            if (KeyStoreUtil.isCustomKeyStore(keyStoreName)) {
                return getCustomKeyStorePrivateKey(keyStoreName);
            }

            // Primary key store or custom key stores does not expect the key alias.
            if (StringUtils.isEmpty(alias)) {
                throw new SecurityException("Private key alias is null or empty.");
            }
            return getTenantPrivateKey(keyStoreName, alias);
        } catch (Exception e) {
            log.error("Error loading the private key from the key store : " + keyStoreName);
            throw new SecurityException("Error loading the private key from the key store : " +
                    keyStoreName, e);
        }
    }

    /**
     * This method loads the public certificate of a given key store.
     *
     * @param keyStoreName  Name of the key store.
     * @param alias         Alias of the certificate.
     * @return Public Certificate corresponding to the alias.
     * @throws Exception    If there is an error when retriving the public certificate from given keystore.
     */
    public Certificate getCertificate(String keyStoreName, String alias) throws Exception {

        if (StringUtils.isEmpty(keyStoreName)) {
            throw new SecurityException(KEY_STORE_NAME_NULL_ERROR);
        }

        if (KeyStoreUtil.isPrimaryStore(keyStoreName)) {
            return getDefaultPrimaryCertificate();
        }

        if (KeyStoreUtil.isCustomKeyStore(keyStoreName)) {
            return getCustomKeyStoreCertificate(keyStoreName);
        }

        // Primary or custom key store methods does not expect alias.
        if (StringUtils.isEmpty(alias)) {
            throw new SecurityException("Certificate alias is null or empty.");
        }
        return getTenantCertificate(keyStoreName, alias);
    }

    /**
     * Get the key store password of the given key store resource
     *
     * @param resource key store resource
     * @return password of the key store
     * @throws Exception Error when reading the registry resource of decrypting the password
     * @deprecated Use KeyStoreRegistryPersistenceManager#gePrivateKeyPassword(String) instead.
     */
    @Deprecated
    public String getPassword(org.wso2.carbon.registry.core.Resource resource) throws Exception {
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        String encryptedPassword = resource
                .getProperty(RegistryResources.SecurityManagement.PROP_PRIVATE_KEY_PASS);
        return new String(cryptoUtil.base64DecodeAndDecrypt(encryptedPassword));

    }

    /**
     * Get the key store password for the given key store name.
     * Note:  Caching has been not implemented for this method
     *
     * @param keyStoreName key store name
     * @return KeyStore object
     * @throws Exception If there is not a key store with the given name
     */
    public String getKeyStorePassword(String keyStoreName) throws Exception {

        return String.valueOf(keyStorePersistenceManager.getKeyStorePassword(keyStoreName, tenantId));
    }

    /**
     * Update the key store with the given name using the modified key store object provided.
     *
     * @param name     key store name
     * @param keyStore modified key store object
     * @throws Exception Registry exception or Security Exception
     */
    public void updateKeyStore(String name, KeyStore keyStore) throws Exception {
        ServerConfigurationService config = this.getServerConfigService();

        if (KeyStoreUtil.isPrimaryStore(name)) {
            updatePrimaryKeyStore(keyStore);
            return;
        }

        if (KeyStoreUtil.isTrustStore(name)) {
            updateTrustStore(keyStore);
            return;
        }

        updateTenantKeyStore(name, keyStore);
    }

    private void updatePrimaryKeyStore(KeyStore keyStore) {

        ServerConfigurationService config = this.getServerConfigService();
        String file = new File(config
                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE))
                .getAbsolutePath();
        try (FileOutputStream out = new FileOutputStream(file)) {
            String password = config
                    .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_PASSWORD);
            keyStore.store(out, password.toCharArray());
            primaryKeyStore = keyStore;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new SecurityException("Error updating primary key store", e);
        }
    }

    private void updateTrustStore(KeyStore keyStore)  {

        ServerConfigurationService config = this.getServerConfigService();
        String file = new File(config
                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_TRUSTSTORE_FILE))
                .getAbsolutePath();
        try (FileOutputStream out = new FileOutputStream(file)) {
            String password = config
                    .getFirstProperty(RegistryResources.SecurityManagement.SERVER_TRUSTSTORE_PASSWORD);
            keyStore.store(out, password.toCharArray());
            trustStore = keyStore;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new SecurityException("Error updating primary key store", e);
        }
    }

    private void updateTenantKeyStore(String keyStoreName, KeyStore keyStore) {

        keyStorePersistenceManager.updateKeyStore(keyStoreName, keyStore, tenantId);
        updateTenantKeyStoreCache(keyStoreName, new KeyStoreBean(keyStore, new Date()));
    }

    /**
     * Load the primary key store, this is allowed only for the super tenant
     *
     * @return primary key store object
     * @throws Exception Carbon Exception when trying to call this method from a tenant other
     *                   than tenant 0
     */
    public KeyStore getPrimaryKeyStore() throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Loading primary key store.");
        }
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            if (primaryKeyStore == null) {

                ServerConfigurationService config = this.getServerConfigService();
                String file =
                        new File(config
                                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE))
                                .getAbsolutePath();
                KeyStore store = KeystoreUtils.getKeystoreInstance(config
                                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_TYPE));
                String password = config
                        .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_PASSWORD);
                try (FileInputStream in = new FileInputStream(file)) {
                    store.load(in, password.toCharArray());
                    primaryKeyStore = store;
                }
            }
            return primaryKeyStore;
        } else {
            throw new CarbonException(String.format(PERMISSION_DENIED_ERROR, "primary key store", "primary key store"));
        }
    }

    /**
     * Load the requested tenant keystore.
     *
     * @param keyStoreName  Name of the Tenant KeyStore.
     * @return Key store object.
     * @throws Exception    Exception if failed to retrive the given keystore from registry.
     */
    private KeyStore getTenantKeyStore(String keyStoreName) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Loading tenant key store : " + keyStoreName);
        }
        if (isCachedTenantKeyStoreValid(keyStoreName)) {
            return tenantKeyStores.get(keyStoreName).getKeyStore();
        }

        KeyStore keyStore = keyStorePersistenceManager.getKeyStore(keyStoreName, tenantId);
        KeyStoreBean keyStoreBean = new KeyStoreBean(keyStore,
                keyStorePersistenceManager.getKeyStoreLastModifiedDate(keyStoreName, tenantId));
        updateTenantKeyStoreCache(keyStoreName, keyStoreBean);
        return keyStore;
    }

    /**
     * Load the internal key store, this is allowed only for the super tenant
     *
     * @return internal key store object
     * @throws Exception Carbon Exception when trying to call this method from a tenant other
     *                   than tenant 0
     */
    public KeyStore getInternalKeyStore() throws Exception {

        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            if (internalKeyStore == null) {
                ServerConfigurationService config = this.getServerConfigService();
                if (config.
                        getFirstProperty(RegistryResources.SecurityManagement.SERVER_INTERNAL_KEYSTORE_FILE) == null) {
                    return null;
                }
                String file = new File(config
                        .getFirstProperty(RegistryResources.SecurityManagement.SERVER_INTERNAL_KEYSTORE_FILE))
                        .getAbsolutePath();
                KeyStore store = KeystoreUtils.getKeystoreInstance(config
                        .getFirstProperty(RegistryResources.SecurityManagement.SERVER_INTERNAL_KEYSTORE_TYPE));
                String password = config
                        .getFirstProperty(RegistryResources.SecurityManagement.SERVER_INTERNAL_KEYSTORE_PASSWORD);
                try (FileInputStream in = new FileInputStream(file)) {
                    store.load(in, password.toCharArray());
                    internalKeyStore = store;
                }
            }
            return internalKeyStore;
        } else {
            throw new CarbonException(String.format(PERMISSION_DENIED_ERROR, "internal key store", "internal key store"));
        }
    }

    /**
     * Load the trust store, this is allowed only for the super tenant.
     *
     * @return Trust store object.
     * @throws SecurityException If an error occurs while loading the trust store.
     * @throws CarbonException Carbon Exception when trying to call this method from a tenant other than super tenant.
     */
    public KeyStore getTrustStore() throws CarbonException, SecurityException {

        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            if (trustStore == null) {
                try {
                    ServerConfigurationService config = this.getServerConfigService();
                    String file = new File(config
                            .getFirstProperty(RegistryResources.SecurityManagement.SERVER_TRUSTSTORE_FILE))
                            .getAbsolutePath();
                    KeyStore store = KeystoreUtils.getKeystoreInstance(config
                            .getFirstProperty(RegistryResources.SecurityManagement.SERVER_TRUSTSTORE_TYPE));
                    String password = config
                            .getFirstProperty(RegistryResources.SecurityManagement.SERVER_TRUSTSTORE_PASSWORD);
                    try (FileInputStream in = new FileInputStream(file)) {
                        store.load(in, password.toCharArray());
                        trustStore = store;
                    }
                } catch (KeyStoreException | NoSuchProviderException | IOException | CertificateException |
                    NoSuchAlgorithmException e) {
                    throw new SecurityException("Error loading trust store", e);
                }
            }
            return trustStore;
        } else {
            throw new CarbonException(String.format(PERMISSION_DENIED_ERROR, "trust store", "trust store"));
        }
    }
    
    /**
     * Load the register key store, this is allowed only for the super tenant
     *
     * @deprecated use {@link #getPrimaryKeyStore()} instead.
     *
     * @return register key store object
     * @throws Exception Carbon Exception when trying to call this method from a tenant other
     *                   than tenant 0
     */
    @Deprecated
    public KeyStore getRegistryKeyStore() throws Exception {
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            if (registryKeyStore == null) {

                ServerConfigurationService config = this.getServerConfigService();
                String file =
                        new File(config
                                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_REGISTRY_KEYSTORE_FILE))
                                .getAbsolutePath();
                KeyStore store = KeystoreUtils.getKeystoreInstance(config
                                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_REGISTRY_KEYSTORE_TYPE));
                String password = config
                        .getFirstProperty(RegistryResources.SecurityManagement.SERVER_REGISTRY_KEYSTORE_PASSWORD);
                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                    store.load(in, password.toCharArray());
                    registryKeyStore = store;
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }
            return registryKeyStore;
        } else {
            throw new CarbonException(String.format(PERMISSION_DENIED_ERROR, "registry key store", "registry key store"));
        }
    }

    /**
     * Load custom key stores configured in Carbon.xml file.
     * Custom key store files should reside in <IS-HOME>/repository/resources/security/
     *
     * @param keyStoreName  Name of the custom key store. Must start with the custom key store prefix.
     * @return Key store object
     * @throws Exception    Exception if failed to retrive the given key store.
     */
    private KeyStore getCustomKeyStore(String keyStoreName) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Loading custom key store : " + keyStoreName);
        }
        if (customKeyStores.containsKey(keyStoreName)) {
            return customKeyStores.get(keyStoreName);
        }

        OMElement config = KeyStoreUtil.getCustomKeyStoreConfigElement(keyStoreName, this.getServerConfigService());

        String location = KeyStoreUtil.getCustomKeyStoreConfig(
                config, RegistryResources.SecurityManagement.CustomKeyStore.PROP_LOCATION);
        String type = KeyStoreUtil.getCustomKeyStoreConfig(
                config, RegistryResources.SecurityManagement.CustomKeyStore.PROP_TYPE);
        String password = KeyStoreUtil.getCustomKeyStoreConfig(
                config, RegistryResources.SecurityManagement.CustomKeyStore.PROP_PASSWORD);

        KeyStore keyStore = loadKeyStoreFromFileSystem(location, password, type);
        customKeyStores.put(keyStoreName, keyStore);

        return keyStore;
    }

    /**
     * Get the default private key, only allowed for tenant 0
     *
     * @return Private key
     * @throws Exception Carbon Exception for tenants other than tenant 0
     */
    public PrivateKey getDefaultPrivateKey() throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Loading primary key store private key.");
        }
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            ServerConfigurationService config = this.getServerConfigService();
            String password = config
                    .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_PASSWORD);
            String alias = config
                    .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_KEY_ALIAS);
            return (PrivateKey) primaryKeyStore.getKey(alias, password.toCharArray());
        }
        throw new CarbonException(String.format(PERMISSION_DENIED_ERROR, "primary key store", "primary key store"));
    }

    /**
     * This method loads the private key of a given tenant key store.
     *
     * @param keyStoreName  Name of the tenant key store.
     * @param alias         Alias of the private key.
     * @return Private key corresponding to the alias.
     */
    private PrivateKey getTenantPrivateKey(String keyStoreName, String alias) throws Exception {


        if (log.isDebugEnabled()) {
            log.debug("Loading private key from tenant key store : " + keyStoreName + " alias: " + alias);
        }
        char[] privateKeyPasswordChar = new char[0];
        try {
            KeyStore keyStore = getTenantKeyStore(keyStoreName);
            privateKeyPasswordChar = keyStorePersistenceManager.getPrivateKeyPassword(keyStoreName, tenantId);
            return (PrivateKey) keyStore.getKey(alias, privateKeyPasswordChar);
        } finally {
            Arrays.fill(privateKeyPasswordChar, '\0');
        }
    }

    /**
     * Get the private key of a given custom keystore.
     *
     * @param keyStoreName  Name of the custom key store. Must include the prefix "CustomKeyStore/".
     * @return Private key from custom keystore corresponding to the alias.
     * @throws Exception    If failed to retrive the requested private key.
     */
    private PrivateKey getCustomKeyStorePrivateKey(String keyStoreName) throws Exception {

        OMElement config = KeyStoreUtil.getCustomKeyStoreConfigElement(keyStoreName, this.getServerConfigService());

        String password = KeyStoreUtil.getCustomKeyStoreConfig(
                config, RegistryResources.SecurityManagement.CustomKeyStore.PROP_PASSWORD);
        String alias = KeyStoreUtil.getCustomKeyStoreConfig(
                config, RegistryResources.SecurityManagement.CustomKeyStore.PROP_KEY_ALIAS);

        if (log.isDebugEnabled()) {
            log.debug("Loading private key from custom key store : " + keyStoreName + " alias: " + alias);
        }
        return (PrivateKey) getCustomKeyStore(keyStoreName).getKey(alias, password.toCharArray());
    }

    /**
     * Get default pub. key
     *
     * @return Public Key
     * @throws Exception Exception Carbon Exception for tenants other than tenant 0
     */
    public PublicKey getDefaultPublicKey() throws Exception {
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            ServerConfigurationService config = this.getServerConfigService();
            String alias = config
                    .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_KEY_ALIAS);
            return (PublicKey) primaryKeyStore.getCertificate(alias).getPublicKey();
        }
        throw new CarbonException(String.format(PERMISSION_DENIED_ERROR, "primary key store", "primary key store"));
    }

    /**
     * Get the private key password
     *
     * @return private key password
     * @throws CarbonException Exception Carbon Exception for tenants other than tenant 0
     */
    public String getPrimaryPrivateKeyPasssword() throws CarbonException {
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            ServerConfigurationService config = this.getServerConfigService();
            return config
                    .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_PASSWORD);
        }
        throw new CarbonException(String.format(PERMISSION_DENIED_ERROR, "primary key store", "primary key store"));
    }

    /**
     * This method is used to load the default public certificate of the primary key store
     *
     * @return Default public certificate
     * @throws Exception Permission denied for accessing primary key store
     */
    public X509Certificate getDefaultPrimaryCertificate() throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Loading primary key store public certificate.");
        }
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            ServerConfigurationService config = this.getServerConfigService();
            String alias = config
                    .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_KEY_ALIAS);
            return (X509Certificate) getPrimaryKeyStore().getCertificate(alias);
        }
        throw new CarbonException(String.format(PERMISSION_DENIED_ERROR, "primary key store", "primary key store"));
    }

    /**
     * This method is used to get public certificates of tenant keystores.
     *
     * @param keyStoreName      Name of the tenant key store.
     * @param alias             Public certificate alias.
     * @return Public certificate of a given tenant keystore.
     * @throws Exception        If failed to retrive the requested public certificate from the tenant key store.
     */
    private Certificate getTenantCertificate(String keyStoreName, String alias) throws Exception {

        return getTenantKeyStore(keyStoreName).getCertificate(alias);
    }

    /**
     * This method is used to get public certificates of custom keystores.
     *
     * @param keyStoreName      Custom key store name.
     * @return Public certificate of a given custom keystore
     * @throws Exception        If failed to retrive the requested public certificate from the custom key store.
     */
    private Certificate getCustomKeyStoreCertificate(String keyStoreName) throws Exception {

        OMElement config = KeyStoreUtil.getCustomKeyStoreConfigElement(keyStoreName, this.getServerConfigService());
        String alias = KeyStoreUtil.getCustomKeyStoreConfig(
                config, RegistryResources.SecurityManagement.CustomKeyStore.PROP_KEY_ALIAS);

        return getCustomKeyStore(keyStoreName).getCertificate(alias);
    }

    private boolean isCachedTenantKeyStoreValid(String keyStoreName) {

        if (tenantKeyStores.containsKey(keyStoreName)) {
            Date keyStoreLastModifiedDateFromStorage =
                    keyStorePersistenceManager.getKeyStoreLastModifiedDate(keyStoreName, tenantId);
            KeyStoreBean keyStoreBean = tenantKeyStores.get(keyStoreName);
            return keyStoreBean.getLastModifiedDate().equals(keyStoreLastModifiedDateFromStorage);
        }
        return false;
    }
    private void deleteKeyStoreFromCache(String keyStoreName) {

        tenantKeyStores.remove(keyStoreName);
    }

    private void updateTenantKeyStoreCache(String keyStoreName, KeyStoreBean keyStoreBean) {

        if (tenantKeyStores.containsKey(keyStoreName)) {
            tenantKeyStores.replace(keyStoreName, keyStoreBean);
        } else {
            tenantKeyStores.put(keyStoreName, keyStoreBean);
        }
    }

    public KeyStore loadKeyStoreFromFileSystem(String keyStorePath, String password, String type) {

        CarbonUtils.checkSecurity();
        String absolutePath = new File(keyStorePath).getAbsolutePath();
        FileInputStream inputStream = null;
        try {
            KeyStore store = KeystoreUtils.getKeystoreInstance(type);
            inputStream = new FileInputStream(absolutePath);
            store.load(inputStream, password.toCharArray());
            return store;
        } catch (Exception e) {
            String errorMsg = "Error loading the key store from the given location.";
            log.error(errorMsg);
            throw new SecurityException(errorMsg, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.warn("Error when closing the input stream.", e);
            }
        }
    }
}
