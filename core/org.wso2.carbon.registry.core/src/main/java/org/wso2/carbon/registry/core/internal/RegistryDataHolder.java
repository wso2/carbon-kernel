package org.wso2.carbon.registry.core.internal;

import org.wso2.carbon.user.core.service.RealmService;

/**
 * This singleton data holder contains all the data required by the registry core OSGi bundle
 */
public class RegistryDataHolder {

    private static RegistryDataHolder registryDataHolder = new RegistryDataHolder();

    private RealmService realmService;

    private RegistryDataHolder(){
    }

    public static RegistryDataHolder getInstance(){
        return registryDataHolder;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

}