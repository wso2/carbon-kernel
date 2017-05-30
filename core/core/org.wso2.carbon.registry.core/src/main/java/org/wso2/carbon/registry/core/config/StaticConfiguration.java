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

package org.wso2.carbon.registry.core.config;

import org.wso2.carbon.registry.core.dao.*;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Class containing static (one-time) configuration information, which can be done before first
 * boot.
 */
public class StaticConfiguration {

    private static boolean versioningProperties = false;
    private static boolean versioningTags = false;
    private static boolean versioningRatings = false;
    private static boolean versioningComments = false;
    private static boolean versioningAssociations = false;

    /**
     * Method to obtain whether properties are versioned.
     *
     * @return whether properties are versioned.
     */
    public static boolean isVersioningProperties() {
        return versioningProperties;
    }

    /**
     * Method to set whether properties are versioned.
     *
     * @param versioningProperties whether properties are versioned.
     */
    public static void setVersioningProperties(boolean versioningProperties) {
        StaticConfiguration.versioningProperties = versioningProperties;
    }

    /**
     * Method to obtain whether tags are versioned.
     *
     * @return whether tags are versioned.
     */
    public static boolean isVersioningTags() {
        return versioningTags;
    }

    /**
     * Method to set whether tags are versioned.
     *
     * @param versioningTags whether tags are versioned.
     */
    public static void setVersioningTags(boolean versioningTags) {
        StaticConfiguration.versioningTags = versioningTags;
    }

    /**
     * Method to obtain whether ratings are versioned.
     *
     * @return whether ratings are versioned.
     */
    public static boolean isVersioningRatings() {
        return versioningRatings;
    }

    /**
     * Method to set whether ratings are versioned.
     *
     * @param versioningRatings whether ratings are versioned.
     */
    public static void setVersioningRatings(boolean versioningRatings) {
        StaticConfiguration.versioningRatings = versioningRatings;
    }

    /**
     * Method to obtain whether associations are versioned.
     *
     * @return whether associations are versioned.
     */
    public static boolean isVersioningAssociations() {
        return versioningAssociations;
    }

    /**
     * Method to set whether associations are versioned.
     *
     * @param versioningAssociations whether associations are versioned.
     */
    public static void setVersioningAssociations(boolean versioningAssociations) {
        StaticConfiguration.versioningAssociations = versioningAssociations;
    }

    /**
     * Method to obtain whether comments are versioned.
     *
     * @return whether comments are versioned.
     */
    public static boolean isVersioningComments() {
        return versioningComments;
    }

    /**
     * Method to set whether comments are versioned.
     *
     * @param versioningComments whether comments are versioned.
     */
    public static void setVersioningComments(boolean versioningComments) {
        StaticConfiguration.versioningComments = versioningComments;
    }

    /**
     * returns comments DAO depending on the configurations
     *
     * @return comments DAO
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static CommentsDAO getCommentsDAO() {
        throw new UnsupportedOperationException("This method is no longer supported.");
    }

    /**
     * returns ratings DAO depending on the configurations
     *
     * @return ratings DAO
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static RatingsDAO getRatingsDAO() {
        throw new UnsupportedOperationException("This method is no longer supported.");
    }

    /**
     * returns tag DAO depending on the configurations
     *
     * @return tag DAO
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static TagsDAO getTagsDAO() {
        throw new UnsupportedOperationException("This method is no longer supported.");
    }
}