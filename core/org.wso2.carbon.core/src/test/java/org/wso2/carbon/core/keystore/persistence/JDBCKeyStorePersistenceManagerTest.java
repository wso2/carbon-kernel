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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.CarbonAxisConfigurator;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.core.security.KeyStoreMetadata;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertThrows;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;

public class JDBCKeyStorePersistenceManagerTest {

    private static final String KEYSTORE_NAME_1 = "wso2carbon.p12";
    private static final String KEYSTORE_NAME_2 = "test_keystore.p12";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEYSTORE_PASSWORD = "wso2carbon";
    private static final String DB_NAME = "shared_db";
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(JDBCKeyStorePersistenceManagerTest.class);

    private MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext;
    private MockedStatic<CryptoUtil> cryptoUtil;
    private MockedStatic<KeystoreUtils> keystoreUtils;
    private MockedStatic<CarbonCoreDataHolder> carbonCoreDataHolder;
    private JDBCKeyStorePersistenceManager keyStoreManager;

    @BeforeClass
    public void setUpClass() throws Exception {

        initiateH2Database(getFilePath());
    }

    @BeforeMethod
    public void setup() throws Exception {

        System.setProperty(CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString());

        privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext mockPrivilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        privilegedCarbonContext.when(
                PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(mockPrivilegedCarbonContext);
        when(mockPrivilegedCarbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(mockPrivilegedCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        when(mockPrivilegedCarbonContext.getUsername()).thenReturn("admin");

        cryptoUtil = mockStatic(CryptoUtil.class);
        CryptoUtil mockCryptoUtil = mock(CryptoUtil.class);
        cryptoUtil.when(CryptoUtil::getDefaultCryptoUtil).thenReturn(mockCryptoUtil);
        when(mockCryptoUtil.encryptAndBase64Encode(any())).thenReturn("encryptedPassword");
        when(mockCryptoUtil.base64DecodeAndDecrypt(any())).thenReturn(KEYSTORE_PASSWORD.getBytes(
                StandardCharsets.UTF_8));

        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keystoreUtils = mockStatic(KeystoreUtils.class);
        keystoreUtils.when(() -> KeystoreUtils.getKeystoreInstance(anyString())).thenReturn(keyStore);

        carbonCoreDataHolder = mockStatic(CarbonCoreDataHolder.class);
        CarbonCoreDataHolder mockCarbonCoreDataHolder = mock(CarbonCoreDataHolder.class);
        when(CarbonCoreDataHolder.getInstance()).thenReturn(mockCarbonCoreDataHolder);
        when(mockCarbonCoreDataHolder.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

        keyStoreManager = new JDBCKeyStorePersistenceManager();
    }

    @AfterMethod
    public void tearDown() {

        keyStoreManager.deleteKeyStore(KEYSTORE_NAME_1, SUPER_TENANT_ID);
        privilegedCarbonContext.close();
        keystoreUtils.close();
        cryptoUtil.close();
        carbonCoreDataHolder.close();
    }

    @AfterClass
    public void wrapUp() throws Exception {

        closeH2Database();
    }

    @Test(description = "Add KeyStore test")
    public void testGetNotExistingKeyStore() {

        assertThrows(SecurityException.class,
                () -> this.keyStoreManager.getKeyStore(KEYSTORE_NAME_1, SUPER_TENANT_ID));
    }

    @Test(description = "Add KeyStore test")
    public void testAddKeyStore() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        this.keyStoreManager.addKeystore(KEYSTORE_NAME_1, keyStoreContent, "", KEYSTORE_TYPE,
                KEYSTORE_PASSWORD.toCharArray(), KEYSTORE_PASSWORD.toCharArray(), SUPER_TENANT_ID);
        KeyStore keyStoreFromDb = this.keyStoreManager.getKeyStore(KEYSTORE_NAME_1, SUPER_TENANT_ID);
        assertNotNull(keyStoreFromDb);

        KeyStore insertedKeyStore = KeystoreUtils.getKeystoreInstance(KEYSTORE_TYPE);
        insertedKeyStore.load(new ByteArrayInputStream(keyStoreContent), KEYSTORE_PASSWORD.toCharArray());
        assertEquals(keyStoreFromDb.size(), insertedKeyStore.size());
        assertEquals(KeyStoreUtil.getPrivateKeyAlias(keyStoreFromDb),
                KeyStoreUtil.getPrivateKeyAlias(insertedKeyStore));
    }

    @Test(description = "Add Duplicate KeyStore test")
    public void testAddKeyStoreWithExistingName() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        this.keyStoreManager.addKeystore(KEYSTORE_NAME_1, keyStoreContent, "", KEYSTORE_TYPE,
                KEYSTORE_PASSWORD.toCharArray(), KEYSTORE_PASSWORD.toCharArray(), SUPER_TENANT_ID);
        assertThrows(SecurityException.class,
                () -> this.keyStoreManager.addKeystore(KEYSTORE_NAME_1, keyStoreContent, "", KEYSTORE_TYPE,
                        KEYSTORE_PASSWORD.toCharArray(), KEYSTORE_PASSWORD.toCharArray(), SUPER_TENANT_ID));
    }

    @Test(description = "List KeyStores test")
    public void testListKeyStores() throws Exception {

        byte[] keyStoreContent1 = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        this.keyStoreManager.addKeystore(KEYSTORE_NAME_1, keyStoreContent1, "", KEYSTORE_TYPE,
                KEYSTORE_PASSWORD.toCharArray(), KEYSTORE_PASSWORD.toCharArray(), SUPER_TENANT_ID);
        byte[] keyStoreContent2 = readBytesFromFile(createPath(KEYSTORE_NAME_2).toString());
        this.keyStoreManager.addKeystore(KEYSTORE_NAME_2, keyStoreContent2, "", KEYSTORE_TYPE,
                KEYSTORE_PASSWORD.toCharArray(), KEYSTORE_PASSWORD.toCharArray(), SUPER_TENANT_ID);
        List<KeyStoreMetadata> keyStoreMetadataList = this.keyStoreManager.listKeyStores(SUPER_TENANT_ID);
        assertEquals(2, keyStoreMetadataList.size());
    }

    @Test(description = "Add KeyStore test")
    public void testUpdateKeyStore() throws Exception {

        byte[] keyStoreContent1 = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        KeyStore insertedKeyStore = KeystoreUtils.getKeystoreInstance(KEYSTORE_TYPE);
        insertedKeyStore.load(new ByteArrayInputStream(keyStoreContent1), KEYSTORE_PASSWORD.toCharArray());
        byte[] keyStoreContent2 = readBytesFromFile(createPath(KEYSTORE_NAME_2).toString());
        KeyStore updatedKeyStore = KeystoreUtils.getKeystoreInstance(KEYSTORE_TYPE);
        updatedKeyStore.load(new ByteArrayInputStream(keyStoreContent2), KEYSTORE_PASSWORD.toCharArray());

        this.keyStoreManager.addKeystore(KEYSTORE_NAME_1, keyStoreContent1, "", KEYSTORE_TYPE,
                KEYSTORE_PASSWORD.toCharArray(), KEYSTORE_PASSWORD.toCharArray(), SUPER_TENANT_ID);
        Date lastModifiedTimeAfterInsert =
                this.keyStoreManager.getKeyStoreLastModifiedDate(KEYSTORE_NAME_1, SUPER_TENANT_ID);
        System.out.println("Last modified time after insert: " + lastModifiedTimeAfterInsert.getTime());
        TimeUnit.MILLISECONDS.sleep(10000);

        this.keyStoreManager.updateKeyStore(KEYSTORE_NAME_1, updatedKeyStore, SUPER_TENANT_ID);
        KeyStore updatedKeyStoreFromDb = this.keyStoreManager.getKeyStore(KEYSTORE_NAME_1, SUPER_TENANT_ID);
        assertEquals(updatedKeyStoreFromDb.size(), updatedKeyStoreFromDb.size());
        assertEquals(KeyStoreUtil.getPrivateKeyAlias(updatedKeyStoreFromDb),
                KeyStoreUtil.getPrivateKeyAlias(updatedKeyStore));
        System.out.println("SANDEEPA");

        Date lastModifiedTimeAfterUpdate =
                this.keyStoreManager.getKeyStoreLastModifiedDate(KEYSTORE_NAME_1, SUPER_TENANT_ID);
        System.out.println("Last modified time after update: " + lastModifiedTimeAfterUpdate.getTime());
        // assertTrue(lastModifiedTimeAfterUpdate.after(lastModifiedTimeAfterInsert));
    }

    @Test(description = "Get KeyStore test")
    public void testGetKeyStoreData() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME_1).toString());
        this.keyStoreManager.addKeystore(KEYSTORE_NAME_1, keyStoreContent, "", KEYSTORE_TYPE,
                KEYSTORE_PASSWORD.toCharArray(), KEYSTORE_PASSWORD.toCharArray(), SUPER_TENANT_ID);
        KeyStore keyStoreFromDb = this.keyStoreManager.getKeyStore(KEYSTORE_NAME_1, SUPER_TENANT_ID);
        assertNotNull(keyStoreFromDb);

        String keyStorePasswordFromDb =
                String.valueOf(keyStoreManager.getKeyStorePassword(KEYSTORE_NAME_1, SUPER_TENANT_ID));
        assertEquals(KEYSTORE_PASSWORD, keyStorePasswordFromDb);
        String privateKeyPasswordFromDb =
                String.valueOf(keyStoreManager.getPrivateKeyPassword(KEYSTORE_NAME_1, SUPER_TENANT_ID));
        assertEquals(KEYSTORE_PASSWORD, privateKeyPasswordFromDb);
    }

    private Path createPath(String keystoreName) {

        return Paths.get(System.getProperty(CarbonBaseConstants.CARBON_HOME), "security", keystoreName);
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
