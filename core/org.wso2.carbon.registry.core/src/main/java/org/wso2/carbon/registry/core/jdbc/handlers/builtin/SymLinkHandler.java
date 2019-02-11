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

package org.wso2.carbon.registry.core.jdbc.handlers.builtin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerLifecycleManager;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerManager;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * This handler is used to create a symbolic link from one resource to another and perform registry
 * operations on the symbolic link and have them applied on the actual resource as if the actual
 * resource itself was being used. The symbolic link handler plays a key role to make it possible to
 * create and work with symbolic links via the user interface.
 */
public class SymLinkHandler extends Handler {

    private static final Log log = LogFactory.getLog(ResourceImpl.class);

    // sym-links map to resolve cyclic symlink
    private static Set<SymLinkHandler> symLinkHandlers = new HashSet<SymLinkHandler>();

    private String mountPoint;
    private String targetPoint;
    private String author;

    private boolean isHandlerRegistered = false;

    public int hashCode() {
        return getEqualsComparator().hashCode();
    }

    public boolean equals(Object obj) {
        return (obj != null && obj instanceof SymLinkHandler &&
                ((SymLinkHandler) obj).getEqualsComparator().equals(getEqualsComparator()));
    }


    // Method to generate a unique string that can be used to compare two objects of the same type
    // for equality.
    private String getEqualsComparator() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("|");
        sb.append(mountPoint);
        sb.append("|");
        sb.append(targetPoint);
        sb.append("|");
        sb.append(author);
        return sb.toString();
    }

    public void put(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getResourcePath().getPath();
        String actualPath = getActualPath(fullPath);
        try {
            Resource resource = requestContext.getResource();

            // removes the following properties if they are present
            resource.removeProperty(RegistryConstants.REGISTRY_MOUNT_POINT);
            resource.removeProperty(RegistryConstants.REGISTRY_TARGET_POINT);
            resource.removeProperty(RegistryConstants.REGISTRY_AUTHOR);
            resource.removeProperty(RegistryConstants.REGISTRY_LINK);
            resource.removeProperty(RegistryConstants.REGISTRY_ACTUAL_PATH);

            // sets the actual path of the resource
            requestContext.getRegistry().put(actualPath, requestContext.getResource());
            requestContext.setProcessingComplete(true);
        } catch (Exception e) {
            throw new RegistryException(e.getMessage());
        }
    }

    public boolean resourceExists(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        boolean resourceExists = false;
        String fullPath = requestContext.getResourcePath().getPath();
        String subPath = fullPath.substring(this.mountPoint.length(), fullPath.length());
        String actualPath = this.targetPoint + subPath;
        try {
            resourceExists = requestContext.getRegistry().resourceExists(actualPath);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Could not create the symbolic link. Target " + actualPath + "not found.");
            }
            log.debug("Caused by: ", e);
        }
        requestContext.setProcessingComplete(true);
        return resourceExists;
    }

    public Resource get(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getResourcePath().getPath();
        String subPath = fullPath.substring(this.mountPoint.length(), fullPath.length());
        String actualPath = this.targetPoint + subPath;
        Resource tempResource;
        if (requestContext.getRegistry().resourceExists(actualPath)) {
            tempResource = requestContext.getRegistry().get(actualPath);
            if (tempResource instanceof Collection) {
                String[] paths = (String[]) tempResource.getContent();
                for (int i = 0; i < paths.length; i++) {
                    if (this.targetPoint.equals(RegistryConstants.PATH_SEPARATOR)) {
                        paths[i] = this.mountPoint + paths[i];
                    } else {
                        paths[i] = this.mountPoint +
                                paths[i].substring(this.targetPoint.length(), paths[i].length());
                    }
                }
                ((CollectionImpl) tempResource).setContentWithNoUpdate(paths);
            }
            ((ResourceImpl) tempResource).setPath(fullPath);
            ((ResourceImpl) tempResource).setAuthorUserName(author);
            ((ResourceImpl) tempResource).setUserName(CurrentSession.getUser());
            ((ResourceImpl) tempResource).setTenantId(CurrentSession.getTenantId());
        } else {
            tempResource = requestContext.getRepository().get(this.mountPoint);
            ((ResourceImpl) tempResource).addPropertyWithNoUpdate("registry.absent", "true");
            tempResource.setDescription(
                    "Couldn't create the symbolic link. Content can't be displayed.");
        }
        ((ResourceImpl) tempResource).addPropertyWithNoUpdate(RegistryConstants.REGISTRY_LINK, "true");
        ((ResourceImpl) tempResource).removePropertyWithNoUpdate(RegistryConstants.REGISTRY_NON_RECURSIVE);
        ((ResourceImpl) tempResource).removePropertyWithNoUpdate(RegistryConstants.REGISTRY_LINK_RESTORATION);
        // ensure that a symlink to a remote link will not become a remote link.
        ((ResourceImpl) tempResource).removePropertyWithNoUpdate(RegistryConstants.REGISTRY_REAL_PATH);
        ((ResourceImpl) tempResource).addPropertyWithNoUpdate(RegistryConstants.REGISTRY_MOUNT_POINT, this.mountPoint);
        ((ResourceImpl) tempResource).addPropertyWithNoUpdate(RegistryConstants.REGISTRY_TARGET_POINT, this.targetPoint);
        ((ResourceImpl) tempResource).addPropertyWithNoUpdate(RegistryConstants.REGISTRY_AUTHOR, author);

        if (tempResource.getProperty(RegistryConstants.REGISTRY_ACTUAL_PATH) == null) {
            ((ResourceImpl) tempResource).addPropertyWithNoUpdate(RegistryConstants.REGISTRY_ACTUAL_PATH, actualPath);
        }
        //Used to store paths when there are recursive calls
        ((ResourceImpl) tempResource).addPropertyWithNoUpdate("registry.path", fullPath);
        requestContext.setProcessingComplete(true);
        return tempResource;
    }

    public void delete(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getResourcePath().getPath();
        RegistryContext registryContext = requestContext.getRegistryContext();
        if (fullPath.equals(this.mountPoint)) {
            requestContext.getRegistry().removeLink(fullPath);
            if (registryContext != null) {
                HandlerManager hm = registryContext.getHandlerManager();
                hm.removeHandler(this,
                        HandlerLifecycleManager.TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE);
            }
        } else {
            String subPath = fullPath.substring(this.mountPoint.length(), fullPath.length());
            String actualPath = this.targetPoint + subPath;
            requestContext.getRegistry().delete(actualPath);
        }
        requestContext.setProcessingComplete(true);
    }

    public String rename(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullResourcePath = requestContext.getSourcePath();
        String fullTargetPath = requestContext.getTargetPath();
        RegistryContext registryContext = requestContext.getRegistryContext();
        if (fullResourcePath.equals(this.mountPoint)) {
            requestContext.getRegistry().removeLink(this.mountPoint);
            if (registryContext != null) {
                HandlerManager hm = registryContext.getHandlerManager();
                hm.removeHandler(this,
                        HandlerLifecycleManager.TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE);
            }
            requestContext.getRegistry().createLink(fullTargetPath, this.targetPoint);
        } else {
            String subPath =
                    fullResourcePath.substring(this.mountPoint.length(), fullResourcePath.length());
            String actualResourcePath = this.targetPoint + subPath;
            subPath = fullTargetPath.substring(this.mountPoint.length(), fullTargetPath.length());
            String actualTargetPath = this.targetPoint + subPath;
            requestContext.getRegistry().rename(actualResourcePath, actualTargetPath);
        }
        requestContext.setProcessingComplete(true);

        return fullTargetPath;
    }

    public String move(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullResourcePath = requestContext.getSourcePath();
        String fullTargetPath = requestContext.getTargetPath();
        RegistryContext registryContext = requestContext.getRegistryContext();
        if (fullResourcePath.equals(this.mountPoint)) {
            requestContext.getRegistry().removeLink(this.mountPoint);
            if (registryContext != null) {
                HandlerManager hm = registryContext.getHandlerManager();
                hm.removeHandler(this,
                        HandlerLifecycleManager.TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE);
            }
            requestContext.getRegistry().createLink(fullTargetPath, this.targetPoint);
        } else if (fullResourcePath.startsWith(this.mountPoint)) {
            String subPath =
                    fullResourcePath.substring(this.mountPoint.length(), fullResourcePath.length());
            String actualResourcePath;

            if (this.targetPoint.equals(RegistryConstants.PATH_SEPARATOR)) {
                actualResourcePath = subPath;
            } else {
                actualResourcePath = this.targetPoint + subPath;
            }
            requestContext.getRegistry().move(actualResourcePath, fullTargetPath);
        } else if (fullTargetPath.startsWith(this.mountPoint)) {
            String subPath =
                    fullTargetPath.substring(this.mountPoint.length(), fullTargetPath.length());
            String actualTargetPath;

            if (this.targetPoint.equals(RegistryConstants.PATH_SEPARATOR)) {
                actualTargetPath = subPath;
            } else {
                actualTargetPath = this.targetPoint + subPath;
            }
            requestContext.getRegistry().move(fullResourcePath, actualTargetPath);
        }
        requestContext.setProcessingComplete(true);

        return fullTargetPath;
    }

    public String copy(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullResourcePath = requestContext.getSourcePath();
        String fullTargetPath = requestContext.getTargetPath();
        if (fullResourcePath.equals(this.mountPoint)) {
            requestContext.getRegistry().createLink(fullTargetPath, this.targetPoint);
        } else if (fullResourcePath.startsWith(this.mountPoint)) {
            String subPath =
                    fullResourcePath.substring(this.mountPoint.length(), fullResourcePath.length());
            String actualResourcePath;

            if (this.targetPoint.equals(RegistryConstants.PATH_SEPARATOR)) {
                actualResourcePath = subPath;
            } else {
                actualResourcePath = this.targetPoint + subPath;
            }
            requestContext.getRegistry().copy(actualResourcePath, fullTargetPath);
        } else if (fullTargetPath.startsWith(this.mountPoint)) {
            String subPath =
                    fullTargetPath.substring(this.mountPoint.length(), fullTargetPath.length());
            String actualTargetPath;

            if (this.targetPoint.equals(RegistryConstants.PATH_SEPARATOR)) {
                actualTargetPath = subPath;
            } else {
                actualTargetPath = this.targetPoint + subPath;
            }
            requestContext.getRegistry().copy(fullResourcePath, actualTargetPath);
        }
        requestContext.setProcessingComplete(true);

        return fullTargetPath;
    }

    public float getAverageRating(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getResourcePath().getPath();
        String actualPath = getActualPath(fullPath);

        float rating = requestContext.getRegistry().getAverageRating(actualPath);
        requestContext.setProcessingComplete(true);

        return rating;
    }

    public int getRating(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getResourcePath().getPath();
        String actualPath = getActualPath(fullPath);

        int rating =
                requestContext.getRegistry().getRating(actualPath, requestContext.getUserName());
        requestContext.setProcessingComplete(true);

        return rating;
    }

    public void rateResource(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getResourcePath().getPath();
        String actualPath = getActualPath(fullPath);

        requestContext.getRegistry().rateResource(actualPath, requestContext.getRating());
        requestContext.setProcessingComplete(true);
    }

    public Comment[] getComments(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getResourcePath().getPath();
        String actualPath = getActualPath(fullPath);

        Comment[] comments = requestContext.getRegistry().getComments(actualPath);
        requestContext.setProcessingComplete(true);

        return comments;
    }

    public String addComment(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getResourcePath().getPath();
        String actualPath = getActualPath(fullPath);

        String commentPath = requestContext.getRegistry().addComment(actualPath,
                requestContext.getComment());

        requestContext.setProcessingComplete(true);
        return commentPath;
    }

    public Tag[] getTags(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getResourcePath().getPath();
        String actualPath = getActualPath(fullPath);

        Tag[] tags = requestContext.getRegistry().getTags(actualPath);
        requestContext.setProcessingComplete(true);

        return tags;
    }

    public void applyTag(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getResourcePath().getPath();
        String actualPath = getActualPath(fullPath);

        requestContext.getRegistry().applyTag(actualPath,
                requestContext.getTag());
        requestContext.setProcessingComplete(true);
    }

    public Association[] getAllAssociations(RequestContext requestContext)
            throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getResourcePath().getPath();
        String actualPath = getActualPath(fullPath);

