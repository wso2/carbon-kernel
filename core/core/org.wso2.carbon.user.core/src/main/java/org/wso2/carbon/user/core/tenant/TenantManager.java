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
package org.wso2.carbon.user.core.tenant;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.user.core.UserStoreException;

public interface TenantManager extends org.wso2.carbon.user.api.TenantManager {

    String getSuperTenantDomain() throws UserStoreException;

    void setBundleContext(BundleContext bundleContext);

    /**
     * In some tenant management scenarios, for example: TM with embedded-ldap, we need to
     * initialize existing tenant partitions at the new start of the server.
     * This method will handle that.
     */
    void initializeExistingPartitions();


}