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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.keystore.persistence.model.KeyStoreModel;
import org.wso2.carbon.utils.internal.CarbonUtilsDataHolder;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.AssertJUnit.assertTrue;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;

public class JDBCKeyStorePersistenceManagerTest {

    private static final String KEYSTORE_NAME_1 = "wso2carbon.p12";
    private static final String KEYSTORE_NAME_2 = "test_keystore.p12";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEYSTORE_PASSWORD = "wso2carbon";
    private static final String ENCRYPTED_KEYSTORE_PASSWORD = "encryptedPassword";
    private static final String PRIVATE_KEY_ALIAS = "wso2carbon";
    private static final String PUBLIC_CERT_ID = "00001";
    private static final String DB_NAME = "shared_db";
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();

    private MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext;
    private MockedStatic<KeystoreUtils> keystoreUtils;
    private MockedStatic<CarbonUtilsDataHolder> carbonUtilsDataHolder;
    private JDBCKeyStorePersistenceManager keyStoreManager;

    @BeforeClass
    public void setUpClass() throws Exception {

        initiateH2Database(getFilePath());
    }

    @BeforeMethod
    public void setup() throws Exception {

        privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext mockPrivilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        privilegedCarbonContext.when(
                PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(mockPrivilegedCarbonContext);
        when(mockPrivilegedCarbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(mockPrivilegedCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        when(mockPrivilegedCarbonContext.getUsername()).thenReturn("admin");

        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keystoreUtils = mockStatic(KeystoreUtils.class);
        keystoreUtils.when(() -> KeystoreUtils.getKeystoreInstance(anyString())).thenReturn(keyStore);

        carbonUtilsDataHolder = mockStatic(CarbonUtilsDataHolder.class);
        CarbonUtilsDataHolder mockCarbonCoreDataHolder = mock(CarbonUtilsDataHolder.class);
        when(CarbonUtilsDataHolder.getInstance()).thenReturn(mockCarbonCoreDataHolder);
        when(mockCarbonCoreDataHolder.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

        keyStoreManager = new JDBCKeyStorePersistenceManager();
    }

    @AfterMethod
    public void tearDown() {

        keyStoreManager.deleteKeyStore(KEYSTORE_NAME_1, SUPER_TENANT_ID);
        privilegedCarbonContext.close();
        keystoreUtils.close();
        carbonUtilsDataHolder.close();
    }

    @AfterClass
    public void wrapUp() throws Exception {

        closeH2Database();
        CarbonBaseUtils.getCarbonHome();
    }

    @Test(description = "Get non existing keystore test")
    public void testGetNotExistingKeyStore() {

        Optional<KeyStoreModel> keyStoreResult = keyStoreManager.getKeyStore(KEYSTORE_NAME_1, SUPER_TENANT_ID);
        assertEquals(keyStoreResult, Optional.empty());
    }

    @Test(description = "Add KeyStore test")
    public void testAddKeyStore() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel insertedKeyStoreModel = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent,
                PUBLIC_CERT_ID, null);
        this.keyStoreManager.addKeystore(insertedKeyStoreModel, SUPER_TENANT_ID);

        Optional<KeyStoreModel> keyStoreResult = keyStoreManager.getKeyStore(KEYSTORE_NAME_1, SUPER_TENANT_ID);
        assertTrue(keyStoreResult.isPresent());
        KeyStoreModel keyStoreModelFromDb = keyStoreResult.get();
        assertEquals(keyStoreModelFromDb.getName(), insertedKeyStoreModel.getName());
        assertEquals(keyStoreModelFromDb.getType(), insertedKeyStoreModel.getType());
        assertEquals(keyStoreModelFromDb.getPrivateKeyAlias(), insertedKeyStoreModel.getPrivateKeyAlias());
        assertEquals(keyStoreModelFromDb.getPublicCertId(), insertedKeyStoreModel.getPublicCertId());
        assertEquals(keyStoreModelFromDb.getContent(), insertedKeyStoreModel.getContent());
        assertEquals(keyStoreModelFromDb.getEncryptedPrivateKeyPass(),
                insertedKeyStoreModel.getEncryptedPrivateKeyPass());
        assertEquals(keyStoreModelFromDb.getEncryptedPassword(), insertedKeyStoreModel.getEncryptedPassword());

        KeyStore insertedKeyStore = KeystoreUtils.getKeystoreInstance(KEYSTORE_TYPE);
        insertedKeyStore.load(new ByteArrayInputStream(keyStoreContent), KEYSTORE_PASSWORD.toCharArray());
        KeyStore keyStoreFromDb = KeystoreUtils.getKeystoreInstance(KEYSTORE_TYPE);
        keyStoreFromDb.load(new ByteArrayInputStream(keyStoreModelFromDb.getContent()),
                KEYSTORE_PASSWORD.toCharArray());

        assertEquals(keyStoreFromDb.size(), insertedKeyStore.size());
        assertEquals(getPrivateKeyAlias(keyStoreFromDb), getPrivateKeyAlias(insertedKeyStore));
    }

    @Test(description = "Add Duplicate KeyStore test")
    public void testAddKeyStoreWithExistingName() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel insertedKeySToreModel = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent,
                PUBLIC_CERT_ID, null);
        this.keyStoreManager.addKeystore(insertedKeySToreModel, SUPER_TENANT_ID);
        assertThrows(SecurityException.class,
                () -> this.keyStoreManager.addKeystore(insertedKeySToreModel, SUPER_TENANT_ID));
    }

