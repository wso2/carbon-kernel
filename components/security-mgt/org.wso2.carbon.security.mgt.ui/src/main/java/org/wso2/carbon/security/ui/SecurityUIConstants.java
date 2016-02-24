/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security.ui;

public class SecurityUIConstants {

    public static final int DEFAULT_ITEMS_PER_PAGE = 10;
    public static final int CACHING_PAGE_SIZE = 5;
    public static final String PAGINATED_KEY_STORE_DATA = "PaginatedKeyStoreData";
    public static final String STARTING_CERT_DATA_PAGE = "starting_page";

    public static final String USER_ADMIN_CLIENT = "UserAdminClient";
    public static final String FLAGGED_NAME_PAGE_COUNT = "FlaggedNamePageCount";

    public static final String ROLE_LIST_FILTER = "org.wso2.carbon.role.filter";
    public static final String USER_STORE_INFO = "org.wso2.carbon.userstore.info";
    public static final String ALL_DOMAINS = "ALL-USER-STORE-DOMAINS";
    public static final String DOMAIN_SEPARATOR = "/";
    public static final String ROLE_LIST_DOMAIN_FILTER = "org.wso2.carbon.role.domain.filter";

    public static final int KEYSTORE_DEFAULT_ITEMS_PER_PAGE = 5;
    public static final String SESSION_ATTR_KEYSTORES = "keystores";
    public static final String RE_FETCH_KEYSTORES = "refetchKeystores";

    public static final String KEYSTORE_LIST_FILTER = "org.wso2.carbon.keystore.filter";
    public static final String KEYSTORE_CERT_LIST_FILTER = "org.wso2.carbon.keystore.cert.filter";

    private SecurityUIConstants(){}
}
