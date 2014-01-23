/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
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

package org.apache.ws.security.components.crypto;

import org.apache.ws.security.WSSecurityException;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Crypto.
 * <p/>
 *
 * @author Davanum Srinivas (dims@yahoo.com).
 */
public interface Crypto {
    /**
     * load a X509Certificate from the input stream.
     * <p/>
     *
     * @param in The <code>InputStream</code> array containing the X509 data
     * @return An X509 certificate
     * @throws WSSecurityException
     */
    X509Certificate loadCertificate(InputStream in) throws WSSecurityException;

    /**
     * Construct an array of X509Certificate's from the byte array.
     * <p/>
     *
     * @param data    The <code>byte</code> array containing the X509 data
     * @param reverse If set the first certificate in input data will
     *                the last in the array
     * @return An array of X509 certificates, ordered according to
     *         the reverse flag
     * @throws WSSecurityException
     */
    X509Certificate[] getX509Certificates(byte[] data, boolean reverse) throws WSSecurityException;

    /**
     * get a byte array given an array of X509 certificates.
     * <p/>
     *
     * @param reverse If set the first certificate in the array data will
     *                the last in the byte array
     * @param certs   The certificates to convert
     * @return The byte array for the certificates ordered according
     *         to the reverse flag
     * @throws WSSecurityException
     */
    byte[] getCertificateData(boolean reverse, X509Certificate[] certs) throws WSSecurityException;

    /**
     * Gets the private key identified by <code>alias</> and <code>password</code>.
     * <p/>
     *
     * @param alias    The alias (<code>KeyStore</code>) of the key owner
     * @param password The password needed to access the private key
     * @return The private key
     * @throws Exception
     */
    public PrivateKey getPrivateKey(String alias, String password) throws Exception;

    /**
     * get the list of certificates for a given alias. This method
     * reads a new certificate chain and overwrites a previously
     * stored certificate chain.
     * <p/>
     *
     * @param alias Lookup certificate chain for this alias
     * @return Array of X509 certificates for this alias name, or
     *         null if this alias does not exist in the keystore
     */
    public X509Certificate[] getCertificates(String alias) throws WSSecurityException;

    /**
     * Return a X509 Certificate alias in the keystore according to a given Certificate
     * <p/>
     *
     * @param cert The certificate to lookup
     * @return alias name of the certificate that matches the given certificate
     *         or null if no such certificate was found.
     *         <p/>
     *         See comment above
     *         <p/>
     *         See comment above
     */
    /*
     * See comment above
     */
    public String getAliasForX509Cert(Certificate cert) throws WSSecurityException;

    /**
     * Lookup a X509 Certificate in the keystore according to a given
     * the issuer of a Certificate.
     * <p/>
     * The search gets all alias names of the keystore and gets the certificate chain
     * for each alias. Then the Issuer of each certificate of the chain
     * is compared with the parameters.
     *
     * @param issuer The issuer's name for the certificate
     * @return alias name of the certificate that matches the issuer name
     *         or null if no such certificate was found.
     */
    public String getAliasForX509Cert(String issuer) throws WSSecurityException;

    /**
     * Search a X509 Certificate in the keystore according to a given serial number and
     * the issuer of a Certificate.
     * <p/>
     * The search gets all alias names of the keystore and gets the certificate chain
     * for each alias. Then the SerialNumber and Issuer of each certificate of the chain
     * is compared with the parameters.
     *
     * @param issuer       The issuer's name for the certificate
     * @param serialNumber The serial number of the certificate from the named issuer
     * @return alias name of the certificate that matches serialNumber and issuer name
     *         or null if no such certificate was found.
     */
    public String getAliasForX509Cert(String issuer, BigInteger serialNumber) throws WSSecurityException;

    /**
     * Lookup a X509 Certificate in the keystore according to a given
     * SubjectKeyIdentifier.
     * <p/>
     * The search gets all alias names of the keystore and gets the certificate chain
     * or certificate for each alias. Then the SKI for each user certificate
     * is compared with the SKI parameter.
     *
     * @param skiBytes The SKI info bytes
     * @return alias name of the certificate that matches serialNumber and issuer name
     *         or null if no such certificate was found.
     */
    public String getAliasForX509Cert(byte[] skiBytes) throws WSSecurityException;

    /**
     * Retrieves the alias name of the default certificate which has been
     * specified as a property. This should be the certificate that is used for
     * signature and encryption. This alias corresponds to the certificate that
     * should be used whenever KeyInfo is not present in a signed or
     * an encrypted message. May return null.
     *
     * @return alias name of the default X509 certificate.
     */
    public String getDefaultX509Alias();

    /**
     * Reads the SubjectKeyIdentifier information from the certificate.
     * <p/>
     *
     * @param cert The certificate to read SKI
     * @return The byte array containing the binary SKI data
     */
    public byte[] getSKIBytesFromCert(X509Certificate cert) throws WSSecurityException;
 
    /**
     * Lookup a X509 Certificate in the keystore according to a given
     * Thumbprint.
     * 
     * The search gets all alias names of the keystore, then reads the certificate chain
     * or certificate for each alias. Then the thumbprint for each user certificate
     * is compared with the thumbprint parameter.
     *
     * @param thumb The SHA1 thumbprint info bytes
     * @return alias name of the certificate that matches the thumbprint
     *         or null if no such certificate was found.
     * @throws WSSecurityException if problems during keystore handling or wrong certificate
     */

    public String getAliasForX509CertThumb(byte[] thumb) throws WSSecurityException;
 
    /**
     * Gets the Keystore that was loaded by the underlying implementation
     *
     * @return the Keystore
     */
    public KeyStore getKeyStore();

    /**
     * Gets the CertificateFactory instantiated by the underlying implementation
     *
     * @return the CertificateFactory
     * @throws WSSecurityException
     */
    public CertificateFactory getCertificateFactory() throws WSSecurityException;

    /**
     * Uses the CertPath API to validate a given certificate chain
     * <p/>
     *
     * @param certs Certificate chain to validate
     * @return true if the certificate chain is valid, false otherwise
     * @throws WSSecurityException
     */
    public boolean validateCertPath(X509Certificate[] certs) throws WSSecurityException;

    /**
     * Lookup X509 Certificates in the keystore according to a given DN of the subject of the certificate
     * <p/>
     *
     * @param subjectDN The DN of subject to look for in the keystore
     * @return Vector with all alias of certificates with the same DN as given in the parameters
     * @throws WSSecurityException
     */
    public String[] getAliasesForDN(String subjectDN) throws WSSecurityException;
}
