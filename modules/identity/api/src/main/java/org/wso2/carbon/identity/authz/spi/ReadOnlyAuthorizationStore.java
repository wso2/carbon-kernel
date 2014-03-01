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
import org.wso2.carbon.identity.authz.AuthorizationStoreException;
import org.wso2.carbon.identity.authz.Permission;
import org.wso2.carbon.identity.authz.PermissionIdentifier;
import org.wso2.carbon.identity.authz.PrivilegedRole;
import org.wso2.carbon.identity.authz.RoleIdentifier;
import org.wso2.carbon.identity.authz.VirtualReadOnlyAuthorizationStore;
import org.wso2.carbon.identity.commons.EntityTree;
import org.wso2.carbon.identity.commons.EntryIdentifier;

public interface ReadOnlyAuthorizationStore< U extends PrivilegedUser<G, R>, 
											 G extends PrivilegedGroup<U, R>, 
											 R extends PrivilegedRole<U, G> >
		extends VirtualReadOnlyAuthorizationStore<U, G, R> {

	/**
	 * 
	 * @param properties
	 */
	public void init(Properties properties);

	/**
	 * 
	 * @param userIdentifier
	 * @param roleIdentifier
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public boolean isUserHasRole(UserIdentifier userIdentifier,
			RoleIdentifier roleIdentifier) throws AuthorizationStoreException;

	/**
	 * 
	 * @param userIdentifier
	 * @param permission
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public boolean isUserHasPermission(UserIdentifier userIdentifier,
			Permission permission) throws AuthorizationStoreException;

	/**
	 * 
	 * @param groupIdentifier
	 * @param roleIdentifier
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public boolean isGroupHasRole(GroupIdentifier groupIdentifier,
			RoleIdentifier roleIdentifier) throws AuthorizationStoreException;

	/**
	 * 
	 * @param groupIdentifier
	 * @param permission
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public boolean isGroupHasPermission(GroupIdentifier groupIdentifier,
			Permission permission) throws AuthorizationStoreException;

	/**
	 * 
	 * @param userIdentifier
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<R> getRoles(UserIdentifier userIdentifier)
			throws AuthorizationStoreException;

	/**
	 * 
	 * @param userIdentifier
	 * @param Criteria
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<R> getRoles(UserIdentifier userIdentifier,
			RoleSearchCriteria Criteria) throws AuthorizationStoreException;

	/**
	 * 
	 * @param groupIdentifier
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<R> getRoles(GroupIdentifier groupIdentifier)
			throws AuthorizationStoreException;

	/**
	 * 
	 * @param groupIdentifier
	 * @param searchCriteria
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<R> getRoles(GroupIdentifier groupIdentifier,
			RoleSearchCriteria searchCriteria)
			throws AuthorizationStoreException;

	/**
	 * 
	 * @param roleIdentifier
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<G> getGroupsOfRole(RoleIdentifier roleIdentifier)
			throws AuthorizationStoreException;

	/**
	 * 
	 * @param roleIdentifier
	 * @param searchCriteria
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<G> getGroupsOfRole(RoleIdentifier roleIdentifier,
			GroupSearchCriteria searchCriteria)
			throws AuthorizationStoreException;

	/**
	 * 
	 * @param roleIdentifier
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<U> getUsersOfRole(RoleIdentifier roleIdentifier)
			throws AuthorizationStoreException;

	/**
	 * 
	 * @param roleIdentifier
	 * @param searchCriteria
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<U> getUsersOfRole(RoleIdentifier roleIdentifier,
			UserSearchCriteria searchCriteria)
			throws AuthorizationStoreException;

	/**
	 * 
	 * @param roleIdentifier
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<Permission> getPermissions(RoleIdentifier roleIdentifier)
			throws AuthorizationStoreException;

	/**
	 * 
	 * @param roleIdentifier
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<EntityTree> getChildren(RoleIdentifier roleIdentifier)
			throws AuthorizationStoreException;

	/**
	 * 
	 * @param parentRoleIdentifier
	 * @param childRoleIdentifier
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public boolean hasChild(RoleIdentifier parentRoleIdentifier,
			RoleIdentifier childRoleIdentifier)
			throws AuthorizationStoreException;

	/**
	 * 
	 * @param childRoleIdentifier
	 * @param parentRoleIdentifier
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public boolean hasParent(RoleIdentifier childRoleIdentifier,
			RoleIdentifier parentRoleIdentifier)
			throws AuthorizationStoreException;

	/**
	 * 
	 * @return
	 */
	public StoreIdentifier getStoreIdentifier();

	/**
	 * 
	 * @param roleIdentifier
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public EntryIdentifier getRoleEntryId(RoleIdentifier roleIdentifier)
			throws AuthorizationStoreException;

	/**
	 * 
	 * @param permissionIdentifier
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public EntryIdentifier getPermissionEntryId(PermissionIdentifier permissionIdentifier)
			throws AuthorizationStoreException;

}
