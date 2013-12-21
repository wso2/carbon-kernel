package org.wso2.carbon.identity.credential;

import org.wso2.carbon.identity.commons.IdentityException;

public class InvalideCredentialException extends IdentityException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3994632579726434823L;

	/**
	 * 
	 * @param message
	 * @param e
	 */
	public InvalideCredentialException(String message, Throwable e) {
		super(message, e);
	}

	/**
	 * 
	 * @param message
	 */
	public InvalideCredentialException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param e
	 */
	public InvalideCredentialException(Throwable e) {
		super(e);
	}

}
