/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.LogEntry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.TaggedResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class providing the chroot related functionality. The instance of this is used by the
 * UserRegistry to wrap all the operation for inputs and outputs with chroot
 */
public class ChrootWrapper {

    private static final Log log = LogFactory.getLog(UserRegistry.class);

    /**
     * The base prefix.
     */
    protected String basePrefix = null;

    /**
     * Construct a ChrootWrapper with a base prefix.
     *
     * @param basePrefix the base prefix.
     */
    public ChrootWrapper(String basePrefix) {
        if (basePrefix != null) {
            if (basePrefix.equals(RegistryConstants.ROOT_PATH)) {
                basePrefix = null;
            }
            this.basePrefix = basePrefix;
        }
    }

    /**
     * Method to return the base prefix.
     *
     * @return the base prefix.
     */
    public String getBasePrefix() {
        return basePrefix;
    }

    /**
     * Get an absolute path for the given path argument, taking into account both initial
     * double-slashes (indicating an absolute path) and any basePrefix that has been established.
     * <p/>
     * This is the converse of getOutPath().
     *
     * @param path a relative path
     *
     * @return an absolute path into the "real" registry.
     */
    public String getInPath(String path) {
        // No worries if there's no base prefix
        if (basePrefix == null || basePrefix.length() == 0 || path == null) {
            return path;
        }
        if (log.isTraceEnabled()) {
            log.trace("Deriving the absolute path, " +
                    "chroot-base: " + basePrefix + ", " +
                    "path: " + path + ".");
        }

        if (path.startsWith("//")) {
            // This is an absolute path, so just strip the doubled slash
            return path.substring(1);
        }
        if (!path.startsWith(RegistryConstants.ROOT_PATH)) {
            path = RegistryConstants.ROOT_PATH + path;
        }
        if (path.equals(RegistryConstants.ROOT_PATH)) {
            return basePrefix;
        }

        // Relative path, so prepend basePrefix appropriately
        return basePrefix + path;
    }

    /**
     * Take an absolute path in the "real" registry and convert it to a relative path suitable for
     * this particular RemoteRegistry (which may be rooted at a particular place).
     * <p/>
     * This is the converse of getInPath().
     *
     * @param absolutePath a full path from the root of the registry, starting with "/"
     *
     * @return a relative path which generates the correct absolute path
     */
    public String getOutPath(String absolutePath) {
        // No worries if there's no base prefix
        if (basePrefix == null || basePrefix.length() == 0 || absolutePath == null) {
            return absolutePath;
        }
        if (log.isTraceEnabled()) {
            log.trace("Deriving the relative path, " +
                    "chroot-base: " + basePrefix + ", " +
                    "path: " + absolutePath + ".");
        }

        if (absolutePath.startsWith(basePrefix + RegistryConstants.PATH_SEPARATOR)) {
            return absolutePath.substring(basePrefix.length());
        } else if (absolutePath.equals(basePrefix)) {
            return RegistryConstants.ROOT_PATH;
        } else if (absolutePath.startsWith(basePrefix + ";version")) {
            return "/" + absolutePath.substring(basePrefix.length());
        }

        // Somewhere else, so make sure there are dual slashes at the beginning
        return "/" + absolutePath;
    }

    /**
     * returns a set of relative path for the provided absolute paths.
     *
     * @param absolutePaths the array of absolute paths.
     *
     * @return the array of relative paths
     */
    public String[] getOutPaths(String[] absolutePaths) {
        if (basePrefix == null || basePrefix.length() == 0 ||
                absolutePaths == null || absolutePaths.length == 0) {
            return absolutePaths;
        }
        for (int i = 0; i < absolutePaths.length; i++) {
            String absolutePath = absolutePaths[i];
            absolutePaths[i] = getOutPath(absolutePath);
        }
        return absolutePaths;
    }

