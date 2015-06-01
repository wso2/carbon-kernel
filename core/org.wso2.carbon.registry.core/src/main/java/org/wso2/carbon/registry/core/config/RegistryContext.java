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

package org.wso2.carbon.registry.core.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Aspect;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.Repository;
import org.wso2.carbon.registry.core.jdbc.VersionRepository;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDataAccessManager;
import org.wso2.carbon.registry.core.jdbc.handlers.CustomEditManager;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerLifecycleManager;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerManager;
import org.wso2.carbon.registry.core.jdbc.queries.QueryProcessorManager;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.statistics.StatisticsCollector;
import org.wso2.carbon.registry.core.utils.LogQueue;
import org.wso2.carbon.registry.core.utils.LogWriter;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class provides access to core registry configurations. Registry context is associated with
 * each mounted registry instance. The base registry context can be accessed via the getBaseInstance
 * method.
 */
@SuppressWarnings("unused")
public class RegistryContext {

    private static final Log log = LogFactory.getLog(RegistryContext.class);
    
    /**
     * Classes which are allowed to directly call secured methods in this class
     *
     * Note that we have to use the String form of the class name and not, for example,
     * RegistryResolver.class.getName() since this may unnecessarily cause NoClassDefFoundErrors
     */
    private static final List<String> ALLOWED_CLASSES =
            Arrays.asList(RegistryContext.class.getName(),
                          "org.wso2.carbon.registry.core.ResourceImpl",
                          "org.wso2.carbon.registry.core.jdbc.EmbeddedRegistry",
                          "org.wso2.carbon.registry.core.jdbc.Repository",
                          "org.wso2.carbon.registry.core.jdbc.dao.JDBCLogsDAO",
                          "org.wso2.carbon.registry.core.jdbc.dao.JDBCPathCache");

    private static final String NODE_IDENTIFIER = UUIDGenerator.generateUUID();

    private static volatile List<StatisticsCollector> statisticsCollectors =
            new LinkedList<StatisticsCollector>();

    ////////////////////////////////////////////////////////
    // Items that are stored only on the Base Registry
    // Context.
    ////////////////////////////////////////////////////////

    private String resourceMediaTypes = null;
    private String collectionMediaTypes = null;
    private String customUIMediaTypes = null;

    ////////////////////////////////////////////////////////
    // Items that are common to any RegistryContext
    ////////////////////////////////////////////////////////

    private RealmService realmService;

    @SuppressWarnings("deprecation")
    private RegURLSupplier urlSupplier;

    private DataBaseConfiguration defaultDataBaseConfiguration = null;
    private Map<String, DataBaseConfiguration> dbConfigs =
            new HashMap<String, DataBaseConfiguration>();
    private HandlerLifecycleManager handlerManager = new HandlerLifecycleManager();
    private CustomEditManager customEditManager = new CustomEditManager();
    private Map aspects = new HashMap();
    private boolean versionOnChange;
    private int maxCache;
    private List<RemoteConfiguration> remoteInstances = new ArrayList<RemoteConfiguration>();
    private List<Mount> mounts = new ArrayList<Mount>();
    private List<QueryProcessorConfiguration> queryProcessors =
            new ArrayList<QueryProcessorConfiguration>();
    private String profilesPath = RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
            RegistryConstants.PROFILES_PATH;
    private String servicePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
            RegistryConstants.GOVERNANCE_SERVICE_PATH;
    //OSGi bundle context
    private LogWriter logWriter = null;
    private boolean enableCache = false;

    private List<String> systemResourcePaths = new ArrayList<String>();
    private List<Pattern> noCachePaths = new ArrayList<Pattern>();

    /**
     * As long as this instance remains in memory, it will be used.
     */
    private static RegistryContext registryContext = null;

    ////////////////////////////////////////////////////////
    // Items that are specific to given RegistryContext that
    // are manually specified.
    ////////////////////////////////////////////////////////

    private String registryRoot;
    private boolean readOnly;
    private DataAccessManager dataAccessManager = null;
    private boolean setup = true;
    private boolean clone = false;

    ////////////////////////////////////////////////////////
    // Items that are specific to given RegistryContext that
    // are not manually specified.
    ////////////////////////////////////////////////////////

