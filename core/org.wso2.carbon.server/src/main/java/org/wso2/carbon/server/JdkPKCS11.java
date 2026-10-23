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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Key;
import java.security.PrivateKey;
import java.util.Arrays;

/**
 * Access to the JDK's internal PKCS#11 wrapper ({@code sun.security.pkcs11.wrapper}) and to the SunPKCS11 key,
 * token and session classes, for operations that SunPKCS11 does not offer through JCA (RSA-OAEP decryption).
 * <p>
 * All access is reflective, through the class loader of the {@code jdk.crypto.cryptoki} module, so no compile time
 * or OSGi dependency on the internal packages is needed. The JVM must open the packages:
 * {@code --add-opens=jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED} and
 * {@code --add-opens=jdk.crypto.cryptoki/sun.security.pkcs11.wrapper=ALL-UNNAMED}. The members used here are the
 * same from JDK 11 to JDK 25.
 * <p>
 * Operations run on a session from SunPKCS11's own session pool, so they use the login SunPKCS11 already made.
 */
final class JdkPKCS11 {

    // PKCS#11 v2.40 constants.
    static final long CKM_RSA_PKCS_OAEP = 0x00000009L;
    static final long CKZ_DATA_SPECIFIED = 0x00000001L;

    static final String ADD_OPENS_OPTIONS = "--add-opens=jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED " +
            "--add-opens=jdk.crypto.cryptoki/sun.security.pkcs11.wrapper=ALL-UNNAMED";

    private static final String PKCS11 = "sun.security.pkcs11.";
    private static final String WRAPPER = PKCS11 + "wrapper.";

    private final Class<?> p11KeyClass;
    private final Field keyToken;
    private final Method getKeyID;
    private final Method releaseKeyID;
    private final Method keyLength;
    private final Field tokenP11;
    private final Method getOpSession;
    private final Method releaseSession;
    private final Method killSession;
    private final Method sessionId;
    private final Method decryptInit;
    private final Method decrypt;
    private final Constructor<?> mechanismConstructor;
    private final Field mechanismParameter;
    private final Constructor<?> oaepParamsConstructor;
    private final Field oaepHashAlg;
    private final Field oaepMgf;
    private final Field oaepSource;
    private final Field oaepSourceData;
    private final Class<?> pkcs11ExceptionClass;

    private JdkPKCS11() throws ReflectiveOperationException {

        ClassLoader loader = ClassLoader.getPlatformClassLoader();
        p11KeyClass = Class.forName(PKCS11 + "P11Key", false, loader);
        Class<?> tokenClass = Class.forName(PKCS11 + "Token", false, loader);
        Class<?> sessionClass = Class.forName(PKCS11 + "Session", false, loader);
        Class<?> pkcs11Class = Class.forName(WRAPPER + "PKCS11", false, loader);
        Class<?> mechanismClass = Class.forName(WRAPPER + "CK_MECHANISM", false, loader);
        Class<?> oaepParamsClass = Class.forName(WRAPPER + "CK_RSA_PKCS_OAEP_PARAMS", false, loader);
        pkcs11ExceptionClass = Class.forName(WRAPPER + "PKCS11Exception", false, loader);

        keyToken = accessible(p11KeyClass.getDeclaredField("token"));
        getKeyID = accessible(p11KeyClass.getDeclaredMethod("getKeyID"));
        releaseKeyID = accessible(p11KeyClass.getDeclaredMethod("releaseKeyID"));
        keyLength = accessible(p11KeyClass.getDeclaredMethod("length"));
        tokenP11 = accessible(tokenClass.getDeclaredField("p11"));
        getOpSession = accessible(tokenClass.getDeclaredMethod("getOpSession"));
        releaseSession = accessible(tokenClass.getDeclaredMethod("releaseSession", sessionClass));
        killSession = accessible(tokenClass.getDeclaredMethod("killSession", sessionClass));
        sessionId = accessible(sessionClass.getDeclaredMethod("id"));
        decryptInit = accessible(pkcs11Class.getMethod("C_DecryptInit", long.class, mechanismClass, long.class));
        decrypt = accessible(pkcs11Class.getMethod("C_Decrypt", long.class, long.class, byte[].class, int.class,
                int.class, long.class, byte[].class, int.class, int.class));
        mechanismConstructor = accessible(mechanismClass.getConstructor(long.class));
        mechanismParameter = accessible(mechanismClass.getField("pParameter"));
        oaepParamsConstructor = accessible(oaepParamsClass.getConstructor());
        oaepHashAlg = accessible(oaepParamsClass.getField("hashAlg"));
        oaepMgf = accessible(oaepParamsClass.getField("mgf"));
        oaepSource = accessible(oaepParamsClass.getField("source"));
        oaepSourceData = accessible(oaepParamsClass.getField("pSourceData"));
    }

