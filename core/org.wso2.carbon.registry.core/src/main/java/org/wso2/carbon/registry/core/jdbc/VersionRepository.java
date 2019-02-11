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
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.StaticConfiguration;
import org.wso2.carbon.registry.core.dao.*;
import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.dataobjects.ResourceDO;
import org.wso2.carbon.registry.core.jdbc.utils.VersionInputStream;
import org.wso2.carbon.registry.core.jdbc.utils.VersionRetriever;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.utils.VersionedPath;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for versioned resources.
 */
public class VersionRepository {

    private static Log log = LogFactory.getLog(VersionRepository.class);

    private DataAccessManager dataAccessManager;

    private ResourceDAO resourceDAO;
    private ResourceVersionDAO resourceVersionDAO;
    private CommentsDAO commentsDAO;
    private RatingsDAO ratingsDAO;
    private AssociationDAO associationDAO;
    private TagsDAO tagsDAO;

    /**
     * Constructor accepting data access manager.
     *
     * @param dataAccessManager the data access manager that is used for database communications.
     */
    public VersionRepository(DataAccessManager dataAccessManager) {
        this.dataAccessManager = dataAccessManager;
        this.resourceDAO = dataAccessManager.getDAOManager().getResourceDAO();
        this.resourceVersionDAO = dataAccessManager.getDAOManager().getResourceVersionDAO();
        this.commentsDAO = dataAccessManager.getDAOManager().getCommentsDAO(
                StaticConfiguration.isVersioningComments());
        this.ratingsDAO = dataAccessManager.getDAOManager().getRatingsDAO(
                StaticConfiguration.isVersioningRatings());
        this.associationDAO = dataAccessManager.getDAOManager().getAssociationDAO();
        this.tagsDAO = dataAccessManager.getDAOManager().getTagsDAO(
                StaticConfiguration.isVersioningTags());
    }

    /**
     * Method used to create a snapshot of a given resource.
     *
     * @param resource       the resource.
     * @param isRenewing     whether we are renewing.
     * @param keepProperties whether to preserve properties.
     *
     * @throws RegistryException if the operation failed.
     */
    public void createSnapshot(Resource resource,
                               boolean isRenewing,
                               boolean keepProperties) throws RegistryException {
        ResourceImpl resourceImpl = (ResourceImpl) resource;
        // archiving the old root resource
        createVersionForResource(resourceImpl, isRenewing, keepProperties);

        long version = resourceImpl.getVersionNumber();
        boolean isCollection = resourceImpl instanceof CollectionImpl;
        ResourceIDImpl rootResourceID = resourceImpl.getResourceIDImpl();
        ArrayList<Long> versionList = new ArrayList<Long>();
        versionList.add(version);
        if (isCollection) {
            // for collection we have to iterate through children
            addDescendants(rootResourceID, versionList, isRenewing, keepProperties);
        }
        // wrap the array list into stream
        InputStream versionsInputStream = new VersionInputStream(versionList);

        int pathId = rootResourceID.getPathID();
        String resourceName = rootResourceID.getName();
        long snapshotID =
                resourceVersionDAO.createSnapshot(pathId, resourceName, versionsInputStream);
        // Associations can be created only once we have created the snapshot, since we need to know
        // the snapshotID.
        if (snapshotID != -1) {
            VersionedPath versionedPath = new VersionedPath();
            versionedPath.setVersion(snapshotID);
            versionedPath.setPath(resourceImpl.getPath());
            associationDAO.copyAssociations(resourceImpl.getPath(), versionedPath.toString());
        }
    }

