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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.dataobjects.RatingDO;

/**
 * Data Access Object for Ratings
 */
public interface RatingsDAO {

    /**
     * Method to persist a rating.
     *
     * @param resourceImpl the resource
     * @param userID       the id of the user who added the rating.
     * @param rating       the rating to be persisted.
     *
     * @throws RegistryException if some error occurs while adding a rating
     */
    void addRating(ResourceImpl resourceImpl, String userID, int rating)
            throws RegistryException;

    /**
     * Method to copy ratings.
     *
     * @param fromResource the source resource.
     * @param toResource   the target resource.
     *
     * @throws RegistryException if some error occurs while copying ratings
     */
    void copyRatings(ResourceImpl fromResource, ResourceImpl toResource)
            throws RegistryException;

    /**
     * Method to persist ratings.
     *
     * @param resource  the resource
     * @param ratingDOs the ratings to be persisted.
     *
     * @throws RegistryException if some error occurs while adding ratings
     */
    void addRatings(ResourceImpl resource, RatingDO[] ratingDOs)
            throws RegistryException;

    /**
     * Method to update a rating added to a resource.
     *
     * @param resourceImpl the resource.
     * @param rateID       the rate id.
     * @param rating       the rating.
     *
     * @throws RegistryException if some error occurs while updating the rating.
     */
    void updateRating(ResourceImpl resourceImpl, int rateID, int rating)
            throws RegistryException;

    /**
     * Method to get a id of a rating added to a given resource.
     *
     * @param resourceImpl the resource.
     * @param userID       the id of the user who added the rating.
     *
     * @return the rate id.
     * @throws RegistryException if an error occurs while getting the rate id.
     */
    int getRateID(ResourceImpl resourceImpl, String userID) throws RegistryException;

    /**
     * Method to get the average rating added to a given resource.
     *
     * @param resourceImpl the resource.
     *
     * @return the average rating.
     * @throws RegistryException if an error occurs while getting the average rating.
     */
    float getAverageRating(ResourceImpl resourceImpl) throws RegistryException;

    /**
     * Method to get a rating added by the given user to the given resource.
     *
     * @param resourceImpl the resource.
     * @param userID       the id of the user who added the rating.
     *
     * @return the rating data object.
     * @throws RegistryException if an error occurs while getting the rating.
     */
    RatingDO getRatingDO(ResourceImpl resourceImpl, String userID) throws RegistryException;

    /**
     * Method to get a rating added to a given resource.
     *
     * @param resourceImpl the resource.
     * @param userID       the id of the user who added the rating.
     *
     * @return the rating.
     * @throws RegistryException if an error occurs while getting the rating.
     */
    int getRating(ResourceImpl resourceImpl, String userID) throws RegistryException;

    /**
     * Method to remove all ratings added to a resource.
     *
     * @param resourceImpl the resource.
     *
     * @throws RegistryException if some error occurs while removing ratings.
     */
    void removeRatings(ResourceImpl resourceImpl) throws RegistryException;

    /**
     * Method to remove  rating added to a resource.
     *
     * @param resourceImpl the resource.
     *
     * @throws RegistryException if some error occurs while removing ratings.
     */
    void removeRating(ResourceImpl resourceImpl, int rateID) throws RegistryException;

    /**
     * Method to get ratings added by all users to the given resource.
     *
     * @param resourceImpl the resource.
     *
     * @return array of rating data objects.
     * @throws RegistryException if an error occurs while getting the rating.
     */
    RatingDO[] getResourceRatingDO(ResourceImpl resourceImpl) throws RegistryException;

    /**
     * Method to get users who rated the given resource.
     *
     * @param resourceImpl the resource.
     *
     * @return array of user names.
     * @throws RegistryException if an error occurs while getting the rating.
     */
    String[] getRatedUserNames(ResourceImpl resourceImpl) throws RegistryException;

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
     * Method to move ratings.
     *
     * @param source the source resource.
     * @param target the target resource.
     *
     * @throws RegistryException if some error occurs while moving ratings
     */
    void moveRatings(ResourceIDImpl source, ResourceIDImpl target) throws RegistryException;

    /**
     * Method to move rating paths. This function will move the paths from one path id to another
     * regardless of the resource name.
     *
     * @param source the source resource.
     * @param target the target resource.
     *
     * @throws RegistryException if some error occurs while moving rating paths
     */
    void moveRatingPaths(ResourceIDImpl source, ResourceIDImpl target)
            throws RegistryException;
    
    /**
     * Removes all ratings added to the given resource of a given version. 
     * This applies only to versioned resources. 
     *
     * @param version the version
     *
     * @throws RegistryException if an error occurred while removing tags.
     */
    void removeVersionRatings(long version)
            throws RegistryException;
}
