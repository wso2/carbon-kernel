package org.wso2.carbon.user;

import java.util.List;

import org.wso2.carbon.user.spi.AuthorizationStore;

public interface ReadOnlyAuthorizationStoreManager {

	/**
	 * 
	 * @param storeIdentifier
	 * @return
	 */
	public AuthorizationStore getAuthorizationStore(StoreIdentifier storeIdentifier);

	/**
	 * 
	 * @return
	 */
	public AuthorizationStore getPrimaryAuthorizationStore();

	/**
	 * 
	 * @return
	 */
	public List<StoreIdentifier> getAllAuthorizationStoreIdentifiers();
}
