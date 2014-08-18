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

package org.wso2.carbon.registry.core.utils;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.api.GhostResource;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.caching.RegistryCacheEntry;
import org.wso2.carbon.registry.core.caching.RegistryCacheKey;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.config.RemoteConfiguration;
import org.wso2.carbon.registry.core.config.StaticConfiguration;
import org.wso2.carbon.registry.core.dao.ResourceDAO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerLifecycleManager;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerManager;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.builtin.MountHandler;
import org.wso2.carbon.registry.core.jdbc.handlers.builtin.SymLinkHandler;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.Filter;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.URLMatcher;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.jdbc.utils.ServiceConfigUtil;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.statistics.StatisticsCollector;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class contains a set of useful utility methods used by the Registry Kernel. These can also
 * be used by third party components as well as the various clients written to access the registry
 * via its remote APIs.
 */
public final class RegistryUtils {

    private static final Log log = LogFactory.getLog(RegistryUtils.class);
    private static final String ENCODING = System.getProperty("carbon.registry.character.encoding");

    private RegistryUtils() {
    }

    /**
     * Paths referring to a version of a resource are in the form /c1/c2/r1?v=2. Give a such version
     * path, this method extracts the valid pure path and the version number of the resource. Note
     * that this method should only be used for pure resource paths.
     *
     * @param resourcePath versioned resource path.
     *
     * @return VersionPath object containing the valid resource path and the version number.
     */
    public static VersionedPath getVersionedPath(ResourcePath resourcePath) {

        String versionString = resourcePath.getParameterValue("version");
        long versionNumber = -1;
        if (versionString != null) {
            versionNumber = Long.parseLong(versionString);
        }

        String plainPath = getPureResourcePath(resourcePath.getPath());

        VersionedPath versionedPath = new VersionedPath();
        versionedPath.setPath(plainPath);
        versionedPath.setVersion(versionNumber);

        return versionedPath;
    }

    /**
     * Convert an input stream into a byte array.
     *
     * @param inputStream the input stream.
     *
     * @return the byte array.
     * @throws RegistryException if the operation failed.
     */
    public static byte[] getByteArray(InputStream inputStream) throws RegistryException {

        if (inputStream == null) {
            String msg = "Could not create memory based content for null input stream.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            byte[] contentChunk = new byte[RegistryConstants.DEFAULT_BUFFER_SIZE];
            int byteCount;
            while ((byteCount = inputStream.read(contentChunk)) != -1) {
                out.write(contentChunk, 0, byteCount);
            }
            out.flush();

            return out.toByteArray();

        } catch (IOException e) {
            String msg = "Failed to write data to byte array input stream. " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                inputStream.close();
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                String msg = "Failed to close streams used for creating memory stream. "
                        + e.getMessage();
                log.error(msg, e);
            }
        }
    }

    /**
     * Create an in-memory input stream for the given input stream.
     *
     * @param inputStream the input stream.
     *
     * @return the in-memory input stream.
     * @throws RegistryException if the operation failed.
     */
    public static InputStream getMemoryStream(InputStream inputStream) throws RegistryException {
        return new ByteArrayInputStream(getByteArray(inputStream));
    }

    /**
     * Method to obtain a unique identifier for the database connection.
     *
     * @param connection the database connection.
     *
     * @return the unique identifier.
     */
    public static String getConnectionId(Connection connection) {
        try {
            // The connection URL is unique enough to be used as an identifier since one thread
            // makes one connection to the given URL according to our model.
            DatabaseMetaData connectionMetaData = connection.getMetaData();
            if (connectionMetaData != null) {
                return (connectionMetaData.getUserName() != null ? connectionMetaData.getUserName().split("@")[0] :
                        connectionMetaData.getUserName()) + "@" + connectionMetaData.getURL();
            }
        } catch (SQLException ignore) {
            log.debug("Failed to construct the connectionId ." + ignore.getMessage());
        }
        return null;
    }

    /**
     * Builds the cache key for a resource path.
     *
     * @param connectionId the database connection identifier
     * @param tenantId     the tenant identifier
     * @param resourcePath the resource path
     *
     * @return the cache key.
     */
    public static RegistryCacheKey buildRegistryCacheKey(String connectionId, int tenantId,
                                                   String resourcePath) {
        RegistryContext registryContext = RegistryContext.getBaseInstance();
        String absoluteLocalRepositoryPath = getAbsolutePath(registryContext,
                RegistryConstants.LOCAL_REPOSITORY_BASE_PATH);
        if (resourcePath != null && resourcePath.startsWith(absoluteLocalRepositoryPath)) {
            return new RegistryCacheKey(resourcePath, tenantId,
                    registryContext.getNodeIdentifier() + ":" + connectionId.toLowerCase());
        } else {
            return new RegistryCacheKey(resourcePath, tenantId, connectionId.toLowerCase());
        }
    }

    /**
     * All "valid" paths pure resources should be in the form /c1/c2/r1. That is they should start
     * with "/" and should not end with "/". Given a path of a pure resource, this method prepares
     * the valid path for that path.
     *
     * @param resourcePath Path of a pure resource.
     *
     * @return Valid path of the pure resource.
     */
    public static String getPureResourcePath(String resourcePath) {
        if (resourcePath == null) {
            return null;
        }

        String preparedPath = resourcePath;
        if (preparedPath.equals(RegistryConstants.ROOT_PATH)) {
            return preparedPath;
        } else {
            if (!preparedPath.startsWith(RegistryConstants.ROOT_PATH)) {
                preparedPath = RegistryConstants.ROOT_PATH + preparedPath;
            }
            if (preparedPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                preparedPath = preparedPath.substring(0, preparedPath.length() - 1);
            }
            if (preparedPath.contains("//")) {
                preparedPath = preparedPath.replace("//", "/");
            }
        }

        return preparedPath;
    }

    /**
     * Return an instance of a named cache that is common to all tenants.
     *
     * @param name the name of the cache.
     *
     * @return the named cache instance.
     */
