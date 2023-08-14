/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.security.keystore.model;

import java.util.Date;

/**
 * Model class for KeyStore.
 */
public class KeyStoreModel {

    // TODO: check whether we need the uuid.
//    private String id;
    private String fileName;
    private String type;
    private String provider;
    private String password;
    private String privateKeyAlias;
    private String privateKeyPass;
    private Date lastUpdated;

    // TODO: check whether we can eliminate retrieving content when not needed.
    private byte[] content;

    public KeyStoreModel() {

        this(null, null, null, null, null);
    }

    public KeyStoreModel(String fileName, String type, String provider, String password, byte[] content) {

        this(fileName, type, provider, password, null, null, content);
    }

    public KeyStoreModel(String fileName, String type, String provider, String password,
                         String privateKeyAlias,
                         String privateKeyPass, byte[] content) {

        this.fileName = fileName;
        this.type = type;
        this.provider = provider;
        this.password = password;
        this.content = content;
        this.privateKeyAlias = privateKeyAlias;
        this.privateKeyPass = privateKeyPass;
    }

    public String getFileName() {

        return fileName;
    }

    public void setFileName(String fileName) {

        this.fileName = fileName;
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

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getPrivateKeyAlias() {

        return privateKeyAlias;
    }

    public void setPrivateKeyAlias(String privateKeyAlias) {

        this.privateKeyAlias = privateKeyAlias;
    }

    public String getPrivateKeyPass() {

        return privateKeyPass;
    }

    public void setPrivateKeyPass(String privateKeyPass) {

        this.privateKeyPass = privateKeyPass;
    }

    public byte[] getContent() {

        return content;
    }

    public void setContent(byte[] content) {

        this.content = content;
    }


    public Date getLastUpdated() {

        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {

        this.lastUpdated = lastUpdated;
    }
}
