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

package org.wso2.carbon.registry.core.jdbc.handlers.filters;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

/**
 * Base class of all filter implementations. All handlers have to be registered in the JDBC registry
 * with a filter implementation. Filter implementations determine the conditions to invoke the
 * associating handler.
 */
@SuppressWarnings("unused")
public abstract class Filter {

    ////////////////////////////////////////////////////////
    // Supported method names
    ////////////////////////////////////////////////////////

    /**
     * Represents a get operation on the registry.
     */
    public static final String GET = "GET";
    /**
     * Represents a put operation on the registry.
     */
    public static final String PUT = "PUT";
    /**
     * Represents a delete operation on the registry.
     */
    public static final String DELETE = "DELETE";
    /**
     * Represents an import operation on the registry.
     */
    public static final String IMPORT = "IMPORT";
    /**
     * Represents a putChild operation on the registry.
     */
    public static final String PUT_CHILD = "PUT_CHILD";
    /**
     * Represents an importChild operation on the registry.
     */
    public static final String IMPORT_CHILD = "IMPORT_CHILD";
    /**
     * Represents an invokeAspect operation on the registry.
     */
    public static final String INVOKE_ASPECT = "INVOKE_ASPECT";
    /**
     * Represents a move operation on the registry.
     */
    public static final String MOVE = "MOVE";
    /**
     * Represents a copy operation on the registry.
     */
    public static final String COPY = "COPY";
    /**
     * Represents a rename operation on the registry.
     */
    public static final String RENAME = "RENAME";
    /**
     * Represents a createLink operation on the registry.
     */
    public static final String CREATE_LINK = "CREATE_LINK";
    /**
     * Represents a removeLink operation on the registry.
     */
    public static final String REMOVE_LINK = "REMOVE_LINK";
    /**
     * Represents an addAssociation operation on the registry.
     */
    public static final String ADD_ASSOCIATION = "ADD_ASSOCIATION";
    /**
     * Represents a removeAssociation operation on the registry.
     */
    public static final String REMOVE_ASSOCIATION = "REMOVE_ASSOCIATION";
    /**
     * Represents a getAssociations operation on the registry.
     */
    public static final String GET_ASSOCIATIONS = "GET_ASSOCIATIONS";
    /**
     * Represents a getAllAssociations operation on the registry.
     */
    public static final String GET_ALL_ASSOCIATIONS = "GET_ALL_ASSOCIATIONS";
    /**
     * Represents an applyTag operation on the registry.
     */
    public static final String APPLY_TAG = "APPLY_TAG";
    /**
     * Represents a getResourcePathsWithTag operation on the registry.
     */
    public static final String GET_RESOURCE_PATHS_WITH_TAG = "GET_RESOURCE_PATHS_WITH_TAG";
    /**
     * Represents a getTags operation on the registry.
     */
    public static final String GET_TAGS = "GET_TAGS";
    /**
     * Represents a removeTag operation on the registry.
     */
    public static final String REMOVE_TAG = "REMOVE_TAG";
    /**
     * Represents an addComment operation on the registry.
     */
    public static final String ADD_COMMENT = "ADD_COMMENT";
    /**
     * Represents an editComment operation on the registry.
     */
    public static final String EDIT_COMMENT = "EDIT_COMMENT";
    /**
     * Represents a removeComment operation on the registry.
     */
    public static final String REMOVE_COMMENT = "REMOVE_COMMENT";
    /**
     * Represents a getComments operation on the registry.
     */
    public static final String GET_COMMENTS = "GET_COMMENTS";
    /**
     * Represents a rateResource operation on the registry.
     */
    public static final String RATE_RESOURCE = "RATE_RESOURCE";
    /**
     * Represents a getAverageRating operation on the registry.
     */
    public static final String GET_AVERAGE_RATING = "GET_AVERAGE_RATING";
    /**
     * Represents a getRating operation on the registry.
     */
    public static final String GET_RATING = "GET_RATING";
    /**
     * Represents a createVersions operation on the registry.
     */
    public static final String CREATE_VERSION = "CREATE_VERSION";
    /**
     * Represents a getVersions operation on the registry.
     */
    public static final String GET_VERSIONS = "GET_VERSIONS";
    /**
     * Represents a restoreVersion operation on the registry.
     */
    public static final String RESTORE_VERSION = "RESTORE_VERSION";
    /**
     * Represents an executeQuery operation on the registry.
     */
    public static final String EXECUTE_QUERY = "EXECUTE_QUERY";
    /**
     * Represents a searchContent operation on the registry.
     */
    public static final String SEARCH_CONTENT = "SEARCH_CONTENT";
    /**
     * Represents a resourceExists operation on the registry.
     */
    public static final String RESOURCE_EXISTS = "RESOURCE_EXISTS";
    /**
     * Represents a getRegistryContext operation on the registry.
     */
    public static final String GET_REGISTRY_CONTEXT = "GET_REGISTRY_CONTEXT";
    /**
     * Represents a dump operation on the registry.
     */
    public static final String DUMP = "DUMP";
    /**
     * Represents a restore operation on the registry.
     */
    public static final String RESTORE = "RESTORE";

