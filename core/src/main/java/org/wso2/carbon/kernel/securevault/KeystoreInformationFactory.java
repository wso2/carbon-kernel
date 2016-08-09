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

import java.util.Map;

/**
 * Factory for creating KeyStoreInformation based on properties
 *
 * @since 5.2.0
 */
public class KeystoreInformationFactory {

    private static final Logger logger = LoggerFactory.getLogger(FileBaseSecretRepository.class);

    /**
     * Creates a KeyStoreInformation using synapse properties
     * Uses KeyStore configuration properties
     *
     * @param keyStoreConfig keystore configuration
     * @return IdentityKeyStoreInformation instance
     */
    public static KeyStoreInformation createIdentityKeyStoreInformation(Map<String, Object> keyStoreConfig) {

        KeyStoreInformation keyStoreInformation = new KeyStoreInformation();
        String keyStoreLocation = keyStoreConfig.get("location").toString(); //todo check if the values are null
//        if (keyStoreLocation == null || "".equals(keyStoreLocation)) {
//            if (logger.isDebugEnabled()) {
//                logger.debug("Cannot find a KeyStoreLocation for private key store");
//            }
//            return null;
//        }
        keyStoreInformation.setAlias(keyStoreConfig.get("alias").toString());
        keyStoreInformation.setLocation(keyStoreLocation);
        keyStoreInformation.setStoreType(KeyStoreType.valueOf(keyStoreConfig.get("type").toString()));

        return keyStoreInformation;
    }

}
