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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.context.CarbonCoreInitializedEvent;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerRestartHandler;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.core.encryption.KeyStoreBasedExternalCryptoProvider;
import org.wso2.carbon.core.init.CarbonServerManager;
import org.wso2.carbon.crypto.api.CryptoService;
import org.wso2.carbon.crypto.api.ExternalCryptoProvider;
import org.wso2.carbon.crypto.api.InternalCryptoProvider;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.core.clustering.api.CoordinatedActivity;
import org.wso2.carbon.core.encryption.SymmetricEncryption;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;

@Component(name="carbon.core.dscomponent", immediate=true)
public class CarbonCoreServiceComponent {

    private static Log log = LogFactory.getLog(CarbonCoreServiceComponent.class);

    private CarbonCoreDataHolder dataHolder = CarbonCoreDataHolder.getInstance();
    
    private static List<ServerShutdownHandler> shutdownHandlers = new ArrayList<ServerShutdownHandler>();
    
    private static List<ServerRestartHandler> restartHandlers = new ArrayList<ServerRestartHandler>();
    
    private static List<ServerStartupHandler> startupHandlers = new ArrayList<ServerStartupHandler>();

    private static List<ServerStartupObserver> serverStartupObservers = new ArrayList<ServerStartupObserver>();

    private static boolean serverStarted;
    
    private CarbonServerManager carbonServerManager;
    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            // for new caching, every thread should has its own populated CC. During the deployment time we assume super tenant
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            carbonContext.setTenantId(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID);
            ctxt.getBundleContext().registerService(ServerStartupObserver.class.getName(),
                    new DeploymentServerStartupObserver(), null) ;
            SymmetricEncryption encryption = SymmetricEncryption.getInstance();
            encryption.generateSymmetricKey();

            // Register the external crypto provider which is based on Carbon keystore management service.
            ctxt.getBundleContext().registerService(ExternalCryptoProvider.class,
                    new KeyStoreBasedExternalCryptoProvider(), null);