    private Repository repository;
    private VersionRepository versionRepository;
    private QueryProcessorManager queryProcessorManager;
    private EmbeddedRegistryService embeddedRegistryService;

    /**
     * Determines whether to setup the Registry on start up. Setup actions: Create Registry database
     * tables
     */
//    private JdbcDirectory jdbcDir;

    /**
     * Get a unique identifier for this registry node. This is used to establish uniqueness among
     * multiple nodes of a cluster or a multi-product deployment. Please node that this identifier
     * is not persisted, and its regenerated once per each restart.
     *
     * @return the unique identifier of this node.
     */
    public String getNodeIdentifier() {
        return NODE_IDENTIFIER;
    }

    /**
     * Method to obtain resource media types.
     *
     * @return the resource media types.
     */
    public String getResourceMediaTypes() {
        return RegistryContext.getBaseInstance().resourceMediaTypes;
    }

    /**
     * Method to set resource media types.
     *
     * @param resourceMediaTypes the resource media types.
     */
    public void setResourceMediaTypes(String resourceMediaTypes) {
        RegistryContext.getBaseInstance().resourceMediaTypes = resourceMediaTypes;
    }

    /**
     * Method to obtain collection media types.
     *
     * @return the collection media types.
     */
    public String getCollectionMediaTypes() {
        return RegistryContext.getBaseInstance().collectionMediaTypes;
    }

    /**
     * Method to set collection media types.
     *
     * @param collectionMediaTypes the collection media types.
     */
    public void setCollectionMediaTypes(String collectionMediaTypes) {
        RegistryContext.getBaseInstance().collectionMediaTypes = collectionMediaTypes;
    }

    /**
     * Method to obtain custom UI media types.
     *
     * @return the custom UI media types.
     */
    public String getCustomUIMediaTypes() {
        return RegistryContext.getBaseInstance().customUIMediaTypes;
    }

    /**
     * Method to set custom UI media types.
     *
     * @param customUIMediaTypes the custom UI media types.
     */
    public void setCustomUIMediaTypes(String customUIMediaTypes) {
        RegistryContext.getBaseInstance().customUIMediaTypes = customUIMediaTypes;
    }

    /**
     * The interface to change the url supplement logic
     */
    @Deprecated
    public interface RegURLSupplier {

        String getURL();
    }

    /**
     * To check whether this is a clone or a base registry context.
     *
     * @return true if this is clone, false if this is the base registry context.
     */
    public boolean isClone() {
        return clone;
    }

    /**
     * Set the flag if the current registry context is a clone and not base
     *
     * @param clone whether it is a clone or not
     */
    public void setClone(boolean clone) {
        this.clone = clone;
    }

    /**
     * Get an instance of the base (not cloned),
     *
     * @return base registry context
     */
    public static RegistryContext getBaseInstance() {
        return registryContext;
    }

    /**
     * Create an return a registry context.
     *
     * @return new registry context
     */
    public static RegistryContext getCloneContext() {
        RegistryContext context = new RegistryContext();
        context.setClone(true);
        return context;
    }

    /**
     * destroy the registry context
     */
    public static void destroy() {
        // setBaseInstance will do the necessary security check.
        setBaseInstance(null);
    }
    
    /**
     * Return a singleton object of the base registry context with custom realm service If a
     * registry context doesn't exist, it will create a new one and return it. Otherwise it will
     * create the current base registry context
     *
     * @param realmService realm service
     *
     * @return the base registry context
     */
    public static RegistryContext getBaseInstance(RealmService realmService) {
        return getBaseInstance(realmService, true);
    }

    /**
     * Return a singleton object of the base registry context with custom realm service If a
     * registry context doesn't exist, it will create a new one and return it. Otherwise it will
     * create the current base registry context
     *
     * @param realmService          realm service
     * @param populateConfiguration whether the configuration must be populated or not.
     *
     * @return the base registry context
     */
    public static RegistryContext getBaseInstance(RealmService realmService, 
                                                  boolean populateConfiguration) {
        try {
            // Return existing instance or create a new one.
            if (getBaseInstance() != null) {
                return getBaseInstance();
            }
            new RegistryContext(realmService, populateConfiguration);
        } catch (RegistryException e) {
            log.error("Unable to get instance of the registry context", e);
            return null;
        }
        return getBaseInstance();
    }

