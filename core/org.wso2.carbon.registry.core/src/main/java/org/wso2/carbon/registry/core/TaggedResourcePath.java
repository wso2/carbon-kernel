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

package org.wso2.carbon.registry.core;

import org.wso2.carbon.registry.api.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains a resource path and tagging details of tags on that resource. Instances of this class is
 * returned from the tag searches. Tag searches may be performed on multiple tags. This class only
 * contains the tag counts for the tags that included in the search. e.g. If the search is for tags
 * java, jsp and programming and if the resource /books/computer/Java Web Development matches the
 * search, counts are only contained for the tags java, jsp and programming although that resource
 * may have many other tags like internet, web, servlets.
 */
public class TaggedResourcePath extends org.wso2.carbon.registry.api.TaggedResourcePath {

    /**
     * Method to get the tag count.
     *
     * @return the tag count.
     */
    public long getTagCount() {
        return tags.size();
    }

    /**
     * Method to set the tag count.
     *
     * @param tagCount the tag count.
     * @deprecated There is no need to externally set this
     */
    public void setTagCount(long tagCount) {
    }

    /**
     * Method to get the tag count.
     *
     * @return the tag count.
     */
    public Map<String, String> getTagCounts() {
        Map<String, String> tagMap = new HashMap<String, String>();
        for (Tag tag : tags) {
            tagMap.put(tag.getTagName(), String.valueOf(tag.getTagCount()));
        }
        return tagMap;
    }

    /**
     * Method to set the tag count.
     *
     * @param tagCounts the tag count.
     */
    public void setTagCounts(Map<String, String> tagCounts) {
        tags = new ArrayList<Tag>(tagCounts.size());
        for (Map.Entry<String, String> tagEntry : tagCounts.entrySet()) {
            Tag tag = new Tag();
            tag.setTagName(tagEntry.getKey());
            tag.setTagCount(Long.parseLong(tagEntry.getValue()));
            tags.add(tag);
        }
    }

    /**
     * Method to add the tag count.
     *
     * @param tagName   the tag.
     * @param count the count.
     */
    public void addTagCount(String tagName, long count) {
        Tag tagObj = new Tag();
        tagObj.setTagName(tagName);
        tagObj.setTagCount(count);
        tags.add(tagObj);
    }
}
