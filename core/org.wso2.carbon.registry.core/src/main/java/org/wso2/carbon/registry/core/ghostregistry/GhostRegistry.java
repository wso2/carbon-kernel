
/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.core.ghostregistry;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.Map;

/**
 * This implements the Ghost lazy loading pattern for the Registry. An actual registry instance will
 * not be created until first access.
 */
public class GhostRegistry implements Registry{

    private static Log log = LogFactory.getLog(GhostRegistry.class);
    
    private RegistryService registryService;
    private int             tenantId;
    private RegistryType    registryType;

    private Registry registry;


    public GhostRegistry(RegistryService registryService, int tenantId, RegistryType registryType){
        this.registryService = registryService;
        this.tenantId = tenantId;
        this.registryType = registryType;
    }

    private Registry getRegistry() throws RegistryException {
        if (registry != null) {
            return registry;
        }
        try {
            switch (registryType) {
                case SYSTEM_GOVERNANCE:
                    registry = (Registry) registryService.getGovernanceSystemRegistry(tenantId);
                    break;
                case SYSTEM_CONFIGURATION:
                    registry = (Registry) registryService.getConfigSystemRegistry(tenantId);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid registry type " + registryType);
            }
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            throw new RegistryException(e.getMessage(), e);
        }
        return registry;
    }

    public Resource getMetaData(String path) throws RegistryException {
        return getRegistry().getMetaData(path);
    }

    public String importResource(String suggestedPath, String sourceURL,
                                 Resource resource) throws RegistryException {
        return getRegistry().importResource(suggestedPath, sourceURL, resource);
    }

    public String rename(String currentPath, String newName) throws RegistryException {
        return getRegistry().rename(currentPath, newName);
    }

    public String move(String currentPath, String newPath) throws RegistryException {
        return getRegistry().move(currentPath, newPath);
    }

    public String copy(String sourcePath, String targetPath) throws RegistryException {
        return getRegistry().copy(sourcePath, targetPath);
    }

    public void createVersion(String path) throws RegistryException {
        getRegistry().createVersion(path);
    }

    public String[] getVersions(String path) throws RegistryException {
        return getRegistry().getVersions(path);
    }

    public void restoreVersion(String versionPath) throws RegistryException {
        getRegistry().restoreVersion(versionPath);
    }

    public void addAssociation(String sourcePath, String targetPath,
                               String associationType) throws RegistryException {
        getRegistry().addAssociation(sourcePath, targetPath, associationType);
    }

    public void removeAssociation(String sourcePath, String targetPath,
                                  String associationType) throws RegistryException {
        getRegistry().removeAssociation(sourcePath, targetPath, associationType);
    }

    public Association[] getAllAssociations(String resourcePath) throws RegistryException {
        return getRegistry().getAllAssociations(resourcePath);
    }

    public Association[] getAssociations(String resourcePath,
                                         String associationType) throws RegistryException {
        return getRegistry().getAssociations(resourcePath, associationType);
    }

    public void applyTag(String resourcePath, String tag) throws RegistryException {
        getRegistry().applyTag(resourcePath, tag);
    }

    public TaggedResourcePath[] getResourcePathsWithTag(String tag) throws RegistryException {
        return getRegistry().getResourcePathsWithTag(tag);
    }

    public Tag[] getTags(String resourcePath) throws RegistryException {
        return getRegistry().getTags(resourcePath);
    }

    public void removeTag(String path, String tag) throws RegistryException {
        getRegistry().removeTag(path, tag);
    }

    public String addComment(String resourcePath, Comment comment) throws RegistryException {
        return getRegistry().addComment(resourcePath, comment);
    }

    public void editComment(String commentPath, String text) throws RegistryException {
        getRegistry().editComment(commentPath, text);
    }

    public void removeComment(String commentPath) throws RegistryException {
        getRegistry().removeComment(commentPath);
    }

    public Comment[] getComments(String resourcePath) throws RegistryException {
        return getRegistry().getComments(resourcePath);
    }

    public void rateResource(String resourcePath, int rating) throws RegistryException {
        getRegistry().rateResource(resourcePath, rating);
    }

    public float getAverageRating(String resourcePath) throws RegistryException {
        return getRegistry().getAverageRating(resourcePath);
    }

    public int getRating(String path, String userName) throws RegistryException {
        return getRegistry().getRating(path, userName);
    }
    

    public Collection executeQuery(String path, Map parameters) throws RegistryException {
        return getRegistry().executeQuery(path, parameters);
    }

    public String[] getAvailableAspects() {
        try {
            return getRegistry().getAvailableAspects();
        } catch (RegistryException e) {
            throw new RuntimeException(e);
        }
    }