            carbonServerManager = new CarbonServerManager();
            carbonServerManager.start(ctxt.getBundleContext());
        } catch (Throwable e) {
            log.error("Failed to activate Carbon Core bundle ", e);
        }
    }
    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        try {
            // We assume it's super tenant during component deactivate time
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext
                    .getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            carbonServerManager.stop();  
        } catch (Throwable e) {
            log.error("Failed clean up Carbon core", e);
        }

        try {
            if ("false".equals(dataHolder.getServerConfigurationService().getFirstProperty("RequireCarbonServlet"))) {
                return;
            }
        } catch (Exception e) {
            log.debug("Error while retrieving serverConfiguration instance", e);
        }
        serverStarted = false;
        log.debug("Carbon Core bundle is deactivated ");
    }

    @Reference(name = "server.configuration.service", policy = ReferencePolicy.DYNAMIC, 
            cardinality = ReferenceCardinality.MANDATORY, unbind = "unsetServerConfigurationService")
    protected void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
       dataHolder.setServerConfigurationService(serverConfigurationService);
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfigurationService) {
       dataHolder.setServerConfigurationService(null);
    }

    @Reference(name = "user.realmservice.default", unbind = "unsetRealmService", 
            cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
    protected void setRealmService(RealmService realmService) {
        dataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        dataHolder.setRealmService(null);
    }

    @Reference(name = "http.service", cardinality = ReferenceCardinality.MANDATORY, 
            policy = ReferencePolicy.DYNAMIC, unbind = "unsetHttpService")
    protected void setHttpService(HttpService httpService) {
        dataHolder.setHttpService(httpService);
    }

    protected void unsetHttpService(HttpService httpService) {
        dataHolder.setHttpService(null);
    }

    @Reference(name = "registry.service", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(null);
    }

    @Reference(name = "tenant.registry.loader", cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC, unbind = "unSetTenantRegistryLoader")
    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        dataHolder.setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unSetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        dataHolder.setTenantRegistryLoader(null);
    }

    @Reference(name = "carbonCryptoService", cardinality = ReferenceCardinality.OPTIONAL, 
            policy = ReferencePolicy.DYNAMIC, unbind = "unsetCarbonCryptoService")
    protected void setCarbonCryptoService(CryptoService cryptoService){
        dataHolder.setCryptoService(cryptoService);
    }

    protected void unsetCarbonCryptoService(CryptoService cryptoService){
        dataHolder.setCryptoService(null);
    }

    @Reference(name = "serverShutdownHandler", cardinality = ReferenceCardinality.MULTIPLE, 
            policy = ReferencePolicy.DYNAMIC, unbind = "removeServerShutdownHandler")
    protected void addServerShutdownHandler(ServerShutdownHandler shutdownHandler) {
    	shutdownHandlers.add(shutdownHandler);
    }

    protected void removeServerShutdownHandler(ServerShutdownHandler shutdownHandler) {
        shutdownHandlers.remove(shutdownHandler);
    }

    @Reference(name = "serverRestartHandler", cardinality = ReferenceCardinality.MULTIPLE, 
            policy = ReferencePolicy.DYNAMIC, unbind = "removeServerRestartHandler")
    protected void addServerRestartHandler(ServerRestartHandler restartHandler) {
        restartHandlers.add(restartHandler);
    }

    protected void removeServerRestartHandler(ServerRestartHandler restartHandler) {
        restartHandlers.remove(restartHandler);
    }

    @Reference(name = "serverStartupHandler", cardinality = ReferenceCardinality.MULTIPLE, 
            policy = ReferencePolicy.DYNAMIC, unbind = "removeServerStartupHandler")
    protected void addServerStartupHandler(ServerStartupHandler startupHandler) {
    	synchronized (this.getClass()) {
    		if (serverStarted) {
    		    startupHandler.invoke();
    		} else {
    			startupHandlers.add(startupHandler);
    		}
		}        
    }

    protected void removeServerStartupHandler(ServerStartupHandler startupHandler) {
    	startupHandlers.remove(startupHandler);
    }

    public static void shutdown() {
        for (ServerShutdownHandler shutdownHandler : shutdownHandlers) {
            shutdownHandler.invoke();
        }
    }

    public static void restart() {
        for (ServerRestartHandler restartHandler : restartHandlers) {
            restartHandler.invoke();
        }
    }
    
    public static synchronized void startup() {
        for (ServerStartupHandler startupHandler : startupHandlers) {
        	startupHandler.invoke();
        }
        startupHandlers.clear();
        serverStarted = true;
    }

    @Reference(name = "coordinatedActivity", cardinality = ReferenceCardinality.MULTIPLE, 
            policy = ReferencePolicy.DYNAMIC, unbind = "removeCoordinatedActivity")
    protected void addCoordinatedActivity(CoordinatedActivity coordinatedActivity) {
        CarbonCoreDataHolder.getInstance().addCoordinatedActivity(coordinatedActivity);
    }

    protected void removeCoordinatedActivity(CoordinatedActivity coordinatedActivity) {
        CarbonCoreDataHolder.getInstance().removeCoordinatedActivity(coordinatedActivity);
    }

    @Reference(name = "serverStartupObserver", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "removeServerStartupObserver")
    protected void addServerStartupObserver(ServerStartupObserver startupObserver) {
        synchronized (this.getClass()) {
            if (serverStarted) {
                startupObserver.completedServerStartup();
            } else {
                serverStartupObservers.add(startupObserver);
            }
        }
    }

    protected void removeServerStartupObserver(ServerStartupObserver startupObserver) {
        serverStartupObservers.remove(startupObserver);
    }


    public static synchronized void notifyBefore() {
        for (ServerStartupObserver observer : serverStartupObservers) {
            observer.completingServerStartup();
        }
    }

    public static synchronized void notifyAfter(){
        for (ServerStartupObserver observer : serverStartupObservers) {
            observer.completedServerStartup();
        }
        serverStarted = true;
        startupHandlers.clear();

    }

    protected void unsetCarbonCoreInitializedEventService(CarbonCoreInitializedEvent carbonCoreInitializedEvent){}

    @Reference(name = "carbonCoreInitializedEventService", cardinality = ReferenceCardinality.MANDATORY, 
            policy = ReferencePolicy.DYNAMIC, unbind = "unsetCarbonCoreInitializedEventService")
    protected void setCarbonCoreInitializedEventService(CarbonCoreInitializedEvent carbonCoreInitializedEventService){}
}
