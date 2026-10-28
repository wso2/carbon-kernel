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
import java.util.Arrays;
import java.util.List;
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
 * Tests for the RSA-OAEP provider that need no PKCS11 token: registration, provider selection for software keys,
 * parameter handling and error mapping. Token decryption is covered by {@link PKCS11OAEPProviderHsmTest} (-Phsm-it).
 */
public class PKCS11OAEPProviderTest {

    // Stands in for a configured token: registration needs a provider named SunPKCS11-<name>.
    private static final String TOKEN_PROVIDER = "SunPKCS11-unit-test";

    private KeyPair softwareKeyPair;

    @BeforeClass
    public void setUp() throws Exception {

        addTokenProvider();
        // The surefire configuration opens the SunPKCS11 packages, as the start scripts do for the server.
        assertTrue(PKCS11OAEPProvider.register() || Security.getProvider(PKCS11OAEPProvider.NAME) != null);
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        softwareKeyPair = generator.generateKeyPair();
    }

    @AfterClass
    public void tearDown() {

        Security.removeProvider(PKCS11OAEPProvider.NAME);
        Security.removeProvider(TOKEN_PROVIDER);
    }

    @Test
    public void testRegisteredAfterJdkAndTokenProviders() {

        List<String> names = providerNames();
        int position = names.indexOf(PKCS11OAEPProvider.NAME);
        assertTrue(position > names.indexOf("SunJCE"), names.toString());
        assertTrue(position > names.indexOf(TOKEN_PROVIDER), names.toString());
        assertFalse(PKCS11OAEPProvider.register(), "A second registration must be skipped.");
    }

    @Test
    public void testNotRegisteredWithoutTokenProvider() {

        Security.removeProvider(PKCS11OAEPProvider.NAME);
        Security.removeProvider(TOKEN_PROVIDER);
        try {
            assertFalse(PKCS11OAEPProvider.register());
            assertNull(Security.getProvider(PKCS11OAEPProvider.NAME));
        } finally {
            addTokenProvider();
            assertTrue(PKCS11OAEPProvider.register());
        }
    }

    @Test
    public void testUsableAsCipherProvider() throws Exception {

        // Fails on JVMs that require signed JCE providers (Oracle JDK); register() then removes the provider.
        PKCS11OAEPProvider.checkUsable(Security.getProvider(PKCS11OAEPProvider.NAME));
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
    public void testBareRSAIsNotOAEP() throws Exception {

        // In JCA, "RSA" means PKCS#1 v1.5 padding. Without an OAEP padding the cipher must not default to OAEP.
        Cipher cipher = Cipher.getInstance("RSA", Security.getProvider(PKCS11OAEPProvider.NAME));
        assertNull(cipher.getParameters());
        InvalidKeyException e = expectThrows(InvalidKeyException.class,
                () -> cipher.init(Cipher.DECRYPT_MODE, softwareKeyPair.getPrivate()));
        assertTrue(e.getMessage().contains("No OAEP padding"), e.getMessage());
    }

    @Test
    public void testNoParametersBeforeInitAsSunJCE() throws Exception {

        for (String transformation : new String[]{"RSA/ECB/OAEPPadding", "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"}) {
            assertNull(Cipher.getInstance(transformation, "SunJCE").getParameters(), transformation);
            assertNull(Cipher.getInstance(transformation, Security.getProvider(PKCS11OAEPProvider.NAME))
                    .getParameters(), transformation);
        }
    }

    @Test
    public void testDecryptErrorsWithoutDetail() {

        // C_Decrypt answers can depend on the ciphertext: one BadPaddingException for all of them.
        for (long code : new long[]{0x40L /* CKR_ENCRYPTED_DATA_INVALID */, 0x41L /* ENCRYPTED_DATA_LEN_RANGE */,
                0x05L /* CKR_GENERAL_ERROR */, 0x06L /* CKR_FUNCTION_FAILED */, 0x21L /* CKR_DATA_LEN_RANGE */,
                0x150L /* CKR_BUFFER_TOO_SMALL */, 0x80000001L /* vendor defined */}) {
            assertTrue(PKCS11OAEPCipher.dependsOnCiphertext(failure(JdkPKCS11.TokenException.Phase.DECRYPT, code)),
                    Long.toHexString(code));
        }
    }

    @Test
    public void testOperationalAndInitErrorsKeepDetail() {

        // Device, session, token and login errors during C_Decrypt.
        for (long code : new long[]{0x30L, 0x31L, 0x32L, 0xB0L, 0xB1L, 0xB3L, 0xB4L, 0xB5L, 0xB6L, 0xB7L, 0xB8L,
                0xE0L, 0xE1L, 0xE2L, 0x101L}) {
            assertFalse(PKCS11OAEPCipher.dependsOnCiphertext(failure(JdkPKCS11.TokenException.Phase.DECRYPT, code)),
                    Long.toHexString(code));
        }
        // Not a PKCS#11 return value.
        assertFalse(PKCS11OAEPCipher.dependsOnCiphertext(failure(JdkPKCS11.TokenException.Phase.DECRYPT,
                JdkPKCS11.TokenException.NO_ERROR_CODE)));
        // Before C_Decrypt: does not depend on the ciphertext.
        for (long code : new long[]{0x71L /* CKR_MECHANISM_PARAM_INVALID */, 0x07L /* CKR_ARGUMENTS_BAD */,
                0x40L /* CKR_ENCRYPTED_DATA_INVALID */}) {
            assertFalse(PKCS11OAEPCipher.dependsOnCiphertext(failure(JdkPKCS11.TokenException.Phase.INIT, code)),
                    Long.toHexString(code));
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

    private static JdkPKCS11.TokenException failure(JdkPKCS11.TokenException.Phase phase, long code) {

        return new JdkPKCS11.TokenException(phase, code, "CKR_" + Long.toHexString(code), null);
    }

    private static void addTokenProvider() {

        Security.addProvider(new Provider(TOKEN_PROVIDER, "1.0", "Stand-in for a SunPKCS11 token provider") {
            private static final long serialVersionUID = 1L;
        });
    }

    private static List<String> providerNames() {

        return Arrays.asList(Arrays.stream(Security.getProviders()).map(Provider::getName).toArray(String[]::new));
    }
}
