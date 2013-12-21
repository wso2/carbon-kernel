package org.wso2.carbon.identity.authz;

import org.wso2.carbon.identity.commons.IdentityException;

public class AuthorizationFailureException extends IdentityException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2298412701028285738L;

	/**
	 * 
	 * @param message
	 * @param e
	 */
	public AuthorizationFailureException(String message, Throwable e) {
		super(message, e);
	}

	/**
	 * 
	 * @param message
	 */
	public AuthorizationFailureException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param e
	 */
	public AuthorizationFailureException(Throwable e) {
		super(e);
	}

}
