package org.wso2.carbon.identity.config.spi;

import java.util.Properties;

import org.wso2.carbon.identity.authn.StoreIdentifier;
import org.wso2.carbon.identity.config.AbstractStoreConfig;

public final class AuthorizationStoreConfig extends AbstractStoreConfig {

	/**
	 * 
	 * @param storeClazzName
	 * @param storeIdentifier
	 * @param properties
	 */
	public AuthorizationStoreConfig(String storeClazzName, StoreIdentifier storeIdentifier,
			Properties properties) {
		super(storeClazzName, storeIdentifier, properties);
	}

}
