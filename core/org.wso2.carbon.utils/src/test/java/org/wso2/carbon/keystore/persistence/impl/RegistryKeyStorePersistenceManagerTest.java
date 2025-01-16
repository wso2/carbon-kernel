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

package org.wso2.carbon.keystore.persistence.impl;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.keystore.persistence.model.KeyStoreModel;
import org.wso2.carbon.registry.api.Association;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.registry.api.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.KEY_STORES;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.PROP_PASSWORD;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.PROP_PRIVATE_KEY_ALIAS;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.PROP_PRIVATE_KEY_PASS;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.PROP_PROVIDER;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.PROP_TYPE;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.RegistryResources.TENANT_PUBKEY_RESOURCE;

/**
 * Test class for Registry KeyStore Persistence Manager.
 */
public class RegistryKeyStorePersistenceManagerTest {

    private static final String KEYSTORE_NAME_1 = "wso2carbon.p12";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEYSTORE_PASSWORD = "wso2carbon";
    private static final String ENCRYPTED_KEYSTORE_PASSWORD = "encryptedPassword";
    private static final String PRIVATE_KEY_ALIAS = "wso2carbon";
    private static final String PUBLIC_CERT_ID = "00001";

    private RegistryKeyStorePersistenceManager keyStoreManager;
    private RegistryService registryService;
    private Registry registry;
    private MockedStatic<OSGiDataHolder> osgiDataHolderMock;

    @BeforeMethod
    public void setUp() throws Exception {

        keyStoreManager = new RegistryKeyStorePersistenceManager();
        registry = mock(Registry.class);
        registryService = mock(RegistryService.class);
        osgiDataHolderMock = mockStatic(OSGiDataHolder.class);
        OSGiDataHolder osgiDataHolder = mock(OSGiDataHolder.class);
        when(OSGiDataHolder.getInstance()).thenReturn(osgiDataHolder);
        when(osgiDataHolder.getRegistryService()).thenReturn(registryService);
        when(registryService.getGovernanceSystemRegistry(anyInt())).thenReturn(registry);
    }

    @AfterMethod
    public void tearDown() {

        osgiDataHolderMock.close();
    }

