package org.wso2.carbon.user.core.hybrid;

import org.wso2.carbon.user.core.UserStoreException;

import java.util.List;
import java.util.Map;

public interface HybridRoleBasedManager {

    /**
     * @param roleName Domain-less role
     * @param userList Domain-aware user list
     * @throws UserStoreException
     */
    void addHybridRole(String roleName, String[] userList) throws UserStoreException;

    /**
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    boolean isExistingRole(String roleName) throws UserStoreException;

    /**
     * @param filter
     * @return
     * @throws UserStoreException
     */
    String[] getHybridRoles(String filter) throws UserStoreException;

    /**
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    String[] getUserListOfHybridRole(String roleName) throws UserStoreException;

    /**
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     * @throws UserStoreException
     */
    void updateUserListOfHybridRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException;

    /**
     * Update group list of role.
     *
     * @param roleName      Role name.
     * @param deletedGroups Deleted groups.
     * @param newGroups     New groups.
     * @throws UserStoreException UserStoreException.
     */
    void updateGroupListOfHybridRole(String roleName, String[] deletedGroups, String[] newGroups)
            throws UserStoreException;

    /**
     * Get group list of the given hybrid role.
     *
     * @param roleName Role name.
     * @return List og groups.
     * @throws UserStoreException UserStoreException.
     */
    String[] getGroupListOfHybridRole(String roleName) throws UserStoreException;

    /**
     * @param userName
     * @return
     * @throws UserStoreException
     */
    String[] getHybridRoleListOfUser(String userName, String filter) throws UserStoreException;

    /**
     * Get hybrid role list of users
     *
     * @param userNames user name list
     * @return map of hybrid role list of users
     * @throws UserStoreException userStoreException
     */
    Map<String, List<String>> getHybridRoleListOfUsers(List<String> userNames, String domainName) throws
            UserStoreException;

    /**
     * Get hybrid role list of groups.
     *
     * @param groupNames group name list.
     * @return map of hybrid role list of groups.
     * @throws UserStoreException userStoreException.
     */
    Map<String, List<String>> getHybridRoleListOfGroups(List<String> groupNames, String domainName)
            throws UserStoreException;

    /**
     * @param user
     * @param deletedRoles
     * @param addRoles
     * @throws UserStoreException
     */
    void updateHybridRoleListOfUser(String user, String[] deletedRoles, String[] addRoles)
            throws UserStoreException;

    /**
     * @param roleName
     * @throws UserStoreException
     */
    void deleteHybridRole(String roleName) throws UserStoreException;

    /**
     * @param roleName
     * @param newRoleName
     * @throws UserStoreException
     */
    void updateHybridRoleName(String roleName, String newRoleName) throws UserStoreException;

    /**
     * Get hybrid role count for the given filter.
     *
     * @param filter The domain qualified filter. If the domain is 'Internal', all the 'Application' roles are skipped.
     * @throws UserStoreException If an error occur while getting the hybrid role count using the filter.
     */
    Long countHybridRoles(String filter) throws UserStoreException;

    /**
     * ##### This method is not used anywhere
     *
     * @param userName
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    boolean isUserInRole(String userName, String roleName) throws UserStoreException;

    /**
     * If a user is added to a hybrid role, that entry should be deleted upon deletion of the user.
     *
     * @param userName
     * @throws UserStoreException
     */
    void deleteUser(String userName) throws UserStoreException;

    /**
     * Check whether the group exists in the UM_HYBRID_GROUP_ROLE table.
     *
     * @param groupName        The group name.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    boolean isGroupAssignedToHybridRoles(String groupName) throws UserStoreException;

    /**
     * Update group name in the UM_HYBRID_GROUP_ROLE table.
     *
     * @param groupName        The current group name.
     * @param newGroupName     The new group name.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    void updateGroupName(String groupName, String newGroupName) throws UserStoreException;

    /**
     * Delete group from the UM_HYBRID_GROUP_ROLE table.
     *
     * @param groupName        The group name.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    void removeGroupRoleMappingByGroupName(String groupName) throws UserStoreException;
}
