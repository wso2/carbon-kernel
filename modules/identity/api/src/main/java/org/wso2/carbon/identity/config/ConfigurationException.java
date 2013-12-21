package org.wso2.carbon.identity.config;

import org.wso2.carbon.identity.commons.IdentityException;

public class ConfigurationException extends IdentityException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5420897926536954420L;

	/**
	 * 
	 * @param message
	 * @param e
	 */
	public ConfigurationException(String message, Throwable e) {
		super(message, e);
	}

	/**
	 * 
	 * @param message
	 */
	public ConfigurationException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param e
	 */
	public ConfigurationException(Throwable e) {
		super(e);
	}

}
