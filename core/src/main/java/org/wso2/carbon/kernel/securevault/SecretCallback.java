/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.kernel.securevault;

/**
 * A single secret callback that can be used to collect a single secret
 */
public class SecretCallback {

    /* The secret  */
    private String secret;
    /* The prompt to be used whenever need to request the password */
    private String prompt;
    /* The Id to identify the context that secret is going to be used */
    private String id;

    public SecretCallback(String id) {
        this.id = id;
    }

    public SecretCallback(String prompt, String id) {
        this(id);
        this.prompt = prompt;
    }

    public SecretCallback() {
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return this.secret;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