    /**
     * The resource needed to be modified in case of out resource
     *
     * @param resource the resource that should prepared with chroot to return out.
     *
     * @return the resource after preparing with chroot processing
     * @throws RegistryException throws if the operation failed.
     */
    public Resource getOutResource(Resource resource) throws RegistryException {
        // No worries if there's no base prefix
        if (basePrefix == null || basePrefix.length() == 0) {
            return resource;
        }
        String absolutePath = resource.getPath();
        if (log.isTraceEnabled()) {
            log.trace("Deriving the relative resource, " +
                    "chroot-base: " + basePrefix + ", " +
                    "resource-absolute-path: " + absolutePath + ".");
        }
        if (resource instanceof CollectionImpl) {
            fixCollection((CollectionImpl) resource);
        }
        // fixing the path attribute of the resource
        if (absolutePath != null) {
            String relativePath = getOutPath(absolutePath);
            ((ResourceImpl) resource).setPath(relativePath);
        }
        String permanentPath = resource.getPermanentPath();
        if (permanentPath != null) {
            ((ResourceImpl) resource)
                    .setMatchingSnapshotID(((ResourceImpl) resource).getMatchingSnapshotID());
        }
        fixMountPoints(resource);
        return resource;
    }

    /**
     * When returning collection (with pagination) it need to unset the collection content.
     *
     * @param collection the collection to be prepared with chroot to return out.
     *
     * @return the resource after preparing with chroot processing
     * @throws RegistryException throws if the operation failed.
     */
    public Collection getOutCollection(Collection collection) throws RegistryException {
        if (basePrefix == null || basePrefix.length() == 0) {
            return collection;
        }
        String absolutePath = collection.getPath();
        if (log.isTraceEnabled()) {
            log.trace("Deriving the relative resource, " +
                    "chroot-base: " + basePrefix + ", " +
                    "resource-absolute-path: " + absolutePath + ".");
        }
        fixCollection((CollectionImpl) collection);
        // fixing the path attribute of the resource
        if (absolutePath != null) {
            String relativePath = getOutPath(absolutePath);
            ((ResourceImpl) collection).setPath(relativePath);
        }
        fixMountPoints(collection);
        return collection;
    }

    /**
     * Return the associations array with converting all to relative paths.
     *
     * @param associations the associations that are in absolute paths.
     *
     * @return the associations after converting to the relative paths.
     */
    public Association[] getOutAssociations(Association[] associations) {
        if (basePrefix == null || basePrefix.length() == 0) {
            return associations;
        }
        for (Association association : associations) {
            if (association != null) {
                association.setSourcePath(getOutPath(association.getSourcePath()));
                // Don't fix target path if it is not an external URL.
                if (!association.getDestinationPath().matches("^[a-zA-Z]+://.*")) {
                    association.setDestinationPath(getOutPath(
                            association.getDestinationPath()).replace("//", "/"));
                }
            }
        }
        return associations;
    }

    /**
     * Method to return the tagged resource after converting to relative paths
     *
     * @param taggedResourcePaths the set of tagged resource paths.
     *
     * @return the tagged resource paths after making them relative.
     */
    public TaggedResourcePath[] getOutTaggedResourcePaths(
            TaggedResourcePath[] taggedResourcePaths) {
        if (basePrefix == null || basePrefix.length() == 0) {
            return taggedResourcePaths;
        }
        for (TaggedResourcePath trp : taggedResourcePaths) {
            String path = trp.getResourcePath();
            trp.setResourcePath(getOutPath(path));
        }
        return taggedResourcePaths;
    }

    /**
     * Method to return the comments with relative paths set.
     *
     * @param comments the comments with absolute paths
     *
     * @return the comments after converting to the relative paths.
     */
    public Comment[] getOutComments(Comment[] comments) {
        if (basePrefix == null || basePrefix.length() == 0) {
            return comments;
        }
        for (Comment comment : comments) {
            comment.setPath(getOutPath(comment.getPath()));
            comment.setResourcePath(getOutPath(comment.getResourcePath()));
            comment.setCommentPath(getOutPath(comment.getCommentPath()));
        }
        return comments;
    }

