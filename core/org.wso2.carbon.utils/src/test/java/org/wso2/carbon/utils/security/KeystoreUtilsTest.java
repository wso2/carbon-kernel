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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.utils.security;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Tests for the tenant key algorithm and key size configuration and the PKCS11 store type helpers in
 * {@link KeystoreUtils}.
 */
public class KeystoreUtilsTest {

    private static final String KEY_ALGORITHM_CONFIG = "Security.TenantKeyStore.KeyAlgorithm";
    private static final String KEY_SIZE_CONFIG = "Security.TenantKeyStore.KeySize";

    private MockedStatic<CarbonUtils> carbonUtils;
    private ServerConfiguration serverConfiguration;

    @BeforeMethod
    public void setUp() {

        serverConfiguration = mock(ServerConfiguration.class);
        carbonUtils = mockStatic(CarbonUtils.class);
        carbonUtils.when(CarbonUtils::getServerConfiguration).thenReturn(serverConfiguration);
    }

    @AfterMethod
    public void tearDown() {

        carbonUtils.close();
    }

    @DataProvider(name = "unsetValues")
    public Object[][] unsetValues() {

        return new Object[][]{{null}, {""}, {"  "}};
    }

    @Test(dataProvider = "unsetValues")
    public void testGetTenantKeyAlgorithmDefault(String configuredValue) throws Exception {

        when(serverConfiguration.getFirstProperty(KEY_ALGORITHM_CONFIG)).thenReturn(configuredValue);
        assertEquals(KeystoreUtils.getTenantKeyAlgorithm(), "RSA");
    }

    @DataProvider(name = "validKeyAlgorithms")
    public Object[][] validKeyAlgorithms() {

        return new Object[][]{{"RSA"}, {"rsa"}, {" RSA "}};
    }

    @Test(dataProvider = "validKeyAlgorithms")
    public void testGetTenantKeyAlgorithmValid(String configuredValue) throws Exception {

        when(serverConfiguration.getFirstProperty(KEY_ALGORITHM_CONFIG)).thenReturn(configuredValue);
        assertEquals(KeystoreUtils.getTenantKeyAlgorithm(), "RSA");
    }

    @DataProvider(name = "invalidKeyAlgorithms")
    public Object[][] invalidKeyAlgorithms() {

        return new Object[][]{{"EC"}, {"DSA"}, {"RSASSA-PSS"}, {"abc"}};
    }

    @Test(dataProvider = "invalidKeyAlgorithms")
    public void testGetTenantKeyAlgorithmInvalid(String configuredValue) {

        when(serverConfiguration.getFirstProperty(KEY_ALGORITHM_CONFIG)).thenReturn(configuredValue);
        CarbonException e = expectThrows(CarbonException.class, KeystoreUtils::getTenantKeyAlgorithm);
        assertTrue(e.getMessage().contains("Unsupported key algorithm '" + configuredValue + "'"), e.getMessage());
        assertTrue(e.getMessage().contains("[keystore.tenant] key_algorithm"), e.getMessage());
    }

    @Test(dataProvider = "unsetValues")
    public void testGetTenantKeySizeDefault(String configuredValue) throws Exception {

        when(serverConfiguration.getFirstProperty(KEY_SIZE_CONFIG)).thenReturn(configuredValue);
        assertEquals(KeystoreUtils.getTenantKeySize(), 2048);
    }

    @DataProvider(name = "validKeySizes")
    public Object[][] validKeySizes() {

        return new Object[][]{
                {"2048", 2048}, {"3072", 3072}, {"4096", 4096}, {"5120", 5120}, {"6144", 6144}, {"7168", 7168},
                {"8192", 8192}, {" 3072 ", 3072}
        };
    }

    @Test(dataProvider = "validKeySizes")
    public void testGetTenantKeySizeValid(String configuredValue, int expectedKeySize) throws Exception {

        when(serverConfiguration.getFirstProperty(KEY_SIZE_CONFIG)).thenReturn(configuredValue);
        assertEquals(KeystoreUtils.getTenantKeySize(), expectedKeySize);
    }

    @DataProvider(name = "invalidKeySizes")
    public Object[][] invalidKeySizes() {

        return new Object[][]{
                {"1024"}, {"2047"}, {"3000"}, {"abc"}, {"16384"}, {"9216"}, {"0"}, {"-2048"}, {"3072.0"},
                {"99999999999"}
        };
    }

    @Test(dataProvider = "invalidKeySizes")
    public void testGetTenantKeySizeInvalid(String configuredValue) {

        when(serverConfiguration.getFirstProperty(KEY_SIZE_CONFIG)).thenReturn(configuredValue);
        CarbonException e = expectThrows(CarbonException.class, KeystoreUtils::getTenantKeySize);
        assertTrue(e.getMessage().contains("Invalid key size '" + configuredValue + "'"), e.getMessage());
        assertTrue(e.getMessage().contains("multiple of 1024 between 2048 and 8192"), e.getMessage());
    }

