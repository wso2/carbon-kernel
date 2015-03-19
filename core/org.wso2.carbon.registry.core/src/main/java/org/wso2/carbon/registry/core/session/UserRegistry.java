/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.caching.CacheBackedRegistry;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.dao.ResourceDAO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistry;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.Reader;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Maintains information required for particular user. When registry functionality is invoked
 * through this objects of this class, user's information is passes along the thread of invocation.
 * Therefore, all the actions are recorded against that user's name and all authorizations are
 * checked using that user's realm. Note that, although this class maintains user details, it does
 * not perform any authorization. Underlying registry or other entities in the thread of invocation
 * should perform authorizations as needed using the realm given by this class. In addition to
 * associating user with the registry, this will be used to get a chrooted registry, as the new
 * constructors accept chroot as the last parameter.
 */
public class UserRegistry implements Registry {

    private static final Log log = LogFactory.getLog(UserRegistry.class);

    /**
     * User name of the user. All actions done using this registry will be recorded against this
     * user name.
     */
    private String userName;

    /**
     * tenant id of the registry. All actions done using this registry will be recorded against this
     * tenant id.
     */
    private int tenantId;

    /**
     * tenant id of the user. this can be not equal to tenantId. required when system registry is
     * used where tenantId is always MultitenantConstants.SUPER_TENANT_ID, but the caller's tenant
     * id can be different.
     */
    private int callerTenantId;

    /**
     * The realm service that provides the realm per tenant
     */
    private RealmService realmService;

    /**
     * Realm of the user. All actions done using this registry will be authorized using this realm.
     */
    private UserRealm userRealm;

    /**
     * Core registry to delegate the requests. All UserRegistry instances share this core registry.
     */
    private Registry coreRegistry;

    /**
     * functionality related to chroot.
     */
    private ChrootWrapper chrootWrapper;

    /**
     * Whether core registry is an APP remote registry instance.
     */
    private boolean isRemoteRegistry;

