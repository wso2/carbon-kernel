/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.core.config;

import org.wso2.securevault.SecretResolver;

/**
 * This class contains configuration of a remote instance.
 */
public class RemoteConfiguration {

    private String id;
    private String url;
    private String trustedUser;
    private String trustedPwd;
    private String type;
    private SecretResolver secretResolver;

    private String dbConfig;
    private String readOnly;
    private String cacheEnabled;
    private String cacheId;
    private String registryRoot;

    public void setPasswordManager(SecretResolver secretResolver) {
        this.secretResolver = secretResolver;
    }



    /**
     * Method to obtain the instance identifier.
     *
     * @return the instance identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Method to set the instance identifier.
     *
     * @param id the instance identifier.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Method to obtain the connection URL.
     *
     * @return the connection URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Method to set the connection URL.
     *
     * @param url the connection URL.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Method to obtain the trusted username.
     *
     * @return the trusted username.
     */
    public String getTrustedUser() {
        return trustedUser;
    }

    /**
     * Method to set the trusted username.
     *
     * @param trustedUser the trusted username.
     */
    public void setTrustedUser(String trustedUser) {
        this.trustedUser = trustedUser;
    }

    /**
     * Method to obtain the type of remote registry.
     *
     * @return the type of remote registry.
     */
    public String getType() {
        return type;
    }

    /**
     * Method to set the type of remote registry.
     *
     * @param type the type of remote registry.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Method to obtain the trusted user's password.
     *
     * @return the trusted user's password.
     */
    public String getTrustedPwd() {
        return trustedPwd;
    }

    /**
     * Method to set the trusted user's password.
     *
     * @param trustedPwd the trusted user's password.
     */
    public void setTrustedPwd(String trustedPwd) {
        this.trustedPwd = trustedPwd;
    }

    /**
     * Method to obtain the database configuration.
     *
     * @return the database configuration.
     */
    public String getDbConfig() {
        return dbConfig;
    }

    /**
     * Method to set the database configuration.
     *
     * @param dbConfig the database configuration.
     */
    public void setDbConfig(String dbConfig) {
        this.dbConfig = dbConfig;
    }

    /**
     * Method to obtain whether the registry is read only or not.
     *
     * @return whether the registry is read only or not.
     */
    public String getReadOnly() {
        return readOnly;
    }

    /**
     * Method to set whether the registry is read only or not.
     *
     * @param readOnly whether the registry is read only or not.
     */
    public void setReadOnly(String readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Method to obtain whether caching is enabled or not.
     *
     * @return whether caching is enabled or not.
     */
    public String getCacheEnabled() {
        return cacheEnabled;
    }

    /**
     * Method to set whether caching is enabled or not.
     *
     * @param cacheEnabled whether caching is enabled or not.
     */
    public void setCacheEnabled(String cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    /**
     * Method to get the cache identifier
     *
     * @return the cache identifier
     */
    public String getCacheId() {
        return cacheId;
    }

    /**
     * Method to set the cache identifier.
     *
     * @param cacheId the identifier of the cache
     */
    public void setCacheId(String cacheId) {
        this.cacheId = cacheId;
    }

    /**
     * Method to obtain the root of the remote registry instance.
     *
     * @return the root of the remote registry instance.
     */
    public String getRegistryRoot() {
        return registryRoot;
    }

    /**
     * Method to set the root of the remote registry instance.
     *
     * @param registryRoot the root of the remote registry instance.
     */
    public void setRegistryRoot(String registryRoot) {
        this.registryRoot = registryRoot;
    }

    /**
     * If the password is protected , then decrypts the password and returns the plain text
     * Otherwise, returns the given password as-is
     *
     * @return Resolved password
     */
    public String getResolvedTrustedPwd() {
        if (secretResolver != null && secretResolver.isInitialized()){
            if (secretResolver.isTokenProtected("wso2registry." + id + ".password")) {
                return secretResolver.resolve("wso2registry." + id + ".password");
            } else {
                return trustedPwd;
            }
        } else {
            return trustedPwd;
        }
    }
}
