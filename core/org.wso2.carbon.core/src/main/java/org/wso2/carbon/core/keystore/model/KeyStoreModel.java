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

package org.wso2.carbon.core.keystore.model;

import java.util.Date;

/**
 * Model class for KeyStore.
 */
public class KeyStoreModel {

    // TODO: check whether we need the uuid.
    private final String id;
    private final String fileName;
    private final String type;
    private final String provider;
    private final char[] password;
    private final String privateKeyAlias;
    private final char[] privateKeyPass;
    private final Date lastUpdated;
    private final byte[] content;

    public KeyStoreModel(KeyStoreModelBuilder  builder) {

        this.id = builder.id;
        this.fileName = builder.fileName;
        this.type = builder.type;
        this.provider = builder.provider;
        this.password = builder.password;
        this.privateKeyAlias = builder.privateKeyAlias;
        this.privateKeyPass = builder.privateKeyPass;
        this.lastUpdated = builder.lastUpdated;
        this.content = builder.content;
    }

    public String getId() {

        return id;
    }

    public String getFileName() {

        return fileName;
    }

    public String getType() {

        return type;
    }

    public String getProvider() {

        return provider;
    }

    public char[] getPassword() {

        return password;
    }

    public String getPrivateKeyAlias() {

        return privateKeyAlias;
    }

    public char[] getPrivateKeyPass() {

        return privateKeyPass;
    }

    public byte[] getContent() {

        return content;
    }

    public Date getLastUpdated() {

        return lastUpdated;
    }

    public static class KeyStoreModelBuilder {

        private String id;
        private String fileName;
        private String type;
        private String provider;
        private char[] password;
        private String privateKeyAlias;
        private char[] privateKeyPass;
        private Date lastUpdated;
        private byte[] content;

        public KeyStoreModelBuilder() {
            // Default constructor.
        }

        public KeyStoreModelBuilder id(String id) {

            this.id = id;
            return this;
        }

        public KeyStoreModelBuilder fileName(String fileName) {

            this.fileName = fileName;
            return this;
        }

        public KeyStoreModelBuilder type(String type) {

            this.type = type;
            return this;
        }

        public KeyStoreModelBuilder provider(String provider) {

            this.provider = provider;
            return this;
        }

        public KeyStoreModelBuilder password(char[] password) {

            this.password = password;
            return this;
        }

        public KeyStoreModelBuilder privateKeyAlias(String privateKeyAlias) {

            this.privateKeyAlias = privateKeyAlias;
            return this;
        }

        public KeyStoreModelBuilder privateKeyPass(char[] privateKeyPass) {

            this.privateKeyPass = privateKeyPass;
            return this;
        }

        public KeyStoreModelBuilder lastUpdated(Date lastUpdated) {

            this.lastUpdated = lastUpdated;
            return this;
        }

        public KeyStoreModelBuilder content(byte[] content) {

            this.content = content;
            return this;
        }

        public KeyStoreModel build() {

            return new KeyStoreModel(this);
        }
    }
}
