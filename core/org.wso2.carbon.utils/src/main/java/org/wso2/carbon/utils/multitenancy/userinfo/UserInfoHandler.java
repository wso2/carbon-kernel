/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.utils.multitenancy.userinfo;

/**
 * This API needs to be implemented if someone is willing to extend the behaviour of the
 * tenant to user mapping.
 */
public interface UserInfoHandler {

    /**
     * Need to return tenant aware username for the username provided
     * eg: if the actual username with tenant is: foo@email.com@bar, and user belongs to tenant: bar.com
     * tenant aware username: foo@email.com
     * @param username - Username
     * @return - Tenant aware username
     */
    String getTenantAwareUsername(String username);

    /**
     * Return the domain of the given user
     * @param username - UserName
     * @return - Tenant Domain
     */
    String getTenantDomain(String username);
}
