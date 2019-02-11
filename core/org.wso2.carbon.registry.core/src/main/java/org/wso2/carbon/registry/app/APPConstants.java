/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.app;

import javax.xml.namespace.QName;

/**
 * Constants used in the AtomPub Protocol based remote registry.
 */
public class APPConstants {

    private APPConstants() {
    }

    /**
     * The context for the resource servlet.
     */
    public static final String RESOURCE = "/resource";

    /**
     * The context for the atom servlet.
     */
    public static final String ATOM = "/atom";

    /**
     * The registry namespace.
     */
    public static final String NAMESPACE = "http://wso2.org/registry";

    /**
     * The registry namespace media type
     */
    public static final String NAMESPACE_MEDIA_TYPE = "mediaType";

     /**
     * The registry namespace state
     */
    public static final String NAMESPACE_STATE = "state";

    /**
     * The default media type for imports.
     */
    public static final String IMPORT_MEDIA_TYPE = "application/resource-import";

    /**
     * Constant used to identify aspects.
     */
    public static final String ASPECTS = "aspects";

    /**
     * Constant used to identify aspect.
     */
    public static final String ASPECT = "aspect";

    /**
     * Constant used to identify check points.
     */
    public static final String CHECKPOINT = "checkpoint";

    /**
     * Constant used to identify associations.
     */
    public static final String ASSOCIATIONS = "associations";

    /**
     * Constant used to identify association types.
     */
    public static final String ASSOC_TYPE = "type";

    /**
     * Parameters used to identify split path.
     */
    public static final String PARAMETER_SPLIT_PATH = "splitPath";

    /**
     * Parameters used to identify path.
     */
    public static final String PARAMETER_PATH = "path";

    /**
     * Parameters used to identify version operations.
     */
    public static final String PARAMETER_VERSION = "versions";

    /**
     * Parameters used to identify restore operations.
     */
    public static final String PARAMETER_RESTORE = "restore";

    /**
     * Parameters used to identify rename operations.
     */
    public static final String PARAMETER_RENAME = "rename";

    /**
     * Parameters used to identify move operations.
     */
    public static final String PARAMETER_MOVE = "move";

    /**
     * Parameters used to identify copy operations.
     */
    public static final String PARAMETER_COPY = "copy";

    /**
     * Parameters used to identify tagging operations.
     */
    public static final String PARAMETER_TAGS = "tags";

    /**
     * Parameters used to identify rating operations.
     */
    public static final String PARAMETER_RATINGS = "ratings";

    /**
     * Parameters used to identify commenting operations.
     */
    public static final String PARAMETER_COMMENTS = "comments";

    /**
     * Parameters used to identify log operations.
     */
    public static final String PARAMETER_LOGS = "logs";

    /**
     * Parameters used to identify query operations.
     */
    public static final String PARAMETER_QUERY = "query";

    /**
     * Parameters used to identify dump operations.
     */
    public static final String PARAMETER_DUMP = "dump";

    /**
     * Qualified name used to identify associations.
     */
    public static final QName QN_ASSOC = new QName(NAMESPACE, "association");

    /**
     * Qualified name used to identify the user who did the last update.
     */
    public static final QName QN_LAST_UPDATER = new QName(NAMESPACE, "lastUpdatedUser");

    /**
     * Qualified name used to identify child count.
     */
    public static final QName QN_CHILD_COUNT = new QName(NAMESPACE, "childCount");

    /**
     * Qualified name used to identify media type.
     */
    public static final QName QN_MEDIA_TYPE = new QName(NAMESPACE, "mediaType");

    /**
     * Qualified name used to identify uuid.
     */
    public static final QName QN_UUID_TYPE = new QName(NAMESPACE, "uuid");

    /**
     * Qualified name used to identify snapshot id.
     */
    public static final QName QN_SNAPSHOT_ID = new QName(NAMESPACE, "snapshotID");

    /**
     * Qualified name used to identify average ratings.
     */
    public static final QName QN_AVERAGE_RATING = new QName(NAMESPACE, "AverageRating", "wso2");

    /**
     * Qualified name used to identify comments.
     */
    public static final QName QN_COMMENTS = new QName(NAMESPACE, "isComments");


    ////////////////////////////////////////////////////////
    // HTTP Methods                                       //
    ////////////////////////////////////////////////////////

    /**
     * Constant used to identify HTTP Method POST.
     */
    public static final String HTTP_POST = "POST";

    /**
     * Constant used to identify HTTP Method GET.
     */
    public static final String HTTP_GET = "GET";

    /**
     * Constant used to identify HTTP Method PUT.
     */
    public static final String HTTP_PUT = "PUT";

    /**
     * Constant used to identify HTTP Method HEAD.
     */
    public static final String HTTP_HEAD = "HEAD";

    /**
     * Constant used to identify HTTP Method DELETE.
     */
    public static final String HTTP_DELETE = "DELETE";

    /**
     * Constant used to identify HTTP Method TRACE.
     */
    public static final String HTTP_TRACE = "TRACE";

    /**
     * Constant used to identify HTTP Method CONNECT.
     */
    public static final String HTTP_CONNECT = "CONNECT";

}
