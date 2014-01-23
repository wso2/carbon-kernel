/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.dataaccess;

import org.wso2.carbon.registry.core.dao.*;

/**
 * This describes a an instance of a data access object manager class that can be used to obtain
 * access to the object representations of various data stored on the back-end database.
 */
public interface DAOManager {

    /**
     * Method to obtain an instance of an association data access object.
     *
     * @return instance of the association data access object implementation.
     */
    AssociationDAO getAssociationDAO();

    /**
     * Method to obtain an instance of a comments data access object.
     *
     * @param isVersioned whether the returned data access object should handle versioned data or
     *                    non-versioned data. Versioning can be enabled by setting a parameter on
     *                    the XML-based registry configuration.
     *
     * @return instance of the comments data access object implementation. If versioning has been
     *         enabled, it is mandatory to request for a versioned data access object. And, the
     *         returned data access object is only capable of handling versioned resources. Also, if
     *         versioning has been disabled, a non-versioned data access object must be requested.
     */
    CommentsDAO getCommentsDAO(boolean isVersioned);

    /**
     * Method to obtain an instance of a ratings data access object.
     *
     * @param isVersioned whether the returned data access object should handle versioned data or
     *                    non-versioned data. Versioning can be enabled by setting a parameter on
     *                    the XML-based registry configuration.
     *
     * @return instance of the ratings data access object implementation. If versioning has been
     *         enabled, it is mandatory to request for a versioned data access object. And, the
     *         returned data access object is only capable of handling versioned resources. Also, if
     *         versioning has been disabled, a non-versioned data access object must be requested.
     */
    RatingsDAO getRatingsDAO(boolean isVersioned);

    /**
     * Method to obtain an instance of a tags data access object.
     *
     * @param isVersioned whether the returned data access object should handle versioned data or
     *                    non-versioned data. Versioning can be enabled by setting a parameter on
     *                    the XML-based registry configuration.
     *
     * @return instance of the tags data access object implementation. If versioning has been
     *         enabled, it is mandatory to request for a versioned data access object. And, the
     *         returned data access object is only capable of handling versioned resources. Also, if
     *         versioning has been disabled, a non-versioned data access object must be requested.
     */
    TagsDAO getTagsDAO(boolean isVersioned);

    /**
     * Method to obtain an instance of a logs data access object. This can be used to access audit
     * logs stored on a database, that are related to the registry operations performed.
     *
     * @return instance of the logs data access object implementation.
     */
    LogsDAO getLogsDAO();

    /**
     * Method to obtain an instance of a resource data access object.
     *
     * @return instance of the resource data access object implementation. The returned
     *         data access object can only handle non-versioned resources, and should only be used
     *         when versioning has been disabled. If versioning has been enabled, use the
     *         {@link #getResourceVersionDAO()} method. Automatic versioning of resources can be
     *         disabled by setting a parameter on the XML-based registry configuration.
     */
    ResourceDAO getResourceDAO();

    /**
     * Method to obtain an instance of a versioned-resource data access object.
     *
     * @return instance of the versioned-resource data access object implementation. The returned
     *         data access object can only handle versioned resources, and should only be used when
     *         versioning has been enabled. If versioning has not been enabled, use the
     *         {@link #getResourceDAO()} method. Automatic versioning of resources can be enabled
     *         by setting a parameter on the XML-based registry configuration.
     */
    ResourceVersionDAO getResourceVersionDAO();
}
