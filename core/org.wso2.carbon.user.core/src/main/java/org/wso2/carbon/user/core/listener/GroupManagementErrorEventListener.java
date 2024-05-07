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

import org.wso2.carbon.user.core.NotImplementedException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.Claim;
import org.wso2.carbon.user.core.model.Condition;

import java.util.List;

/**
 * This allows an extension point to implement various additional operations when there is a failure in any of the
 * group management operations.
 */
public interface GroupManagementErrorEventListener {

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
     * Defines any additional actions that need to be done when there is a failure before getting the group by id.
     *
     * @param errorCode          Error code.
     * @param errorMessage       Error message.
     * @param groupID            Group id.
     * @param requiredAttributes Requested attributes.
     * @param userStoreManager   Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onPreGetGroupByIdFailure(String errorCode, String errorMessage, String groupID,
                                     List<String> requiredAttributes, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure before getting the group name by id.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupID          Group id.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onPreGetGroupNameByIdFailure(String errorCode, String errorMessage, String groupID,
                                         UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure before getting the groups list by
     * user id.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param userId           User id.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onPreGetGroupsListByUserIdFailure(String errorCode, String errorMessage, String userId,
                                              UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure before getting the group by
     * group name.
     *
     * @param errorCode          Error code.
     * @param errorMessage       Error message.
     * @param groupName          Group name.
     * @param requiredAttributes Requested attributes.
     * @param userStoreManager   Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onPreGetGroupByNameFailure(String errorCode, String errorMessage, String groupName,
                                       List<String> requiredAttributes, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure before getting the group id by
     * group name.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupName        Group name.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onPreGetGroupIdByNameFailure(String errorCode, String errorMessage, String groupName,
                                         UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure before getting the group list.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param condition        Conditional filter.
     * @param limit            Number of search results.
     * @param offset           Start index of the user search.
     * @param domain           Userstore domain.
     * @param sortBy           Sorted by.
     * @param sortOrder        Sorted order.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onPreListGroupsFailure(String errorCode, String errorMessage, Condition condition, int limit, int offset,
                                   String domain, String sortBy, String sortOrder, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure after getting the groups list by
     * user id.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param userId           User id.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onPostGetGroupsListByUserIdFailure(String errorCode, String errorMessage, String userId,
                                               UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure after getting the group by id.
     *
     * @param errorCode          Error code.
     * @param errorMessage       Error message.
     * @param groupID            Group id.
     * @param requiredAttributes Requested attributes.
     * @param userStoreManager   Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onPostGetGroupByIdFailure(String errorCode, String errorMessage, String groupID,
                                      List<String> requiredAttributes, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure after getting the group name by id.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupID          Group id.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onPostGetGroupNameByIdFailure(String errorCode, String errorMessage, String groupID,
                                          UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure after getting the group by group
     * name.
     *
     * @param errorCode          Error code.
     * @param errorMessage       Error message.
     * @param groupName          Group name.
     * @param requiredAttributes Requested attributes.
     * @param userStoreManager   Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onPostGetGroupByNameFailure(String errorCode, String errorMessage, String groupName,
                                        List<String> requiredAttributes, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure after getting the group id by group
     * name.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupName        Group name.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onPostGetGroupIdByNameFailure(String errorCode, String errorMessage, String groupName,
                                          UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure after getting the group list.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param condition        Conditional filter.
     * @param limit            Number of search results.
     * @param offset           Start index of the user search.
     * @param domain           Userstore domain.
     * @param sortBy           Sorted by.
     * @param sortOrder        Sorted order.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onPostListGroupsFailure(String errorCode, String errorMessage, Condition condition, int limit, int offset,
                                    String domain, String sortBy, String sortOrder, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure getting the group with group id.
     *
     * @param errorCode          Error code.
     * @param errorMessage       Error message.
     * @param groupID            Group id.
     * @param requiredAttributes Requested attributes.
     * @param userStoreManager   Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onGetGroupByIdFailure(String errorCode, String errorMessage, String groupID,
                                  List<String> requiredAttributes, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure getting the group name with group id.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupID          Group id.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onGetGroupNameByIdFailure(String errorCode, String errorMessage, String groupID,
                                      UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure getting the group with group name.
     *
     * @param errorCode          Error code.
     * @param errorMessage       Error message.
     * @param groupName          Group name.
     * @param requiredAttributes Requested attributes.
     * @param userStoreManager   Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onGetGroupByNameFailure(String errorCode, String errorMessage, String groupName,
                                    List<String> requiredAttributes, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure getting the group id by group name.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupName        Group name.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onGetGroupIdByNameFailure(String errorCode, String errorMessage, String groupName,
                                      UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure while getting the groups list by
     * user id.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param userId           User id.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onGetGroupsListByUserIdFailure(String errorCode, String errorMessage, String userId,
                                           UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure getting the group list.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param condition        Conditional filter.
     * @param limit            Number of search results.
     * @param offset           Start index of the user search.
     * @param domain           Userstore domain.
     * @param sortBy           Sorted by.
     * @param sortOrder        Sorted order.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    boolean onListGroupsFailure(String errorCode, String errorMessage, Condition condition, int limit, int offset,
                                String domain, String sortBy, String sortOrder, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions that need to be done when there is a failure while adding a group.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupName        Group name.
     * @param userIDs          User id list.
     * @param claims           Claims list.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    default boolean onAddGroupFailure(String errorCode, String errorMessage, String groupName, List<String> userIDs,
                                      List<Claim> claims, UserStoreManager userStoreManager) throws UserStoreException {

        throw new NotImplementedException("onAddGroupWithIDFailure is not implemented");
    }

    /**
     * Defines any additional actions that need to be done when there is a failure before adding a group with group id.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupName        Group name.
     * @param userIDs          User id list.
     * @param claims           Claims list.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    default boolean onPreAddGroupFailure(String errorCode, String errorMessage, String groupName, List<String> userIDs,
                                         List<Claim> claims, UserStoreManager userStoreManager)
            throws UserStoreException {

        throw new NotImplementedException("onAddGroupWithIDFailure is not implemented");
    }

    /**
     * Defines any additional actions that need to be done when there is a failure after adding a group with group id.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupName        Group name.
     * @param groupId          Group id.
     * @param userIDs          User id list.
     * @param claims           Claims list.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    default boolean onPostAddGroupFailure(String errorCode, String errorMessage, String groupName, String groupId,
                                          List<String> userIDs, List<Claim> claims, UserStoreManager userStoreManager)
            throws UserStoreException {

        throw new NotImplementedException("onAddGroupWithIDFailure is not implemented");
    }

    /**
     * Defines any additional actions that need to be done when there is a failure before deleting a group.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupId          Group id.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    default boolean onPreDeleteGroupFailure(String errorCode, String errorMessage, String groupId,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        throw new NotImplementedException("onPreDeleteGroupFailure is not implemented");
    }

    /**
     * Defines any additional actions that need to be done when there is a failure after deleting a group.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupId          Group id.
     * @param groupName        Group name.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    default boolean onPostDeleteGroupFailure(String errorCode, String errorMessage, String groupId, String groupName,
                                             UserStoreManager userStoreManager) throws UserStoreException {

        throw new NotImplementedException("onPostDeleteGroupFailure is not implemented");
    }

    /**
     * Defines any additional actions that need to be done when there is a failure while deleting a group.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupId          Group id.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    default boolean onDeleteGroupFailure(String errorCode, String errorMessage, String groupId,
                                         UserStoreManager userStoreManager) throws UserStoreException {

        throw new NotImplementedException("onDeleteGroupFailure is not implemented");
    }

    /**
     * Defines any additional actions that need to be done when there is a failure before renaming a group.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupId          Group id.
     * @param newGroupName     New group name.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    default boolean onPreRenameGroupFailure(String errorCode, String errorMessage, String groupId, String newGroupName,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        throw new NotImplementedException("onPreRenameGroupFailure is not implemented");
    }

    /**
     * Defines any additional actions that need to be done when there is a failure after renaming a group.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupId          Group id.
     * @param newGroupName     New group name.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    default boolean onPostRenameGroupFailure(String errorCode, String errorMessage, String groupId, String newGroupName,
                                             UserStoreManager userStoreManager) throws UserStoreException {

        throw new NotImplementedException("onPostRenameGroupFailure is not implemented");
    }

    /**
     * Defines any additional actions that need to be done when there is a failure while renaming a group.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupId          Group id.
     * @param newGroupName     New group name.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    default boolean onRenameGroupFailure(String errorCode, String errorMessage, String groupId, String newGroupName,
                                         UserStoreManager userStoreManager) throws UserStoreException {

        throw new NotImplementedException("onRenameGroupFailure is not implemented");
    }

    /**
     * Defines any additional actions that need to be done when there is a failure before updating user list of a group.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupId          Group id.
     * @param deletedUserIds   Deleted user ids.
     * @param newUserIds       New user ids.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    default boolean onPreUpdateUserListOfGroupFailure(String errorCode, String errorMessage, String groupId,
                                                      List<String> deletedUserIds, List<String> newUserIds,
                                                      UserStoreManager userStoreManager)
            throws UserStoreException {

        throw new NotImplementedException("onPreUpdateUserListOfGroupFailure is not implemented");
    }

    /**
     * Defines any additional actions that need to be done when there is a failure after updating user list of a group.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupId          Group id.
     * @param deletedUserIds   Deleted user ids.
     * @param newUserIds       New user ids.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    default boolean onPostUpdateUserListOfGroupFailure(String errorCode, String errorMessage, String groupId,
                                                       List<String> deletedUserIds, List<String> newUserIds,
                                                       UserStoreManager userStoreManager) throws UserStoreException {

        throw new NotImplementedException("onPostUpdateUserListOfGroupFailure is not implemented");
    }

    /**
     * Defines any additional actions that need to be done when there is a failure while updating user list of a group.
     *
     * @param errorCode        Error code.
     * @param errorMessage     Error message.
     * @param groupId          Group id.
     * @param deletedUserIds   Deleted user ids.
     * @param newUserIds       New user ids.
     * @param userStoreManager Userstore manager.
     * @return True if the handling succeeded.
     * @throws UserStoreException If an error occurred while performing the operation.
     */
    default boolean onUpdateUserListOfGroupFailure(String errorCode, String errorMessage, String groupId,
                                                   List<String> deletedUserIds, List<String> newUserIds,
                                                   UserStoreManager userStoreManager) throws UserStoreException {

        throw new NotImplementedException("onUpdateUserListOfGroupFailure is not implemented");
    }
}
