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

package org.wso2.carbon.identity.authz;

import java.util.Collections;
import java.util.List;

import org.wso2.carbon.identity.authn.PrivilegedRWUser;
import org.wso2.carbon.identity.authn.PrivilegedReadWriteGroup;
import org.wso2.carbon.identity.authn.spi.GroupSearchCriteria;
import org.wso2.carbon.identity.authn.spi.UserSearchCriteria;
import org.wso2.carbon.identity.authz.spi.ReadWriteAuthorizationStore;

public class PrivilegedReadWriteRole extends PrivilegedRole<PrivilegedRWUser,PrivilegedReadWriteGroup> {

	private ReadWriteAuthorizationStore authzStore;

	/**
	 * 
	 * @param authzStore
	 * @param roleIdentifier
	 * @throws AuthorizationStoreException 
	 */
	public PrivilegedReadWriteRole(RoleIdentifier roleIdentifier, ReadWriteAuthorizationStore authzStore) throws AuthorizationStoreException {
		super(roleIdentifier, authzStore);
		this.authzStore = authzStore;
	}

	/**
	 * 
	 * @param permissionIdentifierList
	 * @throws AuthorizationStoreException
	 */
	public void addPermission(List<PermissionIdentifier> permissionIdentifierList)
			throws AuthorizationStoreException {
		authzStore.assignPermissionsToRole(getRoleIdentifier(), permissionIdentifierList);
	}

	/**
	 * 
	 * @param permissionIdentifierList
	 * @throws AuthorizationStoreException
	 */
	public void removePermission(List<PermissionIdentifier> permissionIdentifierList)
			throws AuthorizationStoreException {
		authzStore.removePermissionsFromRole(getRoleIdentifier(), permissionIdentifierList);
	}

	/**
	 * 
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<PrivilegedReadWriteGroup> getGroups() throws AuthorizationStoreException {
		return authzStore.getGroupsOfRole(getRoleIdentifier());
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<PrivilegedReadWriteGroup> getGroups(GroupSearchCriteria searchCriteria)
			throws AuthorizationStoreException {
		List<PrivilegedReadWriteGroup> groups = authzStore.getGroupsOfRole(
				getRoleIdentifier(), searchCriteria);
		return Collections.unmodifiableList(groups);
	}

	/**
	 * 
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<PrivilegedRWUser> getUsers() throws AuthorizationStoreException {
		List<PrivilegedRWUser> users = authzStore
				.getUsersOfRole(getRoleIdentifier());
		return Collections.unmodifiableList(users);
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<PrivilegedRWUser> getUsers(UserSearchCriteria searchCriteria)
			throws AuthorizationStoreException {
		List<PrivilegedRWUser> users = authzStore.getUsersOfRole(
				getRoleIdentifier(), searchCriteria);
		return Collections.unmodifiableList(users);
	}

}