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

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

/**
 * Test JCA provider that simulates an HSM. The RSA private key stays inside the provider. Callers only get an
 * opaque {@link PrivateKey} handle that does not implement {@link java.security.interfaces.RSAPrivateKey} and has no
 * encoding, like a sensitive SunPKCS11 key. Only this provider can sign or decrypt with the handle. Like SunPKCS11, it
 * offers only the PKCS#1 v1.5 RSA cipher (no OAEP).
 */
public class OpaqueRSAKeyTestProvider extends Provider {

    private static final long serialVersionUID = 1L;
    private static final String RSA_PKCS1_CIPHER = "RSA/ECB/PKCS1Padding";
    /**
     * RSA-OAEP transformation that this provider also offers for its key.
     */
    public static final String RSA_OAEP_CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String[] SIGNATURE_ALGORITHMS =
            {"SHA256withRSA", "SHA384withRSA", "SHA512withRSA", "RSASSA-PSS"};

    private final transient KeyPair keyPair;
    private final transient OpaqueRSAPrivateKey handle = new OpaqueRSAPrivateKey();

    public OpaqueRSAKeyTestProvider(int keySize) throws NoSuchAlgorithmException {

        super("OpaqueRSATest", "1.0", "Test provider that keeps RSA private keys inside the provider");
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(keySize);
        keyPair = generator.generateKeyPair();
        for (String algorithm : SIGNATURE_ALGORITHMS) {
            putService(new SignatureService(this, algorithm));
        }
        for (String transformation : new String[]{RSA_PKCS1_CIPHER, RSA_OAEP_CIPHER}) {
            putService(new Service(this, "Cipher", transformation, OpaqueKeyCipherSpi.class.getName(), null, null) {
                @Override
                public Object newInstance(Object constructorParameter) {

                    return new OpaqueKeyCipherSpi((OpaqueRSAKeyTestProvider) getProvider(), transformation);
                }

                @Override
                public boolean supportsParameter(Object parameter) {

                    return parameter == ((OpaqueRSAKeyTestProvider) getProvider()).handle;
                }
            });
        }
    }

    /**
     * @return Opaque handle to the private key held by this provider.
     */
    public PrivateKey getPrivateKey() {

        return handle;
    }

    /**
     * @return Public key matching the private key held by this provider.
     */
    public PublicKey getPublicKey() {

        return keyPair.getPublic();
    }

    /**
     * Opaque private key handle: RSA algorithm, no encoding, not an RSAPrivateKey.
     */
    public static final class OpaqueRSAPrivateKey implements PrivateKey {

        private static final long serialVersionUID = 1L;

        @Override
        public String getAlgorithm() {

            return "RSA";
        }

        @Override
        public String getFormat() {

            return null;
        }

        @Override
        public byte[] getEncoded() {

            return null;
        }
    }

    private static final class SignatureService extends Service {

        SignatureService(OpaqueRSAKeyTestProvider provider, String algorithm) {

            super(provider, "Signature", algorithm, OpaqueKeySignatureSpi.class.getName(), null, null);
        }

        @Override
        public Object newInstance(Object constructorParameter) {

            return new OpaqueKeySignatureSpi((OpaqueRSAKeyTestProvider) getProvider(), getAlgorithm());
        }
    }

    /**
     * PKCS#1 v1.5 RSA cipher SPI that accepts only the provider's own handle for decryption.
     */
    private static final class OpaqueKeyCipherSpi extends CipherSpi {

        private final OpaqueRSAKeyTestProvider provider;
        private final String transformation;
        private Cipher delegate;

        OpaqueKeyCipherSpi(OpaqueRSAKeyTestProvider provider, String transformation) {

            this.provider = provider;
            this.transformation = transformation;
        }

        private void init(int opmode, java.security.Key key, SecureRandom random) throws InvalidKeyException {

            if (key != provider.handle) {
                throw new InvalidKeyException("Only keys held by this provider are supported.");
            }
            try {
                delegate = Cipher.getInstance(transformation, "SunJCE");
                delegate.init(opmode, provider.keyPair.getPrivate(), random);
            } catch (Exception e) {
                throw new InvalidKeyException("Error while initializing the delegate cipher.", e);
            }
        }

