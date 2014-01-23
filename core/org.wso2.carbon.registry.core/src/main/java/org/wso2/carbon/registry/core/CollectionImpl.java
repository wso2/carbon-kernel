/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.dataobjects.ResourceDO;

import java.util.*;

/**
 * The default registry implementation of the Collection interface.
 */
public class CollectionImpl extends ResourceImpl implements Collection {

    private static final Log log = LogFactory.getLog(CollectionImpl.class);

    /**
     * The number of children in this collection.
     */
    protected int childCount;

    /**
     * The default constructor of the CollectionImpl, Create an empty collection with no children.
     */
    public CollectionImpl() {
        childCount = -1;
    }

    /**
     * Construct a collection with the provided children paths.
     *
     * @param paths the children paths.
     */
    public CollectionImpl(String[] paths) {
        try {
            setChildren(paths);
        } catch (RegistryException e) {
            log.warn("Unable to set child paths to this collection.", e);
        }
    }

    /**
     * Construct a collection with the provided path and the resource data object.
     *
     * @param path       the path of the collection.
     * @param resourceDO the resource data object.
     */
    public CollectionImpl(String path, ResourceDO resourceDO) {
        super(path, resourceDO);
        childCount = -1;
    }

    /**
     * A copy constructor used to create a shallow-copy of this collection.
     *
     * @param collection the collection of which the copy is created.
     */
    public CollectionImpl(CollectionImpl collection) {
        super(collection);
        try {
            pullContentFromOriginal();
        } catch (RegistryException ignored) {
            // we are not interested in handling any failures here.
        }
        if (this.content != null) {
            if (this.content instanceof String[]) {
                String[] paths = (String[]) this.content;
                int length = paths.length;
                String[] output = new String[length];
                System.arraycopy(paths, 0, output, 0, length);
                this.content = output;
            }  else if (this.content instanceof Comment[]) {
                Comment[] paths = (Comment[]) this.content;
                int length = paths.length;
                Comment[] output = new Comment[length];
                System.arraycopy(paths, 0, output, 0, length);
                for (int i = 0; i < length; i++) {
                    output[i] = new Comment(output[i]);
                }
                this.content = output;
            } else if (this.content instanceof Resource[]) {
                Resource[] paths = (Resource[]) this.content;
                int length = paths.length;
                Resource[] output = new Resource[length];
                System.arraycopy(paths, 0, output, 0, length);
                for (int i = 0; i < length; i++) {
                    if (output[i] instanceof CollectionVersionImpl) {
                        output[i] = new CollectionVersionImpl((CollectionVersionImpl) output[i]);
                    } else if (output[i] instanceof CollectionImpl) {
                        output[i] = new CollectionImpl((CollectionImpl) output[i]);
                    } else if (output[i] instanceof Comment) {
                        output[i] = new Comment((Comment) output[i]);
                    } else if (output[i] instanceof ResourceImpl) {
                        output[i] = new ResourceImpl((ResourceImpl) output[i]);
                    }
                }
                this.content = output;
            }
        }
        this.childCount = collection.childCount;
    }

    /**
     * Implementation for the setContent. Here the content should always be a array of strings which
     * corresponding to the children paths.
     *
     * @param content array of strings which corresponding to the children paths.
     *
     * @throws RegistryException if the operation fails.
     */
    public void setContent(Object content) throws RegistryException {
        if (content == null) {
            return;
        }
        // note that string contents are allowed in collection to support custom generated UIs.
        if (content instanceof String[]) {
//                  super.setContent(content);
            //We do not update the last modified time when a child resource added to a collection
            super.setContentWithNoUpdate(content);
            childCount = ((String[])content).length;
            return;
        } else if (content instanceof Resource[]) {
//                  super.setContent(content);
            //We do not update the last modified time when a child resource added to a collection
            super.setContentWithNoUpdate(content);
            childCount = ((Resource[])content).length;
            return;
        } else if (content instanceof String) {
//                  super.setContent(content);
            //We do not update the last modified time when a child resource added to a collection
            super.setContentWithNoUpdate(content);

            return;
        }
        throw new IllegalArgumentException("Invalid content for collection. " +
                "Content of type " + content.getClass().toString() +
                " is not allowed for collections.");
    }

    /**
     * Set the resource content without marking the collection as updated.Here the content should
     * always be a array of strings which corresponding to the children paths.
     *
     * @param content array of strings which corresponding to the children paths.
     *
     * @throws RegistryException if the operation fails.
     */
    public void setContentWithNoUpdate(Object content) throws RegistryException {
        if (content == null) {
            return;
        }
        // note that string contents are allowed in collection to support custom generated UIs.
        if (content instanceof String[] ||
                content instanceof Resource[] ||
                content instanceof String) {
            super.setContentWithNoUpdate(content);
            return;
        }
        throw new IllegalArgumentException("Invalid content for collection. " +
                "Content of type " + content.getClass().toString() +
                " is not allowed for collections.");
    }

