/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.config;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.user.core.internal.UserStoreMgtDSComponent;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import javax.crypto.Cipher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for loading the primary key store private key and decrypting secondary user store properties with it, for
 * file based key stores and for keys held in a PKCS11 token.
 */
public class UserStoreConfigXMLProcessorKeyStoreTest {

    private static final String TOKEN_PROVIDER_NAME = "UserCoreTestToken";
    private static final char[] TOKEN_PASSWORD = "tokenpin".toCharArray();
    private static final String PIN = "1234";
    private static final String TOKEN_ALIAS = "wso2-tenant-1";
    private static final String CIPHER_TRANSFORMATION_PROPERTY = "org.wso2.CipherTransformation";
    private static final String PKCS1_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String OPAQUE_PROVIDER_NAME = "OpaqueRSATest";

    private ServerConfigurationService originalConfigurationService;
    private PrivateKey originalPrivateKey;

    @BeforeClass
    public void setUpCarbonHome() {

        // KeystoreUtils reads carbon.xml when it is loaded.
        File carbonHome = new File("../../distribution/kernel/carbon-home");
        if (StringUtils.isBlank(System.getProperty("carbon.home")) && carbonHome.exists()) {
            System.setProperty("carbon.home", carbonHome.getAbsolutePath());
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {

        originalConfigurationService = UserStoreMgtDSComponent.getServerConfigurationService();
        originalPrivateKey = (PrivateKey) staticField("privateKey").get(null);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        setServerConfigurationService(originalConfigurationService);
        staticField("privateKey").set(null, originalPrivateKey);
        Security.removeProvider(TOKEN_PROVIDER_NAME);
        Security.removeProvider(OPAQUE_PROVIDER_NAME);
        System.clearProperty(CIPHER_TRANSFORMATION_PROPERTY);
    }

    @Test
    public void testLoadPrivateKeyFromPKCS11TokenWithoutFile() throws Exception {

        InMemoryTokenProvider tokenProvider;
        try (InputStream in = new FileInputStream(Paths.get(System.getProperty("basedir", "."), "src", "test",
                "resources", "security", "pkcs11-token-test.p12").toFile())) {
            tokenProvider = new InMemoryTokenProvider(TOKEN_PROVIDER_NAME,
                    InMemoryTokenProvider.loadPKCS12(in, TOKEN_PASSWORD), TOKEN_PASSWORD, PIN.toCharArray());
        }
        Security.addProvider(tokenProvider);
        ServerConfigurationService config = mock(ServerConfigurationService.class);
        when(config.getFirstProperty("Security.KeyStore.Type")).thenReturn("PKCS11");
        when(config.getFirstProperty("Security.KeyStore.Provider")).thenReturn(TOKEN_PROVIDER_NAME);
        when(config.getFirstProperty("Security.KeyStore.Location")).thenReturn("no-such-file.p11");
        when(config.getFirstProperty("Security.KeyStore.Password")).thenReturn(PIN);
        when(config.getFirstProperty("Security.KeyStore.KeyPassword")).thenReturn(PIN);
        when(config.getFirstProperty("Security.KeyStore.KeyAlias")).thenReturn(TOKEN_ALIAS);
        setServerConfigurationService(config);

        PrivateKey key = invokeGetPrivateKey();

        assertNotNull(key, "The private key should be loaded from the token without a key store file.");
        assertEquals(key, tokenProvider.getToken().getKey(TOKEN_ALIAS, TOKEN_PASSWORD));
    }

    @Test
    public void testDecryptWithOpaqueKeyUsesKeyStoreProvider() throws Exception {

        assertDecryptsWithOpaqueKey(PKCS1_TRANSFORMATION);
    }

    @Test
    public void testDecryptWithOpaqueKeyUsingOAEP() throws Exception {

        // Legacy ciphertexts may use an OAEP transformation. No provider is named for an HSM key, so a provider
        // that offers RSA-OAEP for the key is selected.
        assertDecryptsWithOpaqueKey(OpaqueRSAKeyTestProvider.RSA_OAEP_CIPHER);
    }

    private void assertDecryptsWithOpaqueKey(String transformation) throws Exception {

        OpaqueRSAKeyTestProvider hsmProvider = new OpaqueRSAKeyTestProvider(2048);
        Security.addProvider(hsmProvider);
        staticField("privateKey").set(null, hsmProvider.getPrivateKey());
        System.setProperty(CIPHER_TRANSFORMATION_PROPERTY, transformation);
        Cipher encrypter = Cipher.getInstance(transformation, "SunJCE");
        encrypter.init(Cipher.ENCRYPT_MODE, hsmProvider.getPublicKey());
        String cipherText = Base64.getEncoder().encodeToString(
                encrypter.doFinal("userstore-secret".getBytes(StandardCharsets.UTF_8)));

        String plainText = invokeDecryptWithPrimaryKeyStore(cipherText);

        assertEquals(plainText, "userstore-secret");
    }

    @Test
    public void testSoftwareKeyKeepsConfiguredJCEProvider() throws Exception {

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        assertTrue(keyPair.getPrivate() instanceof RSAPrivateKey);
        staticField("privateKey").set(null, keyPair.getPrivate());
        Security.addProvider(new OpaqueRSAKeyTestProvider(2048));

        Method method = UserStoreConfigXMLProcessor.class.getDeclaredMethod("getPrimaryKeyCipher", String.class);
        method.setAccessible(true);
        Cipher cipher = (Cipher) method.invoke(new UserStoreConfigXMLProcessor(null), PKCS1_TRANSFORMATION);

        assertEquals(cipher.getProvider().getName(), BouncyCastleProvider.PROVIDER_NAME);
    }

    private static PrivateKey invokeGetPrivateKey() throws Exception {

        Method method = UserStoreConfigXMLProcessor.class.getDeclaredMethod("getPrivateKey");
        method.setAccessible(true);
        return (PrivateKey) method.invoke(null);
    }

    private static String invokeDecryptWithPrimaryKeyStore(String cipherText) throws Exception {

        Method method = UserStoreConfigXMLProcessor.class.getDeclaredMethod("decryptWithPrimaryKeyStore",
                String.class);
        method.setAccessible(true);
        return (String) method.invoke(new UserStoreConfigXMLProcessor(null), cipherText);
    }

    private static Field staticField(String name) throws NoSuchFieldException {

        Field field = UserStoreConfigXMLProcessor.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static void setServerConfigurationService(ServerConfigurationService service) throws Exception {

        Field field = UserStoreMgtDSComponent.class.getDeclaredField("serverConfigurationService");
        field.setAccessible(true);
        field.set(null, service);
    }
}
