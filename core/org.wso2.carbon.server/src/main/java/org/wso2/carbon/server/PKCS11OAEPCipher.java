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

import java.io.ByteArrayOutputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

/**
 * RSA-OAEP decryption with an RSA private key held in a PKCS#11 token (SunPKCS11 key). The token does the
 * decryption and the OAEP decoding ({@code CKM_RSA_PKCS_OAEP}); the key is used on the token.
 * <p>
 * Supports {@link Cipher#DECRYPT_MODE} and {@link Cipher#UNWRAP_MODE} (secret keys) only; encryption uses the public
 * key in software. Padding names and defaults follow SunJCE: {@code OAEPPadding} and
 * {@code OAEPWith<digest>AndMGF1Padding} use the named digest (SHA-1 for {@code OAEPPadding}) and MGF1 with SHA-1,
 * unless an {@link OAEPParameterSpec} says otherwise. An OAEP padding must be set: a bare {@code RSA} transformation
 * means PKCS#1 v1.5 padding in JCA, which this cipher does not offer.
 * <p>
 * Every failure of the token's decryption that can depend on the ciphertext becomes the same
 * {@link BadPaddingException}, so the result can't be used as a padding oracle (Manger's attack).
 */
public final class PKCS11OAEPCipher extends CipherSpi {

    private static final Pattern OAEP_WITH = Pattern.compile("OAEPWITH(.+)ANDMGF1PADDING");

    // CKM_ (hash) and CKG_ (MGF1) values from PKCS#11 v2.40.
    private static final long[][] HASHES = {
            {0x00000220L, 0x00000001L}, // SHA-1
            {0x00000255L, 0x00000005L}, // SHA-224
            {0x00000250L, 0x00000002L}, // SHA-256
            {0x00000260L, 0x00000003L}, // SHA-384
            {0x00000270L, 0x00000004L}  // SHA-512
    };
    private static final String[] HASH_NAMES = {"SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512"};

    // PKCS#11 v2.40 return values that report the state of the device, token, session or login, not the ciphertext.
    private static final long[] OPERATIONAL_ERRORS = {
            0x00000030L, // CKR_DEVICE_ERROR
            0x00000031L, // CKR_DEVICE_MEMORY
            0x00000032L, // CKR_DEVICE_REMOVED
            0x000000B0L, // CKR_SESSION_CLOSED
            0x000000B1L, // CKR_SESSION_COUNT
            0x000000B3L, // CKR_SESSION_HANDLE_INVALID
            0x000000B4L, // CKR_SESSION_PARALLEL_NOT_SUPPORTED
            0x000000B5L, // CKR_SESSION_READ_ONLY
            0x000000B6L, // CKR_SESSION_EXISTS
            0x000000B7L, // CKR_SESSION_READ_ONLY_EXISTS
            0x000000B8L, // CKR_SESSION_READ_WRITE_SO_EXISTS
            0x000000E0L, // CKR_TOKEN_NOT_PRESENT
            0x000000E1L, // CKR_TOKEN_NOT_RECOGNIZED
            0x000000E2L, // CKR_TOKEN_WRITE_PROTECTED
            0x00000101L  // CKR_USER_NOT_LOGGED_IN
    };
    private static final String DECRYPTION_ERROR = "Decryption error";
    private static final Logger logger = Logger.getLogger(PKCS11OAEPCipher.class.getName());

    private final JdkPKCS11 jdk;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    // OAEP digest from the padding name; null until an OAEP padding is set.
    private String paddingDigest;
    private OAEPParameterSpec spec;
    private PrivateKey key;
    private int opmode;

    PKCS11OAEPCipher(JdkPKCS11 jdk) {

        this.jdk = jdk;
    }

    @Override
    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {

        if (!"ECB".equalsIgnoreCase(mode) && !"NONE".equalsIgnoreCase(mode)) {
            throw new NoSuchAlgorithmException("Unsupported mode: " + mode);
        }
    }

    @Override
    protected void engineSetPadding(String padding) throws NoSuchPaddingException {

        String upper = padding.toUpperCase(Locale.ENGLISH);
        if ("OAEPPADDING".equals(upper)) {
            paddingDigest = "SHA-1";
            return;
        }
        Matcher matcher = OAEP_WITH.matcher(upper);
        if (!matcher.matches() || hashIndex(matcher.group(1)) < 0) {
            throw new NoSuchPaddingException("Unsupported padding: " + padding);
        }
        paddingDigest = HASH_NAMES[hashIndex(matcher.group(1))];
    }

