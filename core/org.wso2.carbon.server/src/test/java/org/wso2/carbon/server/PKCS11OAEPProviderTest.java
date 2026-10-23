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
package org.wso2.carbon.server;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.Security;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Tests for the RSA-OAEP provider that need no PKCS11 token: registration, provider selection for software keys
 * and parameter handling. Token decryption is covered by {@link PKCS11OAEPProviderHsmTest} (-Phsm-it).
 */
public class PKCS11OAEPProviderTest {

    private KeyPair softwareKeyPair;

    @BeforeClass
    public void setUp() throws Exception {

        // The surefire configuration opens the SunPKCS11 packages, as the start scripts do for the server.
        assertTrue(PKCS11OAEPProvider.register() || Security.getProvider(PKCS11OAEPProvider.NAME) != null);
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        softwareKeyPair = generator.generateKeyPair();
    }

    @AfterClass
    public void tearDown() {

        Security.removeProvider(PKCS11OAEPProvider.NAME);
    }

    @Test
    public void testRegisteredAfterJdkProviders() {

        Provider[] providers = Security.getProviders();
        assertEquals(providers[providers.length - 1].getName(), PKCS11OAEPProvider.NAME);
        assertFalse(PKCS11OAEPProvider.register(), "A second registration must be skipped.");
    }

    @Test
    public void testSoftwareKeyKeepsSunJCE() throws Exception {

        Cipher encrypt = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT);
        encrypt.init(Cipher.ENCRYPT_MODE, softwareKeyPair.getPublic(), spec);
        byte[] ciphertext = encrypt.doFinal("secret".getBytes(StandardCharsets.UTF_8));

        Cipher decrypt = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        decrypt.init(Cipher.DECRYPT_MODE, softwareKeyPair.getPrivate(), spec);

        assertEquals(decrypt.getProvider().getName(), "SunJCE");
        assertEquals(new String(decrypt.doFinal(ciphertext), StandardCharsets.UTF_8), "secret");
    }

    @Test
    public void testServiceAcceptsOnlyTokenKeys() {

        Provider.Service service = Security.getProvider(PKCS11OAEPProvider.NAME).getService("Cipher", "RSA");
        assertNotNull(service);
        assertFalse(service.supportsParameter(softwareKeyPair.getPrivate()));
        assertFalse(service.supportsParameter(softwareKeyPair.getPublic()));
        assertFalse(service.supportsParameter("not a key"));
    }

    @Test
    public void testExplicitProviderRejectsSoftwareKeyAndEncryption() throws Exception {

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding",
                Security.getProvider(PKCS11OAEPProvider.NAME));
        expectThrows(InvalidKeyException.class, () -> cipher.init(Cipher.DECRYPT_MODE, softwareKeyPair.getPrivate()));
        expectThrows(InvalidKeyException.class, () -> cipher.init(Cipher.ENCRYPT_MODE, softwareKeyPair.getPublic()));
    }

    @Test
    public void testPaddingNames() throws Exception {

        Provider provider = Security.getProvider(PKCS11OAEPProvider.NAME);
        for (String transformation : new String[]{"RSA/ECB/OAEPPadding", "RSA/ECB/OAEPWithSHA-1AndMGF1Padding",
                "RSA/ECB/OAEPWithSHA1AndMGF1Padding", "RSA/ECB/OAEPWithSHA-256AndMGF1Padding",
                "RSA/ECB/OAEPWithSHA-384AndMGF1Padding", "RSA/ECB/OAEPWithSHA-512AndMGF1Padding",
                "RSA/NONE/OAEPWithSHA-224AndMGF1Padding"}) {
            assertNotNull(Cipher.getInstance(transformation, provider), transformation);
        }
        for (String transformation : new String[]{"RSA/ECB/PKCS1Padding", "RSA/ECB/NoPadding",
                "RSA/ECB/OAEPWithMD5AndMGF1Padding"}) {
            expectThrows(NoSuchPaddingException.class, () -> Cipher.getInstance(transformation, provider));
        }
    }

    @Test
    public void testPKCS11ParameterValues() {

        // CKM_SHA_1/CKG_MGF1_SHA1 ... CKM_SHA512/CKG_MGF1_SHA512 from PKCS#11 v2.40.
        assertEquals(PKCS11OAEPCipher.pkcs11Values("SHA-1"), new long[]{0x220L, 0x1L});
        assertEquals(PKCS11OAEPCipher.pkcs11Values("SHA1"), new long[]{0x220L, 0x1L});
        assertEquals(PKCS11OAEPCipher.pkcs11Values("SHA-224"), new long[]{0x255L, 0x5L});
        assertEquals(PKCS11OAEPCipher.pkcs11Values("SHA-256"), new long[]{0x250L, 0x2L});
        assertEquals(PKCS11OAEPCipher.pkcs11Values("SHA-384"), new long[]{0x260L, 0x3L});
        assertEquals(PKCS11OAEPCipher.pkcs11Values("SHA-512"), new long[]{0x270L, 0x4L});
        assertNull(PKCS11OAEPCipher.pkcs11Values("MD5"));
    }
}
