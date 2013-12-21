package org.wso2.carbon.identity.config;

import java.util.Properties;

import org.wso2.carbon.identity.authn.StoreIdentifier;

public abstract class AbstractStoreConfig extends AbstractManagerConfig {

	private StoreIdentifier storeIdentifier;

	/**
	 * 
	 * @param clazzName
	 * @param storeIdentifier
	 * @param properties
	 */
	public AbstractStoreConfig(String clazzName, StoreIdentifier storeIdentifier,
			Properties properties) {
		super(clazzName, properties);
		this.storeIdentifier = storeIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public StoreIdentifier getStoreIdentifier() {
		return storeIdentifier;
	}
}
