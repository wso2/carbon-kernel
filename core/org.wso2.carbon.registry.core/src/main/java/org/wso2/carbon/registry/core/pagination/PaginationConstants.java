/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core.pagination;

/**
 * This class contains the constant that are used for PaginationContext.
 */
public class PaginationConstants {

    public static final String PAGINATION_HEADER_ELEMENT_NAMESPACE = "http://www.wso2.org/ws/pagination";

    public static final String PAGINATION_HEADER_CONTEXT_START = "start";

    public static final String PAGINATION_HEADER_CONTEXT_COUNT = "count";

    public static final String PAGINATION_HEADER_CONTEXT_SORT_ORDER = "sortOrder";

    public static final String PAGINATION_HEADER_CONTEXT_SORT_BY = "sortBy";

    public static final String PAGINATION_HEADER_CONTEXT_LIMIT = "limit";

    public static final String PAGINATION_HEADER_CONTEXT_ROW_COUNT = "rowCount";

    public static final String PAGINATION_HEADER_ELEMENT_NAMESPACE_PREFIX = "cns";

    public static final String ENABLE_API_PAGINATE = System.getProperty("enable.registry.api.paginating");

}
