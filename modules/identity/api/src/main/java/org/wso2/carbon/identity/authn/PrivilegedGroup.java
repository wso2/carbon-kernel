package org.wso2.carbon.identity.authn;

import java.util.Collections;
import java.util.List;

import org.wso2.carbon.identity.authn.spi.IdentityStore;
import org.wso2.carbon.identity.authn.spi.UserSearchCriteria;
import org.wso2.carbon.identity.authz.Permission;
import org.wso2.carbon.identity.authz.PrivilegedRole;
import org.wso2.carbon.identity.authz.RoleIdentifier;
import org.wso2.carbon.identity.authz.spi.AuthorizationStore;
import org.wso2.carbon.identity.authz.spi.RoleSearchCriteria;
import org.wso2.carbon.identity.commons.EntityTree;
import org.wso2.carbon.identity.commons.EntryIdentifier;

public class PrivilegedGroup extends Group {

	private IdentityStore identityStore;
	private AuthorizationStore authzStore;

	/**
	 * 
	 * @param identityStore
	 * @param authzStore
	 * @param groupIdentifier
	 */
	public PrivilegedGroup(IdentityStore identityStore, AuthorizationStore authzStore,
			GroupIdentifier groupIdentifier) {
		super(groupIdentifier);
		this.authzStore = authzStore;
		this.identityStore = identityStore;
	}

	/**
	 * 
	 * @return
	 */
	public EntryIdentifier getEntryId() {
		return identityStore.getGroupEntryId(getIdentifier());
	}

	/**
	 * 
	 * @return
	 */
	public List<PrivilegedUser> getUsers() {
		return identityStore.getUsersInGroup(getIdentifier());
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedUser> getUsers(UserSearchCriteria searchCriteria) {
		return identityStore.getUsersInGroup(getIdentifier(), searchCriteria);
	}

	/**
	 * 
	 * @return
	 */
	public List<PrivilegedRole> getRoles() {
		List<PrivilegedRole> roles = authzStore.getRoles(getIdentifier());
		return Collections.unmodifiableList(roles);
	}

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedRole> getRoles(RoleSearchCriteria searchCriteria) {
		List<PrivilegedRole> roles = authzStore.getRoles(getIdentifier(), searchCriteria);
		return Collections.unmodifiableList(roles);
	}

	/**
	 * 
	 * @return
	 */
	public List<EntityTree> getChildren() {
		List<EntityTree> children = identityStore.getGroupChildren(getIdentifier());
		return Collections.unmodifiableList(children);
	}

	/**
	 * 
	 * @param childGroupIdentifier
	 * @return
	 */
	public boolean hasChild(GroupIdentifier childGroupIdentifier) {
		return identityStore.hasChildGroup(getIdentifier(), childGroupIdentifier);
	}

	/**
	 * 
	 * @param parentGroupIdentifier
	 * @return
	 */
	public boolean hasParent(GroupIdentifier parentGroupIdentifier) {
		return identityStore.hasParentGroup(getIdentifier(), parentGroupIdentifier);
	}

	/**
	 * 
	 * @param roleIdentifiers
	 */
	public void assignToRole(List<RoleIdentifier> roleIdentifiers) {
		authzStore.assignRoleToGroup(getIdentifier(), roleIdentifiers);
	}

	/**
	 * 
	 * @param userIdentifiers
	 */
	public void addUsers(List<UserIdentifier> userIdentifiers) {
		identityStore.addUsersToGroup(getIdentifier(), userIdentifiers);
	}

	/**
	 * 
	 * @param roleIdentifier
	 * @return
	 */
	public boolean hasRole(RoleIdentifier roleIdentifier) {
		return authzStore.isGroupHasRole(getIdentifier(), roleIdentifier);
	}

	/**
	 * 
	 * @param permission
	 * @return
	 */
	public boolean hasPermission(Permission permission) {
		return authzStore.isGroupHasPermission(getIdentifier(), permission);
	}

	/**
	 * 
	 * @return
	 */
	public StoreIdentifier getStoreIdentifier() {
		return identityStore.getStoreIdentifier();
	}

}