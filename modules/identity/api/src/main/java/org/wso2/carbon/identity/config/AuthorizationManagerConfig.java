package org.wso2.carbon.identity.config;

import java.util.Properties;

import org.wso2.carbon.identity.config.spi.AuthorizationStoreConfig;

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