    /**
     * Return a singleton object of the registry context with a custom configuration, customer realm
     * service. If a registry context doesn't exist, it will create a new one and return it.
     * Otherwise it will create the current base registry context
     *
     * @param configStream config stream (registry.xml)
     * @param realmService realm service to create a registry context with
     *
     * @return the registry context
     */
    public static RegistryContext getBaseInstance(InputStream configStream,
                                                  RealmService realmService) {
        try {
            // Return existing instance or create a new one.
            if (getBaseInstance() != null) {
                return getBaseInstance();
            }
            new RegistryContext(configStream, realmService);
        } catch (RegistryException e) {
            log.error("Unable to get instance of the registry context", e);
            return null;
        }
        return getBaseInstance();
    }

    /**
     * Return a singleton object of the registry context with a custom url supplier and custom
     * config. If a registry context doesn't exist, it will create a new one and return it.
     * Otherwise it will create the current base registry context
     *
     * @param configStream config stream (registry.xml)
     * @param urlSupplier  url supplier
     *
     * @return the singleton object of the registry context.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static RegistryContext getBaseInstance(InputStream configStream,
                                                  RegURLSupplier urlSupplier) {
        try {
            // Return existing instance or create a new one.
            if (getBaseInstance() != null) {
                return getBaseInstance();
            }
            new RegistryContext(configStream, urlSupplier);
        } catch (RegistryException e) {
            log.error("Unable to get instance of the registry context", e);
            return null;
        }
        return getBaseInstance();
    }

    /**
     * set a singleton
     *
     * @param context registry context
     */
    private static synchronized void setBaseInstance(RegistryContext context) {
        registryContext = context;
    }

    /**
     * Return the registry root. (configured in registry.xml)
     *
     * @return the registry root
     */
    public String getRegistryRoot() {
        return registryRoot;
    }

    /**
     * Set the registry root.
     *
     * @param registryRoot the value of the registry root
     */
    public void setRegistryRoot(String registryRoot) {
        this.registryRoot = registryRoot;
    }

    /**
     * Return whether the registry is read-only or not.
     *
     * @return true if readonly, false otherwise.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Set whether the registry is read-only or not
     *
     * @param readOnly the read-only flag
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Return whether the registry caching is enabled or not.
     *
     * @return true if enabled, false otherwise.
     */
    public boolean isCacheEnabled() {
        return enableCache;
    }

    /**
     * Set whether the registry caching is enabled or not.
     *
     * @param enableCache the enable-cache flag
     */
    public void setCacheEnabled(boolean enableCache) {
        this.enableCache = enableCache;
    }

    /**
     * Create a new registry context object with a custom realm service
     *
     * @param realmService associated realm service
     * @param populateConfiguration whether the configuration must be populated or not.
     *
     * @throws RegistryException throws if the construction failed.
     */
    protected RegistryContext(RealmService realmService, boolean populateConfiguration)
            throws RegistryException {
        this(null, realmService, populateConfiguration);
    }

    /**
     * Create a registry context with custom configuration and realm service.
     *
     * @param configStream configuration stream. (registry.xml input stream)
     * @param realmService the associated realm service
     *
     * @throws RegistryException throws if the construction failed.
     */
    protected RegistryContext(InputStream configStream, RealmService realmService)
            throws RegistryException {
        this(configStream, realmService, true);
    }
    
    /**
     * Create a registry context with custom configuration and realm service.
     *
     * @param configStream          configuration stream. (registry.xml input stream)
     * @param realmService          the associated realm service
     * @param populateConfiguration whether the configuration must be populated or not.
     *
     * @throws RegistryException throws if the construction failed.
     */
    protected RegistryContext(InputStream configStream, RealmService realmService, 
                              boolean populateConfiguration)
            throws RegistryException {
        // setBaseInstance will do the necessary security check.
        setBaseInstance(this);
        this.realmService = realmService;
        if (populateConfiguration) {
            RegistryConfigurationProcessor.populateRegistryConfig(configStream, this);
        }
    }