    @Test(description = "Test KeyStore existence")
    public void testIsKeyStoreExists() throws Exception {

        assertFalse(this.keyStoreManager.isKeyStoreExists(KEYSTORE_NAME_1, SUPER_TENANT_ID));

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel insertedKeyStoreModel = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent,
                PUBLIC_CERT_ID, null);
        this.keyStoreManager.addKeystore(insertedKeyStoreModel, SUPER_TENANT_ID);

        assertTrue(this.keyStoreManager.isKeyStoreExists(KEYSTORE_NAME_1, SUPER_TENANT_ID));
    }

    @Test(description = "List KeyStores test")
    public void testListKeyStores() throws Exception {

        byte[] keyStoreContent1 = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel insertedKeySToreModel1 = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent1,
                PUBLIC_CERT_ID, null);
        this.keyStoreManager.addKeystore(insertedKeySToreModel1, SUPER_TENANT_ID);

        byte[] keyStoreContent2 = readBytesFromFile(createPath(KEYSTORE_NAME_2).toString());
        KeyStoreModel insertedKeySToreModel2 = new KeyStoreModel(KEYSTORE_NAME_2, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent2,
                null, null);
        this.keyStoreManager.addKeystore(insertedKeySToreModel2, SUPER_TENANT_ID);

        List<KeyStoreModel> keyStoreMetadataList = this.keyStoreManager.listKeyStores(SUPER_TENANT_ID);
        assertEquals(keyStoreMetadataList.size(), 2);
    }

    @Test(description = "Update KeyStore test")
    public void testUpdateKeyStore() throws Exception {

        byte[] keyStoreContent1 = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel insertedKeyStoreModel = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent1,
                null, null);

        byte[] keyStoreContent2 = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel updatedKeyStoreModel = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent2,
                null, null);

        // Add KeyStore.
        this.keyStoreManager.addKeystore(insertedKeyStoreModel, SUPER_TENANT_ID);
        Date lastModifiedTimeAfterInsert =
                this.keyStoreManager.getKeyStoreLastModifiedDate(KEYSTORE_NAME_1, SUPER_TENANT_ID);

        // Update the previously added KeyStore.
        this.keyStoreManager.updateKeyStore(updatedKeyStoreModel, SUPER_TENANT_ID);
        Optional<KeyStoreModel> updatedKeyStoreModelFromDb =
                this.keyStoreManager.getKeyStore(KEYSTORE_NAME_1, SUPER_TENANT_ID);
        assertEquals(updatedKeyStoreModelFromDb.get().getContent(), updatedKeyStoreModel.getContent());

        Date lastModifiedTimeAfterUpdate =
                this.keyStoreManager.getKeyStoreLastModifiedDate(KEYSTORE_NAME_1, SUPER_TENANT_ID);
        assertTrue(lastModifiedTimeAfterUpdate.after(lastModifiedTimeAfterInsert));
    }

    @Test(description = "Get KeyStore test")
    public void testGetKeyStoreData() throws Exception {

        byte[] keyStoreContent1 = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel insertedKeyStoreModel = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent1,
                null, null);
        keyStoreManager.addKeystore(insertedKeyStoreModel, SUPER_TENANT_ID);
        Optional<KeyStoreModel> keyStoreResult = keyStoreManager.getKeyStore(KEYSTORE_NAME_1, SUPER_TENANT_ID);
        assertTrue(keyStoreResult.isPresent());

        String encryptedKeyStorePasswordFromDb =
                String.valueOf(keyStoreManager.getEncryptedKeyStorePassword(KEYSTORE_NAME_1, SUPER_TENANT_ID));
        assertEquals(encryptedKeyStorePasswordFromDb, ENCRYPTED_KEYSTORE_PASSWORD);
        String encryptedPrivateKeyPasswordFromDb =
                String.valueOf(keyStoreManager.getEncryptedPrivateKeyPassword(KEYSTORE_NAME_1, SUPER_TENANT_ID));
        assertEquals(encryptedPrivateKeyPasswordFromDb, ENCRYPTED_KEYSTORE_PASSWORD);
    }

    @Test(description = "Delete KeyStore test")
    public void testDeleteKeyStore() throws Exception {

        byte[] keyStoreContent1 = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStoreModel insertedKeyStoreModel = new KeyStoreModel(KEYSTORE_NAME_1, KEYSTORE_TYPE, "",
                ENCRYPTED_KEYSTORE_PASSWORD, PRIVATE_KEY_ALIAS, ENCRYPTED_KEYSTORE_PASSWORD, keyStoreContent1,
                null, null);
        keyStoreManager.addKeystore(insertedKeyStoreModel, SUPER_TENANT_ID);
        assertTrue(keyStoreManager.isKeyStoreExists(KEYSTORE_NAME_1, SUPER_TENANT_ID));

        keyStoreManager.deleteKeyStore(KEYSTORE_NAME_1, SUPER_TENANT_ID);
        assertFalse(keyStoreManager.isKeyStoreExists(KEYSTORE_NAME_1, SUPER_TENANT_ID));
    }

    public static String getPrivateKeyAlias(KeyStore store) throws KeyStoreException {

        String alias = null;
        Enumeration<String> enums = store.aliases();
        while (enums.hasMoreElements()) {
            String name = enums.nextElement();
            if (store.isKeyEntry(name)) {
                alias = name;
                break;
            }
        }
        return alias;
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

    private void initiateH2Database(String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + DB_NAME);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("select 1");
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(DB_NAME, dataSource);
    }

    private static String getFilePath() {

        if (StringUtils.isNotBlank("h2.sql")) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", "h2.sql")
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    private static void closeH2Database() throws SQLException {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
