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

package org.wso2.carbon.registry.core.dao;

import org.wso2.carbon.registry.core.ResourceIDImpl;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.dataobjects.TaggingDO;

import java.util.List;

/**
 * Data Access Object for Tags
 */
public interface TagsDAO {

    /**
     * Method to persist a tag.
     *
     * @param resource the resource
     * @param userID   the id of the user who added the tag.
     * @param tagName  the name of tag to be persisted.
     *
     * @throws RegistryException if some error occurs while adding a tag
     */
    void addTagging(String tagName, ResourceImpl resource, String userID)
            throws RegistryException;

    /**
     * Method to persist tags.
     *
     * @param resource   the resource
     * @param taggingDOs the tags to be persisted.
     *
     * @throws RegistryException if some error occurs while adding tags
     */
    void addTaggings(ResourceImpl resource, TaggingDO[] taggingDOs)
            throws RegistryException;

    /**
     * Method to copy tags.
     *
     * @param fromResource the source resource.
     * @param toResource   the target resource.
     *
     * @throws RegistryException if some error occurs while copying tags
     */
    void copyTags(ResourceImpl fromResource, ResourceImpl toResource)
            throws RegistryException;

    /**
     * Method to determine whether the given tag exists.
     *
     * @param resourceImpl the resource
     * @param userID       the id of the user who added the tag.
     * @param tagName      the name of tag to be persisted.
     *
     * @return whether the given tag exists.
     * @throws RegistryException if some error occurs while checking whether a tag exists.
     */
    boolean taggingExists(String tagName, ResourceImpl resourceImpl, String userID)
            throws RegistryException;

    /**
     * Removes a tag by the given name added to the given resource by any user.
     *
     * @param resource the resource
     * @param tag      the name of tag to be persisted.
     *
     * @throws RegistryException if an error occurred while removing the tag.
     */
    void removeTags(ResourceImpl resource, String tag)
            throws RegistryException;

    /**
     * Removes a tag by the given name added to the given resource by user with the given id.
     *
     * @param resource the resource
     * @param tag      the name of tag to be persisted.
     * @param userID   the id of the user who added the tag.
     *
     * @throws RegistryException if an error occurred while removing the tag.
     */
    void removeTags(ResourceImpl resource, String tag, String userID)
            throws RegistryException;

    /**
     * Removes all tags added to the given resource by user with the given id.
     *
     * @param resource the resource
     *
     * @throws RegistryException if an error occurred while removing tags.
     */
    void removeTags(ResourceImpl resource)
            throws RegistryException;
    
    /**
     * Removes all tags added to the given resource of a given version. 
     * This applies only to versioned resources. 
     *
     * @param regVersion the version
     *
     * @throws RegistryException if an error occurred while removing tags.
     */
    void removeVersionTags(long regVersion)
            throws RegistryException;
    

    /**
     * Method to get the names of tags added to the given resource.
     *
     * @param resourceImpl the resource.
     *
     * @return array of tag names.
     * @throws RegistryException if an error occurs while getting the tag names.
     */
    String[] getTags(ResourceImpl resourceImpl) throws RegistryException;

    /**
     * Method to get the data objects of tags added to the given resource.
     *
     * @param resourceImpl the resource.
     *
     * @return list of tagging data objects.
     * @throws RegistryException if an error occurs while getting the tagging data objects.
     */
    List<TaggingDO> getTagDOs(ResourceImpl resourceImpl) throws RegistryException;

    /**
     * Method to obtain the list of paths having any of the given tags.
     *
     * @param tags the tags.
     *
     * @return a list of paths.
     * @throws RegistryException if an error occurs.
     */
    List getPathsWithAnyTag(String[] tags) throws RegistryException;

    /**
     * Method to get the number of tags added to the given resource, by the given name.
     *
     * @param resourceImpl the resource.
     * @param tag          the tag name
     *
     * @return the number of tags.
     * @throws RegistryException if an error occurred while getting the number of tags.
     */
    long getTagCount(ResourceImpl resourceImpl, String tag) throws RegistryException;

    /**
     * Method to get tags added to the given resource, along with the count.
     *
     * @param resourceImpl the resource.
     *
     * @return an array of tags (with counts).
     * @throws RegistryException if an error occurred while getting tags.
     */
    Tag[] getTagsWithCount(ResourceImpl resourceImpl) throws RegistryException;

    /**
     * Method to get a tagging added to a given resource by the given user.
     *
     * @param resource the resource.
     * @param tag      the name of the tag.
     * @param userID   the id of the user who added the tagging.
     *
     * @return the tagging data objects.
     * @throws RegistryException if an error occurs while getting the tagging.
     */
    TaggingDO[] getTagging(ResourceImpl resource, String tag, String userID)
            throws RegistryException;

    /**
     * Method to get all taggings added to a given resource.
     *
     * @param resource the resource.
     *
     * @return the tagging data objects.
     * @throws RegistryException if an error occurs while getting the taggings.
     */
    TaggingDO[] getTagging(ResourceImpl resource)
            throws RegistryException;

    /**
     * Method to get a tagging by the given id.
     *
     * @param taggingID the id of the tagging.
     *
     * @return the tagging data object.
     * @throws RegistryException if an error occurs while getting the tagging.
     */
    TaggingDO getTagging(long taggingID)
            throws RegistryException;

    /**
     * Gets the resource with sufficient data to differentiate it from another resource. This would
     * populate a {@link ResourceImpl} with the <b>path</b>, <b>name</b> and <b>path identifier</b>
     * of a resource.
     *
     * @param path the path of the resource.
     *
     * @return the resource with minimum data.
     * @throws RegistryException if an error occurs while retrieving resource data.
     */
    ResourceImpl getResourceWithMinimumData(String path) throws RegistryException;

    /**
     * Method to move tags.
     *
     * @param source the source resource.
     * @param target the target resource.
     *
     * @throws RegistryException if some error occurs while moving tags
     */
    void moveTags(ResourceIDImpl source, ResourceIDImpl target) throws RegistryException;

    /**
     * Method to move tag paths. This function will move the paths from one path id to another
     * regardless of the resource name.
     *
     * @param source the source resource.
     * @param target the target resource.
     *
     * @throws RegistryException if some error occurs while moving tag paths
     */
    void moveTagPaths(ResourceIDImpl source, ResourceIDImpl target)
            throws RegistryException;
}
