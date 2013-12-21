package org.wso2.carbon.identity.authn;

import org.wso2.carbon.identity.account.LinkedAccountStore;
import org.wso2.carbon.identity.claim.ClaimManager;
import org.wso2.carbon.identity.config.spi.IdentityStoreConfig;
import org.wso2.carbon.identity.credential.CredentialStore;

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
