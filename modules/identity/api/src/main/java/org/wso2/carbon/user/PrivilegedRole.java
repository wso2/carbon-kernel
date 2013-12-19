package org.wso2.carbon.user;

import java.util.Collections;
import java.util.List;

import org.wso2.carbon.user.spi.AuthorizationStore;

public class PrivilegedRole extends Role {

	private AuthorizationStore authzStore;
	private EntryIdentifier entryId;

	/**
	 * 
	 * @param authzStore
	 * @param roleIdentifier
	 */
	public PrivilegedRole(AuthorizationStore authzStore, RoleIdentifier roleIdentifier) {
		super(roleIdentifier);
		this.authzStore = authzStore;
	}

	/**
	 * 
	 * @return
	 */
	public EntryIdentifier getEntryId() {
		return entryId;
	}

	/**
	 * 
	 * @return
	 */
	public List<Permission> getPermissions() {
		List<Permission> permission = authzStore.getPermissions(getRoleIdentifier());
		return Collections.unmodifiableList(permission);
	}

	/**
	 * 
	 * @param permission
	 */
	public void addPermission(List<Permission> permission) {
		authzStore.addPermissionToRole(getRoleIdentifier(), permission);
	}

	/**
	 * 
	 * @param permission
	 */
	public void removePermission(List<Permission> permission) {
		authzStore.removePermissionFromRole(getRoleIdentifier(), permission);
	}

	/**
	 * 
	 * @return
	 */
	public List<PrivilegedGroup> getGroups() {
		return authzStore.getGroupsOfRole(getRoleIdentifier());
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedGroup> getGroups(GroupSearchCriteria searchCriteria) {
		List<PrivilegedGroup> groups = authzStore.getGroupsOfRole(getRoleIdentifier(),
				searchCriteria);
		return Collections.unmodifiableList(groups);
	}

	/**
	 * 
	 * @return
	 */
	public List<PrivilegedUser> getUsers() {
		List<PrivilegedUser> users = authzStore.getUsersOfRole(getRoleIdentifier());
		return Collections.unmodifiableList(users);
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedUser> getUsers(UserSearchCriteria searchCriteria) {
		List<PrivilegedUser> users = authzStore.getUsersOfRole(getRoleIdentifier(), searchCriteria);
		return Collections.unmodifiableList(users);
	}

	/**
	 * 
	 * @return
	 */
	public List<EntityTree> getChildren() {
		List<EntityTree> children = authzStore.getChildren(getRoleIdentifier());
		return Collections.unmodifiableList(children);
	}

	/**
	 * 
	 * @param childRoleIdentifier
	 * @return
	 */
	public boolean hasChild(RoleIdentifier childRoleIdentifier) {
		return authzStore.hasChild(getRoleIdentifier(), childRoleIdentifier);
	}

	/**
	 * 
	 * @param parentRoleIdentifier
	 * @return
	 */
	public boolean hasParent(RoleIdentifier parentRoleIdentifier) {
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