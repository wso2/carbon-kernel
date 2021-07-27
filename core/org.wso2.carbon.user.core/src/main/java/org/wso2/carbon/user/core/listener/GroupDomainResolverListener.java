/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org).
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

package org.wso2.carbon.user.core.listener;

import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.Group;

/**
 * Service interface of the listener to resolve the group domain.
 */
public interface GroupDomainResolverListener {

    /**
     * To check whether particular listener is enabled.
     *
     * @return true if particular listener is enabled.
     */
    boolean isEnable();

    /**
     * Get the execution order identifier for this listener.
     *
     * @return The execution order identifier integer value.
     */
    int getExecutionOrderId();

    /**
     * Defines additional actions when resolving thee domain name of a group.
     *
     * @param group    Group that the domain needs to be resolved.
     * @param tenantId Tenant id.
     * @return If the method execution was successful.
     * @throws UserStoreException If an error occurred.
     */
    boolean preResolveGroupDomainByGroupId(Group group, int tenantId)
            throws UserStoreException;
}
