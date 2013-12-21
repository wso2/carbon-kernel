package org.wso2.carbon.identity.authn;

import org.wso2.carbon.identity.commons.IdentityException;

public class AuthenticationFailureException extends IdentityException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6487931400580719055L;

	/**
	 * 
	 * @param message
	 * @param e
	 */
	public AuthenticationFailureException(String message, Throwable e) {
		super(message, e);
	}

	/**
	 * 
	 * @param message
	 */
	public AuthenticationFailureException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param e
	 */
	public AuthenticationFailureException(Throwable e) {
		super(e);
	}

}
