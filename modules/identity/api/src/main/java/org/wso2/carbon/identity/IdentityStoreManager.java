package org.wso2.carbon.user;

import org.wso2.carbon.user.config.IdentityStoreConfig;
import org.wso2.carbon.user.spi.LinkedAccountStore;
import org.wso2.carbon.user.spi.CredentialStore;

public interface IdentityStoreManager extends ReadOnlyIdentityStoreManager {

	public void init(IdentityStoreConfig primaryStoreConfig, ClaimManager claimManager,
			LinkedAccountStore linkedAccountStore, CredentialStore credentialStore);

	/**
	 * 
	 * @param storeConfig
	 */
	public void addIdentityStore(IdentityStoreConfig storeConfig);

	/**
	 * 
	 * @param storeIdentifier
	 */
	public void dropIdentityStore(StoreIdentifier storeIdentifier);

	/**
	 * 
	 * @param storeConfig
	 */
	public void updateIdentityStore(IdentityStoreConfig storeConfig);

}
