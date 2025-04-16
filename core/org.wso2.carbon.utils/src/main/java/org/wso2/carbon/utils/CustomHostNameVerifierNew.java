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
import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.psl.DomainType;
import org.apache.hc.client5.http.psl.PublicSuffixMatcher;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.DistinguishedNameParser;
import org.apache.hc.client5.http.ssl.HttpClientHostnameVerifier;
import org.apache.hc.client5.http.ssl.SubjectName;
import org.apache.hc.client5.http.utils.DnsUtils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.InetAddressUtils;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.apache.hc.core5.util.TextUtils;
import org.apache.hc.core5.util.Tokenizer;

import java.beans.PropertyEditorSupport;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;

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

        String[] subjectAlternativeNames = getSubjectAltNames(cert);
        String[] subjectAltsWithLocalhosts = (String[]) ArrayUtils.addAll(subjectAlternativeNames, LOCALHOSTS);

        boolean hasValidCommonNames = Optional.ofNullable(commonNames)
                .filter(names -> names.length > 0)
                .map(names -> names[0])
                .isPresent();
        if (hasValidCommonNames && !ArrayUtils.contains(subjectAlternativeNames, commonNames[0])) {
            subjectAltsWithLocalhosts = (String[]) ArrayUtils.add(subjectAltsWithLocalhosts, commonNames[0]);
        }
        super.verify(hostname, commonNames, subjectAltsWithLocalhosts, false);
    }

    private static List<SubjectName> getSubjectAltNames(X509Certificate cert, int subjectName) {
        try {
            Collection<List<?>> entries = cert.getSubjectAlternativeNames();
            if (entries == null) {
                return Collections.emptyList();
            } else {
                List<SubjectName> result = new ArrayList<>();

                for (List<?> entry : entries) {
                    Integer type = entry.size() >= 2 ? (Integer) entry.get(0) : null;
                    if (type != null && (type == subjectName || -1 == subjectName)) {
                        Object o = entry.get(1);
                        if (o instanceof String) {
                            result.add(new SubjectName((String) o, type));
                        }
                    }
                }

                return result;
            }
        } catch (CertificateParsingException var8) {
            return Collections.emptyList();
        }

//        try {
//            Collection<List<?>> entries = cert.getSubjectAlternativeNames();
//            if (entries == null) {
//                return null;
//            } else {
//                ArrayList<String> subjectAltNames = new ArrayList<>();
//
//                for (List<?> entry : entries) {
//                    Integer type = entry.size() >= 2 ? (Integer) entry.get(0) : null;
//                    if (type != null) {
//                        Object o = entry.get(1);
//                        if (o instanceof String) {
//                            subjectAltNames.add((String) o);
//                        }
//                    }
//                }
//
//                return subjectAltNames.toArray(new String[0]);
//            }
//        } catch (CertificateParsingException e) {
//            return null;
//        }
    }

    static void matchIPAddress(String host, List<SubjectName> subjectAlts) throws SSLPeerUnverifiedException {
        for (SubjectName subjectAlt : subjectAlts) {
            if (subjectAlt.getType() == 7 && host.equals(subjectAlt.getValue())) {
                return;
            }
        }

        throw new SSLPeerUnverifiedException("Certificate for <" + host + "> doesn't match any of the subject alternative names: " + subjectAlts);
    }

    static void matchIPv6Address(String host, List<SubjectName> subjectAlts) throws SSLPeerUnverifiedException {
        String normalisedHost = normaliseAddress(host);

        for (SubjectName subjectAlt : subjectAlts) {
            if (subjectAlt.getType() == 7) {
                String normalizedSubjectAlt = normaliseAddress(subjectAlt.getValue());
                if (normalisedHost.equals(normalizedSubjectAlt)) {
                    return;
                }
            }
        }

        throw new SSLPeerUnverifiedException("Certificate for <" + host + "> doesn't match any of the subject alternative names: " + subjectAlts);
    }

    static void matchDNSName(String host, List<SubjectName> subjectAlts, PublicSuffixMatcher publicSuffixMatcher) throws SSLPeerUnverifiedException {
        String normalizedHost = DnsUtils.normalize(host);

        for (SubjectName subjectAlt : subjectAlts) {
            if (subjectAlt.getType() == 2) {
                String normalizedSubjectAlt = DnsUtils.normalize(subjectAlt.getValue());
                if (matchIdentityStrict(normalizedHost, normalizedSubjectAlt, publicSuffixMatcher)) {
                    return;
                }
            }
        }

        throw new SSLPeerUnverifiedException("Certificate for <" + host + "> doesn't match any of the subject alternative names: " + subjectAlts);
    }

    static void matchCN(String host, X509Certificate cert, PublicSuffixMatcher publicSuffixMatcher) throws SSLException {
        X500Principal subjectPrincipal = cert.getSubjectX500Principal();
        String cn = extractCN(subjectPrincipal.getName("RFC2253"));
        if (cn == null) {
            throw new SSLPeerUnverifiedException("Certificate subject for <" + host + "> doesn't contain a common name and does not have alternative names");
        } else {
            String normalizedHost = DnsUtils.normalize(host);
            String normalizedCn = DnsUtils.normalize(cn);
            if (!matchIdentityStrict(normalizedHost, normalizedCn, publicSuffixMatcher)) {
                throw new SSLPeerUnverifiedException("Certificate for <" + host + "> doesn't match common name of the certificate subject: " + cn);
            }
        }
    }

    private static boolean matchIdentityStrict(String host, String identity, PublicSuffixMatcher publicSuffixMatcher) {
        if (publicSuffixMatcher != null && host.contains(".") &&
                publicSuffixMatcher.getDomainRoot(identity, null) == null) {
            return false;
        } else {
            int asteriskIdx = identity.indexOf(42);
            if (asteriskIdx != -1) {
                String prefix = identity.substring(0, asteriskIdx);
                String suffix = identity.substring(asteriskIdx + 1);
                if (!prefix.isEmpty() && !host.startsWith(prefix)) {
                    return false;
                } else if (!suffix.isEmpty() && !host.endsWith(suffix)) {
                    return false;
                } else {
                    String remainder = host.substring(prefix.length(), host.length() - suffix.length());
                    return !remainder.contains(".");
                }
            } else {
                return host.equalsIgnoreCase(identity);
            }
        }
    }

    static String extractCN(String subjectPrincipal) throws SSLException {
        if (subjectPrincipal == null) {
            return null;
        } else {
            for (NameValuePair attribute : DistinguishedNameParser.INSTANCE.parse(subjectPrincipal)) {
                if (TextUtils.isBlank(attribute.getName()) || attribute.getValue() == null) {
                    throw new SSLException(subjectPrincipal + " is not a valid X500 distinguished name");
                }

                if (attribute.getName().equalsIgnoreCase("cn")) {
                    return attribute.getValue();
                }
            }

            return null;
        }
    }

    static HostNameType determineHostFormat(String host) {
        if (InetAddressUtils.isIPv4(host)) {
            return HostNameType.IPv4;
        } else {
            String s = host;
            if (host.startsWith("[") && host.endsWith("]")) {
                s = host.substring(1, host.length() - 1);
            }

            return InetAddressUtils.isIPv6(s) ? HostNameType.IPv6 : HostNameType.DNS;
        }
    }

    static String normaliseAddress(String hostname) {
        if (hostname == null) {
            return hostname;
        } else {
            try {
                InetAddress inetAddress = InetAddress.getByName(hostname);
                return inetAddress.getHostAddress();
            } catch (UnknownHostException var2) {
                return hostname;
            }
        }
    }

    List<NameValuePair> parse(CharArrayBuffer buf, Tokenizer.Cursor cursor) {
        List<NameValuePair> params = new ArrayList();
        this.tokenParser.skipWhiteSpace(buf, cursor);

        while(!cursor.atEnd()) {
            NameValuePair param = this.parseParameter(buf, cursor);
            params.add(param);
        }

        return params;
    }

    List<NameValuePair> parse(String s) {
        if (s == null) {
            return null;
        } else {
            CharArrayBuffer buffer = new CharArrayBuffer(s.length());
            buffer.append(s);
            Tokenizer.Cursor cursor = new Tokenizer.Cursor(0, s.length());
            return this.parse(buffer, cursor);
        }
    }

    enum HostNameType {
        IPv4(7),
        IPv6(7),
        DNS(2);

        final int subjectType;

        private HostNameType(int subjectType) {
            this.subjectType = subjectType;
        }
    }

    static class SubjectName {
        static final int DNS = 2;
        static final int IP = 7;
        private final String value;
        private final int type;

        SubjectName(String value, int type) {
            this.value = Args.notNull(value, "Value");
            this.type = Args.positive(type, "Type");
        }

        public int getType() {
            return this.type;
        }

        public String getValue() {
            return this.value;
        }

        public String toString() {
            return this.value;
        }
    }
}
