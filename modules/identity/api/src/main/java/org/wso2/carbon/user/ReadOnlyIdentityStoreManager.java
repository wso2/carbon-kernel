package org.wso2.carbon.user;

import java.util.List;

import org.wso2.carbon.user.spi.IdentityStore;

public interface ReadOnlyIdentityStoreManager {

	/**
	 * 
	 * @param storeIdentifier
	 * @return
	 */
	public IdentityStore getIdentityStore(StoreIdentifier storeIdentifier);

	/**
	 * 
	 * @return
	 */
	public IdentityStore getPrimaryIdentityStore();

	/**
	 * 
	 * @return
	 */
	public List<StoreIdentifier> getAllIdentityStoreIdentifiers();

}