    @Override
    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {

        try {
            engineInit(opmode, key, (AlgorithmParameterSpec) null, random);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException("Unable to use the default OAEP parameters.", e);
        }
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {

        if (opmode != Cipher.DECRYPT_MODE && opmode != Cipher.UNWRAP_MODE) {
            throw new InvalidKeyException("Only decryption and unwrapping with a PKCS11 private key are supported; " +
                    "encrypt with the public key.");
        }
        if (paddingDigest == null) {
            throw new InvalidKeyException("No OAEP padding is set. Use RSA/ECB/OAEPPadding or " +
                    "RSA/ECB/OAEPWith<digest>AndMGF1Padding; PKCS#1 v1.5 padding is not offered by this provider.");
        }
        if (!jdk.isTokenRSAPrivateKey(key)) {
            throw new InvalidKeyException("Only RSA private keys held in a PKCS11 token are supported.");
        }
        OAEPParameterSpec oaepSpec;
        if (params == null) {
            oaepSpec = new OAEPParameterSpec(paddingDigest, "MGF1", MGF1ParameterSpec.SHA1,
                    PSource.PSpecified.DEFAULT);
        } else if (params instanceof OAEPParameterSpec) {
            oaepSpec = (OAEPParameterSpec) params;
        } else {
            throw new InvalidAlgorithmParameterException("Only OAEPParameterSpec is supported.");
        }
        validate(oaepSpec);
        this.spec = oaepSpec;
        this.key = (PrivateKey) key;
        this.opmode = opmode;
        buffer.reset();
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {

        OAEPParameterSpec oaepSpec = null;
        if (params != null) {
            try {
                oaepSpec = params.getParameterSpec(OAEPParameterSpec.class);
            } catch (InvalidParameterSpecException e) {
                throw new InvalidAlgorithmParameterException("Only OAEP parameters are supported.", e);
            }
        }
        engineInit(opmode, key, oaepSpec, random);
    }

    @Override
    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {

        buffer.write(input, inputOffset, inputLen);
        return new byte[0];
    }

    @Override
    protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) {

        buffer.write(input, inputOffset, inputLen);
        return 0;
    }

    @Override
    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen)
            throws IllegalBlockSizeException, BadPaddingException {

        if (opmode != Cipher.DECRYPT_MODE) {
            throw new IllegalStateException("Cipher not initialized for decryption.");
        }
        if (input != null) {
            buffer.write(input, inputOffset, inputLen);
        }
        byte[] ciphertext = buffer.toByteArray();
        buffer.reset();
        return decrypt(ciphertext);
    }

    @Override
    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
            throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {

        byte[] plaintext = engineDoFinal(input, inputOffset, inputLen);
        try {
            if (output.length - outputOffset < plaintext.length) {
                throw new ShortBufferException("Output buffer too small: " + plaintext.length + " bytes needed.");
            }
            System.arraycopy(plaintext, 0, output, outputOffset, plaintext.length);
            return plaintext.length;
        } finally {
            Arrays.fill(plaintext, (byte) 0);
        }
    }

