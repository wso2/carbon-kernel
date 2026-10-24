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
package org.wso2.carbon.core.util;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * KeyStoreManager test class.
 */
public class KeyStoreManagerTest {

    private static final String KEYSTORE_NAME = "wso2carbon.p12";
    private static final String TRUSTSTORE_NAME = "client-truststore.p12";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEYSTORE_PASSWORD = "wso2carbon";

    @Mock
    ServerConfiguration serverConfiguration;
    @Mock
    private RegistryService registryService;
    @Mock
    private UserRegistry registry;
    @Mock
    private Resource resource;
    @Mock
    private CryptoUtil cryptoUtil;

    MockedStatic<CarbonUtils> carbonUtils;
    private KeyStoreManager keyStoreManager;
    private KeyStore keyStore;

    @BeforeClass
    public void setup() throws KeyStoreException, RegistryException {

        initMocks(this);
        System.setProperty(CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString());
        carbonUtils = mockStatic(CarbonUtils.class);
        carbonUtils.when(CarbonUtils::getServerConfiguration).thenReturn(this.serverConfiguration);
        when(this.serverConfiguration.getFirstProperty("KeyStoreDataPersistenceManager.DataStorageType")).
                thenReturn("registry");

        OSGiDataHolder.getInstance().setRegistryService(this.registryService);
        when(this.registryService.getGovernanceSystemRegistry(anyInt())).thenReturn(this.registry);
        keyStoreManager = KeyStoreManager.getInstance(
                MultitenantConstants.SUPER_TENANT_ID, serverConfiguration, registryService);
        keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
    }

