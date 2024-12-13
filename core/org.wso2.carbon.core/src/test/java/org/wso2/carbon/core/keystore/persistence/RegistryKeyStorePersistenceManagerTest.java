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
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDataAccessManager;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
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
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;

public class RegistryKeyStorePersistenceManagerTest {

    private static final String KEYSTORE_NAME = "wso2carbon.p12";
    private static final String TRUSTSTORE_NAME = "client-truststore.p12";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEYSTORE_PASSWORD = "wso2carbon";
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final String DB_NAME = "shared_db";

    private static final String REG_DB_JNDI_NAME = "jdbc/WSO2RegDB";
    private static final String REG_DB_SQL_FILE = "dbscripts/h2.sql";

    private RealmService testSessionRealmService;
    private RegistryService registryService;

    private MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext;
    private MockedStatic<CryptoUtil> cryptoUtil;
    private MockedStatic<KeystoreUtils> keystoreUtils;
    private KeyStore keyStore;
    private RegistryKeyStorePersistenceManager keyStoreManager;
    private MockedStatic<CarbonCoreDataHolder> carbonCoreDataHolder;

    @BeforeClass
    public void setUpClass() throws Exception {

        // initiateH2Database(getFilePath());
        //createRegistryService(SUPER_TENANT_ID, SUPER_TENANT_DOMAIN_NAME);
    }

    @BeforeMethod
    public void setup() throws Exception {

        System.setProperty(CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString());
        keyStore = KeyStore.getInstance(KEYSTORE_TYPE);

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

        keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keystoreUtils = mockStatic(KeystoreUtils.class);
        keystoreUtils.when(() -> KeystoreUtils.getKeystoreInstance(anyString())).thenReturn(this.keyStore);

        carbonCoreDataHolder = mockStatic(CarbonCoreDataHolder.class);
        CarbonCoreDataHolder mockCarbonCoreDataHolder = mock(CarbonCoreDataHolder.class);
        when(CarbonCoreDataHolder.getInstance()).thenReturn(mockCarbonCoreDataHolder);
        when(mockCarbonCoreDataHolder.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

        keyStoreManager = new RegistryKeyStorePersistenceManager();
    }

    @AfterMethod
    public void tearDown() {

        // privilegedCarbonContext.close();
        cryptoUtil.close();
    }

    @AfterClass
    public void wrapUp() throws Exception {

        closeH2Database();
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
