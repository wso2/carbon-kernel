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

package org.wso2.carbon.registry.core.jdbc.handlers;

import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.TaggedResourcePath;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * Base class of all handler implementations. Provides the methods that handlers should implement.
 * This class also provides the data source, user realm, registry and repository instances to be
 * used by handler implementations.
 * <p/>
 * Handlers can be chained by providing necessary filter combinations. But in such cases, handler
 * authors should make sure that handlers in the chain do not perform conflicting operations. Unless
 * there is a critical requirement and handler authors are confident that handlers do not have
 * negative impact on each other, it is recommended that handlers are configured to execute only one
 * handler per request.
 * <p/>
 * Handler instances may be accessed concurrently by multiple threads. Therefore, handlers should be
 * thread safe. It is recommended that handlers are made stateless, instead of synchronizing them as
 * it could become a performance bottleneck in highly concurrent environments.
 * <p/>
 * Implementations of handlers should be optimized to take the minimum time for processing. As the
 * handlers are executed are always executed before executing the generic database layer code, time
 * consuming operations in handlers could slow down the whole registry.
 */
public abstract class Handler {

    /**
     * Processes the GET action for resource path of the requestContext.
     *
     * @param requestContext Information about the current request.
     *                       <p/>
     *                       requestContext.resourcePath: Path of the resource
     *                       <p/>
     *                       requestContext.resource: Resource at the given path. This can be null
     *                       if no other handler has retrieved that resource so far. If it contains
     *                       a value, matching handlers are free to do any change to the resource,
     *                       even they can replace the resource with completely new instance.
     *
     * @return Resource instance if the handler processed the GET action successfully.
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          If the media type handler is supposed to handle the get on the media type and if the
     *          get fails due a handler specific error
     */
    public Resource get(RequestContext requestContext) throws RegistryException {
        return null;
    }

    /**
     * Processes the PUT action. Actual path to which the resource is put may differ from the path
     * given in the requestContext.resourcePath. Therefore, after putting the resource, the actual
     * path to which the resource is put is set in the requestContext.actualPath.
     *
     * @param requestContext Information about the current request.
     *                       <p/>
     *                       requestContext.resourcePath: Path to put the resource.
     *                       requestContext.resource: Resource to put
     *
     * @throws RegistryException If the media type handler is supposed to handle the put on the
     *                           media type and if the put fails due a handler specific error
     */
    public void put(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Creates a resource in the given path by fetching the resource content from the given URL.
     *
     * @param requestContext Information about the current request.
     *                       <p/>
     *                       requestContext.resourcePath: Path to add the new resource.
     *                       <p/>
     *                       requestContext.sourceURL: URL to fetch the resource content
     *                       <p/>
     *                       requestContext.resource: Resource instance containing the meta data for
     *                       the resource to be imported. Once import is done, new resource is
     *                       created combining the meta data of this meta data object and the
     *                       imported content.
     *
     * @throws RegistryException If the media type handler is supposed to handle the import on the
     *                           media type and if the import fails due a handler specific error
     */
    public void importResource(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Move a resource in the registry.  This is equivalent to 1) delete the resource, then 2) add
     * the resource to the new location.  The operation is atomic, so if it fails the old resource
     * will still be there.
     *
     * @param requestContext Information about the current request.
     *                       <p/>
     *                       requestContext.sourcePath: Source/Current Path
     *                       <p/>
     *                       requestContext.targetPath: Destination/New Path
     *
     * @return the actual path for the new resource if the handler processed the MOVE action
     *         successfully.
     * @throws RegistryException if something went wrong
     */
    public String move(RequestContext requestContext) throws RegistryException {
        return null;
    }

    /**
     * Copy a resource in the registry.  This is equivalent to adding the resource to the new
     * location. The operation is atomic, so if it fails the resource won't be added.
     *
     * @param requestContext Information about the current request.
     *                       <p/>
     *                       requestContext.sourcePath: Source/Current Path
     *                       <p/>
     *                       requestContext.targetPath: Destination/New Path
     *
     * @return the actual path for the new resource if the handler processed the COPY action
     *         successfully.
     * @throws RegistryException if something went wrong
     */
    public String copy(RequestContext requestContext) throws RegistryException {
        return null;
    }

    /**
     * Rename a resource in the registry.  This is equivalent to 1) delete the resource, then 2) add
     * the resource to the new location.  The operation is atomic, so if it fails the old resource
     * will still be there.
     *
     * @param requestContext Information about the current request.
     *                       <p/>
     *                       requestContext.sourcePath: Source/Current Path
     *                       <p/>
     *                       requestContext.targetPath: Destination/New Path
     *
     * @return the actual path for the new resource if the handler processed the RENAME action
     *         successfully.
     * @throws RegistryException if something went wrong
     */
    public String rename(RequestContext requestContext) throws RegistryException {
        return null;
    }

