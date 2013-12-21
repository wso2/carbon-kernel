package org.wso2.carbon.identity.authn;

public class Group {

	private GroupIdentifier identifier;
	private String description;

	/**
	 * 
	 * @param identifier
	 */
	public Group(GroupIdentifier identifier) {
		this.identifier = identifier;
	}

	/**
	 * 
	 * @param identifier
	 * @param description
	 */
	public Group(GroupIdentifier identifier, String description) {
		this.identifier = identifier;
		this.description = description;
	}

	/**
	 * 
	 * @return
	 */
	public GroupIdentifier getIdentifier() {
		return identifier;
	}

	/**
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

}