package org.wso2.carbon.identity;

import org.wso2.carbon.identity.authn.IdentityStoreManager;
import org.wso2.carbon.identity.authz.AuthorizationStoreManager;
import org.wso2.carbon.identity.claim.ClaimManager;
import org.wso2.carbon.identity.config.IdentityServiceConfig;

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
