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
import org.wso2.carbon.user.core.listener.GroupOperationEventListener;
import org.wso2.carbon.user.core.model.Condition;

import java.util.List;

/**
 * Abstract implementation of extension point to implement various additional operations before and after
 * actual group operation is done.
 */
public class AbstractGroupOperationEventListener implements GroupOperationEventListener {

    @Override
    public int getExecutionOrderId() {

        return 0;
    }

    @Override
    public boolean preGetGroupById(String groupID, List<String> requestedClaims, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean preGetGroupNameById(String groupID, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean preGetGroupByName(String groupName, List<String> requestedClaims, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean preGetGroupIdByName(String groupName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean preListGroups(Condition condition, int limit, int offset, String domain, String sortBy,
                                 String sortOrder, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean preGetGroupsListOfUserByUserId(String userId, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean postGetGroupsListOfUserByUserId(String userId, List<Group> groupList,
                                                   UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean postGetGroupById(String groupID, List<String> requestedClaims, Group group,
                                    UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean postGetGroupNameById(String groupID, Group group, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean postGetGroupByName(String groupName, List<String> requestedClaims, Group group,
                                      UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean postGetGroupIdByName(String groupName, Group group, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean postListGroups(Condition condition, int limit, int offset, String domain, String sortBy,
                                  String sortOrder, List<Group> groupsList, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean preAddGroup(String groupName, List<String> userIds, List<Claim> claims,
                               UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean postAddGroup(String groupName, String groupId, List<String> userIds, List<Claim> claims,
                                UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean preDeleteGroup(String groupId, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean postDeleteGroup(String groupId, String groupName, UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean preRenameGroup(String groupId, String newGroupName, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean postRenameGroup(String groupId, String newGroupName, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean preUpdateUserListOfGroup(String groupId, List<String> deletedUserIds, List<String> newUserIds,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean postUpdateUserListOfGroup(String groupId, List<String> deletedUserIds, List<String> newUserIds,
                                             UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }
}
