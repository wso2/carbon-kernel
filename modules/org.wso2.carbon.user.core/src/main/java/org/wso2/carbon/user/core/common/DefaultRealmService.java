/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.core.common;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.builder.ClaimBuilder;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.config.TenantMgtXMLProcessor;
import org.wso2.carbon.user.core.config.multitenancy.MultiTenantRealmConfigBuilder;
import org.wso2.carbon.user.core.profile.builder.ProfileConfigurationBuilder;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

public class DefaultRealmService implements RealmService {

	private RealmCache realmCache = RealmCache.getInstance();

	private BundleContext bc;
	private RealmConfiguration bootstrapRealmConfig;
	private TenantMgtConfiguration tenantMgtConfiguration;
	private DataSource dataSource;
	private OMElement parentElement;
	private TenantManager tenantManager;
	private UserRealm bootstrapRealm;
	private MultiTenantRealmConfigBuilder multiTenantBuilder = null;
	private static final Log log = LogFactory.getLog(DefaultRealmService.class);

	private static final String PRIMARY_TENANT_REALM = "primary";

	private static final String DB_CHECK_SQL = "select * from UM_USER";

	//to track whether this is the first time initialization of the pack.
	private static boolean isFirstInitialization = true;

	//map to store and pass the connections to database and ldap which are created in this class.
	private Map<String, Object> properties = new Hashtable<String, Object>();

	public DefaultRealmService(BundleContext bc, RealmConfiguration realmConfig) throws Exception {
		if (realmConfig != null) {
			this.bootstrapRealmConfig = realmConfig;
		} else {
			this.bootstrapRealmConfig = buildBootStrapRealmConfig();
		}
		this.tenantMgtConfiguration = buildTenantMgtConfig(bc,
				this.bootstrapRealmConfig.getUserStoreProperty(UserCoreConstants.TenantMgtConfig.LOCAL_NAME_TENANT_MANAGER));
		this.dataSource = DatabaseUtil.getRealmDataSource(bootstrapRealmConfig);
		initializeDatabase(dataSource);
		properties.put(UserCoreConstants.DATA_SOURCE, dataSource);
		properties.put(UserCoreConstants.FIRST_STARTUP_CHECK, isFirstInitialization);

		//this.tenantManager = this.initializeTenantManger(this.getTenantConfigurationElement(bc));
		this.tenantManager = this.initializeTenantManger(this.tenantMgtConfiguration);
		this.tenantManager.setBundleContext(bc);
		//initialize existing partitions if applicable with the particular tenant manager.
		this.tenantManager.initializeExistingPartitions();
		// initializing the bootstrapRealm
		this.bc = bc;
		bootstrapRealm = initializeRealm(bootstrapRealmConfig, MultitenantConstants.SUPER_TENANT_ID);
		Dictionary<String, String> dictionary = new Hashtable<String, String>();
		dictionary.put(UserCoreConstants.REALM_GENRE, UserCoreConstants.DELEGATING_REALM);
		if (bc != null) {
			// note that in a case of we don't run this in an OSGI envrionment
			// like checkin-client,
			// we need to avoid the registration of the service
			bc.registerService(UserRealm.class.getName(), bootstrapRealm, dictionary);
		}

	}

	public DefaultRealmService(BundleContext bc) throws Exception {
		this(bc, null);
	}

	/**
	 * Non OSGI constructor
	 */
	public DefaultRealmService(RealmConfiguration realmConfig, TenantManager tenantManager)
			throws Exception {
		this.bootstrapRealmConfig = realmConfig;
		this.dataSource = DatabaseUtil.getRealmDataSource(bootstrapRealmConfig);
		properties.put(UserCoreConstants.DATA_SOURCE, dataSource);
		this.tenantMgtConfiguration = buildTenantMgtConfig(bc,
				this.bootstrapRealmConfig.getUserStoreProperty(UserCoreConstants.TenantMgtConfig.LOCAL_NAME_TENANT_MANAGER));
		this.tenantManager = tenantManager;
		bootstrapRealm = initializeRealm(bootstrapRealmConfig, MultitenantConstants.SUPER_TENANT_ID);
	}

	private RealmConfiguration buildBootStrapRealmConfig() throws UserStoreException {
		this.parentElement = getConfigurationElement();
		OMElement realmElement = parentElement.getFirstChildWithName(new QName(
                                                   UserCoreConstants.RealmConfig.LOCAL_NAME_REALM));
		RealmConfigXMLProcessor rmProcessor = new RealmConfigXMLProcessor();
		rmProcessor.setSecretResolver(parentElement);
		return rmProcessor.buildRealmConfiguration(realmElement);
	}

