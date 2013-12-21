package org.wso2.carbon.identity;

import java.util.Properties;

import org.wso2.carbon.identity.authn.IdentityManager;
import org.wso2.carbon.identity.authz.AuthorizationManager;

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