    /**
     * Create a user registry with authorizing a user
     *
     * @param userName     the username of the user accessing
     * @param password     the password credentials
     * @param tenantId     the tenant the user belong to
     * @param coreRegistry either RemoteRegistry or the EmbeddedRegistry
     * @param realmService the realm provided by user manager
     * @param chroot       the base prefix if the registry needed to chrooted, if provided null
     *                     non-chroot registry will be constructed.
     *
     * @throws RegistryException if the creation of this instance failed.
     */
    public UserRegistry(final String userName, final String password, final int tenantId,
                        final Registry coreRegistry, final RealmService realmService, final String chroot)
            throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws Exception {
                    init(userName, password, tenantId, coreRegistry, realmService, chroot);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void init(String userName, String password, int tenantId,
                Registry coreRegistry, RealmService realmService, String chroot)
                throws RegistryException {

        String tenantUserName = userName;
        if (realmService == null) {
            String msg = "Unable to create an instance of a UserRegistry. The realm service was" +
                    "not specified.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        try {
            if (!realmService.getTenantManager().isTenantActive(tenantId)) {
                // the tenant is not active.
                String msg = "The tenant is not active. tenant id: " + tenantId + ".";
                log.error(msg);
                throw new RegistryException(msg);
            }
            UserRealm tempUserRealm;
            try {
                tempUserRealm = (UserRealm) realmService.getTenantUserRealm(tenantId);
            } catch (Exception e) {
                String msg = "Failed in getting the user realm for the tenant id: " + tenantId;
                log.error(msg);
                throw new RegistryException(msg, e);
            }
            UserStoreManager authenticator;
            try {
                authenticator = tempUserRealm.getUserStoreManager();
            } catch (Exception e) {
                String msg = "Failed in getting the user realm for the tenant id: " + tenantId;
                log.error(msg);
                throw new RegistryException(msg, e);
            }
            int tempTenantId = authenticator.getTenantId();
            if (tempTenantId != MultitenantConstants.INVALID_TENANT_ID &&
            		tempTenantId != MultitenantConstants.SUPER_TENANT_ID) {
                tenantUserName = MultitenantUtils.getTenantAwareUsername(userName);
            }
            if (!authenticator.authenticate(tenantUserName, password)) {
                String msg = "Attempted to authenticate invalid user.";
                log.warn(msg);
                throw new AuthorizationFailedException(msg);
            }

        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Could not obtain the authenticator for authenticating the user " +
                    tenantUserName + ". \nCaused by " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        init(tenantUserName, tenantId, coreRegistry, realmService, chroot, false);
    }

    /**
     * returns a chrooted registry for a given prefix
     *
     * @param chroot the prefix to be chrooted
     *
     * @return UserRegistry chrooted to the provided prefix
     * @throws RegistryException if the operation instance failed.
     */
    // This method is used outside the registry kernel.
    public UserRegistry getChrootedRegistry(String chroot) throws RegistryException {
        String newChroot = RegistryUtils.concatenateChroot(chrootWrapper.getBasePrefix(), chroot);
        return new UserRegistry(userName, tenantId, coreRegistry,
                realmService, newChroot);
    }

    /**
     * Creates a user registry without authorizing the user
     *
     * @param userName     the username of the user accessing
     * @param tenantId     the tenant the user belong to
     * @param coreRegistry either RemoteRegistry or the EmbeddedRegistry
     * @param realmService the realm provided by user manager
     * @param chroot       the base prefix if the registry needed to chrooted, if provided null
     *                     non-chroot registry will be constructed.
     *
     * @throws RegistryException if the creation of this instance failed.
     */
    public UserRegistry(
            String userName, int tenantId, Registry coreRegistry,
            RealmService realmService, String chroot) throws RegistryException {

        this(userName, tenantId, coreRegistry, realmService, chroot, false);
    }

    /**
     * Creates a user registry without authorizing the user
     *
     * @param userName       the username of the user accessing
     * @param tenantId       the tenant the user belong to
     * @param coreRegistry   either RemoteRegistry or the EmbeddedRegistry
     * @param realmService   the realm provided by user manager
     * @param chroot         the base prefix if the registry needed to chrooted, if provided null
     *                       non-chroot registry will be constructed.
     * @param disableCaching whether caching is to be turned off.
     *
     * @throws RegistryException if the creation of this instance failed.
     */
    public UserRegistry(
            final String userName, final int tenantId, final Registry coreRegistry,
            final RealmService realmService, final String chroot, final boolean disableCaching)
            throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws Exception {
                    init(userName, tenantId, coreRegistry, realmService, chroot, disableCaching);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    // Initializes the user registry.
    private void init(
            String userName, int tenantId, Registry registry,
            RealmService realmService, String chroot, boolean disableCaching)
            throws RegistryException {

        RegistryContext registryContext = registry.getRegistryContext();

        if (!disableCaching && registryContext.isCacheEnabled() &&
                registry instanceof EmbeddedRegistry) {
            this.coreRegistry = new CacheBackedRegistry(registry, tenantId);
        } else {
            this.coreRegistry = registry;
        }
        this.isRemoteRegistry = !(registry instanceof EmbeddedRegistry);
        this.chrootWrapper = new ChrootWrapper(chroot);
        this.callerTenantId = tenantId;
        this.tenantId = tenantId;
        this.realmService = realmService;

        // TODO : This code needs some careful thought, and fixing.
        this.userName = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        if (realmService != null) {
            try {
                this.userRealm = new RegistryRealm(realmService.getBootstrapRealm());
            } catch (Exception e) {
                String msg = "Failed in getting the bootstrap realm for the tenantId: " + tenantId
                        + ".";
                log.error(msg);
                throw new RegistryException(msg, e);
            }
        }

        if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            try {
                UserRealm realm = null;
                if (realmService != null) {
                    realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
                }else{
                    //if the realm service is null, I am getting it from the service component
                    // and set it here
                    realmService = RegistryCoreServiceComponent.getRealmService();
                    realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
                }
                if (realm == null) {
                    String msg = "Failed to obtain the user realm for tenant: " + tenantId + ".";
                    throw new RegistryException(msg);
                }
                this.userRealm = new RegistryRealm(realm);
            } catch (UserStoreException e) {
                String msg = "An error occurred while obtaining the user realm for the tenant: " +
                        tenantId + ".";
                log.error(msg);
                throw new RegistryException(msg, e);
            }
        }


        // this is to get a system registry
        try {
            setSessionInformation();
            // The root must be setup, in case the remote registry does not have it.
            if (registryContext.isClone()) {
                try {
                    addRootCollection();
                } catch (Exception ignored) {
                    // There can be situations where the remote instance doesn't like creating the
                    // root collection, if it is already there. Hence, we can simply ignore this.
                }
            } else {
                addRootCollection();
            }
            if (!registryContext.isClone()) {
                addSystemCollections();
            }
        } finally {
            clearSessionInformation();
        }
        this.userName = userName;
        // Registering the mount points will not be done in the User Registry but at the Registry
        // Service level to avoid excessive duplication.
    }


    // This will add the initial system collections to the registry..
    // system collections = /, /_system, /_system/config, /_system/governance/services, etc.
    private void addSystemCollections() throws RegistryException {
        if (userRealm == null) {
            log.debug("The UserRealm is not available. The system collection will not be added.");
            return;
        }
        if (log.isTraceEnabled()) {
            log.trace("adding system collections.");
        }

        try {
            setSessionInformation();

            // Adding base collection structure.
            RegistryUtils.addBaseCollectionStructure(coreRegistry, this.userRealm);
        } finally {
            clearSessionInformation();
        }
    }

    // Adds the root collection, if it doesn't exist.
    // This returns true if the root collection is added, false if it is already existing.
    private void addRootCollection() throws RegistryException {
    	boolean isTransactionStarted = false;
    	
        try {

            if (isRemoteRegistry || coreRegistry.getRegistryContext() == null
                    || coreRegistry.getRegistryContext().getDataAccessManager() == null) {
                // Atom-based RemoteRegistry or WSRegistryServiceClient
            	isTransactionStarted = startTransaction(isTransactionStarted);
                if (userRealm != null) {
                    AuthorizationUtils
                            .setRootAuthorizations(RegistryConstants.ROOT_PATH, userRealm);
                }
            } else {
                ResourceDAO resourceDAO = coreRegistry.getRegistryContext().getDataAccessManager().
                        getDAOManager().getResourceDAO();
                if (log.isTraceEnabled()) {
                    log.trace("Checking the existence of the root collection of the Registry.");
                }
                boolean addAuthorizations =
                        !(RegistryContext.getBaseInstance().isSystemResourcePathRegistered(
                                RegistryConstants.ROOT_PATH));
                if (!RegistryContext.getBaseInstance().isSystemResourcePathRegistered(RegistryConstants.ROOT_PATH)) {
                    isTransactionStarted = startTransaction(isTransactionStarted);
                }
                if (RegistryUtils.systemResourceShouldBeAdded(resourceDAO,
                        RegistryConstants.ROOT_PATH)) {

                    if (log.isTraceEnabled()) {
                        log.trace("Creating the root collection of the Registry.");
                    }
                    isTransactionStarted = startTransaction(isTransactionStarted);
                    
                    CollectionImpl root = new CollectionImpl();
                    root.setUUID(UUID.randomUUID().toString());
                    resourceDAO.addRoot(root);
                }
                if (addAuthorizations && userRealm != null) {    
                	isTransactionStarted = startTransaction(isTransactionStarted);
                    AuthorizationUtils
                            .setRootAuthorizations(RegistryConstants.ROOT_PATH, userRealm);
                }
                String chroot = chrootWrapper.getBasePrefix();
                if (chroot != null &&
                        !chroot.equals(RegistryConstants.ROOT_PATH)) {
                	if (!RegistryContext.getBaseInstance().isSystemResourcePathRegistered(chroot)) {
                        isTransactionStarted = startTransaction(isTransactionStarted);
                    }
                    if (RegistryUtils.systemResourceShouldBeAdded(resourceDAO, chroot)) {
                    	isTransactionStarted = startTransaction(isTransactionStarted);
                        CollectionImpl chrootColl = new CollectionImpl();
                        put(RegistryConstants.ROOT_PATH,
                                chrootColl); // this will be extract to /chroot from the put
                    }
// TODO: This code block is never reached. Need to be completely removed
// after further testing - 24/11/2011
//                    else if (!(resourceDAO.get(chroot) instanceof CollectionImpl)) {
//                        String msg = "Invalid Registry Root. The root of the registry must" +
//                                " not be a resource.";
//                        log.error(msg);
//                        throw new RegistryException(msg);
//                    }
                }
            }

            if (isTransactionStarted){
            	coreRegistry.commitTransaction();
            }

        } catch (Exception e) {

            String msg = "Failed to add the root collection to the coreRegistry.";
            log.fatal(msg, e);

            if (isTransactionStarted){
            	coreRegistry.rollbackTransaction();
            }

            throw new RegistryException(msg, e);
        }
    }
    
    private boolean startTransaction(boolean status) throws RegistryException {
        if(!status){
            coreRegistry.beginTransaction();
            status = true;
        }
        return status;
    }

    public Resource newResource() throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Resource>() {
                @Override
                public Resource run() throws Exception {
                    return newResourceInternal();
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private Resource newResourceInternal() throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation new resource.");
        }
        try {
            setSessionInformation();
            return coreRegistry.newResource();
        } finally {
            clearSessionInformation();
        }
    }

    public Collection newCollection() throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Collection>() {
                @Override
                public Collection run() throws Exception {
                    return newCollectionInternal();
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private Collection newCollectionInternal() throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation new collection.");
        }
        try {
            setSessionInformation();
            return coreRegistry.newCollection();
        } finally {
            clearSessionInformation();
        }
    }

    public void beginTransaction() throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation begin transaction.");
        }
        try {
            setSessionInformation();
            coreRegistry.beginTransaction();
        } finally {
            clearSessionInformation();
        }
    }

