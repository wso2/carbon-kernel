/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.api;

import org.wso2.carbon.privacy.Identifiable;
import org.wso2.carbon.privacy.annotation.Confidential;
import org.wso2.carbon.privacy.annotation.Pseudonym;

/**
 * Represents an User in user store.
 */
@Confidential
public interface User extends Identifiable {

    /**
     * Set the username of this user.
     * @param name Username of the this user.
     */
    void setUserName(String name);

    /**
     * Set the userId of this user. This will be used as a pseudonym for the username.
     * @param id User id of this user.
     */
    void setId(String id);

    @Override
    @Pseudonym
    String getId();

    /**
     * Get the user's user name.
     * @return Username of this user.
     */
    String getUsername();

    /**
     * Set tenant id for this user.
     * @param id Tenant id.
     */
    void setTenantId(int id);

    /**
     * Get the tenant id of this user.
     * @return tenant id of the user.
     */
    int getTenantId();

    /**
     * Set the domain name of this user.
     * @param domainName Domain name.
     */
    void setDomainName(String domainName);

    /**
     * Get the domain name of this user.
     * @return Domain name.
     */
    String getDomainName();
}
