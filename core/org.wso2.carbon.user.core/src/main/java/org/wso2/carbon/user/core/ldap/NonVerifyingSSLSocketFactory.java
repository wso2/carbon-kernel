/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.user.core.ldap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

public class NonVerifyingSSLSocketFactory extends SocketFactory {
    private static SocketFactory nonVerifyingSSLSocketFactory;

    static {
        TrustManager [] distrustManager = new TrustManager [] {new X509ExtendedTrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate [] chain, String authType, Socket socket) {

            }

            @Override
            public void checkServerTrusted(X509Certificate [] chain, String authType, Socket socket) {

            }

            @Override
            public void checkClientTrusted(X509Certificate [] chain, String authType, SSLEngine engine) {

            }

            @Override
            public void checkServerTrusted(X509Certificate [] chain, String authType, SSLEngine engine) {

            }

            public X509Certificate [] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate [] c, String a) {
            }

            public void checkServerTrusted(X509Certificate [] c, String a) {
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, distrustManager, new java.security.SecureRandom());
            nonVerifyingSSLSocketFactory = sc.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is called by the LDAP Context to create the connection
     *
     * @see SocketFactory#getDefault()
     */
    @SuppressWarnings ("unused")
    public static SocketFactory getDefault() {
        return new NonVerifyingSSLSocketFactory();
    }

    /**
     * @see SocketFactory#createSocket(String, int)
     */
    public Socket createSocket(String arg0, int arg1) throws IOException {
        return nonVerifyingSSLSocketFactory.createSocket(arg0, arg1);
    }

    /**
     * @see SocketFactory#createSocket(java.net.InetAddress, int)
     */
    public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
        return nonVerifyingSSLSocketFactory.createSocket(arg0, arg1);
    }

    /**
     * @see SocketFactory#createSocket(String, int, InetAddress, int)
     */
    public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
        return nonVerifyingSSLSocketFactory.createSocket(arg0, arg1, arg2, arg3);
    }

    /**
     * @see SocketFactory#createSocket(InetAddress, int, InetAddress, int)
     */
    public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
                                int arg3) throws IOException {
        return nonVerifyingSSLSocketFactory.createSocket(arg0, arg1, arg2, arg3);
    }

    /**
     * @see SocketFactory#createSocket()
     */
    public Socket createSocket() throws IOException {
        return nonVerifyingSSLSocketFactory.createSocket();
    }

}
