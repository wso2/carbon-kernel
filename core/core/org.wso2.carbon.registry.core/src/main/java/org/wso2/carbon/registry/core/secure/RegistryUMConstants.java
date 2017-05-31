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

package org.wso2.carbon.registry.core.secure;

@Deprecated
public class RegistryUMConstants {

    /**
     * Servlet init parameters to configure the user manager of the registry.
     */
    public static final String UM_DATASOURCE_NAME = "UserManagerDataSource";
    public static final String UM_DATABASE_CONNECTION_URL = "UserManagerDBConnectionURL";
    public static final String UM_DATABASE_DRIVER_NAME = "UserManagerDBDriverName";
    public static final String UM_DATABASE_USER_NAME = "UserManagerDBUserName";
    public static final String UM_DATABASE_PASSWORD = "UserManagerDBPassword";

    public static final String HSQL_DB_URL = "jdbc:hsqldb:mem:umdb";

    // servlet init paramter names
    public static final String SQL_FILE_PATH_PARAM = "sqlFilePath";
    public static final String DB_URL_PARAM = "dbURL";
    public static final String DB_DRIVER_PARAM = "dbDriver";
}