    @Override
    protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType)
            throws InvalidKeyException, NoSuchAlgorithmException {

        if (opmode != Cipher.UNWRAP_MODE) {
            throw new IllegalStateException("Cipher not initialized for unwrapping.");
        }
        if (wrappedKeyType != Cipher.SECRET_KEY) {
            throw new InvalidKeyException("Only secret keys can be unwrapped.");
        }
        byte[] encoded;
        try {
            encoded = decrypt(wrappedKey);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new InvalidKeyException("Unwrapping failed.", e);
        }
        try {
            return new SecretKeySpec(encoded, wrappedKeyAlgorithm);
        } finally {
            Arrays.fill(encoded, (byte) 0);
        }
    }

    @Override
    protected int engineGetBlockSize() {

        return 0;
    }

    @Override
    protected int engineGetOutputSize(int inputLen) {

        return buffer.size() + inputLen;
    }

    @Override
    protected byte[] engineGetIV() {

        return null;
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {

        // Null before init, as SunJCE.
        if (spec == null) {
            return null;
        }
        try {
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("OAEP");
            parameters.init(spec);
            return parameters;
        } catch (NoSuchAlgorithmException | InvalidParameterSpecException e) {
            return null;
        }
    }

    @Override
    protected int engineGetKeySize(Key key) throws InvalidKeyException {

        if (!jdk.isTokenRSAPrivateKey(key)) {
            throw new InvalidKeyException("Only RSA private keys held in a PKCS11 token are supported.");
        }
        return jdk.keySize(key);
    }

    private byte[] decrypt(byte[] ciphertext) throws BadPaddingException, IllegalBlockSizeException {

        if (key == null) {
            throw new IllegalStateException("Cipher not initialized.");
        }
        // The ciphertext length is public, so it is checked here; the token is not asked. As SunJCE, a longer
        // ciphertext is rejected and a shorter one is read as an unsigned integer: it is left-padded with zeros to the
        // modulus length. Some JWE encoders drop leading zero bytes of the encrypted key; RFC 8017 (7.1.2 step 1)
        // requires exactly the modulus length, but software keys accept such JWEs, so token keys do too.
        int modulusLength = (jdk.keySize(key) + 7) / 8;
        if (ciphertext.length > modulusLength) {
            throw new IllegalBlockSizeException("Data must not be longer than " + modulusLength + " bytes");
        }
        if (ciphertext.length < modulusLength) {
            byte[] padded = new byte[modulusLength];
            System.arraycopy(ciphertext, 0, padded, modulusLength - ciphertext.length, ciphertext.length);
            ciphertext = padded;
        }
        long[] hash = HASHES[hashIndex(spec.getDigestAlgorithm())];
        long[] mgf = HASHES[hashIndex(((MGF1ParameterSpec) spec.getMGFParameters()).getDigestAlgorithm())];
        byte[] label = ((PSource.PSpecified) spec.getPSource()).getValue();
        try {
            return jdk.decryptOAEP(key, hash[0], mgf[1], label, ciphertext);
        } catch (JdkPKCS11.TokenException e) {
            if (dependsOnCiphertext(e)) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "PKCS11 RSA-OAEP decryption failed: " + e.getMessage());
                }
                throw new BadPaddingException(DECRYPTION_ERROR);
            }
            throw new ProviderException("The PKCS11 token failed RSA-OAEP decryption with " +
                    spec.getDigestAlgorithm() + "/MGF1-" +
                    ((MGF1ParameterSpec) spec.getMGFParameters()).getDigestAlgorithm() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Whether a failed token decryption must be reported as a {@link BadPaddingException} without detail.
     * <p>
     * A PKCS#11 error from C_Decrypt can depend on the ciphertext, and tokens use different return values for
     * different OAEP decoding failures. So every such error is reported the same way, except errors about the device,
     * token, session or login. Errors before C_Decrypt (session, key handle, C_DecryptInit, for example an OAEP
     * digest the token does not support) don't depend on the ciphertext and keep their detail.
     *
     * @param e Token failure.
     * @return True to report the failure as a {@link BadPaddingException} without detail.
     */
    static boolean dependsOnCiphertext(JdkPKCS11.TokenException e) {

        return e.getPhase() == JdkPKCS11.TokenException.Phase.DECRYPT &&
                e.getErrorCode() != JdkPKCS11.TokenException.NO_ERROR_CODE && !isOperationalError(e.getErrorCode());
    }

    /**
     * @param errorCode PKCS#11 return value.
     * @return True if the value reports the state of the device, token, session or login.
     */
    static boolean isOperationalError(long errorCode) {

        for (long operational : OPERATIONAL_ERRORS) {
            if (operational == errorCode) {
                return true;
            }
        }
        return false;
    }

    private static void validate(OAEPParameterSpec spec) throws InvalidAlgorithmParameterException {

        if (hashIndex(spec.getDigestAlgorithm()) < 0) {
            throw new InvalidAlgorithmParameterException("Unsupported OAEP digest: " + spec.getDigestAlgorithm());
        }
        if (!"MGF1".equalsIgnoreCase(spec.getMGFAlgorithm()) ||
                !(spec.getMGFParameters() instanceof MGF1ParameterSpec) ||
                hashIndex(((MGF1ParameterSpec) spec.getMGFParameters()).getDigestAlgorithm()) < 0) {
            throw new InvalidAlgorithmParameterException("Only MGF1 with SHA-1 or SHA-2 is supported.");
        }
        if (!(spec.getPSource() instanceof PSource.PSpecified)) {
            throw new InvalidAlgorithmParameterException("Only PSource.PSpecified is supported.");
        }
    }

    /**
     * @return Index into HASHES for a digest name such as SHA-256, SHA256 or SHA-1, or -1.
     */
    static int hashIndex(String digest) {

        String normalized = digest.toUpperCase(Locale.ENGLISH).replace("-", "");
        for (int i = 0; i < HASH_NAMES.length; i++) {
            if (HASH_NAMES[i].replace("-", "").equals(normalized)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return CKM_ hash and CKG_ MGF1 values for a digest name, for tests.
     */
    static long[] pkcs11Values(String digest) {

        int index = hashIndex(digest);
        return index < 0 ? null : HASHES[index].clone();
    }
}
