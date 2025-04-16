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

import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.HttpClientHostnameVerifier;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * Custom hostname verifier class.
 */
public class CustomHostNameVerifierNew implements HttpClientHostnameVerifier {

    private static final String[] LOCALHOSTS = {"::1", "127.0.0.1", "localhost", "localhost.localdomain"};

    private final DefaultHostnameVerifier hostnameVerifier;

    public CustomHostNameVerifierNew() {

        this.hostnameVerifier = new DefaultHostnameVerifier();
    }

    @Override
    public boolean verify(String host, SSLSession session) {

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

        if (!Arrays.asList(LOCALHOSTS).contains(host)) {
            hostnameVerifier.verify(host, cert);
        }
    }


}