    /**
     * Load the JDK internals.
     *
     * @return Access object.
     * @throws ReflectiveOperationException If a class or member is missing (different JDK).
     * @throws RuntimeException             If the JVM does not open the packages (InaccessibleObjectException).
     */
    static JdkPKCS11 load() throws ReflectiveOperationException {

        return new JdkPKCS11();
    }

    /**
     * @param key Key to check.
     * @return True for an RSA private key loaded by SunPKCS11.
     */
    boolean isTokenRSAPrivateKey(Key key) {

        return key instanceof PrivateKey && "RSA".equalsIgnoreCase(key.getAlgorithm()) && p11KeyClass.isInstance(key);
    }

    /**
     * @param key SunPKCS11 key.
     * @return Key size in bits.
     */
    int keySize(Key key) {

        try {
            return (int) keyLength.invoke(key);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to read the size of the PKCS11 key.", e);
        }
    }

    /**
     * Decrypt with CKM_RSA_PKCS_OAEP on the token that holds the key.
     *
     * @param key        SunPKCS11 RSA private key.
     * @param hashAlg    CKM_ value of the OAEP hash.
     * @param mgf        CKG_ value of the MGF1 hash.
     * @param label      OAEP label, or null for none.
     * @param ciphertext Ciphertext.
     * @return Plaintext.
     * @throws TokenException If the token rejects the operation.
     */
    byte[] decryptOAEP(PrivateKey key, long hashAlg, long mgf, byte[] label, byte[] ciphertext)
            throws TokenException {

        try {
            Object params = oaepParamsConstructor.newInstance();
            oaepHashAlg.setLong(params, hashAlg);
            oaepMgf.setLong(params, mgf);
            oaepSource.setLong(params, CKZ_DATA_SPECIFIED);
            oaepSourceData.set(params, label == null || label.length == 0 ? null : label);
            Object mechanism = mechanismConstructor.newInstance(CKM_RSA_PKCS_OAEP);
            mechanismParameter.set(mechanism, params);
            return decrypt(key, mechanism, ciphertext);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to build the PKCS11 OAEP mechanism.", e);
        }
    }

    private byte[] decrypt(PrivateKey key, Object mechanism, byte[] ciphertext) throws TokenException {

        Object token;
        Object p11;
        Object session;
        try {
            token = keyToken.get(key);
            p11 = tokenP11.get(token);
            session = getOpSession.invoke(token);
        } catch (InvocationTargetException e) {
            throw tokenException(e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to get a PKCS11 session.", e);
        }
        // A PKCS#11 error from C_DecryptInit leaves no active operation, and one from C_Decrypt ends the operation
        // (PKCS#11 v2.40, 5.2), so the session goes back to the pool. Any other failure leaves the session in an
        // unknown state; it is closed instead.
        boolean closeSession = false;
        try {
            long id = (long) sessionId.invoke(session);
            long keyId = (long) getKeyID.invoke(key);
            try {
                decryptInit.invoke(p11, id, mechanism, keyId);
                byte[] out = new byte[ciphertext.length];
                int length = (int) decrypt.invoke(p11, id, 0L, ciphertext, 0, ciphertext.length, 0L, out, 0,
                        out.length);
                return Arrays.copyOf(out, length);
            } finally {
                releaseKeyID.invoke(key);
            }
        } catch (InvocationTargetException e) {
            closeSession = !pkcs11ExceptionClass.isInstance(e.getCause());
            throw tokenException(e);
        } catch (ReflectiveOperationException e) {
            closeSession = true;
            throw new IllegalStateException("Unable to run the PKCS11 decryption.", e);
        } finally {
            try {
                if (closeSession) {
                    killSession.invoke(token, session);
                } else {
                    releaseSession.invoke(token, session);
                }
            } catch (ReflectiveOperationException e) {
                // Nothing else can be done with the session here.
            }
        }
    }

    private TokenException tokenException(InvocationTargetException e) {

        Throwable cause = e.getCause();
        if (pkcs11ExceptionClass.isInstance(cause)) {
            return new TokenException(cause.getMessage(), cause);
        }
        return new TokenException("PKCS11 operation failed: " + cause, cause);
    }

    private static <T extends java.lang.reflect.AccessibleObject> T accessible(T member) {

        member.setAccessible(true);
        return member;
    }

    /**
     * The token rejected an operation. The message is the PKCS#11 return value name, for example
     * CKR_ENCRYPTED_DATA_INVALID.
     */
    static final class TokenException extends Exception {

        private static final long serialVersionUID = 1L;

        TokenException(String message, Throwable cause) {

            super(message, cause);
        }
    }
}