        @Override
        protected void engineSetMode(String mode) throws java.security.NoSuchAlgorithmException {

            // Fixed transformation.
        }

        @Override
        protected void engineSetPadding(String padding) throws NoSuchPaddingException {

            // Fixed transformation.
        }

        @Override
        protected int engineGetBlockSize() {

            return 0;
        }

        @Override
        protected int engineGetOutputSize(int inputLen) {

            return delegate.getOutputSize(inputLen);
        }

        @Override
        protected byte[] engineGetIV() {

            return null;
        }

        @Override
        protected AlgorithmParameters engineGetParameters() {

            return null;
        }

        @Override
        protected void engineInit(int opmode, java.security.Key key, SecureRandom random) throws InvalidKeyException {

            init(opmode, key, random);
        }

        @Override
        protected void engineInit(int opmode, java.security.Key key, AlgorithmParameterSpec params,
                                  SecureRandom random) throws InvalidKeyException {

            init(opmode, key, random);
        }

        @Override
        protected void engineInit(int opmode, java.security.Key key, AlgorithmParameters params, SecureRandom random)
                throws InvalidKeyException {

            init(opmode, key, random);
        }

        @Override
        protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {

            return delegate.update(input, inputOffset, inputLen);
        }

        @Override
        protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
                throws ShortBufferException {

            return delegate.update(input, inputOffset, inputLen, output, outputOffset);
        }

        @Override
        protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen)
                throws IllegalBlockSizeException, BadPaddingException {

            return delegate.doFinal(input, inputOffset, inputLen);
        }

        @Override
        protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
                throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {

            return delegate.doFinal(input, inputOffset, inputLen, output, outputOffset);
        }
    }

    /**
     * Signature SPI that accepts only the provider's own handle and signs with the key held inside the provider.
     */
    private static final class OpaqueKeySignatureSpi extends SignatureSpi {

        private final OpaqueRSAKeyTestProvider provider;
        private final String algorithm;
        private AlgorithmParameterSpec parameterSpec;
        private Signature delegate;

        OpaqueKeySignatureSpi(OpaqueRSAKeyTestProvider provider, String algorithm) {

            this.provider = provider;
            this.algorithm = algorithm;
        }

        @Override
        protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {

            if (privateKey != provider.handle) {
                throw new InvalidKeyException("Only keys held by this provider are supported.");
            }
            try {
                delegate = Signature.getInstance(algorithm, "SunRsaSign");
                if (parameterSpec != null) {
                    delegate.setParameter(parameterSpec);
                }
                delegate.initSign(provider.keyPair.getPrivate());
            } catch (Exception e) {
                throw new InvalidKeyException("Error while initializing the delegate signature.", e);
            }
        }

        @Override
        protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {

            throw new InvalidKeyException("Verification is not supported by this test provider.");
        }

        @Override
        protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {

            parameterSpec = params;
            if (delegate != null) {
                delegate.setParameter(params);
            }
        }

        @Override
        protected void engineUpdate(byte b) throws SignatureException {

            delegate.update(b);
        }

        @Override
        protected void engineUpdate(byte[] bytes, int off, int len) throws SignatureException {

            delegate.update(bytes, off, len);
        }

        @Override
        protected byte[] engineSign() throws SignatureException {

            return delegate.sign();
        }

        @Override
        protected boolean engineVerify(byte[] sigBytes) throws SignatureException {

            throw new SignatureException("Verification is not supported by this test provider.");
        }

        @Override
        @Deprecated
        protected void engineSetParameter(String param, Object value) {

            throw new InvalidParameterException("Not supported.");
        }

        @Override
        @Deprecated
        protected Object engineGetParameter(String param) {

            throw new InvalidParameterException("Not supported.");
        }
    }
}
