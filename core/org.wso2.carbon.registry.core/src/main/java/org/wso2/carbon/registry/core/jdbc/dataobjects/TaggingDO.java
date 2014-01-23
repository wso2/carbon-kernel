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

import java.util.Date;

/**
 * The data object maps with a tag
 */
public class TaggingDO {

    private long tagID;
    private String resourcePath;
    private String taggedUserName;
    private String tagName;
    private long taggedTime;

    /**
     * Method to get the tag id.
     *
     * @return the tag id.
     */
    public Long getTagID() {
        return tagID;
    }

    /**
     * Method to set the tag id
     *
     * @param tagID the tag id to be set.
     */
    public void setTagID(Long tagID) {
        this.tagID = tagID;
    }

    /**
     * Method to get the resource path.
     *
     * @return the name of the resource.
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Method to set the resource path.
     *
     * @param resourcePath the path to be set.
     */
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * Method to get the name of the tagged user.
     *
     * @return the name of the tagged user.
     */
    public String getTaggedUserName() {
        return taggedUserName;
    }

    /**
     * Method to set the tagged user name.
     *
     * @param taggedUserName the tagged user name.
     */
    public void setTaggedUserName(String taggedUserName) {
        this.taggedUserName = taggedUserName;
    }

    /**
     * The method to get the tag name.
     *
     * @return the tag name.
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * The method to set the tag name.
     *
     * @param tagName the tag name to be set.
     */
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    /**
     * The method to get the tagged time.
     *
     * @return the tagged time.
     */
    public Date getTaggedTime() {
        return new Date(taggedTime);
    }

    /**
     * The method to set the tagged time.
     *
     * @param taggedTime the tagged time.
     */
    public void setTaggedTime(Date taggedTime) {
        this.taggedTime = taggedTime.getTime();
    }
}
