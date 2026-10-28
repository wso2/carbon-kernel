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

import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * RSA-OAEP decryption with a key held in a PKCS11 token (SoftHSM, or NSS softoken for OAEP with SHA-2). Runs only with
 * -Phsm-it; see the profile in the pom.
 */
@Test(groups = "hsm-it")
public class PKCS11OAEPProviderHsmTest {

    private Provider token;
    private PrivateKey tokenKey;
    private PublicKey publicKey;

    @BeforeClass(groups = "hsm-it")
    public void setUp() throws Exception {

        String cfg = System.getProperty("hsm.it.cfg");
        String pin = System.getProperty("hsm.it.pin");
        String alias = System.getProperty("hsm.it.alias");
        if (cfg == null || pin == null || alias == null) {
            throw new SkipException("Set -Dhsm.it.cfg, -Dhsm.it.pin and -Dhsm.it.alias to run the SoftHSM tests.");
        }
        token = Security.getProvider("SunPKCS11").configure(cfg);
        Security.addProvider(token);
        PKCS11OAEPProviderTest.registerOrSkip();
        KeyStore keyStore = KeyStore.getInstance("PKCS11", token);
        keyStore.load(null, pin.toCharArray());
        tokenKey = (PrivateKey) keyStore.getKey(alias, null);
        publicKey = keyStore.getCertificate(alias).getPublicKey();
        assertNull(tokenKey.getEncoded(), "The test key must be sensitive (not extractable).");
    }

    @AfterClass(groups = "hsm-it", alwaysRun = true)
    public void tearDown() {

        Security.removeProvider(PKCS11OAEPProvider.NAME);
        if (token != null) {
            Security.removeProvider(token.getName());
        }
    }

    @Test(groups = "hsm-it")
    public void testUnwrapOAEPSha1WithoutNamingAProvider() throws Exception {

        // As Nimbus RSA-OAEP decrypts a JWE content key: no provider named, UNWRAP_MODE.
        SecretKey contentKey = aesKey();
        byte[] wrapped = encrypt("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", null, contentKey.getEncoded());

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.UNWRAP_MODE, tokenKey);
        Key unwrapped = cipher.unwrap(wrapped, "AES", Cipher.SECRET_KEY);

