package org.wso2.carbon.user;

import java.util.Properties;

public interface Realm {

	/**
	 * 
	 * @param identityManager
	 * @param authzManager
	 * @param properties
	 */
	public void init(IdentityManager identityManager, AuthorizationManager authzManager,
			Properties properties);

	/**
	 * 
	 * @return
	 */
	public IdentityManager getIdentityManager();

	/**
	 * 
	 * @return
	 */
	public AuthorizationManager getAuthorizationManager();
}
