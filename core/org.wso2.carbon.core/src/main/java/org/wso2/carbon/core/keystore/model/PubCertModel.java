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

/**
 * Model class for Public Certificate.
 */
public class PubCertModel {

    private String fileNameAppender;

    private byte[] content;

    public PubCertModel() {
        // Empty constructor for default initialization
    }

    public String getFileNameAppender() {

        return fileNameAppender;
    }

    public void setFileNameAppender(String fileNameAppender) {

        this.fileNameAppender = fileNameAppender;
    }

    public byte[] getContent() {

        return content;
    }

    public void setContent(byte[] content) {

        this.content = content;
    }
}
