package org.wso2.carbon.user.config;

import java.util.Properties;

public final class BootstrapRealmConfig {

	private IdentityManagerConfig identityManagerConfig;
	private AuthorizationManagerConfig authzManagerConfig;
	private String realmClazz;
	private Properties properties;

	/**
	 * 
	 * @param primaryIdentityStoreConfig
	 * @param authzManagerConfig
	 * @param properties
	 */
	public BootstrapRealmConfig(String realmClazz, IdentityManagerConfig identityManagerConfig,
			AuthorizationManagerConfig authzManagerConfig, Properties properties) {
		this.realmClazz = realmClazz;
		this.identityManagerConfig = identityManagerConfig;
		this.authzManagerConfig = authzManagerConfig;
		this.properties = properties;
	}

	/**
	 * 
	 * @return
	 */
	public IdentityManagerConfig getIdentityManagerConfig() {
		return identityManagerConfig;
	}

	/**
	 * 
	 * @return
	 */
	public AuthorizationManagerConfig getAuthzManagerConfig() {
		return authzManagerConfig;
	}

	/**
	 * 
	 * @return
	 */
	public String getRealmClazz() {
		return realmClazz;
	}

	/**
	 * 
	 * @return
	 */
	public Properties getProperties() {
		return properties;
	}

}