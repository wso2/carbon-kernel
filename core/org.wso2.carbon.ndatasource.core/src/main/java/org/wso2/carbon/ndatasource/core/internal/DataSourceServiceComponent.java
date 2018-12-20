/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.ndatasource.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.crypto.api.ExternalCryptoProvider;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceAxis2ConfigurationContextObserver;
import org.wso2.carbon.ndatasource.core.DataSourceManager;
import org.wso2.carbon.ndatasource.core.DataSourceRepository;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.lang.reflect.InvocationTargetException;

@Component(name="org.wso2.carbon.ndatasource", immediate=true)
public class DataSourceServiceComponent {

	private static final Log log = LogFactory.getLog(DataSourceServiceComponent.class);

    private static final String DATA_SOURCE_REPO_CLASS_TAG = "CarbonDataSourceRepositoryClass";
	
	private static RegistryService registryService;

	private static ExternalCryptoProvider externalCryptoProvider;
	
	private static RealmService realmService;
		
	private static SecretCallbackHandlerService secretCallbackHandlerService;
	
	private static ServerConfigurationService serverConfigurationService;
		
	private DataSourceService dataSourceService;
	
	private ComponentContext ctx;
	
	private boolean tenantUserDataSourcesInitialized;
		
	private static ConfigurationContextService configContextService;

    private static Class<DataSourceRepository>  carbonDataSourceRepositoryClass;

	protected synchronized void activate(ComponentContext ctx) {
		this.ctx = ctx;
		if (log.isDebugEnabled()) {
			log.debug("DataSourceServiceComponent activated");
		}
        /** Attempting to load the DatasourceRepository implementation class defined in carbon.xml
        .* If it is not there it will use default implementation
         */
        String carbonDataSourceRepositoryClassName =
                CarbonUtils.getServerConfiguration().getFirstProperty(
                        DATA_SOURCE_REPO_CLASS_TAG);
        if(carbonDataSourceRepositoryClassName!=null){
            try {
                carbonDataSourceRepositoryClass=(Class<DataSourceRepository>) this.getClass()
                        .getClassLoader().loadClass
                                (carbonDataSourceRepositoryClassName);
            } catch (ClassNotFoundException e) {
                log.warn("The specified DataSourceRepositoryClass "
                        + carbonDataSourceRepositoryClassName + " is not there in the class " +
                        "path.Using the default DataSourceRepositoryClass ", e);
            }
        }
		/* if the user data sources are already initialized before setting the ctx,
		 * that means the services aren't registered yet, so we should do it now */
		if (this.tenantUserDataSourcesInitialized) {
			this.registerServices();
		}
	}
	
	protected synchronized void deactivate(ComponentContext ctx) {
		this.ctx = null;
		this.tenantUserDataSourcesInitialized = false;
		if (log.isDebugEnabled()) {
			log.debug("DataSourceServiceComponent deactivated");
		}
	}
	
