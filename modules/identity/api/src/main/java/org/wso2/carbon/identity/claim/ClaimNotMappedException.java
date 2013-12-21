package org.wso2.carbon.identity.claim;

import org.wso2.carbon.identity.commons.IdentityException;

public class ClaimNotMappedException extends IdentityException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6093852280442770576L;

	/**
	 * 
	 * @param message
	 * @param e
	 */
	public ClaimNotMappedException(String message, Throwable e) {
		super(message, e);
	}

	/**
	 * 
	 * @param message
	 */
	public ClaimNotMappedException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param e
	 */
	public ClaimNotMappedException(Throwable e) {
		super(e);
	}

}
