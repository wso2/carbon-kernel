/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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

/**
 * Represents a tag and its meta-data. Instances of this class is returned from the Registry
 * interface, when tags for a given resource path is queried.
 */
public class Tag {

    /**
     * Name of the tag. This may contain spaces.
     */
    protected String tagName;

    /**
     * Number of taggings done using this tag. If a Tag object is returned as a result of a
     * Registry.getTags(String resourcePath) method, then this contains the number of users who
     * tagged the given resource using this tag.
     */
    protected long tagCount;

    /**
     * Get the tag name.
     *
     * @return the tag name.
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * Set the tag name.
     *
     * @param tagName the tag name.
     */
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    /**
     * Set the the number of times the same tag has been used.
     *
     * @param tagCount the number of times the same tag has been used.
     */
    public void setTagCount(long tagCount) {
        this.tagCount = tagCount;
    }

    /**
     * Get the tag count.
     *
     * @return the tag count.
     */
    public long getTagCount() {
        return tagCount;
    }
}
