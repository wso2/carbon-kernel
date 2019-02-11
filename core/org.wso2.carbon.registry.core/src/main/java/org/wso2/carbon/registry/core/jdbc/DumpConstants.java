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
package org.wso2.carbon.registry.core.jdbc;

/**
 * Class to keep the constants used in dumping
 */
public class DumpConstants {

    /**
     * Element name of the resource dump
     */
    public static final String RESOURCE = "resource";

    /**
     * Attribute key of resource name
     */
    public static final String RESOURCE_NAME = "name";

    /**
     * Attribute key of the ignore conflicts
     */
    public static final String IGNORE_CONFLICTS = "ignoreConflicts";
    
    /**
     * Attribute key of resource path, keep to support backward compatibility in restoring
     */
    public static final String RESOURCE_PATH = "path";

    /**
     * Attribute name to keep whether the resource is collection or not
     */
    public static final String RESOURCE_IS_COLLECTION = "isCollection";

    /**
     * Attribute value to represent the resource is a collection
     */
    public static final String RESOURCE_IS_COLLECTION_TRUE = "true"; 

    /**
     * Attribute value to represent the resource is not a collection
     */
    public static final String RESOURCE_IS_COLLECTION_FALSE = "false";

    /**
     * Attribute name to keep whether the resource added/updated/deleted
     */
    public static final String RESOURCE_STATUS = "status";

    /**
     * Attribute value to keep whether the resource updated
     */
    public static final String RESOURCE_UPDATED = "updated";

    /**
     * Attribute value to keep whether the resource added
     */
    public static final String RESOURCE_ADDED = "added";

    /**
     * Attribute value to keep whether the resource deleted
     */
    public static final String RESOURCE_DELETED = "deleted";

    /**
     * Attribute value to keep whether the resource is a dump or not
     */
    public static final String RESOURCE_DUMP = "dump";

    /**
     * Element name of the media type
     */
    public static final String MEDIA_TYPE = "mediaType";

    /**
     * Element name of the version
     */
    public static final String VERSION = "version";

    /**
     * Element name of the creator
     */
    public static final String CREATOR = "creator";

    /**
     * Element name of the created time
     */
    public static final String CREATED_TIME = "createdTime";

    /**
     * Element name of the last updater
     */
    public static final String LAST_UPDATER = "lastUpdater";

    /**
     * Element name of the last modified
     */
    public static final String LAST_MODIFIED = "lastModified";

    /**
     * Element name of the description
     */
    public static final String DESCRIPTION = "description";

    /**
     * Element name for uuid
     */
    public static final String UUID = "uuid";

    /**
     * Element name of the properties
     */
    public static final String PROPERTIES = "properties";

    /**
     * Element name of the property entry
     */
    public static final String PROPERTY_ENTRY = "property";

    /**
     * Attribute name of the property entry key
     */
    public static final String PROPERTY_ENTRY_KEY = "key";

    /**
     * Element name of the content
     */
    public static final String CONTENT = "content";

    /**
     * Element name of the comments
     */
    public static final String COMMENTS = "comments";

    /**
     * Element name of the comment entry
     */
    public static final String COMMENT_ENTRY = "comment";

    /**
     * Element name of the comment entry user
     */
    public static final String COMMENT_ENTRY_USER = "user";

    /**
     * Element name of the comment entry text
     */
    public static final String COMMENT_ENTRY_TEXT = "text";

    /**
     * Element name of the taggings
     */
    public static final String TAGGINGS = "taggings";

    /**
     * Element name of the tagging entry
     */
    public static final String TAGGING_ENTRY = "tagging";

    /**
     * Element name of the tagging entry user
     */
    public static final String TAGGING_ENTRY_USER = "user";

    /**
     * Element name of the tagging entry date
     */
    public static final String TAGGING_ENTRY_DATE = "date";

    /**
     * Element name of the tagging entry tag name
     */
    public static final String TAGGING_ENTRY_TAG_NAME = "tagName";

    /**
     * Element name of the ratings
     */
    public static final String RATINGS = "ratings";

    /**
     * Element name of the rating entry
     */
    public static final String RATING_ENTRY = "rating";

    /**
     * Element name of the rating entry user
     */
    public static final String RATING_ENTRY_USER = "user";

    /**
     * Element name of the rating entry date
     */
    public static final String RATING_ENTRY_DATE = "date";

    /**
     * Element name of the rating entry rate
     */
    public static final String RATING_ENTRY_RATE = "rate";

    /**
     * Element name of the associations
     */
    public static final String ASSOCIATIONS = "associations";

    /**
     * Element name of the association entry
     */
    public static final String ASSOCIATION_ENTRY = "association";

    /**
     * Element name of the association entry source
     */
    public static final String ASSOCIATION_ENTRY_SOURCE = "source";

    /**
     * Element name of the association entry destination
     */
    public static final String ASSOCIATION_ENTRY_DESTINATION = "destination";

    /**
     * Element name of the association entry type
     */
    public static final String ASSOCIATION_ENTRY_TYPE = "type";

    /**
     * Prefix for external association destinations
     */
    public static final String EXTERNAL_ASSOCIATION_DESTINATION_PREFIX = ":";

    /**
     * Element name for children
     */
    public static final String CHILDREN = "children";

    /**
     * Element name for childs, keep to support backward compatibility in restoring
     */
    @Deprecated
    public static final String CHILDS = "childs";
}
