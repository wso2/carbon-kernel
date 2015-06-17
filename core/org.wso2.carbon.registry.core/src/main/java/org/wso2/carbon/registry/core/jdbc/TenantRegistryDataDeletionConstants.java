/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.registry.core.jdbc;

public class TenantRegistryDataDeletionConstants {

    public static final String DELETE_CLUSTER_LOCK_SQL = "DELETE FROM REG_CLUSTER_LOCK WHERE REG_TENANT_ID = ?";

    public static final String DELETE_LOG_SQL = "DELETE FROM REG_LOG WHERE REG_TENANT_ID = ?";

    public static final String DELETE_ASSOCIATION_SQL = "DELETE FROM REG_ASSOCIATION WHERE REG_TENANT_ID = ?";

    public static final String DELETE_SNAPSHOT_SQL = "DELETE FROM REG_SNAPSHOT WHERE REG_TENANT_ID = ?";

    public static final String DELETE_RESOURCE_COMMENT_SQL = "DELETE FROM REG_RESOURCE_COMMENT WHERE REG_TENANT_ID = ?";

    public static final String DELETE_COMMENT_SQL = "DELETE FROM REG_COMMENT WHERE REG_TENANT_ID = ?";

    public static final String DELETE_RESOURCE_RATING_SQL = "DELETE FROM REG_RESOURCE_RATING WHERE REG_TENANT_ID = ?";

    public static final String DELETE_RATING_SQL = "DELETE FROM REG_RATING WHERE REG_TENANT_ID = ?";

    public static final String DELETE_RESOURCE_TAG_SQL = "DELETE FROM REG_RESOURCE_TAG WHERE REG_TENANT_ID = ?";

    public static final String DELETE_TAG_SQL = "DELETE FROM REG_TAG WHERE REG_TENANT_ID = ?";

    public static final String DELETE_RESOURCE_PROPERTY_SQL = "DELETE FROM REG_RESOURCE_PROPERTY WHERE REG_TENANT_ID = ?";

    public static final String DELETE_PROPERTY_SQL = "DELETE FROM REG_PROPERTY WHERE REG_TENANT_ID = ?";

    public static final String DELETE_RESOURCE_HISTORY_SQL = "DELETE FROM REG_RESOURCE_HISTORY WHERE REG_TENANT_ID = ?";

    public static final String DELETE_CONTENT_HISTORY_SQL = "DELETE FROM REG_CONTENT_HISTORY WHERE REG_TENANT_ID = ?";

    public static final String DELETE_RESOURCE_SQL = "DELETE FROM REG_RESOURCE WHERE REG_TENANT_ID = ?";

    public static final String DELETE_CONTENT_SQL = "DELETE FROM REG_CONTENT WHERE REG_TENANT_ID = ?";

    public static final String DELETE_PATH_SQL = "DELETE FROM REG_PATH WHERE REG_TENANT_ID = ?";

}