    @Test
    public void testPKCS11StoreType() throws Exception {

        assertTrue(KeystoreUtils.isPKCS11StoreType("PKCS11"));
        assertTrue(KeystoreUtils.isPKCS11StoreType(" pkcs11 "));
        assertFalse(KeystoreUtils.isPKCS11StoreType("PKCS12"));
        assertFalse(KeystoreUtils.isPKCS11StoreType(null));
        assertEquals(KeystoreUtils.getExtensionByFileType("PKCS11"), ".p11");
        assertEquals(KeystoreUtils.getFileTypeByExtension(".p11"), "PKCS11");
        // The primary key store type PKCS11 is now a supported store type.
        KeystoreUtils.StoreFileType.validateFileType("PKCS11");
    }

    @Test
    public void testGetKeystoreInstanceWithProvider() throws Exception {

        KeyStore keyStore = KeystoreUtils.getKeystoreInstance("PKCS12", "SUN");
        assertEquals(keyStore.getProvider().getName(), "SUN");
        // A blank provider falls back to the provider selection of getKeystoreInstance(String).
        assertEquals(KeystoreUtils.getKeystoreInstance("JKS", " ").getType(), "JKS");
        expectThrows(NoSuchProviderException.class,
                () -> KeystoreUtils.getKeystoreInstance("PKCS11", "NoSuchProvider"));
    }

    @Test
    public void testPKCS11KeystoreWithoutProviderFailsWithMoreThanOneToken() {

        Provider tokenA = new TestPKCS11Provider("TestTokenA");
        Provider tokenB = new TestPKCS11Provider("TestTokenB");
        Security.addProvider(tokenA);
        try {
            // One token: a blank provider is allowed, as before.
            assertEquals(KeystoreUtils.getKeystoreInstance("PKCS11", null).getProvider().getName(),
                    "TestTokenA");
            Security.addProvider(tokenB);
            KeyStoreException e = expectThrows(KeyStoreException.class,
                    () -> KeystoreUtils.getKeystoreInstance("PKCS11", " "));
            assertTrue(e.getMessage().contains("TestTokenA") && e.getMessage().contains("TestTokenB"),
                    e.getMessage());
            // A named provider still works.
            assertEquals(KeystoreUtils.getKeystoreInstance("PKCS11", "TestTokenB").getProvider().getName(),
                    "TestTokenB");
        } catch (KeyStoreException | NoSuchProviderException e) {
            throw new AssertionError(e);
        } finally {
            Security.removeProvider("TestTokenA");
            Security.removeProvider("TestTokenB");
        }
    }

    /**
     * Provider that offers a "PKCS11" key store, like a SunPKCS11 instance for one token.
     */
    private static final class TestPKCS11Provider extends Provider {

        private static final long serialVersionUID = 1L;

        TestPKCS11Provider(String name) {

            super(name, "1.0", "PKCS11 key store for tests");
            putService(new Service(this, "KeyStore", "PKCS11", EmptyKeyStoreSpi.class.getName(), null, null) {
                @Override
                public Object newInstance(Object constructorParameter) {

                    return new EmptyKeyStoreSpi();
                }
            });
        }
    }

    /**
     * Key store with no entries. Only the provider selection is tested.
     */
    private static final class EmptyKeyStoreSpi extends KeyStoreSpi {

        @Override
        public Key engineGetKey(String alias, char[] password) {

            return null;
        }

        @Override
        public Certificate[] engineGetCertificateChain(String alias) {

            return null;
        }

        @Override
        public Certificate engineGetCertificate(String alias) {

            return null;
        }

        @Override
        public Date engineGetCreationDate(String alias) {

            return null;
        }

        @Override
        public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) {

        }

        @Override
        public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) {

        }

        @Override
        public void engineSetCertificateEntry(String alias, Certificate cert) {

        }

        @Override
        public void engineDeleteEntry(String alias) {

        }

        @Override
        public Enumeration<String> engineAliases() {

            return Collections.emptyEnumeration();
        }

        @Override
        public boolean engineContainsAlias(String alias) {

            return false;
        }

        @Override
        public int engineSize() {

            return 0;
        }

        @Override
        public boolean engineIsKeyEntry(String alias) {

            return false;
        }

        @Override
        public boolean engineIsCertificateEntry(String alias) {

            return false;
        }

        @Override
        public String engineGetCertificateAlias(Certificate cert) {

            return null;
        }

        @Override
        public void engineStore(OutputStream stream, char[] password) {

        }

        @Override
        public void engineLoad(InputStream stream, char[] password) {

        }
    }
}