//    public static Cache getCommonCache(String name) {
//        // We create a single cache for all tenants. It is not a good choice to create per-tenant
//        // caches in this case. We qualify tenants by adding the tenant identifier in the cache key.
//        PrivilegedCarbonContext currentContext = PrivilegedCarbonContext.getCurrentContext();
//        currentContext.startTenantFlow();
//        try {
//            currentContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
//            return CarbonUtils.getLocalCache(name);
//        } finally {
//            currentContext.endTenantFlow();
//        }
//    }

    /**
     * Method used to retrieve cache object for resources
     * @param name the name of the cache
     * @return the cache object for the given cache manger and cache name
     */
    public static Cache<RegistryCacheKey, GhostResource> getResourceCache(String name){
        CacheManager manager = getCacheManager();
        return (manager != null) ? manager.<RegistryCacheKey, GhostResource>getCache(name) :
                Caching.getCacheManager().<RegistryCacheKey, GhostResource>getCache(name);
    }

    /**
     * Method used to retrieve cache object for resource paths.
     * @param name the name of the cache
     * @return the cache object for the given cache manger and cache name
     */
    public static Cache<RegistryCacheKey, RegistryCacheEntry> getResourcePathCache(String name){
        CacheManager manager = getCacheManager();
        return (manager != null) ? manager.<RegistryCacheKey, RegistryCacheEntry>getCache(name) :
                Caching.getCacheManager().<RegistryCacheKey, RegistryCacheEntry>getCache(name);
    }

    private static CacheManager getCacheManager() {
        return Caching.getCacheManagerFactory().getCacheManager(
                RegistryConstants.REGISTRY_CACHE_MANAGER);
    }

    /**
     * Method to obtain the parent path of the given resource path.
     *
     * @param resourcePath the resource path.
     *
     * @return the parent path.
     */
    public static String getParentPath(String resourcePath) {
        if (resourcePath == null) {
            return null;
        }

        String parentPath;
        if (resourcePath.equals(RegistryConstants.ROOT_PATH)) {
            parentPath = null;
        } else {
            String formattedPath = resourcePath;
            if (resourcePath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                formattedPath = resourcePath.substring(
                        0, resourcePath.length() - RegistryConstants.PATH_SEPARATOR.length());
            }

            if (formattedPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) <= 0) {
                parentPath = RegistryConstants.ROOT_PATH;
            } else {
                parentPath = formattedPath.substring(
                        0, formattedPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
            }
        }

        return parentPath;
    }

    /**
     * Returns resource name when full resource path is passed.
     *
     * @param resourcePath full resource path.
     *
     * @return the resource name.
     */
    public static String getResourceName(String resourcePath) {
        String resourceName;
        if (resourcePath.equals(RegistryConstants.ROOT_PATH)) {
            resourceName = RegistryConstants.ROOT_PATH;

        } else {

            String formattedPath = resourcePath;
            if (resourcePath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                formattedPath = resourcePath.substring(
                        0, resourcePath.length() - RegistryConstants.PATH_SEPARATOR.length());
            }

            if (formattedPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) == 0) {
                resourceName = formattedPath.substring(1, formattedPath.length());
            } else {
                resourceName = formattedPath.substring(
                        formattedPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1,
                        formattedPath.length());
            }
        }

        return resourceName;
    }

    /**
     * Method to redirect a given response to some URL.
     *
     * @param response the HTTP Response.
     * @param url      The URL to redirect to.
     */
    public static void redirect(HttpServletResponse response, String url) {

        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            String msg = "Failed to redirect to the URL " + url + ". \nCaused by " +
                    e.getMessage();
            log.error(msg, e);
        }
    }

    /**
     * Prepare the given path as a general resource path to be used in the registry. General
     * resource paths may refer to pure resources or virtual resources.
     *
     * @param rawPath Raw path to be prepared
     *
     * @return Prepared general resource path
     */
    public static String prepareGeneralPath(String rawPath) {
        String path = rawPath;

        // path should always start with "/"
        if (!rawPath.startsWith(RegistryConstants.ROOT_PATH)) {
            path = RegistryConstants.ROOT_PATH + rawPath;
        }

        return path;
    }

    /**
     * Method to determine whether the given user is in an admin role.
     *
     * @param userName  the user name.
     * @param userRealm the user realm.
     *
     * @return true if the user is in the admin role, or false otherwise.
     * @throws RegistryException if the operation failed.
     */
    public static boolean hasAdminAuthorizations(String userName, UserRealm userRealm)
            throws RegistryException {

        try {
            UserStoreManager userStoreReader = userRealm.getUserStoreManager();

            RealmConfiguration realmConfig;
            try {
                realmConfig = userRealm.getRealmConfiguration();
            } catch (UserStoreException e) {
                String msg = "Failed to retrieve realm configuration.";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }
            String systemUser = CarbonConstants.REGISTRY_SYSTEM_USERNAME;

            if (systemUser.equals(userName)) {
                return true;
            }

            String adminUser = realmConfig.getAdminUserName();
            if (adminUser.equals(userName)) {
                return true;
            }

            String[] roles = userStoreReader.getRoleListOfUser(userName);
            String adminRoleName = realmConfig.getAdminRoleName();
            if (containsString(adminRoleName, roles)) {
                return true;
            }

        } catch (UserStoreException e) {

            String msg = "Failed to check authorization level of user " +
                    userName + ". Caused by: " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        return false;
    }

    /**
     * Method to determine whether the given array of strings contains the given string.
     *
     * @param value the string to search.
     * @param array the array of string.
     *
     * @return whether the given string was found.
     */
    public static boolean containsString(String value, String[] array) {
        boolean found = false;

        for (String anArray : array) {
            if (anArray.equals(value)) {
                found = true;
            }
        }

        return found;
    }

    /**
     * Method to determine whether the given array of strings contains a string which has the given
     * string as a portion (sub-string) of it.
     *
     * @param value the string to search.
     * @param array the array of string.
     *
     * @return whether the given string was found.
     */
    public static boolean containsAsSubString(String value, String[] array) {
        for (String anArray : array) {
            if (anArray.contains(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to remove the author and updater from a dump.
     *
     * @param root the input XML element.
     *
     * @return the output which contains the input XML element, but without details of the author
     *         and the updater.
     */
    @SuppressWarnings("unused")
    public static OMElement removeAuthorUpdaterFromDump(OMElement root) {
        Iterator children = root.getChildren();

        String isCollectionString = root.getAttributeValue(new QName("isCollection"));
        boolean isCollection = isCollectionString.equals("true");

        OMElement creatorEle = null;
        OMElement createdTimeEle = null;
        OMElement lastUpdaterEle = null;
        OMElement lastModifiedTimeEle = null;

        OMElement childrenEle = null;
        while (children.hasNext()) {
            Object nextChild = children.next();
            if (!(nextChild instanceof OMElement)) {
                continue;
            }
            OMElement child = (OMElement) nextChild;
            String localName = child.getLocalName();
            // creator
            if (localName.equals("creator")) {
                creatorEle = child;
            }
            // createdTime
            else if (localName.equals("createdTime")) {
                createdTimeEle = child;
            }
            // setLastUpdater
            else if (localName.equals("lastUpdater")) {
                lastUpdaterEle = child;
            }
            // LastModified
            else if (localName.equals("lastModified")) {
                lastModifiedTimeEle = child;
            } else if (localName.equals("children")) {
                childrenEle = child;
            }
        }

        // removing the children
        if (creatorEle != null) {
            creatorEle.detach();
        }
        if (createdTimeEle != null) {
            createdTimeEle.detach();
        }
        if (lastUpdaterEle != null) {
            lastUpdaterEle.detach();
        }
        if (lastModifiedTimeEle != null) {
            lastModifiedTimeEle.detach();
        }

        // call this for all the children
        if (isCollection && childrenEle != null) {
            Iterator grandChildren = childrenEle.getChildren();

            while (grandChildren.hasNext()) {
                Object nextChild = grandChildren.next();
                if (!(nextChild instanceof OMElement)) {
                    continue;
                }
                OMElement childE = (OMElement) nextChild;
                removeAuthorUpdaterFromDump(childE);
            }
        }

        return root;
    }

    @Deprecated
    @SuppressWarnings("unused")
    public static void addRootCollectionAuthorization(UserRealm userRealm)
            throws RegistryException {
        AuthorizationUtils.setRootAuthorizations(RegistryConstants.ROOT_PATH, userRealm);
    }

    /**
     * Set-up the system properties required to access the trust-store in Carbon. This is used in
     * the atom-based Remote Registry implementation.
     */
    public static void setTrustStoreSystemProperties() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        String type = config.getFirstProperty("Security.TrustStore.Type");
        String password = config.getFirstProperty("Security.TrustStore.Password");
        String storeFile = new File(
                config.getFirstProperty("Security.TrustStore.Location")).getAbsolutePath();

        System.setProperty("javax.net.ssl.trustStore", storeFile);
        System.setProperty("javax.net.ssl.trustStoreType", type);
        System.setProperty("javax.net.ssl.trustStorePassword", password);
    }

    /**
     * Method to determine whether a system resource (or collection) is existing or whether it
     * should be created. Once it has been identified that a system collection is existing, it will
     * not be checked against a database until the server has been restarted.
     *
     * @param registry the base registry to use.
     * @param absolutePath the absolute path of the system resource (or collection)
     *
     * @return whether the system resource (or collection) needs to be added.
     *
     * @throws RegistryException if an error occurred.
     */
    public static boolean systemResourceShouldBeAdded(Registry registry, String absolutePath)
        throws RegistryException {
        RegistryContext registryContext = registry.getRegistryContext();
        if (registryContext == null) {
            registryContext = RegistryContext.getBaseInstance();
        }
        if (registryContext.isSystemResourcePathRegistered(absolutePath)) {
            return false;
        } else if (registry.resourceExists(absolutePath)) {
            registryContext.registerSystemResourcePath(absolutePath);
            return false;
        }
        return true;
    }

    /**
     * Method to determine whether a system resource (or collection) is existing or whether it
     * should be created. Once it has been identified that a system collection is existing, it will
     * not be checked against a database until the server has been restarted.
     *
     * @param dataAccessObject the resource data access object.
     * @param absolutePath the absolute path of the system resource (or collection)
     *
     * @return whether the system resource (or collection) needs to be added.
     *
     * @throws RegistryException if an error occurred.
     */
    public static boolean systemResourceShouldBeAdded(ResourceDAO dataAccessObject,
                                                      String absolutePath)
            throws RegistryException {
        RegistryContext registryContext = RegistryContext.getBaseInstance();
        if (RegistryContext.getBaseInstance().isSystemResourcePathRegistered(absolutePath)) {
            return false;
        } else if (dataAccessObject.resourceExists(absolutePath)) {
            registryContext.registerSystemResourcePath(absolutePath);
            return false;
        }
        return true;
    }

    /**
     * This method builds the base collection structure for the registry. The base <b>_system</b>
     * collection, and the remaining local, config and governance collections will be created as a
     * result.
     *
     * @param registry  the base registry to use.
     * @param userRealm the user realm.
     *
     * @throws RegistryException if an error occurred.
     */
    @SuppressWarnings("unused")
    public static void addBaseCollectionStructure(Registry registry, UserRealm userRealm)
            throws RegistryException {
        RegistryContext registryContext = registry.getRegistryContext();
        // Registry Context stays the same during the scope of this operation.
        if (log.isTraceEnabled()) {
            log.trace("Checking the existence of '" + RegistryUtils.getAbsolutePath(
                    registryContext, RegistryConstants.SYSTEM_COLLECTION_BASE_PATH)
                    + "' collection of the Registry.");
        }
        if (systemResourceShouldBeAdded(registry, RegistryUtils.getAbsolutePath(
                registryContext, RegistryConstants.SYSTEM_COLLECTION_BASE_PATH))) {
            if (registryContext != null && registryContext.isClone()) {
                return;
            }
            if (log.isTraceEnabled()) {
                log.trace("Creating the '" + RegistryUtils.getAbsolutePath(
                        registryContext,
                        RegistryConstants.SYSTEM_COLLECTION_BASE_PATH)
                        + "' collection of the Registry.");
            }

            CollectionImpl systemCollection = (CollectionImpl) registry.newCollection();
            String systemDescription = "System collection of the Registry. This collection is " +
                    "used to store the resources required by the carbon server.";
            systemCollection.setDescription(systemDescription);
            registry.put(RegistryUtils.getAbsolutePath(
                    registryContext, RegistryConstants.SYSTEM_COLLECTION_BASE_PATH),
                    systemCollection);
            systemCollection.discard();

            CollectionImpl localRepositoryCollection = (CollectionImpl) registry.newCollection();
            String localRepositoryDescription =
                    "Local data repository of the carbon server. This " +
                            "collection is used to store the resources local to this carbon " +
                            "server instance.";
            localRepositoryCollection.setDescription(localRepositoryDescription);
            registry.put(RegistryUtils.getAbsolutePath(
                    registryContext, RegistryConstants.LOCAL_REPOSITORY_BASE_PATH),
                    localRepositoryCollection);
            localRepositoryCollection.discard();

            CollectionImpl configRegistryCollection = (CollectionImpl) registry.newCollection();
            String configRegistryDescription = "Configuration registry of the carbon server. " +
                    "This collection is used to store the resources of this product cluster.";
            configRegistryCollection.setDescription(configRegistryDescription);
            registry.put(RegistryUtils.getAbsolutePath(
                    registryContext, RegistryConstants.CONFIG_REGISTRY_BASE_PATH),
                    configRegistryCollection);
            configRegistryCollection.discard();

            CollectionImpl governanceRegistryCollection = (CollectionImpl) registry.newCollection();
            String governanceRegistryDescription = "Governance registry of the carbon server. " +
                    "This collection is used to store the resources common to the whole " +
                    "platform.";
            governanceRegistryCollection.setDescription(governanceRegistryDescription);
            registry.put(RegistryUtils.getAbsolutePath(
                    registryContext, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH),
                    governanceRegistryCollection);
            governanceRegistryCollection.discard();
        }
        // This is to create the repository collections for the various registries and clean
        // them at start-up, if needed.
        boolean cleanRegistry = false;
        if (System.getProperty(RegistryConstants.CARBON_REGISTRY_CLEAN) != null) {
            cleanRegistry = true;
            System.clearProperty(RegistryConstants.CARBON_REGISTRY_CLEAN);
        }
        if (cleanRegistry) {
            cleanArtifactLCs(registry);
        }
        for (String repositoryPath : new String[]{
                RegistryUtils.getAbsolutePath(
                        registryContext,
                        RegistryConstants.LOCAL_REPOSITORY_BASE_PATH +
                                "/repository"),
                RegistryUtils.getAbsolutePath(
                        registryContext,
                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                "/repository"),
                RegistryUtils.getAbsolutePath(
                        registryContext,
                        RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                                "/repository")}) {
            if (cleanRegistry) {
                if (registry.resourceExists(repositoryPath)) {
                    try {
                        registry.delete(repositoryPath);
                        log.info("Cleaned the registry space of carbon at path: " +
                                repositoryPath);
                    } catch (Exception e) {
                        log.error(
                                "An error occurred while cleaning of registry space of carbon " +
                                        "at path: " + repositoryPath, e);
                    }
                }
            } else if (systemResourceShouldBeAdded(registry, repositoryPath)) {
                registry.put(repositoryPath, registry.newCollection());
            }
        }
    }

    /**
     * This method is used to remove LC information from artifacts when clean up operation is done.
     * @param registry
     * @throws RegistryException
     */
    private static void cleanArtifactLCs(Registry registry) throws RegistryException {
        String resourcePaths[];
        String collectionpaths[];
        String resourceQuery = "SELECT R.REG_PATH_ID, R.REG_NAME FROM REG_RESOURCE R, REG_PROPERTY PP, " +
                "REG_RESOURCE_PROPERTY RP WHERE";

        String collectionQuery = "SELECT R.REG_PATH_ID, R.REG_NAME FROM REG_RESOURCE R, REG_PROPERTY PP, " +
                "REG_RESOURCE_PROPERTY RP WHERE";

        if (StaticConfiguration.isVersioningProperties()) {
            resourceQuery += "R.REG_VERSION=RP.REG_VERSION AND "
                    + "RP.REG_PROPERTY_ID=PP.REG_ID AND lower(PP.REG_NAME) LIKE ?";
        } else {
            resourceQuery += "R.REG_PATH_ID=RP.REG_PATH_ID AND "
                    + "((R.REG_NAME = RP.REG_RESOURCE_NAME)) AND "
                    + "RP.REG_PROPERTY_ID=PP.REG_ID AND lower(PP.REG_NAME) LIKE ?";
        }


        if (StaticConfiguration.isVersioningProperties()) {
            collectionQuery += "R.REG_VERSION=RP.REG_VERSION AND "
                    + "RP.REG_PROPERTY_ID=PP.REG_ID AND  lower(PP.REG_NAME) LIKE ?  AND R.REG_NAME IS NULL";
        } else {
            collectionQuery += "R.REG_PATH_ID=RP.REG_PATH_ID AND "
                            + "R.REG_NAME IS NULL AND RP.REG_RESOURCE_NAME IS NULL AND "
                            + "RP.REG_PROPERTY_ID=PP.REG_ID AND  lower(PP.REG_NAME) LIKE ?  AND R.REG_NAME IS NULL";
        }


        Map<String,Object> paramMapResources = new HashMap<String, Object>();
        paramMapResources.put("1","registry.lc.name");
        paramMapResources.put("query", resourceQuery);

        Map<String,Object> paramMapCollections= new HashMap<String, Object>();
        paramMapCollections.put("1","registry.lc.name");
        paramMapCollections.put("query", collectionQuery);

        resourcePaths = (String[]) registry.executeQuery(null,paramMapResources).getContent();
        collectionpaths = (String[]) registry.executeQuery(null,paramMapCollections).getContent();

        for (String path : resourcePaths){
            removeArtifactLC(path,registry);
        }

        for (String path : collectionpaths){
            removeArtifactLC(path,registry);
        }

    }

    /**
     * Method to remove Lifecycle from a given resource on the registry.
     *
     * @param path     the path of the resource.
     * @param registry the registry instance on which the resource is available.
     *
     * @throws RegistryException if the operation failed.
     */
    private static void removeArtifactLC(String path, Registry registry)
            throws RegistryException {

        try {
            /* set all the variables to the resource */
            Resource resource = registry.get(path);
            Properties props = resource.getProperties();
            //List<Property> propList = new ArrayList<Property>();
            Iterator iKeys = props.keySet().iterator();
            ArrayList<String> propertiesToRemove = new ArrayList<String>();

            while (iKeys.hasNext()) {
                String propKey = (String) iKeys.next();

                if (propKey.startsWith("registry.custom_lifecycle.votes.")
                        ||propKey.startsWith("registry.custom_lifecycle.user.")
                        ||propKey.startsWith("registry.custom_lifecycle.checklist.")
                        || propKey.startsWith("registry.LC.name")
                        || propKey.startsWith("registry.lifecycle.")
                        || propKey.startsWith("registry.Aspects")) {
                    propertiesToRemove.add(propKey);
                }
            }

            for(String propertyName : propertiesToRemove) {
                resource.removeProperty(propertyName);
            }

            registry.put(path, resource);

        } catch (RegistryException e) {

            String msg = "Failed to remove LifeCycle from resource " + path + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }
    @Deprecated
    @SuppressWarnings("unused")
    public static void addSystemCollection(Registry registry, UserRealm userRealm)
            throws RegistryException {
        throw new UnsupportedOperationException("This operation is no longer supported.");
        /*if(log.isTraceEnabled()) {
            log.trace("Checking the existence of '" + RegistryConstants.SYSTEM_PATH + "' collection of the Registry.");
        }
        if (!registry.resourceExists(RegistryConstants.SYSTEM_PATH)) {
            if (registry.getRegistryContext() != null && registry.getRegistryContext().isClone()) {
                return;
            }
            if(log.isTraceEnabled()) {
                log.trace("Creating the '" + RegistryConstants.SYSTEM_PATH + "'collection of the Registry.");
            }
            CollectionImpl systemCollection = (CollectionImpl) registry.newCollection();
            String systemDescription = "System collection of the Registry. This collection is used to store " +
                    "the resources required by the Registry server. It is not recommended to do any modification " +
                    "to this collection.";
            systemCollection.setDescription(systemDescription);
            registry.put(RegistryConstants.SYSTEM_PATH, systemCollection);
            systemCollection.discard();

            AuthorizationUtils.denyAnonAuthorization(RegistryConstants.SYSTEM_PATH, userRealm);
        }*/
    }

    @Deprecated
    @SuppressWarnings("unused")
    public static void addCarbonRootCollection(Registry userRegistry, UserRealm userRealm)
            throws RegistryException {
        /*if (userRegistry.getRegistryContext() != null && userRegistry.getRegistryContext().isClone()) {
            return;
        }
        String carbonPathParent = System.getProperty(RegistryConstants.CARBON_COLLECTION_PARENT_PROPERTY);

        String carbonPath;
        if (carbonPathParent != null) {
            ResourcePath rootPath = new ResourcePath(carbonPathParent);
            rootPath.appendPath(RegistryConstants.CARBON_COLLECTION_NAME);
            carbonPath = rootPath.getPath();
        } else {
            carbonPath = RegistryConstants.ROOT_PATH + RegistryConstants.CARBON_COLLECTION_NAME;
        }

        try {

            if (System.getProperty(RegistryConstants.CARBON_REGISTRY_CLEAN) != null) {
                // clean the system property to prevent the registry cleanup at the next
                // restart, if commanded through the admin console, which will carry all the
                // System properties
                System.clearProperty(RegistryConstants.CARBON_REGISTRY_CLEAN);
                if (userRegistry.resourceExists(carbonPath)) {
                    try {
                        userRegistry.delete(carbonPath);
                        log.info("Cleaned the registry space of carbon at path : " + carbonPath);
                    } catch (Exception e) {
                        log.error("An error occurred while cleaning of registry space of carbon " +
                                "at path : " + carbonPath, e);
                    }
                }
            }
            else {
                if (userRegistry.resourceExists(carbonPath)) {
                    return;
                }
            }

            Collection carbonCollection = userRegistry.newCollection();
            String carbonRootDescription = "Root of the system collections used by the Carbon server.";

            carbonCollection.setDescription(carbonRootDescription);

            userRegistry.put(carbonPath, carbonCollection);
            carbonCollection.discard();


            AuthorizationUtils.denyAnonAuthorization(carbonPath, userRealm);

        } catch (Exception e) {
            String msg = "Unable to setup collections used by the Carbon server.";
            log.error(msg, e);
            userRegistry.rollbackTransaction();
            throw new RegistryException(e.getMessage(), e);
        }*/
    }

    @Deprecated
    @SuppressWarnings("unused")
    public static void addTenantsCollection(Registry registry, UserRealm userRealm)
            throws RegistryException {

        throw new UnsupportedOperationException("This method is no longer supported");

    }

    /**
     * Method to add the collection where the user profiles are stored.
     *
     * @param registry     the registry to use.
     * @param profilesPath the path at which user profiles are stored.
     *
     * @throws RegistryException if an error occurred.
     */
    public static void addUserProfileCollection(Registry registry, String profilesPath)
            throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Checking the existence of the '" + profilesPath +
                    "' collection of the Registry.");
        }
        if (systemResourceShouldBeAdded(registry, profilesPath)) {
            if (registry.getRegistryContext() != null && registry.getRegistryContext().isClone()) {
                return;
            }
            if (log.isTraceEnabled()) {
                log.trace("Creating the '" + profilesPath + "' collection of the Registry.");
            }
            CollectionImpl systemCollection = (CollectionImpl) registry.newCollection();
            String systemDescription = "Collection which contains user-specific details.";
            systemCollection.setDescription(systemDescription);
            registry.put(profilesPath, systemCollection);
        }
    }

    /**
     * Method to add the collection where the services are stored.
     *
     * @param registry    the registry to use.
     * @param servicePath the path at which services are stored.
     *
     * @throws RegistryException if an error occurred.
     */
    public static void addServiceStoreCollection(Registry registry, String servicePath)
            throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("Checking the existence of the '" + servicePath +
                    "' collection of the Registry.");
        }
        if (systemResourceShouldBeAdded(registry, servicePath)) {
            if (registry.getRegistryContext() != null && registry.getRegistryContext().isClone()) {
                return;
            }
            if (log.isTraceEnabled()) {
                log.trace("Creating the '" + servicePath + "' collection of the Registry.");
            }
            CollectionImpl systemCollection = (CollectionImpl) registry.newCollection();
            String systemDescription = "Collection which contains all the Service information";
            systemCollection.setDescription(systemDescription);
            registry.put(servicePath, systemCollection);
        }
    }

    /**
     * Method to add the collection where the mount information is stored.
     *
     * @param registry the registry to use.
     *
     * @throws RegistryException if an error occurred.
     */
    public static void addMountCollection(Registry registry) throws RegistryException {

        if (log.isTraceEnabled()) {
            log.trace("Checking the existence of the '" +
                    RegistryConstants.LOCAL_REPOSITORY_BASE_PATH +
                    RegistryConstants.SYSTEM_MOUNT_PATH + "' collection of the Registry.");
        }
        if (!registry.resourceExists(RegistryConstants.LOCAL_REPOSITORY_BASE_PATH +
                RegistryConstants.SYSTEM_MOUNT_PATH)) {
            if (registry.getRegistryContext() != null && registry.getRegistryContext().isClone()) {
                return;
            }
            if (log.isTraceEnabled()) {
                log.trace("Creating the '" + RegistryConstants.LOCAL_REPOSITORY_BASE_PATH +
                        RegistryConstants.SYSTEM_MOUNT_PATH +
                        "' collection of the Registry.");
            }
            Collection mountCollection = registry.newCollection();
            String description = "Mount collection stores details of mount points of the registry.";
            mountCollection.setDescription(description);
            registry.put(RegistryConstants.LOCAL_REPOSITORY_BASE_PATH +
                    RegistryConstants.SYSTEM_MOUNT_PATH, mountCollection);
        }
    }

    /**
     * Method to register mount points.
     *
     * @param systemRegistry the registry to use.
     * @param tenantId       the identifier of the tenant.
     *
     * @throws RegistryException if an error occurred.
     */
    public static void registerMountPoints(Registry systemRegistry, int tenantId)
            throws RegistryException {
        String mountPath = RegistryConstants.LOCAL_REPOSITORY_BASE_PATH +
                RegistryConstants.SYSTEM_MOUNT_PATH;
        if (!systemRegistry.resourceExists(mountPath)) {
            return;
        }
        CollectionImpl mountCollection = (CollectionImpl) systemRegistry.get(mountPath);
        String[] mountPoints = mountCollection.getChildren();
        Resource mountPoint;
        for (String mountPointString : mountPoints) {
            if (!systemRegistry.resourceExists(mountPointString)) {
                log.warn("Unable to add mount. The mount point " + mountPointString +
                        " was not found.");
                continue;
            }
            mountPoint = systemRegistry.get(mountPointString);
            if (Boolean.toString(true).equals(mountPoint.getProperty(
                    RegistryConstants.REGISTRY_FIXED_MOUNT))) {
                continue;
            }
            String path = mountPoint.getProperty("path");
            String target = mountPoint.getProperty("target");
            String targetSubPath = mountPoint.getProperty("subPath");
            String author = mountPoint.getProperty("author");

            try {
                CurrentSession.setCallerTenantId(tenantId);
                if (log.isTraceEnabled()) {
                    log.trace("Creating the mount point. " +
                            "path: " + path + ", " +
                            "target: " + target + ", " +
                            "target sub path " + targetSubPath + ".");
                }
                if (targetSubPath != null) {
                    registerHandlerForRemoteLinks(systemRegistry.getRegistryContext(), path,
                            target, targetSubPath, author);
                } else {
                    registerHandlerForSymbolicLinks(systemRegistry.getRegistryContext(),
                            path, target, author);
                }
            } catch (RegistryException e) {
                log.warn("Couldn't mount " + target + ".");
                log.debug("Caused by: ", e);
            } finally {
                CurrentSession.removeCallerTenantId();
            }
        }
    }

    /**
     * Method to obtain the filter used with the mounting handler.
     *
     * @param path the path at which the mount was added.
     *
     * @return the built filter instance.
     */
    public static URLMatcher getMountingMatcher(String path) {
        URLMatcher matcher = new MountingMatcher();
        String matchedWith = Pattern.quote(path) + "($|" + RegistryConstants.PATH_SEPARATOR +
                ".*|" + RegistryConstants.URL_SEPARATOR + ".*)";
        matcher.setPattern(matchedWith);
        return matcher;
    }

    // This class is used to implement a URL Matcher that could be used for Mounting related
    // handlers.
    private static class MountingMatcher extends URLMatcher {
        // Mounting related handlers support execute query for any path.
        public boolean handleExecuteQuery(RequestContext requestContext)
                throws RegistryException {
            return true;
        }

        // Mounting related handlers support get resource paths with tag for any path.
        public boolean handleGetResourcePathsWithTag(RequestContext requestContext)
                throws RegistryException {
            return true;
        }
    }

    /**
     * Utility method obtain the list of operations supported by the mount handler.
     *
     * @return the list of operations.
     */
    public static String[] getMountingMethods() {
        return new String[]{Filter.RESOURCE_EXISTS, Filter.GET, Filter.PUT, Filter.DELETE,
                Filter.RENAME,
                Filter.MOVE, Filter.COPY, Filter.GET_AVERAGE_RATING, Filter.GET_RATING,
                Filter.RATE_RESOURCE, Filter.GET_COMMENTS, Filter.ADD_COMMENT, Filter.EDIT_COMMENT,
                Filter.REMOVE_COMMENT, Filter.GET_TAGS, Filter.APPLY_TAG, Filter.REMOVE_TAG,
                Filter.GET_ALL_ASSOCIATIONS, Filter.GET_ASSOCIATIONS, Filter.ADD_ASSOCIATION,
                Filter.DUMP, Filter.RESTORE, Filter.REMOVE_ASSOCIATION, Filter.IMPORT,
                Filter.EXECUTE_QUERY, Filter.GET_RESOURCE_PATHS_WITH_TAG,
                Filter.GET_REGISTRY_CONTEXT, Filter.REMOVE_LINK };
    }

    

   

    /**
     * Method to add the resources where the service configuration are stored.
     *
     * @param registry the registry to use.
     *
     * @throws RegistryException if an error occurred.
     */
    public static void addServiceConfigResources(Registry registry) throws RegistryException {

        try {
            boolean inTransaction = Transaction.isStarted();
            if (!inTransaction) {
                registry.beginTransaction();
            }
            ServiceConfigUtil.addConfig(registry); //add the resource service in to /governance/configuration/
            ServiceConfigUtil.addConfigSchema(registry);
            if (!inTransaction) {
                registry.commitTransaction();
            }
        } catch (Exception e) {
            String msg = "Unable to setup service configuration.";
            log.error(msg, e);
            registry.rollbackTransaction();
            throw new RegistryException(e.getMessage(), e);
        }
    }

    /**
     * Method to concatenate two chroot paths.
     *
     * @param chroot1 the base chroot path (or the registry root).
     * @param chroot2 the the relative chroot path.
     *
     * @return the full chroot path.
     */
    public static String concatenateChroot(String chroot1, String chroot2) {
        String chroot1out = chroot1;
        if (chroot1out == null || chroot1out.equals(RegistryConstants.ROOT_PATH)) {
            return chroot2;
        }
        if (chroot2 == null || chroot2.equals(RegistryConstants.ROOT_PATH)) {
            return chroot1out;
        }
        if (!chroot1out.endsWith(RegistryConstants.PATH_SEPARATOR) &&
                !chroot2.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            chroot1out += RegistryConstants.PATH_SEPARATOR;
        } else if (chroot1out.endsWith(RegistryConstants.PATH_SEPARATOR) &&
                chroot2.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            chroot1out = chroot1out.substring(0, chroot1out.length() - 1);
        }
        String chroot = chroot1out + chroot2;
        if (chroot.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            chroot = chroot.substring(0, chroot.length() - 1);
        }
        return chroot;
    }

    /**
     * Method to determine whether this registry is running in Read-Only mode.
     *
     * @param registryContext the registry context.
     *
     * @return true if read-only or false otherwise.
     */
    public static boolean isRegistryReadOnly(RegistryContext registryContext) {
        String repositoryWriteModeProperty = System.getProperty(ServerConstants.REPO_WRITE_MODE);
        if (repositoryWriteModeProperty != null) {
            return !(repositoryWriteModeProperty.equals("true"));
        }
        ServerConfiguration serverConfig = CarbonUtils.getServerConfiguration();

        String isRegistryReadOnly =
                serverConfig.getFirstProperty("Registry.ReadOnly");
        if (isRegistryReadOnly == null) {
            if (registryContext != null) {
                return registryContext.isReadOnly();
            }
            return RegistryContext.getBaseInstance().isReadOnly();
        }
        return isRegistryReadOnly.equalsIgnoreCase(Boolean.TRUE.toString());
    }

    /**
     * this method can only be called if the registry context is initialized.
     *
     * @param coreRegistry the core registry instance.
     *
     * @return the user registry instance for the special system user.
     * @throws RegistryException if the operation failed.
     */
    public static Registry getSystemRegistry(Registry coreRegistry)
            throws RegistryException {

        RealmService realmService = coreRegistry.getRegistryContext().getRealmService();
        String systemUser = CarbonConstants.REGISTRY_SYSTEM_USERNAME;

        if (systemUser.equals(CurrentSession.getUser())) {
            return CurrentSession.getUserRegistry();
        }

        return new UserRegistry(systemUser, CurrentSession.getTenantId(), coreRegistry,
                realmService, CurrentSession.getChroot(), true);
    }

    /**
     * This method returns the bootstrap (or initial) user realm. You can call this function only if
     * the registry context is initialized
     *
     * @return the bootstrap realm.
     * @throws RegistryException if the operation failed.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static UserRealm getBootstrapRealm() throws RegistryException {
        RegistryContext registryContext = RegistryContext.getBaseInstance();
        if (registryContext == null) {
            String msg = "Registry context is null. Failed to get the registry context.";
            log.error(msg);
            throw new RegistryException(msg);
        }
        if (registryContext.getRealmService() == null) {
            String msg = "Error in getting the bootstrap realm. " +
                    "The realm service is not available.";
            log.error(msg);
            throw new RegistryException(msg);
        }
        try {
            UserRealm realm = registryContext.getRealmService().getBootstrapRealm();
            return new RegistryRealm(realm);
        } catch (Exception e) {
            String msg = "Error in getting the user realm for main tenant.";
            log.error(msg);
            throw new RegistryException(msg, e);
        }
    }

    /**
     * Method to record statistics.
     *
     * @param parameters The parameters to be passed to the statistics collector interface.
     *                   Generally, it is expected that each method invoking this method will pass
     *                   all relevant parameters such that the statistics collectors can examine
     *                   them as required.
     */
    @SuppressWarnings("unused")
    // This method is used in Components.
    public static void recordStatistics(Object ... parameters) {
        StatisticsCollector[] statisticsCollectors =
                RegistryContext.getBaseInstance().getStatisticsCollectors();
        for (StatisticsCollector collector : statisticsCollectors) {
            collector.collect(parameters);
        }
    }

    /**
     * This method returns the bootstrap (or initial) user realm from the realm service.
     *
     * @param realmService the OSGi service which we can use to obtain the user realm.
     *
     * @return the bootstrap realm.
     * @throws RegistryException if the operation failed.
     */
    public static UserRealm getBootstrapRealm(RealmService realmService) throws RegistryException {
        try {
            UserRealm realm = realmService.getBootstrapRealm();
            return new RegistryRealm(realm);
        } catch (Exception e) {
            String msg = "Error in getting the user realm for main tenant.";
            log.error(msg);
            throw new RegistryException(msg, e);
        }
    }

    /**
     * Method to obtain the unchrooted path for the given relative path.
     *
     * @param path the relative path.
     *
     * @return the unchrooted path.
     */
    public static String getUnChrootedPath(String path) {
        if (path == null) {
            return null;
        }
        String localPath = path;
        if (CurrentSession.getLocalPathMap() != null) {
            String temp = CurrentSession.getLocalPathMap().get(path);
            if (temp != null) {
                localPath = temp;
            }
        }
        if (CurrentSession.getUserRealm() != null) {
            // we are already checking the unchrooted paths, so no more work
            return localPath;
        }
        
        String chrootPrefix = getChrootPrefix();
        if (chrootPrefix == null) {
            // no chroot defined or it equals to root.
            return localPath;
        } else if (!localPath.startsWith("/")) {
            // not a registry resource
            return localPath;
        } else if (localPath.equals("/")) {
            return chrootPrefix;
        }

        // Relative path, so prepend basePrefix appropriately

        return chrootPrefix + localPath;
    }

    // method to obtain chroot prefix.
    private static String getChrootPrefix() {
        RegistryContext registryContext = RegistryContext.getBaseInstance();
        if (registryContext == null) {
            // adding non-path resources at the start-up
            return null;
        }
        String chrootPrefix = registryContext.getRegistryRoot();
        if (chrootPrefix == null ||
                chrootPrefix.length() == 0 ||
                chrootPrefix.equals(RegistryConstants.ROOT_PATH)) {
            return null;
        }
        return chrootPrefix;
    }

    /**
     * Method to obtain the relative path for the given absolute path.
     *
     * @param context      the registry context.
     * @param absolutePath the absolute path.
     *
     * @return the relative path.
     */
    public static String getRelativePath(RegistryContext context, String absolutePath) {
        if (context == null) {
            return getRelativePathToOriginal(absolutePath,
                    RegistryContext.getBaseInstance().getRegistryRoot());
        }
        return getRelativePathToOriginal(absolutePath, context.getRegistryRoot());
    }

    /**
     * Method to obtain the absolute path for the given relative path.
     *
     * @param context      the registry context.
     * @param relativePath the relative path.
     *
     * @return the absolute path.
     */
    public static String getAbsolutePath(RegistryContext context, String relativePath) {
        if (context == null) {
            return getAbsolutePathToOriginal(relativePath,
                    RegistryContext.getBaseInstance().getRegistryRoot());
        }
        return getAbsolutePathToOriginal(relativePath, context.getRegistryRoot());
    }

    /**
     * Method to obtain the path relative to the given path.
     *
     * @param absolutePath the absolute path.
     * @param originalPath the path to which we need to make the given absolute path a relative
     *                     one.
     *
     * @return the relative path.
     */
    public static String getRelativePathToOriginal(String absolutePath, String originalPath) {
        // the relative path of a path that is null, will be null
        if (absolutePath == null) {
            return null;
        }
        if (!absolutePath.startsWith("/")) {
            // we can consider this is not a path
            return absolutePath;
        }
        // No worries if there's no original path
        if (originalPath == null || originalPath.length() == 0 || originalPath.equals("/")) {
            return absolutePath;
        }

        if (originalPath.equals(absolutePath)) {
            return "/";
        }

        if (absolutePath.startsWith(originalPath)) {
            return absolutePath.substring(originalPath.length());
        }

        // Somewhere else, so make sure there are dual slashes at the beginning
        return "/" + absolutePath;
    }

    /**
     * Method to obtain the path absolute to the given path.
     *
     * @param relativePath the relative path.
     * @param originalPath the path to which we need to make the given relative path a absolute
     *                     one.
     *
     * @return the absolute path.
     */
    public static String getAbsolutePathToOriginal(String relativePath, String originalPath) {
        // the absolute path of a path that is null, will be null
        if (relativePath == null) {
            return null;
        }
        if (!relativePath.startsWith("/")) {
            // we can consider this is not a path
            return relativePath;
        }
        // No worries if there's no original path
        if (originalPath == null || originalPath.length() == 0 || originalPath.equals("/")) {
            return relativePath;
        }
        if (relativePath.startsWith("//")) {
            // the path is outside the scope of the original path, so return absolute path removing
            // the first '/'
            return relativePath.substring(1);
        }
        // then it is the concatenate that make the absolute path
        return originalPath + relativePath;
    }

    /**
     * Method to register handlers for symbolic links for the tenant on the current registry
     * session.
     *
     * @param context the registry context.
     * @param path    the path at which the symbolic link is created.
     * @param target  the target path.
     * @param author  the creator of the symbolic link.
     *
     * @throws RegistryException if the operation failed.
     */
    public static void registerHandlerForSymbolicLinks(RegistryContext context,
                                                       String path, String target, String author)
            throws RegistryException {
        SymLinkHandler handler = new SymLinkHandler();
        handler.setMountPoint(path);
        handler.setTargetPoint(target);
        handler.setAuthor(author);

        HandlerManager hm = context.getHandlerManager();
        hm.addHandler(RegistryUtils.getMountingMethods(),
                RegistryUtils.getMountingMatcher(path), handler,
                HandlerLifecycleManager.TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE);
        // now we are going to iterate through all the already available symbolic links and resolve
        // the cyclic symbolic links

        Set<SymLinkHandler> symLinkHandlers = SymLinkHandler.getSymLinkHandlers();

        List<SymLinkHandler> handlersToRemove = new ArrayList<SymLinkHandler>();
        for (SymLinkHandler symLinkHandler: symLinkHandlers) {
            String symLinkTarget = symLinkHandler.getTargetPoint();

            // and then we remove old entries for the same mount path
            String mountPath = symLinkHandler.getMountPoint();
            if (path.equals(mountPath) && !target.equals(symLinkTarget)) {
                handlersToRemove.add(symLinkHandler);
            }
        }
        // removing the symlink handlers
        for (SymLinkHandler handlerToRemove : handlersToRemove) {
            hm.removeHandler(handlerToRemove,
                HandlerLifecycleManager.TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE);
        }

        // and importantly add the new entry, the currently creating symlink information..
        symLinkHandlers.add(handler);
    }

    /**
     * Method to register handlers for remote links for the tenant on the current registry session.
     *
     * @param registryContext the registry context.
     * @param path            the path at which the remote link is created.
     * @param target          the target path.
     * @param targetSubPath   the target sub-path.
     * @param author          the creator of the remote link.
     *
     * @throws RegistryException if the operation failed.
     */
    public static void registerHandlerForRemoteLinks(RegistryContext registryContext,
                                                     String path, String target,
                                                     String targetSubPath, String author)
            throws RegistryException {
        registerHandlerForRemoteLinks(registryContext, path, target, targetSubPath, author, false);
    }

    /**
     * Method to register handlers for remote links.
     *
     * @param registryContext the registry context.
     * @param path            the path at which the remote link is created.
     * @param target          the target path.
     * @param targetSubPath   the target sub-path.
     * @param author          the creator of the remote link.
     * @param forAllTenants   whether the remote link should be added to the tenant on the current
     *                        registry session or to all the tenants.
     *
     * @throws RegistryException if the operation failed.
     */
    public static void registerHandlerForRemoteLinks(RegistryContext registryContext,
                                                     String path, String target,
                                                     String targetSubPath, String author,
                                                     boolean forAllTenants)
            throws RegistryException {
        HandlerManager hm = registryContext.getHandlerManager();
        List<RemoteConfiguration> remoteInstances = registryContext.getRemoteInstances();
        for (RemoteConfiguration config : remoteInstances) {
            if (config.getId().equals(target)) {
                MountHandler handler = new MountHandler();
                handler.setUserName(config.getTrustedUser());
                handler.setPassword(config.getResolvedTrustedPwd());
                handler.setDbConfig(config.getDbConfig());
                handler.setRegistryRoot(config.getRegistryRoot());
                handler.setReadOnly(config.getReadOnly() != null &&
                        Boolean.toString(true).equals(config.getReadOnly().toLowerCase()));
                handler.setId(target);
                handler.setConURL(config.getUrl());
                handler.setMountPoint(path);
                handler.setSubPath(targetSubPath);
                handler.setAuthor(author);
                if (config.getTrustedUser() == null || config.getTrustedPwd() == null) {
                    handler.setRemote(false);
                } else {
                    handler.setRemote(true);
                    handler.setRegistryType(config.getType());
                }
                if (forAllTenants) {
                    hm.addHandler(RegistryUtils.getMountingMethods(),
                            RegistryUtils.getMountingMatcher(path), handler);
                } else {
                    hm.addHandler(RegistryUtils.getMountingMethods(),
                            RegistryUtils.getMountingMatcher(path), handler,
                            HandlerLifecycleManager.TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE);
                }
                return;
            }
        }
        // We will get here, if somebody checks in to a remote registry with a no mount paths.
        // Such information ends up in our debug logs.
        if (remoteInstances.size() == 0) {
            String msg = "No remote instances have been found, " +
                    "The following mount point is not registered. " +
                    "path: " + path + ", " +
                    "target: " + target + ", " +
                    ((targetSubPath == null)? "": ("target sub path: " + targetSubPath + ", "));
            log.debug(msg);
        } else {
            String msg = "Target mount path is not found, " +
                    "The following mount point is not registered. " +
                    "path: " + path + ", " +
                    "target: " + target + ", " +
                    ((targetSubPath == null)? "": ("target sub path: " + targetSubPath + ", "));
            log.debug(msg);
        }
    }

    /**
     * Method to add a mount entry.
     *
     * @param registry        the registry instance to use.
     * @param registryContext the registry context.
     * @param path            the source path.
     * @param target          the target path or instance.
     * @param targetSubPath   the target sub-path.
     * @param author          the author
     *
     * @throws RegistryException if the operation failed.
     */
    public static void addMountEntry(Registry registry, RegistryContext registryContext,
                                     String path, String target, String targetSubPath,
                                     String author)
            throws RegistryException {
        Resource r = new ResourceImpl();
        String relativePath = RegistryUtils.getRelativePath(registryContext, path);
        r.addProperty("path", relativePath);
        r.addProperty("target", target);
        r.addProperty("author", author);
        r.addProperty("subPath", targetSubPath);
        r.setMediaType(RegistryConstants.MOUNT_MEDIA_TYPE);
        String mountPath = RegistryConstants.LOCAL_REPOSITORY_BASE_PATH +
                        RegistryConstants.SYSTEM_MOUNT_PATH + "/" +
                relativePath.replace("/", "-");
        if (!registry.resourceExists(mountPath)) {
            registry.put(mountPath, r);
        }
    }

    /**
     * Method to add a mount entry.
     *
     * @param registry        the registry instance to use.
     * @param registryContext the registry context.
     * @param path            the source path.
     * @param target          the target path or instance.
     * @param remote          whether local or remote link.
     * @param author          the author
     *
     * @throws RegistryException if the operation failed.
     */
    public static void addMountEntry(Registry registry, RegistryContext registryContext,
                                     String path, String target, boolean remote, String author)
            throws RegistryException {
        //persist mount details
        Resource r = new ResourceImpl();
        String relativePath;
        if (remote) {
            relativePath = path;
        } else {
            relativePath = RegistryUtils.getRelativePath(registryContext, path);
        }
        r.addProperty("path", relativePath);
        r.addProperty("target", RegistryUtils.getRelativePath(registryContext,
                target));
        r.addProperty("author", author);
        r.setMediaType(RegistryConstants.MOUNT_MEDIA_TYPE);
        String mountPath = RegistryConstants.LOCAL_REPOSITORY_BASE_PATH +
                        RegistryConstants.SYSTEM_MOUNT_PATH + "/" +
                relativePath.replace("/", "-");
        if (!registry.resourceExists(mountPath)) {
            registry.put(mountPath, r);
        }
    }

    /**
     * Gets the resource with sufficient data to differentiate it from another resource. This would
     * populate a {@link ResourceImpl} with the <b>path</b>, <b>name</b> and <b>path identifier</b>
     * of a resource.
     *
     * @param path        the path of the resource.
     * @param resourceDAO the resource data access object to use.
     * @param versioned   whether version or not.
     *
     * @return the resource with minimum data.
     * @throws RegistryException if an error occurs while retrieving resource data.
     */
    public static ResourceImpl getResourceWithMinimumData(String path, ResourceDAO resourceDAO,
                                                          boolean versioned)
            throws RegistryException {
        ResourceIDImpl resourceID = resourceDAO.getResourceID(path);
        if (resourceID == null) {
            return null;
        }
        ResourceImpl resourceImpl;
        if (resourceID.isCollection()) {
            resourceImpl = new CollectionImpl();
        } else {
            resourceImpl = new ResourceImpl();
        }
        if (versioned) {
            resourceImpl.setVersionNumber(resourceDAO.getVersion(resourceID));
        } else {
            resourceImpl.setName(resourceID.getName());
            resourceImpl.setPathID(resourceID.getPathID());
        }
        resourceImpl.setPath(path);
        return resourceImpl;
    }

    /**
     * Calculate the relative associations path  to the reference path if an absolute path is given.
     * This is used in dump.
     *
     * @param path          absolute path value.
     * @param referencePath the reference path
     *
     * @return the relative path
     */
    public static String getRelativeAssociationPath(String path, String referencePath) {
        //StringTokenizer basePathTokenizer = new StringTokenizer(referencePath);
        String[] referencePathParts = referencePath.split("/");
        String[] pathParts = path.split("/");

        int i;
        for (i = 0; i < referencePathParts.length - 1 && i < pathParts.length - 1; i++) {
            if (!referencePathParts[i].equals(pathParts[i])) {
                break;
            }
        }


        StringBuilder prefix = new StringBuilder();
        int j = i;
        for (; i < referencePathParts.length - 1; i++) {
            prefix.append("../");
        }

        for (; j < pathParts.length - 1; j++) {
            prefix.append(pathParts[j]).append("/");
        }

        String relPath;
        if(pathParts.length != 0){
            relPath = prefix.append(pathParts[j]).toString();
        } else {
            relPath = path; //For the root("/") associations
        }

        while (relPath.contains("//")) {
            relPath = relPath.replaceAll("//", "/../"); // in case "//" is found.
        }
        return relPath;
    }

    /**
     * Calculate the absolute associations path if a relative path is given to the reference path.
     * This works only ".." components are there at the very start. This is used in restore.
     *
     * @param path          relative path value.
     * @param referencePath the reference path
     *
     * @return the absolute path
     */
    public static String getAbsoluteAssociationPath(String path,
                                                    String referencePath) {
        //StringTokenizer basePathTokenizer = new StringTokenizer(referencePath);
        String[] referencePathParts = referencePath.split("/");
        String[] pathParts = path.split("/");

        int i;
        // here the limit of going up = referencePathParts.length - 2
        // (excluding "" component and resource name)
        for (i = 0; i < referencePathParts.length - 2 && i < pathParts.length; i++) {
            if (!pathParts[i].equals("..")) {
                break;
            }
        }
        int goUps = i;
        StringBuilder absolutePath = new StringBuilder();
        // we are checking whether the path parts have more .. beyond the reference paths reach
        for (; i < pathParts.length; i++) {
            if (pathParts[i].equals("..")) {
                absolutePath.append("/");
            } else {
                break;
            }
        }
        int remainLen = i;

        for (i = 0; i < referencePathParts.length - goUps - 1; i++) {
            absolutePath.append(referencePathParts[i]).append("/");
        }
        // from that onwards we collecting path parts.
        for (i = remainLen; i < pathParts.length - 1; i++) {
            absolutePath.append(pathParts[i]).append("/");
        }
        return absolutePath.append(pathParts[pathParts.length - 1]).toString();
    }

    /**
     * Load the class with the given name
     *
     * @param name name of the class
     *
     * @return java class
     * @throws ClassNotFoundException if the class does not exists in the classpath
     */
    public static Class loadClass(String name) throws ClassNotFoundException {
        try {
            return Class.forName(name);
        } catch(ClassNotFoundException e) {
            File extensionLibDirectory = new File(RegistryUtils.getExtensionLibDirectoryPath());
            if (extensionLibDirectory.exists() && extensionLibDirectory.isDirectory()) {
                File[] files = extensionLibDirectory.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name != null && name.endsWith(".jar");
                    }
                });
                if (files != null && files.length > 0) {
                    List<URL> urls = new ArrayList<URL>(files.length);
                    for(File file : files) {
                        try {
                            urls.add(file.toURI().toURL());
                        } catch (MalformedURLException ignore) { }
                    }
                    ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
                    try {
                        ClassLoader cl = new URLClassLoader(urls.toArray(new URL[urls.size()]),
                                RegistryUtils.class.getClassLoader());
                        return cl.loadClass(name);
                    } finally {
                        Thread.currentThread().setContextClassLoader(origTCCL);
                    }
                }
            }
            throw e;
        }
    }

    /**
     * Method to determine whether a property is a hidden property or not.
     * @param propertyName the name of property.
     * @return true if the property is a hidden property or false if not.
     */
    public static boolean isHiddenProperty(String propertyName) {
        return propertyName.startsWith("registry.");
    }

    public static String getExtensionLibDirectoryPath() {
        CarbonContext carbonContext =
               CarbonContext.getThreadLocalCarbonContext(); 
        int tempTenantId = carbonContext.getTenantId();
        return ((tempTenantId != MultitenantConstants.INVALID_TENANT_ID &&
        		tempTenantId != MultitenantConstants.SUPER_TENANT_ID) ?
                (CarbonUtils.getCarbonTenantsDirPath() + File.separator +
                        carbonContext.getTenantId()) :
                (CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                        "deployment" + File.separator + "server")) +
                File.separator + "registryextensions";
    }

    public static String decodeBytes(byte[] byteContent)
            throws RegistryException {
        String co;
        try {
            if (ENCODING == null) {
                co = new String(byteContent);
            } else {
                co = new String(byteContent, ENCODING);
            }
        } catch (UnsupportedEncodingException e) {
            String msg = ENCODING + " is unsupported encoding type";
            log.error(msg ,e);
            throw new RegistryException(msg, e);
        }
        return co;
    }

    public static byte[] encodeString(String content) throws RegistryException {
        byte[] bytes;
        try {
            if (ENCODING == null) {
                bytes = content.getBytes();
            } else {
                bytes = content.getBytes(ENCODING);
            }

        } catch (UnsupportedEncodingException e) {
            String msg = ENCODING + " is unsupported encoding type";
            log.error(msg,e);
            throw new RegistryException(msg, e);
        }
        return bytes;
    }
}

