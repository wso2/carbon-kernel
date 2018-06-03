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
package org.wso2.carbon.core.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.ListenerManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.crypto.api.CryptoService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;

import org.wso2.carbon.core.clustering.api.CoordinatedActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * This singleton data holder contains all the data required by the Carbon core OSGi bundle
 */
public class CarbonCoreDataHolder {
    private  static CarbonCoreDataHolder instance = new CarbonCoreDataHolder();
    private  Log log = LogFactory.getLog(CarbonCoreDataHolder.class);

    private  BundleContext bundleContext;
    private  RealmService realmService;
    private  RegistryService registryService;
    private  HttpService httpService;
    private  ListenerManager listenerManager;
    private  ConfigurationContext mainServerConfigContext;
    private  ServerConfigurationService serverConfigurationService;
    private TenantRegistryLoader tenantRegistryLoader;

    private List<CoordinatedActivity> coordinatedActivities = new ArrayList<CoordinatedActivity>() ;
    private CryptoService cryptoService;

    public  static CarbonCoreDataHolder getInstance() {
        return instance;
    }

    private CarbonCoreDataHolder() {
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public  void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public  void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public  void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public  void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public  HttpService getHttpService() throws Exception {
        if (httpService == null) {
            String msg = "Before activating Carbon Core bundle, an instance of "
                    + HttpService.class.getName() + " should be in existance";
            log.error(msg);
            throw new Exception(msg);
        }
        return httpService;
    }

    public  RealmService getRealmService() throws Exception {
        if (realmService == null) {
            String msg = "Before activating Carbon Core bundle, an instance of "
                    + "UserRealm service should be in existance";
            log.error(msg);
            throw new Exception(msg);
        }
        return realmService;
    }

    public  RegistryService getRegistryService() throws Exception {
        if (registryService == null) {
            String msg = "Before activating Carbon Core bundle, an instance of "
                    + "RegistryService should be in existance";
            log.error(msg);
            throw new Exception(msg);
        }
        return registryService;
    }

    public  ServerConfigurationService getServerConfigurationService() {
        if (this.serverConfigurationService == null) {
            String msg = "Before activating Carbon Core bundle, an instance of "
                    + "ServerConfigurationService should be in existance";
            log.error(msg);
        }
        return this.serverConfigurationService;
    }

    public  ListenerManager getListenerManager() {
        return listenerManager;
    }

    public  void setListenerManager(ListenerManager listenerManager) {
        this.listenerManager = listenerManager;
    }


    public  void setMainServerConfigContext(ConfigurationContext mainServerConfigContext) {
        this.mainServerConfigContext = mainServerConfigContext;
    }

    public  ConfigurationContext getMainServerConfigContext() {
        return mainServerConfigContext;
    }

    public void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        this.tenantRegistryLoader = tenantRegistryLoader;
    }

    public TenantRegistryLoader getTenantRegistryLoader() {
        return tenantRegistryLoader;
    }

    public void addCoordinatedActivity(CoordinatedActivity coordinatedActivity) {
        coordinatedActivities.add(coordinatedActivity);
    }

    public void removeCoordinatedActivity(CoordinatedActivity coordinatedActivity) {
        coordinatedActivities.remove(coordinatedActivity);
    }

    public List<CoordinatedActivity> getCoordinatedActivities() {
        return coordinatedActivities ;
    }

    public void setCryptoService(CryptoService cryptoService) {

        this.cryptoService = cryptoService;
    }

    public CryptoService getCryptoService() {

        return cryptoService;
    }
}
