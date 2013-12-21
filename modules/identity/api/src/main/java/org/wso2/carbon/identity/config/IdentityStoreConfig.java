package org.wso2.carbon.user.config;

import java.util.Properties;

import org.wso2.carbon.user.StoreIdentifier;

public final class IdentityStoreConfig extends AbstractStoreConfig {

	/**
	 * 
	 * @param storeClazzName
	 * @param storeIdentifier
	 * @param properties
	 */
	public IdentityStoreConfig(String storeClazzName, StoreIdentifier storeIdentifier,
			Properties properties) {
		super(storeClazzName, storeIdentifier, properties);
	}

}