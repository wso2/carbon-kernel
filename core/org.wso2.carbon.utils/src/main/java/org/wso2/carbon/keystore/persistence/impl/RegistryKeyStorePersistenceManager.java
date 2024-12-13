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

package org.wso2.carbon.keystore.persistence.impl;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.keystore.persistence.KeyStorePersistenceManager;
import org.wso2.carbon.keystore.persistence.model.KeyStoreModel;
import org.wso2.carbon.registry.api.Association;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.KEY_STORES;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.PRIMARY_KEYSTORE_PHANTOM_RESOURCE;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.PROP_PASSWORD;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.PROP_PRIVATE_KEY_ALIAS;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.PROP_PRIVATE_KEY_PASS;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.PROP_PROVIDER;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.PROP_TYPE;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.TENANT_PUBKEY_RESOURCE;

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
     * @param keyStoreModel KeyStore model object.
     * @throws SecurityException If an error occurs while adding the key store.
     */
    public void addKeystore(KeyStoreModel keyStoreModel) throws SecurityException {

        Registry registry = getGovernanceRegistry(keyStoreModel.getTenantId());
        if (isKeyStoreExistsInRegistry(keyStoreModel.getName(), registry)) {
            throw new SecurityException("Key store " + keyStoreModel.getName() + " already available");
        }

        boolean isTenantPrimaryKeyStore = false;
        try {
            if (keyStoreModel.getTenantId() != MultitenantConstants.SUPER_TENANT_ID &&
                    !registry.resourceExists(KEY_STORES)) {
                isTenantPrimaryKeyStore = true;
            }

            Resource resource = registry.newResource();
            resource.addProperty(PROP_PASSWORD, keyStoreModel.getEncryptedPassword());
            resource.addProperty(PROP_PROVIDER, keyStoreModel.getProvider());
            resource.addProperty(PROP_TYPE, keyStoreModel.getType());
            resource.setContent(keyStoreModel.getContent());

            if (StringUtils.isNotBlank(keyStoreModel.getPrivateKeyAlias())) {
                resource.addProperty(PROP_PRIVATE_KEY_ALIAS, keyStoreModel.getPrivateKeyAlias());
            }
            if (StringUtils.isNotBlank(keyStoreModel.getEncryptedPrivateKeyPass())) {
                resource.addProperty(PROP_PRIVATE_KEY_PASS, keyStoreModel.getEncryptedPrivateKeyPass());
            }
            registry.put(getKeyStorePath(keyStoreModel.getName()), resource);

            if (isTenantPrimaryKeyStore) {
                // Create the public key resource for tenant's primary keystore.
                addTenantPublicKey(keyStoreModel.getName(), keyStoreModel.getPublicCert(), registry);
            }
        } catch (RegistryException e) {
            throw new SecurityException("Error adding key store " + keyStoreModel.getName(), e);
        }
    }

    /**
     * Add tenant's public certificate to the database.
     *
     * @param keyStoreName Name of the key store.
     * @param publicCert   Public certificate of the tenant.
     * @throws SecurityException If an error occurs while adding the tenant's public certificate.
     */
    private void addTenantPublicKey(String keyStoreName, byte[] publicCert, Registry registry)
            throws SecurityException {

        try {
            // Create the public key resource.
            Resource pubKeyResource = registry.newResource();
            pubKeyResource.setContent(publicCert);
            pubKeyResource.addProperty(PROP_TENANT_PUB_KEY_FILE_NAME_APPENDER, generatePublicCertId());
            registry.put(TENANT_PUBKEY_RESOURCE, pubKeyResource);

            // Associate the public key with the keystore.
            registry.addAssociation(KEY_STORES + REGISTRY_PATH_SEPARATOR + keyStoreName,
                    TENANT_PUBKEY_RESOURCE, ASSOCIATION_TENANT_KS_PUB_KEY);
        } catch (RegistryException e) {
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
    public KeyStoreModel getKeyStore(String keyStoreName, int tenantId) throws SecurityException {

        Registry registry = getGovernanceRegistry(tenantId);
        if (!isKeyStoreExistsInRegistry(keyStoreName, registry)) {
            throw new SecurityException("Key Store with a name: " + keyStoreName + " does not exist.");
        }

        try {
            Resource resource = registry.get(getKeyStorePath(keyStoreName));
            KeyStoreModel keyStoreModel = new KeyStoreModel();
            keyStoreModel.setName(keyStoreName);
            keyStoreModel.setType(resource.getProperty(PROP_TYPE));
            keyStoreModel.setContent((byte[]) resource.getContent());
            keyStoreModel.setEncryptedPassword(resource.getProperty(PROP_PASSWORD));
            resource.discard();
            return keyStoreModel;
        } catch (RegistryException e) {
            throw new SecurityException("Error getting key store: " + keyStoreName, e);
        }
    }

    private String getKeyStorePath(String keyStoreName) {

        return KEY_STORES + REGISTRY_PATH_SEPARATOR + keyStoreName;
    }

    /**
     * Method to retrieve list of keystore metadata of all the keystores in a tenant.
     *
     * @param tenantId Tenant Id.
     * @return List of KeyStoreMetaData objects.
     * @throws SecurityException If an error occurs while retrieving the keystore data.
     */
    public List<KeyStoreModel> listKeyStores(int tenantId) throws SecurityException {

        Registry registry = getGovernanceRegistry(tenantId);
        List<KeyStoreModel> keyStoreList = new ArrayList<>();
        try {
            if (!registry.resourceExists(KEY_STORES)) {
                return keyStoreList;
            }

            Collection keyStoreCollection = (Collection) registry.get(KEY_STORES);
            String[] keyStorePaths = keyStoreCollection.getChildren();

            for (String keyStorePath : keyStorePaths) {
                if (PRIMARY_KEYSTORE_PHANTOM_RESOURCE.equals(keyStorePath)) {
                    continue;
                }
                keyStoreList.add(getKeyStoreData(keyStorePath, registry, tenantId));
            }
            return keyStoreList;
        } catch (RegistryException e) {
            throw new SecurityException("Error when getting keyStore metadata.", e);
        }
    }

    private KeyStoreModel getKeyStoreData(String keyStorePath, Registry registry, int tenantId)
            throws RegistryException {

        Resource keyStoreResource = registry.get(keyStorePath);
        int lastIndex = keyStorePath.lastIndexOf(REGISTRY_PATH_SEPARATOR);
        String name = keyStorePath.substring(lastIndex + 1);
        String type = keyStoreResource.getProperty(PROP_TYPE);
        String provider = keyStoreResource.getProperty(PROP_PROVIDER);
        String alias = keyStoreResource.getProperty(PROP_PRIVATE_KEY_ALIAS);

        KeyStoreModel keyStoreModel = new KeyStoreModel();
        keyStoreModel.setName(name);
        keyStoreModel.setType(type);
        keyStoreModel.setProvider(provider);
        keyStoreModel.setPrivateKeyAlias(alias);

        if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            Association[] associations = registry.getAssociations(keyStorePath, ASSOCIATION_TENANT_KS_PUB_KEY);
            if (associations != null && associations.length > 0) {
                Resource pubKeyResource = registry.get(associations[0].getDestinationPath());
                keyStoreModel.setPublicCertId(pubKeyResource.getProperty(PROP_TENANT_PUB_KEY_FILE_NAME_APPENDER));
                keyStoreModel.setPublicCert((byte[]) pubKeyResource.getContent());
            }
        }

        return keyStoreModel;
    }

    /**
     * Update the key store in the registry.
     *
     * @param keyStoreModel Key store model.
     */
    public void updateKeyStore(KeyStoreModel keyStoreModel) {

        Registry registry = getGovernanceRegistry(keyStoreModel.getTenantId());
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            String path = getKeyStorePath(keyStoreModel.getName());
            Resource resource = registry.get(path);
            resource.setContent(keyStoreModel.getContent());
            registry.put(path, resource);
            resource.discard();
        } catch (IOException | RegistryException e) {
            throw new SecurityException("Error updating tenanted key store: " + keyStoreModel.getName(), e);
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
     * Get the encrypted password for the given key store name.
     *
     * @param keyStoreName Key store name.
     * @return KeyStore Password.
     * @throws SecurityException If there is an error while getting the key store password.
     */
    public String getEncryptedKeyStorePassword(String keyStoreName, int tenantId) throws SecurityException {

        Registry registry = getGovernanceRegistry(tenantId);
        if (!isKeyStoreExistsInRegistry(keyStoreName, registry)) {
            throw new SecurityException("Key Store with a name: " + keyStoreName + " does not exist.");
        }

        try {
            Resource resource = registry.get(getKeyStorePath(keyStoreName));
            return resource.getProperty(PROP_PASSWORD);
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
    public String getEncryptedPrivateKeyPassword(String keyStoreName, int tenantId) throws SecurityException {

        Registry registry = getGovernanceRegistry(tenantId);
        try {
            Resource resource = registry.get(getKeyStorePath(keyStoreName));
            String encryptedPassword = resource.getProperty(PROP_PRIVATE_KEY_PASS);
            if (encryptedPassword == null) {
                throw new SecurityException("Private Key Password of " + keyStoreName + " does not exist.");
            }
            return encryptedPassword;
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

    private Registry getGovernanceRegistry(int tenantId) {

        try {
            return OSGiDataHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);
        } catch (Exception e) {
            throw new SecurityException("Error while getting the registry for tenant: " + tenantId, e);
        }
    }
}