    /**
     * Create a registry context with custom configuration and realm service.
     *
     * @param configStream configuration stream. (registry.xml input stream)
     * @param urlSupplier  url supplier object.
     *
     * @throws RegistryException throws if the construction failed.
     */
    @SuppressWarnings("deprecation")
    protected RegistryContext(InputStream configStream, RegURLSupplier urlSupplier)
            throws RegistryException {
        // setBaseInstance will do the necessary security check.
        setBaseInstance(this);
        this.urlSupplier = urlSupplier;
        RegistryConfigurationProcessor.populateRegistryConfig(configStream, this);
    }

    /**
     * Create a registry context with default values.
     */
    protected RegistryContext() {
        RegistryContext baseContext = getBaseInstance();
        if (baseContext != null) {
            this.realmService = baseContext.realmService;
            this.urlSupplier = baseContext.urlSupplier;
            this.defaultDataBaseConfiguration = baseContext.defaultDataBaseConfiguration;
            this.dbConfigs = baseContext.dbConfigs;
            this.handlerManager = baseContext.handlerManager;
            this.customEditManager = baseContext.customEditManager;
            this.aspects = baseContext.aspects;
            this.versionOnChange = baseContext.versionOnChange;
            this.maxCache = baseContext.maxCache;
            this.profilesPath = baseContext.profilesPath;
            this.remoteInstances = baseContext.remoteInstances;
            this.mounts = baseContext.mounts;
            this.queryProcessors = baseContext.queryProcessors;
            this.servicePath = baseContext.servicePath;
            this.logWriter = baseContext.logWriter;
            this.systemResourcePaths = baseContext.systemResourcePaths;
            this.noCachePaths = baseContext.noCachePaths;
        }
        // Make sure that the setup flag is always set.
        this.setup = true;
    }

    /**
     * Return the associated realm service.
     *
     * @return realm service.
     */
    public RealmService getRealmService() {
        return realmService;
    }

    /**
     * Set a maximum entries for cache value
     *
     * @param maxCache the maximum number for cache value.
     */
    @Deprecated
    public void setMaxCache(int maxCache) {
        this.maxCache = maxCache;
    }

    /**
     * Get the number of maximum cache entries
     *
     * @return number of maximum cache entries
     */
    @Deprecated
    public int getMaxCache() {
        return this.maxCache;
    }

    /**
     * Return the repository object, which provides an interface to put, get resources to the
     * repository.
     *
     * @return the repository object
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * Set the repository object.
     *
     * @param repository the repository object.
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * Return the version repository object, which provides an interface to create versions,
     * retrieve old versions of resources
     *
     * @return a version repository object.
     */
    public VersionRepository getVersionRepository() {
        return versionRepository;
    }

    /**
     * Set a version repository object.
     *
     * @param versionRepository version repository test
     */
    public void setVersionRepository(VersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }

    /**
     * Return a query processor.
     *
     * @return the query processor object.
     */
    public QueryProcessorManager getQueryProcessorManager() {
        return queryProcessorManager;
    }

    /**
     * Set a query processor object
     *
     * @param queryProcessorManager the query processor object.
     */
    public void setQueryProcessorManager(QueryProcessorManager queryProcessorManager) {
        this.queryProcessorManager = queryProcessorManager;
    }

    /**
     * Whether the version should be created automatically on a change (only for non-collection
     * resources)
     *
     * @return true, if version is changing automatically on a change. false, otherwise.
     */
    public boolean isVersionOnChange() {
        return versionOnChange;
    }

    /**
     * Set whether the version should be created automatically on a change (only for non-collection
     * resources)
     *
     * @param versionOnChange Flag to set whether the version should be created,
     */
    public void setVersionOnChange(boolean versionOnChange) {
        this.versionOnChange = versionOnChange;
    }

    /**
     * Whether the "setup" system property is set at the start.
     *
     * @return true if the "setup" system property is set, false otherwise.
     */
    public boolean isSetup() {
        return setup;
    }

    /**
     * Set if the "setup" system property is set at the start.
     *
     * @param setup the flag for the setup property.
     */
    public void setSetup(boolean setup) {
        this.setup = setup;
    }

