package org.wso2.carbon.identity.authz;

public class Permission {

	private String resource;
	private String action;
	private PermissionIdentifier permissionIdentifier;
	private String description;

	/**
	 * 
	 * @param resource
	 * @param action
	 * @param storeIdentifier
	 */
	public Permission(String resource, String action, PermissionIdentifier permissionIdentifier) {
		this.resource = resource;
		this.action = action;
		this.permissionIdentifier = permissionIdentifier;
	}

	/**
	 * 
	 * @param resource
	 * @param action
	 * @param storeIdentifier
	 * @param description
	 */
	public Permission(String resource, String action, PermissionIdentifier permissionIdentifier,
			String description) {
		this.resource = resource;
		this.action = action;
		this.permissionIdentifier = permissionIdentifier;
		this.description = description;
	}

	/**
	 * 
	 * @return
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * 
	 * @return
	 */
	public String getAction() {
		return action;
	}

	/**
	 * 
	 * @return
	 */
	public PermissionIdentifier getPermissionIdentifier() {
		return permissionIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

}