    /**
     * Filter search results, so the results outside the base prefix will be ignored and results
     * inside the base prefix will be converted to relative paths.
     *
     * @param collection unfiltered search results
     *
     * @return filtered search results
     * @throws RegistryException throws if the operation failed.
     */
    public Collection filterSearchResult(Collection collection) throws RegistryException {
        if (basePrefix == null || basePrefix.length() == 0) {
            return collection;
        }
        String[] results = collection.getChildren();
        if (results == null || results.length == 0) {
            return collection;
        }
        List<String> filteredResult = new ArrayList<String>();
        for (String result : results) {
            if (result.startsWith(basePrefix + RegistryConstants.PATH_SEPARATOR)) {
                filteredResult.add(result);
            }
        }
        String[] filteredResultArr = filteredResult.toArray(new String[filteredResult.size()]);
        collection.setContent(filteredResultArr);
        return collection;
    }

    /**
     * The internal method to convert the collection to hold relative path values.
     *
     * @param collection the collection with absolute paths.
     *
     * @throws RegistryException throws if the operation failed.
     */
    private void fixCollection(CollectionImpl collection) throws RegistryException {
        if (basePrefix == null || basePrefix.length() == 0) {
            return;
        }
        Object content = collection.getContent();
        if (content instanceof String[]) {
            String[] paths = (String[]) content;
            for (int i = 0; i < paths.length; i++) {
                paths[i] = getOutPath(paths[i]);
            }
        } else if (content instanceof Resource[]) {
            Resource[] resources = (Resource[]) content;
            for (Resource resource : resources) {
                ((ResourceImpl) resource).setPath(getOutPath(resource.getPath()));
                if (resource instanceof Comment) {
                    Comment comment = (Comment) resource;
                    comment.setResourcePath(getOutPath(comment.getResourcePath()));
                }
            }
        }
    }

    /**
     * Mount points of a give resource are converted to relative values.
     *
     * @param resource the resource which it is mount points are converting to relative values.
     */
    private void fixMountPoints(Resource resource) {
        if (basePrefix == null || basePrefix.length() == 0) {
            return;
        }
        String mountPoint = resource.getProperty(RegistryConstants.REGISTRY_MOUNT_POINT);
        if (mountPoint != null) {
            resource.setProperty(RegistryConstants.REGISTRY_MOUNT_POINT, getOutPath(mountPoint));
        }
        String targetPoint = resource.getProperty(RegistryConstants.REGISTRY_TARGET_POINT);
        if (targetPoint != null) {
            resource.setProperty(RegistryConstants.REGISTRY_TARGET_POINT, getOutPath(targetPoint));
        }
        String actualPath = resource.getProperty(RegistryConstants.REGISTRY_ACTUAL_PATH);
        if (actualPath != null) {
            resource.setProperty(RegistryConstants.REGISTRY_ACTUAL_PATH, getOutPath(actualPath));
        }
    }

    /**
     * Convert the paths of the log entries to relative values.
     *
     * @param logEntries the array of log entries to be converted to relative paths.
     *
     * @return the log entries after converting them relative values.
     */
    public LogEntry[] fixLogEntries(LogEntry[] logEntries) {
        if (basePrefix == null || basePrefix.length() == 0) {
            return logEntries;
        }
        List<LogEntry> fixedLogEntries = new ArrayList<LogEntry>();
        for (LogEntry logEntry : logEntries) {
            String logPath = logEntry.getResourcePath();
            if (logPath == null ||
                    (!logPath.startsWith(basePrefix + RegistryConstants.PATH_SEPARATOR) &&
                            !logPath.equals(basePrefix))) {
                continue;
            }
            logEntry.setResourcePath(getOutPath(logPath));
            if (logEntry.getActionData() != null &&
                    logEntry.getActionData().startsWith(basePrefix)) {
                logEntry.setActionData(getOutPath(logEntry.getActionData()));
            } else if (logEntry.getAction() == LogEntry.ADD_ASSOCIATION ||
                    logEntry.getAction() == LogEntry.REMOVE_ASSOCIATION) {
                String actionData = logEntry.getActionData();
                String[] temp = actionData.split(";");
                if (temp.length == 2 && temp[1].startsWith(basePrefix)) {
                    logEntry.setActionData(temp[0] + ";" + getOutPath(temp[1]));
                }
            }
            fixedLogEntries.add(logEntry);
        }
        return fixedLogEntries.toArray(new LogEntry[fixedLogEntries.size()]);
    }
}
