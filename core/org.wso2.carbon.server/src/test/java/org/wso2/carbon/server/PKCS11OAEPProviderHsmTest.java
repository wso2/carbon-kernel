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
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * RSA-OAEP decryption with a key held in a SoftHSM token. Runs only with -Phsm-it; see the profile in the pom.
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
        assertTrue(PKCS11OAEPProvider.register() || Security.getProvider(PKCS11OAEPProvider.NAME) != null);
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
    public void testUnwrapOAEPSha256() throws Exception {

        // As Nimbus RSA-OAEP-256 decrypts: SHA-256 and MGF1-SHA-256 passed as parameters.
        OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT);
        SecretKey contentKey = aesKey();
        byte[] wrapped = encrypt("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", spec, contentKey.getEncoded());

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.UNWRAP_MODE, tokenKey, spec);
        try {
            assertEquals(cipher.unwrap(wrapped, "AES", Cipher.SECRET_KEY).getEncoded(), contentKey.getEncoded());
        } catch (ProviderException e) {
            // SoftHSM 2.x supports OAEP with SHA-1 only and answers CKR_ARGUMENTS_BAD.
            if (String.valueOf(e.getMessage()).contains("CKR_ARGUMENTS_BAD") ||
                    String.valueOf(e.getMessage()).contains("CKR_MECHANISM_PARAM_INVALID")) {
                throw new SkipException("The token does not support RSA-OAEP with SHA-256: " + e.getMessage());
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
