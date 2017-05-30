/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.context.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.user.api.UserRealmService;

/**
 * TODO: class description
 */
public class OSGiDataHolder {
    private static final Log log = LogFactory.getLog(OSGiDataHolder.class);
    private static OSGiDataHolder instance = new OSGiDataHolder();

    private BundleContext bundleContext;
    private RegistryService registryService;
    private ServerConfigurationService serverConfigurationService;
    private UserRealmService userRealmService;

    public static OSGiDataHolder getInstance() {
        return instance;
    }

    private OSGiDataHolder() {
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public UserRealmService getUserRealmService() {
        return userRealmService;
    }

    public void setUserRealmService(UserRealmService userRealmService) {
        this.userRealmService = userRealmService;
    }

    public RegistryService getRegistryService() throws Exception {
        if (registryService == null) {
            String msg = "Before activating Carbon Core bundle, an instance of "
                         + "RegistryService should be in existence";
            log.error(msg);
            throw new Exception(msg);
        }
        return registryService;
    }

    public ServerConfigurationService getServerConfigurationService() {
        if (this.serverConfigurationService == null) {
            String msg = "Before activating Carbon Core bundle, an instance of "
                         + "ServerConfigurationService should be in existence";
            log.error(msg);
        }
        return this.serverConfigurationService;
    }
}
