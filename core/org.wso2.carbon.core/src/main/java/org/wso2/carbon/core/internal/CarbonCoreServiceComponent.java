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

import org.apache.axis2.clustering.MembershipScheme;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.context.CarbonCoreInitializedEvent;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerRestartHandler;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastConstants;
import org.wso2.carbon.core.clustering.hazelcast.aws.AWSBasedMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.multicast.MulticastBasedMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.wka.WKABasedMembershipScheme;
import org.wso2.carbon.core.init.CarbonServerManager;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.core.clustering.api.CoordinatedActivity;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * @scr.component name="carbon.core.dscomponent"" immediate="true"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="server.configuration.service" interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"  bind="setServerConfigurationService" unbind="unsetServerConfigurationService"
 * @scr.reference name="http.service" interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic"  bind="setHttpService" unbind="unsetHttpService"
 * @scr.reference name="serverShutdownHandler" interface="org.wso2.carbon.core.ServerShutdownHandler"
 * cardinality="0..n" policy="dynamic"  bind="addServerShutdownHandler" unbind="removeServerShutdownHandler"
 * @scr.reference name="serverRestartHandler" interface="org.wso2.carbon.core.ServerRestartHandler"
 * cardinality="0..n" policy="dynamic"  bind="addServerRestartHandler" unbind="removeServerRestartHandler"
 * @scr.reference name="serverStartupHandler" interface="org.wso2.carbon.core.ServerStartupHandler"
 * cardinality="0..n" policy="dynamic"  bind="addServerStartupHandler" unbind="removeServerStartupHandler"
 * @scr.reference name="tenant.registry.loader" interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic" bind="setTenantRegistryLoader" unbind="unSetTenantRegistryLoader"
 * @scr.reference name="coordinatedActivity" interface="org.wso2.carbon.core.clustering.api.CoordinatedActivity"
 * cardinality="0..n" policy="dynamic" bind="addCoordinatedActivity" unbind="removeCoordinatedActivity"
 * @scr.reference name="serverStartupObserver" interface="org.wso2.carbon.core.ServerStartupObserver"
 * cardinality="0..n" policy="dynamic"  bind="addServerStartupObserver" unbind="removeServerStartupObserver"
 * @scr.reference name="carbonCoreInitializedEventService" interface="org.wso2.carbon.context.CarbonCoreInitializedEvent"
 * cardinality="1..1" policy="dynamic"  bind="setCarbonCoreInitializedEventService" unbind="unsetCarbonCoreInitializedEventService"
  */
public class CarbonCoreServiceComponent {

    private static Log log = LogFactory.getLog(CarbonCoreServiceComponent.class);

    private CarbonCoreDataHolder dataHolder = CarbonCoreDataHolder.getInstance();
    
    private static List<ServerShutdownHandler> shutdownHandlers = new ArrayList<ServerShutdownHandler>();
    
    private static List<ServerRestartHandler> restartHandlers = new ArrayList<ServerRestartHandler>();
    
    private static List<ServerStartupHandler> startupHandlers = new ArrayList<ServerStartupHandler>();

    private static List<ServerStartupObserver> serverStartupObservers = new ArrayList<ServerStartupObserver>();

    private static boolean serverStarted;
    
    private CarbonServerManager carbonServerManager;

    protected void activate(ComponentContext ctxt) {
        try {
            // for new caching, every thread should has its own populated CC. During the deployment time we assume super tenant
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            carbonContext.setTenantId(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID);

            BundleContext bundleContext = ctxt.getBundleContext();
            bundleContext.registerService(ServerStartupObserver.class.getName(), new DeploymentServerStartupObserver(), null) ;

            // register membership scheme services
            bundleContext.registerService(MembershipScheme.class.getName(), new AWSBasedMembershipScheme(),
                    prepareMembershipSchemeParameters(HazelcastConstants.AWS_MEMBERSHIP_SCHEME));
            bundleContext.registerService(MembershipScheme.class.getName(), new MulticastBasedMembershipScheme(),
                    prepareMembershipSchemeParameters(HazelcastConstants.MULTICAST_MEMBERSHIP_SCHEME));
            bundleContext.registerService(MembershipScheme.class.getName(), new WKABasedMembershipScheme(),
                    prepareMembershipSchemeParameters(HazelcastConstants.WKA_MEMBERSHIP_SCHEME));

            carbonServerManager = new CarbonServerManager();
            carbonServerManager.start(bundleContext);
        } catch (Throwable e) {
            log.error("Failed to activate Carbon Core bundle ", e);
        }
    }

    private Dictionary<String, String> prepareMembershipSchemeParameters(String membershipSchemeName) {
        Dictionary<String, String> parameters = new Hashtable<String, String>();
        parameters.put(HazelcastConstants.MEMBERSHIP_SCHEME_NAME, membershipSchemeName);
        return parameters;
    }

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

    protected void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
       dataHolder.setServerConfigurationService(serverConfigurationService);
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfigurationService) {
       dataHolder.setServerConfigurationService(null);
    }

    protected void setRealmService(RealmService realmService) {
        dataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        dataHolder.setRealmService(null);
    }

    protected void setHttpService(HttpService httpService) {
        dataHolder.setHttpService(httpService);
    }

    protected void unsetHttpService(HttpService httpService) {
        dataHolder.setHttpService(null);
    }

    protected void setRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(null);
    }

    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        dataHolder.setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unSetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        dataHolder.setTenantRegistryLoader(null);
    }

    protected void addServerShutdownHandler(ServerShutdownHandler shutdownHandler) {
    	shutdownHandlers.add(shutdownHandler);
    }

    protected void removeServerShutdownHandler(ServerShutdownHandler shutdownHandler) {
        shutdownHandlers.remove(shutdownHandler);
    }

    protected void addServerRestartHandler(ServerRestartHandler restartHandler) {
        restartHandlers.add(restartHandler);
    }

    protected void removeServerRestartHandler(ServerRestartHandler restartHandler) {
        restartHandlers.remove(restartHandler);
    }
    
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

    protected void addCoordinatedActivity(CoordinatedActivity coordinatedActivity) {
        CarbonCoreDataHolder.getInstance().addCoordinatedActivity(coordinatedActivity);
    }

    protected void removeCoordinatedActivity(CoordinatedActivity coordinatedActivity) {
        CarbonCoreDataHolder.getInstance().removeCoordinatedActivity(coordinatedActivity);
    }

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

    protected void setCarbonCoreInitializedEventService(CarbonCoreInitializedEvent carbonCoreInitializedEventService){}
}
