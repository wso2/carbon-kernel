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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.keystore.persistence.model.KeyStoreModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

/**
 * This class tests the behavior of the Hybrid KeyStore Persistence Manager class.
 */
public class HybridKeyStorePersistenceManagerTest {

    private static final String KEYSTORE_NAME = "test_keystore_123.p12";
    private static final String KEYSTORE_NAME_1 = "wso2carbon.p12";
    private static final String KEYSTORE_NAME_2 = "test_keystore.p12";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEYSTORE_PASSWORD = "wso2carbon";
    private static final String ENCRYPTED_KEYSTORE_PASSWORD = "encryptedPassword";
    private static final String PRIVATE_KEY_ALIAS = "wso2carbon";

    private JDBCKeyStorePersistenceManager jdbcKeyStorePersistenceManager;
    private RegistryKeyStorePersistenceManager registryKeyStorePersistenceManager;
    HybridKeyStorePersistenceManager hybridKeyStorePersistenceManager;

    @BeforeMethod
    public void setUp() throws Exception {

        jdbcKeyStorePersistenceManager = mock(JDBCKeyStorePersistenceManager.class);
        registryKeyStorePersistenceManager = mock(RegistryKeyStorePersistenceManager.class);
        hybridKeyStorePersistenceManager = new HybridKeyStorePersistenceManager();
        Field field1 = HybridKeyStorePersistenceManager.class.getDeclaredField("jdbcKeyStorePersistenceManager");
        field1.setAccessible(true);
        field1.set(hybridKeyStorePersistenceManager, jdbcKeyStorePersistenceManager);
        Field field2 = HybridKeyStorePersistenceManager.class.getDeclaredField("registryKeyStorePersistenceManager");
        field2.setAccessible(true);
        field2.set(hybridKeyStorePersistenceManager, registryKeyStorePersistenceManager);
    }

    @Test
    public void addKeystore() throws Exception {

        KeyStoreModel keyStoreModel = new KeyStoreModel();
        hybridKeyStorePersistenceManager.addKeystore(keyStoreModel, 1);
        verify(jdbcKeyStorePersistenceManager).addKeystore(keyStoreModel, 1);
        verify(registryKeyStorePersistenceManager, never()).addKeystore(keyStoreModel, 1);
    }

    @Test
    public void getKeyStoreFromJDBC() throws Exception {

        KeyStoreModel keyStoreModel = new KeyStoreModel();
        when(jdbcKeyStorePersistenceManager.getKeyStore(KEYSTORE_NAME, 1)).thenReturn(Optional.of(keyStoreModel));
        Optional<KeyStoreModel> result = hybridKeyStorePersistenceManager.getKeyStore(KEYSTORE_NAME, 1);
        assertTrue(result.isPresent());
        assertEquals(result.get(), keyStoreModel);
        verify(jdbcKeyStorePersistenceManager).getKeyStore(KEYSTORE_NAME, 1);
        verify(registryKeyStorePersistenceManager, never()).getKeyStore(KEYSTORE_NAME, 1);
    }

    @Test
    public void getKeyStoreFromRegistry() throws Exception {

        KeyStoreModel keyStoreModel = new KeyStoreModel();
        when(jdbcKeyStorePersistenceManager.getKeyStore(KEYSTORE_NAME, 1)).thenReturn(Optional.empty());
        when(registryKeyStorePersistenceManager.getKeyStore(KEYSTORE_NAME, 1)).thenReturn(Optional.of(keyStoreModel));
        Optional<KeyStoreModel> result = hybridKeyStorePersistenceManager.getKeyStore(KEYSTORE_NAME, 1);
        assertTrue(result.isPresent());
        assertEquals(result.get(), keyStoreModel);
        verify(jdbcKeyStorePersistenceManager).getKeyStore(KEYSTORE_NAME, 1);
        verify(registryKeyStorePersistenceManager).getKeyStore(KEYSTORE_NAME, 1);
    }

