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

import java.security.KeyStore;

/**
 * Created by nipuni on 6/7/16.   //todo
 */
public class KeyStoreInformation {

    private static final Logger logger = LoggerFactory.getLogger(KeyStoreInformation.class);
    /* KeyStore type */
    private KeyStoreType storeType;
    /* Alias who belong this key */
    private String alias;
    /* KeyStore location */
    private String location;

    public KeyStoreType getStoreType() {
        return storeType;
    }

    public void setStoreType(KeyStoreType storeType) {
        this.storeType = storeType;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Constructs the KeyStore according to the store type
     *
     * @return KeyStore Instance
     */
    protected KeyStore getKeyStore() {

        if (logger.isDebugEnabled()) {
            logger.debug("Loading KeyStore with type : " + getStoreType());
        }
        String keyStorePassword = "wso2carbon";  //todo this need to be accessed via a provider
        switch (storeType) {
            case JKS:
                KeyStoreLoader jksKeyStoreLoader = new KeyStoreLoader();
                return jksKeyStoreLoader.getKeyStore(getLocation(), keyStorePassword, getStoreType().toString(), null);
            //todo support other keystore types
            default:
                if (logger.isDebugEnabled()) {
                    logger.debug("No KeyStore Found");
                }
                return null;
        }
    }

}