    /**
     * Whether to invert the result of the evaluated filter condition or not.
     */
    protected boolean invert = false;

    /**
     * Determines whether the associating handler should handle the get action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public abstract boolean handleGet(RequestContext requestContext) throws RegistryException;

    /**
     * Determines whether the associating handler should handle the put action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public abstract boolean handlePut(RequestContext requestContext) throws RegistryException;

    /**
     * Determines whether the associating handler should handle the import resource action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public abstract boolean handleImportResource(RequestContext requestContext)
            throws RegistryException;

    /**
     * Determines whether the associating handler should handle the delete action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public abstract boolean handleDelete(RequestContext requestContext) throws RegistryException;

    /**
     * Determines whether the associating handler should handle the putChild action. putChild action
     * occurs when it is attempted to put a resource as a child of the resource referred by
     * requestContext.parentPath or to the resource requestContext.parentCollection.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public abstract boolean handlePutChild(RequestContext requestContext) throws RegistryException;

    /**
     * Determines whether the associating handler should handle the importChild action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public abstract boolean handleImportChild(RequestContext requestContext)
            throws RegistryException;

    /**
     * Determines whether the associating handler should handle the invokeAspect action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleInvokeAspect(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the move action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleMove(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the copy action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleCopy(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the rename action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleRename(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the createLink action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleCreateLink(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the removeLink action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleRemoveLink(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the addAssociation action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleAddAssociation(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the removeAssociation action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleRemoveAssociation(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the getAllAssociations action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleGetAllAssociations(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the getAssociations action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleGetAssociations(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the applyTag action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleApplyTag(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the removeTag action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleRemoveTag(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the rateResource action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleRateResource(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the restoreVersion action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleRestoreVersion(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the createVersion action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleCreateVersion(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the editComment action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleEditComment(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the addComment action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleAddComment(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the removeComment action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleRemoveComment(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the getComments action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleGetComments(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the getResourcePathsWithTag action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleGetResourcePathsWithTag(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the getTags action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleGetTags(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the getAverageRating action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleGetAverageRating(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the getRating action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleGetRating(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the getVersions action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleGetVersions(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the executeQuery action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleExecuteQuery(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the searchContent action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleSearchContent(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the resourceExists action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleResourceExists(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the getRegistryContext action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     */
    public boolean handleGetRegistryContext(RequestContext requestContext) {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the dump action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleDump(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Determines whether the associating handler should handle the restore action.
     *
     * @param requestContext Information about the current requestContext.
     *
     * @return true if the associating handler should handle the request. false otherwise.
     * @throws RegistryException Filter implementations should deal with the specific exceptions and
     *                           throw a RegistryException if the exception has to be propagated to
     *                           the surface.
     */
    public boolean handleRestore(RequestContext requestContext)
            throws RegistryException {
        return false;
    }

    /**
     * Method to get inversion status.
     *
     * @return inversion status.
     */
    public String getInvert() {
        return Boolean.toString(invert);
    }

    /**
     * Method to set inversion status.
     *
     * @param invertStr inversion status string.
     */
    public void setInvert(String invertStr) {
        this.invert = invertStr.equalsIgnoreCase(Boolean.TRUE.toString());
    }

    /**
     * This overrides the default hash code implementation for filter objects, to make sure that
     * each filter of the same type will have identical hash codes unless otherwise it has its own
     * extension.
     *
     * @return hash code for this filter type.
     */
    public int hashCode() {
        // As per contract for hashCode, If two objects are equal according to the equals(Object)
        // method, then calling the hashCode method on each of the two objects must produce the same
        // integer result. Therefore, two Filter objects having the same class name will have
        // identical hash codes if they are both inverting or non-inverting.
        return (getClass().getName() + invert).hashCode();
    }

    /**
     * Revised implementation of the equals comparison to suite the modified hashCode method.
     *
     * @param obj object to compare for equality.
     *
     * @return whether equal or not.
     */
    public boolean equals(Object obj) {
        return (obj != null && obj instanceof Filter && obj.getClass().equals(getClass()) &&
                ((Filter) obj).invert == this.invert);
    }
}
