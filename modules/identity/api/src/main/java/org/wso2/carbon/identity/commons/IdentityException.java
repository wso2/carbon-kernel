package org.wso2.carbon.identity.commons;

public class IdentityException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4207197252391553857L;

	/**
	 * 
	 * @param message
	 * @param e
	 */
	public IdentityException(String message, Throwable e) {
		super(message, e);
	}

	/**
	 * 
	 * @param message
	 */
	public IdentityException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param e
	 */
	public IdentityException(Throwable e) {
		super(e);
	}

}
