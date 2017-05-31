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

/**
 * Represents a tag and its meta-data. Instances of this class is returned from the Registry
 * interface, when tags for a given resource path is queried.
 */
public class Tag extends org.wso2.carbon.registry.api.Tag {

    private static final int DEFAULT_CATEGORY = 1;

    private static final int LIMIT_ONE = 2;
    private static final int LIMIT_TWO = 8;
    private static final int LIMIT_THREE = 20;
    private static final int LIMIT_FOUR = 50;

    /**
     * Tags are categorized according to the tag count. Then the category indicates the validity
     * of the tag. the {@link #setTagCount} method explains how category is calculated based on
     * the tag count. This is used in the WSO2 Registry web UI to generate the tag cloud.
     */
    private int category = DEFAULT_CATEGORY;

    /**
     * Set the tag count.
     *
     * @param tagCount the tag count.
     */
    public void setTagCount(long tagCount) {
        this.tagCount = tagCount;

        category = 1;
        if (tagCount > LIMIT_ONE) {
            category++;
        }
        if (tagCount > LIMIT_TWO) {
            category++;
        }
        if (tagCount > LIMIT_THREE) {
            category++;
        }
        if (tagCount > LIMIT_FOUR) {
            category++;
        }
    }

    /**
     * Get the category.
     *
     * @return the tag category.
     */
    @SuppressWarnings("unused")
    public int getCategory() {
        return category;
    }

    /**
     * Set the category.
     *
     * @param category the category.
     */
    @SuppressWarnings("unused")
    public void setCategory(int category) {
        this.category = category;
    }
}
