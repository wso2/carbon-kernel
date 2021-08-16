/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.user.core.common;

import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.listener.GroupResolver;
import org.wso2.carbon.user.core.model.Condition;

import java.util.List;

/**
 * Abstract implementation of GroupResolver.
 */
public class AbstractGroupResolver implements GroupResolver {

    @Override
    public boolean isEnable() {

        return false;
    }

    @Override
    public int getExecutionOrderId() {

        return 0;
    }

    @Override
    public boolean resolveGroupDomainByGroupId(Group group, int tenantId) throws UserStoreException {

        return true;
    }

    @Override
    public boolean getGroupsListOfUserByUserId(String userId, List<Group> groupList, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean getGroupIdByName(String groupName, Group group, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean getGroupNameById(String groupID, Group group, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean getGroupById(String groupID, List<String> requestedClaims, Group group,
                                UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean getGroupByName(String groupName, List<String> requestedClaims, Group group,
                                  UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean listGroups(Condition condition, int limit, int offset, String sortBy, String domain,
                              String sortOrder, List<Group> groupsList, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }
}
