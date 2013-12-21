package org.wso2.carbon.identity.credential;

import org.wso2.carbon.identity.commons.IdentityException;

public class UnsupportedCredentialException extends IdentityException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6717320164562836238L;

	/**
	 * 
	 * @param message
	 * @param e
	 */
	public UnsupportedCredentialException(String message, Throwable e) {
		super(message, e);
	}

	/**
	 * 
	 * @param message
	 */
	public UnsupportedCredentialException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param e
	 */
	public UnsupportedCredentialException(Throwable e) {
		super(e);
	}

}
