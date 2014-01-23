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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.security.auth.x500.X500Principal;

/**
 * Created by IntelliJ IDEA.
 * User: dims
 * Date: Sep 15, 2005
 * Time: 9:50:40 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class CryptoBase implements Crypto {
    private static Log log = LogFactory.getLog(CryptoBase.class);
    private static final Constructor BC_509CLASS_CONS;
    

    protected static Map certFactMap = new HashMap();
    protected KeyStore keystore = null;
    static String SKI_OID = "2.5.29.14";
    protected KeyStore cacerts = null;

    static {
        Constructor cons = null;
        try {
            Class c = Class.forName("org.bouncycastle.asn1.x509.X509Name");
            cons = c.getConstructor(new Class[] {String.class});
        } catch (Exception e) {
            //ignore
        }
        BC_509CLASS_CONS = cons;
    }

    
    /**
     * Constructor
     */
    protected CryptoBase() {
    }
    
    /**
     * @return      a crypto provider name.  This operation should
     *              return null if the default crypto provider should
     *              be used.
     */
    protected abstract String getCryptoProvider();
    
    
    private String mapKeystoreProviderToCertProvider(String s) {
        if ("SunJSSE".equals(s)) {
            return "SUN";
        }
        return s;
    }
    
    /**
     * Singleton certificate factory for this Crypto instance.
     * <p/>
     *
     * @return Returns a <code>CertificateFactory</code> to construct
     *         X509 certificates
     * @throws org.apache.ws.security.WSSecurityException
     *
     */
    public synchronized CertificateFactory getCertificateFactory() throws WSSecurityException {
        String provider = getCryptoProvider();
        String keyStoreProvider = keystore == null ? null : keystore.getProvider().getName();

        //Try to find a CertificateFactory that generates certs that are fully
        //compatible with the certs in the KeyStore  (Sun -> Sun, BC -> BC, etc...)
        CertificateFactory factory = null;
        if (provider != null) {
            factory = (CertificateFactory)certFactMap.get(provider);
        } else if (keyStoreProvider != null) {
            factory = 
                (CertificateFactory)certFactMap.get(
                    mapKeystoreProviderToCertProvider(keyStoreProvider)
                );
            if (factory == null) {
                factory = (CertificateFactory)certFactMap.get(keyStoreProvider);                
            }
        } else {
            factory = (CertificateFactory)certFactMap.get("DEFAULT");
        }
        if (factory == null) {
            try {
                if (provider == null || provider.length() == 0) {
                    if (keyStoreProvider != null && keyStoreProvider.length() != 0) {
                        try {
                            factory = 
                                CertificateFactory.getInstance(
                                    "X.509", 
                                    mapKeystoreProviderToCertProvider(keyStoreProvider)
                                );
                            certFactMap.put(keyStoreProvider, factory);
                            certFactMap.put(
                                mapKeystoreProviderToCertProvider(keyStoreProvider), factory
                            );
                        } catch (Exception ex) {
                            log.debug(ex);
                            //Ignore, we'll just use the default since they didn't specify one.
                            //Hopefully that will work for them.
                        }
                    }
                    if (factory == null) {
                        factory = CertificateFactory.getInstance("X.509");
                        certFactMap.put("DEFAULT", factory);
                    }
                } else {
                    factory = CertificateFactory.getInstance("X.509", provider);
                    certFactMap.put(provider, factory);
                }
                certFactMap.put(factory.getProvider().getName(), factory);
            } catch (CertificateException e) {
                throw new WSSecurityException(
                    WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "unsupportedCertType",
                    null, e
                );
            } catch (NoSuchProviderException e) {
                throw new WSSecurityException(
                    WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "noSecProvider",
                    null, e
                );
            }
        }
        return factory;
    }

    /**
     * load a X509Certificate from the input stream.
     * <p/>
     *
     * @param in The <code>InputStream</code> array containing the X509 data
     * @return Returns a X509 certificate
     * @throws org.apache.ws.security.WSSecurityException
     *
     */
    public X509Certificate loadCertificate(InputStream in) throws WSSecurityException {
        X509Certificate cert = null;
        try {
            cert = (X509Certificate) getCertificateFactory().generateCertificate(in);
        } catch (CertificateException e) {
            throw new WSSecurityException(
                WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "parseError",
                null, e
            );
        }
        return cert;
    }

    /**
     * Gets the private key identified by <code>alias</> and <code>password</code>.
     * <p/>
     *
     * @param alias    The alias (<code>KeyStore</code>) of the key owner
     * @param password The password needed to access the private key
     * @return The private key
     * @throws Exception
     */
    public PrivateKey getPrivateKey(String alias, String password) throws Exception {
        if (alias == null) {
            throw new Exception("alias is null");
        }
        boolean b = keystore.isKeyEntry(alias);
        if (!b) {
            String msg = "Cannot find key for alias: [" + alias + "]";
            String logMsg = createKeyStoreErrorMessage(keystore);
            log.error(msg + logMsg);
            throw new Exception(msg);
        }
        
        Key keyTmp = keystore.getKey(alias, password == null ? new char[]{} : password.toCharArray());
        if (!(keyTmp instanceof PrivateKey)) {
            String msg = "Key is not a private key, alias: [" + alias + "]";
            String logMsg = createKeyStoreErrorMessage(keystore);
            log.error(msg + logMsg);
            throw new Exception(msg);
        }
        return (PrivateKey) keyTmp;
    }
    
    protected static String createKeyStoreErrorMessage(KeyStore keystore) throws KeyStoreException {
        Enumeration aliases = keystore.aliases();
        StringBuffer sb = new StringBuffer(keystore.size() * 7);
        boolean firstAlias = true;
        while (aliases.hasMoreElements()) {
            if (!firstAlias) {
                sb.append(", ");
            }
            sb.append(aliases.nextElement());
            firstAlias = false;
        }
        String msg = " in keystore of type [" + keystore.getType()
            + "] from provider [" + keystore.getProvider()
            + "] with size [" + keystore.size() + "] and aliases: {"
            + sb.toString() + "}";
        return msg;
    }

    protected Vector splitAndTrim(String inString) {
        X509NameTokenizer nmTokens = new X509NameTokenizer(inString);
        Vector vr = new Vector();

        while (nmTokens.hasMoreTokens()) {
            vr.add(nmTokens.nextToken());
        }
        java.util.Collections.sort(vr);
        return vr;
    }

    /**
     * Lookup a X509 Certificate in the keystore according to a given
     * the issuer of a Certificate.
     * <p/>
     * The search gets all alias names of the keystore and gets the certificate chain
     * for each alias. Then the Issuer for each certificate of the chain
     * is compared with the parameters.
     *
     * @param issuer The issuer's name for the certificate
     * @return alias name of the certificate that matches the issuer name
     *         or null if no such certificate was found.
     */
    public String getAliasForX509Cert(String issuer) throws WSSecurityException {
        return getAliasForX509Cert(issuer, null, false);
    }
    
    
    private Object createBCX509Name(String s) {
        if (BC_509CLASS_CONS != null) {
            try {
                return BC_509CLASS_CONS.newInstance(new Object[] {s});
            } catch (Exception e) {
                //ignore
            }
        }
        return new X500Principal(s);
    }
    /**
     * Lookup a X509 Certificate in the keystore according to a given serial number and
     * the issuer of a Certificate.
     * <p/>
     * The search gets all alias names of the keystore and gets the certificate chain
     * for each alias. Then the SerialNumber and Issuer for each certificate of the chain
     * is compared with the parameters.
     *
     * @param issuer       The issuer's name for the certificate
     * @param serialNumber The serial number of the certificate from the named issuer
     * @return alias name of the certificate that matches serialNumber and issuer name
     *         or null if no such certificate was found.
     */
    public String getAliasForX509Cert(String issuer, BigInteger serialNumber)
        throws WSSecurityException {
        return getAliasForX509Cert(issuer, serialNumber, true);
    }

    /*
    * need to check if "getCertificateChain" also finds certificates that are
    * used for encryption only, i.e. they may not be signed by a CA
    * Otherwise we must define a restriction how to use certificate:
    * each certificate must be signed by a CA or is a self signed Certificate
    * (this should work as well).
    * --- remains to be tested in several ways --
    */
    private String getAliasForX509Cert(
        String issuer, 
        BigInteger serialNumber,
        boolean useSerialNumber
    ) throws WSSecurityException {
        Object issuerName = null;
        Certificate cert = null;
        
        if (keystore == null) {
            return null;
        }
        
        //
        // Convert the issuer DN to a java X500Principal object first. This is to ensure
        // interop with a DN constructed from .NET, where e.g. it uses "S" instead of "ST".
        // Then convert it to a BouncyCastle X509Name, which will order the attributes of
        // the DN in a particular way (see WSS-168). If the conversion to an X500Principal
        // object fails (e.g. if the DN contains "E" instead of "EMAILADDRESS"), then fall
        // back on a direct conversion to a BC X509Name
        //
        try {
            X500Principal issuerRDN = new X500Principal(issuer);
            issuerName = createBCX509Name(issuerRDN.getName());
        } catch (java.lang.IllegalArgumentException ex) {
            issuerName = createBCX509Name(issuer);
        }

        try {
            for (Enumeration e = keystore.aliases(); e.hasMoreElements();) {
                String alias = (String) e.nextElement();
                Certificate[] certs = keystore.getCertificateChain(alias);
                if (certs == null || certs.length == 0) {
                    // no cert chain, so lets check if getCertificate gives us a result.
                    cert = keystore.getCertificate(alias);
                    if (cert == null) {
                        continue;
                    }
                } else {
                    cert = certs[0];
                }
                if (!(cert instanceof X509Certificate)) {
                    continue;
                }
                X509Certificate x509cert = (X509Certificate) cert;
                if (!useSerialNumber || x509cert.getSerialNumber().compareTo(serialNumber) == 0) {
                    Object certName = createBCX509Name(x509cert.getIssuerX500Principal().getName());
                    if (certName.equals(issuerName)) {
                        return alias;
                    }
                }
            }
        } catch (KeyStoreException e) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "keystore", null, e);
        }
        return null;
    }

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
     * @throws org.apache.ws.security.WSSecurityException
     *          if problems during keystore handling or wrong certificate (no SKI data)
     */
    public String getAliasForX509Cert(byte[] skiBytes) throws WSSecurityException {
        Certificate cert = null;

        if (keystore == null) {
            return null;
        }
        try {
            for (Enumeration e = keystore.aliases(); e.hasMoreElements();) {
                String alias = (String) e.nextElement();
                Certificate[] certs = keystore.getCertificateChain(alias);
                if (certs == null || certs.length == 0) {
                    // no cert chain, so lets check if getCertificate gives us a  result.
                    cert = keystore.getCertificate(alias);
                    if (cert == null) {
                        continue;
                    }
                } else {
                    cert = certs[0];
                }
                if (!(cert instanceof X509Certificate)) {
                    continue;
                }
                byte[] data = getSKIBytesFromCert((X509Certificate) cert);
                if (data.length != skiBytes.length) {
                    continue;
                }
                if (Arrays.equals(data, skiBytes)) {
                    return alias;
                }
            }
        } catch (KeyStoreException e) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "keystore", null, e);
        }
        return null;
    }

    /**
     * Return a X509 Certificate alias in the keystore according to a given Certificate
     * <p/>
     *
     * @param cert The certificate to lookup
     * @return alias name of the certificate that matches the given certificate
     *         or null if no such certificate was found.
     */
    public String getAliasForX509Cert(Certificate cert) throws WSSecurityException {
        try {
            if (keystore == null) {
                return null;
            }
            //
            // The following code produces the wrong alias in BouncyCastle and so
            // we'll just use the brute-force search
            //
            // String alias = keystore.getCertificateAlias(cert);
            // if (alias != null) {
            //     return alias;
            // }
            Enumeration e = keystore.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                Certificate retrievedCert = keystore.getCertificate(alias);
                if (retrievedCert != null && retrievedCert.equals(cert)) {
                    return alias;
                }
            }
        } catch (KeyStoreException e) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "keystore", null, e);
        }
        return null;
    }


    /**
     * Gets the list of certificates for a given alias.
     * <p/>
     *
     * @param alias Lookup certificate chain for this alias
     * @return Array of X509 certificates for this alias name, or
     *         null if this alias does not exist in the keystore
     */
    public X509Certificate[] getCertificates(String alias) throws WSSecurityException {
        Certificate[] certs = null;
        Certificate cert = null;
        try {
            if (this.keystore != null) {
                //There's a chance that there can only be a set of trust stores
                certs = keystore.getCertificateChain(alias);
                if (certs == null || certs.length == 0) {
                    // no cert chain, so lets check if getCertificate gives us a
                    // result.
                    cert = keystore.getCertificate(alias);
                }
            }

            if (certs == null && cert == null && cacerts != null) {
                // Now look into the trust stores
                certs = cacerts.getCertificateChain(alias);
                if (certs == null) {
                    cert = cacerts.getCertificate(alias);
                }
            }

            if (cert != null) {
                certs = new Certificate[]{cert};
            } else if (certs == null) {
                // At this point we don't have certs or a cert
                return null;
            }
        } catch (KeyStoreException e) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "keystore", null, e);
        }

        X509Certificate[] x509certs = new X509Certificate[certs.length];
        for (int i = 0; i < certs.length; i++) {
            x509certs[i] = (X509Certificate) certs[i];
        }
        return x509certs;
    }

    /**
     * Lookup a X509 Certificate in the keystore according to a given
     * Thumbprint.
     * <p/>
     * The search gets all alias names of the keystore, then reads the certificate chain
     * or certificate for each alias. Then the thumbprint for each user certificate
     * is compared with the thumbprint parameter.
     *
     * @param thumb The SHA1 thumbprint info bytes
     * @return alias name of the certificate that matches the thumbprint
     *         or null if no such certificate was found.
     * @throws org.apache.ws.security.WSSecurityException
     *          if problems during keystore handling or wrong certificate
     */
    public String getAliasForX509CertThumb(byte[] thumb) throws WSSecurityException {
        Certificate cert = null;
        MessageDigest sha = null;
        
        if (keystore == null) {
            return null;
        }

        try {
            sha = MessageDigest.getInstance("SHA-1");
            sha.reset();
        } catch (NoSuchAlgorithmException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, "noSHA1availabe", null, e
            );
        }
        try {
            for (Enumeration e = keystore.aliases(); e.hasMoreElements();) {
                String alias = (String) e.nextElement();
                Certificate[] certs = keystore.getCertificateChain(alias);
                if (certs == null || certs.length == 0) {
                    // no cert chain, so lets check if getCertificate gives us a  result.
                    cert = keystore.getCertificate(alias);
                    if (cert == null) {
                        continue;
                    }
                } else {
                    cert = certs[0];
                }
                if (!(cert instanceof X509Certificate)) {
                    continue;
                }
                try {
                    sha.update(cert.getEncoded());
                } catch (CertificateEncodingException ex) {
                    throw new WSSecurityException(
                        WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "encodeError",
                        null, ex
                    );
                }
                byte[] data = sha.digest();

                if (Arrays.equals(data, thumb)) {
                    return alias;
                }
            }
        } catch (KeyStoreException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, "keystore", null, e
            );
        }
        return null;
    }

    /**
     * A Hook for subclasses to set the keystore without having to
     * load it from an <code>InputStream</code>.
     *
     * @param ks existing keystore
     */
    public void setKeyStore(KeyStore ks) {
        keystore = ks;
    }

    /**
     * Reads the SubjectKeyIdentifier information from the certificate.
     * <p/>
     * If the the certificate does not contain a SKI extension then
     * try to compute the SKI according to RFC3280 using the
     * SHA-1 hash value of the public key. The second method described
     * in RFC3280 is not support. Also only RSA public keys are supported.
     * If we cannot compute the SKI throw a WSSecurityException.
     *
     * @param cert The certificate to read SKI
     * @return The byte array containing the binary SKI data
     */
    public byte[] getSKIBytesFromCert(X509Certificate cert) throws WSSecurityException {
        //
        // Gets the DER-encoded OCTET string for the extension value (extnValue)
        // identified by the passed-in oid String. The oid string is represented
        // by a set of positive whole numbers separated by periods.
        //
        byte[] derEncodedValue = cert.getExtensionValue(SKI_OID);

        if (cert.getVersion() < 3 || derEncodedValue == null) {
            PublicKey key = cert.getPublicKey();
            if (!(key instanceof RSAPublicKey)) {
                throw new WSSecurityException(
                        1,
                        "noSKIHandling",
                        new Object[]{"Support for RSA key only"});
            }
            byte[] encoded = key.getEncoded();
            // remove 22-byte algorithm ID and header
            byte[] value = new byte[encoded.length - 22];
            System.arraycopy(encoded, 22, value, 0, value.length);
            MessageDigest sha;
            try {
                sha = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException ex) {
                throw new WSSecurityException(
                    WSSecurityException.UNSUPPORTED_SECURITY_TOKEN, "noSKIHandling",
                    new Object[]{"Wrong certificate version (<3) and no SHA1 message digest availabe"},
                    ex
                );
            }
            sha.reset();
            sha.update(value);
            return sha.digest();
        }

        //
        // Strip away first four bytes from the DerValue (tag and length of
        // ExtensionValue OCTET STRING and KeyIdentifier OCTET STRING)
        //
        byte abyte0[] = new byte[derEncodedValue.length - 4];

        System.arraycopy(derEncodedValue, 4, abyte0, 0, abyte0.length);
        return abyte0;
    }

    public KeyStore getKeyStore() {
        return this.keystore;
    }
    
    /**
     * Lookup X509 Certificates in the keystore according to a given DN of the subject of the certificate
     * <p/>
     * The search gets all alias names of the keystore and gets the certificate (chain)
     * for each alias. Then the DN of the certificate is compared with the parameters.
     *
     * @param subjectDN The DN of subject to look for in the keystore
     * @return Vector with all alias of certificates with the same DN as given in the parameters
     * @throws org.apache.ws.security.WSSecurityException
     *
     */
    public String[] getAliasesForDN(String subjectDN) throws WSSecurityException {

        //
        // Convert the subject DN to a java X500Principal object first. This is to ensure
        // interop with a DN constructed from .NET, where e.g. it uses "S" instead of "ST".
        // Then convert it to a BouncyCastle X509Name, which will order the attributes of
        // the DN in a particular way (see WSS-168). If the conversion to an X500Principal
        // object fails (e.g. if the DN contains "E" instead of "EMAILADDRESS"), then fall
        // back on a direct conversion to a BC X509Name
        //
        Object subject;
        try {
            X500Principal subjectRDN = new X500Principal(subjectDN);
            subject = createBCX509Name(subjectRDN.getName());
        } catch (java.lang.IllegalArgumentException ex) {
            subject = createBCX509Name(subjectDN);
        }
        Vector aliases = getAlias(subject, keystore);
        
        //If we can't find the issuer in the keystore then look at cacerts
        if (aliases.size() == 0 && cacerts != null) {
            aliases = getAlias(subject, cacerts);
        }
        
        // Convert the vector into an array
        String[] result = new String[aliases.size()];
        for (int i = 0; i < aliases.size(); i++) {
            result[i] = (String) aliases.elementAt(i);
        }

        return result;
    }
    
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
    public byte[] getCertificateData(boolean reverse, X509Certificate[] certs)
        throws WSSecurityException {
        Vector list = new Vector();
        for (int i = 0; i < certs.length; i++) {
            if (reverse) {
                list.insertElementAt(certs[i], 0);
            } else {
                list.add(certs[i]);
            }
        }
        try {
            CertPath path = getCertificateFactory().generateCertPath(list);
            return path.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new WSSecurityException(
                WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "encodeError",
                null, e
            );
        } catch (CertificateException e) {
            throw new WSSecurityException(
                WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "parseError",
                null, e
            );
        }
    }

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
    public X509Certificate[] getX509Certificates(byte[] data, boolean reverse)
        throws WSSecurityException {
        InputStream in = new ByteArrayInputStream(data);
        CertPath path = null;
        try {
            path = getCertificateFactory().generateCertPath(in);
        } catch (CertificateException e) {
            throw new WSSecurityException(
                WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "parseError",
                null, e
            );
        }
        List l = path.getCertificates();
        X509Certificate[] certs = new X509Certificate[l.size()];
        Iterator iterator = l.iterator();
        for (int i = 0; i < l.size(); i++) {
            certs[(reverse) ? (l.size() - 1 - i) : i] = (X509Certificate) iterator.next();
        }
        return certs;
    }

    /**
     * Overridden because there's a bug in the base class where they don't use
     * the provider variant for the certificate validator.
     *
     * @param certs
     *            Certificate chain to validate
     * @return true if the certificate chain is valid, false otherwise
     * @throws WSSecurityException
     */
    public boolean
    validateCertPath(
        java.security.cert.X509Certificate[] certs
    ) throws org.apache.ws.security.WSSecurityException {
        try {
            // Generate cert path
            java.util.List cert_list = java.util.Arrays.asList(certs);
            java.security.cert.CertPath path =
                getCertificateFactory().generateCertPath(cert_list);

            // Use the certificates in the keystore as TrustAnchors
            java.security.cert.PKIXParameters param =
                new java.security.cert.PKIXParameters(this.keystore);
            
            // Do not check a revocation list
            param.setRevocationEnabled(false);

            // Verify the trust path using the above settings
            String provider = getCryptoProvider();
            java.security.cert.CertPathValidator validator = null;
            if (provider == null || provider.length() == 0) {
                validator =
                    java.security.cert.CertPathValidator.getInstance("PKIX");
            } else {
                validator =
                    java.security.cert.CertPathValidator.getInstance(
                        "PKIX",
                        provider
                    );
            }
            validator.validate(path, param);
        } catch (java.security.NoSuchProviderException e) {
                throw new org.apache.ws.security.WSSecurityException(
                    org.apache.ws.security.WSSecurityException.FAILURE,
                    "certpath",
                    new Object[] { e.getMessage() },
                    e
                );
        } catch (java.security.NoSuchAlgorithmException e) {
                throw new org.apache.ws.security.WSSecurityException(
                    org.apache.ws.security.WSSecurityException.FAILURE,
                    "certpath", new Object[] { e.getMessage() },
                    e
                );
        } catch (java.security.cert.CertificateException e) {
                throw new org.apache.ws.security.WSSecurityException(
                    org.apache.ws.security.WSSecurityException.FAILURE,
                    "certpath", new Object[] { e.getMessage() },
                    e
                );
        } catch (java.security.InvalidAlgorithmParameterException e) {
                throw new org.apache.ws.security.WSSecurityException(
                    org.apache.ws.security.WSSecurityException.FAILURE,
                    "certpath",
                    new Object[] { e.getMessage() },
                    e
                );
        } catch (java.security.cert.CertPathValidatorException e) {
                throw new org.apache.ws.security.WSSecurityException(
                    org.apache.ws.security.WSSecurityException.FAILURE,
                    "certpath",
                    new Object[] { e.getMessage() },
                    e
                );
        } catch (java.security.KeyStoreException e) {
                throw new org.apache.ws.security.WSSecurityException(
                    org.apache.ws.security.WSSecurityException.FAILURE,
                    "certpath",
                    new Object[] { e.getMessage() },
                    e
                );
        }

        return true;
    }
    
    /**
     * The subjectRDN argument is either an X500Principal or a BouncyCastle X509Name instance.
     */
    private Vector getAlias(Object subjectRDN, KeyStore store) throws WSSecurityException {
        // Store the aliases found
        Vector aliases = new Vector();
        Certificate cert = null;
        
        try {
            for (Enumeration e = store.aliases(); e.hasMoreElements();) {
                String alias = (String) e.nextElement();

                Certificate[] certs = store.getCertificateChain(alias);
                if (certs == null || certs.length == 0) {
                    // no cert chain, so lets check if getCertificate gives us a  result.
                    cert = store.getCertificate(alias);
                    if (cert == null) {
                        continue;
                    }
                    certs = new Certificate[]{cert};
                } else {
                    cert = certs[0];
                }
                if (cert instanceof X509Certificate) {
                    X500Principal foundRDN = ((X509Certificate) cert).getSubjectX500Principal();
                    Object certName = createBCX509Name(foundRDN.getName());

                    if (subjectRDN.equals(certName)) {
                        aliases.add(alias);
                    }
                }
            }
        } catch (KeyStoreException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, "keystore", null, e
            );
        }
        return aliases;
    }
}
