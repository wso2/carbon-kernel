/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.core.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.utils.ServerConstants;

import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;

import static org.wso2.carbon.core.util.CryptoUtil.getJCEProvider;

public class SignatureUtil {

    private static final String THUMB_DIGEST_ALGORITHM_SHA1 = "SHA-1";
    private static final String THUMB_DIGEST_ALGORITHM_SHA256 = "SHA-256";
    private static final String signatureAlgorithmSHA1 = "SHA1withRSA";
    private static final String signatureAlgorithmSHA256 = "SHA256withRSA";


    private SignatureUtil() {
        // hide default constructor for utility class
    }

    public static void init() throws Exception {

        String providerName = ServerConfiguration.getInstance().getFirstProperty(ServerConstants.JCE_PROVIDER);
        Provider provider;
        if (StringUtils.isBlank(providerName) || providerName.equals(ServerConstants.JCE_PROVIDER_BC)) {
            provider = (Provider) (Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider")).
                    getDeclaredConstructor().newInstance();

        } else if (providerName.equals(ServerConstants.JCE_PROVIDER_BCFIPS)) {
            provider = (Provider) (Class.forName("org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider")).
                    getDeclaredConstructor().newInstance();

        } else {
            throw new NoSuchProviderException("Configured JCE provider is not supported.");
        }
        Security.addProvider(provider);
    }

    /**
     * Retrieves the thumbprint for alias.
     *
     * @param alias The alias
     * @return Thumbprint is returned.
     * @throws Exception
     */
    public static byte[] getThumbPrintForAlias(String alias) throws Exception {

        MessageDigest sha;
        if (Boolean.parseBoolean(ServerConfiguration.getInstance().getFirstProperty(
                ServerConstants.SIGNATURE_UTIL_ENABLE_SHA256_ALGO))) {
            sha = MessageDigest.getInstance(THUMB_DIGEST_ALGORITHM_SHA256);
        } else {
            sha = MessageDigest.getInstance(THUMB_DIGEST_ALGORITHM_SHA1);
        }
        sha.reset();
        Certificate cert = getCertificate(alias);
        sha.update(cert.getEncoded());
        return sha.digest();
    }

    /**
     * Validates the signature with the given thumbprint
     *
     * @param thumb     Thumbprint of the certificate
     * @param data      Data on which the signature is performed
     * @param signature The signature.
     * @return
     * @throws Exception
     */
    public static boolean validateSignature(byte[] thumb, String data, byte[] signature) throws Exception {

        Signature signer;
        if (Boolean.parseBoolean(ServerConfiguration.getInstance().getFirstProperty(
                ServerConstants.SIGNATURE_UTIL_ENABLE_SHA256_ALGO))) {
            signer = Signature.getInstance(signatureAlgorithmSHA256, getJCEProvider());
        } else {
            signer = Signature.getInstance(signatureAlgorithmSHA1, getJCEProvider());
        }
        signer.initVerify(getPublicKey(thumb));
        signer.update(data.getBytes());
        return signer.verify(signature);
    }

    /**
     * Validate the signature with the default thumbprint.
     *
     * @param data      The data which is used to perfrom the signature.
     * @param signature The signature to be validated.
     * @return True is returned if singature is valid.
     * @throws Exception
     */
    public static boolean validateSignature(String data, byte[] signature) throws Exception {

        Signature signer;
        if (Boolean.parseBoolean(ServerConfiguration.getInstance().getFirstProperty(
                ServerConstants.SIGNATURE_UTIL_ENABLE_SHA256_ALGO))) {
            signer = Signature.getInstance(signatureAlgorithmSHA256, getJCEProvider());
        } else {
            signer = Signature.getInstance(signatureAlgorithmSHA1, getJCEProvider());
        }
        signer.initVerify(getDefaultPublicKey());
        signer.update(data.getBytes());
        return signer.verify(signature);
    }

    /**
     * Performs the signature with the default private key in the system.
     *
     * @param data Data to be signed.
     * @return The signature is returned.
     * @throws Exception
     */
    public static byte[] doSignature(String data) throws Exception {

        Signature signer;
        if (Boolean.parseBoolean(ServerConfiguration.getInstance().getFirstProperty(
                ServerConstants.SIGNATURE_UTIL_ENABLE_SHA256_ALGO))) {
            signer = Signature.getInstance(signatureAlgorithmSHA256, getJCEProvider());
        } else {
            signer = Signature.getInstance(signatureAlgorithmSHA1, getJCEProvider());
        }
        signer.initSign(getDefaultPrivateKey());
        signer.update(data.getBytes());
        return signer.sign();
    }

    private static PrivateKey getDefaultPrivateKey() throws Exception {
        KeyStoreManager keyStoreMan = KeyStoreManager.getInstance(
                MultitenantConstants.SUPER_TENANT_ID);
        KeyStore keyStore = keyStoreMan.getPrimaryKeyStore();
        ServerConfigurationService config = CarbonCoreDataHolder.getInstance().getServerConfigurationService();
        String password = config
                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_PASSWORD);
        String alias = config.getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_KEY_ALIAS);
        return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
    }

    private static PublicKey getDefaultPublicKey() throws Exception {
        KeyStoreManager keyStoreMan = KeyStoreManager.getInstance(
                MultitenantConstants.SUPER_TENANT_ID);
        KeyStore keyStore = keyStoreMan.getPrimaryKeyStore();
        ServerConfigurationService config = CarbonCoreDataHolder.getInstance().getServerConfigurationService();
        String alias = config
                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_KEY_ALIAS);
        return (PublicKey) keyStore.getCertificate(alias).getPublicKey();

    }

    private static PublicKey getPublicKey(byte[] thumb) throws Exception {
        KeyStoreManager keyStoreMan = KeyStoreManager.getInstance(
                MultitenantConstants.SUPER_TENANT_ID);
        KeyStore keyStore = keyStoreMan.getPrimaryKeyStore();
        PublicKey pubKey = null;
        Certificate cert = null;
        MessageDigest sha;
        if (Boolean.parseBoolean(ServerConfiguration.getInstance().getFirstProperty(
                ServerConstants.SIGNATURE_UTIL_ENABLE_SHA256_ALGO))) {
            sha = MessageDigest.getInstance(THUMB_DIGEST_ALGORITHM_SHA256);
        } else {
            sha = MessageDigest.getInstance(THUMB_DIGEST_ALGORITHM_SHA1);
        }
        sha.reset();
        for (Enumeration<String> e = keyStore.aliases(); e.hasMoreElements(); ) {
            String alias = e.nextElement();
            cert = getCertificate(alias);
            sha.update(cert.getEncoded());
            byte[] data = sha.digest();

            if (Arrays.equals(data, thumb)) {
                pubKey = cert.getPublicKey();
                break;
            }
        }
        return pubKey;
    }

    private static Certificate getCertificate(String alias) throws Exception {
        KeyStoreManager keyStoreMan = KeyStoreManager.getInstance(
                MultitenantConstants.SUPER_TENANT_ID);
        KeyStore keyStore = keyStoreMan.getPrimaryKeyStore();
        Certificate cert = null;
        Certificate[] certs = keyStore.getCertificateChain(alias);
        if (certs == null || certs.length == 0) {
            cert = keyStore.getCertificate(alias);
        } else {
            cert = certs[0];
        }
        if (!(cert instanceof X509Certificate)) {
            throw new Exception("Please check alias. Cannot retrieve valid certificate");
        }
        return cert;
    }

}