//        Association[] associations = requestContext.getRegistryContext().getDataAccessManager().
//                getDAOManager().getAssociationDAO().getAllAssociations(actualPath);
        Association[] associations = requestContext.getRegistry().getAllAssociations(actualPath);
        String sourcePath;
        String destinationPath;
        for (Association association : associations) {
            sourcePath = association.getSourcePath();
            destinationPath = association.getDestinationPath();
            // either source path or destination path should be equal to the actual path
            // we are changing that path to the rewritten (symbolic linking) path
            if (sourcePath.equals(actualPath)) {
                association.setSourcePath(fullPath);
            }
            if (destinationPath.equals(actualPath)) {
                association.setDestinationPath(fullPath);
            }
        }
        requestContext.setProcessingComplete(true);
        return associations;
    }

    public Association[] getAssociations(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getResourcePath().getPath();
        String actualPath = getActualPath(fullPath);

        Association[] associations = requestContext.getRegistry().getAssociations(actualPath,
                requestContext.getAssociationType());
        String sourcePath, destinationPath;
        for (Association association : associations) {
            sourcePath = association.getSourcePath();
            if (sourcePath.substring(sourcePath.lastIndexOf('/'),sourcePath.length()).equals(fullPath)) {
                if (this.targetPoint.equals(RegistryConstants.PATH_SEPARATOR)) {
                    association.setSourcePath(this.mountPoint + sourcePath);
                } else {
                    association.setSourcePath(this.mountPoint + sourcePath.substring(
                            this.targetPoint.length(), sourcePath.length()));
                }
            } else {
                destinationPath = association.getDestinationPath();
                if (this.targetPoint.equals(RegistryConstants.PATH_SEPARATOR)) {
                    association.setDestinationPath(this.mountPoint + destinationPath);
                } else {
                    association.setDestinationPath(this.mountPoint + destinationPath.substring(
                            this.targetPoint.length(), destinationPath.length()));
                }
            }
        }
        requestContext.setProcessingComplete(true);
        return associations;
    }

    public void addAssociation(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getSourcePath();
        String actualPath = getActualPath(fullPath);

        requestContext.getRegistry().addAssociation(actualPath, requestContext.getTargetPath(),
                requestContext.getAssociationType());
        requestContext.setProcessingComplete(true);
    }

    public void importResource(RequestContext requestContext)
            throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getResourcePath().getPath();
        String actualPath = getActualPath(fullPath);

        requestContext.getRegistry().importResource(actualPath, requestContext.getSourceURL(),
                requestContext.getResource());

        requestContext.setProcessingComplete(true);
    }



    public void removeLink(RequestContext requestContext) {
        requestContext.setProperty(RegistryConstants.SYMLINK_TO_REMOVE_PROPERTY_NAME, this);
        // we are not setting the processing complete true, as the basic registry itself
        // has the operation to do in removing permanent entries.
    }

    private void registerHandler(Registry registry) throws RegistryException {
        if (!isHandlerRegistered) {
            RegistryUtils.addMountEntry(registry, RegistryContext.getBaseInstance(), mountPoint,
                    targetPoint, false, author);
            isHandlerRegistered = true;
        }
    }

    /**
     * Method to set the mount point
     *
     * @param mountPoint the mount point
     */
    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    /**
     * Method to remove an already mount point
     *
     * @return mountPoint the mount point
     */
    public String getMountPoint() {
        return this.mountPoint;
    }

    /**
     * Method to set the target point
     *
     * @param targetPoint the target point
     */
    public void setTargetPoint(String targetPoint) {
        if (targetPoint.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            this.targetPoint = targetPoint.substring(0, targetPoint.length() - 1);
        } else {
            this.targetPoint = targetPoint;
        }
    }

    /**
     * Method to get the target point
     *
     * @return the target point
     */
    public String getTargetPoint() {
        return targetPoint;
    }

    // Utility method used to compute the actual path
    private String getActualPath(String fullPath) {
        String actualPath;
        if (fullPath.equals(this.mountPoint)) {
            actualPath = this.targetPoint;
        } else {
            String subPath = fullPath.substring(this.mountPoint.length(), fullPath.length());
            if (this.targetPoint.equals(RegistryConstants.PATH_SEPARATOR)) {
                actualPath = subPath;
            } else {
                actualPath = this.targetPoint + subPath;
            }
        }

        return actualPath;
    }

    /**
     * Method to set the author
     *
     * @param author the author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Return the sym-link handler set
     *
     * @return  sym-link handler set
     */
    public static Set<SymLinkHandler> getSymLinkHandlers() {
        return symLinkHandlers;
    }

    public void removeAssociation(RequestContext requestContext)throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getSourcePath();
        String actualPath = getActualPath(fullPath);
        requestContext.getRegistry().removeAssociation(actualPath, requestContext.getTargetPath(),
                requestContext.getAssociationType());
        requestContext.setProcessingComplete(true);
    }

    public void removeTag(RequestContext requestContext) throws RegistryException {
        registerHandler(requestContext.getSystemRegistry());
        String fullPath = requestContext.getResourcePath().getPath();
        String actualPath = getActualPath(fullPath);
        requestContext.getRegistry().removeTag(actualPath,
                requestContext.getTag());
        requestContext.setProcessingComplete(true);
    }

}
