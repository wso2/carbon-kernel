package org.wso2.carbon.identity.authz;

import org.wso2.carbon.identity.authn.StoreIdentifier;
import org.wso2.carbon.identity.config.spi.AuthorizationStoreConfig;

public interface AuthorizationStoreManager extends ReadOnlyAuthorizationStoreManager {

	/**
	 * 
	 * @param primaryAuthzStoreConfig
	 */
	public void init(AuthorizationStoreConfig primaryAuthzStoreConfig);

	/**
	 * 
	 * @param storeConfig
	 */
	public void addAuthorizationStore(AuthorizationStoreConfig storeConfig);

	/**
	 * 
	 * @param storeIdentifier
	 */
	public void dropAuthorizationStore(StoreIdentifier storeIdentifier);

	/**
	 * 
	 * @param storeConfig
	 */
	public void updateAuthorizationStore(AuthorizationStoreConfig storeConfig);

}
