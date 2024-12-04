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

package org.wso2.carbon.core.keystore.persistence.model;

public class KeyStoreModel {

    String name;
    String type;
    String provider;
    String encryptedPassword;
    String privateKeyAlias;
    String encryptedPrivateKeyPass;
    byte[] content;
    int tenantId;
    String publicCertId;

    public KeyStoreModel() {}

    public KeyStoreModel(String name, String type, String provider, String encryptedPassword, String privateKeyAlias,
                         String encryptedPrivateKeyPass, byte[] content, int tenantId, String publicCertId) {
        this.name = name;
        this.type = type;
        this.provider = provider;
        this.encryptedPassword = encryptedPassword;
        this.privateKeyAlias = privateKeyAlias;
        this.encryptedPrivateKeyPass = encryptedPrivateKeyPass;
        this.content = content;
        this.tenantId = tenantId;
        this.publicCertId = publicCertId;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getPublicCertId() {

        return publicCertId;
    }

    public void setPublicCertId(String publicCertId) {

        this.publicCertId = publicCertId;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getProvider() {

        return provider;
    }

    public void setProvider(String provider) {

        this.provider = provider;
    }

    public int getTenantId() {

        return tenantId;
    }

    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    public byte[] getContent() {

        return content;
    }

    public void setContent(byte[] content) {

        this.content = content;
    }

    public String getEncryptedPassword() {

        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {

        this.encryptedPassword = encryptedPassword;
    }

    public String getPrivateKeyAlias() {

        return privateKeyAlias;
    }

    public void setPrivateKeyAlias(String privateKeyAlias) {

        this.privateKeyAlias = privateKeyAlias;
    }

    public String getEncryptedPrivateKeyPass() {

        return encryptedPrivateKeyPass;
    }

    public void setEncryptedPrivateKeyPass(String encryptedPrivateKeyPass) {

        this.encryptedPrivateKeyPass = encryptedPrivateKeyPass;
    }
}