    @Test
    public void addKeystore() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel keyStoreModel = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent,
                null, null);

        when(registry.resourceExists(anyString())).thenReturn(false);
        when(registry.resourceExists(KEY_STORES)).thenReturn(true);
        Resource resource = mock(Resource.class);
        when(registry.newResource()).thenReturn(resource);

        keyStoreManager.addKeystore(keyStoreModel, 1);
    }

    @Test
    public void addTenantPrimaryKeystore() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel keyStoreModel = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent,
                PUBLIC_CERT_ID, null);

        when(registry.resourceExists(anyString())).thenReturn(false);
        Resource resource = mock(Resource.class);
        when(registry.newResource()).thenReturn(resource);
        keyStoreManager.addKeystore(keyStoreModel, 1);

        verify(registry).put(eq(KEY_STORES + "/" + KEYSTORE_NAME_1), eq(resource));
        verify(registry).put(eq(TENANT_PUBKEY_RESOURCE), eq(resource));
    }

    @Test(expectedExceptions = SecurityException.class)
    public void addKeystoreAlreadyExists() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel keyStoreModel = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent,
                null, null);
        when(registry.resourceExists(anyString())).thenReturn(true);
        keyStoreManager.addKeystore(keyStoreModel, 1);
    }

    @Test
    public void getKeyStore() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());

        Resource resource = mock(Resource.class);
        when(resource.getContent()).thenReturn(keyStoreContent);
        when(resource.getProperty(PROP_TYPE)).thenReturn(KEYSTORE_TYPE);
        when(resource.getProperty(PROP_PASSWORD)).thenReturn(ENCRYPTED_KEYSTORE_PASSWORD);

        when(registry.get(anyString())).thenReturn(resource);
        when(registry.resourceExists(anyString())).thenReturn(true);

        Optional<KeyStoreModel> keyStore = keyStoreManager.getKeyStore(KEYSTORE_NAME_1, 1);
        assertTrue(keyStore.isPresent());
        assertEquals(keyStore.get().getName(), KEYSTORE_NAME_1);
    }

    @Test(expectedExceptions = SecurityException.class)
    public void getKeyStoreNotExists() throws Exception {

        when(registry.resourceExists(anyString())).thenReturn(false);
        keyStoreManager.getKeyStore(KEYSTORE_NAME_1, 1);
    }

    @Test
    public void isKeyStoreExists() throws Exception {

        when(registry.resourceExists(anyString())).thenReturn(true);
        assertTrue(keyStoreManager.isKeyStoreExists(KEYSTORE_NAME_1, 1));
    }

    @Test
    public void listKeyStores() throws Exception {

        when(registry.resourceExists(anyString())).thenReturn(true);
        Collection collection = mock(Collection.class);
        when(registry.get(KEY_STORES)).thenReturn(collection);
        when(collection.getChildren()).thenReturn(new String[]{"/keystores/" + KEYSTORE_NAME_1});

        Resource resource = mock(Resource.class);
        when(registry.get("/keystores/" + KEYSTORE_NAME_1)).thenReturn(resource);
        when(resource.getProperty(PROP_TYPE)).thenReturn(KEYSTORE_TYPE);
        when(resource.getProperty(PROP_PROVIDER)).thenReturn("");
        when(resource.getProperty(PROP_PRIVATE_KEY_ALIAS)).thenReturn(PRIVATE_KEY_ALIAS);
        when(resource.getProperty(PROP_PASSWORD)).thenReturn(ENCRYPTED_KEYSTORE_PASSWORD);
        when(resource.getProperty(PROP_PRIVATE_KEY_PASS)).thenReturn(ENCRYPTED_KEYSTORE_PASSWORD);

        List<KeyStoreModel> keyStores = keyStoreManager.listKeyStores(1);

        assertEquals(keyStores.size(), 1);
        assertEquals(keyStores.get(0).getName(), KEYSTORE_NAME_1);
    }

    @Test
    public void updateKeyStore() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel keyStoreModel = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent,
                null, null);

        Resource resource = mock(Resource.class);
        when(registry.get(anyString())).thenReturn(resource);
        keyStoreManager.updateKeyStore(keyStoreModel, 1);
        verify(registry).put(eq(KEY_STORES + "/" + KEYSTORE_NAME_1), eq(resource));
    }

    @Test
    public void deleteKeyStore() throws Exception {

        when(registry.getAllAssociations(anyString())).thenReturn(new Association[0]);
        keyStoreManager.deleteKeyStore(KEYSTORE_NAME_1, 1);
        verify(registry).delete(KEY_STORES + "/" + KEYSTORE_NAME_1);
    }

    @Test(expectedExceptions = SecurityException.class)
    public void deleteKeyStoreInUse() throws Exception {

        Association association = mock(Association.class);
        when(registry.getAllAssociations(anyString())).thenReturn(new Association[]{association});
        keyStoreManager.deleteKeyStore("testKeystore", 1);
    }

    @Test
    public void getKeyStoreLastModifiedDate() throws Exception {

        Resource resource = mock(Resource.class);
        when(registry.get(anyString())).thenReturn(resource);
        Date date = new Date();
        when(resource.getLastModified()).thenReturn(date);
        Date lastModifiedDate = keyStoreManager.getKeyStoreLastModifiedDate("testKeystore", 1);
        assertEquals(lastModifiedDate, date);
    }

    @Test
    public void getEncryptedKeyStorePassword() throws Exception {

        when(registry.resourceExists(anyString())).thenReturn(true);
        Resource resource = mock(Resource.class);
        when(registry.get(anyString())).thenReturn(resource);
        when(resource.getProperty(PROP_PASSWORD)).thenReturn(KEYSTORE_PASSWORD);

        String password = keyStoreManager.getEncryptedKeyStorePassword(KEYSTORE_NAME_1, 1);
        assertEquals(password, KEYSTORE_PASSWORD);
    }

    @Test(expectedExceptions = SecurityException.class)
    public void getEncryptedKeyStorePasswordNotExists() throws Exception {

        when(registry.resourceExists(anyString())).thenReturn(false);
        keyStoreManager.getEncryptedKeyStorePassword(KEYSTORE_NAME_1, 1);
    }

    @Test
    public void getEncryptedPrivateKeyPassword() throws Exception {

        when(registry.resourceExists(anyString())).thenReturn(true);
        Resource resource = mock(Resource.class);
        when(registry.get(anyString())).thenReturn(resource);
        when(resource.getProperty(PROP_PRIVATE_KEY_PASS)).thenReturn(KEYSTORE_PASSWORD);

        String password = keyStoreManager.getEncryptedPrivateKeyPassword(KEYSTORE_NAME_1, 1);
        assertEquals(password, KEYSTORE_PASSWORD);
    }

    @Test(expectedExceptions = SecurityException.class)
    public void getEncryptedPrivateKeyPasswordNotExists() throws Exception {

        Resource resource = mock(Resource.class);
        when(registry.get(anyString())).thenReturn(resource);
        when(resource.getProperty(PROP_PRIVATE_KEY_PASS)).thenReturn(null);
        keyStoreManager.getEncryptedPrivateKeyPassword(KEYSTORE_NAME_1, 1);
    }

    private Path createPath(String keystoreName) {

        return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "security", keystoreName);
    }

    private byte[] readBytesFromFile(String filePath) throws IOException {

        File file = new File(filePath);
        byte[] bytes = new byte[(int) file.length()];

        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            int bytesRead = 0;
            while (bytesRead < bytes.length) {
                int read = inputStream.read(bytes, bytesRead, bytes.length - bytesRead);
                if (read == -1) break;
                bytesRead += read;
            }
        }
        return bytes;
    }
}
