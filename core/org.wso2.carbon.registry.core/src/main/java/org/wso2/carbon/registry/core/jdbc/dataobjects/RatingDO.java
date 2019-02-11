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
 * The data object maps with a rating
 */
public class RatingDO {

    private String resourcePath;
    private String ratedUserName;
    private long ratedTime;
    private int rating;
    private int ratingID = -1;

    /**
     * Method to get the rated resource path.
     *
     * @return the path
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Method to set the rate resource path.
     *
     * @param resourcePath the path to be set
     */
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * Method to get the rated user name.
     *
     * @return the rated user name.
     */
    public String getRatedUserName() {
        return ratedUserName;
    }

    /**
     * Method to set the rated user name.
     *
     * @param ratedUserName the rated user name to be set.
     */
    public void setRatedUserName(String ratedUserName) {
        this.ratedUserName = ratedUserName;
    }

    /**
     * Method to get the rated time.
     *
     * @return the rated time.
     */
    public Date getRatedTime() {
        return new Date(ratedTime);
    }

    /**
     * Method to set the rated time.
     *
     * @param ratedTime the rated time to be set.
     */
    public void setRatedTime(Date ratedTime) {
        this.ratedTime = ratedTime.getTime();
    }

    /**
     * Method to get the rating value.
     *
     * @return the rating value.
     */
    public int getRating() {
        return rating;
    }

    /**
     * Method to set the rating value.
     *
     * @param rating the value to be set.
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Method to get rating id.
     *
     * @return return the rating id.
     */
    public int getRatingID() {
        return ratingID;
    }

    /**
     * Method to set the rating id.
     *
     * @param ratingID the value to be set.
     */
    public void setRatingID(int ratingID) {
        this.ratingID = ratingID;
    }
}