        assertEquals(cipher.getProvider().getName(), PKCS11OAEPProvider.NAME);
        assertEquals(unwrapped.getEncoded(), contentKey.getEncoded());
    }

    @Test(groups = "hsm-it")
    public void testDecryptOAEPPaddingWithParameterSpec() throws Exception {

        OAEPParameterSpec spec = new OAEPParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1,
                PSource.PSpecified.DEFAULT);
        byte[] ciphertext = encrypt("RSA/ECB/OAEPPadding", spec, "hello token".getBytes(StandardCharsets.UTF_8));

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(Cipher.DECRYPT_MODE, tokenKey, spec);

        assertEquals(new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8), "hello token");
    }

    @Test(groups = "hsm-it")
    public void testPKCS1KeepsSunPKCS11() throws Exception {

        // RSA1_5 JWE decryption: no provider named; SunPKCS11 must still be selected.
        byte[] ciphertext = encrypt("RSA/ECB/PKCS1Padding", null, "pkcs1".getBytes(StandardCharsets.UTF_8));

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, tokenKey);

        assertEquals(new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8), "pkcs1");
        assertEquals(cipher.getProvider().getName(), token.getName());
    }

    @Test(groups = "hsm-it")
    public void testTamperedCiphertextRejected() throws Exception {

        byte[] ciphertext = encrypt("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", null, new byte[16]);
        ciphertext[ciphertext.length - 1] ^= 1;

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, tokenKey);

        expectThrows(BadPaddingException.class, () -> cipher.doFinal(ciphertext));
    }

    @Test(groups = "hsm-it")
    public void testOAEPDecodingFailuresLookTheSame() throws Exception {

        // Manger's attack needs different answers for "first byte of the decoded block is not zero" and "lHash does
        // not match". Build both blocks with raw RSA and the public key; the token must answer the same for both.
        // The cipher reports device, token, session and login errors with detail, so this fails if a token answers
        // one of the two cases with such a code. It must pass on every HSM used with this provider.
        int length = (((RSAPublicKey) publicKey).getModulus().bitLength() + 7) / 8;
        SecureRandom random = new SecureRandom();
        byte[] nonZeroFirstByte = new byte[length];
        random.nextBytes(nonZeroFirstByte);
        nonZeroFirstByte[0] = 0x01;
        byte[] wrongLabelHash = new byte[length];
        random.nextBytes(wrongLabelHash);
        wrongLabelHash[0] = 0x00;
        Cipher raw = Cipher.getInstance("RSA/ECB/NoPadding", "SunJCE");
        raw.init(Cipher.ENCRYPT_MODE, publicKey);

        for (byte[] block : new byte[][]{nonZeroFirstByte, wrongLabelHash}) {
            byte[] ciphertext = raw.doFinal(block);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, tokenKey);
            BadPaddingException e = expectThrows(BadPaddingException.class, () -> cipher.doFinal(ciphertext));
            assertEquals(e.getMessage(), "Decryption error");
            assertNull(e.getCause());
            assertEquals(cipher.getProvider().getName(), PKCS11OAEPProvider.NAME);
        }
    }

    @Test(groups = "hsm-it")
    public void testLongCiphertextRejectedBeforeToken() throws Exception {

        byte[] ciphertext = encrypt("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", null, new byte[16]);
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, tokenKey);

        expectThrows(IllegalBlockSizeException.class, () -> cipher.doFinal(Arrays.copyOf(ciphertext,
                ciphertext.length + 1)));
        // The cipher is still usable.
        assertEquals(cipher.doFinal(ciphertext), new byte[16]);
    }

    @Test(groups = "hsm-it")
    public void testCiphertextWithoutLeadingZeroAccepted() throws Exception {

        // As SunJCE: a ciphertext whose leading zero byte was dropped (some JWE encoders do this) still decrypts.
        byte[] plaintext = "short".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = null;
        for (int i = 0; i < 20_000 && ciphertext == null; i++) {
            byte[] candidate = encrypt("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", null, plaintext);
            if (candidate[0] == 0) {
                ciphertext = candidate;
            }
        }
        assertTrue(ciphertext != null, "No ciphertext with a leading zero byte was produced.");
        byte[] stripped = Arrays.copyOfRange(ciphertext, 1, ciphertext.length);

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, tokenKey);

        assertEquals(cipher.doFinal(stripped), plaintext);
        assertEquals(cipher.getProvider().getName(), PKCS11OAEPProvider.NAME);
    }

    @Test(groups = "hsm-it")
    public void testDoFinalInUnwrapModeRejected() throws Exception {

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.UNWRAP_MODE, tokenKey);

        expectThrows(IllegalStateException.class, () -> cipher.doFinal(new byte[0]));
    }

    @Test(groups = "hsm-it")
    public void testSessionsReusableAfterFailures() throws Exception {

        // Each failed decryption must end the token operation and return the session to the pool. A C_Decrypt error
        // already ends the operation, so this checks the release path, not finishing an operation that is still
        // active (that needs a token that fails on purpose).
        byte[] ciphertext = encrypt("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", null, "after".getBytes(
                StandardCharsets.UTF_8));
        byte[] tampered = ciphertext.clone();
        tampered[tampered.length - 1] ^= 1;
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, tokenKey);
        for (int i = 0; i < 64; i++) {
            expectThrows(BadPaddingException.class, () -> cipher.doFinal(tampered));
        }

        assertEquals(new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8), "after");
    }

    @DataProvider(name = "sha2Digests")
    public Object[][] sha2Digests() {

        return new Object[][]{{"SHA-256", MGF1ParameterSpec.SHA256}, {"SHA-384", MGF1ParameterSpec.SHA384},
                {"SHA-512", MGF1ParameterSpec.SHA512}};
    }

    @Test(groups = "hsm-it", dataProvider = "sha2Digests")
    public void testUnwrapOAEPSha2(String digest, MGF1ParameterSpec mgf1) throws Exception {

        // As Nimbus RSA-OAEP-256/384/512 decrypts: the digest and MGF1 with the same digest passed as parameters.
        OAEPParameterSpec spec = new OAEPParameterSpec(digest, "MGF1", mgf1, PSource.PSpecified.DEFAULT);
        String transformation = "RSA/ECB/OAEPWith" + digest + "AndMGF1Padding";
        SecretKey contentKey = aesKey();
        byte[] wrapped = encrypt(transformation, spec, contentKey.getEncoded());

        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.UNWRAP_MODE, tokenKey, spec);
        try {
            assertEquals(cipher.unwrap(wrapped, "AES", Cipher.SECRET_KEY).getEncoded(), contentKey.getEncoded());
            assertEquals(cipher.getProvider().getName(), PKCS11OAEPProvider.NAME);
        } catch (ProviderException e) {
            // SoftHSM 2.x supports OAEP with SHA-1 only and answers CKR_ARGUMENTS_BAD. NSS softoken supports SHA-2.
            if (String.valueOf(e.getMessage()).contains("CKR_ARGUMENTS_BAD") ||
                    String.valueOf(e.getMessage()).contains("CKR_MECHANISM_PARAM_INVALID")) {
                throw new SkipException("The token does not support RSA-OAEP with " + digest + ": " +
                        e.getMessage());
            }
            throw e;
        }
    }

    private byte[] encrypt(String transformation, OAEPParameterSpec spec, byte[] data) throws Exception {

        Cipher cipher = Cipher.getInstance(transformation, "SunJCE");
        if (spec == null) {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, spec);
        }
        return cipher.doFinal(data);
    }

    private static SecretKey aesKey() throws Exception {

        KeyGenerator generator = KeyGenerator.getInstance("AES", "SunJCE");
        generator.init(256);
        return generator.generateKey();
    }
}
