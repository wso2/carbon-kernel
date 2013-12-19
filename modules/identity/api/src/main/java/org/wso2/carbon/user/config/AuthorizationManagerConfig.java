package org.wso2.carbon.user.config;

import java.util.Properties;

public class AuthorizationManagerConfig extends AbstractManagerConfig {

	private AuthorizationStoreConfig primaryAuthzStoreConfig;

	/**
	 * 
	 * @param clazzName
	 * @param properties
	 * @param primaryAuthzStoreConfig
	 */
	public AuthorizationManagerConfig(String clazzName, Properties properties,
			AuthorizationStoreConfig primaryAuthzStoreConfig) {
		super(clazzName, properties);
		this.primaryAuthzStoreConfig = primaryAuthzStoreConfig;
	}

	/**
	 * 
	 * @return
	 */
	public AuthorizationStoreConfig getPrimaryAuthzStoreConfig() {
		return primaryAuthzStoreConfig;
	}
}