    /**
     * Create a symbolic link or mount a registry.
     *
     * @param requestContext Information about the current request.
     *                       <p/>
     *                       requestContext.targetPath: Destination/New Path
     *
     * @throws RegistryException if something went wrong
     */
    public void createLink(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Remove a symbolic link or un-mount a registry.
     *
     * @param requestContext Information about the current request.
     *
     * @throws RegistryException if something went wrong
     */
    public void removeLink(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Processes the DELETE action of the media type.
     *
     * @param requestContext Information about the current request.
     *                       <p/>
     *                       requestContext.resourcePath: path of the resource to be deleted.
     *
     * @throws RegistryException If the media type handler is supposed to handle the delete on the
     *                           media type and if the delete fails due a handler specific error
     */
    public void delete(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Invokes when a child resource is added. Only the media type handlers of collection resources
     * may have a meaningful implementation of this method.
     *
     * @param requestContext requestContext.resourcePath: path of the parent collection
     *                       requestContext.resource: New child resource to be added
     *
     * @throws RegistryException If the media type handler is supposed to handle the putChild on the
     *                           media type and if the putChild fails due a handler specific error
     */
    @SuppressWarnings("unused")
    public void putChild(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Invokes when a child resource is imported. Only the media type handlers of collection
     * resources may have a meaningful implementation of this method.
     *
     * @param requestContext requestContext.resourcePath
     *
     * @throws RegistryException If the media type handler is supposed to handle the importChild on
     *                           the media type and if the importChild fails due a handler specific
     *                           error
     */
    public void importChild(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Gets called when an Associated Aspect gets invoked.
     *
     * @param requestContext requestContext.resourcePath: path of the resource.
     *                       requestContext.aspect: The Aspect to be invoked requestContext.action:
     *                       The action to be provided when invoking the Aspect
     *
     * @throws RegistryException If the media type handler is supposed to handle the invokeAspect on
     *                           the media type and if the invokeAspect fails due a handler specific
     *                           error
     */
    public void invokeAspect(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Gets called when an Association is added.
     *
     * @param requestContext Information about the current request. requestContext.sourcePath:
     *                       Source/Current Path requestContext.targetPath: Destination/New Path
     *                       requestContext.associationType: Type of Association
     *
     * @throws RegistryException If the media type handler is supposed to handle the addAssociation
     *                           on the media type and if the addAssociation fails due a handler
     *                           specific error
     */
    public void addAssociation(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Gets called when an Association is removed.
     *
     * @param requestContext Information about the current request. requestContext.sourcePath:
     *                       Source/Current Path requestContext.targetPath: Destination/New Path
     *                       requestContext.associationType: Type of Association
     *
     * @throws RegistryException If the media type handler is supposed to handle the
     *                           removeAssociation on the media type and if the removeAssociation
     *                           fails due a handler specific error
     */
    public void removeAssociation(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Gets called when getting all Associations.
     *
     * @param requestContext Information about the current request. requestContext.resourcePath:
     *                       Path of Resource
     *
     * @return Array of all associations if the handler processed the GET_ALL_ASSOCIATIONS action
     *         successfully.
     * @throws RegistryException If the media type handler is supposed to handle the
     *                           getAllAssociations on the media type and if the getAllAssociations
     *                           fails due a handler specific error
     */
    public Association[] getAllAssociations(RequestContext requestContext)
            throws RegistryException {
        return null;
    }

    /**
     * Gets called when getting Associations of given type.
     *
     * @param requestContext Information about the current request. requestContext.resourcePath:
     *                       Path of Resource requestContext.associationType: Type of Association
     *
     * @return Array of associations of given type if the handler processed the GET_ASSOCIATIONS
     *         action successfully.
     * @throws RegistryException If the media type handler is supposed to handle the getAssociations
     *                           on the media type and if the getAssociations fails due a handler
     *                           specific error
     */
    public Association[] getAssociations(RequestContext requestContext) throws RegistryException {
        return null;
    }

    /**
     * Gets called when a tag is applied.
     *
     * @param requestContext Information about the current request. requestContext.resourcePath:
     *                       Path of Resource requestContext.tag: Tag
     *
     * @throws RegistryException If the media type handler is supposed to handle the applyTag on the
     *                           media type and if the applyTag fails due a handler specific error
     */
    public void applyTag(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Gets called when a tag is removed.
     *
     * @param requestContext Information about the current request. requestContext.resourcePath:
     *                       Path of Resource requestContext.tag: Tag
     *
     * @throws RegistryException If the media type handler is supposed to handle the removeTag on
     *                           the media type and if the removeTag fails due a handler specific
     *                           error
     */
    public void removeTag(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Gets called when rating a resource.
     *
     * @param requestContext Information about the current request. requestContext.resourcePath:
     *                       Path of Resource requestContext.rating: Rating
     *
     * @throws RegistryException If the media type handler is supposed to handle the rateResource on
     *                           the media type and if the rateResource fails due a handler specific
     *                           error
     */
    public void rateResource(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Gets called when restoring a version.
     *
     * @param requestContext Information about the current request. requestContext.versionPath: Path
     *                       of Resource with version This can be used to derive the path of the
     *                       resource as well.
     *
     * @throws RegistryException If the media type handler is supposed to handle the restoreVersion
     *                           on the media type and if the restoreVersion fails due a handler
     *                           specific error
     */
    public void restoreVersion(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Gets called when creating a version.
     *
     * @param requestContext Information about the current request. requestContext.resourcePath:
     *                       Path of Resource
     *
     * @throws RegistryException If the media type handler is supposed to handle the createVersion
     *                           on the media type and if the createVersion fails due a handler
     *                           specific error
     */
    public void createVersion(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Gets called when editing a comment.
     *
     * @param requestContext Information about the current request. requestContext.comment: The
     *                       comment with associated modifications.
     *
     * @throws RegistryException If the media type handler is supposed to handle the editComment on
     *                           the media type and if the editComment fails due a handler specific
     *                           error
     */
    public void editComment(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Gets called when adding a comment.
     *
     * @param requestContext Information about the current request. requestContext.resourcePath:
     *                       Path of Resource requestContext.comment: The comment to add
     *
     * @return The comment id of the comment added if the handler processed the ADD_COMMENT action
     *         successfully.
     * @throws RegistryException If the media type handler is supposed to handle the addComment on
     *                           the media type and if the addComment fails due a handler specific
     *                           error
     */
    public String addComment(RequestContext requestContext) throws RegistryException {
        return null;
    }

    /**
     * Gets called when removing a comment.
     *
     * @param requestContext Information about the current request. requestContext.resourcePath:
     *                       Path of Resource requestContext.comment: The comment to remove
     *
     * @throws RegistryException If the media type handler is supposed to handle the addComment on
     *                           the media type and if the addComment fails due a handler specific
     *                           error
     */
    public void removeComment(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Gets called when retrieving comments.
     *
     * @param requestContext Information about the current request. requestContext.resourcePath:
     *                       Path of Resource
     *
     * @return Array of comments if the handler processed the REMOVE_COMMENT action successfully.
     * @throws RegistryException If the media type handler is supposed to handle the getComments on
     *                           the media type and if the getComments fails due a handler specific
     *                           error
     */
    public Comment[] getComments(RequestContext requestContext) throws RegistryException {
        return null;
    }

    /**
     * Gets called when getting average rating.
     *
     * @param requestContext Information about the current request. requestContext.resourcePath:
     *                       Path of Resource
     *
     * @return The average rating if the handler processed the GET_AVERAGE_RATING action
     *         successfully.
     * @throws RegistryException If the media type handler is supposed to handle the
     *                           getAverageRating on the media type and if the getAverageRating
     *                           fails due a handler specific error
     */
    public float getAverageRating(RequestContext requestContext) throws RegistryException {
        return -1;
    }

    /**
     * Gets called when getting a rating given by a specific user.
     *
     * @param requestContext Information about the current request. requestContext.resourcePath:
     *                       Path of Resource requestContext.userName: The name of the user
     *
     * @return The rating given by the user if the handler processed the GET_RATING action
     *         successfully.
     * @throws RegistryException If the media type handler is supposed to handle the getRating on
     *                           the media type and if the getRating fails due a handler specific
     *                           error
     */
    public int getRating(RequestContext requestContext) throws RegistryException {
        return -1;
    }

    /**
     * Gets called when getting versions.
     *
     * @param requestContext Information about the current request. requestContext.resourcePath:
     *                       Path of Resource
     *
     * @return an array of Version paths are returned in the form /projects/resource?v=12 if the
     *         handler processed the GET_VERSIONS action successfully.
     * @throws RegistryException If the media type handler is supposed to handle the getVersions on
     *                           the media type and if the getVersions fails due a handler specific
     *                           error
     */
    public String[] getVersions(RequestContext requestContext) throws RegistryException {
        return null;
    }

    /**
     * Gets called when getting tags.
     *
     * @param requestContext Information about the current request. requestContext.resourcePath:
     *                       Path of Resource
     *
     * @return Array of tags added to the given resource if the handler processed the GET_TAGS
     *         action successfully.
     * @throws RegistryException If the media type handler is supposed to handle the getTags on the
     *                           media type and if the getTags fails due a handler specific error
     */
    public Tag[] getTags(RequestContext requestContext) throws RegistryException {
        return null;
    }

    /**
     * Gets called when getting the resource paths corresponding to the given tag.
     *
     * @param requestContext Information about the current request. requestContext.tag: Tag
     *
     * @return The resource paths tagged with the given tag if the handler processed the
     *         GET_TAGGED_RESOURCE_PATHS action successfully.
     * @throws RegistryException If the media type handler is supposed to handle the
     *                           getResourcePathsWithTag on the media type and if the
     *                           getResourcePathsWithTag fails due a handler specific error
     */
    public TaggedResourcePath[] getResourcePathsWithTag(RequestContext requestContext)
            throws RegistryException {
        return null;
    }

    /**
     * Gets called when executing Queries.
     *
     * @param requestContext Information about the current request. requestContext.resourcePath:
     *                       Path of Resource requestContext.queryParameters: Map of query
     *                       parameters.
     *
     * @return A collection containing results as its child resources if the handler processed the
     *         EXECUTE_QUERY action successfully.
     * @throws RegistryException If the media type handler is supposed to handle the executeQuery on
     *                           the media type and if the executeQuery fails due a handler specific
     *                           error
     */
    public Collection executeQuery(RequestContext requestContext) throws RegistryException {
        return null;
    }

    /**
     * Gets called when searching for content.
     *
     * @param requestContext Information about the current request. requestContext.keywords: Search
     *                       keywords.
     *
     * @return The result set as a collection if the handler processed the SEARCH_CONTENT action
     *         successfully.
     * @throws RegistryException If the media type handler is supposed to handle the searchContent
     *                           on the media type and if the searchContent fails due a handler
     *                           specific error
     */
    public Collection searchContent(RequestContext requestContext) throws RegistryException {
        return null;
    }

    /**
     * Gets called when searching for existence of resource.
     *
     * @param requestContext Information about the current request.
     *
     * @return True if the resource exists and false if not  if the handler processed the
     *         RESOURCE_EXISTS action successfully.
     * @throws RegistryException If the media type handler is supposed to handle the resourceExists
     *                           on the media type and if the resourceExists fails due a handler
     *                           specific error
     */
    public boolean resourceExists(RequestContext requestContext) throws RegistryException {
        return false;
    }

    /**
     * Gets called when obtaining the registry context.
     *
     * @param requestContext Information about the current request.
     *
     * @return An instance of the corresponding registry context.
     */
    public RegistryContext getRegistryContext(RequestContext requestContext) {
        return null;
    }

    /**
     * Gets called when dumping an path
     *
     * @param requestContext Information about the current request. requestContext.keywords: Search
     *                       keywords.
     *
     * @throws RegistryException If the media type handler is supposed to handle the resourceExists
     *                           on the media type and if the resourceExists fails due a handler
     *                           specific error
     */
    public void dump(RequestContext requestContext) throws RegistryException {
    }

    /**
     * Gets called when restoring a path
     *
     * @param requestContext Information about the current request. requestContext.keywords: Search
     *                       keywords.
     *
     * @throws RegistryException If the media type handler is supposed to handle the resourceExists
     *                           on the media type and if the resourceExists fails due a handler
     *                           specific error
     */
    public void restore(RequestContext requestContext) throws RegistryException {
    }

    /**
     * This overrides the default hash code implementation for handler objects, to make sure that
     * each handler of the same type will have identical hash codes unless otherwise it has its own
     * extension.
     *
     * @return hash code for this handler type.
     */
    public int hashCode() {
        // As per contract for hashCode, If two objects are equal according to the equals(Object)
        // method, then calling the hashCode method on each of the two objects must produce the same
        // integer result. Therefore, two Handler objects having the same class name will have
        // identical hash codes.
        return getClass().getName().hashCode();
    }

    /**
     * Revised implementation of the equals comparison to suite the modified hashCode method.
     *
     * @param obj object to compare for equality.
     *
     * @return whether equal or not.
     */
    public boolean equals(Object obj) {
        return (obj != null && obj instanceof Handler && obj.getClass().getName().equals(getClass().getName()));
    }
    
    /**
     * Gets called when dumping an path from registry depsync
     *
     * @param requestContext Information about the current request. requestContext.keywords: Search
     *                       keywords.
     *
     * @throws RegistryException If the media type handler is supposed to handle the resourceExists
     *                           on the media type and if the resourceExists fails due a handler
     *                           specific error
     */
    protected void dumpLite(RequestContext requestContext) throws RegistryException {
    }
}
