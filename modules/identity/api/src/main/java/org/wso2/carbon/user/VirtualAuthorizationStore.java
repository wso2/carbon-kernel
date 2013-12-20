package org.wso2.carbon.user;

import java.util.List;

public interface VirtualAuthorizationStore {

	/**
	 * 
	 * @param permissions
	 */
	public void createPermissions(List<Permission> permissions);

	/**
	 * 
	 * @param permission
	 * @return
	 */
	public PermissionIdentifier createPermission(Permission permission);

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 */
	public List<Permission> getPermissions(PermissionSearchCriteria searchCriteria);

	/**
	 * 
	 * @param role
	 * @return
	 */
	public PrivilegedRole createRole(Role role);

	/**
	 * 
	 * @param roleIdentifier
	 * @return
	 */
	public PrivilegedRole getRole(RoleIdentifier roleIdentifier);

	/**
	 * 
	 * @param searchCriteria
	 * @return
	 */
	public List<PrivilegedRole> getRoles(RoleSearchCriteria searchCriteria);

}
