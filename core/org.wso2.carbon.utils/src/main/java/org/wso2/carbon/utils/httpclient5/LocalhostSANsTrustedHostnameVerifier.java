/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.utils.httpclient5;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.HttpClientHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;

/**
 * Custom hostname verifier class with Apache Http Client 5.
 */
public class LocalhostSANsTrustedHostnameVerifier implements HttpClientHostnameVerifier {

    private static final LocalhostSANsTrustedHostnameVerifier INSTANCE = new LocalhostSANsTrustedHostnameVerifier();
    private static final DefaultHostnameVerifier DEFAULT_HOSTNAME_VERIFIER = new DefaultHostnameVerifier();

    private static final Logger LOG = LoggerFactory.getLogger(LocalhostSANsTrustedHostnameVerifier.class);

    private static final String[] LOCALHOST_SANS = {"::1", "127.0.0.1", "localhost", "localhost.localdomain"};

    private LocalhostSANsTrustedHostnameVerifier() {

    }

    public static LocalhostSANsTrustedHostnameVerifier getInstance() {

        return INSTANCE;
    }

    @Override
    public boolean verify(String host, SSLSession session) {

        try {
            Certificate[] certs = session.getPeerCertificates();
            X509Certificate x509 = (X509Certificate) certs[0];
            this.verify(host, x509);
            return true;
        } catch (SSLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage(), e);
            }
            return false;
        }
    }
    
    @Override
    public void verify(String host, X509Certificate cert) throws SSLException {
        
        try {
            // Extract subject alternative names.
            String[] subjectAlternativeNames = extractSubjectAlternativeNames(cert);
            
            // Extract CN from subject.
            String[] commonNames = extractCommonNames(cert);
            
            // Merge subject alternative names with localhost alternatives.
            String[] subjectAlternativeNamesWithLocalhosts =
                    (String[]) ArrayUtils.addAll(subjectAlternativeNames, LOCALHOST_SANS);

            if (commonNames.length > 0 && !ArrayUtils.contains(subjectAlternativeNames, commonNames[0])) {
                subjectAlternativeNamesWithLocalhosts =
                        (String[]) ArrayUtils.add(subjectAlternativeNamesWithLocalhosts, commonNames[0]);
            }
            
            // Check if the host matches any of our accepted names.
            if (ArrayUtils.contains(subjectAlternativeNamesWithLocalhosts, host)) {
                return;
            }
            
            // If not in the extended list, use the default verifier.
            DEFAULT_HOSTNAME_VERIFIER.verify(host, cert);
        } catch (CertificateParsingException e) {
            throw new SSLException("Certificate parsing error", e);
        }
    }

    private String[] extractSubjectAlternativeNames(X509Certificate cert) throws CertificateParsingException {

        // getSubjectAlternativeNames returns a collection of SANs, where each SAN is represented as list with two
        // elements. The 0th element of the list contains the type of the SAN as an integer and 1st element contains
        // the SAN value as a String or byte[].
        Collection<List<?>> subjectAltNames = cert.getSubjectAlternativeNames();

        List<String> result = new ArrayList<>();
        
        if (subjectAltNames != null) {
            // Iterate through each element of the collection of SANs.
            for (List<?> san : subjectAltNames) {
                // Check if the SAN is a pair of objects, i.e., it has two elements.
                if (san != null && san.size() >= 2) {
                    Object typeObj = san.get(0);
                    // Check object type for safety, and it is expected to be Integer.
                    if (typeObj instanceof Integer) {
                        Integer type = (Integer) san.get(0);
                        // DNS names are type 2, IP addresses are type 7.
                        if (type.equals(2) || type.equals(7)) {
                            Object valueObj = san.get(1);
                            // Check object type for safety, and it is expected to be String or byte[].
                            if (valueObj instanceof String) {
                                result.add((String) valueObj);
                            } else if (valueObj instanceof byte[]) {
                                result.add(new String((byte []) valueObj));
                            }
                        }
                    }
                }
            }
        }
        return result.toArray(new String[0]);
    }

    private String[] extractCommonNames(X509Certificate cert) {

        X500Principal principal = cert.getSubjectX500Principal();
        String distinguishedNames = principal.getName(X500Principal.RFC2253);

        if (distinguishedNames != null && !distinguishedNames.isEmpty()) {
            // Split distinguishedNames string by commas, to get the part containing the common names.
            for (String part : distinguishedNames.split(",")) {
                // Check if the part starts with "CN=" representing common name.
                if (part.toLowerCase().startsWith("cn=")) {
                    // Omit the "CN=" prefix (first three characters) and trim the value.
                    return new String[] {part.substring(3).trim()};
                }
            }
        }

        return new String[0];
    }
}
