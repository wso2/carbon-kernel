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
import org.wso2.carbon.user.core.listener.GroupManagementErrorEventListener;
import org.wso2.carbon.user.core.model.Condition;

import java.util.List;

/**
 * Abstract implementation of GroupManagementErrorEventListener.
 */
public class AbstractGroupManagementErrorEventListener implements GroupManagementErrorEventListener {

    @Override
    public boolean isEnable() {

        return false;
    }

    @Override
    public int getExecutionOrderId() {

        return 0;
    }

    @Override
    public boolean onPreGetGroupByIdFailure(String errorCode, String errorMessage, String groupID,
                                            List<String> requiredAttributes, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean onPreGetGroupNameByIdFailure(String errorCode, String errorMessage, String groupID,
                                                UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onPreGetGroupsListByUserIdFailure(String errorCode, String errorMessage, String userId,
                                                     UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onPreGetGroupByNameFailure(String errorCode, String errorMessage, String groupName,
                                              List<String> requiredAttributes, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean onPreGetGroupIdByNameFailure(String errorCode, String errorMessage, String groupName,
                                                UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onPreListGroupsFailure(String errorCode, String errorMessage, Condition condition, int limit,
                                          int offset, String domain, String sortBy, String sortOrder,
                                          UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onPostGetGroupsListByUserIdFailure(String errorCode, String errorMessage, String userId,
                                                      UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onPostGetGroupByIdFailure(String errorCode, String errorMessage, String groupID,
                                             List<String> requiredAttributes, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean onPostGetGroupNameByIdFailure(String errorCode, String errorMessage, String groupID,
                                                 UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onPostGetGroupByNameFailure(String errorCode, String errorMessage, String groupName,
                                               List<String> requiredAttributes, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean onPostGetGroupIdByNameFailure(String errorCode, String errorMessage, String groupName,
                                                 UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onPostListGroupsFailure(String errorCode, String errorMessage, Condition condition, int limit,
                                           int offset, String domain, String sortBy, String sortOrder,
                                           UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onGetGroupByIdFailure(String errorCode, String errorMessage, String groupID,
                                         List<String> requiredAttributes, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean onGetGroupNameByIdFailure(String errorCode, String errorMessage, String groupID,
                                             UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onGetGroupByNameFailure(String errorCode, String errorMessage, String groupName,
                                           List<String> requiredAttributes, UserStoreManager userStoreManager)
            throws UserStoreException {

        return true;
    }

    @Override
    public boolean onGetGroupIdByNameFailure(String errorCode, String errorMessage, String groupName,
                                             UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onGetGroupsListByUserIdFailure(String errorCode, String errorMessage, String userId,
                                                  UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }

    @Override
    public boolean onListGroupsFailure(String errorCode, String errorMessage, Condition condition, int limit,
                                       int offset, String domain, String sortBy, String sortOrder,
                                       UserStoreManager userStoreManager) throws UserStoreException {

        return true;
    }
}
