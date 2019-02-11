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

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.utils.VersionedPath;

/**
 * This is a built-in Filter implementation that evaluates against the media type of the resources.
 * This has to be initialized with a media type. For all filtering methods, this will evaluates to
 * true if the media type of the currently processing resource (or the parent of the current
 * resource in some cases) and the media type of this implementation are equal.
 * <p/>
 * Handler authors can use this as the filter, if the filtering requirement is only to match the
 * media type of the resource.
 */
public class MediaTypeMatcher extends Filter {

    /**
     * Media type to filter.
     */
    private String mediaType;

    /**
     * Default constructor.
     */
    @SuppressWarnings("unused")
    public MediaTypeMatcher() {
        this(null);
    }

    /**
     * Constructor that accepts a media type.
     *
     * @param mediaType the media type.
     */
    public MediaTypeMatcher(String mediaType) {
        this.mediaType = mediaType;
    }

    public int hashCode() {
        return getEqualsComparator().hashCode();
    }

    // Method to generate a unique string that can be used to compare two objects of the same type
    // for equality.
    private String getEqualsComparator() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("|");
        sb.append(mediaType);
        sb.append("|");
        sb.append(invert);
        return sb.toString();
    }

    /**
     * Compares this MediaTypeMatcher to the specified object.  The result is {@code true} if and
     * only if the argument is not {@code null} and is a {@code MediaTypeMatcher} object that
     * contains the same values for the fields as this object.
     *
     * @param other The object to compare the {@code MediaTypeMatcher} against
     *
     * @return {@code true} if the given object represents a {@code MediaTypeMatcher} equivalent to
     *         this instance, {@code false} otherwise.
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other == null) {
            return false;
        }
        if (other instanceof MediaTypeMatcher) {
            MediaTypeMatcher otherMediaTypeMatcher = (MediaTypeMatcher) other;
            return (getEqualsComparator().equals(otherMediaTypeMatcher.getEqualsComparator()));
        }
        return false;
    }

    /**
     * Matches if the media type of the current resource is equal to the handler's media type. If a
     * resource is not set in the RequestContext, this method will retrieve the resource for given
     * path from the database and set it in the RequestContext.
     * <p/>
     * Media type matcher supports special case for generating UIs for creating new resources. URLs
     * of the form <resource-path>;new:<view-name>;mediaType:<media-type> For this URLs, media type
     * matcher tries to match media type given in the URL, instead of trying to retrieve the
     * resource from the repository. Handler associated with such media types should support this
     * special case (e.g. Implementation of UIEnabledHandler).
     *
     * @param requestContext RequestContext for the current request
     *
     * @return true if the media type of the current resource is equal to the handler's media type.
     * @throws RegistryException
     */
    public boolean handleGet(RequestContext requestContext) throws RegistryException {

        // check if the request is for new resource
        ResourcePath resourcePath = requestContext.getResourcePath();
        if (resourcePath.parameterExists("new")) {
            String mediaType = resourcePath.getParameterValue("mediaType");
            return (mediaType != null && (invert != this.mediaType.equals(mediaType)));
        }

        Resource resource = requestContext.getResource();
        if (resource == null) {
            VersionedPath versionedPath =
                    RegistryUtils.getVersionedPath(requestContext.getResourcePath());

            if (versionedPath.getVersion() == -1) {
                resource = requestContext.getRepository().
                        get(requestContext.getResourcePath().getPath());
                requestContext.setResource(resource);
            }
        }

        if (resource != null) {
            String mType = resource.getMediaType();
            if (mType != null && (invert != mType.equals(mediaType))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Matches if the media type of the current resource is equal to the handler's media type.
     *
     * @param requestContext RequestContext for the current request
     *
     * @return true if the media type of the current resource is equal to the handler's media type.
     * @throws RegistryException
     */
    public boolean handlePut(RequestContext requestContext) throws RegistryException {

        Resource resource = requestContext.getResource();
        if (resource == null) {
            return false;
        }

        String mType = resource.getMediaType();
        return mType != null && (invert != mType.equals(mediaType));

    }

    /**
     * Matches if the media type of the current resource is equal to the handler's media type.
     *
     * @param requestContext RequestContext for the current request
     *
     * @return true if the media type of the current resource is equal to the handler's media type.
     * @throws RegistryException
     */
    public boolean handleImportResource(RequestContext requestContext) throws RegistryException {

        Resource resource = requestContext.getResource();
        if (resource == null) {
            return false;
        }

        String mType = resource.getMediaType();
        return mType != null && (invert != mType.equals(mediaType));

    }

    /**
     * Matches if the media type of the current resource is equal to the handler's media type.
     *
     * @param requestContext RequestContext for the current request
     *
     * @return true if the media type of the current resource is equal to the handler's media type.
     * @throws RegistryException
     */
    public boolean handleDelete(RequestContext requestContext) throws RegistryException {

        Resource resource = requestContext.getResource();
        if (resource == null) {
            resource =
                    requestContext.getRepository().get(requestContext.getResourcePath().getPath());
            requestContext.setResource(resource);
        }

        if (resource != null) {
            String mType = resource.getMediaType();
            if (mType != null && (invert != mType.equals(mediaType))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Matches if the media type of the current resource is equal to the handler's media type.
     *
     * @param requestContext RequestContext for the current request
     *
     * @return true if the media type of the current resource is equal to the handler's media type.
     * @throws RegistryException
     */
    public boolean handleRemoveLink(RequestContext requestContext) throws RegistryException {

        return handleDelete(requestContext);
    }

    /**
     * Matches if the media type of the current resource is equal to the handler's media type.
     *
     * @param requestContext RequestContext for the current request
     *
     * @return true if the media type of the current resource is equal to the handler's media type.
     * @throws RegistryException
     */
    public boolean handleCreateLink(RequestContext requestContext) throws RegistryException {

        return handleDelete(requestContext);
    }

    /**
     * Matches if the media type of the current resource is equal to the handler's media type.
     *
     * @param requestContext RequestContext for the current request
     *
     * @return true if the media type of the current resource is equal to the handler's media type.
     * @throws RegistryException
     */
    public boolean handleCopy(RequestContext requestContext) throws RegistryException {

        Resource resource =
                requestContext.getRepository().get(requestContext.getSourcePath());
        requestContext.setResource(resource);

        if (resource != null) {
            String mType = resource.getMediaType();
            if (mType != null && (invert != mType.equals(mediaType))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Matches if the media type of the current resource is equal to the handler's media type.
     *
     * @param requestContext RequestContext for the current request
     *
     * @return true if the media type of the current resource is equal to the handler's media type.
     * @throws RegistryException
     */
    public boolean handleMove(RequestContext requestContext) throws RegistryException {

        return handleCopy(requestContext);
    }

    /**
     * Matches if the media type of the current resource is equal to the handler's media type.
     *
     * @param requestContext RequestContext for the current request
     *
     * @return true if the media type of the current resource is equal to the handler's media type.
     * @throws RegistryException
     */
    public boolean handleRename(RequestContext requestContext) throws RegistryException {

        return handleCopy(requestContext);
    }

    /**
     * Matches if the media type of the current resource is equal to the handler's media type.
     *
     * @param requestContext RequestContext for the current request
     *
     * @return true if the media type of the current resource is equal to the handler's media type.
     * @throws RegistryException
     */
    public boolean handleInvokeAspect(RequestContext requestContext) throws RegistryException {

        Resource resource = requestContext.getResource();
        if (resource == null) {
            resource =
                    requestContext.getRepository().get(requestContext.getResourcePath().getPath());
            requestContext.setResource(resource);
        }

        if (resource != null) {
            String mType = resource.getMediaType();
            if (mType != null && (invert != mType.equals(mediaType))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Matches if the media type of the parent collection of the current resource is equal to the
     * handler's media type. If the parent collection is not set in the RequestContext, this method
     * will retrieve the parent collection of the current resource from the database and set it in
     * the RequestContext.
     *
     * @param requestContext RequestContext for the current request
     *
     * @return true if the media type of the parent collection of the current resource is equal to
     *         the handler's media type.
     * @throws RegistryException
     */
    public boolean handlePutChild(RequestContext requestContext) throws RegistryException {

        Collection parentCollection = requestContext.getParentCollection();
        if (parentCollection == null) {
            String parentPath = requestContext.getParentPath();
            if (parentPath == null) {
                parentPath = RegistryUtils.
                        getParentPath(requestContext.getResourcePath().getPath());
                requestContext.setParentPath(parentPath);
            }

            VersionedPath versionedPath =
                    RegistryUtils.getVersionedPath(requestContext.getResourcePath());

            if (versionedPath.getVersion() == -1) {
                Resource parentResource = requestContext.getRepository().get(parentPath);
                if (parentResource != null) {
                    if (parentResource instanceof Collection) {
                        parentCollection = (Collection) parentResource;
                        requestContext.setParentCollection(parentCollection);
                    } else {
                        // parent should be a collection, already exists a non-collection

                        String msg = "There already exist non collection resource." + parentPath +
                                "Child can only be added to collections";
                        throw new RegistryException(msg);
                    }
                }
            }
        }

        if (parentCollection != null) {
            String parentMediaType = parentCollection.getMediaType();
            if (parentMediaType != null && (invert != parentMediaType.equals(mediaType))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Matches if the media type of the parent collection of the current resource is equal to the
     * handler's media type. If the parent collection is not set in the RequestContext, this method
     * will retrieve the parent collection of the current resource from the database and set it in
     * the RequestContext.
     *
     * @param requestContext RequestContext for the current request
     *
     * @return true if the media type of the parent collection of the current resource is equal to
     *         the handler's media type.
     * @throws RegistryException
     */
    public boolean handleImportChild(RequestContext requestContext) throws RegistryException {

        Collection parentCollection = requestContext.getParentCollection();
        if (parentCollection == null) {
            String parentPath = requestContext.getParentPath();
            if (parentPath == null) {
                parentPath = RegistryUtils.
                        getParentPath(requestContext.getResourcePath().getPath());
                requestContext.setParentPath(parentPath);
            }

            VersionedPath versionedPath =
                    RegistryUtils.getVersionedPath(requestContext.getResourcePath());

            if (versionedPath.getVersion() == -1) {
                parentCollection = (Collection) requestContext.getRepository().get(parentPath);
                requestContext.setParentCollection(parentCollection);
            }
        }

        if (parentCollection != null) {
            String parentMediaType = parentCollection.getMediaType();
            if (parentMediaType != null && (invert != parentMediaType.equals(mediaType))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Method to obtain media type.
     *
     * @return the media type.
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Method to set media type.
     *
     * @param mediaType the media type.
     */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}
