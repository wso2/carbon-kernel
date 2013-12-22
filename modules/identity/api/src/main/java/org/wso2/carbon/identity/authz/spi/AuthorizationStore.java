/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.authz.spi;

import java.util.List;
import java.util.Properties;

import org.wso2.carbon.identity.authn.GroupIdentifier;
import org.wso2.carbon.identity.authn.PrivilegedGroup;
import org.wso2.carbon.identity.authn.PrivilegedUser;
import org.wso2.carbon.identity.authn.StoreIdentifier;
import org.wso2.carbon.identity.authn.UserIdentifier;
import org.wso2.carbon.identity.authn.spi.GroupSearchCriteria;
import org.wso2.carbon.identity.authn.spi.UserSearchCriteria;
import org.wso2.carbon.identity.authz.Permission;
import org.wso2.carbon.identity.authz.PrivilegedRole;
import org.wso2.carbon.identity.authz.RoleIdentifier;
import org.wso2.carbon.identity.authz.VirtualAuthorizationStore;
import org.wso2.carbon.identity.commons.EntityTree;

public interface AuthorizationStore extends VirtualAuthorizationStore {

	/**
	 * 
	 * @param properties
	 */
	public void init(Properties properties);

	/**
	 * 
	 * @param userIdentifier
	 * @return
	 */
	public List<PrivilegedRole> getRoles(UserIdentifier userIdentifier);

	/**
	 * 
	 * @param userIdentifier
	 * @param Criteria
	 * @return
	 */
	public List<PrivilegedRole> getRoles(UserIdentifier userIdentifier, RoleSearchCriteria Criteria);

	/**
	 * 
	 * @param userIdentifier
	 * @param roleIdentifier
	 * @return
	 */
	public boolean isUserHasRole(UserIdentifier userIdentifier, RoleIdentifier roleIdentifier);

	/**
	 * 
	 * @param userIdentifier
	 * @param permission
	 * @return
	 */
	public boolean isUserHasPermission(UserIdentifier userIdentifier, Permission permission);

	/**
	 * 
	 * @param groupIdentifier
	 * @param roleIdentifier
	 * @return
	 */
	public boolean isGroupHasRole(GroupIdentifier groupIdentifier, RoleIdentifier roleIdentifier);

	/**
	 * 
	 * @param groupIdentifier
	 * @param permission
	 * @return
	 */
	public boolean isGroupHasPermission(GroupIdentifier groupIdentifier, Permission permission);

	/**
	 * 
	 * @param userIdentifier
	 * @param roleIdentifiers
	 */
	public void assignRolesToUser(UserIdentifier userIdentifier,
			List<RoleIdentifier> roleIdentifiers);

	/**
	 * 
	 * @param userIdentifier
	 * @param roleIdentifier
	 */
	public void assignRoleToUser(UserIdentifier userIdentifier, RoleIdentifier roleIdentifier);

	/**
	 * 
	 * @param groupIdentifier
	 * @return
	 */
	public List<PrivilegedRole> getRoles(GroupIdentifier groupIdentifier);

	/**
	 * 
	 * @param groupIdentifier
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedRole> getRoles(GroupIdentifier groupIdentifier,
			RoleSearchCriteria searchCriteria);

	/**
	 * 
	 * @param groupIdentifier
	 * @param roleIdentifier
	 */
	public void assignRoleToGroup(GroupIdentifier groupIdentifier,
			List<RoleIdentifier> roleIdentifier);

	/**
	 * 
	 * @param roleIdentifier
	 * @return
	 */
	public List<Permission> getPermissions(RoleIdentifier roleIdentifier);

	/**
	 * 
	 * @param roleIdentifier
	 * @param permission
	 */
	public void addPermissionToRole(RoleIdentifier roleIdentifier, List<Permission> permission);

	/**
	 * 
	 * @param roleIdentifier
	 * @param permission
	 */
	public void removePermissionFromRole(RoleIdentifier roleIdentifier, List<Permission> permission);

	/**
	 * 
	 * @param roleIdentifier
	 * @return
	 */
	public List<PrivilegedGroup> getGroupsOfRole(RoleIdentifier roleIdentifier);

	/**
	 * 
	 * @param roleIdentifier
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedGroup> getGroupsOfRole(RoleIdentifier roleIdentifier,
			GroupSearchCriteria searchCriteria);

	/**
	 * 
	 * @param roleIdentifier
	 * @return
	 */
	public List<PrivilegedUser> getUsersOfRole(RoleIdentifier roleIdentifier);

	/**
	 * 
	 * @param roleIdentifier
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedUser> getUsersOfRole(RoleIdentifier roleIdentifier,
			UserSearchCriteria searchCriteria);

	/**
	 * 
	 * @param roleIdentifier
	 * @return
	 */
	public List<EntityTree> getChildren(RoleIdentifier roleIdentifier);

	/**
	 * 
	 * @param parentRoleIdentifier
	 * @param childRoleIdentifier
	 * @return
	 */
	public boolean hasChild(RoleIdentifier parentRoleIdentifier, RoleIdentifier childRoleIdentifier);

	/**
	 * 
	 * @param childRoleIdentifier
	 * @param parentRoleIdentifier
	 * @return
	 */
	public boolean hasParent(RoleIdentifier childRoleIdentifier, RoleIdentifier parentRoleIdentifier);

	/**
	 * 
	 * @return
	 */
	public StoreIdentifier getStoreIdentifier();

}
