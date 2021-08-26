/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.user.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.crypto.api.CryptoService;
import org.wso2.carbon.user.core.hash.HashProviderFactory;
import org.wso2.carbon.user.core.listener.GroupResolver;

import java.util.HashMap;
import java.util.Map;

public class UserStoreMgtDataHolder {

    private static UserStoreMgtDataHolder instance = new UserStoreMgtDataHolder();
    private static final Log log = LogFactory.getLog(UserStoreMgtDataHolder.class);
    private CryptoService cryptoService;
    private Map<String, HashProviderFactory> hashProviderFactoryMap;
    private GroupResolver groupResolver = null;

    /**
     * Get GroupResolver instance.
     *
     * @return GroupResolver instance.
     */
    public GroupResolver getGroupResolver() {

        return groupResolver;
    }

    /**
     * Set GroupResolver instance.
     *
     * @param groupResolver GroupResolver instance.
     */
    public void setGroupResolver(GroupResolver groupResolver) {

        this.groupResolver = groupResolver;
    }

    public static UserStoreMgtDataHolder getInstance() {

        return instance;
    }

    private UserStoreMgtDataHolder() {

    }

    public void setCryptoService(CryptoService cryptoService) {

        this.cryptoService = cryptoService;
    }

    public CryptoService getCryptoService() {

        return cryptoService;
    }

    /**
     * Set each HashProviderFactory to the HashProviderFactory collection.
     *
     * @param hashProviderFactory Instance of HashProviderFactory.
     */
    public void setHashProviderFactory(HashProviderFactory hashProviderFactory) {

        if (hashProviderFactoryMap == null) {
            hashProviderFactoryMap = new HashMap<>();
        }
        hashProviderFactoryMap.put(hashProviderFactory.getAlgorithm(), hashProviderFactory);
    }

    /**
     * Get the HashProviderFactory from HashProviderFactory collection.
     *
     * @param algorithm Algorithm name for respective instance of HashProviderFactory.
     * @return The HashProviderFactory instance which has the given algorithm as the type.
     * The method will return NULL if there were no matching HashProviderFactory to the given algorithm.
     */
    public HashProviderFactory getHashProviderFactory(String algorithm) {

        if (hashProviderFactoryMap == null) {
            return null;
        }
        return hashProviderFactoryMap.get(algorithm);
    }

    /**
     * Remove HashProviderFactory from HashProviderFactory collection.
     *
     * @param hashProviderFactory Instance of HashProviderFactory.
     */
    public void unbindHashProviderFactory(HashProviderFactory hashProviderFactory) {

        hashProviderFactoryMap.remove(hashProviderFactory.getAlgorithm());
    }
}