	/**
	 * This method getting called implement some important functionality to make sure that,
	 * components which depend on the DataSourceService will always get it after the component
	 * is fully initialized.
	 */
	private void registerServices() {
		if (this.getDataSourceService() == null) {
			this.dataSourceService = new DataSourceService();
		}
		BundleContext bundleContext = this.ctx.getBundleContext();
		bundleContext.registerService(DataSourceService.class.getName(), 
				this.getDataSourceService(), null);
		bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                new DataSourceAxis2ConfigurationContextObserver(), null);
	}
	
	public DataSourceService getDataSourceService() {
		return dataSourceService;
	}

    @Reference(name = "user.realmservice.default", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
    	if (log.isDebugEnabled()) {
    		log.debug("RealmService acquired");
    	}
    	DataSourceServiceComponent.realmService = realmService;
    	this.checkInitTenantUserDataSources();
    }
    
    protected void unsetRealmService(RealmService realmService) {
    	DataSourceServiceComponent.realmService = null;
    }
	
    public static RealmService getRealmService() {
    	return DataSourceServiceComponent.realmService;
    }

    @Reference(name = "registry.service", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
    	if (log.isDebugEnabled()) {
    		log.debug("RegistryService acquired");
    	}
    	DataSourceServiceComponent.registryService = registryService;
    	this.checkInitTenantUserDataSources();
    }

    protected void unsetRegistryService(RegistryService registryService) {
        registryService = null;
    }

    public static RegistryService getRegistryService() {
        return DataSourceServiceComponent.registryService;
    }

    @Reference(name = "external.crypto.service", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetExternalCryptoProvider")
    protected void setExternalCryptoProvider(ExternalCryptoProvider externalCryptoProvider) {
	if (log.isDebugEnabled()) {
		log.debug("ExternalCryptoProvider acquired");
	}
	this.externalCryptoProvider = externalCryptoProvider;
	this.checkInitTenantUserDataSources();
    }

    protected void unsetExternalCryptoProvider(ExternalCryptoProvider externalCryptoProvider) {
	this.externalCryptoProvider = null;
    }

    public static ExternalCryptoProvider getExternalCryptoProvider() {
	return DataSourceServiceComponent.externalCryptoProvider;
    }
	
    public static SecretCallbackHandlerService getSecretCallbackHandlerService() {
    	return DataSourceServiceComponent.secretCallbackHandlerService;
    }

    @Reference(name = "secret.callback.handler.service", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MANDATORY, 
            unbind = "unsetSecretCallbackHandlerService")
    protected void setSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
    	if (log.isDebugEnabled()) {
    		log.debug("SecretCallbackHandlerService acquired");
    	}
    	DataSourceServiceComponent.secretCallbackHandlerService = secretCallbackHandlerService;
    	this.initSystemDataSources();
    	this.checkInitTenantUserDataSources();
    }

    protected void unsetSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
    	DataSourceServiceComponent.secretCallbackHandlerService = null;
    }
    
    private void initSystemDataSources() {
    	if (log.isDebugEnabled()) {
    		log.debug("Initializing system data sources...");
    	}
    	try {
    	    DataSourceManager.getInstance().initSystemDataSources();
    	    if (log.isDebugEnabled()) {
    	    	log.debug("System data sources successfully initialized");
    	    }
    	} catch (Exception e) {
			log.error("Error in intializing system data sources: " + e.getMessage(), e);
		}    	
    }
    
    private synchronized void checkInitTenantUserDataSources() {
    	if (DataSourceServiceComponent.getRealmService() != null && 
    			DataSourceServiceComponent.getRegistryService() != null &&
    			DataSourceServiceComponent.getSecretCallbackHandlerService() != null && 
    			DataSourceServiceComponent.getServerConfigurationService() != null &&
			DataSourceServiceComponent.getExternalCryptoProvider() != null) {
    		this.initSuperTenantUserDataSources();
    	}
    }
    
    private synchronized void initSuperTenantUserDataSources() {
    	try {
    		if (log.isDebugEnabled()) {
        		log.debug("Initializing super tenant user data sources...");
        	}
    		DataSourceManager.getInstance().initTenant(MultitenantConstants.SUPER_TENANT_ID);
    	    if (log.isDebugEnabled()) {
    	    	log.debug("Super tenant user data sources successfully initialized");
    	    }
    	    this.tenantUserDataSourcesInitialized = true;
    	    if (this.ctx != null) {
    	        this.registerServices();
    	    }
    	} catch (Exception e) {
			log.error("Error in intializing system data sources: " + e.getMessage(), e);
		} 
    }
    
    public boolean isTenantUserDataSourcesInitialized() {
		return tenantUserDataSourcesInitialized;
	}

    public static ServerConfigurationService getServerConfigurationService() {
    	return DataSourceServiceComponent.serverConfigurationService;
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        DataSourceServiceComponent.serverConfigurationService = null;
    }

    @Reference(name = "server.configuration.service", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL, 
            unbind = "unsetServerConfigurationService")
    protected void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
    	if (log.isDebugEnabled()) {
    		log.debug("ServerConfigurationService acquired");
    	}
    	DataSourceServiceComponent.serverConfigurationService = serverConfigurationService;
    	this.checkInitTenantUserDataSources();
    }

    @Reference(name = "config.context.service", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configContextService) {
    	DataSourceServiceComponent.configContextService = configContextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configContextService) {
    	DataSourceServiceComponent.configContextService = null;
    }
    
	public static ConfigurationContextService getConfigContextService() {
		return configContextService;
	}
    public static DataSourceRepository getNewTenantDataSourceRepository(int tenantId) throws
            DataSourceException {
        DataSourceRepository dataSourceRepository = null;
        if (carbonDataSourceRepositoryClass != null) {
            try {
                dataSourceRepository = carbonDataSourceRepositoryClass.getConstructor
                        (Integer.TYPE).newInstance(tenantId);
         } catch (InstantiationException e) {
                log.warn("The specified DataSourceRepositoryClass " +
                        "" + carbonDataSourceRepositoryClass.getName() + " could not be " +
                        "instantiated.Using the default DataSourceRepositoryClass ", e);
            } catch (IllegalAccessException e) {
                log.warn("The specified DataSourceRepositoryClass "
                        + carbonDataSourceRepositoryClass.getName() + " could not be " +
                        "accessed.Using the default DataSourceRepositoryClass ", e);
            } catch (NoSuchMethodException e) {
                log.warn("The specified DataSourceRepositoryClass "
                        + carbonDataSourceRepositoryClass.getName() + " do not have " +
                        "constructor that takes tenantId as argument.Using the default " +
                        "DataSourceRepositoryClass", e);
            } catch (InvocationTargetException e) {
                log.warn("Error while instantiating specified DataSourceRepositoryClass "
                        + carbonDataSourceRepositoryClass.getName() + " .Using the default " +
                        "DataSourceRepositoryClass", e);
            }
        }
        if (dataSourceRepository != null) {
            return dataSourceRepository;
        }
        return new DataSourceRepository(tenantId);
    }
}
