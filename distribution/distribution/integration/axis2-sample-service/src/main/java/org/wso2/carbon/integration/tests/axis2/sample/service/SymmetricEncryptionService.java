/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.integration.tests.axis2.sample.service;

import org.apache.axiom.om.util.Base64;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

public class SymmetricEncryptionService {

    public String encrypt(String plainText) throws CryptoException {
        CryptoUtil cryptoUtilInstance = CryptoUtil.getDefaultCryptoUtil();
        return Base64.encode(cryptoUtilInstance.encrypt(plainText.getBytes()));
    }

    public String decrypt(String encryptedText) throws CryptoException {
        CryptoUtil cryptoUtilInstance = CryptoUtil.getDefaultCryptoUtil();
        return new String(cryptoUtilInstance.decrypt(Base64.decode(encryptedText)));
    }
}
