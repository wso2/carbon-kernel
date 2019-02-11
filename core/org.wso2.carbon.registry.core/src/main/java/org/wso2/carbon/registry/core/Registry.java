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

package org.wso2.carbon.registry.core;

import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.dataaccess.TransactionManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.Map;

/**
 * This is the "Full" Registry interface.  It contains not only the get/put behavior from
 * CoreRegistry, but also APIs which control tags/comments/ratings/versions/etc.
 */
 public interface Registry extends org.wso2.carbon.registry.core.CoreRegistry,
                                  org.wso2.carbon.registry.api.Registry,
                                  TransactionManager {

    /**
     * Returns the meta data of the resource at a given path.
     *
     * @param path Path of the resource. e.g. /project1/server/deployment.xml
     *
     * @return Resource instance
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          is thrown if the resource is not in the registry
     */
     Resource getMetaData(String path) throws RegistryException;

    /**
     * Creates a resource by fetching the resource content from the given URL.
     *
     * @param suggestedPath path where we'd like to add the new resource. Although this path is
     *                      specified by the caller of the method, resource may not be actually
     *                      added at this path.
     * @param sourceURL     where to fetch the resource content
     * @param resource      a template Resource
     *
     * @return actual path to the new resource
     * @throws RegistryException if we couldn't get or store the new resource
     */
     String importResource(String suggestedPath,
                                 String sourceURL,
                                 Resource resource) throws RegistryException;

    /**
     * Rename a resource in the registry.  This is equivalent to 1) delete the resource, then 2) add
     * the resource by the new name.  The operation is atomic, so if it fails the old resource will
     * still be there.
     *
     * @param currentPath current path of the resource
     * @param newName     the name of the new resource
     *
     * @return the actual path for the new resource
     * @throws RegistryException if something went wrong
     */
     String rename(String currentPath, String newName) throws RegistryException;

    /**
     * Move a resource in the registry.  This is equivalent to 1) delete the resource, then 2) add
     * the resource to the new location.  The operation is atomic, so if it fails the old resource
     * will still be there.
     *
     * @param currentPath current path of the resource
     * @param newPath     where we'd like to move the resource
     *
     * @return the actual path for the new resource
     * @throws RegistryException if something went wrong
     */
     String move(String currentPath, String newPath) throws RegistryException;

    /**
     * Copy a resource in the registry.  The operation is atomic, so if the resource was a
     * collection, all children and the collection would be copied in a single-go.
     *
     * @param sourcePath current path of the resource
     * @param targetPath where we'd like to copy the resource
     *
     * @return the actual path for the new resource
     * @throws RegistryException if something went wrong
     */
     String copy(String sourcePath, String targetPath) throws RegistryException;

    /**
     * Creates a new version of the resource.
     *
     * @param path the resource path.
     *
     * @throws RegistryException if something went wrong.
     */
     void createVersion(String path) throws RegistryException;

    /**
     * Get a list of all versions of the resource located at the given path. Version paths are
     * returned in the form /projects/resource?v=12
     *
     * @param path path of a current version of a resource
     *
     * @return a String array containing the individual paths of versions
     * @throws RegistryException if there is an error
     */
     String[] getVersions(String path) throws RegistryException;

    /**
     * Reverts a resource to a given version.
     *
     * @param versionPath path of the version to be reverted. It is not necessary to provide the
     *                    path of the resource as it can be derived from the version path.
     *
     * @throws RegistryException if there is an error
     */
     void restoreVersion(String versionPath) throws RegistryException;

    ////////////////////////////////////////////////////////
    // Associations
    ////////////////////////////////////////////////////////

    /**
     * Adds an association stating that the resource at "associationPath" associate on the resource
     * at "associationPath". Paths may be the resource paths of the current versions or paths of the
     * old versions. If a path refers to the current version, it should contain the path in the form
     * /c1/c2/r1. If it refers to an old version, it should be in the form /c1/c2/r1?v=2.
     *
     * @param sourcePath      Path of the source resource
     * @param targetPath      Path of the target resource
     * @param associationType Type of the association
     *
     * @throws RegistryException Depends on the implementation
     */
     void addAssociation(String sourcePath,
                               String targetPath,
                               String associationType) throws RegistryException;

    /**
     * To remove an association for a given resource
     *
     * @param sourcePath      Path of the source resource
     * @param targetPath      Path of the target resource
     * @param associationType Type of the association
     *
     * @throws RegistryException Depends on the implementation
     */
     void removeAssociation(String sourcePath,
                                  String targetPath,
                                  String associationType) throws RegistryException;

    /**
     * Get all associations of the given resource. This is a chain of association starting from the
     * given resource both upwards (source to destination) and downwards (destination to source). T
     * his is useful to analyse how changes to other resources would affect the given resource.
     *
     * @param resourcePath Path of the resource to analyse associations.
     *
     * @return List of Association
     * @throws RegistryException If something went wrong
     */
     Association[] getAllAssociations(String resourcePath) throws RegistryException;

    /**
     * Get all associations of the given resource for a give association type. This is a chain of
     * association starting from the given resource both upwards (source to destination) and
     * downwards (destination to source). T his is useful to analyse how changes to other resources
     * would affect the given resource.
     *
     * @param resourcePath    Path of the resource to analyse associations.
     * @param associationType Type of the association , that could be dependency, or some other
     *                        type.
     *
     * @return List of Association
     * @throws RegistryException If something went wrong
     */
     Association[] getAssociations(String resourcePath, String associationType)
            throws RegistryException;

    ////////////////////////////////////////////////////////
    // Tagging
    ////////////////////////////////////////////////////////

    /**
     * Applies the given tag to the resource in the given path. If the given tag is not defined in
     * the registry, it will be defined.
     *
     * @param resourcePath Path of the resource to be tagged.
     * @param tag          Tag. Any string can be used for the tag.
     *
     * @throws RegistryException is thrown if a resource does not exist in the given path.
     */
     void applyTag(String resourcePath, String tag) throws RegistryException;

    /**
     * Returns the paths of all Resources that are tagged with the given tag.
     *
     * @param tag the tag to search for
     *
     * @return an array of TaggedResourcePaths
     * @throws RegistryException if an error occurs
     */
     TaggedResourcePath[] getResourcePathsWithTag(String tag) throws RegistryException;

    /**
     * Returns all tags used for tagging the given resource.
     *
     * @param resourcePath Path of the resource
     *
     * @return Tags tag names
     * @throws RegistryException is thrown if a resource does not exist in the given path.
     */
     Tag[] getTags(String resourcePath) throws RegistryException;

    /**
     * Removes a tag on a resource. If the resource at the path is owned by the current user, all
     * taggings done using the given tag will be removed. If the resource is not owned by the
     * current user, only the tagging done by the current user will be removed.
     *
     * @param path Resource path tagged with the given tag.
     * @param tag  Name of the tag to be removed.
     *
     * @throws RegistryException if there's a problem
     */
     void removeTag(String path, String tag) throws RegistryException;

    ////////////////////////////////////////////////////////
    // Comments
    ////////////////////////////////////////////////////////

    /**
     * Adds a comment to a resource.
     *
     * @param resourcePath Path of the resource to add the comment.
     * @param comment      Comment instance for the new comment.
     *
     * @return the path of the new comment.
     * @throws RegistryException is thrown if a resource does not exist in the given path.
     */
     String addComment(String resourcePath, Comment comment) throws RegistryException;

    /**
     * Change the text of an existing comment.
     *
     * @param commentPath path to comment resource ("..foo/r1;comment:1")
     * @param text        new text for the comment.
     *
     * @throws RegistryException Registry implementations may handle exceptions and throw
     *                           RegistryException if the exception has to be propagated to the
     *                           client.
     */
     void editComment(String commentPath, String text) throws RegistryException;

    /**
     * Delete an existing comment.
     *
     * @param commentPath path to comment resource ("..foo/r1;comment:1")
     *
     * @throws RegistryException Registry implementations may handle exceptions and throw
     *                           RegistryException if the exception has to be propagated to the
     *                           client.
     */
     void removeComment(String commentPath) throws RegistryException;

    /**
     * Get all comments for the given resource.
     *
     * @param resourcePath path of the resource.
     *
     * @return an array of Comment objects.
     * @throws RegistryException Registry implementations may handle exceptions and throw
     *                           RegistryException if the exception has to be propagated to the
     *                           client.
     */
     Comment[] getComments(String resourcePath) throws RegistryException;

    ////////////////////////////////////////////////////////
    // Ratings
    ////////////////////////////////////////////////////////

    /**
     * Rate the given resource.
     *
     * @param resourcePath Path of the resource.
     * @param rating       Rating value between 1 and 5.
     *
     * @throws RegistryException Registry implementations may handle exceptions and throw
     *                           RegistryException if the exception has to be propagated to the
     *                           client.
     */
     void rateResource(String resourcePath, int rating) throws RegistryException;

    /**
     * Returns the average rating for the given resource. This is the average of all ratings done by
     * all users for the given resource.
     *
     * @param resourcePath Path of the resource.
     *
     * @return Average rating between 1 and 5.
     * @throws RegistryException if an error occurs
     */
     float getAverageRating(String resourcePath) throws RegistryException;

    /**
     * Returns the rating given to the specified resource by the given user
     *
     * @param path     Path of the resource
     * @param userName username of the user
     *
     * @return rating given by the given user
     * @throws RegistryException if there is a problem
     */
     int getRating(String path, String userName) throws RegistryException;

    /**
     * Executes a custom query which lives at the given path in the Registry.
     *
     * @param path       Path of the query to execute.
     * @param parameters a Map of query parameters (name -> value)
     *
     * @return a Collection containing any resource paths which match the query
     * @throws RegistryException depends on the implementation.
     */
     Collection executeQuery(String path, Map parameters) throws RegistryException;

    /**
     * Returns the logs of the activities occurred in the registry.
     *
     * @param resourcePath If given, only the logs related to the resource path will be returned. If
     *                     null, logs for all resources will be returned.
     * @param action       Only the logs pertaining to this action will be returned.  For acceptable
     *                     values, see LogEntry.
     * @param userName     If given, only the logs for activities done by the given user will be
     *                     returned. If null, logs for all users will be returned.
     * @param from         If given, logs for activities occurred after the given date will be
     *                     returned. If null, there will not be a bound for the starting date.
     * @param to           If given, logs for activities occurred before the given date will be
     *                     returned. If null, there will not be a bound for the ending date.
     * @param recentFirst  If true, returned activities will be most-recent first. If false,
     *                     returned activities will be oldest first.
     *
     * @return Array of LogEntry objects representing the logs
     * @throws RegistryException if there is a problem
     * @see LogEntry Accepted values for action parameter
     */
     LogEntry[] getLogs(String resourcePath,
                              int action,
                              String userName,
                              Date from,
                              Date to,
                              boolean recentFirst) throws RegistryException;

    /**
     * Returns the logs of the activities occurred in the registry.
     *
     * @param resourcePath If given, only the logs related to the resource path will be returned. If
     *                     null, logs for all resources will be returned.
     * @param action       Only the logs pertaining to this action will be returned.  For acceptable
     *                     values, see LogEntry.
     * @param userName     If given, only the logs for activities done by the given user will be
     *                     returned. If null, logs for all users will be returned.
     * @param from         If given, logs for activities occurred after the given date will be
     *                     returned. If null, there will not be a bound for the starting date.
     * @param to           If given, logs for activities occurred before the given date will be
     *                     returned. If null, there will not be a bound for the ending date.
     * @param recentFirst  If true, returned activities will be most-recent first. If false,
     *                     returned activities will be oldest first.
     *
     * @return LogEntryCollection representing collection of log entries
     * @throws RegistryException if there is a problem
     * @see LogEntry Accepted values for action parameter
     *
     * @deprecated instead use {@link #getLogs(String, int, String, java.util.Date, java.util.Date, boolean)}}
     */
    @Deprecated
     LogEntryCollection getLogCollection(String resourcePath,
                                               int action,
                                               String userName,
                                               Date from,
                                               Date to,
                                               boolean recentFirst) throws RegistryException;

    /**
     * Get a list of the available Aspects for this Registry
     *
     * @return a String array containing available Aspect names
     */
     String[] getAvailableAspects();

    /**
     * Associate an Aspect with a resource.
     *
     * @param resourcePath Path of the resource
     * @param aspect       Name of the aspect
     *
     * @throws RegistryException If some thing went wrong while doing associating the phase
     */
     void associateAspect(String resourcePath, String aspect) throws RegistryException;

    /**
     * This invokes an action on a specified Aspect, which must be associated with the Resource at
     * the given path.
     *
     * @param resourcePath Path of the resource
     * @param aspectName   Name of the aspect
     * @param action       Which action was selected - actions are aspect-specific
     *
     * @throws RegistryException if the Aspect isn't associated with the Resource, or the action
     *                           isn't valid, or an Aspect-specific problem occurs.
     */
     void invokeAspect(String resourcePath, String aspectName, String action)
            throws RegistryException;

    /**
     * This invokes an action on a specified Aspect, which must be associated with the Resource at
     * the given path.
     *
     * @param resourcePath Path of the resource
     * @param aspectName   Name of the aspect
     * @param action       Which action was selected - actions are aspect-specific
     * @param parameters   Parameters to be used for the operation
     *
     * @throws RegistryException if the Aspect isn't associated with the Resource, or the action
     *                           isn't valid, or an Aspect-specific problem occurs.
     */
     void invokeAspect(String resourcePath, String aspectName, String action,
                             Map<String, String> parameters)
            throws RegistryException;

    /**
     * Obtain a list of the available actions on a given resource for a given Aspect.  The Aspect
     * must be associated with the Resource (@see associateAspect).  The actions are determined by
     * asking the Aspect itself, so they may change depending on the state of the Resource, the user
     * who's asking, etc)
     *
     * @param resourcePath path of the Resource
     * @param aspectName   name of the Aspect to query for available actions
     *
     * @return a String[] of action names
     * @throws RegistryException if the Aspect isn't associated or an Aspect-specific problem
     *                           occurs
     */
     String[] getAspectActions(String resourcePath, String aspectName)
            throws RegistryException;

    /**
     * Get the configuration for this Registry
     *
     * @return the currently active RegistryContext, or null
     */
     RegistryContext getRegistryContext();

    /**
     * Search the content of resources
     *
     * @param keywords keywords to look for
     *
     * @return the result set as a collection
     * @throws RegistryException throws if the operation fail
     */
     Collection searchContent(String keywords) throws RegistryException;

    /**
     * Create a symbolic link or mount a registry
     *
     * @param path   the mount path
     * @param target the point to be mounted
     *
     * @throws RegistryException throws if the operation fail
     */
     void createLink(String path, String target) throws RegistryException;

    /**
     * Create a symbolic link or mount a registry
     *
     * @param path          the mount path
     * @param target        the point to be mounted
     * @param subTargetPath sub path in the remote instance to be mounted
     *
     * @throws RegistryException throws if the operation fail
     */
     void createLink(String path, String target, String subTargetPath)
            throws RegistryException;

    /**
     * Remove a symbolic link or mount point created
     *
     * @param path the mount path
     *
     * @throws RegistryException throws if the operation fail
     */
     void removeLink(String path) throws RegistryException;

    /**
     * Check in the input axiom element into database.
     *
     * @param path   path to check in
     * @param reader reader containing resource
     *
     * @throws RegistryException throws if the operation fail
     */
     void restore(String path, Reader reader) throws RegistryException;

    /**
     * Check out the given path as an xml.
     *
     * @param path   path to check out
     * @param writer writer to write the response
     *
     * @throws RegistryException throws if the operation fail
     */
     void dump(String path, Writer writer) throws RegistryException;

    /**
     * Gets the URL of the WS-Eventing Service.
     *
     * @param path the path to which the WS-Eventing Service URL is required
     *
     * @return the URL of the WS-Eventing Service
     * @throws RegistryException throws if the operation fail
     */
     String getEventingServiceURL(String path) throws RegistryException;

    /**
     * Sets the URL of the WS-Eventing Service.
     *
     * @param path               the path to which the WS-Eventing Service URL is associated
     * @param eventingServiceURL the URL of the WS-Eventing Service
     *
     * @throws RegistryException throws if the operation fail
     */
     void setEventingServiceURL(String path, String eventingServiceURL)
            throws RegistryException;

    /**
     * Remove the given aspect from registry context.
     *
     * @param aspect the name of the aspect to be removed
     *
     * @return return true if the operation finished successful, false otherwise.
     * @throws RegistryException throws if the operation fail
     */
     boolean removeAspect(String aspect) throws RegistryException;

    /**
     * Add aspect by passing a name and the aspect object.
     *
     * @param name   the name of the aspect to be added
     * @param aspect the name of the aspect object to be added to registry context
     *
     * @return return true if the operation finished successful, false otherwise.
     * @throws RegistryException throws if the operation fail
     */
     boolean addAspect(String name, Aspect aspect) throws RegistryException;
    
    /**
     * Removes a given version history of a resource.
     *
     * @param version the version number of the resource
     * @param path the path of the resource 
     *
     * @return return true if the operation finished successful, false otherwise.
     * @throws RegistryException throws if the operation fails.
     */
     boolean removeVersionHistory(String path, long snapshotId) throws RegistryException;
     
     /**
      * Check out the given path as an xml. This operation will not checkout
      * comment, rating, tags, association of given resource.
      *
      * @param path   path to check out
      * @param writer writer to write the response
      *
      * @throws RegistryException throws if the operation fail
      */
      void dumpLite(String path, Writer writer) throws RegistryException;
}
