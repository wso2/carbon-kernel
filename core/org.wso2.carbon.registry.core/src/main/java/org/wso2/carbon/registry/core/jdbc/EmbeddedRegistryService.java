/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.clustering.NodeGroupLock;
import org.wso2.carbon.registry.core.config.DataBaseConfiguration;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDataAccessManager;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * This is a core class used by application that use registry in the embedded mode. This class is
 * used to create embedded registry instances for user sessions.
 * <p/>
 * UserRegistry is the embedded mode implementation of the Registry API. In this mode, all registry
 * accesses has to be done using a UserRegistry instance. And there has to be separate UserRegistry
 * instance for each user to access the registry. These UserRegistry instances has be obtained from
 * the {@link EmbeddedRegistry}. It is recommended to have only one EmbeddedRegistry instance per
 * application. But there can be exceptions, where it is required to maintain two or more registries
 * pointing to different data sources.
 * <p/>
 * Applications should initialize an EmbeddedRegistry instance at the start-up using following
 * code.
 * <p/>
 * InputStream configStream = new FileInputStream("/projects/registry.xml"); RegistryContext
 * registryContext = new RegistryContext(configStream); EmbeddedRegistry embeddedRegistry = new
 * EmbeddedRegistry(registryContext);
 * <p/>
 * After initializing an EmbeddedRegistry instance it should be stored in some globally accessible
 * location, so that it can be used by necessary modules to create UserRegistry instances. From
 * this, it is possible to create UserRegistry instances using various parameter combinations
 * documented in getXXRegistry methods.
 * <p/>
 * UserRegistry adminRegistry = embeddedRegistry.getRegistry("admin", "admin");
 *
 * @see EmbeddedRegistry
 * @see UserRegistry
 */
public class EmbeddedRegistryService implements RegistryService {

    private static final Log log = LogFactory.getLog(EmbeddedRegistryService.class);

    private EmbeddedRegistry embeddedRegistry;
    private RealmService realmService;
    private String chroot;

    /**
     * The registry context used by this registry service instance.
     */
    protected RegistryContext registryContext;

    /**
     * Instantiates the EmbeddedRegistry using the configuration given in the context and the given
     * UserRealm. Data source given in the context will be used for the resource store. User store
     * is accessed from the given UserRealm, which may point to a different source.
     *
     * @param context Registry Context containing the configuration.
     *
     * @throws RegistryException if the creation of the embedded registry service fails.
     */
    public EmbeddedRegistryService(RegistryContext context) throws RegistryException {
        this.registryContext = context;
        long start = System.nanoTime();
        configure(context.getRealmService());
        if (log.isInfoEnabled()) {
            try {
                Connection connection =
                        ((JDBCDataAccessManager) context.getDataAccessManager()).getDataSource()
                                .getConnection();
                try {
                    String jdbcURL = connection.getMetaData().getURL();
                    Iterator<String> dbConfigNames = context.getDBConfigNames();
                    while (dbConfigNames.hasNext()) {
                        String name = dbConfigNames.next();
                        DataBaseConfiguration configuration = context.getDBConfig(name);
                        if (jdbcURL != null && jdbcURL.equals(configuration.getDbUrl())) {
                            String dbConfigName = configuration.getConfigName();
                            DataBaseConfiguration defaultDBConfiguration =
                                    context.getDefaultDataBaseConfiguration();
                            if (dbConfigName != null && defaultDBConfiguration != null &&
                                    dbConfigName.equals(defaultDBConfiguration.getConfigName())) {
                                log.info("Configured Registry in " +
                                        ((System.nanoTime() - start) / 1000000L) + "ms");
                            } else {
                                log.info("Connected to mount at " + dbConfigName + " in " +
                                        ((System.nanoTime() - start) / 1000000L) + "ms");
                            }
                        }
                    }

                } finally {
                    connection.close();
                }
            } catch (SQLException ignored) {
                // We are only interested in logging the connection time in here. So, simply ignore
                // any exceptions that might result in the process.
            }
        }
    }

    /**
     * This constructor is used by the inherited InMemoryEmbeddedRegistry class as it has to be
     * instantiated using the default constructor.
     */
    protected EmbeddedRegistryService() {
    }