    @Test(description = "Get primary key store test")
    public void testGetPrimaryKeyStore() throws Exception {

        try (MockedStatic<ServerConfiguration> serverConfiguration = mockStatic(ServerConfiguration.class);
             MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeystoreUtils> keystoreUtils = mockStatic(KeystoreUtils.class)) {

            keystoreUtils.when(() -> KeystoreUtils.getKeystoreInstance(anyString())).thenReturn(this.keyStore);
            keystoreUtils.when(() -> KeystoreUtils.getKeystoreInstance(anyString(), any())).thenReturn(this.keyStore);

            serverConfiguration.when(ServerConfiguration::getInstance).thenReturn(this.serverConfiguration);
            when(this.serverConfiguration.getFirstProperty(
                    RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE)).thenReturn(
                            createPath(KEYSTORE_NAME).toString());
            when(this.serverConfiguration.getFirstProperty(
                    RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_PASSWORD)).thenReturn(KEYSTORE_PASSWORD);
            when(this.serverConfiguration.getFirstProperty(
                    RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_TYPE)).thenReturn(KEYSTORE_TYPE);

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);

            KeyStore primaryKeystore = this.keyStoreManager.getPrimaryKeyStore();
            assertSame(primaryKeystore, this.keyStore);
        }
    }

    @Test(description = "Get trust store test")
    public void testGetTrustStore() throws Exception {

        try (MockedStatic<ServerConfiguration> serverConfiguration = mockStatic(ServerConfiguration.class);
             MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeystoreUtils> keystoreUtils = mockStatic(KeystoreUtils.class)) {

            keystoreUtils.when(() -> KeystoreUtils.getKeystoreInstance(anyString())).thenReturn(this.keyStore);

            serverConfiguration.when(ServerConfiguration::getInstance).thenReturn(this.serverConfiguration);
            when(this.serverConfiguration.getFirstProperty(
                    RegistryResources.SecurityManagement.SERVER_TRUSTSTORE_FILE)).thenReturn(createPath(TRUSTSTORE_NAME).toString());
            when(this.serverConfiguration.getFirstProperty(
                    RegistryResources.SecurityManagement.SERVER_TRUSTSTORE_PASSWORD)).thenReturn(KEYSTORE_PASSWORD);
            when(this.serverConfiguration.getFirstProperty(
                    RegistryResources.SecurityManagement.SERVER_TRUSTSTORE_TYPE)).thenReturn(KEYSTORE_TYPE);

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);

            KeyStore trustStore = this.keyStoreManager.getTrustStore();
            assertSame(trustStore, this.keyStore);
        }
    }

    @Test(description = "Add KeyStore test")
    public void testAddKeyStore() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME).toString());

        try (MockedStatic<CryptoUtil>cryptoUtil = mockStatic(CryptoUtil.class);
             MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeystoreUtils> keystoreUtils = mockStatic(KeystoreUtils.class);
             MockedStatic<KeyStoreUtil> keyStoreUtil = mockStatic(KeyStoreUtil.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);

            keyStoreUtil.when(() -> KeyStoreUtil.isPrimaryStore(any())).thenReturn(false);
            keyStoreUtil.when(() -> KeyStoreUtil.isTrustStore(any())).thenReturn(false);
            keyStoreUtil.when(() -> KeyStoreUtil.getPrivateKeyAlias(any())).thenReturn("wso2carbon");

            keystoreUtils.when(() -> KeystoreUtils.getKeystoreInstance(anyString())).thenReturn(this.keyStore);

            when(registry.newResource()).thenReturn(resource);
            when(registry.resourceExists(anyString())).thenReturn(false);

            cryptoUtil.when(CryptoUtil::getDefaultCryptoUtil).thenReturn(this.cryptoUtil);
            when(this.cryptoUtil.encryptAndBase64Encode(any())).thenReturn("encryptedPassword");

            this.keyStoreManager.addKeyStore(keyStoreContent, "new_keystore.jks",
                    KEYSTORE_PASSWORD.toCharArray(), " ", KEYSTORE_TYPE, KEYSTORE_PASSWORD.toCharArray());
        }
    }

    @Test(description = "A PKCS11 key store can't be added with the generic addKeyStore")
    public void testAddPKCS11KeyStoreRejected() throws Exception {

        try (MockedStatic<KeyStoreUtil> keyStoreUtil = mockStatic(KeyStoreUtil.class)) {
            keyStoreUtil.when(() -> KeyStoreUtil.isPrimaryStore(any())).thenReturn(false);
            keyStoreUtil.when(() -> KeyStoreUtil.isTrustStore(any())).thenReturn(false);
            when(registry.resourceExists(anyString())).thenReturn(false);

            // The caller can't choose the token entry, provider or PIN of a PKCS11 key store.
            SecurityException e = expectThrows(SecurityException.class, () -> this.keyStoreManager.addKeyStore(
                    "label=wso2-tenant-1\n".getBytes(), "some-store.p11", "1234".toCharArray(),
                    "SunPKCS11-is-tenants", "PKCS11", "1234".toCharArray()));
            assertTrue(e.getMessage().contains("PKCS11 key stores can't be added with addKeyStore"));
        }
    }

    @Test(description = "The super tenant has no PKCS11 tenant key store")
    public void testAddTenantPKCS11KeyStoreRejectedForSuperTenant() {

        SecurityException e = expectThrows(SecurityException.class,
                () -> this.keyStoreManager.addTenantPKCS11KeyStore());
        assertTrue(e.getMessage().contains("super tenant"));
    }

    @Test(description = "A PKCS11 key store is loaded from its token through the given provider, without a file")
    public void testLoadPKCS11KeyStoreWithoutFile() throws Exception {

        String providerName = "KeyStoreManagerTestToken";
        char[] tokenPassword = "tokenpin".toCharArray();
        KeyStore tokenContents;
        try (java.io.InputStream in = new java.io.FileInputStream(createPath("pkcs11-token-test.p12").toFile())) {
            tokenContents = InMemoryTokenProvider.loadPKCS12(in, tokenPassword);
        }
        InMemoryTokenProvider provider = new InMemoryTokenProvider(providerName, tokenContents, tokenPassword,
                "1234".toCharArray());
        java.security.Security.addProvider(provider);
        try {
            // A wrong PIN fails the token login.
            expectThrows(SecurityException.class, () -> this.keyStoreManager.loadKeyStoreFromFileSystem(
                    "/no/such/file.p11", "0000", "PKCS11", providerName));

            KeyStore keyStore = this.keyStoreManager.loadKeyStoreFromFileSystem("/no/such/file.p11", "1234",
                    "PKCS11", providerName);
            assertSame(keyStore.getProvider(), provider);
            assertTrue(keyStore.isKeyEntry("wso2-tenant-1"));

            // Like SunPKCS11, the PIN is not checked again once the token is logged in.
            assertTrue(provider.isLoggedIn());
            assertTrue(this.keyStoreManager.loadKeyStoreFromFileSystem("/no/such/file.p11", "0000", "PKCS11",
                    providerName).isKeyEntry("wso2-tenant-1"));
        } finally {
            java.security.Security.removeProvider(providerName);
        }
    }

    @Test(description = "Delete KeyStore test")
    public void testDeleteKeyStore() throws Exception {

        try (MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeyStoreUtil> keyStoreUtil = mockStatic(KeyStoreUtil.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);

            keyStoreUtil.when(() -> KeyStoreUtil.isPrimaryStore(anyString())).thenReturn(false);
            keyStoreUtil.when(() -> KeyStoreUtil.isTrustStore(anyString())).thenReturn(false);

            when(registry.getAllAssociations(anyString())).thenReturn(new Association[]{});
            when(registry.resourceExists(anyString())).thenReturn(true);

            this.keyStoreManager.deleteStore("new_keystore.jks");
        }
    }

    @Test(description = "A PKCS11 key store record is deleted even when its token entry can't be deleted")
    public void testDeletePKCS11KeyStoreDeletesRecordFirst() throws Exception {

        Resource pkcs11Resource = org.mockito.Mockito.mock(Resource.class);
        when(pkcs11Resource.getProperty(anyString())).thenReturn("PKCS11");
        when(pkcs11Resource.getContent()).thenReturn(TenantPKCS11KeyStore.createDescriptor(
                TenantPKCS11KeyStore.labelFor(MultitenantConstants.SUPER_TENANT_ID)));
        try (MockedStatic<KeyStoreUtil> keyStoreUtil = mockStatic(KeyStoreUtil.class);
             MockedStatic<ServerConfiguration> serverConfiguration = mockStatic(ServerConfiguration.class)) {

            keyStoreUtil.when(() -> KeyStoreUtil.isPrimaryStore(anyString())).thenReturn(false);
            keyStoreUtil.when(() -> KeyStoreUtil.isTrustStore(anyString())).thenReturn(false);
            serverConfiguration.when(ServerConfiguration::getInstance).thenReturn(this.serverConfiguration);
            // The token can't be reached: its provider is not registered.
            when(this.serverConfiguration.getFirstProperty(
                    RegistryResources.SecurityManagement.TENANT_KEYSTORE_PROVIDER)).thenReturn("NoSuchToken");
            when(this.serverConfiguration.getFirstProperty(
                    RegistryResources.SecurityManagement.TENANT_KEYSTORE_PASSWORD)).thenReturn("1234");
            when(registry.resourceExists(anyString())).thenReturn(true);
            when(registry.get(org.mockito.ArgumentMatchers.contains("hsm-tenant.p11"))).thenReturn(pkcs11Resource);
            when(registry.getAllAssociations(anyString())).thenReturn(new Association[]{});

            this.keyStoreManager.deleteStore("hsm-tenant.p11");
            org.mockito.Mockito.verify(registry).delete(org.mockito.ArgumentMatchers.contains("hsm-tenant.p11"));
        }
    }

    @Test(description = "Delete TrustStore test")
    public void testDeleteTrustStore() throws Exception {

        try (MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeyStoreUtil> keyStoreUtil = mockStatic(KeyStoreUtil.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);

            keyStoreUtil.when(() -> KeyStoreUtil.isPrimaryStore(anyString())).thenReturn(false);
            keyStoreUtil.when(() -> KeyStoreUtil.isTrustStore(anyString())).thenReturn(false);

            when(registry.getAllAssociations(anyString())).thenReturn(new Association[]{});
            when(registry.resourceExists(anyString())).thenReturn(true);

            this.keyStoreManager.deleteStore("new_truststore.jks");
        }
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

}
