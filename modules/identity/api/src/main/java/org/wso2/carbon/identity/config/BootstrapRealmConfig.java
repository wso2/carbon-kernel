/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.config;

import java.util.Properties;

public final class BootstrapRealmConfig {

    private IdentityManagerConfig identityManagerConfig;
    private AuthorizationManagerConfig authzManagerConfig;
    private String realmClazz;
    private Properties properties;

    /**
     * @param primaryIdentityStoreConfig
     * @param authzManagerConfig
     * @param properties
     */
    public BootstrapRealmConfig(String realmClazz, IdentityManagerConfig identityManagerConfig,
                                AuthorizationManagerConfig authzManagerConfig, Properties properties) {
        this.realmClazz = realmClazz;
        this.identityManagerConfig = identityManagerConfig;
        this.authzManagerConfig = authzManagerConfig;
        this.properties = properties;
    }

    /**
     * @return
     */
    public IdentityManagerConfig getIdentityManagerConfig() {
        return identityManagerConfig;
    }

    /**
     * @return
     */
    public AuthorizationManagerConfig getAuthzManagerConfig() {
        return authzManagerConfig;
    }

    /**
     * @return
     */
    public String getRealmClazz() {
        return realmClazz;
    }

    /**
     * @return
     */
    public Properties getProperties() {
        return properties;
    }

}
