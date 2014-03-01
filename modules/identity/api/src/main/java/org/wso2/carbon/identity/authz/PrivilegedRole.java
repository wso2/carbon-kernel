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

import org.wso2.carbon.identity.authn.IdentityStoreException;
import org.wso2.carbon.identity.authn.PrivilegedGroup;
import org.wso2.carbon.identity.authn.PrivilegedUser;
import org.wso2.carbon.identity.authn.StoreIdentifier;
import org.wso2.carbon.identity.authn.spi.GroupSearchCriteria;
import org.wso2.carbon.identity.authn.spi.UserSearchCriteria;
import org.wso2.carbon.identity.authz.spi.ReadOnlyAuthorizationStore;
import org.wso2.carbon.identity.commons.EntityTree;
import org.wso2.carbon.identity.commons.EntryIdentifier;

public abstract class PrivilegedRole<U extends PrivilegedUser, G extends PrivilegedGroup>
		extends Role {

	private ReadOnlyAuthorizationStore authzStore;
	private EntryIdentifier entryIdentifier;
	// Id of the entry in AuthorizationStore for this role
	private EntryIdentifier roleEntryIdentifier;

	/**
	 * 
	 * @param authzStore
	 * @param roleIdentifier
	 */
	public PrivilegedRole(RoleIdentifier roleIdentifier,
			ReadOnlyAuthorizationStore authzStore)
			throws AuthorizationStoreException {
		super(roleIdentifier);
		this.authzStore = authzStore;
		this.roleEntryIdentifier = authzStore
				.getRoleEntryId(getRoleIdentifier());
	}

	/**
	 * 
	 * @return
	 * @throws IdentityStoreException
	 */
	public EntryIdentifier getRoleEntryId() {
		return roleEntryIdentifier;
	}

	/**
	 * 
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<Permission> getPermissions() throws AuthorizationStoreException {
		List<Permission> permission = authzStore
				.getPermissions(getRoleIdentifier());
		return Collections.unmodifiableList(permission);
	}

	/**
	 * 
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<G> getGroups() throws AuthorizationStoreException {
		return authzStore.getGroupsOfRole(getRoleIdentifier());
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<G> getGroups(GroupSearchCriteria searchCriteria)
			throws AuthorizationStoreException {
		List<G> groups = authzStore.getGroupsOfRole(getRoleIdentifier(),
				searchCriteria);
		return Collections.unmodifiableList(groups);
	}

	/**
	 * 
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<U> getUsers() throws AuthorizationStoreException {
		List<U> users = authzStore.getUsersOfRole(getRoleIdentifier());
		return Collections.unmodifiableList(users);
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<U> getUsers(UserSearchCriteria searchCriteria)
			throws AuthorizationStoreException {
		List<U> users = authzStore.getUsersOfRole(getRoleIdentifier(),
				searchCriteria);
		return Collections.unmodifiableList(users);
	}

	/**
	 * 
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public List<EntityTree> getChildren() throws AuthorizationStoreException {
		List<EntityTree> children = authzStore.getChildren(getRoleIdentifier());
		return Collections.unmodifiableList(children);
	}

	/**
	 * 
	 * @param childRoleIdentifier
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public boolean hasChild(RoleIdentifier childRoleIdentifier)
			throws AuthorizationStoreException {
		return authzStore.hasChild(getRoleIdentifier(), childRoleIdentifier);
	}

	/**
	 * 
	 * @param parentRoleIdentifier
	 * @return
	 * @throws AuthorizationStoreException
	 */
	public boolean hasParent(RoleIdentifier parentRoleIdentifier)
			throws AuthorizationStoreException {
		return authzStore.hasParent(getRoleIdentifier(), parentRoleIdentifier);
	}

	/**
	 * 
	 * @return
	 */
	public StoreIdentifier getStoreIdentifier() {
		return authzStore.getStoreIdentifier();
	}

}