    /**
     * Method to set the absolute paths of the children belonging to this collection. Absolute paths
     * begin from the ROOT collection.
     *
     * @param paths the array of absolute paths of the children
     *
     * @throws RegistryException if the operation fails.
     */
    public void setChildren(String[] paths) throws RegistryException {
        String[] temp = fixPaths(paths);
        content = temp;
        childCount = temp.length;
    }

    /**
     * Method to return the children.
     *
     * @return an array of children paths.
     * @throws RegistryException if the operation fails.
     */
    public String[] getChildren() throws RegistryException {
        if (getContent() instanceof String[]) {
            return fixPaths((String[])getContent());
        } else {
            return new String[0];
        }
    }

    /**
     * Method to return the paths of the selected range of children.
     *
     * @param start   the starting number of children.
     * @param pageLen the number of entries to retrieve.
     *
     * @return an array of paths of the selected range of children.
     * @throws RegistryException if the operation fails.
     */
    public String[] getChildren(int start, int pageLen) throws RegistryException {
        setSessionInformation();
        try {
            pullContentFromOriginal();
            if (content == null) {
                if (resourceDAO == null) {
                    String msg = "The data access object for resources has not been created.";
                    log.error(msg);
                    throw new RegistryException(msg);
                }
                return fixPaths(resourceDAO.getChildren(this, start, pageLen, dataAccessManager));

            } else {

                if (content instanceof String[]) {

                    String childNodes[] = (String[]) content;
                    int limit = start + pageLen;
                    if (start > childNodes.length) {
                        return new String[0];
                    }
                    if (limit > childNodes.length) {
                        limit = childNodes.length;
                    }

                    return fixPaths(Arrays.copyOfRange(childNodes, start, limit));
                }
            }
            return new String[0];
        } finally {
            clearSessionInformation();
        }
    }

    /**
     * Method to return the the number of children.
     *
     * @return the number of children.
     * @throws RegistryException if the operation fails.
     */
    public int getChildCount() throws RegistryException {

        try {
            setSessionInformation();
            pullContentFromOriginal();
            if (childCount != -1) {
                return childCount;

            } else if (content != null && content instanceof String[]) {

                String[] childPaths = (String[]) content;
                return fixPaths(childPaths).length;

            }
            if (resourceDAO == null) {
                String msg = "The data access object for resources has not been created.";
                log.error(msg);
                throw new RegistryException(msg);
            }
            return resourceDAO.getChildCount(this, dataAccessManager);
        } finally {
            clearSessionInformation();
        }
    }

    /**
     * Method to set the child count.
     *
     * @param count the child count.
     */
    public void setChildCount(int count) {
        childCount = count;
    }


    /**
     * Collection's content is a string array, which contains paths of its children. These paths are
     * loaded on demand to increase performance. It is recommended to use {@link #getChildren()}
     * method to get child paths of a collection, which provides pagination. Calling this method
     * will load all child paths.
     *
     * @return String array of child paths.
     * @throws RegistryException On any error.
     */
    public Object getContent() throws RegistryException {
        setSessionInformation();
        try {
            pullContentFromOriginal();
            if (content == null) {
                if (resourceDAO == null) {
                    String msg = "The data access object for resources has not been created.";
                    log.error(msg);
                    throw new RegistryException(msg);
                }
                resourceDAO.fillChildren(this, dataAccessManager);
            }
            return content;
        } finally {
            clearSessionInformation();
        }
    }

    /**
     * Method to return a shallow copy of a collection.
     *
     * @return the shallow copy of the collection.
     * @throws RegistryException if the operation fails.
     */
    public ResourceImpl getShallowCopy() throws RegistryException {
        CollectionImpl newCollection = new CollectionImpl();
        fillCollectionCopy(newCollection);
        return newCollection;
    }

    /**
     * Copy all the values of the current collection attribute to the passed collection.
     *
     * @param collection the collection to get all the current collection attribute copied.
     *
     * @throws RegistryException if the operation fails.
     */
    public void fillCollectionCopy(CollectionImpl collection) throws RegistryException {
        super.fillResourceCopy(collection);
        collection.setChildCount(this.childCount);
    }

    /**
     * Method to fix duplicated entries in a collection's child paths.
     * @param paths the collection's child paths.
     * @return the distinct set of children.
     */
    @SuppressWarnings("ManualArrayToCollectionCopy")
    protected String[] fixPaths(String[] paths) {
        Set<String> temp = new LinkedHashSet<String>();
        // We want to make sure that each element is added one after the other in the exact order
        // that they were passed in.
        for (String path : paths) {
            temp.add(path);
        }
        return temp.toArray(new String[temp.size()]);
    }
}
