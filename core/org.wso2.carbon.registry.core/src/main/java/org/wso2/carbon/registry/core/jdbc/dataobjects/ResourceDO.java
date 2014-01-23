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

package org.wso2.carbon.registry.core.jdbc.dataobjects;

/**
 * The data object maps with a resource
 */
public class ResourceDO {

    private int pathID;
    private String name;
    private long version = -1;
    private String mediaType;
    private String author;
    private long createdOn;
    private String lastUpdater;
    private long lastUpdatedOn;
    private String description;
    private int contentID = -1;
    private String uuid;

    /**
     * Method to get the path id of the resource. If it is a collection it gives the id of the path
     * itself. If it is a non-collection, it give the path id of the parent path.
     *
     * @return the path id.
     */
    public int getPathID() {
        return pathID;
    }

    /**
     * Method to set the path id.
     *
     * @param pathID path id to be set.
     */
    public void setPathID(int pathID) {
        this.pathID = pathID;
    }

    /**
     * Method to get the name of the resource.
     *
     * @return the name of the resource
     */
    public String getName() {
        return name;
    }

    /**
     * Method to set the name of the resource.
     *
     * @param name the name of the resource.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Method to get the version of the resource.
     *
     * @return get the version.
     */
    public long getVersion() {
        return version;
    }

    /**
     * Method to set the version of the resource.
     *
     * @param version the version to be set.
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * Method to get the media type.
     *
     * @return media type
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Method to set the media type.
     *
     * @param mediaType media type to be set.
     */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Method to get the author.
     *
     * @return the author.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Method to set the author.
     *
     * @param author the author to be set.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Method to get the time the resource created.
     *
     * @return the time the resource created.
     */
    public long getCreatedOn() {
        return createdOn;
    }

    /**
     * Method to set the time the resource created
     *
     * @param createdOn the time to be set.
     */
    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * Method to get the last updated user of the resource.
     *
     * @return the last updated user
     */
    public String getLastUpdater() {
        return lastUpdater;
    }

    /**
     * Method to set the last updated user name of the resource.
     *
     * @param lastUpdater the last updated user to be set.
     */
    public void setLastUpdater(String lastUpdater) {
        this.lastUpdater = lastUpdater;
    }

    /**
     * Method to get the last updated time.
     *
     * @return the last updated time.
     */
    public long getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    /**
     * Method to set the last updated time.
     *
     * @param lastUpdatedOn the last updated time to be set.
     */
    public void setLastUpdatedOn(long lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    /**
     * Method to get the resource description.
     *
     * @return the resource description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Method to set the resource description.
     *
     * @param description the resource description to be set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Method to get the content id. (the database index of the content of the resource)
     *
     * @return the content id.
     */
    public int getContentID() {
        return contentID;
    }

    /**
     * Method to set the content id.  (the database index of the content of the resource)
     *
     * @param contentID the content id to be set.
     */
    public void setContentID(int contentID) {
        this.contentID = contentID;
    }

    /**
     * Method to set the UUID
     *
     * @param uuid the UUID to be set.
     */
    public void setUUID(String uuid){
        this.uuid = uuid;
    }

    /**
     * Method to get the UUID.
     *
     * @return the UUID of the resource.
     */
    public String getUUID(){
        return uuid;
    }
}

