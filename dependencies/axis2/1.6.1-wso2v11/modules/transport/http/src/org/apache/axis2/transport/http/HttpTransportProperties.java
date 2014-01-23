/*
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

package org.apache.axis2.transport.http;

import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;

import java.util.List;
import java.util.Properties;

/**
 * Utility bean for setting transport properties in runtime.
 */
public class HttpTransportProperties {
    protected boolean chunked;
    protected HttpVersion httpVersion;
    protected String protocol;

    public HttpTransportProperties() {
    }

    public boolean getChunked() {
        return chunked;
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setChunked(boolean chunked) {
        this.chunked = chunked;
    }

    public void setHttpVersion(HttpVersion httpVerion) {
        this.httpVersion = httpVerion;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public static class ProxyProperties {
        protected int proxyPort = -1;
        protected String domain = null;
        protected String passWord = null;
        protected String proxyHostName = null;
        protected String userName = null;

        public ProxyProperties() {
        }

        public String getDomain() {
            return domain;
        }

        public String getPassWord() {
            return passWord;
        }

        public String getProxyHostName() {
            return proxyHostName;
        }

        public int getProxyPort() {
            return proxyPort;
        }

        public String getUserName() {
            return userName;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public void setPassWord(String passWord) {
            this.passWord = passWord;
        }

        public void setProxyName(String proxyHostName) {
            this.proxyHostName = proxyHostName;
        }

        public void setProxyPort(int proxyPort) {
            this.proxyPort = proxyPort;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }

    /*
    This class is responsible for holding all the necessary information needed for NTML, Digest
    and Basic Authentication. Authentication itself is handled by httpclient. User doesn't need to
    warry about what authentication mechanism it uses. Axis2 uses httpclinet's default authentication
    patterns.
    */
    public static class Authenticator {
        /*host that needed to be authenticated with*/
        private String host;
        /*port of the host that needed to be authenticated with*/
        private int port = AuthScope.ANY_PORT;
        /*Realm for authentication scope*/
        private String realm = AuthScope.ANY_REALM;
        /*Domain needed by NTCredentials for NT Domain*/
        private String domain;
        /*User for authenticate*/
        private String username;
        /*Password of the user for authenticate*/
        private String password;
        /* Switch to use preemptive authentication or not*/
        private boolean preemptive = false;
        /* if Authentication scheme needs retry just turn on the following flag */
        private boolean allowedRetry = false;
        /* Changing the priorty or adding a custom AuthPolicy*/
        private List authSchemes;

        /* Default Auth Schems*/
        public static final String NTLM = AuthPolicy.NTLM;
        public static final String DIGEST = AuthPolicy.DIGEST;
        public static final String BASIC = AuthPolicy.BASIC;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getRealm() {
            return realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setPreemptiveAuthentication(boolean preemptive) {
            this.preemptive = preemptive;
        }

        public boolean getPreemptiveAuthentication() {
            return this.preemptive;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public void setAuthSchemes(List authSchemes) {
            this.authSchemes = authSchemes;
        }

        public List getAuthSchemes() {
            return this.authSchemes;
        }

        public void setAllowedRetry(boolean allowedRetry) {
            this.allowedRetry = allowedRetry;
        }

        public boolean isAllowedRetry() {
            return this.allowedRetry;
        }
    }

    /**
     * @deprecated org.apache.axis2.transport.http.HttpTransportProperties.MailProperties has been
     * deprecated and user are encourage the use of java.util.Properties instead.  
     */
    public static class MailProperties {
        final Properties mailProperties = new Properties();

        private String password;

        public void addProperty(String key, String value) {
            mailProperties.put(key, value);
        }

        public void deleteProperty(String key) {
            mailProperties.remove(key);
        }

        public Properties getProperties() {
            return mailProperties;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }
}
