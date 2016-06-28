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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the cipher related information
 *
 * @since 5.2.0
 */

public class CipherInformation {

    private static final Logger log = LoggerFactory.getLogger(CipherInformation.class);

    /* Cipher algorithm */
    private String algorithm;

    /* Cipher operation mode - ENCRYPT or DECRYPT */
    private CipherOperationMode cipherOperationMode;

    public static Logger getLog() {
        return log;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public CipherOperationMode getCipherOperationMode() {
        return cipherOperationMode;
    }

    public void setCipherOperationMode(CipherOperationMode cipherOperationMode) {
        this.cipherOperationMode = cipherOperationMode;
    }
}
