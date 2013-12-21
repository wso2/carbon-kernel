package org.wso2.carbon.user;

public class User {

	@SuppressWarnings("rawtypes")
	private Credential credentials;
	private UserIdentifier userIdentifier;

	/**
	 * 
	 * @param userIdentifier
	 */
	public User(UserIdentifier userIdentifier) {
		this.userIdentifier = userIdentifier;
	}

	/**
	 * 
	 * @param credentials
	 */
	@SuppressWarnings("rawtypes")
	public User(Credential credentials) {
		this.credentials = credentials;
		this.userIdentifier = credentials.getUserIdentifier();
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Credential getCredentials() {
		return credentials;
	}

	/**
	 * 
	 * @return
	 */
	public UserIdentifier getUserIdentifier() {
		return userIdentifier;
	}

}