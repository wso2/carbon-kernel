package org.wso2.carbon.identity.authz;

import java.util.Collections;
import java.util.List;

public class Role {

	private RoleIdentifier roleIdentifier;
	private List<Permission> permission;

	/**
	 * 
	 * @param roleIdentifier
	 */
	public Role(RoleIdentifier roleIdentifier) {
		this.roleIdentifier = roleIdentifier;
	}

	/**
	 * 
	 * @param roleIdentifier
	 * @param permission
	 */
	public Role(RoleIdentifier roleIdentifier, List<Permission> permission) {
		this.roleIdentifier = roleIdentifier;
		this.permission = permission;
	}

	/**
	 * 
	 * @return
	 */
	public RoleIdentifier getRoleIdentifier() {
		return roleIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public List<Permission> getPermission() {
		return Collections.unmodifiableList(permission);
	}
}
