/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.server.admin.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * This singleton data holder contains all the data required by the server admin OSGi bundle
 */
public class ServerAdminDataHolder {
    private static ServerAdminDataHolder instance = new ServerAdminDataHolder();

    private String registryDBDriver;
    private String userManagerDBDriver;
    private ServerConfigurationService serverConfig;
    private RegistryService registryService;
    private RealmService realmService;
    private ConfigurationContext configContext;

    /**
     * The classloader which should be set in the TCCL before restarting the server
     */
    public ClassLoader restartThreadContextClassloader;

    public static ServerAdminDataHolder getInstance() {
        return instance;
    }

    private ServerAdminDataHolder() {
    }

    public String getRegistryDBDriver() {
        return registryDBDriver;
    }

    public void setRegistryDBDriver(String registryDBDriver) {
        this.registryDBDriver = registryDBDriver;
    }

    public String getUserManagerDBDriver() {
        return userManagerDBDriver;
    }

    public void setUserManagerDBDriver(String userManagerDBDriver) {
        this.userManagerDBDriver = userManagerDBDriver;
    }

    public ServerConfigurationService getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfigurationService serverConfig) {
        this.serverConfig = serverConfig;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public ConfigurationContext getConfigContext() {
        return configContext;
    }

    public void setConfigContext(ConfigurationContext configContext) {
        this.configContext = configContext;
    }

    public ClassLoader getRestartThreadContextClassloader() {
        return restartThreadContextClassloader;
    }

    public void setRestartThreadContextClassloader(ClassLoader restartThreadContextClassloader) {
        this.restartThreadContextClassloader = restartThreadContextClassloader;
    }
}
