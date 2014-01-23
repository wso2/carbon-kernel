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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * JDK1.4 based implementation of Crypto (uses keystore). <p/>
 * 
 * @author Davanum Srinivas (dims@yahoo.com).
 */
public class Merlin extends AbstractCrypto {
   /**
    * OID For the NameConstraints Extension to X.509
    *
    * http://java.sun.com/j2se/1.4.2/docs/api/
    * http://www.ietf.org/rfc/rfc3280.txt (s. 4.2.1.11)
    */
    public static final String NAME_CONSTRAINTS_OID = "2.5.29.30";

    /**
     * Constructor. <p/>
     * 
     * @param properties
     * @throws CredentialException
     * @throws IOException
     */
    public Merlin(Properties properties) throws CredentialException, IOException {
        super(properties);
    }

    public Merlin(Properties properties, ClassLoader loader)
        throws CredentialException, IOException {
        super(properties, loader);
    }

    /**
     * Construct an array of X509Certificate's from the byte array. <p/>
     * 
     * @param data
     *            The <code>byte</code> array containing the X509 data
     * @param reverse
     *            If set the first certificate in input data will the last in
     *            the array
     * @return An array of X509 certificates, ordered according to the reverse
     *         flag
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
                WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "parseError", null, e
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
     * get a byte array given an array of X509 certificates. <p/>
     * 
     * @param reverse
     *            If set the first certificate in the array data will the last
     *            in the byte array
     * @param certs
     *            The certificates to convert
     * @return The byte array for the certificates ordered according to the
     *         reverse flag
     * @throws WSSecurityException
     */
    public byte[] getCertificateData(boolean reverse, X509Certificate[] certs)
        throws WSSecurityException {
        List list = new Vector();
        for (int i = 0; i < certs.length; i++) {
            if (reverse) {
                list.add(0, certs[i]);
            } else {
                list.add(certs[i]);
            }
        }
        try {
            CertPath path = getCertificateFactory().generateCertPath(list);
            return path.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new WSSecurityException(
                WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "encodeError", null, e
            );
        } catch (CertificateException e) {
            throw new WSSecurityException(
                WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "parseError", null, e
            );
        }
    }

    public boolean validateCertPath(X509Certificate[] certs) throws WSSecurityException {
        try {
            // Generate cert path
            java.util.List certList = java.util.Arrays.asList(certs);
            CertPath path = this.getCertificateFactory().generateCertPath(certList);

            java.util.Set set = new HashSet();
            if (this.cacerts != null) {
                Enumeration cacertsAliases = this.cacerts.aliases();
                while (cacertsAliases.hasMoreElements()) {
                    String alias = (String) cacertsAliases.nextElement();
                    X509Certificate cert = 
                        (X509Certificate) this.cacerts.getCertificate(alias);
                    TrustAnchor anchor = 
                        new TrustAnchor(cert, cert.getExtensionValue(NAME_CONSTRAINTS_OID));
                    set.add(anchor);
                }
            }

            // Add certificates from the keystore
            Enumeration aliases = this.keystore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                X509Certificate cert = 
                    (X509Certificate) this.keystore.getCertificate(alias);
                TrustAnchor anchor = 
                    new TrustAnchor(cert, cert.getExtensionValue(NAME_CONSTRAINTS_OID));
                set.add(anchor);
            }

            PKIXParameters param = new PKIXParameters(set);

            // Do not check a revocation list
            param.setRevocationEnabled(false);

            // Verify the trust path using the above settings
            String provider = 
                properties.getProperty("org.apache.ws.security.crypto.merlin.cert.provider");
            CertPathValidator certPathValidator;
            if (provider == null || provider.length() == 0) {
                certPathValidator = CertPathValidator.getInstance("PKIX");
            } else {
                certPathValidator = CertPathValidator.getInstance("PKIX", provider);
            }
            certPathValidator.validate(path, param);
        } catch (NoSuchProviderException ex) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "certpath", new Object[] { ex.getMessage() },
                    (Throwable) ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "certpath", new Object[] { ex.getMessage() },
                    (Throwable) ex);
        } catch (CertificateException ex) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "certpath", new Object[] { ex.getMessage() },
                    (Throwable) ex);
        } catch (InvalidAlgorithmParameterException ex) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "certpath", new Object[] { ex.getMessage() },
                    (Throwable) ex);
        } catch (CertPathValidatorException ex) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "certpath", new Object[] { ex.getMessage() },
                    (Throwable) ex);
        } catch (KeyStoreException ex) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "certpath", new Object[] { ex.getMessage() },
                    (Throwable) ex);
        }

        return true;
    }
}