    /**
     * Method to configure the embedded registry service.
     *
     * @param realmService the user realm service instance.
     *
     * @throws RegistryException if an error occurs.
     */
    protected void configure(RealmService realmService) throws RegistryException {
        if (realmService == null) {
            String msg = "The realm service is not available.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        this.realmService = realmService;

        if (log.isTraceEnabled()) {
            log.trace("Configuring the embedded registry.");
        }

        DataAccessManager dataAccessManager = registryContext.getDataAccessManager();
        if (!(dataAccessManager instanceof JDBCDataAccessManager)) {
            String msg = "Failed to configure the embedded registry. Invalid data access manager.";
            log.error(msg);
            throw new RegistryException(msg);
        }
        NodeGroupLock.init(dataAccessManager);
        Transaction.init(dataAccessManager);

        try {
            if (log.isTraceEnabled()) {
                log.trace("Obtaining a cluster wide database lock.");
            }

            NodeGroupLock.lock(NodeGroupLock.INITIALIZE_LOCK);

            if (log.isTraceEnabled()) {
                log.trace("Cluster wide database lock obtained successfully.");
            }
            if (registryContext.isSetup()) {
                if (!dataAccessManager.isDatabaseExisting()) {
                    // mean the database tables are needed
                    if (log.isTraceEnabled()) {
                        log.trace("Creating database tables.");
                    }
                    try {
                        dataAccessManager.createDatabase();
                    } catch (Exception ex) {
                        String msg = "Error occurred while creating the database";
                        log.error(msg);
                        throw new RegistryException(msg, ex);
                    }

                    if (log.isTraceEnabled()) {
                        log.trace("Database tables created successfully.");
                    }
                } else if (log.isTraceEnabled()) {
                    log.trace("Continue the use of existing database tables");
                }
            } else if (log.isTraceEnabled()) {
                log.trace("Registry is not initialized in setup mode. " +
                        "Registry database tables will not be created.");
            }
            if (log.isTraceEnabled()) {
                log.trace("Creating the JDBC Registry instance ..");
            }

            embeddedRegistry = new EmbeddedRegistry(registryContext, realmService);
            chroot = embeddedRegistry.getRegistryContext().getRegistryRoot();

            if (!registryContext.isClone()) {

                // adding initial system collection as system user of tenant 0
                if (log.isTraceEnabled()) {
                    log.trace("Adding mount collection and register mount points.");
                }
                UserRegistry systemRegistry = getSystemRegistry();

                RegistryUtils.addMountCollection(systemRegistry);
                RegistryUtils.registerMountPoints(systemRegistry,
                        MultitenantConstants.SUPER_TENANT_ID);
            }

            if (log.isTraceEnabled()) {
                log.trace("JDBC Registry instance created successfully.");
            }

        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Releasing a cluster wide database lock.");
            }

            NodeGroupLock.unlock(NodeGroupLock.INITIALIZE_LOCK);
            if (log.isTraceEnabled()) {
                log.trace("Cluster wide database lock released successfully.");
            }
        }
    }

