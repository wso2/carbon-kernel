package org.wso2.carbon.user.config;


public final class IdentityServiceConfig {

	private BootstrapRealmConfig bootstrapRealmConfig;
	private ClaimManagerConfig claimManagerConfig;
	private AuthorizationStoreManagerConfig authorizationStoreManagerConfig;
	private IdentityStoreManagerConfig identityStoreManagerConfig;

	/**
	 * 
	 * 
	 * @param bootstrapRealmConfig
	 * @param claimManagerConfig
	 * @param authorizationStoreManagerConfig
	 * @param identityStoreManagerConfig
	 */
	public IdentityServiceConfig(BootstrapRealmConfig bootstrapRealmConfig,
			ClaimManagerConfig claimManagerConfig,
			AuthorizationStoreManagerConfig authorizationStoreManagerConfig,
			IdentityStoreManagerConfig identityStoreManagerConfig) {
		this.bootstrapRealmConfig = bootstrapRealmConfig;
		this.claimManagerConfig = claimManagerConfig;
		this.authorizationStoreManagerConfig = authorizationStoreManagerConfig;
		this.identityStoreManagerConfig = identityStoreManagerConfig;
	}

	/**
	 * 
	 * @return
	 */
	public BootstrapRealmConfig getBootstrapRealmConfig() {
		return bootstrapRealmConfig;
	}

	/**
	 * 
	 * @return
	 */
	public ClaimManagerConfig getClaimManagerConfig() {
		return claimManagerConfig;
	}

	/**
	 * 
	 * @return
	 */
	public AuthorizationStoreManagerConfig getAuthorizationStoreManagerConfig() {
		return authorizationStoreManagerConfig;
	}

	/**
	 * 
	 * @return
	 */
	public IdentityStoreManagerConfig getIdentityStoreManagerConfig() {
		return identityStoreManagerConfig;
	}

}