    /**
     * Method used to create a snapshot of a given resource.
     *
     * @param resourcePath   the resource path.
     * @param isRenewing     whether we are renewing.
     * @param keepProperties whether to preserve properties.
     *
     * @throws RegistryException if the operation failed.
     */
    public void createSnapshot(ResourcePath resourcePath,
                               boolean isRenewing,
                               boolean keepProperties) throws RegistryException {

        if (!resourcePath.isCurrentVersion()) {
            String msg = "Failed to create snapshot of the resource " + resourcePath +
                    ". Given path refers to an archived version of the resource.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        String path = resourcePath.getPath();
        path = RegistryUtils.getPureResourcePath(path);

        ResourceIDImpl resourceID = resourceDAO.getResourceID(path);
        if (resourceID == null) {
            String msg = "Failed to get resource id to create a snapshot to the resource " +
                    path + ". ";
            throw new RegistryException(msg);

        }
        ResourceImpl resource = resourceDAO.getResourceMetaData(resourceID);
        createSnapshot(resource, isRenewing, keepProperties);
    }

    /**
     * Here the versions of the descendants of the resource is added to the versionList array.
     * immediate descendants will be added to the list before others
     *
     * @param resourceID     the resource identifier
     * @param versionList    a list of versions.
     * @param isRenewing     whether we are renewing.
     * @param keepProperties whether to preserve properties.
     *
     * @throws RegistryException if the operation failed.
     */
    public void addDescendants(ResourceIDImpl resourceID,
                               ArrayList<Long> versionList,
                               boolean isRenewing,
                               boolean keepProperties) throws RegistryException {
        List<ResourceIDImpl> childIds = resourceDAO.getChildPathIds(resourceID);
        // immediate children will be added before others
        for (ResourceIDImpl childId : childIds) {
            ResourceImpl childResourceImpl = resourceDAO.getResourceWithNoUpdate(childId);
            long version = childResourceImpl.getVersionNumber();
            versionList.add(version);

            // we are archiving all the resources
            createVersionForResource(childResourceImpl, isRenewing, keepProperties);
        }
        // then next immediate levels will be added..
        for (ResourceIDImpl childId : childIds) {
            if (childId.isCollection()) {
                // add descendants recursively
                addDescendants(childId, versionList, isRenewing, keepProperties);
            }
        }
        // we have to explicitly copy the collections to the history table
        ResourceDO resourceDO = resourceDAO.getResourceDO(resourceID);
        if (resourceID.isCollection() &&
                !resourceVersionDAO.isResourceHistoryExist(resourceDO.getVersion())) {
            resourceVersionDAO.putResourceToHistory(resourceDO);
        }
    }

    // Method to create version for a resource.
    private void createVersionForResource(ResourceImpl resourceImpl,
                                          boolean isRenewing,
                                          boolean keepProperties) throws RegistryException {
        ResourceDO resourceDO = resourceImpl.getResourceDO();
        if (resourceDO.getVersion() <= 0) {
            // we need to fetch the resource from the database
            resourceDO = resourceDAO.getResourceDO(resourceImpl.getResourceIDImpl());
            isRenewing = false;
        }
        if (isRenewing) {
            // retrieve the old content and properties before versioning
            if (!(resourceImpl instanceof CollectionImpl)) {
                // we have to load the content
                resourceDAO.fillResourceContentWithNoUpdate(resourceImpl);
            }
            if (StaticConfiguration.isVersioningProperties() || !keepProperties) {
                // we need to load the properties as well
                resourceDAO.fillResourcePropertiesWithNoUpdate(resourceImpl);
            }
        }
        resourceVersionDAO.versionResource(resourceDO, keepProperties);
        if (isRenewing) {
            // we add a new copy to the resource table with a newer version
            ResourceImpl renewedResourceImpl = resourceImpl.getShallowCopy();
            if (!(renewedResourceImpl instanceof CollectionImpl)) {
                resourceDAO.addContent(renewedResourceImpl);
            }
            resourceDAO.addResourceWithNoUpdate(renewedResourceImpl);
            if (StaticConfiguration.isVersioningProperties() || !keepProperties) {
                // if the properties are not versioned, we can here safely assume
                // the properties are already there, so no need to add twice
                resourceDAO.addProperties(renewedResourceImpl);
            }

            commentsDAO.copyComments(resourceImpl, renewedResourceImpl);
            tagsDAO.copyTags(resourceImpl, renewedResourceImpl);
            ratingsDAO.copyRatings(resourceImpl, renewedResourceImpl);
        }
    }

    /**
     * Method to obtain a list of versioned paths. for a given path.
     *
     * @param resourcePath the resource path.
     *
     * @return array of version paths.
     * @throws RegistryException if the operation failed.
     */
    public String[] getVersions(String resourcePath) throws RegistryException {

        resourcePath = RegistryUtils.getPureResourcePath(resourcePath);

        Long[] snapshotNumbers = resourceVersionDAO.getSnapshotIDs(resourcePath);

        List<String> versionPaths = new ArrayList<String>();
        for (Long snapshotNumber : snapshotNumbers) {
            String versionPath = resourcePath + RegistryConstants.URL_SEPARATOR +
                    "version:" + snapshotNumber;
            versionPaths.add(versionPath);
        }

        return versionPaths.toArray(new String[versionPaths.size()]);
    }

    /**
     * Gets the meta data of resource referred by the given path.
     *
     * @param versionedPath Path of a versioned resource.
     *
     * @return Resource referred by the given path. Resource can be a file or a collection.
     * @throws RegistryException if the operation failed.
     */
    public Resource getMetaData(VersionedPath versionedPath) throws RegistryException {
        ResourceIDImpl resourceID = resourceDAO.getResourceID(versionedPath.getPath());

        if (!AuthorizationUtils.authorize(versionedPath.getPath(), ActionConstants.GET)) {
            String msg = "User " + CurrentSession.getUser() + " is not authorized to " +
                    "read the resource " + versionedPath + ".";
            log.warn(msg);
            throw new AuthorizationFailedException(msg);
        }

        long snapshotID = versionedPath.getVersion();

        ResourceImpl resourceImpl = resourceVersionDAO.get(resourceID, snapshotID);

        if (resourceImpl == null) {
            String msg = "Resource " + versionedPath.getPath() +
                    " does not have a version " + versionedPath.getVersion();
            log.error(msg);
            throw new RegistryException(msg);
        }
        resourceImpl.setDataAccessManager(dataAccessManager);

        resourceImpl.setPath(versionedPath.getPath());
        resourceImpl.setSnapshotID(snapshotID);
        resourceImpl.setMatchingSnapshotID(snapshotID);
        return resourceImpl;
    }

    /**
     * Checks if a pure resource exists in the given path.
     *
     * @param versionedPath Path of a versioned resource.
     *
     * @return true if a resource exists in the given path. false otherwise.
     * @throws RegistryException if the operation failed.
     */
    public boolean resourceExists(VersionedPath versionedPath) throws RegistryException {
        ResourceIDImpl resourceID = resourceDAO.getResourceID(versionedPath.getPath());

        long snapshotID = versionedPath.getVersion();
        return resourceVersionDAO.resourceExists(resourceID, snapshotID);
    }

    /**
     * Gets the pure resource referred by the given path.
     *
     * @param versionedPath Path of a versioned resource.
     *
     * @return Resource referred by the given path. Resource can be a file or a collection.
     * @throws RegistryException if the operation failed.
     */
    public Resource get(VersionedPath versionedPath) throws RegistryException {
        ResourceIDImpl resourceID = resourceDAO.getResourceID(versionedPath.getPath());

        if (!AuthorizationUtils.authorize(versionedPath.getPath(), ActionConstants.GET)) {
            String msg = "User " + CurrentSession.getUser() + " is not authorized to " +
                    "read the resource " + versionedPath + ".";
            log.warn(msg);
            throw new AuthorizationFailedException(msg);
        }

        long snapshotID = versionedPath.getVersion();

        ResourceImpl resourceImpl = resourceVersionDAO.get(resourceID, snapshotID);

        if (resourceImpl == null) {
            String msg = "Resource " + versionedPath.getPath() +
                    " does not have a version " + versionedPath.getVersion();
            log.error(msg);
            throw new RegistryException(msg);
        }

        int contentId = resourceImpl.getDbBasedContentID();
        if (contentId > 0) {
            resourceVersionDAO.fillResourceContentArchived(resourceImpl);
        }
        resourceDAO.fillResourcePropertiesWithNoUpdate(resourceImpl);

        resourceImpl.setDataAccessManager(dataAccessManager);
        resourceImpl.setUserName(CurrentSession.getUser());
        resourceImpl.setTenantId(CurrentSession.getTenantId());
        resourceImpl.setUserRealm(CurrentSession.getUserRealm());

        resourceImpl.setPath(versionedPath.getPath());
        resourceImpl.setSnapshotID(snapshotID);
        resourceImpl.setMatchingSnapshotID(snapshotID);

        return resourceImpl;
    }

    /**
     * Method to get a paged collection.
     *
     * @param versionedPath the collection path (which also contains the version).
     * @param start         the starting index.
     * @param pageLen       the page length.
     *
     * @return collection with resources on the given page.
     * @throws RegistryException if the operation failed.
     */
    public Collection get(VersionedPath versionedPath, int start, int pageLen)
            throws RegistryException {

        ResourceIDImpl resourceID = resourceDAO.getResourceID(versionedPath.getPath());

        long snapshotID = versionedPath.getVersion();

        CollectionImpl collectionImpl =
                resourceVersionDAO.get(resourceID, snapshotID, start, pageLen);

        if (collectionImpl == null) {
            String msg = "Resource " + versionedPath.getPath() +
                    " does not have a version " + versionedPath.getVersion();
            log.error(msg);
            throw new RegistryException(msg);
        }

        collectionImpl.setDataAccessManager(dataAccessManager);
        collectionImpl.setUserName(CurrentSession.getUser());
        collectionImpl.setTenantId(CurrentSession.getTenantId());
        collectionImpl.setUserRealm(CurrentSession.getUserRealm());

        collectionImpl.setPath(versionedPath.getPath());
        collectionImpl.setSnapshotID(snapshotID);
        collectionImpl.setMatchingSnapshotID(snapshotID);
        return collectionImpl;
    }

    /**
     * Method to restore a version.
     *
     * @param resourcePath the resource path (which also contains the version).
     *
     * @throws RegistryException if the operation failed.
     */
    public void restoreVersion(ResourcePath resourcePath) throws RegistryException {

        VersionedPath versionedPath = RegistryUtils.getVersionedPath(resourcePath);
        if (versionedPath.getVersion() == -1) {
            String msg = "Failed to restore resource. " +
                    versionedPath + " is not a valid version path.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        String versionedResourcePath = versionedPath.getPath();
        long snapshotID = versionedPath.getVersion();


        ResourceIDImpl resourceID = resourceDAO.getResourceID(versionedResourcePath);
        if (!AuthorizationUtils.authorize(versionedResourcePath, ActionConstants.PUT)) {
            String msg = "User " + CurrentSession.getUser() + " is not authorized to " +
                    "restore the resource " + versionedResourcePath + ".";
            log.warn(msg);
            throw new AuthorizationFailedException(msg);
        }

        restoreSnapshotNetwork(resourceID, snapshotID);
    }

    // Utility method to restore version-by-version using a version retriever.
    private void restoreSnapshotNetwork(ResourceIDImpl resourceID, long snapshotID)
            throws RegistryException {


        // the algorithm to restore snapshot now..
        // check if resource exist in the the snapshot
        // if no return failure
        // delete the current resource.
        // restore the versioned things
        //      get each resource from resource_history and copy to resource
        //      restore contents
        // That's all

        VersionRetriever versionRetriever =
                resourceVersionDAO.getVersionList(resourceID, snapshotID);

        if (versionRetriever == null) {
            String msg = "The snapshot " + snapshotID + " doesn't contain the " +
                    "resource " + resourceID.getPath();
            log.warn(msg);
            throw new AuthorizationFailedException(msg);
        }
        int versionIndex = 0;
        while (true) {
            long version = versionRetriever.getVersion(versionIndex);
            if (version == -1) {
                // no more stream
                break;
            }
            // restore resource and content
            resourceVersionDAO.restoreResources(version, snapshotID);
            versionIndex++;
        }
    }
    
    public boolean removeVersionHistory(String path, long snapshotId)
    		throws RegistryException {

        if (!AuthorizationUtils.authorize(path, ActionConstants.DELETE)) {
            String msg = "User " + CurrentSession.getUser() + " is not authorized to " +
                    "remove the version of the resource " + path + ".";
            log.warn(msg);
            throw new AuthorizationFailedException(msg);
        }

    	VersionRetriever versionRetriever =
                resourceVersionDAO.getVersionList(snapshotId);

        if (versionRetriever == null) {
            String msg = "The snapshot with the ID: " + snapshotId + " doesn't exists";
            log.warn(msg);
            throw new AuthorizationFailedException(msg);
        }
        
        int versionIndex = 0;
              
        long regVersion = versionRetriever.getVersion(versionIndex);                
        
        if(regVersion == -1) {
        	return false;     
        }
         	
        // Remove the tags / 
    	tagsDAO.removeVersionTags(regVersion);   	   	
        
        // Remove the comments /
        commentsDAO.removeVersionComments(regVersion);        
    	
        // Remove the ratings  /
        ratingsDAO.removeVersionRatings(regVersion);       
        
        // Remove the Property
        resourceVersionDAO.removePropertyValues(regVersion);        
        
        // Remove the snapshot    
        resourceVersionDAO.removeSnapshot(snapshotId);    	
    	
    	return true;    
  }
}
