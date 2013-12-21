package org.wso2.carbon.identity.credential.spi;

import org.wso2.carbon.identity.authn.UserIdentifier;

public class Credential<T> {

	private UserIdentifier userIdentifier;
	private T value;

	public Credential(UserIdentifier userIdentifier) {
		this.userIdentifier = userIdentifier;
	}

	/**
	 * 
	 * @param userIdentifier
	 * @param value
	 * @param credentialType
	 */
	public Credential(UserIdentifier userIdentifier, T value) {
		this.userIdentifier = userIdentifier;
		this.value = value;
	}

	/**
	 * 
	 * @return
	 */
	public UserIdentifier getUserIdentifier() {
		return userIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public T getValue() {
		return value;
	}

}