    @Test
    public void isKeyStoreExistsInJDBC() throws Exception {

        when(jdbcKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1)).thenReturn(true);
        assertTrue(hybridKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1));
        verify(jdbcKeyStorePersistenceManager).isKeyStoreExists(KEYSTORE_NAME, 1);
        verify(registryKeyStorePersistenceManager, never()).isKeyStoreExists(KEYSTORE_NAME, 1);
    }

    @Test
    public void isKeyStoreExistsInRegistry() throws Exception {

        when(jdbcKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1)).thenReturn(false);
        when(registryKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1)).thenReturn(true);
        assertTrue(hybridKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1));
        verify(jdbcKeyStorePersistenceManager).isKeyStoreExists(KEYSTORE_NAME, 1);
        verify(registryKeyStorePersistenceManager).isKeyStoreExists(KEYSTORE_NAME, 1);
    }

    @Test
    public void listKeyStores() throws Exception {

        byte[] keyStoreContent1 = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel keyStoreModel1 = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent1,
                null, null);
        byte[] keyStoreContent2 = readBytesFromFile(createPath(KEYSTORE_NAME_2).toString());
        KeyStoreModel keyStoreModel2 = new KeyStoreModel(KEYSTORE_NAME_2, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent2,
                null, null);

        List<KeyStoreModel> list1 = new ArrayList<>();
        list1.add(keyStoreModel1);
        List<KeyStoreModel> list2 = new ArrayList<>();
        list2.add(keyStoreModel2);

        when(jdbcKeyStorePersistenceManager.listKeyStores(1)).thenReturn(list1);
        when(registryKeyStorePersistenceManager.listKeyStores(1)).thenReturn(list2);
        List<KeyStoreModel> result = hybridKeyStorePersistenceManager.listKeyStores(1);
        assertEquals(result.size(), 2);
        verify(jdbcKeyStorePersistenceManager).listKeyStores(1);
        verify(registryKeyStorePersistenceManager).listKeyStores(1);
    }

    @Test
    public void updateKeyStoreInJDBC() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel keyStoreModel = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent,
                null, null);
        when(jdbcKeyStorePersistenceManager.isKeyStoreExists(keyStoreModel.getName(), 1)).thenReturn(true);
        hybridKeyStorePersistenceManager.updateKeyStore(keyStoreModel, 1);
        verify(jdbcKeyStorePersistenceManager).updateKeyStore(keyStoreModel, 1);
        verify(registryKeyStorePersistenceManager, never()).updateKeyStore(keyStoreModel, 1);
    }

    @Test
    public void updateKeyStoreInRegistry() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel keyStoreModel = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent,
                null, null);
        when(jdbcKeyStorePersistenceManager.isKeyStoreExists(keyStoreModel.getName(), 1)).thenReturn(false);
        when(registryKeyStorePersistenceManager.isKeyStoreExists(keyStoreModel.getName(), 1)).thenReturn(false);
        hybridKeyStorePersistenceManager.updateKeyStore(keyStoreModel, 1);
        verify(registryKeyStorePersistenceManager).updateKeyStore(keyStoreModel, 1);
        verify(jdbcKeyStorePersistenceManager, never()).updateKeyStore(keyStoreModel, 1);
    }

    @Test
    public void deleteKeyStoreInJDBC() throws Exception {

        when(jdbcKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1)).thenReturn(true);
        hybridKeyStorePersistenceManager.deleteKeyStore(KEYSTORE_NAME, 1);
        verify(jdbcKeyStorePersistenceManager).deleteKeyStore(KEYSTORE_NAME, 1);
        verify(registryKeyStorePersistenceManager, never()).deleteKeyStore(KEYSTORE_NAME, 1);
    }

    @Test
    public void deleteKeyStoreInRegistry() throws Exception {

        when(jdbcKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1)).thenReturn(false);
        when(registryKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1)).thenReturn(true);
        hybridKeyStorePersistenceManager.deleteKeyStore(KEYSTORE_NAME, 1);
        verify(registryKeyStorePersistenceManager).deleteKeyStore(KEYSTORE_NAME, 1);
        verify(jdbcKeyStorePersistenceManager, never()).deleteKeyStore(KEYSTORE_NAME, 1);
    }

    @Test
    public void getKeyStoreLastModifiedDateFromJDBC() {

        Date date = new Date();
        when(jdbcKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1)).thenReturn(true);
        when(jdbcKeyStorePersistenceManager.getKeyStoreLastModifiedDate(KEYSTORE_NAME, 1)).thenReturn(date);
        Date result = hybridKeyStorePersistenceManager.getKeyStoreLastModifiedDate(KEYSTORE_NAME, 1);
        assertEquals(result, date);
        verify(jdbcKeyStorePersistenceManager).getKeyStoreLastModifiedDate(KEYSTORE_NAME, 1);
        verify(registryKeyStorePersistenceManager, never()).getKeyStoreLastModifiedDate(KEYSTORE_NAME, 1);
    }

    @Test
    public void getKeyStoreLastModifiedDateFromRegistry() {

        Date date = new Date();
        when(jdbcKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1)).thenReturn(false);
        when(registryKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1)).thenReturn(true);
        when(registryKeyStorePersistenceManager.getKeyStoreLastModifiedDate(KEYSTORE_NAME, 1)).thenReturn(date);
        Date result = hybridKeyStorePersistenceManager.getKeyStoreLastModifiedDate(KEYSTORE_NAME, 1);
        assertEquals(result, date);
        verify(jdbcKeyStorePersistenceManager, never()).getKeyStoreLastModifiedDate(KEYSTORE_NAME, 1);
        verify(registryKeyStorePersistenceManager).getKeyStoreLastModifiedDate(KEYSTORE_NAME, 1);
    }

    @Test
    public void getEncryptedKeyStorePasswordFromJDBC() throws Exception {

        when(jdbcKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1)).thenReturn(true);
        when(jdbcKeyStorePersistenceManager.getEncryptedKeyStorePassword(KEYSTORE_NAME, 1)).thenReturn(
                KEYSTORE_PASSWORD);
        String keyStorePassword = hybridKeyStorePersistenceManager.getEncryptedKeyStorePassword(KEYSTORE_NAME, 1);
        assertEquals(keyStorePassword, KEYSTORE_PASSWORD);
        verify(jdbcKeyStorePersistenceManager).getEncryptedKeyStorePassword(KEYSTORE_NAME, 1);
        verify(registryKeyStorePersistenceManager, never()).getEncryptedKeyStorePassword(KEYSTORE_NAME, 1);
    }

    @Test
    public void getEncryptedKeyStorePasswordFromRegistry() throws Exception {

        when(jdbcKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1)).thenReturn(false);
        when(registryKeyStorePersistenceManager.getEncryptedKeyStorePassword(KEYSTORE_NAME, 1)).thenReturn(
                KEYSTORE_PASSWORD);
        when(registryKeyStorePersistenceManager.getEncryptedKeyStorePassword(KEYSTORE_NAME, 1)).thenReturn(
                KEYSTORE_PASSWORD);
        String keyStorePassword = hybridKeyStorePersistenceManager.getEncryptedKeyStorePassword(KEYSTORE_NAME, 1);
        assertEquals(keyStorePassword, KEYSTORE_PASSWORD);
        verify(jdbcKeyStorePersistenceManager, never()).getEncryptedKeyStorePassword(KEYSTORE_NAME, 1);
        verify(registryKeyStorePersistenceManager).getEncryptedKeyStorePassword(KEYSTORE_NAME, 1);
    }

    @Test
    public void getEncryptedPrivateKeyPasswordFromJDBC() throws Exception {

        when(jdbcKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1)).thenReturn(true);
        when(jdbcKeyStorePersistenceManager.getEncryptedPrivateKeyPassword(KEYSTORE_NAME, 1)).thenReturn(
                KEYSTORE_PASSWORD);
        String keyStorePassword = hybridKeyStorePersistenceManager.getEncryptedPrivateKeyPassword(KEYSTORE_NAME, 1);
        assertEquals(keyStorePassword, KEYSTORE_PASSWORD);
        verify(jdbcKeyStorePersistenceManager).getEncryptedPrivateKeyPassword(KEYSTORE_NAME, 1);
        verify(registryKeyStorePersistenceManager, never()).getEncryptedPrivateKeyPassword(KEYSTORE_NAME, 1);
    }

    @Test
    public void getEncryptedPrivateKeyPasswordFromRegistry() throws Exception {

        when(jdbcKeyStorePersistenceManager.isKeyStoreExists(KEYSTORE_NAME, 1)).thenReturn(false);
        when(registryKeyStorePersistenceManager.getEncryptedPrivateKeyPassword(KEYSTORE_NAME, 1)).thenReturn(
                KEYSTORE_PASSWORD);
        String keyStorePassword = hybridKeyStorePersistenceManager.getEncryptedPrivateKeyPassword(KEYSTORE_NAME, 1);
        assertEquals(keyStorePassword, KEYSTORE_PASSWORD);
        verify(jdbcKeyStorePersistenceManager, never()).getEncryptedPrivateKeyPassword(KEYSTORE_NAME, 1);
        verify(registryKeyStorePersistenceManager).getEncryptedPrivateKeyPassword(KEYSTORE_NAME, 1);
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
