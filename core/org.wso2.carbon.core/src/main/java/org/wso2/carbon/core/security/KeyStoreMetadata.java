/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.core.security;

/**
 * This class holds the metadata of a keystore.
 */
public class KeyStoreMetadata {

    private String keyStoreName;
    private String keyStoreType;
    private String provider;
    private boolean isPrivateStore;
    private byte[] publicCert;
    private String publicCertName;

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

    public boolean isPrivateStore() {

        return isPrivateStore;
    }

    public void setPrivateStore(boolean privateStore) {

        isPrivateStore = privateStore;
    }

    public byte[] getPublicCert() {

        return publicCert;
    }

    public void setPublicCert(byte[] publicCert) {

        this.publicCert = publicCert;
    }

    public String getPublicCertName() {

        return publicCertName;
    }

    public void setPublicCertName(String publicCertName) {

        this.publicCertName = publicCertName;
    }
}