    public void rollbackTransaction() throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation rollback transaction.");
        }
        try {
            setSessionInformation();
            coreRegistry.rollbackTransaction();
        } finally {
            clearSessionInformation();
        }
    }

    public void commitTransaction() throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation commit transaction.");
        }
        try {
            setSessionInformation();
            coreRegistry.commitTransaction();
        } finally {
            clearSessionInformation();
        }
    }

    public RegistryContext getRegistryContext() {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get registryContext.");
        }
        try {
            setSessionInformation();
            return coreRegistry.getRegistryContext();
        } finally {
            clearSessionInformation();
        }
    }

    /**
     * Method to obtain the user name.
     *
     * @return the user name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Method to obtain the tenant identifier.
     *
     * @return the tenant identifier.
     */
    public int getTenantId() {
        return tenantId;
    }

    /**
     * Method to obtain the caller tenant identifier.
     *
     * @return the caller tenant identifier.
     */
    public int getCallerTenantId() {
        return callerTenantId;
    }

    /**
     * Registry API users access the user realm using this method. Registry authorizations are
     * stored based on resource UUIDs. But UUIDs are meaningless to the API users. API users are
     * expected to deal only with resource paths. Therefore, this method, returns a user realm,
     * which converts paths to resource UUIDs before, any resource related operation.
     *
     * @return Resource path based registry realm.
     */
    public UserRealm getUserRealm() {
        return userRealm;
    }

    public Resource get(final String path) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Resource>() {
                @Override
                public Resource run() throws Exception {
                    return getInternal(path);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private Resource getInternal(String path) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get, " +
                    "path: " + path + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            ResourceImpl resource = (ResourceImpl) coreRegistry.get(chrootWrapper.getInPath(path));
            if (resource != null) {
                if (coreRegistry instanceof CacheBackedRegistry) {
                    if (resource instanceof CollectionVersionImpl) {
                        resource = new CollectionVersionImpl((CollectionVersionImpl) resource);
                    } else if (resource instanceof CollectionImpl) {
                        resource = new CollectionImpl((CollectionImpl) resource);
                    } else if (resource instanceof Comment) {
                        resource = new Comment((Comment) resource);
                    } else {
                        resource = new ResourceImpl(resource);
                    }
                }
                resource.setUserName(userName);
                resource.setTenantId(tenantId);
                resource.setUserRealm(userRealm);

                // removing the chrooted paths in returning values
                resource = (ResourceImpl) chrootWrapper.getOutResource(resource);
            }
            return resource;

        } finally {
            clearSessionInformation();
        }
    }

    public Resource getMetaData(final String path) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Resource>() {
                @Override
                public Resource run() throws Exception {
                    return getMetaDataInternal(path);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private Resource getMetaDataInternal(String path) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get meta data, " +
                    "path: " + path + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            ResourceImpl resource = (ResourceImpl) coreRegistry.getMetaData(chrootWrapper.getInPath(path));
            if (resource != null) {
                resource.setUserName(userName);
                resource.setTenantId(tenantId);
                resource.setUserRealm(userRealm);

                // removing the chrooted paths in returning values
                resource = (ResourceImpl) chrootWrapper.getOutResource(resource);
            }
            return resource;

        } finally {
            clearSessionInformation();
        }
    }

    public String importResource(final String suggestedPath, final String sourceURL, final org.wso2.carbon.registry.api.Resource resource) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    return importResourceInternal(suggestedPath, sourceURL, resource);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private String importResourceInternal(String suggestedPath, String sourceURL,
                                 org.wso2.carbon.registry.api.Resource resource)
            throws org.wso2.carbon.registry.api.RegistryException {
        return importResourceInternal(suggestedPath, sourceURL, (Resource) resource);
    }

    public Collection get(final String path, final int start, final int pageSize) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Collection>() {
                @Override
                public Collection run() throws Exception {
                    return getInternal(path, start, pageSize);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private Collection getInternal(String path, int start, int pageSize) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get with pagination, " +
                    "path: " + path + ", " +
                    "start: " + start + ", " +
                    "page size: " + pageSize + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            Collection collection = coreRegistry.get(chrootWrapper.getInPath(path), start, pageSize);
            if (collection != null) {
                if (coreRegistry instanceof CacheBackedRegistry
                        ) {
                    if (collection instanceof CollectionVersionImpl) {
                        collection =
                                new CollectionVersionImpl(((CollectionVersionImpl) collection));
                    } else {
                        collection = new CollectionImpl(((CollectionImpl) collection));
                    }
                }
                // collection implementation extends from the resource implementation.
                ResourceImpl resourceImpl = (ResourceImpl) collection;
                resourceImpl.setUserName(userName);
                resourceImpl.setTenantId(tenantId);
                resourceImpl.setUserRealm(userRealm);

                // removing the chrooted paths in returning values
                collection = chrootWrapper.getOutCollection(collection);
            }
            return collection;

        } finally {
            clearSessionInformation();
        }
    }

    public boolean resourceExists(final String path) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                @Override
                public Boolean run() throws Exception {
                    return resourceExistsInternal(path);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private boolean resourceExistsInternal(String path) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation resource exists, " +
                    "path: " + path + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            return coreRegistry.resourceExists(chrootWrapper.getInPath(path));

        } finally {
            clearSessionInformation();
        }
    }

    public String put(final String suggestedPath, final org.wso2.carbon.registry.api.Resource resource) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    return putInternal(suggestedPath, resource);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private String putInternal(String suggestedPath, org.wso2.carbon.registry.api.Resource resource)
            throws org.wso2.carbon.registry.api.RegistryException {
        return putInternal(suggestedPath, (Resource) resource);
    }

    public String put(final String suggestedPath, final Resource resource) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    return putInternal(suggestedPath, resource);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private String putInternal(String suggestedPath, Resource resource) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation put, " +
                    "path: " + suggestedPath + ".");
        }
        // If this node is operating in read-only mode, do not put the resource
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation put, the coreRegistry is read-only");
            }
            return suggestedPath;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            String returningPath = coreRegistry.put(chrootWrapper.getInPath(suggestedPath), resource);
            returningPath = chrootWrapper.getOutPath(returningPath);

            return returningPath;
        } finally {
            clearSessionInformation();
        }
    }

    public void delete(final String path) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    deleteInternal(path);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void deleteInternal(String path) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation delete, " +
                    "path: " + path + ".");
        }
        // If this node is operating in read-only mode, do not delete the resource
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation delete, the coreRegistry is read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.delete(chrootWrapper.getInPath(path));
        } finally {
            clearSessionInformation();
        }
    }

    public String importResource(final String suggestedPath, final String sourceURL, final Resource resource) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    return importResourceInternal(suggestedPath, sourceURL, resource);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private String importResourceInternal(String suggestedPath, String sourceURL, Resource resource)
            throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation import resource. " +
                    "path: " + suggestedPath + ".");
        }
        // If this node is operating in read-only mode, do not import the  resource
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation import resource, the coreRegistry is " +
                        "read-only");
            }
            return suggestedPath;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            String returnedPath = coreRegistry.importResource(chrootWrapper.getInPath(suggestedPath), sourceURL, resource);

            // removing the chrooted paths in returning values
            returnedPath = chrootWrapper.getOutPath(returnedPath);
            return returnedPath;
        } finally {
            clearSessionInformation();
        }
    }

    public String rename(final String currentPath, final String newName) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    return renameInternal(currentPath, newName);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private String renameInternal(String currentPath, String newPath) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation rename. " +
                    "source: " + currentPath + ", " +
                    "target: " + newPath + ".");
        }
        // If this node is operating in read-only mode, do not move the resource
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation rename, the coreRegistry is read-only");
            }
            return currentPath;
        }
        try {
            String updatedNewPath =newPath;
            // setting session information + chrooted incoming paths
            setSessionInformation();
            if (updatedNewPath.startsWith(RegistryConstants.ROOT_PATH)) {
                // this is not an absolute path, so needed to be transformed
                updatedNewPath = chrootWrapper.getInPath(newPath);
            }

            String renamedPath = coreRegistry.rename(chrootWrapper.getInPath(currentPath), updatedNewPath);

            // removing the chrooted paths in returning values
            renamedPath = chrootWrapper.getOutPath(renamedPath);
            return renamedPath;
        } finally {
            clearSessionInformation();
        }
    }

    public String move(final String currentPath, final String newPath) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    return moveInternal(currentPath, newPath);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private String moveInternal(String currentPath, String newPath) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation move. " +
                    "source: " + currentPath + ", " +
                    "target: " + newPath + ".");
        }
        // If this node is operating in read-only mode, do not move the resource
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation move, the coreRegistry is read-only");
            }
            return currentPath;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            String movedPath = coreRegistry.move(chrootWrapper.getInPath(currentPath),
                                    chrootWrapper.getInPath(newPath));

            // removing the chrooted paths in returning values
            movedPath = chrootWrapper.getOutPath(movedPath);
            return movedPath;
        } finally {
            clearSessionInformation();
        }
    }

    public String copy(final String sourcePath, final String targetPath) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    return copyInternal(sourcePath, targetPath);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private String copyInternal(String sourcePath, String targetPath) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation copy. " +
                    "source: " + sourcePath + ", " +
                    "target: " + targetPath + ".");
        }
        // If this node is operating in read-only mode, do not copy the resource
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation copy, the coreRegistry is read-only");
            }
            return sourcePath;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            String copiedPath = coreRegistry.copy(chrootWrapper.getInPath(sourcePath),
                                    chrootWrapper.getInPath(targetPath));

            // removing the chrooted paths in returning values
            copiedPath = chrootWrapper.getOutPath(copiedPath);
            return copiedPath;
        } finally {
            clearSessionInformation();
        }
    }

    public void createVersion(final String path) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    createVersionInternal(path);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void createVersionInternal(String path) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation create version, " +
                    "path: " + path + ".");
        }
        // If this node is operating in read-only mode, do not create version
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation create version, the coreRegistry is " +
                        "read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.createVersion(chrootWrapper.getInPath(path));
        } finally {
            clearSessionInformation();
        }
    }

    public String[] getVersions(final String path) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<String[]>() {
                @Override
                public String[] run() throws Exception {
                    return getVersionsInternal(path);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private String[] getVersionsInternal(String path) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get versions, " +
                    "path: " + path + ".");
        }
        String[] versionPaths;
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            versionPaths = coreRegistry.getVersions(chrootWrapper.getInPath(path));

            // removing the chrooted paths in returning values
            versionPaths = chrootWrapper.getOutPaths(versionPaths);
            return versionPaths;
        } finally {
            clearSessionInformation();
        }
    }

    public void restoreVersion(final String versionPath) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    restoreVersionInternal(versionPath);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void restoreVersionInternal(String versionPath) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation restore version, " +
                    "version path: " + versionPath + ".");
        }
        // If this node is operating in read-only mode, do not restore version
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation restore version, the coreRegistry is " +
                        "read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.restoreVersion(chrootWrapper.getInPath(versionPath));
        } finally {
            clearSessionInformation();
        }
    }

    ////////////////////////////////////////////////////////
    // Associations
    ////////////////////////////////////////////////////////
    public void addAssociation(final String sourcePath, final String targetPath, final String associationType) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    addAssociationInternal(sourcePath, targetPath, associationType);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void addAssociationInternal(String sourcePath, String targetPath, String associationType)
            throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation add association, " +
                    "source: " + sourcePath + ", " +
                    "target: " + targetPath + ", " +
                    "type: " + associationType + ".");
        }
        // If this node is operating in read-only mode, do not add association
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation add association, the coreRegistry is " +
                        "read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();
            String updatedTargetPath = targetPath;
            // Don't fix target path if it is not an external URL.
            if (!updatedTargetPath.matches("^[a-zA-Z]+://.*")) {
                updatedTargetPath = chrootWrapper.getInPath(targetPath);
            }

            coreRegistry.addAssociation(chrootWrapper.getInPath(sourcePath), updatedTargetPath, associationType);
        } finally {
            clearSessionInformation();
        }
    }

    public void removeAssociation(final String sourcePath, final String targetPath, final String associationType) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    removeAssociationInternal(sourcePath, targetPath, associationType);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void removeAssociationInternal(String sourcePath, String associationPath,
                                  String associationType)
            throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation remove association, " +
                    "source: " + sourcePath + ", " +
                    "path: " + associationPath + ", " +
                    "type: " + associationType + ".");
        }
        // If this node is operating in read-only mode, do not delete the associations
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation remove associations, the coreRegistry " +
                        "is read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();
            String updatedAssociationPath = associationPath;
            // Don't fix target path if it is not an external URL.
            if (!updatedAssociationPath.matches("^[a-zA-Z]+://.*")) {
                updatedAssociationPath = chrootWrapper.getInPath(associationPath);
            }

            coreRegistry.removeAssociation(chrootWrapper.getInPath(sourcePath), updatedAssociationPath, associationType);
        } finally {
            clearSessionInformation();
        }
    }

    public Association[] getAllAssociations(final String resourcePath) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Association[]>() {
                @Override
                public Association[] run() throws Exception {
                    return getAllAssociationsInternal(resourcePath);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private Association[] getAllAssociationsInternal(String resourcePath) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get all associations, " +
                    "path: " + resourcePath + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            Association[] associations = coreRegistry.getAllAssociations(chrootWrapper.getInPath(resourcePath));

            // removing the chrooted paths in returning values
            associations = chrootWrapper.getOutAssociations(associations);
            return associations;
        } finally {
            clearSessionInformation();
        }
    }

    public Association[] getAssociations(final String resourcePath, final String associationType) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Association[]>() {
                @Override
                public Association[] run() throws Exception {
                    return getAssociationsInternal(resourcePath, associationType);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private Association[] getAssociationsInternal(String resourcePath, String associationType)
            throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get associations, " +
                    "path: " + resourcePath + ", " +
                    "association type: " + associationType + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            Association[] associations =
                    coreRegistry.getAssociations(chrootWrapper.getInPath(resourcePath), associationType);

            // removing the chrooted paths in returning values
            associations = chrootWrapper.getOutAssociations(associations);
            return associations;
        } finally {
            clearSessionInformation();
        }
    }

    public void applyTag(final String resourcePath, final String tag) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    applyTagInternal(resourcePath, tag);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }
    
    private void applyTagInternal(String resourcePath, String tag) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation apply tag, " +
                    "path: " + resourcePath + ", " +
                    "tag: " + tag + ".");
        }
        // If this node is operating in read-only mode, do not apply the tag
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation apply tag, the coreRegistry is read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.applyTag(chrootWrapper.getInPath(resourcePath), tag);
        } finally {
            clearSessionInformation();
        }
    }

    public TaggedResourcePath[] getResourcePathsWithTag(final String tag) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<TaggedResourcePath[]>() {
                @Override
                public TaggedResourcePath[] run() throws Exception {
                    return getResourcePathsWithTagInternal(tag);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private TaggedResourcePath[] getResourcePathsWithTagInternal(String tag) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get resource paths with tags, " +
                    "tag: " + tag + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();
            TaggedResourcePath[] taggedResourcePaths = coreRegistry.getResourcePathsWithTag(tag);

            // removing the chrooted paths in returning values
            taggedResourcePaths = chrootWrapper.getOutTaggedResourcePaths(taggedResourcePaths);
            return taggedResourcePaths;
        } finally {
            clearSessionInformation();
        }
    }

    public Tag[] getTags(final String resourcePath) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Tag[]>() {
                @Override
                public Tag[] run() throws Exception {
                    return getTagsInternal(resourcePath);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private Tag[] getTagsInternal(String resourcePath) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get tags, " +
                    "path: " + resourcePath + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            return coreRegistry.getTags(chrootWrapper.getInPath(resourcePath));
        } finally {
            clearSessionInformation();
        }
    }

    public void removeTag(final String path, final String tag) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    removeTagInternal(path, tag);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void removeTagInternal(String path, String tag) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation remove tag, " +
                    "path: " + path + ", " +
                    "tag: " + tag + ".");
        }
        // If this node is operating in read-only mode, do not remove tag
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "Cannot continue the operation remove tag, the coreRegistry is read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.removeTag(chrootWrapper.getInPath(path), tag);
        } finally {
            clearSessionInformation();
        }
    }

    public String addComment(final String resourcePath, final org.wso2.carbon.registry.api.Comment comment) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    return addCommentInternal(resourcePath, comment);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private String addCommentInternal(String resourcePath, org.wso2.carbon.registry.api.Comment comment)
            throws org.wso2.carbon.registry.api.RegistryException {
        return addCommentInternal(resourcePath, (Comment) comment);
    }

    public String addComment(final String resourcePath, final Comment comment) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    return addCommentInternal(resourcePath, comment);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private String addCommentInternal(String resourcePath, Comment comment) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation add comment, " +
                    "path: " + resourcePath + ".");
        }
        // If this node is operating in read-only mode, do not add the comment
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "Cannot continue the operation add comment, the coreRegistry is read-only");
            }
            return resourcePath;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            String commentPath = coreRegistry.addComment(chrootWrapper.getInPath(resourcePath), comment);
            return chrootWrapper.getOutPath(commentPath);
        } finally {
            clearSessionInformation();
        }
    }

    public void editComment(final String commentPath, final String text) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    editCommentInternal(commentPath, text);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void editCommentInternal(String commentPath, String text) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation edit comment, " +
                    "path: " + commentPath + ".");
        }
        // If this node is operating in read-only mode, do not edit the comment
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation edit comment, the coreRegistry is " +
                        "read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.editComment(chrootWrapper.getInPath(commentPath), text);
        } finally {
            clearSessionInformation();
        }
    }

    public void removeComment(final String commentPath) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    removeCommentInternal(commentPath);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void removeCommentInternal(String commentPath) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation edit comment, " +
                    "path: " + commentPath + ".");
        }
        // If this node is operating in read-only mode, do not edit the comment
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation edit comment, the coreRegistry is " +
                        "read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.removeComment(chrootWrapper.getInPath(commentPath));
        } finally {
            clearSessionInformation();
        }
    }

    public Comment[] getComments(final String resourcePath) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Comment[]>() {
                @Override
                public Comment[] run() throws Exception {
                    return getCommentsInternal(resourcePath);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private Comment[] getCommentsInternal(String resourcePath) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get comments, " +
                    "path: " + resourcePath + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            Comment[] returnedComments = coreRegistry.getComments(chrootWrapper.getInPath(resourcePath));

            // removing the chrooted paths in returning values
            returnedComments = chrootWrapper.getOutComments(returnedComments);
            return returnedComments;
        } finally {
            clearSessionInformation();
        }
    }

    public void rateResource(final String resourcePath, final int rating) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    rateResourceInternal(resourcePath, rating);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void rateResourceInternal(String resourcePath, int rating) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation rate resource, " +
                    "path: " + resourcePath + ", " +
                    "rating: " + rating + ".");
        }
        // If this node is operating in read-only mode, do not rate the resource
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation rate resource, the coreRegistry is " +
                        "read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.rateResource(chrootWrapper.getInPath(resourcePath), rating);
        } finally {
            clearSessionInformation();
        }
    }

    public float getAverageRating(final String resourcePath) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Float>() {
                @Override
                public Float run() throws Exception {
                    return getAverageRatingInternal(resourcePath);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private float getAverageRatingInternal(String resourcePath) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get average ratings, " +
                    "path: " + resourcePath + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            return coreRegistry.getAverageRating(chrootWrapper.getInPath(resourcePath));
        } finally {
            clearSessionInformation();
        }
    }

    public int getRating(final String path, final String userName) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Integer>() {
                @Override
                public Integer run() throws Exception {
                    return getRatingInternal(path, userName);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private int getRatingInternal(String path, String userName) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get ratings, " +
                    "path: " + path + ", " +
                    "user name: " + userName + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            return coreRegistry.getRating(chrootWrapper.getInPath(path), userName);
        } finally {
            clearSessionInformation();
        }
    }

    @SuppressWarnings("rawtypes")
    public Collection executeQuery(final String path, final Map parameters) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Collection>() {
                @Override
                public Collection run() throws Exception {
                    return executeQueryInternal(path, parameters);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    @SuppressWarnings("rawtypes")
    private Collection executeQueryInternal(String path, Map parameters) throws RegistryException {
        if (log.isTraceEnabled()) {
            String msg = "Preparing operation execute query, " +
                    "path: " + path + ", " +
                    "values: ";
            Object[] paramValues = parameters.values().toArray();
            StringBuilder sb = new StringBuilder(msg);
            for (int i = 0; i < paramValues.length; i++) {
                String value = (String) paramValues[i];
                sb.append(value);
                if (i != paramValues.length - 1) {
                    sb.append(", ");
                } else {
                    sb.append(".");
                }
            }
            log.trace(sb.toString());
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();
            if (path != null) {
                String newPath = chrootWrapper.getInPath(path);
                if (newPath != null) {
                    path = newPath.replace(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH,
                            RegistryConstants.CONFIG_REGISTRY_BASE_PATH).replace(
                            RegistryConstants.LOCAL_REPOSITORY_BASE_PATH,
                            RegistryConstants.CONFIG_REGISTRY_BASE_PATH);
                    // The '/' path is used in the remote registry case as a workaround, instead of
                    // passing null.
                    if (!path.contains(RegistryConstants.CONFIG_REGISTRY_BASE_PATH) &&
                            !path.equals(chrootWrapper.getInPath(RegistryConstants.ROOT_PATH))) {
                        log.warn("Running Query in Backwards-Compatible mode. Queries must be " +
                                "stored and accessed from the Configuration System Registry in " +
                                "the new model. Path: " + path);
                    }
                } else {
                    path = null;
                }
            }

            // here the path will always be made to reside in the config registry.

            Collection collection = coreRegistry.executeQuery(path, parameters);
            if (collection != null) {
                ResourceImpl resourceImpl = (ResourceImpl) collection;
                resourceImpl.setUserName(userName);
                resourceImpl.setTenantId(tenantId);
                resourceImpl.setUserRealm(userRealm);

                // removing the chrooted paths in returning values
                collection = chrootWrapper.filterSearchResult(collection);
                collection = (Collection) chrootWrapper.getOutResource(collection);
            }
            return collection;

        } finally {
            clearSessionInformation();
        }
    }

    public LogEntry[] getLogs(final String resourcePath, final int action, final String userName, final Date from, final Date to, final boolean recentFirst)
            throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<LogEntry[]>() {
                @Override
                public LogEntry[] run() throws Exception {
                    return getLogsInternal(resourcePath, action, userName, from, to, recentFirst);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private LogEntry[] getLogsInternal(
            String resourcePath,
            int action,
            String userName,
            Date from,
            Date to,
            boolean recentFirst)
            throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get logs, " +
                    "resource path: " + resourcePath + ", " +
                    "action: " + action + ", " +
                    "user name: " + userName + ", " +
                    "from: " + from + ", " +
                    "to: " + to + ", " +
                    "recent first: " + recentFirst + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            LogEntry[] logEntries = coreRegistry.getLogs(chrootWrapper.getInPath(resourcePath),
                                        action, userName, from, to, recentFirst);
            return chrootWrapper.fixLogEntries(logEntries);

        } finally {
            clearSessionInformation();
        }
    }


    public LogEntryCollection getLogCollection(String resourcePath,
                                               int action,
                                               String userName,
                                               Date from,
                                               Date to,
                                               boolean recentFirst) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get logs, " +
                    "resource path: " + resourcePath + ", " +
                    "action: " + action + ", " +
                    "user name: " + userName + ", " +
                    "from: " + from + ", " +
                    "to: " + to + ", " +
                    "recent first: " + recentFirst + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            return coreRegistry.getLogCollection(chrootWrapper.getInPath(resourcePath),
                                                    action, userName, from, to, recentFirst);
        } finally {
            clearSessionInformation();
        }
    }

    public String[] getAvailableAspects() {
        return AccessController.doPrivileged(new PrivilegedAction<String[]>() {
            @Override
            public String[] run() {
                return getAvailableAspectsInternal();
            }
        });
    }

    private String[] getAvailableAspectsInternal() {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get available actions.");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();
            return coreRegistry.getAvailableAspects();
        } finally {
            clearSessionInformation();
        }
    }

    public void associateAspect(final String resourcePath, final String aspect) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    associateAspectInternal(resourcePath, aspect);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void associateAspectInternal(String resourcePath, String aspect) throws RegistryException {

        if (log.isTraceEnabled()) {
            log.trace("Preparing operation associate aspect, " +
                    "path: " + resourcePath + ", " +
                    "aspect: " + aspect + ".");
        }
        // If this node is operating in read-only mode, do not associate aspects
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation associate aspect, the coreRegistry is " +
                        "read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.associateAspect(chrootWrapper.getInPath(resourcePath), aspect);
        } finally {
            clearSessionInformation();
        }
    }

    public void invokeAspect(final String resourcePath, final String aspectName, final String action) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    invokeAspectInternal(resourcePath, aspectName, action);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void invokeAspectInternal(String resourcePath, String aspectName, String action)
            throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation invoke aspects, " +
                    "path: " + resourcePath + ", " +
                    "aspect name: " + aspectName + ", " +
                    "action: " + action + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.invokeAspect(chrootWrapper.getInPath(resourcePath), aspectName, action);
        } finally {
            clearSessionInformation();
        }
    }

    public void invokeAspect(String resourcePath, String aspectName, String action,
                             Map<String, String> parameters)
            throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation invoke aspects, " +
                    "path: " + resourcePath + ", " +
                    "aspect name: " + aspectName + ", " +
                    "action: " + action + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.invokeAspect(chrootWrapper.getInPath(resourcePath), aspectName, action, parameters);
        } finally {
            clearSessionInformation();
        }
    }

    public boolean removeAspect(String aspect) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation remove Aspect, " +
                    "Aspect Name: " + aspect);
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            return coreRegistry.removeAspect(aspect);
        } finally {
            clearSessionInformation();
        }
    }

    public boolean addAspect(String name, Aspect aspect) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation remove Aspect, " +
                    "Aspect Name: " + aspect);
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            return coreRegistry.addAspect(name, aspect);
        } finally {
            clearSessionInformation();
        }
    }

    public String[] getAspectActions(final String resourcePath, final String aspectName) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<String[]>() {
                @Override
                public String[] run() throws Exception {
                    return getAspectActionsInternal(resourcePath, aspectName);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private String[] getAspectActionsInternal(String resourcePath, String aspectName)
            throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get aspect actions, " +
                    "path: " + resourcePath + ", " +
                    "aspect name: " + aspectName + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            return coreRegistry.getAspectActions(chrootWrapper.getInPath(resourcePath), aspectName);
        } finally {
            clearSessionInformation();
        }
    }

    public Collection searchContent(final String keywords) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Collection>() {
                @Override
                public Collection run() throws Exception {
                    return searchContentInternal(keywords);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private Collection searchContentInternal(String keywords) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation search content, " +
                    "keywords: " + keywords + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            Collection collection = coreRegistry.searchContent(keywords);
            if (collection != null) {
                ResourceImpl resourceImpl = (ResourceImpl) collection;
                resourceImpl.setUserName(userName);
                resourceImpl.setTenantId(tenantId);
                resourceImpl.setUserRealm(userRealm);

                // removing the chrooted paths in returning values
                collection = chrootWrapper.filterSearchResult(collection);
                collection = (Collection) chrootWrapper.getOutResource(collection);
            }
            return collection;

        } finally {
            clearSessionInformation();
        }
    }

    public void createLink(final String path, final String target) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    createLinkInternal(path, target);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void createLinkInternal(String path, String target) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation create link, " +
                    "path: " + path + ", " +
                    "target: " + target + ".");
        }
        // If this node is operating in read-only mode, do not create link
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "Cannot continue the operation create link, the coreRegistry is read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.createLink(chrootWrapper.getInPath(path), chrootWrapper.getInPath(target));
        } finally {
            clearSessionInformation();
        }
    }

    public void createLink(final String path, final String target, final String subTargetPath) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    createLinkInternal(path, target, subTargetPath);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void createLinkInternal(String path, String target,
                           String targetSubPath) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation create link, " +
                    "path: " + path + ", " +
                    "target: " + target + ", " +
                    "target sub path: " + targetSubPath + ".");
        }
        // If this node is operating in read-only mode, do not create link
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "Cannot continue the operation create link, the coreRegistry is read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.createLink(chrootWrapper.getInPath(path), target, targetSubPath);
        } finally {
            clearSessionInformation();
        }
    }

    public void removeLink(final String path) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    removeLinkInternal(path);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void removeLinkInternal(String path) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation remove link, " +
                    "path: " + path + ".");
        }
        // If this node is operating in read-only mode, do not remove link
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "Cannot continue the operation remove link, the coreRegistry is read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.removeLink(chrootWrapper.getInPath(path));
        } finally {
            clearSessionInformation();
        }
    }

    ////////////////////////////////////////////////////////
    // Check-in, check-out
    ////////////////////////////////////////////////////////

    public void restore(final String path, final Reader reader) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    restoreInternal(path, reader);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void restoreInternal(String path, Reader reader) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation restore dump, " +
                    "path: " + path + ".");
        }
        // If this node is operating in read-only mode, do not restore dump
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            log.warn("Cannot continue the operation restore dump, the coreRegistry is " +
                    "read-only");
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.restore(chrootWrapper.getInPath(path), reader);
        } finally {
            clearSessionInformation();
        }
    }

    public void dump(final String path, final Writer writer) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    dumpInternal(path, writer);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void dumpInternal(String path, Writer writer) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation dump, " +
                    "path: " + path + ".");
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.dump(chrootWrapper.getInPath(path), writer);
        } finally {
            clearSessionInformation();
        }
    }

    public String getEventingServiceURL(final String path) throws RegistryException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    return getEventingServiceURLInternal(path);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private String getEventingServiceURLInternal(String path) throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation get eventing service url, " +
                    "path: " + path + ".");
        }
        String eventingServiceURL = null;
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            eventingServiceURL = coreRegistry.getEventingServiceURL(chrootWrapper.getInPath(path));
        } finally {
            clearSessionInformation();
        }
        return eventingServiceURL;
    }

    public void setEventingServiceURL(final String path, final String eventingServiceURL) throws RegistryException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                @Override
                public String run() throws Exception {
                    setEventingServiceURLInternal(path, eventingServiceURL);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (RegistryException) e.getException();
        }
    }

    private void setEventingServiceURLInternal(String path, String eventingServiceURL)
            throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Preparing operation set eventing service url, " +
                    "path: " + path + ", " +
                    "eventing service url: " + eventingServiceURL + ".");
        }
        // If this node is operating in read-only mode, do not set eventing service url
        if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation eventing service url, the coreRegistry " +
                        "is read-only");
            }
            return;
        }
        try {
            // setting session information + chrooted incoming paths
            setSessionInformation();

            coreRegistry.setEventingServiceURL(chrootWrapper.getInPath(path), eventingServiceURL);
        } finally {
            clearSessionInformation();
        }
    }

    /**
     * Method to set the information related to users in to the current session.
     */
    public final void setSessionInformation() {
        if (log.isTraceEnabled()) {
            log.trace("Setting the session for registry operation, " +
                    "chroot: " + chrootWrapper.getBasePrefix() + ", " +
                    "username: " + userName + ", " +
                    "tenantId: " + tenantId + ", " +
                    "callerTenantId: " + callerTenantId + ".");
        }
        CurrentSession.setUser(userName);
        if (userRealm != null) {
            CurrentSession.setUserRealm(userRealm);
        }
        CurrentSession.setTenantId(tenantId);
        CurrentSession.setCallerTenantId(callerTenantId);
        CurrentSession.setChroot(chrootWrapper.getBasePrefix());
        CurrentSession.setUserRegistry(this);
    }

    /**
     * Method to clear session information.
     */
    private final void clearSessionInformation() {

        if (log.isTraceEnabled()) {
            log.trace("Clearing the session for registry operation, " +
                    "chroot: " + chrootWrapper.getBasePrefix() + ", " +
                    "username: " + CurrentSession.getUser() + ", " +
                    "tenantId: " + CurrentSession.getTenantId() + ".");
        }
        CurrentSession.removeUser();
        CurrentSession.removeUserRealm();
        CurrentSession.removeTenantId();
        CurrentSession.removeCallerTenantId();
        CurrentSession.removeChroot();
        CurrentSession.removeUserRegistry();
        if (CurrentSession.getUserRegistry() == null) {
            CurrentSession.removeAttributes();
        }
    }
    
    public boolean removeVersionHistory(String path, long snapshotId)
    		throws RegistryException {

    	if (RegistryUtils.isRegistryReadOnly(coreRegistry.getRegistryContext())) {
            if (log.isTraceEnabled()) {
                log.trace("Cannot continue the operation removing the version history, the coreRegistry " +
                        "is read-only");
            }
            return false;
        }
        try {
            // setting session information
            setSessionInformation();

            return coreRegistry.removeVersionHistory(path, snapshotId);
        } finally {
            clearSessionInformation();            
        }    	    	
    }
    
    @Override
    public void dumpLite(String path, Writer writer) throws RegistryException {
        if (log.isTraceEnabled()) {
			log.trace("Preparing operation dump, " + "path: " + path + ".");
        }
        try {
            // setting session information      chrooted incoming paths
            setSessionInformation();

            coreRegistry.dumpLite(chrootWrapper.getInPath(path), writer);
        } finally {
            clearSessionInformation();
        }
        
    }
}

