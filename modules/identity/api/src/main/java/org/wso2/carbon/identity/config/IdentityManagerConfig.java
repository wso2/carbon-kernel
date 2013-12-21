package org.wso2.carbon.user.config;

import java.util.Properties;

public final class IdentityManagerConfig extends AbstractManagerConfig {

	private IdentityStoreConfig primaryIdentityStoreConfig;

	/**
	 * 
	 * @param clazzName
	 * @param properties
	 * @param primaryIdentityStoreConfig
	 */
	public IdentityManagerConfig(String clazzName, Properties properties,
			IdentityStoreConfig primaryIdentityStoreConfig) {
		super(clazzName, properties);
		this.primaryIdentityStoreConfig = primaryIdentityStoreConfig;
	}

	/**
	 * 
	 * @return
	 */
	public IdentityStoreConfig getPrimaryIdentityStoreConfig() {
		return primaryIdentityStoreConfig;
	}

}
