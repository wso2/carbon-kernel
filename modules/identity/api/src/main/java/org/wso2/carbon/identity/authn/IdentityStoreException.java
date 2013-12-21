package org.wso2.carbon.identity.authn;

import org.wso2.carbon.identity.commons.IdentityException;

public class IdentityStoreException extends IdentityException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3127250178993434677L;

	/**
	 * 
	 * @param message
	 * @param e
	 */
	public IdentityStoreException(String message, Throwable e) {
		super(message, e);
	}

	/**
	 * 
	 * @param message
	 */
	public IdentityStoreException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param e
	 */
	public IdentityStoreException(Throwable e) {
		super(e);
	}

}
