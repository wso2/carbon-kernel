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

import org.wso2.carbon.registry.api.Activity;

/**
 * Representation of a log entry. Log entry is a record of a single action performed on the
 * registry.
 */
public class LogEntry extends Activity {

    ////////////////////////////////////////////////////////
    // Filters for log queries - these represent the
    // possible actions that get logged.
    ////////////////////////////////////////////////////////
    
    /**
     * The log action to filter with. All is for don't filter at all.
     */
    @SuppressWarnings("unused")
    public static final int ALL = Activity.ALL;

    /**
     * Filter value for the resource adding action.
     */
    public static final int ADD = Activity.ADD;

    /**
     * Filter value for the resource updating action.
     */
    public static final int UPDATE = Activity.UPDATE;

    /**
     * Filter value for the resource comment action.
     */
    public static final int COMMENT = Activity.COMMENT;

    /**
     * Filter value for the action of deleting a comment.
     */
    public static final int DELETE_COMMENT = Activity.DELETE_COMMENT;

    /**
     * Filter value for the resource tagging action.
     */
    public static final int TAG = Activity.TAG;

    /**
     * Filter value for the action of removing a tag.
     */
    public static final int REMOVE_TAG = Activity.REMOVE_TAG;

    /**
     * Filter value for the resource rating action.
     */
    public static final int RATING = Activity.RATING;

    /**
     * Filter value for the resource deleting action.
     */
    public static final int DELETE_RESOURCE = Activity.DELETE_RESOURCE;

    /**
     * Filter value for the resource restoring action.
     */
    public static final int RESTORE = Activity.RESTORE;

    /**
     * Filter value for the resource renaming action.
     */
    public static final int RENAME = Activity.RENAME;

    /**
     * Filter value for the resource moving action.
     */
    public static final int MOVE = Activity.MOVE;

    /**
     * Filter value for the resource copying action.
     */
    public static final int COPY = Activity.COPY;

    /**
     * Filter value for the creating remote link action.
     */
    public static final int CREATE_REMOTE_LINK =
            Activity.CREATE_REMOTE_LINK;

    /**
     * Filter value for the creating symbolic link action.
     */
    public static final int CREATE_SYMBOLIC_LINK =
            Activity.CREATE_SYMBOLIC_LINK;

    /**
     * Filter value for the removing link action.
     */
    public static final int REMOVE_LINK = Activity.REMOVE_LINK;

    /**
     * Filter value for the adding association action.
     */
    public static final int ADD_ASSOCIATION = Activity.ADD_ASSOCIATION;

    /**
     * Filter value for the removing association action.
     */
    public static final int REMOVE_ASSOCIATION =
            Activity.REMOVE_ASSOCIATION;

    /**
     * Filter value for the associating an aspect action.
     */
    public static final int ASSOCIATE_ASPECT =
            Activity.ASSOCIATE_ASPECT;
}
