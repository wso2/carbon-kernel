package org.wso2.carbon.identity.authz;

import org.wso2.carbon.identity.commons.IdentityException;

public class AuthorizationStoreException extends IdentityException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7796791564982570357L;

	/**
	 * 
	 * @param message
	 * @param e
	 */
	public AuthorizationStoreException(String message, Throwable e) {
		super(message, e);
	}

	/**
	 * 
	 * @param message
	 */
	public AuthorizationStoreException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param e
	 */
	public AuthorizationStoreException(Throwable e) {
		super(e);
	}

}
