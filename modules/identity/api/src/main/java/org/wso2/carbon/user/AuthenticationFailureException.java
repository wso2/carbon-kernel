package org.wso2.carbon.user;

public class AuthenticationFailureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6806920324809282056L;

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
