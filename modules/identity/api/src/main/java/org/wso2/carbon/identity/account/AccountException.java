package org.wso2.carbon.identity.account;

import org.wso2.carbon.identity.commons.IdentityException;

public class AccountException extends IdentityException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6962690373172344042L;

	/**
	 * 
	 * @param message
	 * @param e
	 */
	public AccountException(String message, Throwable e) {
		super(message, e);
	}

	/**
	 * 
	 * @param message
	 */
	public AccountException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param e
	 */
	public AccountException(Throwable e) {
		super(e);
	}

}
