/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.core.util;

import org.wso2.carbon.core.internal.permission.update.PermissionUpdater;

/**
 * This class is used to expose the functionality of updating the in memory permission tree
 * to the external bundles. This wraps the functionality of
 * org.wso2.carbon.core.internal.permission.update.PermissionUpdater class and expose it as utility
 * method.
 */

public class PermissionUpdateUtil {

    /**
     * Holds the org.wso2.carbon.core.internal.permission.update.PermissionUpdater instance
     */
    private static PermissionUpdater permissionUpdater = new PermissionUpdater();

    private PermissionUpdateUtil(){

    }

    /**
     * Update the in-memory permission tree for the given tenant
     * @param tenantId  ID of the tenant of which the permission tree should be updated.
     */
    public static void updatePermissionTree(int tenantId){
        permissionUpdater.update(tenantId);
    }
}
