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
package org.wso2.carbon.utils;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.HttpClientHostnameVerifier;

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;

/**
 * Custom hostname verifier class written using Apache Http Client 5.
 */
public class Http5CustomHostNameVerifier implements HttpClientHostnameVerifier {

    private static final String[] LOCALHOSTS = {"::1", "127.0.0.1", "localhost", "localhost.localdomain"};
    private final DefaultHostnameVerifier hostnameVerifier;

    public Http5CustomHostNameVerifier() {
        this.hostnameVerifier = new DefaultHostnameVerifier();
    }

    @Override
    public boolean verify(String host, SSLSession session) {
        // For localhost verification, always return true
        if (Arrays.asList(LOCALHOSTS).contains(host)) {
            return true;
        } else {
            try {
                Certificate[] certs = session.getPeerCertificates();
                X509Certificate x509 = (X509Certificate) certs[0];
                this.verify(host, x509);
                return true;
            } catch (SSLException ex) {
                return false;
            }
        }
    }
    
    @Override
    public void verify(String host, X509Certificate cert) throws SSLException {
        // For localhost verification, always pass
        if (Arrays.asList(LOCALHOSTS).contains(host)) {
            return;
        }
        
        try {
            // Extract subject alternative names
            List<String> subjectAltNames = extractSubjectAltNames(cert);
            String[] subjectAlternativeNames = subjectAltNames.toArray(new String[0]);
            
            // Extract CN from subject
            String[] commonNames = extractCommonNames(cert);
            
            // Merge subject alternative names with localhosts
            String[] subjectAltsWithLocalhosts = (String[]) ArrayUtils.addAll(subjectAlternativeNames, LOCALHOSTS);
            
            boolean hasValidCommonNames = commonNames.length > 0;
            if (hasValidCommonNames && !ArrayUtils.contains(subjectAlternativeNames, commonNames[0])) {
                subjectAltsWithLocalhosts = (String[]) ArrayUtils.add(subjectAltsWithLocalhosts, commonNames[0]);
            }
            
            // Check if the host matches any of our accepted names
            if (ArrayUtils.contains(subjectAltsWithLocalhosts, host)) {
                return;
            }
            
            // If not in the extended list, use the default verifier
            hostnameVerifier.verify(host, cert);
        } catch (CertificateParsingException e) {
            throw new SSLException("Certificate parsing error", e);
        }
    }
    
    /**
     * Method similar to original class for API compatibility.
     */
    public void verify(String hostname, String[] commonNames, String[] subjectAlternativeNames) throws SSLException {
        String[] subjectAltsWithLocalhosts = (String[]) ArrayUtils.addAll(subjectAlternativeNames, LOCALHOSTS);
        
        boolean hasValidCommonNames = Optional.ofNullable(commonNames)
                .filter(names -> names.length > 0)
                .map(names -> names[0])
                .isPresent();
        if (hasValidCommonNames && !ArrayUtils.contains(subjectAlternativeNames, commonNames[0])) {
            subjectAltsWithLocalhosts = (String[]) ArrayUtils.add(subjectAltsWithLocalhosts, commonNames[0]);
        }
        
        if (!Arrays.asList(subjectAltsWithLocalhosts).contains(hostname) && 
            !Arrays.asList(LOCALHOSTS).contains(hostname)) {
            throw new SSLException("Hostname verification failed");
        }
    }

    private List<String> extractSubjectAltNames(X509Certificate cert) throws CertificateParsingException {
        Collection<List<?>> subjectAltNames = cert.getSubjectAlternativeNames();
        List<String> result = new ArrayList<>();
        
        if (subjectAltNames != null) {
            for (List<?> san : subjectAltNames) {
                if (san != null && san.size() >= 2) {
                    // DNS names are type 2, IP addresses are type 7
                    Integer type = (Integer) san.get(0);
                    if (type == 2 || type == 7) {
                        String value = (String) san.get(1);
                        result.add(value);
                    }
                }
            }
        }
        return result;
    }

    private String[] extractCommonNames(X509Certificate cert) {
        X500Principal principal = cert.getSubjectX500Principal();
        String dn = principal.getName(X500Principal.RFC2253);
        
        // Parse the DN to find CN
        for (String part : dn.split(",")) {
            if (part.toLowerCase().startsWith("cn=")) {
                return new String[] {part.substring(3).trim()};
            }
        }
        return new String[0];
    }
}