    /**
     * Return a embedded registry service. If there is no registry service existing, this will
     * create a registry service an return
     *
     * @return the newly create registry service.
     * @throws RegistryException throws if the retrieval of the embedded registry service is
     *                           failed.
     */
    public EmbeddedRegistryService getEmbeddedRegistryService() throws RegistryException {
        if (embeddedRegistryService == null) {
            try {
                embeddedRegistryService = new EmbeddedRegistryService(this);
            } catch (RegistryException e) {
                String msg = "Couldn't initialize EmbeddedRegistryService. " + e.getMessage();
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }
        }
        return embeddedRegistryService;
    }

    /**
     * Return the default database configuration.
     *
     * @return the default database configuration.
     */
    public DataBaseConfiguration getDefaultDataBaseConfiguration() {
        return defaultDataBaseConfiguration;
    }

    /**
     * Sets the default database configuration.
     *
     * @param dataBaseConfiguration the default database configuration.
     */
    public void setDefaultDataBaseConfiguration(DataBaseConfiguration dataBaseConfiguration) {
        this.defaultDataBaseConfiguration = dataBaseConfiguration;
    }

    /**
     * Select a database configuration among the available database configuration.
     *
     * @param dbConfigName name of the selecting database configuration.
     *
     * @return selected database configuration.
     */
    public DataBaseConfiguration selectDBConfig(String dbConfigName) {
        DataBaseConfiguration config = dbConfigs.get(dbConfigName);
        if (config == null) {
            log.error("Couldn't find db configuration '" + dbConfigName + "'");
            return null;
        }

        dataAccessManager = new JDBCDataAccessManager(config);
        return config;
    }

    /**
     * Get the available database configuration names.
     *
     * @return string iterator of available database configurations
     */
    public Iterator<String> getDBConfigNames() {
        return dbConfigs.keySet().iterator();
    }

    /**
     * Get the database configuration of a given configuration name
     *
     * @param dbConfigName database configuration name
     *
     * @return database configuration object
     */
    public DataBaseConfiguration getDBConfig(String dbConfigName) {
        return dbConfigs.get(dbConfigName);
    }

    /**
     * Add database configuration with the given name.
     *
     * @param name   the name of the database configuration.
     * @param config database configuration.
     */
    public void addDBConfig(String name, DataBaseConfiguration config) {
        // Process "$basedir$" if it's in the URL.
        String url = config.getDbUrl();
        if (url != null) {
            config.setDbUrl(url.replace("$basedir$", getBasePath()));
        }
        dbConfigs.put(name, config);
    }

    /**
     * Add an aspect of the name for a given tenant id
     *
     * @param name     name of the aspect
     * @param aspect   Aspect object
     * @param tenantId tenant id
     */
    @SuppressWarnings("unchecked")
    public void addAspect(String name, Aspect aspect, int tenantId) {
        Map tenantAspect = (Map) aspects.get(tenantId);
        if (tenantAspect == null) {
            tenantAspect = new HashMap();
        }
        tenantAspect.put(name, aspect);
        aspects.put(tenantId, tenantAspect);
    }