	private TenantMgtConfiguration buildTenantMgtConfig(BundleContext bc, String tenantManagerClass)
			throws UserStoreException {
		TenantMgtXMLProcessor tenantMgtXMLProcessor = new TenantMgtXMLProcessor();
		tenantMgtXMLProcessor.setBundleContext(bc);
		return tenantMgtXMLProcessor.buildTenantMgtConfigFromFile(tenantManagerClass);
	}

	@Override
	public org.wso2.carbon.user.api.UserRealm getTenantUserRealm(int tenantId)
			throws org.wso2.carbon.user.api.UserStoreException {
		if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
			return bootstrapRealm;
		}

		org.wso2.carbon.user.api.UserRealm userRealm = getCachedUserRealm(tenantId);
		if (userRealm != null) {
			return userRealm;
		}
		try {
			if (tenantManager.getTenant(tenantId) != null) {

				Tenant tenant = tenantManager.getTenant(tenantId);
				RealmConfiguration tenantRealmConfig = tenant.getRealmConfig();
				MultiTenantRealmConfigBuilder realmConfigBuilder = getMultiTenantRealmConfigBuilder();
				if (realmConfigBuilder != null) {
					tenantRealmConfig = realmConfigBuilder.getRealmConfigForTenantToCreateRealm(
					                                                                            bootstrapRealmConfig, tenantRealmConfig, tenantId);
				}

                synchronized (this) {
                    userRealm = initializeRealm(tenantRealmConfig, tenantId);
                    realmCache.addToCache(tenantId, PRIMARY_TENANT_REALM, userRealm);
                }
            }

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new org.wso2.carbon.user.api.UserStoreException(e.getMessage(), e);
		}
		return userRealm;
	}

	@Override
	public UserRealm getCachedUserRealm(int tenantId) throws UserStoreException {
		return (UserRealm) realmCache.getUserRealm(tenantId, PRIMARY_TENANT_REALM);
    }

    @Override
    public void clearCachedUserRealm(int tenantId) throws UserStoreException {
        realmCache.clearFromCache(tenantId, PRIMARY_TENANT_REALM);
    }

    @Override
	public UserRealm getUserRealm(RealmConfiguration tenantRealmConfig) throws UserStoreException {
		UserRealm userRealm = null;
		int tenantId = tenantRealmConfig.getTenantId();
		if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
			return bootstrapRealm;
		}
		userRealm = (UserRealm) realmCache.getUserRealm(tenantId, PRIMARY_TENANT_REALM);
		if (userRealm == null) {
			MultiTenantRealmConfigBuilder realmConfigBuilder = getMultiTenantRealmConfigBuilder();
			if (realmConfigBuilder != null) {
				tenantRealmConfig = realmConfigBuilder.getRealmConfigForTenantToCreateRealm(
				                                                                            bootstrapRealmConfig, tenantRealmConfig, tenantId);
			}
			synchronized (this) {
				userRealm = initializeRealm(tenantRealmConfig, tenantId);
				realmCache.addToCache(tenantId, PRIMARY_TENANT_REALM, userRealm);
			}
		}
		return userRealm;
	}

	@SuppressWarnings("rawtypes")
	public UserRealm initializeRealm(RealmConfiguration realmConfig, int tenantId)
			throws UserStoreException {
		ClaimBuilder.setBundleContext(bc);
		ProfileConfigurationBuilder.setBundleContext(bc);
		UserRealm userRealm = null;
		try {
			Class clazz = Class.forName(realmConfig.getRealmClassName());
			userRealm = (UserRealm) clazz.newInstance();
			userRealm.init(realmConfig, properties, tenantId);
		} catch (Exception e) {
			if (!realmConfig.isPrimary()) {
				String msg = "Cannot initialize the realm.";
				log.warn(msg, e);
			} else {
				String msg = "Cannot initialize the realm.";
				log.error(msg, e);
				throw new UserStoreException(msg, e);
			}
		}
		return userRealm;
	}

	// TODO : Move this into RealmConfigXMLProcessor

	private OMElement getConfigurationElement() throws UserStoreException {
		try {
			String userMgt = CarbonUtils.getUserMgtXMLPath();
			InputStream inStream = null;
			if (userMgt != null) {
				File userMgtXml = new File(userMgt);
				if (!userMgtXml.exists()) {
					String msg = "Instance of a WSO2 User Manager has not been created. user-mgt.xml is not found.";
					throw new FileNotFoundException(msg);
				}
				inStream = new FileInputStream(userMgtXml);
			} else {
				inStream = this.getClass().getClassLoader()
						.getResourceAsStream("repository/conf/user-mgt.xml");
				if (inStream == null) {
					String msg = "Instance of a WSO2 User Manager has not been created. user-mgt.xml is not found. Please set the carbon.home";
					throw new FileNotFoundException(msg);
				}
			}

			StAXOMBuilder builder = new StAXOMBuilder(CarbonUtils.replaceSystemVariablesInXml(inStream));
			return builder.getDocumentElement();
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			throw new UserStoreException(e.getMessage(), e);
		} catch (XMLStreamException e) {
			log.error(e.getMessage(), e);
			throw new UserStoreException(e.getMessage(), e);
		} catch (CarbonException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
    }

	private void initializeDatabase(DataSource ds) throws Exception {
		String value = System.getProperty("setup");
		if (value != null) {
			DatabaseCreator databaseCreator = new DatabaseCreator(ds);
			try {
				if (!databaseCreator.isDatabaseStructureCreated(DB_CHECK_SQL)) {
					databaseCreator.createRegistryDatabase();
				} else {
					isFirstInitialization = false;
					log.info("Database already exists. Not creating a new database.");
				}
			} catch (Exception e) {
				String msg = "Error in creating the database";
				throw new Exception(msg, e);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private TenantManager initializeTenantManger(TenantMgtConfiguration tenantMgtConfiguration)
			throws Exception {
		TenantManager tenantManager = null;
		// read the tenant manager from tenant-mgt.xml
		//String className = configElement.getAttribute(new QName("class")).getAttributeValue();
		String className = tenantMgtConfiguration.getTenantManagerClass();
		Class clazz = Class.forName(className);

		Constructor constructor = clazz.getConstructor(OMElement.class, Map.class);
		/*put the tenantMgtConfiguration and realm configuration inside the property map that is
        passed to tenant manager constructor. These are mainly used by LDAPTenantManager*/
		properties.put(UserCoreConstants.TENANT_MGT_CONFIGURATION, tenantMgtConfiguration);
		properties.put(UserCoreConstants.REALM_CONFIGURATION, bootstrapRealmConfig);

		//tenant config OMElement passed to the constructor is not used anymore. Hence passing a null.
		Object newObject = constructor.newInstance(null, properties);
		tenantManager = (TenantManager) newObject;

		return tenantManager;
	}

	@Override
	public RealmConfiguration getBootstrapRealmConfiguration() {
		return bootstrapRealmConfig;
	}

	@Override
	public UserRealm getBootstrapRealm() throws UserStoreException {
		return bootstrapRealm;
	}

	@Override
	public void setTenantManager(org.wso2.carbon.user.api.TenantManager tenantManager)
			throws org.wso2.carbon.user.api.UserStoreException {
		setTenantManager((TenantManager) tenantManager);
	}

	@Override
	public TenantManager getTenantManager() {
		return this.tenantManager;
	}

	@Override
	public void setTenantManager(TenantManager tenantManager) {
		this.tenantManager = tenantManager;
	}

	@Override
	public TenantMgtConfiguration getTenantMgtConfiguration() {
		return tenantMgtConfiguration;
	}

    @Override
    public void setBootstrapRealmConfiguration(RealmConfiguration realmConfiguration) {
        this.bootstrapRealmConfig = realmConfiguration;
    }

    @SuppressWarnings("rawtypes")
    @Override
	public MultiTenantRealmConfigBuilder getMultiTenantRealmConfigBuilder()
			throws UserStoreException {
		try {
			if (multiTenantBuilder == null) {
				String clazzName = tenantMgtConfiguration
						.getTenantStoreProperty(UserCoreConstants.TenantMgtConfig.PROPERTY_MULTI_TENANT_REALM_CONFIG_BUILDER);
				if (clazzName != null) {
					Class clazz = Class.forName(clazzName);
					MultiTenantRealmConfigBuilder multiConfigBuilder = (MultiTenantRealmConfigBuilder) clazz
							.newInstance();
					return multiConfigBuilder;
				}
				return null;
			} else {
				return multiTenantBuilder;
			}
		} catch (ClassNotFoundException e) {
			errorEncountered(e);
		} catch (InstantiationException e) {
			errorEncountered(e);
		} catch (IllegalAccessException e) {
			errorEncountered(e);
		}
		return null;
	}

	private void errorEncountered(Exception e) throws UserStoreException {
		String msg = "Exception while creating multi tenant builder " + e.getMessage();
		log.error(msg, e);
		throw new UserStoreException(msg, e);
	}
}



