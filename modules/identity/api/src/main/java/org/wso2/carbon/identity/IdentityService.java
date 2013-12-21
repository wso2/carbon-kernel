package org.wso2.carbon.user;

import org.wso2.carbon.user.config.IdentityServiceConfig;

public interface IdentityService {
	
	/**
	 * Entry point for identity service library. Builds the service 
	 * @param identityServiceConfig
	 */
	public void init(IdentityServiceConfig identityServiceConfig);

	/**
	 * 
	 * @return
	 */
	public Realm getUserRealm();

	/**
	 * 
	 * @return
	 */
	public IdentityStoreManager getIdentityStoreManager();

	/**
	 * 
	 * @return
	 */
	public AuthorizationStoreManager getAuthorizationStoreManager();

	/**
	 * 
	 * @return
	 */
	public ClaimManager getClaimManager();
}
