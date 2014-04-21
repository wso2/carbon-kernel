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
package org.wso2.carbon.registry.core.caching;

import javax.cache.Cache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.api.GhostResource;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.DataBaseConfiguration;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.config.RemoteConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistry;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * CacheBackedRegistry has wrapped from original Registry interface to support caching
 */
public class CacheBackedRegistry implements Registry {

    /**
     * wrapped this original registry object from CachedBackedRegistry
     */
    private Registry registry;

    private int tenantId = MultitenantConstants.INVALID_TENANT_ID;

    private Map<String, String> cacheIds =
            new HashMap<String, String>();
    private Map<String, DataBaseConfiguration> dbConfigs =
            new HashMap<String, DataBaseConfiguration>();
    private Map<String, String> pathMap =
            new HashMap<String, String>();

    private static Cache<RegistryCacheKey, GhostResource> getCache() {
        return RegistryUtils.getResourceCache(RegistryConstants.REGISTRY_CACHE_BACKED_ID);
    }

    private static final Log log = LogFactory.getLog(CacheBackedRegistry.class);


    public CacheBackedRegistry(Registry registry) {
        this.registry = registry;
        RegistryContext registryContext = RegistryContext.getBaseInstance();
        for (Mount mount : registryContext.getMounts()) {
            for(RemoteConfiguration configuration : registryContext.getRemoteInstances()) {
                if (configuration.getDbConfig() != null &&
                        mount.getInstanceId().equals(configuration.getId())) {
                    dbConfigs.put(mount.getPath(),
                            registryContext.getDBConfig(configuration.getDbConfig()));
                    pathMap.put(mount.getPath(), mount.getTargetPath());
                } else if (configuration.getCacheId() != null &&
                        mount.getInstanceId().equals(configuration.getId())) {
                    cacheIds.put(mount.getPath(), configuration.getCacheId());
                    pathMap.put(mount.getPath(), mount.getTargetPath());
                }
            }
        }
    }

    public CacheBackedRegistry(Registry registry, int tenantId) {
        this(registry);
        this.tenantId = tenantId;
    }

    /**
     * This method used to calculate the cache key
     *
     * @param registry Registry
     * @param path     Resource path
     *
     * @return RegistryCacheKey
     */
    private RegistryCacheKey getRegistryCacheKey(Registry registry, String path) {
        String connectionId = "";

        int tenantId;
        if (this.tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            tenantId = CurrentSession.getTenantId();
            if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            }
        } else {
            tenantId = this.tenantId;
        }
        String resourceCachePath;
        RegistryContext registryContext = registry.getRegistryContext();
        if (registryContext == null) {
            registryContext = RegistryContext.getBaseInstance();
        }
        if (registry instanceof EmbeddedRegistry) {
            resourceCachePath = path;
        } else {
            resourceCachePath = RegistryUtils.getAbsolutePath(registryContext, path);
        }
        DataBaseConfiguration dataBaseConfiguration = null;
        if (dbConfigs.size() > 0) {
            for (String sourcePath : dbConfigs.keySet()) {
                if (resourceCachePath.startsWith(sourcePath)) {
                    resourceCachePath = pathMap.get(sourcePath) + resourceCachePath.substring(sourcePath.length());
                    dataBaseConfiguration = dbConfigs.get(sourcePath);
                    break;
                }
            }
        } else if (cacheIds.size() > 0) {
            for (String sourcePath : cacheIds.keySet()) {
                if (resourceCachePath.startsWith(sourcePath)) {
                    resourceCachePath = pathMap.get(sourcePath) + resourceCachePath.substring(sourcePath.length());
                    connectionId = cacheIds.get(sourcePath);
                    break;
                }
            }
        }
        if (connectionId.length() == 0) {
            if (dataBaseConfiguration == null) {
                dataBaseConfiguration = registryContext.getDefaultDataBaseConfiguration();
            }
            if (dataBaseConfiguration != null) {
                connectionId = (dataBaseConfiguration.getUserName() != null
                        ? dataBaseConfiguration.getUserName().split("@")[0]:dataBaseConfiguration.getUserName()) + "@" + dataBaseConfiguration.getDbUrl();
            }
        }