    /**
     * Remove an aspect with the given name for a given tenant id
     *
     * @param name     Name of the aspect
     * @param tenantId tenant id
     *
     * @return true if the aspect existed and removed, false otherwise
     */
    public boolean removeAspect(String name, int tenantId) {
        Map tenantAspect = (Map) aspects.get(tenantId);
        if (tenantAspect != null) {
            if (tenantAspect.get(name) == null) {
                return false;
            }
            tenantAspect.remove(name);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return an aspect of given name and given tenant id.
     *
     * @param name     name of the aspect
     * @param tenantId tenant id
     *
     * @return the aspect object.
     */
    public Aspect getAspect(String name, int tenantId) {
        Map tenantAspect = (Map) aspects.get(tenantId);
        if (tenantAspect != null) {
            return (Aspect) tenantAspect.get(name);
        } else {
            return null;
        }
    }

    /**
     * Return engaged aspect names for a given tenant
     *
     * @param tenantId tenant id
     *
     * @return the array of aspects.
     */
    @SuppressWarnings("unchecked")
    public String[] getAspectNames(int tenantId) {
        Map tenantAspect = (Map) aspects.get(tenantId);
        if (tenantAspect != null) {
            final Set aspectNames = tenantAspect.keySet();
            return (String[]) aspectNames.toArray(new String[aspectNames.size()]);
        } else {
            return null;
        }
    }

    /**
     * Return a list of available query processor.
     *
     * @return list of query processor
     */
    public List getQueryProcessors() {
        return queryProcessors;
    }

    /**
     * Set the query processor list.
     *
     * @param queryProcessors the list of query processors to be set.
     */
    public void setQueryProcessors(List<QueryProcessorConfiguration> queryProcessors) {
        this.queryProcessors = queryProcessors;
    }

    /**
     * Add a new query processor.
     *
     * @param queryProcessorConfiguration query processor to be set.
     */
    public void addQueryProcessor(QueryProcessorConfiguration queryProcessorConfiguration) {
        queryProcessors.add(queryProcessorConfiguration);
    }

    /**
     * Return the base path calculated using the url supplier.
     *
     * @return the base path calculated using the url supplier.
     */
    public String getBasePath() {
        String basePath = null;
        if (urlSupplier != null) {
            basePath = urlSupplier.getURL();
        }
        if (basePath == null) {
            basePath = System.getProperty("basedir", "");
        }
        return basePath;
    }

    /**
     * Return the data access manager, created using the database configuration associated with the
     * registry context
     *
     * @return the data access manager
     */
    public DataAccessManager getDataAccessManager() {
        return dataAccessManager;
    }

    /**
     * Set the data access manager.
     *
     * @param dataAccessManager data access manager to be set.
     */
    public void setDataAccessManager(DataAccessManager dataAccessManager) {
        this.dataAccessManager = dataAccessManager;
    }

    /**
     * Return the handler manager.
     *
     * @return handler manager
     */
    public HandlerManager getHandlerManager() {
        return handlerManager;
    }

    /**
     * Return the handler manager.
     *
     * @param lifecyclePhase The name of the lifecycle phase.
     *
     * @return handler manager
     */
    public HandlerManager getHandlerManager(String lifecyclePhase) {
        return handlerManager.getHandlerManagerForPhase(lifecyclePhase);
    }

    /**
     * Return a custom edit manager, which is used by custom UI implementations.
     *
     * @return the CustomEditManager object.
     */
    public CustomEditManager getCustomEditManager() {
        return customEditManager;
    }

    /**
     * Set a custom edit manager.
     *
     * @param customEditManager the CustomEditManager to be set.
     */
    public void setCustomEditManager(CustomEditManager customEditManager) {
        this.customEditManager = customEditManager;
    }

//    /**
//     * Set the JDBC dir. Uses for indexing.
//     *
//     * @param jdbcDir JDBC dir to set.
//     */
//    public void setJdbcDir(JdbcDirectory jdbcDir) {
//        this.jdbcDir = jdbcDir;
//    }

//    /**
//     * Return the JDBC dir. Uses for indexing.
//     *
//     * @return the JDBC dir.
//     */
//    public JdbcDirectory getJdbcDir() {
//        return jdbcDir;
//    }

    /**
     * Return a list of mounted remote instances.
     *
     * @return remote instance list.
     */
    public List<RemoteConfiguration> getRemoteInstances() {
        return remoteInstances;
    }

    /**
     * Set list of remote instances.
     *
     * @param remoteInstances the list of remote instances to be set.
     */
    public void setRemoteInstances(List<RemoteConfiguration> remoteInstances) {
        this.remoteInstances = remoteInstances;
    }

    /**
     * Return a list of mounted registry configurations (Mount object).
     *
     * @return a list of mount
     */
    public List<Mount> getMounts() {
        return mounts;
    }

    /**
     * Set a list of mounted registry configurations.
     *
     * @param mounts list of mount to be set.
     */
    public void setMounts(List<Mount> mounts) {
        this.mounts = mounts;
    }

    /**
     * Set profile storage path.
     *
     * @param path the path to be set
     */
    public void setProfilesPath(String path) {
        this.profilesPath = path;
    }

    /**
     * Return the profile storage path.
     *
     * @return path of the profile
     */
    public String getProfilesPath() {
        return this.profilesPath;
    }

    /**
     * Get the service storage path.
     *
     * @return the service path.
     */
    public String getServicePath() {
        return servicePath;
    }

    /**
     * Set the service storage path
     *
     * @param servicePath service path to be set.
     */
    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }   

	@Deprecated
    @SuppressWarnings("deprecation")
    public List<HandlerConfiguration> getHandlerConfigurations() {
        throw new UnsupportedOperationException("This method is no longer supported.");
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public void setHandlerConfigurations(List<HandlerConfiguration> handlerConfigurations) {
        throw new UnsupportedOperationException("This method is no longer supported.");
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public void addHandlerConfiguration(HandlerConfiguration handlerConfiguration) {
        throw new UnsupportedOperationException("This method is no longer supported.");
    }

    @Deprecated
    public List getMediaTypeHandlers() {
        throw new UnsupportedOperationException("This method is no longer supported.");
    }

    @Deprecated
    public void setMediaTypeHandlers(List mediaTypeHandlers) {
        throw new UnsupportedOperationException("This method is no longer supported.");
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public void addMediaTypeHandler(MediaTypeHandlerConfiguration mediaTypeHandlerConfiguration) {
        throw new UnsupportedOperationException("This method is no longer supported.");
    }

    @Deprecated
    public List getUrlHandlers() {
        throw new UnsupportedOperationException("This method is no longer supported.");
    }

    @Deprecated
    public void setUrlHandlers(List urlHandlers) {
        throw new UnsupportedOperationException("This method is no longer supported.");
    }

    @Deprecated
    public void addURLHandler(String urlHandler) {
        throw new UnsupportedOperationException("This method is no longer supported.");
    }

    /**
     * Method to obtain the logWriter instance.
     * @return the logWriter instance.
     */
    public LogWriter getLogWriter() {
        if (logWriter == null) {
            logWriter = new LogWriter(new LogQueue(), dataAccessManager);
            logWriter.start();
        }
        return logWriter;
    }

    /**
     * Method to set the logWriter instance.
     * @param logWriter the logWriter instance.
     */
    public void setLogWriter(LogWriter logWriter) {
        this.logWriter = logWriter;
    }

    /**
     * Method to determine whether a system resource (or collection) path has been registered.
     *
     * @param absolutePath the absolute path of the system resource (or collection)
     *
     * @return true if the system resource (or collection) path is registered or false if not.
     */
    public boolean isSystemResourcePathRegistered(String absolutePath) {
        return systemResourcePaths.contains(CurrentSession.getTenantId() + ":" + absolutePath);
    }

    /**
     * Method to register a system resource (or collection) path.
     *
     * @param absolutePath the absolute path of the system resource (or collection)
     */
    public void registerSystemResourcePath(String absolutePath) {
        systemResourcePaths.add(CurrentSession.getTenantId() + ":" + absolutePath);
    }

    /**
     * Method to determine whether caching is disabled for the given path.
     *
     * @param path the path to test
     *
     * @return true if caching is disabled or false if not.
     */
    public boolean isNoCachePath(String path) {
        for (Pattern noCachePath : noCachePaths) {
            if (noCachePath.matcher(path).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to register a no-cache path. If caching is disabled for a collection, all downstream
     * resources and collections won't be cached.
     *
     * @param path the path of a resource (or collection) for which caching is disabled.
     */
    public void registerNoCachePath(String path) {
        noCachePaths.add(Pattern.compile(Pattern.quote(path) + "($|" +
                RegistryConstants.PATH_SEPARATOR + ".*|" +
                RegistryConstants.URL_SEPARATOR + ".*)"));
    }

    /**
     * Method to obtain a list of statistics collectors.
     *
     * @return array of statistics collectors if one or more statistics collectors exist, or an
     * empty array.
     */
    public StatisticsCollector[] getStatisticsCollectors() {
        return statisticsCollectors.isEmpty() ? new StatisticsCollector[0] :
                statisticsCollectors.toArray(new StatisticsCollector[statisticsCollectors.size()]);
    }

    /**
     * Method to add a statistics collector
     *
     * @param statisticsCollector the statistics collector to be added.
     */
    public void addStatisticsCollector(StatisticsCollector statisticsCollector) {
        statisticsCollectors.add(statisticsCollector);
    }

    /**
     * Method to remove a statistics collector
     *
     * @param statisticsCollector the statistics collector to be removed.
     */
    public void removeStatisticsCollector(StatisticsCollector statisticsCollector) {
        statisticsCollectors.remove(statisticsCollector);
    }

}
