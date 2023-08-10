/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security.keystore.service;

public class KeyStoreData {

    private CertData[] certs;
    private CertData key;
    private String keyValue;
    private String keyStoreName = null;
    private String keyStoreType = null;
    private String provider = null;
    private String pubKeyFilePath = null;
    private boolean isPrivateStore = false;

    public String getKeyStoreName() {
        return keyStoreName;
    }

    public void setKeyStoreName(String keyStoreName) {
        this.keyStoreName = keyStoreName;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean getPrivateStore() {
        return isPrivateStore;
    }

    public void setPrivateStore(boolean isPrivateStore) {
        this.isPrivateStore = isPrivateStore;
    }

    public CertData[] getCerts() {
        return certs;
    }

    public void setCerts(CertData[] certs) {
        this.certs = certs;
    }

    public CertData getKey() {
        return key;
    }

    public void setKey(CertData key) {
        this.key = key;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getPubKeyFilePath() {
        return pubKeyFilePath;
    }

    public void setPubKeyFilePath(String pubKeyFilePath) {
        this.pubKeyFilePath = pubKeyFilePath;
    }
}