    /**
     * Creates a UserRegistry instance for anonymous user. Permissions set for anonymous user will
     * be applied for all operations performed using this instance.
     *
     * @return UserRegistry for the anonymous user.
     * @throws RegistryException
     */
    public UserRegistry getUserRegistry() throws RegistryException {
        return getUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME,
                MultitenantConstants.SUPER_TENANT_ID);
    }

    /**
     * Returns a registry to be used for system operations. Human users should not be allowed log in
     * using this registry.
     *
     * @return User registry for system user.
     * @throws RegistryException
     */
    public UserRegistry getSystemRegistry() throws RegistryException {
        return getSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
    }

    /**
     * Returns a registry to be used for system operations. Human users should not be allowed log in
     * using this registry.
     *
     * @param tenantId tenant id of the user tenant.
     *
     * @return User registry for system user.
     * @throws RegistryException
     */
    public UserRegistry getSystemRegistry(int tenantId) throws RegistryException {

        return getSystemRegistry(tenantId, null);
    }

    /**
     * Returns a registry to be used for system operations. Human users should not be allowed log in
     * using this registry.
     *
     * @param tenantId tenant id of the user tenant.
     * @param chroot   to return a chrooted registry
     *
     * @return User registry for system user.
     * @throws RegistryException
     */
    public UserRegistry getSystemRegistry(int tenantId, String chroot) throws RegistryException {
        String username = CarbonConstants.REGISTRY_SYSTEM_USERNAME;

        return getUserRegistry(
                username,
                tenantId,
                chroot);
    }

    /**
     * Creates UserRegistry instances for normal users. Applications should use this method to
     * create UserRegistry instances, unless there is a specific need documented in other methods.
     * User name and the password will be authenticated by the EmbeddedRegistry before creating the
     * requested UserRegistry instance.
     *
     * @param userName User name of the user.
     * @param password Password of the user.
     *
     * @return UserRegistry instance for the given user.
     * @throws RegistryException
     */
    public UserRegistry getUserRegistry(String userName, String password) throws RegistryException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(userName);
            userName = MultitenantUtils.getTenantAwareUsername(userName);
            int tenantId = MultitenantConstants.SUPER_TENANT_ID;
            if (tenantDomain != null &&
            		!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            }
            return getUserRegistry(userName, password, tenantId);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed in retrieving the tenant id for the tenant domain for " + userName;
            log.error(msg);
            throw new RegistryException(msg, e);
        }
    }

    /**
     * Creates UserRegistry instances for normal users. Applications should use this method to
     * create UserRegistry instances, unless there is a specific need documented in other methods.
     * User name and the password will be authenticated by the EmbeddedRegistry before creating the
     * requested UserRegistry instance.
     *
     * @param userName User name of the user.
     * @param password Password of the user.
     * @param tenantId Tenant id of the user tenant.
     *
     * @return UserRegistry instance for the given user.
     * @throws RegistryException
     */
    public UserRegistry getUserRegistry(String userName, String password, int tenantId)
            throws RegistryException {
        return getUserRegistry(userName, password, tenantId, null);
    }

    /**
     * Creates UserRegistry instances for normal users. Applications should use this method to
     * create UserRegistry instances, unless there is a specific need documented in other methods.
     * User name and the password will be authenticated by the EmbeddedRegistry before creating the
     * requested UserRegistry instance.
     *
     * @param userName User name of the user.
     * @param password Password of the user.
     * @param tenantId Tenant id of the user tenant.
     * @param chroot   to return a chrooted registry
     *
     * @return UserRegistry instance for the given user.
     * @throws RegistryException
     */
    public UserRegistry getUserRegistry(String userName, String password,
                                        int tenantId, String chroot) throws RegistryException {
        String concatenatedChroot = RegistryUtils.concatenateChroot(this.chroot, chroot);
        return new UserRegistry(userName,
                password,
                tenantId,
                embeddedRegistry,
                realmService,
                concatenatedChroot);
    }

    /**
     * Creates a UserRegistry instance for the given user. This method will NOT authenticate the
     * user before creating the UserRegistry instance. It assumes that the user is authenticated
     * outside the EmbeddedRegistry.
     *
     * @param userName User name of the user.
     *
     * @return UserRegistry instance for the given user.
     * @throws RegistryException
     */
    public UserRegistry getUserRegistry(String userName) throws RegistryException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(userName);
            userName = MultitenantUtils.getTenantAwareUsername(userName);
            int tenantId = MultitenantConstants.SUPER_TENANT_ID;
            if (tenantDomain != null &&
            		!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            }
            return getUserRegistry(userName, tenantId);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed in retrieving the tenant id for the tenant for user " + userName;
            log.error(msg);
            throw new RegistryException(msg, e);
        }
    }

    /**
     * Creates a UserRegistry instance for the given user. This method will NOT authenticate the
     * user before creating the UserRegistry instance. It assumes that the user is authenticated
     * outside the EmbeddedRegistry.
     *
     * @param userName User name of the user.
     * @param tenantId Tenant id of the user tenant.
     *
     * @return UserRegistry instance for the given user.
     * @throws RegistryException
     */
    public UserRegistry getUserRegistry(String userName, int tenantId) throws RegistryException {
        return getUserRegistry(userName, tenantId, null);
    }

    /**
     * Creates a UserRegistry instance for the given user. This method will NOT authenticate the
     * user before creating the UserRegistry instance. It assumes that the user is authenticated
     * outside the EmbeddedRegistry.
     *
     * @param userName User name of the user.
     * @param tenantId Tenant id of the user tenant.
     * @param chroot   to return a chrooted registry
     *
     * @return UserRegistry instance for the given user.
     * @throws RegistryException
     */
    public UserRegistry getUserRegistry(String userName, int tenantId, String chroot)
            throws RegistryException {
        String concatenatedChroot = RegistryUtils.concatenateChroot(this.chroot, chroot);
        return new UserRegistry(userName,
                tenantId,
                embeddedRegistry,
                realmService,
                concatenatedChroot);
    }

    public UserRealm getUserRealm(int tenantId) throws RegistryException {
        try {
            UserRealm realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
            RegistryRealm regRealm = new RegistryRealm(realm);
            return regRealm;
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new RegistryException(e.getMessage(), e);
        }
    }

    public UserRegistry getRegistry(String userName, int tenantId, String chroot)
            throws RegistryException {
        return getUserRegistry(userName, tenantId, chroot);
    }

    public UserRegistry getRegistry(String userName, String password, int tenantId, String chroot)
            throws RegistryException {
        return getUserRegistry(userName, password, tenantId, chroot);
    }

    public UserRegistry getRegistry() throws RegistryException {
        return getRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
    }

    public UserRegistry getRegistry(String userName) throws RegistryException {
        return getRegistry(userName, MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getRegistry(String userName, int tenantId) throws RegistryException {
        return getRegistry(userName, tenantId, null);
    }

    public UserRegistry getRegistry(String userName, String password) throws RegistryException {
        return getRegistry(userName, password, MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getRegistry(String userName, String password, int tenantId)
            throws RegistryException {
        return getRegistry(userName, password, tenantId, null);
    }

    public UserRegistry getLocalRepository() throws RegistryException {
        return getLocalRepository(MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getLocalRepository(int tenantId) throws RegistryException {
        return getSystemRegistry(tenantId, RegistryConstants.LOCAL_REPOSITORY_BASE_PATH);
    }

    public UserRegistry getConfigSystemRegistry(int tenantId) throws RegistryException {
        return getSystemRegistry(tenantId, RegistryConstants.CONFIG_REGISTRY_BASE_PATH);
    }

    public UserRegistry getConfigSystemRegistry() throws RegistryException {
        return getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getConfigUserRegistry(String userName, int tenantId)
            throws RegistryException {
        return getRegistry(userName, tenantId, RegistryConstants.CONFIG_REGISTRY_BASE_PATH);
    }

    public UserRegistry getConfigUserRegistry(String userName, String password, int tenantId)
            throws RegistryException {
        return getRegistry(userName, password, tenantId,
                RegistryConstants.CONFIG_REGISTRY_BASE_PATH);
    }

    public UserRegistry getConfigUserRegistry() throws RegistryException {
        return getConfigUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
    }

    public UserRegistry getConfigUserRegistry(String userName) throws RegistryException {
        return getConfigUserRegistry(userName, MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getConfigUserRegistry(String userName, String password)
            throws RegistryException {
        return getConfigUserRegistry(userName, password, MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getGovernanceSystemRegistry(int tenantId) throws RegistryException {
        return getSystemRegistry(tenantId, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
    }

    public UserRegistry getGovernanceSystemRegistry() throws RegistryException {
        return getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getGovernanceUserRegistry(String userName, int tenantId)
            throws RegistryException {
        return getRegistry(userName, tenantId, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
    }

    public UserRegistry getGovernanceUserRegistry(String userName, String password, int tenantId)
            throws RegistryException {
        return getRegistry(userName, password, tenantId,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
    }

    public UserRegistry getGovernanceUserRegistry() throws RegistryException {
        return getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
    }

    public UserRegistry getGovernanceUserRegistry(String userName) throws RegistryException {
        return getGovernanceUserRegistry(userName, MultitenantConstants.SUPER_TENANT_ID);
    }

    public UserRegistry getGovernanceUserRegistry(String userName, String password)
            throws RegistryException {
        return getGovernanceUserRegistry(userName, password, MultitenantConstants.SUPER_TENANT_ID);
    }
}