    public void associateAspect(String resourcePath, String aspect) throws RegistryException {
        getRegistry().associateAspect(resourcePath, aspect);
    }

    public void invokeAspect(String resourcePath, String aspectName,
                             String action) throws RegistryException {
        getRegistry().invokeAspect(resourcePath, aspectName, action);
    }

    public String[] getAspectActions(String resourcePath,
                                     String aspectName) throws RegistryException {
        return getRegistry().getAspectActions(resourcePath, aspectName);
    }

    public Collection searchContent(String keywords) throws RegistryException {
        return getRegistry().searchContent(keywords);
    }

    public void createLink(String path, String target) throws RegistryException {
        getRegistry().createLink(path, target);
    }

    public void createLink(String path, String target,
                           String subTargetPath) throws RegistryException {
        getRegistry().createLink(path, target, subTargetPath);
    }

    public void removeLink(String path) throws RegistryException {
        getRegistry().removeLink(path);
    }

    public void restore(String path, Reader reader) throws RegistryException {
        getRegistry().restore(path, reader);
    }

    public void dump(String path, Writer writer) throws RegistryException {
        getRegistry().dump(path, writer);
    }

    public String getEventingServiceURL(String path) throws RegistryException {
        return getRegistry().getEventingServiceURL(path);
    }

    public void setEventingServiceURL(String path,
                                      String eventingServiceURL) throws RegistryException {
        getRegistry().setEventingServiceURL(path, eventingServiceURL);
    }

    public Resource newResource() throws RegistryException {
        return getRegistry().newResource();
    }

    public Collection newCollection() throws RegistryException {
        return getRegistry().newCollection();
    }

    public Resource get(String path) throws RegistryException {
        return getRegistry().get(path);
    }

    public Collection get(String path, int start, int pageSize) throws RegistryException {
        return getRegistry().get(path, start, pageSize);
    }

    public boolean resourceExists(String path) throws RegistryException {
        return getRegistry().resourceExists(path);
    }

    public void delete(String path) throws RegistryException {
        getRegistry().delete(path);
    }

    @Override
    public String put(String suggestedPath, Resource resource) throws RegistryException {
        return getRegistry().put(suggestedPath, resource);
    }

    @Override
    public String put(String suggestedPath, org.wso2.carbon.registry.api.Resource resource) 
            throws org.wso2.carbon.registry.api.RegistryException {
        return getRegistry().put(suggestedPath, resource);
    }

    @Override
    public void beginTransaction() throws RegistryException {
        getRegistry().beginTransaction();
    }

    @Override
    public void commitTransaction() throws RegistryException {
        getRegistry().commitTransaction();
    }

    @Override
    public void rollbackTransaction() throws RegistryException {
        getRegistry().rollbackTransaction();    
    }

    @Override
    public String addComment(String resourcePath, org.wso2.carbon.registry.api.Comment comment)
            throws org.wso2.carbon.registry.api.RegistryException {
        return getRegistry().addComment(resourcePath, comment);
    }

    @Override
    public String importResource(String suggestedPath, String sourceURL, 
                                 org.wso2.carbon.registry.api.Resource resource)
                                 throws org.wso2.carbon.registry.api.RegistryException {
        return getRegistry().importResource(suggestedPath, sourceURL, resource);
    }

    @Override
    public LogEntry[] getLogs(String resourcePath, int action, String userName,
                              Date from, Date to, boolean recentFirst) throws RegistryException {
        return getRegistry().getLogs(resourcePath, action, userName, from, to, recentFirst);
    }

    @Deprecated
    public LogEntryCollection getLogCollection(String resourcePath, int action,
                                               String userName, Date from, Date to, boolean recentFirst)
                                               throws RegistryException {
        return getRegistry().getLogCollection(resourcePath, action, userName, from, to, recentFirst);
    }

    @Override
    public void invokeAspect(String resourcePath, String aspectName,
                             String action, Map<String, String> parameters) throws RegistryException {
        getRegistry().invokeAspect(resourcePath, aspectName, action, parameters);
    }

    @Override
    public boolean removeAspect(String aspect) throws RegistryException {
        return getRegistry().removeAspect(aspect);
    }

    @Override
    public boolean addAspect(String name, Aspect aspect) throws RegistryException {
        return getRegistry().addAspect(name, aspect);
    }

    @Override
    public boolean removeVersionHistory(String path, long snapshotId) throws RegistryException {
        return getRegistry().removeVersionHistory(path, snapshotId);
    }

    @Override
    public RegistryContext getRegistryContext() {
        try {
            return registry.getRegistryContext();
        } catch (Exception e) {
            log.error("Error getting Registry", e);
        }
        return null;
    }

}