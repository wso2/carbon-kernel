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
 * This class contains a set of constants used when interacting with databases
 * and data access.
 */
public class DatabaseConstants {

	/**
	 * Represents the version field.
	 */
	public static final String VERSION_FIELD = "REG_VERSION";
	/**
	 * Represents the name field.
	 */
	public static final String NAME_FIELD = "REG_NAME";
	/**
	 * Represents the id field.
	 */
	public static final String ID_FIELD = "REG_ID";
	/**
	 * Represents the path field.
	 */
	public static final String PATH_FIELD = "REG_PATH";
	/**
	 * Represents the path identifier field.
	 */
	public static final String PATH_ID_FIELD = "REG_PATH_ID";
	/**
	 * Represents the media type field.
	 */
	public static final String MEDIA_TYPE_FIELD = "REG_MEDIA_TYPE";
	/**
	 * Represents the creator field.
	 */
	public static final String CREATOR_FIELD = "REG_CREATOR";
	/**
	 * Represents the created time field.
	 */
	public static final String CREATED_TIME_FIELD = "REG_CREATED_TIME";
	/**
	 * Represents the description field.
	 */
	public static final String DESCRIPTION_FIELD = "REG_DESCRIPTION";
	/**
	 * Represents the last updater field.
	 */
	public static final String LAST_UPDATER_FIELD = "REG_LAST_UPDATOR";
	/**
	 * Represents the last updated time field.
	 */
	public static final String LAST_UPDATED_TIME_FIELD = "REG_LAST_UPDATED_TIME";
	/**
	 * Represents the log count field.
	 */
	public static final String LOG_COUNT_FIELD = "REG_LOG_COUNT";
	/**
	 * Represents the content id field.
	 */
	public static final String CONTENT_ID_FIELD = "REG_CONTENT_ID";
	/**
	 * Represents the resource name field.
	 */
	public static final String RESOURCE_NAME_FIELD = "REG_RESOURCE_NAME";
    /**
     * Represents the resource UUID field.
     */
    public static final String UUID_FIELD = "REG_UUID";
	/**
	 * Represents the property identifier field.
	 */
	public static final String PROPERTY_ID_FIELD = "REG_PROPERTY_ID";
	/**
	 * Represents the snapshot identifier field.
	 */
	public static final String SNAPSHOT_ID_FIELD = "REG_SNAPSHOT_ID";
	/**
	 * Represents the resource VIDs field.
	 */
	public static final String RESOURCE_VIDS_FIELD = "REG_RESOURCE_VIDS";
	/**
	 * Represents the child resource id field.
	 */
	public static final String CHILD_RID_FIELD = "REG_CHILD_RID";
	/**
	 * Represents the path's parent's id field.
	 */
	public static final String PATH_PARENT_ID_FIELD = "REG_PATH_PARENT_ID";
	/**
	 * Represents the value field.
	 */
	public static final String VALUE_FIELD = "REG_VALUE";
	/**
	 * Represents the path value field.
	 */
	public static final String PATH_VALUE_FIELD = "REG_PATH_VALUE";
	/**
	 * Represents the content data field.
	 */
	public static final String CONTENT_DATA_FIELD = "REG_CONTENT_DATA";
	/**
	 * Represents the resource count field.
	 */
	public static final String RES_COUNT_FIELD = "REG_RES_COUNT";

	// //////////////////////////////////////////////////////
	// Field names of Associations table
	// //////////////////////////////////////////////////////

	/**
	 * Represents the source path field.
	 */
	public static final String SOURCEPATH_FIELD = "REG_SOURCEPATH";
	/**
	 * Represents the target field.
	 */
	public static final String TARGETPATH_FIELD = "REG_TARGETPATH";
	/**
	 * Represents the association type field.
	 */
	public static final String ASSOCIATION_TYPE_FIELD = "REG_ASSOCIATION_TYPE";

	// //////////////////////////////////////////////////////
	// Field names of Tags table
	// //////////////////////////////////////////////////////

	/**
	 * Represents the tag name field.
	 */
	public static final String TAG_NAME_FIELD = "REG_TAG_NAME";
	/**
	 * Represents the tagged time field.
	 */
	public static final String TAGGED_TIME_FIELD = "REG_TAGGED_TIME";
	/**
	 * Represents the tag identifier field.
	 */
	public static final String TAGGING_ID_FIELD = "REG_TAG_ID";

	// //////////////////////////////////////////////////////
	// Field names of Comments table
	// //////////////////////////////////////////////////////

	/**
	 * Represents the comment identifier field.
	 */
	public static final String COMMENT_ID_FIELD = "REG_COMMENT_ID";
	/**
	 * Represents the comment text field.
	 */
	public static final String COMMENT_TEXT_FIELD = "REG_COMMENT_TEXT";

	/**
	 * Represents the commented time field.
	 */
	public static final String COMMENTED_TIME_FIELD = "REG_COMMENTED_TIME";

	// //////////////////////////////////////////////////////
	// Field names of Logs table
	// //////////////////////////////////////////////////////

	/**
	 * Represents the logged time field.
	 */
	public static final String LOGGED_TIME_FIELD = "REG_LOGGED_TIME";
	/**
	 * Represents the action field.
	 */
	public static final String ACTION_FIELD = "REG_ACTION";
	/**
	 * Represents the action data field.
	 */
	public static final String ACTION_DATA_FIELD = "REG_ACTION_DATA";

	// //////////////////////////////////////////////////////
	// Field names of Ratings table
	// //////////////////////////////////////////////////////

	/**
	 * Represents the rating identifier field.
	 */
	public static final String RATING_ID_FIELD = "REG_RATING_ID";
	/**
	 * Represents the rated time field.
	 */
	public static final String RATED_TIME_FIELD = "REG_RATED_TIME";
	/**
	 * Represents the rating field.
	 */
	public static final String RATING_FIELD = "REG_RATING";

	/**
	 * Represents the user identifier field.
	 */
	public static final String USER_ID_FIELD = "REG_USER_ID";

	/**
	 * Represents the default max active field.
	 */
	public static final int DEFAULT_MAX_ACTIVE = 40;
	/**
	 * Represents the default max wait field.
	 */
	public static final int DEFAULT_MAX_WAIT = 1000 * 60;
	/**
	 * Represents the default min idle field.
	 */
	public static final int DEFAULT_MIN_IDLE = 5;

    /**
     * Fields for summarized resource tag cloud
     */
    public static final String TAG_NAME = "TAG_NAME";
    public static final String USED_COUNT = "USED_COUNT";
    /**
     * used to bypass ChrootWrapper -> filterSearchResult. A valid registry path is a must for executeQuery results
     * to be passed to client side
     */
    public static final String MOCK_PATH = "MOCK_PATH";
}
