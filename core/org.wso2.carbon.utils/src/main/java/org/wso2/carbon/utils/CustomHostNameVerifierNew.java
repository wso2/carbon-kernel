/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
import org.apache.hc.client5.http.ssl.HttpClientHostnameVerifier;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * Custom hostname verifier class.
 */
public class CustomHostNameVerifierNew implements HttpClientHostnameVerifier {

    private final static String[] LOCALHOSTS = {"::1", "127.0.0.1", "localhost", "localhost.localdomain"};

    @Override
    public boolean verify(String hostname, SSLSession session) {
        try {
            X509Certificate cert = (X509Certificate) session.getPeerCertificates()[0];
            verify(hostname, cert);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public void verify(String hostname, X509Certificate cert) throws SSLException {
        try {
            String[] commonNames = extractCommonNames(cert);
            String[] subjectAlternativeNames = extractSubjectAltNames(cert);
            
            // Add localhost addresses to subject alternative names
            String[] subjectAltsWithLocalhosts = (String[]) ArrayUtils.addAll(subjectAlternativeNames, LOCALHOSTS);
            
            // Add first common name if it exists and is not already in the list
            boolean hasValidCommonNames = Optional.ofNullable(commonNames)
                    .filter(names -> names.length > 0)
                    .map(names -> names[0])
                    .isPresent();
            if (hasValidCommonNames && !ArrayUtils.contains(subjectAlternativeNames, commonNames[0])) {
                subjectAltsWithLocalhosts = (String[]) ArrayUtils.add(subjectAltsWithLocalhosts, commonNames[0]);
            }
            
            // Verify hostname against the enhanced list
            if (!verifyHostname(hostname, commonNames, subjectAltsWithLocalhosts)) {
                throw new SSLException("Hostname verification failed for: " + hostname);
            }
        } catch (SSLException e) {
            throw e;
        } catch (Exception e) {
            throw new SSLException("Error during hostname verification", e);
        }
    }
    
    private boolean verifyHostname(String hostname, String[] commonNames, String[] subjectAltNames) {
        // Check if the hostname is in the list of valid names
        if (commonNames != null) {
            for (String cn : commonNames) {
                if (hostname.equalsIgnoreCase(cn)) {
                    return true;
                }
            }
        }
        
        if (subjectAltNames != null) {
            for (String san : subjectAltNames) {
                if (hostname.equalsIgnoreCase(san)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private String[] extractCommonNames(X509Certificate cert) throws InvalidNameException {
        String subjectDN = cert.getSubjectX500Principal().getName();
        LdapName ldapName = new LdapName(subjectDN);
        
        for (Rdn rdn : ldapName.getRdns()) {
            if ("CN".equalsIgnoreCase(rdn.getType())) {
                return new String[] { rdn.getValue().toString() };
            }
        }
        
        return new String[0];
    }
    
    private String[] extractSubjectAltNames(X509Certificate cert) throws CertificateParsingException {
        List<String> result = new ArrayList<>();
        Collection<List<?>> altNames = cert.getSubjectAlternativeNames();
        
        if (altNames != null) {
            for (List<?> altName : altNames) {
                if (altName != null && altName.size() >= 2) {
                    // Type 2 is DNS name
                    if ((Integer) altName.get(0) == 2) {
                        result.add((String) altName.get(1));
                    }
                }
            }
        }
        
        return result.toArray(new String[0]);
    }
}
