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
package org.wso2.carbon.registry.api;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains a resource path and tagging details of tags on that resource. Instances of this class is
 * returned from the tag searches. Tag searches may be performed on multiple tags. This class only
 * contains the tag counts for the tags that included in the search. e.g. If the search is for tags
 * java, jsp and programming and if the resource /books/computer/Java Web Development matches the
 * search, counts are only contained for the tags java, jsp and programming although that resource
 * may have many other tags like internet, web, servlets.
 */
public class TaggedResourcePath {

    /**
     * Resource path for which the tag counts are associated.
     */
    protected String resourcePath;

    /**
     * Tags which are associated with <code>resourcePath</code>
     */
    protected List<Tag> tags = new ArrayList<Tag>();

    /**
     * Method to get the resource path.
     *
     * @return the resource path.
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Method to get the resource path.
     *
     * @param resourcePath the resource path.
     */
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * Method to get the tag count.
     *
     * @return the tag count.
     */
    public Tag[] getTags() {
        return tags.toArray(new Tag[tags.size()]);
    }
}