        return RegistryUtils.buildRegistryCacheKey(connectionId, tenantId, resourceCachePath);
    }

    @SuppressWarnings("unchecked")
    public Resource get(String path) throws RegistryException {
        if (registry.getRegistryContext().isNoCachePath(path) || isCommunityFeatureRequest(path)) {
            return registry.get(path);
        }
        
        Resource resource;        
        if (!AuthorizationUtils.authorize(path, ActionConstants.GET)) {
            String msg = "User " + CurrentSession.getUser() + " is not authorized to " +
                    "read the resource " + path + ".";
            log.warn(msg);
            throw new AuthorizationFailedException(msg);
        }

        GhostResource<Resource> ghostResource = getGhostResourceFromCache(path);

        resource = ghostResource.getResource();
        if (resource == null) {
            resource = registry.get(path);
            if (resource.getProperty(RegistryConstants.REGISTRY_LINK) == null ||
                    resource.getProperty(RegistryConstants.REGISTRY_MOUNT) != null) {
                ghostResource.setResource(resource);
            }
        }

        return resource;
    }
    
    private GhostResource<Resource> getGhostResourceFromCache(String path) throws RegistryException {
        Resource resource;
        RegistryCacheKey registryCacheKey = getRegistryCacheKey(registry, path);

        GhostResource<Resource> ghostResource;
        Object ghostResourceObject;
        Cache<RegistryCacheKey, GhostResource> cache = getCache();
        if ((ghostResourceObject = cache.get(registryCacheKey)) == null) {
            synchronized (path.intern()){
                //Checking again as the some other previous thread might have updated the cache
                if ((ghostResourceObject = cache.get(registryCacheKey)) == null) {
                    resource = registry.get(path);
                    ghostResource = new GhostResource<Resource>(resource);
                    if (resource.getProperty(RegistryConstants.REGISTRY_LINK) == null ||
                            resource.getProperty(RegistryConstants.REGISTRY_MOUNT) != null) {
                        cache.put(registryCacheKey, ghostResource);
                    }
                }else {
                    ghostResource = (GhostResource<Resource>) ghostResourceObject;
                }
            }
        } else {
            ghostResource = (GhostResource<Resource>) ghostResourceObject;
        }
       return ghostResource;
    }


    private GhostResource<Resource> getGhostCollectionFromCache(String path, int start, int pageSize)
            throws RegistryException {
        Collection collection;

        GhostResource<Resource> ghostResource;

        RegistryCacheKey registryCacheKey = getRegistryCacheKey(registry, path +
                ";start=" + start + ";pageSize=" + pageSize);
        
        Cache<RegistryCacheKey, GhostResource> cache = getCache();
        if (!cache.containsKey(registryCacheKey)) {
            synchronized (path.intern()) {
                //check again to cache to validate whether any other thread have updated with that time.
                if (!cache.containsKey(registryCacheKey)) {
                    collection = registry.get(path, start, pageSize);
                    ghostResource = new GhostResource<Resource>(collection);
                    if (collection.getProperty(RegistryConstants.REGISTRY_LINK) == null) {
                        cache.put(registryCacheKey, ghostResource);
                    }
                } else {
                    ghostResource =
                            (GhostResource<Resource>) cache.get(registryCacheKey);
                }
            }
        } else {
            ghostResource =
                    (GhostResource<Resource>) cache.get(registryCacheKey);
        }

        return ghostResource;
    }

    @SuppressWarnings("unchecked")
    public Collection get(String path, int start, int pageSize) throws RegistryException {
        if (registry.getRegistryContext().isNoCachePath(path) || isCommunityFeatureRequest(path)) {
            return registry.get(path, start, pageSize);
        }
        if (!AuthorizationUtils.authorize(path, ActionConstants.GET)) {
            String msg = "User " + CurrentSession.getUser() + " is not authorized to " +
                    "read the resource " + path + ".";
            log.warn(msg);
            throw new AuthorizationFailedException(msg);
        }

        GhostResource<Resource> ghostResource = getGhostCollectionFromCache(path, start, pageSize);
        Collection collection = (Collection) ghostResource.getResource();
        if (collection == null) {
            collection = registry.get(path, start, pageSize);
            if (collection.getProperty(RegistryConstants.REGISTRY_LINK) == null) {
                ghostResource.setResource(collection);
            }
        }
        return collection;
    }

    // test whether this request was made specifically for a tag, comment or a rating.
    private boolean isCommunityFeatureRequest(String path) {
        if (path == null) {
            return false;
        }
        String resourcePath = new ResourcePath(path).getPath();
        if (path.length() > resourcePath.length()) {
            String fragment = path.substring(resourcePath.length());
            for (String temp : new String[] {"tags", "comments", "ratings"}) {
                if (fragment.contains(temp)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean resourceExists(String path) throws RegistryException {
        if (registry.getRegistryContext().isNoCachePath(path)) {
            return registry.resourceExists(path);
        }
        Cache<RegistryCacheKey, GhostResource> cache = getCache();
        RegistryCacheKey registryCacheKey = getRegistryCacheKey(registry, path);
        if (cache.containsKey(registryCacheKey)) {
            return true;
        } else if (registry.resourceExists(path)) {
            cache.put(registryCacheKey, new GhostResource<Resource>(null));
            return true;
        }
        return false;
    }

    public Resource getMetaData(String path) throws RegistryException {
        return registry.getMetaData(path);
    }

    public String importResource(String suggestedPath, String sourceURL, Resource resource)
            throws RegistryException {
        return registry.importResource(suggestedPath, sourceURL, resource);
    }

    public String rename(String currentPath, String newName) throws RegistryException {
        return registry.rename(currentPath, newName);
    }

    public String move(String currentPath, String newPath) throws RegistryException {
        return registry.move(currentPath, newPath);
    }

    public String copy(String sourcePath, String targetPath) throws RegistryException {
        return registry.copy(sourcePath, targetPath);
    }

    public void createVersion(String path) throws RegistryException {
        registry.createVersion(path);
    }

    public String[] getVersions(String path) throws RegistryException {
        return registry.getVersions(path);
    }

    public void restoreVersion(String versionPath) throws RegistryException {
        registry.restoreVersion(versionPath);
    }

    public void addAssociation(String sourcePath, String targetPath, String associationType)
            throws RegistryException {
        registry.addAssociation(sourcePath, targetPath, associationType);
    }

    public void removeAssociation(String sourcePath, String targetPath, String associationType)
            throws RegistryException {
        registry.removeAssociation(sourcePath, targetPath, associationType);
    }

    public Association[] getAllAssociations(String resourcePath) throws RegistryException {
        return registry.getAllAssociations(resourcePath);
    }

    public Association[] getAssociations(String resourcePath, String associationType)
            throws RegistryException {
        return registry.getAssociations(resourcePath, associationType);
    }

    public void applyTag(String resourcePath, String tag) throws RegistryException {
        registry.applyTag(resourcePath, tag);
    }

    public TaggedResourcePath[] getResourcePathsWithTag(String tag) throws RegistryException {
        return registry.getResourcePathsWithTag(tag);
    }

    public Tag[] getTags(String resourcePath) throws RegistryException {
        return registry.getTags(resourcePath);
    }

    public void removeTag(String path, String tag) throws RegistryException {
        registry.removeTag(path, tag);
    }

    public String addComment(String resourcePath, Comment comment) throws RegistryException {
        return registry.addComment(resourcePath, comment);
    }

    public void editComment(String commentPath, String text) throws RegistryException {
        registry.editComment(commentPath, text);
    }

    public void removeComment(String commentPath) throws RegistryException {
        registry.removeComment(commentPath);
    }

    public Comment[] getComments(String resourcePath) throws RegistryException {
        return registry.getComments(resourcePath);
    }

    public void rateResource(String resourcePath, int rating) throws RegistryException {
        registry.rateResource(resourcePath, rating);
    }

    public float getAverageRating(String resourcePath) throws RegistryException {
        return registry.getAverageRating(resourcePath);
    }

    public int getRating(String path, String userName) throws RegistryException {
        return registry.getRating(path, userName);
    }

    public Collection executeQuery(String path, Map parameters) throws RegistryException {
        return registry.executeQuery(path, parameters);
    }

    public LogEntry[] getLogs(String resourcePath, int action, String userName, Date from, Date to,
                              boolean recentFirst) throws RegistryException {
        return registry.getLogs(resourcePath, action, userName, from, to, recentFirst);
    }

    public LogEntryCollection getLogCollection(String resourcePath, int action, String userName,
                                               Date from, Date to, boolean recentFirst)
            throws RegistryException {
        return registry.getLogCollection(resourcePath, action, userName, from, to, recentFirst);
    }

    public String[] getAvailableAspects() {
        return registry.getAvailableAspects();
    }

    public void associateAspect(String resourcePath, String aspect) throws RegistryException {
        registry.associateAspect(resourcePath, aspect);
    }

    public void invokeAspect(String resourcePath, String aspectName, String action)
            throws RegistryException {
        registry.invokeAspect(resourcePath, aspectName, action);
    }

    public void invokeAspect(String resourcePath, String aspectName, String action,
                             Map<String, String> parameters)
            throws RegistryException {
        registry.invokeAspect(resourcePath, aspectName, action, parameters);
    }

    public String[] getAspectActions(String resourcePath, String aspectName)
            throws RegistryException {
        return registry.getAspectActions(resourcePath, aspectName);
    }

    public RegistryContext getRegistryContext() {
        return registry.getRegistryContext();
    }

    public Collection searchContent(String keywords) throws RegistryException {
        return registry.searchContent(keywords);
    }

    public void createLink(String path, String target) throws RegistryException {
        registry.createLink(path, target);
    }

    public void createLink(String path, String target, String subTargetPath)
            throws RegistryException {
        registry.createLink(path, target, subTargetPath);
    }

    public void removeLink(String path) throws RegistryException {
        registry.removeLink(path);
    }

    public void restore(String path, Reader reader) throws RegistryException {
        registry.restore(path, reader);
    }

    public void dump(String path, Writer writer) throws RegistryException {
        registry.dump(path, writer);
    }

    public String getEventingServiceURL(String path) throws RegistryException {
        return registry.getEventingServiceURL(path);
    }

    public void setEventingServiceURL(String path, String eventingServiceURL)
            throws RegistryException {
        registry.setEventingServiceURL(path, eventingServiceURL);
    }

    public boolean removeAspect(String aspect) throws RegistryException {
        return registry.removeAspect(aspect);
    }

    public boolean addAspect(String name, Aspect aspect) throws RegistryException {
        return registry.addAspect(name, aspect);
    }

    public void beginTransaction() throws RegistryException {
        registry.beginTransaction();
    }

    public void commitTransaction() throws RegistryException {
        registry.commitTransaction();
    }

    public void rollbackTransaction() throws RegistryException {
        registry.rollbackTransaction();
    }

    public Resource newResource() throws RegistryException {
        return registry.newResource();
    }

    public Collection newCollection() throws RegistryException {
        return registry.newCollection();
    }

    public String importResource(String suggestedPath, String sourceURL,
                                 org.wso2.carbon.registry.api.Resource resource)
            throws org.wso2.carbon.registry.api.RegistryException {
        return registry.importResource(suggestedPath, sourceURL, resource);
    }

    public String put(String suggestedPath, Resource resource) throws RegistryException {
        return registry.put(suggestedPath, resource);
    }

    public void delete(String path) throws RegistryException {
        registry.delete(path);
    }

    public String addComment(String resourcePath, org.wso2.carbon.registry.api.Comment comment)
            throws org.wso2.carbon.registry.api.RegistryException {
        return registry.addComment(resourcePath, comment);
    }

    public String put(String suggestedPath, org.wso2.carbon.registry.api.Resource resource)
            throws org.wso2.carbon.registry.api.RegistryException {
        return registry.put(suggestedPath, resource);
    }
    
    public boolean removeVersionHistory(String path, long snapshotId)
    		throws RegistryException {
    	return registry.removeVersionHistory(path, snapshotId);
    }
}
